<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue';
import { Icon } from '@xcan-angus/vue-ui';
import * as eCharts from 'echarts';
import { caseOverViewConfig, taskOverViewConfig } from './config';
import { kanban } from 'src/api/tester';
import noData from './Image/nodata.png';
import { throttle } from 'throttle-debounce';
import { RadioGroup } from 'ant-design-vue';

interface Props {
  projectId: string;
  sprintId: string;
  planId: string;
  creatorObjectType: 'GROUP' | 'USER' | 'DEPT';
  creatorObjectId: string;
  createdDateStart: string;
  createdDateEnd: string;
  countType: 'task' | 'useCase';
  onShow: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  userId: undefined,
  sprintId: undefined,
  planId: undefined,
  creatorObjectType: undefined,
  creatorObjectId: undefined,
  createdDateStart: undefined,
  createdDateEnd: undefined,
  countType: 'task',
  onShow: false
});

// 优先级分布
const priorityRef = ref();
// let priorityEcharts;
// const priorityEchartsConfig = {
//   title: {
//     // text: 0,
//     // subtext: '总数',
//     // left: '49.5%',
//     // top: '25%',
//     // padding: 2,
//     // textAlign: 'center',
//     // textStyle: {
//     //   fontSize: 14,
//     //   width: 120,
//     //   fontWeight: 'bolder'
//     // },
//     // subtextStyle: {
//     //   fontSize: 12
//     // }
//   },
//   tooltip: {
//     trigger: 'item'
//   },
//   legend: {
//     left: 'center',
//     bottom: '0',
//     itemHeight: 14,
//     itemWidth: 14,
//     itemGap: 2
//   },
//   series: [
//     {
//       name: '总数',
//       type: 'pie',
//       radius: ['30%', '50%'],
//       center: ['50%', '40%'],
//       avoidLabelOverlap: true,
//       label: {
//         show: true,
//         formatter: '{c}'
//       },
//       itemStyle: {
//         borderRadius: 5,
//         borderColor: '#fff',
//         borderWidth: 1
//       },
//       emphasis: {
//         label: {
//           show: true
//         }
//       },
//       labelLine: {
//         show: true,
//       },
//       data: [
//         {
//           name: '最高',
//           value: 0,
//           itemStyle: {
//             color: '#F5222D'
//           }
//         },
//         {
//           name: '高',
//           value: 0,
//           itemStyle: {
//             color: '#FF8100'
//           }
//         },
//         {
//           name: '中',
//           value: 0,
//           itemStyle: {
//             color: '#FFB925'
//           }
//         },
//         {
//           name: '低',
//           value: 0,
//           itemStyle: {
//             color: '#52C41A'
//           }
//         },
//         {
//           name: '最低',
//           value: 0,
//           itemStyle: {
//             color: '#67D7FF'
//           }
//         }
//       ]
//     }
//   ]
// };

const burnDownOpt = computed(() => [
  {
    value: 'NUM',
    label: props.countType === 'task' ? '任务数' : '用例数'
  },
  {
    value: 'WORKLOAD',
    label: '工作量'
  }
]);
const burnDownTarget = ref('NUM');
let burnDownData;
watch(() => burnDownTarget.value, () => {
  if (burnDownData) {
    const xData = (burnDownData[burnDownTarget.value]?.expected || []).map(i => i.timeSeries);
    const expectedYData = (burnDownData[burnDownTarget.value]?.expected || []).map(i => i.value);
    const remainingYData = (burnDownData[burnDownTarget.value]?.remaining || []).map(i => i.value);
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
      name: '剩余',
      data: [],
      type: 'line',
      smooth: true
    },
    {
      name: '期望',
      data: [],
      type: 'line'
    }
  ]
};

// 评审状态分布
// const reviewStatusRef = ref();
// let reviewStatusEcharts;
// const reviewStatusEchartsConfig = {
//   title: {
//     // text: 0,
//     // subtext: '总数',
//     // left: '29.5%',
//     // top: '35%',
//     // padding: 2,
//     // textAlign: 'center',
//     // textStyle: {
//     //   fontSize: 14,
//     //   width: 120,
//     //   fontWeight: 'bolder'
//     // },
//     // subtextStyle: {
//     //   fontSize: 12
//     // }
//   },
//   tooltip: {
//     trigger: 'item'
//   },
//   legend: {
//     top: 'middle',
//     right: '0',
//     orient: 'vertical',
//     itemHeight: 14,
//     itemWidth: 14,
//   },
//   series: [
//     {
//       name: '总数',
//       type: 'pie',
//       radius: ['30%', '50%'],
//       center: ['30%', '50%'],
//       avoidLabelOverlap: true,
//       label: {
//         show: true,
//         formatter: '{c}'
//       },
//       itemStyle: {
//         borderRadius: 5,
//         borderColor: '#fff',
//         borderWidth: 1
//       },
//       emphasis: {
//         label: {
//           show: true,
//         }
//       },
//       labelLine: {
//         show: true
//       },
//       data: [
//         {
//           name: '无需评审',
//           value: 0,
//           itemStyle: {
//             color: 'rgba(201, 119, 255, 1)'
//           }
//         },
//         {
//           name: '待评审',
//           value: 0,
//           itemStyle: {
//             color: 'rgba(82, 196, 26, 1)'
//           }
//         },
//         {
//           name: '评审未通过',
//           value: 0,
//           itemStyle: {
//             color: 'rgba(245, 34, 45, 1)'
//           }
//         },
//         {
//           name: '评审通过',
//           value: 0,
//           itemStyle: {
//             color: 'rgba(245, 34, 45, 1)'
//           }
//         }
//       ]
//     }
//   ]
// };

