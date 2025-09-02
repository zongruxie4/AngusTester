/**
 * Composable for managing API list functionality
 * Handles loading and searching APIs for service association
 */
import { computed, ref, watch } from 'vue';
import { apis } from '@/api/tester';
import { ApiItem } from '../types';

/**
 * Composable for managing API list functionality
 * @param projectId - Current project ID
 * @param serviceId - Selected service ID
 * @param checkedIds - Initially checked API IDs
 * @returns API list data and loading function
 */
export function useApiList (projectId: string, serviceId: string, checkedIds: string[]) {
  // Reactive state
  const name = ref('');
  const dataList = ref<ApiItem[]>([]); // All APIs
  const checkedList = ref<string[]>([...checkedIds]); // Currently selected APIs

  // Search parameters
  const params = computed(() => {
    if (name.value) {
      return { filters: [{ key: 'summary', op: 'MATCH_END', value: name.value }] };
    }
    return { filters: [] };
  });

  /**
   * Load API list for the selected service
   */
  const loadList = async (): Promise<void> => {
    const [error, { data }] = await apis.getApiList({
      ...params.value,
      pageSize: 2000,
      pageNo: 1,
      serviceId: serviceId,
      projectId: projectId
    });

    if (error) {
      return;
    }

    checkedList.value = [];
    dataList.value = data?.list || [];
  };

  /**
   * Handle select all/none checkbox change
   * @param checked - Whether all items should be selected
   */
  const handleCheckAllChange = (checked: boolean): void => {
    if (checked) {
      checkedList.value = dataList.value.map(m => m.id);
    } else {
      checkedList.value = [];
    }
  };

  /**
   * Update checked list with new values
   * @param newCheckedIds - New checked API IDs
   */
  const updateCheckedList = (newCheckedIds: string[]): void => {
    checkedList.value = [...new Set([...newCheckedIds, ...checkedIds])];
  };

  // Watch for changes in projectId, serviceId, or search params
  watch([projectId, serviceId, params], () => {
    if (projectId && serviceId) {
      loadList();
    }
  }, {
    immediate: true
  });

  // Watch for changes in checkedIds prop
  watch(checkedIds, (newValue) => {
    checkedList.value = newValue;
  }, {
    deep: true
  });

  return {
    name,
    dataList,
    checkedList,
    loadList,
    handleCheckAllChange,
    updateCheckedList
  };
}
