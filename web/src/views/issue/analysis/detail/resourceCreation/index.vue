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

const EChart = defineAsyncComponent(() => import('./EChart.vue'));

const targetDataCategery = {
  TOTAL: t('common.total'),
  SPRINT: t('common.sprint'),
  TASK_SPRINT: t('common.sprint'),
  TASK: t('common.issue'),
  ANALYSIS: t('common.analysis'),
  BACKLOG: t('common.backlog'),
  MEETING: t('common.meeting')
};

const getChartData = (data) => {
  const res = {} as any;

  const { analysisNum = 0, backlogNum = 0, meetingNum = 0, sprintNum = 0, taskNum = 0, totalNum = 0 } = data;
  res.overdueAssessmentData = data;
  res.chart0Value = {
    yData: [backlogNum, sprintNum, taskNum, meetingNum, analysisNum, totalNum]
  };

  const keys: string[] = Object.keys(data.timeSeries || {});
  let xData: string[] = [];
  let series: any[] = [];
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
    series = keys.map((key: string) => {
      return {
        name: targetDataCategery[key],
        data: xData.map((i: string) => {
          const target = (data.timeSeries[key] || []).find((item: any) => item.timeSeries === i);
          if (target) {
            return target.value;
          }
          return 0;
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

interface PersonValue {
  userName: string;
  chartData: any;
  id: string;
}

const personValues = ref<PersonValue[]>([]);

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
