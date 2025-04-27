<script lang="ts" setup>
import { computed } from 'vue';
import { Icon } from '@xcan-angus/vue-ui';
import apiUtils from 'angus-design/utils';

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
  },
  // passedInfo: {
  //   passed: boolean;
  //   failureMessage?: string;
  // }
}

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
      label: '响应时间(RT)',
      dataIndex: 'art',
      resultIndex: percentitleKey,
      compareOprate: '<='
    },
    {
      label: '每秒事务数(TPS)',
      dataIndex: 'tps',
      resultIndex: 'tps'
    },
    {
      label: '错误率',
      dataIndex: 'errorRate',
      resultIndex: 'errorRate'
    },
    {
      label: '应用系统平均负载',
      dataIndex: 'sys'
    }
  ];
});

const sysIndicatorItem = [
  {
    label: 'CPU使用率',
    dataIndex: 'meanCpu',
    idx: 'cpu',
    maxIndex: 'maxCpu'
  },
  {
    label: '内存使用率',
    dataIndex: 'meanMemory',
    idx: 'memory',
    maxIndex: 'maxMemory'
  },
  {
    label: '磁盘使用率',
    dataIndex: 'meanFilesystem',
    idx: 'disk',
    maxIndex: 'maxFilesystem'
  },
  {
    label: '网络使用量',
    dataIndex: 'meanNetwork',
    idx: 'network',
    maxIndex: 'maxNetwork'
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
  const [, unit] = apiUtils.splitDuration(props.indicatorStability.duration || '');
  if (!mseconds) {
    return '--';
  }
  return apiUtils.formatMillisecondToShortDuraiton(+mseconds, unit);
};

</script>
<template>
  <div class="border rounded bg-white">
    <div class="flex py-1 px-2">
      <span class="w-30">测试参数</span>
      <span class="flex-1">测试指标</span>
      <span class="flex-1">测试结果</span>
    </div>
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
              {{ `最大 ${props.result[sysItem.maxIndex] || '--'}${sysItem.idx === 'network' ? 'MB' : '%'}` }}
            </div>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>
