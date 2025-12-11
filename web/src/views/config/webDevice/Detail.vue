<script setup lang="ts">
import { ref, computed, defineAsyncComponent } from 'vue';
import { Icon, Card, Grid, Select } from '@xcan-angus/vue-ui';
import { Button, Tag, Switch } from 'ant-design-vue';
import type { VersionListProps } from '../mobileDevice/types';
import { Tabs, TabPane } from 'ant-design-vue';

const Performance = defineAsyncComponent(() => import('./Performance.vue'));

// Component props with default values
withDefaults(defineProps<VersionListProps & { data?: any }>(), {
  data: undefined
});

// Active tab
const activeTab = ref('info');

// Browser instance detail data
const browserDetail = ref({
  instanceName: 'Chrome实例-01',
  instanceId: 'chrome-instance-01',
  hostAddress: '192.168.1.101:5555',
  status: 'online',
  browserType: 'chrome',
  version: '118.0.5993.88',
  os: 'windows',
  sessionCount: 3,
  maxSessions: 5,
  lastActivity: '10秒前'
});

// Browser basic information data
const browserBasicInfo = ref({
  instanceId: 'chrome-instance-01',
  instanceName: 'Chrome实例-01',
  instanceRole: '执行实例',
  registrationTime: '2023-10-15 08:30:00',
  lastHeartbeat: '10秒前',
  browserType: 'Chrome',
  browserVersion: '118.0.5993.88',
  webDriverVersion: 'ChromeDriver 118.0.5993.70',
  supportedProtocol: 'W3C WebDriver',
  seleniumVersion: '4.16.0'
});

// System and network information data
const systemNetworkInfo = ref({
  operatingSystem: 'Windows',
  osFullName: 'Windows 11',
  systemArchitecture: 'x86_64',
  systemVersion: 'Windows 11',
  javaVersion: 'OpenJDK 17.0.8',
  browserTimezone: 'Asia/Shanghai',
  hostAddress: '192.168.1.101',
  instancePort: '5555',
  hubAddress: 'http://192.168.1.100:4444',
  connectionStatus: '稳定连接',
  networkLatency: '12ms'
});

// Resource and capability information data
const resourceCapabilityInfo = ref({
  cpuCores: '8核',
  cpuUsage: '42%',
  totalMemory: '16GB',
  availableMemory: '8.2 GB',
  memoryUsage: '48%',
  maxSessions: '5',
  currentSessions: '3',
  sessionTimeout: '300秒',
  browserTimeout: '30秒',
  supportedResolutions: '1920x1080, 1366x768'
});

// Basic information columns
const basicInfoColumns = [
  [
    {
      label: '实例ID',
      dataIndex: 'instanceId'
    },
    {
      label: '浏览器实例',
      dataIndex: 'instanceName'
    },
    {
      label: '实例角色',
      dataIndex: 'instanceRole'
    },
    {
      label: '注册时间',
      dataIndex: 'registrationTime'
    },
    {
      label: '最后心跳',
      dataIndex: 'lastHeartbeat'
    }
  ],
  [
    {
      label: '浏览器类型',
      dataIndex: 'browserType'
    },
    {
      label: '浏览器版本',
      dataIndex: 'browserVersion'
    },
    {
      label: 'WebDriver版本',
      dataIndex: 'webDriverVersion'
    },
    {
      label: '支持协议',
      dataIndex: 'supportedProtocol'
    },
    {
      label: 'Selenium版本',
      dataIndex: 'seleniumVersion'
    }
  ]
];

// System and network information columns
const systemNetworkColumns = [
  [
    {
      label: '操作系统',
      dataIndex: 'operatingSystem'
    },
    {
      label: '系统架构',
      dataIndex: 'systemArchitecture'
    },
    {
      label: '系统版本',
      dataIndex: 'systemVersion'
    },
    {
      label: 'Java版本',
      dataIndex: 'javaVersion'
    },
    {
      label: '浏览器时区',
      dataIndex: 'browserTimezone'
    }
  ],
  [
    {
      label: '主机地址',
      dataIndex: 'hostAddress'
    },
    {
      label: '实例端口',
      dataIndex: 'instancePort'
    },
    {
      label: 'Hub地址',
      dataIndex: 'hubAddress'
    },
    {
      label: '连接状态',
      dataIndex: 'connectionStatus'
    },
    {
      label: '网络延迟',
      dataIndex: 'networkLatency'
    }
  ]
];

// Resource and capability information columns
const resourceCapabilityColumns = [
  [
    {
      label: 'CPU核心数',
      dataIndex: 'cpuCores'
    },
    {
      label: 'CPU使用率',
      dataIndex: 'cpuUsage'
    },
    {
      label: '总内存',
      dataIndex: 'totalMemory'
    },
    {
      label: '可用内存',
      dataIndex: 'availableMemory'
    },
    {
      label: '内存使用率',
      dataIndex: 'memoryUsage'
    }
  ],
  [
    {
      label: '最大会话数',
      dataIndex: 'maxSessions'
    },
    {
      label: '当前会话数',
      dataIndex: 'currentSessions'
    },
    {
      label: '会话超时',
      dataIndex: 'sessionTimeout'
    },
    {
      label: '浏览器超时',
      dataIndex: 'browserTimeout'
    },
    {
      label: '支持分辨率',
      dataIndex: 'supportedResolutions'
    }
  ]
];

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