// 任务task 分类
const taskTypeRef = ref();
let taskTypeEcharts;
const taskTypeEchartsConfig = {
  title: {

  },
  tooltip: {
    trigger: 'item'
  },
  legend: {
    top: 'middle',
    right: '0',
    orient: 'vertical',
    itemHeight: 14,
    itemWidth: 14,
    itemGap: 2
  },
  series: [
    {
      name: '总数',
      type: 'pie',
      radius: ['30%', '50%'],
      center: ['35%', '50%'],
      avoidLabelOverlap: true,
      label: {
        show: true,
        formatter: '{c}'
      },
      itemStyle: {
        borderRadius: 5,
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
          name: '故事',
          value: 0,
          itemStyle: {
            color: 'rgba(136, 185, 242, 1)'
          }
        },
        {
          name: '需求',
          value: 0,
          itemStyle: {
            color: 'rgba(201, 119, 255, 1)'
          }
        },
        {
          name: '任务',
          value: 0,
          itemStyle: {
            color: 'rgba(255, 165, 43, 1)'
          }
        },
        {
          name: '缺陷',
          value: 0,
          itemStyle: {
            color: 'rgba(245, 34, 45, 1)'
          }
        },
        {
          name: '接口测试',
          value: 0,
          itemStyle: {
            color: 'rgba(82, 196, 26, 1)'
          }
        },
        {
          name: '场景测试',
          value: 0,
          itemStyle: {
            color: 'rgba(0,119,255,1)'
          }
        }
      ]
    }
  ]
};

// （任务|用例） 数量
const targetNumRef = ref();
let targetEcharts;
const targetEchartsConfig = {
  graphic: {
    type: 'image',
    left: 'center',
    top: 'middle',
    z: 0,
    silent: true,
    invisible: false,
    style: {
      width: 100,
      image: noData
    }
  },
  tooltip: {
    trigger: 'axis',
    axisPointer: {
      type: 'shadow'
    }
  },
  grid: {
    left: 60,
    top: 0,
    bottom: 50
  },
  toolbox: {
    show: true
  },
  xAxis: {
    type: 'value',
    axisLabel: {
      formatter: '{value}'
    }
  },
  legend: {
    show: true,
    bottom: 0
  },
  yAxis: {
    type: 'category',
    data: [],
    axisLine: {
      show: false
    },
    axisTick: {
      show: false
    }
  },
  series: [
    {
      name: '总数',
      type: 'bar',
      itemStyle: {
        color: 'rgba(45, 142, 255, 1)'
      },
      data: [],
      barMaxWidth: '16',
      barMinWidth: '2',
      barGap: 0
    },
    {
      name: '完成数',
      type: 'bar',
      itemStyle: {
        color: 'rgba(82, 196, 26, 1)'
      },
      data: [],
      barMaxWidth: '16',
      barMinWidth: '2',
      barGap: 0
    }
  ]
};

// （任务|用例） 完成率
const targetRateRef = ref();
let targetRateEcharts;
const targetRateEchartsConfig = {
  graphic: {
    type: 'image',
    left: 'center',
    top: 'middle',
    z: 0,
    silent: true,
    invisible: false,
    style: {
      width: 100,
      image: noData
    }
  },
  tooltip: {
    trigger: 'axis',
    axisPointer: {
      type: 'shadow'
    },
    valueFormatter: (value) => value + '%'
  },
  grid: {
    left: 60,
    top: 0,
    bottom: 0
  },
  toolbox: {
    show: true
  },
  xAxis: {
    type: 'value',

    axisLabel: {
      formatter: '{value}'
    }
  },
  yAxis: {
    type: 'category',
    data: [],
    axisLine: {
      show: false
    },
    axisTick: {
      show: false
    }
  },
  series: [
    {
      type: 'bar',
      itemStyle: {
        color: 'rgba(45, 142, 255, 1)'
      },
      data: [],
      barMaxWidth: '16'
    }
  ]
};

// 工作量
const workloadRef = ref();
let workloadEcharts;
const workloadEchartsConfig = {
  graphic: {
    type: 'image',
    left: 'center',
    top: 'middle',
    z: 0,
    silent: true,
    invisible: false,
    style: {
      width: 100,
      image: noData
    }
  },
  tooltip: {
    trigger: 'axis',
    axisPointer: {
      type: 'shadow'
    }
  },
  grid: {
    left: 60,
    top: 0,
    bottom: 50
  },
  toolbox: {
    show: true
  },
  legend: {
    show: true,
    bottom: 0
  },
  xAxis: {
    type: 'value',

    axisLabel: {
      formatter: '{value}'
    }
  },
  yAxis: {
    type: 'category',
    data: [],
    axisLine: {
      show: false
    },
    axisTick: {
      show: false
    }
  },
  series: [
    {
      name: '总数',
      type: 'bar',
      itemStyle: {
        color: 'rgba(45, 142, 255, 1)'
      },
      data: [],
      barMaxWidth: '16',
      barMinWidth: '2',
      barGap: 0
    },
    {
      name: '完成数',
      type: 'bar',
      itemStyle: {
        color: 'rgba(82, 196, 26, 1)'
      },
      data: [],
      barMaxWidth: '16',
      barMinWidth: '2',
      barGap: 0
    }

  ]
};

// 节省工作量
const workloadRateRef = ref();
let workloadRateEcharts;
const workloadRateEchartsConfig = {
  graphic: {
    type: 'image',
    left: 'center',
    top: 'middle',
    z: 0,
    silent: true,
    invisible: false,
    style: {
      width: 100,
      image: noData
    }
  },
  tooltip: {
    trigger: 'axis',
    axisPointer: {
      type: 'shadow'
    }
  },
  grid: {
    left: 60,
    top: 0,
    bottom: 0
  },
  toolbox: {
    show: true
  },
  xAxis: {
    type: 'value',

    axisLabel: {
      formatter: '{value}'
    }
  },
  yAxis: {
    type: 'category',
    data: [],
    axisLine: {
      show: false
    },
    axisTick: {
      show: false
    }
  },
  series: [
    {
      type: 'bar',
      itemStyle: {
        color: 'rgba(82, 196, 26, 1)'
      },
      data: [],
      barMaxWidth: '16'
    }
  ]
};

