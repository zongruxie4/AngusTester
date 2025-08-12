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
  TEST_CUSTOMIZATION: t('taskAnalysis.detail.resourceCreation.customTest'),
  TEST_FUNCTIONALITY: t('taskAnalysis.detail.resourceCreation.functionalTest'),
  TEST_PERFORMANCE: t('taskAnalysis.detail.resourceCreation.performanceTest'),
  TEST_STABILITY: t('taskAnalysis.detail.resourceCreation.stabilityTest'),
  SERVICES: t('taskAnalysis.detail.resourceCreation.services'),
  APIS: t('taskAnalysis.detail.resourceCreation.apis'),
  CASES: t('taskAnalysis.detail.resourceCreation.cases'),
  PLAN: t('taskAnalysis.detail.resourceCreation.plan'),
  SPRINT: t('taskAnalysis.detail.resourceCreation.sprint'),
  TASK_SPRINT: t('taskAnalysis.detail.resourceCreation.sprint'),
  TASK: t('taskAnalysis.detail.resourceCreation.task'),
  MOCK_APIS: t('taskAnalysis.detail.resourceCreation.mockApis'),
  MOCK_PUSHBACK: t('taskAnalysis.detail.resourceCreation.mockPushback'),
  MOCK_RESPONSE: t('taskAnalysis.detail.resourceCreation.mockResponse'),
  MOCK_SERVICE: t('taskAnalysis.detail.resourceCreation.mockService'),
  DATA_DATASET: t('taskAnalysis.detail.resourceCreation.dataset'),
  DATA_DATASOURCE: t('taskAnalysis.detail.resourceCreation.datasource'),
  DATA_VARIABLE: t('taskAnalysis.detail.resourceCreation.variable'),
  TOTAL: t('taskAnalysis.detail.resourceCreation.total'),
  REPORT: t('taskAnalysis.detail.resourceCreation.report'),
  REPORT_RECORD: t('taskAnalysis.detail.resourceCreation.record'),
  API_TEST: t('taskAnalysis.detail.resourceCreation.apiTest'),
  BUG: t('taskAnalysis.detail.resourceCreation.bug'),
  REQUIREMENT: t('taskAnalysis.detail.resourceCreation.requirement'),
  STORY: t('taskAnalysis.detail.resourceCreation.story'),
  SCENARIO_TEST: t('taskAnalysis.detail.resourceCreation.scenarioTest'),
  ANALYSIS: t('taskAnalysis.detail.resourceCreation.analysis'),
  BACKLOG: t('taskAnalysis.detail.resourceCreation.backlog'),
  MEETING: t('taskAnalysis.detail.resourceCreation.meeting')
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
