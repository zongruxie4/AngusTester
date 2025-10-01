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

const EChart = defineAsyncComponent(() => import('./EChart.vue'));

const getChartData = (data) => {
  const res = {} as any;

  const failureLevelCount = data?.failureLevelCount || {};
  const { CRITICAL, MAJOR, MINOR, TRIVIAL } = failureLevelCount;

  const {
    bugNum = 0, failureAvgTime = 0,
    failureCompletedNum = 0, failureCompletedRate = 0,
    failureMaxTime = 0, failureMinTime = 0, failureNum = 0,
    failureOverdueNum = 0, failureOverdueRate = 0,
    failureTotalTime = 0, failureWorkload = 0,
    oneTimeFailureNum = 0, oneTimeFailureRate = 0,
    totalNum = 0, totalWorkload = 0,
    twoTimeFailureNum = 0, twoTimeFailureRate = 0
  } = data;
  res.chart0Value = {
    yData: [failureNum, failureCompletedNum, failureOverdueNum, oneTimeFailureNum, twoTimeFailureNum]
  };
  res.chart1Value = {
    yData: [failureTotalTime, failureAvgTime, failureMaxTime, failureMinTime]
  };

  res.chart2Value = {
    title: '',
    value: [
      { name: t('functionAnalysis.detail.failures.criticalFailureCount'), value: CRITICAL },
      { name: t('functionAnalysis.detail.failures.majorFailureCount'), value: MAJOR },
      { name: t('functionAnalysis.detail.failures.minorFailureCount'), value: MINOR },
      { name: t('functionAnalysis.detail.failures.trivialFailureCount'), value: TRIVIAL }
    ]
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
