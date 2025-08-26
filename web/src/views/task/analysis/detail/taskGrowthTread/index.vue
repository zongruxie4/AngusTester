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
  TEST_CUSTOMIZATION: t('taskAnalysis.detail.taskGrowthTread.resourceTypes.TEST_CUSTOMIZATION'),
  TEST_FUNCTIONALITY: t('taskAnalysis.detail.taskGrowthTread.resourceTypes.TEST_FUNCTIONALITY'),
  TEST_PERFORMANCE: t('taskAnalysis.detail.taskGrowthTread.resourceTypes.TEST_PERFORMANCE'),
  TEST_STABILITY: t('taskAnalysis.detail.taskGrowthTread.resourceTypes.TEST_STABILITY'),
  SERVICES: t('taskAnalysis.detail.taskGrowthTread.resourceTypes.SERVICES'),
  APIS: t('taskAnalysis.detail.taskGrowthTread.resourceTypes.APIS'),
  CASES: t('taskAnalysis.detail.taskGrowthTread.resourceTypes.CASES'),
  PLAN: t('taskAnalysis.detail.taskGrowthTread.resourceTypes.PLAN'),
  SPRINT: t('taskAnalysis.detail.taskGrowthTread.resourceTypes.SPRINT'),
  TASK_SPRINT: t('taskAnalysis.detail.taskGrowthTread.resourceTypes.TASK_SPRINT'),
  TASK: t('taskAnalysis.detail.taskGrowthTread.resourceTypes.TASK'),
  MOCK_APIS: t('taskAnalysis.detail.taskGrowthTread.resourceTypes.MOCK_APIS'),
  MOCK_PUSHBACK: t('taskAnalysis.detail.taskGrowthTread.resourceTypes.MOCK_PUSHBACK'),
  MOCK_RESPONSE: t('taskAnalysis.detail.taskGrowthTread.resourceTypes.MOCK_RESPONSE'),
  MOCK_SERVICE: t('taskAnalysis.detail.taskGrowthTread.resourceTypes.MOCK_SERVICE'),
  DATA_DATASET: t('taskAnalysis.detail.taskGrowthTread.resourceTypes.DATA_DATASET'),
  DATA_DATASOURCE: t('taskAnalysis.detail.taskGrowthTread.resourceTypes.DATA_DATASOURCE'),
  DATA_VARIABLE: t('taskAnalysis.detail.taskGrowthTread.resourceTypes.DATA_VARIABLE'),
  TOTAL: t('taskAnalysis.detail.taskGrowthTread.resourceTypes.TOTAL'),
  REPORT: t('taskAnalysis.detail.taskGrowthTread.resourceTypes.REPORT'),
  REPORT_RECORD: t('taskAnalysis.detail.taskGrowthTread.resourceTypes.REPORT_RECORD'),
  API_TEST: t('taskAnalysis.detail.taskGrowthTread.resourceTypes.API_TEST'),
  BUG: t('taskAnalysis.detail.taskGrowthTread.resourceTypes.BUG'),
  REQUIREMENT: t('taskAnalysis.detail.taskGrowthTread.resourceTypes.REQUIREMENT'),
  STORY: t('taskAnalysis.detail.taskGrowthTread.resourceTypes.STORY'),
  SCENARIO_TEST: t('taskAnalysis.detail.taskGrowthTread.resourceTypes.SCENARIO_TEST')
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
    <div class="font-semibold pl-3">{{ t('taskAnalysis.detail.taskGrowthTread.total') }}</div>
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
