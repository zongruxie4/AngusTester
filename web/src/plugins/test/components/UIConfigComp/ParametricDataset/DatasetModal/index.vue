<script setup lang="ts">
import { ref, onMounted, watch, computed } from 'vue';
import { useI18n } from 'vue-i18n';
import { Modal, NoData, Table, Icon, Input, Spin } from '@xcan-angus/vue-ui';
import { Button } from 'ant-design-vue';
import { duration } from '@xcan-angus/infra';
import { debounce } from 'throttle-debounce';
import { dataset } from '@/api/tester';

import { DatasetItem } from './PropsType';

// Initialize i18n for internationalization
const { t } = useI18n();

/**
 * Sort column key type
 */
type OrderByKey = 'lastModifiedDate' | 'lastModifiedByName';

/**
 * Sort direction type
 */
type OrderSortKey = 'ASC' | 'DESC';

/**
 * Component props interface
 */
type Props = {
  projectId: string;       // Project identifier for filtering datasets
  visible: boolean;        // Modal visibility state
  selectedNames: string[]; // Already selected dataset names (will be disabled)
}

// Define props with default values
const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  visible: false,
  selectedNames: () => []
});

/**
 * Event emitters
 */
const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void;  // Update modal visibility
  (e: 'ok', value: DatasetItem[]): void;        // Emit selected datasets
}>();

/**
 * State Management
 */
const loaded = ref(false);                      // Whether data has been loaded at least once
const loading = ref(false);                     // Loading state during API calls
const searchValue = ref<string>();              // Search input value
const orderBy = ref<OrderByKey>();              // Current sort column
const orderSort = ref<OrderSortKey>();          // Current sort direction

/**
 * Pagination configuration
 */
const pagination = ref<{ 
  current: number; 
  pageSize: number; 
  total: number; 
  showSizeChanger: boolean;
}>({ 
  current: 1,             // Current page number
  pageSize: 10,           // Items per page
  total: 0,               // Total items count
  showSizeChanger: false  // Hide page size changer
});

/**
 * Row selection configuration
 */
const rowSelection = ref<{
  onChange: (key: string[]) => void;
  getCheckboxProps: (data: DatasetItem) => ({ disabled: boolean; });
  selectedRowKeys: string[];
}>();

/**
 * Selected datasets map (for preserving selection across pages)
 */
const selectedDataMap = ref<Map<string, DatasetItem>>(new Map());

/**
 * Current page table data
 */
const tableData = ref<DatasetItem[]>([]);

/**
 * Handle search input change (debounced)
 * Resets to first page and reloads data
 * 
 * @param event - Input change event
 */
const searchInputChange = debounce(duration.search, (event: any) => {
  searchValue.value = event.target?.value;
  pagination.value.current = 1;
  loadData();
});

/**
 * Refresh table data
 * Resets to first page and reloads
 */
const toRefresh = (): void => {
  pagination.value.current = 1;
  loadData();
};

/**
 * Load dataset list from API
 * Builds query parameters, fetches data, and processes source type labels
 */
