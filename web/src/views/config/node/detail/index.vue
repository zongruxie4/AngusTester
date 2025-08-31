<script setup lang="ts">
/**
 * Node Detail Page Component
 * <p>
 * This component displays detailed information about a specific node including
 * basic information, resource monitoring, proxy service status, and execution tasks.
 * The component uses composables to separate business logic from UI presentation.
 * </p>
 */

import { defineAsyncComponent, onMounted, ref } from 'vue';
import { useRoute } from 'vue-router';
import { Button, Progress, RadioButton, RadioGroup, TabPane, Tabs } from 'ant-design-vue';
import { Grid, Hints, Icon, IntervalTimestamp, NoData, Select, Spin, Tooltip } from '@xcan-angus/vue-ui';

// Import interfaces and utilities
// Import composables
import { infoItem, installConfigColumns, useNodeData } from './composables/useNodeData';
import { getStrokeColor, internetInfo, nodeUseProgresses, useNodeMetrics } from './composables/useNodeMetrics';
import { nodeEchartsTabs, useNodeCharts } from './composables/useNodeCharts';
import { useNodeActions } from './composables/useNodeActions';

// Import ECharts manager
import { EChartsManager } from './echartsManager';

// Async component imports
const AgentChart = defineAsyncComponent(() => import('./AgentChart.vue'));
const AgentInfo = defineAsyncComponent(() => import('./AgentInfo.vue'));
const AgentLog = defineAsyncComponent(() => import('./AgentLog.vue'));
const Execution = defineAsyncComponent(() => import('./Execution.vue'));
const MockService = defineAsyncComponent(() => import('./MockService.vue'));
const ExecutionPropulsion = defineAsyncComponent(() => import('./ExecutionPropulsion.vue'));

// Route and node ID
const route = useRoute();
const id = ref<string>(route.params.id as string);

// ECharts reference and manager
const echartRef = ref();
const echartsManager = new EChartsManager();

// Initialize composables
const {
  state,
  loadingInfo,
  showPassword,
  showInstallStep,
  showInstallCtrlAccessToken,
  installing,
  enabled,
  activeKey,
  loadInfo,
  handleShowPassword,
  installAgent,
  getInstallStep,
  foldInstallAgent,
  turnback,
  enableNode,
  deleteNode,
  toggleShowCtrlAccessToken,
  getOnlineInstallTip,
  getDeleteTip
} = useNodeData();

const {
  sourceUse,
  networkDeviceData,
  currentDeviceName,
  loadMetrics,
  loadNetwork,
  onDeviceNameChange
} = useNodeMetrics();

const {
  loadingChart,
  currentSourceServer,
  chartParams,
  activeTab,
  showNoData,
  memoryDataOptions,
  diskDataOptions,
  networkDataOptions,
  showMemoryPercentChart,
  diskChartKey,
  networkChartKey,
  diskNames,
  activeDisk,
  networkNames,
  activeNetwork,
  chartServersMap,
  onChartDataChange,
  loadEchartData
} = useNodeCharts(id.value, echartsManager);

const {
  isAdmin,
  tenantInfo,
  userInfo,
  copyContent,
  refreshTimer,
  canPerformActions,
  canDeleteNode,
  canInstallAgent
} = useNodeActions();

// UI state
const proxyActiveKey = ref<'proxy' | 'log'>('proxy');

// Proxy options
const proxyOpt = [
  {
    label: '代理信息',
    value: 'proxy'
  },
  {
    label: '日志',
    value: 'log'
  }
];

/**
 * Initialize component data
 * <p>
 * Loads initial node information, initializes ECharts, and sets up refresh timer.
 * </p>
 */
const initializeComponent = () => {
  id.value = route.params.id as string;

  // Load data sequentially
  loadInfo(id.value).then(() => {
    echartsManager.initEcharts(echartRef.value);
    return loadMetrics(id.value);
  }).then(() => {
    return loadNetwork(id.value);
  }).then(() => {
    // Set initial chart server and load initial chart data
    currentSourceServer.value = chartServersMap.cpu;
    loadEchartData();

    // Start refresh timer
    refreshTimer(() => {
      loadMetrics(id.value);
      loadNetwork(id.value);
    }, activeKey.value);
  }).catch(error => {
    console.error('Failed to initialize component:', error);
  });
};

