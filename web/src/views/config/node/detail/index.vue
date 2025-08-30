<script setup lang="ts">
import { defineAsyncComponent, inject, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import dayjs from 'dayjs';
import { Button, Progress, RadioButton, RadioGroup, TabPane, Tabs } from 'ant-design-vue';
import {
  Grid,
  Hints,
  Icon,
  IntervalTimestamp,
  modal,
  NoData,
  notification,
  Select,
  Spin,
  Tooltip
} from '@xcan-angus/vue-ui';
import { appContext, TESTER, toClipboard } from '@xcan-angus/infra';
import { useI18n } from 'vue-i18n';

import { infoItem, internetInfo, nodeEchartsTabs, nodeUseProgresses } from './interface';
import { getStrokeColor, installConfigColumns } from '../interface';
import { nodeCtrl, nodeInfo } from 'src/api/ctrl';
import { node } from '@/api/tester';

import { formatBytes } from '@/utils/common';
import { EChartsManager } from './echartsManager';

const { t } = useI18n();

const AgentChart = defineAsyncComponent(() => import('./AgentChart.vue'));
const AgentInfo = defineAsyncComponent(() => import('./AgentInfo.vue'));
const AgentLog = defineAsyncComponent(() => import('./AgentLog.vue'));
const Execution = defineAsyncComponent(() => import('./Execution.vue'));
const MockService = defineAsyncComponent(() => import('./MockService.vue'));
const ExecutionPropulsion = defineAsyncComponent(() => import('./ExecutionPropulsion.vue'));

const echartRef = ref();

const route = useRoute();
const router = useRouter();
const id = ref(route.params.id as string);

const state = reactive<{infos: Record<string, any>, linuxOfflineInstallSteps: Record<string, any>, windowsOfflineInstallSteps: Record<string, any>, installConfig?: any}>({
  infos: {},
  linuxOfflineInstallSteps: {},
  windowsOfflineInstallSteps: {},
  installConfig: undefined
});

const showPassword = ref(false);

const chartServersMap = {
  cpu: `${TESTER}/node/${id.value}/metrics/cpu`,
  memory: `${TESTER}/node/${id.value}/metrics/memory`,
  disk: `${TESTER}/node/${id.value}/metrics/disk`,
  network: `${TESTER}/node/${id.value}/metrics/network`
};
const loadingChart = ref(false);

const currentSourceServer = ref();

const chartParams = ref({}); // 图表参数

const activeTab = ref('cpu');

const diskNames = ref<{label: string, value: string}[]>([]); // 所有磁盘名字 list
const activeDisk = ref<string>(); // 选中的磁盘

const networkNames = ref<{label: string, value: string}[]>([]); // 所有网络名字
const activeNetwork = ref<string>(); // 选中的网络

const notMerge = ref(true); // 图表数据覆盖是否重新渲染

const memoryDataOptions = [{ label: t('node.nodeDetail.chartOptions.memory.usage'), value: false }, { label: t('node.nodeDetail.chartOptions.memory.usageRate'), value: true }];
const diskDataOptions = [
  // { label: '使用量', value: 'disk' }, { label: '使用率', value: 'percent' },
  { label: t('node.nodeDetail.chartOptions.disk.iops'), value: 'rate' }, { label: t('node.nodeDetail.chartOptions.disk.mbPerSecond'), value: 'bytesRate' }];
const networkDataOptions = [{ label: t('node.nodeDetail.chartOptions.network.mbPerSecond'), value: 'network' }, { label: t('node.nodeDetail.chartOptions.network.bytes'), value: 'bytes' }, { label: t('node.nodeDetail.chartOptions.network.errorPackets'), value: 'packet' }];

const showMemoryPercentChart = ref(false); // 显示内存百分比图表
const diskChartKey = ref('rate'); // 显示文件系统百分比图表
const networkChartKey = ref('network');

const timestamp = ref<string | undefined>(); // 选择时间段

const showInstallStep = ref(false); // 显示手动安装步骤

// 创建ECharts管理器实例
const echartsManager = new EChartsManager();
const sourceUse = echartsManager.getSourceUse();

const showNoData = ref(false); // 是否显示无数据提示

const loadingInfo = ref(true);
// 获取节点基本信息
const loadInfo = async () => {
  loadingInfo.value = true;
  const [error, res] = await node.getNodeDetail(id.value);
  loadingInfo.value = false;
  if (error) {
    return;
  }
  // delete res.data.online;
  const rolesName = res.data.roles.map(i => i.message).join(',');
  const rolesValues = res.data.roles.map(i => i.value);
  state.infos = {
    ...state.infos,
    ...res.data,
    sourceName: res.data.source.message,
    rolesName,
    rolesValues
  };
};

// 显示密码
const handleShowPassd = () => {
  showPassword.value = !showPassword.value;
};

const installing = ref(false); // 代理安装中
// 安装代理
const installAgen = async () => {
  installing.value = true;
  const [error] = await node.installNodeAgent({ id: id.value });
  installing.value = false;
  if (error) {
    // 检查error是否有data属性并且是对象类型
    if (error && typeof error === 'object' && 'data' in error && typeof error.data !== 'object') {
      return;
    }
    if (error && typeof error === 'object' && 'data' in error && error.data && typeof error.data === 'object' && 'linuxOfflineInstallSteps' in error.data) {
      state.linuxOfflineInstallSteps = error.data.linuxOfflineInstallSteps as Record<string, any>;
      showInstallStep.value = true;
    }
    if (error && typeof error === 'object' && 'data' in error && error.data && typeof error.data === 'object' && 'windowsOfflineInstallSteps' in error.data) {
      state.windowsOfflineInstallSteps = error.data.windowsOfflineInstallSteps as Record<string, any>;
      showInstallStep.value = true;
    }
    return;
  }

  state.infos.geAgentInstallationCmd = true;
};

// 手动安装步骤
const getInstallStep = async () => {
  if (showInstallStep.value) {
    foldInstallAgent();
    return;
  }
  const [error, res] = await nodeInfo.geAgentInstallationCmd({ id: id.value });
  if (error) {
    return;
  }
  showInstallStep.value = true;
  if (res.data.linuxOfflineInstallSteps) {
    state.linuxOfflineInstallSteps = res.data.linuxOfflineInstallSteps;
  }
  if (res.data.windowsOfflineInstallSteps) {
    state.windowsOfflineInstallSteps = res.data.windowsOfflineInstallSteps;
  }
  state.installConfig = res.data?.installConfig || {};
};

// 收起手动安装步骤
const foldInstallAgent = () => {
  showInstallStep.value = false;
};

const turnback = () => {
  router.push({ path: '/config#node' });
};

const enabled = ref(false); // 启用、禁用中
// 启用 禁用
const enableNode = async () => {
  enabled.value = true;
  const [error] = await node.enableNode([{ enabled: !state.infos.enabled, id: id.value }]);
  enabled.value = false;
  if (error) {
    return;
  }
  state.infos.enabled = !state.infos.enabled;
};

// 资源使用 ->cpu 内存 文件系统 交换区
const loadMetrics = async () => {
  const [error, res] = await nodeCtrl.getLatestMetrics(id.value);
  if (error) {
    return;
  }
  const { cvsCpu, cvsFilesystem, cvsMemory } = res.data || {};

  // CPU cvsCpu => idle,sys,user,wait,other,total
  if (cvsCpu) {
    const cpu = +(+cvsCpu.split(',')[5]).toFixed(2);
    const cpuPercent = cpu;
    const cpuTotal = (100 - cpu).toFixed(2);
    sourceUse.cpu = cpu;
    sourceUse.cpuPercent = cpuPercent;
    sourceUse.cpuTotal = cpuTotal;
  }

  // 内存  cvsMemory => free,used,freePercent,usedPercent,actualFree,actualUsed,actualFreePercent,actualUsedPercent,swapFree,swapUsed
  // 取实际使用值
  if (cvsMemory) {
    const cvsMemoryValues = cvsMemory.split(',');
    const memoryPercent = +(+cvsMemoryValues[7]).toFixed(2);
    const memory = +(+cvsMemoryValues[5]).toFixed(2);
    const memoryTotal = +(+cvsMemoryValues[4] + +cvsMemoryValues[5]).toFixed(2);
    sourceUse.memory = formatBytes(memory, 2);
    sourceUse.memoryPercent = memoryPercent;
    sourceUse.memoryTotal = formatBytes(memoryTotal, 2);

    // 交换区
    const swapTotal = (+cvsMemoryValues[9] || 0) + (+cvsMemoryValues[8] || 0);
    const swapPercent = +((+cvsMemoryValues[9] || 0) / (+swapTotal || 1) * 100).toFixed(2);
    const swap = (+cvsMemoryValues[9] || 0);
    sourceUse.swapTotal = formatBytes(swapTotal, 2);
    sourceUse.swapPercent = swapPercent;
    sourceUse.swap = formatBytes(swap, 2);
  }

  // 文件系统 cvsFilesystem => free,used,avail,usePercent
  if (cvsFilesystem) {
    const cvsFilesystems = cvsFilesystem.split(',');
    const disk = +(+cvsFilesystems[1]).toFixed(2);
    const diskPercent = +(+cvsFilesystems[3]).toFixed(2);
    const diskTotal = +(Number(cvsFilesystems[0]) + Number(cvsFilesystems[1])).toFixed(2);
    sourceUse.disk = formatBytes(disk, 2);
    sourceUse.diskPercent = diskPercent;
    sourceUse.diskTotal = formatBytes(diskTotal, 2);
  }

  if (res.data) {
    const timestamp = res.data?.timestamp;
    const datetime = res.datetime;
    // state.infos.online = true;
    if (dayjs(timestamp).add(30, 'second') < dayjs(datetime)) {
      sourceUse.cpu = 0;
      sourceUse.cpuPercent = 0;
      sourceUse.cpuTotal = 0;
      sourceUse.memory = '0';
      sourceUse.memoryPercent = 0;
      sourceUse.memoryTotal = '0';
      sourceUse.disk = '0';
      sourceUse.diskPercent = 0;
      sourceUse.diskTotal = '0';
      // state.infos.online = false;
    }
  } else {
    // state.infos.online = false;
  }
};

const networkDeviceData = ref<{deviceName: string, networkUsage: {cvsValue: string, timestamp: string}}[]>([]);
const currentDeviceName = ref();
let networkDatatime = '';

const onDeviceNameChange = (value) => {
  currentDeviceName.value = value;
  setNetworkData();
};

// 资源使用 ->网络
const loadNetwork = async () => {
  const [error, res] = await nodeCtrl.getNetworkInfoMetrics(id.value);
  if (error) {
    return;
  }
  networkDeviceData.value = res.data || [];
  networkDatatime = res.datetime;
  if (!currentDeviceName.value || !networkDeviceData.value.find(item => item.deviceName === currentDeviceName.value)) {
    currentDeviceName.value = networkDeviceData.value[0]?.deviceName;
  }
  if (!currentDeviceName.value) {
    return;
  }
  setNetworkData();
};

const setNetworkData = () => {
  const currentNetworkUsage = networkDeviceData.value.find(item => item.deviceName === currentDeviceName.value);
  const { cvsValue } = currentNetworkUsage?.networkUsage || {};
  if (!cvsValue) {
    return;
  }
  const cvsValues = cvsValue.split(',');
  sourceUse.rxBytesRate = +(+cvsValues[1]); // 下载
  sourceUse.txBytesRate = +(+cvsValues[4]); // 上传
  sourceUse.rxBytes = formatBytes(+cvsValues[0], 2); // 总下载
  sourceUse.txBytes = formatBytes(+cvsValues[3], 2); // 总上传

  if (currentNetworkUsage?.networkUsage) {
    const timestamp = currentNetworkUsage?.networkUsage?.timestamp;
    const datetime = networkDatatime;
    if (dayjs(timestamp).add(30, 'second') < dayjs(datetime)) {
      sourceUse.rxBytesRate = 0;
      sourceUse.txBytesRate = 0;
      sourceUse.rxBytes = '0';
      sourceUse.txBytes = '0';
    }
  }
};

// 图表数据变更
const onChartDataChange = (data) => {
  echartsManager.setChartsData(data);
  if (!data.length) {
    showNoData.value = true;
  } else {
    showNoData.value = false;
  }
  if (activeTab.value === 'cpu') {
    echartsManager.loadCpuEchartData(data, t, notMerge.value);
  }
  if (activeTab.value === 'memory') {
    echartsManager.loadMemoryEchartData(data, t, showMemoryPercentChart.value, notMerge.value);
  }
  if (activeTab.value === 'disk') {
    echartsManager.loadDiskEchartData(data, t, diskChartKey.value, notMerge.value);
  }
  if (activeTab.value === 'network') {
    echartsManager.loadNetworkEchartData(data, t, networkChartKey.value, notMerge.value);
  }
};

const loadEchartData = async () => {
  if (activeTab.value === 'cpu') {
    currentSourceServer.value = chartServersMap.cpu;
    chartParams.value = {
      orderSort: 'ASC'
    };
  }
  if (activeTab.value === 'memory') {
    currentSourceServer.value = chartServersMap.memory;
    chartParams.value = {
      orderSort: 'ASC'
    };
  }
  if (activeTab.value === 'disk') {
    if (!diskNames.value.length) {
      // loadingChartData.value = true;
      const [error, res] = await nodeCtrl.getDiskInfoMetrics(id.value);
      if (error) {
        return;
      }

      const _diskNames:{label: string, value: string}[] = [];
      const deviceNameMp = {};
      for (const item of (res.data || [])) {
        if (!deviceNameMp[item.deviceName]) {
          _diskNames.push({ label: item.deviceName, value: item.deviceName });
          deviceNameMp[item.deviceName] = true;
        }
      }
      diskNames.value = _diskNames;
      activeDisk.value = diskNames.value?.[0]?.value;
    }
    chartParams.value = {
      orderSort: 'ASC',
      filters: [{ key: 'deviceName', op: 'EQUAL', value: activeDisk.value }]
    };
    currentSourceServer.value = chartServersMap.disk;
  }

  if (activeTab.value === 'network') {
    if (!networkNames.value.length) {
      // loadingChartData.value = true;
      const [error, res] = await nodeCtrl.getNetworkInfoMetrics(id.value);
      if (error) {
        return;
      }
      const _networkNames: {label: string, value: string}[] = [];
      const networkNamesMp = {};
      for (const item of (res.data || [])) {
        if (!networkNamesMp[item.deviceName]) {
          _networkNames.push({ label: item.deviceName, value: item.deviceName });
          networkNamesMp[item.deviceName] = true;
        }
      }

      networkNames.value = _networkNames;
      activeNetwork.value = networkNames.value?.[0]?.value;
    }
    chartParams.value = {
      orderSort: 'ASC',
      filters: [{ key: 'deviceName', op: 'EQUAL', value: activeNetwork.value }]
    };
    currentSourceServer.value = chartServersMap.network;
  }
};

let timer; // 定时刷新数据
const refreshTimer = () => {
  timer && clearInterval(timer);
  timer = setInterval(() => {
    if (activeKey.value !== 'source') {
      return;
    }
    loadMetrics();
    loadNetwork();
  }, 5000);
};

const proxyActiveKey = ref('proxy');
const proxyOpt = [
  {
    label: t('node.nodeDetail.proxyOpt.proxyInfo'),
    value: 'proxy'
  },
  {
    label: t('node.nodeDetail.proxyOpt.log'),
    value: 'log'
  }
];

watch(() => showMemoryPercentChart.value, () => {
  echartsManager.updateChartDisplay(activeTab.value, showMemoryPercentChart.value, diskChartKey.value, networkChartKey.value);
});

watch(() => diskChartKey.value, () => {
  echartsManager.updateChartDisplay(activeTab.value, showMemoryPercentChart.value, diskChartKey.value, networkChartKey.value);
});

watch(() => networkChartKey.value, () => {
  echartsManager.updateChartDisplay(activeTab.value, showMemoryPercentChart.value, diskChartKey.value, networkChartKey.value);
});

const isAdmin = inject('isAdmin', ref(false));
const tenantInfo = ref(appContext.getTenant());
const userInfo = ref(appContext.getUser());
onMounted(async () => {
  id.value = route.params.id as string;
  await loadInfo();
  echartsManager.initEcharts(echartRef.value);
  await loadMetrics();
  await loadNetwork();
  timestamp.value = '5-minute';
  currentSourceServer.value = chartServersMap.cpu;
  refreshTimer();
});

const getOnlineInstallTip = (node) => {
  if (node.infos.geAgentInstallationCmd) {
    return t('node.nodeDetail.labels.installed');
  }
  if (!isAdmin.value) {
    return t('node.nodeDetail.labels.installAgentTip');
  }
};

const delNode = () => {
  modal.confirm({
    content: `确定删除节点【${state.infos.name}】吗?`,
    onOk: async () => {
      const [error] = await node.deleteNode({ ids: [id.value] });
      if (error) {
        return;
      }
      turnback();
    }
  });
};

// 删除按钮禁用提示
const getDelTip = (node) => {
  if (node.enabled) {
    return t('node.nodeDetail.labels.disableDelete');
  }
  if (!isAdmin.value) {
    return t('node.nodeDetail.labels.deleteNodeTip');
  }
  if (node.source?.value !== 'OWN_NODE') {
    return t('node.nodeDetail.labels.onlyOwnNode');
  }
};

onBeforeUnmount(() => {
  timer && clearInterval(timer);
});

// 监听 tab 变更
watch([() => activeTab.value, () => activeDisk.value, () => activeNetwork.value], () => {
  notMerge.value = true;
  loadEchartData();
});

const showInstallCtrlAccessToken = ref(false);
const toggleShowCtrlAccessToken = () => {
  showInstallCtrlAccessToken.value = !showInstallCtrlAccessToken.value;
};

const copyContent = (text) => {
  toClipboard(text).then(() => {
    notification.success(t('node.nodeDetail.labels.copySuccess'));
  });
};

const activeKey = ref<'source' | 'proxy'>('source');
</script>

<template>
  <div class="h-full overflow-auto">
    <Spin :spinning="loadingInfo" mask>
      <div class="bg-white px-10 text-3">
        <!-- 基本信息 -->
        <div class="basic">
          <div class="title flex justify-between pt-2.5 items-center">
            <span class="mr-15 font-semibold">基本信息</span>
            <div class="detai-btns-wrapper">
              <Button
                v-if="!state.infos.enabled"
                class="node-action-btn"
                :loading="enabled"
                :disabled="state.infos?.tenantId !== tenantInfo?.id || !(isAdmin || state.infos?.createdBy === userInfo?.id)"
                @click="enableNode">
                <Icon icon="icon-qiyong" />{{ t('node.nodeDetail.buttons.enable') }}
              </Button>
              <Button
                v-else
                class="node-action-btn"
                :loading="enabled"
                :disabled="state.infos?.tenantId !== tenantInfo?.id || !(isAdmin || state.infos?.createdBy === userInfo?.id)"
                @click="enableNode">
                <Icon icon="icon-jinyong" />{{ t('node.nodeDetail.buttons.disable') }}
              </Button>
              <Tooltip v-if="state.infos?.tenantId !== tenantInfo?.id || !(isAdmin || state.infos?.createdBy === userInfo?.id) || state.infos?.enabled" :title="getDelTip(state.infos)">
                <Button
                  class="node-action-btn"
                  :disabled="true">
                  <Icon icon="icon-qingchu" class="mr-1" />
                  <span>{{ t('node.nodeDetail.buttons.delete') }}</span>
                </Button>
              </Tooltip>
              <Button
                v-else
                class="node-action-btn"
                @click="delNode()">
                <Icon icon="icon-qingchu" class="mr-1" />
                <span>{{ t('node.nodeDetail.buttons.delete') }}</span>
              </Button>
              <Tooltip v-if="state.infos.installNodeAgent || state.infos.free || !isAdmin" :title="getOnlineInstallTip(state)">
                <Button
                  :disabled="true"
                  :loading="installing"
                  class="node-action-btn"
                  @click="installAgen">
                  <Icon icon="icon-anzhuangdaili" />
                  {{ t('node.nodeDetail.buttons.onlineInstallAgent') }}
                </Button>
              </Tooltip>
              <Button
                v-else
                :disabled="state.infos.installNodeAgent || !isAdmin"
                :loading="installing"
                class="node-action-btn"
                @click="installAgen">
                <Icon icon="icon-anzhuangdaili" />{{ t('node.nodeDetail.buttons.onlineInstallAgent') }}
                <Hints
                  v-if="installing"
                  class="absolute left-5 -bottom-3"
                  :text="t('node.nodeDetail.labels.estimatedTime')" />
              </Button>
              <Tooltip v-if="!isAdmin || state.infos.free" :title="!isAdmin ? `安装代理需要系统管理员或应用管理员权限` : ''">
                <Button
                  :disabled="true"
                  class="node-action-btn"
                  @click="getInstallStep">
                  <Icon icon="icon-anzhuangdaili" />{{ t('node.nodeDetail.buttons.manualInstallAgent') }}
                </Button>
              </Tooltip>
              <Button
                v-else
                :disabled="!isAdmin"
                class="node-action-btn"
                @click="getInstallStep">
                <Icon icon="icon-anzhuangdaili" />{{ t('node.nodeDetail.buttons.manualInstallAgent') }}
              </Button>
              <Button class="node-action-btn" @click="turnback">
                <Icon icon="icon-fanhui" />{{ t('node.nodeDetail.buttons.back') }}
              </Button>
            </div>
          </div>
          <!-- 手动安装步骤 -->
          <div v-show="showInstallStep" class="pt-4 mb-4 relative">
            <Tabs size="small">
              <TabPane key="linux" :tab="t('node.nodeDetail.installSteps.linuxTitle')">
                <div class="text-3">{{ t('node.nodeDetail.installSteps.description') }}</div>
                <div class="text-3">
                  {{ t('node.nodeDetail.installSteps.method1') }}<Icon
                    icon="icon-fuzhi"
                    class="cursor-pointer text-3.5 text-blue-1"
                    @click="copyContent(state.linuxOfflineInstallSteps?.onlineInstallCmd)" />
                  <p class="install-step whitespace-pre-line">
                    {{ state.linuxOfflineInstallSteps?.onlineInstallCmd }}
                  </p>
                  {{ t('node.nodeDetail.installSteps.method2') }}
                  <p class="install-step whitespace-pre-line">
                    {{ t('node.nodeDetail.installSteps.downloadScript') }}<a class="cursor-pointer" :href="state.linuxOfflineInstallSteps?.installScriptUrl">{{ state.linuxOfflineInstallSteps?.installScriptName }}</a> <br />
                    {{ t('node.nodeDetail.installSteps.copyScript', { scriptName: state.linuxOfflineInstallSteps?.installScriptName }) }}<br />
                    {{ state.linuxOfflineInstallSteps?.runInstallCmd }}
                  </p>
                </div>
              </TabPane>
              <TabPane key="config" :tab="t('node.nodeDetail.installSteps.configTitle')">
                <Grid
                  :dataSource="state.installConfig"
                  :columns="installConfigColumns">
                  <template #tenantId="{text}">
                    <div class="flex items-center">
                      <span>{{ text }}</span> <Button
                        type="link"
                        size="small"
                        @click="copyContent(text)">
                        <Icon icon="icon-fuzhi" class="ml-0.5" />
                      </Button>
                    </div>
                  </template>
                  <template #deviceId="{text}">
                    <div class="flex items-center">
                      <span>{{ text }}</span> <Button
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
              {{ t('node.nodeDetail.buttons.fold') }}
            </Button>
          </div>
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
                  @click="handleShowPassd" />
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
              <span class="status flex items-center" :class="{'success': text, 'fail': !text}">{{ text ? t('node.nodeDetail.status.enabled') : t('node.nodeDetail.status.disabled') }}</span>
            </template>
            <template #installAgent="{text}">
              <span class="status  flex items-center" :class="{'success': text, 'fail': !text}">{{ text ? t('node.nodeDetail.status.installed') : t('node.nodeDetail.status.notInstalled') }}</span>
            </template>
            <template #online="{text}">
              <span class="status  flex items-center" :class="{'success': text, 'fail': !text}">{{ text ? t('node.nodeDetail.status.connected') : t('node.nodeDetail.status.notConnected') }}</span>
            </template>
          </Grid>
        </div>
        <!-- 资源使用 -->
        <div class="source">
          <Tabs v-model:activeKey="activeKey" size="small">
            <TabPane key="source" forceRender>
              <template #tab><span class="font-semibold">{{ t('node.nodeDetail.labels.resourceMonitoring') }}</span></template>
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
                        <label class="inline-block w-12 text-text-content">{{ t('node.nodeDetail.labels.used') }}:</label>
                        <span class="text-black-active ">{{ sourceUse[item.valueKey] }}{{ item.unit }}</span>
                      </div>
                      <div class="leading-5">
                        <label class="inline-block w-12 text-text-content">{{ item.valueKey === 'cpu' ? t('node.nodeDetail.labels.idle') : t('node.nodeDetail.labels.total') }}:</label>
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
                          @change="onDeviceNameChange" />
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
                  <!-- <RadioGroup v-show="activeTab === 'network'"
                            v-model:value="activeNetwork"
                            class="ml-2"
                            :options="networkNames" /> -->
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
            <TabPane key="proxy">
              <template #tab><span class="font-semibold">{{ t('node.nodeDetail.labels.proxyService') }}</span></template>
              <RadioGroup
                v-model:value="proxyActiveKey"
                :options="proxyOpt"
                size="small"
                buttonStyle="solid"
                optionType="button">
              </RadioGroup>
              <template v-if="proxyActiveKey==='proxy'">
                <AgentInfo :ip="state.infos.publicIp || state.infos.ip" :port="state.infos.port" />
                <AgentChart :id="id" />
              </template>
              <template v-if="proxyActiveKey==='log'">
                <AgentLog
                  class="mt-4"
                  :ip="state.infos.publicIp || state.infos.ip"
                  :port="state.infos.agentPort" />
              </template>
            </TabPane>
            <template v-if="state.infos?.rolesValues?.includes('EXECUTION')">
              <TabPane key="execTask">
                <template #tab><span class="font-semibold">{{ t('node.nodeDetail.labels.executingTasks') }}</span></template>
                <Execution :id="id" />
              </TabPane>
              <TabPane key="execPropulsion">
                <template #tab><span class="font-semibold">{{ t('node.nodeDetail.labels.executorProcess') }}</span></template>
                <ExecutionPropulsion :nodeId="id" :tenantId="state.infos?.tenantId" />
              </TabPane>
            </template>
            <template v-if="state.infos?.rolesValues?.includes('MOCK_SERVICE')">
              <TabPane key="mock">
                <template #tab><span class="font-semibold">{{ t('node.nodeDetail.labels.mockServiceInstance') }}</span></template>
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
