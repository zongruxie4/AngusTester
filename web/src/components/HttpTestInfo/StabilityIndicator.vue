<script lang="ts" setup>
// Vue core imports
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

/**
 * Component props interface for stability indicator data
 */
interface Props {
  dataSource: { [key: string]: string };
}

// Component props with default values
const props = withDefaults(defineProps<Props>(), {
  dataSource: () => ({})
});

// Stability indicator configuration
const stabilityIndicatorConfig = [
  {
    label: t('xcan_httpTestInfo.concurrency'),
    dataIndex: 'threads'
  },
  {
    label: t('xcan_httpTestInfo.testDuration'),
    dataIndex: 'duration'
  },
  {
    label: t('xcan_httpTestInfo.responseTime'),
    dataIndex: 'art'
  },
  {
    label: t('xcan_httpTestInfo.transactionsPerSecond'),
    dataIndex: 'tps',
    compareOperator: '>='
  },
  {
    label: t('xcan_httpTestInfo.errorRate'),
    dataIndex: 'errorRate'
  },
  {
    label: t('xcan_httpTestInfo.applicationSystemAverageLoad'),
    dataIndex: 'sys'
  }
];

// System resource usage configuration
const systemResourceUsageConfig = [
  {
    label: t('xcan_httpTestInfo.cpuUsage'),
    dataIndex: 'cpu',
    unit: '%'
  },
  {
    label: t('xcan_httpTestInfo.memoryUsage'),
    dataIndex: 'memory',
    unit: '%'
  },
  {
    label: t('xcan_httpTestInfo.diskUsage'),
    dataIndex: 'disk',
    unit: '%'
  },
  {
    label: t('xcan_httpTestInfo.networkUsage'),
    dataIndex: 'network',
    unit: 'MB'
  }
];

</script>
<template>
  <div class="flex border rounded text-3">
    <div class="flex-1 bg-gray-light flex flex-col p-2">
      <span v-for="indicatorItem in stabilityIndicatorConfig" :key="indicatorItem.dataIndex">
        {{ indicatorItem.label }}
      </span>
    </div>
    <div class="flex-1 flex flex-col p-2">
      <div
        v-for="indicatorItem in stabilityIndicatorConfig"
        :key="indicatorItem.dataIndex"
        class="flex items-center space-x-2">
        <span v-if="indicatorItem.dataIndex === 'art'">{{ (props.dataSource.percentile as any)?.value ? `${(props.dataSource.percentile as any).value} <= ${props.dataSource.art || ''}` : '--' }}</span>
        <div
          v-else-if="indicatorItem.dataIndex === 'sys'"
          class="flex flex-col">
          <span v-for="systemResourceItem in systemResourceUsageConfig" :key="systemResourceItem.dataIndex">
            {{ systemResourceItem.label }} &lt;= {{ props.dataSource[systemResourceItem.dataIndex] ? `${props.dataSource[systemResourceItem.dataIndex]}${systemResourceItem.unit}` : '--' }}
          </span>
        </div>
        <div v-else-if="indicatorItem.dataIndex === 'errorRate'">
          &lt;= {{ props.dataSource[indicatorItem.dataIndex] }} %
        </div>
        <span v-else>{{ (indicatorItem.compareOperator || '') + (props.dataSource[indicatorItem.dataIndex] || '') || '--' }}</span>
      </div>
    </div>
  </div>
</template>
