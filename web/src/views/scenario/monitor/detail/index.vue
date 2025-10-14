<script setup lang="ts">
import { onMounted } from 'vue';
import { useI18n } from 'vue-i18n';
import { Hints, Icon, NoData, Spin } from '@xcan-angus/vue-ui';
import { Button, TabPane, Tabs, Tag } from 'ant-design-vue';
import { BasicProps } from '@/types/types';

import { useMonitorData } from './composables/useMonitorData';
import { useHistoryData } from './composables/useHistoryData';
import { useMonitorActions } from './composables/useMonitorActions';

import Chart from '@/views/scenario/monitor/detail/Chart.vue';
import DebugLog from '@/components/DebugLog/index.vue';
import ExecLog from '@/components/ExecLog/index.vue';

import ScenarioHttpDebugResult from '@/components/ScenarioHttpDebugResult/index.vue';
import ScenarioJdbcDebugResult from '@/components/ScenarioJdbcDebugResult/index.vue';
import ScenarioFtpDebugResult from '@/components/ScenarioFtpDebugResult/index.vue';
import ScenarioWebsocketDebugResult from '@/components/ScenarioWebsocketDebugResult/index.vue';
import ScenarioLdapDebugResult from '@/components/ScenarioLdapDebugResult/index.vue';
import ScenarioMailDebugResult from '@/components/ScenarioMailDebugResult/index.vue';
import ScenarioTcpDebugResult from '@/components/ScenarioTcpDebugResult/index.vue';
import ScenarioSmtpDebugResult from '@/components/ScenarioSmtpDebugResult/index.vue';

import { ScenarioMonitorStatus } from '@/enums/enums';

const { t } = useI18n();

// Component props with proper typing
const props = withDefaults(defineProps<BasicProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  data: undefined
});

// Use composables for separated logic
const {
  dataSource,
  scenarioData,
  scenarioPlugin,
  loading,
  loadData,
  watchDataChanges
} = useMonitorData();

const {
  historyList,
  waitingHistory,
  currentHistoryId,
  historyExecData,
  loadHistoryContent,
  loadHistoryList,
  changeHistory,
  watchCurrentHistory
} = useHistoryData();

const {
  editMonitor,
  runMonitor,
  getStatusColorConfig
} = useMonitorActions();

/**
 * Handle monitor execution with data refresh
 */
const handleRunMonitor = async (data: any) => {
  await runMonitor(data, async () => {
    await loadData(data?.id);
    await loadHistoryList(data?.id);
  });
};

/**
 * Initialize component on mount
 */
onMounted(() => {
  // Watch for data changes and load monitor data
  watchDataChanges(() => props.data);

  // Watch for current history changes and load execution data
  watchCurrentHistory();

  // Load initial data if available
  if (props.data?.id) {
    loadData(props.data.id);
    loadHistoryList(props.data.id);
  }
});

// Status color configuration
const statusColorConfig = getStatusColorConfig();
</script>

