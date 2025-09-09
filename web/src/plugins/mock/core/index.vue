<script lang="ts" setup>
import { defineAsyncComponent, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { Progress } from 'ant-design-vue';

const { t } = useI18n();

import { formatBytes } from './useExecCount';

interface Props {
  execInfo: Record<string, any>;
  exception?:{
    codeName:string;
    messageName:string;
    code:string;
    message:string;
  };
}
const props = withDefaults(defineProps<Props>(), {
  execInfo: () => ({}),
  exception: undefined
});

const ChartInfo = defineAsyncComponent(() => import('./ChartInfo.vue'));
const TestBasicInfo = defineAsyncComponent(() => import('@/components/TestBasicInfo/index.vue'));
const chartInfoRef = ref();
const countCard = [
  {
    key: 'iterations',
    name: t('mockPlugin.chartLabels.rows'),
    icon: 'icon-yizhihangshijian',
    color: '129, 154, 218'
  },
  {
    key: 'vu',
    name: t('mockPlugin.chartLabels.concurrency'),
    icon: 'icon-bingfashu',
    color: '255, 177, 59'
  },
  {
    key: 'tps',
    name: t('mockPlugin.chartLabels.tps'),
    icon: 'icon-meimiaochaxunshu',
    color: '3, 206, 92'
  },
  {
    key: 'err',
    name: t('mockPlugin.chartLabels.errors'),
    icon: 'icon-cuowushuai',
    color: '245, 34, 45'
  },
  {
    key: 'upload',
    name: t('mockPlugin.chartLabels.writeData'),
    icon: 'icon-meimiaoshiwushu',
    color: '45, 142, 255'
  }
];
// const letterMap = {
//   ms: '毫秒',
//   s: '秒',
//   min: '分钟',
//   h: '小时',
//   d: '天'
// };

// const splitTime = (str: string): [string, string] => {
//   const number = str.replace(/\D/g, '');
//   const unit = str.replace(/\d/g, '');
//   return [number, letterMap[unit]];
// };

const getNumFixed = (str:string) => {
  return str ? Number(str).toFixed(2) : '0';
};

defineExpose({
  restart: () => {
    chartInfoRef.value.restart();
  }
});

</script>
<template>
  <div>
    <TestBasicInfo :value="props.execInfo" :exception="props.exception" />
    <div class="text-3 flex flex-wrap text-text-sun-content mt-5 -mr-2">
      <div
        v-for="item in countCard"
        :key="item.key"
        class="border boder-border-divider rounded mr-2 p-3.5 flex-none mb-2 border-l-4"
        style="width: calc((100% - 40px) / 5);"
        :style="{
          borderLeft: '4px solid rgb(' + item.color + ')',
        }">
        <div
          class="text-text-title mb-1 text-4 font-semibold"
          :style="{
            color: 'rgb(' + item.color + ')',
          }">
          <template v-if="item.key === 'iterations'">
            <template v-if="props.execInfo?.configuration">
              <span>{{ props.execInfo.sampleSummaryInfo?.iterations || '--' }}</span>
              <span>&nbsp;|&nbsp;</span>
              <span>{{ props.execInfo?.task?.mockData?.settings?.rows }}</span>
            </template>
            <template v-else>
              <span>--</span>
              <span>&nbsp;/&nbsp;</span>
              <span>--</span>
            </template>
          </template>
          <template v-if="item.key === 'vu'">
            <span>{{ props.execInfo?.sampleSummaryInfo?.threadPoolActiveSize || "--" }}</span>
            <span>&nbsp;/&nbsp;</span>
            <span>{{ props.execInfo?.thread || "--" }}</span>
          </template>
          <template v-if="item.key === 'tps'">
            {{ getNumFixed(props.execInfo?.sampleSummaryInfo?.tps) }}
          </template>
          <template v-if="item.key === 'err'">
            <span>{{ props.execInfo?.sampleSummaryInfo?.errors ||"--" }}</span>
            <span> &nbsp;|&nbsp;</span>
            <span>{{ props.execInfo?.sampleSummaryInfo?.errorRate?props.execInfo.sampleSummaryInfo?.errorRate+'%':'--' }}</span>
          </template>
          <template v-if="item.key === 'upload'">
            <span>{{ formatBytes(props.execInfo?.sampleSummaryInfo?.bwps)? `${formatBytes(props.execInfo?.sampleSummaryInfo?.bwps)}${t('mockPlugin.chartLabels.perSecond')}`:'--' }}</span>
            <span> &nbsp;|&nbsp;</span>
            <span>{{ formatBytes(props.execInfo?.sampleSummaryInfo?.writeBytes) || '--' }}</span>
          </template>
        </div>
        <div>{{ item.name }}</div>
      </div>
    </div>
    <div v-if="props.execInfo.sampleSummaryInfo?.uploadResultProgress > 0" class="text-3 text-center mt-7">
      <span class="mr-2">{{ t('mockPlugin.chartLabels.uploadProgress') }}</span>
      <Progress
        class="w-100"
        :percent="props.execInfo.sampleSummaryInfo?.uploadResultProgress"
        :format="() => `${formatBytes(props.execInfo.sampleSummaryInfo?.uploadResultBytes)} / ${formatBytes(props.execInfo.sampleSummaryInfo?.uploadResultTotalBytes)}`" />
    </div>
    <ChartInfo
      ref="chartInfoRef"
      class="mt-5"
      :dataSource="props.execInfo" />
  </div>
</template>
