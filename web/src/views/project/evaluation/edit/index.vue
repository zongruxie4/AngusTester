<script setup lang="ts">
import { computed, inject, nextTick, onMounted, ref, watch } from 'vue';
import { Button, Form, FormItem, Radio, RadioGroup, Tree } from 'ant-design-vue';
import { DatePicker, Icon, Input, notification, Select, Spin } from '@xcan-angus/vue-ui';
import { enumUtils, TESTER, utils } from '@xcan-angus/infra';
import dayjs from 'dayjs';
import type { Rule } from 'ant-design-vue/es/form';
import { evaluation, modules, project } from '@/api/tester';
import { useI18n } from 'vue-i18n';
import { DATE_TIME_FORMAT, TIME_FORMAT } from '@/utils/constant';
import { EvaluationDetail, EvaluationFormState } from '../types';
import { EvaluationScope, EvaluationPurpose } from '@/enums/enums';
import { BasicProps } from '@/types/types';
import { travelTreeData } from '@/utils/utils';

const { t } = useI18n();

// Props
const props = withDefaults(defineProps<BasicProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  data: undefined
});

const updateTabPane = inject<(data: { [key: string]: any }) => void>('updateTabPane', () => ({}));
const deleteTabPane = inject<(keys: string[]) => void>('deleteTabPane', () => ({}));
const replaceTabPane = inject<(id: string, data: { [key: string]: any }) => void>('replaceTabPane', () => ({}));

// Computed properties
const isEditMode = computed(() => {
  return !!props.data?.id && props.data.id !== 'new-evaluation';
});

// FUNCTIONAL_SCORE：功能实现完整性与正确性
// PERFORMANCE_SCORE：系统处理速度与资源效率
// STABILITY_SCORE：系统长时间无故障运行能力
// SECURITY_SCORE：抵御安全威胁与数据保护能力
// COMPATIBILITY_SCORE：与不同软硬件环境适配程度
// COMPLIANCE_SCORE：符合行业标准与法规要求
// AVAILABILITY_SCORE：服务可正常访问的时间比例
// USABILITY_SCORE：界面友好与操作便捷程度
// MAINTAINABILITY_SCORE：系统修改、更新与修复便利性

const getPurposeMessage = (purpose: EvaluationPurpose) => {
  switch (purpose) {
    case EvaluationPurpose.FUNCTIONAL_SCORE:
      return '功能实现完整性与正确性';
    case EvaluationPurpose.PERFORMANCE_SCORE:
      return '系统处理速度与资源效率';
    case EvaluationPurpose.STABILITY_SCORE:
      return '系统长时间无故障运行能力';
    case EvaluationPurpose.SECURITY_SCORE:
      return '抵御安全威胁与数据保护能力';
    case EvaluationPurpose.COMPATIBILITY_SCORE:
      return '与不同软硬件环境适配程度';
    case EvaluationPurpose.COMPLIANCE_SCORE:
      return '符合行业标准与法规要求';
    case EvaluationPurpose.AVAILABILITY_SCORE:
      return '服务可正常访问的时间比例';
    case EvaluationPurpose.USABILITY_SCORE:
      return '界面友好与操作便捷程度';
    case EvaluationPurpose.MAINTAINABILITY_SCORE:
      return '系统修改、更新与修复便利性';
    case EvaluationPurpose.SCALABILITY_SCORE:
      return '系统可扩展性与性能优化能力';
    default:
      return '';
  }
};

// Reactive data
const formRef = ref();
const loading = ref(false);
const evaluationDetail = ref<EvaluationDetail>();


// Resource selection state
const moduleTreeData = ref<any[]>([]);
const projectList = ref<any[]>([]);

// Enum options - Load from enumUtils to get internationalized messages
const scopeOptions = computed(() => {
  return enumUtils.enumToMessages(EvaluationScope).map(item => ({
    label: item.message,
    value: item.value
  }));
});