// Browser type configuration
const getBrowserTypeConfig = (type: string) => {
  const configs: Record<string, { text: string; icon: string; color: string; bgColor: string }> = {
    chrome: { text: 'Chrome', icon: 'icon-chrome', color: '#4285f4', bgColor: '#e8f0fe' },
    firefox: { text: 'Firefox', icon: 'icon-firefox', color: '#ff7139', bgColor: '#fff4f0' },
    safari: { text: 'Safari', icon: 'icon-safari', color: '#000000', bgColor: '#f5f5f5' },
    edge: { text: 'Edge', icon: 'icon-edge', color: '#0078d4', bgColor: '#e6f2ff' }
  };
  return configs[type] || configs.chrome;
};

// OS configuration
const getOSConfig = (os: string) => {
  const configs: Record<string, { text: string; color: string; bgColor: string }> = {
    windows: { text: 'Windows', color: '#1890ff', bgColor: '#e6f7ff' },
    linux: { text: 'Linux', color: '#fa8c16', bgColor: '#fff7e6' },
    macos: { text: 'macOS', color: '#722ed1', bgColor: '#f9f0ff' }
  };
  return configs[os] || configs.windows;
};

// Handle actions
const handleRestartBrowser = () => {
  console.log('Restart browser');
};

const handleStopBrowser = () => {
  console.log('Stop browser');
};

const handleRefresh = () => {
  console.log('Refresh browser');
};

const handleCreateSession = () => {
  console.log('Create session');
};

const handleOpenConsole = () => {
  console.log('Open console');
};

// Active sessions data
const activeSessions = ref([
  {
    id: 'f3a7b8c9d0e1',
    sessionId: 'f3a7b8c9d0e1f2a3b4c5d6e7f8a9b0c1d2',
    taskName: '登录功能测试',
    status: 'active',
    browser: 'chrome 118.0.5993.88',
    startTime: '2023-10-15 09:30:22',
    runtime: '15分钟',
    testUser: 'tester01'
  },
  {
    id: 'a1b2c3d4e5f6',
    sessionId: 'a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6',
    taskName: '商品搜索测试',
    status: 'active',
    browser: 'chrome 118.0.5993.88',
    startTime: '2023-10-15 09:45:10',
    runtime: '5分钟',
    testUser: 'tester02'
  },
  {
    id: 'k2l3m4n5o6p7',
    sessionId: 'k2l3m4n5o6p7q8r9s0t1u2v3w4x5y6z7',
    taskName: '等待测试分配',
    status: 'idle',
    browser: 'chrome 118.0.5993.88',
    startTime: '2023-10-15 10:00:05',
    runtime: '2分钟',
    testUser: 'system'
  }
]);

const sessionColumns = [
  [
  {
    label: '总会话数',
    dataIndex: 'totalSessions'
  },
  {
    label: '活动会话',
    dataIndex: 'activeSessions'
  },
  {
    label: '空闲会话',
    dataIndex: 'idleSessions'
  },
  {
    label: '会话容量',
    dataIndex: 'sessionCapacity'
  }

  ],
  [
    {
      label: '平均会话时长',
      dataIndex: 'averageDuration'
    },
    {
      label: '最长会话',
      dataIndex: 'longestSession'
    },
    {
      label: '今日创建',
      dataIndex: 'createdToday'
    },
    {
      label: '失败会话',
      dataIndex: 'failedSessions'
    }
  ]
]

// Session statistics data
const sessionStatistics = ref({
  totalSessions: 3,
  activeSessions: 2,
  idleSessions: 1,
  sessionCapacity: '3/5 (60%)',
  averageDuration: '15分钟',
  longestSession: '45分钟',
  createdToday: 12,
  failedSessions: 1
});

// Get session status config
const getSessionStatusConfig = (status: string) => {
  const configs: Record<string, { text: string; color: string; bgColor: string }> = {
    active: { text: '活动中', color: '#1890ff', bgColor: '#e6f7ff' },
    idle: { text: '空闲', color: '#8c8c8c', bgColor: '#fafafa' },
    failed: { text: '失败', color: '#ff4d4f', bgColor: '#fff1f0' }
  };
  return configs[status] || configs.idle;
};

// Format session ID (truncate)
const formatSessionId = (sessionId: string) => {
  if (sessionId.length > 20) {
    return sessionId.substring(0, 20) + '...';
  }
  return sessionId;
};

// Handle session actions
const handleRefreshSessions = () => {
  console.log('Refresh sessions');
};

const handleViewSession = (sessionId: string) => {
  console.log('View session:', sessionId);
};

