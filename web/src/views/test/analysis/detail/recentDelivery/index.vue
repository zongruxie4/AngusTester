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

  const { lastMonth, lastWeek, today } = data;

  res.chart0Value = {
    yData0: [
      today.completedNum || 0,
      lastWeek.completedNum || 0,
      lastMonth.completedNum || 0
    ],
    yData1: [
      today.overdueNum || 0,
      lastWeek.overdueNum || 0,
      lastMonth.overdueNum || 0
    ],
    yData2: [
      today.totalNum || 0,
      lastWeek.totalNum || 0,
      lastMonth.totalNum || 0
    ]
  };
  res.chart1Value = {
    yData0: [
      today.completedWorkload || 0,
      lastWeek.completedWorkload || 0,
      lastMonth.completedWorkload || 0
    ],
    yData1: [
      today.overdueWorkload || 0,
      lastWeek.overdueWorkload || 0,
      lastMonth.overdueWorkload || 0
    ],
    yData2: [
      today.totalWorkload || 0,
      lastWeek.totalWorkload || 0,
      lastMonth.totalWorkload || 0
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
