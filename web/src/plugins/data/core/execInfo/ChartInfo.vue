<script setup lang="ts">
import { ref, onMounted, defineAsyncComponent, onBeforeUnmount, computed } from 'vue';
import { RadioGroup, RadioButton, Slider } from 'ant-design-vue';
import { Spin, NoData} from '@xcan-angus/vue-ui';
import dayjs from 'dayjs';
import { exec } from '@/api/tester';

import apiUtils from '@/utils/ApiUtils/index';
import { ListData, useExecCount } from './useExecCount';
import { allCvsKeys, allResponseTimeColumns, allErrorsColumns, allErrorRateColumns, allRowsColumns } from './ChartConfig';
// allColumns, throughputCvsKeys, throughputOptions, throughputColumns, threadCvsKeys, threadOptions, threadColumns, responseTimeCvsKeys, responseTimeOptions, responseTimeColumns, errorOptions, errorCvsKeys, errorColumns
interface Props {
  dataSource: { [key: string]: any };
}

const props = withDefaults(defineProps<Props>(), {
  dataSource: () => ({})
});

const LineChart = defineAsyncComponent(() => import('./LineChart.vue'));
const InfoTable = defineAsyncComponent(() => import('./InfoTable.vue'));
const { convertCvsValue } = useExecCount();
const pagination = {
  pageNo: 1,
  pageSize: 500
};

const groupItem = [
  {
    label: '按吞吐量',
    value: 'tps'
  },
  {
    label: '按响应时间',
    value: 'time'
  },
  {
    label: '按行数',
    value: 'rows'
  },
  // {
  //   label: '按错误数',
  //   value: 'errorNum'
  // },
  // {
  //   label: '按错误率',
  //   value: 'errorRate'
  // },
  {
    label: '按上传',
    value: 'upload'
  }
];

const echartsLegendConfig = {
  writeBytes: '写数据大小',
  bwps: '写数据速率',
  transactions: '行数',
  errors: '错误数',
  errorRate: '错误率',
  iterations: '迭代数',
  tps: '每秒行数',
  tranMean: '平均',
  tranMin: '最小',
  tranMax: '最大',
  tranP50: 'P50',
  tranP75: 'P75',
  tranP90: 'P90',
  tranP95: 'P95',
  tranP99: 'P99',
  tranP999: 'P999'
};

const loading = ref(false);
const activeType = ref('tps');

const xData = ref<string[]>([]);
const seriesData = ref<{name: string, data: number[]}[]>([]);
const sliderValue = ref<number[]>([0, 0]);

const times = ref<string[]>([]);
const chartSourceData = ref<ListData[]>([]);

const writeBytesUnit = ref<'KB' | 'MB'>('KB');
const bwpsUnit = ref<'KB' | 'MB'>('KB');

let timerDuration = 5000;
const loaded = ref(false);

const writeBytesNumbers: number[] = [];
const bwpsNumbers: number[] = [];
const handleNumber = (key: string, value: number) => {
  if (key === 'writeBytes') {
    writeBytesNumbers.push(value);
  } else if (key === 'bwps') {
    bwpsNumbers.push(value);
  }
};

const loadChartData = async () => {
  const { id } = props.dataSource;
  loading.value = true;
  // const [error, { data }] = await exec.getAggregateData(id, { ...pagination });
  const [error, { data }] = await exec.getSampleSummaryList(id, { ...pagination });
  if (error) {
    return;
  }
  if (pagination.pageNo === 1) {
    chartSourceData.value = convertCvsValue(data.list || [], allCvsKeys, handleNumber);
  } else {
    chartSourceData.value.push(...convertCvsValue(data.list || [], allCvsKeys, handleNumber));
  }
  if (chartSourceData.value.length < +data.total) {
    pagination.pageNo += 1;
    await loadChartData();
    return;
  }
  loaded.value = true;
  loading.value = false;
  const maxWriteBytes = Math.max(...writeBytesNumbers);
  if (maxWriteBytes / 1024 > 1000) {
    writeBytesUnit.value = 'MB';
  }
  const maxBwps = Math.max(...bwpsNumbers);
  if (maxBwps / 1024 > 1000) {
    bwpsUnit.value = 'MB';
  }
  chartSourceData.value.forEach(i => {
    times.value.push(i.timestamp);
    if (writeBytesUnit.value === 'KB') {
      i.values[0].writeBytes = +(i.values[0].writeBytes as number / 1024).toFixed(2);
    } else {
      i.values[0].writeBytes = +(i.values[0].writeBytes as number / 1024 / 1024).toFixed(2);
    }
    if (bwpsUnit.value === 'KB') {
      i.values[0].bwps = +(i.values[0].bwps as number / 1024).toFixed(2);
    } else {
      i.values[0].bwps = +(i.values[0].bwps as number / 1024 / 1024).toFixed(2);
    }
  });

  // times.value = chartSourceData.value.map(i => i.timestamp);
  // xData.value = times.value;
  sliderValue.value = [0, times.value.length - 1];
  if (!props.dataSource.endDate && ['PENDING', 'RUNNING'].includes(props.dataSource?.status?.value)) {
    const [interval, unit] = apiUtils.splitDuration(props.dataSource.reportInterval);
    if (unit === 's') {
      timerDuration = +interval * 1000;
    }
    if (unit === 'min') {
      timerDuration = +interval * 1000 * 60;
    }
    if (unit === 'h') {
      timerDuration = +interval * 1000 * 60 * 60;
    }
    if (timerDuration < 5000) {
      timerDuration = 5000;
    }
    interTimeGetChartData();
  } else {
    const dataLength = chartSourceData.value.length;
    if (dataLength > 2 && chartSourceData.value[dataLength - 1].timestamp === chartSourceData.value[dataLength - 2].timestamp) {
      chartSourceData.value.splice(dataLength - 2);
    }
  }
  setChartData();
};

