<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, Ref, ref, watch, inject } from 'vue';
import { useI18n } from 'vue-i18n';
import { Icon, Image, NoData } from '@xcan-angus/vue-ui';
import { Popover } from 'ant-design-vue';
import elementResizeDetector from 'element-resize-detector';
import * as eCharts from 'echarts';
import { throttle } from 'throttle-debounce';
import { kanban } from '@/api/tester';
import { getDateArr, getDateArrWithTime } from '@/utils/utils';
import { EnumMessage, enumUtils } from '@xcan-angus/infra';
import { ReportCategory } from '@/enums/enums';
import SelectEnum from '@/components/enum/SelectEnum.vue';
import { DataAssetsProps } from './types';
import { useDataAssetsData } from './composables/useDataAssetsData';
import { useChartConfigs } from './composables/useChartConfigs';
import { useChartManagement } from './composables/useChartManagement';

const { t } = useI18n();

// Project type show map from inject
const proTypeShowMap = inject<Ref<{[key: string]: boolean}>>('proTypeShowMap', ref({ showTask: true, showSprint: true }));

// Component props with defaults
const props = withDefaults(defineProps<DataAssetsProps>(), {
  projectId: undefined,
  creatorObjectType: undefined,
  creatorObjectId: undefined,
  createdDateStart: undefined,
  createdDateEnd: undefined,
  onShow: false
});

const params = computed(() => {
  const { onShow, ...other } = props;
  return {
    ...other
  };
});

const rankIcon = {
  0: 'icon-diyiming',
  1: 'icon-dierming',
  2: 'icon-disanming'
};

// Initialize composables
const {
  rankingData,
  loadRankData,
  growthTrendData,
  caseData,
  apiData,
  taskData,
  scenarioData,
  scriptData,
  mockData,
  dataAssetsData,
  loadGrowthTrendData,
  loadAllData,
  refresh
} = useDataAssetsData(props);

const {
  chartSeriesColorConfig,
  methodColorConfig,
  targetDataCategory,
  rankIconConfig,
  createGrowthTrendConfig,
  createCaseBarConfig,
  createCasePieConfig,
  createApiBarConfig,
  createApiPieConfig,
  createTaskBarConfig,
  createTaskPieConfig,
  createPlanPieConfig,
  createSprintPieConfig,
  createScenarioPieConfig,
  createScriptBarConfig
} = useChartConfigs();

const {
  growthTrendConfig,
  caseBarConfig,
  casePieConfig,
  apiBarConfig,
  apiPieConfig,
  taskBarConfig,
  taskPieConfig,
  planPieConfig,
  sprintPieConfig,
  scenarioPieConfig,
  scriptBarConfig,
  setChartRefs,
  initializeCharts,
  updateGrowthTrendChart,
  updateCaseCharts,
  updateApiCharts,
  updateTaskCharts,
  updateScenarioChart,
  updateScriptChart,
  resizeAllCharts,
  handleRightSideResize
} = useChartManagement();

const erd = elementResizeDetector({ strategy: 'scroll' });

const targetType = ref('TASK');
// Target data category is now provided by useChartConfigs composable
const reportCategoryOpt = ref<EnumMessage<ReportCategory>[]>([]);
const loadEnums = () => {
  reportCategoryOpt.value = enumUtils.enumToMessages(ReportCategory);
  if (reportCategoryOpt.value.length) {
    reportBarChartsConfig.xAxis.data = reportCategoryOpt.value.map(i => i.message);
  }
};

// API parameters are now computed in useDataAssetsData composable

// Rank icon configuration is now provided by useChartConfigs composable
// Ranking data and loadRankData are now provided by useDataAssetsData composable

// 增长趋势数据
// Growth trend data loading is now handled by useDataAssetsData and useChartManagement composables

let increasEcharts;
const increasEmpty = ref();
const increasRef = ref(); // 增长趋势echarts ref
const increaseEchartConfig = {
  grid: {
    left: '5%',
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
      name: 'api',
      data: [],
      type: 'line',
      smooth: true
    }
  ]
};

// 用例数据
const loadCase = async () => {
  const [error, { data = {} }] = await kanban.getTesting({ ...params.value });
  if (error) {
    return;
  }
  caseTotal.value = data.allCase || 0;
  const { PENDING = 0, PASSED = 0, NOT_PASSED = 0, BLOCKED = 0, CANCELED = 0 } = data.caseByTestResult || {};
  const barData = [CANCELED, BLOCKED, NOT_PASSED, PASSED, PENDING];
  caseBarEchartsConfig.series[0].data.forEach((item, idx) => {
    item.value = barData[idx];
  });
  caseBarEcharts.setOption(caseBarEchartsConfig);

  if (data.caseByReviewStatus) {
    const { PEDING = 0, PASSED = 0, FAILED = 0 } = data.caseByReviewStatus || {};
    const pieData = [PEDING, PASSED, FAILED];
    casePieEchartsConfig.series[0].data.forEach((item, idx) => {
      item.value = pieData[idx];
    });
    casePieEchartsConfig.title.text = caseTotal.value;
    casePieEcharts.setOption(casePieEchartsConfig);
    nextTick(() => {
      casePieEcharts.resize();
    });
  }
  planTotal.value = data.allPlan || 0;
  planPieEchartsConfig.title.text = planTotal.value;
  if (data.planByStatus) {
    const { PENDING, IN_PROGRESS, COMPLETED, BLOCKED } = data.planByStatus;
    const statusData = [PENDING, IN_PROGRESS, COMPLETED, BLOCKED];
    planPieEchartsConfig.series[0].data.forEach((item, idx) => {
      item.value = statusData[idx];
    });
  } else {
    planPieEchartsConfig.series[0].data.forEach((item) => {
      item.value = 0;
    });
  }
  planPieEcharts.setOption(planPieEchartsConfig);
  nextTick(() => planPieEcharts.resize());
};