const purposeOptions = computed(() => {
  return enumUtils.enumToMessages(EvaluationPurpose).map(item => ({
    label: item.message,
    value: item.value,
    message: getPurposeMessage(item.value as EvaluationPurpose)
  }));
});

// Form state initialization
const defaultStartDate = dayjs().format(DATE_TIME_FORMAT);
const defaultDeadlineDate = dayjs().add(1, 'month').format(DATE_TIME_FORMAT);
const formState = ref<EvaluationFormState>({
  projectId: props.projectId || '',
  name: '',
  scope: EvaluationScope.PROJECT,
  purposes: [],
  resourceId: undefined,
  startDate: defaultStartDate,
  deadlineDate: defaultDeadlineDate,
  date: [defaultStartDate, defaultDeadlineDate]
});


/**
 * Validates purposes selection
 */
const validatePurposes = async (_rule: Rule, value: EvaluationPurpose[]) => {
  if (!value || value.length === 0) {
    return Promise.reject(new Error(t('evaluation.placeholders.selectPurposes')));
  }
  return Promise.resolve();
};

const handleScopeChange = () => {
  formState.value.resourceId = undefined;
};

/**
 * Check if a purpose is selected
 */
const isPurposeSelected = (purpose: EvaluationPurpose): boolean => {
  return formState.value.purposes?.includes(purpose) || false;
};

/**
 * Toggle purpose selection
 */
const togglePurpose = (purpose: string) => {
  const currentPurposes = formState.value.purposes || [];
  const purposeValue = purpose as EvaluationPurpose;
  const index = currentPurposes.indexOf(purposeValue);
  if (index > -1) {
    // Remove if already selected
    formState.value.purposes = currentPurposes.filter(p => p !== purposeValue);
  } else {
    // Add if not selected
    formState.value.purposes = [...currentPurposes, purposeValue];
  }
};

/**
 * Prepares form parameters for API submission
 */
const prepareFormParams = () => {
  // Determine projectId based on scope
  const { projectId, scope} = formState.value;

  const params: any = {
    projectId: scope === EvaluationScope.PROJECT ? projectId : props.projectId,
    name: formState.value.name,
    scope: formState.value.scope,
    purposes: formState.value.purposes || []
  };

  if (isEditMode.value && evaluationDetail.value?.id) {
    params.id = evaluationDetail.value.id;
  }

  const dateRange = formState.value.date;
  if (dateRange && dateRange[0] && dateRange[1]) {
    params.startDate = dateRange[0];
    params.deadlineDate = dateRange[1];
  }

  // Only set resourceId for FUNC_PLAN and MODULE scopes
  if (formState.value.scope !== EvaluationScope.PROJECT) {
    if (formState.value.resourceId !== undefined && formState.value.resourceId !== null && formState.value.resourceId !== '') {
      params.resourceId = formState.value.resourceId
    }
  } else {
    params.resourceId = formState.value.projectId;
  }

  return params;
};

/**
 * Refreshes the evaluation list after successful operations
 */
const refreshEvaluationList = () => {
  nextTick(() => {
    updateTabPane({ _id: 'evaluationList', notify: utils.uuid() });
  });
};

/**
 * Handles evaluation update operation
 */
const handleEvaluationUpdate = async () => {
  const params = prepareFormParams();
  loading.value = true;
  const [error] = await evaluation.updateEvaluation(params);
  loading.value = false;
  if (error) {
    return;
  }

  notification.success(t('actions.tips.saveSuccess'));

  const id = params.id;
  const name = params.name;
  updateTabPane({ _id: String(id), name });
  if (evaluationDetail.value) {
    evaluationDetail.value.name = name;
  }
};

/**
 * Handles evaluation creation operation
 */
const handleEvaluationCreation = async () => {
  const params = prepareFormParams();
  loading.value = true;
  const [error] = await evaluation.addEvaluation(params);
  loading.value = false;
  if (error) {
    return;
  }

  notification.success(t('actions.tips.addSuccess'));

  deleteTabPane([props?.data?._id as string]);
};

/**
 * Handles form submission with validation
 */
