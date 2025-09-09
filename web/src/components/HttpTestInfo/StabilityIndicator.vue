<script lang="ts" setup>
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

interface Props {
  dataSource: {[key: string]: string}
}

const props = withDefaults(defineProps<Props>(), {
  dataSource: () => ({})
});

const indicatorItem = [
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
    compareOprate: '>='
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

const sysItems = [
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
      <span v-for="item in indicatorItem" :key="item.dataIndex">
        {{ item.label }}
      </span>
    </div>
    <div class="flex-1 flex flex-col p-2">
      <div
        v-for="item in indicatorItem"
        :key="item.dataIndex"
        class="flex items-center space-x-2">
        <span v-if="item.dataIndex === 'art'">{{ props.dataSource.percentile?.value ? `${props.dataSource.percentile.value} <= ${props.dataSource.art || ''}` : '--' }}</span>
        <div
          v-else-if="item.dataIndex === 'sys'"
          class="flex flex-col">
          <span v-for="sysItem in sysItems" :key="sysItem.dataIndex">
            {{ sysItem.label }} &lt;= {{ props.dataSource[sysItem.dataIndex] ? `${props.dataSource[sysItem.dataIndex]}${sysItem.unit}` : '--' }}
          </span>
        </div>
        <div v-else-if="item.dataIndex === 'errorRate'">
          &lt;= {{ props.dataSource[item.dataIndex] }} %
        </div>
        <span v-else>{{ (item.compareOprate || '') + (props.dataSource[item.dataIndex] || '') || '--' }}</span>
      </div>
    </div>
  </div>
</template>
