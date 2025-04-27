<script lang="ts" setup>
import { onMounted, ref, watch } from 'vue';
import * as eCharts from 'echarts';

interface Props {

  overdueAssessmentData: Record<string, any>;
  chart0Value: {
    yData: number[]
  },
  chart1Value: {
    yData: number[]
  },
}
const props = withDefaults(defineProps<Props>(), {
  overdueAssessmentData: () => ({}),
  chart0Value: () => ({
    yData: [0, 0]
  }),
  chart1Value: () => ({
    yData: [0, 0]
  })
});

const unplannedTaskRef = ref();
const unplannedWorkloadRef = ref();

let unplannedTaskRefEchart;
let unplannedWorkloadRefEchart;

// 用例数
const unplannedTaskEchartConfig = {
  title: {
    text: '用例数',
    bottom: 0,
    left: 'center',
    textStyle: {
      fontSize: 12
    }
  },
  grid: {
    left: '40',
    right: '30',
    bottom: '50',
    top: '40'
  },
  xAxis: {
    type: 'category',
    data: ['总数', '计划外数', '计划外完成数'],
    axisLabel: {
      interval: 0,
      overflow: 'break'
    }
  },
  yAxis: [{
    type: 'value'
  }],
  tooltip: {
    show: true
  },
  legend: {
    show: true,
    top: 0
  },
  series: [
    {
      name: '',
      itemStyle: {
        color: 'rgba(45, 142, 255, 1)',
        borderRadius: [5, 5, 0, 0]
      },
      barGap: 0,
      data: [0, 0, 0, 0, 0, 0, 0, 0],
      type: 'bar',
      barMaxWidth: '20',
      label: {
        show: true,
        position: 'top'
      }
    }
  ]
};

const unplannedWorkloadEchartConfig = JSON.parse(JSON.stringify({
  ...unplannedTaskEchartConfig,
  xAxis: {
    type: 'category',
    data: ['总工作量', '计划外工作量', '计划外完成工作量'],
    axisLabel: {
      interval: 0,
      overflow: 'break'
    }
  },
  title: {
    text: '工作量',
    bottom: 0,
    left: 'center',
    textStyle: {
      fontSize: 12
    }
  }
}));

onMounted(() => {
  unplannedTaskRefEchart = eCharts.init(unplannedTaskRef.value);

  unplannedWorkloadRefEchart = eCharts.init(unplannedWorkloadRef.value);

  watch([() => props.chart0Value, () => props.chart1Value], () => {
    unplannedTaskEchartConfig.series[0].data = props.chart0Value.yData;
    unplannedWorkloadEchartConfig.series[0].data = props.chart1Value.yData;

    unplannedTaskRefEchart.setOption(unplannedTaskEchartConfig);
    unplannedWorkloadRefEchart.setOption(unplannedWorkloadEchartConfig);
  }, {
    immediate: true,
    deep: true
  });
});

defineExpose({
  resize: () => {
    unplannedTaskRefEchart.resize();
    unplannedWorkloadRefEchart.resize();
  }
});

</script>
<template>
  <div class="flex">
    <div class="px-3 w-50 flex items-center">
      <div class="text-center flex-1">
        <div class="font-semibold "><span class="text-5 text-status-pending">{{ props.overdueAssessmentData.unplannedWorkloadProcessingTime || 0 }}</span>小时</div>
        <div>
          计划外用例预计耗时
        </div>
      </div>
    </div>
    <div ref="unplannedTaskRef" class="flex-1 h-40"></div>
    <div ref="unplannedWorkloadRef" class="flex-1 h-40"></div>
  </div>
</template>
<style scoped>
.risk-level-LOW {
  color: 'gold'
}

.risk-level-HIGH {
  color: 'red'
}

.risk-level-NONE {
  color: '#52C41A'
}
</style>
