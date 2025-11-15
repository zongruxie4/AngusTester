<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Modal, Steps, Form, FormItem, RadioGroup, RadioButton, CheckboxGroup, Checkbox, Button, Card } from 'ant-design-vue';
import { Icon } from '@xcan-angus/vue-ui';
import type { EvaluationScope, MetricKey } from './types';

const { t } = useI18n();

interface Props {
  visible: boolean;
  projectId?: string | number;
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  projectId: undefined
});

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void;
  (e: 'ok'): void;
}>();

// Step control
const currentStep = ref(0);
const formRef = ref();

// Form data
const formState = ref({
  name: '',
  scopeType: 'project' as 'project' | 'application' | 'plan',
  scopeId: '',
  scopeName: '',
  selectedMetrics: [] as MetricKey[]
});

// Mock data for scope selection
const mockProjects = [
  { id: '1', name: 'AngusTester 主项目' },
  { id: '2', name: '移动端应用项目' },
  { id: '3', name: 'Web 前端项目' }
];

const mockApplications = [
  { id: '1', name: '移动端应用' },
  { id: '2', name: 'Web 应用' },
  { id: '3', name: 'API 服务' }
];

const mockPlans = [
  { id: '1', name: '2024年度计划' },
  { id: '2', name: 'Q1季度计划' },
  { id: '3', name: 'Q2季度计划' }
];

// Available metrics
const allMetrics: Array<{ key: MetricKey; label: string; description: string }> = [
  { key: 'requirement_completion_rate', label: '需求完成率', description: '评估需求完成情况' },
  { key: 'function_test_coverage', label: '功能测试覆盖率', description: '功能测试覆盖范围' },
  { key: 'function_test_pass_rate', label: '功能测试通过率', description: '功能测试通过情况' },
  { key: 'performance_test_pass_rate', label: '性能测试通过率', description: '性能测试通过情况' },
  { key: 'stability_test_pass_rate', label: '稳定性测试通过率', description: '稳定性测试通过情况' },
  { key: 'compatibility_score', label: '兼容性评分', description: '系统兼容性评估' },
  { key: 'usability_score', label: '易用性评分', description: '用户体验评估' },
  { key: 'maintainability_score', label: '可维护性评分', description: '代码可维护性评估' },
  { key: 'extensibility_score', label: '可扩展性得分', description: '系统扩展能力评估' }
];

// Computed
const scopeOptions = computed(() => {
  switch (formState.value.scopeType) {
    case 'project':
      return mockProjects;
    case 'application':
      return mockApplications;
    case 'plan':
      return mockPlans;
    default:
      return [];
  }
});

const canGoNext = computed(() => {
  switch (currentStep.value) {
    case 0:
      return formState.value.scopeType && formState.value.scopeId && formState.value.scopeName;
    case 1:
      return formState.value.selectedMetrics.length > 0;
    case 2:
      return formState.value.name.trim().length > 0;
    default:
      return false;
  }
});

// Methods
const handleCancel = () => {
  emit('update:visible', false);
  resetForm();
};

const resetForm = () => {
  currentStep.value = 0;
  formState.value = {
    name: '',
    scopeType: 'project',
    scopeId: '',
    scopeName: '',
    selectedMetrics: []
  };
};

const handleNext = () => {
  if (canGoNext.value && currentStep.value < 2) {
    currentStep.value++;
  }
};

const handlePrev = () => {
  if (currentStep.value > 0) {
    currentStep.value--;
  }
};

const handleScopeTypeChange = () => {
  formState.value.scopeId = '';
  formState.value.scopeName = '';
};

const handleScopeSelect = (scope: { id: string; name: string }) => {
  formState.value.scopeId = scope.id;
  formState.value.scopeName = scope.name;
};

const handleOk = async () => {
  if (!canGoNext.value) {
    return;
  }

  // 生成测评结果（模拟）
  const result = generateEvaluationResult(formState.value.selectedMetrics);

  // 这里应该调用 API 保存测评
  console.log('创建测评:', {
    ...formState.value,
    result
  });

  // 模拟 API 调用
  await new Promise(resolve => setTimeout(resolve, 1000));

  emit('ok');
  handleCancel();
};

// 生成测评结果（模拟数据）
const generateEvaluationResult = (metrics: MetricKey[]) => {
  const result: Record<string, { score: number; value: string }> = {};
  let totalScore = 0;

  metrics.forEach(metric => {
    // 生成随机分数（60-95之间）
    const score = Math.floor(Math.random() * 35) + 60;
    totalScore += score;

    if (metric.includes('rate') || metric.includes('coverage')) {
      result[metric] = { score, value: `${score}%` };
    } else {
      result[metric] = { score, value: `${score}分` };
    }
  });

  const overallScore = totalScore / metrics.length;

  return {
    overallScore: Math.round(overallScore * 10) / 10,
    metrics: result,
    generatedAt: new Date().toISOString()
  };
};

// Watch visible change
watch(() => props.visible, (visible) => {
  if (!visible) {
    resetForm();
  }
});
</script>

