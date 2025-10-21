<script lang="ts" setup>
// Vue core imports
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';

// UI component imports
import { Icon } from '@xcan-angus/vue-ui';

// Utility imports
import ApiUtils from '@/utils/apis';

const { t } = useI18n();
const { splitDuration, formatMillisecondToShortDuration } = ApiUtils;

/**
 * Performance indicator configuration interface
 */
interface PerformanceIndicatorConfig {
  duration: string;
  errorRate: string;
  tps: string;
  threads: string;
  percentile: { value: string }; // [ ALL, P50, P75, P90, P95, P99, P999 ]
  rampUpInterval: string;
  rampUpThreads: string;
  art: string;
}

/**
 * Performance test result data interface
 */
interface PerformanceTestResult {
  tranMax: string;
  tranP50: string;
  tranP75: string;
  tranP90: string;
  tranP95: string;
  tranP99: string;
  tranP999: string;
  tps: string;
  art: string;
  errorRate: string;
  rampUpInterval: string;
  rampUpThreads: string;
}

/**
 * Component props interface for performance result display
 */
interface Props {
  indicatorPerf: PerformanceIndicatorConfig;
  result: PerformanceTestResult;
}

// Component props with default values
const props = withDefaults(defineProps<Props>(), {
  indicatorPerf: () => ({
    duration: '',
    errorRate: '',
    tps: '',
    threads: '',
    percentile: { value: '' },
    art: '',
    rampUpInterval: '',
    rampUpThreads: ''
  }),
  result: () => ({
    tranMax: '',
    tranP50: '',
    tranP75: '',
    tranP90: '',
    tranP95: '',
    tranP99: '',
    tranP999: '',
    tps: '',
    art: '',
    errorRate: '',
    rampUpInterval: '',
    rampUpThreads: ''
  })
});

/**
 * Computed property for performance indicator configuration
 */
const performanceIndicatorConfig = computed(() => {
  const percentile = props.indicatorPerf.percentile.value;
  let percentileKey;
  switch (percentile) {
    case 'ALL':
      percentileKey = 'tranMax';
      break;
    case 'P50':
      percentileKey = 'tranP50';
      break;
    case 'P75':
      percentileKey = 'tranP75';
      break;
    case 'P90':
      percentileKey = 'tranP90';
      break;
    case 'P95':
      percentileKey = 'tranP95';
      break;
    case 'P99':
      percentileKey = 'tranP99';
      break;
    case 'P999':
      percentileKey = 'tranP999';
      break;
    default:
      percentileKey = '';
  }
  return [
    {
      label: t('xcan_httpTestInfo.concurrency'),
      dataIndex: 'threadPoolSize'
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
      label: `${t('protocol.responseTime')}(${percentile})`,
      dataIndex: percentileKey || '--'
    },
    {
      label: t('xcan_httpTestInfo.transactionsPerSecond'),
      dataIndex: 'tps'
    },
    {
      label: t('xcan_httpTestInfo.errorRate'),
      dataIndex: 'errorRate',
      unit: '%'
    }
  ];
});

/**
 * Get status icon based on performance indicator comparison
 * @param valueKey - The data key to check
 * @returns Icon configuration object with class and icon name
 */
const getPerformanceStatusIcon = (valueKey: string) => {
  if (valueKey.includes('tran')) {
    if (!props.result[valueKey] && props.indicatorPerf.art) {
      return {
        class: 'text-status-error',
        icon: 'icon-cuowu'
      };
    }
    if (+props.result[valueKey] > +props.indicatorPerf.art) {
      return {
        class: 'text-status-error',
        icon: 'icon-shangjiantou'
      };
    }
    return {
      class: 'text-status-success',
      icon: 'icon-xiajiantou2'
    };
  }
  if (valueKey === 'tps') {
    if (!props.result[valueKey] && props.indicatorPerf.tps) {
      return {
        class: 'text-status-error',
        icon: 'icon-cuowu'
      };
    }
    if (+props.result[valueKey] < +props.indicatorPerf.tps) {
      return {
        class: 'text-status-error',
        icon: 'icon-xiajiantou1'
      };
    }
    return {
      class: 'text-status-success',
      icon: 'icon-shangjiantou1'
    };
  }

  if (valueKey === 'errorRate') {
    if (!props.result[valueKey] && props.indicatorPerf.errorRate) {
      return {
        class: 'text-status-error',
        icon: 'icon-cuowu'
      };
    }
    if (+props.result[valueKey] > +props.indicatorPerf.errorRate) {
      return {
        class: 'text-status-error',
        icon: 'icon-shangjiantou'
      };
    }
    return {
      class: 'text-status-success',
      icon: 'icon-xiajiantou2'
    };
  }
  return {};
};

/**
 * Format duration from milliseconds to readable format
 * @param milliseconds - Duration in milliseconds
 * @returns Formatted duration string
 */
const formatDurationFromMilliseconds = (milliseconds: string) => {
  const [, unit] = splitDuration(props.indicatorPerf.duration);
  if (!milliseconds) {
    return '--';
  }
  return formatMillisecondToShortDuration(+milliseconds, unit as 's' | 'h' | 'min' | 'day');
};

</script>
<template>
  <div class="flex border rounded text-3">
    <div class="flex-1 bg-status-warn flex flex-col p-2">
      <span v-for="indicatorItem in performanceIndicatorConfig" :key="indicatorItem.dataIndex">
        {{ indicatorItem.label }}
      </span>
    </div>
    <div class="flex-1 flex flex-col p-2">
      <div
        v-for="indicatorItem in performanceIndicatorConfig"
        :key="indicatorItem.dataIndex"
        class="flex items-center space-x-2">
        <span>{{ props.result[indicatorItem.dataIndex] ? `${indicatorItem.dataIndex ==='duration' ? formatDurationFromMilliseconds(props.result[indicatorItem.dataIndex]) : props.result[indicatorItem.dataIndex]}${indicatorItem.unit || ''}` : '--' }}</span>
        <Icon v-bind="getPerformanceStatusIcon(indicatorItem.dataIndex)" />
      </div>
    </div>
  </div>
</template>
