<script setup lang="ts">
import { inject, onMounted, ref, watch } from 'vue';
import { Hints, Icon, modal, NoData, Spin } from '@xcan-angus/vue-ui';
import { TESTER, CTRL, http } from '@xcan-angus/tools';
import { Button, TabPane, Tabs, Tag } from 'ant-design-vue';
import { DebugLog, ExecLog, ScenarioHttpDebugResult, ScenarioJdbcDebugResult, ScenarioFtpDebugResult, ScenarioWebsocketDebugResult, ScenarioLdapDebugResult, ScenarioMailDebugResult, ScenarioTcpDebugResult, ScearioSmtpDebugResult } from 'angus-design';

import { MonitorInfo } from '../PropsType';
import Chart from '@/views/scenario/monitor/detail/chart/index.vue';
// export { default as ScearioSmtpDebugResult } from "./ScearioSmtpDebugResult/index";
// export { default as ScenarioFtpDebugResult } from "./ScenarioFtpDebugResult/index";
// export { default as ScenarioHttpDebugResult } from "./ScenarioHttpDebugResult/index";
// export { default as ScenarioJdbcDebugResult } from "./ScenarioJdbcDebugResult/index";
// export { default as ScenarioLdapDebugResult } from "./ScenarioLdapDebugResult/index";
// export { default as ScenarioMailDebugResult } from "./ScenarioMailDebugResult/index";
// export { default as ScenarioTcpDebugResult } from "./ScenarioTcpDebugResult/index";
// export { default as ScenarioWebsocketDebugResult } from "./ScenarioWebsocketDebugResult/index";

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
const addTabPane = inject<(keys: string[]) => void>('addTabPane', () => ({}));

const dataSource = ref<MonitorInfo>();
const historyList = ref<{
  createdBy: string;
  createdByName: string;
  createdDate: string;
  execEndDate: string;
  execId: string;
  execStartDate: string;
  failureMessage: string;
  id: string;
  monitorId: string;
  projectId: string;
  responseDelay: string;
  status: {message: string; value: string};
}[]>([]);

const waitingHistory = ref<number[]>([]);

const currentHistoyId = ref();
const currentExecId = ref();
const historyExecData = ref();
const debugExecInfo = ref();
const scenarioPlugin = ref();

const loadHIstoryContent = ref(false);

const loading = ref(false);
const loadData = async (id: string) => {
  if (loading.value) {
    return;
  }

  loading.value = true;
  const [error, res] = await http.get(`${TESTER}/scenario/monitor/${id}`);
  loading.value = false;
  if (error) {
    return;
  }

  const data = res?.data as MonitorInfo;
  if (!data) {
    return;
  }
  dataSource.value = res.data;
  if (dataSource.value?.scenarioId) {
    loadScenarioPlugin(dataSource.value?.scenarioId);
  }
};

const scenarioData = ref({});
// 获取场景详情
const loadScenarioPlugin = async (scenarioId: string) => {
  const [error, { data }] = await http.get(`${TESTER}/scenario/${scenarioId}`);
  if (error) {
    return;
  }
  scenarioData.value = data;
  scenarioPlugin.value = data.plugin;
};

// 历史执行记录
const loadHistoryList = async (id) => {
  const [error, { data }] = await http.get(`${TESTER}/scenario/monitor/history`, {
    monitorId: id,
    pageSize: 100,
    pageNo: 1
  });
  if (error) {
    return;
  }
  historyList.value = data?.list || [];
  if (!currentHistoyId.value) {
    currentHistoyId.value = historyList.value[0]?.id;
    currentExecId.value = historyList.value[0]?.execId;
  }
  waitingHistory.value = Array.from(new Array(100 - historyList.value.length).fill(0));
};
const changeHistory = (history: {id: string; execId: string}) => {
  if (loadHIstoryContent.value) {
    return;
  }
  currentHistoyId.value = history.id;
  currentExecId.value = history.execId;
};

// 执行记录内容
const loadExecData = async () => {
  loadHIstoryContent.value = true;
  const [error, { data }] = await http.get(`${TESTER}/scenario/monitor/history/${currentHistoyId.value}`);
  loadHIstoryContent.value = false;
  if (error) {
    return;
  }
  historyExecData.value = data;
  if (historyExecData.value?.sampleLogContent) {
    historyExecData.value.sampleLogContent = historyExecData.value.sampleLogContent.replaceAll('\\n', '\n');
  }
};

// 打开编辑
const editMonitor = (data: MonitorInfo) => {
  addTabPane({
    value: 'monitorEdit',
    _id: data.id,
    id: data.id,
    data,
    name: data.name
  });
};

// 执行
const run = async (data: MonitorInfo) => {
  modal.confirm({
    content: `立即执行监控【${data.name}】`,
    async onOk () {
      const id = data.id;
      const [error] = await http.post(`${TESTER}/scenario/monitor/${id}/run`);
      if (error) {
        return;
      }
      loadData(data?.id);
      loadHistoryList(data?.id);
    }
  });
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
    await loadData(id);
    await loadHistoryList(id);
  }, { immediate: true });

  watch(() => currentHistoyId.value, () => {
    loadExecData();
  });
});