// 逾期数
const overdueRef = ref();
let overdueEcharts;
const overdueEchartsConfig = {
  graphic: {
    type: 'image',
    left: 'center',
    top: 'middle',
    z: 0,
    silent: true,
    invisible: false,
    style: {
      width: 100,
      image: noData
    }
  },
  tooltip: {
    trigger: 'axis',
    axisPointer: {
      type: 'shadow'
    }
  },
  grid: {
    left: 60,
    top: 0,
    bottom: 50
  },
  toolbox: {
    show: true
  },
  legend: {
    show: true,
    bottom: 0
  },
  xAxis: {
    type: 'value',

    axisLabel: {
      formatter: '{value}'
    }
  },
  yAxis: {
    type: 'category',
    data: [],
    axisLine: {
      show: false
    },
    axisTick: {
      show: false
    }
  },
  series: [
    {
      name: '总数',
      type: 'bar',
      itemStyle: {
        color: 'rgba(45, 142, 255, 1)'
      },
      data: [],
      barMaxWidth: '16',
      barMinWidth: '2',
      barGap: 0
    },
    {
      name: '逾期数',
      type: 'bar',
      itemStyle: {
        color: 'rgba(245, 34, 45, 1)'
      },
      data: [],
      barMaxWidth: '16',
      barMinWidth: '2',
      barGap: 0
    }
  ]
};

// 逾期率
const overdueRateRef = ref();
let overdueRateEcharts;
const overdueRateEchartsConfig = {
  graphic: {
    type: 'image',
    left: 'center',
    top: 'middle',
    z: 0,
    silent: true,
    invisible: false,
    style: {
      width: 100,
      image: noData
    }
  },
  tooltip: {
    trigger: 'axis',
    axisPointer: {
      type: 'shadow'
    },
    valueFormatter: (value) => value + '%'
  },
  grid: {
    left: 60,
    top: 0,
    bottom: 0
  },
  toolbox: {
    show: true
  },
  xAxis: {
    type: 'value',

    axisLabel: {
      formatter: '{value}'
    }
  },
  yAxis: {
    type: 'category',
    data: [],
    axisLine: {
      show: false
    },
    axisTick: {
      show: false
    }
  },
  series: [
    {
      type: 'bar',
      itemStyle: {
        color: 'rgba(245, 34, 45, 1)'
      },
      data: [],
      barMaxWidth: '16'
    }
  ]
};

// 一次性通过数
const oneTimePassedTestRef = ref();
let oneTimePassedTestEharts;
const oneTimePassedTestConfig = {
  graphic: {
    type: 'image',
    left: 'center',
    top: 'middle',
    z: 0,
    silent: true,
    invisible: false,
    style: {
      width: 100,
      image: noData
    }
  },
  tooltip: {
    trigger: 'axis',
    axisPointer: {
      type: 'shadow'
    }
  },
  grid: {
    left: 60,
    top: 0,
    bottom: 50
  },
  toolbox: {
    show: true
  },
  legend: {
    show: true,
    bottom: 0
  },
  xAxis: {
    type: 'value',

    axisLabel: {
      formatter: '{value}'
    }
  },
  yAxis: {
    type: 'category',
    data: [],
    axisLine: {
      show: false
    },
    axisTick: {
      show: false
    }
  },
  series: [
    {
      name: '总数',
      type: 'bar',
      itemStyle: {
        color: 'rgba(45, 142, 255, 1)'
      },
      data: [],
      barMaxWidth: '16',
      barMinWidth: '2',
      barGap: 0
    },
    {
      name: '通过数',
      type: 'bar',
      itemStyle: {
        color: 'rgba(82, 196, 26, 1)'
      },
      data: [],
      barMaxWidth: '16',
      barMinWidth: '2',
      barGap: 0
    }
  ]
};

// 一次性通过率
const oneTimePassedTestRateRef = ref();
let oneTimePassedTestRateEharts;
const oneTimePassedTestRateConfig = {
  graphic: {
    type: 'image',
    left: 'center',
    top: 'middle',
    z: 0,
    silent: true,
    invisible: false,
    style: {
      width: 100,
      image: noData
    }
  },
  tooltip: {
    trigger: 'axis',
    axisPointer: {
      type: 'shadow'
    },
    valueFormatter: (value) => value + '%'
  },
  grid: {
    left: 60,
    top: 0,
    bottom: 0
  },
  toolbox: {
    show: true
  },
  xAxis: {
    type: 'value',

    axisLabel: {
      formatter: '{value}'
    }
  },
  yAxis: {
    type: 'category',
    data: [],
    axisLine: {
      show: false
    },
    axisTick: {
      show: false
    }
  },
  series: [
    {
      type: 'bar',
      itemStyle: {
        color: 'rgba(82, 196, 26, 1)'
      },
      data: [],
      barMaxWidth: '16'
    }
  ]
};

// 缺陷数|一次性评审通过数
const oneTimeUnPassedTestRef = ref();
let oneTimeUnPassedTestEharts;
const oneTimeUnPassedTestConfig = {
  graphic: {
    type: 'image',
    left: 'center',
    top: 'middle',
    z: 0,
    silent: true,
    invisible: false,
    style: {
      width: 100,
      image: noData
    }
  },
  tooltip: {
    trigger: 'axis',
    axisPointer: {
      type: 'shadow'
    }
  },
  grid: {
    left: 60,
    top: 0,
    bottom: 50
  },
  toolbox: {
    show: true
  },
  legend: {
    show: true,
    bottom: 0
  },
  xAxis: {
    type: 'value',

    axisLabel: {
      formatter: '{value}'
    }
  },
  yAxis: {
    type: 'category',
    data: [],
    axisLine: {
      show: false
    },
    axisTick: {
      show: false
    }
  },
  series: [
    {
      name: '总数',
      type: 'bar',
      itemStyle: {
        color: 'rgba(45, 142, 255, 1)'
      },
      data: [],
      barMaxWidth: '16',
      barMinWidth: '2',
      barGap: 0
    },
    {
      name: '通过数',
      type: 'bar',
      itemStyle: {
        // 任务下为rgba(82, 196, 26, 1)   用例下为rgba(255, 165, 43, 1)'
        color: 'rgba(82, 196, 26, 1)'
      },
      data: [],
      barMaxWidth: '16',
      barMinWidth: '2',
      barGap: 0
    }
  ]
};

