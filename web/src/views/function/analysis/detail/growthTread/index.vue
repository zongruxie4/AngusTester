<script lang="ts" setup>
import { defineAsyncComponent, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { getDateArr } from '@/utils/utils';

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

  const { apiTestNum = 0, requirementNum = 0, scenarioTestNum = 0, storyNum = 0, bugNum = 0, taskNum = 0, totalNum = 0 } = data;
  res.overdueAssessmentData = data;
  res.chart0Value = {
    yData: [requirementNum, storyNum, taskNum, bugNum, apiTestNum, scenarioTestNum, totalNum]
  };

  // const keys = Object.keys(data.timeSeries);
  let xData = [];
  let series = [];
  if (data.timeSeries.length) {
    data.timeSeries.forEach(i => {
      if (!xData.includes(i.timeSeries)) {
        xData.push(i.timeSeries);
      }
    });

    xData.sort((a, b) => {
      return a > b ? 1 : a < b ? -1 : 0;
    });
    series = [{
      name: t('functionAnalysis.detail.taskGrowthTread.cases'),
      data: xData.map(i => {
        const target = data.timeSeries.find(item => item.timeSeries === i);
        if (target) {
          return target.value;
        } else {

        }
      }),
      type: 'line',
      smooth: true,
      connectNulls: true,
      itemStyle: {
        color: 'orange'
      }
    }];
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
      {{ t('functionAnalysis.detail.taskGrowthTread.total') }}
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
</template>
