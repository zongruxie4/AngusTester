<script lang="ts" setup>
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';
import { Icon } from '@xcan-angus/vue-ui';

import apiUtils from '@/utils/apis/index';
// Define component props
interface Props {
  indicatorStability: {
    duration: string;
    errorRate: string;
    threads: string;
    tps: string;
    percentile: string;
    art: string;
    cpu: string;
    disk: string;
    memory: string;
    network: string;
  },
  result: {
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
}

const { t } = useI18n();
const props = withDefaults(defineProps<Props>(), {
  indicatorStability: () => ({
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

// Compute indicator items for display
const indicatorItem = computed(() => {
  const percentile = props.indicatorStability.percentile.value;
  let percentitleKey;
  switch (percentile) {
    case 'ALL':
      percentitleKey = 'tranMax';
      break;
    case 'P50':
      percentitleKey = 'tranP50';
      break;
    case 'P75':
      percentitleKey = 'tranP75';
      break;
    case 'P90':
      percentitleKey = 'tranP90';
      break;
    case 'P95':
      percentitleKey = 'tranP95';
      break;
    case 'P99':
      percentitleKey = 'tranP99';
      break;
    case 'P999':
      percentitleKey = 'tranP999';
      break;
    default:
      percentitleKey = '';
  }
  return [
    {
      label: t('execution.testResult.concurrency'),
      dataIndex: 'threads',
      resultIndex: 'threadPoolSize'
    },
    {
      label: t('execution.testResult.testDuration'),
      dataIndex: 'duration',
      resultIndex: 'duration'
    },
    {
      label: t('execution.testResult.responseTime'),
      dataIndex: 'art',
      resultIndex: percentitleKey,
      compareOprate: '<='
    },
    {
      label: t('execution.testResult.transactionsPerSecond'),
      dataIndex: 'tps',
      resultIndex: 'tps'
    },
    {
      label: t('execution.testResult.errorRateSimple'),
      dataIndex: 'errorRate',
      resultIndex: 'errorRate'
    },
    {
      label: t('execution.testResult.applicationSystemAverageLoad'),
      dataIndex: 'sys'
    }
  ];
});

// System indicator items configuration
const sysIndicatorItem = [
  {
    label: t('execution.testResult.cpuUsage'),
    dataIndex: 'meanCpu',
    idx: 'cpu',
    maxIndex: 'maxCpu'
  },
  {
    label: t('execution.testResult.memoryUsage'),
    dataIndex: 'meanMemory',
    idx: 'memory',
    maxIndex: 'maxMemory'
  },
  {
    label: t('execution.testResult.diskUsage'),
    dataIndex: 'meanFilesystem',
    idx: 'disk',
    maxIndex: 'maxFilesystem'
  },
  {
    label: t('execution.testResult.networkUsage'),
    dataIndex: 'meanNetwork',
    idx: 'network',
    maxIndex: 'maxNetwork'
  }
];

// Get icon configuration based on value key and comparison result
const getIcon = (valueKey) => {
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

// Format duration value for display
const getDuration = (mseconds) => {
  const [, unit] = apiUtils.splitDuration(props.indicatorStability.duration || '');
  if (!mseconds) {
    return '--';
  }
  return apiUtils.formatMillisecondToShortDuraiton(+mseconds, unit);
};
</script>

<template>
  <div class="border rounded bg-white">
    <!-- Stability test header -->
    <div class="flex py-1 px-2">
      <span class="w-30">{{ t('execution.testResult.testParameters') }}</span>
      <span class="flex-1">{{ t('execution.testResult.testIndicators') }}</span>
      <span class="flex-1">{{ t('execution.testResult.testResults') }}</span>
    </div>

    <!-- Stability indicators and results -->
    <div
      v-for="item in indicatorItem"
      :key="item.dataIndex"
      class="flex border-t py-1 px-2 items-center">
      <span class="w-30">{{ item.label }}</span>
      <span v-if="item.dataIndex !== 'sys'" class="flex-1 text-text-sub-content">
        <template v-if="item.dataIndex === 'art'">{{ props.indicatorStability.percentile.value }}</template>
        {{ (item.compareOprate || '') + (props.indicatorStability[item.dataIndex] || '') }}
      </span>
      <div v-else class="flex-1 text-text-sub-content">
        <div
          v-for="sysItem in sysIndicatorItem"
          :key="sysItem.dataIndex"
          class="py-0.5">
          {{ `${sysItem.label} <= ${props.indicatorStability[sysItem.idx] || ''}${sysItem.idx === 'network' ? 'MB' : '%'}` }}
        </div>
      </div>
      <div class="flex-1">
        <template v-if="item.dataIndex !== 'sys'">
          {{ item.resultIndex === 'duration' ? getDuration(props.result[item.resultIndex]) : props.result[item.resultIndex] }}
          <Icon v-bind="getIcon(item.resultIndex)" />
        </template>
        <template v-else>
          <div
            v-for="sysItem in sysIndicatorItem"
            :key="sysItem.dataIndex"
            class="py-0.5 flex">
            <div class="flex-1">
              {{ `${sysItem.label} ${props.result[sysItem.dataIndex] || '--'}${sysItem.idx === 'network' ? 'MB' : '%'}` }}
              <Icon v-bind="getIcon(sysItem.dataIndex)" />
            </div>
            <div class="flex-1 text-text-sub-content">
              {{ `${t('execution.testResult.maximum')} ${props.result[sysItem.maxIndex] || '--'}${sysItem.idx === 'network' ? 'MB' : '%'}` }}
            </div>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>