const handleStopSession = (sessionId: string) => {
  console.log('Stop session:', sessionId);
};

// Browser Logs Data
const logTypeFilter = ref('all');
const logLevelFilter = ref('all');

const logTypeOptions = [
  { label: '全部日志', value: 'all' }
];

const logLevelOptions = [
  { label: '全部级别', value: 'all' },
  { label: 'INFO', value: 'info' },
  { label: 'WARN', value: 'warn' },
  { label: 'ERROR', value: 'error' }
];

const browserLogs = ref([
  {
    id: '1',
    time: '08:30:00',
    level: 'info',
    message: '浏览器实例启动成功 - chrome-instance-01'
  },
  {
    id: '2',
    time: '08:30:05',
    level: 'info',
    message: '注册到Hub: http://192.168.1.100:4444'
  },
  {
    id: '3',
    time: '08:30:10',
    level: 'info',
    message: '浏览器驱动初始化 - ChromeDriver 118.0.5993.70'
  },
  {
    id: '4',
    time: '08:30:15',
    level: 'info',
    message: '浏览器配置加载完成 - 最大会话数: 5'
  },
  {
    id: '5',
    time: '09:30:22',
    level: 'info',
    message: '创建新会话 - sessionld: f3a7b8c9d0e1f2a3b4c5d6e7'
  },
  {
    id: '6',
    time: '09:30:25',
    level: 'info',
    message: '会话能力: {browserName: chrome, browserVersion: 118.0}'
  },
  {
    id: '7',
    time: '09:45:10',
    level: 'info',
    message: '创建新会话 - sessionld: a1b2c3d4e5f6g7h8i9j0k1'
  },
  {
    id: '8',
    time: '10:00:05',
    level: 'info',
    message: '创建新会话 - sessionld: k213m4n5o6p7q8r9s0t1u2'
  },
  {
    id: '9',
    time: '10:15:30',
    level: 'warn',
    message: '会话超时检测 - 准备清理空闲会话'
  },
  {
    id: '10',
    time: '10:20:15',
    level: 'info',
    message: '心跳发送成功 - 浏览器状态正常'
  },
  {
    id: '11',
    time: '10:30:45',
    level: 'info',
    message: '性能监控数据上报 - CPU: 42%, 内存: 7.8GB'
  },
  {
    id: '12',
    time: '10:45:20',
    level: 'info',
    message: '浏览器运行正常 - 活动会话: 3, 空闲会话: 0'
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
  let logs = browserLogs.value;
  
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
  browserLogs.value = [];
};

// Browser Configuration Data
const seleniumConfig = ref({
  hubAddress: 'http://192.168.1.100:4444',
  instancePort: '5555',
  sessionTimeout: '300秒',
  heartbeatInterval: '10秒',
  maxSessions: '5'
});

const browserSettings = ref({
  headlessMode: false,
  disableGPU: false,
  sandboxMode: true,
  enableLogs: true
});

const capabilityConfig = ref({
  browserName: 'chrome',
  browserVersion: '118.0',
  platformName: 'windows',
  acceptInsecureCerts: true,
  timeouts: {
    implicit: 0,
    pageLoad: 30000,
    script: 30000
  },
  'goog:chromeOptions': {
    args: [
      '--disable-gpu',
      '--no-sandbox',
      '--disable-dev-shm-usage'
    ]
  }
});

const environmentVariables = ref({
  SE_NODE_MAX_SESSIONS: '5',
  SE_NODE_SESSION_TIMEOUT: '300',
  SE_NODE_OVERRIDE_MAX_SESSIONS: 'true',
  SE_NODE_GRID_URL: 'http://192.168.1.100:4444'
});

// Format JSON for display
const formattedCapabilityConfig = computed(() => {
  return JSON.stringify(capabilityConfig.value, null, 2);
});

// Handle browser settings changes
const handleBrowserSettingChange = (key: string, value: boolean | string | number) => {
  (browserSettings.value as any)[key] = Boolean(value);
  console.log(`Browser setting ${key} changed to:`, value);
};

// Handle browser control actions
const handleDeregisterInstance = () => {
  console.log('Deregister instance');
};

const handleForceTerminateSession = () => {
  console.log('Force terminate session');
};
</script>

<template>
  <div class="browser-detail-page">
    <div class="flex space-x-2 mb-2">
      <Button
        type="default"
        size="small"
        class="action-button restart-button"
        @click="handleRestartBrowser">
        <Icon icon="icon-shuaxin" class="mr-1" />
        重启浏览器
      </Button>
      <Button
        type="default"
        size="small"
        danger
        class="action-button stop-button"
        @click="handleStopBrowser">
        <Icon icon="icon-jinyong" class="mr-1" />
        停止浏览器
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
    <!-- Browser Instance Detail Card -->
    <Card class="browser-detail-card">
      <div class="browser-detail-content">
        <!-- Left: Browser Icon -->
        <div class="browser-icon-wrapper">
          <div
            class="browser-icon"
            :style="{ backgroundColor: getBrowserTypeConfig(browserDetail.browserType).color }">
            <Icon
              :icon="getBrowserTypeConfig(browserDetail.browserType).icon"
              class="browser-icon-svg" />
          </div>
        </div>

        <!-- Center: Browser Info -->
        <div class="browser-info-section">
          <div class="browser-name">{{ browserDetail.instanceName }}</div>
          <div class="browser-details">
            <span class="detail-text">实例ID: {{ browserDetail.instanceId }}</span>
            <span class="detail-separator">|</span>
            <span class="detail-text">主机: {{ browserDetail.hostAddress }}</span>
          </div>
          <div class="browser-tags">
            <Tag
              :style="{ backgroundColor: getStatusConfig(browserDetail.status).bgColor, color: getStatusConfig(browserDetail.status).color, border: 'none' }">
              {{ getStatusConfig(browserDetail.status).text }}
            </Tag>
            <Tag
              :style="{ backgroundColor: getBrowserTypeConfig(browserDetail.browserType).bgColor, color: getBrowserTypeConfig(browserDetail.browserType).color, border: 'none' }">
              <Icon
                :icon="getBrowserTypeConfig(browserDetail.browserType).icon"
                class="mr-1" />
              {{ getBrowserTypeConfig(browserDetail.browserType).text }}
            </Tag>
            <Tag
              :style="{ backgroundColor: getOSConfig(browserDetail.os).bgColor, color: getOSConfig(browserDetail.os).color, border: 'none' }">
              {{ getOSConfig(browserDetail.os).text }}
            </Tag>
            <Tag
              style="background-color: #f5f5f5; color: #595959; border: none;">
              <Icon icon="icon-wenjianjia" class="mr-1" />
              {{ browserDetail.sessionCount }}/{{ browserDetail.maxSessions }} 会话
            </Tag>
          </div>
        </div>

        <!-- Right: Action Buttons -->
        <div class="browser-actions">
          <Button
            type="primary"
            size="large"
            class="action-btn create-session-btn"
            @click="handleCreateSession">
            <Icon icon="icon-kaishi" class="mr-1" />
            创建会话
          </Button>
          <Button
            type="default"
            size="large"
            class="action-btn console-btn"
            @click="handleOpenConsole">
            <Icon icon="icon-xiajiantou-copy" class="mr-1" />
            控制台
          </Button>
        </div>
      </div>
    </Card>

    <!-- Tabs Section -->
    <Tabs v-model:activeKey="activeTab" class="browser-tabs">
      <TabPane key="info" tab="基本信息">
        <template #tab>
          <div class="tab-label">
            <Icon icon="icon-tishi1" class="tab-icon" />
            <span>基本信息</span>
  </div>
</template>
        <div class="tab-content">
          <!-- General Instance Information -->
          <div class="info-section">
            <div class="section-title">浏览器基本信息</div>
            <Grid :columns="basicInfoColumns" :dataSource="browserBasicInfo" />
          </div>

          <!-- System and Network Information -->
          <div class="info-section">
            <div class="section-title">系统与网络信息</div>
            <Grid :columns="systemNetworkColumns" :dataSource="systemNetworkInfo" />
          </div>

          <!-- Resource and Capability Information -->
          <div class="info-section">
            <div class="section-title">资源与能力信息</div>
            <Grid :columns="resourceCapabilityColumns" :dataSource="resourceCapabilityInfo" />
          </div>
        </div>
      </TabPane>
      <TabPane key="sessions" tab="活动会话">
        <template #tab>
          <div class="tab-label">
            <Icon icon="icon-wenjianjia" class="tab-icon" />
            <span>活动会话</span>
          </div>
        </template>
        <div class="tab-content">
          <!-- Session Management Header -->
          <div class="session-header">
            <div class="session-title">
              <Icon icon="icon-wenjianjia" class="session-title-icon" />
              <span>活动会话管理</span>
            </div>
            <div class="session-actions">
              <Button
                type="primary"
                size="small"
                class="create-session-btn"
                @click="handleCreateSession">
                <Icon icon="icon-jia" class="mr-1" />
                创建会话
              </Button>
              <Button
                type="default"
                size="small"
                class="refresh-session-btn"
                @click="handleRefreshSessions">
                <Icon icon="icon-shuaxin" class="mr-1" />
                刷新会话
              </Button>
            </div>
          </div>

          <!-- Session Cards -->
          <div class="session-cards-grid">
            <Card
              v-for="session in activeSessions"
              :key="session.id"
              class="session-card">
              <div class="session-card-content">
                <div class="session-card-header">
                  <Tag
                    :style="{ backgroundColor: getSessionStatusConfig(session.status).bgColor, color: getSessionStatusConfig(session.status).color, border: 'none' }">
                    <Icon
                      icon="icon-liulanqi"
                      class="mr-1"
                      :style="{ color: getSessionStatusConfig(session.status).color }" />
                    {{ getSessionStatusConfig(session.status).text }}
                  </Tag>
                  <div class="session-id">{{ formatSessionId(session.sessionId) }}</div>
                </div>
                <div class="session-task-name">{{ session.taskName }}</div>
                <div class="session-details">
                  <div class="session-detail-item">
                    <span class="detail-label">浏览器:</span>
                    <span class="detail-value">{{ session.browser }}</span>
                  </div>
                  <div class="session-detail-item">
                    <span class="detail-label">开始时间:</span>
                    <span class="detail-value">{{ session.startTime }}</span>
                  </div>
                  <div class="session-detail-item">
                    <span class="detail-label">运行时长:</span>
                    <span class="detail-value">{{ session.runtime }}</span>
                  </div>
                  <div class="session-detail-item">
                    <span class="detail-label">测试用户:</span>
                    <span class="detail-value">{{ session.testUser }}</span>
                  </div>
                </div>
                <div class="session-card-actions">
                  <Button
                    type="default"
                    size="small"
                    class="view-btn"
                    @click="handleViewSession(session.sessionId)">
                    <Icon icon="icon-chakan" class="mr-1" />
                    查看
                  </Button>
                  <Button
                    type="default"
                    size="small"
                    danger
                    class="stop-btn"
                    @click="handleStopSession(session.sessionId)">
                    <Icon icon="icon-tingzhi" class="mr-1" />
                    停止
                  </Button>
                </div>
              </div>
            </Card>
          </div>

          <!-- Session Statistics -->
          <div class="session-statistics-section">
            <div class="section-title">
              <Icon icon="icon-zhexiantu" class="section-title-icon" />
              <span>会话统计</span>
            </div>
            <Grid :columns="sessionColumns" :dataSource="sessionStatistics" />
          </div>
        </div>
      </TabPane>
      <TabPane key="performance" tab="性能监控">
        <template #tab>
          <div class="tab-label">
            <Icon icon="icon-zhexiantu" class="tab-icon" />
            <span>性能监控</span>
          </div>
        </template>
        <div class="tab-content">
          <Performance />
        </div>
      </TabPane>
      <TabPane key="logs" tab="浏览器日志">
        <template #tab>
          <div class="tab-label">
            <Icon icon="icon-wendang" class="tab-icon" />
            <span>浏览器日志</span>
          </div>
        </template>
        <div class="tab-content logs-content">
          <!-- Logs Header -->
          <div class="logs-header">
            <div class="logs-title">
              <Icon icon="icon-wendang" class="logs-title-icon" />
              <span>浏览器日志</span>
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

          <!-- Logs Display Area -->
          <div class="logs-container">
            <div
              v-for="(log, index) in filteredLogs"
              :key="log.id"
              class="log-entry-wrapper">
              <div
                class="log-entry"
                :class="`log-${log.level}`">
                <span class="log-time">{{ log.time }}</span>
                <span class="log-level" :style="{ color: getLogLevelConfig(log.level).color }">
                  [{{ getLogLevelConfig(log.level).text }}]
                </span>
                <span class="log-message">{{ log.message }}</span>
              </div>
              <div v-if="index < filteredLogs.length - 1" class="log-divider"></div>
            </div>
            <div v-if="filteredLogs.length === 0" class="logs-empty">
              暂无日志数据
            </div>
          </div>
        </div>
      </TabPane>
      <TabPane key="config" tab="浏览器配置">
        <template #tab>
          <div class="tab-label">
            <Icon icon="icon-shezhi" class="tab-icon" />
            <span>浏览器配置</span>
          </div>
        </template>
        <div class="tab-content settings-content">
          <!-- Settings Header -->
          <div class="settings-header">
            <div class="settings-title">
              <Icon icon="icon-shezhi" class="settings-title-icon" />
              <span>浏览器配置</span>
            </div>
          </div>

          <!-- Configuration Panels -->
          <div class="settings-panels">
            <!-- Selenium Configuration Panel -->
            <Card class="settings-panel selenium-panel">
              <template #title>
                <div class="panel-title">Selenium配置</div>
              </template>
              <div class="config-list">
                <div class="config-item">
                  <span class="config-label">Hub地址:</span>
                  <span class="config-value">{{ seleniumConfig.hubAddress }}</span>
                </div>
                <div class="config-item">
                  <span class="config-label">实例端口:</span>
                  <span class="config-value">{{ seleniumConfig.instancePort }}</span>
                </div>
                <div class="config-item">
                  <span class="config-label">会话超时:</span>
                  <span class="config-value">{{ seleniumConfig.sessionTimeout }}</span>
                </div>
                <div class="config-item">
                  <span class="config-label">心跳间隔:</span>
                  <span class="config-value">{{ seleniumConfig.heartbeatInterval }}</span>
                </div>
                <div class="config-item">
                  <span class="config-label">最大会话数:</span>
                  <span class="config-value">{{ seleniumConfig.maxSessions }}</span>
                </div>
              </div>
            </Card>

            <!-- Browser Settings Panel -->
            <Card class="settings-panel browser-settings-panel">
              <template #title>
                <div class="panel-title">浏览器设置</div>
              </template>
              <div class="control-list">
                <div class="control-item">
                  <span class="control-label">无头模式</span>
                  <Switch
                    v-model:checked="browserSettings.headlessMode"
                    @change="(checked) => handleBrowserSettingChange('headlessMode', checked)" />
                </div>
                <div class="control-item">
                  <span class="control-label">禁用GPU</span>
                  <Switch
                    v-model:checked="browserSettings.disableGPU"
                    @change="(checked) => handleBrowserSettingChange('disableGPU', checked)" />
                </div>
                <div class="control-item">
                  <span class="control-label">沙箱模式</span>
                  <Switch
                    v-model:checked="browserSettings.sandboxMode"
                    @change="(checked) => handleBrowserSettingChange('sandboxMode', checked)" />
                </div>
                <div class="control-item">
                  <span class="control-label">启用日志</span>
                  <Switch
                    v-model:checked="browserSettings.enableLogs"
                    @change="(checked) => handleBrowserSettingChange('enableLogs', checked)" />
                </div>
              </div>
            </Card>

            <!-- Environment Variables Panel -->
            <Card class="settings-panel env-panel">
              <template #title>
                <div class="panel-title">环境变量</div>
              </template>
              <div class="config-list">
                <div class="config-item">
                  <span class="config-label">SE_NODE_MAX_SESSIONS:</span>
                  <span class="config-value">{{ environmentVariables.SE_NODE_MAX_SESSIONS }}</span>
                </div>
                <div class="config-item">
                  <span class="config-label">SE_NODE_SESSION_TIMEOUT:</span>
                  <span class="config-value">{{ environmentVariables.SE_NODE_SESSION_TIMEOUT }}</span>
                </div>
                <div class="config-item">
                  <span class="config-label">SE_NODE_OVERRIDE_MAX_SESSIONS:</span>
                  <span class="config-value">{{ environmentVariables.SE_NODE_OVERRIDE_MAX_SESSIONS }}</span>
                </div>
                <div class="config-item">
                  <span class="config-label">SE_NODE_GRID_URL:</span>
                  <span class="config-value">{{ environmentVariables.SE_NODE_GRID_URL }}</span>
                </div>
              </div>
            </Card>
          </div>

          <!-- Advanced Configuration -->
          <Card class="advanced-config-card">
            <template #title>
              <div class="panel-title">
                <Icon icon="icon-shezhi" class="panel-title-icon" />
                <span>高级配置</span>
              </div>
            </template>
            <div class="capability-config-section">
              <div class="capability-title">能力配置</div>
              <div class="capability-json">
                <pre>{{ formattedCapabilityConfig }}</pre>
              </div>
            </div>
          </Card>

          <!-- Browser Control -->
          <Card class="browser-control-card">
            <template #title>
              <div class="panel-title">
                <Icon icon="icon-jinggao" class="panel-title-icon" />
                <span>浏览器控制</span>
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
                  @click="handleRestartBrowser">
                  <Icon icon="icon-shuaxin" class="mr-1" />
                  重启浏览器
                </Button>
                <Button
                  type="default"
                  size="large"
                  danger
                  class="dangerous-btn stop-btn"
                  @click="handleStopBrowser">
                  <Icon icon="icon-guanji" class="mr-1" />
                  停止浏览器
                </Button>
                <Button
                  type="default"
                  size="large"
                  class="dangerous-btn deregister-btn"
                  @click="handleDeregisterInstance">
                  <Icon icon="icon-qingchu" class="mr-1" />
                  注销实例
                </Button>
                <Button
                  type="primary"
                  danger
                  size="large"
                  class="dangerous-btn force-terminate-btn"
                  @click="handleForceTerminateSession">
                  <Icon icon="icon-guanbi" class="mr-1" />
                  强制终止会话
                </Button>
              </div>
              <div class="dangerous-warning">
                <Icon icon="icon-tishi1" class="warning-info-icon" />
                <span class="warning-text">警告:这些操作可能会导致正在执行的测试中断,请谨慎操作!</span>
              </div>
            </div>
          </Card>
        </div>
      </TabPane>
    </Tabs>
  </div>
</template>

<style scoped>
.browser-detail-page {
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

.stop-button {
  background: #fff1f0;
  border-color: #ffccc7;
  color: #ff4d4f;
}

.stop-button:hover {
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

/* Browser Detail Card */
.browser-detail-card {
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  overflow: hidden;
  margin-bottom: 20px;
}

.browser-detail-content {
  display: flex;
  align-items: center;
  padding: 30px;
  gap: 30px;
}

/* Browser Icon */
.browser-icon-wrapper {
  flex-shrink: 0;
}

.browser-icon {
  width: 120px;
  height: 120px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.browser-icon-svg {
  font-size: 64px;
  color: #fff;
}

/* Browser Info Section */
.browser-info-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.browser-name {
  font-size: 28px;
  font-weight: 600;
  color: #262626;
  line-height: 1.2;
}

.browser-details {
  font-size: 14px;
  color: #595959;
  line-height: 1.6;
}

.detail-text {
  margin-right: 8px;
}

.detail-separator {
  margin: 0 8px;
  color: #d9d9d9;
}

.browser-tags {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

/* Browser Actions */
.browser-actions {
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

.create-session-btn {
  background: #1890ff;
  border-color: #1890ff;
  color: #fff;
}

.create-session-btn:hover {
  background: #40a9ff;
  border-color: #40a9ff;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.3);
}

.console-btn {
  background: #f5f5f5;
  border-color: #d9d9d9;
  color: #595959;
}

.console-btn:hover {
  background: #e6e6e6;
  border-color: #bfbfbf;
  color: #262626;
  transform: translateY(-2px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

/* Tabs Styles */
.browser-tabs {
  margin-top: 0;
}

.browser-tabs :deep(.ant-tabs-nav) {
  margin: 0;
  padding: 0 20px;
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
  border-radius: 8px 8px 0 0;
}

.browser-tabs :deep(.ant-tabs-tab) {
  padding: 16px 20px;
  font-size: 14px;
  color: #595959;
}

.browser-tabs :deep(.ant-tabs-tab-active) {
  color: #1890ff;
}

.browser-tabs :deep(.ant-tabs-tab-active .ant-tabs-tab-btn) {
  color: #1890ff;
  font-weight: 500;
}

.browser-tabs :deep(.ant-tabs-ink-bar) {
  background: #1890ff;
}

.browser-tabs :deep(.ant-tabs-content-holder) {
  padding: 0;
  background: #fff;
  border-radius: 0 0 8px 8px;
}

.tab-label {
  display: flex;
  align-items: center;
  gap: 6px;
}

.tab-icon {
  font-size: 14px;
}

.tab-content {
  padding: 24px;
  min-height: 400px;
}

.empty-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 300px;
  color: #8c8c8c;
  font-size: 14px;
}

/* Info Section Styles */
.info-section {
  margin-bottom: 32px;
  background: #fff;
  border-radius: 8px;
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
  display: flex;
  align-items: center;
  gap: 8px;
}

.section-title-icon {
  font-size: 16px;
  color: #1890ff;
}

/* Session Management Styles */
.session-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 16px 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.session-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #262626;
}

.session-title-icon {
  font-size: 18px;
  color: #1890ff;
}

.session-actions {
  display: flex;
  gap: 12px;
}

.create-session-btn {
  background: #1890ff;
  border-color: #1890ff;
  color: #fff;
}

.create-session-btn:hover {
  background: #40a9ff;
  border-color: #40a9ff;
}

.refresh-session-btn {
  background: #f5f5f5;
  border-color: #d9d9d9;
  color: #595959;
}

.refresh-session-btn:hover {
  background: #e6e6e6;
  border-color: #bfbfbf;
  color: #262626;
}

/* Session Cards Grid */
.session-cards-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}

.session-card {
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.3s;
}

.session-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
  transform: translateY(-2px);
}

.session-card-content {
  padding: 20px;
}

.session-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.session-id {
  font-size: 12px;
  color: #8c8c8c;
  font-family: 'Courier New', monospace;
}

.session-task-name {
  font-size: 16px;
  font-weight: 600;
  color: #262626;
  margin-bottom: 16px;
}

.session-details {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 16px;
}

.session-detail-item {
  display: flex;
  align-items: center;
  font-size: 14px;
}

.detail-label {
  color: #8c8c8c;
  min-width: 80px;
  margin-right: 8px;
}

.detail-value {
  color: #262626;
  flex: 1;
}

.session-card-actions {
  display: flex;
  gap: 8px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

.view-btn {
  flex: 1;
  background: #f5f5f5;
  border-color: #d9d9d9;
  color: #595959;
}

.view-btn:hover {
  background: #e6e6e6;
  border-color: #bfbfbf;
  color: #262626;
}

.stop-btn {
  flex: 1;
  background: #fff7e6;
  border-color: #ffd591;
  color: #fa8c16;
}

.stop-btn:hover {
  background: #ffe7ba;
  border-color: #ffc069;
  color: #d46b08;
}

/* Session Statistics */
.session-statistics-section {
  background: #fff;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.statistics-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 32px;
}

.statistics-left,
.statistics-right {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.stat-item:last-child {
  border-bottom: none;
}

.stat-label {
  font-size: 14px;
  color: #595959;
}

.stat-value {
  font-size: 14px;
  font-weight: 600;
  color: #262626;
}

.stat-value.active {
  color: #1890ff;
}

.stat-value.idle {
  color: #8c8c8c;
}

.stat-value.failed {
  color: #ff4d4f;
}

/* Browser Logs Styles */
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
  padding: 16px 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
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
  background: #f5f5f5;
  border-color: #d9d9d9;
  color: #595959;
}

.export-btn:hover {
  background: #e6e6e6;
  border-color: #bfbfbf;
  color: #262626;
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

.logs-container {
  flex: 1;
  background: #1e1e1e;
  border-radius: 8px;
  padding: 20px;
  overflow-y: auto;
  font-family: 'Courier New', 'Consolas', 'Monaco', monospace;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.log-entry-wrapper {
  margin-bottom: 0;
}

.log-entry {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 8px 0;
  color: #d4d4d4;
  font-size: 13px;
  line-height: 1.6;
  word-break: break-all;
}

.log-time {
  color: #858585;
  font-weight: 500;
  min-width: 80px;
  flex-shrink: 0;
}

.log-level {
  font-weight: 600;
  min-width: 60px;
  flex-shrink: 0;
}

.log-message {
  flex: 1;
  color: #d4d4d4;
}

.log-entry.log-info .log-message {
  color: #d4d4d4;
}

.log-entry.log-warn .log-message {
  color: #ffa940;
}

.log-entry.log-error .log-message {
  color: #ff7875;
}

.log-divider {
  height: 1px;
  background: #3e3e3e;
  margin: 4px 0;
}

.logs-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 200px;
  color: #858585;
  font-size: 14px;
}

/* Responsive Design for Sessions */
@media (max-width: 1200px) {
  .session-cards-grid {
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  }
}

@media (max-width: 768px) {
  .session-header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }

  .session-actions {
    width: 100%;
  }

  .session-actions .ant-btn {
    flex: 1;
  }

  .session-cards-grid {
    grid-template-columns: 1fr;
  }

  .statistics-content {
    grid-template-columns: 1fr;
    gap: 24px;
  }

  .logs-header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }

  .logs-actions {
    width: 100%;
    flex-wrap: wrap;
  }

  .log-filter-select {
    flex: 1;
    min-width: 100px;
  }
}

/* Browser Settings Styles */
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
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.settings-panel {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  border-left: 4px solid #1890ff;
}

.selenium-panel {
  border-left-color: #1890ff;
}

.browser-settings-panel {
  border-left-color: #52c41a;
}

.env-panel {
  border-left-color: #722ed1;
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

/* Advanced Configuration */
.advanced-config-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.capability-config-section {
  padding: 8px 0;
}

.capability-title {
  font-size: 14px;
  font-weight: 600;
  color: #262626;
  margin-bottom: 16px;
}

.capability-json {
  background: #1e1e1e;
  border-radius: 8px;
  padding: 20px;
  overflow-x: auto;
}

.capability-json pre {
  margin: 0;
  color: #d4d4d4;
  font-family: 'Courier New', 'Consolas', 'Monaco', monospace;
  font-size: 13px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-wrap: break-word;
}

/* Browser Control */
.browser-control-card {
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
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.warning-icon {
  font-size: 18px;
  color: #ff4d4f;
}

.dangerous-title {
  font-size: 15px;
  font-weight: 600;
  color: #262626;
}

.dangerous-buttons {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 20px;
}

.dangerous-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 48px;
  font-size: 14px;
  font-weight: 500;
  border-radius: 8px;
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
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(250, 140, 22, 0.3);
}

.stop-btn {
  background: #fff1f0;
  border-color: #ffccc7;
  color: #ff4d4f;
}

.stop-btn:hover {
  background: #ffccc7;
  border-color: #ffa39e;
  color: #cf1322;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 77, 79, 0.3);
}

.deregister-btn {
  background: #f5f5f5;
  border-color: #d9d9d9;
  color: #595959;
}

.deregister-btn:hover {
  background: #e6e6e6;
  border-color: #bfbfbf;
  color: #262626;
  transform: translateY(-2px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.force-terminate-btn {
  background: #ff4d4f;
  border-color: #ff4d4f;
  color: #fff;
}

.force-terminate-btn:hover {
  background: #ff7875;
  border-color: #ff7875;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 77, 79, 0.4);
}

.dangerous-warning {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: #fff7e6;
  border-radius: 6px;
  border-left: 4px solid #ffa940;
}

.warning-info-icon {
  font-size: 16px;
  color: #ffa940;
  flex-shrink: 0;
}

.warning-text {
  font-size: 13px;
  color: #d46b08;
  line-height: 1.6;
}

/* Responsive Design for Settings */
@media (max-width: 1200px) {
  .settings-panels {
    grid-template-columns: repeat(2, 1fr);
  }

  .dangerous-buttons {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .settings-panels {
    grid-template-columns: 1fr;
  }

  .dangerous-buttons {
    grid-template-columns: 1fr;
  }
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
  .browser-detail-content {
    flex-direction: column;
    align-items: flex-start;
  }

  .browser-actions {
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


  .browser-detail-content {
    padding: 20px;
  }

  .browser-icon {
    width: 80px;
    height: 80px;
  }

  .browser-icon-svg {
    font-size: 40px;
  }

  .browser-name {
    font-size: 22px;
  }
}
</style>
