<script lang="ts" setup>
import { computed } from 'vue';
import { Icon } from '@xcan-angus/vue-ui';
import apiUtils from 'angus-design/utils';

interface Props {
  indicatorPerf: {
    duration: string;
    errorRate: string;
    tps: string;
    threads: string;
    percentile: string; // [ ALL, P50, P75, P90, P95, P99, P999 ]
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
  },
  // passedInfo: {
  //   passed: boolean;
  //   failureMessage?: string;
  // }
}

const props = withDefaults(defineProps<Props>(), {
  indicatorPerf: () => ({
    duration: '',
    errorRate: '',
    tps: '',
    threads: '',
    percentile: '',
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
      label: '并发数',
      dataIndex: 'threads',
      resultIndex: 'threadPoolSize'
    },
    {
      label: '测试时长',
      dataIndex: 'duration',
      resultIndex: 'duration'
    },
    {
      label: '增压并发时数',
      dataIndex: 'rampUpThreads',
      resultIndex: 'rampUpThreads'
    },
    {
      label: '增压并发时长',
      dataIndex: 'rampUpInterval',
      resultIndex: 'rampUpInterval'
    },
    {
      label: '响应时间(RT)',
      dataIndex: 'art',
      compareOprate: '<=',
      resultIndex: percentitleKey || '--'
    },
    {
      label: '每秒事务数(TPS)',
      dataIndex: 'tps',
      compareOprate: '>=',
      resultIndex: 'tps'
    },
    {
      label: '错误率(ErrorRate)',
      dataIndex: 'errorRate',
      compareOprate: '<=',
      resultIndex: 'errorRate',
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
  const [value, unit] = apiUtils.splitDuration(props.indicatorPerf.duration || '');
  if (!mseconds) {
    return '--';
  }
  return apiUtils.formatMillisecondToShortDuraiton(+mseconds, unit);
};

</script>
<template>
  <div class="border rounded p-2 bg-white">
    <!-- <div
      class="flex py-1 items-center"
      :class="{'text-status-error': !props.passedInfo.passed, 'text-status-success': props.passedInfo.passed }">
      <template v-if="props.passedInfo.passed">
        <Icon icon="icon-duihao" class="text-status-success mr-1" />
      </template>
      <template v-else>
        <Icon icon="icon-chahao" class="text-status-error mr-1" />
      </template>
      {{ props.passedInfo.passed ? '通过' : '未通过：' }} {{ props.passedInfo?.failureMessage || '' }}
    </div> -->
    <div class="flex py-1 ">
      <span class="w-50">性能测试</span>
      <span class="flex-1">测试指标</span>
      <span class="flex-1">测试结果</span>
    </div>
    <div
      v-for="item in indicatorItem"
      :key="item.dataIndex"
      class="flex py-1 border-t">
      <span class="w-50">{{ item.label }}</span>
      <span class="flex-1 text-text-sub-content">
        <template v-if="item.dataIndex.includes('tran')">
          {{ props.indicatorPerf.percentile.value }}
        </template>
        {{ (item.compareOprate || '') + (props.indicatorPerf[item.dataIndex] || '') + `${item.unit || ''}` }}
      </span>
      <div class="flex-1">
        {{ (item.resultIndex === 'duration' ? getDuration(props.result[item.resultIndex]) : (props.result[item.resultIndex] || '--')) + `${item.unit || ''}` }}
        <Icon v-bind="getIcon(item.resultIndex)" />
      </div>
    </div>
  </div>
</template>
