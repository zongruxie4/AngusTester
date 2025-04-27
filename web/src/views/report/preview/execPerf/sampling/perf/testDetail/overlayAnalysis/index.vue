<script setup lang="ts">
import { defineAsyncComponent, onMounted, ref, watch } from 'vue';
import { cloneDeep } from 'lodash-es';

const LineStackChart = defineAsyncComponent(() => import('@/views/report/preview/execPerf/sampling/perf/testDetail/lineStackChart/index.vue'));

interface Props {
  timestampList: string[];
  names: string[];
  dataMap: {
    Total: {
      threadPoolActiveSize: string[];
      tps: string[];
      tranP90: string[];
      errorRate: string[];
    }
  };
}

const props = withDefaults(defineProps<Props>(), {
  timestampList: () => [],
  names: () => [],
  dataMap: undefined
});

const xAxis = ref<{ data: string[] }>({ data: [] });
const series = ref<{ name: string; data: string[] }[]>([]);

const setSeries = (data: { Total: {
  threadPoolActiveSize: string[];
  tps: string[];
  tranP90: string[];
  errorRate: string[];
 } }) => {
  if (!data) {
    return;
  }

  const totalItem = data.Total;
  const result: { name: string; data: string[] }[] = [];
  result.push({
    name: '吞吐量 - Total - 每秒事务数',
    data: totalItem.tps
  });

  result.push({
    name: '并发数 - Total - 活跃线程数',
    data: totalItem.threadPoolActiveSize
  });

  result.push({
    name: '响应时间 - Total - P90',
    data: totalItem.tranP90
  });

  result.push({
    name: '错误 - Total - 错误率',
    data: totalItem.errorRate
  });

  series.value = result;
};

const reset = () => {
  xAxis.value = { data: [] };
  series.value = [];
};

onMounted(() => {
  watch([() => props.dataMap, () => props.timestampList], ([dataMap, timestampList]) => {
    reset();

    if (!timestampList.length) {
      return;
    }

    xAxis.value = { data: cloneDeep(timestampList) };
    setSeries(dataMap);
  }, { immediate: true });
});
</script>
<template>
  <LineStackChart
    :series="series"
    :xAxis="xAxis"
    :gridTop="50"
    style="height:320px;" />
</template>
