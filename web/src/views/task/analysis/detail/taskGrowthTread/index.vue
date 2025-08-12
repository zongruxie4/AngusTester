<script lang="ts" setup>
import { defineAsyncComponent, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { getDateArr } from '@/utils/utils';

interface Props {
  analysisInfo?: Record<string, any>;
}

const { t } = useI18n();
const props = withDefaults(defineProps<Props>(), {
  analysisInfo: undefined
});

const Echart = defineAsyncComponent(() => import('./echart.vue'));

const targetDataCategery = {
  TEST_CUSTOMIZATION: t('taskAnalysis.detail.taskGrowthTrend.customTest'),
  TEST_FUNCTIONALITY: t('taskAnalysis.detail.taskGrowthTrend.functionalTest'),
  TEST_PERFORMANCE: t('taskAnalysis.detail.taskGrowthTrend.performanceTest'),
  TEST_STABILITY: t('taskAnalysis.detail.taskGrowthTrend.stabilityTest'),
  SERVICES: t('taskAnalysis.detail.taskGrowthTrend.services'),
  APIS: t('taskAnalysis.detail.taskGrowthTrend.apis'),
  CASES: t('taskAnalysis.detail.taskGrowthTrend.cases'),
  PLAN: t('taskAnalysis.detail.taskGrowthTrend.plan'),
  SPRINT: t('taskAnalysis.detail.taskGrowthTrend.sprint'),
  TASK_SPRINT: t('taskAnalysis.detail.taskGrowthTrend.sprint'),
  TASK: t('taskAnalysis.detail.taskGrowthTrend.task'),
  MOCK_APIS: t('taskAnalysis.detail.taskGrowthTrend.mockApis'),
  MOCK_PUSHBACK: t('taskAnalysis.detail.taskGrowthTrend.mockPushback'),
  MOCK_RESPONSE: t('taskAnalysis.detail.taskGrowthTrend.mockResponse'),
  MOCK_SERVICE: t('taskAnalysis.detail.taskGrowthTrend.mockService'),
  DATA_DATASET: t('taskAnalysis.detail.taskGrowthTrend.dataset'),
  DATA_DATASOURCE: t('taskAnalysis.detail.taskGrowthTrend.datasource'),
  DATA_VARIABLE: t('taskAnalysis.detail.taskGrowthTrend.variable'),
  TOTAL: t('taskAnalysis.detail.taskGrowthTrend.total'),
  REPORT: t('taskAnalysis.detail.taskGrowthTrend.report'),
  REPORT_RECORD: t('taskAnalysis.detail.taskGrowthTrend.record'),
  API_TEST: t('taskAnalysis.detail.taskGrowthTrend.apiTest'),
  BUG: t('taskAnalysis.detail.taskGrowthTrend.bug'),
  REQUIREMENT: t('taskAnalysis.detail.taskGrowthTrend.requirement'),
  STORY: t('taskAnalysis.detail.taskGrowthTrend.story'),
  SCENARIO_TEST: t('taskAnalysis.detail.taskGrowthTrend.scenarioTest')
};

const getChartData = (data) => {
  const res = {};

  const { apiTestNum = 0, requirementNum = 0, scenarioTestNum = 0, storyNum = 0, bugNum = 0, taskNum = 0, totalNum = 0 } = data;
  res.overdueAssessmentData = data;
  res.chart0Value = {
    yData: [requirementNum, storyNum, taskNum, bugNum, apiTestNum, scenarioTestNum, totalNum]
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
