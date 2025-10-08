import { computed, inject, ref, Ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRoute, useRouter } from 'vue-router';
import { notification } from '@xcan-angus/vue-ui';
import { ScriptType } from '@xcan-angus/infra';

import { angusScript } from '@/api/tester';
import { exec } from '@/api/ctrl';
import { ProjectInfo } from '@/layout/types';

export interface FormState {
  name: string;
  scriptId: string | undefined;
  scriptType: string | undefined;
}

export interface ScriptInfo {
  type: string;
  plugin?: any;
  configuration?: any;
  task?: any;
}

/**
 * Composable for managing execution form functionality
 * Handles form validation, script selection, and form submission
 */
export function useExecutionForm () {
  const { t } = useI18n();
  const router = useRouter();
  const route = useRoute();

  // Inject project information
  const projectInfo = inject<Ref<ProjectInfo>>('projectInfo', ref({} as ProjectInfo));
  const projectId = computed(() => projectInfo.value?.id);

  // Form state
  const formState = ref<FormState>({
    name: '',
    scriptId: undefined,
    scriptType: undefined
  });

  // Form references
  const infoFormRef = ref();
  const execSettingFormRef = ref();

  // Loading state
  const isLoading = ref(false);

  // Script information
  const scriptInfo = ref<ScriptInfo>();
  const pluginType = ref();

  // Form validation rules
  const formRules = ref<Record<string, boolean | undefined>>({
    name: undefined,
    scriptId: undefined
  });

  /**
   * Handle script selection and load script details
   */
  const selectScript = async (value: any) => {
    const scriptId = typeof value === 'string' ? value : String(value);
    if (!scriptId) return;

    const [error, { data }] = await angusScript.getDetail(scriptId);
    if (error) {
      return;
    }

    scriptInfo.value = {
      ...data.content,
      type: data.content.type.value
    };

    formState.value.scriptId = scriptId;
    formState.value.scriptType = data.type.value;
    infoFormRef.value?.clearValidate();
    pluginType.value = data.type;

    if (!formState.value.name) {
      formState.value.name = data.name;
    }
  };

  /**
   * Handle form validation
   */
  const validateForm = (name: string | number | string[] | number[], status: boolean) => {
    const fieldName = Array.isArray(name) ? name.join('.') : String(name);
    formRules.value[fieldName] = status;
  };

  /**
   * Scroll to error element in form
   */
  const scrollToErrorElement = (formName: string, errors: any) => {
    const formElement = document.querySelector(`.${formName}`);
    if (!formElement || !errors[formName]?.errorFields?.length) {
      return;
    }

    const errEleClass = errors[formName].errorFields[0].name.join('-');
    const errorElement = formElement.querySelector(`.${errEleClass}`);
    if (!errorElement) {
      return;
    }

    errorElement.scrollIntoView({
      block: 'center',
      inline: 'start'
    });
  };

  /**
   * Validate all forms and return validation result
   */
  const validateAllForms = async () => {
    let hasError = true;

    // Validate info form
    await infoFormRef.value?.validate().then().catch((errors: any) => {
      if (errors?.errorFields?.length) {
        scrollToErrorElement('info-form', { 'info-form': errors });
        hasError = false;
      }
    });

    // Validate execution setting form
    const execFormData = await execSettingFormRef.value?.isValid();
    if (hasError) {
      hasError = execFormData?.valid || false;
    }

    // Handle execution setting form errors
    const errors = execFormData?.errors || {};
    const formNames = ['execut-form', 'global-form', 'http-form', 'websocket-form', 'jdbc-form'];

    for (const formName of formNames) {
      if (errors[formName]?.errorFields?.length) {
        switch (formName) {
          case 'execut-form':
            execSettingFormRef.value?.openExecutParames();
            break;
          case 'global-form':
            execSettingFormRef.value?.openGlobalParames();
            break;
          case 'http-form':
          case 'websocket-form':
          case 'jdbc-form':
            execSettingFormRef.value?.openPulginParames();
            break;
        }
        scrollToErrorElement(formName, errors);
        break;
      }
    }

    return hasError;
  };

  /**
   * Save execution settings
   */
  const saveSettings = async () => {
    const isValid = await validateAllForms();
    if (!isValid) {
      return;
    }

    let params: any = {
      name: formState.value.name,
      scriptType: formState.value.scriptType,
      ...execSettingFormRef.value?.getData()
    };

    // Add script ID for new executions
    if (!route.params.id) {
      params = {
        ...params,
        scriptId: formState.value.scriptId
      };
    }

    // Add trial flag for expression type
    const queryType = route.query?.type;
    if (queryType === 'expr') {
      params = {
        ...params,
        trial: true
      };
    }

    isLoading.value = true;

    try {
      const [error] = route.params.id
        ? await exec.putScriptConfig(route.params.id as string, params)
        : await exec.addByScript(params);

      if (error) {
        return;
      }

      notification.success(
        route.params.id ? t('actions.tips.updateSuccess') : t('execution.add.addSuccess')
      );

      await router.push('/execution');
    } finally {
      isLoading.value = false;
    }
  };

  /**
   * Load execution detail for editing
   */
  const loadExecutionDetail = async () => {
    if (isLoading.value || !route.params.id) {
      return;
    }

    isLoading.value = true;

    try {
      const executionId = route.params.id as string;
      if (!executionId) {
        isLoading.value = false;
        return;
      }

      const [error, { data }] = await exec.getDetail(executionId);
      if (error) {
        return;
      }

      // Process script info
      if (data?.scriptType?.value === ScriptType.MOCK_DATA) {
        data.batchRows = data.task?.mockData?.settings.batchRows || '1';
      }

      const { configuration, plugin, task, scriptId, scriptType } = data;
      scriptInfo.value = {
        type: scriptType.value,
        plugin,
        configuration,
        task
      };

      formState.value.name = data.name;
      formState.value.scriptId = scriptId;
      formState.value.scriptType = scriptType.value;
    } finally {
      isLoading.value = false;
    }
  };

  /**
   * Get script parameters for API call
   */
  const scriptParams = {
    filters: [{
      key: 'type',
      op: 'IN',
      value: [
        ScriptType.TEST_FUNCTIONALITY, ScriptType.TEST_PERFORMANCE,
        ScriptType.TEST_STABILITY, ScriptType.TEST_CUSTOMIZATION,
        ScriptType.MOCK_DATA
      ]
    }]
  };

  /**
   * Get disabled script type keys based on current selection
   */
  const disabledScriptTypeKeys = computed(() => {
    const scriptType = scriptInfo.value?.type;
    if (scriptType === ScriptType.MOCK_DATA) {
      return [
        ScriptType.TEST_FUNCTIONALITY, ScriptType.TEST_PERFORMANCE,
        ScriptType.TEST_STABILITY, ScriptType.TEST_CUSTOMIZATION
      ];
    }
    return [ScriptType.MOCK_DATA];
  });

  /**
   * Check if script type should be excluded
   */
  const shouldExcludeScriptType = (option: any) => {
    return option.value === ScriptType.MOCK_APIS;
  };

  return {
    // State
    projectId,
    formState,
    scriptInfo,
    pluginType,
    isLoading,
    formRules,
    scriptParams,
    disabledScriptTypeKeys,

    // Refs
    infoFormRef,
    execSettingFormRef,

    // Methods
    selectScript,
    validateForm,
    saveSettings,
    loadExecutionDetail,
    shouldExcludeScriptType
  };
}