const handleFormSubmit = async () => {
  formRef.value.validate().then(async () => {
    if (!isEditMode.value) {
      await handleEvaluationCreation();
    } else {
      await handleEvaluationUpdate();
    }

    refreshEvaluationList();
  }).catch(() => {
    // Validation failed
  });
};

/**
 * Loads evaluation detail data from API
 */
const loadEvaluationData = async (evaluationId: string) => {
  if (loading.value) {
    return;
  }

  loading.value = true;
  const [error, res] = await evaluation.getEvaluationDetail(evaluationId);
  loading.value = false;
  if (error) {
    return;
  }

  const data = res?.data as EvaluationDetail;
  if (!data) {
    return;
  }

  evaluationDetail.value = data;
  initializeFormData(data);

  const name = data.name;
  if (name && typeof updateTabPane === 'function') {
    updateTabPane({ name, _id: evaluationId });
  }
};

/**
 * Initializes form data from evaluation detail or creates default values
 */
const initializeFormData = (data: EvaluationDetail) => {
  if (!data) {
    const startDate = dayjs().format(DATE_TIME_FORMAT);
    const deadlineDate = dayjs().add(1, 'month').format(DATE_TIME_FORMAT);
    formState.value = {
      projectId: props.projectId || '',
      name: '',
      scope: EvaluationScope.PROJECT,
      purposes: [],
      resourceId: undefined,
      startDate,
      deadlineDate,
      date: [startDate, deadlineDate]
    };
    return;
  }

  const {
    name = '',
    scope,
    purposes = [],
    resourceId,
    startDate = '',
    deadlineDate = '',
    projectId = ''
  } = data;

  formState.value.name = name;
  formState.value.scope = (scope as any)?.value || scope || EvaluationScope.PROJECT;
  formState.value.purposes = purposes.map((p: any) => p.value || p) || [];
  formState.value.resourceId = formState.value.scope === EvaluationScope.PROJECT ? undefined : resourceId;
  formState.value.startDate = startDate;
  formState.value.deadlineDate = deadlineDate;
  formState.value.date = [startDate, deadlineDate];
  formState.value.projectId = projectId;
};

/**
 * Load project list for project selection
 */
const loadProjectList = async () => {
  if (!props.userInfo?.id) {
    return;
  }
  const userId = props.userInfo.id;
  const [error, res] = await project.getJoinedProject(userId as string);
  if (error) {
    return;
  }
  projectList.value = res?.data || [];
};

/**
 * Load module tree for module selection
 */
const loadModuleTree = async () => {
  if (!props.projectId) {
    return;
  }
  const [error, { data }] = await modules.getModuleTree({
    projectId: formState.value.projectId
  });
  if (error) {
    return;
  }
  moduleTreeData.value = [{ name: t('common.noModule'), id: '-1' }, ...travelTreeData(data || [])];
};

/**
 * Handle project selection change
 */
const handleProjectChange = (value: any) => {
  formState.value.resourceId = undefined;
  loadModuleTree();
};

/**
 * Handle plan selection change
 */
const handlePlanChange = (value: any) => {
  formState.value.resourceId = value || undefined;
};

/**
 * Handle module selection change
 */
const handleModuleChange = (selectKeys: any[]) => {
  if (!selectKeys.length) {
    formState.value.resourceId = undefined;
    return;
  }
  const moduleId = selectKeys[0];
  if (moduleId === '-1' || moduleId === undefined) {
    formState.value.resourceId = undefined;
  } else {
    formState.value.resourceId = moduleId;
  }
};

// Watch scope change to reset resource selection
watch(() => formState.value.scope, (newScope) => {
  // Load data based on scope
  if (newScope === EvaluationScope.MODULE) {
    loadModuleTree();
  } else if (newScope === EvaluationScope.PROJECT) {
    loadProjectList();
  }
});

/**
 * Cancels the edit operation and closes the tab
 */
const cancelEdit = async () => {
  if (props.data?._id) {
    deleteTabPane([props.data._id]);
  }
};

