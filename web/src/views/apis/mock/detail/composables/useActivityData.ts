import { onMounted, ref } from 'vue';
import { debounce } from 'throttle-debounce';
import { duration, PageQuery, SearchCriteria } from '@xcan-angus/infra';
import { activity } from '@/api/tester';
import { ActivityItem } from '@/types/types';
import { CombinedTargetType } from '@/enums/enums';

/**
 * Encapsulate activity list data loading, pagination and search behaviors.
 * <p>
 * Provide a stable view-model aligned with `ActivityInfo` component typing.
 */
export function useActivityData (initialTargetId?: string) {
  // Pagination and filter parameters sent to the backend
  const params = ref<PageQuery>({
    pageNo: 1,
    pageSize: 20,
    filters: [
      { key: 'targetType', value: CombinedTargetType.MOCK_SERVICE, op: SearchCriteria.OpEnum.Equal },
      { key: 'targetId', value: initialTargetId ?? '', op: SearchCriteria.OpEnum.Equal }
    ]
  });

  // Total record count for pagination
  const total = ref(0);

  // Data source for ActivityInfo
  const activities = ref<ActivityItem[]>([]);

  // Local search input binding
  const detailKeyword = ref('');

  // Loading state to avoid duplicate requests
  const loading = ref(false);

  /**
   * Fetch activity list by current `params`.
   * <p>
   * Also normalizes each item to ensure `avatar` is a non-empty string.
   */
  const loadActivities = async () => {
    if (loading.value) return;
    loading.value = true;

    try {
      const [error, { data = { list: [], total: 0 } }] = await activity.getActivityList(params.value);
      loading.value = false;

      if (error) {
        console.error('Failed to load activities:', error);
        activities.value = [];
        total.value = 0;
        return;
      }

      // Ensure data.list is an array before processing
      const listData = Array.isArray(data.list) ? data.list : [];

      // Normalize to satisfy ActivityInfo's ActiveObj typing
      activities.value = listData.map((item: any) => ({
        id: String(item.id ?? ''),
        optDate: String(item.optDate ?? ''),
        targetId: String(item.targetId ?? ''),
        targetType: String(item.targetType ?? ''),
        title: String(item.title ?? ''),
        fullName: String(item.fullName ?? ''),
        description: String(item.description ?? ''),
        detail: String(item.detail ?? ''),
        details: Array.isArray(item.details) ? item.details : [],
        // Ensure non-empty string to match library typing (string, not string | undefined)
        avatar: item.avatar ? String(item.avatar) : ''
      }));
      total.value = Number(data.total ?? 0);
    } catch (err) {
      loading.value = false;
      console.error('Error loading activities:', err);
      activities.value = [];
      total.value = 0;
    }
  };

  /**
   * Debounced handler to update search filter and reload list.
   */
  const onSearchChange = debounce(duration.search, (value: string) => {
    const filtersWithoutDetail = params.value.filters.filter(f => f.key !== 'detail');
    if (value) {
      params.value.filters = [
        ...filtersWithoutDetail,
        { key: 'detail', op: SearchCriteria.OpEnum.Match, value }
      ];
    } else {
      params.value.filters = filtersWithoutDetail;
    }
    params.value.pageNo = 1;
    loadActivities();
  });

  /**
   * Pagination change handler
   */
  const onPageChange = (pageNo: number, pageSize: number) => {
    params.value.pageNo = pageNo;
    params.value.pageSize = pageSize;
    loadActivities();
  };

  onMounted(() => {
    loadActivities();
  });

  return {
    // state
    params,
    total,
    activities,
    detailKeyword,
    loading,
    // actions
    loadActivities,
    onSearchChange,
    onPageChange
  };
}
