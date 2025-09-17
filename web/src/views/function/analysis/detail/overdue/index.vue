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

const Echart = defineAsyncComponent(() => import('./EChart.vue'));

const getChartData = (data) => {
  const res = {};

  const {
    dailyProcessedWorkload = 0, overdueNum = 0,
    overdueRate = 0, overdueTime = 0,
    overdueWorkload = 0, overdueWorkloadProcessingTime = 0,
    overdueWorkloadRate = 0, riskLevel = 0,
    totalNum = 0, totalWorkload = 0
  } = data;

  res.overdueAssessmentData = data;
  res.chart1Value = {
    title: overdueRate + '%',
    value: [
      { name: t('functionAnalysis.detail.overdueAssessment.notOverdueCount'), value: totalNum - overdueNum },
      { name: t('functionAnalysis.detail.overdueAssessment.overdueCount'), value: overdueNum }
    ]
  };

  res.chart2Value = {
    title: overdueWorkloadRate + '%',
    value: [
      { name: t('functionAnalysis.detail.overdueAssessment.notOverdueWorkload'), value: totalWorkload - overdueWorkload },
      { name: t('functionAnalysis.detail.overdueAssessment.overdueWorkload'), value: overdueWorkload }
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
    <div>
      <div class="font-semibold pl-3">
        {{ t('functionAnalysis.detail.overdueAssessment.total') }}
      </div>
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
  </div>
</template>
