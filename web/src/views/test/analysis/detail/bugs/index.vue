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
    totalNum = 0, validBugNum = 0, invalidBugNum = 0, escapedBugNum = 0, validBugRate = 0,
    bugWorkload = 0, totalWorkload = 0, bugWorkloadRate = 0, escapedBugRate = 0
  } = data;
  res.value = data;
  res.chart0Value = {
    yData: [totalNum, validBugNum, invalidBugNum, escapedBugNum]
  };
  res.chart1Value = {
    title: '',
    value: [
      { name: t('testAnalysis.detail.bugs.chartLabels.criticalBugs'), value: CRITICAL },
      { name: t('testAnalysis.detail.bugs.chartLabels.majorBugs'), value: MAJOR },
      { name: t('testAnalysis.detail.bugs.chartLabels.minorBugs'), value: MINOR },
      { name: t('testAnalysis.detail.bugs.chartLabels.trivialBugs'), value: TRIVIAL }
    ]
  };

  res.chart2Value = {
    title: validBugRate + '%',
    value: [
      { name: t('testAnalysis.detail.bugs.chartLabels.invalidBugs'), value: invalidBugNum },
      { name: t('testAnalysis.detail.bugs.chartLabels.validBugs'), value: validBugNum }
    ]
  };

  res.chart3Value = {
    title: escapedBugRate + '%',
    value: [
      { name: t('testAnalysis.detail.bugs.chartLabels.nonEscapedBugs'), value: totalNum - escapedBugNum },
      { name: t('testAnalysis.detail.bugs.chartLabels.escapedBugsCount'), value: escapedBugNum }
    ]
  };

  res.chart4Value = {
    title: bugWorkloadRate + '%',
    value: [
      { name: t('testAnalysis.detail.bugs.chartLabels.nonBugWorkload'), value: totalWorkload - bugWorkload },
      { name: t('testAnalysis.detail.bugs.chartLabels.bugWorkload'), value: bugWorkload }
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
