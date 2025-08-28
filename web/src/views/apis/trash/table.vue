<script setup lang="ts">
import { onMounted, watch } from 'vue';
import { Button } from 'ant-design-vue';
import { Icon, Image, Table } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import { useTrashData } from './composables/useTrashData';
import { useTableColumns } from './composables/useTableColumns';
import { useTrashActions } from './composables/useTrashActions';
import type { TrashTableProps, TrashItem } from './types';

/**
 * Component props with default values
 */
const props = withDefaults(defineProps<TrashTableProps>(), {
  projectId: '',
  userInfo: () => ({ id: '' }),
  params: () => ({ targetType: 'API' as const }),
  notify: '',
  spinning: false
});

/**
 * Component emits
 */
const emit = defineEmits<{
  'update:spinning': [value: boolean];
  'tableChange': [value: TrashItem[]];
}>();

// Internationalization
const { t } = useI18n();

// Use composables
const {
  tableData,
  loading,
  loaded,
  pagination,
  loadData,
  handleTableChange: handleTableDataChange,
  resetPagination,
  updateCurrentPage
} = useTrashData(props.projectId, props.userInfo);

const { columns, emptyTextStyle } = useTableColumns();

const { recoverItem, deleteItem } = useTrashActions(props.projectId);

/**
 * Handle recover action for a single item
 * @param data - Trash item to recover
 */
const recoverHandler = async (data: TrashItem) => {
  emit('update:spinning', true);
  const success = await recoverItem(data);
  if (success) {
    updateCurrentPage();
    await loadData(props.params);
  }
  emit('update:spinning', false);
};

/**
 * Handle delete action for a single item
 * @param data - Trash item to delete
 */
const deleteHandler = async (data: TrashItem) => {
  emit('update:spinning', true);
  const success = await deleteItem(data);
  if (success) {
    updateCurrentPage();
    await loadData(props.params);
  }
  emit('update:spinning', false);
};

/**
 * Handle table change events (pagination, sorting)
 * @param paginationInfo - Pagination information
 * @param filters - Table filters
 * @param sorter - Sorting information
 */
const tableChange = (
  paginationInfo: { current?: number; pageSize?: number },
  filters: any,
  sorter: { orderBy?: string; orderSort?: 'ASC' | 'DESC' }
) => {
  handleTableDataChange(paginationInfo, filters, sorter);
  loadData(props.params);
};

/**
 * Watch for prop changes and reload data
 */
watch(
  [() => props.projectId, () => props.params],
  () => {
    resetPagination();
    loadData(props.params);
  },
  { immediate: true }
);

/**
 * Watch for notify changes and reload data
 */
watch(
  () => props.notify,
  (newValue) => {
    if (newValue === undefined || newValue === null || newValue === '') {
      return;
    }
    resetPagination();
    loadData(props.params);
  },
  { immediate: true }
);

/**
 * Watch for loading state changes and emit to parent
 */
watch(
  loading,
  (newLoading) => {
    emit('update:spinning', newLoading);
  }
);

/**
 * Watch for table data changes and emit to parent
 */
watch(
  tableData,
  (newData) => {
    emit('tableChange', newData);
  },
  { deep: true }
);

// Lifecycle hook
onMounted(() => {
  // Initial data loading is handled by watchers
});
</script>

<template>
  <Table
    v-if="loaded"
    :dataSource="tableData"
    :columns="columns"
    :pagination="pagination"
    :emptyTextStyle="emptyTextStyle"
    noDataSize="small"
    :noDataText="t('common.noData')"
    size="small"
    rowKey="id"
    @change="tableChange">
    <template #bodyCell="{ record, column }">
      <!-- Deleted by column with avatar -->
      <div
        v-if="column.dataIndex === 'deletedByName'"
        :title="record.deletedByName"
        class="flex items-center overflow-hidden">
        <div class="flex items-center flex-shrink-0 w-5 h-5 rounded-xl overflow-hidden mr-2">
          <Image
            :src="record.deletedByAvatar"
            type="avatar"
            class="w-full object-cover" />
        </div>
        <div class="flex-1 truncate text-sm">{{ record.deletedByName }}</div>
      </div>

      <!-- Created by column with avatar -->
      <div
        v-else-if="column.dataIndex === 'createdByName'"
        :title="record.createdByName"
        class="flex items-center overflow-hidden">
        <div class="flex items-center flex-shrink-0 w-5 h-5 rounded-xl overflow-hidden mr-2">
          <Image
            :src="record.createdByAvatar"
            type="avatar"
            class="w-full object-cover" />
        </div>
        <div class="flex-1 truncate text-sm">{{ record.createdByName }}</div>
      </div>

      <!-- Action column with buttons -->
      <div
        v-else-if="column.dataIndex === 'action'"
        class="flex items-center space-x-2.5">
        <!-- Recover button -->
        <Button
          :disabled="record.disabled"
          :title="t('apiTrash.table.actions.recover')"
          size="small"
          type="text"
          class="space-x-1 flex items-center p-0 hover:bg-blue-50"
          @click="recoverHandler(record)">
          <Icon
            icon="icon-zhongzhi"
            class="cursor-pointer text-blue-600 hover:text-blue-800" />
        </Button>

        <!-- Delete button -->
        <Button
          :disabled="record.disabled"
          :title="t('actions.delete')"
          size="small"
          type="text"
          class="space-x-1 flex items-center p-0 hover:bg-red-50"
          @click="deleteHandler(record)">
          <Icon
            icon="icon-qingchu"
            class="text-3.5 cursor-pointer text-red-600 hover:text-red-800" />
        </Button>
      </div>
    </template>
  </Table>
</template>

<style scoped>
/**
 * Table component styles
 * Using Tailwind CSS classes for most styling
 */
.ant-table-tbody > tr:hover > td {
  @apply bg-gray-50;
}

.ant-table-thead > tr > th {
  @apply bg-gray-50 font-medium;
}

/* Custom button hover effects are handled via Tailwind classes in template */
</style>