// Lifecycle hooks
onMounted(() => {
  // Load project list for project selection
  loadProjectList();

  // Initialize form data immediately on mount
  if (!props.data?.id || props.data.id === 'new-evaluation') {
    initializeFormData(null as any);
  }

  watch(() => props.data, async (newValue, oldValue) => {
    const evaluationId = newValue?.id;
    if (!evaluationId || evaluationId === 'new-evaluation') {
      initializeFormData(null as any);
      return;
    }

    const previousEvaluationId = oldValue?.id;
    if (evaluationId === previousEvaluationId) {
      return;
    }

    await loadEvaluationData(String(evaluationId));
  }, { immediate: true });
});
</script>

<template>
  <Spin :spinning="loading" class="h-full text-3 leading-5 px-5 py-5 overflow-auto">
    <div class="flex items-center space-x-2.5 mb-5">
      <Button
        type="primary"
        size="small"
        class="flex items-center space-x-1"
        @click="handleFormSubmit">
        <Icon icon="icon-dangqianxuanzhong" class="text-3.5" />
        <span>{{ t('actions.save') }}</span>
      </Button>

      <Button
        type="default"
        size="small"
        class="flex items-center space-x-1"
        @click="cancelEdit">
        <Icon icon="icon-zhongzhi2" class="text-3.5" />
        <span>{{ t('actions.cancel') }}</span>
      </Button>
    </div>

    <Form
      ref="formRef"
      :model="formState"
      :labelCol="{ style: { width: '150px' } }"
      class="max-w-242.5"
      size="small"
      layout="horizontal">
      <FormItem
        :label="t('common.name')"
        name="name"
        :rules="{ required: true, message: t('evaluation.placeholders.enterName') }">
        <Input
          v-model:value="formState.name"
          size="small"
          :maxlength="100"
          :placeholder="t('evaluation.placeholders.namePlaceholder')" />
      </FormItem>

      <FormItem
        :label="t('evaluation.columns.scope')"
        name="scope"
        :rules="{ required: true, message: t('evaluation.placeholders.selectScope') }">
        <RadioGroup
          v-model:value="formState.scope"
          class="mt-0.5"
          @change="handleScopeChange">
          <Radio
            v-for="item in scopeOptions"
            :key="item.value"
            :value="item.value">
            {{ item.label }}
          </Radio>
        </RadioGroup>
      </FormItem>

      <!-- Project Selection -->
      <FormItem
        :label="t('project.project')"
        name="projectId"
        :rules="{ required: true, message: t('evaluation.placeholders.selectProject') }">
        <Select
          v-model:value="formState.projectId"
          size="small"
          showSearch
          :options="projectList"
          :fieldNames="{ label: 'name', value: 'id' }"
          :placeholder="t('evaluation.placeholders.selectProject')"
          @change="handleProjectChange">
          <template #option="item">
            <div class="flex items-center" :title="item.name">
              <Icon icon="icon-xiangmu" class="mr-1 text-3.5" />
              <div class="truncate" style="max-width: 300px;">{{ item.name }}</div>
            </div>
          </template>
        </Select>
      </FormItem>

      <!-- Plan Selection -->
      <FormItem
        v-if="formState.scope === EvaluationScope.FUNC_PLAN"
        :label="t('common.plan')"
        name="resourceId"
        :rules="[{ required: true, message: t('common.placeholders.selectOrSearchPlan') }]">
        <Select
          v-model:value="formState.resourceId"
          size="small"
          showSearch
          :action="`${TESTER}/func/plan?projectId=${formState.projectId}&fullTextSearch=true`"
          :fieldNames="{ value: 'id', label: 'name' }"
          :placeholder="t('common.placeholders.selectOrSearchPlan')"
          @change="handlePlanChange">
          <template #option="item">
            <div class="flex items-center" :title="item.name">
              <Icon icon="icon-jihua" class="mr-1 text-3.5" />
              <div class="truncate" style="max-width: 300px;">{{ item.name }}</div>
            </div>
          </template>
        </Select>
      </FormItem>

      <!-- Module Tree Selection -->
      <FormItem
        v-if="formState.scope === EvaluationScope.MODULE"
        :label="t('module.title')"
        name="resourceId"
        :rules="{ required: true, message: t('evaluation.placeholders.selectModule') }">
        <div class="border border-gray-300 rounded p-2" style="max-height: 300px; overflow-y: auto;">
          <div
            :class="{'bg-blue-50': formState.resourceId === undefined}"
            class="flex items-center space-x-2 h-9 leading-9 pl-2 cursor-pointer hover:bg-gray-100 rounded"
            @click="handleModuleChange([undefined])">
            <Icon icon="icon-liebiaoshitu" class="text-3.5" />
            <span class="flex-1">{{ t('common.all') }}</span>
          </div>
          <Tree
            v-if="moduleTreeData.length > 0"
            :treeData="moduleTreeData"
            :selectedKeys="formState.resourceId ? [formState.resourceId] : []"
            blockNode
            defaultExpandAll
            :fieldNames="{
              children: 'children',
              title: 'name',
              key: 'id'
            }"
            @select="handleModuleChange">
            <template #title="{ name, id }">
              <div class="flex items-center space-x-2">
                <Icon v-if="id !== '-1'" icon="icon-mokuai" />
                <Icon v-else icon="icon-liebiaoshitu" class="text-3.5" />
                <span class="flex-1">{{ name }}</span>
              </div>
            </template>
          </Tree>
        </div>
      </FormItem>

      <FormItem
        :label="t('evaluation.columns.purposes')"
        name="purposes"
        :rules="{ required: true, validator: validatePurposes }">
        <div class="mt-0.5">
          <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-3">
            <div
              v-for="item in purposeOptions"
              :key="item.value"
              :class="[
                'p-4 rounded-lg border-2 cursor-pointer transition-all duration-200',
                'hover:shadow-md active:scale-98',
                isPurposeSelected(item.value as EvaluationPurpose)
                  ? 'border-blue-500 bg-blue-50 shadow-sm'
                  : 'border-gray-200 bg-white hover:border-blue-300 hover:bg-gray-50'
              ]"
              @click="togglePurpose(item.value)">
              <div class="flex items-center">
                <div
                  :class="[
                    'w-5 h-5 rounded border-2 mr-3 flex items-center justify-center flex-shrink-0 transition-all',
                    isPurposeSelected(item.value as EvaluationPurpose)
                      ? 'border-blue-500 bg-blue-500 shadow-sm'
                      : 'border-gray-300 bg-white'
                  ]">
                  <Icon
                    v-if="isPurposeSelected(item.value as EvaluationPurpose)"
                    icon="icon-duigou"
                    class="text-white text-3" />
                </div>
                <div class="flex flex-col items-start">
                  <span
                    :class="[
                      'text-3.5 leading-5 flex-1',
                      isPurposeSelected(item.value as EvaluationPurpose)
                        ? 'text-blue-700 font-semibold'
                        : 'text-gray-700'
                    ]">
                    {{ item.label }}
                  </span>
                  <span class="text-3 text-sub-content">{{ item.message }}</span>
                </div>
                
              </div>
            </div>
          </div>
        </div>
      </FormItem>

      <FormItem
        :label="t('evaluation.columns.resourceCreationTime')"
        name="date">
        <DatePicker
          v-model:value="formState.date"
          format="YYYY-MM-DD HH:mm:ss"
          :showNow="false"
          :showTime="{ format: TIME_FORMAT }"
          :placeholder="[t('common.startDate'), t('common.deadlineDate')]"
          type="date-range"
          size="small"
          class="w-full" />
      </FormItem>
    </Form>
  </Spin>
</template>

<style scoped>
:deep(.ant-form-item-label>label::after) {
  margin-right: 10px;
}

:deep(.ant-form-item-label>label) {
  font-weight: 500;
  color: #000;
}
</style>