/**
 * Handle node deletion with confirmation
 * <p>
 * Shows confirmation dialog and deletes the node if confirmed.
 * </p>
 */
const handleDeleteNode = () => {
  deleteNode(id.value, state.infos.name || '');
};

/**
 * Handle agent installation
 * <p>
 * Attempts to install the agent on the node.
 * </p>
 */
const handleInstallAgent = () => {
  installAgent(id.value);
};

/**
 * Handle manual installation steps
 * <p>
 * Fetches and displays manual installation instructions.
 * </p>
 */
const handleGetInstallStep = () => {
  getInstallStep(id.value);
};

/**
 * Handle node enable/disable toggle
 * <p>
 * Toggles the node's enabled state.
 * </p>
 */
const handleEnableNode = () => {
  enableNode(id.value);
};

// Initialize component on mount
onMounted(() => {
  initializeComponent();
});
</script>

<template>
  <div class="h-full overflow-auto">
    <Spin :spinning="loadingInfo" mask>
      <div class="bg-white px-10 text-3">
        <!-- Basic Information Section -->
        <div class="basic">
          <div class="title flex justify-between pt-2.5 items-center">
            <span class="mr-15 font-semibold">基本信息</span>
            <div class="detai-btns-wrapper">
              <!-- Enable/Disable Button -->
              <Button
                v-if="!state.infos.enabled"
                class="node-action-btn"
                :loading="enabled"
                :disabled="!canPerformActions(state.infos?.tenantId, state.infos?.createdBy)"
                @click="handleEnableNode">
                <Icon icon="icon-qiyong" />{{ '启用' }}
              </Button>
              <Button
                v-else
                class="node-action-btn"
                :loading="enabled"
                :disabled="!canPerformActions(state.infos?.tenantId, state.infos?.createdBy)"
                @click="handleEnableNode">
                <Icon icon="icon-jinyong" />{{ '禁用' }}
              </Button>

              <!-- Delete Button -->
              <Tooltip v-if="!canDeleteNode(state.infos)" :title="getDeleteTip(state.infos, isAdmin, tenantInfo, userInfo)">
                <Button class="node-action-btn" :disabled="true">
                  <Icon icon="icon-qingchu" class="mr-1" />
                  <span>{{ '删除' }}</span>
                </Button>
              </Tooltip>
              <Button
                v-else
                class="node-action-btn"
                @click="handleDeleteNode">
                <Icon icon="icon-qingchu" class="mr-1" />
                <span>{{ '删除' }}</span>
              </Button>

              <!-- Online Install Agent Button -->
              <Tooltip v-if="state.infos.installNodeAgent || state.infos.free || !canInstallAgent()" :title="getOnlineInstallTip(state, isAdmin)">
                <Button
                  :disabled="true"
                  :loading="installing"
                  class="node-action-btn"
                  @click="handleInstallAgent">
                  <Icon icon="icon-anzhuangdaili" />
                  {{ '在线安装代理' }}
                </Button>
              </Tooltip>
              <Button
                v-else
                :disabled="state.infos.installNodeAgent || !canInstallAgent()"
                :loading="installing"
                class="node-action-btn"
                @click="handleInstallAgent">
                <Icon icon="icon-anzhuangdaili" />{{ '在线安装代理' }}
                <Hints
                  v-if="installing"
                  class="absolute left-5 -bottom-3"
                  :text="'预计需要几分钟时间'" />
              </Button>

              <!-- Manual Install Agent Button -->
              <Tooltip v-if="!canInstallAgent() || state.infos.free" :title="!canInstallAgent() ? '安装代理需要系统管理员或应用管理员权限' : ''">
                <Button
                  :disabled="true"
                  class="node-action-btn"
                  @click="handleGetInstallStep">
                  <Icon icon="icon-anzhuangdaili" />{{ '手动安装代理' }}
                </Button>
              </Tooltip>
              <Button
                v-else
                :disabled="!canInstallAgent()"
                class="node-action-btn"
                @click="handleGetInstallStep">
                <Icon icon="icon-anzhuangdaili" />{{ '手动安装代理' }}
              </Button>

              <!-- Back Button -->
              <Button class="node-action-btn" @click="turnback">
                <Icon icon="icon-fanhui" />{{ '返回' }}
              </Button>
            </div>
          </div>

          <!-- Manual Installation Steps -->
          <div v-show="showInstallStep" class="pt-4 mb-4 relative">
            <Tabs size="small">
              <TabPane key="linux" tab="Linux">
                <div class="text-3">请按照以下步骤手动安装代理：</div>
                <div class="text-3">
                  方法1：复制以下命令到目标服务器执行
                  <Icon
                    icon="icon-fuzhi"
                    class="cursor-pointer text-3.5 text-blue-1"
                    @click="copyContent(state.linuxOfflineInstallSteps?.onlineInstallCmd || '')" />
                  <p class="install-step whitespace-pre-line">
                    {{ state.linuxOfflineInstallSteps?.onlineInstallCmd }}
                  </p>
                  方法2：下载安装脚本
                  <p class="install-step whitespace-pre-line">
                    下载脚本：<a class="cursor-pointer" :href="state.linuxOfflineInstallSteps?.installScriptUrl">{{ state.linuxOfflineInstallSteps?.installScriptName }}</a> <br />
                    复制脚本到目标服务器：{{ state.linuxOfflineInstallSteps?.installScriptName }}<br />
                    执行安装命令：{{ state.linuxOfflineInstallSteps?.runInstallCmd }}
                  </p>
                </div>
              </TabPane>
              <TabPane key="config" tab="配置信息">
                <Grid
                  :dataSource="state.installConfig"
                  :columns="installConfigColumns">
                  <template #tenantId="{text}">
                    <div class="flex items-center">
                      <span>{{ text }}</span>
                      <Button
                        type="link"
                        size="small"
                        @click="copyContent(text)">
                        <Icon icon="icon-fuzhi" class="ml-0.5" />
                      </Button>
                    </div>
                  </template>
                  <template #deviceId="{text}">
                    <div class="flex items-center">
                      <span>{{ text }}</span>
                      <Button
                        type="link"
                        size="small"
                        @click="copyContent(text)">
                        <Icon icon="icon-fuzhi" class="ml-0.5" />
                      </Button>
                    </div>
                  </template>
                  <template #ctrlAccessToken="{text}">
                    <div class="flex items-center">
                      <span>{{ showInstallCtrlAccessToken ? text : '******' }}</span>
                      <Button
                        size="small"
                        type="link"
                        class="leading-5 h-5"
                        @click="toggleShowCtrlAccessToken">
                        <Icon class="text-4" :icon="showInstallCtrlAccessToken ? 'icon-biyan' : 'icon-zhengyan'" />
                      </Button>
                    </div>
                  </template>
                </Grid>
              </TabPane>
            </Tabs>
            <Button
              class="absolute right-0 top-0 text-3"
              type="link"
              size="small"
              @click="foldInstallAgent">
              {{ '收起' }}
            </Button>
          </div>

          <!-- Node Information Grid -->
          <Grid
            class="py-4 status-wrapper"
            font-size="12px"
            :dataSource="state.infos"
            :columns="infoItem">
            <template #password="{text}">
              <template v-if="showPassword">
                <span class="text-black-active align-middle">{{ text }}</span>
                <Icon
                  icon="icon-zhengyan"
                  class="text-3 cursor-pointer align-middle ml-2"
                  @click="handleShowPassword" />
              </template>
              <template v-else>
                <div class="inline-flex items-center">
                  <span class="text-black-active">******</span>
                </div>
              </template>
            </template>
            <template #spec="{record}">
              <div v-if="!!record.text">
                {{ record.text?.showLabel }}
              </div>
            </template>
            <template #roles="{text}">
              <template v-if="text?.length">
                <span
                  v-for="item in text"
                  :key="item.value"
                  class="mr-1 border px-1.5 rounded-full py-1">{{ item.message }}</span>
              </template>
              <template v-else>--</template>
            </template>
            <template #enabled="{text}">
              <span class="status flex items-center" :class="{'success': text, 'fail': !text}">
                {{ text ? '已启用' : '已禁用' }}
              </span>
            </template>
            <template #installAgent="{text}">
              <span class="status flex items-center" :class="{'success': text, 'fail': !text}">
                {{ text ? '已安装' : '未安装' }}
              </span>
            </template>
            <template #online="{text}">
              <span class="status flex items-center" :class="{'success': text, 'fail': !text}">
                {{ text ? '已连接' : '未连接' }}
              </span>
            </template>
          </Grid>
        </div>

        <!-- Resource Monitoring Section -->
        <div class="source">
          <Tabs v-model:activeKey="activeKey" size="small">
            <TabPane key="source" forceRender>
              <template #tab><span class="font-semibold">资源监控</span></template>

              <!-- Resource Usage Progress Bars -->
              <ul class="flex pb-5 justify-between text-3">
                <li
                  v-for="item in nodeUseProgresses"
                  :key="item.valueKey"
                  class="inline-flex">
                  <div class="rounded flex border py-2.5 px-3.5">
                    <Progress
                      v-show="item.valueKey!=='network'"
                      type="circle"
                      :percent="sourceUse[item.percentValue]"
                      :width="60"
                      :strokeColor="getStrokeColor(sourceUse[item.percentValue])">
                      <template #format>
                        <span v-if="item.valueKey==='network'"></span>
                        <span v-else style="font-size: 12px; font-weight: bold;">{{ sourceUse[item.percentValue] }}%</span>
                      </template>
                    </Progress>
                    <div v-if="item.valueKey !== 'network'" class="pl-5 w-35">
                      <span class="text-3">{{ item.label }}</span>
                      <div class="leading-5">
                        <label class="inline-block w-12 text-text-content">使用:</label>
                        <span class="text-black-active ">{{ sourceUse[item.valueKey] }}{{ item.unit }}</span>
                      </div>
                      <div class="leading-5">
                        <label class="inline-block w-12 text-text-content">{{ item.valueKey === 'cpu' ? '空闲' : '总计' }}:</label>
                        <span class="text-black-active ">{{ sourceUse[item.totalKey] }}{{ item.unit }}</span>
                      </div>
                    </div>
                    <div v-else class="pl-5 w-80">
                      <div class="flex justify-between items-center">
                        <span class="text-3 ">{{ item.label }}</span>
                        <Select
                          v-model:value="currentDeviceName"
                          :options="networkDeviceData"
                          :fieldNames="{value: 'deviceName', label: 'deviceName'}"
                          class="min-w-25 device-select"
                          @change="(value: any) => onDeviceNameChange(value)" />
                      </div>
                      <div class="flex w-full flex-wrap">
                        <div
                          v-for="info in internetInfo"
                          :key="info.valueKey"
                          class="leading-5 w-1/2">
                          <label class="inline-block w-12 text-text-content">{{ info.label }}</label>
                          <span class="text-black-active ">{{ sourceUse[info.valueKey] }} {{ info.unit }}</span>
                        </div>
                      </div>
                    </div>
                  </div>
                </li>
              </ul>

              <!-- Chart Controls -->
              <div class="flex justify-between">
                <div>
                  <RadioGroup
                    v-model:value="activeTab"
                    buttonStyle="solid"
                    class="node-tab">
                    <RadioButton
                      v-for="radio in nodeEchartsTabs"
                      :key="radio.valueKey"
                      :value="radio.valueKey">
                      {{ radio.label }}
                    </RadioButton>
                  </RadioGroup>
                  <Select
                    v-show="activeTab === 'disk'"
                    v-model:value="activeDisk"
                    class="ml-2 min-w-25"
                    size="small"
                    :options="diskNames" />
                  <Select
                    v-show="activeTab === 'network'"
                    v-model:value="activeNetwork"
                    class="ml-2 min-w-25"
                    size="small"
                    :options="networkNames" />
                </div>
                <div class="flex items-center">
                  <template v-if="activeKey === 'source'">
                    <IntervalTimestamp
                      v-model:loading="loadingChart"
                      :action="currentSourceServer"
                      :params="chartParams"
                      @change="onChartDataChange" />
                  </template>
                </div>
              </div>

              <!-- Chart Options -->
              <RadioGroup
                v-show="activeTab==='memory'"
                v-model:value="showMemoryPercentChart"
                class="mt-3 block text-center"
                size="small"
                :options="memoryDataOptions" />
              <RadioGroup
                v-show="activeTab==='disk'"
                v-model:value="diskChartKey"
                class="mt-3 block text-center"
                size="small"
                :options="diskDataOptions" />
              <RadioGroup
                v-show="activeTab === 'network'"
                v-model:value="networkChartKey"
                class="mt-3 block text-center"
                size="small"
                :options="networkDataOptions" />

              <!-- Chart Display -->
              <Spin
                :spinning="loadingChart"
                mask
                :delay="0">
                <div ref="echartRef" class="w-full h-65 mt-7.5">
                </div>
                <NoData
                  v-if="showNoData"
                  class="absolute top-0 left-1/2 -translate-x-1/2" />
              </Spin>
            </TabPane>

            <!-- Proxy Service Tab -->
            <TabPane key="proxy">
              <template #tab><span class="font-semibold">代理服务</span></template>
              <RadioGroup
                v-model:value="proxyActiveKey"
                :options="proxyOpt"
                size="small"
                buttonStyle="solid"
                optionType="button">
              </RadioGroup>
              <template v-if="proxyActiveKey==='proxy'">
                <AgentInfo :ip="state.infos.publicIp || state.infos.ip" :port="state.infos.port || 0" />
                <AgentChart :id="id" />
              </template>
              <template v-if="proxyActiveKey==='log'">
                <AgentLog
                  class="mt-4"
                  :ip="state.infos.publicIp || state.infos.ip"
                  :port="state.infos.agentPort || 0" />
              </template>
            </TabPane>

            <!-- Execution Tasks Tab -->
            <template v-if="state.infos?.rolesValues?.includes('EXECUTION')">
              <TabPane key="execTask">
                <template #tab><span class="font-semibold">执行任务</span></template>
                <Execution :id="id" />
              </TabPane>
              <TabPane key="execPropulsion">
                <template #tab><span class="font-semibold">执行器进程</span></template>
                <ExecutionPropulsion :nodeId="id" :tenantId="state.infos?.tenantId" />
              </TabPane>
            </template>

            <!-- Mock Service Tab -->
            <template v-if="state.infos?.rolesValues?.includes('MOCK_SERVICE')">
              <TabPane key="mock">
                <template #tab><span class="font-semibold">Mock服务实例</span></template>
                <MockService :nodeId="id" />
              </TabPane>
            </template>
          </Tabs>
        </div>
      </div>
    </Spin>
  </div>
