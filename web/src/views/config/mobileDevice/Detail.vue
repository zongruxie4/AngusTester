<script setup lang="ts">
import { ref, inject, computed, defineAsyncComponent } from 'vue';
import { Icon, Card, Grid, Select } from '@xcan-angus/vue-ui';
import { Button, Tag, Checkbox } from 'ant-design-vue';
import type { VersionListProps } from './types';
import { Tabs, TabPane } from 'ant-design-vue';

const Performance = defineAsyncComponent(() => import('./Performance.vue'));

// Component props with default values
const props = withDefaults(defineProps<VersionListProps & { data?: any }>(), {
  data: undefined
});

const deleteTabPane = inject<(keys: string[]) => void>('deleteTabPane', () => {});

// Active tab
const activeTab = ref('info');

// Device detail data
const deviceDetail = ref({
  deviceName: 'Samsung Galaxy S23',
  udid: 'R58M40MMWFK',
  model: 'SM-S911B',
  status: 'online',
  type: 'android',
  systemVersion: 'Android 13.0',
  resolution: '1080x2400',
  node: '节点1',
  lastActivity: '2分钟前',
  // Basic Information
  manufacturer: 'Samsung',
  deviceType: '手机',
  os: 'Android',
  osVersion: '13.0',
  apiLevel: 33,
  securityPatch: '2023-08-01',
  kernelVersion: '5.10.81',
  // Display Information
  screenSize: '6.1英寸',
  pixelDensity: '425 PPI',
  refreshRate: '120Hz',
  screenOrientation: '竖屏',
  // Hardware Information
  cpuArchitecture: 'arm64-v8a',
  cpuCores: '8核',
  cpuFrequency: '3.36 GHz',
  totalMemory: '8 GB',
  storageSpace: '256 GB',
  // Connection Information
  connectionMethod: 'USB',
  connectionPort: 4723,
  appiumService: 'http://localhost:4723',
  adbStatus: '已连接',
  connectionTime: '2023-10-15 09:30:22',
  // Node Information
  nodeAddress: '192.168.1.101',
  nodeStatus: '在线',
  deviceLocation: '测试实验室A-01'
});

// Status configuration
const getStatusConfig = (status: string) => {
  const configs: Record<string, { text: string; color: string; bgColor: string }> = {
    online: { text: '在线', color: '#52c41a', bgColor: '#f6ffed' },
    busy: { text: '繁忙', color: '#fa8c16', bgColor: '#fff7e6' },
    offline: { text: '离线', color: '#8c8c8c', bgColor: '#fafafa' },
    abnormal: { text: '异常', color: '#ff4d4f', bgColor: '#fff1f0' }
  };
  return configs[status] || configs.offline;
};

// Type configuration
const getTypeConfig = (type: string) => {
  const configs: Record<string, { text: string; color: string; bgColor: string }> = {
    android: { text: 'Android', color: '#1890ff', bgColor: '#e6f7ff' },
    ios: { text: 'iOS', color: '#722ed1', bgColor: '#f9f0ff' },
    harmonyos: { text: 'HarmonyOS', color: '#1890ff', bgColor: '#e6f7ff' }
  };
  return configs[type] || configs.android;
};

// Handle actions
const handleRestart = () => {
  console.log('Restart device');
};

const handleDisconnect = () => {
  console.log('Disconnect device');
};

const handleRefresh = () => {
  console.log('Refresh device');
};

const handleStartTest = () => {
  console.log('Start test');
};

const handleAdbShell = () => {
  console.log('Open ADB Shell');
};

// Installed Applications Data
const installedApps = ref([
  {
    id: '1',
    name: 'Chrome浏览器',
    packageName: 'com.android.chrome',
    version: '118.0.5993.65',
    type: 'user',
    size: '125MB',
    date: '2023-09-10',
    iconColor: '#1890ff'
  },
  {
    id: '2',
    name: 'Gmail',
    packageName: 'com.google.android.gm',
    version: '2023.09.10',
    type: 'user',
    size: '85MB',
    date: '2023-08-15',
    iconColor: '#722ed1'
  },
  {
    id: '3',
    name: '测试应用',
    packageName: 'com.example.testapp',
    version: '2.1.0',
    type: 'user',
    size: '45MB',
    date: '2023-10-15',
    iconColor: '#fa8c16'
  },
  {
    id: '4',
    name: '相机',
    packageName: 'com.android.camera',
    version: '13.0',
    type: 'system',
    size: '95MB',
    date: '2023-01-01',
    iconColor: '#1890ff'
  },
  {
    id: '5',
    name: '设置',
    packageName: 'com.android.settings',
    version: '13.0',
    type: 'system',
    size: '120MB',
    date: '2023-01-01',
    iconColor: '#1890ff'
  },
  {
    id: '6',
    name: '文件管理器',
    packageName: 'com.android.documentsui',
    version: '13.0',
    type: 'system',
    size: '65MB',
    date: '2023-01-01',
    iconColor: '#fa8c16'
  }
]);