const caseTotal = ref(0);
// 用例图表
let caseBarEcharts;
let casePieEcharts;
const caseReviewRef = ref();
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
    data: [t('kanban.dataAssets.status.cancelled'), t('kanban.dataAssets.status.blocked'), t('kanban.dataAssets.status.testFailed'), t('kanban.dataAssets.status.testPassed'), t('kanban.dataAssets.status.pendingTest')]
  },
  series: [
    {
      type: 'bar',
      showBackground: true,
      barMaxWidth: '16',
      data: [{
        value: 0,
        itemStyle: {
          color: 'rgb(246,159,42)'
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

const casePieEchartsConfig = {
  title: {
    text: 0,
    subtext: t('kanban.dataAssets.total'),
    left: '29.5%',
    top: '35%',
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
    trigger: 'item'
  },
  legend: {
    top: 'middle',
    right: '0',
    orient: 'vertical',
    itemHeight: 14,
    itemWidth: 14
  },
  series: [
    {
      name: t('kanban.dataAssets.total'),
      type: 'pie',
      radius: ['40%', '60%'],
      center: ['30%', '50%'],
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
        show: true
      },
      data: [
        {
          name: t('kanban.dataAssets.reviewStatus.pendingReview'),
          value: 0,
          itemStyle: {
            color: 'rgba(201, 119, 255, 1)'
          }
        },
        {
          name: t('kanban.dataAssets.reviewStatus.reviewPassed'),
          value: 0,
          itemStyle: {
            color: 'rgba(82, 196, 26, 1)'
          }
        },
        {
          name: t('kanban.dataAssets.reviewStatus.reviewFailed'),
          value: 0,
          itemStyle: {
            color: 'rgba(245, 34, 45, 1)'
          }
        }
      ]
    }
  ]
};

// 接口数据
const loadApis = async () => {
  const [error, { data = {} }] = await kanban.getApis({ ...params.value });
  if (error) {
    return;
  }
  apiTotal.value = data.allApis || 0;
  apiPieEchartsConfig.title.text = apiTotal.value;
  if (data.apisByMethod) {
    const { PUT = 0, POST = 0, DELETE = 0, GET = 0, HEAD = 0, PATCH = 0, OPTIONS = 0, TRACE = 0 } = data.apisByMethod;
    const methodData = [PUT, POST, HEAD, GET, DELETE, PATCH, OPTIONS, TRACE];
    apiPieEchartsConfig.series[0].data.forEach((item, idx) => {
      item.value = methodData[idx];
    });
  } else {
    apiPieEchartsConfig.series[0].data.forEach(item => {
      item.value = 0;
    });
  }
  apiPieEcharts.setOption(apiPieEchartsConfig);
  nextTick(() => {
    apiPieEcharts.resize();
  });

  if (data.apisByStatus) {
    const { UNKNOWN = 0, IN_DESIGN = 0, IN_DEV = 0, DEV_COMPLETED = 0, RELEASED = 0 } = data.apisByStatus;
    const statusData = [UNKNOWN, IN_DESIGN, IN_DEV, DEV_COMPLETED, RELEASED];
    apiBarEchartsConfig.series?.[0].data.forEach((item, idx) => {
      item.value = statusData[idx];
    });
  } else {
    apiBarEchartsConfig.series?.[0].data.forEach(item => {
      item.value = 0;
    });
  }
  apiBarEcharts.setOption(apiBarEchartsConfig);
};

const apiTotal = ref(0);
// 接口图表
const methodColorSet = {
  GET: 'rgba(30, 136, 229, 1)',
  HEAD: '#67D7FF',
  POST: 'rgba(51, 183, 130, 1)',
  PUT: 'rgba(255, 167, 38, 1)',
  PATCH: 'rgba(171, 71, 188, 1)',
  DELETE: 'rgba(255, 82, 82, 1)',
  OPTIONS: 'rgba(0, 150, 136, 1)',
  TRACE: '#7F91FF'
};
let apiBarEcharts;
let apiPieEcharts;
const apiReviewRef = ref();
const apiStatusRef = ref();
const apiBarEchartsConfig = {
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
    data: [t('kanban.dataAssets.status.unknown'), t('kanban.dataAssets.status.designing'), t('kanban.dataAssets.status.developing'), t('kanban.dataAssets.status.developmentCompleted'), t('kanban.dataAssets.status.published')]
  },
  series: [
    {
      type: 'bar',
      showBackground: true,
      barMaxWidth: '16',
      data: [{
        value: 0,
        itemStyle: {
          color: 'rgba(200, 202, 208, 1)'
        }
      }, {
        value: 0,
        itemStyle: {
          color: '#52C41A'
        }
      }, {
        value: 0,
        itemStyle: {
          color: '#FFB925'
        }
      }, {
        value: 0,
        itemStyle: {
          color: '#FF8100'
        }
      }, {
        value: 0,
        itemStyle: {
          color: '#7F91FF'
        }
      }]
    }
  ]
};
const apiPieEchartsConfig = {
  title: {
    text: 0,
    subtext: '总数',
    left: '29.5%',
    top: '35%',
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
    trigger: 'item'
  },
  legend: {
    top: 'middle',
    right: '0',
    orient: 'vertical',
    itemHeight: 14,
    itemWidth: 14,
    itemGap: 14
  },
  series: [
    {
      name: '',
      type: 'pie',
      radius: ['40%', '60%'],
      center: ['30%', '50%'],
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
        show: true
      },
      data: [
        {
          name: 'PUT',
          value: 0,
          itemStyle: {
            color: methodColorSet.PUT
          }
        },
        {
          name: 'POST',
          value: 0,
          itemStyle: {
            color: methodColorSet.POST
          }
        },
        {
          name: 'HEAD',
          value: 0,
          itemStyle: {
            color: methodColorSet.HEAD
          }
        },
        {
          name: 'GET',
          value: 0,
          itemStyle: {
            color: methodColorSet.GET
          }
        },
        {
          name: 'DELETE',
          value: 0,
          itemStyle: {
            color: methodColorSet.DELETE
          }
        },
        {
          name: 'PATCH',
          value: 0,
          itemStyle: {
            color: methodColorSet.PATCH
          }
        },
        {
          name: 'OPTIONS',
          value: 0,
          itemStyle: {
            color: methodColorSet.OPTIONS
          }
        },
        {
          name: 'TRACE',
          value: 0,
          itemStyle: {
            color: methodColorSet.TRACE
          }
        }
      ]
    }
  ]
};

// 任务/迭代数据
const loadTask = async () => {
  if (!proTypeShowMap.value.showTask) {
    return;
  }
  const [error, { data = {} }] = await kanban.getTask({ ...params.value });
  if (error) {
    return;
  }
  taskTotal.value = data.allTask || 0;
  if (data.taskByType) {
    const { STORY = 0, TASK = 0, BUG = 0, API_TEST = 0, SCENARIO_TEST = 0, REQUIREMENT = 0 } = data.taskByType;
    const typeData = [STORY, REQUIREMENT, TASK, BUG, API_TEST, SCENARIO_TEST];
    taskPieEchartsConfig.series[0].data.forEach((item, idx) => {
      item.value = typeData[idx];
    });
  } else {
    taskPieEchartsConfig.series[0].data.forEach(item => {
      item.value = 0;
    });
  }
  taskPieEchartsConfig.title.text = taskTotal.value;
  taskPieEcharts.setOption(taskPieEchartsConfig);
  nextTick(() => {
    taskPieEcharts.resize();
  });
  if (data.taskByStatus) {
    const { PENDING = 0, IN_PROGRESS = 0, CONFIRMING = 0, COMPLETED = 0, CANCELED = 0 } = data.taskByStatus;
    const statusData = [CANCELED, COMPLETED, CONFIRMING, IN_PROGRESS, PENDING];
    taskBarEchartsConfig.series[0].data.forEach((item, idx) => {
      item.value = statusData[idx];
    });
  } else {
    taskBarEchartsConfig.series[0].data.forEach((item) => {
      item.value = 0;
    });
  }
  taskBarEcharts.setOption(taskBarEchartsConfig);

  if (!proTypeShowMap.value.showSprint) {
    return;
  }
  sprintTotal.value = data.allSprint || 0;
  interationPieEchartsConfig.title.text = sprintTotal.value;
  if (data.sprintByStatus) {
    const { PENDING, IN_PROGRESS, COMPLETED, BLOCKED } = data.sprintByStatus;
    const statusData = [PENDING, IN_PROGRESS, COMPLETED, BLOCKED];
    interationPieEchartsConfig.series[0].data.forEach((item, idx) => {
      item.value = statusData[idx];
    });
  } else {
    interationPieEchartsConfig.series[0].data.forEach(item => {
      item.value = 0;
    });
  }
  interationPieEcharts.setOption(interationPieEchartsConfig);
  nextTick(() => interationPieEcharts.resize());
};

const taskTotal = ref(0);
// 任务图表
let taskBarEcharts;
let taskPieEcharts;
const taskReviewRef = ref();
const taskStatusRef = ref();
const taskBarEchartsConfig = {
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
    data: [t('kanban.dataAssets.status.cancelled'), t('kanban.dataAssets.status.completed'), t('kanban.dataAssets.status.pendingConfirmation'), t('kanban.dataAssets.status.inProgress'), t('kanban.dataAssets.status.pending')]
  },
  series: [
    {
      type: 'bar',
      showBackground: true,
      barMaxWidth: '16',
      data: [
        {
          value: 0,
          itemStyle: {
            color: 'rgba(200, 202, 208, 1)'
          }
        }, {
          value: 0,
          itemStyle: {
            color: '#52C41A'
          }
        }, {
          value: 0,
          itemStyle: {
            color: '#FFB925'
          }
        }, {
          value: 0,
          itemStyle: {
            color: '#FF8100'
          }
        }, {
          value: 0,
          itemStyle: {
            color: '#7F91FF'
          }
        }]
    }
  ]
};
const taskPieEchartsConfig = {
  title: {
    text: 0,
    subtext: t('kanban.dataAssets.total'),
    left: '29.5%',
    top: '35%',
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
    trigger: 'item'
  },
  legend: {
    top: 'middle',
    right: '0',
    orient: 'vertical',
    itemHeight: 14,
    itemWidth: 14,
    itemGap: 6
  },
  series: [
    {
      name: '',
      type: 'pie',
      radius: ['40%', '60%'],
      center: ['30%', '50%'],
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
        show: true
      },
      data: [
        {
          name: t('kanban.dataAssets.taskTypes.story'),
          value: 0,
          itemStyle: {
            color: 'rgba(136, 185, 242, 1)'
          }
        },
        {
          name: t('kanban.dataAssets.taskTypes.requirement'),
          value: 0,
          itemStyle: {
            color: 'rgba(201, 119, 255, 1)'
          }
        },
        {
          name: t('kanban.dataAssets.taskTypes.task'),
          value: 0,
          itemStyle: {
            color: 'rgba(255, 165, 43, 1)'
          }
        },
        {
          name: t('kanban.dataAssets.taskTypes.defect'),
          value: 0,
          itemStyle: {
            color: 'rgba(245, 34, 45, 1)'
          }
        },
        {
          name: t('kanban.dataAssets.taskTypes.apiTest'),
          value: 0,
          itemStyle: {
            color: 'rgba(82, 196, 26, 1)'
          }
        },
        {
          name: t('kanban.dataAssets.taskTypes.scenarioTest'),
          value: 0,
          itemStyle: {
            color: 'rgba(0,119,255,1)'
          }
        }
      ]
    }
  ]
};