<template>
  <Spin :spinning="loading" class="h-full text-3 leading-5 px-5 py-5 overflow-auto">
    <div class="bg-gray-light py-2 px-4 flex space-x-4 items-center rounded">
      <div class="text-center ml-3 mr-5">
        <div class="font-semibold text-8 text-text-sub-content mb-2">
          {{ dataSource?.count?.totalNum }}
        </div>
        {{ t('common.total') }}
      </div>

      <div class="flex-1 min-w-0">
        <div class="space-x-2 space-y-5">
          <span class="text-text-title font-semibold text-4">
            {{ dataSource?.name }}
          </span>

          <Tag
            v-if="dataSource?.status?.value"
            :color="statusColorConfig[dataSource?.status?.value]">
            {{ dataSource?.status?.message }}
          </Tag>
        </div>

        <div>
          <template v-if="dataSource?.description">{{ dataSource?.description }}</template>
          <span v-else class="text-text-sub-content">{{ t('common.noDescription') }}</span>
        </div>

        <div class="flex items-center">
          <span class="font-semibold">{{ dataSource?.lastModifiedByName }}</span>
          <span class="ml-1">{{ t('scenarioMonitor.detail.lastModified') }} {{ dataSource?.lastModifiedDate }}</span>

          <div class="ml-10">
            <Button
              size="small"
              type="text"
              :title="t('actions.execute')"
              @click="handleRunMonitor(dataSource)">
              <Icon icon="icon-zhihang" />
            </Button>

            <Button
              size="small"
              type="text"
              :title="t('actions.edit')"
              @click="editMonitor(dataSource as any)">
              <Icon icon="icon-xiugai" />
            </Button>
          </div>
        </div>
      </div>
    </div>

    <div class="flex mt-5">
      <div class="flex-1">
        <div class="title-backend relative pl-2 text-text-title font-semibold text-3.5">
          {{ t('scenarioMonitor.detail.successRate') }}
        </div>

        <div class="flex py-2 space-x-6 items-center">
          <Chart class="w-80" :count="dataSource?.count" />

          <div class="text px-2 space-y-1 flex flex-col text-center">
            <span class=" font-medium">{{ t('scenarioMonitor.detail.timeRanges.last24Hours') }}</span>
            <span class="text-4 font-medium">{{ dataSource?.count?.last24HoursSuccessRate }}%</span>
            <span class="text-text-sub-content">{{ `${dataSource?.count?.last24HoursSuccessNum} / ${dataSource?.count?.last24HoursNum}` }}</span>
          </div>

          <div class="text px-2 space-y-1 flex flex-col text-center">
            <span class=" font-medium">{{ t('quickSearch.last7Days') }}</span>
            <span class="text-4 font-medium">{{ dataSource?.count?.last7DaySuccessRate }}%</span>
            <span class="text-text-sub-content">{{ `${dataSource?.count?.last7DaySuccessNum} / ${dataSource?.count?.last7DayNum}` }}</span>
          </div>

          <div class="text px-2 space-y-1 flex flex-col text-center">
            <span class=" font-medium">{{ t('quickSearch.last30Days') }}</span>
            <span class="text-4 font-medium">{{ dataSource?.count?.last30DaySuccessRate }}%</span>
            <span class="text-text-sub-content">{{ `${dataSource?.count?.last30DaySuccessNum} / ${dataSource?.count?.last30DayNum}` }}</span>
          </div>
        </div>
      </div>

      <div class="flex-1">
        <div class="title-backend relative pl-2 text-text-title font-semibold text-3.5">
          {{ t('scenarioMonitor.detail.responseDelay') }}
        </div>

        <div class="flex  py-5 space-x-6 items-center">
          <div class="text px-2 space-y-1 flex flex-col text-center">
            <span class="text-4 font-medium">{{ dataSource?.count?.avgDelayTime }}</span>
            <span class=" font-medium">{{ t('chart.average') }}</span>
          </div>

          <div class="text px-2 space-y-1 flex flex-col text-center">
            <span class="text-4 font-medium">{{ dataSource?.count?.minDelayTime }}</span>
            <span class=" font-medium">{{ t('chart.min') }}</span>
          </div>

          <div class="text px-2 space-y-1 flex flex-col text-center">
            <span class="text-4 font-medium">{{ dataSource?.count?.maxDelayTime }}</span>
            <span class=" font-medium">{{ t('chart.max') }}</span>
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

    <div class="mt-5">
      <div class="title-backend relative pl-2 flex items-center">
        <span class="text-text-title font-semibold text-3.5 mr-2">
          {{ t('common.executionRecord') }}
        </span>

        <Hints :text="t('scenarioMonitor.detail.executionRecordsHint')" />
      </div>

      <div class="flex mt-4 flex-wrap">
        <div
          v-for="history in historyList"
          :key="history.id"
          class="w-4 h-6 border mb-2  text-center mr-2"
          :class="[history.status.value, currentHistoryId === history.id
            ? 'border-blue-1 shadow-lg' : 'border-transparent', loadHistoryContent
            ? 'cursor-not-allowed' : 'cursor-pointer' ]"
          @click="changeHistory(history)">
          <Icon
            v-show="currentHistoryId === history.id"
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
        <template v-if="historyExecData?.status?.value === ScenarioMonitorStatus.SUCCESS">
          {{ t('scenarioMonitor.detail.in') }} {{ historyExecData?.execStartDate }} {{ t('status.success') }}
        </template>

        <template v-if="historyExecData?.status?.value === ScenarioMonitorStatus.FAILURE">
          {{ t('scenarioMonitor.detail.in') }} {{ historyExecData?.execStartDate }} {{ t('status.failed') }}，
          {{ t('common.reason') }}：{{ historyExecData?.failureMessage || '--' }}
        </template>
      </div>

      <Tabs size="small" class="mt-5">
        <TabPane key="result" :tab="t('common.debugResult')">
          <template v-if="scenarioPlugin === 'Http'">
            <ScenarioHttpDebugResult
              v-if="historyExecData?.sampleContents"
              :content="historyExecData?.sampleContents || []" />
            <NoData
              v-else
              size="small"
              class="mt-10" />
          </template>

          <template v-if="scenarioPlugin === 'Jdbc'">
            <ScenarioJdbcDebugResult
              v-if="historyExecData?.sampleContents"
              :content="historyExecData?.sampleContents || []" />
            <NoData
              v-else
              size="small"
              class="mt-10" />
          </template>

          <template v-if="scenarioPlugin === 'Ftp'">
            <ScenarioFtpDebugResult
              v-if="historyExecData?.sampleContents"
              :content="historyExecData?.sampleContents || []" />
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
            <ScenarioSmtpDebugResult
              v-if="historyExecData?.sampleContents"
              :value="scenarioData"
              :content="historyExecData?.sampleContents || []" />
            <NoData
              v-else
              size="small"
              class="mt-10" />
          </template>
        </TabPane>

        <TabPane key="debuglog" :tab="t('scenarioMonitor.detail.tabs.scheduleLog')">
          <DebugLog
            v-if="historyExecData?.schedulingResult"
            :value="historyExecData?.schedulingResult" />
          <NoData
            v-else
            size="small"
            class="mt-10" />
        </TabPane>

        <TabPane key="execlog" :tab="t('common.execLog')">
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
