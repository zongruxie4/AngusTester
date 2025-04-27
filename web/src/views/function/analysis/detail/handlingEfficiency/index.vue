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

  const { totalNum = 0, passedTestRate = 0, passedTestNum = 0, oneTimeNotPassedNum = 0, oneTimePassedNum = 0, oneTimePassedRate = 0, twoTimePassedNum = 0, twoTimePassedRate = 0, twoTimeNotPassedNum = 0 } = data;
  res.chart0Value = {
    yData: [passedTestNum, oneTimeNotPassedNum, oneTimePassedNum]
  };
  res.chart1Value = {
    title: passedTestRate + '%',
    value: [{ name: '未完成用例数', value: totalNum - passedTestNum }, { name: '完成用例数', value: passedTestNum }]
  };

  res.chart2Value = {
    title: oneTimePassedRate + '%',
    value: [{ name: '一次性未完成用例数', value: oneTimeNotPassedNum }, { name: '一次性完成用例数', value: oneTimePassedNum }]
  };

  res.chart3Value = {
    title: twoTimePassedRate + '%',
    value: [{ name: '两次未完成用例数', value: passedTestNum - twoTimePassedNum }, { name: '两次完成用例数', value: twoTimePassedNum }]
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
            userName: assignees[userId]?.fullname,
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