const planTotal = ref(0);
// 计划图表
let planPieEcharts;
const planReviewRef = ref();
const planPieEchartsConfig = {
  title: {
    text: 0,
    subtext: t('kanban.dataAssets.total'),
    left: '29.5%',
    top: '35%',
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
    trigger: 'item'
  },
  legend: {
    top: 'middle',
    right: '24',
    orient: 'vertical',
    itemHeight: 14,
    itemWidth: 14,
    itemGap: 12
  },
  series: [
    {
      name: '',
      type: 'pie',
      radius: ['40%', '60%'],
      center: ['30%', '50%'],
      avoidLabelOverlap: true,
      label: {
        show: true,
        formatter: '{c}'
      },
      emphasis: {
        label: {
          show: true
        }
      },
      labelLine: {
        show: true
      },
      itemStyle: {
        borderRadius: 5,
        borderColor: '#fff',
        borderWidth: 1
      },
      data: [
        {
          name: t('kanban.dataAssets.status.pendingStart'),
          value: 0,
          itemStyle: {
            color: 'rgba(45, 142, 255, 1)'
          }
        },
        {
          name: t('kanban.dataAssets.status.inProgress'),
          value: 0,
          itemStyle: {
            color: 'rgba(103, 215, 255, 1)'
          }
        },
        {
          name: t('kanban.dataAssets.status.completed'),
          value: 0,
          itemStyle: {
            color: 'rgba(82, 196, 26, 1)'
          }
        },
        {
          name: t('kanban.dataAssets.status.blockedStatus'),
          value: 0,
          itemStyle: {
            color: 'rgba(245, 34, 45, 1)'
          }
        }
      ]
    }
  ]
};

const sprintTotal = ref(0);
// 迭代图表
let interationPieEcharts;
const interationReviewRef = ref();
const interationPieEchartsConfig = {
  title: {
    text: 0,
    subtext: t('kanban.dataAssets.total'),
    left: '29.5%',
    top: '35%',
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
    trigger: 'item'
  },
  legend: {
    top: 'middle',
    right: '24',
    orient: 'vertical',
    itemHeight: 14,
    itemWidth: 14,
    itemGap: 12
  },
  series: [
    {
      name: '',
      type: 'pie',
      radius: ['40%', '60%'],
      center: ['30%', '50%'],
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
        show: true
      },
      data: [
        {
          name: t('kanban.dataAssets.status.pendingStart'),
          value: 0,
          itemStyle: {
            color: 'rgba(45, 142, 255, 1)'
          }
        },
        {
          name: t('kanban.dataAssets.status.inProgress'),
          value: 0,
          itemStyle: {
            color: 'rgba(103, 215, 255, 1)'
          }
        },
        {
          name: t('kanban.dataAssets.status.completed'),
          value: 0,
          itemStyle: {
            color: 'rgba(82, 196, 26, 1)'
          }
        },
        {
          name: t('kanban.dataAssets.status.blockedStatus'),
          value: 0,
          itemStyle: {
            color: 'rgba(245, 34, 45, 1)'
          }
        }
      ]
    }
  ]
};

// 场景数据
const loadScenario = async () => {
  const [error, { data = {} }] = await kanban.getScenario({ ...params.value });
  if (error) {
    return;
  }
  scenarioTotal.value = data.allSce || 0;
  scenairoPieChartsConfig.title.text = scenarioTotal.value;
  if (data.sceByScriptType) {
    const { TEST_PERFORMANCE, TEST_STABILITY, TEST_FUNCTIONALITY, TEST_CUSTOMIZATION } = data.sceByScriptType;
    const typeData = [TEST_PERFORMANCE, TEST_STABILITY, TEST_FUNCTIONALITY, TEST_CUSTOMIZATION];
    scenairoPieChartsConfig.series[0].data.forEach((item, idx) => {
      item.value = typeData[idx];
    });
  } else {
    scenairoPieChartsConfig.series[0].data.forEach((item) => {
      item.value = 0;
    });
  }
  scenairoPieCharts.setOption(scenairoPieChartsConfig);
  nextTick(() => scenairoPieCharts.resize());
};

const scenarioTotal = ref(0);
// 场景图表
let scenairoPieCharts;
const scenairoRef = ref();
const scenairoPieChartsConfig = {
  title: {
    text: 0,
    subtext: t('kanban.dataAssets.total'),
    left: '29.5%',
    top: '35%',
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
    trigger: 'item'
  },
  legend: {
    top: 'middle',
    right: '0',
    orient: 'vertical',
    itemHeight: 14,
    itemWidth: 14,
    itemGap: 12
  },
  series: [
    {
      name: '',
      type: 'pie',
      radius: ['40%', '60%'],
      center: ['30%', '50%'],
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
        show: true
      },
      data: [
        {
          name: t('kanban.dataAssets.categories.testPerformance'),
          value: 0,
          itemStyle: {
            color: 'rgba(45, 142, 255, 1)'
          }
        },
        {
          name: t('kanban.dataAssets.categories.testStability'),
          value: 0,
          itemStyle: {
            color: 'rgba(201, 119, 255, 1)'
          }
        },
        {
          name: t('kanban.dataAssets.categories.testFunctionality'),
          value: 0,
          itemStyle: {
            color: 'rgba(255, 102, 0, 1)'
          }
        },
        {
          name: t('kanban.dataAssets.categories.testCustomization'),
          value: 0,
          itemStyle: {
            color: 'rgba(82, 196, 26, 1)'
          }
        }
      ]
    }
  ]
};

