<script setup lang="ts">
import { defineAsyncComponent, ref, onMounted, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Button, Table, Tag, Progress, Empty } from 'ant-design-vue';
import { Icon, IconRefresh, Spin } from '@xcan-angus/vue-ui';
import { BasicProps } from '@/types/types';
import type { EvaluationItem } from './types';

const { t } = useI18n();

const props = withDefaults(defineProps<BasicProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined
});

// Async components
const CreateEvaluationModal = defineAsyncComponent(() => import('./CreateModal.vue'));
const ResultModal = defineAsyncComponent(() => import('./ResultModal.vue'));

// State
const loading = ref(false);
const evaluationList = ref<EvaluationItem[]>([]);
const createModalVisible = ref(false);
const resultModalVisible = ref(false);
const selectedEvaluation = ref<EvaluationItem | null>(null);

// Mock data - 使用 JSON 配置数据
const mockEvaluations: EvaluationItem[] = [
  {
    id: '1',
    name: '项目质量测评 - 2024年Q1',
    scope: {
      type: 'project',
      name: 'AngusTester 主项目'
    },
    metrics: [
      'requirement_completion_rate',
      'function_test_coverage',
      'function_test_pass_rate',
      'performance_test_pass_rate',
      'stability_test_pass_rate',
      'compatibility_score',
      'usability_score',
      'maintainability_score',
      'extensibility_score'
    ],
    createdAt: '2024-01-15 10:30:00',
    createdBy: '张三',
    status: 'completed',
    result: {
      overallScore: 85.5,
      metrics: {
        requirement_completion_rate: { score: 90, value: '90%' },
        function_test_coverage: { score: 88, value: '88%' },
        function_test_pass_rate: { score: 92, value: '92%' },
        performance_test_pass_rate: { score: 85, value: '85%' },
        stability_test_pass_rate: { score: 80, value: '80%' },
        compatibility_score: { score: 88, value: '88分' },
        usability_score: { score: 82, value: '82分' },
        maintainability_score: { score: 90, value: '90分' },
        extensibility_score: { score: 85, value: '85分' }
      }
    }
  },
  {
    id: '2',
    name: '应用性能测评 - 移动端',
    scope: {
      type: 'application',
      name: '移动端应用'
    },
    metrics: [
      'performance_test_pass_rate',
      'stability_test_pass_rate',
      'compatibility_score',
      'usability_score'
    ],
    createdAt: '2024-01-20 14:20:00',
    createdBy: '李四',
    status: 'completed',
    result: {
      overallScore: 78.5,
      metrics: {
        performance_test_pass_rate: { score: 75, value: '75%' },
        stability_test_pass_rate: { score: 80, value: '80%' },
        compatibility_score: { score: 78, value: '78分' },
        usability_score: { score: 81, value: '81分' }
      }
    }
  },
  {
    id: '3',
    name: '计划完成度测评',
    scope: {
      type: 'plan',
      name: '2024年度计划'
    },
    metrics: [
      'requirement_completion_rate',
      'function_test_coverage',
      'function_test_pass_rate'
    ],
    createdAt: '2024-01-25 09:15:00',
    createdBy: '王五',
    status: 'processing'
  }
];

// 测评指标配置
const metricsConfig: Record<string, { label: string; unit: string }> = {
  requirement_completion_rate: { label: '需求完成率', unit: '%' },
  function_test_coverage: { label: '功能测试覆盖率', unit: '%' },
  function_test_pass_rate: { label: '功能测试通过率', unit: '%' },
  performance_test_pass_rate: { label: '性能测试通过率', unit: '%' },
  stability_test_pass_rate: { label: '稳定性测试通过率', unit: '%' },
  compatibility_score: { label: '兼容性评分', unit: '分' },
  usability_score: { label: '易用性评分', unit: '分' },
  maintainability_score: { label: '可维护性评分', unit: '分' },
  extensibility_score: { label: '可扩展性得分', unit: '分' }
};

// 范围类型配置
const scopeTypeConfig: Record<string, { label: string; color: string }> = {
  project: { label: '项目', color: 'blue' },
  application: { label: '应用', color: 'green' },
  plan: { label: '计划', color: 'orange' }
};

// 加载测评列表
const loadEvaluations = async () => {
  loading.value = true;
  try {
    // 模拟 API 调用延迟
    await new Promise(resolve => setTimeout(resolve, 500));
    evaluationList.value = mockEvaluations;
  } catch (error) {
    console.error('加载测评列表失败:', error);
  } finally {
    loading.value = false;
  }
};

// 刷新数据
const handleRefresh = () => {
  loadEvaluations();
};

// 打开创建测评弹窗
const handleCreate = () => {
  createModalVisible.value = true;
};

// 创建成功回调
const handleCreateSuccess = () => {
  createModalVisible.value = false;
  loadEvaluations();
};

// 查看评审结果
const handleViewResult = (record: EvaluationItem) => {
  selectedEvaluation.value = record;
  resultModalVisible.value = true;
};

// 删除测评
const handleDelete = (record: EvaluationItem) => {
  evaluationList.value = evaluationList.value.filter(item => item.id !== record.id);
};

// 获取状态标签
const getStatusTag = (status: string) => {
  const statusMap: Record<string, { color: string; text: string }> = {
    completed: { color: 'success', text: '已完成' },
    processing: { color: 'processing', text: '进行中' },
    failed: { color: 'error', text: '失败' }
  };
  return statusMap[status] || { color: 'default', text: status };
};

