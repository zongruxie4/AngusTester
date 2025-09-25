import { computed, inject, ref } from 'vue';
import { modal, notification } from '@xcan-angus/vue-ui';
import { PageQuery, SearchCriteria, appContext, toClipboard } from '@xcan-angus/infra';
import { report } from '@/api/tester';
import { getCurrentPage } from '@/utils/utils';
import { useI18n } from 'vue-i18n';
import type {
  OrderByKey,
  PaginationConfig,
  ProjectInfo,
  Report,
  UseReportDataReturn
} from '../types';

/**
 * Composable for managing report data operations
 * Handles data fetching, CRUD operations, and state management
 */
export function useReportData () {
  const { t } = useI18n();

  // Reactive state
  const dataList = ref<Report[]>([]);
  const loading = ref(false);
  const pagination = ref<PaginationConfig>({
    current: 1,
    pageSize: 10,
    total: 0
  });
  const filters = ref<SearchCriteria[]>([]);
  const category = ref<string>();
  const selectId = ref<string | undefined>();
  const selectReportPermissions = ref<string[]>([]);
  const generateLoading = ref<Record<string, boolean>>({});

  // Injected dependencies
  const projectInfo = inject<ProjectInfo>('projectInfo', ref({ id: '', type: { value: '' } }));
  const appInfo = ref(appContext.getAccessApp());
  const userInfo = ref(appContext.getUser());

  // Computed properties
  const projectId = computed(() => projectInfo.value?.id);

  /**
   * Load report list with current filters and pagination
   */
  const loadDataList = async () => {
    if (loading.value || !projectId.value) {
      return;
    }

    const { current, pageSize } = pagination.value;
    const params: {
      pageNo: number;
      pageSize: number;
      projectId: string;
      filters?: SearchCriteria[];
      orderBy?: OrderByKey;
      orderSort?: PageQuery.OrderSort;
    } = {
      pageNo: current,
      pageSize,
      projectId: projectId.value,
      filters: []
    };

    // Apply filters
    if (filters.value?.length) {
      params.filters = filters.value;
    }
    if (category.value) {
      params.filters?.push({ key: 'category', op: 'EQUAL', value: category.value });
    }

    loading.value = true;
    const [error, { data = {} }] = await report.getReportList(params);
    loading.value = false;

    if (error) {
      return;
    }

    // Process data
    dataList.value = (data.list || []).map(i => ({
      ...i,
      currentAuths: i.currentAuths.map(auth => auth.value)
    }));
    pagination.value.total = +data.total || 0;

    // Handle empty results
    if (dataList.value.length === 0 && pagination.value.current !== 1) {
      pagination.value.current = 1;
      loadDataList();
    }
  };

  /**
   * Delete a report with confirmation
   */
  const deleteReport = (report: Report) => {
    modal.confirm({
      content: t('actions.tips.confirmDelete', { name: report.name }),
      onOk () {
        return report.deleteReport([report.id]).then((resp) => {
          const [error] = resp;
          if (error) {
            return;
          }

          pagination.value.current = getCurrentPage(
            pagination.value.current,
            pagination.value.pageSize,
            pagination.value.total
          );
          loadDataList();
          notification.success(t('actions.tips.deleteSuccess'));
        });
      }
    });
  };

  /**
   * Get share token for a report
   */
  const getShareToken = async (report: Report) => {
    const [error, { data }] = await report.getReportShareToken(report.id);
    if (error) {
      notification.error(t('reportHome.messages.getTokenFailed'));
      return;
    }
    const url = data;
    await toClipboard(url);
    notification.success(t('reportHome.messages.shareLinkCopied'));
  };

  /**
   * Generate report
   */
  const generateReport = async (report: Report) => {
    generateLoading.value[report.id] = true;
    const [error] = await report.generateReport(report.id);
    generateLoading.value[report.id] = false;
    if (error) {
      return;
    }
    notification.success(t('reportHome.messages.generatingReport'));
    loadDataList();
  };

  /**
   * Open add/edit report modal
   */
  const addReport = (reportId?: string) => {
    if (reportId) {
      selectId.value = reportId;
    } else {
      selectId.value = undefined;
    }
  };

  /**
   * Open view report modal
   */
  const viewReport = (report?: Report) => {
    if (report) {
      selectId.value = report.id;
      selectReportPermissions.value = report.currentAuths || [];
    } else {
      selectId.value = undefined;
      selectReportPermissions.value = [];
    }
  };

  /**
   * Handle auth flag change
   */
  const authFlagChange = async ({ auth }: { auth: boolean }) => {
    const _list = dataList.value;
    const targetId = selectId.value;
    for (let i = 0, len = _list.length; i < len; i++) {
      if (_list[i].id === targetId) {
        _list[i].auth = auth;
        break;
      }
    }
  };

  /**
   * Handle search panel change
   */
  const searchPanelChange = (data: SearchCriteria[]) => {
    filters.value = data;
    pagination.value.current = 1;
    loadDataList();
  };

  /**
   * Handle category change
   */
  const categoryChange = (value: string) => {
    category.value = value;
    pagination.value.current = 1;
    loadDataList();
  };

  /**
   * Handle pagination change
   */
  const onPageChange = (page: any, _: any, sortData: any) => {
    pagination.value.current = page.current;
    pagination.value.pageSize = page.pageSize;
    loadDataList();
  };

  /**
   * Handle sort change
   */
  const toSort = (data: { orderBy: OrderByKey; orderSort: PageQuery.OrderSort }) => {
    pagination.value.current = 1;
    loadDataList();
  };

  return {
    // State
    dataList,
    loading,
    pagination,
    filters,
    category,
    selectId,
    selectReportPermissions,
    generateLoading,
    projectId,
    appInfo,
    userInfo,

    // Methods
    loadDataList,
    deleteReport,
    getShareToken,
    generateReport,
    addReport,
    viewReport,
    authFlagChange,
    searchPanelChange,
    categoryChange,
    onPageChange,
    toSort
  } as UseReportDataReturn;
}
