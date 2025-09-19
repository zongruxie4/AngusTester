<script lang="ts" setup>
import { Modal } from '@xcan-angus/vue-ui';
import { computed, onMounted, ref, watch } from 'vue';
import * as echarts from 'echarts';
import { RadioGroup } from 'ant-design-vue';
import { analysis } from '@/api/tester';
import { useI18n } from 'vue-i18n';

// composables
const { t } = useI18n();

// interfaces
interface Props {
  visible: boolean;
  planId: string;
}

// props and emits
const props = withDefaults(defineProps<Props>(), {
  visible: false,
  planId: ''
});

const emits = defineEmits<{(e: 'update:visible', value: boolean):void}>();

// reactive data
const burndownData = ref();
const burndownTarget = ref('NUM');
const chartRef = ref();
let burndownChartInstance;

// computed properties
const burndownOptions = computed(() => [
  {
    value: 'NUM',
    label: t('functionPlan.comp.burndownChart.caseCount')
  },
  {
    value: 'WORKLOAD',
    label: t('functionPlan.comp.burndownChart.workload')
  }
]);

// chart configuration
const burndownChartConfig = {
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
      name: t('functionPlan.comp.burndownChart.remaining'),
      data: [],
      type: 'line'
    },
    {
      name: t('functionPlan.comp.burndownChart.expected'),
      data: [],
      type: 'line',
      smooth: true
    }
  ]
};

/**
 * Handles modal cancel event and emits visibility update
 */
const handleCancel = () => {
  emits('update:visible', false);
};

/**
 * Loads burndown chart data from API based on plan ID
 */
const loadBurndownData = async () => {
  const [error, { data }] = await analysis.getFuncPlanBurndown(props.planId);
  if (error) {
    return;
  }
  burndownData.value = data;
};

// lifecycle hooks
onMounted(() => {
  burndownChartInstance = echarts.init(chartRef.value);
  burndownChartInstance.setOption(burndownChartConfig);

  watch(() => props.visible, (newValue) => {
    if (newValue) {
      loadBurndownData();
    }
  }, {
    immediate: true
  });

  watch([() => burndownTarget.value, () => burndownData.value], () => {
    if (burndownData.value) {
      const xData = (burndownData.value[burndownTarget.value]?.expected || []).map(i => i.timeSeries);
      const expectedYData = (burndownData.value[burndownTarget.value]?.expected || []).map(i => i.value);
      const remainingYData = (burndownData.value[burndownTarget.value]?.remaining || []).map(i => i.value);
      burndownChartConfig.xAxis.data = xData;
      burndownChartConfig.series[0].data = remainingYData;
      burndownChartConfig.series[1].data = expectedYData;
    } else {
      burndownChartConfig.xAxis.data = [];
      burndownChartConfig.series[0].data = [];
      burndownChartConfig.series[1].data = [];
    }
    burndownChartInstance.setOption(burndownChartConfig);
  });
});
</script>
<template>
  <Modal
    :visible="props.visible"
    :footer="null"
    :width="800"
    :title="t('functionPlan.comp.burndownChart.title')"
    @cancel="handleCancel">
    <div class="pt-1.5">
      <div class="text-3.5 font-semibold flex justify-between">
        <RadioGroup v-model:value="burndownTarget" :options="burndownOptions">
        </RadioGroup>
      </div>
      <div ref="chartRef" class="border rounded p-2 my-3 h-60">
      </div>
    </div>
  </Modal>
</template>
