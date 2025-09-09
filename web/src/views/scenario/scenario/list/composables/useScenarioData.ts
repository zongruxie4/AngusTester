import { onMounted, Ref, ref, watch } from 'vue';
import { PageQuery, ProjectPageQuery, SearchCriteria } from '@xcan-angus/infra';
import { scenario } from '@/api/tester';
import { ScenarioInfo, MenuItem } from '../types';
import { ScenarioPermission } from '@/enums/enums';

/**
 * Sort keys for scenarios
 */
export type SortKey = 'createdDate' | 'name' | 'createdByName';

/**
 * Composable for managing scenario data
 * @param projectId - The project ID to fetch scenarios for
 * @param notify - Notification trigger
 */
export function useScenarioData (projectId: Ref<string | undefined>, notify: Ref<string | undefined>) {

  const loaded = ref(false);
  const loading = ref(false);
  const errorMessage = ref<string>();
  const dataList = ref<ScenarioInfo[]>([]);

  /**
   * Load scenario data from API
   * @param filters - Filter criteria
   * @param orderBy - Field to order by
   * @param orderSort - Sort order
   */
  const loadData = async (
    filters?: SearchCriteria[],
    orderBy?: SortKey,
    orderSort?: PageQuery.OrderSort
  ): Promise<void> => {
    // Skip if no project ID
    if (!projectId.value) {
      return;
    }

    const params: ProjectPageQuery = {
      projectId: projectId.value,
      pageNo: 1,
      pageSize: 2000,
      infoScope: PageQuery.InfoScope.DETAIL
    };

    // Add filters if provided
    if (filters?.length) {
      params.filters = filters;
    }

    // Add sorting if provided
    if (orderBy) {
      params.orderBy = orderBy;
    }

    if (orderSort) {
      params.orderSort = orderSort;
    }

    loading.value = true;
    const [error, res] = await scenario.getScenarioList(params);
    loaded.value = true;
    loading.value = false;
    errorMessage.value = undefined;

    if (error) {
      dataList.value = [];
      errorMessage.value = error.message;
      return;
    }

    const data = res?.data || { total: 0, list: [] };
    dataList.value = data.list.map((item) => {
      return {
        ...item,
        nameLinkUrl: `/scenario#scenario?id=${item.id}&name=${item.name}&plugin=${item.plugin}`,
        detailLink: `/scenario#scenario?id=${item.id}&name=${item.name}&plugin=${item.plugin}&type=detail`
      };
    });
  };

  // Watch for project ID changes
  onMounted(() => {
    watch(() => projectId.value, (newValue) => {
      if (!newValue) {
        return;
      }
      loadData();
    }, { immediate: true });

    watch(() => notify.value, (newValue) => {
      if (!newValue) {
        return;
      }
      loadData();
    });
  });

  return {
    // State
    loaded,
    loading,
    errorMessage,
    dataList,

    // Methods
    loadData,
  };
}
