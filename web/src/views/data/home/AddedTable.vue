<script setup lang="ts">
import { onMounted, watch } from 'vue';
import { Button } from 'ant-design-vue';
import { Icon, IconCopy, Table } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import { getCurrentPage } from '@/utils/utils';
import { useAddedData, useAddedTableColumns, useAddedEmptyState } from './composables';
import { AddedItem, AddedTableProps } from '@/views/data/home/types';
import { DataMenuKey } from '@/views/data/menu';

const { t } = useI18n();

/**
 * Component props with default values
 */
const props = withDefaults(defineProps<AddedTableProps>(), {
  projectId: undefined,
  total: 0,
  userId: '',
  notify: undefined,
  refreshNotify: undefined
});

/**
 * Component emits for parent communication
 */
// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'update:total', value: number): void;
  (e: 'update:deletedNotify', value: string): void;
}>();

/**
 * Initialize composables for data management, table columns, and empty state
 */
const {
  projectId,
  tableData,
  loading,
  loaded,
  pagination,
  total,
  isEmpty,
  loadData,
  handleTableChange,
  deleteItem,
  navigateToCreate
} = useAddedData(props.projectId, props.userId, props.type);

const { columns } = useAddedTableColumns(props.type);
const { emptyStateConfig, hasCreateAction } = useAddedEmptyState(props.type);

/**
 * Handle create button click based on data type
 */
const handleCreateClick = () => {
  if (props.type === 'variable') {
    navigateToCreate.variable();
  } else if (props.type === 'dataset') {
    navigateToCreate.dataset();
  }
};

/**
 * Emit total count updates to parent component
 */
const emitTotalUpdate = () => {
  emit('update:total', total.value);
};

/**
 * Emit deleted notification to parent component
 */
const emitDeletedNotify = () => {
  emit('update:deletedNotify', 'deleted');
};

/**
 * Enhanced delete handler with emit updates
 */
const handleDelete = (record: AddedItem) => {
  deleteItem(record);
  emitDeletedNotify();
};

/**
 * Watch for prop changes and trigger data reload
 */
onMounted(() => {
  // Watch project ID changes
  watch(() => props.projectId, () => {
    projectId.value = props.projectId;
    loadData();
  }, { immediate: true });

  // Watch notify changes for refresh
  watch(() => props.notify, (newValue) => {
    if (newValue === undefined || newValue === null || newValue === '') {
      return;
    }
    loadData();
  }, { immediate: true });

  // Watch deleted notify changes for pagination adjustment
  watch(() => props.refreshNotify, (newValue) => {
    if (newValue === undefined || newValue === null || newValue === '') {
      return;
    }

    pagination.value.current = getCurrentPage(
      pagination.value.current,
      pagination.value.pageSize,
      pagination.value.total
    );
    loadData();
  }, { immediate: true });

  // Watch total changes to emit updates
  watch(total, emitTotalUpdate);
});

/**
 * Table empty text style configuration
 */
const emptyTextStyle = {
  margin: '14px auto',
  height: 'auto'
};
</script>

<template>
  <div>
    <template v-if="loaded">
      <!-- Empty state display -->
      <template v-if="isEmpty">
        <div class="flex-1 flex flex-col items-center justify-center">
          <img class="w-27.5" src="../../../assets/images/nodata.png">
          <div class="flex items-center text-theme-sub-content text-3 leading-5">
            <span>{{ emptyStateConfig.message }}</span>
            <Button
              v-if="hasCreateAction"
              type="link"
              size="small"
              class="py-0 px-1 h-5 leading-5"
              @click="handleCreateClick">
              {{ emptyStateConfig.actionText }}
            </Button>
          </div>
        </div>
      </template>

      <!-- Data table display -->
      <Table
        v-else
        :dataSource="tableData"
        :columns="columns"
        :pagination="pagination"
        :loading="loading"
        :emptyTextStyle="emptyTextStyle"
        :minSize="5"
        rowKey="id"
        size="small"
        @change="handleTableChange">
        <!-- Custom cell rendering -->
        <template #bodyCell="{ record, column }">
          <!-- Variable type name cell -->
          <template v-if="props.type === 'variable'">
            <div v-if="column.dataIndex === 'name'" class="flex items-center">
              <RouterLink
                class="link flex-1 truncate"
                :title="record.name"
                :to="`/data#${DataMenuKey.VARIABLES}?id=${record.id}`">
                {{ record.name }}
              </RouterLink>
              <IconCopy :copyText="`{${record.name}}`" class="ml-1.5" />
            </div>
          </template>

          <!-- Dataset type name cell -->
          <template v-else-if="props.type === 'dataset'">
            <div v-if="column.dataIndex === 'name'" class="flex items-center">
              <RouterLink
                class="link flex-1 truncate"
                :title="record.name"
                :to="`/data#${DataMenuKey.DATASET}?id=${record.id}`">
                {{ record.name }}
              </RouterLink>
              <IconCopy :copyText="`{${record.name}}`" class="ml-1.5" />
            </div>
          </template>

          <!-- Action column -->
          <div v-if="column.dataIndex === 'action'">
            <Button
              :title="t('actions.delete')"
              size="small"
              type="text"
              class="space-x-1 flex items-center py-0 px-1"
              @click="handleDelete(record)">
              <Icon icon="icon-qingchu" class="text-3.5 cursor-pointer text-theme-text-hover" />
            </Button>
          </div>
        </template>
      </Table>
    </template>
  </div>
</template>

<style scoped>
.link {
  color: #1890ff;
  cursor: pointer;
}

.api-status-UNKNOWN {
  color: rgba(140, 140, 140, 100%);
}

.api-status-IN_DESIGN {
  color: rgba(255, 129, 0, 100%);
}

.api-status-IN_DEV {
  color: rgba(0, 119, 255, 100%);
}

.api-status-DEV_COMPLETED {
  color: rgba(82, 196, 26, 100%);
}

.api-status-RELEASED {
  color: rgb(56, 158, 13, 100%);
}

:deep(.ant-pagination) {
  margin-bottom: 0;
}
</style>
