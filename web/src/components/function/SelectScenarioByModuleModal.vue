<script lang="ts" setup>
import { defineAsyncComponent, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Input, Modal, Table } from '@xcan-angus/vue-ui';
import { duration } from '@xcan-angus/infra';
import { debounce } from 'throttle-debounce';
import { ScenarioInfo } from '@/views/scenario/scenario/list/types';
import { scenario } from '@/api/tester/';

const ModuleTree = defineAsyncComponent(() => import('@/components/module/ModuleTreeSelector.vue'));

const { t } = useI18n();

/**
 * Component props interface for Scenario selection modal
 */
interface Props {
  visible: boolean;
  projectId: string;
  hadSelectedIds: string[];
}

// Component props with default values
const props = withDefaults(defineProps<Props>(), {
  visible: false,
  hadSelectedIds: () => []
});

// Component events

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void;
  (e: 'ok', value: string[], rowValue: ScenarioInfo[]): void;
}>();

// Component state management
const selectedScenarioIds = ref<string[]>([]);
const selectedScenarioRows = ref<ScenarioInfo[]>([]);
const allScenarioData = ref<any[]>([]);
const isLoading = ref(false);
const selectedModuleId = ref<string>();
const searchKeywords = ref<string>('');

/**
 * Handle modal cancel action
 */
const handleModalCancel = () => {
  emit('update:visible', false);
};

/**
 * Handle modal confirm action with selected Scenarios
 */
const handleModalConfirm = () => {
  if (selectedScenarioIds.value.length) {
    emit('ok', selectedScenarioIds.value, selectedScenarioRows.value);
  } else {
    handleModalCancel();
  }
};

/**
 * Load test Scenarios data from API based on selected module
 */
const loadTestScenariosData = async () => {
  if (!props.projectId) {
    return;
  }
  isLoading.value = true;
  const [error, response] = await scenario.getScenarioList({
    projectId: props.projectId,
    moduleId: selectedModuleId.value,
    filters: [
      props.hadSelectedIds.length && { key: 'id', op: 'NOT_IN', value: props.hadSelectedIds},
      searchKeywords.value &&{ key: 'name', op: 'MATCH', value: searchKeywords.value}].filter(Boolean),
    pageNo: 1,
    pageSize: 2000
  });
  isLoading.value = false;
  if (error) {
    return;
  }
  allScenarioData.value = response?.data?.list || [];
};

/**
 * Handle search filter with debounce
 */
const handleSearchFilter = debounce(duration.search, () => {
  selectedScenarioIds.value = [];
  loadTestScenariosData();
});

/**
 * Handle table row selection change
 * @param selectedRowKeys - Selected row keys
 * @param selectedRows - Selected row data
 */
 const handleRowSelectionChange = (selectedRowKeys: string[], selectedRows: ScenarioInfo[]) => {
  selectedScenarioIds.value = selectedRowKeys;
  selectedScenarioRows.value = selectedRows;
};

// Table row selection configuration
const tableRowSelection = ref({
  onChange: handleRowSelectionChange,
  getCheckboxProps: (record: ScenarioInfo) => {
    return {
      disabled: !selectedScenarioIds.value.includes(record.id) && (selectedScenarioIds.value.length + props.hadSelectedIds.length > 19)
    }
  },
  hideSelectAll: true
});

// Component watchers
watch(() => props.visible, (isVisible: boolean) => {
  if (isVisible) {
    selectedScenarioIds.value = [];
    loadTestScenariosData();
  }
}, {
  immediate: true
});

watch(() => selectedModuleId.value, () => {
  selectedScenarioIds.value = [];
  loadTestScenariosData();
});

// Table configuration
const tableColumns = [
  {
    key: 'name',
    title: t('common.name'),
    dataIndex: 'name'
  },
  {
    key: 'plugin',
    title: t('common.plugin'),
    dataIndex: 'plugin',
    width: '20%'
  },
  {
    key: 'scriptType',
    title: t('common.scriptType'),
    dataIndex: 'scriptType',
    customRender: ({text}) => {
      return text?.message;
    },
     width: '30%'
  }
];
</script>
<template>
  <Modal
    :title="t('common.placeholders.selectScenario')"
    :visible="props.visible"
    :width="1000"
    :loading="isLoading"
    @cancel="handleModalCancel"
    @ok="handleModalConfirm">
    <div class="flex">
      <div class="w-50 h-144.5 overflow-y-auto">
        <ModuleTree
          v-model:moduleId="selectedModuleId"
          :projectId="props.projectId" />
      </div>
      <div class="flex-1 ml-2">
        <Input
          v-model:value="searchKeywords"
          :placeholder="t('common.placeholders.searchKeyword')"
          class="w-100"
          @change="handleSearchFilter" />
        <Table
          :columns="tableColumns"
          :dataSource="allScenarioData"
          :rowSelection="tableRowSelection"
          :pagination="false"
          :scroll="{y: 500, x: true, scrollToFirstRowOnChange: false}"
          class="mt-2"
          rowKey="id"
          noDataSize="small">
        </Table>
      </div>
    </div>
  </Modal>
</template>
