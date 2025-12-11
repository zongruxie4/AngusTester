<script setup lang="ts">
import { ref, inject } from 'vue';
import { Table, Icon, Card, SearchPanel } from '@xcan-angus/vue-ui';
import { Button, Tag, Dropdown, Menu } from 'ant-design-vue';
import type { VersionListProps } from '../mobileDevice/types';

// Component props with default values
const props = withDefaults(defineProps<VersionListProps>(), {
});

const addTabPane = inject<(data: any) => void>('addTabPane', () => {});

// Browser Statistics Data
const browserStats = ref({
  total: {
    value: 18,
    trend: 3,
    trendType: 'increase' as 'increase' | 'decrease',
    period: '较上月'
  },
  online: {
    value: 15,
    trend: 2,
    trendType: 'increase' as 'increase' | 'decrease',
    period: '较昨日'
  },
  offline: {
    value: 2,
    trend: 1,
    trendType: 'decrease' as 'increase' | 'decrease',
    period: '较昨日'
  },
  activeSessions: {
    value: 24,
    maxConcurrency: 36
  }
});

// Browser Type Distribution
const browserTypes = ref([
  {
    name: 'Chrome',
    count: 8,
    percentage: 44.4,
    icon: 'icon-chrome',
    color: '#4285f4'
  },
  {
    name: 'Firefox',
    count: 5,
    percentage: 27.8,
    icon: 'icon-firefox',
    color: '#ff7139'
  },
  {
    name: 'Safari',
    count: 3,
    percentage: 16.7,
    icon: 'icon-safari',
    color: '#000000'
  },
  {
    name: 'Edge',
    count: 2,
    percentage: 11.1,
    icon: 'icon-edge',
    color: '#0078d4'
  }
]);

// Filter states (for future use)
// const statusFilter = ref('all');
// const browserFilter = ref('all');
// const systemFilter = ref('all');

// Filter options
const statusOptions = [
  { label: '全部状态', value: 'all' },
  { label: '在线', value: 'online' },
  { label: '离线', value: 'offline' },
  { label: '繁忙', value: 'busy' },
  { label: '异常', value: 'abnormal' }
];

const browserOptions = [
  { label: '全部浏览器', value: 'all' },
  { label: 'Chrome', value: 'chrome' },
  { label: 'Firefox', value: 'firefox' },
  { label: 'Safari', value: 'safari' },
  { label: 'Edge', value: 'edge' }
];

const systemOptions = [
  { label: '全部系统', value: 'all' },
  { label: 'Windows', value: 'windows' },
  { label: 'Linux', value: 'linux' },
  { label: 'macOS', value: 'macos' }
];

