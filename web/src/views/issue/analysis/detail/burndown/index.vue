<script lang="ts" setup>
import { defineAsyncComponent, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { getDateArr } from '@/utils/utils';

interface Props {
  analysisInfo?: Record<string, any>;
}

const props = withDefaults(defineProps<Props>(), {
  analysisInfo: undefined
});

const { t } = useI18n();

const EChart = defineAsyncComponent(() => import('./EChart.vue'));

const getChartData = (data, target = 'NUM') => {
  const res = {} as any;
  if (data) {
    const xData = (data[target]?.expected || []).map(i => i.timeSeries);
    const expectedYData = (data[target]?.expected || []).map(i => i.value);
    const remainingYData = (data[target]?.remaining || []).map(i => i.value);
    res.xData = xData;
    res.yData = [remainingYData, expectedYData];
  } else {
    res.xData = [];
    res.yData = [[], []];
  }
  if (res.xData.length === 0) {
    res.xData = getDateArr();
    res.yData = [[0, 0, 0, 0, 0, 0, 0], []];
  }
  return res;
};

interface PersonValue {
  userName: string;
  chartData: any;
  id: string;
}

const totalValue = ref({});
const personValues = ref<PersonValue[]>([]);

onMounted(() => {
  watch(() => props.analysisInfo, (newValue) => {
    if (newValue) {
      const sourceData = newValue.data?.totalBurnDownCharts || {};
      totalValue.value.chart0Data = getChartData(sourceData, 'NUM');
      totalValue.value.chart1Data = getChartData(sourceData, 'WORKLOAD');

      if (newValue?.containsUserAnalysis) {
        const sourceData = newValue.data?.assigneesBurnDownCharts || {};
        const assignees = newValue.data?.assignees || [];
        Object.keys(sourceData).forEach(userId => {
          const viewData = sourceData[userId] || {};

          const chartData = {
            chart0Data: getChartData(viewData, 'NUM'),
            chart1Data: getChartData(viewData, 'WORKLOAD')
          };

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