// 脚本数据
const loadScript = async () => {
  const [error, { data = {} }] = await kanban.getScript({ ...params.value });
  if (error) {
    return;
  }
  scriptTotal.value = data.allScript || 0;

  if (data.scriptByPluginName) {
    const names = Object.keys(data.scriptByPluginName);
    const value = Object.values(data.scriptByPluginName);
    if (names.length) {
      scriptBarChartsConfig.series[0].data = value;
      scriptBarChartsConfig.xAxis.data = names;
      if (names.length > 5) {
        scriptBarChartsConfig.xAxis.axisLabel.rotate = -45;
        scriptBarChartsConfig.grid.bottom = '60';
      } else {
        scriptBarChartsConfig.xAxis.axisLabel.rotate = 0;
        scriptBarChartsConfig.grid.bottom = '30';
      }
    } else {
      scriptBarChartsConfig.series[0].data = [0, 0, 0, 0];
      scriptBarChartsConfig.xAxis.data = ['Http', 'WebSocket', 'Jdbc', 'Tcp'];
      scriptBarChartsConfig.xAxis.axisLabel.rotate = 0;
      scriptBarChartsConfig.grid.bottom = '30';
    }
  } else {
    scriptBarChartsConfig.series[0].data = [0, 0, 0, 0];
    scriptBarChartsConfig.xAxis.data = ['Http', 'WebSocket', 'Jdbc', 'Tcp'];
    scriptBarChartsConfig.xAxis.axisLabel.rotate = 0;
    scriptBarChartsConfig.grid.bottom = '30';
  }
  scriptBarCharts.setOption(scriptBarChartsConfig);
  nextTick(() => scriptBarCharts.resize());
};

const scriptTotal = ref(0);
// 脚本图表
let scriptBarCharts;
const scriptRef = ref();
const scriptBarChartsConfig = {
  grid: {
    left: '60',
    right: '30',
    bottom: '60',
    top: '20'
  },
  xAxis: {
    type: 'category',
    data: ['http', 'websocket', 'jdbc', 'tcp'],
    axisLabel: {
      interval: 0,
      rotate: -45,
      overflow: 'break'
    }
  },
  yAxis: {
    type: 'value'
  },
  tooltip: {
    show: true
  },
  series: [
    {
      itemStyle: {
        color: 'rgba(45, 142, 255, 1)',
        borderRadius: [5, 5, 0, 0]
      },

      data: [0, 0, 0, 0],
      type: 'bar',
      barMaxWidth: '20',
      label: {
        show: true,
        position: 'top'
      }
    }
  ]
};

// 报告数据
// const loadReport = async () => {
//   const [error, { data = {} }] = await kanban.loadReport({ ...params.value });
//   if (error) {
//     return;
//   }
//   reportTotal.value = data.allReport || 0;
//   if (data.reportByCategory) {
//     if (reportCategoryOpt.value.length) {
//       reportBarChartsConfig.series[0].data = reportCategoryOpt.value.map(i => {
//         return data.reportByCategory?.[i.value] || 0;
//       });
//     } else {
//       const { APIS, FUNCTIONAL, PROJECT, SCENARIO, TASK, EXECUTION } = data.reportByCategory;
//       const yData = [APIS, FUNCTIONAL, PROJECT, SCENARIO, TASK, EXECUTION];
//       reportBarChartsConfig.series[0].data = yData;
//     }
//   } else {
//     reportBarChartsConfig.series[0].data = [];
//   }
//   reportBarCharts.setOption(reportBarChartsConfig);
//   nextTick(() => reportBarCharts.resize());
// };

// 报告图表
// let reportBarCharts;
// const reportRef = ref();
const reportBarChartsConfig = {
  grid: {
    left: '60',
    right: '20',
    bottom: '30',
    top: '20'
  },
  xAxis: {
    type: 'category',
    data: [t('kanban.dataAssets.categories.apis'), t('kanban.dataAssets.categories.cases'), t('kanban.dataAssets.categories.project'), t('kanban.dataAssets.categories.scenario'), t('kanban.dataAssets.categories.task'), t('kanban.dataAssets.categories.execution')],
    axisLabel: {
      interval: 0,
      rotate: -15,
      overflow: 'break'
    }
  },
  yAxis: {
    type: 'value'
  },
  tooltip: {
    show: true
  },
  series: [
    {
      itemStyle: {
        color: 'rgba(45, 142, 255, 1)',
        borderRadius: [5, 5, 0, 0]
      },
      barMaxWidth: '20',
      data: [],
      type: 'bar',
      label: {
        show: true,
        position: 'top'
      }
    }
  ]
};
const reportTotal = ref(0);
// mock数据
const loadMock = async () => {
  const [error, { data = {} }] = await kanban.getMock({ ...params.value });
  if (error) {
    return;
  }
  const { allApi, allService, allResponse, allPushback } = data;
  mockData.allApi = allApi || 0;
  mockData.allService = allService || 0;
  mockData.allResponse = allResponse || 0;
  mockData.allPushback = allPushback || 0;
};

// const mockData = reactive({
//   allApi: 0,
//   allService: 0,
//   allResponse: 0,
//   allPushback: 0
// });

// 数据数据
const loadData = async () => {
  const [error, { data = {} }] = await kanban.getData({ ...params.value });
  if (error) {
    return;
  }
  const { allDataset, allDatasource, allFile, allVariable } = data;
  dataData.allDataset = allDataset || 0;
  dataData.allDatasource = allDatasource || 0;
  dataData.allFile = allFile || 0;
  dataData.allVariable = allVariable || 0;
};

const dataData = reactive({
  allDataset: 0,
  allDatasource: 0,
  allFile: 0,
  allVariable: 0
});

// 当前屏幕宽度大于1560，显示三列，小于1560显示两列
const echartsCol = ref(2);
if (window.document.body.clientWidth >= 1560) {
  echartsCol.value = 3;
}
const rightPercent = ref('calc(25% - 24px)');
const resizeNotify = ref(false);
const handleWindowResize = throttle(500, () => {
  if (!props.onShow) {
    resizeNotify.value = true;
    return;
  }
  if (window.document.body.clientWidth < 1560) {
    echartsCol.value = 2;
  } else {
    echartsCol.value = 3;
  }
  nextTick(() => {
    resizeEcharts();
  });
});

const resizeEcharts = () => {
  increasEcharts.resize();
  caseBarEcharts.resize();
  casePieEcharts.resize();

  apiBarEcharts = eCharts.init(apiStatusRef.value);
  apiBarEcharts.setOption(apiBarEchartsConfig);

  apiPieEcharts = eCharts.init(apiReviewRef.value);
  apiPieEcharts.setOption(apiPieEchartsConfig);
  apiBarEcharts.resize();
  apiPieEcharts.resize();

  if (proTypeShowMap.value.showTask) {
    taskBarEcharts.resize();
    taskPieEcharts.resize();
  }
  planPieEcharts.resize();
  interationPieEcharts.resize();
  scenairoPieCharts.resize();
  scriptBarCharts.resize();
  // reportBarCharts.resize();
};

// 监听右侧宽度变化，重绘右侧Echarts
const rightWrapRef = ref();
const resizeRightEchart = () => {
  if (rightWrapRef.value.clientWidth < 350) {
    if (scenairoPieChartsConfig.series[0].labelLine.length === 5) {
      return;
    } else {
      scenairoPieChartsConfig.series[0].labelLine.length = 5;
      planPieEchartsConfig.series[0].labelLine.length = 5;
      interationPieEchartsConfig.series[0].labelLine.length = 5;
      scenairoPieChartsConfig.series[0].radius = ['40%', '60%'];
      planPieEchartsConfig.series[0].radius = ['40%', '60%'];
      interationPieEchartsConfig.series[0].radius = ['40%', '60%'];
    }
  } else {
    if (!scenairoPieChartsConfig.series[0].labelLine.length) {
      return;
    } else {
      delete scenairoPieChartsConfig.series[0].labelLine.length;
      delete planPieEchartsConfig.series[0].labelLine.length;
      delete interationPieEchartsConfig.series[0].labelLine.length;
      scenairoPieChartsConfig.series[0].radius = ['50%', '70%'];
      planPieEchartsConfig.series[0].radius = ['50%', '70%'];
      interationPieEchartsConfig.series[0].radius = ['50%', '70%'];
    }
  }
  scenairoPieCharts.setOption(scenairoPieChartsConfig, true);
  planPieEcharts.setOption(planPieEchartsConfig, true);
  interationPieEcharts.setOption(planPieEchartsConfig, true);
};