// 缺陷率|一次性评审通过率
const oneTimeUnPassedTestRateRef = ref();
let oneTimeUnPassedTestRateEharts;
const oneTimeUnPassedTestRateConfig = {
  graphic: {
    type: 'image',
    left: 'center',
    top: 'middle',
    z: 0,
    silent: true,
    invisible: false,
    style: {
      width: 100,
      image: noData
    }
  },
  tooltip: {
    trigger: 'axis',
    axisPointer: {
      type: 'shadow'
    },
    valueFormatter: (value) => value + '%'
  },
  grid: {
    left: 60,
    top: 0,
    bottom: 0
  },
  toolbox: {
    show: true
  },
  xAxis: {
    type: 'value',

    axisLabel: {
      formatter: '{value}'
    }
  },
  yAxis: {
    type: 'category',
    data: [],
    axisLine: {
      show: false
    },
    axisTick: {
      show: false
    }
  },
  series: [
    {
      type: 'bar',
      itemStyle: {
        // 任务下为rgba(82, 196, 26, 1)   用例下为rgba(255, 165, 43, 1)'
        color: 'rgba(255, 165, 43, 1)'
      },
      data: [],
      barMaxWidth: '16'
    }
  ]
};

const params = computed(() => {
  const { countType, planId, sprintId, onShow, ...other } = props;
  if (countType === 'task') {
    return {
      ...other,
      planId: sprintId
    };
  }
  return {
    ...other,
    planId
  };
});

const overViewData = ref<{[key: string]: string}>({});

