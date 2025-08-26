<script lang="ts" setup>
import { defineAsyncComponent, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';

interface Props {
  analysisInfo?: Record<string, any>;
}

const props = withDefaults(defineProps<Props>(), {
  analysisInfo: undefined
});

const { t } = useI18n();

const Echart = defineAsyncComponent(() => import('./echart.vue'));

const getChartData = (data) => {
  const res = {};

  //   completedNum
  // :
  // "491"
  // completedRate
  // :
  // "97.04"
  // oneTimeNotPassedNum
  // :
  // "22"
  // oneTimeNotPassedRate
  // :
  // "4.48"
  // oneTimePassedNum
  // :
  // "469"
  // oneTimePassedRate
  // :
  // "95.52"
  // totalNum
  // :
  // "506"
  // twoTimePassedNum
  // :
  // "19"
  // twoTimePassedRate
  // :
  // "3.87"

  const { totalNum = 0, completedNum = 0, completedRate = 0, oneTimeNotPassedNum = 0, oneTimePassedNum = 0, oneTimePassedRate = 0, twoTimePassedNum = 0, twoTimePassedRate = 0, twoTimeNotPassedNum = 0 } = data;
  res.chart0Value = {
    yData: [completedNum, oneTimeNotPassedNum, oneTimePassedNum]
  };
  res.chart1Value = {
    title: completedRate + '%',
    value: [{ name: t('taskAnalysis.detail.handlingEfficiency.chartLabels.uncompletedTaskCount'), value: totalNum - completedNum }, { name: t('taskAnalysis.detail.handlingEfficiency.chartLabels.completedTaskCount2'), value: completedNum }]
  };

  res.chart2Value = {
    title: oneTimePassedRate + '%',
    value: [{ name: t('taskAnalysis.detail.handlingEfficiency.chartLabels.oneTimeUncompletedTaskCount'), value: oneTimeNotPassedNum }, { name: t('taskAnalysis.detail.handlingEfficiency.chartLabels.oneTimeCompletedTaskCount'), value: oneTimePassedNum }]
  };

  res.chart3Value = {
    title: twoTimePassedRate + '%',
    value: [{ name: t('taskAnalysis.detail.handlingEfficiency.chartLabels.twoTimeUncompletedTaskCount'), value: completedNum - twoTimePassedNum }, { name: t('taskAnalysis.detail.handlingEfficiency.chartLabels.twoTimeCompletedTaskCount'), value: twoTimePassedNum }]
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
    <div class="font-semibold pl-3">{{ t('taskAnalysis.detail.handlingEfficiency.total') }}</div>
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
