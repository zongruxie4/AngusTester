<script setup lang="ts">
import { computed } from 'vue';
import { Icon } from '@xcan-angus/vue-ui';
import ApiUtils from 'src/utils/apis';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();
const { formatMillisecondToShortDuraiton, splitDuration } = ApiUtils;

interface Props {
  indicatorStability: {
    duration: string;
    errorRate: string;
    threads: string;
    tps: string;
    percentile: {value: string};
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
      label: t('xcan_httpTestInfo.concurrency'),
      dataIndex: 'threadPoolSize'
    },
    {
      label: t('xcan_httpTestInfo.testDuration'),
      dataIndex: 'duration'
    },
    {
      label: `${t('xcan_httpTestInfo.responseTime')}(${percentile})`,
      dataIndex: percentitleKey || '--'
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

const sysIndicatorItem = [
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

const sysMaxIndicatorItem = [
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

const getDuration = (mseconds) => {
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  const [value, unit] = splitDuration(props.indicatorStability.duration);
  if (!mseconds) {
    return '--';
  }
  return formatMillisecondToShortDuraiton(+mseconds, unit);
};

</script>
<template>
  <div class="flex border rounded text-3">
    <div class="flex-1 bg-status-warn flex flex-col p-2">
      <span v-for="item in indicatorItem" :key="item.dataIndex">
        {{ item.label }}
      </span>
    </div>
    <div class="flex-1 flex flex-col p-2">
      <div
        v-for="item in indicatorItem"
        :key="item.dataIndex"
        class="flex items-center space-x-2">
        <span>{{ item.dataIndex === 'duration' ? getDuration(props.result[item.dataIndex]) : props.result[item.dataIndex] || '--' }}</span>
        <Icon v-bind="getIcon(item.dataIndex)" />
      </div>
    </div>
  </div>
  <div class="flex border rounded text-3 mt-2">
    <div class="flex-1 bg-status-warn flex flex-col p-2">
      <span v-for="item in sysIndicatorItem" :key="item.dataIndex">
        {{ item.label }}
      </span>
    </div>
    <div class="flex-1 flex p-2 max-w-2/3">
      <div class="flex flex-col flex-1 min-w-0">
        <div
          v-for="item in sysIndicatorItem"
          :key="item.dataIndex"
          class="flex items-center space-x-2">
          <span
            class="truncate"
            :title="props.result[item.dataIndex] ? `${props.result[item.dataIndex]}${item.unit}` : ''">
            {{ t('xcan_httpTestInfo.average') }} {{ props.result[item.dataIndex] ? `${props.result[item.dataIndex]}${item.unit}` : '--' }}
          </span>
          <Icon style="min-width: 12px;" v-bind="getIcon(item.dataIndex)" />
        </div>
      </div>
      <div class="text-text-sub-content flex flex-col flex-1 min-w-0">
        <div
          v-for="item in sysMaxIndicatorItem"
          :key="item.dataIndex"
          class="flex items-center space-x-2">
          <span
            class="flex-1 truncate"
            :title="props.result[item.dataIndex] ? `${props.result[item.dataIndex]}${item.unit}` : ''">
            {{ t('xcan_httpTestInfo.max') }} {{ props.result[item.dataIndex] ? `${props.result[item.dataIndex]}${item.unit}` : '--' }}
          </span>
        </div>
      </div>
    </div>
  </div>
</template>
