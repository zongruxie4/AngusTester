<script setup lang="ts">
import { ref } from 'vue';
import { Table, Icon, Select, Card } from '@xcan-angus/vue-ui';
import { Button, Tag, Dropdown, Menu } from 'ant-design-vue';
import type { VersionListProps } from './types';

// Component props with default values
const props = withDefaults(defineProps<VersionListProps>(), {
});

// Device Statistics Data
const deviceStats = ref({
  total: {
    value: 15,
    trend: 2,
    trendType: 'increase' as 'increase' | 'decrease',
    period: '较上月'
  },
  online: {
    value: 12,
    trend: 3,
    trendType: 'increase' as 'increase' | 'decrease',
    period: '较昨日'
  },
  offline: {
    value: 2,
    trend: 1,
    trendType: 'decrease' as 'increase' | 'decrease',
    period: '较昨日'
  },
  busy: {
    value: 4,
    description: '正在执行测试'
  }
});

// Filter states
const statusFilter = ref('all');
const typeFilter = ref('all');
const nodeFilter = ref('all');

// Filter options
const statusOptions = [
  { label: '全部状态', value: 'all' },
  { label: '在线', value: 'online' },
  { label: '离线', value: 'offline' },
  { label: '繁忙', value: 'busy' },
  { label: '异常', value: 'abnormal' }
];

const typeOptions = [
  { label: '全部类型', value: 'all' },
  { label: 'Android', value: 'android' },
  { label: 'iOS', value: 'ios' },
  { label: 'HarmonyOS', value: 'harmonyos' }
];

const nodeOptions = [
  { label: '全部节点', value: 'all' },
  { label: '节点1', value: 'node1' },
  { label: '节点2', value: 'node2' },
  { label: '节点3', value: 'node3' }
];

// Device List Data
const dataList = ref([
  {
    key: '1',
    deviceName: 'Samsung Galaxy S23',
    deviceId: 'R58M40MMWFK...',
    status: 'online',
    type: 'android',
    systemVersion: 'Android 13.0',
    resolution: '1080x2400',
    node: '节点1',
    lastActivity: '2分钟前'
  },
  {
    key: '2',
    deviceName: 'iPhone 15 Pro',
    deviceId: '00008101-001...',
    status: 'busy',
    type: 'ios',
    systemVersion: 'iOS 17.0',
    resolution: '1179x2556',
    node: '节点2',
    lastActivity: '正在测试'
  },
  {
    key: '3',
    deviceName: 'Xiaomi 13 Pro',
    deviceId: '7f8e3a9b4c2d...',
    status: 'online',
    type: 'android',
    systemVersion: 'Android 12.0',
    resolution: '1440x3200',
    node: '节点1',
    lastActivity: '5分钟前'
  },
  {
    key: '4',
    deviceName: 'Google Pixel 7',
    deviceId: '0a1b2c3d4e5f...',
    status: 'offline',
    type: 'android',
    systemVersion: 'Android 14.0',
    resolution: '1080x2400',
    node: '节点3',
    lastActivity: '1小时前'
  },
  {
    key: '5',
    deviceName: 'Huawei P50 Pro',
    deviceId: '8g7h6f5e4d3c...',
    status: 'abnormal',
    type: 'android',
    systemVersion: 'HarmonyOS 3.0',
    resolution: '1228x2700',
    node: '节点2',
    lastActivity: '连接异常'
  }
]);

