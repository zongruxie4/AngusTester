import { useI18n } from 'vue-i18n';
import { computed, onMounted, ref, watch } from 'vue';
import { isEqual } from 'lodash-es';
import { variable } from '@/api/tester';
import { notification } from '@xcan-angus/vue-ui';
import { StaticVariableFormState, VariableItem, DetailTabKey, ButtonGroupAction } from '../types';

/**
 * Composable for managing static variable logic in variable detail components
 * Handles data management, API calls, and form validation for static variables
 */
export function useStaticVariable (
  props: {
    projectId: string;
    dataSource?: VariableItem;
  },
  emit: (event: string, ...args: any[]) => void
) {
  const { t } = useI18n();

  // Reactive references for component state
  const confirmLoading = ref(false);
  const activeKey = ref<DetailTabKey>('value');
  const passwordValue = ref(false);
  const variableName = ref<string>('');
  const variableNameError = ref(false);
  const description = ref<string>('');
  const variableValue = ref<string>('');
  const previewData = ref<{
    name: string;
    value: string;
  }>();

  /**
   * Computed property for variable ID
   */
  const variableId = computed(() => {
    return props.dataSource?.id || '';
  });

  /**
   * Computed property to determine if we're in edit mode
   */
  const editFlag = computed(() => {
    return !!props.dataSource?.id;
  });

  /**
   * Computed property to determine if the OK button should be disabled
   */
  const okButtonDisabled = computed(() => {
    return !variableName.value;
  });

  /**
   * Handle variable name change event
   */
  const nameChange = () => {
    variableNameError.value = false;
  };

  /**
   * Handle variable name blur event
   */
  const nameBlur = (event: { target: { value: string; } }) => {
    const name = event.target.value;
    if (!name) {
      return;
    }

    validName(name);
  };

  /**
   * Validate variable name
   * @param name - The name to validate
   * @returns boolean indicating if name is valid
   */
  const validName = (name: string) => {
    // eslint-disable-next-line prefer-regex-literals
    const rex = new RegExp(/[^a-zA-Z0-9!$%^&*_\-+=./]/);
    if (rex.test(name)) {
      variableNameError.value = true;
      return false;
    }

    return true;
  };

  /**
   * Handle button group click events
   */
  const buttonGroupClick = (key: ButtonGroupAction) => {
    if (key === 'ok') {
      ok();
      return;
    }

    if (key === 'delete') {
      emit('delete', variableId.value);
      return;
    }

    if (key === 'export') {
      emit('export', variableId.value);
      return;
    }

    if (key === 'clone') {
      emit('clone', variableId.value);
      return;
    }

    if (key === 'copyLink') {
      emit('copyLink', variableId.value);
      return;
    }

    if (key === 'refresh') {
      emit('refresh', variableId.value);
    }
  };

  /**
   * Save variable (either create or update)
   */
  const ok = async () => {
    if (!validName(variableName.value)) {
      return;
    }

    if (editFlag.value) {
      toEdit();
      return;
    }

    toCreate();
  };

  /**
   * Update existing variable
   */
  const toEdit = async () => {
    const params = getParams();
    confirmLoading.value = true;
    const [error] = await variable.putVariables(params);
    confirmLoading.value = false;
    if (error) {
      return;
    }

    notification.success(t('actions.tips.editSuccess'));
    emit('ok', params, true);
  };

  /**
   * Create new variable
   */
  const toCreate = async () => {
    const params = getParams();
    confirmLoading.value = true;
    const [error, res] = await variable.addVariables(params);
    confirmLoading.value = false;
    if (error) {
      return;
    }

    notification.success(t('actions.tips.addSuccess'));
    const id = res?.data?.id;
    emit('ok', { ...params, id }, false);
  };

  /**
   * Get parameters for API call
   * @returns StaticVariableFormState object
   */
  const getParams = (): StaticVariableFormState => {
    const params: StaticVariableFormState = {
      projectId: props.projectId,
      name: variableName.value,
      value: variableValue.value,
      passwordValue: passwordValue.value,
      description: description.value
    };

    const id = variableId.value;
    if (id) {
      params.id = id;
    }

    return params;
  };

  /**
   * Initialize form with data from props
   */
  const initialize = () => {
    const data = props.dataSource;
    if (!data) {
      return;
    }

    passwordValue.value = data.passwordValue;
    variableName.value = data.name;
    variableValue.value = data.value;
    description.value = data.description;
  };

  /**
   * Update preview data when active tab changes to preview
   */
  const updatePreviewData = () => {
    const newData = {
      name: variableName.value,
      value: variableValue.value
    };

    if (!isEqual(newData, previewData.value)) {
      previewData.value = newData;
    }
  };

  // Initialize component when mounted
  onMounted(() => {
    watch(() => props.dataSource, (newValue) => {
      if (!newValue) {
        return;
      }

      initialize();
    }, { immediate: true });

    watch(() => activeKey.value, (newValue) => {
      if (newValue !== 'preview') {
        return;
      }

      updatePreviewData();
    }, { immediate: true });
  });

  return {
    // State
    confirmLoading,
    activeKey,
    passwordValue,
    variableName,
    variableNameError,
    description,
    variableValue,
    previewData,

    // Computed properties
    variableId,
    editFlag,
    okButtonDisabled,

    // Methods
    nameChange,
    nameBlur,
    buttonGroupClick,
    initialize,
    updatePreviewData
  };
}
