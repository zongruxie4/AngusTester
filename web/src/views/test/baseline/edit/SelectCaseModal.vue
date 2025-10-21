<script lang="ts" setup>
import { defineAsyncComponent, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Input, Modal, ReviewStatus, Table } from '@xcan-angus/vue-ui';
import { duration } from '@xcan-angus/infra';
import { debounce } from 'throttle-debounce';
import { testPlan } from '@/api/tester';

import { BaselineCaseInfo } from '@/views/test/baseline/types';

import TestResult from '@/components/test/TestResult.vue';
import TaskPriority from '@/components/task/TaskPriority.vue';

const { t } = useI18n();

// Async Components
const ModuleTree = defineAsyncComponent(() => import('@/views/test/baseline/case/list/ModuleTree.vue'));

// Props Definition
interface Props {
  planId: string;
  visible: boolean;
  baselineId?: string;
  projectId: string;
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  baselineId: undefined
});

// Emits Definition
const emit = defineEmits<{(e: 'update:visible', value: boolean):void; (e: 'ok', value: string[], rowValue: BaselineCaseInfo[]):void}>();

// Reactive Data
const selectedCaseIds = ref<string[]>([]);
const selectedCaseRows = ref<BaselineCaseInfo[]>([]);
const allCaseData = ref([]);
const filteredCaseData = ref([]);
const isLoading = ref(false);
const selectedModuleId = ref('');
const searchKeywords = ref();

// Event Handlers
/**
 * Handle modal cancellation
 */
const handleModalCancel = () => {
  emit('update:visible', false);
};

/**
 * Handle modal confirmation
 */
const handleModalConfirm = () => {
  if (selectedCaseIds.value.length) {
    emit('ok', selectedCaseIds.value, selectedCaseRows.value);
  } else {
    handleModalCancel();
  }
};

/**
 * Load available cases for selection
 */
const loadAvailableCases = async () => {
  if (!props.planId) {
    return;
  }
  isLoading.value = true;
  const [error, resp] = await testPlan.getCaseNotEstablishedBaseline(props.planId, {
    baselineId: props.baselineId,
    moduleId: selectedModuleId.value
  });
  isLoading.value = false;
  if (error) {
    return;
  }
  allCaseData.value = resp?.data || [];
  filteredCaseData.value = allCaseData.value.filter(item => (item.name || '').includes(searchKeywords.value || '') || (item.code || '')
    .includes(searchKeywords.value || ''));
};

/**
 * Handle search filter with debounce
 */
const handleSearchFilter = debounce(duration.search, () => {
  selectedCaseIds.value = [];
  filteredCaseData.value = allCaseData.value.filter(item => (item.name).includes(searchKeywords.value || '') || (item.code || '').includes(searchKeywords.value || ''));
});

// Table Configuration
const tableColumns = [
  {
    title: t('common.code'),
    dataIndex: 'code',
    width: 120
  },
  {
    title: t('common.name'),
    dataIndex: 'name'
  },
  {
    title: t('common.version'),
    dataIndex: 'version',
    width: 80,
    customRender: ({ text }) => 'v' + text || '--'
  },
  {
    title: t('common.priority'),
    dataIndex: 'priority',
    width: 80
  },
  {
    title: t('common.reviewStatus'),
    dataIndex: 'reviewStatus',
    width: 100
  },
  {
    title: t('common.testResult'),
    dataIndex: 'testResult',
    width: 100
  },
  {
    title: t('common.tester'),
    dataIndex: 'testerName',
    width: 90
  }
];

/**
 * Row selection configuration
 */
const rowSelectionConfig = ref({
  onChange: (selectedRowKeys, selectedRows) => {
    selectedCaseIds.value = selectedRowKeys;
    selectedCaseRows.value = selectedRows;
  }
});

/**
 * Watch for modal visibility changes
 */
watch(() => props.visible, (isVisible) => {
  if (isVisible) {
    selectedCaseIds.value = [];
    loadAvailableCases();
  }
}, {
  immediate: true
});

/**
 * Watch for module ID changes
 */
watch(() => selectedModuleId.value, () => {
  selectedCaseIds.value = [];
  loadAvailableCases();
});
</script>
<template>
  <Modal
    :title="t('common.placeholders.selectCase')"
    :visible="props.visible"
    :width="1280"
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
          :dataSource="filteredCaseData"
          :rowSelection="rowSelectionConfig"
          :pagination="false"
          :scroll="{y: 500}"
          class="mt-2"
          rowKey="id"
          noDataSize="small">
          <template #bodyCell="{record, column}">
            <template v-if="column.dataIndex === 'priority'">
              <TaskPriority :value="record.priority" />
            </template>

            <template v-if="column.dataIndex === 'reviewStatus'">
              <ReviewStatus :value="record.reviewStatus" />
            </template>

            <template v-if="column.dataIndex === 'testResult'">
              <TestResult :value="record.testResult" />
            </template>
          </template>
        </Table>
      </div>
    </div>
  </Modal>
</template>
