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
  const { CRITICAL = 0, MAJOR = 0, MINOR = 0, TRIVIAL = 0 } = data.bugLevelCount || {};
  const {
    totalNum = 0, validBugNum = 0, invalidBugNum = 0, missingBugNum = 0, validBugRate = 0,
    bugWorkload = 0, totalWorkload = 0, bugWorkloadRate = 0, missingBugRate = 0
  } = data;
  res.value = data;
  res.chart0Value = {
    yData: [totalNum, validBugNum, invalidBugNum, missingBugNum]
  };
  res.chart1Value = {
    title: '',
    value: [
      { name: t('functionAnalysis.detail.bugs.criticalBugs'), value: CRITICAL },
      { name: t('functionAnalysis.detail.bugs.majorBugs'), value: MAJOR },
      { name: t('functionAnalysis.detail.bugs.minorBugs'), value: MINOR },
      { name: t('functionAnalysis.detail.bugs.trivialBugs'), value: TRIVIAL }
    ]
  };

  res.chart2Value = {
    title: validBugRate + '%',
    value: [
      { name: t('functionAnalysis.detail.bugs.invalidBugs'), value: invalidBugNum },
      { name: t('functionAnalysis.detail.bugs.validBugs'), value: validBugNum }
    ]
  };

  res.chart3Value = {
    title: missingBugRate + '%',
    value: [
      { name: t('functionAnalysis.detail.bugs.nonMissingBugs'), value: totalNum - missingBugNum },
      { name: t('functionAnalysis.detail.bugs.missingBugCount'), value: missingBugNum }
    ]
  };

  res.chart4Value = {
    title: bugWorkloadRate + '%',
    value: [
      { name: t('functionAnalysis.detail.bugs.nonBugWorkload'), value: totalWorkload - bugWorkload },
      { name: t('functionAnalysis.detail.bugs.bugWorkload'), value: bugWorkload }
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
