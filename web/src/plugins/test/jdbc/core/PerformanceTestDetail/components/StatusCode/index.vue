<script setup lang="ts">
import { ref, defineAsyncComponent, watch, nextTick } from 'vue';
import { NoData } from '@xcan-angus/vue-ui';

const GroupBarChart = defineAsyncComponent(() => import('./GroupBarChart.vue'));
const OneGroupBarChart = defineAsyncComponent(() => import('./OneGroupBarChart.vue'));
const PirChart = defineAsyncComponent(() => import('./PirChart.vue'));

interface Props {
  isSingleInterface:boolean;
  stutasCodeData:Record<string, any>;
}

const props = withDefaults(defineProps<Props>(), {
  isSingleInterface: false,
  stutasCodeData: undefined
});

const chartData = ref({
  xData: [],
  yData: []
});

const totalData = ref<{name:string, value:number}[]>([]);

const codeEnums = ['2xx', '3xx', '4xx', '5xx', 'Exception'];

const getColor = (name:string) => {
  switch (name) {
    case '1xx' :
      return 'rgba(103, 215, 255, 1)';
    case '2xx' :
      return 'rgba(82, 196, 26, 1)';
    case '3xx' :
      return 'rgba(45, 142, 255, 1)';
    case '4xx' :
      return 'rgba(255, 215, 0, 1)';
    case '5xx' :
      return 'rgba(255, 99, 71, 1)';
    case 'Exception' :
      return 'rgb(255, 0, 0, 1)';
  }
};

const getXDataAndYData = (data) => {
  if (!data) {
    return { xData: [], yData: [] };
  }
  const keys = Object.keys(data);
  if (!keys.length) {
    return { xData: [], yData: [] };
  }

  if (props.isSingleInterface) {
    const xData = Object.keys(data[keys[0]]);
    const yData = Object.values(data[keys[0]])?.map(item => item ? +item : 0) || [];
    return { xData, yData };
  } else {
    const xData = Object.keys(data);
    const yData = [];
    for (let i = 0; i < codeEnums.length; i++) {
      const name = codeEnums[i];
      const obj = {
        name,
        data: []
      };
      for (let j = 0; j < keys.length; j++) {
        const key = keys[j];
        const _value = data[key]?.[name] || 0;
        obj.data.push(_value ? +_value : 0);
      }
      yData.push(obj);
    }

    return { xData, yData };
  }
};

watch(() => props.stutasCodeData, (newValue) => {
  chartData.value = getXDataAndYData(newValue);
  if (newValue.Total) {
    totalData.value = codeEnums.map((item) => {
      return {
        name: item,
        value: Number(newValue.Total?.[item]) || 0,
        color: getColor(item)
      };
    }) || [];
  }
}, {
  immediate: true
});

const oneGroupBarChartRef = ref();
const groupBarChartRef = ref();
const pirChartRef = ref();
const resizeChart = () => {
  nextTick(() => {
    if (oneGroupBarChartRef.value) {
      oneGroupBarChartRef.value.resizeChart();
    }
    if (groupBarChartRef.value) {
      groupBarChartRef.value.resizeChart();
    }
    if (pirChartRef.value) {
      pirChartRef.value.resizeChart();
    }
  });
};

defineExpose({
  resizeChart
});
</script>
<template>
  <template v-if="chartData.xData.length">
    <div class="h-full flex flex-col justify-center">
      <div class="text-3 text-text-content w-full h-75 flex items-center  justify-center">
        <template v-if="props.isSingleInterface">
          <div class="h-full w-full flex" style="max-width:1000px">
            <OneGroupBarChart
              ref="oneGroupBarChartRef"
              :xData="chartData.xData"
              :yData="totalData" />
          </div>
        </template>
        <template v-else>
          <div class="pb-2 h-full w-80">
            <PirChart ref="pirChartRef" :dataSource="totalData" />
          </div>
          <div class="overflow-x-auto pb-2 h-full flex-1 ml-5">
            <GroupBarChart
              ref="groupBarChartRef"
              :colors="totalData.map(item=>item.color)"
              :xData="chartData.xData"
              :yData="chartData.yData" />
          </div>
        </template>
      </div>
    </div>
  </template>
  <template v-else>
    <NoData class="h-full" />
  </template>
</template>
