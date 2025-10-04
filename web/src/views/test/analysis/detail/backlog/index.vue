<script lang="ts" setup>
import { defineAsyncComponent, onMounted, ref, Ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

interface Props {
  analysisInfo?: Record<string, any>;
}

const props = withDefaults(defineProps<Props>(), {
  analysisInfo: undefined
});

const totalChartRef = ref();
const chartListRef: Ref[] = [];

const EChart = defineAsyncComponent(() => import('./EChart.vue'));

const getChartData = (data) => {
  const res = {} as any;
  const {
    backloggedCompletionTime = 0,
    backloggedNum = 0,
    backloggedRate = 0,
    backloggedWorkload = 0,
    backloggedWorkloadRate = 0,
    dailyProcessedNum = 0,
    dailyProcessedWorkload = 0,
    processedInDay = 0,
    totalNum = 0,
    totalWorkload = 0
  } = data;

  res.overdueAssessmentData = data;
  res.chart0Value = {
    yData0: [backloggedNum, backloggedWorkload],
    yData1: [totalNum, totalWorkload]
  };
  res.chart1Value = {
    title: backloggedRate + '%',
    value: [
      { name: t('testAnalysis.detail.backlogTasks.notBackloggedCount'), value: totalNum - backloggedNum },
      { name: t('testAnalysis.detail.backlogTasks.backloggedCount'), value: backloggedNum }
    ]
  };

  res.chart2Value = {
    title: backloggedWorkloadRate + '%',
    value: [
      {
        name: t('testAnalysis.detail.backlogTasks.notBackloggedWorkload'),
        value: totalWorkload - backloggedWorkload
      },
      { name: t('testAnalysis.detail.backlogTasks.backloggedWorkload'), value: backloggedWorkload }
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
