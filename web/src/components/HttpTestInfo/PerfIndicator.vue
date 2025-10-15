<script lang="ts" setup>
// Vue core imports
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

/**
 * Component props interface for performance indicator data
 */
interface Props {
  dataSource: { [key: string]: string };
}

// Component props with default values
const props = withDefaults(defineProps<Props>(), {
  dataSource: () => ({})
});

// Performance indicator configuration
const performanceIndicatorConfig = [
  {
    label: t('xcan_httpTestInfo.concurrency'),
    dataIndex: 'threads'
  },
  {
    label: t('xcan_httpTestInfo.testDuration'),
    dataIndex: 'duration'
  },
  {
    label: t('xcan_httpTestInfo.rampUpConcurrency'),
    dataIndex: 'rampUpThreads'
  },
  {
    label: t('xcan_httpTestInfo.rampUpDuration'),
    dataIndex: 'rampUpInterval'
  },
  {
    label: t('protocol.responseTime'),
    dataIndex: 'art',
    compareOperator: '<='
  },
  {
    label: t('xcan_httpTestInfo.transactionsPerSecond'),
    dataIndex: 'tps',
    compareOperator: '>='
  },
  {
    label: t('xcan_httpTestInfo.errorRate'),
    dataIndex: 'errorRate',
    compareOperator: '<=',
    unit: '%'
  }
];

</script>
<template>
  <div class="flex border rounded text-3">
    <div class="flex-1 bg-gray-light flex flex-col p-2">
      <span v-for="indicatorItem in performanceIndicatorConfig" :key="indicatorItem.dataIndex">
        {{ indicatorItem.label }}
      </span>
    </div>
    <div class="flex-1 flex flex-col p-2">
      <div
        v-for="indicatorItem in performanceIndicatorConfig"
        :key="indicatorItem.dataIndex"
        class="flex items-center space-x-2">
        <span v-if="indicatorItem.dataIndex === 'art'">{{ (props.dataSource.percentile as any)?.value ? `${(props.dataSource.percentile as any).value} <= ${props.dataSource.art}` : '--' }}</span>
        <span v-else>
          {{ props.dataSource[indicatorItem.dataIndex] ? `${indicatorItem.compareOperator || ''} ${props.dataSource[indicatorItem.dataIndex]}${indicatorItem.unit || ''}` : '--' }}
        </span>
      </div>
    </div>
  </div>
</template>
