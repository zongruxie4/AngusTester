import { computed, ref, watch, onMounted } from 'vue';
import { debounce } from 'throttle-debounce';
import { dataset } from '@/api/tester';
import { utils, duration } from '@xcan-angus/infra';
import { useI18n } from 'vue-i18n';
import { DataSetDetail, PreviewPagination, PreviewColumn } from '../../types';

/**
 * Composable for managing preview data logic in dataset preview component
 * Handles data loading, pagination, and error handling for dataset previews
 */
export function usePreviewData (props: { dataSource?: DataSetDetail }) {
  const { t } = useI18n();

  // Reactive references for component state
  const rowNum = ref<string>('20');
  const pagination = ref<PreviewPagination>({
    current: 1,
    pageSize: 10,
    total: 0,
    showSizeChanger: false
  });
  const loading = ref(false);
  const loaded = ref(false);
  const errorMessage = ref<string>();
  const columns = ref<PreviewColumn[]>([]);
  const dataList = ref<any>([]);

  /**
   * Computed property for no data text
   * Returns error message if exists, otherwise returns default no data text
   */
  const noDataText = computed(() => {
    return errorMessage.value ? errorMessage.value : t('common.noData');
  });

  /**
   * Debounced input change handler for row number input
   * Triggers data reload when user changes the number of rows to preview
   */
  const handleRowNumChange = debounce(duration.search, (event: { target: { value: string; } }) => {
    const value = event.target.value;
    rowNum.value = value || '20';
    loadData();
  });

  /**
   * Refresh preview data
   * Reloads data with current parameters
   */
  const refresh = () => {
    if (loading.value) {
      return;
    }
    loadData();
  };

  /**
   * Load preview data from API
   * Fetches dataset preview data based on current configuration
   */
  const loadData = async () => {
    if (!props.dataSource || loading.value) {
      return;
    }

    const params = {
      ...props.dataSource,
      rowNum: rowNum.value
    };

    loading.value = true;
    const [error, res] = await dataset.previewDataSetValue(params, { silence: true });
    loading.value = false;
    loaded.value = true;

    // Reset data
    columns.value = [];
    dataList.value = [];
    pagination.value.total = 0;

    if (error) {
      errorMessage.value = error.message;
      return;
    }

    errorMessage.value = undefined;

    const data = res?.data;
    if (data) {
      const entries = Object.entries(data);
      entries.every(([key, value]) => {
        columns.value.push({ key, dataIndex: key, title: key, ellipsis: true });
        if (Array.isArray(value)) {
          const newValue = value as string[];
          newValue.every((item, index) => {
            if (dataList.value[index]) {
              dataList.value[index][key] = item;
            } else {
              dataList.value[index] = {
                id: utils.uuid(),
                [key]: item
              };
            }
            return true;
          });
        }
        return true;
      });

      pagination.value.total = dataList.value.length;
    }
  };

  /**
   * Reset component state
   * Clears all data and resets to initial state
   */
  const reset = () => {
    rowNum.value = '20';
    pagination.value.total = 0;
    loading.value = false;
    loaded.value = false;
    errorMessage.value = undefined;
    columns.value = [];
    dataList.value = [];
  };

  // Watch for changes in data source and reinitialize
  onMounted(() => {
    watch(() => props.dataSource, (newValue) => {
      reset();

      if (!newValue) {
        return;
      }

      loadData();
    }, { immediate: true });
  });

  return {
    // State
    rowNum,
    pagination,
    loading,
    loaded,
    errorMessage,
    columns,
    dataList,

    // Computed properties
    noDataText,

    // Methods
    handleRowNumChange,
    refresh,
    loadData,
    reset
  };
}
