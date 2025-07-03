<script setup lang="ts">
import { computed, defineAsyncComponent, inject, onMounted, ref, watch } from 'vue';
import { Button, Progress, TabPane, Tabs } from 'ant-design-vue';
import { Colon, Icon, Image, notification, Spin, Table } from '@xcan-angus/vue-ui';
import { TESTER, clipboard, download } from '@xcan-angus/tools';
import { funcPlan } from '@/api/tester';

import { PlanInfo } from '../PropsType';

type Props = {
  projectId: string;
  userInfo: { id: string; };
  appInfo: { id: string; };
  data: {
    _id: string;
    id: string | undefined;
  }
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  data: undefined
});

const RichEditor = defineAsyncComponent(() => import('@/components/richEditor/index.vue'));
const BurnDownChart = defineAsyncComponent(() => import('@/views/function/plan/detail/burndownChart/index.vue'));
const MemberProgress = defineAsyncComponent(() => import('@/views/function/plan/detail/memberProgress/index.vue'));
const WorkCalendar = defineAsyncComponent(() => import('@/views/function/homepage/workCalendar/index.vue'));

const updateTabPane = inject<(data: { [key: string]: any }) => void>('updateTabPane', () => ({}));
const setCaseListPlanParam = inject<(value: any) => void>('setCaseListPlanParam');
const isAdmin = inject('isAdmin', ref(false));

const loading = ref(false);
const exportLoading = ref(false);

const permissions = ref<string[]>([]);
const dataSource = ref<PlanInfo>();
const testerResponsibilities = ref<{id:string;name:string;content:string;}[]>([]);
const completedRate = ref(0);

const goCase = () => {
  setCaseListPlanParam({ ...dataSource.value, planId: dataSource.value?.id, planName: dataSource.value?.name });
};

const loadPermissions = async (id: string) => {
  if (isAdmin.value) {
    permissions.value = [
      'MODIFY_PLAN',
      'DELETE_PLAN',
      'ADD_CASE',
      'MODIFY_CASE',
      'DELETE_CASE',
      'EXPORT_CASE',
      'REVIEW',
      'RESET_REVIEW_RESULT',
      'TEST',
      'RESET_TEST_RESULT',
      'GRANT',
      'VIEW'
    ];

    return;
  }

  const params = {
    admin: true
  };
  loading.value = true;
  const [error, res] = await funcPlan.getCurrentAuthByPlanId(id, params);
  loading.value = false;
  if (error) {
    return;
  }

  permissions.value = (res?.data?.permissions || []).map(item => item.value);
};

const loadData = async (id: string) => {
  if (loading.value) {
    return;
  }

  loading.value = true;
  const [error, res] = await funcPlan.getPlanInfo(id);
  loading.value = false;
  if (error) {
    return;
  }

  const data = res?.data as PlanInfo;
  if (!data) {
    return;
  }

  dataSource.value = data;
  if (dataSource.value.progress?.completedRate) {
    completedRate.value = +dataSource.value.progress.completedRate.replace(/(\d+\.\d{2})\d+/, '$1');
  }

  testerResponsibilities.value = [];
  const members = data.members || [];
  for (let i = 0, len = members.length; i < len; i++) {
    const { id, fullName } = members[i];
    testerResponsibilities.value.push({
      name: fullName,
      ...members[i],
      content: dataSource.value.testerResponsibilities[id]
    });
  }

  const name = data.name;
  if (name && typeof updateTabPane === 'function') {
    updateTabPane({ name, _id: id + '-case' });
  }
};

const toExport = async () => {
  if (!dataSource.value) {
    return;
  }

  const { id, projectId } = dataSource.value;
  if (exportLoading.value) {
    return;
  }

  exportLoading.value = true;
  await download(`${TESTER}/func/case/export?projectId=${projectId}&planId=${id}`);
  exportLoading.value = false;
};

const toCopyHref = () => {
  const message = window.location.origin + '/function#plans?id=' + planId.value;
  clipboard.toClipboard(message).then(() => {
    notification.success('复制成功');
  }).catch(() => {
    notification.error('复制失败');
  });
};

const toRefresh = async () => {
  const id = planId.value;
  if (!id) {
    return;
  }

  await loadPermissions(id);
  await loadData(id);
};

onMounted(() => {
  watch(() => props.data, async (newValue, oldValue) => {
    const id = newValue?.id;
    if (!id) {
      return;
    }

    const oldId = oldValue?.id;
    if (id === oldId) {
      return;
    }

    await loadPermissions(id);
    await loadData(id);
  }, { immediate: true });
});

const planId = computed(() => {
  return dataSource.value?.id;
});

