<script lang="ts" setup>
import { defineAsyncComponent, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Icon, modal, Table } from '@xcan-angus/vue-ui';
import { Button } from 'ant-design-vue';
import { func } from '@/api/tester';
import TaskPriority from '@/components/TaskPriority/index.vue';

const { t } = useI18n();

const ModuleTree = defineAsyncComponent(() => import('./ModuleTree.vue'));
const SelectCaseModal = defineAsyncComponent(() => import('@/views/function/baseline/edit/SelectCaseModal.vue'));

// const SelectCaseModal = defineAsyncComponent(() => import('@/views/function/review/edit/selectCaseModal.vue'));
// const ReviewForm = defineAsyncComponent(() => import('@/views/function/review/case/reviewForm.vue'));
const CaseReviewResult = defineAsyncComponent(() => import('@/views/function/review/components/CaseReviewResult.vue'));
const CaseStep = defineAsyncComponent(() => import('@/views/function/case/list/case/CaseSteps.vue'));
const CaseBasicInfo = defineAsyncComponent(() => import('@/views/function/review/components/CaseBasicInfo.vue'));
const Precondition = defineAsyncComponent(() => import('@/views/function/review/components/Precondition.vue'));
const Members = defineAsyncComponent(() => import('@/views/function/review/components/Member.vue'));
const TestInfo = defineAsyncComponent(() => import('@/views/function/review/components/TestInfo.vue'));
const Attachment = defineAsyncComponent(() => import('@/views/function/review/components/Attachment.vue'));
const AssocTasks = defineAsyncComponent(() => import('@/views/function/review/components/AssocTask.vue'));
const AssocCases = defineAsyncComponent(() => import('@/views/function/review/components/AssocCase.vue'));
const Description = defineAsyncComponent(() => import('@/views/function/review/components/Description.vue'));
const Search = defineAsyncComponent(() => import('./Search.vue'));

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

const columns = [
  {
    title: t('functionBaseline.case.code'),
    dataIndex: 'code'
  },
  {
    title: t('functionBaseline.case.name'),
    dataIndex: 'name'
  },
  {
    title: t('functionBaseline.case.priority'),
    dataIndex: 'priority'
  },
  {
    title: t('functionBaseline.case.version'),
    dataIndex: 'version'
  },
  {
    title: t('functionBaseline.case.creator'),
    dataIndex: 'createdByName'
  },
  {
    title: t('functionBaseline.case.createTime'),
    dataIndex: 'createdDate'
  },
  {
    title: t('functionBaseline.case.operation'),
    dataIndex: 'action'
  }
];

const baselineInfo = ref();

const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0
});
const filters = ref([]);

const selectCaseVisible = ref(false);

const moduleId = ref('');
const baselineCaseList = ref([]);

const selectCaseOk = async (caseIds: string[] = []) => {
  selectCaseVisible.value = false;
  const [error] = await func.addBaselineCase(props.baselineId, caseIds);
  if (error) {
    return;
  }
  loadBaseLineCaseList();
};

const loadBaseLineCaseList = async () => {
  const { current, pageSize } = pagination.value;
  const [error, { data }] = await func.getBaselineCaseList(props.baselineId, {
    moduleId: moduleId.value,
    projectId: props.projectId,
    pageNo: current,
    pageSize,
    filters: filters.value
  });
  if (error) {
    return;
  }
  baselineCaseList.value = data?.list || [];
  pagination.value.total = +data.total || 0;
};

const onPageChang = ({ current, pageSize }) => {
  pagination.value.current = current;
  pagination.value.pageSize = pageSize;
  loadBaseLineCaseList();
};

const handleAddCase = () => {
  selectCaseVisible.value = true;
};

const delCase = (record) => {
  modal.confirm({
    content: t('functionBaseline.case.confirmUnlinkCase', { name: record.name }),
    onOk () {
      return func.deleteBaselineCase(props.baselineId, [record.id])
        .then(([error]) => {
          if (error) {
            return;
          }
          if (pagination.value.current > 1 && baselineCaseList.value.length === 1) {
            pagination.value.current -= 1;
          }
          if (selectedRowKey.value === record.id) {
            selectedRowKey.value = undefined;
          }
          loadBaseLineCaseList();
        });
    }
  });
};

