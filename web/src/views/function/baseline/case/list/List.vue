<script lang="ts" setup>
import { defineAsyncComponent, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Icon, modal, Table } from '@xcan-angus/vue-ui';
import { Button } from 'ant-design-vue';
import { func } from '@/api/tester';
import TaskPriority from '@/components/TaskPriority/index.vue';
import { CaseDetail } from '@/views/function/types';

const { t } = useI18n();

// Async components for case management
const ModuleTree = defineAsyncComponent(() => import('./ModuleTree.vue'));
const SelectCaseModal = defineAsyncComponent(() => import('@/views/function/baseline/edit/SelectCaseModal.vue'));

// Async components for case details
const CaseReviewResult = defineAsyncComponent(() => import('@/views/function/review/detail/case/CaseReviewResult.vue'));
const CaseStep = defineAsyncComponent(() => import('@/views/function/case/list/CaseSteps.vue'));
const CaseBasicInfo = defineAsyncComponent(() => import('@/views/function/review/detail/case/CaseBasicInfo.vue'));
const Precondition = defineAsyncComponent(() => import('@/views/function/review/detail/case/Precondition.vue'));
const Members = defineAsyncComponent(() => import('@/views/function/review/detail/case/Member.vue'));
const TestInfo = defineAsyncComponent(() => import('@/views/function/review/detail/case/TestResult.vue'));
const Attachment = defineAsyncComponent(() => import('@/views/function/review/detail/case/Attachment.vue'));
const AssocTasks = defineAsyncComponent(() => import('@/views/function/review/detail/case/AssocTask.vue'));
const AssocCases = defineAsyncComponent(() => import('@/views/function/review/detail/case/AssocCase.vue'));
const Description = defineAsyncComponent(() => import('@/views/function/review/detail/case/Description.vue'));
const Search = defineAsyncComponent(() => import('./Search.vue'));

// Props Definition
type Props = {
  projectId: string;
  userInfo: { id: string; };
  appInfo: { id: string; };
  baselineId: string;
  planId: string;
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  baselineId: undefined,
  planId: undefined
});

// Table Configuration
const tableColumns = [
  {
    title: t('common.code'),
    dataIndex: 'code'
  },
  {
    title: t('common.name'),
    dataIndex: 'name'
  },
  {
    title: t('common.version'),
    dataIndex: 'version',
    customRender: ({ text }) => 'v' + text || '--'
  },
  {
    title: t('common.priority'),
    dataIndex: 'priority'
  },
  {
    title: t('common.creator'),
    dataIndex: 'createdByName'
  },
  {
    title: t('common.createTime'),
    dataIndex: 'createdDate'
  },
  {
    title: t('common.actions'),
    dataIndex: 'action'
  }
];

// Reactive Data
const currentBaselineInfo = ref();
const paginationConfig = ref({
  current: 1,
  pageSize: 10,
  total: 0
});
const searchFilters = ref([]);
const isSelectCaseModalVisible = ref(false);
const selectedModuleId = ref('');
const baselineCaseList = ref([]);

// Data Loading Methods
/**
 * Handle case selection confirmation
 * @param caseIds - Array of selected case IDs
 */
const handleCaseSelectionConfirm = async (caseIds: string[] = []) => {
  isSelectCaseModalVisible.value = false;
  const [error] = await func.addBaselineCase(props.baselineId, caseIds);
  if (error) {
    return;
  }
  await loadBaselineCaseList();
};

/**
 * Load baseline case list with pagination and filters
 */
const loadBaselineCaseList = async () => {
  const { current, pageSize } = paginationConfig.value;
  const [error, { data }] = await func.getBaselineCaseList(props.baselineId, {
    moduleId: selectedModuleId.value,
    projectId: props.projectId,
    pageNo: current,
    pageSize,
    filters: searchFilters.value
  });
  if (error) {
    return;
  }
  baselineCaseList.value = data?.list || [];
  paginationConfig.value.total = +data.total || 0;
};

/**
 * Load baseline information
 */
const loadBaselineInfo = async () => {
  const [error, res] = await func.getBaselineDetail(props.baselineId);
  if (error) {
    return;
  }

  const data = res?.data;
  if (!data) {
    return;
  }
  currentBaselineInfo.value = data;
};

/**
 * Handle pagination change
 * @param paginationParams - Pagination parameters
 */
const handlePaginationChange = ({ current, pageSize }) => {
  paginationConfig.value.current = current;
  paginationConfig.value.pageSize = pageSize;
  loadBaselineCaseList();
};

/**
 * Open add case modal
 */
const openAddCaseModal = () => {
  isSelectCaseModalVisible.value = true;
};

/**
 * Delete case from baseline
 * @param record - Case record to delete
 */