const setChartData = () => {
  let oneApiTableKey: string[] = [];
  if (activeType.value === 'time') {
    oneApiTableKey = ['tranMean', 'tranMin', 'tranMax', 'tranP50', 'tranP75', 'tranP90', 'tranP95', 'tranP99', 'tranP999'];
  }
  if (activeType.value === 'rows') {
    oneApiTableKey = ['iterations', 'transactions', 'errors'];
  }
  if (activeType.value === 'errorNum') {
    oneApiTableKey = ['errors'];
  }
  if (activeType.value === 'errorRate') {
    oneApiTableKey = ['errorRate'];
  }
  if (activeType.value === 'tps') {
    oneApiTableKey = ['tps', 'errorRate'];
  }
  if (activeType.value === 'upload') {
    oneApiTableKey = ['writeBytes', 'bwps'];
  }
  const yData:{name: string, data: number[]}[] = [];
  const showData = chartSourceData.value.filter((_i, idx) => idx >= sliderValue.value[0] && idx <= sliderValue.value[1]);
  xData.value = times.value.filter((_i, idx) => idx >= sliderValue.value[0] && idx <= sliderValue.value[1]);
  oneApiTableKey.forEach(key => {
    const data = showData.map(item => item.values[0][key]);
    yData.push({
      name: echartsLegendConfig[key] || key,
      data: data
    });
  });
  seriesData.value = yData;
};

const loadChartDataInDuration = async () => {
  const { id } = props.dataSource;
  const startDate = times.value[times.value.length - 1] || props.dataSource.actualStartDate;
  const startTime = dayjs(startDate).format('YYYY-MM-DD HH:mm:ss');
  const [error, { data }] = await exec.getSampleSummaryList(id, {
    pageNo: 1,
    pageSize: 100,
    filters: [
      { key: 'timestamp', op: 'GREATER_THAN_EQUAL', value: startTime }]
  });
  if (error) {
    return;
  }
  if (data.list?.length && chartSourceData.value.length && data.list[0].timestamp === chartSourceData.value[chartSourceData.value.length - 1]?.timestamp) {
    data.list.splice(0, 1);
  }
  if (!data.list?.length) {
    interTimeGetChartData();
    return;
  }

  const chartData = convertCvsValue(data.list || [], allCvsKeys, handleNumber);
  if (writeBytesUnit.value === 'KB' || bwpsUnit.value === 'KB') {
    let writeBytesChange = false;
    let bwpsUnitChange = false;
    if (writeBytesUnit.value === 'KB') {
      const maxWriteBytes = Math.max(...writeBytesNumbers);
      if (maxWriteBytes / 1024 > 1000) {
        writeBytesUnit.value = 'MB';
        writeBytesChange = true;
      }
    }
    if (bwpsUnit.value === 'KB') {
      const maxBwps = Math.max(...bwpsNumbers);
      if (maxBwps / 1024 > 1000) {
        bwpsUnit.value = 'MB';
        bwpsUnitChange = true;
      }
    }
    chartSourceData.value.forEach(i => {
      if (writeBytesChange) {
        i.values[0].writeBytes = +(i.values[0].writeBytes as number / 1024).toFixed(2);
      }
      if (bwpsUnitChange) {
        i.values[0].bwps = +(i.values[0].bwps as number / 1024).toFixed(2);
      }
    });
  }
  chartData.forEach(i => {
    if (bwpsUnit.value === 'KB') {
      i.values[0].bwps = +(i.values[0].bwps as number / 1024).toFixed(2);
    } else {
      i.values[0].bwps = +(i.values[0].bwps as number / 1024 / 1024).toFixed(2);
    }
    if (writeBytesUnit.value === 'KB') {
      i.values[0].writeBytes = +(i.values[0].writeBytes as number / 1024).toFixed(2);
    } else {
      i.values[0].writeBytes = +(i.values[0].writeBytes as number / 1024 / 1024).toFixed(2);
    }
  });
  chartSourceData.value.push(...chartData);
  const dataLength = chartSourceData.value.length;
  if (dataLength > 2 && chartSourceData.value[dataLength - 1].timestamp === chartSourceData.value[dataLength - 2].timestamp) {
    chartSourceData.value.splice(dataLength - 2, 1);
    return;
  }
  if (!sliderValue.value.length) {
    times.value = chartSourceData.value.map(i => i.timestamp);
    sliderValue.value = [0, times.value.length - 1];
  } else if (sliderValue.value[1] === times.value.length - 1) {
    times.value = chartSourceData.value.map(i => i.timestamp);
    sliderValue.value[1] = times.value.length - 1;
  } else {
    times.value = chartSourceData.value.map(i => i.timestamp);
  }
  setChartData();
  // xData.value = times.value;
  interTimeGetChartData();
};