const loadData = async (): Promise<void> => {
  // Build query parameters
  const params: {
    projectId: string;
    pageNo: number;
    pageSize: number;
    name?: string;
    orderBy?: OrderByKey;
    orderSort?: OrderSortKey;
  } = {
    projectId: props.projectId,
    pageNo: pagination.value.current,
    pageSize: pagination.value.pageSize
  };

  // Add sorting if specified
  if (orderSort.value) {
    params.orderBy = orderBy.value;
    params.orderSort = orderSort.value;
  }

  // Add name filter if search value exists
  if (searchValue.value?.length) {
    params.name = searchValue.value;
  }

  // Fetch data from API
  loading.value = true;
  const [error, res] = await dataset.getDataSetList(params);
  loaded.value = true;
  loading.value = false;

  // Handle error
  if (error) {
    pagination.value.total = 0;
    tableData.value = [];
    return;
  }

  // Process response data
  const data = res?.data || { total: 0, list: [] };
  if (data) {
    pagination.value.total = +data.total;
    const _list = data.list as DatasetItem[];
    tableData.value = [];

    const names = props.selectedNames;
    
    // Process each dataset item
    _list.forEach((item) => {
      const { extracted, extraction, name, id, createdByName } = item;
      
      // Determine source type label
      if (!extraction || !['FILE', 'HTTP', 'JDBC'].includes(extraction.source)) {
        // Static value (no extraction)
        item.source = t('commonPlugin.parametricDataset.sourceTypes.static');
      } else {
        const { source } = extraction;
        if (!extracted) {
          // Exact value (with source type)
          item.source = t('commonPlugin.parametricDataset.sourceTypes.exact');
          if (source === 'FILE') {
            item.source += ` (${t('commonPlugin.parametricDataset.sourceTypes.file')})`;
          } else if (source === 'HTTP') {
            item.source += ` (${t('commonPlugin.parametricDataset.sourceTypes.http')})`;
          } else if (source === 'JDBC') {
            item.source += ` (${t('commonPlugin.parametricDataset.sourceTypes.jdbc')})`;
          }
        } else {
          // Extracted value (with source type)
          item.source = t('commonPlugin.parametricDataset.sourceTypes.extracted');
          if (source === 'FILE') {
            item.source += ` (${t('commonPlugin.parametricDataset.sourceTypes.file')})`;
          } else if (source === 'JDBC') {
            item.source += ` (${t('commonPlugin.parametricDataset.sourceTypes.jdbc')})`;
          }
        }
      }

      // Add custom fields for internal use
      item['x-id'] = id;
      item['x-createdByName'] = createdByName;

      tableData.value.push(item);

      // Auto-select previously selected items
      if (rowSelection.value) {
        if (names.includes(name) && !rowSelection.value.selectedRowKeys.includes(name)) {
          rowSelection.value.selectedRowKeys.push(item.id);
        }
      }
    });
  }
};

/**
 * Handle table pagination, filter, or sort change
 * Updates pagination state, sorting parameters, and reloads data
 * 
 * @param pagination - New pagination state
 * @param _filters - Filter parameters (unused)
 * @param sorter - Sort parameters
 */
const tableChange = (
  { current, pageSize }: { current: number; pageSize: number; }, 
  _filters: { [key: string]: any }[], 
  sorter: { orderBy: OrderByKey; orderSort: OrderSortKey }
): void => {
  pagination.value.current = current;
  pagination.value.pageSize = pageSize;

  orderBy.value = sorter.orderBy;
  orderSort.value = sorter.orderSort;

  loadData();
};

/**
 * Handle table row selection change
 * Manages selected datasets across pages using a Map for persistence
 * 
 * @param keys - Array of selected row keys (IDs)
 */
const tableSelect = (keys: string[]): void => {
  if (!rowSelection.value) {
    return;
  }

  // Find deselected items on current page
  const currentIds = tableData.value.map(item => item.id);
  const deleteIds = currentIds.reduce((prev, cur) => {
    if (!keys.includes(cur)) {
      prev.push(cur);
    }
    return prev;
  }, [] as string[]);

  // Remove deselected items from map
  for (let i = 0, len = deleteIds.length; i < len; i++) {
    const id = deleteIds[i];
    selectedDataMap.value.delete(id);
  }

  // Remove deselected item IDs from selection
  const selectedRowKeys = rowSelection.value.selectedRowKeys.filter(
    item => !deleteIds.includes(item)
  );

  // Add newly selected items
  for (let i = 0, len = keys.length; i < len; i++) {
    const id = keys[i];
    if (!selectedRowKeys.includes(id)) {
      selectedRowKeys.push(id);

      // Find and store the data for this selection
      const data = tableData.value.find(item => item.id === id);
      if (data) {
        selectedDataMap.value.set(id, data);
      }
    }
  }

  rowSelection.value.selectedRowKeys = selectedRowKeys;
};

/**
 * Handle cancel button click
 * Closes modal without emitting selection
 */
const cancel = (): void => {
  emit('update:visible', false);
};

/**
 * Handle OK button click
 * Emits selected datasets and closes modal
 */
const ok = (): void => {
  const list = Array.from(selectedDataMap.value.values());
  emit('ok', list);
  cancel();
};

