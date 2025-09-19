<script setup lang="ts">
import { ref, defineAsyncComponent, watch, computed } from 'vue';
import { RadioGroup, RadioButton, Slider } from 'ant-design-vue';
import { cloneDeep } from 'lodash-es';
import { useI18n } from 'vue-i18n';

import { allResponseTimeColumns, allCvsNames, allErrorsColumns, allErrorRateColumns, allUploadColumns, oneUploadColumns, throughputColumns, oneThroughputColumns } from '../../ChartConfig';

const LineChart = defineAsyncComponent(() => import('../LineChart.vue'));
const InfoTable = defineAsyncComponent(() => import('../InfoTable.vue'));

interface Props {
  isSingleInterface:boolean;
  cvsKeys:string[];
  timestampData:string[];
  apiNames:string[];
  brpsUnit:'KB' | 'MB';
  minBrpsUnit: 'KB' | 'MB';
  maxBrpsUnit:'KB' | 'MB';
  meanBrpsUnit:'KB' | 'MB';
  bwpsUnit:'KB' | 'MB';
  minBwpsUnit: 'KB' | 'MB';
  maxBwpsUnit:'KB' | 'MB';
  meanBwpsUnit:'KB' | 'MB';
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
  indexDimensionObj: undefined,
  apiDimensionObj: undefined,
  columns: () => [],
  indexOptions: () => [],
  pipelineTargetMappings: undefined
});

const { t } = useI18n();

const activeKey = ref<'responseTime' | 'throughput' | 'errorNum' | 'errorRate' | 'upload'>('throughput');

const currIndex = ref(props.cvsKeys[0]);
const currApi = ref('Total');

const xData = ref<string[]>([]);
const seriesData = ref<{name:string, data:number[]}[]>([]);
const sliderValue = ref<[number, number]>([0, 0]);
const sliderMin = ref<number>(0);
const sliderMax = ref<number>(0);
const isSliding = ref(false);

const tableData = ref<Record<string, any>[]>([]);

const getTableData = (mappings:{[key:string]:string[]}, keys:string[], dataSource:{[key:string]:{[key:string]:number}}) => {
  if (props.isSingleInterface) {
    const obj = {
      name: 'Total'
    };
    for (const key in props.apiDimensionObj.Total) {
      if (keys.includes(key)) {
        obj[key] = props.apiDimensionObj.Total[key][sliderValue.value[1]];
      }
    }
    return [obj];
  }

  if (!mappings || !Object.keys(mappings)?.length || !keys?.length) {
    return [];
  }
  const result:any = [];
  for (const key in mappings) {
    const item = { name: key, list: [] };
    keys.reduce((prev, cur) => {
      prev[cur] = isNaN(Number(dataSource?.[key]?.[cur])) ? '--' : dataSource[key][cur];
      return prev;
    }, item);
    if (mappings[key].length) {
      item.list = mappings[key].map((_key) => {
        const listItem = { name: _key };

        return keys.reduce((prev, cur) => {
          prev[cur] = isNaN(Number(dataSource?.[_key]?.[cur])) ? '--' : dataSource[_key][cur];
          return prev;
        }, listItem);
      });
    }

    result.push(item);
  }

  return result;
};

const oneApiTableKey = ref(props.isSingleInterface ? ['tps'] : ['ops', 'tps']);

const getApiDimensionObj = (apiDimensionObj, index) => {
  const newApiDimensionObj = {};
  for (const key in apiDimensionObj) {
    newApiDimensionObj[key] = {};
    for (const prop in apiDimensionObj[key]) {
      newApiDimensionObj[key][prop] = apiDimensionObj[key][prop][index];
    }
  }
  tableData.value = getTableData(props.pipelineTargetMappings, props.cvsKeys, newApiDimensionObj);
};

// 设置操作更新图表数据
const setUpdateSeriesData = () => {
  if (activeKey.value === 'responseTime') {
    oneApiTableKey.value = ['tranMean', 'tranMin', 'tranMax', 'tranP50', 'tranP75', 'tranP90', 'tranP95', 'tranP99', 'tranP999'];
  }
  if (activeKey.value === 'throughput') {
    oneApiTableKey.value = props.isSingleInterface ? ['tps'] : ['ops', 'tps'];
  }
  if (activeKey.value === 'errorNum') {
    oneApiTableKey.value = ['errors'];
  }
  if (activeKey.value === 'errorRate') {
    oneApiTableKey.value = ['errorRate'];
  }
  if (activeKey.value === 'upload') {
    oneApiTableKey.value = ['brps', 'bwps'];
  }
  const yData:{name:string, key:string, data:number[]}[] = [];
  for (const item of Object.keys(props.indexDimensionObj)) {
    if (oneApiTableKey.value.includes(item)) {
      const data = props.indexDimensionObj[item][currApi.value].slice(sliderValue.value[0], sliderValue.value[1] + 1);
      yData.push({ name: allCvsNames?.[item] || '', key: item, data: data });
    }
  }
  seriesData.value = yData;
};

