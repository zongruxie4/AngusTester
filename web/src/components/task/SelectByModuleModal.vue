<script lang="ts" setup>
import { computed, defineAsyncComponent, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Icon, IconTask, Input, Modal, Table } from '@xcan-angus/vue-ui';
import { duration, http } from '@xcan-angus/infra';
import { debounce } from 'throttle-debounce';

const { t } = useI18n();

// ===== Component Props and Emits =====
interface Props {
  visible: boolean;
  projectId: string;
  action: string;
  title: string;
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  action: '',
  title: undefined
});

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void;
  (e: 'ok', value: string[], rowValue: any[]): void;
}>();

// Lazy load ModuleTree component to improve performance
const ModuleTree = defineAsyncComponent(() => import('@/components/module/treeSelector/index.vue'));

// ===== Reactive Data and State =====
const selectedTaskIds = ref<string[]>([]);
const selectedTaskRows = ref([]);
const allTaskData = ref<any[]>([]);
const filteredTaskData = ref<any[]>([]);
const isLoading = ref(false);
const selectedModuleId = ref('');
const searchKeywords = ref('');

// ===== Table Configuration =====
/**
 * <p>
 * Row selection configuration for the task table.
 * <p>
 * Handles selection changes and updates the selected task IDs and rows.
 */
const tableRowSelection = {
  onChange: (selectedRowKeys, selectedRows) => {
    selectedTaskIds.value = selectedRowKeys;
    selectedTaskRows.value = selectedRows;
  }
};

/**
 * <p>
 * Table column configuration for displaying task information.
 * <p>
 * Defines the columns for code, name, status, and task type.
 */
const tableColumns = [
  {
    key: 'code',
    title: t('common.code'),
    dataIndex: 'code',
    width: '20%'
  },
  {
    key: 'name',
    title: t('common.name'),
    dataIndex: 'name'
  },
  {
    key: 'status',
    title: t('common.status'),
    dataIndex: 'status',
    width: '10%'
  },
  {
    key: 'taskType',
    title: t('common.type'),
    dataIndex: 'taskType',
    width: '15%'
  }
];

// ===== Computed Properties =====
/**
 * <p>
 * Computed property for the modal title.
 * <p>
 * Returns the provided title or falls back to the default translated title.
 * @returns The modal title string
 */
const computedModalTitle = computed(() => {
  return props.title || t('commonComp.selectTaskByModuleModal.title');
});

// ===== API Methods =====
/**
 * <p>
 * Loads task cases from the API based on the selected module.
 * <p>
 * Fetches task data and applies initial filtering based on search keywords.
 */
const loadTaskCases = async () => {
  if (!props.projectId) {
    return;
  }
  isLoading.value = true;
  const [error, response] = await http.get(props.action, {
    moduleId: selectedModuleId.value
  });
  isLoading.value = false;
  if (error) {
    return;
  }
  allTaskData.value = response?.data || [];
  applyDataFiltering();
};

// ===== Data Filtering Methods =====
/**
 * <p>
 * Applies filtering to the task data based on search keywords.
 * <p>
 * Filters tasks by name or code containing the search keywords.
 */
const applyDataFiltering = () => {
  filteredTaskData.value = allTaskData.value.filter(item =>
    (item.name || '').includes(searchKeywords.value || '') ||
    (item.code || '').includes(searchKeywords.value || '')
  );
};

/**
 * <p>
 * Debounced search filter handler.
 * <p>
 * Clears current selection and applies filtering when search keywords change.
 */
const handleSearchFilter = debounce(duration.search, () => {
  selectedTaskIds.value = [];
  applyDataFiltering();
});

// ===== Event Handlers =====
/**
 * <p>
 * Handles the modal cancel action.
 * <p>
 * Emits the update:visible event to close the modal.
 */
const handleModalCancel = () => {
  emit('update:visible', false);
};

/**
 * <p>
 * Handles the modal confirm action.
 * <p>
 * Emits the ok event with selected task IDs and rows, or cancels if no selection.
 */
const handleModalConfirm = () => {
  if (selectedTaskIds.value.length) {
    emit('ok', selectedTaskIds.value, selectedTaskRows.value);
  } else {
    handleModalCancel();
  }
};

// ===== Lifecycle Hooks =====
// Watch for modal visibility changes
watch(() => props.visible, (isVisible) => {
  if (isVisible) {
    selectedTaskIds.value = [];
    loadTaskCases();
  }
}, {
  immediate: true
});

