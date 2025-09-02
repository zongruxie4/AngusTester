import { ref, computed } from 'vue';
import { MockAPIConfig, HttpMethod } from '../types';
import { utils } from '@xcan-angus/infra';

/**
 * Composable for managing Mock API form state and operations
 * Handles form data, validation, and temporary data storage
 */
export function useMockApiForm() {
  // Form state
  const mockAPIConfig = ref<MockAPIConfig>();
  const mockAPIId = ref<string>();
  const readonly = ref(false);

  // Form fields
  const summary = ref<string>('');
  const summaryError = ref(false);
  const method = ref<HttpMethod>('GET');
  const endpoint = ref<string>('');
  const description = ref<string>('');

  // Temporary data storage for unsaved changes
  const tempMockApiMap = ref<{
    [key: string]: {
      api: {
        isTempFlag: boolean;
        summary: string;
        method: HttpMethod;
        endpoint: string;
        description: string;
      };
      response: {
        responseIdList: string[];
        responseMap: { [key: string]: any };
        nameMap: { [key: string]: string | undefined };
        openKeys: string[];
        priorityMap: { [key: string]: string };
        enablePushbackSet: Set<string>;
      }
    }
  }>({});

  // Computed properties
  const isSavedMockApi = computed(() => {
    return mockAPIConfig.value?.id && !mockAPIConfig.value?.isTempFlag;
  });

  /**
   * Set form data from API configuration
   * @param data - Mock API configuration data
   */
  const setApiForm = (data: MockAPIConfig) => {
    const {
      id,
      summary: _summary,
      method: _method,
      endpoint: _endpoint,
      description: _description
    } = data;
    
    summary.value = _summary;
    method.value = _method;
    endpoint.value = _endpoint;
    description.value = _description;
    mockAPIId.value = id;
    mockAPIConfig.value = data;
    summaryError.value = false;
  };

  /**
   * Store current form data as temporary data
   * @param responseData - Response configuration data
   */
  const storeTempMockApiData = (responseData: {
    responseIdList: string[];
    responseMap: { [key: string]: any };
    nameMap: { [key: string]: string | undefined };
    openKeys: string[];
    priorityMap: { [key: string]: string };
    enablePushbackSet: Set<string>;
  }) => {
    const prevApiId = mockAPIId.value;
    if (!prevApiId) {
      return;
    }

    tempMockApiMap.value[prevApiId] = {
      api: {
        isTempFlag: !!mockAPIConfig.value?.isTempFlag,
        summary: summary.value,
        method: method.value,
        endpoint: endpoint.value,
        description: description.value
      },
      response: {
        responseMap: JSON.parse(JSON.stringify(responseData.responseMap)),
        enablePushbackSet: new Set(responseData.enablePushbackSet),
        nameMap: JSON.parse(JSON.stringify(responseData.nameMap)),
        openKeys: JSON.parse(JSON.stringify(responseData.openKeys)),
        priorityMap: JSON.parse(JSON.stringify(responseData.priorityMap)),
        responseIdList: JSON.parse(JSON.stringify(responseData.responseIdList))
      }
    };
  };

  /**
   * Restore form data from temporary storage
   * @param id - API ID to restore
   */
  const restoreTempMockApiData = (id: string) => {
    const prevMockApiData = tempMockApiMap.value[id];
    if (!prevMockApiData) {
      return false;
    }

    const { api, response } = prevMockApiData;
    
    summary.value = api.summary;
    method.value = api.method;
    endpoint.value = api.endpoint;
    description.value = api.description;
    
    mockAPIId.value = id;
    mockAPIConfig.value = {
      id,
      isTempFlag: api.isTempFlag,
      summary: api.summary,
      method: api.method,
      endpoint: api.endpoint,
      description: api.description,
      mockServiceId: ''
    };

    return { response };
  };

  /**
   * Reset form to initial state
   */
  const resetMockApi = () => {
    mockAPIConfig.value = undefined;
    mockAPIId.value = '';
    readonly.value = false;
    summary.value = '';
    summaryError.value = false;
    method.value = 'GET';
    endpoint.value = '';
    description.value = '';
  };

  /**
   * Validate form data
   * @returns True if form is valid, false otherwise
   */
  const isValid = (): boolean => {
    if (!summary.value) {
      summaryError.value = true;
      return false;
    }
    return true;
  };

  /**
   * Get API parameters for save/update operations
   * @param mockServiceId - Service ID to associate with
   * @returns Array of API parameters
   */
  const getAPIParams = (mockServiceId: string) => {
    const _params: {
      description: string;
      endpoint: string;
      method: HttpMethod;
      mockServiceId: string;
      summary: string;
      id?: string;
    } = {
      description: description.value,
      endpoint: endpoint.value,
      method: method.value,
      mockServiceId,
      summary: summary.value
    };

    if (mockAPIId.value) {
      _params.id = mockAPIId.value;
    }

    return [_params];
  };

  // Form field change handlers
  const summaryChange = (event: { target: { value: string } }) => {
    summary.value = event.target.value;
    summaryError.value = false;
  };

  const descriptionChange = (event: { target: { value: string } }) => {
    description.value = event.target.value;
  };

  return {
    // State
    mockAPIConfig,
    mockAPIId,
    readonly,
    summary,
    summaryError,
    method,
    endpoint,
    description,
    tempMockApiMap,
    isSavedMockApi,

    // Methods
    setApiForm,
    storeTempMockApiData,
    restoreTempMockApiData,
    resetMockApi,
    isValid,
    getAPIParams,
    summaryChange,
    descriptionChange
  };
}