const shouldNotify = ref(false);


const updateIncreasEcharts = () => {
  const data = growthTrendData.value;
  const keys = Object.keys(data);
  if (keys.length) {
    increasEmpty.value = false;
    const xData = [];
    keys.forEach(key => {
      data[key].forEach(i => {
        if (!xData.includes(i.timeSeries)) {
          xData.push(i.timeSeries);
        }
      });
    });
    xData.sort((a, b) => {
      return a > b ? 1 : a < b ? -1 : 0;
    });
    increaseEchartConfig.series = keys.map((key, idx) => {
      return {
        name: targetDataCategory[key],
        data: xData.map(i => {
          const target = data[key].find(item => item.timeSeries === i);
          if (target) {
            return target.value;
          } else {

          }
        }),
        itemStyle: {
          color: new eCharts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 1, color: `rgba(${chartSeriesColorConfig[idx]}, 0.1)` },
            { offset: 0, color: `rgba(${chartSeriesColorConfig[idx]}, 1)` }
          ])
        },
        type: 'line',
        smooth: true,
        connectNulls: true,
        areaStyle: {}
      };
    });
    increaseEchartConfig.xAxis.data = xData;
  } else {
    increaseEchartConfig.xAxis.data = [];
    increaseEchartConfig.series = [];
    increasEmpty.value = true;
  }

  if (increaseEchartConfig.xAxis.data.length === 0) {
    if (props.createdDateEnd && props.createdDateStart) {
      increaseEchartConfig.xAxis.data = getDateArrWithTime(props.createdDateStart, props.createdDateEnd);
    } else {
      increaseEchartConfig.xAxis.data = getDateArr();
    }
    if (increaseEchartConfig.series.length) {
      increaseEchartConfig.series[0].data = Array.from(new Array(increaseEchartConfig.xAxis.data.length)).fill(0);
    }
  }
  increasEcharts.setOption(increaseEchartConfig, true);
};

onMounted(async () => {
  await loadEnums();
  watch(() => proTypeShowMap.value.showTask, () => {
    if (!proTypeShowMap.value.showTask && targetType.value === 'TASK') {
      targetType.value = 'FUNC';
    }
  }, {
    immediate: true
  });
  watch(() => targetType.value, async () => {
    if (props.projectId) {
      loadGrowthTrendData(targetType.value);
    }
  });
  watch(() => proTypeShowMap.value.ShowTask, () => {
    nextTick(() => {
      initChart();
    });
  }, {
    deep: true
  });

  watch([() => props.createdDateEnd, () => props.createdDateStart, () => props.creatorObjectId, () => props.creatorObjectType, () => props.projectId], async () => {
    if (!props.onShow && props.projectId) {
      shouldNotify.value = true;
      return;
    }
    if (props.projectId) {
      await loadGrowthTrendData(targetType.value);
      loadRankData();
      loadCase();
      loadApis();
      loadTask();
      loadScenario();
      loadScript();
      loadMock();
      loadData();
      // loadReport();
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

  increasEcharts = eCharts.init(increasRef.value);
  increasEcharts.setOption(increaseEchartConfig);

  planPieEcharts = eCharts.init(planReviewRef.value);
  planPieEcharts.setOption(planPieEchartsConfig);

  scenairoPieCharts = eCharts.init(scenairoRef.value);
  scenairoPieCharts.setOption(scenairoPieChartsConfig);

  initChart();


  watch(() => growthTrendData.value, () => {
    updateIncreasEcharts();
  })

  window.addEventListener('resize', handleWindowResize);
  erd.listenTo(rightWrapRef.value, resizeRightEchart);
});

const initChart = () => {
  caseBarEcharts = eCharts.init(caseStatusRef.value);
  caseBarEcharts.setOption(caseBarEchartsConfig);

  casePieEcharts = eCharts.init(caseReviewRef.value);
  casePieEcharts.setOption(casePieEchartsConfig);

  apiBarEcharts = eCharts.init(apiStatusRef.value);
  apiBarEcharts.setOption(apiBarEchartsConfig);

  apiPieEcharts = eCharts.init(apiReviewRef.value);
  apiPieEcharts.setOption(apiPieEchartsConfig);

  if (taskStatusRef.value) {
    taskBarEcharts = eCharts.init(taskStatusRef.value);
    taskBarEcharts.setOption(taskBarEchartsConfig);

    taskPieEcharts = eCharts.init(taskReviewRef.value);
    taskPieEcharts.setOption(taskPieEchartsConfig);
  }

  scriptBarCharts = eCharts.init(scriptRef.value);
  scriptBarCharts.setOption(scriptBarChartsConfig);

  // reportBarCharts = eCharts.init(reportRef.value);
  // reportBarCharts.setOption(reportBarChartsConfig);

  if (interationReviewRef.value) {
    interationPieEcharts = eCharts.init(interationReviewRef.value);
    interationPieEcharts.setOption(interationPieEchartsConfig);
  }
};

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleWindowResize);
  erd.removeListener(rightWrapRef.value, resizeRightEchart);
});



// const refresh = () => {
//   if (!props.projectId) {
//     return;
//   }
//   loadGrowthTrendData();
//   loadRankData();
//   loadCase();
//   loadApis();
//   loadTask();
//   loadScenario();
//   loadScript();
//   loadMock();
//   loadData();
//   // loadReport();
// };

defineExpose({
  refresh,
  handleWindowResize
});

