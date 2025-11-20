<script setup lang="ts">
import { computed } from 'vue';
import { Modal, Progress, Card, Tag, Divider, Statistic } from 'ant-design-vue';
import { Icon } from '@xcan-angus/vue-ui';
import type { EvaluationItem } from './types';

interface Props {
  visible: boolean;
  evaluation: EvaluationItem | null;
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  evaluation: null
});

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void;
}>();

// 测评指标配置
const metricsConfig: Record<string, { label: string; unit: string; description: string }> = {
  requirement_completion_rate: { label: '需求完成率', unit: '%', description: '评估需求完成情况，反映项目进度' },
  function_test_coverage: { label: '功能测试覆盖率', unit: '%', description: '功能测试覆盖范围，反映测试完整性' },
  function_test_pass_rate: { label: '功能测试通过率', unit: '%', description: '功能测试通过情况，反映功能质量' },
  performance_test_pass_rate: { label: '性能测试通过率', unit: '%', description: '性能测试通过情况，反映性能质量' },
  stability_test_pass_rate: { label: '稳定性测试通过率', unit: '%', description: '稳定性测试通过情况，反映系统稳定性' },
  compatibility_score: { label: '兼容性评分', unit: '分', description: '系统兼容性评估，反映跨平台能力' },
  usability_score: { label: '易用性评分', unit: '分', description: '用户体验评估，反映产品易用性' },
  maintainability_score: { label: '可维护性评分', unit: '分', description: '代码可维护性评估，反映代码质量' },
  extensibility_score: { label: '可扩展性得分', unit: '分', description: '系统扩展能力评估，反映架构设计' }
};

// 范围类型配置
const scopeTypeConfig: Record<string, { label: string; color: string }> = {
  project: { label: '项目', color: 'blue' },
  application: { label: '应用', color: 'green' },
  plan: { label: '计划', color: 'orange' }
};

// 获取分数颜色
const getScoreColor = (score: number) => {
  if (score >= 90) return '#52c41a';
  if (score >= 80) return '#1890ff';
  if (score >= 60) return '#faad14';
  return '#ff4d4f';
};

// 获取分数等级
const getScoreLevel = (score: number) => {
  if (score >= 90) return { text: '优秀', color: 'success' };
  if (score >= 80) return { text: '良好', color: 'processing' };
  if (score >= 60) return { text: '及格', color: 'warning' };
  return { text: '需改进', color: 'error' };
};

// 计算指标统计
const metricsStats = computed(() => {
  if (!props.evaluation?.result) return null;

  const metrics = props.evaluation.result.metrics;
  const scores = Object.values(metrics).map(m => m.score);
  const maxScore = Math.max(...scores);
  const minScore = Math.min(...scores);
  const avgScore = scores.reduce((a, b) => a + b, 0) / scores.length;

  return {
    max: maxScore,
    min: minScore,
    avg: avgScore,
    count: scores.length
  };
});

const handleCancel = () => {
  emit('update:visible', false);
};
</script>

