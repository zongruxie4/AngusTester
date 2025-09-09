<script lang="ts" setup>
import { computed, defineAsyncComponent, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { IconTask, Input, Modal, Table } from '@xcan-angus/vue-ui';
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
    title: t('commonComp.selectTaskByModuleModal.code'),
    dataIndex: 'code',
    width: '20%'
  },
  {
    key: 'name',
    title: t('commonComp.selectTaskByModuleModal.name'),
    dataIndex: 'name'
  },
  {
    key: 'status',
    title: t('commonComp.selectTaskByModuleModal.status'),
    dataIndex: 'status',
    width: '10%'
  },
  {
    key: 'taskType',
    title: t('commonComp.selectTaskByModuleModal.type'),
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
    :title="computedModalTitle"
    :visible="props.visible"
    :width="1000"
    :loading="isLoading"
    @cancel="handleModalCancel"
    @ok="handleModalConfirm">
    <div v-if="props.visible" class="flex">
      <div class="w-50 h-144.5 overflow-y-auto">
        <ModuleTree
          v-model:moduleId="selectedModuleId"
          :title="t('commonComp.selectTaskByModuleModal.task')"
          :projectId="props.projectId"
          :projectName="''" />
      </div>
      <div class="flex-1 ml-2">
        <Input
          v-model:value="searchKeywords"
          :placeholder="t('commonComp.selectTaskByModuleModal.searchPlaceholder')"
          class="w-100"
          @change="handleSearchFilter" />
        <Table
          :columns="tableColumns"
          :dataSource="filteredTaskData"
          :rowSelection="tableRowSelection"
          :pagination="false"
          :scroll="{x: true, y: 500, scrollToFirstRowOnChange: true}"
          class="mt-2"
          rowKey="id"
          noDataText="No data"
          noDataSize="small">
          <template #bodyCell="{record, column}">
            <template v-if="column.dataIndex === 'status'">
              {{ record.status?.message }}
            </template>
            <template v-if="column.dataIndex === 'taskType'">
              <IconTask :value="record.taskType?.value" />
              {{ record.taskType?.message }}
            </template>
          </template>
        </Table>
      </div>
    </div>
  </Modal>
</template>
