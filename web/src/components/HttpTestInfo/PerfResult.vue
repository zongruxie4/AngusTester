<script lang="ts" setup>
import { computed } from 'vue';
import { Icon } from '@xcan-angus/vue-ui';
import ApiUtils from '@/utils/ApiUtils';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();
const { splitDuration, formatMillisecondToShortDuraiton } = ApiUtils;

interface Props {
  indicatorPerf: {
    duration: string;
    errorRate: string;
    tps: string;
    threads: string;
    percentile: {value: string}; // [ ALL, P50, P75, P90, P95, P99, P999 ]
    rampUpInterval: string;
    rampUpThreads: string;
    art: string;
  },
  result: {
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
}

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

const indicatorItem = computed(() => {
  const percentile = props.indicatorPerf.percentile.value;
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
      label: t('xcan_httpTestInfo.rampUpConcurrency'),
      dataIndex: 'rampUpThreads'
    },
    {
      label: t('xcan_httpTestInfo.rampUpDuration'),
      dataIndex: 'rampUpInterval'
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
      dataIndex: 'errorRate',
      unit: '%'
    }
  ];
});

const getIcon = (valueKey) => {
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

const getDuration = (mseconds) => {
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  const [value, unit] = splitDuration(props.indicatorPerf.duration);
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
        <span>{{ props.result[item.dataIndex] ? `${item.dataIndex ==='duration' ? getDuration(props.result[item.dataIndex]) : props.result[item.dataIndex]}${item.unit || ''}` : '--' }}</span>
        <Icon v-bind="getIcon(item.dataIndex)" />
      </div>
    </div>
  </div>
</template>
