<script setup lang="ts">
// Vue core imports
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';

// UI component imports
import { Icon } from '@xcan-angus/vue-ui';

// Utility imports
import ApiUtils from 'src/utils/apis';

const { t } = useI18n();
const { formatMillisecondToShortDuraiton, splitDuration } = ApiUtils;

/**
 * Stability indicator configuration interface
 */
interface StabilityIndicatorConfig {
  duration: string;
  errorRate: string;
  threads: string;
  tps: string;
  percentile: { value: string };
  art: string;
  cpu: string;
  disk: string;
  memory: string;
  network: string;
}

/**
 * Stability test result data interface
 */
interface StabilityTestResult {
  art: string;
  cpu: string;
  disk: string;
  duration: string;
  errorRate: string;
  threads: string;
  tps: string;
  percentile: string;
  memory: string;
  network: string;
}

/**
 * Component props interface for stability result display
 */
interface Props {
  indicatorStability: StabilityIndicatorConfig;
  result: StabilityTestResult;
}

// Component props with default values
const props = withDefaults(defineProps<Props>(), {
  indicatorStability: () => ({
    percentile: { value: '' },
    duration: '',
    errorRate: '',
    threads: '',
    tps: '',
    art: '',
    cpu: '',
    disk: '',
    memory: '',
    network: ''
  }),
  result: () => ({
    percentile: '',
    duration: '',
    errorRate: '',
    threads: '',
    tps: '',
    art: '',
    cpu: '',
    disk: '',
    memory: '',
    network: ''
  })
});

/**
 * Computed property for stability indicator configuration
 */
const stabilityIndicatorConfig = computed(() => {
  const percentile = props.indicatorStability.percentile.value;
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
      label: `${t('xcan_httpTestInfo.responseTime')}(${percentile})`,
      dataIndex: percentileKey || '--'
    },
    {
      label: t('xcan_httpTestInfo.transactionsPerSecond'),
      dataIndex: 'tps'
    },
    {
      label: t('xcan_httpTestInfo.errorRate'),
      dataIndex: 'errorRate'
    }
  ];
});

// System resource average usage configuration
const systemResourceAverageConfig = [
  {
    label: t('xcan_httpTestInfo.cpuUsage'),
    dataIndex: 'meanCpu',
    unit: '%'
  },
  {
    label: t('xcan_httpTestInfo.memoryUsage'),
    dataIndex: 'meanMemory',
    unit: '%'
  },
  {
    label: t('xcan_httpTestInfo.diskUsage'),
    dataIndex: 'meanFilesystem',
    unit: '%'
  },
  {
    label: t('xcan_httpTestInfo.networkUsage'),
    dataIndex: 'meanNetwork',
    unit: 'MB'
  }
];

// System resource maximum usage configuration
const systemResourceMaximumConfig = [
  {
    label: t('xcan_httpTestInfo.averageCpu'),
    dataIndex: 'maxCpu',
    unit: '%'
  },
  {
    label: t('xcan_httpTestInfo.averageMemory'),
    dataIndex: 'maxMemory',
    unit: '%'
  },
  {
    label: t('xcan_httpTestInfo.averageDisk'),
    dataIndex: 'maxFilesystem',
    unit: '%'
  },
  {
    label: t('xcan_httpTestInfo.averageNetwork'),
    dataIndex: 'maxNetwork',
    unit: 'MB'
  }
];

/**
 * Get status icon based on stability indicator comparison
 * @param valueKey - The data key to check
 * @returns Icon configuration object with icon name
 */
