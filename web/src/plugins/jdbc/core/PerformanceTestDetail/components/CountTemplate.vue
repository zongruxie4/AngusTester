<script setup lang="ts">
import { ref, defineAsyncComponent, computed, watch, nextTick } from 'vue';
import { RadioGroup, RadioButton, Slider, CheckboxGroup, Checkbox, TableColumnType } from 'ant-design-vue';
import { Select } from '@xcan-angus/vue-ui';
import { cloneDeep } from 'lodash-es';
import { useI18n } from 'vue-i18n';

import { allCvsNames } from '../ChartConfig';

const LineChart = defineAsyncComponent(() => import('./LineChart.vue'));
const InfoTable = defineAsyncComponent(() => import('./InfoTable.vue'));

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
  tabKey?: string;
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
  pipelineTargetMappings: undefined,
  tabKey: ''
});

const { t } = useI18n();

const activeKey = ref<'api' | 'metric' | 'overlay'>('api');

const currIndex = ref(props.cvsKeys[0]);
const currApi = ref('Total');

const xData = ref<string[]>([]);
const seriesData = ref<{name:string, data:number[], key:string}[]>([]);
const sliderValue = ref<[number, number]>([0, 0]);
const sliderMin = ref<number>(0);
const sliderMax = ref<number>(0);
const isSliding = ref(false);

// 设置操作更新图表数据
const setUpdateSeriesData = () => {
  if (props.isSingleInterface) {
    setApiseriesData();
  } else {
    if (activeKey.value === 'metric') {
      setIndexSeriesData();
    } else if (activeKey.value === 'api') {
      setApiseriesData();
    } else {
      setCharData(checkboxGroupValue.value);
    }
  }
};

const setApiseriesData = () => {
  const resulet:{ name: string; data: number[];key:string }[] = [];
  for (let i = 0; i < props.cvsKeys.length; i++) {
    const cvskey = props.cvsKeys[i];
    const apiObjIndexList = props.indexDimensionObj[cvskey][currApi.value];
    resulet.push({
      key: cvskey,
      name: allCvsNames?.[cvskey] || '',
      data: apiObjIndexList.slice(sliderValue.value[0], sliderValue.value[1] + 1)
    });
  }
  seriesData.value = resulet;
};

const setIndexSeriesData = () => {
  const resulet:{ name: string; data: number[];key:string }[] = [];
  for (let i = 0; i < props.apiNames.length; i++) {
    const name = props.apiNames[i];
    const indexObjApiList = props.apiDimensionObj[name][currIndex.value];
    resulet.push({
      key: name,
      name,
      data: indexObjApiList.slice(sliderValue.value[0], sliderValue.value[1] + 1)
    });
  }
  seriesData.value = resulet;
};

const tableData = ref<Record<string, any>[]>([]);
const getTableData = (mappings:{[key:string]:string[]}, keys:string[], dataSource:{[key:string]:{[key:string]:number}}) => {
  const result:any = [];
  if (props.isSingleInterface) {
    if (Object.prototype.hasOwnProperty.call(dataSource, 'Total')) {
      result.push({ name: 'Total', ...dataSource.Total });
    }

    return result;
  }

  if (!mappings || !Object.keys(mappings)?.length || !keys?.length) {
    return [];
  }
  for (const key in mappings) {
    const item = { name: key, list: [] };
    keys.reduce((prev, cur) => {
      prev[cur] = isNaN(Number(dataSource?.[key]?.[cur])) ? '--' : dataSource[key][cur];
      return prev;
    }, item);
    if (mappings[key].length) {
      item.list = mappings[key].map(_key => {
        const listItem = { name: _key };
        return keys.reduce((prev, cur) => {
          prev[cur] = isNaN(Number(dataSource?.[_key]?.[cur])) ? '--' : dataSource[_key][cur];
          return prev;
        }, listItem);
      });
    }
    result.push(item);
  }
  if (dataSource === undefined) {
    return [];
  }
  return result;
};

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
  }

  if (!isSliding.value) {
    xData.value = JSON.parse(JSON.stringify(newVal));
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

  xData.value = props.timestampData.slice(value[0], value[1] + 1);
  setUpdateSeriesData();
};

const checkboxGroupValue = ref<string[]>([]);
const CheckboxGroupChange = (values:string[]) => {
  checkboxGroupValue.value = values;
  setCharData(values);
};

