<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch, withDefaults } from 'vue';
import { RadioButton, RadioGroup } from 'ant-design-vue';
import { useI18n } from 'vue-i18n';
import { BurnDownDataByType, EffectivenessProps } from './types';
import { useEffectivenessData } from './composables/useEffectivenessData';
import { getOverviewValueColorClass, useEffectivenessConfig } from './composables/useEffectivenessConfig';
import { useChartManagement } from './composables/useChartManagement';
import { useEffectivenessLifecycle } from './composables/useEffectivenessLifecycle';

// Component props with defaults
const props = withDefaults(defineProps<EffectivenessProps>(), {
  onShow: true
});

// Internationalization
const { t } = useI18n();

// Chart DOM references
const taskTypeChartRef = ref<HTMLElement>();
const burnDownChartRef = ref<HTMLElement>();
const targetCountChartRef = ref<HTMLElement>();
const targetRateChartRef = ref<HTMLElement>();
const workloadChartRef = ref<HTMLElement>();
const workloadRateChartRef = ref<HTMLElement>();
const overdueChartRef = ref<HTMLElement>();
const overdueRateChartRef = ref<HTMLElement>();
const oneTimePassedTestChartRef = ref<HTMLElement>();
const oneTimePassedTestRateRef = ref<HTMLElement>();
const oneTimeUnpassedTestChartRef = ref<HTMLElement>();
const oneTimeUnpassedTestRateChartRef = ref<HTMLElement>();
const assigneeRankingChartRef = ref<HTMLElement>();
const testerRankingChartRef = ref<HTMLElement>();

// Initialize composables
const {
  overviewData,
  burnDownData,
  totalTypeData,
  assigneeRanking,
  testerRanking,
  assignees,
  testers,
  loadEffectivenessData
} = useEffectivenessData(props);

const {
  initializeCharts,
  resizeAllCharts,
  updateTaskTypeChart,
  updateBurnDownChart,
  updateRankingCharts
} = useChartManagement();

const {
  isNormalSize,
  setupLifecycle,
  cleanup
} = useEffectivenessLifecycle(props, loadEffectivenessData, () => resizeAllCharts());



// Initialize configuration
const {   taskOverViewConfig,
  caseOverViewConfig, } = useEffectivenessConfig(props.countType);


const currentOverviewConfig = computed(() => {
  return props.countType === 'task' ? taskOverViewConfig : caseOverViewConfig;
});
// Flatten to a single array so that layout is controlled purely by grid columns
const flatOverviewItems = computed(() => {
  const rows = (currentOverviewConfig as any)?.value ?? currentOverviewConfig;
  if (Array.isArray(rows)) {
    return rows.flat?.() ?? ([] as any[]).concat(...rows);
  }
  return [] as any[];
});

// Local state
const burnDownOption = ref<'NUM' | 'WORKLOAD'>('NUM');

// getValueColorClass moved to useEffectivenessConfig.ts as getOverviewValueColorClass

// Event handlers
const handleBurnDownOptionChange = () => {
  nextTick(() => {
    if (burnDownData.value) {
      updateBurnDownChart(burnDownData.value as BurnDownDataByType, burnDownOption.value);
    }
  });
};

// Lifecycle hooks
onMounted(async () => {
  setupLifecycle();

  watch(() => props.projectId, async () => {
    if (props.projectId) {
      initializeCharts({
        taskTypeRef: taskTypeChartRef.value as HTMLElement,
        burnDownRef: burnDownChartRef.value as HTMLElement,
        targetCountRef: targetCountChartRef.value as HTMLElement,
        workloadRef: workloadChartRef.value as HTMLElement,
        overdueRef: overdueChartRef.value as HTMLElement,
        oneTimePassedTestRef: oneTimePassedTestChartRef.value as HTMLElement,
        assigneeRankingRef: assigneeRankingChartRef.value as HTMLElement,
        testerRankingRef: testerRankingChartRef.value as HTMLElement,
        targetRateRef: targetRateChartRef.value as HTMLElement,
        workloadRateRef: workloadRateChartRef.value as HTMLElement,
        overdueRateRef: overdueRateChartRef.value as HTMLElement,
        oneTimePassedTestRateRef: oneTimePassedTestRateRef.value as HTMLElement,
        oneTimeUnpassedTestRef: oneTimeUnpassedTestChartRef.value as HTMLElement,
        oneTimeUnpassedTestRateRef: oneTimeUnpassedTestRateChartRef.value as HTMLElement
      });
      await loadEffectivenessData();
      await nextTick();
    }
  }, { immediate: true });
});

