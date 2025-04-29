<script lang="ts" setup>
import { defineAsyncComponent, onMounted, ref, watch } from 'vue';

interface Props {
  analysisInfo?: Record<string, any>;
}

const props = withDefaults(defineProps<Props>(), {
  analysisInfo: undefined
});

const Echart = defineAsyncComponent(() => import('./echart.vue'));

const getChartData = (data) => {
  const res = {};

  const { totalNum = 0, passedReviewNum = 0, oneTimePassedReviewNum = 0, oneTimeNotPassedReviewNum = 0, oneTimeNotPassedReviewRate = 0, oneTimePassedReviewRate = 0, passedReviewRate = 0, twoTimePassedReviewRate = 0, twoTimePassedReviewNum = 0, twoTimeNotPassedReviewNum } = data;
  res.chart0Value = {
    yData: [passedReviewNum, oneTimePassedReviewNum, twoTimePassedReviewNum]
  };
  res.chart1Value = {
    title: passedReviewRate + '%',
    value: [{ name: '未通过用例数', value: totalNum - passedReviewNum }, { name: '通过用例数', value: passedReviewNum }]
  };

  res.chart2Value = {
    title: oneTimePassedReviewRate + '%',
    value: [{ name: '一次性未通过用例数', value: twoTimePassedReviewNum }, { name: '一次性通过用例数', value: oneTimeNotPassedReviewNum }]
  };

  res.chart3Value = {
    title: twoTimePassedReviewRate + '%',
    value: [{ name: '两次未通过用例数', value: passedReviewNum - twoTimePassedReviewNum }, { name: '两次通过用例数', value: twoTimePassedReviewNum }]
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
    <div class="font-semibold pl-3">总共</div>
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