// Application Statistics
const appStatistics = ref({
  totalApps: 42,
  systemApps: 28,
  userApps: 14,
  testApps: 5,
  totalStorage: '4.2 GB',
  averageSize: '102 MB',
  recentlyInstalled: '测试应用_v2.1.0',
  lastUpdatedTime: '2023-10-15 09:15:00'
});

// Handle app actions
const handleInstallApp = () => {
  console.log('Install application');
};

const handleRefreshApps = () => {
  console.log('Refresh app list');
};

// Get app type config
const getAppTypeConfig = (type: string) => {
  return type === 'system' 
    ? { text: '系统应用', color: '#8c8c8c', bgColor: '#fafafa' }
    : { text: '用户应用', color: '#1890ff', bgColor: '#e6f7ff' };
};

// Device Logs Data
const logTypeFilter = ref('all');
const logLevelFilter = ref('all');

const logTypeOptions = [
  { label: '全部日志', value: 'all' },
  { label: '系统日志', value: 'system' },
  { label: '应用日志', value: 'app' },
  { label: '测试日志', value: 'test' }
];

const logLevelOptions = [
  { label: '全部级别', value: 'all' },
  { label: 'INFO', value: 'info' },
  { label: 'WARN', value: 'warn' },
  { label: 'ERROR', value: 'error' }
];

const deviceLogs = ref([
  {
    id: '1',
    time: '09:30:22',
    level: 'info',
    message: '设备连接成功 - UDID: R58M40MMWFK'
  },
  {
    id: '2',
    time: '09:30:25',
    level: 'info',
    message: 'Appium服务启动完成 - 端口: 4723'
  },
  {
    id: '3',
    time: '09:31:10',
    level: 'info',
    message: '自动化引擎初始化 - UiAutomator2'
  },
  {
    id: '4',
    time: '09:31:15',
    level: 'info',
    message: '设备信息获取成功'
  },
  {
    id: '5',
    time: '09:32:05',
    level: 'warn',
    message: '电池电量低于20%, 建议充电'
  },
  {
    id: '6',
    time: '09:33:45',
    level: 'info',
    message: '开始执行测试用例: TC_001'
  },
  {
    id: '7',
    time: '09:35:20',
    level: 'info',
    message: '测试用例 TC_001 执行通过'
  },
  {
    id: '8',
    time: '09:36:10',
    level: 'info',
    message: '开始执行测试用例: TC_002'
  },
  {
    id: '9',
    time: '09:37:55',
    level: 'error',
    message: '元素定位失败: login_button'
  },
  {
    id: '10',
    time: '09:38:30',
    level: 'info',
    message: '测试用例 TC_002 执行失败'
  },
  {
    id: '11',
    time: '09:40:15',
    level: 'info',
    message: '开始执行测试用例: TC_003'
  },
  {
    id: '12',
    time: '09:41:50',
    level: 'info',
    message: '测试用例 TC_003 执行通过'
  },
  {
    id: '13',
    time: '09:42:30',
    level: 'info',
    message: '测试执行完成 - 通过: 2, 失败: 1'
  },
  {
    id: '14',
    time: '09:43:10',
    level: 'info',
    message: '生成测试报告: test_report_20231015.html'
  },
  {
    id: '15',
    time: '09:45:00',
    level: 'info',
    message: '设备断开连接'
  }
]);

// Get log level config
const getLogLevelConfig = (level: string) => {
  const configs: Record<string, { text: string; color: string }> = {
    info: { text: 'INFO', color: '#69c0ff' },
    warn: { text: 'WARN', color: '#ffa940' },
    error: { text: 'ERROR', color: '#ff7875' }
  };
  return configs[level] || configs.info;
};

// Filtered logs
const filteredLogs = computed(() => {
  let logs = deviceLogs.value;
  
  if (logLevelFilter.value !== 'all') {
    logs = logs.filter(log => log.level === logLevelFilter.value);
  }
  
  return logs;
});

// Handle log actions
const handleExportLogs = () => {
  console.log('Export logs');
};

const handleClearLogs = () => {
  console.log('Clear logs');
  deviceLogs.value = [];
};

// Device Settings Data
const appiumConfig = ref({
  automationEngine: 'UiAutomator2',
  appiumPort: 4723,
  sessionTimeout: 300,
  autoAuthorization: true,
  noResetMode: false
});

const deviceControls = ref({
  screenRotation: true,
  touchHints: false,
  performanceLog: true,
  autoScreenshot: true
});

// Handle device control changes
const handleControlChange = (key: string, value: boolean) => {
  deviceControls.value[key] = value;
  console.log('Device control changed:', key, value);
};

// Handle dangerous operations
const handleRestartDevice = () => {
  console.log('Restart device');
};

const handleFactoryReset = () => {
  console.log('Factory reset');
};

const handleForceDisconnect = () => {
  console.log('Force disconnect');
};