</script>
<template>
  <div class="flex space-x-2 mt-2 text-3">
    <div class="py-2 border rounded flex-1 min-w-0">
      <div class="px-2">
        {{ t('kanban.dataAssets.growthTrend') }}：
        <SelectEnum
          v-model:value="targetType"
          :lazy="false"
          :excludes="(opt) => opt.value === 'TASK' && !proTypeShowMap.showTask"
          enumKey="DataAssetsCategory"
          class="w-50" />
      </div>
      <NoData v-show="increasEmpty" class="h-60"></NoData>
      <div
        v-show="!increasEmpty"
        ref="increasRef"
        class="h-60"></div>
    </div>
    <div
      style="min-width: 320px;"
      :style="{ width: rightPercent }"
      class="border rounded p-2">
      <div class="flex space-x-1 items-center">
        <span class="font-semibold">{{ t('kanban.dataAssets.contributionRanking') }}</span>
        <Popover
          :content="t('kanban.dataAssets.rankingDescription')">
          <Icon icon="icon-tishi1" class="text-blue-icon text-3.5" />
        </Popover>
      </div>
      <div class="h-60 overflow-y-auto space-y-2 py-2">
        <div
          v-for="(item, idx) in rankingData"
          :key="item.userId"
          class="flex items-center px-2 space-x-2">
          <div class="relative">
            <Icon :icon="rankIcon[idx]" class="text-4.5" />
            <span v-if="idx < 3" class="absolute z-20 left-1.5 text-white">{{ idx + 1 }}</span>
          </div>
          <Image
            class="w-6 h-6 rounded-full"
            type="avatar"
            :src="item.avatar" />
          <div class="flex-1 truncate min-w-0" :title="item.fullName">{{ item.fullName }}</div>
          <div>{{ item.count }}</div>
        </div>
        <NoData
          v-if="!rankingData.length"
          size="small"
          text="t('common.noData')" />
      </div>
    </div>
  </div>
  <div class="flex mt-2 text-3">
    <div v-if="proTypeShowMap.showTask" class="flex-1 min-w-0 mr-2">
      <div class="flex space-x-2">
        <!--任务-->
        <div class="border rounded p-2 min-w-0" :class="`flex-1/${echartsCol}`">
          <div class="font-semibold">{{ t('kanban.dataAssets.categories.task') }}</div>
          <div class="flex">
            <!-- <div class="px-2 text-center flex flex-col justify-center">
                            <div class="text-5 font-semibold">{{ taskTotal }}</div>
                            <div>总数</div>
                       </div> -->
            <div ref="taskReviewRef" class="h-35 flex-1 min-w-0"></div>
          </div>
          <div ref="taskStatusRef" class="h-40"></div>
        </div>

        <!--用例-->
        <div
          class=" border rounded p-2 min-w-0"
          :class="`flex-1/${echartsCol}`">
          <div class="font-semibold">{{ t('kanban.dataAssets.categories.cases') }}</div>
          <div class="flex">
            <div ref="caseReviewRef" class="h-35 flex-1 min-w-0"></div>
          </div>
          <div ref="caseStatusRef" class="h-40"></div>
        </div>

        <!--接口-->
        <div
          v-if="echartsCol === 3"
          class=" border rounded p-2 min-w-0"
          :class="`flex-1/${echartsCol}`">
          <div class="font-semibold">{{ t('kanban.dataAssets.categories.apis') }}</div>
          <div class="flex">
            <div ref="apiReviewRef" class="h-35 flex-1 min-w-0"></div>
          </div>
          <div ref="apiStatusRef" class="h-40"></div>
        </div>
      </div>
      <div v-if="echartsCol === 2" class="flex space-x-2 mt-2">
        <!--接口-->
        <div class=" border rounded p-2 min-w-0" :class="`flex-1/${echartsCol}`">
          <div class="font-semibold">{{ t('kanban.dataAssets.categories.apis') }}</div>
          <div class="flex">
            <div ref="apiReviewRef" class="h-35 flex-1 min-w-0"></div>
          </div>
          <div ref="apiStatusRef" class="h-40"></div>
        </div>
        <!--mock、数据-->
        <div class="flex flex-col space-y-2 min-h-0" :class="`flex-1/${echartsCol}`">
          <div class="flex-1 rounded border p-2 flex flex-col">
            <div class="px-2 font-semibold">Mock</div>
            <div class=" flex-1 flex justify-around items-center space-x-2">
              <div class="flex-1 text-center h-full bg-board-blue flex flex-col justify-center rounded relative min-w-0">
                <img src="./image/service.png" class="w-1/2 absolute top-0 right-0" />
                <div :title="mockData.allService" class="text-5 truncate">{{ mockData.allService }}</div>
                <div>{{ t('kanban.dataAssets.categories.service') }}</div>
              </div>
              <div class="flex-1 text-center h-full bg-board-orange flex flex-col justify-center rounded relative min-w-0">
                <img src="./image/apis.png" class="w-1/2 absolute top-0 right-0" />
                <div :title="mockData.allApi" class="text-5 truncate">{{ mockData.allApi }}</div>
                <div>{{ t('kanban.dataAssets.categories.apis') }}</div>
              </div>
              <div class="flex-1 text-center h-full bg-board-qing flex flex-col justify-center rounded relative min-w-0">
                <img src="./image/response.png" class="w-1/2 absolute top-0 right-0" />
                <div :title="mockData.allResponse" class="text-5 truncate">{{ mockData.allResponse }}</div>
                <div>{{ t('kanban.dataAssets.categories.response') }}</div>
              </div>
              <div class="flex-1 text-center h-full bg-board-yellow flex flex-col justify-center rounded relative min-w-0">
                <img src="./image/pushback.png" class="w-1/2 absolute top-0 right-0" />
                <div :title="mockData.allPushback" class="text-5 truncate">{{ mockData.allPushback }}</div>
                <div>{{ t('kanban.dataAssets.categories.pushback') }}</div>
              </div>
            </div>
          </div>
          <div class="flex-1 rounded border p-2 flex flex-col">
            <div class="px-2 font-semibold">{{ t('kanban.dataAssets.categories.data') }}</div>
            <div class=" flex-1 flex justify-around items-center space-x-2">
              <div class="flex-1 text-center h-full bg-board-blue flex flex-col justify-center rounded relative min-w-0">
                <img src="./image/variable.png" class="w-1/2 absolute top-0 right-0" />
                <div :title="dataData.allVariable" class="text-5 truncate">{{ dataData.allVariable }}</div>
                <div>{{ t('kanban.dataAssets.categories.variable') }}</div>
              </div>
              <div class="flex-1 text-center h-full bg-board-orange flex flex-col justify-center rounded relative min-w-0">
                <img src="./image/dataSet.png" class="w-1/2 absolute top-0 right-0" />
                <div :title="dataData.allDataset" class="text-5 truncate">{{ dataData.allDataset }}</div>
                <div>{{ t('kanban.dataAssets.categories.dataset') }}</div>
              </div>
              <div class="flex-1 text-center h-full bg-board-qing flex flex-col justify-center rounded relative min-w-0">
                <img src="./image/file.png" class="w-1/2 absolute top-0 right-0" />
                <div :title="dataData.allFile" class="text-5 truncate">{{ dataData.allFile }}</div>
                <div>{{ t('kanban.dataAssets.categories.file') }}</div>
              </div>
              <div class="flex-1 text-center h-full bg-board-yellow flex flex-col justify-center rounded relative min-w-0">
                <img src="./image/dataSource.png" class="w-1/2 absolute top-0 right-0" />
                <div :title="dataData.allDatasource" class="text-5 truncate">{{ dataData.allDatasource }}</div>
                <div>{{ t('kanban.dataAssets.categories.datasource') }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div class="flex space-x-2 mt-2">
        <!--mock、数据-->
        <div
          v-if="echartsCol === 3"
          class="flex space-x-2 min-w-0"
          :class="`flex-1/${echartsCol}`">
          <div class="flex-1 rounded border p-2 flex flex-col">
            <div class="px-2 font-semibold">Mock</div>
            <div class=" flex-1 flex justify-around flex-col space-y-2">
              <div class="flex flex-1  space-x-2">
                <div class="flex-1 text-center h-full bg-board-blue flex flex-col justify-center rounded relative min-w-0">
                  <img src="./image/service.png" class="w-1/2 absolute top-0 right-0" />
                  <div :title="mockData.allService" class="text-5 truncate">{{ mockData.allService }}</div>
                  <div>{{ t('kanban.dataAssets.categories.service') }}</div>
                </div>
                <div class="flex-1 text-center h-full bg-board-orange flex flex-col justify-center rounded relative min-w-0">
                  <img src="./image/apis.png" class="w-1/2 absolute top-0 right-0" />
                  <div :title="mockData.allApi" class="text-5 truncate">{{ mockData.allApi }}</div>
                  <div>{{ t('kanban.dataAssets.categories.apis') }}</div>
                </div>
              </div>
              <div class="flex flex-1  space-x-2">
                <div class="flex-1 text-center h-full bg-board-qing flex flex-col justify-center rounded relative min-w-0">
                  <img src="./image/response.png" class="w-1/2 absolute top-0 right-0" />
                  <div :title="mockData.allResponse" class="text-5 truncate">{{ mockData.allResponse }}</div>
                  <div>{{ t('kanban.dataAssets.categories.response') }}</div>
                </div>
                <div class="flex-1 text-center h-full bg-board-yellow flex flex-col justify-center rounded relative min-w-0">
                  <img src="./image/pushback.png" class="w-1/2 absolute top-0 right-0" />
                  <div :title="mockData.allPushback" class="text-5 truncate">{{ mockData.allPushback }}</div>
                  <div>{{ t('kanban.dataAssets.categories.pushback') }}</div>
                </div>
              </div>
            </div>
          </div>
          <div class="flex-1 rounded border p-2 flex flex-col">
            <div class="px-2 font-semibold">{{ t('kanban.dataAssets.categories.data') }}</div>
            <div class=" flex-1 flex flex-col justify-around space-y-2">
              <div class="flex flex-1  space-x-2">
                <div class="flex-1 text-center h-full bg-board-blue flex flex-col justify-center rounded relative min-w-0">
                  <img src="./image/variable.png" class="w-1/2 absolute top-0 right-0" />
                  <div :title="dataData.allVariable" class="text-5 truncate">{{ dataData.allVariable }}</div>
                  <div>{{ t('kanban.dataAssets.categories.variable') }}</div>
                </div>
                <div class="flex-1 text-center h-full bg-board-orange flex flex-col justify-center rounded relative min-w-0">
                  <img src="./image/dataSet.png" class="w-1/2 absolute top-0 right-0" />
                  <div :title="dataData.allDataset" class="text-5 truncate">{{ dataData.allDataset }}</div>
                  <div>{{ t('kanban.dataAssets.categories.dataset') }}</div>
                </div>
              </div>
              <div class="flex flex-1 space-x-2">
                <div class="flex-1 text-center h-full bg-board-qing flex flex-col justify-center rounded relative min-w-0">
                  <img src="./image/file.png" class="w-1/2 absolute top-0 right-0" />
                  <div :title="dataData.allFile" class="text-5 truncate">{{ dataData.allFile }}</div>
                  <div>{{ t('kanban.dataAssets.categories.file') }}</div>
                </div>
                <div class="flex-1 text-center h-full bg-board-yellow flex flex-col justify-center rounded relative min-w-0">
                  <img src="./image/dataSource.png" class="w-1/2 absolute top-0 right-0" />
                  <div :title="dataData.allDatasource" class="text-5 truncate">{{ dataData.allDatasource }}</div>
                  <div>{{ t('kanban.dataAssets.categories.datasource') }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!--脚本-->
        <div class="border rounded py-2 min-w-0" :class="`flex-${echartsCol === 2 ? 1 : '2/3'}`">
          <div class="px-2 font-semibold">{{ t('kanban.dataAssets.categories.script') }}</div>
          <div class="flex px-2">
            <div class="px-2 text-center flex flex-col justify-center">
              <div class="font-semibold">{{ scriptTotal }}</div>
              <div>{{ t('kanban.dataAssets.total') }}</div>
            </div>
            <div ref="scriptRef" class="h-35 flex-1 min-w-0"></div>
          </div>
        </div>

        <!--报告-->
        <!--        <div class=" border rounded py-2 min-w-0" :class="`flex-1/${echartsCol}`">-->
        <!--          <div class="px-2 font-semibold">报告</div>-->
        <!--          <div class="flex px-2">-->
        <!--            <div class="px-2 text-center flex flex-col justify-center">-->
        <!--              <div class=" font-semibold">{{ reportTotal }}</div>-->
        <!--              <div>总数</div>-->
        <!--            </div>-->
        <!--            <div ref="reportRef" class="h-35 flex-1 min-w-0"></div>-->
        <!--          </div>-->
        <!--        </div>-->
      </div>
    </div>
    <div v-if="!proTypeShowMap.showTask" class="flex-1 min-w-0">
      <div class="flex flex-wrap">
        <!--用例-->
        <div
          class="min-w-0 flex-grow-0 pr-2"
          :class="`flex-1/${echartsCol}`">
          <div class="border rounded p-2 pb-2">
            <div class="font-semibold">{{ t('kanban.dataAssets.categories.cases') }}</div>
            <div class="flex">
              <div ref="caseReviewRef" class="h-35 flex-1 min-w-0"></div>
            </div>
            <div ref="caseStatusRef" class="h-40"></div>
          </div>
        </div>

        <!--接口-->
        <div class="min-w-0 flex-grow-0 pr-2 pb-2" :class="`flex-1/${echartsCol}`">
          <div class="border rounded p-2">
            <div class="font-semibold">{{ t('kanban.dataAssets.categories.apis') }}</div>
            <div class="flex">
              <div ref="apiReviewRef" class="h-35 flex-1 min-w-0"></div>
            </div>
            <div ref="apiStatusRef" class="h-40"></div>
          </div>
        </div>

        <!--mock、数据-->
        <div :class="`flex-1/${echartsCol}`" class="min-h-0 flex-grow-0 pr-2 pb-2">
          <div v-if="echartsCol === 3" class="flex flex-col space-y-2 h-full">
            <div class="flex-1 rounded border p-2 flex flex-col">
              <div class="px-2 font-semibold">Mock</div>
              <div class=" flex-1 flex justify-around items-center space-x-2">
                <div class="flex-1 text-center h-full bg-board-blue flex flex-col justify-center rounded relative min-w-0">
                  <img src="./image/service.png" class="w-1/2 absolute top-0 right-0" />
                  <div :title="mockData.allService" class="text-5 truncate">{{ mockData.allService }}</div>
                  <div>{{ t('kanban.dataAssets.categories.service') }}</div>
                </div>
                <div class="flex-1 text-center h-full bg-board-orange flex flex-col justify-center rounded relative min-w-0">
                  <img src="./image/apis.png" class="w-1/2 absolute top-0 right-0" />
                  <div :title="mockData.allApi" class="text-5 truncate">{{ mockData.allApi }}</div>
                  <div>{{ t('kanban.dataAssets.categories.apis') }}</div>
                </div>
                <div class="flex-1 text-center h-full bg-board-qing flex flex-col justify-center rounded relative min-w-0">
                  <img src="./image/response.png" class="w-1/2 absolute top-0 right-0" />
                  <div :title="mockData.allResponse" class="text-5 truncate">{{ mockData.allResponse }}</div>
                  <div>{{ t('kanban.dataAssets.categories.response') }}</div>
                </div>
                <div class="flex-1 text-center h-full bg-board-yellow flex flex-col justify-center rounded relative min-w-0">
                  <img src="./image/pushback.png" class="w-1/2 absolute top-0 right-0" />
                  <div :title="mockData.allPushback" class="text-5 truncate">{{ mockData.allPushback }}</div>
                  <div>{{ t('kanban.dataAssets.categories.pushback') }}</div>
                </div>
              </div>
            </div>
            <div class="flex-1 rounded border p-2 flex flex-col">
              <div class="px-2 font-semibold">{{ t('kanban.dataAssets.categories.data') }}</div>
              <div class=" flex-1 flex justify-around items-center space-x-2">
                <div class="flex-1 text-center h-full bg-board-blue flex flex-col justify-center rounded relative min-w-0">
                  <img src="./image/variable.png" class="w-1/2 absolute top-0 right-0" />
                  <div :title="dataData.allVariable" class="text-5 truncate">{{ dataData.allVariable }}</div>
                  <div>{{ t('kanban.dataAssets.categories.variable') }}</div>
                </div>
                <div class="flex-1 text-center h-full bg-board-orange flex flex-col justify-center rounded relative min-w-0">
                  <img src="./image/dataSet.png" class="w-1/2 absolute top-0 right-0" />
                  <div :title="dataData.allDataset" class="text-5 truncate">{{ dataData.allDataset }}</div>
                  <div>{{ t('kanban.dataAssets.categories.dataset') }}</div>
                </div>
                <div class="flex-1 text-center h-full bg-board-qing flex flex-col justify-center rounded relative min-w-0">
                  <img src="./image/file.png" class="w-1/2 absolute top-0 right-0" />
                  <div :title="dataData.allFile" class="text-5 truncate">{{ dataData.allFile }}</div>
                  <div>{{ t('kanban.dataAssets.categories.file') }}</div>
                </div>
                <div class="flex-1 text-center h-full bg-board-yellow flex flex-col justify-center rounded relative min-w-0">
                  <img src="./image/dataSource.png" class="w-1/2 absolute top-0 right-0" />
                  <div :title="dataData.allDatasource" class="text-5 truncate">{{ dataData.allDatasource }}</div>
                  <div>{{ t('kanban.dataAssets.categories.datasource') }}</div>
                </div>
              </div>
            </div>
          </div>
          <div
            v-if="echartsCol === 2"
            class="flex space-x-2 min-w-0 h-full"
            :class="`flex-1/${echartsCol}`">
            <div class="flex-1 rounded border p-2 flex flex-col">
              <div class="px-2 font-semibold">Mock</div>
              <div class=" flex-1 flex justify-around flex-col space-y-2">
                <div class="flex flex-1  space-x-2">
                  <div class="flex-1 text-center h-full bg-board-blue flex flex-col justify-center rounded relative min-w-0">
                    <img src="./image/service.png" class="w-1/2 absolute top-0 right-0" />
                    <div :title="mockData.allService" class="text-5 truncate">{{ mockData.allService }}</div>
                    <div>{{ t('kanban.dataAssets.categories.service') }}</div>
                  </div>
                  <div class="flex-1 text-center h-full bg-board-orange flex flex-col justify-center rounded relative min-w-0">
                    <img src="./image/apis.png" class="w-1/2 absolute top-0 right-0" />
                    <div :title="mockData.allApi" class="text-5 truncate">{{ mockData.allApi }}</div>
                    <div>{{ t('kanban.dataAssets.categories.apis') }}</div>
                  </div>
                </div>
                <div class="flex flex-1  space-x-2">
                  <div class="flex-1 text-center h-full bg-board-qing flex flex-col justify-center rounded relative min-w-0">
                    <img src="./image/response.png" class="w-1/2 absolute top-0 right-0" />
                    <div :title="mockData.allResponse" class="text-5 truncate">{{ mockData.allResponse }}</div>
                    <div>{{ t('kanban.dataAssets.categories.response') }}</div>
                  </div>
                  <div class="flex-1 text-center h-full bg-board-yellow flex flex-col justify-center rounded relative min-w-0">
                    <img src="./image/pushback.png" class="w-1/2 absolute top-0 right-0" />
                    <div :title="mockData.allPushback" class="text-5 truncate">{{ mockData.allPushback }}</div>
                    <div>{{ t('kanban.dataAssets.categories.pushback') }}</div>
                  </div>
                </div>
              </div>
            </div>
            <div class="flex-1 rounded border p-2 flex flex-col">
              <div class="px-2 font-semibold">{{ t('kanban.dataAssets.categories.data') }}</div>
              <div class=" flex-1 flex flex-col justify-around space-y-2">
                <div class="flex flex-1  space-x-2">
                  <div class="flex-1 text-center h-full bg-board-blue flex flex-col justify-center rounded relative min-w-0">
                    <img src="./image/variable.png" class="w-1/2 absolute top-0 right-0" />
                    <div :title="dataData.allVariable" class="text-5 truncate">{{ dataData.allVariable }}</div>
                    <div>{{ t('kanban.dataAssets.categories.variable') }}</div>
                  </div>
                  <div class="flex-1 text-center h-full bg-board-orange flex flex-col justify-center rounded relative min-w-0">
                    <img src="./image/dataSet.png" class="w-1/2 absolute top-0 right-0" />
                    <div :title="dataData.allDataset" class="text-5 truncate">{{ dataData.allDataset }}</div>
                    <div>{{ t('kanban.dataAssets.categories.dataset') }}</div>
                  </div>
                </div>
                <div class="flex flex-1 space-x-2">
                  <div class="flex-1 text-center h-full bg-board-qing flex flex-col justify-center rounded relative min-w-0">
                    <img src="./image/file.png" class="w-1/2 absolute top-0 right-0" />
                    <div :title="dataData.allFile" class="text-5 truncate">{{ dataData.allFile }}</div>
                    <div>{{ t('kanban.dataAssets.categories.file') }}</div>
                  </div>
                  <div class="flex-1 text-center h-full bg-board-yellow flex flex-col justify-center rounded relative min-w-0">
                    <img src="./image/dataSource.png" class="w-1/2 absolute top-0 right-0" />
                    <div :title="dataData.allDatasource" class="text-5 truncate">{{ dataData.allDatasource }}</div>
                    <div>{{ t('kanban.dataAssets.categories.datasource') }}</div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!--脚本-->
        <div class="min-w-0 flex-grow-0 pr-2 pb-2" :class="`flex-${echartsCol === 2 ? 1 : '2/3'}`">
          <div class="border rounded py-2">
            <div class="px-2 font-semibold">{{ t('kanban.dataAssets.categories.script') }}</div>
            <div class="flex px-2">
              <div class="px-2 text-center flex flex-col justify-center">
                <div class="font-semibold">{{ scriptTotal }}</div>
                <div>{{ t('kanban.dataAssets.total') }}</div>
              </div>
              <div ref="scriptRef" class="h-35 flex-1 min-w-0"></div>
            </div>
          </div>
        </div>

        <!--报告-->
        <!--        <div class=" pr-2 min-w-0  flex-grow-0 pb-2" :class="`flex-1/${echartsCol}`">-->
        <!--          <div class="border rounded py-2">-->
        <!--            <div class="px-2 font-semibold">报告</div>-->
        <!--            <div class="flex px-2">-->
        <!--              <div class="px-2 text-center flex flex-col justify-center">-->
        <!--                <div class=" font-semibold">{{ reportTotal }}</div>-->
        <!--                <div>总数</div>-->
        <!--              </div>-->
        <!--              <div ref="reportRef" class="h-35 flex-1 min-w-0"></div>-->
        <!--            </div>-->
        <!--          </div>-->
        <!--        </div>-->
      </div>
    </div>
    <div
      ref="rightWrapRef"
      style="min-width: 320px;"
      :style="{ width: rightPercent }"
      class="flex flex-col space-y-2">
      <!--场景-->
      <div class="flex-1/3 flex-grow-0 border rounded p-2 flex flex-col" :class="{' max-h-42': echartsCol === 2}">
        <div class=" font-semibold">{{ t('kanban.dataAssets.categories.scenario') }}</div>
        <div class="flex flex-1">
          <!-- <div class="px-2 text-center flex flex-col justify-center">
                        <div class="text-5 font-semibold">{{scenarioTotal}}</div>
                        <div>总数</div>
                    </div> -->
          <div ref="scenairoRef" class="h-full flex-1 min-w-0"></div>
        </div>
      </div>

      <!--计划-->
      <div class=" border rounded p-2 flex-1/3 flex-grow-0 flex flex-col" :class="{' max-h-42': echartsCol === 2}">
        <div class=" font-semibold">{{ t('kanban.dataAssets.categories.plan') }}</div>
        <div class="flex flex-1">
          <!-- <div class="px-2 text-center flex flex-col justify-center">
                        <div class="text-5 font-semibold">{{ planTotal }}</div>
                        <div>总数</div>
                    </div>  -->
          <div ref="planReviewRef" class="h-full flex-1 min-w-0"></div>
        </div>
      </div>

      <!--迭代-->
      <div
        v-if="proTypeShowMap.showSprint"
        class="border rounded p-2 flex-1/3 flex-grow-0 flex flex-col"
        :class="{' max-h-42': echartsCol === 2}">
        <div class=" font-semibold">{{ t('kanban.dataAssets.categories.sprint') }}</div>
        <div class="flex flex-1">
          <div ref="interationReviewRef" class="h-full flex-1 min-w-0"></div>
        </div>
      </div>
    </div>
  </div>
</template>
