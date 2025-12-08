<script setup lang="ts">
import { computed, defineAsyncComponent, inject, nextTick, onMounted, ref, watch } from 'vue';
import { Button, Form, FormItem, Input, Radio, RadioGroup, Tooltip } from 'ant-design-vue';
import { notification, Spin, Dropdown, Icon, Hints, IconTask, Select } from '@xcan-angus/vue-ui';
import { utils, enumUtils, EvalWorkloadMethod, Priority } from '@xcan-angus/infra';
import { isEqual } from 'lodash-es';
import { template as templateApi } from '@/api/tester';
import { useI18n } from 'vue-i18n';
import { TestTemplateEditFormState, TestTemplateDetail } from '../types';
import { BasicProps } from '@/types/types';
import { TestTemplateType, CaseStepView, TestLayer, TestPurpose, TaskType , BugLevel} from '@/enums/enums';
import { CaseTestStep } from '@/views/test/types';
import CaseSteps from '@/views/test/case/list/CaseSteps.vue';
import TaskPriority from '@/components/task/TaskPriority.vue';


const { t } = useI18n();

// Props
const props = withDefaults(defineProps<BasicProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  data: undefined
});

// Async components
import SelectEnum from '@/components/form/enum/SelectEnum.vue';
const RichEditor = defineAsyncComponent(() => import('@/components/editor/richEditor/index.vue'));

// Injected dependencies
const updateTabPane = inject<(data: { [key: string]: any }) => void>('updateTabPane', () => ({}));
const deleteTabPane = inject<(keys: string[]) => void>('deleteTabPane', () => ({}));
const replaceTabPane = inject<(id: string, data: { [key: string]: any }) => void>('replaceTabPane', () => ({}));

// Computed properties
const isEditMode = computed(() => {
  return !!props.data?.id;
});

// Reactive data
const formRef = ref();
const loading = ref(false);
const templateDetailData = ref<TestTemplateDetail>();
const originalFormState = ref<TestTemplateEditFormState>();

// Form state initialization
const formState = ref<TestTemplateEditFormState>({
  name: '',
  templateType: TestTemplateType.TEST_PLAN,
  templateContent_issue: {
    templateType: 'ISSUE',
    name: '',
    taskType: TaskType.REQUIREMENT,
    bugLevel: BugLevel.MINOR,
    priority: Priority.MEDIUM,
    missingBug: false,
    evalWorkloadMethod: EvalWorkloadMethod.WORKING_HOURS,
    evalWorkload: 0,
    actualWorkload: 0,
    description: undefined,
  },
  templateContent_plan: {
    templateType: TestTemplateType.TEST_PLAN,
    testingScope: undefined,
    testingObjectives: undefined,
    acceptanceCriteria: undefined,
    otherInformation: undefined
  },
  templateContent_case: {
    templateType: TestTemplateType.TEST_CASE,
    testLayer: TestLayer.UI,
    testPurpose: TestPurpose.FUNCTIONAL,
    precondition: undefined,
    steps: [],
    description: undefined
  }
});

// const issueFormKeys = ['name', 'taskType', 'bugLevel', 'priority', 'missingBug', 'evalWorkloadMethod', 'evalWorkload', 'actualWorkload', 'description'];
// const planFormKeys = ['testingScope', 'testingObjectives', 'acceptanceCriteria', 'otherInformation'];
// const caseFormKeys = ['testLayer', 'testPurpose', 'precondition', 'steps', 'description'];

// Rich editor references
const scopeRichRef = ref();
const objectiveRichRef = ref();
const criteriaRichRef = ref();
const infoRichRef = ref();

// Step management
const stepDefaultValue = ref<CaseTestStep[]>([]);
const stepViewOpt = ref<{ name: string; key: string }[]>([]);
const workloadMethodOptions = ref(enumUtils.enumToMessages(EvalWorkloadMethod));

/**
 * Form validation rules
 */
const rules = {
  name: [
    { required: true, message: t('testTemplate.messages.nameRequired'), trigger: 'blur' }
  ],
  templateType: [
    { required: true, message: t('testTemplate.messages.templateTypeRequired'), trigger: 'change' }
  ]
};


