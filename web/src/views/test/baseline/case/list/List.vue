<script lang="ts" setup>
import { defineAsyncComponent, onMounted, onUnmounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Icon, modal, Table } from '@xcan-angus/vue-ui';
import { Button } from 'ant-design-vue';
import { test } from '@/api/tester';
import { CaseDetail } from '@/views/test/types';
import { BasicProps } from '@/types/types';

import TaskPriority from '@/components/task/TaskPriority.vue';

const { t } = useI18n();

// Async components for case management
const ModuleTree = defineAsyncComponent(() => import('@/components/module/ModuleSelectTree/index.vue'));
const SelectCaseModal = defineAsyncComponent(() => import('@/views/test/baseline/edit/SelectCaseModal.vue'));

// Async components for case details
const CaseReviewResult = defineAsyncComponent(() => import('@/views/test/review/detail/case/CaseReviewResult.vue'));
const CaseStep = defineAsyncComponent(() => import('@/views/test/case/list/CaseSteps.vue'));
const CaseBasicInfo = defineAsyncComponent(() => import('@/views/test/review/detail/case/CaseBasicInfo.vue'));
const Precondition = defineAsyncComponent(() => import('@/views/test/review/detail/case/Precondition.vue'));
const Members = defineAsyncComponent(() => import('@/views/test/review/detail/case/Member.vue'));
const TestInfo = defineAsyncComponent(() => import('@/views/test/review/detail/case/TestResult.vue'));
const Attachment = defineAsyncComponent(() => import('@/views/test/review/detail/case/Attachment.vue'));
const AssocTasks = defineAsyncComponent(() => import('@/views/test/review/detail/case/AssocIssues.vue'));
const AssocCases = defineAsyncComponent(() => import('@/views/test/review/detail/case/AssocCases.vue'));
const Description = defineAsyncComponent(() => import('@/views/test/review/detail/case/Description.vue'));
const Search = defineAsyncComponent(() => import('./Search.vue'));