</template>

<style scoped>
.node-action-btn {
  @apply rounded mr-2 text-text-content text-3 border-0  px-2 shadow-none inline-flex items-center;
}

.node-action-btn :deep(span) {
  @apply ml-1;
}

.node-action-btn[disabled],
.node-action-btn:not([disabled]),
.node-action-btn:focus {
  @apply bg-transparent !important;
}

.node-action-btn[disabled] {
  @apply opacity-50;
}

.node-action-btn:not([disabled]):hover {
  @apply text-text-link;
}

.node-action-btn::after {
  @apply hidden;
}

.device-select {
  @apply h-5;
}

:deep(.device-select) > .ant-select-selector {
  @apply !h-full;
}

:deep(.device-select) > .ant-select-selector >.ant-select-selection-item {
  @apply !leading-5;
}

.status-wrapper > label {
  @apply text-text-content mr-2;
}

.status-wrapper .status {
  @apply text-black-active  mr-10;
}

.status-wrapper .status::before {
  @apply w-1.5 h-1.5 rounded-full mr-2 relative inline-block;
  content: "";
}

.status-wrapper .success::before {
  @apply bg-status-success;
}

.status-wrapper .fail::before {
  @apply bg-status-error;
}

.node-tab .ant-radio-button-wrapper:not(.ant-radio-button-wrapper-checked) {
  @apply bg-gray-light;
}

.detai-btns-wrapper button {
  @apply border-0 shadow-none text-text-content;
}

.detai-btns-wrapper button > :deep(span) {
  @apply ml-2;
}

.node-tab.ant-radio-group .ant-radio-button-wrapper {
  @apply border-0 text-3 leading-6 h-6;
}

.node-tab.ant-radio-group .ant-radio-button-wrapper::before {
  @apply hidden;
}

.node-tab.ant-radio-group .ant-radio-button-wrapper:focus-within {
  @apply shadow-none;
}

.ant-table-wrapper :deep(.ant-table-tbody),
.ant-table-wrapper :deep(.ant-table-thead) {
  @apply text-3;
}

.ant-table-wrapper :deep(.ant-table.ant-table-small) .ant-table-tbody > tr > td,
.ant-table-wrapper :deep(.ant-table.ant-table-small) .ant-table-thead > tr > th {
  @apply bg-white border-t-0;
}

.ant-radio-group.ant-radio-group-small :deep(.ant-radio-wrapper) {
  @apply text-3;
}

.install-step {
  @apply px-3 py-1.5 my-2 leading-6;
  background-color: #f6f6f6;
}
</style>
