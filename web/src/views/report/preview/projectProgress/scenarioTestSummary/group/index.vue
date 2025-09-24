<script setup lang="ts">
import { onMounted, ref, watch } from 'vue';
import * as echarts from 'echarts';
import { useI18n } from 'vue-i18n';
import { ReportContent } from '../../PropsType';

type Props = {
  projectInfo: { [key: string]: any };
  userInfo: { [key: string]: any };
  appInfo: { [key: string]: any };
  dataSource: ReportContent;
}

const { t } = useI18n();

const props = withDefaults(defineProps<Props>(), {
  projectInfo: undefined,
  userInfo: undefined,
  appInfo: undefined,
  dataSource: undefined
});

const taskStatusRef = ref();
const priorityRef = ref();

const taskStatusOption = {
  tooltip: {
    trigger: 'axis',
    axisPointer: { type: 'shadow' },
    textStyle: {
      fontSize: 12
    }
  },
  grid: {
    left: '0',
    right: '0',
    top: '35px',
    bottom: '0',
    containLabel: true
  },
  xAxis: [{
    type: 'category',
    data: [],
    axisTick: { alignWithLabel: true }
  }],
  yAxis: [{ type: 'value' }],
  series: [{
    name: t('reportPreview.projectProgress.scenarioTestSummary.groupStats.quantity'),
    type: 'bar',
    barWidth: '20px',
    data: [],
    itemStyle: {
      color: '#2D8EFF',
      borderRadius: [4, 4, 0, 0]
    },
    label: {
      show: true, // 显示数值
      position: 'top' // 数值显示在柱形的顶部
    }
  }]
};

const priorityOptions = {
  title: {
    text: 0,
    subtext: t('chart.total'),
    left: '29.5%',
    top: '40%',
    padding: 2,
    textAlign: 'center',
    textStyle: {
      fontSize: 14,
      width: 120,
      fontWeight: 'bolder'
    },
    subtextStyle: {
      fontSize: 12
    }
  },
  tooltip: {
    trigger: 'item',
    axisPointer: { type: 'shadow' },
    textStyle: {
      fontSize: 12
    }
  },
  legend: {
    top: 'middle',
    right: '0',
    orient: 'vertical',
    itemHeight: 14,
    itemWidth: 14,
    itemGap: 4
  },
  color: ['#67D7FF', '#FFB925', '#52C41A', '#2D8EFF'],
  series: [
    {
      name: '',
      type: 'pie',
      radius: ['40%', '55%'],
      center: ['30%', '50%'],
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
        { name: t('reportPreview.projectProgress.scenarioTestSummary.groupStats.testTypes.perfTest'), value: 0 },
        { name: t('reportPreview.projectProgress.scenarioTestSummary.groupStats.testTypes.stabilityTest'), value: 0 },
        { name: t('reportPreview.projectProgress.scenarioTestSummary.groupStats.testTypes.funcTest'), value: 0 },
        { name: t('reportPreview.projectProgress.scenarioTestSummary.groupStats.testTypes.customTest'), value: 0 }
      ]
    }
  ]
};

let statusEchart;
let priorityEchart;

onMounted(() => {
  watch(() => props.dataSource, newValue => {
    const sceByPluginName = newValue?.content?.scenarios?.sceByPluginName;
    taskStatusOption.xAxis![0].data = [];
    taskStatusOption.series![0].data = [];
    if (sceByPluginName) {
      const keys = Object.keys(sceByPluginName);
      if (keys.length) {
        keys.forEach(key => {
            taskStatusOption.xAxis![0].data.push(key);
            taskStatusOption.series![0].data.push(sceByPluginName[key]);
        });
      } else {
          taskStatusOption.xAxis![0].data = ['Http', 'WebSocket', 'Jdbc', 'Tcp'];
          taskStatusOption.series![0].data = [0, 0, 0, 0];
      }
    }

    const sceByScriptType = newValue?.content?.scenarios?.sceByScriptType;
    if (sceByScriptType) {
      const {
        TEST_STABILITY = 0,
        TEST_PERFORMANCE = 0,
        TEST_CUSTOMIZATION = 0,
        TEST_FUNCTIONALITY = 0
      } = sceByScriptType;
      const methodData = [
        TEST_PERFORMANCE,
        TEST_STABILITY,
        TEST_FUNCTIONALITY,
        TEST_CUSTOMIZATION
      ];
      priorityOptions.series[0].data.forEach((item, idx) => {
        item.value = methodData[idx];
      });
    } else {
      priorityOptions.series[0].data.forEach(item => {
        item.value = 0;
      });
    }
    const enabledTestNum = newValue?.content?.scenarios?.testScenarios?.totalScenarioNum;
    priorityOptions.title.text = enabledTestNum;

    statusEchart = echarts.init(taskStatusRef.value);
    priorityEchart = echarts.init(priorityRef.value);
    statusEchart.setOption(taskStatusOption);
    priorityEchart.setOption(priorityOptions);
  }, {
    immediate: true
  });
});

</script>

<template>
  <h1 class="text-theme-title font-medium mb-3">
    <span class="text-3 text-theme-title font-medium">{{ t('reportPreview.projectProgress.scenarioTestSummary.groupStats.byProtocol') }}</span>
  </h1>
  <div ref="taskStatusRef" class="flex-1 h-50 w-120 mb-7">
  </div>

  <h1 class="text-theme-title font-medium mb-3">
    <span class="text-3 text-theme-title font-medium">{{ t('reportPreview.projectProgress.scenarioTestSummary.groupStats.byTestType') }}</span>
  </h1>
  <div ref="priorityRef" class="flex-1 h-50  w-120">
  </div>
</template>

<style scoped>
.content-text-container {
  text-indent: 2em;
}
</style>