const columns = computed(() => {
  if (activeType.value === 'time') {
    return allResponseTimeColumns;
  }
  if (activeType.value === 'errorNum') {
    return allErrorsColumns;
  }
  if (activeType.value === 'errorRate') {
    return allErrorRateColumns;
  }
  if (activeType.value === 'upload') {
    // return allUploadColumns;
    return [
      {
        title: '名称',
        dataIndex: 'name',
        ellipsis: true
      },
      {
        title: '写数据大小',
        dataIndex: 'writeBytes',
        ellipsis: true
      },
      {
        title: '写数据速率',
        dataIndex: 'bwps',
        ellipsis: true
      }
    ];
  }
  if (activeType.value === 'tps') {
    return [
      {
        title: '名称',
        dataIndex: 'name',
        ellipsis: true
      },
      {
        title: '每秒行数',
        dataIndex: 'tps',
        width: '33%'
      },
      {
        title: '错误率',
        dataIndex: 'errorRate',
        width: '33%'
      }
    ];
  }
  if (activeType.value === 'rows') {
    return allRowsColumns;
  }
  return [];
});

const tableData = computed(() => {
  if (chartSourceData.value.length) {
    return chartSourceData.value[sliderValue.value[1]].values;
  }
  return [];
});

const onActiveTypeChange = () => {
  setChartData();
};

const onSliderChange = () => {
  setChartData();
};

let timer;
const interTimeGetChartData = async () => {
  if (timer) {
    clearTimeout(timer);
    timer = null;
  }
  if (['PENDING', 'RUNNING'].includes(props.dataSource?.status?.value)) {
    timer = setTimeout(() => {
      loadChartDataInDuration();
    }, timerDuration);
  }
};

onMounted(() => {
  loadChartData();
});

onBeforeUnmount(() => {
  if (timer) {
    clearTimeout(timer);
  }
});

defineExpose({
  restart: () => {
    pagination.pageNo = 1;
    loaded.value = false;
    times.value = [];
    xData.value = [];
    loadChartData();
    if (timer) {
      clearTimeout(timer);
    }
  }
});
</script>
<template>
  <NoData v-if="!times.length && loaded" class="!my-30" />
  <div v-else>
    <Spin :spinning="loading" :delay="0">
      <RadioGroup
        v-model:value="activeType"
        buttonStyle="solid"
        size="small"
        class="ml-10"
        @change="onActiveTypeChange">
        <RadioButton
          v-for="item in groupItem"
          :key="item.value"
          :value="item.value">
          {{ item.label }}
        </RadioButton>
      </RadioGroup>
      <LineChart
        :xData="xData"
        :series="seriesData"
        type="throughput"
        :bwpsUnit="bwpsUnit"
        :writeBytesUnit="writeBytesUnit" />
      <div v-if="times.length > 1" class="-mt-5 pl-10 pr-12">
        <Slider
          v-model:value="sliderValue"
          :min="0"
          :max="times.length - 1 > 0 ? times.length - 1 : sliderValue[1]"
          :tipFormatter="(value) => chartSourceData.length ? chartSourceData[value]?.timestamp : ''"
          range
          @change="onSliderChange" />
      </div>
      <InfoTable
        class="mt-5"
        :columns="columns"
        :tableData="tableData"
        :bwpsUnit="bwpsUnit"
        :writeBytesUnit="writeBytesUnit" />
    </Spin>
  </div>
</template>
<style scoped>
:deep(.ant-slider-rail){
  height: 10px;
  border-radius:1px;
  background-color: rgba(0, 0, 0, 0%);

  @apply border border-border-divider;
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
