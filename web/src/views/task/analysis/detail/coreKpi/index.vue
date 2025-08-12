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

const Echart = defineAsyncComponent(() => import('./echart.vue'));

const getChartData = (data) => {
  const res = {};

  const {
    totalNum = 0, completedNum = 0, completedRate = 0,
    bugNum = 0, bugRate = 0, completedBugNum, completedBugRate, completedOverdueNum = 0, completedOverdueRate = 0, completedWorkload = 0, completedWorkloadRate = 0,
    evalWorkload = 0, overdueNum = 0, overdueRate = 0
  } = data;
  res.chart0Value = {
    yData0: [completedNum, completedWorkload, completedOverdueNum, completedBugNum],
    yData1: [totalNum, evalWorkload, overdueNum, bugNum]
  };
  res.chart1Value = {
    title: completedRate + '%',
    value: [{ name: t('taskAnalysis.detail.coreKpi.unfinishedTaskCount'), value: totalNum - completedNum }, { name: t('taskAnalysis.detail.coreKpi.completedTaskCount'), value: completedNum }]
  };
  res.chart2Value = {
    title: completedRate + '%',
    value: [{ name: t('taskAnalysis.detail.coreKpi.unfinishedWorkload'), value: evalWorkload - completedWorkload }, { name: t('taskAnalysis.detail.coreKpi.completedWorkload'), value: completedWorkload }]
  };

  res.chart3Value = {
    title: completedOverdueRate + '%',
    value: [{ name: t('taskAnalysis.detail.coreKpi.unfinishedOverdueCount'), value: overdueNum - completedOverdueNum }, { name: t('taskAnalysis.detail.coreKpi.completedOverdueCount'), value: completedOverdueNum }]
  };

  res.chart4Value = {
    title: completedBugRate + '%',
    value: [{ name: t('taskAnalysis.detail.coreKpi.unfinishedBugCount'), value: bugNum - completedBugNum }, { name: t('taskAnalysis.detail.coreKpi.completedBugCount'), value: completedBugNum }]
  };
  return res;
};

const totalValue = ref({

});

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
    <div class="font-semibold pl-3">{{ t('taskAnalysis.detail.total') }}</div>
    <Echart
      ref="totalChartRef"
      v-bind="totalValue"
      class="ml-3" />
  </div>

  <div
    v-for="item in personValues"
    :key="item.id"
    class="mt-5">
    <div class="font-semibold pl-3">{{ item.userName }}</div>
    <Echart
      ref="chartListRef"
      v-bind="item.chartData"
      class="ml-3" />
  </div>
</template>