const selectedRowKey = ref();
const selectCaseInfo = ref();
const putCustom = (record) => {
  return {
    onClick: async () => {
      if (record.id === selectedRowKey.value) {
        selectedRowKey.value = '';
      } else {
        selectedRowKey.value = record.id;
        // selectCaseInfo.value = record;
        const [error, { data }] = await func.getBaselineCaseDetail(props.baselineId, record.id);
        if (error) {
          selectCaseInfo.value = record;
          return;
        }
        selectCaseInfo.value = data;
      }
    }
  };
};

const onCloseDrawer = () => {
  selectedRowKey.value = '';
  // caseDetailVisible.value = false;
};

const handleSearchChange = (params) => {
  const { pageNo, pageSize } = params;
  pagination.value.current = pageNo;
  pagination.value.pageSize = pageSize;
  filters.value = params.filters;
  loadBaseLineCaseList();
};

const loadBaseLineData = async () => {
  const [error, res] = await func.getBaselineDetail(props.baselineId);
  if (error) {
    return;
  }

  const data = res?.data;
  if (!data) {
    return;
  }
  baselineInfo.value = data;
};

onMounted(() => {
  loadBaseLineData();
  loadBaseLineCaseList();
  watch(() => moduleId.value, () => {
    loadBaseLineCaseList();
  });
});

</script>
<template>
  <div class="flex space-x-2 h-full">
    <div
      class="w-70">
      <ModuleTree
        v-model:moduleId="moduleId"
        v-bind="props" />
    </div>
    <div class="flex-1">
      <Search
        :established="baselineInfo?.established"
        @handleAddCase="handleAddCase"
        @change="handleSearchChange" />
      <Table
        noDataSize="small"
        :columns="columns"
        :dataSource="baselineCaseList"
        :rowClassName="(record) => record.id === selectedRowKey ? 'ant-table-row-selected' : ''"
        :pagination="pagination"
        :customRow="putCustom"
        @change="onPageChang">
        <template #bodyCell="{column, record}">
          <template v-if="column.dataIndex === 'priority'">
            <TaskPriority
              :value="record.priority" />
          </template>
          <template v-if="column.dataIndex === 'version'">
            <span>v{{ record.version }}</span>
          </template>
          <template v-if="column.dataIndex === 'action'">
            <Button
              type="text"
              size="small"
              @click.stop="delCase(record)">
              <Icon icon="icon-qingchu" class="mr-1" />
              {{ t('functionBaseline.case.cancel') }}
            </Button>
          </template>
        </template>
      </Table>
    </div>
    <div class="flex flex-col transition-all -mt-4" :class="{'w-0': !selectedRowKey, 'w-100 border-l p-2': !!selectedRowKey}">
      <div>
        <Icon
          icon="icon-shanchuguanbi"
          class="text-5 cursor-pointer"
          @click="onCloseDrawer" />
      </div>

      <div class="flex-1 overflow-auto p-4">
        <CaseBasicInfo
          class="pb-5"
          :caseInfo="selectCaseInfo" />

        <Precondition
          class="py-5"
          :caseInfo="selectCaseInfo" />

        <div class="font-semibold text-3.5">
          {{ t('functionBaseline.case.testSteps') }}
        </div>

        <CaseStep
          class="pb-5 pt-3"
          :defaultValue="selectCaseInfo?.steps || []"
          readonly />

        <CaseReviewResult
          class="py-5"
          :caseInfo="selectCaseInfo" />

        <Description
          class="py-5"
          :caseInfo="selectCaseInfo" />

        <Members
          class="py-5"
          :caseInfo="selectCaseInfo" />

        <TestInfo
          class="py-5"
          :caseInfo="selectCaseInfo" />

        <AssocTasks
          class="py-5"
          :dataSource="selectCaseInfo?.refTaskInfos"
          :projectId="props.projectId"
          :caseInfo="selectCaseInfo" />

        <AssocCases
          class="py-5"
          :dataSource="selectCaseInfo?.refCaseInfos"
          :projectId="props.projectId"
          :caseInfo="selectCaseInfo" />

        <Attachment
          class="py-5"
          :caseInfo="selectCaseInfo" />
      </div>
    </div>

    <SelectCaseModal
      v-model:visible="selectCaseVisible"
      v-bind="props"
      @ok="selectCaseOk" />
  </div>
</template>