/**
 * Loads template detail data for editing
 */
const loadTemplateDetail = async () => {
  if (!isEditMode.value || !props.data?.id) {
    return;
  }

  loading.value = true;
  const [error, res] = await templateApi.getTemplateList();
  loading.value = false;

  if (error) {
    notification.error(error.message || t('testTemplate.messages.loadFailed'));
    return;
  }

  const templates = res?.data || [];
  const template = templates.find((t: TestTemplateDetail) => t.id === props.data?.id);

  if (template) {
    templateDetailData.value = template;
    const templateType = template.templateType;
    const templateContent = template.templateContent || {};

    updateTabPane({name: template.name, _id: template.id});
    formState.value = {
      id: template.id,
      name: template.name,
      templateType: templateType,
    };

    // Initialize stepView and steps for test case template
    if (templateType === TestTemplateType.TEST_CASE) {

      let stepView = CaseStepView.TABLE;
      let steps: CaseTestStep[] = [];
      let testLayer = TestLayer.UI;
      let testPurpose = TestPurpose.FUNCTIONAL;
      stepView = (templateContent.stepView?.value as CaseStepView) || CaseStepView.TABLE;
      steps = templateContent.steps || [];
      testLayer = templateContent.testLayer || TestLayer.UI;
      testPurpose = templateContent.testPurpose || TestPurpose.FUNCTIONAL;
      stepDefaultValue.value = steps;
      formState.value.templateContent_case = {
        templateType: TestTemplateType.TEST_CASE,
        testLayer: testLayer,
        testPurpose: testPurpose,
        precondition: templateContent.precondition || '',
        steps: steps,
        description: templateContent.description || '',
        stepView: stepView
      };
    }

    if (templateType === TestTemplateType.TEST_PLAN) {
      formState.value.templateContent_plan = {
        templateType: TestTemplateType.TEST_PLAN,
        testingScope: templateContent.testingScope || '',
        testingObjectives: templateContent.testingObjectives || '',
        acceptanceCriteria: templateContent.acceptanceCriteria || '',
        otherInformation: templateContent.otherInformation || ''
      };
    }

    if (templateType === 'ISSUE') {
      formState.value.templateContent_issue = {
        templateType: 'ISSUE',
        name: templateContent.name || '',
        taskType: templateContent.taskType?.value || templateContent.taskType || '',
        bugLevel: templateContent.bugLevel?.value || templateContent.bugLevel || '',
        priority: templateContent.priority?.value || templateContent.priority || '',
        missingBug: templateContent.missingBug || false,
        evalWorkloadMethod: templateContent.evalWorkloadMethod?.value || EvalWorkloadMethod.WORKING_HOURS,
        evalWorkload: templateContent.evalWorkload || 0,
        actualWorkload: templateContent.actualWorkload || 0,
        description: templateContent.description || ''
      };
    }
    originalFormState.value = JSON.parse(JSON.stringify(formState.value));
  }
};

/**
 * Prepares form parameters for API submission
 */
const prepareFormParams = () => {

  const params: TestTemplateEditFormState = { ...formState.value };
  if (params.templateType === TestTemplateType.TEST_CASE) {
    params.templateContent = params.templateContent_case;
  } else if (params.templateType === TestTemplateType.TEST_PLAN) {
    params.templateContent = params.templateContent_plan;
  } else if (params.templateType === 'ISSUE') {
    params.templateContent = params.templateContent_issue;
  }
  delete params.templateContent_case;
  delete params.templateContent_plan;
  delete params.templateContent_issue;
  return params;
};

/**
 * Refreshes the template list after successful operations
 */
const refreshTemplateList = () => {
  nextTick(() => {
    updateTabPane({ _id: 'templateList', notify: utils.uuid() });
  });
};

/**
 * Handles template update operation
 */
