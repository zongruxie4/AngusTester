<script setup lang="ts">
import {  defineAsyncComponent, inject, onMounted, ref, watch } from 'vue';
import { Button, Form, FormItem } from 'ant-design-vue';
import { notification, Spin, Hints } from '@xcan-angus/vue-ui';
import { enumUtils } from '@xcan-angus/infra';
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

const RichEditor = defineAsyncComponent(() => import('@/components/editor/richEditor/index.vue'));

// Injected dependencies
const updateTabPane = inject<(data: { [key: string]: any }) => void>('updateTabPane', () => ({}));
const deleteTabPane = inject<(keys: string[]) => void>('deleteTabPane', () => ({}));

// Computed propertie

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
 * Loads template detail data for editing
 */
const loadTemplateDetail = async () => {
  if (!props.data?.id) {
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

    updateTabPane({name: template.name, _id: props.data?._id});
    
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
      stepDefaultValue.value = steps;
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
 * Handles cancel action
 */
const handleCancel = () => {
  props.data?._id && deleteTabPane([props.data?._id]);
};

/**
 * Load enum options for step view
 */
const loadEnumOptions = () => {
  const data = enumUtils.enumToMessages(CaseStepView);
  stepViewOpt.value = data.map(i => ({ name: i.message, key: i.value }));
};


// Lifecycle hooks
onMounted(() => {
  loadEnumOptions();
  loadTemplateDetail();
});
</script>

<template>
  <div class="flex flex-col h-full overflow-auto px-5 py-5">
    <Spin :spinning="loading">
      <Form
        ref="formRef"
        :model="formState"
        :colon="true"
        labelAlign="left"
        layout="horizontal"
        class="template-edit-form">
        <div class="flex items-center justify-between mb-4">
          <div class="text-4 font-semibold">
            查看模板
          </div>
          <div class="flex space-x-2">
            <Button @click="handleCancel" size="small">
              {{ t('actions.close') }}
            </Button>
          </div>
        </div>

        <!-- Basic Information -->
        <div class="mb-6">
          <div class="text-3.5 font-semibold mb-4">{{ t('common.basicInfo') }}</div>
          <FormItem name="name" :label="t('common.name')">
            {{formState.name}}
          </FormItem>

          <FormItem name="templateType" :label="t('testTemplate.columns.templateType')">
            {{ enumUtils.getEnumDescription(TestTemplateType, formState.templateType) }}
          </FormItem>
        </div>

        <!-- Template Content -->
        <div>
          <div class="text-3.5 font-semibold mb-4">{{ t('testTemplate.templateContent') }}</div>
          
          <!-- Test Plan Template Content -->
          <template v-if="formState.templateType === TestTemplateType.TEST_PLAN">
            <FormItem :label="t('testTemplate.columns.testingScope')">
            </FormItem>
            <RichEditor
              ref="scopeRichRef"
              v-model:value="formState.templateContent.testingScope"
              mode="view"
              class="-mt-4"
              :options="{placeholder: t('testPlan.placeholders.testingScopePlaceholder')}" />

            <FormItem :label="t('testTemplate.columns.testingObjectives')" class="mt-4">
            </FormItem>
            <RichEditor
                ref="objectiveRichRef"
                v-model:value="formState.templateContent.testingObjectives"
                mode="view"
                 class="-mt-4"
                :options="{placeholder: t('testPlan.placeholders.testingObjectivesPlaceholder')}" />

            <FormItem :label="t('testTemplate.columns.acceptanceCriteria')" class="mt-4">
            </FormItem>
            <RichEditor
                ref="criteriaRichRef"
                v-model:value="formState.templateContent.acceptanceCriteria"
                mode="view"
                 class="-mt-4"
                :options="{placeholder: t('testPlan.placeholders.acceptanceCriteriaPlaceholder')}" />

            <FormItem :label="t('testTemplate.columns.otherInformation')" class="mt-4">
            </FormItem>
            <RichEditor
                ref="infoRichRef"
                v-model:value="formState.templateContent.otherInformation"
                mode="view"
                 class="-mt-4"
                :options="{placeholder: t('testPlan.placeholders.otherInformationPlaceholder')}" />
          </template>

          <!-- Test Case Template Content -->
          <template v-if="formState.templateType === TestTemplateType.TEST_CASE">

            <FormItem
              :name="['templateContent', 'testLayer']"
              :label="t('common.testLayer')"
              class="flex-1">

              {{ enumUtils.getEnumDescription(TestLayer, formState.templateContent.testLayer) }}
            </FormItem>

            <FormItem
              :name="['templateContent', 'testPurpose']"
              :label="t('common.testPurpose')"
              class="flex-1">
              {{ enumUtils.getEnumDescription(TestPurpose, formState.templateContent.testPurpose) }}
              </FormItem>
            <FormItem :label="t('common.precondition')">
              <RichEditor
                v-model:value="formState.templateContent.precondition"
                mode="view"
                :options="{placeholder: t('testCase.messages.enterPrecondition')}" />
            </FormItem>

            <FormItem  :colon="false">
              <template #label>
                <div class="text-3 flex space-x-2 items-center">
                  <span>{{ t('common.testSteps') }}</span>
                  <Hints :text="t('testCase.messages.testStepsHint')" class="flex-1" />
                </div>
              </template>
            </FormItem>
            <CaseSteps
                v-model:value="formState.templateContent.steps"
                mode="view"
                class="-mt-4"
                :defaultValue="stepDefaultValue"
                :stepView="formState.templateContent.stepView"
                :readonly="true"
                :showOutBorder="true" />

            <FormItem class="mt-4" :label="t('common.description')">
              <RichEditor
                v-model:value="formState.templateContent.description"
                mode="view"
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