const bsicInfoColumns = [
  [
    {
      label: '设备名称',
      dataIndex: 'deviceName'
    },
    {
      label: '设备型号',
      dataIndex: 'model'
    },
    {
      label: '制造商',
      dataIndex: 'manufacturer'
    },
    {
      label: '设备类型',
      dataIndex: 'deviceType'
    },
    {
      label: 'UDID/序列号',
      dataIndex: 'udid'
    }
  ],
  [
    {
      label: '操作系统',
      dataIndex: 'os'
    },
    {
      label: '系统版本',
      dataIndex: 'osVersion'
    },
    {
      label: 'API Level',
      dataIndex: 'apiLevel'
    },
    {
      label: '安全补丁',
      dataIndex: 'securityPatch'
    },
    {
      label: '内核版本',
      dataIndex: 'kernelVersion'
    }
  ]
];

const screenInfoColumns = [
  [
    {
      label: '屏幕分辨率',
      dataIndex: 'resolution'
    },
    {
      label: '屏幕尺寸',
      dataIndex: 'screenSize'
    },
    {
      label: '像素密度',
      dataIndex: 'pixelDensity'
    },
    {
      label: '刷新率',
      dataIndex: 'refreshRate'
    },
    {
      label: '屏幕方向',
      dataIndex: 'screenOrientation'
    }
  ],
  [
    {
      label: 'CPU架构',
      dataIndex: 'cpuArchitecture'
    },
    {
      label: 'CPU核心数',
      dataIndex: 'cpuCores'
    },
    {
      label: 'CPU频率',
      dataIndex: 'cpuFrequency'
    },
    {
      label: '总内存',
      dataIndex: 'totalMemory'
    },
    {
      label: '存储空间',
      dataIndex: 'storageSpace'
    }
  ]
];

const connectionNodeColumns = [
  [
    {
      label: '连接方式',
      dataIndex: 'connectionMethod'
    },
    {
      label: '连接端口',
      dataIndex: 'connectionPort'
    },
    {
      label: 'Appium服务',
      dataIndex: 'appiumService'
    },
    {
      label: 'ADB状态',
      dataIndex: 'adbStatus'
    },
    {
      label: '连接时间',
      dataIndex: 'connectionTime'
    }
  ],
  [
    {
      label: '所在节点',
      dataIndex: 'node'
    },
    {
      label: '节点地址',
      dataIndex: 'nodeAddress'
    },
    {
      label: '节点状态',
      dataIndex: 'nodeStatus'
    },
    {
      label: '设备位置',
      dataIndex: 'deviceLocation'
    },
    {
      label: '最后活动',
      dataIndex: 'lastActivity'
    }
  ]
];

const appStatisticsColumns = [
  [
    {
      label: '应用总数',
      dataIndex: 'totalApps'
    },
    {
      label: '系统应用',
      dataIndex: 'systemApps'
    },
    {
      label: '用户应用',
      dataIndex: 'userApps'
    },
    {
      label: '测试应用',
      dataIndex: 'testApps'
    }
  ],
  [
    {
      label: '总存储占用',
      dataIndex: 'totalStorage'
    },
    {
      label: '平均应用大小',
      dataIndex: 'averageSize'
    },
    {
      label: '最近安装',
      dataIndex: 'recentlyInstalled'
    },
    {
      label: '最近更新时间',
      dataIndex: 'lastUpdatedTime'
    }
  ]
];

</script>

