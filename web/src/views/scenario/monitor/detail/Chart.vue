<script lang="ts" setup>
import { onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import * as echarts from 'echarts';

const { t } = useI18n();

interface Props {
  count?: {
    failureNum: number;
    successNum: number;
    successRate: string;
  }
}
const props = withDefaults(defineProps<Props>(), {
  count: () => ({
    failureNum: 0,
    successNum: 0,
    successRate: '0'
  })
});

const successRateRef = ref();

let successRateChart;
const successRateConfig = {
  title: {
    text: '0%',
    left: '35%',
    top: '38%',
    padding: 2,
    // left: '25%',
    // top: '40%',
    itemGap: 47,
    textAlign: 'center',
    textStyle: {
      fontSize: 12,
      fontWeight: 'bolder'
    },
    subtextStyle: {
      fontSize: 12,
      color: '#000'
    }
  },
  tooltip: {
    trigger: 'item'
  },
  legend: {
    top: 'middle',
    right: '10',
    orient: 'vertical',
    itemHeight: 14,
    itemWidth: 14,
    itemGap: 2
  },
  series: [
    {
      name: '',
      type: 'pie',
      radius: ['50%', '70%'],
      center: ['35%', '45%'],
      avoidLabelOverlap: true,
      label: {
        show: true,
        formatter: '{c}'
      },
      itemStyle: {
        borderRadius: 2,
        borderColor: '#fff',
        borderWidth: 1
      },
      emphasis: {
        label: {
          show: true
        }
      },
      labelLine: {
        show: true,
        length: 5
      },
      data: [
        {
          name: t('scenarioMonitor.chart.successCount'),
          value: 0,
          itemStyle: {
            color: '#52C41A'
          }
        },
        {
          name: t('scenarioMonitor.chart.failureCount'),
          value: 0,
          itemStyle: {
            color: 'rgba(245, 34, 45, 1)'
          }
        }
      ]
    }
  ]
};

onMounted(() => {
  successRateChart = echarts.init(successRateRef.value);
  successRateChart.setOption(successRateConfig);
  watch(() => props.count, () => {
    const { failureNum = 0, successNum = 0, successRate = '0' } = props.count;
    successRateConfig.title.text = successRate + '%';
    successRateConfig.series[0].data[0].value = successNum;
    successRateConfig.series[0].data[1].value = failureNum;
    successRateChart.setOption(successRateConfig);
  });
});
</script>
<template>
  <div ref="successRateRef" class="h-25">
  </div>
</template>
