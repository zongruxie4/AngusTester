<script setup lang="ts">
import { ref, defineAsyncComponent, watch } from 'vue';
import { Slider, CheckboxGroup, Checkbox } from 'ant-design-vue';
import { Toggle } from '@xcan-angus/vue-ui';
import { cloneDeep } from 'lodash-es';
import { useI18n } from 'vue-i18n';

import { allCvsNames } from '../../ChartConfig';
const LineChart = defineAsyncComponent(() => import('../LineChart.vue'));

interface Props {
  isSingleInterface:boolean;
  cvsKeys:string[];
  timestampData:string[];
  apiNames:string[];
  brpsUnit: 'KB' | 'MB';
  bwpsUnit: 'KB' | 'MB';
  apiDimensionObj: {[key:string]:{ [key:string]: number[]}};
  indexDimensionObj: {[key:string]:{ [key:string]: number[]}};
  columns:{label:string, dataIndex:string}[];
  indexOptions:{label:string, value:string}[];
  pipelineTargetMappings: Record<string, string[] | null >;
}

const props = withDefaults(defineProps<Props>(), {
  isSingleInterface: false,
  cvsKeys: () => [],
  timestampData: () => [],
  apiNames: () => [],
  brpsUnit: 'KB',
  bwpsUnit: 'KB',
  apiDimensionObj: undefined,
  columns: () => [],
  indexOptions: () => [],
  pipelineTargetMappings: undefined
});

const { t } = useI18n();

const currIndex = ref(props.cvsKeys[0]);

const xData = ref<string[]>([]);
const seriesData = ref<{name:string, data:number[]}[]>([]);
const sliderValue = ref<[number, number]>([0, 0]);
const sliderMin = ref<number>(0);
const sliderMax = ref<number>(0);
const isSliding = ref(false);

const checkboxGroupValue = ref<string[]>([]);
const CheckboxGroupChange = (values:string[]) => {
  checkboxGroupValue.value = values;
  setCharData(values);
};

const sliderChange = (value) => {
  if (value[1] < props.timestampData.length - 1) {
    isSliding.value = true;
  } else {
    isSliding.value = false;
  }

  sliderValue.value = value;

  xData.value = props.timestampData.slice(value[0], value[1] + 1);
  setCharData(checkboxGroupValue.value);
};

const setCharData = (values:string[]) => {
  seriesData.value = [];
  for (let i = 0; i < values.length; i++) {
    const apiName = values[i].slice(0, values[i].lastIndexOf('-'));
    const cvsKey = values[i].slice(values[i].lastIndexOf('-')).slice(1);
    const data = isSliding.value ? props.indexDimensionObj[cvsKey][apiName].slice(sliderValue.value[0], sliderValue.value[1] + 1) : props.indexDimensionObj[cvsKey][apiName];
    seriesData.value.push({ name: `${apiName}-${allCvsNames[cvsKey]}`, key: cvsKey, data });
  }
};

const sliderFormatter = (value: number) => {
  return props.timestampData[value];
};

const throughputOptions = [
  {
    label: t('ftpPlugin.performanceTestDetail.superimposeAnalysis.throughputOptions.queriesPerSecond'),
    value: 'ops'
  },
  {
    label: t('ftpPlugin.performanceTestDetail.superimposeAnalysis.throughputOptions.transactionsPerSecond'),
    value: 'tps'
  },
  {
    label: t('ftpPlugin.performanceTestDetail.superimposeAnalysis.throughputOptions.downloadPerSecond'),
    value: 'brps'
  },
  {
    label: t('ftpPlugin.performanceTestDetail.superimposeAnalysis.throughputOptions.uploadPerSecond'),
    value: 'bwps'
  }
];