<template>
  <div class="device-detail-page">

    <!-- Device Detail Card -->
    <div class="flex space-x-2 mb-2">
        <Button
        type="default"
        size="small"
        class="action-button restart-button"
        @click="handleRestart">
        <Icon icon="icon-shuaxin" class="mr-1" />
        重启设备
        </Button>
        <Button
        type="default"
        size="small"
        danger
        class="action-button disconnect-button"
        @click="handleDisconnect">
        <Icon icon="icon-duankai" class="mr-1" />
        断开连接
        </Button>
        <Button
        type="default"
        size="small"
        class="action-button refresh-button"
        @click="handleRefresh">
        <Icon icon="icon-shuaxin" class="mr-1" />
        刷新
        </Button>
    </div>
    <Card class="device-detail-card">
      <div class="device-detail-content">
        <!-- Left: Android Icon -->
        <div class="device-icon-wrapper">
          <div class="android-icon">
            <svg
              viewBox="0 0 24 24"
              fill="none"
              xmlns="http://www.w3.org/2000/svg"
              class="android-icon-svg">
              <path
                d="M17.523 15.3414c-.5511 0-.9993-.4486-.9993-.9997s.4482-.9993.9993-.9993c.5511 0 .9993.4482.9993.9993.0001.5511-.4482.9997-.9993.9997m-11.046 0c-.5511 0-.9993-.4486-.9993-.9997s.4482-.9993.9993-.9993c.551 0 .9993.4482.9993.9993 0 .5511-.4483.9997-.9993.9997m11.4045-6.02l1.9973-3.4592a.416.416 0 00-.1521-.5676.416.416 0 00-.5676.1521l-2.0223 3.503C15.5902 8.2439 13.8533 7.8508 12 7.8508s-3.5902.3931-5.1349 1.2297L4.8429 5.5775a.4161.4161 0 00-.5676-.1521.4157.4157 0 00-.1521.5676l1.9973 3.4592C2.6889 11.186.8535 12.3064 0 13.8v8.15h24V13.8c-.8535-1.4936-2.6889-2.6139-4.1185-3.4786"
                fill="#fff" />
            </svg>
          </div>
        </div>

        <!-- Center: Device Info -->
        <div class="device-info-section">
          <div class="device-name">{{ deviceDetail.deviceName }}</div>
          <div class="device-info-lines">
            <div class="device-info-line">
              <span class="info-label">UDID:</span>
              <span class="info-value">{{ deviceDetail.udid }}</span>
            </div>
            <div class="device-info-line">
              <span class="info-label">型号:</span>
              <span class="info-value">{{ deviceDetail.model }}</span>
            </div>
          </div>
          <div class="device-tags">
            <Tag
              :style="{ backgroundColor: getStatusConfig(deviceDetail.status).bgColor, color: getStatusConfig(deviceDetail.status).color, border: 'none' }">
              {{ getStatusConfig(deviceDetail.status).text }}
            </Tag>
            <Tag
              :style="{ backgroundColor: getTypeConfig(deviceDetail.type).bgColor, color: getTypeConfig(deviceDetail.type).color, border: 'none' }">
              {{ getTypeConfig(deviceDetail.type).text }}
            </Tag>
            <Tag
              style="background-color: #f5f5f5; color: #595959; border: none;">
              <Icon icon="icon-fuwuqi" class="mr-1" />
              {{ deviceDetail.node }}
            </Tag>
          </div>
        </div>

        <!-- Right: Action Buttons -->
        <div class="device-actions">
          <Button
            type="primary"
            size="large"
            class="action-btn start-test-btn"
            @click="handleStartTest">
            <Icon icon="icon-kaishi" class="mr-1" />
            开始测试
          </Button>
          <Button
            type="default"
            size="large"
            class="action-btn adb-shell-btn"
            @click="handleAdbShell">
            <Icon icon="icon-xiajiantou-copy" class="mr-1" />_
            ADB Shell
          </Button>
        </div>
      </div>
    </Card>

    <Tabs v-model:activeKey="activeTab" class="device-tabs">
        <TabPane key="info" tab="基本信息">
          <div class="tab-content">
            <!-- Device Basic Information -->
            <div class="info-section">
              <div class="section-title">设备基本信息</div>
              <Grid :columns="bsicInfoColumns" :dataSource="deviceDetail" />
            </div>

            <!-- Display and Hardware Information -->
            <div class="info-section">
              <div class="section-title">显示与硬件信息</div>
              <Grid :columns="screenInfoColumns" :dataSource="deviceDetail" />
            </div>

            <!-- Connection and Node Information -->
            <div class="info-section">
              <div class="section-title">连接与节点信息</div>
              <Grid :columns="connectionNodeColumns" :dataSource="deviceDetail" />
            </div>
          </div>
        </TabPane>
        <TabPane key="apps" tab="安装应用">
          <div class="tab-content">
            <!-- Installed Applications Header -->
            <div class="apps-header">
              <div class="apps-title">已安装应用</div>
              <div class="apps-actions">
                <Button
                  type="primary"
                  size="small"
                  class="install-btn"
                  @click="handleInstallApp">
                  <Icon icon="icon-jia" class="mr-1" />
                  安装应用
                </Button>
                <Button
                  type="default"
                  size="small"
                  class="refresh-btn"
                  @click="handleRefreshApps">
                  <Icon icon="icon-shuaxin" class="mr-1" />
                  刷新列表
                </Button>
              </div>
            </div>

            <!-- Application Cards Grid -->
            <div class="apps-grid">
              <Card
                v-for="app in installedApps"
                :key="app.id"
                class="app-card">
                <div class="app-card-content">
                  <div class="app-icon" :style="{ backgroundColor: app.iconColor }">
                    <Icon icon="icon-yingyong" class="app-icon-svg" />
                  </div>
                  <div class="app-info">
                    <div class="app-name">{{ app.name }}</div>
                    <div class="app-package">{{ app.packageName }}</div>
                    <div class="app-details">
                      <div class="app-detail-item">
                        <span class="detail-label">版本:</span>
                        <span class="detail-value">{{ app.version }}</span>
                      </div>
                      <div class="app-detail-item">
                        <span class="detail-label">类型:</span>
                        <Tag
                          :style="{ backgroundColor: getAppTypeConfig(app.type).bgColor, color: getAppTypeConfig(app.type).color, border: 'none' }">
                          {{ getAppTypeConfig(app.type).text }}
                        </Tag>
                      </div>
                      <div class="app-detail-item">
                        <span class="detail-label">大小:</span>
                        <span class="detail-value">{{ app.size }}</span>
                      </div>
                      <div class="app-detail-item">
                        <span class="detail-label">日期:</span>
                        <span class="detail-value">{{ app.date }}</span>
                      </div>
                    </div>
                  </div>
                </div>
              </Card>
            </div>

            <!-- Application Statistics -->
            <div class="app-statistics-section">
              <div class="section-title">应用统计</div>
              <Grid :columns="appStatisticsColumns" :dataSource="appStatistics" />
              <div class="statistics-grid">
              </div>
            </div>
          </div>
        </TabPane>
        <TabPane key="performance" tab="性能监控">
          <div class="tab-content">
            <Performance />
          </div>
        </TabPane>
        <TabPane key="logs" tab="设备日志">
          <div class="tab-content logs-content">
            <!-- Logs Header -->
            <div class="logs-header">
              <div class="logs-title">
                <Icon icon="icon-wendang" class="logs-title-icon" />
                <span>设备日志</span>
              </div>
              <div class="logs-actions">
                <Select
                  v-model:value="logTypeFilter"
                  :options="logTypeOptions"
                  class="log-filter-select"
                  style="width: 120px" />
                <Select
                  v-model:value="logLevelFilter"
                  :options="logLevelOptions"
                  class="log-filter-select"
                  style="width: 120px" />
                <Button
                  type="default"
                  size="small"
                  class="export-btn"
                  @click="handleExportLogs">
                  <Icon icon="icon-xiajiantou" class="mr-1" />
                  导出日志
                </Button>
                <Button
                  type="primary"
                  size="small"
                  class="clear-btn"
                  @click="handleClearLogs">
                  <Icon icon="icon-qingchu" class="mr-1" />
                  清空日志
                </Button>
              </div>
            </div>

            <!-- Logs List -->
            <div class="logs-container">
              <div
                v-for="log in filteredLogs"
                :key="log.id"
                class="log-entry"
                :class="`log-${log.level}`">
                <span class="log-time">{{ log.time }}</span>
                <span class="log-level" :style="{ color: getLogLevelConfig(log.level).color }">
                  [{{ getLogLevelConfig(log.level).text }}]
                </span>
                <span class="log-message">{{ log.message }}</span>
              </div>
              <div v-if="filteredLogs.length === 0" class="logs-empty">
                暂无日志数据
              </div>
            </div>
          </div>
        </TabPane>
        <TabPane key="settings" tab="设备设置">
          <div class="tab-content settings-content">
            <!-- Settings Header -->
            <div class="settings-header">
              <div class="settings-title">
                <Icon icon="icon-shezhi" class="settings-title-icon" />
                <span>设备设置</span>
              </div>
            </div>

            <!-- Appium Configuration and Device Control -->
            <div class="settings-panels">
              <!-- Appium Configuration Panel -->
              <Card class="settings-panel">
                <template #title>
                  <div class="panel-title">Appium配置</div>
                </template>
                <div class="config-list">
                  <div class="config-item">
                    <span class="config-label">自动化引擎:</span>
                    <span class="config-value">{{ appiumConfig.automationEngine }}</span>
                  </div>
                  <div class="config-item">
                    <span class="config-label">Appium端口:</span>
                    <span class="config-value">{{ appiumConfig.appiumPort }}</span>
                  </div>
                  <div class="config-item">
                    <span class="config-label">会话超时:</span>
                    <span class="config-value">{{ appiumConfig.sessionTimeout }}秒</span>
                  </div>
                  <div class="config-item">
                    <span class="config-label">自动授权:</span>
                    <span class="config-value" :class="{ 'enabled': appiumConfig.autoAuthorization, 'disabled': !appiumConfig.autoAuthorization }">
                      {{ appiumConfig.autoAuthorization ? '启用' : '禁用' }}
                    </span>
                  </div>
                  <div class="config-item">
                    <span class="config-label">无重置模式:</span>
                    <span class="config-value" :class="{ 'enabled': appiumConfig.noResetMode, 'disabled': !appiumConfig.noResetMode }">
                      {{ appiumConfig.noResetMode ? '启用' : '禁用' }}
                    </span>
                  </div>
                </div>
              </Card>

              <!-- Device Control Panel -->
              <Card class="settings-panel">
                <template #title>
                  <div class="panel-title">设备控制</div>
                </template>
                <div class="control-list">
                  <div class="control-item">
                    <span class="control-label">屏幕旋转</span>
                    <Checkbox
                      v-model:checked="deviceControls.screenRotation"
                      @change="(e) => handleControlChange('screenRotation', e.target.checked)" />
                  </div>
                  <div class="control-item">
                    <span class="control-label">触摸提示</span>
                    <Checkbox
                      v-model:checked="deviceControls.touchHints"
                      @change="(e) => handleControlChange('touchHints', e.target.checked)" />
                  </div>
                  <div class="control-item">
                    <span class="control-label">性能日志</span>
                    <Checkbox
                      v-model:checked="deviceControls.performanceLog"
                      @change="(e) => handleControlChange('performanceLog', e.target.checked)" />
                  </div>
                  <div class="control-item">
                    <span class="control-label">自动截图</span>
                    <Checkbox
                      v-model:checked="deviceControls.autoScreenshot"
                      @change="(e) => handleControlChange('autoScreenshot', e.target.checked)" />
                  </div>
                </div>
              </Card>
            </div>

            <!-- Advanced Settings -->
            <Card class="advanced-settings-card">
              <template #title>
                <div class="panel-title">
                  <Icon icon="icon-jinggao" class="panel-title-icon" />
                  <span>高级设置</span>
                </div>
              </template>
              <div class="dangerous-operations">
                <div class="dangerous-header">
                  <Icon icon="icon-jinggao" class="warning-icon" />
                  <span class="dangerous-title">危险操作区域</span>
                </div>
                <div class="dangerous-buttons">
                  <Button
                    type="default"
                    size="large"
                    class="dangerous-btn restart-btn"
                    @click="handleRestartDevice">
                    <Icon icon="icon-shuaxin" class="mr-1" />
                    重启设备
                  </Button>
                  <Button
                    type="default"
                    size="large"
                    class="dangerous-btn factory-reset-btn"
                    @click="handleFactoryReset">
                    <Icon icon="icon-qingchu" class="mr-1" />
                    恢复出厂设置
                  </Button>
                  <Button
                    type="primary"
                    danger
                    size="large"
                    class="dangerous-btn force-disconnect-btn"
                    @click="handleForceDisconnect">
                    <Icon icon="icon-guanji" class="mr-1" />
                    强制断开
                  </Button>
                </div>
                <div class="dangerous-warning">
                  <Icon icon="icon-tishi1" class="warning-info-icon" />
                  <span class="warning-text">警告:这些操作可能会导致设备数据丢失或测试中断,请谨慎操作!</span>
                </div>
              </div>
            </Card>
          </div>
        </TabPane>
      </Tabs>


  </div>
