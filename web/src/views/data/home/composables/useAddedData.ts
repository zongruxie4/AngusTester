import { computed, inject, ref } from 'vue';
import { useRouter } from 'vue-router';
import { modal, notification } from '@xcan-angus/vue-ui';
import { utils } from '@xcan-angus/infra';
import { dataSource, dataSet, variable } from '@/api/tester';
import { space } from '@/api/storage';
import { useI18n } from 'vue-i18n';

import { AddedItem, DataType } from '@/views/data/home/types';

/**
 * <p>
 * Data management composable for handling CRUD operations on different data types
 * </p>
 * <p>
 * Provides functionality for loading, deleting, and paginating data with proper error handling
 * </p>
 */
export function useAddedData (projectId: string, userId: string, type: DataType) {
  const { t } = useI18n();
  const router = useRouter();

  // API configuration for different data types
  const loadDataApiConfig = {
    dataSource: dataSource.getDataSourceList,
    space: space.getSpaceList,
    dataSet: dataSet.getDataSetList,
    variable: variable.getVariablesList
  };

  const delDataApiConfig = {
    dataSource: dataSource.deleteDataSource,
    space: space.deleteSpace,
    dataSet: dataSet.deleteDataSet,
    variable: variable.deleteVariables
  };

  // Reactive state
  const tableData = ref<AddedItem[]>();
  const loading = ref(false);
  const loaded = ref(false);
  const orderBy = ref<string>();
  const orderSort = ref<'ASC' | 'DESC'>();

  // Pagination configuration
  const pagination = ref({
    total: 0,
    current: 1,
    pageSize: 5,
    showSizeChanger: false,
    size: 'small' as const,
    showTotal: (total: number) => {
      const totalPage = Math.ceil(total / pagination.value.pageSize);
      return t('dataHome.summaryTable.pagination.pageInfo', {
        current: pagination.value.current,
        totalPage
      });
    }
  });

  const appInfo = inject('appInfo', ref({ code: '' }));
  const updateRefreshNotify = inject<(value: string) => void>('updateRefreshNotify');

  /**
   * <p>
   * Load data from API based on current pagination and sorting parameters
   * </p>
   */
  const loadData = async () => {
    loading.value = true;
    const { current, pageSize } = pagination.value;

    const params: {
      projectId: string;
      pageNo: number;
      pageSize: number;
      createdBy?: string;
      orderBy?: string;
      orderSort?: string;
      appCode?: string;
    } = {
      projectId,
      pageNo: current,
      pageSize,
      createdBy: userId,
      appCode: appInfo.value.code
    };

    if (orderSort.value) {
      params.orderBy = orderBy.value;
      params.orderSort = orderSort.value;
    }

    const [error, { data = {} }] = await loadDataApiConfig[type](params);
    loading.value = false;
    if (error) {
      return;
    }

    tableData.value = data.list || [];
    pagination.value.total = +data.total || 0;
    loaded.value = true;
  };

  /**
   * <p>
   * Handle table change events including pagination and sorting
   * </p>
   */
  const handleTableChange = ({
    current = 1,
    pageSize = 10
  }, _filters, sorter: { orderBy: string; orderSort: 'ASC' | 'DESC'; }) => {
    orderBy.value = sorter.orderBy;
    orderSort.value = sorter.orderSort;
    pagination.value.current = current;
    pagination.value.pageSize = pageSize;
    loadData();
  };

  /**
   * <p>
   * Delete item with confirmation modal
   * </p>
   */
  const deleteItem = (data: AddedItem) => {
    modal.confirm({
      content: t('dataHome.summaryTable.messages.deleteConfirm', { name: data.name }),
      async onOk () {
        const [error] = await delDataApiConfig[type]([data.id]);
        if (error) {
          return;
        }
        notification.success(t('tips.deleteSuccess'));

        // Trigger refresh notifications
        if (typeof updateRefreshNotify === 'function') {
          updateRefreshNotify(utils.uuid());
        }
      }
    });
  };

  /**
   * <p>
   * Navigation functions for creating new items
   * </p>
   */
  const navigateToCreate = {
    variable: () => router.push('/data#variables'),
    dataSet: () => router.push('/data#dataSet')
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
