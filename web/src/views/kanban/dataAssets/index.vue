<script setup lang="ts">
import { computed, inject, nextTick, onBeforeUnmount, onMounted, reactive, ref, Ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Icon, Image, NoData } from '@xcan-angus/vue-ui';
import { Popover } from 'ant-design-vue';
import elementResizeDetector from 'element-resize-detector';
import { throttle } from 'throttle-debounce';
import { kanban } from '@/api/tester';
import { EnumMessage, enumUtils } from '@xcan-angus/infra';
import { ReportCategory } from '@/enums/enums';
import SelectEnum from '@/components/form/enum/SelectEnum.vue';
import { DataAssetsProps } from './types';
import { useDataAssetsData } from './composables/useDataAssetsData';
import { useChartConfigs } from './composables/useChartConfigs';
import { useChartManagement } from './composables/useChartManagement';

// Initialize internationalization
const { t } = useI18n();

// Project type visibility configuration injected from parent component
const proTypeShowMap = inject<Ref<{[key: string]: boolean}>>('proTypeShowMap', ref({ showTask: true, showSprint: true }));

// Component props with default values
const props = withDefaults(defineProps<DataAssetsProps>(), {
  projectId: undefined,
  creatorObjectType: undefined,
  creatorObjectId: undefined,
  createdDateStart: undefined,
  createdDateEnd: undefined,
  onShow: false
});

// Computed params excluding onShow prop for API calls
const params = computed(() => {
  const { onShow, ...other } = props;
  return {
    ...other
  };
});

// Initialize data management composable for handling data assets
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

// Initialize chart configuration composable
const {
  chartSeriesColorConfig,
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

// Create chart configurations for different data types
const increaseEchartConfig = createGrowthTrendConfig();
const caseBarEchartsConfig = createCaseBarConfig();
const casePieEchartsConfig = createCasePieConfig();
const apiBarEchartsConfig = createApiBarConfig();
const apiPieEchartsConfig = createApiPieConfig();
const taskBarEchartsConfig = createTaskBarConfig();
const taskPieEchartsConfig = createTaskPieConfig();
const planPieEchartsConfig = createPlanPieConfig();
const interationPieEchartsConfig = createSprintPieConfig();
const scenairoPieChartsConfig = createScenarioPieConfig();
const scriptBarChartsConfig = createScriptBarConfig();

// Initialize chart management composable for handling chart operations
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
  // DOM references for chart containers
  growthTrendRef,
  caseStatusRef,
  caseReviewRef,
  apiStatusRef,
  apiReviewRef,
  taskStatusRef,
  taskReviewRef,
  planReviewRef,
  sprintReviewRef,
  scenarioRef,
  scriptRef,
  initializeCharts,
  updateGrowthTrendChart,
  updateCaseCharts,
  updateApiCharts,
  updateTaskCharts,
  updateScenarioChart,
  updateScriptChart,
  resizeAllCharts,
  handleRightSideResize
} = useChartManagement() as any;

// Element resize detector for responsive chart resizing
const erd = elementResizeDetector({ strategy: 'scroll' });

// Target data type for growth trend chart
const targetType = ref('TASK');
// Report category options for dropdown selection
const reportCategoryOpt = ref<EnumMessage<ReportCategory>[]>([]);

/**
 * Load enum options for report categories
 */
const loadEnums = () => {
  reportCategoryOpt.value = enumUtils.enumToMessages(ReportCategory);
};

// Flag indicating if growth trend chart should show empty state
const increasEmpty = ref();

/**
 * Load test case data and update charts
 */
const loadCase = async () => {
  const [error, { data = {} }] = await kanban.getTesting({ ...params.value });
  if (error) {
    return;
  }
  caseTotal.value = data.allCase || 0;
  planTotal.value = data.allPlan || 0;
  updateCaseCharts(data);
};

// Total count of test cases
const caseTotal = ref(0);

/**
 * Load API data and update charts
 */
