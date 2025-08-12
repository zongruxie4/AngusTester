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
    value: [{ name: '未完成任务数', value: totalNum - completedNum }, { name: '完成任务数', value: completedNum }]
  };
  res.chart2Value = {
    title: completedRate + '%',
    value: [{ name: '未完成工作量', value: evalWorkload - completedWorkload }, { name: '完成工作量', value: completedWorkload }]
  };

  res.chart3Value = {
    title: completedOverdueRate + '%',
    value: [{ name: '未完成逾期数', value: overdueNum - completedOverdueNum }, { name: '完成逾期数', value: completedOverdueNum }]
  };

  res.chart4Value = {
    title: completedBugRate + '%',
    value: [{ name: '未完成缺陷数', value: bugNum - completedBugNum }, { name: '完成缺陷数', value: completedBugNum }]
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
