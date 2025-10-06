<script lang="ts" setup>
import { defineAsyncComponent, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';

interface Props {
  analysisInfo?: Record<string, any>;
}

const props = withDefaults(defineProps<Props>(), {
  analysisInfo: undefined
});

const { t } = useI18n();

const EChart = defineAsyncComponent(() => import('./EChart.vue'));

const getChartData = (data) => {
  const res = {} as any;

  if (!data) {
    console.warn('getChartData: data is undefined or null');
    return res;
  }

  const { lastMonth = {}, last7Days = {}, today = {} } = data;

  res.chart0Value = {
    yData0: [
      today.completedNum || 0,
      last7Days.completedNum || 0,
      lastMonth.completedNum || 0
    ],
    yData1: [
      today.overdueNum || 0,
      last7Days.overdueNum || 0,
      lastMonth.overdueNum || 0
    ],
    yData2: [
      today.totalNum || 0,
      last7Days.totalNum || 0,
      lastMonth.totalNum || 0
    ]
  };
  res.chart1Value = {
    yData0: [
      today.completedWorkload || 0,
      last7Days.completedWorkload || 0,
      lastMonth.completedWorkload || 0
    ],
    yData1: [
      today.overdueWorkload || 0,
      last7Days.overdueWorkload || 0,
      lastMonth.overdueWorkload || 0
    ],
    yData2: [
      today.totalWorkload || 0,
      last7Days.totalWorkload || 0,
      lastMonth.totalWorkload || 0
    ]
  };
  return res;
};

const totalValue = ref({});

interface PersonValue {
  userName: string;
  chartData: any;
  id: string;
}

const personValues = ref<PersonValue[]>([]);

onMounted(() => {
  watch(() => props.analysisInfo, (newValue) => {
    if (newValue) {
      try {
        const sourceData = newValue.data?.totalOverview || {};
        totalValue.value = getChartData(sourceData);

        if (newValue?.containsUserAnalysis && newValue.data?.assigneesOverview) {
          const sourceData = newValue.data.assigneesOverview;
          const assignees = newValue.data?.assignees || [];

          personValues.value = [];

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
      } catch (error) {
        totalValue.value = {};
        personValues.value = [];
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

const totalChartRef = ref<any>();
const chartListRef = ref<any[]>([]);
defineExpose({
  resize: () => {
    totalChartRef.value?.resize();
    chartListRef.value.forEach(item => {
      item?.resize();
    });
  }
});

</script>
<template>
  <div class="overdue-analysis-page">
    <div class="analysis-section">
      <div class="section-header">
        <h3 class="section-title">{{ t('chart.total') }}</h3>
      </div>
      <EChart
        ref="totalChartRef"
        v-bind="totalValue"
        class="analysis-chart" />
    </div>

    <div
      v-for="item in personValues"
      :key="item.id"
      class="analysis-section">
      <div class="section-header">
        <h3 class="section-title">{{ item.userName }}</h3>
      </div>
      <EChart
        :ref="(el) => { if (el) chartListRef.push(el) }"
        v-bind="item.chartData"
        class="analysis-chart" />
    </div>
  </div>
</template>

<style scoped>
.overdue-analysis-page {
  padding: 6px;
}

.analysis-section {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  margin-bottom: 12px;
  overflow: hidden;
  transition: all 0.3s ease;
}

.analysis-section:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
  transform: translateY(-2px);
}

.section-header {
  padding: 12px 16px 8px;
  border-bottom: 1px solid #f0f0f0;
  background: linear-gradient(135deg, #fafafa 0%, #f5f5f5 100%);
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #262626;
  margin: 0;
  line-height: 1.3;
}

.analysis-chart {
  padding: 0;
}

@media (max-width: 768px) {
  .overdue-analysis-page {
    padding: 8px;
  }

  .analysis-section {
    margin-bottom: 8px;
  }

  .section-header {
    padding: 10px 12px 6px;
  }

  .section-title {
    font-size: 14px;
  }
}
</style>