const loadApis = async () => {
  const [error, { data = {} }] = await kanban.getApis({ ...params.value });
  if (error) {
    return;
  }
  apiTotal.value = data.allApis || 0;
  updateApiCharts(data);
};

// Total count of APIs
const apiTotal = ref(0);

/**
 * Load task and sprint data and update charts
 */
const loadTask = async () => {
  if (!proTypeShowMap.value.showTask) {
    return;
  }
  const [error, { data = {} }] = await kanban.getTask({ ...params.value });
  if (error) {
    return;
  }
  taskTotal.value = data.allTask || 0;
  updateTaskCharts(data);
  if (!proTypeShowMap.value.showSprint) return;
  sprintTotal.value = data.allSprint || 0;
};

// Total count of tasks
const taskTotal = ref(0);

// Total count of plans
const planTotal = ref(0);

// Total count of sprints
const sprintTotal = ref(0);

/**
 * Load scenario data and update charts
 */
const loadScenario = async () => {
  const [error, { data = {} }] = await kanban.getScenario({ ...params.value });
  if (error) {
    return;
  }
  scenarioTotal.value = data.allSce || 0;
  updateScenarioChart(data);
};

// Total count of scenarios
const scenarioTotal = ref(0);

/**
 * Load script data and update charts
 */
const loadScript = async () => {
  const [error, { data = {} }] = await kanban.getScript({ ...params.value });
  if (error) {
    return;
  }
  scriptTotal.value = data.allScript || 0;
  updateScriptChart(data);
};

// Total count of scripts
const scriptTotal = ref(0);

/**
 * Load mock data including APIs, services, responses, and pushbacks
 */
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

/**
 * Load data assets including datasets, datasources, files, and variables
 */
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

// Reactive data object for data assets statistics
const dataData = reactive({
  allDataset: 0,
  allDatasource: 0,
  allFile: 0,
  allVariable: 0
});

// Responsive layout: 3 columns when screen width >= 1560px, 2 columns otherwise
const echartsCol = ref(2);
if (window.document.body.clientWidth >= 1560) {
  echartsCol.value = 3;
}

// Right panel width percentage
const rightPercent = ref('calc(25% - 24px)');

// Flag to notify when resize is needed but component is not visible
const resizeNotify = ref(false);

/**
 * Handle window resize events with throttling
 * Updates column layout and resizes charts accordingly
 */
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

/**
 * Resize all charts when window size changes
 */
const resizeEcharts = () => {
  resizeAllCharts();
};

// Reference to right panel wrapper for resize detection
const rightWrapRef = ref();

/**
 * Handle right panel width changes and resize charts accordingly
 */
const resizeRightEchart = () => {
  if (!rightWrapRef.value) return;
  handleRightSideResize(rightWrapRef.value.clientWidth);
};

// Flag to indicate if data should be refreshed when component becomes visible
const shouldNotify = ref(false);

/**
 * Update growth trend chart UI based on data availability
 * Shows empty state when no data is available
 */
const updateGrowthChartUI = () => {
  const data = growthTrendData.value;
  const hasData = Object.keys(data).length > 0;
  increasEmpty.value = !hasData;
  updateGrowthTrendChart(
    data,
    targetType.value,
    chartSeriesColorConfig,
    targetDataCategory,
    props.createdDateStart,
    props.createdDateEnd
  );
};

const refreshData = async () => {
  await loadGrowthTrendData(targetType.value);
  loadRankData();
  loadCase();
  loadApis();
  loadTask();
  loadScenario();
  loadScript();
  loadMock();
  loadData();
};

/**
 * Component mounted lifecycle hook
 * Initializes all data loading, chart configurations, and event listeners
 */
