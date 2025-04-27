<script lang="ts" setup>
import { Modal } from '@xcan-angus/vue-ui';
import { computed, onMounted, ref, watch } from 'vue';
import * as echarts from 'echarts';
import { http, TESTER } from '@xcan-angus/tools';
import { RadioGroup } from 'ant-design-vue';

interface Props {
  visible: boolean;
  sprintId: string;
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  sprintId: ''
});

const emits = defineEmits<{(e: 'update:visible', value: boolean):void}>();

const cancel = () => {
  emits('update:visible', false);
};

const burnDownOpt = computed(() => [
  {
    value: 'NUM',
    label: '任务数'
  },
  {
    value: 'WORKLOAD',
    label: '工作量'
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
    type: 'value'
  },
  series: [
    {
      name: '剩余',
      data: [],
      type: 'line'
    },
    {
      name: '期望',
      data: [],
      type: 'line',
      smooth: true
    }
  ]
};

const loadChartData = async () => {
  const [error, { data }] = await http.get(`${TESTER}/analysis/task/sprint/${props.sprintId}/burndown`);
  if (error) {
    return;
  }
  burnDownData.value = data;
};
onMounted(() => {
  burnDownEcharts = echarts.init(chartRef.value);
  burnDownEcharts.setOption(burnDownEchartsConfig);

  watch(() => props.visible, (newValue) => {
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
    burnDownEcharts.setOption(burnDownEchartsConfig);
  });
});

</script>
<template>
  <Modal
    :visible="props.visible"
    :footer="null"
    :width="800"
    title="燃尽图"
    @cancel="cancel">
    <div class="pt-1.5">
      <RadioGroup v-model:value="burnDownTarget" :options="burnDownOpt" />
      <div ref="chartRef" class="border rounded p-2 my-3 h-60">
      </div>
    </div>
  </Modal>
</template>
