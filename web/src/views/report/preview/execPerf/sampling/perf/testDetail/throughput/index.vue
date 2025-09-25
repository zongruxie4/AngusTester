<script setup lang="ts">
import { computed, defineAsyncComponent, onMounted, ref, watch } from 'vue';
import { cloneDeep } from 'lodash-es';
import { utils } from '@xcan-angus/infra';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

const LineStackChart = defineAsyncComponent(() => import('@/views/report/preview/execPerf/sampling/perf/testDetail/lineStackChart/index.vue'));

interface Props {
  timestampList: string[];
  names: string[];
  dataMap: {
    [key: string]: {
      ops: string[];
      tps: string[];
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

const setSeries = (data: { [key: string]: { ops: string[]; tps: string[] } }, names: string[]) => {
  const result: { name: string; data: string[] }[] = [];
  for (let i = 0, len = names.length; i < len; i++) {
    const name = names[i];
    result.push({
      name: name + ' - ' + t('reportPreview.execPerf.sampling.testDetail.throughput.queriesPerSecond'),
      data: data[name].ops
    });
    result.push({
      name: name + ' - ' + t('reportPreview.execPerf.sampling.testDetail.throughput.transactionsPerSecond'),
      data: data[name].tps
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
    const _data = dataMap[item];
    const opsList = _data.ops?.map(item => +((+item).toFixed(2).replace(/(\d+).00/, '$1'))) || [];
    const opsNum = opsList.reduce((prev, cur) => prev + cur, 0);
    const meanOps = (opsNum / opsList.length).toFixed(2).replace(/(\d+).00/, '$1');

    const tpsList = _data.tps?.map(item => +((+item).toFixed(2).replace(/(\d+).00/, '$1'))) || [];
    const tpsNum = tpsList.reduce((prev, cur) => prev + cur, 0);
    const meanTps = (tpsNum / tpsList.length).toFixed(2).replace(/(\d+).00/, '$1');
    return {
      id: utils.uuid(),
      name: item,
      ops: opsList[opsList.length - 1],
      minOps: Math.min(...opsList),
      maxOps: Math.max(...opsList),
      meanOps,
      tps: tpsList[tpsList.length - 1],
      minTps: Math.min(...tpsList),
      maxTps: Math.max(...tpsList),
      meanTps
    };
  });
});
</script>
<template>
  <div>
    <LineStackChart :series="series" :xAxis="xAxis" />

    <div v-if="!!tableData?.length" class="mt-7 border border-solid border-border-input">
      <div class="flex border-b border-solid border-border-input">
        <div
          class="flex-1 flex-shrink-0 bg-blue-table px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
          <span>{{ t('common.name') }}</span>
        </div>
        <div
          class="w-80 flex-shrink-0 bg-blue-table px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
          <span>{{ t('reportPreview.execPerf.sampling.testDetail.throughput.queriesPerSecondWithStats') }}</span>
        </div>
        <div class="w-80 flex-shrink-0 bg-blue-table px-1.5 py-1.5 break-all whitespace-pre-wrap">
          <span>{{ t('reportPreview.execPerf.sampling.testDetail.throughput.transactionsPerSecondWithStats') }}</span>
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
          class="w-80 flex-shrink-0 px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
          <div class="flex items-center">
            <div>{{ item.ops }}</div>
            <div class="w-0.25 h-3 mx-1 bg-gray-400 rounded"></div>
            <div>{{ item.minOps }}</div>
            <div class="w-0.25 h-3 mx-1 bg-gray-400 rounded"></div>
            <div>{{ item.maxOps }}</div>
            <div class="w-0.25 h-3 mx-1 bg-gray-400 rounded"></div>
            <div>{{ item.meanOps }}</div>
          </div>
        </div>
        <div class="w-80 flex-shrink-0 px-1.5 py-1.5 break-all whitespace-pre-wrap">
          <div class="flex items-center">
            <div>{{ item.tps }}</div>
            <div class="w-0.25 h-3 mx-1 bg-gray-400 rounded"></div>
            <div>{{ item.minTps }}</div>
            <div class="w-0.25 h-3 mx-1 bg-gray-400 rounded"></div>
            <div>{{ item.maxTps }}</div>
            <div class="w-0.25 h-3 mx-1 bg-gray-400 rounded"></div>
            <div>{{ item.meanTps }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