onMounted(async () => {
  // Load enum options for dropdowns
  await loadEnums();

  // Watch for task visibility changes and update target type accordingly
  watch(() => proTypeShowMap.value.showTask, () => {
    if (!proTypeShowMap.value.showTask && targetType.value === 'TASK') {
      targetType.value = 'FUNC';
    }
  }, {
    immediate: true
  });

  // Watch for target type changes and reload growth trend data
  watch(() => targetType.value, async () => {
    if (props.projectId) {
      loadGrowthTrendData(targetType.value);
    }
  });

  // Watch for project type changes and reinitialize charts
  watch(() => proTypeShowMap.value.ShowTask, () => {
    nextTick(() => {
      initializeCharts();
    });
  }, {
    deep: true
  });

  // Watch for prop changes and reload all data
  watch([
    () => props.createdDateEnd,
    () => props.createdDateStart,
    () => props.creatorObjectId,
    () => props.creatorObjectType,
    () => props.projectId
  ], async () => {
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

  // Watch for component visibility changes and handle refresh/resize
  watch(() => props.onShow, newValue => {
    if (newValue && shouldNotify.value && props.projectId) {
      refreshData();
      shouldNotify.value = false;
    }
    if (newValue && resizeNotify.value && props.projectId) {
      handleWindowResize();
      resizeNotify.value = false;
    }
  });

  // Inject chart configurations into chart manager
  growthTrendConfig.value = increaseEchartConfig;
  caseBarConfig.value = caseBarEchartsConfig;
  casePieConfig.value = casePieEchartsConfig;
  apiBarConfig.value = apiBarEchartsConfig;
  apiPieConfig.value = apiPieEchartsConfig;
  taskBarConfig.value = taskBarEchartsConfig;
  taskPieConfig.value = taskPieEchartsConfig;
  planPieConfig.value = planPieEchartsConfig;
  sprintPieConfig.value = interationPieEchartsConfig;
  scenarioPieConfig.value = scenairoPieChartsConfig;
  scriptBarConfig.value = scriptBarChartsConfig;

  // Initialize all charts
  initializeCharts();

  // Watch for growth trend data changes and update UI
  watch(() => growthTrendData.value, () => {
    updateGrowthChartUI();
  });

  // Add event listeners for window resize and element resize detection
  window.addEventListener('resize', handleWindowResize);
  erd.listenTo(rightWrapRef.value, resizeRightEchart);
});

/**
 * Component before unmount lifecycle hook
 * Clean up event listeners and resize detectors
 */
onBeforeUnmount(() => {
  window.removeEventListener('resize', handleWindowResize);
  erd.removeListener(rightWrapRef.value, resizeRightEchart);
});

// Expose methods for parent component access
defineExpose({
  refresh: refreshData,
  handleWindowResize
});
</script>
<template>
  <div class="flex space-x-2 mt-2 text-3">
    <div class="py-2 border rounded flex-1 min-w-0">
      <div class="px-2 text-3.5 font-semibold">
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
        ref="growthTrendRef"
        class="h-60"></div>
    </div>
    <div
      style="min-width: 320px;"
      :style="{ width: rightPercent }"
      class="border rounded p-2">
      <div class="flex space-x-1 items-center">
        <span class="text-3.5 font-semibold">{{ t('kanban.dataAssets.contributionRanking') }}</span>
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
            <Icon :icon="rankIconConfig[idx]" class="text-4.5" />
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
          :text="t('common.noData')" />
      </div>
    </div>
  </div>
  <div class="flex mt-2 text-3">
    <div v-if="proTypeShowMap.showTask" class="flex-1 min-w-0 mr-2">
      <div class="flex space-x-2">
        <!-- Task Section -->
        <div class="border rounded p-2 min-w-0" :class="`flex-1/${echartsCol}`">
          <div class="text-3.5 font-semibold">{{ t('common.issue') }}</div>
          <div class="flex">
            <div ref="taskReviewRef" class="h-35 flex-1 min-w-0"></div>
          </div>
          <div ref="taskStatusRef" class="h-40"></div>
        </div>

        <!-- Test Cases Section -->
        <div
          class=" border rounded p-2 min-w-0"
          :class="`flex-1/${echartsCol}`">
          <div class="text-3.5 font-semibold">{{ t('common.useCase') }}</div>
          <div class="flex">
            <div ref="caseReviewRef" class="h-35 flex-1 min-w-0"></div>
          </div>
          <div ref="caseStatusRef" class="h-40"></div>
        </div>

        <!-- APIs Section -->
        <div
          v-if="echartsCol === 3"
          class=" border rounded p-2 min-w-0"
          :class="`flex-1/${echartsCol}`">
          <div class="text-3.5 font-semibold">{{ t('common.api') }}</div>
          <div class="flex">
            <div ref="apiReviewRef" class="h-35 flex-1 min-w-0"></div>
          </div>
          <div ref="apiStatusRef" class="h-40"></div>
        </div>
      </div>
      <div v-if="echartsCol === 2" class="flex space-x-2 mt-2">
        <!-- APIs Section -->
        <div class=" border rounded p-2 min-w-0" :class="`flex-1/${echartsCol}`">
          <div class="text-3.5 font-semibold">{{ t('common.api') }}</div>
          <div class="flex">
            <div ref="apiReviewRef" class="h-35 flex-1 min-w-0"></div>
          </div>
          <div ref="apiStatusRef" class="h-40"></div>
        </div>
        <!-- Mock & Data Section -->
        <div class="flex flex-col space-y-2 min-h-0" :class="`flex-1/${echartsCol}`">
          <div class="flex-1 rounded border p-2 flex flex-col">
            <div class="px-2 font-semibold">Mock</div>
            <div class=" flex-1 flex justify-around items-center space-x-2">
              <div class="flex-1 text-center h-full bg-board-blue flex flex-col justify-center rounded relative min-w-0">
                <img src="./image/service.png" class="w-1/2 absolute top-0 right-0" />
                <div :title="String(mockData.allService)" class="text-5 truncate">{{ mockData.allService }}</div>
                <div>{{ t('common.service') }}</div>
              </div>
              <div class="flex-1 text-center h-full bg-board-orange flex flex-col justify-center rounded relative min-w-0">
                <img src="./image/apis.png" class="w-1/2 absolute top-0 right-0" />
                <div :title="String(mockData.allApi)" class="text-5 truncate">{{ mockData.allApi }}</div>
                <div>{{ t('common.api') }}</div>
              </div>
              <div class="flex-1 text-center h-full bg-board-qing flex flex-col justify-center rounded relative min-w-0">
                <img src="./image/response.png" class="w-1/2 absolute top-0 right-0" />
                <div :title="String(mockData.allResponse)" class="text-5 truncate">{{ mockData.allResponse }}</div>
                <div>{{ t('protocol.response') }}</div>
              </div>
              <div class="flex-1 text-center h-full bg-board-yellow flex flex-col justify-center rounded relative min-w-0">
                <img src="./image/pushback.png" class="w-1/2 absolute top-0 right-0" />
                <div :title="String(mockData.allPushback)" class="text-5 truncate">{{ mockData.allPushback }}</div>
                <div>{{ t('kanban.dataAssets.categories.pushback') }}</div>
              </div>
            </div>
          </div>
          <div class="flex-1 rounded border p-2 flex flex-col">
            <div class="px-2 font-semibold">{{ t('common.data') }}</div>
            <div class=" flex-1 flex justify-around items-center space-x-2">
              <div class="flex-1 text-center h-full bg-board-blue flex flex-col justify-center rounded relative min-w-0">
                <img src="./image/variable.png" class="w-1/2 absolute top-0 right-0" />
                <div :title="String(dataData.allVariable)" class="text-5 truncate">{{ dataData.allVariable }}</div>
                <div>{{ t('common.variables') }}</div>
              </div>
              <div class="flex-1 text-center h-full bg-board-orange flex flex-col justify-center rounded relative min-w-0">
                <img src="./image/dataset.png" class="w-1/2 absolute top-0 right-0" />
                <div :title="String(dataData.allDataset)" class="text-5 truncate">{{ dataData.allDataset }}</div>
                <div>{{ t('common.dataset') }}</div>
              </div>
              <div class="flex-1 text-center h-full bg-board-qing flex flex-col justify-center rounded relative min-w-0">
                <img src="./image/file.png" class="w-1/2 absolute top-0 right-0" />
                <div :title="String(dataData.allFile)" class="text-5 truncate">{{ dataData.allFile }}</div>
                <div>{{ t('common.file') }}</div>
              </div>
              <div class="flex-1 text-center h-full bg-board-yellow flex flex-col justify-center rounded relative min-w-0">
                <img src="image/datasource.png" class="w-1/2 absolute top-0 right-0" />
                <div :title="String(dataData.allDatasource)" class="text-5 truncate">{{ dataData.allDatasource }}</div>
                <div>{{ t('common.datasource') }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div class="flex space-x-2 mt-2">
        <!-- Mock & Data Section -->
        <div
          v-if="echartsCol === 3"
          class="flex space-x-2 min-w-0"
          :class="`flex-1/${echartsCol}`">
          <div class="flex-1 rounded border p-2 flex flex-col">
            <div class="px-2 text-3.5 font-semibold">Mock</div>
            <div class=" flex-1 flex justify-around flex-col space-y-2">
              <div class="flex flex-1  space-x-2">
                <div class="flex-1 text-center h-full bg-board-blue flex flex-col justify-center rounded relative min-w-0">
                  <img src="./image/service.png" class="w-1/2 absolute top-0 right-0" />
                  <div :title="String(mockData.allService)" class="text-5 truncate">{{ mockData.allService }}</div>
                  <div>{{ t('common.service') }}</div>
                </div>
                <div class="flex-1 text-center h-full bg-board-orange flex flex-col justify-center rounded relative min-w-0">
                  <img src="./image/apis.png" class="w-1/2 absolute top-0 right-0" />
                  <div :title="String(mockData.allApi)" class="text-5 truncate">{{ mockData.allApi }}</div>
                  <div>{{ t('common.api') }}</div>
                </div>
              </div>
              <div class="flex flex-1  space-x-2">
                <div class="flex-1 text-center h-full bg-board-qing flex flex-col justify-center rounded relative min-w-0">
                  <img src="./image/response.png" class="w-1/2 absolute top-0 right-0" />
                  <div :title="String(mockData.allResponse)" class="text-5 truncate">{{ mockData.allResponse }}</div>
                  <div>{{ t('protocol.response') }}</div>
                </div>
                <div class="flex-1 text-center h-full bg-board-yellow flex flex-col justify-center rounded relative min-w-0">
                  <img src="./image/pushback.png" class="w-1/2 absolute top-0 right-0" />
                  <div :title="String(mockData.allPushback)" class="text-5 truncate">{{ mockData.allPushback }}</div>
                  <div>{{ t('kanban.dataAssets.categories.pushback') }}</div>
                </div>
              </div>
            </div>
          </div>
          <div class="flex-1 rounded border p-2 flex flex-col">
            <div class="px-2 text-3.5 font-semibold">{{ t('common.data') }}</div>
            <div class=" flex-1 flex flex-col justify-around space-y-2">
              <div class="flex flex-1  space-x-2">
                <div class="flex-1 text-center h-full bg-board-blue flex flex-col justify-center rounded relative min-w-0">
                  <img src="./image/variable.png" class="w-1/2 absolute top-0 right-0" />
                  <div :title="String(dataData.allVariable)" class="text-5 truncate">{{ dataData.allVariable }}</div>
                  <div>{{ t('common.variables') }}</div>
                </div>
                <div class="flex-1 text-center h-full bg-board-orange flex flex-col justify-center rounded relative min-w-0">
                  <img src="./image/dataset.png" class="w-1/2 absolute top-0 right-0" />
                  <div :title="String(dataData.allDataset)" class="text-5 truncate">{{ dataData.allDataset }}</div>
                  <div>{{ t('common.dataset') }}</div>
                </div>
              </div>
              <div class="flex flex-1 space-x-2">
                <div class="flex-1 text-center h-full bg-board-qing flex flex-col justify-center rounded relative min-w-0">
                  <img src="./image/file.png" class="w-1/2 absolute top-0 right-0" />
                  <div :title="String(dataData.allFile)" class="text-5 truncate">{{ dataData.allFile }}</div>
                  <div>{{ t('common.file') }}</div>
                </div>
                <div class="flex-1 text-center h-full bg-board-yellow flex flex-col justify-center rounded relative min-w-0">
                  <img src="image/datasource.png" class="w-1/2 absolute top-0 right-0" />
                  <div :title="String(dataData.allDatasource)" class="text-5 truncate">{{ dataData.allDatasource }}</div>
                  <div>{{ t('common.datasource') }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Script Section -->
        <div class="border rounded py-2 min-w-0" :class="`flex-${echartsCol === 2 ? 1 : '2/3'}`">
          <div class="px-2 text-3.5 font-semibold">{{ t('common.script') }}</div>
          <div class="flex px-2">
            <div class="px-2 text-center flex flex-col justify-center">
              <div class="text-3.5 font-semibold">{{ scriptTotal }}</div>
              <div>{{ t('chart.total') }}</div>
            </div>
            <div ref="scriptRef" class="h-35 flex-1 min-w-0"></div>
          </div>
        </div>
      </div>
    </div>
    <div v-if="!proTypeShowMap.showTask" class="flex-1 min-w-0">
      <div class="flex flex-wrap">
        <!-- Test Cases Section -->
        <div
          class="min-w-0 flex-grow-0 pr-2"
          :class="`flex-1/${echartsCol}`">
          <div class="border rounded p-2 pb-2">
            <div class="text-3.5 font-semibold">{{ t('common.useCase') }}</div>
            <div class="flex">
              <div ref="caseReviewRef" class="h-35 flex-1 min-w-0"></div>
            </div>
            <div ref="caseStatusRef" class="h-40"></div>
          </div>
        </div>

        <!-- APIs Section -->
        <div class="min-w-0 flex-grow-0 pr-2 pb-2" :class="`flex-1/${echartsCol}`">
          <div class="border rounded p-2">
            <div class="text-3.5 font-semibold">{{ t('common.api') }}</div>
            <div class="flex">
              <div ref="apiReviewRef" class="h-35 flex-1 min-w-0"></div>
            </div>
            <div ref="apiStatusRef" class="h-40"></div>
          </div>
        </div>

        <!-- Mock & Data Section -->
        <div :class="`flex-1/${echartsCol}`" class="min-h-0 flex-grow-0 pr-2 pb-2">
          <div v-if="echartsCol === 3" class="flex flex-col space-y-2 h-full">
            <div class="flex-1 rounded border p-2 flex flex-col">
              <div class="px-2 text-3.5 font-semibold">Mock</div>
              <div class=" flex-1 flex justify-around items-center space-x-2">
                <div class="flex-1 text-center h-full bg-board-blue flex flex-col justify-center rounded relative min-w-0">
                  <img src="./image/service.png" class="w-1/2 absolute top-0 right-0" />
                  <div :title="String(mockData.allService)" class="text-5 truncate">{{ mockData.allService }}</div>
                  <div>{{ t('common.service') }}</div>
                </div>
                <div class="flex-1 text-center h-full bg-board-orange flex flex-col justify-center rounded relative min-w-0">
                  <img src="./image/apis.png" class="w-1/2 absolute top-0 right-0" />
                  <div :title="String(mockData.allApi)" class="text-5 truncate">{{ mockData.allApi }}</div>
                  <div>{{ t('common.api') }}</div>
                </div>
                <div class="flex-1 text-center h-full bg-board-qing flex flex-col justify-center rounded relative min-w-0">
                  <img src="./image/response.png" class="w-1/2 absolute top-0 right-0" />
                  <div :title="String(mockData.allResponse)" class="text-5 truncate">{{ mockData.allResponse }}</div>
                  <div>{{ t('protocol.response') }}</div>
                </div>
                <div class="flex-1 text-center h-full bg-board-yellow flex flex-col justify-center rounded relative min-w-0">
                  <img src="./image/pushback.png" class="w-1/2 absolute top-0 right-0" />
                  <div :title="String(mockData.allPushback)" class="text-5 truncate">{{ mockData.allPushback }}</div>
                  <div>{{ t('kanban.dataAssets.categories.pushback') }}</div>
                </div>
              </div>
            </div>
            <div class="flex-1 rounded border p-2 flex flex-col">
              <div class="px-2 text-3.5 font-semibold">{{ t('common.data') }}</div>
              <div class=" flex-1 flex justify-around items-center space-x-2">
                <div class="flex-1 text-center h-full bg-board-blue flex flex-col justify-center rounded relative min-w-0">
                  <img src="./image/variable.png" class="w-1/2 absolute top-0 right-0" />
                  <div :title="String(dataData.allVariable)" class="text-5 truncate">{{ dataData.allVariable }}</div>
                  <div>{{ t('common.variables') }}</div>
                </div>
                <div class="flex-1 text-center h-full bg-board-orange flex flex-col justify-center rounded relative min-w-0">
                  <img src="./image/dataset.png" class="w-1/2 absolute top-0 right-0" />
                  <div :title="String(dataData.allDataset)" class="text-5 truncate">{{ dataData.allDataset }}</div>
                  <div>{{ t('common.dataset') }}</div>
                </div>
                <div class="flex-1 text-center h-full bg-board-qing flex flex-col justify-center rounded relative min-w-0">
                  <img src="./image/file.png" class="w-1/2 absolute top-0 right-0" />
                  <div :title="String(dataData.allFile)" class="text-5 truncate">{{ dataData.allFile }}</div>
                  <div>{{ t('common.file') }}</div>
                </div>
                <div class="flex-1 text-center h-full bg-board-yellow flex flex-col justify-center rounded relative min-w-0">
                  <img src="image/datasource.png" class="w-1/2 absolute top-0 right-0" />
                  <div :title="String(dataData.allDatasource)" class="text-5 truncate">{{ dataData.allDatasource }}</div>
                  <div>{{ t('common.datasource') }}</div>
                </div>
              </div>
            </div>
          </div>
          <div
            v-if="echartsCol === 2"
            class="flex space-x-2 min-w-0 h-full"
            :class="`flex-1/${echartsCol}`">
            <div class="flex-1 rounded border p-2 flex flex-col">
              <div class="px-2 text-3.5 font-semibold">Mock</div>
              <div class=" flex-1 flex justify-around flex-col space-y-2">
                <div class="flex flex-1  space-x-2">
                  <div class="flex-1 text-center h-full bg-board-blue flex flex-col justify-center rounded relative min-w-0">
                    <img src="./image/service.png" class="w-1/2 absolute top-0 right-0" />
                    <div :title="String(mockData.allService)" class="text-5 truncate">{{ mockData.allService }}</div>
                    <div>{{ t('common.service') }}</div>
                  </div>
                  <div class="flex-1 text-center h-full bg-board-orange flex flex-col justify-center rounded relative min-w-0">
                    <img src="./image/apis.png" class="w-1/2 absolute top-0 right-0" />
                    <div :title="String(mockData.allApi)" class="text-5 truncate">{{ mockData.allApi }}</div>
                    <div>{{ t('common.api') }}</div>
                  </div>
                </div>
                <div class="flex flex-1  space-x-2">
                  <div class="flex-1 text-center h-full bg-board-qing flex flex-col justify-center rounded relative min-w-0">
                    <img src="./image/response.png" class="w-1/2 absolute top-0 right-0" />
                    <div :title="String(mockData.allResponse)" class="text-5 truncate">{{ mockData.allResponse }}</div>
                    <div>{{ t('protocol.response') }}</div>
                  </div>
                  <div class="flex-1 text-center h-full bg-board-yellow flex flex-col justify-center rounded relative min-w-0">
                    <img src="./image/pushback.png" class="w-1/2 absolute top-0 right-0" />
                    <div :title="String(mockData.allPushback)" class="text-5 truncate">{{ mockData.allPushback }}</div>
                    <div>{{ t('kanban.dataAssets.categories.pushback') }}</div>
                  </div>
                </div>
              </div>
            </div>
            <div class="flex-1 rounded border p-2 flex flex-col">
              <div class="px-2 text-3.5 font-semibold">{{ t('common.data') }}</div>
              <div class=" flex-1 flex flex-col justify-around space-y-2">
                <div class="flex flex-1  space-x-2">
                  <div class="flex-1 text-center h-full bg-board-blue flex flex-col justify-center rounded relative min-w-0">
                    <img src="./image/variable.png" class="w-1/2 absolute top-0 right-0" />
                    <div :title="String(dataData.allVariable)" class="text-5 truncate">{{ dataData.allVariable }}</div>
                    <div>{{ t('common.variables') }}</div>
                  </div>
                  <div class="flex-1 text-center h-full bg-board-orange flex flex-col justify-center rounded relative min-w-0">
                    <img src="./image/dataset.png" class="w-1/2 absolute top-0 right-0" />
                    <div :title="String(dataData.allDataset)" class="text-5 truncate">{{ dataData.allDataset }}</div>
                    <div>{{ t('common.dataset') }}</div>
                  </div>
                </div>
                <div class="flex flex-1 space-x-2">
                  <div class="flex-1 text-center h-full bg-board-qing flex flex-col justify-center rounded relative min-w-0">
                    <img src="./image/file.png" class="w-1/2 absolute top-0 right-0" />
                    <div :title="String(dataData.allFile)" class="text-5 truncate">{{ dataData.allFile }}</div>
                    <div>{{ t('common.file') }}</div>
                  </div>
                  <div class="flex-1 text-center h-full bg-board-yellow flex flex-col justify-center rounded relative min-w-0">
                    <img src="image/datasource.png" class="w-1/2 absolute top-0 right-0" />
                    <div :title="String(dataData.allDatasource)" class="text-5 truncate">{{ dataData.allDatasource }}</div>
                    <div>{{ t('common.datasource') }}</div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Script Section -->
        <div class="min-w-0 flex-grow-0 pr-2 pb-2" :class="`flex-${echartsCol === 2 ? 1 : '2/3'}`">
          <div class="border rounded py-2">
            <div class="px-2 text-3.5 font-semibold">{{ t('common.script') }}</div>
            <div class="flex px-2">
              <div class="px-2 text-center flex flex-col justify-center">
                <div class="text-3.5 font-semibold">{{ scriptTotal }}</div>
                <div>{{ t('chart.total') }}</div>
              </div>
              <div ref="scriptRef" class="h-35 flex-1 min-w-0"></div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div
      ref="rightWrapRef"
      style="min-width: 320px;"
      :style="{ width: rightPercent }"
      class="flex flex-col space-y-2">
      <!-- Scenario Section -->
      <div class="flex-1/3 flex-grow-0 border rounded p-2 flex flex-col" :class="{' max-h-42': echartsCol === 2}">
        <div class="text-3.5 font-semibold">{{ t('common.scenario') }}</div>
        <div class="flex flex-1">
          <div ref="scenarioRef" class="h-full flex-1 min-w-0"></div>
        </div>
      </div>

      <!-- Plan Section -->
      <div class=" border rounded p-2 flex-1/3 flex-grow-0 flex flex-col" :class="{' max-h-42': echartsCol === 2}">
        <div class="text-3.5 font-semibold">{{ t('common.plan') }}</div>
        <div class="flex flex-1">
          <div ref="planReviewRef" class="h-full flex-1 min-w-0"></div>
        </div>
      </div>

      <!-- Sprint Section -->
      <div
        v-if="proTypeShowMap.showSprint"
        class="border rounded p-2 flex-1/3 flex-grow-0 flex flex-col"
        :class="{' max-h-42': echartsCol === 2}">
        <div class="text-3.5 font-semibold">{{ t('common.sprint') }}</div>
        <div class="flex flex-1">
          <div ref="sprintReviewRef" class="h-full flex-1 min-w-0"></div>
        </div>
      </div>
    </div>
  </div>
</template>
