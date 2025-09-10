<script setup lang="ts">
import { computed, defineAsyncComponent, onMounted, ref, watch } from 'vue';
import { cloneDeep } from 'lodash-es';
import { utils } from '@xcan-angus/infra';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

const LineStackChart = defineAsyncComponent(() => import('@/views/report/preview/execPerf/sampling/perf/testDetail/lineStackChart/index.vue'));

interface Props {
  singleHttp: boolean;
  timestampList: string[];
  names: string[];
  dataMap: {
    [key: string]: {
      n: string[];
      operations: string[];
      transactions: string[];
      errors: string[];
      errorRate: string[];
    }
  };
}

const props = withDefaults(defineProps<Props>(), {
  singleHttp: false,
  timestampList: () => [],
  names: () => [],
  dataMap: () => ({})
});

const xAxis = ref<{ data: string[] }>({ data: [] });
const series = ref<{ name: string; data: string[] }[]>([]);

const setSeries = (data: {
  [key: string]: {
    n: string[];
    operations: string[];
    transactions: string[];
    errors: string[];
    errorRate: string[];
  }
}, names: string[]) => {
  const result: { name: string; data: string[] }[] = [];
  for (let i = 0, len = names.length; i < len; i++) {
    const name = names[i];
    result.push({
      name: name + ' - ' + t('reportPreview.execPerf.sampling.testDetail.error.sampleCount'),
      data: data[name].n
    });
    if (!props.singleHttp) {
      result.push({
        name: name + ' - ' + t('reportPreview.execPerf.sampling.testDetail.error.requestCount'),
        data: data[name].operations
      });
    }
    result.push({
      name: name + ' - ' + t('reportPreview.execPerf.sampling.testDetail.error.transactionCount'),
      data: data[name].transactions
    });
    result.push({
      name: name + ' - ' + t('reportPreview.execPerf.sampling.testDetail.error.errorCount'),
      data: data[name].errors
    });
    result.push({
      name: name + ' - ' + t('reportPreview.execPerf.sampling.testDetail.error.errorRate'),
      data: data[name].errorRate
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
      n: _data.n[_data.n.length - 1],
      operations: _data.operations[_data.operations.length - 1],
      transactions: _data.transactions[_data.transactions.length - 1],
      errors: _data.errors[_data.errors.length - 1],
      errorRate: _data.errorRate[_data.errorRate.length - 1]
    };
  });
});

const style = computed(() => {
  if (props.singleHttp) {
    return 'width:115px;';
  }

  return 'width:92px;';
});
</script>
<template>
  <div>
    <LineStackChart
      :series="series"
      :xAxis="xAxis"
      :gridTop="150"
      style="height: 400px;" />

    <div v-if="!!tableData?.length" class="mt-7 border border-solid border-border-input">
      <div class="flex border-b border-solid border-border-input">
        <div
          class="flex-1 flex-shrink-0 bg-blue-table px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
          <span>{{ t('reportPreview.execPerf.sampling.testDetail.error.name') }}</span>
        </div>
        <div
          :style="style"
          class="flex-shrink-0 bg-blue-table px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
          <span>{{ t('reportPreview.execPerf.sampling.testDetail.error.sampleCount') }}</span>
        </div>
        <div
          v-if="!props.singleHttp"
          :style="style"
          class="flex-shrink-0 bg-blue-table px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
          <span>{{ t('reportPreview.execPerf.sampling.testDetail.error.requestCount') }}</span>
        </div>
        <div
          :style="style"
          class="flex-shrink-0 bg-blue-table px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
          <span>{{ t('reportPreview.execPerf.sampling.testDetail.error.transactionCount') }}</span>
        </div>
        <div
          :style="style"
          class="flex-shrink-0 bg-blue-table px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
          <span>{{ t('reportPreview.execPerf.sampling.testDetail.error.errorCount') }}</span>
        </div>
        <div
          :style="style"
          class="flex-shrink-0 bg-blue-table px-1.5 py-1.5 break-all whitespace-pre-wrap">
          <span>{{ t('reportPreview.execPerf.sampling.testDetail.error.errorRate') }}</span>
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
          :style="style"
          class="flex-shrink-0 px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
          {{ item.n }}
        </div>
        <div
          v-if="!props.singleHttp"
          :style="style"
          class="flex-shrink-0 px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
          {{ item.operations }}
        </div>
        <div
          :style="style"
          class="flex-shrink-0 px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
          {{ item.transactions }}
        </div>
        <div
          :style="style"
          class="flex-shrink-0 px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
          {{ item.errors }}
        </div>
        <div
          :style="style"
          class="flex-shrink-0 px-1.5 py-1.5 break-all whitespace-pre-wrapt">
          {{ item.errorRate }}%
        </div>
      </div>
    </div>
  </div>
</template>
