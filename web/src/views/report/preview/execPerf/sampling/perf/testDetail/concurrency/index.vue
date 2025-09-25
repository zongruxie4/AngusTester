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
      threadPoolSize: string[];
      threadPoolActiveSize: string[];
      threadMaxPoolSize: string[];
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

const setSeries = (data: { [key: string]: { threadPoolSize: string[]; threadPoolActiveSize: string[]; threadMaxPoolSize: string[] } }, names: string[]) => {
  const result: { name: string; data: string[] }[] = [];
  for (let i = 0, len = names.length; i < len; i++) {
    const name = names[i];
    result.push({
      name: name + ' - ' + t('reportPreview.execPerf.sampling.testDetail.concurrency.threadCount'),
      data: data[name].threadPoolSize
    });
    result.push({
      name: name + ' - ' + t('reportPreview.execPerf.sampling.testDetail.concurrency.activeThreadCount'),
      data: data[name].threadPoolActiveSize
    });
    result.push({
      name: name + ' - ' + t('reportPreview.execPerf.sampling.testDetail.concurrency.maxThreadCount'),
      data: data[name].threadMaxPoolSize
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
      threadPoolSize: _data.threadPoolSize[_data.threadPoolSize.length - 1],
      threadPoolActiveSize: _data.threadPoolActiveSize[_data.threadPoolActiveSize.length - 1],
      threadMaxPoolSize: _data.threadMaxPoolSize[_data.threadMaxPoolSize.length - 1]
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
          <span>{{ t('reportPreview.execPerf.sampling.testDetail.concurrency.title') }}</span>
        </div>
        <div
          class="w-40 flex-shrink-0 bg-blue-table px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
          <span>{{ t('reportPreview.execPerf.sampling.testDetail.concurrency.threadCount') }}</span>
        </div>
        <div
          class="w-40 flex-shrink-0 bg-blue-table px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
          <span>{{ t('reportPreview.execPerf.sampling.testDetail.concurrency.activeThreadCount') }}</span>
        </div>
        <div
          class="w-40 flex-shrink-0 bg-blue-table px-1.5 py-1.5 break-all whitespace-pre-wrap">
          <span>{{ t('reportPreview.execPerf.sampling.testDetail.concurrency.maxThreadCount') }}</span>
        </div>
      </div>

      <div
        v-for="item in tableData"
        :key="item.id"
        class="flex border-b border-solid border-border-input last:border-0">
        <div class="flex-1 flex-shrink-0 px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
          {{ item.name }}
        </div>
        <div class="w-40 flex-shrink-0 px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
          {{ item.threadPoolSize }}
        </div>
        <div class="w-40 flex-shrink-0 px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
          {{ item.threadPoolActiveSize }}
        </div>
        <div class="w-40 flex-shrink-0 px-1.5 py-1.5 break-all whitespace-pre-wrap">
          {{ item.threadPoolSize }}
        </div>
      </div>
    </div>
  </div>
</template>
