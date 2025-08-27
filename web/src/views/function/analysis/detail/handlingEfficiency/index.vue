<script lang="ts" setup>
import { defineAsyncComponent, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

interface Props {
  analysisInfo?: Record<string, any>;
}

const props = withDefaults(defineProps<Props>(), {
  analysisInfo: undefined
});

const Echart = defineAsyncComponent(() => import('./echart.vue'));

const getChartData = (data) => {
  const res = {};

  const { totalNum = 0, passedTestRate = 0, passedTestNum = 0, oneTimeNotPassedNum = 0, oneTimePassedNum = 0, oneTimePassedRate = 0, twoTimePassedNum = 0, twoTimePassedRate = 0, twoTimeNotPassedNum = 0 } = data;
  res.chart0Value = {
    yData: [passedTestNum, oneTimeNotPassedNum, oneTimePassedNum]
  };
  res.chart1Value = {
    title: passedTestRate + '%',
    value: [{ name: t('functionAnalysis.detail.handlingEfficiency.incompleteCaseCount'), value: totalNum - passedTestNum }, { name: t('functionAnalysis.detail.handlingEfficiency.completedCaseCount2'), value: passedTestNum }]
  };

  res.chart2Value = {
    title: oneTimePassedRate + '%',
    value: [{ name: t('functionAnalysis.detail.handlingEfficiency.oneTimeIncompleteCaseCount'), value: oneTimeNotPassedNum }, { name: t('functionAnalysis.detail.handlingEfficiency.oneTimeCompletedCaseCount'), value: oneTimePassedNum }]
  };

  res.chart3Value = {
    title: twoTimePassedRate + '%',
    value: [{ name: t('functionAnalysis.detail.handlingEfficiency.twoTimeIncompleteCaseCount'), value: passedTestNum - twoTimePassedNum }, { name: t('functionAnalysis.detail.handlingEfficiency.twoTimeCompletedCaseCount'), value: twoTimePassedNum }]
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
        const sourceData = newValue.data?.testersOverview || {};
        const assignees = newValue.data?.testers || [];
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
    <div class="font-semibold pl-3">{{ t('functionAnalysis.detail.handlingEfficiency.total') }}</div>
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