// Watch for module ID changes
watch(() => selectedModuleId.value, () => {
  selectedTaskIds.value = [];
  loadTaskCases();
});
</script>
<template>
  <Modal
    class="select-module-modal-container"
    :title="computedModalTitle"
    :visible="props.visible"
    :width="1200"
    :loading="isLoading"
    @cancel="handleModalCancel"
    @ok="handleModalConfirm">
    <div v-if="props.visible" class="modal-content">
      <!-- Left module tree area -->
      <div class="module-tree-section">
        <h4 class="section-title">{{ t('commonComp.selectTaskByModuleModal.moduleTree') }}</h4>
        <div class="module-tree-container">
          <ModuleTree
            v-model:moduleId="selectedModuleId"
            :title="t('common.issue')"
            :projectId="props.projectId"
            :projectName="''" />
        </div>
      </div>

      <!-- Right task list area -->
      <div class="task-list-section">
        <h4 class="section-title">{{ t('commonComp.selectTaskByModuleModal.taskList') }}</h4>

        <!-- Search area -->
        <div class="search-section">
          <Input
            v-model:value="searchKeywords"
            :placeholder="t('commonComp.selectTaskByModuleModal.searchPlaceholder')"
            class="search-input"
            @change="handleSearchFilter">
            <template #prefix>
              <Icon icon="icon-search" class="search-icon" />
            </template>
          </Input>
        </div>

        <!-- Task table area -->
        <div class="table-section">
          <Table
            :columns="tableColumns"
            :dataSource="filteredTaskData"
            :rowSelection="tableRowSelection"
            :pagination="false"
            :scroll="{x: true, y: 500, scrollToFirstRowOnChange: true}"
            class="task-table"
            rowKey="id"
            noDataText="No data"
            noDataSize="small">
            <template #bodyCell="{record, column}">
              <template v-if="column.dataIndex === 'status'">
                <span class="status-text">{{ record.status?.message }}</span>
              </template>
              <template v-if="column.dataIndex === 'taskType'">
                <div class="task-type-cell">
                  <IconTask :value="record.taskType?.value" class="task-type-icon" />
                  <span class="task-type-text">{{ record.taskType?.message }}</span>
                </div>
              </template>
            </template>
          </Table>
        </div>
      </div>
    </div>
  </Modal>
</template>

<style scoped>
/* Modal content area */
.modal-content {
  display: flex;
  gap: 20px;
  height: 600px;
}

/* Left module tree area */
.module-tree-section {
  width: 300px;
  display: flex;
  flex-direction: column;
  background-color: #fafafa;
  border: 1px solid #f0f0f0;
  border-radius: 6px;
  overflow: hidden;
}

.section-title {
  font-size: 12px;
  font-weight: 600;
  color: #262626;
  margin: 0;
  padding: 12px 16px;
  background-color: #f5f5f5;
  border-bottom: 1px solid #f0f0f0;
}

.module-tree-container {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

/* Right task list area */
.task-list-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  background-color: #fafafa;
  border: 1px solid #f0f0f0;
  border-radius: 6px;
  overflow: hidden;
}

/* Search area */
.search-section {
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
  background-color: #fff;
}

.search-input {
  width: 100%;
}

.search-icon {
  color: #bfbfbf;
  font-size: 14px;
}

/* Table area */
.table-section {
  flex: 1;
  padding: 16px;
  background-color: #fff;
  overflow: hidden;
}

.task-table {
  height: 100%;
}

/* Table content styles */
.status-text {
  font-size: 12px;
  color: #595959;
}

.task-type-cell {
  display: flex;
  align-items: center;
  gap: 6px;
}

.task-type-icon {
  font-size: 14px;
}

.task-type-text {
  font-size: 12px;
  color: #262626;
}

/* Deep style overrides */
:deep(.ant-input) {
  font-size: 12px;
  border-radius: 4px;
}

:deep(.ant-input:focus) {
  border-color: #1890ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
}

:deep(.ant-table-thead > tr > th) {
  background-color: #fafafa;
  font-size: 12px;
  font-weight: 600;
  color: #262626;
  border-bottom: 1px solid #f0f0f0;
  padding: 12px 8px;
}

:deep(.ant-table-tbody > tr > td) {
  font-size: 12px;
  color: #262626;
  padding: 12px 8px;
  border-bottom: 1px solid #f0f0f0;
}

:deep(.ant-table-tbody > tr:hover > td) {
  background-color: #f5f5f5;
}

:deep(.ant-table-tbody > tr.ant-table-row-selected > td) {
  background-color: #e6f7ff;
}

:deep(.ant-checkbox-wrapper) {
  font-size: 12px;
}

:deep(.ant-checkbox-inner) {
  width: 16px;
  height: 16px;
}

:deep(.ant-checkbox-checked .ant-checkbox-inner) {
  background-color: #1890ff;
  border-color: #1890ff;
}

/* Responsive design */
@media (max-width: 1200px) {
  .modal-content {
    flex-direction: column;
    height: auto;
    max-height: 600px;
  }

  .module-tree-section {
    width: 100%;
    height: 200px;
  }

  .task-list-section {
    height: 400px;
  }
}

@media (max-width: 768px) {
  .search-section {
    padding: 12px;
  }

  .table-section {
    padding: 12px;
  }

  .module-tree-container {
    padding: 6px;
  }
}
</style>
