<script setup lang="ts">
import { computed, defineAsyncComponent, onMounted, ref, watch } from 'vue';
import { cloneDeep } from 'lodash-es';
import { utils } from '@xcan-angus/tools';

const LineStackChart = defineAsyncComponent(() => import('@/views/report/preview/execPerf/sampling/perf/testDetail/lineStackChart/index.vue'));

interface Props {
  timestampList: string[];
  names: string[];
  dataMap: {
    [key: string]: {
      tranMean: string[];
      tranMin: string[];
      tranMax: string[];
      tranP50: string[];
      tranP75: string[];
      tranP90: string[];
      tranP95: string[];
      tranP99: string[];
      tranP999: string[];
    }
  };
}

const props = withDefaults(defineProps<Props>(), {
  timestampList: () => [],
  names: () => [],
  dataMap: () => ({})
});

const xAxis = ref<{ data: string[] }>({ data: [] });
const series = ref<{ name: string; data: string[] }[]>([]);

const setSeries = (data: {
  [key: string]: {
    tranMean: string[];
    tranMin: string[];
    tranMax: string[];
    tranP50: string[];
    tranP75: string[];
    tranP90: string[];
    tranP95: string[];
    tranP99: string[];
    tranP999: string[];
  }
}, names: string[]) => {
  const result: { name: string; data: string[] }[] = [];
  for (let i = 0, len = names.length; i < len; i++) {
    const name = names[i];
    result.push({
      name: name + ' - 平均',
      data: data[name].tranMean
    });
    result.push({
      name: name + ' - 最小',
      data: data[name].tranMin
    });
    result.push({
      name: name + ' - 最大',
      data: data[name].tranMax
    });
    result.push({
      name: name + ' - P50',
      data: data[name].tranP50
    });
    result.push({
      name: name + ' - P75',
      data: data[name].tranP75
    });
    result.push({
      name: name + ' - P90',
      data: data[name].tranP90
    });
    result.push({
      name: name + ' - P95',
      data: data[name].tranP95
    });
    result.push({
      name: name + ' - P99',
      data: data[name].tranP99
    });
    result.push({
      name: name + ' - P999',
      data: data[name].tranP999
    });
  }

  series.value = result;
};

const reset = () => {
  xAxis.value = { data: [] };
  series.value = [];
};

onMounted(() => {
  watch([() => props.dataMap, () => props.names, () => props.timestampList], ([dataMap = {}, names = [], timestampList]) => {
    reset();

    if (!timestampList.length) {
      return;
    }

    xAxis.value = { data: cloneDeep(timestampList) };
    setSeries(dataMap, names);
  }, { immediate: true });
});

const tableData = computed(() => {
  const names = props.names || [];
  const dataMap = props.dataMap || {};
  return names.map((item) => {
    const _data = dataMap[item] || {};
    return {
      id: utils.uuid(),
      name: item,
      tranMean: _data.tranMean[_data.tranMean.length - 1],
      tranMin: _data.tranMin[_data.tranMin.length - 1],
      tranMax: _data.tranMax[_data.tranMax.length - 1],
      tranP50: _data.tranP50[_data.tranP50.length - 1],
      tranP75: _data.tranP75[_data.tranP75.length - 1],
      tranP90: _data.tranP90[_data.tranP90.length - 1],
      tranP95: _data.tranP95[_data.tranP95.length - 1],
      tranP99: _data.tranP99[_data.tranP99.length - 1],
      tranP999: _data.tranP999[_data.tranP999.length - 1]
    };
  });
});
</script>
<template>
  <div>
    <LineStackChart
      :series="series"
      :xAxis="xAxis"
      :gridTop="180"
      style="height: 450px;" />

    <div v-if="!!tableData?.length" class="mt-7 border border-solid border-border-input">
      <div class="flex border-b border-solid border-border-input">
        <div
          class="flex-1 flex-shrink-0 bg-blue-table px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
          <span>名称</span>
        </div>
        <div
          class="w-16 flex-shrink-0 bg-blue-table px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
          <span>平均</span>
        </div>
        <div
          class="w-16 flex-shrink-0 bg-blue-table px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
          <span>最小</span>
        </div>
        <div
          class="w-16 flex-shrink-0 bg-blue-table px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
          <span>最大</span>
        </div>
        <div
          class="w-16 flex-shrink-0 bg-blue-table px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
          <span>P50</span>
        </div>
        <div
          class="w-16 flex-shrink-0 bg-blue-table px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
          <span>P75</span>
        </div>
        <div
          class="w-16 flex-shrink-0 bg-blue-table px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
          <span>P90</span>
        </div>
        <div
          class="w-16 flex-shrink-0 bg-blue-table px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
          <span>P95</span>
        </div>
        <div
          class="w-16 flex-shrink-0 bg-blue-table px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
          <span>P99</span>
        </div>
        <div class="w-16 flex-shrink-0 bg-blue-table px-1.5 py-1.5 break-all whitespace-pre-wrap">
          <span>P999</span>
        </div>
      </div>

      <div
        v-for="item in tableData"
        :key="item.id"
        class="flex border-b border-solid border-border-input last:border-0">
        <div
          class="flex-1 flex-shrink-0 px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
          {{ item.name }}
        </div>
        <div
          class="w-16 flex-shrink-0 px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
          {{ item.tranMean }}
        </div>
        <div
          class="w-16 flex-shrink-0 px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
          {{ item.tranMin }}
        </div>
        <div
          class="w-16 flex-shrink-0 px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
          {{ item.tranMax }}
        </div>
        <div
          class="w-16 flex-shrink-0 px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
          {{ item.tranP50 }}
        </div>
        <div
          class="w-16 flex-shrink-0 px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
          {{ item.tranP75 }}
        </div>
        <div
          class="w-16 flex-shrink-0 px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
          {{ item.tranP90 }}
        </div>
        <div
          class="w-16 flex-shrink-0 px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
          {{ item.tranP95 }}
        </div>
        <div
          class="w-16 flex-shrink-0 px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
          {{ item.tranP99 }}
        </div>
        <div class="w-16 flex-shrink-0 px-1.5 py-1.5 break-all whitespace-pre-wrap">
          {{ item.tranP999 }}
        </div>
      </div>
    </div>
  </div>
</template>