// Browser Instance List Data
const dataList = ref([
  {
    key: '1',
    instanceName: 'Chrome实例-01',
    instanceId: 'chrome-instance-01',
    status: 'online',
    browserType: 'chrome',
    version: '118.0.5993.88',
    os: 'windows',
    hostAddress: '192.168.1.101:5555',
    sessionCount: 3,
    maxSessions: 5,
    lastActivity: '10秒前'
  },
  {
    key: '2',
    instanceName: 'Firefox实例-01',
    instanceId: 'firefox-instance-01',
    status: 'busy',
    browserType: 'firefox',
    version: '119.0',
    os: 'linux',
    hostAddress: '192.168.1.102:5555',
    sessionCount: 4,
    maxSessions: 4,
    lastActivity: '正在测试'
  },
  {
    key: '3',
    instanceName: 'Safari实例-01',
    instanceId: 'safari-instance-01',
    status: 'online',
    browserType: 'safari',
    version: '17.0',
    os: 'macos',
    hostAddress: '192.168.1.103:5555',
    sessionCount: 2,
    maxSessions: 3,
    lastActivity: '5分钟前'
  },
  {
    key: '4',
    instanceName: 'Edge实例-01',
    instanceId: 'edge-instance-01',
    status: 'offline',
    browserType: 'edge',
    version: '118.0.2088.76',
    os: 'windows',
    hostAddress: '192.168.1.104:5555',
    sessionCount: 0,
    maxSessions: 5,
    lastActivity: '1小时前'
  },
  {
    key: '5',
    instanceName: 'Chrome实例-02',
    instanceId: 'chrome-instance-02',
    status: 'online',
    browserType: 'chrome',
    version: '118.0.5993.88',
    os: 'linux',
    hostAddress: '192.168.1.105:5555',
    sessionCount: 1,
    maxSessions: 5,
    lastActivity: '2分钟前'
  },
  {
    key: '6',
    instanceName: 'Firefox实例-02',
    instanceId: 'firefox-instance-02',
    status: 'online',
    browserType: 'firefox',
    version: '119.0',
    os: 'windows',
    hostAddress: '192.168.1.106:5555',
    sessionCount: 2,
    maxSessions: 4,
    lastActivity: '15分钟前'
  },
  {
    key: '7',
    instanceName: 'Safari实例-02',
    instanceId: 'safari-instance-02',
    status: 'busy',
    browserType: 'safari',
    version: '17.0',
    os: 'macos',
    hostAddress: '192.168.1.107:5555',
    sessionCount: 3,
    maxSessions: 3,
    lastActivity: '正在测试'
  },
  {
    key: '8',
    instanceName: 'Edge实例-02',
    instanceId: 'edge-instance-02',
    status: 'online',
    browserType: 'edge',
    version: '118.0.2088.76',
    os: 'windows',
    hostAddress: '192.168.1.108:5555',
    sessionCount: 1,
    maxSessions: 5,
    lastActivity: '30分钟前'
  },
  {
    key: '9',
    instanceName: 'Chrome实例-03',
    instanceId: 'chrome-instance-03',
    status: 'online',
    browserType: 'chrome',
    version: '118.0.5993.88',
    os: 'windows',
    hostAddress: '192.168.1.109:5555',
    sessionCount: 4,
    maxSessions: 5,
    lastActivity: '1小时前'
  },
  {
    key: '10',
    instanceName: 'Firefox实例-03',
    instanceId: 'firefox-instance-03',
    status: 'offline',
    browserType: 'firefox',
    version: '119.0',
    os: 'linux',
    hostAddress: '192.168.1.110:5555',
    sessionCount: 0,
    maxSessions: 4,
    lastActivity: '2小时前'
  }
]);