<template>
  <Modal
    :visible="props.visible"
    title="评测结果"
    :width="1200"
    :footer="null"
    @cancel="handleCancel">
    <div v-if="evaluation && evaluation.result" class="result-modal">
      <!-- Header Info -->
      <div class="mb-6">
        <div class="flex items-center justify-between mb-4">
          <div>
            <h3 class="text-lg font-semibold text-gray-900">{{ evaluation.name }}</h3>
            <div class="flex items-center space-x-4 mt-2 text-sm text-gray-600">
              <div class="flex items-center">
                <Tag :color="scopeTypeConfig[evaluation.scope.type]?.color">
                  {{ scopeTypeConfig[evaluation.scope.type]?.label }}
                </Tag>
                <span class="ml-2">{{ evaluation.scope.name }}</span>
              </div>
              <span>创建时间：{{ evaluation.createdAt }}</span>
              <span>创建人：{{ evaluation.createdBy }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Overall Score -->
      <Card class="mb-6 bg-gradient-to-r from-blue-50 to-purple-50">
        <div class="flex items-center justify-between">
          <div class="flex-1">
            <div class="text-sm text-gray-600 mb-2">综合得分</div>
            <div class="flex items-baseline space-x-2">
              <span class="text-4xl font-bold" :style="{ color: getScoreColor(evaluation.result.overallScore) }">
                {{ evaluation.result.overallScore.toFixed(1) }}
              </span>
              <span class="text-gray-500">分</span>
              <Tag :color="getScoreLevel(evaluation.result.overallScore).color" class="ml-2">
                {{ getScoreLevel(evaluation.result.overallScore).text }}
              </Tag>
            </div>
          </div>
          <div class="flex items-center space-x-6">
            <Statistic
              v-if="metricsStats"
              title="最高分"
              :value="metricsStats.max"
              :precision="1"
              suffix="分" />
            <Statistic
              v-if="metricsStats"
              title="最低分"
              :value="metricsStats.min"
              :precision="1"
              suffix="分" />
            <Statistic
              v-if="metricsStats"
              title="平均分"
              :value="metricsStats.avg"
              :precision="1"
              suffix="分" />
          </div>
        </div>
      </Card>

      <Divider>详细指标</Divider>

      <!-- Metrics Details -->
      <div class="grid grid-cols-2 gap-4">
        <Card
          v-for="(metricKey, index) in evaluation.metrics"
          :key="metricKey"
          class="metric-card">
          <div class="flex items-start justify-between mb-3">
            <div class="flex-1">
              <div class="flex items-center space-x-2 mb-1">
                <span class="font-semibold text-gray-900">
                  {{ metricsConfig[metricKey]?.label || metricKey }}
                </span>
                <Tag color="blue">{{ index + 1 }}</Tag>
              </div>
              <p class="text-xs text-gray-500">
                {{ metricsConfig[metricKey]?.description }}
              </p>
            </div>
          </div>

          <div v-if="evaluation.result.metrics[metricKey]" class="mt-4">
            <div class="flex items-center justify-between mb-2">
              <span class="text-sm text-gray-600">得分</span>
              <span
                class="text-lg font-bold"
                :style="{ color: getScoreColor(evaluation.result.metrics[metricKey].score) }">
                {{ evaluation.result.metrics[metricKey].value }}
              </span>
            </div>
            <Progress
              :percent="evaluation.result.metrics[metricKey].score"
              :strokeColor="getScoreColor(evaluation.result.metrics[metricKey].score)"
              :showInfo="false" />
            <div class="flex items-center justify-between mt-2">
              <Tag :color="getScoreLevel(evaluation.result.metrics[metricKey].score).color" size="small">
                {{ getScoreLevel(evaluation.result.metrics[metricKey].score).text }}
              </Tag>
              <span class="text-xs text-gray-400">
                {{ evaluation.result.metrics[metricKey].score }}/100
              </span>
            </div>
          </div>
        </Card>
      </div>

      <!-- Summary -->
      <Card class="mt-6 bg-gray-50">
        <div class="flex items-start space-x-3">
          <Icon icon="icon-tishi" class="text-blue-500 text-lg mt-0.5" />
          <div class="flex-1">
            <div class="font-medium text-gray-900 mb-2">测评总结</div>
            <div class="text-sm text-gray-600 space-y-1">
              <p>
                本次测评共评估了 <strong>{{ evaluation.metrics.length }}</strong> 项指标，
                综合得分为 <strong>{{ evaluation.result.overallScore.toFixed(1) }}</strong> 分，
                评级为 <strong>{{ getScoreLevel(evaluation.result.overallScore).text }}</strong>。
              </p>
              <p v-if="metricsStats">
                其中最高分为 {{ metricsStats.max.toFixed(1) }} 分，最低分为 {{ metricsStats.min.toFixed(1) }} 分，
                平均分为 {{ metricsStats.avg.toFixed(1) }} 分。
              </p>
            </div>
          </div>
        </div>
      </Card>
    </div>

    <div v-else class="text-center py-8 text-gray-500">
      暂无测评结果
    </div>
  </Modal>
</template>

<style scoped>
.result-modal {
  padding: 10px 0;
}

.metric-card {
  transition: all 0.3s;
}

.metric-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

:deep(.ant-statistic-title) {
  font-size: 12px;
  color: #8c8c8c;
}

:deep(.ant-statistic-content) {
  font-size: 20px;
  font-weight: 600;
}
</style>
