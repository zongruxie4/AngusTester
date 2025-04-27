<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import * as echarts from 'echarts';

import { ReportContent } from '../PropsType';

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

const testResult = computed(() => {
  return props.dataSource?.content?.testResult;
});

const configInfo = [
  [{ label: '总测试数', dataIndex: 'enabledTestNum' }, { label: '功能测试', dataIndex: 'enabledFuncTestNum' }],
  [{ label: '性能测试', dataIndex: 'enabledPerfTestNum' }, { label: '稳定性测试', dataIndex: 'enabledStabilityTestNum' }]
];

const testProgressRef = ref();
const passdProgressRef = ref();
const warpRef = ref();

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
let passdEchart;

onMounted(() => {
  watch(() => props.dataSource, newValue => {
    const testConfig = JSON.parse(JSON.stringify(echartsConfigOption));
    testConfig.series[0].data = [
      {
        name: '已测试',
        value: newValue.testedNum || 0,
        itemStyle: {
          color: '#52C41A'
        }
      },
      {
        name: '未测试',
        value: newValue.unTestedNum || 0,
        itemStyle: {
          color: 'rgba(200, 202, 208, 1)'
        }
      }

    ];

    const passdConfig = JSON.parse(JSON.stringify(echartsConfigOption));

    passdConfig.series[0].data = [
      {
        name: '已通过',
        value: newValue.testPassedNum || 0,
        itemStyle: {
          color: '#52C41A'
        }
      },
      {
        name: '未通过',
        value: newValue.testUnPassedNum || 0,
        itemStyle: {
          color: 'rgba(200, 202, 208, 1)'
        }
      }

    ];
    testEchart = echarts.init(testProgressRef.value);
    passdEchart = echarts.init(passdProgressRef.value);
    testEchart.setOption(testConfig);
    passdEchart.setOption(passdConfig);
  }, {
    immediate: true
  });
});

</script>

<template>
  <div>
    <h1 class="text-theme-title font-medium mb-5">
      <span id="a3" class="text-4 text-theme-title font-medium">三、<em class="inline-block w-0.25"></em>测试结果汇总</span>
    </h1>
    <h1 class="text-theme-title font-medium mb-3">
      <span id="a3.1" class="text-3.5 text-theme-title font-medium">3.1 <em class="inline-block w-0.25"></em>测试进度</span>
    </h1>
    <div class="flex space-x-4 w-120 mb-7">
      <div class="text-center py-2.5 flex-1 bg-blue-bg3 rounded flex justify-center items-center space-x-4 pr-4">
        <img class="w-10" src="./image/jiekou.png" />
        <div>
          <div class="text-4 font-semibold">{{ testResult?.testApis?.totalApisNum || '--' }}</div>
          总接口
        </div>
      </div>
      <div class="text-center py-1 flex-1 bg-blue-bg3 rounded flex justify-center items-center space-x-4 pr-4">
        <img class="w-10" src="./image/jindu.png" />
        <div>
          <div class="text-4 font-semibold">{{ testResult.progress.completedRate + '%' }}</div>
          进度
        </div>
      </div>
    </div>
    <h1 class="text-theme-title font-medium mb-3">
      <span id="a3.2" class="text-3.5 text-theme-title font-medium">3.2 <em class="inline-block w-0.25"></em> 测试接口统计</span>
    </h1>
    <div class="flex space-x-4 w-120 mb-7">
      <div class="space-y-2 text-3 w-full">
        <li
          v-for="(line, idx) in configInfo"
          :key="idx"
          class="flex space-x-2">
          <div
            v-for="item in line"
            :key="item.dataIndex"
            class="flex flex-1 h-10 leading-10">
            <span class="flex-1 text-white bg-blue-6 px-2 rounded">{{ item.label }}</span>
            <span class="flex-1 bg-gray-light pl-2 rounded-r">{{ testResult.testApis[item.dataIndex] || '--' }}</span>
          </div>
        </li>
      </div>
    </div>
    <h1 class="text-theme-title font-medium mb-3">
      <span id="a3.3" class="text-3.5 text-theme-title font-medium">3.3 <em class="inline-block w-0.25"></em> 测试状态统计</span>
    </h1>
    <div ref="warpRef" class="flex w-100 mb-7">
      <div ref="testProgressRef" class="flex-1 h-30">
      </div>
      <div ref="passdProgressRef" class="flex-1 h-30">
      </div>
    </div>
  </div>
</template>

<style scoped>
.content-text-container {
  text-indent: 2em;
}
</style>