const attachments = computed(() => {
  return dataSource.value?.attachments || [];
});

const columns = [
  // {
  //   dataIndex: 'id',
  //   title: 'ID',
  //   width: '25%',
  //   ellipsis: true
  // },
  {
    dataIndex: 'name',
    title: '姓名',
    width: '25%',
    ellipsis: true
  },
  {
    dataIndex: 'content',
    title: '工作职责',
    ellipsis: true
  }
];
</script>

<template>
  <Spin :spinning="loading" class="h-full text-3 leading-5 px-5 py-5 overflow-auto">
    <div class="flex items-start flex-wrap space-y-b-2 space-x-2.5 mb-3.5">
      <Button
        type="default"
        size="small"
        class="p-0">
        <RouterLink
          class="flex items-center space-x-1 leading-6.5 px-1.75"
          :to="`/function#plans?id=${planId}&type=edit`">
          <Icon icon="icon-shuxie" class="text-3.5" />
          <span>编辑</span>
        </RouterLink>
      </Button>

      <Button
        :disabled="!isAdmin && !permissions.includes('VIEW')"
        type="default"
        size="small"
        class="flex items-center space-x-1"
        @click="goCase">
        <Icon icon="icon-ceshiyongli1" class="text-3.5" />
        <span>查看用例</span>
      </Button>

      <!-- <Button
        type="default"
        size="small"
        class="p-0">
        <RouterLink
          class="flex items-center space-x-1 leading-6.5 px-1.75"
          :to="`/function#cases?planId=${planId}&sprintName=${dataSource?.name}`">
          <Icon icon="icon-renwu2" class="text-3.5" />
          <span>查看用例</span>
        </RouterLink>
      </Button> -->

      <Button
        :disabled="!isAdmin && !permissions.includes('EXPORT_CASE')"
        type="default"
        size="small"
        class="flex items-center space-x-1"
        @click="toExport">
        <Icon icon="icon-daochu" class="text-3.5" />
        <span>导出用例</span>
      </Button>

      <Button
        size="small"
        class="flex items-center"
        @click="toCopyHref">
        <Icon class="mr-1 flex-shrink-0" icon="icon-fuzhi" />
        <span>复制链接</span>
      </Button>

      <Button
        type="default"
        size="small"
        class="flex items-center"
        @click="toRefresh">
        <Icon class="mr-1 flex-shrink-0" icon="icon-shuaxin" />
        <span>刷新</span>
      </Button>
    </div>

    <div class="max-w-250 mb-2">
      <div class="text-theme-title mb-2">基本信息</div>
      <div class="space-y-2.5">
        <div class="flex items-start space-x-5">
          <div class="w-1/2 flex items-start">
            <div class="w-15.5 flex items-center whitespace-nowrap flex-shrink-0">
              <span>计划名称</span>
              <Colon class="w-1" />
            </div>

            <div class="whitespace-pre-wrap break-words break-all">{{ dataSource?.name }}</div>
          </div>

          <div class="w-1/2 flex items-start">
            <div class="w-18.5 flex items-center whitespace-nowrap flex-shrink-0">
              <span>时间计划</span>
              <Colon class="w-1" />
            </div>

            <div class="text-3 whitespace-nowrap">
              <span>{{ dataSource?.startDate }}</span>
              <span class="mx-2">至</span>
              <span>{{ dataSource?.deadlineDate }}</span>
            </div>
          </div>
        </div>

        <div class="flex items-start space-x-5">
          <div class="w-1/2 flex items-start">
            <div class="w-15.5 flex items-center whitespace-nowrap flex-shrink-0">
              <span>负责人</span>
              <Colon class="w-1" />
            </div>

            <div class="whitespace-pre-wrap break-words break-all">{{ dataSource?.ownerName }}</div>
          </div>

          <div class="w-1/2 flex items-start">
            <div class="w-18.5 flex items-center whitespace-nowrap flex-shrink-0">
              <span>用例前缀</span>
              <Colon class="w-1" />
            </div>

            <div class="whitespace-pre-wrap break-words break-all">{{ dataSource?.casePrefix }}</div>
          </div>
        </div>

        <div class="flex items-start space-x-5">
          <div class="w-1/2 flex items-center">
            <div class="w-15.5 flex items-center whitespace-nowrap flex-shrink-0">
              <span>是否评审</span>
              <Colon class="w-1" />
            </div>

            <div class="whitespace-pre-wrap break-words break-all">
              {{ dataSource?.review?'是':'否' }}
            </div>
          </div>

          <div class="w-1/2 flex items-center">
            <div class="w-18.5 flex items-center whitespace-nowrap flex-shrink-0">
              <span>工作量评估</span>
              <Colon class="w-1" />
            </div>

            <div class="whitespace-pre-wrap break-words break-all">
              {{ dataSource?.evalWorkloadMethod?.message }}
            </div>
          </div>
        </div>

        <div class="flex items-start space-x-5">
          <div class="w-1/2 flex items-center">
            <div class="w-15.5 flex items-center whitespace-nowrap flex-shrink-0">
              <span>状态</span>
              <Colon class="w-1" />
            </div>

            <div
              class="text-3 leading-4 flex items-center flex-none whitespace-nowrap mr-3.5">
              <div class="h-1.5 w-1.5 rounded-full mr-1" :class="dataSource?.status?.value"></div>
              <div>{{ dataSource?.status?.message }}</div>
            </div>
          </div>

          <div class="w-1/2 flex items-center">
            <div class="w-18.5 flex items-center whitespace-nowrap flex-shrink-0">
              <span>进度</span>
              <Colon class="w-1" />
            </div>

            <Progress :percent="completedRate" style="width:150px;" />
          </div>
        </div>

        <div class="flex items-start">
          <div style="width:calc(50% - 10px);" class="flex items-start">
            <div class="w-15.5 flex items-center whitespace-nowrap flex-shrink-0">
              <span>附件</span>
              <Colon class="w-1" />
            </div>

            <div class="space-y-1 truncate">
              <a
                v-for="item in attachments"
                :key="item.id"
                class="block w-auto truncate"
                :download="item.name"
                :href="item.url">
                {{ item.name }}
              </a>
            </div>
          </div>
        </div>
      </div>
    </div>

    <Tabs size="small" class="max-w-250">
      <TabPane key="testerResponsibilities" tab="测试人员">
        <Table
          :columns="columns"
          :dataSource="testerResponsibilities"
          :pagination="false">
          <template #bodyCell="{record, column}">
            <template v-if="column.dataIndex === 'name'">
              <div class="flex items-center">
                <Image
                  class="w-5 h-5 mr-1"
                  type="avatar"
                  :src="record.avatar" />
                {{ record.name }}
              </div>
            </template>
          </template>
        </Table>
      </TabPane>
      <TabPane key="testingObjectives" tab="测试目标">
        <div class="space-y-1 whitespace-pre-wrap break-words break-all">
          <!-- {{ dataSource?.testingObjectives }} -->
          <RichEditor
            v-if="dataSource?.otherInformation"
            :value="dataSource.testingObjectives"
            mode="view" />
        </div>
      </TabPane>
      <TabPane key="testingScope" tab="测试范围">
        <div class="space-y-1 whitespace-pre-wrap break-words break-all">
          <!-- {{ dataSource?.testingScope }} -->
          <RichEditor
            v-if="dataSource?.otherInformation"
            :value="dataSource.testingScope"
            mode="view" />
        </div>
      </TabPane>
      <TabPane key="acceptanceCriteria" tab="验收标准">
        <div class="space-y-1 whitespace-pre-wrap break-words break-all">
          <!-- {{ dataSource?.acceptanceCriteria }} -->
          <RichEditor
            v-if="dataSource?.acceptanceCriteria"
            :value="dataSource.acceptanceCriteria"
            mode="view" />
        </div>
      </TabPane>
      <TabPane key="otherInformation" tab="其他说明">
        <div class="space-y-1 whitespace-pre-wrap break-words break-all">
          <RichEditor
            v-if="dataSource?.otherInformation"
            :value="dataSource.otherInformation"
            mode="view" />
        </div>
      </TabPane>
      <TabPane key="chart" tab="燃尽图">
        <BurnDownChart :planId="planId" />
      </TabPane>
      <TabPane key="progress" tab="成员进度">
        <MemberProgress :planId="planId" :projectId="props.projectId" />
      </TabPane>
      <TabPane key="workCalendar" tab="工作日历">
        <WorkCalendar
          :projectId="props.projectId"
          :userInfo="props.userInfo"
          :planId="planId" />
      </TabPane>
    </Tabs>
  </Spin>
</template>

<style scoped>
.PENDING {
  background-color: rgba(45, 142, 255, 100%);
}

.IN_PROGRESS {
  background-color: rgba(103, 215, 255, 100%);
}

.COMPLETED {
  background-color: rgba(82, 196, 26, 100%);
}

.BLOCKED {
  background-color: rgba(255, 165, 43, 100%);
}

.meeting-container {
  border: 1px solid var(--border-text-box);
  border-radius: 4px;
  background-color: #fafafa;
}
</style>
