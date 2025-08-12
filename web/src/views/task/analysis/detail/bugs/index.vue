<script lang="ts" setup>
import { defineAsyncComponent, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';

interface Props {
  analysisInfo?: Record<string, any>;
}

const { t } = useI18n();
const props = withDefaults(defineProps<Props>(), {
  analysisInfo: undefined
});

const Echart = defineAsyncComponent(() => import('./echart.vue'));

const getChartData = (data) => {
  const res = {};
  //   bugLevelCount
  // :
  // {CRITICAL: "2", MAJOR: "1", MINOR: "152", TRIVIAL: "0"}
  // bugLevelRate
  // :
  // {CRITICAL: "1.29", MAJOR: "0.65", MINOR: "98.06", TRIVIAL: "0.0"}
  // bugNum
  // :
  // "159"
  // bugRate
  // :
  // "0.0"
  // bugWorkload
  // :
  // "55.5"
  // bugWorkloadRate
  // :
  // "21.06"
  // invalidBugNum
  // :
  // "4"
  // invalidBugRate
  // :
  // "2.52"
  // missingBugNum
  // :
  // "0"
  // missingBugRate
  // :
  // "0.0"
  // oneTimePassedBugNum
  // :
  // "152"
  // oneTimePassedBugRate
  // :
  // "98.06"
  // totalNum
  // :
  // "506"
  // totalWorkload
  // :
  // "263.5"
  // validBugNum
  // :
  // "155"
  // validBugRate
  // :
  // "97.48"
  const { CRITICAL = 0, MAJOR = 0, MINOR = 0, TRIVIAL = 0 } = data.bugLevelCount || {};
  const { totalNum = 0, validBugNum = 0, invalidBugNum = 0, missingBugNum = 0, validBugRate = 0, bugWorkload = 0, totalWorkload = 0, bugWorkloadRate = 0, missingBugRate = 0 } = data;
  res.chart0Value = {
    yData: [totalNum, validBugNum, invalidBugNum, missingBugNum]
  };
  res.chart1Value = {
    title: '',
    value: [{ name: t('taskAnalysis.detail.bugs.criticalBugs'), value: CRITICAL }, { name: t('taskAnalysis.detail.bugs.majorBugs'), value: MAJOR }, { name: t('taskAnalysis.detail.bugs.minorBugs'), value: MINOR }, { name: t('taskAnalysis.detail.bugs.trivialBugs'), value: TRIVIAL }]
  };

  res.chart2Value = {
    title: validBugRate + '%',
    value: [{ name: t('taskAnalysis.detail.bugs.invalidBugCount'), value: invalidBugNum }, { name: t('taskAnalysis.detail.bugs.validBugCount'), value: validBugNum }]
  };

  res.chart3Value = {
    title: missingBugRate + '%',
    value: [{ name: t('taskAnalysis.detail.bugs.nonMissingBugs'), value: totalNum - missingBugNum }, { name: t('taskAnalysis.detail.bugs.missingBugCount'), value: missingBugNum }]
  };

  res.chart4Value = {
    title: bugWorkloadRate + '%',
    value: [{ name: t('taskAnalysis.detail.bugs.nonBugWorkload'), value: totalWorkload - bugWorkload }, { name: t('taskAnalysis.detail.bugs.bugWorkload'), value: bugWorkload }]
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