watch(() => props.timestampData, (newVal) => {
  if (!newVal.length) {
    tableData.value = [];
    seriesData.value = [];
    xData.value = [];
    return;
  }

  sliderMax.value = newVal.length - 1;
  if (!isSliding.value) {
    sliderValue.value[1] = sliderMax.value;
    getApiDimensionObj(props.apiDimensionObj, sliderValue.value[1]);
    xData.value = cloneDeep(newVal);
    setUpdateSeriesData();
  }
}, {
  immediate: true
});

const sliderChange = (value) => {
  if (value[1] < props.timestampData.length - 1) {
    isSliding.value = true;
  } else {
    isSliding.value = false;
  }

  if (value[1] !== sliderValue.value[1]) {
    getApiDimensionObj(props.apiDimensionObj, value[1]);
  }

  sliderValue.value = value;

  xData.value = props.timestampData.slice(sliderValue.value[0], sliderValue.value[1] + 1);
  setUpdateSeriesData();
};

const radioGroupChange = (e) => {
  activeKey.value = e.target.value;
  xData.value = props.timestampData.slice(sliderValue.value[0], sliderValue.value[1] + 1);
  setUpdateSeriesData();

  let keys = oneApiTableKey.value;
  if (activeKey.value === 'upload') {
    keys = ['brps', 'minBrps', 'maxBrps', 'meanBrps', 'bwps', 'minBwps', 'maxBwps', 'meanBwps'];
  }

  if (activeKey.value === 'throughput') {
    keys = props.isSingleInterface ? ['tps', 'minTps', 'maxTps', 'meanTps'] : ['ops', 'minOps', 'maxOps', 'meanOps', 'tps', 'minTps', 'maxTps', 'meanTps'];
  }
  tableData.value = getTableData({}, keys, {});
};

const sliderFormatter = (value: number) => {
  return props.timestampData[value];
};

const columns = computed(() => {
  if (props.isSingleInterface) {
    if (activeKey.value === 'responseTime') {
      return allResponseTimeColumns;
    }
    if (activeKey.value === 'throughput') {
      return props.isSingleInterface ? oneThroughputColumns : throughputColumns;
    }
    if (activeKey.value === 'errorNum') {
      return props.isSingleInterface ? allErrorsColumns.filter(item => item.dataIndex !== 'ops' && item.dataIndex !== 'operations') : allErrorsColumns;
    }
    if (activeKey.value === 'errorRate') {
      return allErrorRateColumns;
    }
    if (activeKey.value === 'upload') {
      return props.isSingleInterface ? oneUploadColumns : allUploadColumns;
    }
  }
  return props.isSingleInterface ? props.columns.filter(item => item.dataIndex !== 'ops' && item.dataIndex !== 'operations') : props.columns;
});

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
  <div class="w-full min-h-ull">
    <template v-if="isSingleInterface">
      <RadioGroup
        v-model:value="activeKey"
        buttonStyle="solid"
        size="small"
        class="ml-10"
        @change="radioGroupChange">
        <RadioButton value="throughput">{{ t('ftpPlugin.performanceTestDetail.aggregate.radioButtons.byThroughput') }}</RadioButton>
        <RadioButton value="responseTime">{{ t('ftpPlugin.performanceTestDetail.aggregate.radioButtons.byResponseTime') }}</RadioButton>
        <RadioButton value="errorNum">{{ t('ftpPlugin.performanceTestDetail.aggregate.radioButtons.byErrorCount') }}</RadioButton>
        <RadioButton value="errorRate">{{ t('ftpPlugin.performanceTestDetail.aggregate.radioButtons.byErrorRate') }}</RadioButton>
        <RadioButton value="upload">{{ t('ftpPlugin.performanceTestDetail.aggregate.radioButtons.byUploadDownload') }}</RadioButton>
      </RadioGroup>
      <LineChart
        ref="chartRef"
        :series="seriesData"
        :xData="xData"
        :selectedIndex="currIndex"
        type="throughput"
        :brpsUnit="props.brpsUnit"
        :bwpsUnit="props.bwpsUnit"
        class="mt-2" />
      <div v-if="props.timestampData.length >= 2" class="-mt-5 pl-10 pr-12 mb-8">
        <Slider
          :value="sliderValue"
          :min="sliderMin"
          :max="sliderMax"
          :tipFormatter="sliderFormatter"
          range
          @change="sliderChange" />
      </div>
    </template>
    <InfoTable
      tabKey="aggregate"
      :isSingleInterface="props.isSingleInterface"
      :brpsUnit="props.brpsUnit"
      :bwpsUnit="props.bwpsUnit"
      :minBrpsUnit="props.minBrpsUnit"
      :maxBrpsUnit="props.maxBrpsUnit"
      :meanBrpsUnit="props.meanBrpsUnit"
      :minBwpsUnit="props.minBwpsUnit"
      :maxBwpsUnit="props.maxBwpsUnit"
      :meanBwpsUnit="props.meanBwpsUnit"
      :columns="columns"
      :tableData="tableData" />
  </div>
</template>
<style scoped>
.group-table :deep(.ant-table){
  margin: 2px -7px 2px 12px !important;
  padding-left: 13px;
}

:deep(.ant-table .last-row > td){
  background-color:  #FAFCFC;
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