const handleTemplateUpdate = async () => {
  const hasChanges = isEqual(originalFormState.value, formState.value);
  if (hasChanges) {
    return;
  }

  const params = prepareFormParams();
  loading.value = true;
  const [error] = await templateApi.updateTemplate({...params});
  loading.value = false;

  if (error) {
    notification.error( error.message || t('testTemplate.messages.updateFailed'));
    return;
  }

  notification.success(t('actions.tips.saveSuccess'));

  const id = params.id;
  const name = params.name;
  if (id) {
    updateTabPane({ _id: id, name });
  }
  if (templateDetailData.value) {
    templateDetailData.value.name = name;
  }
};

/**
 * Handles template creation operation
 */
const handleTemplateCreation = async () => {
  const params = prepareFormParams();
  loading.value = true;
  const [error] = await templateApi.addTemplate({...params});
  loading.value = false;

  if (error) {
    notification.error({
      message: t('common.error'),
      description: error.message || t('testTemplate.messages.createFailed')
    });
    return;
  }

  notification.success(t('actions.tips.addSuccess'));
  handleCancel();
};

/**
 * Handles form submission with validation
 */
const handleFormSubmit = async () => {
  formRef.value.validate().then(async () => {
    if (!isEditMode.value) {
      await handleTemplateCreation();
    } else {
      await handleTemplateUpdate();
    }
    refreshTemplateList();
  }).catch(() => {
    // Validation failed
  });
};

/**
 * Handles cancel action
 */
const handleCancel = () => {
  props.data?._id && deleteTabPane([props.data?._id]);
};

/**
 * Change step view mode
 * @param param - Object containing key
 */
const handleStepViewChange = ({ key }: { key: string }) => {
  if (formState.value.templateContent) {
    formState.value.templateContent.stepView = key as CaseStepView;
  }
};

/**
 * Load enum options for step view
 */
const loadEnumOptions = () => {
  const data = enumUtils.enumToMessages(CaseStepView);
  stepViewOpt.value = data.map(i => ({ name: i.message, key: i.value }));
};

// Watch template type changes to initialize/clean up fields
watch(() => formState.value.templateType, (newType) => {
  if (formState.value.templateContent) {
    formState.value.templateContent.templateType = newType as TestTemplateType;
  }
});

// Lifecycle hooks
onMounted(() => {
  loadEnumOptions();
  if (isEditMode.value) {
    loadTemplateDetail();
  } else {
    // Initialize default step view for new test case template
    if (formState.value.templateContent) {
      formState.value.templateContent.stepView = CaseStepView.TABLE;
      formState.value.templateContent.steps = [];
    }
  }
});
</script>