const deleteCaseFromBaseline = (record) => {
  modal.confirm({
    content: t('functionBaseline.case.confirmUnlinkCase', { name: record.name }),
    onOk () {
      return func.deleteBaselineCase(props.baselineId, [record.id])
        .then(([error]) => {
          if (error) {
            return;
          }
          if (paginationConfig.value.current > 1 && baselineCaseList.value.length === 1) {
            paginationConfig.value.current -= 1;
          }
          if (selectedRowKey.value === record.id) {
            selectedRowKey.value = undefined;
          }
          loadBaselineCaseList();
        });
    }
  });
};

/**
 * Handle search parameters change
 * @param params - Search parameters
 */
const handleSearchParametersChange = (params) => {
  const { pageNo, pageSize } = params;
  paginationConfig.value.current = pageNo;
  paginationConfig.value.pageSize = pageSize;
  searchFilters.value = params.filters;
  loadBaselineCaseList();
};

// Component State
const selectedRowKey = ref();
const selectedCaseInfo = ref<CaseDetail>();

/**
 * Create custom row click handler
 * @param record - Table row record
 * @returns Row click handler configuration
 */
const createRowClickHandler = (record) => {
  return {
    onClick: async () => {
      if (record.id === selectedRowKey.value) {
        selectedRowKey.value = '';
      } else {
        selectedRowKey.value = record.id;
        const [error, { data }] = await func.getBaselineCaseDetail(props.baselineId, record.id);
        if (error) {
          selectedCaseInfo.value = record;
          return;
        }
        selectedCaseInfo.value = data;
      }
    }
  };
};

/**
 * Close case details drawer
 */
const closeCaseDetailsDrawer = () => {
  selectedRowKey.value = '';
};

/**
 * Initialize component data on mount
 */
onMounted(() => {
  loadBaselineInfo();
  loadBaselineCaseList();
  watch(() => selectedModuleId.value, () => {
    loadBaselineCaseList();
  });
});
</script>
<template>
  <div class="flex space-x-2 h-full">
    <div
      class="w-70">
      <ModuleTree
        v-model:moduleId="selectedModuleId"
        v-bind="props" />
    </div>
    <div class="flex-1">
      <!-- Search -->
      <Search
        :established="currentBaselineInfo?.established"
        @handleAddCase="openAddCaseModal"
        @change="handleSearchParametersChange" />

      <!-- Table -->
      <Table
        noDataSize="small"
        :columns="tableColumns"
        :dataSource="baselineCaseList"
        :rowClassName="(record) => record.id === selectedRowKey ? 'ant-table-row-selected' : ''"
        :pagination="paginationConfig"
        :customRow="createRowClickHandler"
        @change="handlePaginationChange">
        <template #bodyCell="{column, record}">
          <template v-if="column.dataIndex === 'priority'">
            <TaskPriority
              :value="record.priority" />
          </template>
          <template v-if="column.dataIndex === 'action'">
            <Button
              type="text"
              size="small"
              @click.stop="deleteCaseFromBaseline(record)">
              <Icon icon="icon-qingchu" class="mr-1" />
              {{ t('actions.cancel') }}
            </Button>
          </template>
        </template>
      </Table>
    </div>

    <!-- Case Details Drawer -->
    <div class="flex flex-col transition-all -mt-4" :class="{'w-0': !selectedRowKey, 'w-100 border-l p-2': !!selectedRowKey}">
      <div>
        <Icon
          icon="icon-shanchuguanbi"
          class="text-5 cursor-pointer"
          @click="closeCaseDetailsDrawer" />
      </div>

      <div class="flex-1 overflow-auto p-4">
        <CaseBasicInfo
          class="pb-5"
          :caseInfo="selectedCaseInfo" />

        <Precondition
          class="py-5"
          :caseInfo="selectedCaseInfo" />

        <div class="font-semibold text-3.5">
          {{ t('functionBaseline.case.testSteps') }}
        </div>

        <CaseStep
          class="pb-5 pt-3"
          :defaultValue="selectedCaseInfo?.steps || []"
          readonly />

        <CaseReviewResult
          class="py-5"
          :caseInfo="selectedCaseInfo" />

        <Description
          class="py-5"
          :caseInfo="selectedCaseInfo" />

        <Members
          class="py-5"
          :caseInfo="selectedCaseInfo" />

        <TestInfo
          class="py-5"
          :caseInfo="selectedCaseInfo" />

        <AssocTasks
          class="py-5"
          :dataSource="selectedCaseInfo?.refTaskInfos"
          :projectId="props.projectId"
          :caseInfo="selectedCaseInfo" />

        <AssocCases
          class="py-5"
          :dataSource="selectedCaseInfo?.refCaseInfos"
          :projectId="props.projectId"
          :caseInfo="selectedCaseInfo" />

        <Attachment
          class="py-5"
          :caseInfo="selectedCaseInfo" />
      </div>
    </div>

    <SelectCaseModal
      v-model:visible="isSelectCaseModalVisible"
      v-bind="props"
      @ok="handleCaseSelectionConfirm" />
  </div>
</template>
