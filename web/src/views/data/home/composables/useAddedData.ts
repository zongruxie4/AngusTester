import { computed, inject, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { modal, notification } from '@xcan-angus/vue-ui';
import { utils, PageQuery } from '@xcan-angus/infra';
import { datasource, dataset, variable } from '@/api/tester';
import { space } from '@/api/storage';
import { useI18n } from 'vue-i18n';
import { DataMenuKey } from '@/views/data/menu';
import { AddedItem, DataType } from '@/views/data/home/types';

/**
 * Data management composable for handling CRUD operations on different data types
 * <p>
 * Provides functionality for loading, deleting, and paginating data with proper error handling
 * </p>
 */
export function useAddedData (props: {projectId: string, userId: string; type: DataType}) {
  const { t } = useI18n();
  const router = useRouter();

  // API configuration for different data types
  const loadDataApiConfig = {
    datasource: datasource.getDataSourceList,
    space: space.getSpaceList,
    dataset: dataset.getDataSetList,
    variable: variable.getVariablesList
  };

  const delDataApiConfig = {
    datasource: datasource.deleteDataSource,
    space: space.deleteSpace,
    dataset: dataset.deleteDataSet,
    variable: variable.deleteVariables
  };

  // Reactive state
  const tableData = ref<AddedItem[]>();
  const loading = ref(false);
  const loaded = ref(false);
  const orderBy = ref<string>();
  const orderSort = ref<PageQuery.OrderSort>();

  // Pagination configuration
  const pagination = ref({
    total: 0,
    current: 1,
    pageSize: 5,
    showSizeChanger: false,
    size: 'small' as const,
    showTotal: (total: number) => {
      const totalPage = Math.ceil(total / pagination.value.pageSize);
      return t('pagination.pageInfo', {
        current: pagination.value.current,
        totalPage
      });
    }
  });

  const appInfo = inject('appInfo', ref({ code: '' }));
  const updateRefreshNotify = inject<(value: string) => void>('updateRefreshNotify');

  /**
   * Load data from API based on current pagination and sorting parameters
   */
  const loadData = async () => {
    loading.value = true;
    const { current, pageSize } = pagination.value;

    const params: { [key: string]: any; pageNo: number; pageSize: number } = {
      projectId: props.projectId,
      pageNo: current,
      pageSize,
      createdBy: props.userId,
      appCode: appInfo.value.code
    };

    if (orderSort.value) {
      params.orderBy = orderBy.value;
      params.orderSort = orderSort.value;
    }

    const [error, { data = {} }] = await loadDataApiConfig[props.type](params);
    loading.value = false;
    if (error) {
      return;
    }

    tableData.value = data.list || [];
    pagination.value.total = +data.total || 0;
    loaded.value = true;
  };

  /**
   * Handle table change events including pagination and sorting
   */
  const handleTableChange = ({
    current = 1,
    pageSize = 5
  }, _filters, sorter: { orderBy: string; orderSort: PageQuery.OrderSort; }) => {
    orderBy.value = sorter.orderBy;
    orderSort.value = sorter.orderSort;
    pagination.value.current = current;
    pagination.value.pageSize = pageSize;
    loadData();
  };

  /**
   * Delete item with confirmation modal
   */
  const deleteItem = (data: AddedItem) => {
    modal.confirm({
      content: t('actions.tips.confirmDelete', { name: data.name }),
      async onOk () {
        let error: Error | null = null;
        if (props.type === 'datasource' || props.type === 'space') {
          [error] = await delDataApiConfig[props.type](data.id);
        } else {
          [error] = await delDataApiConfig[props.type]([data.id]);
        }

        if (error) {
          return;
        }
        notification.success(t('actions.tips.deleteSuccess'));

        // Trigger refresh notifications
        if (typeof updateRefreshNotify === 'function') {
          updateRefreshNotify(utils.uuid());
        }
      }
    });
  };

  /**
   * Navigation functions for creating new items
   */
  const navigateToCreate = {
    variable: () => router.push(`/data#${DataMenuKey.VARIABLES}`),
    dataset: () => router.push(`/data#${DataMenuKey.DATASET}`)
  };

  // Computed properties
  const total = computed(() => pagination.value.total);
  const isEmpty = computed(() => !tableData.value?.length);

  return {
    // State
    tableData,
    loading,
    loaded,
    pagination,
    total,
    isEmpty,

    // Methods
    loadData,
    handleTableChange,
    deleteItem,
    navigateToCreate
  };
}
