<script lang="ts" setup>
import { computed, inject, onBeforeUnmount, onMounted, ref, watch, Ref } from 'vue';
import * as echarts from 'echarts';
import { RadioGroup } from 'ant-design-vue';
import elementResizeDetector from 'element-resize-detector';
import { getDateArr } from '@/utils/utils';
import { analysis } from '@/api/tester';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

interface Props {
  userInfo?: {[key: string]: string};
}
const erd = elementResizeDetector({ strategy: 'scroll' });
const props = withDefaults(defineProps<Props>(), {
  userInfo: undefined
});
// Inject project information
const projectId = inject<Ref<string>>('projectId', ref(''));
const burnDownOpt = computed(() => [
  {
    value: 'NUM',
    label: t('functionHome.burndownChart.caseCount')
  },
  {
    value: 'WORKLOAD',
    label: t('functionHome.burndownChart.workload')
  }
]);
const burnDownData = ref();
const burnDownTarget = ref('NUM');
const chartRef = ref();
let burnDownEcharts;
const burnDownEchartsConfig = {
  grid: {
    left: '30',
    right: '20',
    bottom: '60',
    top: 20
  },
  legend: {
    show: true,
    bottom: 0,
    type: 'plain'
  },
  tooltip: {
    show: true,
    trigger: 'axis'
  },
  xAxis: {
    type: 'category',
    data: []
  },
  yAxis: {
    type: 'value',
    min: function (value) {
      if (value.max < 1) {
        return 10;
      } else {
        return undefined;
      }
    }
  },
  series: [
    {
      name: t('functionHome.burndownChart.remaining'),
      data: [],
      type: 'line'
    },
    {
      name: t('functionHome.burndownChart.expected'),
      data: [],
      type: 'line',
      smooth: true
    }
  ]
};

const loadChartData = async () => {
  const [error, { data }] = await analysis.getFuncTesterBurndown({
    projectId: projectId.value,
    userId: props.userInfo?.id
  });
  if (error) {
    return;
  }
  burnDownData.value = data;
};

const resizeHandler = () => {
  burnDownEcharts.setOption(burnDownEchartsConfig);
  burnDownEcharts.resize();
};
onMounted(() => {
  burnDownEcharts = echarts.init(chartRef.value);
  burnDownEcharts.setOption(burnDownEchartsConfig);

  watch(() => projectId.value, (newValue) => {
    if (newValue) {
      loadChartData();
    }
  }, {
    immediate: true
  });
  watch([() => burnDownTarget.value, () => burnDownData.value], () => {
    if (burnDownData.value) {
      const xData = (burnDownData.value[burnDownTarget.value]?.expected || []).map(i => i.timeSeries);
      const expectedYData = (burnDownData.value[burnDownTarget.value]?.expected || []).map(i => i.value);
      const remainingYData = (burnDownData.value[burnDownTarget.value]?.remaining || []).map(i => i.value);
      burnDownEchartsConfig.xAxis.data = xData;
      burnDownEchartsConfig.series[0].data = remainingYData;
      burnDownEchartsConfig.series[1].data = expectedYData;
    } else {
      burnDownEchartsConfig.xAxis.data = [];
      burnDownEchartsConfig.series[0].data = [];
      burnDownEchartsConfig.series[1].data = [];
    }
    if (burnDownEchartsConfig.xAxis.data.length === 0) {
      burnDownEchartsConfig.xAxis.data = getDateArr();
      burnDownEchartsConfig.series[0].data = [0, 0, 0, 0, 0, 0, 0];
    }
    burnDownEcharts.setOption(burnDownEchartsConfig);
  });
  erd.listenTo(chartRef.value, resizeHandler);
});

onBeforeUnmount(() => {
  erd.removeListener(chartRef.value, resizeHandler);
});
</script>
<template>
  <div class="pt-1.5 flex flex-col">
    <div class="text-3.5 font-semibold flex justify-between">
      {{ t('functionHome.burndownChart.title') }}
      <RadioGroup v-model:value="burnDownTarget" :options="burnDownOpt">
      </RadioGroup>
    </div>
    <div ref="chartRef" class="border rounded p-2 flex-1 mt-3">
    </div>
  </div>
</template>
