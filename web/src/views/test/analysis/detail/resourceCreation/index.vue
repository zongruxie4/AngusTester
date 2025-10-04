<script lang="ts" setup>
import { defineAsyncComponent, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { getDateArr } from '@/utils/utils';

const { t } = useI18n();

interface Props {
  analysisInfo?: Record<string, any>;
}

const props = withDefaults(defineProps<Props>(), {
  analysisInfo: undefined
});

const EChart = defineAsyncComponent(() => import('./EChart.vue'));

const targetDataCategery = {
  TEST_CUSTOMIZATION: t('testAnalysis.detail.resourceCreation.customTest'),
  TEST_FUNCTIONALITY: t('testAnalysis.detail.resourceCreation.functionalTest'),
  TEST_PERFORMANCE: t('testAnalysis.detail.resourceCreation.performanceTest'),
  TEST_STABILITY: t('testAnalysis.detail.resourceCreation.stabilityTest'),
  SERVICES: t('testAnalysis.detail.resourceCreation.services'),
  APIS: t('common.api'),
  CASES: t('common.useCase'),
  PLAN: t('common.plan'),
  SPRINT: t('common.sprint'),
  TASK_SPRINT: t('testAnalysis.detail.resourceCreation.taskSprint'),
  TASK: t('common.issue'),
  MOCK_APIS: t('testAnalysis.detail.resourceCreation.mockApis'),
  MOCK_PUSHBACK: t('testAnalysis.detail.resourceCreation.mockPushback'),
  MOCK_RESPONSE: t('testAnalysis.detail.resourceCreation.mockResponse'),
  MOCK_SERVICE: t('testAnalysis.detail.resourceCreation.mockService'),
  DATA_DATASET: t('testAnalysis.detail.resourceCreation.dataDataset'),
  DATA_DATASOURCE: t('testAnalysis.detail.resourceCreation.dataDatasource'),
  DATA_VARIABLE: t('testAnalysis.detail.resourceCreation.dataVariable'),
  TOTAL: t('testAnalysis.detail.resourceCreation.total2'),
  REPORT: t('testAnalysis.detail.resourceCreation.report'),
  REPORT_RECORD: t('testAnalysis.detail.resourceCreation.reportRecord'),
  API_TEST: t('testAnalysis.detail.resourceCreation.apiTest'),
  BUG: t('testAnalysis.detail.resourceCreation.bug'),
  REQUIREMENT: t('testAnalysis.detail.resourceCreation.requirement'),
  STORY: t('testAnalysis.detail.resourceCreation.story'),
  SCENARIO_TEST: t('testAnalysis.detail.resourceCreation.scenarioTest'),
  ANALYSIS: t('common.analysis'),
  BACKLOG: t('common.backlog'),
  MEETING: t('common.meeting'),
  BASELINE: t('testAnalysis.detail.resourceCreation.baseline'),
  CASE: t('common.useCase'),
  REVIEW: t('common.review')
};

const getChartData = (data) => {
  const res = {} as any;

  const { planNum = 0, caseNum = 0, reviewNum = 0, analysisNum = 0, baselineNum = 0, totalNum = 0 } = data;
  res.overdueAssessmentData = data;
  res.chart0Value = {
    yData: [planNum, caseNum, reviewNum, baselineNum, analysisNum, totalNum]
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

const totalValue = ref({});

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
    <div class="font-semibold pl-3">
      {{ t('chart.total') }}
    </div>
    <EChart
      ref="totalChartRef"
      v-bind="totalValue"
      class="ml-3" />
  </div>

  <div
    v-for="item in personValues"
    :key="item.id"
    class="mt-5">
    <div class="font-semibold pl-3">{{ item.userName }}</div>
    <EChart
      ref="chartListRef"
      v-bind="item.chartData"
      class="ml-3" />
  </div>
</template>
