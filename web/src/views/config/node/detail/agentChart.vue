<script setup lang="ts">
import { defineAsyncComponent, ref } from 'vue';
import { IntervalTimestamp } from '@xcan-angus/vue-ui';
import { TESTER } from '@xcan-angus/infra';

const CacheChart = defineAsyncComponent(() => import('@/components/chart/mock/cacheChart.vue'));
const RamChart = defineAsyncComponent(() => import('@/components/chart/mock/ramChart.vue'));
const CpuChart = defineAsyncComponent(() => import('@/components/chart/mock/cpuChart.vue'));

interface Props {
  id:string;
}

const props = withDefaults(defineProps<Props>(), {
  id: undefined
});

const cacheChartData = ref<{
  xData: string[],
  numData: number[],
  totalData: number[],
  usedData: number[],
  maxTotalData:number;
  maxNumData:number;
}>({
  xData: [],
  numData: [],
  totalData: [],
  usedData: [],
  maxTotalData: 0,
  maxNumData: 0
});

const ramChartData = ref<{
  xData: string[],
  submitData: number[],
  totalData: number[],
  usedData: number[]
}>({
  xData: [],
  submitData: [],
  totalData: [],
  usedData: []
});

const cpuChartData = ref<{
  xData: string[],
  totalData: number[],
  systemCpuUsed: number[],
  processCpuUsed: number[]
}>({
  xData: [],
  totalData: [],
  systemCpuUsed: [],
  processCpuUsed: []
});

const getChartData = async (data, params) => {
  cacheChartData.value = {
    xData: [],
    numData: [0, 0],
    totalData: [0, 0],
    usedData: [0, 0],
    maxTotalData: 0,
    maxNumData: 0
  };

  ramChartData.value = {
    xData: [],
    submitData: [0, 0],
    totalData: [0, 0],
    usedData: [0, 0]
  };

  cpuChartData.value = {
    xData: [],
    totalData: [0, 0],
    systemCpuUsed: [0, 0],
    processCpuUsed: [0, 0]
  };

  if (!data.length) {
    const startTime = params.filters.filter(item => item.key === 'timestamp' && item.op === 'GREATER_THAN_EQUAL')[0].value;
    const endTime = params.filters.filter(item => item.key === 'timestamp' && item.op === 'LESS_THAN_EQUAL')[0].value;
    const _tiemData = [startTime.replace(' ', '\n'), endTime.replace(' ', '\n')];
    cacheChartData.value.xData = _tiemData;
    ramChartData.value.xData = _tiemData;
    cpuChartData.value.xData = _tiemData;
    return;
  }

  cacheChartData.value = {
    xData: [],
    numData: [],
    totalData: [],
    usedData: [],
    maxTotalData: 0,
    maxNumData: 0
  };

  ramChartData.value = {
    xData: [],
    submitData: [],
    totalData: [],
    usedData: []
  };

  cpuChartData.value = {
    xData: [],
    totalData: [],
    systemCpuUsed: [],
    processCpuUsed: []
  };

  for (let i = 0; i < data.length; i++) {
    const item = data[i];
    const values = item.cvsJvm.split(',');

    cacheChartData.value.xData.push(item.timestamp.replace(' ', '\n'));
    ramChartData.value.xData.push(item.timestamp.replace(' ', '\n'));
    cpuChartData.value.xData.push(item.timestamp.replace(' ', '\n'));

    const numValue = +values[0];
    cacheChartData.value.numData.push(numValue);
    if (numValue > cacheChartData.value.maxNumData) {
      cacheChartData.value.maxNumData = numValue;
    }

    const usedValue = parseFloat(values[1]) / 1024 / 1024 / 1024;
    cacheChartData.value.usedData.push(+usedValue.toFixed(2));

    const totalValue = parseFloat(values[2]) / 1024 / 1024 / 1024;
    cacheChartData.value.totalData.push(+totalValue.toFixed(2));
    if (totalValue > cacheChartData.value.maxTotalData) {
      cacheChartData.value.maxTotalData = totalValue;
    }

    cacheChartData.value.numData.push(numValue);
    if (numValue > cacheChartData.value.maxNumData) {
      cacheChartData.value.maxNumData = numValue;
    }

    const ramUsedData = parseFloat(values[3]) / 1024 / 1024 / 1024;
    ramChartData.value.usedData.push(+ramUsedData.toFixed(2));

    const ramSubmitData = parseFloat(values[4]) / 1024 / 1024 / 1024;
    ramChartData.value.submitData.push(+ramSubmitData.toFixed(2));

    const ramtotaltData = parseFloat(values[5]) / 1024 / 1024 / 1024;
    ramChartData.value.totalData.push(+ramtotaltData.toFixed(2));

    const cpu = item.cvsProcessor.split(',');
    cpuChartData.value.totalData.push(+cpu[0]);
    cpuChartData.value.systemCpuUsed.push(+cpu[1]);
    cpuChartData.value.processCpuUsed.push(+cpu[2]);
  }
};

</script>
<template>
  <div class="font-medium text-3 mt-5">
    进程监控
  </div>
  <div class="flex justify-end pr-15">
    <IntervalTimestamp
      :action="`${TESTER}/mock/service/${props.id}/metrics`"
      @change="getChartData" />
  </div>
  <div class="flex h-60 space-x-2 mt-2">
    <CacheChart
      class="w-1/3"
      :xData="cacheChartData.xData"
      :numData="cacheChartData.numData"
      :totalData="cacheChartData.totalData"
      :usedData="cacheChartData.usedData"
      :maxNumData="cacheChartData.maxNumData"
      :maxTotalData="cacheChartData.maxTotalData" />
    <RamChart
      class="w-1/3"
      :xData="ramChartData.xData"
      :submitData="ramChartData.submitData"
      :totalData="ramChartData.totalData"
      :usedData="ramChartData.usedData" />
    <CpuChart
      class="w-1/3"
      :xData="cpuChartData.xData"
      :totalData="cpuChartData.totalData"
      :systemCpuUsed="cpuChartData.systemCpuUsed"
      :processCpuUsed="cpuChartData.processCpuUsed" />
  </div>
</template>