const pagination = ref<any>({
  pageSize: 10,
  current: 1,
  total: 10
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

// Table columns
const columns = [
  {
    title: '浏览器实例',
    dataIndex: 'instanceName',
    key: 'instanceName',
    width: 200
  },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
    width: 100
  },
  {
    title: '浏览器类型',
    dataIndex: 'browserType',
    key: 'browserType',
    width: 150
  },
  {
    title: '版本',
    dataIndex: 'version',
    key: 'version',
    width: 150
  },
  {
    title: '操作系统',
    dataIndex: 'os',
    key: 'os',
    width: 120
  },
  {
    title: '主机地址',
    dataIndex: 'hostAddress',
    key: 'hostAddress',
    width: 180
  },
  {
    title: '会话数',
    dataIndex: 'sessionCount',
    key: 'sessionCount',
    width: 100
  },
  {
    title: '最后活动',
    dataIndex: 'lastActivity',
    key: 'lastActivity',
    width: 150
  },
  {
    title: '操作',
    key: 'action',
    width: 120,
    fixed: 'right' as const
  }
];

// Handle browser actions
const handleView = (record: any) => {
  console.log('View browser:', record);
};

const handleRefresh = (record?: any) => {
  if (record) {
    console.log('Refresh browser:', record);
  } else {
    console.log('Refresh list');
  }
};

const handleMore = (record: any, key: string) => {
  console.log('More action:', key, record);
};

const viewDetail = ({ id, instanceName }: { id: string; instanceName: string }) => {
  addTabPane({
    value: 'webDetails',
    name: instanceName,
    _id: id + 'detail',
    data: { id },
    noCache: true
  });
};

// More actions menu
const moreMenuItems = [
  { key: 'edit', label: '编辑' },
  { key: 'delete', label: '删除' },
  { key: 'logs', label: '查看日志' }
];

const searchPanels = [
  {
    type: 'input' as const,
    valueKey: 'searchKeyWords',
    placeholder: '搜索关键词'
  },
  {
    type: 'select' as const,
    valueKey: 'status',
    options: statusOptions,
    placeholder: '状态'
  },
  {
    type: 'select' as const,
    valueKey: 'browserType',
    options: browserOptions,
    placeholder: '浏览器'
  },
  {
    type: 'select' as const,
    valueKey: 'os',
    options: systemOptions,
    placeholder: '系统'
  }
];
</script>

<template>
  <div class="flex flex-col h-full overflow-auto px-5 py-5 leading-5 text-3">
    <!-- Browser Statistics Cards - First Row -->
    <div class="stats-cards-grid mb-4">
      <Card class="stat-card total-card">
        <div class="stat-card-content">
          <div class="stat-label">总浏览器实例</div>
          <div class="stat-value">{{ browserStats.total.value }}</div>
          <div class="stat-trend increase">
            <Icon icon="icon-shangjiantou1" class="trend-icon" />
            {{ browserStats.total.trend }} {{ browserStats.total.period }}
          </div>
        </div>
      </Card>

      <Card class="stat-card online-card">
        <div class="stat-card-content">
          <div class="stat-label">在线浏览器</div>
          <div class="stat-value">{{ browserStats.online.value }}</div>
          <div class="stat-trend increase">
            <Icon icon="icon-shangjiantou1" class="trend-icon" />
            {{ browserStats.online.trend }} {{ browserStats.online.period }}
          </div>
        </div>
      </Card>

      <Card class="stat-card offline-card">
        <div class="stat-card-content">
          <div class="stat-label">离线浏览器</div>
          <div class="stat-value">{{ browserStats.offline.value }}</div>
          <div class="stat-trend decrease">
            <Icon icon="icon-xiajiantou1" class="trend-icon" />
            {{ browserStats.offline.trend }} {{ browserStats.offline.period }}
          </div>
        </div>
      </Card>

      <Card class="stat-card sessions-card">
        <div class="stat-card-content">
          <div class="stat-label">活动会话</div>
          <div class="stat-value">{{ browserStats.activeSessions.value }}</div>
          <div class="stat-description">
            最大并发: {{ browserStats.activeSessions.maxConcurrency }}
          </div>
        </div>
      </Card>
    </div>

    <!-- Browser Type Distribution Cards - Second Row -->
    <div class="stats-cards-grid mb-6">
      <Card
        v-for="browser in browserTypes"
        :key="browser.name"
        class="stat-card browser-type-card">
        <div class="stat-card-content">
          <div class="browser-type-header">
            <div class="browser-icon-wrapper" :style="{ backgroundColor: browser.color + '20', borderColor: browser.color + '40' }">
              <Icon :icon="browser.icon" class="browser-icon" :style="{ color: browser.color }" />
            </div>
            <div class="browser-info">
              <div class="browser-name">{{ browser.name }}</div>
              <div class="browser-count">{{ browser.count }}</div>
            </div>
          </div>
          <div class="browser-percentage">{{ browser.percentage }}%</div>
        </div>
      </Card>
    </div>

    <!-- Browser Instance List Section -->
    <div class="browser-list-section">
      <div class="text-3.5 font-semibold mb-4">浏览器实例列表</div>
      <div class="flex justify-between">
        <SearchPanel
          class="mb-2"
          :options="searchPanels" />
        <Button
          type="primary"
          size="small"
          @click="handleRefresh">
          <Icon icon="icon-shuaxin" class="mr-1" />
          刷新
        </Button>
      </div>

      <Table
        :columns="columns"
        :dataSource="dataList"
        :pagination="pagination"
        noDataSize="small"
        noDataText="暂无浏览器实例数据"
        class="browser-table">
        <template #bodyCell="{ column, record, text }">
          <template v-if="column.dataIndex === 'instanceName'">
            <div class="instance-name-cell">
              <div class="instance-name-main">
                <Button
                  size="small"
                  type="link"
                  @click="viewDetail({ id: record.key, instanceName: record.instanceName })">
                  {{ record.instanceName }}
                </Button>
              </div>
              <div class="instance-name-id pl-2">{{ record.instanceId }}</div>
            </div>
          </template>

          <template v-else-if="column.dataIndex === 'status'">
            <Tag
              :style="{ backgroundColor: getStatusConfig(record.status).bgColor }">
              {{ getStatusConfig(record.status).text }}
            </Tag>
          </template>

          <template v-else-if="column.dataIndex === 'browserType'">
            <div class="browser-type-cell">
              <Icon
                :icon="getBrowserTypeConfig(record.browserType).icon"
                class="browser-type-icon"
                :style="{ color: getBrowserTypeConfig(record.browserType).color }" />
              <span>{{ getBrowserTypeConfig(record.browserType).text }}</span>
            </div>
          </template>

          <template v-else-if="column.dataIndex === 'os'">
            <Tag
              :style="{ backgroundColor: getOSConfig(record.os).bgColor }">
              {{ getOSConfig(record.os).text }}
            </Tag>
          </template>

          <template v-else-if="column.dataIndex === 'sessionCount'">
            <span
              :class="{
                'session-full': record.sessionCount >= record.maxSessions,
                'session-available': record.sessionCount < record.maxSessions
              }">
              {{ record.sessionCount }}/{{ record.maxSessions }}
            </span>
          </template>

          <template v-else-if="column.key === 'action'">
            <div class="action-buttons">
              <Button
                type="text"
                size="small"
                class="action-btn"
                @click="handleView(record)">
                <Icon icon="icon-chakan" />
              </Button>
              <Button
                type="text"
                size="small"
                class="action-btn"
                @click="handleRefresh(record)">
                <Icon icon="icon-shuaxin" />
              </Button>
              <Dropdown>
                <template #overlay>
                  <Menu @click="(info) => handleMore(record, String(info.key))">
                    <Menu.Item
                      v-for="item in moreMenuItems"
                      :key="item.key">
                      {{ item.label }}
                    </Menu.Item>
                  </Menu>
                </template>
                <Button
                  type="text"
                  size="small"
                  class="action-btn">
                  <Icon icon="icon-gengduo" />
                </Button>
              </Dropdown>
            </div>
          </template>

          <template v-else>
            {{ text }}
          </template>
        </template>
      </Table>
    </div>
  </div>
