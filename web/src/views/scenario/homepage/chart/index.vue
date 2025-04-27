<script setup lang="ts">
import { defineAsyncComponent, inject, onMounted, ref, watch } from 'vue';
import { http, TESTER } from '@xcan-angus/tools';

import { ResourceInfo } from './PropsType';

type Props = {
  projectId: string;
  notify: string;
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  notify: undefined
});

const ChartBar = defineAsyncComponent(() => import('./barChart.vue'));
const ChartPie = defineAsyncComponent(() => import('./pieChart.vue'));

const windowResizeNotify = inject('windowResizeNotify', ref<string>());

const dataSource = ref<ResourceInfo>();

const loadData = async () => {
  const params = { projectId: props.projectId };
  const [error, res] = await http.get(`${TESTER}/analysis/scenario/resources/count`, params);
  if (error) {
    return;
  }

  dataSource.value = res?.data as ResourceInfo;
};

const reset = () => {
  dataSource.value = {
    allSce: '0',
    sceByLastWeek: '0',
    sceByLastMonth: '0',
    sceByScriptType: {
      TEST_FUNCTIONALITY: '0',
      TEST_PERFORMANCE: '0',
      TEST_STABILITY: '0',
      TEST_CUSTOMIZATION: '0',
      MOCK_DATA: '0',
      MOCK_APIS: '0'
    },
    sceByPluginName: {}
  };
};

onMounted(() => {
  watch(() => props.projectId, (newValue) => {
    if (!newValue) {
      return;
    }

    reset();
    loadData();
  }, { immediate: true });

  watch(() => props.notify, (newValue) => {
    if (newValue === undefined || newValue === null || newValue === '') {
      return;
    }

    loadData();
  }, { immediate: true });
});
</script>

<template>
  <div>
    <div class="text-3.5 font-semibold mb-3">资源统计</div>
    <div class="flex space-x-3.75">
      <ChartBar
        :dataSource="dataSource"
        :resizeNotify="windowResizeNotify"
        style="width:calc(50% - 8px)" />
      <ChartPie
        :dataSource="dataSource"
        :resizeNotify="windowResizeNotify"
        style="width:calc(50% - 8px)" />
    </div>
  </div>
</template>