</template>

<style scoped>
.device-detail-page {

  height: 100%;
  padding: 20px;
  overflow: auto;
}

/* Header Bar */
.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  margin-bottom: 20px;
}

.header-left {
  flex: 1;
}

.back-button {
  background: #f5f5f5;
  border: 1px solid #d9d9d9;
  color: #595959;
}

.back-button:hover {
  background: #e6e6e6;
  border-color: #bfbfbf;
  color: #262626;
}

.header-center {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
}

.header-icon {
  font-size: 18px;
  color: #1890ff;
}

.header-title {
  font-size: 18px;
  font-weight: 600;
  color: #262626;
}

.action-button {
  display: flex;
  align-items: center;
}

.restart-button {
  background: #fff7e6;
  border-color: #ffd591;
  color: #fa8c16;
}

.restart-button:hover {
  background: #ffe7ba;
  border-color: #ffc069;
  color: #d46b08;
}

.disconnect-button {
  background: #fff1f0;
  border-color: #ffccc7;
  color: #ff4d4f;
}

.disconnect-button:hover {
  background: #ffccc7;
  border-color: #ffa39e;
  color: #cf1322;
}

.refresh-button {
  background: #f6ffed;
  border-color: #b7eb8f;
  color: #52c41a;
}

.refresh-button:hover {
  background: #d9f7be;
  border-color: #95de64;
  color: #389e0d;
}

