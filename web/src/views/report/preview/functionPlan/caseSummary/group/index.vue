<script setup lang="ts">
import { onMounted, ref, watch } from 'vue';
import * as echarts from 'echarts';

import { ReportContent } from '../../PropsType';

type Props = {
  projectInfo: { [key: string]: any };
  userInfo: { [key: string]: any };
  appInfo: { [key: string]: any };
  dataSource: ReportContent;
}

const props = withDefaults(defineProps<Props>(), {
  projectInfo: undefined,
  userInfo: undefined,
  appInfo: undefined,
  dataSource: undefined
});

const caseStatusRef = ref();
const caseBarEchartsConfig = {
  title: {
    text: '',
    textStyle: {
      fontSize: 12
    }
  },
  tooltip: {
    trigger: 'axis',
    axisPointer: {
      type: 'shadow'
    }
  },
  legend: {},
  grid: {
    top: '8%',
    left: '3%',
    right: '6%',
    bottom: '3%',
    containLabel: true
  },
  xAxis: {
    type: 'value',
    splitLine: { show: false }
  },
  yAxis: {
    type: 'category',
    axisTick: { show: false },
    splitLine: { show: false },
    axisLine: { show: false },
    data: ['已取消', '阻塞', '测试未通过', '测试通过', '待测试']
  },
  series: [
    {
      type: 'bar',
      showBackground: true,
      barMaxWidth: '16',
      data: [{
        value: 0,
        itemStyle: {
          color: 'rgba(217, 217, 217, 1)'
        }
      }, {
        value: 0,
        itemStyle: {
          color: 'rgba(255, 165, 43, 1)'
        }
      }, {
        value: 0,
        itemStyle: {
          color: 'rgba(245, 34, 45, 1)'
        }
      }, {
        value: 0,
        itemStyle: {
          color: 'rgba(82, 196, 26, 1)'
        }
      }, {
        value: 0,
        itemStyle: {
          color: 'rgba(45, 142, 255, 1)'
        }
      }]
    }
  ]
};
const priorityRef = ref();

const priorityOptions = {
  tooltip: {
    trigger: 'item',
    axisPointer: { type: 'shadow' },
    textStyle: {
      fontSize: 12
    }
  },
  legend: {
    top: 'middle',
    right: 0,
    orient: 'vertical',
    itemHeight: 14,
    itemWidth: 14,
    itemGap: 10,
    formatter: function (name) {
      return name;
    }
  },
  color: ['#F5222D', '#FF8100', '#FFB925', '#52C41A', '#67D7FF'],
  series: [
    {
      name: '',
      type: 'pie',
      center: ['50%', '50%'],
      radius: ['40%', '60%'],
      avoidLabelOverlap: true,
      itemStyle: {
        borderRadius: 4,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: {
        show: true,
        formatter: '{c}'
      },
      labelLine: {
        show: true
      },
      emphasis: {
        label: {
          show: false,
          fontSize: 12,
          fontWeight: 'normal'
        }
      },
      data: [
        {
          name: '最高',
          value: 0
        },
        {
          name: '高',
          value: 0
        },
        {
          name: '中',
          value: 0
        },
        {
          name: '底',
          value: 0
        },
        {
          name: '最底',
          value: 0
        }
      ]
    }
  ]
};

let statusEchart;
let priorityEchart;

onMounted(() => {
  watch(() => props.dataSource, newValue => {
    const caseByTestResult = newValue?.content?.cases?.totalTestResultOverview || {};
    const { PENDING = 0, PASSED = 0, NOT_PASSED = 0, BLOCKED = 0, CANCELED = 0 } = caseByTestResult || {};
    const barData = [CANCELED, BLOCKED, NOT_PASSED, PASSED, PENDING];
    caseBarEchartsConfig.series[0].data.forEach((item, idx) => {
      item.value = barData[idx];
    });

    const totalPriorityOverview = newValue?.content?.cases?.totalPriorityOverview || {};
    const { HIGHEST = 0, HIGH = 0, MEDIUM = 0, LOW = 0, LOWEST = 0 } = totalPriorityOverview;
    const pieData = [HIGHEST, HIGH, MEDIUM, LOW, LOWEST];
    priorityOptions.series[0].data.forEach((item, idx) => {
      item.value = +pieData[idx];
    });

    statusEchart = echarts.init(caseStatusRef.value);
    priorityEchart = echarts.init(priorityRef.value);
    statusEchart.setOption(caseBarEchartsConfig);
    priorityEchart.setOption(priorityOptions);
  }, {
    immediate: true
  });
});

</script>

<template>
  <div>
    <h1 class="text-theme-title font-medium mb-3">
      <span class="text-3 text-theme-title font-medium">按状态分组</span>
    </h1>
    <div ref="caseStatusRef" class="flex-1 h-50 w-120 mb-7">
    </div>

    <h1 class="text-theme-title font-medium mb-3">
      <span class="text-3 text-theme-title font-medium">按优先级分组</span>
    </h1>
    <div ref="priorityRef" class="flex-1 h-50  w-120">
    </div>
  </div>
</template>

<style scoped>
.content-text-container {
  text-indent: 2em;
}
</style>
