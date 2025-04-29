<script lang="ts" setup>
import { defineAsyncComponent, onMounted, ref, watch } from 'vue';
import { getDateArr } from '@/utils/utils';

interface Props {
  analysisInfo?: Record<string, any>;
}

const props = withDefaults(defineProps<Props>(), {
  analysisInfo: undefined
});

const Echart = defineAsyncComponent(() => import('./echart.vue'));

const targetDataCategery = {
  TEST_CUSTOMIZATION: '自定义测试',
  TEST_FUNCTIONALITY: '功能测试',
  TEST_PERFORMANCE: '性能测试',
  TEST_STABILITY: '稳定性测试',
  SERVICES: '服务',
  APIS: '接口',
  CASES: '用例',
  PLAN: '计划',
  SPRINT: '迭代',
  TASK_SPRINT: '迭代',
  TASK: '任务',
  MOCK_APIS: 'Mock接口',
  MOCK_PUSHBACK: 'Mock回推',
  MOCK_RESPONSE: 'Mock响应',
  MOCK_SERVICE: 'Mock服务',
  DATA_DATASET: '数据集',
  DATA_DATASOURCE: '数据源',
  DATA_VARIABLE: '变量',
  TOTAL: '合计',
  REPORT: '报告',
  REPORT_RECORD: '记录',
  API_TEST: '接口测试',
  BUG: '缺陷',
  REQUIREMENT: '需求',
  STORY: '故事',
  SCENARIO_TEST: '场景测试',
  ANALYSIS: '分析',
  BACKLOG: 'backlog',
  MEETING: '会议'
};

const getChartData = (data) => {
  const res = {};

  const { analysisNum = 0, backlogNum = 0, meetingNum = 0, sprintNum = 0, taskNum = 0, totalNum = 0 } = data;
  res.overdueAssessmentData = data;
  res.chart0Value = {
    yData: [backlogNum, sprintNum, taskNum, meetingNum, analysisNum, totalNum]
  };

  const keys = Object.keys(data.timeSeries);
  let xData = [];
  let series = [];
  if (keys.length) {
    keys.forEach(key => {
      data.timeSeries[key].forEach(i => {
        if (!xData.includes(i.timeSeries)) {
          xData.push(i.timeSeries);
        }
      });
    });
    xData.sort((a, b) => {
      return a > b ? 1 : a < b ? -1 : 0;
    });
    series = keys.map(key => {
      return {
        name: targetDataCategery[key],
        data: xData.map(i => {
          const target = data.timeSeries[key].find(item => item.timeSeries === i);
          if (target) {
            return target.value;
          } else {

          }
        }),
        type: 'line',
        smooth: true,
        connectNulls: true
      };
    });
  }
  if (xData.length === 0) {
    xData = getDateArr();
    if (series.length) {
      series[0].data = Array.from(new Array(xData.length)).fill(0);
    }
  }
  res.chart1Value = {
    value: series,
    xData
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
        const sourceData = newValue.data?.creatorOverview || {};
        const assignees = newValue.data?.creators || [];
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