const loadData = async () => {
  const [error, { data = {} }] = await (props.countType === 'task' ? kanban.loadTaskOverView({ ...params.value }) : kanban.loadCaseOverView({ ...params.value }));
  if (error) {
    return;
  }
  // 优先级
  overViewData.value = data.totalOverview || {};
  // if (data.totalPriorityCount) {
  //   const {HIGH = 0, HIGHEST = 0, LOW = 0, LOWEST = 0, MEDIUM = 0} = data.totalPriorityCount;
  //   const priorityData = [HIGHEST, HIGH, MEDIUM, LOW, LOWEST];
  //   priorityEchartsConfig.series[0].data.forEach((item, idx) => {
  //     item.value = priorityData[idx];
  //   })
  // } else {
  //   priorityEchartsConfig.series[0].data.forEach((item) => {
  //     item.value = 0;
  //   })
  // }
  // priorityEcharts.setOption(priorityEchartsConfig);
  burnDownData = data.burnDownCharts;
  if (data.burnDownCharts) {
    const xData = (data.burnDownCharts[burnDownTarget.value].expected || []).map(i => i.timeSeries);
    const expectedYData = (data.burnDownCharts[burnDownTarget.value].expected || []).map(i => i.value);
    const remainingYData = (data.burnDownCharts[burnDownTarget.value].remaining || []).map(i => i.value);
    burnDownEchartsConfig.xAxis.data = xData;
    burnDownEchartsConfig.series[0].data = remainingYData;
    burnDownEchartsConfig.series[1].data = expectedYData;
  } else {
    burnDownEchartsConfig.xAxis.data = [];
    burnDownEchartsConfig.series[0].data = [];
    burnDownEchartsConfig.series[1].data = [];
  }
  burnDownEcharts.setOption(burnDownEchartsConfig);
  // 分类
  if (data.totalTypeCount) {
    const { STORY = 0, REQUIREMENT = 0, TASK = 0, BUG = 0, API_TEST = 0, SCENARIO_TEST = 0 } = data.totalTypeCount;
    const typeData = [STORY, REQUIREMENT, TASK, BUG, API_TEST, SCENARIO_TEST];
    taskTypeEchartsConfig.series[0].data.forEach((item, idx) => {
      item.value = typeData[idx];
    });
  } else {
    taskTypeEchartsConfig.series[0].data.forEach((item) => {
      item.value = 0;
    });
  }
  taskTypeEcharts.setOption(taskTypeEchartsConfig);
  if (props.countType === 'task') {
    targetEchartsConfig.series[1].name = '完成数';
    oneTimeUnPassedTestConfig.series[1].name = '缺陷数';
  } else {
    targetEchartsConfig.series[1].name = '测试通过数';
    oneTimeUnPassedTestConfig.series[1].name = '通过数';
  }

  const ranking = data.assigneeRanking || data.testerRanking;

  // 完成数
  const assignees = data.assignees || data.testers;
  if (ranking?.completedNumRank || ranking?.passedTestNumRank) {
    const rankData = (ranking.completedNumRank || ranking.passedTestNumRank).slice(0, 10).reverse();
    const validTaskNumRank = ranking?.validTaskNumRank || ranking?.validCaseNumRank || [];
    const userIds = rankData.map(i => i.assigneeId);
    const yData = userIds.map(i => assignees[i].fullName);
    const totalXData = userIds.map((userId) => validTaskNumRank.find(i => i.assigneeId === userId)?.score || 0);
    const completeXData = rankData.map(i => i.score);
    targetEchartsConfig.yAxis.data = yData.map(i => ({ value: i, textStyle: { width: 50, overflow: 'breakAll' } }));
    targetEchartsConfig.series[0].data = totalXData;
    targetEchartsConfig.series[1].data = completeXData;
  } else {
    targetEchartsConfig.yAxis.data = [];
    targetEchartsConfig.series[0].data = [];
    targetEchartsConfig.series[1].data = [];
  }
  if (!targetEchartsConfig.yAxis.data.length) {
    targetEchartsConfig.graphic.invisible = false;
  } else {
    targetEchartsConfig.graphic.invisible = true;
  }

  targetEcharts.setOption(targetEchartsConfig);

  // 工作量
  if (ranking?.completedWorkloadRank) {
    const rankData = (ranking.completedWorkloadRank).slice(0, 10).reverse();
    const actualWorkloadRank = ranking?.actualWorkloadRank || [];
    const userIds = rankData.map(i => i.assigneeId);
    const yData = userIds.map(i => assignees[i].fullName);
    const totalXData = userIds.map((userId) => actualWorkloadRank.find(i => i.assigneeId === userId)?.score || 0);
    const completeXData = rankData.map(i => i.score);
    workloadEchartsConfig.yAxis.data = yData.map(i => ({ value: i, textStyle: { width: 50, overflow: 'breakAll' } }));
    workloadEchartsConfig.series[0].data = totalXData;
    workloadEchartsConfig.series[1].data = completeXData;
  } else {
    workloadEchartsConfig.yAxis.data = [];
    workloadEchartsConfig.series[0].data = [];
    workloadEchartsConfig.series[1].data = [];
  }
  if (!workloadEchartsConfig.yAxis.data.length) {
    workloadEchartsConfig.graphic.invisible = false;
  } else {
    workloadEchartsConfig.graphic.invisible = true;
  }
  workloadEcharts.setOption(workloadEchartsConfig);

  // 逾期数
  if (ranking?.overdueNumRank) {
    const rankData = ranking.overdueNumRank.slice(0, 10).reverse();
    const validTaskNumRank = ranking?.validTaskNumRank || ranking?.validCaseNumRank || [];
    const userIds = rankData.map(i => i.assigneeId);
    const yData = userIds.map(i => assignees[i].fullName);
    const totalXData = userIds.map((userId) => validTaskNumRank.find(i => i.assigneeId === userId)?.score || 0);
    const completeXData = rankData.map(i => i.score);
    overdueEchartsConfig.yAxis.data = yData.map(i => ({ value: i, textStyle: { width: 50, overflow: 'breakAll' } }));
    overdueEchartsConfig.series[0].data = totalXData;
    overdueEchartsConfig.series[1].data = completeXData;
  } else {
    overdueEchartsConfig.yAxis.data = [];
    overdueEchartsConfig.series[0].data = [];
    overdueEchartsConfig.series[1].data = [];
  }
  if (!overdueEchartsConfig.yAxis.data.length) {
    overdueEchartsConfig.graphic.invisible = false;
  } else {
    overdueEchartsConfig.graphic.invisible = true;
  }
  overdueEcharts.setOption(overdueEchartsConfig);

  // 一次通过数
  if (ranking?.oneTimePassedNumRank || ranking?.oneTimePassedTestNumRank) {
    const rankData = (ranking.oneTimePassedNumRank || ranking?.oneTimePassedTestNumRank).slice(0, 10).reverse();
    const validTaskNumRank = ranking?.validTaskNumRank || ranking?.validCaseNumRank || [];
    const userIds = rankData.map(i => i.assigneeId);
    const yData = userIds.map(i => assignees[i].fullName);
    const totalXData = userIds.map((userId) => validTaskNumRank.find(i => i.assigneeId === userId)?.score || 0);
    const completeXData = rankData.map(i => i.score);
    oneTimePassedTestConfig.yAxis.data = yData.map(i => ({ value: i, textStyle: { width: 50, overflow: 'breakAll' } }));
    oneTimePassedTestConfig.series[0].data = totalXData;
    oneTimePassedTestConfig.series[1].data = completeXData;
  } else {
    oneTimePassedTestConfig.yAxis.data = [];
    oneTimePassedTestConfig.series[0].data = [];
    oneTimePassedTestConfig.series[1].data = [];
  }

  if (!oneTimePassedTestConfig.yAxis.data.length) {
    oneTimePassedTestConfig.graphic.invisible = false;
  } else {
    oneTimePassedTestConfig.graphic.invisible = true;
  }
  oneTimePassedTestEharts.setOption(oneTimePassedTestConfig);

  // validBugNumRank
  // 缺陷数 | 一次性通过评审数
  if (ranking?.validBugNumRank || ranking?.oneTimePassedReviewNumRank) {
    const rankData = (ranking.validBugNumRank || ranking?.oneTimePassedReviewNumRank).slice(0, 10).reverse();
    const validTaskNumRank = ranking?.validTaskNumRank || ranking?.validCaseNumRank || [];
    const userIds = rankData.map(i => i.assigneeId);
    const yData = userIds.map(i => assignees[i].fullName);
    const totalXData = userIds.map((userId) => validTaskNumRank.find(i => i.assigneeId === userId)?.score || 0);
    const completeXData = rankData.map(i => i.score);
    oneTimeUnPassedTestConfig.yAxis.data = yData.map(i => ({ value: i, textStyle: { width: 50, overflow: 'breakAll' } }));
    oneTimeUnPassedTestConfig.series[0].data = totalXData;
    oneTimeUnPassedTestConfig.series[1].data = completeXData;
  } else {
    oneTimeUnPassedTestConfig.yAxis.data = [];
    oneTimeUnPassedTestConfig.series[0].data = [];
    oneTimeUnPassedTestConfig.series[1].data = [];
  }
  if (!oneTimeUnPassedTestConfig.yAxis.data.length) {
    oneTimeUnPassedTestConfig.graphic.invisible = false;
  } else {
    oneTimeUnPassedTestConfig.graphic.invisible = true;
  }
  oneTimeUnPassedTestEharts.setOption(oneTimeUnPassedTestConfig);

  // 任务数完成率 completedRateRank
  if (ranking?.completedRateRank || ranking?.passedTestRateRank) {
    const rankData = (ranking.completedRateRank || ranking?.passedTestRateRank).slice(0, 10).reverse();
    const userIds = rankData.map(i => i.assigneeId);
    const yData = userIds.map(i => assignees[i].fullName);
    const XData = rankData.map(i => i.score);
    targetRateEchartsConfig.yAxis.data = yData.map(i => ({ value: i, textStyle: { width: 50, overflow: 'breakAll' } }));
    targetRateEchartsConfig.series[0].data = XData;
  } else {
    targetRateEchartsConfig.yAxis.data = [];
    targetRateEchartsConfig.series[0].data = [];
  }
  if (!targetRateEchartsConfig.yAxis.data.length) {
    targetRateEchartsConfig.graphic.invisible = false;
  } else {
    targetRateEchartsConfig.graphic.invisible = true;
  }
  targetRateEcharts.setOption(targetRateEchartsConfig);

  // 节省工作量
  if (ranking?.savingWorkloadRank) {
    const rankData = ranking.savingWorkloadRank.slice(0, 10).reverse();
    const userIds = rankData.map(i => i.assigneeId);
    const yData = userIds.map(i => assignees[i].fullName);
    const XData = rankData.map(i => i.score);
    workloadRateEchartsConfig.yAxis.data = yData.map(i => ({ value: i, textStyle: { width: 50, overflow: 'breakAll' } }));
    workloadRateEchartsConfig.series[0].data = XData;
  } else {
    workloadRateEchartsConfig.yAxis.data = [];
    workloadRateEchartsConfig.series[0].data = [];
  }
  if (!workloadRateEchartsConfig.yAxis.data.length) {
    workloadRateEchartsConfig.graphic.invisible = false;
  } else {
    workloadRateEchartsConfig.graphic.invisible = true;
  }
  workloadRateEcharts.setOption(workloadRateEchartsConfig);

  // 逾期率 overdueRateRank
  if (ranking?.overdueRateRank) {
    const rankData = ranking.overdueRateRank.slice(0, 10).reverse();
    const userIds = rankData.map(i => i.assigneeId);
    const yData = userIds.map(i => assignees[i].fullName);
    const XData = rankData.map(i => i.score);
    overdueRateEchartsConfig.yAxis.data = yData.map(i => ({ value: i, textStyle: { width: 50, overflow: 'breakAll' } }));
    overdueRateEchartsConfig.series[0].data = XData;
  } else {
    overdueRateEchartsConfig.yAxis.data = [];
    overdueRateEchartsConfig.series[0].data = [];
  }
  if (!overdueRateEchartsConfig.yAxis.data.length) {
    overdueRateEchartsConfig.graphic.invisible = false;
  } else {
    overdueRateEchartsConfig.graphic.invisible = true;
  }
  overdueRateEcharts.setOption(overdueRateEchartsConfig);

  // 一次性通过(测试)率 oneTimePassedRateRank/oneTimePassedTestRateRank
  if (ranking?.oneTimePassedRateRank || ranking?.oneTimePassedTestRateRank) {
    const rankData = (ranking?.oneTimePassedRateRank || ranking?.oneTimePassedTestRateRank).slice(0, 10).reverse();
    const userIds = rankData.map(i => i.assigneeId);
    const yData = userIds.map(i => assignees[i].fullName);
    const XData = rankData.map(i => i.score);
    oneTimePassedTestRateConfig.yAxis.data = yData.map(i => ({ value: i, textStyle: { width: 50, overflow: 'breakAll' } }));
    oneTimePassedTestRateConfig.series[0].data = XData;
  } else {
    oneTimePassedTestRateConfig.yAxis.data = [];
    oneTimePassedTestRateConfig.series[0].data = [];
  }
  if (!oneTimePassedTestRateConfig.yAxis.data.length) {
    oneTimePassedTestRateConfig.graphic.invisible = false;
  } else {
    oneTimePassedTestRateConfig.graphic.invisible = true;
  }
  oneTimePassedTestRateEharts.setOption(oneTimePassedTestRateConfig);

  // 缺陷率/一次性评审通过率 validBugRateRank / oneTimePassedReviewRateRank
  if (ranking?.validBugRateRank || ranking?.oneTimePassedReviewRateRank) {
    const rankData = (ranking?.validBugRateRank || ranking?.oneTimePassedReviewRateRank).slice(0, 10).reverse();
    const userIds = rankData.map(i => i.assigneeId);
    const yData = userIds.map(i => assignees[i].fullName);
    const XData = rankData.map(i => i.score);
    oneTimeUnPassedTestRateConfig.yAxis.data = yData.map(i => ({ value: i, textStyle: { width: 50, overflow: 'breakAll' } }));
    oneTimeUnPassedTestRateConfig.series[0].data = XData;
  } else {
    oneTimeUnPassedTestRateConfig.yAxis.data = [];
    oneTimeUnPassedTestRateConfig.series[0].data = [];
  }
  if (!oneTimeUnPassedTestRateConfig.yAxis.data.length) {
    oneTimeUnPassedTestRateConfig.graphic.invisible = false;
  } else {
    oneTimeUnPassedTestRateConfig.graphic.invisible = true;
  }
  oneTimeUnPassedTestRateEharts.setOption(oneTimeUnPassedTestRateConfig);
};