const statusColorConfig = {
  SUCCESS: 'success',
  PENDING: 'processing',
  FAILURE: 'default'
};

</script>

<template>
  <Spin :spinning="loading" class="h-full text-3 leading-5 px-5 py-5 overflow-auto">
    <div class="bg-gray-light py-2 px-4 flex space-x-4 items-center rounded">
      <div class="text-center">
        <div class="font-semibold text-8 text-text-sub-content mb-2">{{ dataSource?.count?.totalNum }}</div>
        总执行次数
      </div>

      <div class="flex-1 min-w-0 space-y-2">
        <div class="space-x-2">
          <span class="text-text-title font-semibold text-4">{{ dataSource?.name }}</span>
          <Tag v-if="dataSource?.status?.value" :color="statusColorConfig[dataSource?.status?.value]">{{ dataSource?.status?.message }}</Tag>
        </div>

        <div>
          <template v-if="dataSource?.description">{{ dataSource?.description }}</template>
          <span v-else class="text-text-sub-content">无描述~</span>
        </div>

        <div class="flex items-center">
          <span class="font-semibold">{{ dataSource?.lastModifiedByName }}</span><span class="ml-1">最后修改于{{ dataSource?.lastModifiedDate }}</span>
          <div class="ml-10">
            <Button size="small" type="text">
              <Icon icon="icon-zhihang" @click="run(dataSource as MonitorInfo)" />
            </Button>
            <Button
              size="small"
              type="text"
              @click="editMonitor(dataSource as MonitorInfo)">
              <Icon icon="icon-xiugai" />
            </Button>
          </div>
        </div>
      </div>
    </div>

    <div class="flex mt-4">
      <div class="flex-1">
        <div class="title-backend relative pl-2 text-text-title font-semibold text-3.5">成功率</div>
        <div class="flex py-2 space-x-6 items-center">
          <Chart class="w-80" :count="dataSource?.count" />
          <div class="text px-2 space-y-1 flex flex-col text-center">
            <span class=" font-medium">近24小时</span>
            <span class="text-4 font-medium">{{ dataSource?.count?.last24HoursSuccessRate }}%</span>
            <span class="text-text-sub-content">{{ `${dataSource?.count?.last24HoursSuccessNum} / ${dataSource?.count?.last24HoursNum}` }}</span>
          </div>

          <div class="text px-2 space-y-1 flex flex-col text-center">
            <span class=" font-medium">近7天</span>
            <span class="text-4 font-medium">{{ dataSource?.count?.last7DaySuccessRate }}%</span>
            <span class="text-text-sub-content">{{ `${dataSource?.count?.last7DaySuccessNum} / ${dataSource?.count?.last7DayNum}` }}</span>
          </div>

          <div class="text px-2 space-y-1 flex flex-col text-center">
            <span class=" font-medium">近30天</span>
            <span class="text-4 font-medium">{{ dataSource?.count?.last30DaySuccessRate }}%</span>
            <span class="text-text-sub-content">{{ `${dataSource?.count?.last30DaySuccessNum} / ${dataSource?.count?.last30DayNum}` }}</span>
          </div>
        </div>
      </div>

      <div class="flex-1">
        <div class="title-backend relative pl-2 text-text-title font-semibold text-3.5">响应延迟</div>
        <div class="flex  py-5 space-x-6 items-center">
          <div class="text px-2 space-y-1 flex flex-col text-center">
            <span class="text-4 font-medium">{{ dataSource?.count?.avgDelayTime }}</span>
            <span class=" font-medium">平均</span>
          </div>

          <div class="text px-2 space-y-1 flex flex-col text-center">
            <span class="text-4 font-medium">{{ dataSource?.count?.minDelayTime }}</span>
            <span class=" font-medium">最小</span>
          </div>

          <div class="text px-2 space-y-1 flex flex-col text-center">
            <span class="text-4 font-medium">{{ dataSource?.count?.maxDelayTime }}</span>
            <span class=" font-medium">最大</span>
          </div>

          <div class="text px-2 space-y-1 flex flex-col text-center">
            <span class="text-4 font-medium">{{ dataSource?.count?.p50DelayTime }}</span>
            <span class=" font-medium">P50</span>
          </div>

          <div class="text px-2 space-y-1 flex flex-col text-center">
            <span class="text-4 font-medium">{{ dataSource?.count?.p75DelayTime }}</span>
            <span class=" font-medium">P75</span>
          </div>

          <div class="text px-2 space-y-1 flex flex-col text-center">
            <span class="text-4 font-medium">{{ dataSource?.count?.p90DelayTime }}</span>
            <span class=" font-medium">P90</span>
          </div>
        </div>
      </div>
    </div>

    <div class="mt-4">
      <div class="title-backend relative pl-2 flex items-center">
        <span class="text-text-title font-semibold text-3.5 mr-2">执行记录</span>
        <Hints text="注意：只展示近100次监控记录。" />
      </div>

      <div class="flex mt-4 flex-wrap">
        <div
          v-for="history in historyList"
          :key="history.id"
          class="w-4 h-6 border mb-2  text-center mr-2"
          :class="[history.status.value, currentHistoyId === history.id ? 'border-blue-1 shadow-lg' : 'border-transparent', loadHIstoryContent ? 'cursor-not-allowed' : 'cursor-pointer' ]"
          @click="changeHistory(history)">
          <Icon
            v-show="currentHistoyId === history.id"
            icon="icon-dangqianxuanzhong"
            class="text-3.5 text-status-pending" />
        </div>
        <div
          v-for="(_, idx) in waitingHistory"
          :key="idx"
          class="w-4 h-6 border mb-2 border-transparent text-center  mr-2 bg-gray-light-b">
        </div>
      </div>

      <div class="text-text-title font-medium">
        <template v-if="historyExecData?.status?.value === 'SUCCESS'">
          在 {{ historyExecData?.execStartDate }} 运行成功
        </template>
        <template v-if="historyExecData?.status?.value === 'FAILURE'">
          在 {{ historyExecData?.execStartDate }} 运行失败，原因：{{ historyExecData?.failureMessage || '--' }}
        </template>
      </div>

      <Tabs size="small">
        <TabPane key="result" tab="调试结果">
          <template v-if="scenarioPlugin === 'Http'">
            <ScenarioHttpDebugResult v-if="historyExecData?.sampleContents" :content="historyExecData?.sampleContents || []" />
            <NoData
              v-else
              size="small"
              class="mt-10" />
          </template>
          <template v-if="scenarioPlugin === 'Jdbc'">
            <ScenarioJdbcDebugResult v-if="historyExecData?.sampleContents" :content="historyExecData?.sampleContents || []" />
            <NoData
              v-else
              size="small"
              class="mt-10" />
          </template>

          <template v-if="scenarioPlugin === 'Ftp'">
            <ScenarioFtpDebugResult v-if="historyExecData?.sampleContents" :content="historyExecData?.sampleContents || []" />
            <NoData
              v-else
              size="small"
              class="mt-10" />
          </template>

          <template v-if="scenarioPlugin === 'WebSocket'">
            <ScenarioWebsocketDebugResult
              v-if="historyExecData?.sampleContents"
              :value="scenarioData"
              :content="historyExecData?.sampleContents || []" />
            <NoData
              v-else
              size="small"
              class="mt-10" />
          </template>

          <template v-if="scenarioPlugin === 'Ldap'">
            <ScenarioLdapDebugResult
              v-if="historyExecData?.sampleContents"
              :value="scenarioData"
              :content="historyExecData?.sampleContents || []" />
            <NoData
              v-else
              size="small"
              class="mt-10" />
          </template>

          <template v-if="scenarioPlugin === 'Mail'">
            <ScenarioMailDebugResult
              v-if="historyExecData?.sampleContents"
              :value="scenarioData"
              :content="historyExecData?.sampleContents || []" />
            <NoData
              v-else
              size="small"
              class="mt-10" />
          </template>

          <template v-if="scenarioPlugin === 'Tcp'">
            <ScenarioTcpDebugResult
              v-if="historyExecData?.sampleContents"
              :value="scenarioData"
              :content="historyExecData?.sampleContents || []" />
            <NoData
              v-else
              size="small"
              class="mt-10" />
          </template>
          <template v-if="scenarioPlugin === 'Smtp'">
            <ScearioSmtpDebugResult
              v-if="historyExecData?.sampleContents"
              :value="scenarioData"
              :content="historyExecData?.sampleContents || []" />
            <NoData
              v-else
              size="small"
              class="mt-10" />
          </template>
        </TabPane>
        <TabPane key="debuglog" tab="调度日志">
          <DebugLog v-if="historyExecData?.schedulingResult" :value="historyExecData?.schedulingResult" />
          <NoData
            v-else
            size="small"
            class="mt-10" />
        </TabPane>
        <TabPane key="execlog" tab="执行日志">
          <ExecLog
            v-if="historyExecData?.schedulingResult"
            :execId="historyExecData?.execId"
            :execNode="historyExecData?.execNode"
            :schedulingResult="historyExecData?.schedulingResult"
            :sampleLogContent="historyExecData?.sampleLogContent" />
          <NoData
            v-else
            size="small"
            class="mt-10" />
        </TabPane>
      </Tabs>
    </div>
  </Spin>
</template>
<style scoped>

.title-backend::before {
  content: '';
  display: inline-block;
  position: absolute;
  top: 2px;
  left: 0;
  width: 3px;
  height: 14px;
  background-color: #07F;
}

.SUCCESS {
  @apply bg-status-success;
}

.PENDING {
  @apply bg-status-pending;
}

.FAILURE {
  @apply bg-status-error1;
}

</style>