<template>
  <div class="flex flex-col h-full overflow-auto px-5 py-5">
    <Spin :spinning="loading">
      <Form
        ref="formRef"
        :model="formState"
        :rules="rules"
        layout="vertical"
        class="template-edit-form">
        <div class="flex items-center justify-between mb-4">
          <div class="text-4 font-semibold">
            {{ isEditMode ? t('testTemplate.actions.editTemplate') : t('testTemplate.actions.addTemplate') }}
          </div>
          <div class="flex space-x-2">
            <Button @click="handleCancel" size="small">
              {{ t('actions.cancel') }}
            </Button>
            <Button type="primary" @click="handleFormSubmit" size="small" >
              {{ t('actions.save') }}
            </Button>
          </div>
        </div>

        <!-- Basic Information -->
        <div class="mb-6">
          <div class="text-3.5 font-semibold mb-4">{{ t('common.basicInfo') }}</div>
          <FormItem name="name" :label="t('common.name')">
            <Input
              v-model:value="formState.name"
              :placeholder="t('testTemplate.placeholders.namePlaceholder')"
              :maxlength="100" />
          </FormItem>

          <FormItem name="templateType" :label="t('testTemplate.columns.templateType')">
            <RadioGroup v-model:value="formState.templateType">
              <Radio value="ISSUE">
                问题
              </Radio>
              <Radio :value="TestTemplateType.TEST_PLAN">
                {{ t('xcm.enum.TestTemplateType.TEST_PLAN') }}
              </Radio>
              <Radio :value="TestTemplateType.TEST_CASE">
                {{ t('xcm.enum.TestTemplateType.TEST_CASE') }}
              </Radio>
            </RadioGroup>
          </FormItem>
        </div>

        <!-- Template Content -->
        <div>
          <div class="text-3.5 font-semibold mb-4">{{ t('testTemplate.templateContent') }}</div>

          <!-- Test Plan Template Content -->
          <template v-if="formState.templateType === TestTemplateType.TEST_PLAN && formState.templateContent_plan">
            <FormItem :label="t('testTemplate.columns.testingScope')">
              <RichEditor
                ref="scopeRichRef"
                v-model:value="formState.templateContent_plan.testingScope"
                :options="{placeholder: t('testPlan.placeholders.testingScopePlaceholder')}" />
            </FormItem>

            <FormItem :label="t('testTemplate.columns.testingObjectives')">
              <RichEditor
                ref="objectiveRichRef"
                v-model:value="formState.templateContent_plan.testingObjectives"
                :options="{placeholder: t('testPlan.placeholders.testingObjectivesPlaceholder')}" />
            </FormItem>

            <FormItem :label="t('testTemplate.columns.acceptanceCriteria')">
              <RichEditor
                ref="criteriaRichRef"
                v-model:value="formState.templateContent_plan.acceptanceCriteria"
                :options="{placeholder: t('testPlan.placeholders.acceptanceCriteriaPlaceholder')}" />
            </FormItem>

            <FormItem :label="t('testTemplate.columns.otherInformation')">
              <RichEditor
                ref="infoRichRef"
                v-model:value="formState.templateContent_plan.otherInformation"
                :options="{placeholder: t('testPlan.placeholders.otherInformationPlaceholder')}" />
            </FormItem>
          </template>

          <!-- Test Case Template Content -->
          <template v-if="formState.templateType === TestTemplateType.TEST_CASE && formState.templateContent_case">

            <div class="flex space-x-2">
              <FormItem
                :name="['templateContent_case', 'testLayer']"
                :label="t('common.testLayer')"
                class="flex-1"
                :rules="[{ required: true, message: t('common.placeholders.selectTestLayer') }]">
                <SelectEnum
                  v-model:value="formState.templateContent_case.testLayer"
                  enumKey="TestLayer"
                  size="small" />
              </FormItem>

              <FormItem
                :name="['templateContent_case', 'testPurpose']"
                :label="t('common.testPurpose')"
                class="flex-1"
                :rules="[{ required: true, message: t('common.placeholders.selectTestPurpose') }]">
                  <SelectEnum
                    v-model:value="formState.templateContent_case.testPurpose"
                    enumKey="TestPurpose"
                    size="small" />
                </FormItem>
            </div>
            <FormItem :label="t('common.precondition')">
              <RichEditor
                v-model:value="formState.templateContent_case.precondition"
                :options="{placeholder: t('testCase.messages.enterPrecondition')}" />
            </FormItem>

            <FormItem>
              <template #label>
                <div class="text-3 flex space-x-2 items-center">
                  <span>{{ t('common.testSteps') }}</span>

                  <Dropdown
                    :value="[formState.templateContent_case.stepView || CaseStepView.TABLE]"
                    :menuItems="stepViewOpt"
                    @click="handleStepViewChange">
                    <span class="text-theme-special">{{ t('testCase.actions.switchType') }}
                      <Icon icon="icon-xiajiantou" /></span>
                  </Dropdown>
                  <Hints :text="t('testCase.messages.testStepsHint')" class="flex-1" />
                </div>
              </template>

              <CaseSteps
                v-model:value="formState.templateContent_case.steps"
                :defaultValue="stepDefaultValue"
                :stepView="formState.templateContent_case.stepView"
                :readonly="false"
                :showOutBorder="true" />
            </FormItem>

            <FormItem :label="t('common.description')">
              <RichEditor
                v-model:value="formState.templateContent_case.description"
                :options="{placeholder: t('common.placeholders.inputDescription30')}" />
            </FormItem>
          </template>

          <!-- Issue Template Content -->
          <template v-if="formState.templateType === 'ISSUE' && formState.templateContent_issue">
            <FormItem
              :name="['templateContent_issue', 'name']"
              :label="t('common.name')"
              :rules="{ required: true, message: t('common.placeholders.inputName2') }">
              <Input
                v-model:value="formState.templateContent_issue.name"
                trim
                :maxlength="200"
                :placeholder="t('common.placeholders.inputName2')" />
            </FormItem>

            <div class="flex space-x-4">
              <FormItem
                :name="['templateContent_issue', 'taskType']"
                class="flex-1/2"
                required>
                <template #label>
                  {{ t('common.type') }}
                </template>
                <SelectEnum
                  v-model:value="formState.templateContent_issue.taskType"
                  :allowClear="false"
                  internal
                  enumKey="TaskType"
                  :placeholder="t('common.placeholders.selectIssueType')">
                  <template #option="record">
                    <div class="flex items-center">
                      <IconTask :value="record.value as TaskType" class="text-4 flex-shrink-0" />
                      <span class="ml-1">{{ record.label }}</span>
                    </div>
                  </template>
                </SelectEnum>
              </FormItem>

              <FormItem
                :name="['templateContent_issue', 'priority']"
                :label="t('common.priority')"
                class="flex-1/2"
                required>
                <SelectEnum
                  v-model:value="formState.templateContent_issue.priority"
                  :allowClear="false"
                  internal
                  enumKey="Priority"
                  :placeholder="t('common.placeholders.selectPriority')">
                  <template #option="record">
                    <TaskPriority :value="record" />
                  </template>
                </SelectEnum>
              </FormItem>
            </div>

            <template v-if="formState.templateContent_issue?.taskType === TaskType.BUG">
              <div class="flex space-x-4">
                <FormItem
                  :name="['templateContent_issue', 'bugLevel']"
                  :label="t('common.bugLevel')"
                  class="flex-1/2">
                  <SelectEnum
                    v-model:value="formState.templateContent_issue.bugLevel"
                    enumKey="BugLevel"
                    :allowClear="false"
                    :lazy="false" />
                </FormItem>

                <FormItem
                  :name="['templateContent_issue', 'missingBug']"
                  :label="t('common.escapedBug')"
                  class="flex-1/2">
                  <Select
                    :value="(formState.templateContent_issue.missingBug as any)"
                    :options="[
                      { value: (true as any), label: t('status.yes') },
                      { value: (false as any), label: t('status.no') }
                    ]">
                  </Select>
                </FormItem>
              </div>
            </template>
            <FormItem
              :label="t('common.evalWorkloadMethod')"
              :name="['templateContent_issue', 'evalWorkloadMethod']">
              <RadioGroup v-model:value="formState.templateContent_issue.evalWorkloadMethod">
                <Radio
                  v-for="item in workloadMethodOptions"
                  :key="item.value"
                  :value="item.value">
                  {{ item.message }}
                </Radio>
              </RadioGroup>
              <Tooltip
                placement="right"
                arrowPointAtCenter
                :overlayStyle="{ 'max-width': '400px' }">
                <template #title>
                  <div>{{ t('sprint.tips.evalWorkloadMethodTip') }}</div>
                </template>
                <Icon icon="icon-tishi1" class="text-tips text-3.5 cursor-pointer" />
              </Tooltip>
            </FormItem>

            <div class="flex space-x-4">
              <FormItem
                class="flex-1/2"
                :name="['templateContent_issue', 'evalWorkload']">
                <template #label>
                  {{ t('common.evalWorkload') }}
                </template>
                <Input
                  v-model:value="formState.templateContent_issue.evalWorkload"
                  size="small"
                  dataType="float"
                  trimAll
                  :min="0.1"
                  :max="1000"
                  :placeholder="t('common.placeholders.workloadRange')" />
              </FormItem>
              <formItem
                class="flex-1/2"
                :name="['templateContent_issue', 'actualWorkload']">
                <template #label>
                  {{ t('common.actualWorkload') }}
                </template>
                <Input
                  v-model:value="formState.templateContent_issue.actualWorkload"
                  size="small"
                  dataType="float"
                  trimAll
                  :min="0.1"
                  :max="1000"
                  :placeholder="t('common.placeholders.workloadRange')" />
              </FormItem>
              
            </div>
          </template>
        </div>
      </Form>
    </Spin>
  </div>
</template>

<style scoped>
.template-edit-form {
  max-width: 1200px;
}
</style>