const shouldNotify = ref(false);
onMounted(() => {
  watch(() => props.countType, (newValue) => {
    if (newValue === 'task') {
      nextTick(() => {
        taskTypeEcharts.resize();
      });
    }
  });
  watch([() => props.createdDateEnd, () => props.createdDateStart, () => props.creatorObjectId, () => props.creatorObjectType, () => props.projectId, () => props.countType, () => props.planId, () => props.sprintId], () => {
    if (!props.onShow && props.projectId) {
      shouldNotify.value = true;
      return;
    }
    if (props.countType === 'task') {
      oneTimeUnPassedTestRateConfig.series[0].itemStyle.color = 'rgba(255, 165, 43, 1)';
      oneTimeUnPassedTestConfig.series[1].itemStyle.color = 'rgba(255, 165, 43, 1)';
    } else {
      oneTimeUnPassedTestRateConfig.series[0].itemStyle.color = 'rgba(82, 196, 26, 1)';
      oneTimeUnPassedTestConfig.series[1].itemStyle.color = 'rgba(82, 196, 26, 1)';
    }
    if (props.projectId) {
      loadData();
    }
  }, {
    immediate: true
  });

  watch(() => props.onShow, newValue => {
    if (newValue && shouldNotify.value && props.projectId) {
      refresh();
      shouldNotify.value = false;
    }
    if (newValue && resizeNotify.value && props.projectId) {
      handleWindowResize();
      resizeNotify.value = false;
    }
  });

  // priorityEcharts = eCharts.init(priorityRef.value);
  // priorityEcharts.setOption(priorityEchartsConfig);

  burnDownEcharts = eCharts.init(priorityRef.value);
  burnDownEcharts.setOption(burnDownEchartsConfig);

  // reviewStatusEcharts = eCharts.init(reviewStatusRef.value);
  // reviewStatusEcharts.setOption(reviewStatusEchartsConfig);

  taskTypeEcharts = eCharts.init(taskTypeRef.value);
  taskTypeEcharts.setOption(taskTypeEchartsConfig);

  targetEcharts = eCharts.init(targetNumRef.value);
  targetEcharts.setOption(targetEchartsConfig);

  workloadEcharts = eCharts.init(workloadRef.value);
  workloadEcharts.setOption(workloadEchartsConfig);

  overdueEcharts = eCharts.init(overdueRef.value);
  overdueEcharts.setOption(overdueEchartsConfig);

  oneTimePassedTestEharts = eCharts.init(oneTimePassedTestRef.value);
  oneTimePassedTestEharts.setOption(oneTimePassedTestConfig);

  targetRateEcharts = eCharts.init(targetRateRef.value);
  targetRateEcharts.setOption(targetRateEchartsConfig);

  workloadRateEcharts = eCharts.init(workloadRateRef.value);
  workloadRateEcharts.setOption(workloadRateEchartsConfig);

  overdueRateEcharts = eCharts.init(overdueRateRef.value);
  overdueRateEcharts.setOption(overdueRateEchartsConfig);

  oneTimePassedTestRateEharts = eCharts.init(oneTimePassedTestRateRef.value);
  oneTimePassedTestRateEharts.setOption(oneTimePassedTestRateConfig);

  oneTimeUnPassedTestEharts = eCharts.init(oneTimeUnPassedTestRef.value);
  oneTimeUnPassedTestEharts.setOption(oneTimeUnPassedTestConfig);

  oneTimeUnPassedTestRateEharts = eCharts.init(oneTimeUnPassedTestRateRef.value);
  oneTimeUnPassedTestRateEharts.setOption(oneTimeUnPassedTestRateConfig);
  window.addEventListener('resize', handleWindowResize);
});

