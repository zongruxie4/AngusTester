<script lang="ts" setup>
import { defineAsyncComponent, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Input, Modal, ReviewStatus, Table } from '@xcan-angus/vue-ui';
import { http, duration } from '@xcan-angus/infra';
import { debounce } from 'throttle-debounce';
import TestResult from '@/components/TestResult/index.vue';
import { ReviewCaseInfo } from '@/views/test/review/types';

// Async component definitions
const ModuleTree = defineAsyncComponent(() => import('@/components/module/ModuleTreeSelector.vue'));

const { t } = useI18n();

/**
 * Component props interface for case selection modal
 */
interface Props {
  visible: boolean;
  projectId: number;
  action: string;
}

// Component props with default values
const props = withDefaults(defineProps<Props>(), {
  visible: false,
  action: ''
});

// Component events
// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void;
  (e: 'ok', value: number[], rowValue: ReviewCaseInfo[]): void;
}>();

// Component state management
const selectedCaseIds = ref<number[]>([]);
const selectedCaseRows = ref<ReviewCaseInfo[]>([]);
const allCaseData = ref<any[]>([]);
const filteredCaseData = ref<any[]>([]);
const isLoading = ref(false);
const selectedModuleId = ref<number>();
const searchKeywords = ref<string>('');

/**
 * Handle modal cancel action
 */
const handleModalCancel = () => {
  emit('update:visible', false);
};

/**
 * Handle modal confirm action with selected cases
 */
const handleModalConfirm = () => {
  if (selectedCaseIds.value.length) {
    emit('ok', selectedCaseIds.value, selectedCaseRows.value);
  } else {
    handleModalCancel();
  }
};

/**
 * Load test cases data from API based on selected module
 */
const loadTestCasesData = async () => {
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
  allCaseData.value = response?.data || [];
  applySearchFilter();
};

/**
 * Apply search filter to case data based on keywords
 */
const applySearchFilter = () => {
  filteredCaseData.value = allCaseData.value.filter(item =>
    (item.name || '').includes(searchKeywords.value || '') ||
    (item.code || '').includes(searchKeywords.value || '')
  );
};

/**
 * Handle search filter with debounce
 */
const handleSearchFilter = debounce(duration.search, () => {
  selectedCaseIds.value = [];
  applySearchFilter();
});

/**
 * Handle table row selection change
 * @param selectedRowKeys - Selected row keys
 * @param selectedRows - Selected row data
 */
 const handleRowSelectionChange = (selectedRowKeys: number[], selectedRows: ReviewCaseInfo[]) => {
  selectedCaseIds.value = selectedRowKeys;
  selectedCaseRows.value = selectedRows;
};

// Table row selection configuration
const tableRowSelection = ref({
  onChange: handleRowSelectionChange
});

// Component watchers
watch(() => props.visible, (isVisible: boolean) => {
  if (isVisible) {
    selectedCaseIds.value = [];
    loadTestCasesData();
  }
}, {
  immediate: true
});

watch(() => selectedModuleId.value, () => {
  selectedCaseIds.value = [];
  loadTestCasesData();
});

// Table configuration
const tableColumns = [
  {
    key: 'code',
    title: t('common.code'),
    dataIndex: 'code'
  },
  {
    key: 'name',
    title: t('common.name'),
    dataIndex: 'name',
    width: '40%'
  },
  {
    key: 'reviewStatus',
    title: t('common.reviewStatus'),
    dataIndex: 'reviewStatus'
  },
  {
    key: 'testResult',
    title: t('common.testResult'),
    dataIndex: 'testResult'
  },
  {
    key: 'testerName',
    title: t('common.tester'),
    dataIndex: 'testerName'
  }
];
</script>
<template>
  <Modal
    :title="t('commonComp.selectCaseByModule.title')"
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
          :placeholder="t('commonComp.selectCaseByModule.searchPlaceholder')"
          class="w-100"
          @change="handleSearchFilter" />
        <Table
          :columns="tableColumns"
          :dataSource="filteredCaseData"
          :rowSelection="tableRowSelection"
          :pagination="false"
          :scroll="{y: 500, x: true, scrollToFirstRowOnChange: false}"
          class="mt-2"
          rowKey="id"
          noDataSize="small">
          <template #bodyCell="{record, column}">
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