/* Device Detail Card */
.device-detail-card {
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.device-detail-content {
  display: flex;
  align-items: center;
  gap: 30px;
}

/* Android Icon */
.device-icon-wrapper {
  flex-shrink: 0;
}

.android-icon {
  width: 120px;
  height: 120px;
  background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(82, 196, 26, 0.3);
}

.android-icon-svg {
  width: 80px;
  height: 80px;
}

/* Device Info Section */
.device-info-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.device-name {
  font-size: 28px;
  font-weight: 600;
  color: #262626;
  line-height: 1.2;
}

.device-info-lines {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.device-info-line {
  font-size: 14px;
  color: #595959;
  line-height: 1.6;
}

.info-label {
  font-weight: 500;
  margin-right: 8px;
}

.info-value {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', 'Consolas', monospace;
  color: #262626;
}

.device-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

/* Device Actions */
.device-actions {
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-width: 140px;
}

.action-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 44px;
  font-size: 14px;
  font-weight: 500;
  border-radius: 8px;
  transition: all 0.3s;
}

.start-test-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.3);
}


.adb-shell-btn:hover {
  color: #262626;
  transform: translateY(-2px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

/* Card Styles */
:deep(.ant-card) {
  background: #fff;
}

:deep(.ant-card-body) {
  padding: 0;
}

/* Responsive Design */
@media (max-width: 1200px) {
  .device-detail-content {
    flex-direction: column;
    align-items: flex-start;
  }

  .device-actions {
    width: 100%;
    flex-direction: row;
  }

  .action-btn {
    flex: 1;
  }
}

@media (max-width: 768px) {
  .detail-header {
    flex-direction: column;
    gap: 12px;
  }

  .device-detail-content {
    padding: 20px;
  }

  .android-icon {
    width: 80px;
    height: 80px;
  }

  .android-icon-svg {
    width: 50px;
    height: 50px;
  }

  .device-name {
    font-size: 22px;
  }
}

/* Tabs Card */
.tabs-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.device-tabs {
  margin-top: 0;
}

.device-tabs :deep(.ant-tabs-nav) {
  margin: 0;
  padding: 0 20px;
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
}

.device-tabs :deep(.ant-tabs-tab) {
  padding: 16px 20px;
  font-size: 14px;
  color: #595959;
}

.device-tabs :deep(.ant-tabs-tab-active) {
  color: #1890ff;
}

.device-tabs :deep(.ant-tabs-tab-active .ant-tabs-tab-btn) {
  color: #1890ff;
  font-weight: 500;
}

.device-tabs :deep(.ant-tabs-ink-bar) {
  background: #1890ff;
}

.device-tabs :deep(.ant-tabs-content-holder) {
  padding: 0;
}

/* Tab Content */
.tab-content {
  padding: 24px;
  min-height: 400px;
}

/* Info Section */
.info-section {
  margin-bottom: 32px;
}

.info-section:last-child {
  margin-bottom: 0;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #262626;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24px;
}

.info-column {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.info-item {
  display: flex;
  align-items: flex-start;
  font-size: 14px;
  line-height: 1.6;
}

.info-item .info-label {
  min-width: 120px;
  color: #8c8c8c;
  font-weight: 500;
  flex-shrink: 0;
}

.info-item .info-value {
  color: #262626;
  word-break: break-all;
}

/* Empty Placeholder */
.empty-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 300px;
  color: #8c8c8c;
  font-size: 14px;
}

/* Responsive Design for Tabs */
@media (max-width: 768px) {
  .info-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }

  .info-item .info-label {
    min-width: 100px;
  }

  .tab-content {
    padding: 16px;
  }
}

/* Installed Applications Styles */
.apps-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.apps-title {
  font-size: 16px;
  font-weight: 600;
  color: #262626;
}

.apps-actions {
  display: flex;
  gap: 12px;
}

.install-btn {
  background: #1890ff;
  border-color: #1890ff;
  color: #fff;
}

.install-btn:hover {
  background: #40a9ff;
  border-color: #40a9ff;
}

.refresh-btn {
  background: #fff;
  border-color: #d9d9d9;
  color: #595959;
}

.refresh-btn:hover {
  border-color: #40a9ff;
  color: #1890ff;
}

/* Application Cards Grid */
.apps-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 32px;
}

