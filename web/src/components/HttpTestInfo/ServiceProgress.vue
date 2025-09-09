<script lang="ts" setup>
import { ref, watch, onMounted, onBeforeUnmount } from 'vue';
import * as echarts from 'echarts';
import elementResizeDetector from 'element-resize-detector';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

const erd = elementResizeDetector({ strategy: 'scroll' });
const testProgressRef = ref();
const passwordProgressRef = ref();
const warpRef = ref();

interface Props {
    value: Record<string, any>
}

const props = withDefaults(defineProps<Props>(), {
  value: () => ({
  })
});

const echartsConfigOption = {
  title: {
    text: '',
    textStyle: {
      fontSize: 12
    }
  },
  tooltip: {
    trigger: 'item',
    confine: true,
    textStyle: {
      fontSize: 12
    }
  },
  legend: {
    left: 'center',
    bottom: 0,
    orient: 'horizontal',
    itemHeight: 14,
    itemWidth: 14
  },
  series: [
    {
      name: '',
      type: 'pie',
      startAngle: 90,
      radius: ['35%', '50%'],
      center: ['50%', '40%'],
      avoidLabelOverlap: true,
      label: {
        show: true,
        formatter: '{c}'
      },
      labelLine: {
        length: 5,
        length2: 5
      },
      data: []
    }
  ]
};

let testEchart;
let passwordEchart;

const resizeEcharts = () => {
  testEchart.resize();
  passwordEchart.resize();
};

onMounted(() => {
  watch(() => props.value, newValue => {
    const testConfig = JSON.parse(JSON.stringify(echartsConfigOption));
    testConfig.series[0].data = [
      {
        name: t('xcan_httpTestInfo.tested'),
        value: newValue.testedNum || 0,
        itemStyle: {
          color: '#52C41A'
        }
      },
      {
        name: t('xcan_httpTestInfo.untested'),
        value: newValue.unTestedNum || 0,
        itemStyle: {
          color: 'rgba(200, 202, 208, 1)'
        }
      }

    ];

    const passwordConfig = JSON.parse(JSON.stringify(echartsConfigOption));

    passwordConfig.series[0].data = [
      {
        name: t('xcan_httpTestInfo.passed'),
        value: newValue.testPassedNum || 0,
        itemStyle: {
          color: '#52C41A'
        }
      },
      {
        name: t('xcan_httpTestInfo.failed'),
        value: newValue.testUnPassedNum || 0,
        itemStyle: {
          color: 'rgba(200, 202, 208, 1)'
        }
      }

    ];
    testEchart = echarts.init(testProgressRef.value);
    passwordEchart = echarts.init(passwordProgressRef.value);
    testEchart.setOption(testConfig);
    passwordEchart.setOption(passwordConfig);
  }, {
    immediate: true
  });

  erd.listenTo(warpRef.value, resizeEcharts);
});

onBeforeUnmount(() => {
  erd.removeListener(warpRef.value, resizeEcharts);
});

</script>
<template>
  <div ref="warpRef" class="flex">
    <div ref="testProgressRef" class="flex-1 h-30">
    </div>
    <div ref="passwordProgressRef" class="flex-1 h-30">
    </div>
  </div>
</template>