/**
 * Component mount lifecycle hook
 * Sets up row selection configuration and watches modal visibility
 */
onMounted(() => {
  // Configure row selection behavior
  rowSelection.value = {
    onChange: tableSelect,
    // Disable checkboxes for already selected datasets
    getCheckboxProps: ({ name }) => {
      return {
        disabled: props.selectedNames.includes(name)
      };
    },
    selectedRowKeys: []
  };

  /**
   * Watch for modal visibility changes
   * Resets state when closed, loads data when opened
   */
  watch(() => props.visible, (newValue) => {
    if (!newValue) {
      // Reset state when modal closes
      loaded.value = false;
      searchValue.value = undefined;
      pagination.value.current = 1;
      pagination.value.total = 0;
      selectedDataMap.value.clear();
      tableData.value = [];

      if (rowSelection.value) {
        rowSelection.value.selectedRowKeys = [];
      }

      return;
    }

    // Load data when modal opens
    loadData();
  }, { immediate: true });
});

/**
 * Computed OK button props
 * Disables OK button when no datasets are selected
 */
const okButtonProps = computed(() => {
  return {
    disabled: !rowSelection.value?.selectedRowKeys?.length
  };
});

/**
 * Table column definitions
 */
const columns = [
  {
    title: t('common.name'),
    dataIndex: 'name',
    ellipsis: true         // Truncate long text with ellipsis
  },
  {
    title: t('httpPlugin.uiConfig.httpConfigs.parametric.dataset.datasetModal.descriptionColumn'),
    dataIndex: 'description',
    ellipsis: true
  },
  {
    title: t('httpPlugin.uiConfig.httpConfigs.parametric.dataset.datasetModal.valueSource'),
    dataIndex: 'source',
    ellipsis: true,
    width: '10%'
  },
  {
    title: t('common.lastModifiedBy'),
    dataIndex: 'lastModifiedByName',
    ellipsis: true,
    width: '11%',
    sort: true             // Enable sorting for this column
  },
  {
    title: t('common.lastModifiedDate'),
    dataIndex: 'lastModifiedDate',
    ellipsis: true,
    width: '15%',
    sort: true             // Enable sorting for this column
  }
];
</script>

<template>
  <!-- Dataset selection modal -->
  <Modal
    :title="t('httpPlugin.uiConfig.httpConfigs.parametric.dataset.datasetModal.title')"
    :visible="props.visible"
    :width="1100"
    :okButtonProps="okButtonProps"
    wrapClassName="table-pagination-mini"
    @cancel="cancel"
    @ok="ok">
    <!-- Loading spinner wrapper -->
    <Spin :spinning="loading" class="h-full">
      <!-- Content (shown after initial load) -->
      <template v-if="loaded">
        <!-- Search and refresh toolbar -->
        <div class="flex items-center justify-between mb-3.5 space-x-5">
          <!-- Search input (max 150 chars) -->
          <Input
            :maxlength="150"
            allowClear
            trim
            class="w-75 flex-grow-0 flex-shrink"
            :placeholder="t('common.placeholders.searchKeyword')"
            @change="searchInputChange" />
          
          <!-- Refresh button -->
          <Button
            type="default"
            size="small"
            class="flex items-center"
            @click="toRefresh">
            <Icon class="mr-1 flex-shrink-0 text-3.5" icon="icon-shuaxin" />
            <span>{{ t('actions.refresh') }}</span>
          </Button>
        </div>

        <!-- No data placeholder (when table is empty) -->
        <NoData 
          v-if="tableData.length === 0" 
          class="flex-1 mt-10 mb-5" />

        <!-- Dataset table with selection, pagination, and sorting -->
        <Table
          v-else
          :dataSource="tableData"
          :columns="columns as any"
          :pagination="pagination"
          :rowSelection="rowSelection"
          rowKey="id"
          class="flex-1"
          noDataSize="small"
          @change="tableChange" />
      </template>
    </Spin>
  </Modal>
</template>

<style>
.table-pagination-mini .ant-pagination {
  margin-bottom: 0;
}
</style>