.app-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.3s;
  border: 1px solid #f0f0f0;
}

.app-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
  transform: translateY(-2px);
}

.app-card-content {
  padding: 16px;
  display: flex;
  gap: 16px;
}

.app-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.app-icon-svg {
  font-size: 28px;
  color: #fff;
}

.app-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-width: 0;
}

.app-name {
  font-size: 16px;
  font-weight: 600;
  color: #262626;
  line-height: 1.4;
}

.app-package {
  font-size: 12px;
  color: #8c8c8c;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', 'Consolas', monospace;
  word-break: break-all;
}

.app-details {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-top: 4px;
}

.app-detail-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
}

.detail-label {
  color: #8c8c8c;
  min-width: 40px;
  flex-shrink: 0;
}

.detail-value {
  color: #262626;
  word-break: break-all;
}

/* Application Statistics */
.app-statistics-section {
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid #f0f0f0;
}

.statistics-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24px;
}

.statistics-column {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.statistics-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
  padding: 12px;
  background: #fafafa;
  border-radius: 6px;
}

.statistics-label {
  color: #8c8c8c;
  font-weight: 500;
}

.statistics-value {
  color: #262626;
  font-weight: 600;
}

/* Responsive Design for Apps */
@media (max-width: 1200px) {
  .apps-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .apps-grid {
    grid-template-columns: 1fr;
  }

  .apps-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .apps-actions {
    width: 100%;
  }

  .statistics-grid {
    grid-template-columns: 1fr;
  }
}

/* Device Logs Styles */
.logs-content {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.logs-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.logs-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #262626;
}

.logs-title-icon {
  font-size: 18px;
  color: #1890ff;
}

.logs-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.log-filter-select {
  min-width: 120px;
}

.export-btn {
  background: #fff;
  border-color: #d9d9d9;
  color: #595959;
}

.export-btn:hover {
  border-color: #40a9ff;
  color: #1890ff;
}

