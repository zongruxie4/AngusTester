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
  TEST_CUSTOMIZATION: t('taskAnalysis.detail.resourceCreation.resourceTypes.TEST_CUSTOMIZATION'),
  TEST_FUNCTIONALITY: t('taskAnalysis.detail.resourceCreation.resourceTypes.TEST_FUNCTIONALITY'),
  TEST_PERFORMANCE: t('taskAnalysis.detail.resourceCreation.resourceTypes.TEST_PERFORMANCE'),
  TEST_STABILITY: t('taskAnalysis.detail.resourceCreation.resourceTypes.TEST_STABILITY'),
  SERVICES: t('taskAnalysis.detail.resourceCreation.resourceTypes.SERVICES'),
  APIS: t('taskAnalysis.detail.resourceCreation.resourceTypes.APIS'),
  CASES: t('taskAnalysis.detail.resourceCreation.resourceTypes.CASES'),
  PLAN: t('taskAnalysis.detail.resourceCreation.resourceTypes.PLAN'),
  SPRINT: t('taskAnalysis.detail.resourceCreation.resourceTypes.SPRINT'),
  TASK_SPRINT: t('taskAnalysis.detail.resourceCreation.resourceTypes.TASK_SPRINT'),
  TASK: t('taskAnalysis.detail.resourceCreation.resourceTypes.TASK'),
  MOCK_APIS: t('taskAnalysis.detail.resourceCreation.resourceTypes.MOCK_APIS'),
  MOCK_PUSHBACK: t('taskAnalysis.detail.resourceCreation.resourceTypes.MOCK_PUSHBACK'),
  MOCK_RESPONSE: t('taskAnalysis.detail.resourceCreation.resourceTypes.MOCK_RESPONSE'),
  MOCK_SERVICE: t('taskAnalysis.detail.resourceCreation.resourceTypes.MOCK_SERVICE'),
  DATA_DATASET: t('taskAnalysis.detail.resourceCreation.resourceTypes.DATA_DATASET'),
  DATA_DATASOURCE: t('taskAnalysis.detail.resourceCreation.resourceTypes.DATA_DATASOURCE'),
  DATA_VARIABLE: t('taskAnalysis.detail.resourceCreation.resourceTypes.DATA_VARIABLE'),
  TOTAL: t('taskAnalysis.detail.resourceCreation.resourceTypes.TOTAL'),
  REPORT: t('taskAnalysis.detail.resourceCreation.resourceTypes.REPORT'),
  REPORT_RECORD: t('taskAnalysis.detail.resourceCreation.resourceTypes.REPORT_RECORD'),
  API_TEST: t('taskAnalysis.detail.resourceCreation.resourceTypes.API_TEST'),
  BUG: t('taskAnalysis.detail.resourceCreation.resourceTypes.BUG'),
  REQUIREMENT: t('taskAnalysis.detail.resourceCreation.resourceTypes.REQUIREMENT'),
  STORY: t('taskAnalysis.detail.resourceCreation.resourceTypes.STORY'),
  SCENARIO_TEST: t('taskAnalysis.detail.resourceCreation.resourceTypes.SCENARIO_TEST'),
  ANALYSIS: t('taskAnalysis.detail.resourceCreation.resourceTypes.ANALYSIS'),
  BACKLOG: t('taskAnalysis.detail.resourceCreation.resourceTypes.BACKLOG'),
  MEETING: t('taskAnalysis.detail.resourceCreation.resourceTypes.MEETING')
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
    <div class="font-semibold pl-3">{{ t('taskAnalysis.detail.resourceCreation.total') }}</div>
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