const resizeEcharts = () => {
  taskTypeEcharts.resize();
  targetEcharts.resize();
  // priorityEcharts.resize();
  burnDownEcharts.resize();
  workloadEcharts.resize();
  overdueEcharts.resize();
  targetRateEcharts.resize();
  workloadRateEcharts.resize();
  overdueRateEcharts.resize();
  oneTimePassedTestEharts.resize();
  oneTimePassedTestRateEharts.resize();
  oneTimeUnPassedTestEharts = eCharts.init(oneTimeUnPassedTestRef.value);
  oneTimeUnPassedTestEharts.setOption(oneTimeUnPassedTestConfig);
  oneTimeUnPassedTestRateEharts = eCharts.init(oneTimeUnPassedTestRateRef.value);
  oneTimeUnPassedTestRateEharts.setOption(oneTimeUnPassedTestRateConfig);
};

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleWindowResize);
});

const isNormalSize = ref(true);
if (window.document.body.clientWidth < 1440) {
  isNormalSize.value = false;
}

const resizeNotify = ref(false);
const handleWindowResize = throttle(500, () => {
  if (!props.onShow) {
    resizeNotify.value = true;
    return;
  }
  if (window.document.body.clientWidth < 1440) {
    isNormalSize.value = false;
  } else {
    isNormalSize.value = true;
  }
  nextTick(() => {
    resizeEcharts();
  });
});

const refresh = () => {
  if (!props.projectId) {
    return;
  }
  loadData();
};

