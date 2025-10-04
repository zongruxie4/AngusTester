<script lang="ts" setup>
import { defineAsyncComponent, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Input, Modal, ReviewStatus, Table } from '@xcan-angus/vue-ui';
import { duration } from '@xcan-angus/infra';
import { debounce } from 'throttle-debounce';
import { funcPlan } from '@/api/tester';
import { ReviewCaseInfo } from '@/views/test/review/types';

// Component imports
import TaskPriority from '@/components/TaskPriority/index.vue';
const ModuleTree = defineAsyncComponent(() => import('@/views/test/review/edit/ModuleTree.vue'));

// Type Definitions
interface Props {
  planId: string;
  visible: boolean;
  reviewId?: string;
  projectId: string;
}

// Props and Context
const props = withDefaults(defineProps<Props>(), {
  visible: false,
  reviewId: undefined,
  projectId: undefined
});

const { t } = useI18n();

// Event Emitters
const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void;
  (e: 'ok', value: string[], rowValue: ReviewCaseInfo[]): void
}>();

// Reactive State
const selectedCaseIds = ref<string[]>([]);
const selectedCaseRows = ref<ReviewCaseInfo[]>([]);
const availableCases = ref<ReviewCaseInfo[]>([]);
const filteredCases = ref<ReviewCaseInfo[]>([]);
const loading = ref(false);
const searchKeywords = ref<string>();

/**
 * Handles modal cancellation
 * <p>
 * Closes modal and emits visibility update
 */
const handleModalCancel = () => {
  emit('update:visible', false);
};

/**
 * Handles modal confirmation
 * <p>
 * Emits selected cases if any are selected, otherwise closes modal
 */
const handleModalConfirm = () => {
  if (selectedCaseIds.value.length) {
    emit('ok', selectedCaseIds.value, selectedCaseRows.value);
  } else {
    handleModalCancel();
  }
};

// Case Loading and Filtering
const selectedModuleId = ref('');

/**
 * Loads available cases for selection
 * <p>
 * Fetches cases that are not yet reviewed for the selected plan and module
 * <p>
 * Applies initial filtering based on search keywords
 */
const loadAvailableCases = async () => {
  if (!props.planId) {
    return;
  }
  loading.value = true;
  const [error, response] = await funcPlan.getNotReviewedCase(props.planId, {
    reviewId: props.reviewId,
    moduleId: selectedModuleId.value
  });
  loading.value = false;
  if (error) {
    return;
  }
  availableCases.value = response.data || [];
  applyCaseFiltering();
};

/**
 * Applies search filter to available cases
 * <p>
 * Filters cases by name or code containing search keywords
 * <p>
 * Updates filtered cases display
 */
const applyCaseFiltering = () => {
  filteredCases.value = availableCases.value
    .filter(caseItem =>
      (caseItem.name).includes(searchKeywords.value || '') ||
      (caseItem.code || '').includes(searchKeywords.value || '')
    );
};

/**
 * Handles search keyword change with debounce
 * <p>
 * Clears selection and applies filtering when keywords change
 */
const handleSearchFilter = debounce(duration.search, () => {
  selectedCaseIds.value = [];
  applyCaseFiltering();
});

/**
 * Table column configuration for case selection
 * <p>
 * Defines display columns for case information
 */
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
    title: t('common.priority'),
    dataIndex: 'priority',
    width: 80
  },
  {
    title: t('common.tester'),
    dataIndex: 'testerName',
    width: 100
  },
  {
    title: t('common.developer'),
    dataIndex: 'developerName',
    width: 100
  },
  {
    title: t('common.reviewStatus'),
    dataIndex: 'reviewStatus',
    width: 100
  }
];

/**
 * Handles row selection change
 * <p>
 * Updates selected case IDs and row data
 */
const handleRowSelectionChange = (selectedRowKeys: string[], selectedRows: ReviewCaseInfo[]) => {
  selectedCaseIds.value = selectedRowKeys;
  selectedCaseRows.value = selectedRows;
};

const rowSelection = ref({
  onChange: handleRowSelectionChange
});

/**
 * Watches module ID changes
 * <p>
 * Clears selection and reloads cases when module changes
 */
watch(() => selectedModuleId.value, () => {
  selectedCaseIds.value = [];
  loadAvailableCases();
});

/**
 * Watches modal visibility changes
 * <p>
 * Clears selection and loads cases when modal opens
 */
watch(() => props.visible, (isVisible) => {
  if (isVisible) {
    selectedCaseIds.value = [];
    loadAvailableCases();
  }
}, {
  immediate: true
});
</script>
<template>
  <Modal
    :title="t('common.placeholders.selectCase')"
    :visible="props.visible"
    :width="1100"
    :loading="loading"
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
          allowClear="true"
          @change="handleSearchFilter" />
        <Table
          :columns="tableColumns"
          :dataSource="filteredCases"
          :rowSelection="rowSelection"
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
          </template>
        </Table>
      </div>
    </div>
  </Modal>
</template>