<template>
  <Modal
    :visible="props.visible"
    title="创建测评"
    :width="800"
    :okButtonProps="{
      loading: false
    }"
    :okText="currentStep === 2 ? '完成创建' : '下一步'"
    :cancelText="currentStep === 0 ? '取消' : '上一步'"
    @ok="currentStep === 2 ? handleOk() : handleNext()"
    @cancel="currentStep === 0 ? handleCancel() : handlePrev()">
    <div class="create-evaluation-modal">
      <Steps :current="currentStep" class="mb-6">
        <Steps.Step title="指定范围" description="选择测评的项目、应用或计划" />
        <Steps.Step title="选择指标" description="选择需要测评的指标项" />
        <Steps.Step title="生成结果" description="确认信息并生成测评结果" />
      </Steps>

      <div class="step-content">
        <!-- Step 1: Scope Selection -->
        <div v-if="currentStep === 0" class="step-panel">
          <Form :model="formState" layout="vertical">
            <FormItem label="测评范围类型" required>
              <RadioGroup v-model:value="formState.scopeType" @change="handleScopeTypeChange">
                <RadioButton value="project">
                  <Icon icon="icon-xiangmu" class="mr-1" />
                  项目
                </RadioButton>
                <RadioButton value="application">
                  <Icon icon="icon-yingyong" class="mr-1" />
                  应用
                </RadioButton>
                <RadioButton value="plan">
                  <Icon icon="icon-jihua" class="mr-1" />
                  计划
                </RadioButton>
              </RadioGroup>
            </FormItem>

            <FormItem label="选择具体范围" required>
              <div class="grid grid-cols-3 gap-3">
                <Card
                  v-for="scope in scopeOptions"
                  :key="scope.id"
                  :class="[
                    'cursor-pointer transition-all',
                    formState.scopeId === scope.id ? 'border-blue-500 bg-blue-50' : 'hover:border-gray-400'
                  ]"
                  @click="handleScopeSelect(scope)">
                  <div class="flex items-center">
                    <div
                      :class="[
                        'w-4 h-4 rounded-full border-2 mr-2 flex items-center justify-center',
                        formState.scopeId === scope.id ? 'border-blue-500 bg-blue-500' : 'border-gray-300'
                      ]">
                      <Icon
                        v-if="formState.scopeId === scope.id"
                        icon="icon-xuanzhongduigou"
                        class="text-white text-xs" />
                    </div>
                    <span>{{ scope.name }}</span>
                  </div>
                </Card>
              </div>
            </FormItem>
          </Form>
        </div>

        <!-- Step 2: Metrics Selection -->
        <div v-if="currentStep === 1" class="step-panel">
          <Form :model="formState" layout="vertical">
            <FormItem label="选择测评指标" required>
              <CheckboxGroup v-model:value="formState.selectedMetrics" class="w-full">
                <div class="grid grid-cols-2 gap-3">
                  <Card
                    v-for="metric in allMetrics"
                    :key="metric.key"
                    :class="[
                      'cursor-pointer transition-all',
                      formState.selectedMetrics.includes(metric.key) ? 'border-blue-500 bg-blue-50' : 'hover:border-gray-400'
                    ]"
                    @click="
                      const index = formState.selectedMetrics.indexOf(metric.key);
                      if (index > -1) {
                        formState.selectedMetrics.splice(index, 1);
                      } else {
                        formState.selectedMetrics.push(metric.key);
                      }
                    ">
                    <Checkbox :value="metric.key" class="w-full">
                      <div>
                        <div class="font-medium">{{ metric.label }}</div>
                        <div class="text-xs text-gray-500 mt-1">{{ metric.description }}</div>
                      </div>
                    </Checkbox>
                  </Card>
                </div>
              </CheckboxGroup>
            </FormItem>
          </Form>
        </div>

        <!-- Step 3: Confirm and Generate -->
        <div v-if="currentStep === 2" class="step-panel">
          <Form :model="formState" layout="vertical">
            <FormItem label="测评名称" required>
              <input
                v-model="formState.name"
                type="text"
                class="w-full px-3 py-2 border border-gray-300 rounded focus:outline-none focus:border-blue-500"
                placeholder="请输入测评名称"
                maxlength="100" />
            </FormItem>

            <FormItem label="测评信息确认">
              <div class="bg-gray-50 p-4 rounded space-y-3">
                <div class="flex items-center">
                  <span class="w-24 text-gray-600">测评范围：</span>
                  <Tag color="blue">{{ formState.scopeType === 'project' ? '项目' : formState.scopeType === 'application' ? '应用' : '计划' }}</Tag>
                  <span class="ml-2">{{ formState.scopeName }}</span>
                </div>
                <div class="flex items-start">
                  <span class="w-24 text-gray-600">测评指标：</span>
                  <div class="flex-1">
                    <div class="flex flex-wrap gap-2">
                      <Tag
                        v-for="metricKey in formState.selectedMetrics"
                        :key="metricKey"
                        color="green">
                        {{ allMetrics.find(m => m.key === metricKey)?.label }}
                      </Tag>
                    </div>
                  </div>
                </div>
              </div>
            </FormItem>

            <div class="mt-4 p-4 bg-blue-50 rounded border border-blue-200">
              <div class="flex items-center text-blue-700">
                <Icon icon="icon-tishi" class="mr-2" />
                <span class="font-medium">提示</span>
              </div>
              <p class="text-sm text-blue-600 mt-2">
                点击"完成创建"后将自动生成测评结果，您可以在测评列表中查看详细结果。
              </p>
            </div>
          </Form>
        </div>
      </div>
    </div>
  </Modal>
</template>

<style scoped>
.create-evaluation-modal {
  padding: 20px 0;
}

.step-content {
  min-height: 400px;
}

.step-panel {
  padding: 20px 0;
}

:deep(.ant-steps-item-title) {
  font-size: 14px;
}

:deep(.ant-card-body) {
  padding: 12px;
}

:deep(.ant-checkbox-wrapper) {
  width: 100%;
  margin: 0;
}
</style>