</template>

<style scoped>
/* Statistics Cards */
.stats-cards-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.stat-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.3s;
  border: 1px solid #f0f0f0;
}

.stat-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
  transform: translateY(-2px);
}

.total-card {
  border-left: 4px solid #1890ff;
}

.online-card {
  border-left: 4px solid #52c41a;
}

.offline-card {
  border-left: 4px solid #8c8c8c;
}

.sessions-card {
  border-left: 4px solid #722ed1;
}

.stat-card-content {
  padding: 20px;
}

.stat-label {
  font-size: 14px;
  color: #8c8c8c;
  margin-bottom: 12px;
  font-weight: 500;
}

.stat-value {
  font-size: 32px;
  font-weight: 600;
  color: #262626;
  margin-bottom: 12px;
  line-height: 1.2;
}

.stat-trend {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 500;
}

.stat-trend.increase {
  color: #52c41a;
}

.stat-trend.decrease {
  color: #ff4d4f;
}

.trend-icon {
  font-size: 14px;
}

.stat-description {
  font-size: 13px;
  color: #8c8c8c;
}

/* Browser Type Cards */
.browser-type-card {
  border-left: 4px solid transparent;
}

.browser-type-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.browser-icon-wrapper {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid;
}

.browser-icon {
  font-size: 24px;
}

.browser-info {
  flex: 1;
}

.browser-name {
  font-size: 16px;
  font-weight: 600;
  color: #262626;
  margin-bottom: 4px;
}

.browser-count {
  font-size: 24px;
  font-weight: 600;
  color: #262626;
}

.browser-percentage {
  font-size: 14px;
  color: #8c8c8c;
  text-align: right;
}

/* Browser List Section */
.browser-list-section {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.browser-table {
  flex: 1;
}

.instance-name-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.instance-name-main {
  font-size: 14px;
  font-weight: 500;
}

.instance-name-id {
  font-size: 12px;
  color: #8c8c8c;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', 'Consolas', monospace;
}

.browser-type-cell {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.browser-type-icon {
  font-size: 18px;
}

.session-full {
  color: #ff4d4f;
  font-weight: 600;
}

.session-available {
  color: #52c41a;
  font-weight: 500;
}

.action-buttons {
  display: flex;
  align-items: center;
  gap: 4px;
}

.action-btn {
  padding: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border-radius: 4px;
  transition: all 0.2s;
}

.action-btn:hover {
  background-color: #f0f0f0;
}

/* Card Styles */
:deep(.ant-card) {
  background: #fff;
}

:deep(.ant-card-body) {
  padding: 0;
}

/* Table Styles */
:deep(.ant-table-thead > tr > th) {
  background: #fafafa;
  font-weight: 600;
  color: #262626;
  border-bottom: 2px solid #f0f0f0;
}

:deep(.ant-table-tbody > tr > td) {
  border-bottom: 1px solid #f0f0f0;
}

:deep(.ant-table-tbody > tr:hover > td) {
  background: #fafafa;
}

/* Responsive Design */
@media (max-width: 1200px) {
  .stats-cards-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .stats-cards-grid {
    grid-template-columns: 1fr;
  }
}
</style>
