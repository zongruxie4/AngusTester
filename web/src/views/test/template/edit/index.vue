<script setup lang="ts">
import { computed, defineAsyncComponent, inject, nextTick, onMounted, ref, watch } from 'vue';
import { Button, Form, FormItem, Input, Radio, RadioGroup } from 'ant-design-vue';
import { notification, Spin, Dropdown, Icon, Hints } from '@xcan-angus/vue-ui';
import { utils, enumUtils } from '@xcan-angus/infra';
import { isEqual } from 'lodash-es';
import { testTemplate } from '@/api/tester';
import { useI18n } from 'vue-i18n';
import { TestTemplateEditFormState, TestTemplateDetail } from '../types';
import { BasicProps } from '@/types/types';
import { TestTemplateType, CaseStepView, TestLayer, TestPurpose } from '@/enums/enums';
import { CaseTestStep } from '@/views/test/types';
import CaseSteps from '@/views/test/case/list/CaseSteps.vue';

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
  templateContent: {
    templateType: TestTemplateType.TEST_PLAN,
    testingScope: '',
    testingObjectives: '',
    acceptanceCriteria: '',
    otherInformation: '',
    stepView: CaseStepView.TABLE,
    steps: [],
    testLayer: TestLayer.UI,
    testPurpose: TestPurpose.FUNCTIONAL,
    description: '',
    precondition: ''
  }
});

// Rich editor references
const scopeRichRef = ref();
const objectiveRichRef = ref();
const criteriaRichRef = ref();
const infoRichRef = ref();

// Step management
const stepDefaultValue = ref<CaseTestStep[]>([]);
const stepViewOpt = ref<{ name: string; key: string }[]>([]);

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
  const [error, res] = await testTemplate.getTemplateList();
  loading.value = false;

  if (error) {
    notification.error(error.message || t('testTemplate.messages.loadFailed'));
    return;
  }

  const templates = res?.data || [];
  const template = templates.find((t: TestTemplateDetail) => t.id === props.data?.id);

  if (template) {
    templateDetailData.value = template;
    const templateType = template.templateType as TestTemplateType;
    const templateContent = template.templateContent || {};

    updateTabPane({name: template.name, _id: template.id});
    
    // Initialize stepView and steps for test case template
    let stepView = CaseStepView.TABLE;
    let steps: CaseTestStep[] = [];
    let testLayer = TestLayer.UI;
    let testPurpose = TestPurpose.FUNCTIONAL;
    if (templateType === TestTemplateType.TEST_CASE) {
      stepView = (templateContent.stepView?.value as CaseStepView) || CaseStepView.TABLE;
      steps = templateContent.steps || [];
      testLayer = templateContent.testLayer || TestLayer.UI;
      testPurpose = templateContent.testPurpose || TestPurpose.FUNCTIONAL;
    }

    formState.value = {
      id: template.id,
      name: template.name,
      templateType: templateType,
      templateContent: {
        templateType: templateType,
        testingScope: templateContent.testingScope || '',
        testingObjectives: templateContent.testingObjectives || '',
        acceptanceCriteria: templateContent.acceptanceCriteria || '',
        otherInformation: templateContent.otherInformation || '',
        precondition: templateContent.precondition || '',
        stepView: stepView,
        steps: steps,
        description: templateContent.description || '',
        testLayer: testLayer,
        testPurpose: testPurpose
      }
    };
    originalFormState.value = JSON.parse(JSON.stringify(formState.value));
  }
};

/**
 * Prepares form parameters for API submission
 */
