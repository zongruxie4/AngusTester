<script lang="ts" setup>
import { defineAsyncComponent, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';

interface Props {
  analysisInfo?: Record<string, any>;
}

const { t } = useI18n();

const props = withDefaults(defineProps<Props>(), {
  analysisInfo: undefined
});

const EChart = defineAsyncComponent(() => import('./EChart.vue'));

const getChartData = (data) => {
  const res = {} as any;
  const {
    actualWorkload = 0, completedWorkload = 0, completedWorkloadRate = 0, evalWorkload = 0,
    invalidWorkload = 0, invalidWorkloadRate = 0, savingWorkload = 0, savingWorkloadRate = 0
  } = data;
  res.chart0Value = {
    yData: [evalWorkload, actualWorkload, completedWorkload, savingWorkload]
  };
  res.chart1Value = {
    title: completedWorkloadRate + '%',
    value: [
      { name: t('common.counts.uncompletedWorkload'), value: evalWorkload - completedWorkload },
      { name: t('common.completedWorkload'), value: completedWorkload }
    ]
  };

  res.chart2Value = {
    title: savingWorkloadRate + '%',
    value: [
      { name: t('issueAnalysis.detail.workload.pieLabels.incompleteSaving'), value: evalWorkload - savingWorkload },
      { name: t('issueAnalysis.detail.workload.pieLabels.completedSaving'), value: savingWorkload }
    ]
  };
  return res;
};

const totalValue = ref({});
const personValues = ref([]);

onMounted(() => {
  watch(() => props.analysisInfo, (newValue) => {
    if (newValue) {
      const sourceData = newValue.data?.totalOverview || {};
      totalValue.value = getChartData(sourceData);

      if (newValue?.containsUserAnalysis) {
        const sourceData = newValue.data?.assigneesOverview || {};
        const assignees = newValue.data?.assignees || [];
        Object.keys(sourceData).forEach(userId => {
          const viewData = sourceData[userId] || {};
          const chartData = getChartData(viewData);

          personValues.value.push({
            userName: assignees[userId]?.fullName,
            chartData,
            id: userId
          });
        });
      }
    } else {
      totalValue.value = {};
      personValues.value = [];
    }
  }, {
    immediate: true,
    deep: true
  });
});

const totalChartRef = ref();
const chartListRef = [];
defineExpose({
  resize: () => {
    totalChartRef.value.resize();
    chartListRef.forEach(item => {
      item.resize();
    });
  }
});
</script>
<template>
  <div>
    <div class="font-semibold pl-3">
      {{ t('chart.total') }}
    </div>
    <EChart
      ref="totalChartRef"
      v-bind="totalValue"
      class="ml-3" />
  </div>

  <div
    v-for="item in personValues"
    :key="item.id"
    class="mt-5">
    <div class="font-semibold pl-3">{{ item.userName }}</div>
    <EChart
      ref="chartListRef"
      v-bind="item.chartData"
      class="ml-3" />
  </div>
</template>