watch(() => props.timestampData, (newVal) => {
  if (!newVal.length) {
    seriesData.value = [];
    xData.value = [];
    return;
  }

  sliderMax.value = newVal.length - 1;
  if (!isSliding.value) {
    sliderValue.value[1] = sliderMax.value;
  }

  if (!isSliding.value) {
    xData.value = cloneDeep(newVal);
    if (!checkboxGroupValue.value.length) {
      checkboxGroupValue.value = throughputOptions.map(item => 'Total-' + item.value);
    }
    setCharData(checkboxGroupValue.value);
  }
}, {
  immediate: true
});

const throughputExpand = ref(true);
const threadExpand = ref(true);
const responseTimeExpand = ref(true);
const errorExpand = ref(true);

const threadOptions = [
  {
    label: t('ftpPlugin.performanceTestDetail.superimposeAnalysis.threadOptions.threadCount'),
    value: 'threadPoolSize'
  },
  {
    label: t('ftpPlugin.performanceTestDetail.superimposeAnalysis.threadOptions.maxThreadCount'),
    value: 'threadMaxPoolSize'
  },
  {
    label: t('ftpPlugin.performanceTestDetail.superimposeAnalysis.threadOptions.activeThreadCount'),
    value: 'threadPoolActiveSize'
  }
];

const responseTimeOptions = [
  {
    label: t('chart.average'),
    value: 'tranMean'
  },
  {
    label: t('chart.min'),
    value: 'tranMin'
  },
  {
    label: t('chart.max'),
    value: 'tranMax'
  },
  {
    label: t('chart.p50'),
    value: 'tranP50'
  },
  {
    label: t('chart.p75'),
    value: 'tranP75'
  },
  {
    label: t('chart.p90'),
    value: 'tranP90'
  },
  {
    label: t('chart.p95'),
    value: 'tranP95'
  },
  {
    label: t('chart.p99'),
    value: 'tranP99'
  },
  {
    label: t('ftpPlugin.performanceTestDetail.superimposeAnalysis.responseTimeOptions.p999'),
    value: 'tranP999'
  }
];

const errorOptions = [
  {
    label: t('ftpPlugin.performanceTestDetail.superimposeAnalysis.errorOptions.errorCount'),
    value: 'errors'
  },
  {
    label: t('ftpPlugin.performanceTestDetail.superimposeAnalysis.errorOptions.errorRate'),
    value: 'errorRate'
  }
];

const chartRef = ref();
const resizeChart = () => {
  if (chartRef.value) {
    chartRef.value.resizeChart();
  }
};