const props = withDefaults(defineProps<BasicProps>(), {
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
    dataIndex: 'code',
    width: 150
  },
  {
    title: t('common.name'),
    dataIndex: 'name'
  },
  {
    title: t('common.version'),
    dataIndex: 'version',
    customRender: ({ text }) => 'v' + text || '--',
    width: 100
  },
  {
    title: t('common.priority'),
    dataIndex: 'priority',
    width: 100
  },
  {
    title: t('common.creator'),
    dataIndex: 'createdByName',
    width: 120
  },
  {
    title: t('common.createdDate'),
    dataIndex: 'createdDate',
    width: 150
  },
  {
    title: t('common.actions'),
    dataIndex: 'action',
    width: 100
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
const selectedModuleId = ref<number|undefined>();
const baselineCaseList = ref([]);

// Data Loading Methods
/**
 * Handle case selection confirmation
 * @param caseIds - Array of selected case IDs
 */
const handleCaseSelectionConfirm = async (caseIds: string[] = []) => {
  isSelectCaseModalVisible.value = false;
  const [error] = await test.addBaselineCase(props.baselineId, caseIds);
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
  const [error, { data }] = await test.getBaselineCaseList(props.baselineId, {
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
  const [error, res] = await test.getBaselineDetail(props.baselineId);
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
    content: t('testCaseBaseline.case.confirmUnlinkCase', { name: record.name }),
    onOk () {
      return test.deleteBaselineCase(props.baselineId, [record.id])
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
const isMobile = ref(false);

// Right drawer section expand states
const expand = ref({
  basicInfo: true,
  precondition: true,
  steps: true,
  reviewResult: true,
  description: true,
  members: true,
  testInfo: true,
  assocTasks: true,
  assocCases: true,
  attachments: true
});

const toggleSection = (key: keyof typeof expand.value) => {
  expand.value[key] = !expand.value[key];
};

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
        const [error, { data }] = await test.getBaselineCaseDetail(props.baselineId, record.id);
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
 * Check if the current screen size is mobile
 */
const checkMobile = () => {
  isMobile.value = window.innerWidth < 1024;
};

/**
 * Initialize component data on mount
 */
onMounted(() => {
  // Responsive detection
  checkMobile();
  window.addEventListener('resize', checkMobile);

  loadBaselineInfo();
  loadBaselineCaseList();
  watch(() => selectedModuleId.value, () => {
    loadBaselineCaseList();
  });
});

onUnmounted(() => {
  window.removeEventListener('resize', checkMobile);
});
</script>
<template>
  <div class="flex space-x-2 h-full">
    <div
      class="w-70">
      <ModuleTree
        v-model:moduleId="selectedModuleId"
        v-bind="props"
        :readonly="true" />
    </div>
    <div class="flex-1">
      <!-- Search -->
      <Search
        :established="currentBaselineInfo?.established"
        :userId="props.userInfo?.id"
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
              :disabled="currentBaselineInfo?.established"
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
    <div
      class="flex flex-col mt-4 transition-all duration-300 ease-in-out bg-white border border-gray-200 shadow-lg"
      :class="{
        'w-0 opacity-0 overflow-hidden': !selectedRowKey,
        'opacity-100 w-[28.8rem]': !!selectedRowKey && !isMobile,
        'fixed inset-0 z-50 w-full': selectedRowKey && isMobile
      }">
      <!-- Drawer header -->
      <div class="flex items-center justify-between p-4 border-b border-gray-200 bg-gray-50">
        <h3 class="text-lg font-semibold text-gray-900">
          {{ t('testCaseBaseline.case.detailTitle') }}
        </h3>
        <Button
          type="text"
          size="small"
          class="text-gray-400 hover:text-gray-600"
          @click="closeCaseDetailsDrawer">
          <Icon icon="icon-shanchuguanbi" class="text-lg" />
        </Button>
      </div>

      <!-- Drawer content -->
      <div class="flex-1 overflow-auto">
        <!-- Case details content: title outside the card, supports collapse -->
        <div class="p-4 space-y-4 drawer-sections">
          <!-- Basic info -->
          <div class="space-y-2">
            <div class="flex items-center justify-between cursor-pointer select-none" @click="toggleSection('basicInfo')">
              <div class="flex items-center text-gray-800 text-sm font-medium">
                <Icon icon="icon-jibenxinxi" class="mr-1 text-blue-500" />
                <span>{{ t('common.basicInfo') }}</span>
              </div>
              <Icon :icon="expand.basicInfo ? 'icon-shouqijiantou1' : 'icon-zhankaijiantou1'" class="text-gray-400" />
            </div>
            <div v-show="expand.basicInfo">
              <CaseBasicInfo
                :caseInfo="selectedCaseInfo"
                :projectId="props.projectId" />
            </div>
          </div>

          <!-- Precondition -->
          <div class="space-y-2">
            <div class="flex items-center justify-between cursor-pointer select-none" @click="toggleSection('precondition')">
              <div class="flex items-center text-gray-800 text-sm font-medium">
                <Icon icon="icon-shezhi1" class="mr-1 text-orange-500" />
                <span>{{ t('common.precondition') }}</span>
              </div>
              <Icon :icon="expand.precondition ? 'icon-shouqijiantou1' : 'icon-zhankaijiantou1'" class="text-gray-400" />
            </div>
            <div v-show="expand.precondition">
              <Precondition :caseInfo="selectedCaseInfo" />
            </div>
          </div>

          <!-- Test steps -->
          <div class="space-y-2">
            <div class="flex items-center justify-between cursor-pointer select-none" @click="toggleSection('steps')">
              <div class="flex items-center text-gray-800 text-sm font-medium">
                <Icon icon="icon-jihua1" class="mr-1 text-indigo-500" />
                <span>{{ t('common.testSteps') }}</span>
              </div>
              <Icon :icon="expand.steps ? 'icon-shouqijiantou1' : 'icon-zhankaijiantou1'" class="text-gray-400" />
            </div>
            <div v-show="expand.steps">
              <CaseStep
                :defaultValue="selectedCaseInfo?.steps || []"
                readonly />
            </div>
          </div>

          <!-- Description -->
          <div class="space-y-2">
            <div class="flex items-center justify-between cursor-pointer select-none" @click="toggleSection('description')">
              <div class="flex items-center text-gray-800 text-sm font-medium">
                <Icon icon="icon-shuoming" class="mr-1 text-purple-500" />
                <span>{{ t('common.description') }}</span>
              </div>
              <Icon :icon="expand.description ? 'icon-shouqijiantou1' : 'icon-zhankaijiantou1'" class="text-gray-400" />
            </div>
            <div v-show="expand.description">
              <Description :caseInfo="selectedCaseInfo" />
            </div>
          </div>

          <!-- Review result -->
          <div class="space-y-2">
            <div class="flex items-center justify-between cursor-pointer select-none" @click="toggleSection('reviewResult')">
              <div class="flex items-center text-gray-800 text-sm font-medium">
                <Icon icon="icon-pingshen" class="mr-1 text-green-500" />
                <span>{{ t('common.reviewResult') }}</span>
              </div>
              <Icon :icon="expand.reviewResult ? 'icon-shouqijiantou1' : 'icon-zhankaijiantou1'" class="text-gray-400" />
            </div>
            <div v-show="expand.reviewResult">
              <CaseReviewResult :caseInfo="selectedCaseInfo" />
            </div>
          </div>

          <!-- Test info -->
          <div class="space-y-2">
            <div class="flex items-center justify-between cursor-pointer select-none" @click="toggleSection('testInfo')">
              <div class="flex items-center text-gray-800 text-sm font-medium">
                <Icon icon="icon-ceshixinxi" class="mr-1 text-amber-500" />
                <span>{{ t('common.testInfo') }}</span>
              </div>
              <Icon :icon="expand.testInfo ? 'icon-shouqijiantou1' : 'icon-zhankaijiantou1'" class="text-gray-400" />
            </div>
            <div v-show="expand.testInfo">
              <TestInfo :caseInfo="selectedCaseInfo" />
            </div>
          </div>

          <!-- Members -->
          <div class="space-y-2">
            <div class="flex items-center justify-between cursor-pointer select-none" @click="toggleSection('members')">
              <div class="flex items-center text-gray-800 text-sm font-medium">
                <Icon icon="icon-chuangjianren" class="mr-1 text-pink-500" />
                <span>{{ t('common.members') }}</span>
              </div>
              <Icon :icon="expand.members ? 'icon-shouqijiantou1' : 'icon-zhankaijiantou1'" class="text-gray-400" />
            </div>
            <div v-show="expand.members">
              <Members
                :caseInfo="selectedCaseInfo"
                :userInfo="props.userInfo" />
            </div>
          </div>

          <!-- Related tasks -->
          <div class="space-y-2 space-x-4">
            <div class="flex items-center justify-between cursor-pointer select-none" @click="toggleSection('assocTasks')">
              <div class="flex items-center text-gray-800 text-sm font-medium">
                <Icon icon="icon-renwu" class="mr-1 text-emerald-500" />
                <span>{{ t('common.assocIssues') }}</span>
              </div>
              <Icon :icon="expand.assocTasks ? 'icon-shouqijiantou1' : 'icon-zhankaijiantou1'" class="text-gray-400" />
            </div>
            <div v-show="expand.assocTasks">
              <AssocTasks
                :dataSource="selectedCaseInfo?.refTaskInfos"
                :projectId="props.projectId"
                :caseInfo="selectedCaseInfo"
                :hideTitle="true" />
            </div>
          </div>

          <!-- Related cases -->
          <div class="space-y-2 space-x-4">
            <div class="flex items-center justify-between cursor-pointer select-none" @click="toggleSection('assocCases')">
              <div class="flex items-center text-gray-800 text-sm font-medium">
                <Icon icon="icon-gongnengyongli" class="mr-1 text-cyan-500" />
                <span>{{ t('common.assocCases') }}</span>
              </div>
              <Icon :icon="expand.assocCases ? 'icon-shouqijiantou1' : 'icon-zhankaijiantou1'" class="text-gray-400" />
            </div>
            <div v-show="expand.assocCases">
              <AssocCases
                :dataSource="selectedCaseInfo?.refCaseInfos"
                :projectId="props.projectId"
                :caseInfo="selectedCaseInfo"
                :hideTitle="true" />
            </div>
          </div>

          <!-- Attachments -->
          <div class="space-y-2 space-x-4">
            <div class="flex items-center justify-between cursor-pointer select-none" @click="toggleSection('attachments')">
              <div class="flex items-center text-gray-800 text-sm font-medium">
                <Icon icon="icon-wenjian" class="mr-1 text-indigo-500" />
                <span>{{ t('common.attachment') }}</span>
              </div>
              <Icon :icon="expand.attachments ? 'icon-shouqijiantou1' : 'icon-zhankaijiantou1'" class="text-gray-400" />
            </div>
            <div v-show="expand.attachments">
              <Attachment :caseInfo="selectedCaseInfo" :hideTitle="true" />
            </div>
          </div>
        </div>
      </div>
    </div>

    <SelectCaseModal
      v-model:visible="isSelectCaseModalVisible"
      v-bind="props"
      @ok="handleCaseSelectionConfirm" />
  </div>
</template>

<style scoped>
/* Drawer animation */
.drawer-enter-active,
.drawer-leave-active {
  transition: all 0.3s ease-in-out;
}

.drawer-enter-from,
.drawer-leave-to {
  transform: translateX(100%);
  opacity: 0;
}

/* Responsive design */
@media (max-width: 1024px) {
  .drawer-mobile {
    position: fixed;
    top: 0;
    right: 0;
    bottom: 0;
    left: 0;
    z-index: 50;
  }
}

/* Scrollbar style */
:deep(.ant-table-body) {
  scrollbar-width: thin;
  scrollbar-color: #cbd5e0 #f7fafc;
}

:deep(.ant-table-body)::-webkit-scrollbar {
  height: 6px;
}

:deep(.ant-table-body)::-webkit-scrollbar-track {
  background: #f7fafc;
  border-radius: 3px;
}

:deep(.ant-table-body)::-webkit-scrollbar-thumb {
  background: #cbd5e0;
  border-radius: 3px;
}

:deep(.ant-table-body)::-webkit-scrollbar-thumb:hover {
  background: #a0aec0;
}
</style>