// 表格列配置
const columns = [
  {
    title: '测评名称',
    dataIndex: 'name',
    key: 'name',
    width: 200,
    ellipsis: true
  },
  {
    title: '测评范围',
    key: 'scope',
    width: 150
  },
  {
    title: '测评指标',
    key: 'metrics',
    width: 200
  },
  {
    title: '综合得分',
    key: 'score',
    width: 120
  },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
    width: 100
  },
  {
    title: '创建时间',
    dataIndex: 'createdAt',
    key: 'createdAt',
    width: 160
  },
  {
    title: '创建人',
    dataIndex: 'createdBy',
    key: 'createdBy',
    width: 100
  },
  {
    title: '操作',
    key: 'action',
    width: 150,
    fixed: 'right' as const
  }
];

// 监听 projectId 变化
watch(() => props.projectId, (newProjectId) => {
  if (newProjectId) {
    loadEvaluations();
  }
}, { immediate: true });

onMounted(() => {
  if (props.projectId) {
    loadEvaluations();
  }
});
</script>

<template>
  <div class="w-full h-full bg-white rounded-lg shadow-sm flex flex-col">
    <!-- Header Section -->
    <div class="bg-gradient-to-r from-purple-50 to-pink-50 px-6 py-4 border-b border-gray-100">
      <div class="flex items-center justify-between">
        <div class="flex items-center">
          <div class="w-8 h-8 bg-purple-100 rounded flex items-center justify-center mr-3">
            <Icon icon="icon-pingshen" class="text-purple-600" />
          </div>
          <div>
            <h2 class="text-base font-medium text-gray-900">{{ t('evaluation.introduce.aboutAssessment') }}</h2>
            <p class="text-3.5 text-gray-500 mt-0.5">{{ t('evaluation.introduce.description') }}</p>
          </div>
        </div>
        <div class="flex items-center space-x-3">
          <Button
            type="primary"
            size="small"
            class="flex items-center space-x-1"
            @click="handleCreate">
            <Icon icon="icon-jia" class="text-xs" />
            <span>创建测评</span>
          </Button>
          <IconRefresh @click="handleRefresh">
            <template #default>
              <div class="flex items-center cursor-pointer text-gray-600 space-x-1 hover:text-gray-800 text-xs">
                <Icon icon="icon-shuaxin" class="text-xs" />
                <span>刷新</span>
              </div>
            </template>
          </IconRefresh>
        </div>
      </div>
    </div>

    <!-- Content Section -->
    <div class="flex-1 p-5 overflow-auto">
      <Spin :spinning="loading">
        <div v-if="evaluationList.length > 0">
          <Table
            :columns="columns"
            :dataSource="evaluationList"
            :pagination="false"
            :scroll="{ x: 1200 }"
            row-key="id">
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'scope'">
                <div class="flex items-center space-x-2">
                  <Tag :color="scopeTypeConfig[record.scope.type]?.color || 'default'">
                    {{ scopeTypeConfig[record.scope.type]?.label || record.scope.type }}
                  </Tag>
                  <span class="text-gray-600">{{ record.scope.name }}</span>
                </div>
              </template>

              <template v-if="column.key === 'metrics'">
                <div class="text-gray-600">
                  {{ record.metrics.slice(0, 3).map(key => metricsConfig[key]?.label || key).join('、') }}
                  <span v-if="record.metrics.length > 3" class="text-gray-400">
                    等{{ record.metrics.length }}项
                  </span>
                </div>
              </template>

              <template v-if="column.key === 'score'">
                <div v-if="record.status === 'completed' && record.result" class="flex items-center">
                  <Progress
                    type="circle"
                    :width="58"
                    :percent="record.result.overallScore"
                    :strokeColor="record.result.overallScore >= 90 ? '#52c41a' : record.result.overallScore >= 80 ? '#1890ff' : record.result.overallScore >= 60 ? '#faad14' : '#ff4d4f'"
                    :format="() => record.result.overallScore.toFixed(1)" />
                </div>
                <span v-else class="text-gray-400">-</span>
              </template>

              <template v-if="column.key === 'status'">
                <Tag :color="getStatusTag(record.status).color">
                  {{ getStatusTag(record.status).text }}
                </Tag>
              </template>

              <template v-if="column.key === 'action'">
                <div class="flex items-center space-x-2">
                  <Button
                    v-if="record.status === 'completed'"
                    type="link"
                    size="small"
                    @click="handleViewResult(record)">
                    查看结果
                  </Button>
                  <Button
                    type="link"
                    size="small"
                    danger
                    @click="handleDelete(record)">
                    删除
                  </Button>
                </div>
              </template>
            </template>
          </Table>
        </div>
        <Empty v-else description="暂无测评数据，点击创建测评开始" class="py-16">
          <template #image>
            <div class="w-24 h-24 bg-gray-100 rounded-full flex items-center justify-center">
              <Icon icon="icon-pingce" class="text-4xl text-gray-400" />
            </div>
          </template>
          <Button type="primary" @click="handleCreate">
            <Icon icon="icon-jia" />
            <span class="ml-1">创建测评</span>
          </Button>
        </Empty>
      </Spin>
    </div>

    <!-- Create Evaluation Modal -->
    <CreateEvaluationModal
      v-model:visible="createModalVisible"
      :projectId="projectId"
      @ok="handleCreateSuccess" />

    <!-- Result Modal -->
    <ResultModal
      v-model:visible="resultModalVisible"
      :evaluation="selectedEvaluation" />
  </div>
</template>

<style scoped>
:deep(.ant-table) {
  font-size: 12px;
}

:deep(.ant-table-thead > tr > th) {
  background: #fafafa;
  font-weight: 600;
}
</style>
