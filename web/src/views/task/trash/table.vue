<script setup lang="ts">
import { onMounted, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Button } from 'ant-design-vue';
import { Icon, Image, Table } from '@xcan-angus/vue-ui';
import { useTaskTrashData } from './composables/useTaskTrashData';
import { useTaskTableColumns } from './composables/useTaskTableColumns';
import { useTaskTrashActions } from './composables/useTaskTrashActions';
import type { TaskTrashTableProps, TaskTrashItem } from './types';

/**
 * Component props with default values
 */
const props = withDefaults(defineProps<TaskTrashTableProps>(), {
  projectId: '',
  userInfo: () => ({ id: '' }),
  params: () => ({ targetType: 'TASK' as const }),
  notify: '',
  spinning: false
});

/**
 * Component emits
 */
const emit = defineEmits<{
  'update:spinning': [value: boolean];
  'listChange': [value: TaskTrashItem[]];
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
} = useTaskTrashData(props.projectId, props.userInfo);

const { columns, emptyTextStyle } = useTaskTableColumns();

const { recoverItem, deleteItem } = useTaskTrashActions(props.projectId);

/**
 * Handle recover action for a single item
 * @param data - Task trash item to recover
 */
const recoverHandler = async (data: TaskTrashItem) => {
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
 * @param data - Task trash item to delete
 */
const deleteHandler = async (data: TaskTrashItem) => {
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
    emit('listChange', newData);
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
        <div class="flex items-center flex-shrink-0 w-6 h-6 rounded-xl overflow-hidden mr-2 border border-gray-200">
          <Image
            :src="record.deletedByAvatar"
            type="avatar"
            class="w-full h-full object-cover" />
        </div>
        <div class="flex-1 truncate text-sm font-medium">{{ record.deletedByName }}</div>
      </div>

      <!-- Created by column with avatar -->
      <div
        v-else-if="column.dataIndex === 'createdByName'"
        :title="record.createdByName"
        class="flex items-center overflow-hidden">
        <div class="flex items-center flex-shrink-0 w-6 h-6 rounded-xl overflow-hidden mr-2 border border-gray-200">
          <Image
            :src="record.createdByAvatar"
            type="avatar"
            class="w-full h-full object-cover" />
        </div>
        <div class="flex-1 truncate text-sm font-medium">{{ record.createdByName }}</div>
      </div>

      <!-- Action column with buttons -->
      <div
        v-else-if="column.dataIndex === 'action'"
        class="flex items-center space-x-2.5">
        <!-- Recover button -->
        <Button
          :disabled="record.disabled"
          :title="t('taskTrash.actions.recover')"
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
          :title="t('taskTrash.actions.delete')"
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
 * Task trash table component styles
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