let isFirstLoadMetric = true;
const radioGroupChange = (e) => {
  xData.value = props.timestampData.slice(sliderValue.value[0], sliderValue.value[1] + 1);

  if (e.target.value === 'metric') {
    setIndexSeriesData();
  } else if (e.target.value === 'api') {
    setApiseriesData();
  } else {
    if (isFirstLoadMetric) {
      seriesData.value = [];
      for (const cvsKey of props.cvsKeys) {
        const name = `Total-${allCvsNames[cvsKey]}`;
        checkboxGroupValue.value.push(`Total-${cvsKey}`);
        const data = props.indexDimensionObj[cvsKey].Total.slice(sliderValue.value[0], sliderValue.value[1] + 1);
        seriesData.value.push({ name, data, key: name });
      }
      isFirstLoadMetric = false;
    } else {
      setCharData(checkboxGroupValue.value);
    }
  }
};

const setCharData = (values:string[]) => {
  seriesData.value = [];
  for (let i = 0; i < values.length; i++) {
    const [apiName, cvsKey] = values[i].split('-');
    const data = isSliding.value ? props.indexDimensionObj[cvsKey][apiName].slice(sliderValue.value[0], sliderValue.value[1] + 1) : props.indexDimensionObj[cvsKey][apiName];
    const name = `${apiName}-${allCvsNames[cvsKey]}`;
    seriesData.value.push({ name, data, key: name });
  }
};

const currIndexChange = () => {
  setIndexSeriesData();
};

const currApiChange = () => {
  setApiseriesData();
};

const sliderFormatter = (value: number) => {
  return props.timestampData[value];
};

const metricOptions = computed(() => {
  return props.apiNames.map(item => ({ label: item, value: item }));
});

const tableColumns = computed(() => {
  return props.isSingleInterface ? props.columns.filter(item => item.dataIndex !== 'ops' && item.dataIndex !== 'operations') : props.columns;
});

const chartRef = ref();
const resizeChart = () => {
  nextTick(() => {
    if (chartRef.value) {
      chartRef.value.resizeChart();
    }
  });
};

defineExpose({
  resizeChart
});
</script>
<template>
  <div style="width: 100%; min-height: 100%;">
    <div class="flex items-center text-3 pl-10 pr-15" :class="props.isSingleInterface?'justify-end':'justify-between'">
      <RadioGroup
        v-if="!isSingleInterface"
        v-model:value="activeKey"
        buttonStyle="solid"
        size="small"
        class="whitespace-nowrap"
        @change="radioGroupChange">
        <RadioButton value="api">{{ t('ftpPlugin.performanceTestDetail.countTemplate.radioButtons.byApi') }}</RadioButton>
        <RadioButton value="metric">{{ t('ftpPlugin.performanceTestDetail.countTemplate.radioButtons.byMetric') }}</RadioButton>
        <RadioButton value="overlay">{{ t('ftpPlugin.performanceTestDetail.countTemplate.radioButtons.overlayAnalysis') }}</RadioButton>
      </RadioGroup>
      <template v-if="!isSingleInterface">
        <Select
          v-if="activeKey === 'metric'"
          v-model:value="currIndex"
          class="w-60"
          :options="indexOptions"
          @change="currIndexChange" />
        <Select
          v-else-if="activeKey === 'api'"
          v-model:value="currApi"
          class="w-60"
          :options="metricOptions"
          @change="currApiChange" />
      </template>
    </div>
    <LineChart
      ref="chartRef"
      :series="seriesData"
      :xData="xData"
      :selectedIndex="currIndex"
      :tabKey="props.tabKey"
      :brpsUnit="props.brpsUnit"
      :bwpsUnit="props.bwpsUnit"
      class="mt-2" />
    <div v-if="props.timestampData.length >=2" class="-mt-5 pl-10 pr-12">
      <Slider
        :value="sliderValue"
        :min="sliderMin"
        :max="sliderMax"
        :tipFormatter="sliderFormatter"
        range
        @change="sliderChange" />
    </div>
    <template v-if="!isSingleInterface && activeKey === 'overlay'">
      <div class="pl-17 text-3 mt-3.5">
        <CheckboxGroup :value="checkboxGroupValue" @change="CheckboxGroupChange">
          <div
            v-for="apiName in props.apiNames"
            :key="apiName"
            class="flex">
            <div class="w-60 truncate" :title="apiName">{{ apiName }}</div>
            <div class="space-x-5">
              <Checkbox
                v-for="metric in props.indexOptions"
                :key="apiName+'-'+metric.value"
                :value="apiName+'-'+metric.value"
                :disabled="(!checkboxGroupValue.includes(apiName+'-'+metric.value) && checkboxGroupValue.length >=10) || (checkboxGroupValue.includes(apiName+'-'+metric.value) && checkboxGroupValue.length === 1)">
                {{ metric.label }}
              </Checkbox>
            </div>
          </div>
        </CheckboxGroup>
      </div>
    </template>
    <InfoTable
      class="mt-8"
      :brpsUnit="props.brpsUnit"
      :bwpsUnit="props.bwpsUnit"
      :columns="tableColumns"
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
  border-color: var(--border-divider);
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
