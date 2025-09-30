import { useI18n } from 'vue-i18n';
import { computed, onMounted, ref, watch } from 'vue';
import { isEqual } from 'lodash-es';
import { variable, apis } from '@/api/tester';
import { notification } from '@xcan-angus/vue-ui';
import { ExtractionMethod, ExtractionSource, HttpExtractionLocation } from '@xcan-angus/infra';
import { HttpVariableFormState, DetailTabKey, ButtonGroupAction } from '../types';
import { VariableItem } from '../../types';

/**
 * Composable for managing HTTP variable logic in variable detail components
 * Handles data management, API calls, and form validation for HTTP-based variables
 */
export function useHttpVariable (
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

  const variableName = ref<string>('');
  const variableNameError = ref(false);
  const description = ref<string>('');

  // Extraction configuration fields
  const method = ref<ExtractionMethod>(ExtractionMethod.EXACT_VALUE);
  const location = ref<HttpExtractionLocation>(HttpExtractionLocation.RESPONSE_BODY);
  const parameterName = ref<string>('');
  const defaultValue = ref<string>('');
  const expression = ref<string>('');
  const matchItem = ref<string>('');

  // Modal visibility
  const selectApiVisible = ref(false);

  // Request configuration
  const requestConfigs = ref<{ url: string; }>({
    url: ''
  });

  const previewData = ref<{
    name: string;
    extraction: {
      source: ExtractionSource;
      method: ExtractionMethod;
      expression: string;
      defaultValue: string;
      location: HttpExtractionLocation;
      matchItem: string;
      parameterName: string;
      request: { url: string; };
    };
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
    let disabled = !variableName.value;

    if (!disabled) {
      // For body locations, expression is required for non-exact methods
      if ([HttpExtractionLocation.REQUEST_RAW_BODY, HttpExtractionLocation.RESPONSE_BODY].includes(location.value)) {
        if ([ExtractionMethod.JSON_PATH, ExtractionMethod.REGEX, ExtractionMethod.X_PATH].includes(method.value)) {
          disabled = !expression.value;
        }
      } else {
        // For other locations, parameter name is always required
        disabled = !parameterName.value;

        // For non-exact methods, expression is also required
        if (!disabled && [ExtractionMethod.JSON_PATH, ExtractionMethod.REGEX, ExtractionMethod.X_PATH].includes(method.value)) {
          disabled = !expression.value;
        }
      }
    }

    return disabled;
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

    if (key === 'cancel') {
      emit('cancel');
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
   * Open API selection modal
   */
  const toSelectApi = () => {
    selectApiVisible.value = true;
  };

  /**
   * Handle API selection confirmation
   */
  const selectApiOk = async (ids: string[]) => {
    selectApiVisible.value = false;
    const [error, res] = await apis.getApiDetail(ids[0], true);
    if (error) {
      return;
    }

    const data = res?.data;
    if (!data) {
      return;
    }

    requestConfigs.value = await requestConfigs(data);
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
   * @returns HttpVariableFormState object
   */
  const getParams = (): HttpVariableFormState => {
    const params: HttpVariableFormState = {
      projectId: props.projectId,
      name: variableName.value,
      description: description.value,
      passwordValue: false,
      extraction: {
        source: ExtractionSource.HTTP,
        method: method.value,
        expression: expression.value,
        defaultValue: defaultValue.value,
        location: location.value,
        matchItem: matchItem.value,
        parameterName: parameterName.value,
        request: requestConfigs.value
      }
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

    variableName.value = data.name;
    description.value = data.description;

    // Initialize extraction configuration
    method.value = data.extraction.method.value;
    location.value = data.extraction.location;
    parameterName.value = data.extraction.parameterName;
    defaultValue.value = data.extraction.defaultValue;
    expression.value = data.extraction.expression;
    matchItem.value = data.extraction.matchItem;

    // Initialize request configuration
    requestConfigs.value = data.extraction.request;
  };

  /**
   * Update preview data when active tab changes to preview
   */
  const updatePreviewData = () => {
    const newData = {
      name: variableName.value,
      extraction: {
        source: ExtractionSource.HTTP,
        method: method.value,
        expression: expression.value,
        defaultValue: defaultValue.value,
        location: location.value,
        matchItem: matchItem.value,
        parameterName: parameterName.value,
        request: requestConfigs.value
      }
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
    variableName,
    variableNameError,
    description,
    previewData,

    // HTTP configuration fields
    method,
    location,
    parameterName,
    defaultValue,
    expression,
    matchItem,
    selectApiVisible,
    requestConfigs,

    // Computed properties
    variableId,
    editFlag,
    okButtonDisabled,

    // Methods
    nameChange,
    nameBlur,
    buttonGroupClick,
    toSelectApi,
    selectApiOk,
    initialize,
    updatePreviewData
  };
}