const getStabilityStatusIcon = (valueKey: string) => {
  if (valueKey.includes('tran')) {
    if (!props.result[valueKey] && props.indicatorStability.art) {
      return {
        icon: 'icon-cuowu'
      };
    }
    if (+props.result[valueKey] > +props.indicatorStability.art) {
      return {
        icon: 'icon-shangjiantou'
      };
    }
    return {
      icon: 'icon-xiajiantou2'
    };
  }
  if (valueKey === 'tps') {
    if (!props.result[valueKey] && props.indicatorStability.tps) {
      return {
        icon: 'icon-cuowu'
      };
    }
    if (+props.result[valueKey] < +props.indicatorStability.tps) {
      return {
        icon: 'icon-xiajiantou1'
      };
    }
    return {
      icon: 'icon-shangjiantou1'
    };
  }

  if (valueKey === 'errorRate') {
    if (!props.result[valueKey] && props.indicatorStability.errorRate) {
      return {
        icon: 'icon-cuowu'
      };
    }
    if (+props.result[valueKey] > +props.indicatorStability.errorRate) {
      return {
        icon: 'icon-shangjiantou'
      };
    }
    return {
      icon: 'icon-xiajiantou2'
    };
  }
  if (['meanCpu', 'meanMemory', 'meanFilesystem', 'meanNetwork'].includes(valueKey)) {
    let key = valueKey.replace('mean', '').toLowerCase();
    if (valueKey === 'meanFilesystem') {
      key = 'disk';
    }
    if (!props.result[valueKey] && props.indicatorStability[key]) {
      return {
        icon: 'icon-cuowu'
      };
    }
    if (+props.result[valueKey] > +props.indicatorStability[key]) {
      return {
        icon: 'icon-shangjiantou'
      };
    }
    if (!props.indicatorStability[key]) {
      return {};
    }
    return {
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
  const [, unit] = splitDuration(props.indicatorStability.duration);
  if (!milliseconds) {
    return '--';
  }
  return formatMillisecondToShortDuraiton(+milliseconds, unit as 's' | 'h' | 'min' | 'day');
};

</script>
<template>
  <div class="flex border rounded text-3">
    <div class="flex-1 bg-status-warn flex flex-col p-2">
      <span v-for="indicatorItem in stabilityIndicatorConfig" :key="indicatorItem.dataIndex">
        {{ indicatorItem.label }}
      </span>
    </div>
    <div class="flex-1 flex flex-col p-2">
      <div
        v-for="indicatorItem in stabilityIndicatorConfig"
        :key="indicatorItem.dataIndex"
        class="flex items-center space-x-2">
        <span>{{ indicatorItem.dataIndex === 'duration' ? formatDurationFromMilliseconds(props.result[indicatorItem.dataIndex]) : props.result[indicatorItem.dataIndex] || '--' }}</span>
        <Icon v-bind="getStabilityStatusIcon(indicatorItem.dataIndex)" />
      </div>
    </div>
  </div>
  <div class="flex border rounded text-3 mt-2">
    <div class="flex-1 bg-status-warn flex flex-col p-2">
      <span v-for="systemResourceItem in systemResourceAverageConfig" :key="systemResourceItem.dataIndex">
        {{ systemResourceItem.label }}
      </span>
    </div>
    <div class="flex-1 flex p-2 max-w-2/3">
      <div class="flex flex-col flex-1 min-w-0">
        <div
          v-for="systemResourceItem in systemResourceAverageConfig"
          :key="systemResourceItem.dataIndex"
          class="flex items-center space-x-2">
          <span
            class="truncate"
            :title="props.result[systemResourceItem.dataIndex] ? `${props.result[systemResourceItem.dataIndex]}${systemResourceItem.unit}` : ''">
            {{ t('xcan_httpTestInfo.average') }} {{ props.result[systemResourceItem.dataIndex] ? `${props.result[systemResourceItem.dataIndex]}${systemResourceItem.unit}` : '--' }}
          </span>
          <Icon style="min-width: 12px;" v-bind="getStabilityStatusIcon(systemResourceItem.dataIndex)" />
        </div>
      </div>
      <div class="text-text-sub-content flex flex-col flex-1 min-w-0">
        <div
          v-for="systemResourceItem in systemResourceMaximumConfig"
          :key="systemResourceItem.dataIndex"
          class="flex items-center space-x-2">
          <span
            class="flex-1 truncate"
            :title="props.result[systemResourceItem.dataIndex] ? `${props.result[systemResourceItem.dataIndex]}${systemResourceItem.unit}` : ''">
            {{ t('xcan_httpTestInfo.max') }} {{ props.result[systemResourceItem.dataIndex] ? `${props.result[systemResourceItem.dataIndex]}${systemResourceItem.unit}` : '--' }}
          </span>
        </div>
      </div>
    </div>
  </div>
</template>
