<script lang="ts" setup>
import { ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { Progress, Card, Tag, Statistic } from 'ant-design-vue';
import { Icon } from '@xcan-angus/vue-ui';

const { t } = useI18n();

/**
 * Component props with default values
 */
const props = withDefaults(defineProps<{
  projectId?: string;
  onShow?: boolean;
}>(), {
  projectId: undefined,
  onShow: false
});

/**
 * Mock evaluation data - 使用 JSON 配置数据
 */
const evaluationData = ref({
  // 综合评分
  overallScore: 85.5,
  scoreLevel: '良好',
  
  // 需求完成率
  requirementCompletion: {
    rate: 90,
    completed: 45,
    total: 50
  },
  
  // 功能测试覆盖率
  functionTestCoverage: {
    rate: 88,
    covered: 220,
    total: 250
  },
  
  // 功能测试通过率
  functionTestPassRate: {
    rate: 92,
    passed: 202,
    total: 220
  },
  
  // 性能测试通过率
  performanceTestPassRate: {
    rate: 85,
    passed: 85,
    total: 100
  },
  
  // 稳定性测试通过率
  stabilityTestPassRate: {
    rate: 80,
    passed: 80,
    total: 100
  },
  
  // 兼容性评分
  compatibilityScore: 88,
  
  // 易用性评分
  usabilityScore: 82,
  
  // 可维护性评分
  maintainabilityScore: 90,
  
  // 可扩展性得分
  extensibilityScore: 85,
  
  // 测评统计
  statistics: {
    totalEvaluations: 12,
    completedEvaluations: 10,
    averageScore: 82.3,
    highestScore: 92.5,
    lowestScore: 68.0
  }
});

/**
 * Get score color based on score value
 */
const getScoreColor = (score: number) => {
  if (score >= 90) return '#52c41a';
  if (score >= 80) return '#1890ff';
  if (score >= 60) return '#faad14';
  return '#ff4d4f';
};

/**
 * Get score level text and color
 */
const getScoreLevel = (score: number) => {
  if (score >= 90) return { text: '优秀', color: 'success' };
  if (score >= 80) return { text: '良好', color: 'processing' };
  if (score >= 60) return { text: '及格', color: 'warning' };
  return { text: '需改进', color: 'error' };
};

/**
 * Refresh data (placeholder for future API integration)
 */
const refresh = () => {
  // TODO: Load data from API
  console.log('Refreshing evaluation data');
};

/**
 * Handle window resize (placeholder for future chart integration)
 */
const handleWindowResize = () => {
  // TODO: Resize charts if needed
  console.log('Handling window resize');
};

defineExpose({
  refresh,
  handleWindowResize
});
</script>

<template>
  <div class="py-2 text-3 space-y-2">
    <!-- Row 0: Statistics Summary (Full width) - Moved to top -->
    <div class="flex space-x-2 mb-2">
      <div class="statistics-card border rounded h-35 flex-1 p-4 bg-gradient-to-r from-blue-50 via-purple-50 to-pink-50">
        <div class="section-title mb-4 flex items-center">
          <Icon icon="icon-zonglan" class="mr-2 text-blue-600 text-lg" />
          <span class="text-base font-semibold">测评统计</span>
        </div>
        <div class="flex justify-around">
          <div class="statistic-item">
            <Statistic
              title="总测评数"
              :value="evaluationData.statistics.totalEvaluations"
              suffix="次"
              :value-style="{ fontSize: '28px', fontWeight: 'bold', color: '#1890ff' }" />
          </div>
          <div class="statistic-item">
            <Statistic
              title="已完成"
              :value="evaluationData.statistics.completedEvaluations"
              suffix="次"
              :value-style="{ fontSize: '28px', fontWeight: 'bold', color: '#52c41a' }" />
          </div>
          <div class="statistic-item">
            <Statistic
              title="平均得分"
              :value="evaluationData.statistics.averageScore"
              :precision="1"
              suffix="分"
              :value-style="{ fontSize: '28px', fontWeight: 'bold', color: '#722ed1' }" />
          </div>
          <div class="statistic-item">
            <Statistic
              title="最高分"
              :value="evaluationData.statistics.highestScore"
              :precision="1"
              suffix="分"
              :value-style="{ fontSize: '28px', fontWeight: 'bold', color: '#52c41a' }" />
          </div>
          <div class="statistic-item">
            <Statistic
              title="最低分"
              :value="evaluationData.statistics.lowestScore"
              :precision="1"
              suffix="分"
              :value-style="{ fontSize: '28px', fontWeight: 'bold', color: '#ff4d4f' }" />
          </div>
        </div>
      </div>
    </div>

    <!-- Row 1: Overall Score and Requirement Completion (3:5 ratio) -->
    <div class="flex space-x-2 h-44">
      <!-- Overall Score Section (3/8 width) -->
      <div class="border rounded h-full flex-3/8 p-2">
        <div class="section-title">综合评分</div>
        <div class="flex items-center justify-center h-3/4 space-x-4">
          <Progress
            type="circle"
            :width="120"
            :percent="evaluationData.overallScore"
            :strokeColor="getScoreColor(evaluationData.overallScore)"
            :format="() => evaluationData.overallScore.toFixed(1)" />
          <div class="flex flex-col justify-center">
            <Tag :color="getScoreLevel(evaluationData.overallScore).color" class="text-base px-3 py-1 mb-2">
              {{ getScoreLevel(evaluationData.overallScore).text }}
            </Tag>
            <div class="text-sm text-slate-500">综合得分：{{ evaluationData.overallScore.toFixed(1) }} 分</div>
          </div>
        </div>
      </div>

      <!-- Requirement Completion Section (5/8 width) -->
      <div class="border rounded h-full flex-5/8 p-2">
        <div class="section-title mb-2">需求完成率</div>
        <div class="flex items-center h-3/4 space-x-4">
          <div class="flex-shrink-0 ml-10">
            <Progress
              type="circle"
              :width="100"
              :percent="evaluationData.requirementCompletion.rate"
              :strokeColor="getScoreColor(evaluationData.requirementCompletion.rate)"
              :format="() => `${evaluationData.requirementCompletion.rate}%`" />
          </div>
          <div class="flex-1 flex space-x-2 justify-around">
            <div class="flex-1 text-center">
              <div class="font-bold text-5" :style="{ color: getScoreColor(evaluationData.requirementCompletion.rate) }">
                {{ evaluationData.requirementCompletion.rate }}%
              </div>
              <div class="text-sm text-slate-500 mt-1">完成率</div>
            </div>
            <div class="flex-1 text-center">
              <div class="font-bold text-5 text-blue-600">
                {{ evaluationData.requirementCompletion.completed }}/{{ evaluationData.requirementCompletion.total }}
              </div>
              <div class="text-sm text-slate-500 mt-1">已完成/总数</div>
            </div>
            <div class="flex-1 text-center">
              <div class="font-bold text-5 text-emerald-600">
                {{ evaluationData.requirementCompletion.total - evaluationData.requirementCompletion.completed }}
              </div>
              <div class="text-sm text-slate-500 mt-1">待完成</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Row 2: Test Coverage and Pass Rates (3:5 ratio) -->
    <div class="flex space-x-2 h-45">
      <!-- Test Coverage Section (3/8 width) -->
      <div class="border rounded h-full flex-3/8 p-2 space-y-2">
        <div class="section-title">功能测试覆盖率</div>
        <div class="flex items-center justify-center h-3/4 space-x-4">
          <Progress
            type="circle"
            :width="100"
            :percent="evaluationData.functionTestCoverage.rate"
            :strokeColor="getScoreColor(evaluationData.functionTestCoverage.rate)"
            :format="() => `${evaluationData.functionTestCoverage.rate}%`" />
          <div class="flex flex-col justify-center">
            <div class="font-bold text-4 text-slate-700">
              {{ evaluationData.functionTestCoverage.covered }}/{{ evaluationData.functionTestCoverage.total }}
            </div>
            <div class="text-sm text-slate-500 mt-1">已覆盖/总数</div>
          </div>
        </div>
      </div>

      <!-- Test Pass Rates Section (5/8 width) -->
      <div class="border rounded h-full flex-5/8 p-2 space-y-2">
        <div class="section-title">测试通过率</div>
        <div class="flex space-x-2 justify-around">
          <div class="flex-1 text-center">
            <div class="font-bold text-4" :style="{ color: getScoreColor(evaluationData.functionTestPassRate.rate) }">
              {{ evaluationData.functionTestPassRate.rate }}%
            </div>
            <div class="text-sm text-slate-500 mt-1">功能测试</div>
            <div class="text-xs text-slate-400 mt-0.5">
              {{ evaluationData.functionTestPassRate.passed }}/{{ evaluationData.functionTestPassRate.total }}
            </div>
          </div>
          <div class="flex-1 text-center">
            <div class="font-bold text-4" :style="{ color: getScoreColor(evaluationData.performanceTestPassRate.rate) }">
              {{ evaluationData.performanceTestPassRate.rate }}%
            </div>
            <div class="text-sm text-slate-500 mt-1">性能测试</div>
            <div class="text-xs text-slate-400 mt-0.5">
              {{ evaluationData.performanceTestPassRate.passed }}/{{ evaluationData.performanceTestPassRate.total }}
            </div>
          </div>
          <div class="flex-1 text-center">
            <div class="font-bold text-4" :style="{ color: getScoreColor(evaluationData.stabilityTestPassRate.rate) }">
              {{ evaluationData.stabilityTestPassRate.rate }}%
            </div>
            <div class="text-sm text-slate-500 mt-1">稳定性测试</div>
            <div class="text-xs text-slate-400 mt-0.5">
              {{ evaluationData.stabilityTestPassRate.passed }}/{{ evaluationData.stabilityTestPassRate.total }}
            </div>
          </div>
        </div>
        <div class="flex space-x-2 h-1/2">
          <div class="flex-1 flex items-center justify-center">
            <Progress
              type="circle"
              :width="70"
              :percent="evaluationData.functionTestPassRate.rate"
              :strokeColor="getScoreColor(evaluationData.functionTestPassRate.rate)" />
          </div>
          <div class="flex-1 flex items-center justify-center">
            <Progress
              type="circle"
              :width="70"
              :percent="evaluationData.performanceTestPassRate.rate"
              :strokeColor="getScoreColor(evaluationData.performanceTestPassRate.rate)" />
          </div>
          <div class="flex-1 flex items-center justify-center">
            <Progress
              type="circle"
              :width="70"
              :percent="evaluationData.stabilityTestPassRate.rate"
              :strokeColor="getScoreColor(evaluationData.stabilityTestPassRate.rate)" />
          </div>
        </div>
      </div>
    </div>

    <!-- Row 3: Quality Scores (Full width) -->
    <div class="flex space-x-2 h-43.5">
      <!-- Compatibility Score -->
      <div class="border rounded h-full flex-1 p-2">
        <div class="section-title">兼容性评分</div>
        <div class="flex flex-col items-center justify-center h-3/4">
          <Progress
            type="circle"
            :width="100"
            :percent="evaluationData.compatibilityScore"
            :strokeColor="getScoreColor(evaluationData.compatibilityScore)"
            :format="() => `${evaluationData.compatibilityScore}分`" />
          <div class="mt-4">
            <Tag :color="getScoreLevel(evaluationData.compatibilityScore).color">
              {{ getScoreLevel(evaluationData.compatibilityScore).text }}
            </Tag>
          </div>
        </div>
      </div>

      <!-- Usability Score -->
      <div class="border rounded h-full flex-1 p-2">
        <div class="section-title">易用性评分</div>
        <div class="flex flex-col items-center justify-center h-3/4">
          <Progress
            type="circle"
            :width="100"
            :percent="evaluationData.usabilityScore"
            :strokeColor="getScoreColor(evaluationData.usabilityScore)"
            :format="() => `${evaluationData.usabilityScore}分`" />
          <div class="mt-4">
            <Tag :color="getScoreLevel(evaluationData.usabilityScore).color">
              {{ getScoreLevel(evaluationData.usabilityScore).text }}
            </Tag>
          </div>
        </div>
      </div>

      <!-- Maintainability Score -->
      <div class="border rounded h-full flex-1 p-2">
        <div class="section-title">可维护性评分</div>
        <div class="flex flex-col items-center justify-center h-3/4">
          <Progress
            type="circle"
            :width="100"
            :percent="evaluationData.maintainabilityScore"
            :strokeColor="getScoreColor(evaluationData.maintainabilityScore)"
            :format="() => `${evaluationData.maintainabilityScore}分`" />
          <div class="mt-4">
            <Tag :color="getScoreLevel(evaluationData.maintainabilityScore).color">
              {{ getScoreLevel(evaluationData.maintainabilityScore).text }}
            </Tag>
          </div>
        </div>
      </div>

      <!-- Extensibility Score -->
      <div class="border rounded h-full flex-1 p-2">
        <div class="section-title">可扩展性得分</div>
        <div class="flex flex-col items-center justify-center h-3/4">
          <Progress
            type="circle"
            :width="100"
            :percent="evaluationData.extensibilityScore"
            :strokeColor="getScoreColor(evaluationData.extensibilityScore)"
            :format="() => `${evaluationData.extensibilityScore}分`" />
          <div class="mt-4">
            <Tag :color="getScoreLevel(evaluationData.extensibilityScore).color">
              {{ getScoreLevel(evaluationData.extensibilityScore).text }}
            </Tag>
          </div>
        </div>
      </div>
    </div>

  </div>
</template>

<style scoped>
/* Custom flex classes for 3:5 ratio layout */
.flex-3\/8 {
  flex: 0 0 37.5%; /* 3/8 = 37.5% */
}

.flex-5\/8 {
  flex: 0 0 62.5%; /* 5/8 = 62.5% */
}

/* Section title styling - consistent with CTO page */
.section-title {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
  color: #262626;
}

/* Height utilities */
.h-45 {
  height: 260px;
}

.h-50 {
  height: 200px;
}

.h-3\/4 {
  height: 75%;
}

/* Statistics card styling */
.statistics-card {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  border: 1px solid #e8e8e8;
  transition: all 0.3s ease;
}

.statistics-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
  transform: translateY(-2px);
}

.statistic-item {
  flex: 1;
  text-align: center;
  padding: 0 8px;
}

.statistic-item :deep(.ant-statistic-title) {
  font-size: 13px;
  color: #8c8c8c;
  margin-bottom: 4px;
  font-weight: 500;
}

.statistic-item :deep(.ant-statistic-content) {
  font-size: 28px;
  font-weight: bold;
  line-height: 1.2;
}

.statistic-item :deep(.ant-statistic-content-suffix) {
  font-size: 16px;
  margin-left: 4px;
  color: #8c8c8c;
}
</style>
