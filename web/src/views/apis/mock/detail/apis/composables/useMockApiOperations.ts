import { ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { notification } from '@xcan-angus/vue-ui';
import { mock } from '@/api/tester';
import { MockAPIConfig, ResponseConfig } from '../types';

/**
 * Composable for Mock API CRUD operations
 * Handles create, read, update, delete operations for Mock APIs
 */
export function useMockApiOperations() {
  const { t } = useI18n();
  const loading = ref(false);

  /**
   * Create new Mock API
   * @param params - API parameters for creation
   * @returns Promise with creation result
   */
  const createMockApi = async (params: any[]) => {
    loading.value = true;
    const [error, { data }] = await mock.addMockApi(params);
    loading.value = false;
    
    if (error) {
      return { error, data: null };
    }
    
    return { error: null, data };
  };

  /**
   * Update existing Mock API
   * @param params - API parameters for update
   * @returns Promise with update result
   */
  const updateMockApi = async (params: any[]) => {
    loading.value = true;
    const [error] = await mock.updateMockApi(params);
    loading.value = false;
    
    return { error };
  };

  /**
   * Delete Mock API by ID
   * @param id - API ID to delete
   * @returns Promise with deletion result
   */
  const deleteMockApi = async (id: string) => {
    loading.value = true;
    const [error] = await mock.deleteMockApi({ ids: [id] });
    loading.value = false;
    
    if (error) {
      return { error };
    }
    
    notification.success(t('mock.mockApis.notifications.deleteSuccess'));
    return { error: null };
  };

  /**
   * Clone Mock API by ID
   * @param id - API ID to clone
   * @returns Promise with cloning result
   */
  const cloneMockApi = async (id: string) => {
    loading.value = true;
    const [error] = await mock.cloneMockApi(id);
    loading.value = false;
    
    if (error) {
      return { error };
    }
    
    notification.success(t('mock.mockApis.notifications.cloneSuccess'));
    return { error: null };
  };

  /**
   * Copy API to Mock service
   * @param mockServiceId - Target mock service ID
   * @param apiId - Source API ID
   * @returns Promise with copy result
   */
  const copyApiToMock = async (mockServiceId: string, apiId: string) => {
    loading.value = true;
    const [error, { data }] = await mock.copyApiToMock(mockServiceId, apiId);
    loading.value = false;
    
    if (error) {
      return { error, data: null };
    }
    
    notification.success(t('mock.mockApis.notifications.copyApiSuccess'));
    return { error: null, data };
  };

  /**
   * Associate API with Mock service
   * @param mockServiceId - Target mock service ID
   * @param apiId - Source API ID
   * @returns Promise with association result
   */
  const associateApiToMock = async (mockServiceId: string, apiId: string) => {
    loading.value = true;
    const [error, { data }] = await mock.assocApiToMock(mockServiceId, apiId);
    loading.value = false;
    
    if (error) {
      return { error, data: null };
    }
    
    notification.success(t('mock.mockApis.notifications.linkApiSuccess'));
    return { error: null, data };
  };

  /**
   * Import demo Mock APIs
   * @param mockServiceId - Target mock service ID
   * @returns Promise with import result
   */
  const importDemoMockApi = async (mockServiceId: string) => {
    loading.value = true;
    const [error] = await mock.importDemoMockApi(mockServiceId);
    loading.value = false;
    
    if (error) {
      return { error };
    }
    
    notification.success(t('mock.mockApis.notifications.importDemoSuccess'));
    return { error: null };
  };

  /**
   * Get Mock API detail by ID
   * @param id - API ID to fetch
   * @returns Promise with API detail
   */
  const getMockApiDetail = async (id: string) => {
    loading.value = true;
    const [error, { data }] = await mock.getMockApiDetail(id);
    loading.value = false;
    
    return { error, data };
  };

  /**
   * Get Mock API response configurations
   * @param id - API ID to fetch responses for
   * @returns Promise with response configurations
   */
  const getMockApiResponse = async (id: string) => {
    loading.value = true;
    const [error, { data }] = await mock.getMockApiResponse(id);
    loading.value = false;
    
    return { error, data };
  };

  /**
   * Add Mock API response configurations
   * @param id - API ID to add responses to
   * @param params - Response configuration parameters
   * @returns Promise with add result
   */
  const addMockApiResponse = async (id: string, params: ResponseConfig[]) => {
    loading.value = true;
    const [error] = await mock.addMockApiResponse(id, params);
    loading.value = false;
    
    return { error };
  };

  /**
   * Update Mock API response configurations
   * @param id - API ID to update responses for
   * @param params - Response configuration parameters
   * @returns Promise with update result
   */
  const updateMockApiResponse = async (id: string, params: ResponseConfig[]) => {
    loading.value = true;
    const [error] = await mock.updateMockApiResponse(id, params);
    loading.value = false;
    
    return { error };
  };

  /**
   * Sync API instance configuration
   * @param id - API ID to sync
   * @returns Promise with sync result
   */
  const syncApiInstanceConfig = async (id: string) => {
    loading.value = true;
    const [error] = await mock.syncApiInstanceConfig(id);
    loading.value = false;
    
    if (error) {
      return { error };
    }
    
    notification.success(t('mock.mockApis.notifications.refreshInstanceSuccess'));
    return { error: null };
  };

  return {
    loading,
    createMockApi,
    updateMockApi,
    deleteMockApi,
    cloneMockApi,
    copyApiToMock,
    associateApiToMock,
    importDemoMockApi,
    getMockApiDetail,
    getMockApiResponse,
    addMockApiResponse,
    updateMockApiResponse,
    syncApiInstanceConfig
  };
}