.clear-btn {
  background: #1890ff;
  border-color: #1890ff;
  color: #fff;
}

.clear-btn:hover {
  background: #40a9ff;
  border-color: #40a9ff;
}

/* Logs Container */
.logs-container {
  flex: 1;
  background: #1e1e1e;
  border-radius: 8px;
  padding: 16px;
  overflow-y: auto;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', 'Consolas', 'Courier New', monospace;
  min-height: 300px;
  max-height: 400px;
}

.log-entry {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 8px 0;
  font-size: 13px;
  line-height: 1.6;
  color: #d4d4d4;
  border-bottom: 1px solid rgba(255, 255, 255, 0.05);
  transition: background-color 0.2s;
}

.log-entry:hover {
  background-color: rgba(255, 255, 255, 0.03);
}

.log-entry:last-child {
  border-bottom: none;
}

.log-time {
  color: #858585;
  flex-shrink: 0;
  min-width: 80px;
  font-weight: 500;
}

.log-level {
  flex-shrink: 0;
  min-width: 60px;
  font-weight: 600;
}

.log-message {
  flex: 1;
  color: #d4d4d4;
  word-break: break-word;
}

/* Log Level Colors */
.log-entry.log-info .log-level {
  color: #69c0ff;
}

.log-entry.log-warn .log-level {
  color: #ffa940;
}

.log-entry.log-error .log-level {
  color: #ff7875;
}

.logs-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 300px;
  color: #858585;
  font-size: 14px;
}

/* Scrollbar Styling for Logs Container */
.logs-container::-webkit-scrollbar {
  width: 8px;
}

.logs-container::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 4px;
}

.logs-container::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.2);
  border-radius: 4px;
}

.logs-container::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 255, 255, 0.3);
}

/* Responsive Design for Logs */
@media (max-width: 768px) {
  .logs-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .logs-actions {
    width: 100%;
    flex-wrap: wrap;
  }

  .log-filter-select {
    flex: 1;
    min-width: 100px;
  }

  .logs-container {
    min-height: 400px;
  }
}

/* Device Settings Styles */
.settings-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.settings-header {
  margin-bottom: 8px;
}

.settings-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #262626;
}

.settings-title-icon {
  font-size: 18px;
  color: #1890ff;
}

/* Settings Panels */
.settings-panels {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.settings-panel {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.panel-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: #262626;
}

.panel-title-icon {
  font-size: 16px;
  color: #fa8c16;
}

.config-list,
.control-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 8px 0;
}

.config-item,
.control-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.config-item:last-child,
.control-item:last-child {
  border-bottom: none;
}

.config-label,
.control-label {
  font-size: 14px;
  color: #595959;
  font-weight: 500;
}

.config-value {
  font-size: 14px;
  color: #262626;
  font-weight: 500;
}

.config-value.enabled {
  color: #52c41a;
}

.config-value.disabled {
  color: #8c8c8c;
}

/* Advanced Settings */
.advanced-settings-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.dangerous-operations {
  padding: 8px 0;
}

.dangerous-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 20px;
  padding: 12px 16px;
  background: #fff1f0;
  border-radius: 6px;
  border-left: 4px solid #ff4d4f;
}

.warning-icon {
  font-size: 18px;
  color: #ff4d4f;
}

.dangerous-title {
  font-size: 15px;
  font-weight: 600;
  color: #ff4d4f;
}

.dangerous-buttons {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.dangerous-btn {
  display: flex;
  align-items: center;
  height: 40px;
  font-size: 14px;
  font-weight: 500;
  border-radius: 6px;
  transition: all 0.3s;
}

.restart-btn {
  background: #fff7e6;
  border-color: #ffd591;
  color: #fa8c16;
}

.restart-btn:hover {
  background: #ffe7ba;
  border-color: #ffc069;
  color: #d46b08;
}

.factory-reset-btn {
  background: #f5f5f5;
  border-color: #d9d9d9;
  color: #595959;
}

.factory-reset-btn:hover {
  background: #e6e6e6;
  border-color: #bfbfbf;
  color: #262626;
}

.force-disconnect-btn {
  background: #ff4d4f;
  border-color: #ff4d4f;
  color: #fff;
}

.force-disconnect-btn:hover {
  background: #ff7875;
  border-color: #ff7875;
}

.dangerous-warning {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 12px 16px;
  background: #fff7e6;
  border-radius: 6px;
  border-left: 4px solid #fa8c16;
}

.warning-info-icon {
  font-size: 16px;
  color: #fa8c16;
  flex-shrink: 0;
  margin-top: 2px;
}

.warning-text {
  font-size: 13px;
  color: #d46b08;
  line-height: 1.6;
}

/* Responsive Design for Settings */
@media (max-width: 1200px) {
  .settings-panels {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .dangerous-buttons {
    flex-direction: column;
  }

  .dangerous-btn {
    width: 100%;
    justify-content: center;
  }
}
</style>