const pagination = ref<any>({
  pageSize: 10,
  current: 1,
  total: 5
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

// Table columns
const columns = [
  {
    title: '设备名称',
    dataIndex: 'deviceName',
    key: 'deviceName',
    width: 200,
    customRender: ({ record }: { record: any }) => {
      return `${record.deviceName} (${record.deviceId})`;
    }
  },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
    width: 100
  },
  {
    title: '类型',
    dataIndex: 'type',
    key: 'type',
    width: 120
  },
  {
    title: '系统版本',
    dataIndex: 'systemVersion',
    key: 'systemVersion',
    width: 150
  },
  {
    title: '分辨率',
    dataIndex: 'resolution',
    key: 'resolution',
    width: 120
  },
  {
    title: '所在节点',
    dataIndex: 'node',
    key: 'node',
    width: 120
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

// Handle device actions
const handleView = (record: any) => {
  console.log('View device:', record);
};

const handleRefresh = (record: any) => {
  console.log('Refresh device:', record);
};

const handleMore = (record: any, key: string) => {
  console.log('More action:', key, record);
};

// More actions menu
const moreMenuItems = [
  { key: 'edit', label: '编辑' },
  { key: 'delete', label: '删除' },
  { key: 'logs', label: '查看日志' }
];

</script>

<template>
  <div class="flex flex-col h-full overflow-auto px-5 py-5 leading-5 text-3">
    <!-- Device Statistics Cards -->
    <div class="stats-cards-grid mb-6">
      <Card class="stat-card total-card">
        <div class="stat-card-content">
          <div class="stat-label">总设备数</div>
          <div class="stat-value">{{ deviceStats.total.value }}</div>
          <div class="stat-trend increase">
            <Icon icon="icon-shangjiantou1" class="trend-icon" />
             {{ deviceStats.total.trend }} {{ deviceStats.total.period }}
          </div>
        </div>
      </Card>

      <Card class="stat-card online-card">
        <div class="stat-card-content">
          <div class="stat-label">在线设备</div>
          <div class="stat-value">{{ deviceStats.online.value }}</div>
          <div class="stat-trend increase">
            <Icon icon="icon-shangjiantou1" class="trend-icon" />
            {{ deviceStats.online.trend }} {{ deviceStats.online.period }}
          </div>
        </div>
      </Card>

      <Card class="stat-card offline-card">
        <div class="stat-card-content">
          <div class="stat-label">离线设备</div>
          <div class="stat-value">{{ deviceStats.offline.value }}</div>
          <div class="stat-trend decrease">
            <Icon icon="icon-xiajiantou1" class="trend-icon" />
             {{ deviceStats.offline.trend }} {{ deviceStats.offline.period }}
          </div>
        </div>
      </Card>

      <Card class="stat-card busy-card">
        <div class="stat-card-content">
          <div class="stat-label">繁忙设备</div>
          <div class="stat-value">{{ deviceStats.busy.value }}</div>
          <div class="stat-description">{{ deviceStats.busy.description }}</div>
        </div>
      </Card>
    </div>

    <!-- Device List Section -->
    <div class="device-list-section">
      <div class="text-3.5 font-semibold mb-4">设备列表</div>
      <div class="flex">
        <Select
          v-model:value="statusFilter"
          :options="statusOptions"
          class="filter-select"
          style="width: 120px" />
        <Select
          v-model:value="typeFilter"
          :options="typeOptions"
          class="filter-select"
          style="width: 120px" />
        <Select
          v-model:value="nodeFilter"
          :options="nodeOptions"
          class="filter-select"
          style="width: 120px" />
      </div>

      <Table
        :columns="columns"
        :dataSource="dataList"
        :pagination="pagination"
        noDataSize="small"
        noDataText="暂无设备数据"
        class="device-table">
        <template #bodyCell="{ column, record, text }">
          <template v-if="column.dataIndex === 'deviceName'">
            <div class="device-name-cell">
              <div class="device-name-main">{{ record.deviceName }}</div>
              <div class="device-name-id">{{ record.deviceId }}</div>
            </div>
          </template>

          <template v-else-if="column.dataIndex === 'status'">
            <Tag
              :style="{ backgroundColor: getStatusConfig(record.status).bgColor }">
              {{ getStatusConfig(record.status).text }}
            </Tag>
          </template>

          <template v-else-if="column.dataIndex === 'type'">
            <Tag
              :style="{ backgroundColor: getTypeConfig(record.type).bgColor }">
              {{ getTypeConfig(record.type).text }}
            </Tag>
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

.busy-card {
  border-left: 4px solid #fa8c16;
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

/* Device List Section */
.device-list-section {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.device-list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.filter-group {
  display: flex;
  gap: 12px;
}

.filter-select {
  min-width: 120px;
}

/* Device Table */
.device-table {
  flex: 1;
}

.device-name-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.device-name-main {
  font-size: 14px;
  font-weight: 500;
  color: #262626;
}

.device-name-id {
  font-size: 12px;
  color: #8c8c8c;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', 'Consolas', monospace;
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

  .device-list-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .filter-group {
    width: 100%;
    flex-wrap: wrap;
  }
}
</style>