defineExpose({
  refresh,
  handleWindowResize
});
</script>
<template>
  <div class="text-3 mt-2 space-y-2">
    <div class="flex items-center">
      <Icon icon="icon-zonglan" class="text-4 mr-1" />
      <span class="text-3.5 font-medium text-theme-title">总览</span>
    </div>
    <div class="flex space-x-2">
      <div class="border rounded p-2 flex flex-1 space-x-6">
        <div class="text-center flex flex-col space-y-2 min-w-1/7">
          <div class="flex-1 px-2 rounded flex" style="background: linear-gradient( 180deg, #EAF0FF 0%, #F5F8FF 100%)">
            <div class="flex flex-col justify-center flex-1">
              <span class="font-semibold text-4">{{ overViewData.totalTaskNum || overViewData.totalCaseNum || 0 }}</span>
              <div>总数</div>
            </div>
            <img src="./Image/total.png" class="w-10 h-10 self-end" />
          </div>
          <div class="flex-1 px-2 rounded flex" style="background: linear-gradient( 180deg, #E7F7FF 0%, #F1F9FF 100%)">
            <div class="flex flex-col justify-center flex-1">
              <span class="font-semibold text-4">{{ overViewData.progress || 0 }}%</span>
              <div>完成率</div>
            </div>
            <img src="./Image/progress.png" class="w-10 h-10 self-end" />
          </div>
        </div>
        <div class="flex-1 flex flex-col space-y-2">
          <div
            v-for="(col, idx) in taskOverViewConfig"
            v-if="props.countType === 'task'"
            :key="idx"
            class="flex flex-wrap space-x-2 flex-1">
            <div
              v-for="item in col"
              :key="item.dataIndex"
              class="flex-1 min-w-0 p-2 flex space-x-2 items-center">
              <Icon :icon="item.icon" class="text-8" />
              <div class="flex-1 min-w-0">
                <div>{{ overViewData[item.dataIndex] || '0' }}{{ item.unit || '' }}</div>
                {{ item.name }}
              </div>
            </div>
          </div>
          <div
            v-for="(col, idx) in caseOverViewConfig"
            v-if="props.countType === 'useCase'"
            :key="idx"
            class="flex flex-wrap space-x-2 flex-1">
            <div
              v-for="item in col"
              :key="item.dataIndex"
              class="flex-1 min-w-0 p-2 flex space-x-2 items-center">
              <Icon :icon="item.icon" class="text-8" />
              <div class="flex-1 min-w-0">
                <div>{{ overViewData[item.dataIndex] || '0' }}{{ item.unit || '' }}</div>
                {{ item.name }}
              </div>
            </div>
          </div>
        </div>
      </div>
      <div
        v-show="props.countType === 'task'"
        class="min-w-70 border rounded p-2 "
        style="width: calc(20% - 8px)">
        <div class="font-semibold">分类统计</div>
        <div ref="taskTypeRef" class="h-35"></div>
      </div>
      <!-- <div  v-show="props.countType === 'useCase'" class="min-w-70 border rounded p-2">
        <div class="font-semibold">评审状态分布</div>
        <div class="h-35" ref="reviewStatusRef"></div>
      </div> -->
      <div class="min-w-60 border rounded p-2 w-1/5" style="width: calc(20% - 8px)">
        <div class="font-semibold flex justify-between">
          燃尽图
          <RadioGroup v-model:value="burnDownTarget" :options="burnDownOpt">
          </RadioGroup>
        </div>
        <div ref="priorityRef" class="h-35"></div>
      </div>
    </div>
    <div class="flex items-center">
      <Icon icon="icon-paihangbang" class="text-4 mr-1" />
      <span class="text-3.5 font-medium text-theme-title">排行榜</span>
    </div>
    <div class="flex space-x-2">
      <div class="min-w-70 border rounded p-2 flex-1">
        <div class="font-semibold">{{ props.countType ==='task' ? '任务数' : '用例数' }}</div>
        <div ref="targetNumRef" class="h-70"></div>
      </div>
      <div class="min-w-70 border rounded p-2 flex-1">
        <div class="font-semibold">工作量</div>
        <div ref="workloadRef" class="h-70"></div>
      </div>
      <div class="min-w-70 border rounded p-2 flex-1">
        <div class="font-semibold">逾期数</div>
        <div ref="overdueRef" class="h-70"></div>
      </div>
      <div class="min-w-70 border rounded p-2 flex-1">
        <div class="font-semibold">{{ props.countType ==='task' ? '一次性通过数' : '一次性测试通过数' }}</div>
        <div ref="oneTimePassedTestRef" class="h-70"></div>
      </div>
      <div v-if="isNormalSize" class="min-w-70 border rounded p-2 flex-1">
        <div class="font-semibold">{{ props.countType ==='task' ? '缺陷数' : '一次性通过评审数' }}</div>
        <div ref="oneTimeUnPassedTestRef" class="h-70"></div>
      </div>
    </div>
    <div class="flex space-x-2">
      <div class="min-w-70 border rounded p-2 flex-1">
        <div class="font-semibold">{{ props.countType ==='task' ? '任务完成率' : '用例通过率' }}</div>
        <div ref="targetRateRef" class="h-70"></div>
      </div>
      <div class="min-w-70 border rounded p-2 flex-1">
        <div class="font-semibold">节省工作量</div>
        <div ref="workloadRateRef" class="h-70"></div>
      </div>
      <div class="min-w-70 border rounded p-2 flex-1">
        <div class="font-semibold">逾期率</div>
        <div ref="overdueRateRef" class="h-70"></div>
      </div>
      <div class="min-w-70 border rounded p-2 flex-1">
        <div class="font-semibold">{{ props.countType ==='task' ? '一次性通过率' : '一次性测试通过率' }}</div>
        <div ref="oneTimePassedTestRateRef" class="h-70"></div>
      </div>
      <div v-if="isNormalSize" class="min-w-70 border rounded p-2 flex-1">
        <div class="font-semibold">{{ props.countType ==='task' ? '缺陷率' : '一次性评审通过率' }}</div>
        <div ref="oneTimeUnPassedTestRateRef" class="h-70"></div>
      </div>
    </div>
    <div v-if="!isNormalSize" class="flex space-x-2">
      <div class="min-w-70 border rounded p-2 flex-1">
        <div class="font-semibold">{{ props.countType ==='task' ? '缺陷数' : '一次性通过评审数' }}</div>
        <div ref="oneTimeUnPassedTestRef" class="h-70"></div>
      </div>
      <div class="min-w-70 border rounded p-2 flex-1">
        <div class="font-semibold">{{ props.countType ==='task' ? '缺陷率' : '一次性评审通过率' }}</div>
        <div ref="oneTimeUnPassedTestRateRef" class="h-70"></div>
      </div>
    </div>
  </div>
</template>