defineExpose({
  resizeChart
});
</script>
<template>
  <div class="w-full">
    <LineChart
      ref="chartRef"
      :series="seriesData"
      :xData="xData"
      :selectedIndex="currIndex"
      :brpsUnit="brpsUnit"
      :bwpsUnit="bwpsUnit"
      tabKey="analyze" />
    <div v-if="props.timestampData.length >=2" class="-mt-5 pl-10 pr-12">
      <Slider
        :value="sliderValue"
        :min="sliderMin"
        :max="sliderMax"
        :tipFormatter="sliderFormatter"
        range
        @change="sliderChange" />
    </div>
    <div class="pl-10 text-3 mt-5">
      <CheckboxGroup :value="checkboxGroupValue" @change="CheckboxGroupChange">
        <Toggle
          v-model:open="throughputExpand"
          :title="t('ftpPlugin.performanceTestDetail.superimposeAnalysis.toggleTitles.throughput')">
          <div
            v-for="apiName in props.apiNames"
            :key="apiName"
            class="flex">
            <div class="w-60 truncate" :title="apiName">{{ apiName }}</div>
            <div class="space-x-5">
              <Checkbox
                v-for="option in throughputOptions"
                :key="apiName+'-'+option.value"
                :value="apiName+'-'+option.value"
                :disabled="(!checkboxGroupValue.includes(apiName+'-'+option.value) && checkboxGroupValue.length >=10) || (checkboxGroupValue.includes(apiName+'-'+option.value) && checkboxGroupValue.length === 1)">
                {{ option.label }}
              </Checkbox>
            </div>
          </div>
        </Toggle>
        <Toggle
          v-model:open="threadExpand"
          :title="t('ftpPlugin.performanceTestDetail.superimposeAnalysis.toggleTitles.concurrency')"
          class="mt-2">
          <div
            v-for="apiName in props.apiNames"
            :key="apiName"
            class="flex">
            <div class="w-60 truncate" :title="apiName">{{ apiName }}</div>
            <div class="space-x-5">
              <Checkbox
                v-for="option in threadOptions"
                :key="apiName+'-'+option.value"
                :value="apiName+'-'+option.value"
                :disabled="(!checkboxGroupValue.includes(apiName+'-'+option.value) && checkboxGroupValue.length >=10) || (checkboxGroupValue.includes(apiName+'-'+option.value) && checkboxGroupValue.length === 1)">
                {{ option.label }}
              </Checkbox>
            </div>
          </div>
        </Toggle>
        <Toggle
          v-model:open="responseTimeExpand"
          :title="t('ftpPlugin.performanceTestDetail.superimposeAnalysis.toggleTitles.responseTime')"
          class="mt-2">
          <div
            v-for="apiName in props.apiNames"
            :key="apiName"
            class="flex">
            <div class="w-60 truncate" :title="apiName">{{ apiName }}</div>
            <div class="space-x-5">
              <Checkbox
                v-for="option in responseTimeOptions"
                :key="apiName+'-'+option.value"
                :value="apiName+'-'+option.value"
                :disabled="(!checkboxGroupValue.includes(apiName+'-'+option.value) && checkboxGroupValue.length >=10) || (checkboxGroupValue.includes(apiName+'-'+option.value) && checkboxGroupValue.length === 1)">
                {{ option.label }}
              </Checkbox>
            </div>
          </div>
        </Toggle>
        <Toggle
          v-model:open="errorExpand"
          :title="t('ftpPlugin.performanceTestDetail.superimposeAnalysis.toggleTitles.error')"
          class="mt-2">
          <div
            v-for="apiName in props.apiNames"
            :key="apiName"
            class="flex">
            <div class="w-60 truncate" :title="apiName">{{ apiName }}</div>
            <div class="space-x-5">
              <Checkbox
                v-for="option in errorOptions"
                :key="apiName+'-'+option.value"
                :value="apiName+'-'+option.value"
                :disabled="(!checkboxGroupValue.includes(apiName+'-'+option.value) && checkboxGroupValue.length >=10) || (checkboxGroupValue.includes(apiName+'-'+option.value) && checkboxGroupValue.length === 1)">
                {{ option.label }}
              </Checkbox>
            </div>
          </div>
        </Toggle>
      </CheckboxGroup>
    </div>
  </div>
</template>
<style scoped>
:deep(.toggle-title){
  @apply text-3
}

:deep(.toggle-main-container){
  @apply px-8 pt-2
}

:deep(.ant-slider-rail){
  height: 10px;
  border: 1px solid var(--border-divider);
  border-radius:1px;
  background-color: rgba(0, 0, 0, 0%);
}

:deep(.ant-slider:hover .ant-slider-rail){
  height: 10px;
  background-color: rgba(0, 0, 0, 0%);
}

:deep(.ant-slider-track){
  height: 10px;
  border-color:rgba(145, 213, 255, 30%);
  background-color: rgba(245, 245, 245, 100%);

  @apply border;
}

:deep(.ant-slider:hover .ant-slider-track){
  height: 10px;
  background-color: rgba(245, 245, 245, 100%);
}

:deep(.ant-slider-step){
  height: 10px;
  background-color: rgba(0, 0, 0, 0%);
}

:deep(.ant-slider-handle){
  width: 10px;
  height: 16px;
  margin-top:-3px;
  border-radius:3px;
  border-color:rgba(145, 213, 255, 100%);

  @apply border-2 ;
}

:deep(.ant-slider-handle:focus){
  border-color:rgba(145, 213, 255, 100%);
  box-shadow:none
}

:deep(.ant-slider:hover .ant-slider-handle){
  border-color: rgba(145, 213, 255, 100%);
}
</style>