const prepareFormParams = () => {
  const params: TestTemplateEditFormState = { ...formState.value };
  
  // Ensure templateContent has templateType
  if (params.templateContent) {
    params.templateContent.templateType = params.templateType as TestTemplateType;
    if (params.templateType === TestTemplateType.TEST_CASE) {
      delete params.templateContent.testingScope;
      delete params.templateContent.testingObjectives;
      delete params.templateContent.acceptanceCriteria;
      delete params.templateContent.otherInformation;
    } else {
      delete params.templateContent.precondition;
      delete params.templateContent.stepView;
      delete params.templateContent.steps;
      delete params.templateContent.description;
      delete params.templateContent.testLayer;
      delete params.templateContent.testPurpose;
    }
  }
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
  const [error] = await testTemplate.updateTemplate({...params});
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
  const [error] = await testTemplate.addTemplate({...params});
  loading.value = false;
  
  if (error) {
    notification.error({
      message: t('common.error'),
      description: error.message || t('testTemplate.messages.createFailed')
    });
    return;
  }

  notification.success(t('actions.tips.addSuccess'));
  // const currentTabId = props.data?._id;
  // const newTemplateId = res?.data?.id;
  // const name = params.name;
  // if (newTemplateId && currentTabId) {
  //   replaceTabPane(currentTabId, {
  //     _id: newTemplateId,
  //     uiKey: newTemplateId,
  //     name,
  //     data: { _id: newTemplateId, id: newTemplateId }
  //   });
  // }
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
    
    if (newType === TestTemplateType.TEST_CASE) {
      // Initialize step view and steps for test case template
      if (!formState.value.templateContent.stepView) {
        formState.value.templateContent.stepView = CaseStepView.TABLE;
      }
      if (!formState.value.templateContent.steps) {
        formState.value.templateContent.steps = [];
        stepDefaultValue.value = [];
      }
    } else {
      // Clear test case specific fields for test plan template (but keep them in the object)
      formState.value.templateContent.stepView = undefined;
      formState.value.templateContent.steps = undefined;
      formState.value.templateContent.precondition = undefined;
      formState.value.templateContent.description = undefined;
    }
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
          <template v-if="formState.templateType === TestTemplateType.TEST_PLAN">
            <FormItem :label="t('testTemplate.columns.testingScope')">
              <RichEditor
                ref="scopeRichRef"
                v-model:value="formState.templateContent.testingScope"
                :options="{placeholder: t('testPlan.placeholders.testingScopePlaceholder')}" />
            </FormItem>

            <FormItem :label="t('testTemplate.columns.testingObjectives')">
              <RichEditor
                ref="objectiveRichRef"
                v-model:value="formState.templateContent.testingObjectives"
                :options="{placeholder: t('testPlan.placeholders.testingObjectivesPlaceholder')}" />
            </FormItem>

            <FormItem :label="t('testTemplate.columns.acceptanceCriteria')">
              <RichEditor
                ref="criteriaRichRef"
                v-model:value="formState.templateContent.acceptanceCriteria"
                :options="{placeholder: t('testPlan.placeholders.acceptanceCriteriaPlaceholder')}" />
            </FormItem>

            <FormItem :label="t('testTemplate.columns.otherInformation')">
              <RichEditor
                ref="infoRichRef"
                v-model:value="formState.templateContent.otherInformation"
                :options="{placeholder: t('testPlan.placeholders.otherInformationPlaceholder')}" />
            </FormItem>
          </template>

          <!-- Test Case Template Content -->
          <template v-if="formState.templateType === TestTemplateType.TEST_CASE">

            <div class="flex space-x-2">
              <FormItem
                name="testLayer"
                :label="t('common.testLayer')"
                class="flex-1"
                :rules="[{ required: true, message: t('common.placeholders.selectTestLayer') }]">
                <SelectEnum
                  v-model:value="formState.templateContent.testLayer"
                  enumKey="TestLayer"
                  size="small" />
              </FormItem>

              <FormItem
                name="testPurpose"
                :label="t('common.testPurpose')"
                class="flex-1"
                :rules="[{ required: true, message: t('common.placeholders.selectTestPurpose') }]">
                  <SelectEnum
                    v-model:value="formState.templateContent.testPurpose"
                    enumKey="TestPurpose"
                    size="small" />
                </FormItem>
            </div>
            <FormItem :label="t('common.precondition')">
              <RichEditor
                v-model:value="formState.templateContent.precondition"
                :options="{placeholder: t('testCase.messages.enterPrecondition')}" />
            </FormItem>

            <FormItem>
              <template #label>
                <div class="text-3 flex space-x-2 items-center">
                  <span>{{ t('common.testSteps') }}</span>

                  <Dropdown
                    :value="[formState.templateContent.stepView || CaseStepView.TABLE]"
                    :menuItems="stepViewOpt"
                    @click="handleStepViewChange">
                    <span class="text-theme-special">{{ t('testCase.actions.switchType') }}
                      <Icon icon="icon-xiajiantou" /></span>
                  </Dropdown>
                  <Hints :text="t('testCase.messages.testStepsHint')" class="flex-1" />
                </div>
              </template>

              <CaseSteps
                v-model:value="formState.templateContent.steps"
                :stepView="formState.templateContent.stepView"
                :defaultValue="stepDefaultValue"
                :readonly="false"
                :showOutBorder="true" />
            </FormItem>

            <FormItem :label="t('common.description')">
              <RichEditor
                v-model:value="formState.templateContent.description"
                :options="{placeholder: t('common.placeholders.inputDescription30')}" />
            </FormItem>
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