onBeforeUnmount(() => {
  cleanup();
});

// Watch for data changes and update charts
watch([overviewData, burnDownData, totalTypeData, assigneeRanking, testerRanking], async () => {
  if (props.onShow) {
    await nextTick();

    // Update available charts with new data
    if (totalTypeData.value) {
      updateTaskTypeChart(totalTypeData.value);
    }
    if (burnDownData.value) {
      updateBurnDownChart(burnDownData.value as BurnDownDataByType, burnDownOption.value);
    }
    if (assigneeRanking.value && assignees.value) {
      updateRankingCharts(assigneeRanking.value, assignees.value, props.countType);
    }

    if (testerRanking.value && testers.value) {
      updateRankingCharts(testerRanking.value, testers.value, props.countType);
    }
  }
}, { deep: true });

// Watch for countType changes to update task type chart
watch(() => props.countType, async () => {
  if (props.onShow && totalTypeData.value) {
    await nextTick();
    updateTaskTypeChart(totalTypeData.value);
  }
});

defineExpose({
  refresh: loadEffectivenessData
});
</script>

<template>
  <div class="effectiveness-dashboard">

    <!-- Overview Section -->
    <div class="overview-section">
      <div class="overview-grid">
        <div class="overview-row">
          <div
            v-for="(item, index) in flatOverviewItems"
            :key="index"
            class="overview-item">
            <div class="overview-icon">
              <i :class="item.icon"></i>
            </div>
            <div class="overview-content">
              <div class="overview-value" :class="getOverviewValueColorClass(item, overviewData[item.dataIndex])">
                {{ overviewData[item.dataIndex] || 0 }}
                <span v-if="item.unit" class="overview-unit">{{ item.unit }}</span>
              </div>
              <div class="overview-label">{{ item.name }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Charts Section -->
    <div class="charts-section">
      <!-- Burn Down Chart -->
      <div class="chart-container">
        <div class="chart-header">
          <h3>{{ $t('kanban.effectiveness.burnDownChart') }}</h3>
          <div class="chart-controls">
            <RadioGroup
              v-model:value="burnDownOption"
              size="small"
              @change="handleBurnDownOptionChange">
              <RadioButton value="NUM">{{ props.countType === 'task' ? t('kanban.effectiveness.taskNumber') : t('kanban.effectiveness.testNumber') }}</RadioButton>
              <RadioButton value="WORKLOAD">{{ t('kanban.effectiveness.workload') }}</RadioButton>
            </RadioGroup>
          </div>
        </div>
        <div ref="burnDownChartRef" class="chart-content"></div>
      </div>

      <!-- Task Type Chart -->
      <div v-show="props.countType==='task'" class="chart-container">
        <div class="chart-header">
          <h3>{{ $t('kanban.effectiveness.taskTypeTitle') }}</h3>
        </div>
        <div ref="taskTypeChartRef" class="chart-content"></div>
      </div>

      <!-- Target Count Chart -->
      <div class="chart-container">
        <div class="chart-header">
          <h3>{{ props.countType === 'task' ? t('kanban.effectiveness.taskCount') : t('kanban.effectiveness.useCaseCount') }}</h3>
        </div>
        <div ref="targetCountChartRef" class="chart-content"></div>
      </div>

      <!-- Workload Chart -->
      <div class="chart-container">
        <div class="chart-header">
          <h3>{{ $t('kanban.effectiveness.workload') }}</h3>
        </div>
        <div ref="workloadChartRef" class="chart-content"></div>
      </div>

      <!-- Overdue Chart -->
      <div class="chart-container">
        <div class="chart-header">
          <h3>{{ $t('kanban.effectiveness.overdue') }}</h3>
        </div>
        <div ref="overdueChartRef" class="chart-content"></div>
      </div>

      <!-- One Time Passed Test Chart -->
      <div class="chart-container">
        <div class="chart-header">
          <h3>{{ props.countType === 'task' ? $t('kanban.effectiveness.oneTimePassedTest') : t('kanban.effectiveness.oneTimeTestPassedCount') }}</h3>
        </div>
        <div ref="oneTimePassedTestChartRef" class="chart-content"></div>
      </div>

      <!--targetRateChartRef-->
      <div class="chart-container">
        <div class="chart-header">
          <h3>{{ t('kanban.effectiveness.completionRate') }}</h3>
        </div>
        <div ref="targetRateChartRef" class="chart-content"></div>
      </div>

      <!-- workloadRateChart -->
      <div class="chart-container">
        <div class="chart-header">
          <h3>{{ t('kanban.effectiveness.workloadSaved') }}</h3>
        </div>
        <div ref="workloadRateChartRef" class="chart-content"></div>
      </div>

      <!--overdueRateChart-->
      <div class="chart-container">
        <div class="chart-header">
          <h3>{{ t('kanban.effectiveness.overdueRate') }}</h3>
        </div>
        <div ref="overdueRateChartRef" class="chart-content"></div>
      </div>

      <!--oneTimePassedTestRateRef-->
      <div class="chart-container">
        <div class="chart-header">
          <h3>{{ props.countType === 'task' ? t('kanban.effectiveness.oneTimePassRate') : t('kanban.effectiveness.oneTimeTestPassRate') }}</h3>
        </div>
        <div ref="oneTimePassedTestRateRef" class="chart-content"></div>
      </div>

      <!--oneTimeUnpassedTestChartRef-->
      <div class="chart-container">
        <div class="chart-header">
          <h3>{{ props.countType === 'task' ? t('kanban.effectiveness.validBugCount') : t('kanban.effectiveness.oneTimeReviewPassedCount') }}</h3>
        </div>
        <div ref="oneTimeUnpassedTestChartRef" class="chart-content"></div>
      </div>

      <!-- oneTimeUnpassedTestRateChartRef-->
      <div class="chart-container">
        <div class="chart-header">
          <h3>{{ props.countType === 'task' ? t('kanban.effectiveness.validBugRate') : t('kanban.effectiveness.oneTimeReviewPassRate') }}</h3>
        </div>
        <div ref="oneTimeUnpassedTestRateChartRef" class="chart-content"></div>
      </div>

      <!-- Ranking Charts -->
      <!-- <div class="ranking-charts">
        <div class="chart-container">
          <div class="chart-header">
            <h3>{{ $t('kanban.effectiveness.assigneeRanking') }}</h3>
          </div>
          <div ref="assigneeRankingChartRef" class="chart-content"></div>
        </div>

        <div class="chart-container">
          <div class="chart-header">
            <h3>{{ $t('kanban.effectiveness.testerRanking') }}</h3>
          </div>
          <div ref="testerRankingChartRef" class="chart-content"></div>
        </div>
      </div> -->
    </div>
  </div>
</template>

<style scoped>
.effectiveness-dashboard {
  margin-top: 10px;
  padding: 2px;
}

.overview-section {
  margin-bottom: 30px;
}

.overview-grid {
  display: block;
}

.overview-row {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 15px;
}

.overview-item {
  display: flex;
  align-items: center;
  padding: 15px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.overview-icon {
  margin-right: 15px;
  font-size: 24px;
  color: #1890ff;
}

.overview-content {
  flex: 1;
}

.overview-value {
  font-size: 24px;
  font-weight: bold;
  color: #262626;
  margin-bottom: 5px;
}

/* Semantic value colors */
.value-neutral { color: #262626; }
.value-primary { color: #1890ff; }
.value-good { color: #16a34a; }
.value-warning { color: #f59e0b; }
.value-bad { color: #dc2626; }

.overview-unit {
  font-size: 14px;
  color: #8c8c8c;
  margin-left: 5px;
}

.overview-label {
  font-size: 14px;
  color: #8c8c8c;
}

.charts-section {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 20px;
}

.chart-container {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  border-bottom: 1px solid #f0f0f0;
}

.chart-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #262626;
}

.chart-controls {
  display: flex;
  align-items: center;
}

.chart-content {
  height: 300px;
  padding: 20px;
}

.ranking-charts {
  grid-column: 1 / -1;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 20px;
}

@media (max-width: 1440px) {
  .charts-section {
    grid-template-columns: 1fr;
  }

  .ranking-charts {
    grid-template-columns: 1fr;
  }
}
</style>
