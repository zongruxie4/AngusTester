<script lang="ts" setup>
import { defineAsyncComponent, onMounted, ref, watch } from 'vue';
import { http, TESTER } from '@xcan-angus/tools';
import { Icon, modal, Table, TaskPriority } from '@xcan-angus/vue-ui';
import { Button } from 'ant-design-vue';

const ModuleTree = defineAsyncComponent(() => import('./moduleTree.vue'));
const SelectCaseModal = defineAsyncComponent(() => import('@/views/function/baseline/edit/selectCaseModal.vue'));

// const SelectCaseModal = defineAsyncComponent(() => import('@/views/function/review/edit/selectCaseModal.vue'));
// const ReviewForm = defineAsyncComponent(() => import('@/views/function/review/case/reviewForm.vue'));
const CaseReviewResult = defineAsyncComponent(() => import('@/views/function/review/components/caseReviewResult/index.vue'));
const CaseStep = defineAsyncComponent(() => import('@/views/function/case/list/case/add/caseSteps.vue'));
const CaseBasicInfo = defineAsyncComponent(() => import('@/views/function/review/components/caseBasicInfo/index.vue'));
const Precondition = defineAsyncComponent(() => import('@/views/function/review/components/precondition/index.vue'));
const Members = defineAsyncComponent(() => import('@/views/function/review/components/member/index.vue'));
const TestInfo = defineAsyncComponent(() => import('@/views/function/review/components/testInfo/index.vue'));
const Attachment = defineAsyncComponent(() => import('@/views/function/review/components/attachment/index.vue'));
const AssocTasks = defineAsyncComponent(() => import('@/views/function/review/components/assocTask/index.vue'));
const AssocCases = defineAsyncComponent(() => import('@/views/function/review/components/assocCase/index.vue'));
const Description = defineAsyncComponent(() => import('@/views/function/review/components/description/index.vue'));
const Search = defineAsyncComponent(() => import('./search.vue'));

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
    title: '编码',
    dataIndex: 'code'
  },
  {
    title: '名称',
    dataIndex: 'name'
  },
  {
    title: '优先级',
    dataIndex: 'priority'
  },
  {
    title: '版本',
    dataIndex: 'version'
  },
  {
    title: '创建人',
    dataIndex: 'createdByName'
  },
  {
    title: '创建时间',
    dataIndex: 'createdDate'
  },
  {
    title: '操作',
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
  const [error] = await http.post(`${TESTER}/func/baseline/${props.baselineId}/case`, caseIds);
  if (error) {
    return;
  }
  loadBaseLineCaseList();
};

const loadBaseLineCaseList = async () => {
  const { current, pageSize } = pagination.value;
  const [error, { data }] = await http.get(`${TESTER}/func/baseline/${props.baselineId}/case/search`, {
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
    content: `确认取消关联用例【${record.name}】吗？`,
    onOk () {
      return http.del(`${TESTER}/func/baseline/${props.baselineId}/case`, [record.id], {
        dataType: true
      })
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
        const [error, { data }] = await http.get(`${TESTER}/func/baseline/${props.baselineId}/case/${record.id}`);
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
  const [error, res] = await http.get(`${TESTER}/func/baseline/${props.baselineId}`);
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
        :establishedFlag="baselineInfo?.establishedFlag"
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
              取消
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
          测试步骤
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
