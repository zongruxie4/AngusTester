import { ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { notification } from '@xcan-angus/vue-ui';
import { debounce } from 'throttle-debounce';
import { appContext, duration } from '@xcan-angus/infra';
import { project } from '@/api/tester';
import { getCurrentPage } from '@/utils/utils';
import { TrashItem } from '../types';

/**
 * <p>Composable for managing trash data operations including CRUD operations,</p>
 * <p>pagination, search, and sorting functionality.</p>
 */
export function useTrashData (projectId: string, userInfo: { id: string }) {
  const { t } = useI18n();

  // Reactive state for data management
  const loading = ref(false);
  const loaded = ref(false);
  const tableData = ref<TrashItem[]>([]);

  // Search and filter state
  const inputValue = ref<string>();
  const orderBy = ref<string>();
  const orderSort = ref<'ASC' | 'DESC'>();

  // Pagination state
  const pagination = ref<{ total: number; current: number; pageSize: number; }>({
    total: 0,
    current: 1,
    pageSize: 10
  });

  // Injected dependencies
  const isAdmin = appContext.isAdmin();

  /**
   * <p>Recovers a single item from trash by calling the backend API.</p>
   * <p>Updates pagination and refreshes the list after successful recovery.</p>
   *
   * @param data - The trash item to recover
   */
  const backTrash = async (data: TrashItem) => {
    loading.value = true;
    const [error] = await project.backTrash(data.id);
    if (error) {
      loading.value = false;
      return;
    }

    notification.success(t('projectTrash.messages.recoverSuccess'));
    pagination.value.current = getCurrentPage(
      pagination.value.current,
      pagination.value.pageSize,
      pagination.value.total
    );
    await getList();
  };

  /**
   * <p>Permanently deletes a single item from trash.</p>
   * <p>Updates pagination and refreshes the list after successful deletion.</p>
   *
   * @param data - The trash item to delete
   */
  const deleteTrash = async (data: TrashItem) => {
    loading.value = true;
    const [error] = await project.deleteTrash(data.id);
    if (error) {
      loading.value = false;
      return;
    }

    notification.success(t('projectTrash.messages.deleteSuccess'));
    pagination.value.current = getCurrentPage(
      pagination.value.current,
      pagination.value.pageSize,
      pagination.value.total
    );
    await getList();
  };

  /**
   * <p>Recovers all items in the current project's trash.</p>
   * <p>Resets pagination to first page and refreshes the list.</p>
   */
  const backAll = async () => {
    loading.value = true;
    const params = { projectId };
    const [error] = await project.backAllTrash(params);
    if (error) {
      loading.value = false;
      return;
    }
    notification.success(t('projectTrash.messages.recoverAllSuccess'));
    pagination.value.current = 1;
    await getList();
  };

  /**
   * <p>Permanently deletes all items in the current project's trash.</p>
   * <p>Resets pagination to first page and refreshes the list.</p>
   */
  const deleteAll = async () => {
    loading.value = true;
    const params = { projectId };
    const [error] = await project.deleteAllTrash(params);
    if (error) {
      loading.value = false;
      return;
    }

    notification.success(t('projectTrash.messages.deleteAllSuccess'));
    pagination.value.current = 1;
    await getList();
  };

  /**
   * <p>Fetches trash list from the backend with current filters and pagination.</p>
   * <p>Processes the response to set disabled state based on user permissions.</p>
   */
  const getList = async () => {
    loading.value = true;
    const params: {
      projectId: string;
      pageNo: number;
      pageSize: number;
      targetName?: string;
      orderBy?: string;
      orderSort?: string;
    } = {
      projectId,
      pageNo: pagination.value.current,
      pageSize: pagination.value.pageSize
    };

    // Add search filter if input value exists
    if (inputValue.value) {
      params.targetName = inputValue.value;
    }

    // Add sorting parameters if they exist
    if (orderSort.value) {
      params.orderBy = orderBy.value;
      params.orderSort = orderSort.value;
    }

    const [error, res] = await project.getTrashList(params);
    loaded.value = true;
    loading.value = false;
    if (error) {
      return;
    }

    const data = res?.data || { list: [], total: 0 };
    const userId = userInfo?.id;

    // Process items to set disabled state based on user permissions
    tableData.value = data.list.map(item => {
      item.disabled = true;
      if (isAdmin || userId === item.createdBy || userId === item.deletedBy) {
        item.disabled = false;
      }
      return item;
    });

    pagination.value.total = +(data.total || 0);
  };

  /**
   * <p>Refreshes the trash list by resetting to first page.</p>
   */
  const toRefresh = () => {
    pagination.value.current = 1;
    getList();
  };

  /**
   * <p>Debounced search function that triggers list refresh.</p>
   * <p>Resets pagination to first page when search criteria changes.</p>
   */
  const inputChange = debounce(duration.search, () => {
    pagination.value.current = 1;
    getList();
  });

  /**
   * <p>Handles table changes including pagination, sorting, and filtering.</p>
   * <p>Updates internal state and triggers list refresh.</p>
   *
   * @param paginationInfo - Pagination change information
   * @param filters - Filter information (unused)
   * @param sorter - Sorting information
   */
  const tableChange = (
    { current = 1, pageSize = 10 },
    _filters,
    sorter: { orderBy: string; orderSort: 'ASC' | 'DESC'; }
  ) => {
    orderBy.value = sorter.orderBy;
    orderSort.value = sorter.orderSort;
    pagination.value.current = current;
    pagination.value.pageSize = pageSize;
    getList();
  };

  /**
   * <p>Watches for project ID changes and refreshes the list.</p>
   * <p>Resets pagination to first page when project changes.</p>
   */
  const watchProjectId = () => {
    watch(() => projectId, () => {
      pagination.value.current = 1;
      getList();
    }, { immediate: true });
  };

  /**
   * <p>Watches for notification changes and refreshes the list.</p>
   * <p>Resets pagination to first page when notifications are received.</p>
   */
  const watchNotify = (notify: string) => {
    watch(() => notify, (newValue) => {
      if (newValue === undefined || newValue === null || newValue === '') {
        return;
      }

      pagination.value.current = 1;
      getList();
    }, { immediate: true });
  };

  return {
    // State
    loading,
    loaded,
    tableData,
    inputValue,
    orderBy,
    orderSort,
    pagination,

    // Methods
    backTrash,
    deleteTrash,
    backAll,
    deleteAll,
    getList,
    toRefresh,
    inputChange,
    tableChange,
    watchProjectId,
    watchNotify
  };
}
