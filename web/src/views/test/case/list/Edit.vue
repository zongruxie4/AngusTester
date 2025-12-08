<script lang="ts" setup>
import { computed, inject, onMounted, ref, watch, Ref, defineAsyncComponent, onBeforeUnmount } from 'vue';
import {
  DatePicker, Hints, Icon, IconTask, Input, Modal, notification, Select, Spin, Dropdown
} from '@xcan-angus/vue-ui';
import { Button, Form, FormItem, Tooltip, TreeSelect, Upload } from 'ant-design-vue';
import type { Rule } from 'ant-design-vue/es/form';
import {
  EvalWorkloadMethod, Priority, SearchCriteria, enumUtils, TESTER,
  upload, localStore, utils, appContext
} from '@xcan-angus/infra';
import { CaseStepView, SoftwareVersionStatus, TestLayer, TestPurpose, TestTemplateType } from '@/enums/enums';
import dayjs from 'dayjs';
import { testCase, modules, project } from '@/api/tester';
import { DATE_TIME_FORMAT, TIME_FORMAT, MAX_FILE_SIZE_MB, UPLOAD_TEST_FILE_KEY } from '@/utils/constant';

import { useI18n } from 'vue-i18n';
import { CaseDetail } from '@/views/test/types';
import { CaseEditState } from './types';

import TaskPriority from '@/components/task/TaskPriority.vue';
import RichEditor from '@/components/editor/richEditor/index.vue';
import SelectEnum from '@/components/form/enum/SelectEnum.vue';
import CaseSteps from '@/views/test/case/list/CaseSteps.vue';

const DropdownTemplateSelect = defineAsyncComponent(() => import('@/components/test/DropdownTemplateSelect.vue'));

const { t } = useI18n();

// Component props interface
interface Props {
  visible: boolean;
  editCase?: CaseDetail;
  moduleId?: number;
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  editCase: undefined
});

// Component emits
const emits = defineEmits<{(e: 'update:visible', value: boolean): void; (e: 'update', id?: string): void }>();

// Basic state management
const userInfo = ref(appContext.getUser());
const projectId = inject<Ref<string>>('projectId', ref(''));
const addCaseSizeKey = `${userInfo.value?.id || ''}${projectId.value}addFuncCaseSize`;

// Form state management
const formRef = ref();

// Extended form state interface to include all form fields
const caseFormData = ref<CaseEditState>({
  testLayer: TestLayer.UI,
  testPurpose: TestPurpose.FUNCTIONAL,
  attachments: [],
  deadlineDate: '',
  description: '',
  evalWorkload: '',
  actualWorkload: '',
  moduleId: undefined,
  name: '',
  planId: undefined,
  precondition: '',
  priority: Priority.MEDIUM,
  steps: [],
  tagIds: [],
  testerId: userInfo.value?.id?.toString(),
  refCaseIds: [],
  refTaskIds: [],
  developerId: undefined,
  softwareVersion: undefined,
  stepView: CaseStepView.TABLE,
  caseType: 'FUNC'
});

const originalFormData = ref<CaseEditState>();
const currentCaseDetail = ref<CaseDetail>();

/**
 * Load case details for editing
 */
const loadCaseDetails = async () => {
  if (!props.editCase?.id) {
    return;
  }
  const [error, { data }] = await testCase.getCaseDetail(props.editCase.id);
  if (error) {
    return;
  }
  Object.keys(caseFormData.value).forEach((key) => {
    if (key === 'tagIds') {
      caseFormData.value.tagIds = (data.tags || []).map(item => {
        return item.id;
      });
    } else if (key === 'attachments') {
      caseFormData.value[key] = data[key] || [];
    } else if (key === 'refTaskIds') {
      caseFormData.value.refTaskIds = (data.refTaskInfos || []).map(item => {
        return item.id;
      });
    } else if (key === 'refCaseIds') {
      caseFormData.value.refCaseIds = (data.refCaseInfos || []).map(item => {
        return item.id;
      });
    } else if (key === 'actualWorkload') {
      caseFormData.value.actualWorkload = data?.actualWorkload || data?.evalWorkload;
    } else if (key === 'priority') {
      caseFormData.value.priority = data?.priority?.value || Priority.MEDIUM;
    } else if (key === 'precondition') {
      caseFormData.value[key] = getJson(data?.[key]) || '';
    } else if (key === 'stepView') {
      caseFormData.value[key] = data?.[key]?.value || 'TABLE';
    } else {
      if (key === 'moduleId') {
        if (data?.[key] === '-1') {
          data[key] = undefined;
        }
      }
      (caseFormData.value as any)[key] = data?.[key];
    }
  });

  currentCaseDetail.value = data;
  stepDefaultValue.value = data.steps;
  originalFormData.value = JSON.parse(JSON.stringify(caseFormData.value));
};

/**
 * Parse JSON value for rich text content
 * @param value - Value to parse
 * @returns Parsed JSON string
 */
const getJson = (value: string): string | undefined => {
  if (!value) {
    return undefined;
  }
  try {
    const result = JSON.parse(value);
    if (typeof result === 'object') {
      return value;
    }
    return JSON.stringify([{ insert: value }]);
  } catch {
    return JSON.stringify([{ insert: value }]);
  }
};

/**
 * Close the modal
 */
const close = () => {
  emits('update:visible', false);
};

const stepsRef = ref();

/**
 * Reset form to initial state
 * @param type - Reset type ('save' for save and continue)
 */
const resetForm = (type?: 'save') => {
  formRef.value.resetFields(Object.keys(caseFormData.value as any).filter(item => item !== 'planId' && item !== 'deadlineDate' && item !== 'testLayer' && item !== 'testPurpose'));
  caseFormData.value.refCaseIds = [];
  caseFormData.value.refTaskIds = [];
  if (type !== 'save') {
    caseFormData.value.steps = [];
    caseFormData.value.description = '';
    stepDefaultValue.value = [];
    // Set module ID if provided and valid, otherwise undefined to show placeholder
    if (props.moduleId && +props.moduleId > 0) {
      caseFormData.value.moduleId = props.moduleId;
    } else {
      caseFormData.value.moduleId = undefined;
    }
  }
  formRef.value.clearValidate();
};

// Form submission functions
const saveType = ref('save');

/**
 * Save form data
 * @param type - Save type ('save' or 'add')
 */
const save = (type: 'save' | 'add') => {
  if (isLoading.value) {
    return;
  }
  saveType.value = type;
  let validationRuleKeys = [
    'name',
    'planId',
    'testerId',
    'deadlineDate',
    'testLayer',
    'testPurpose',
    'description'
  ];

  if (caseFormData.value.actualWorkload) {
    validationRuleKeys = [...validationRuleKeys, 'evalWorkload'];
  }

  formRef.value.validate(validationRuleKeys)
    .then(async () => {
      if (props.editCase) {
        await saveEditedCase();
      } else {
        await saveNewCase();
      }
    });
};

/**
 * Prepare form parameters for API calls
 * @returns Cleaned form parameters
 */
const prepareFormParams = () => {
  const params = JSON.parse(JSON.stringify(caseFormData.value));
  if (!params.tagIds?.length) {
    delete params.tagIds;
  }
  if (!params.refTaskIds?.length) {
    delete params.refTaskIds;
  }
  if (!params.refCaseIds?.length) {
    delete params.refCaseIds;
  }
  if (!params.attachments?.length) {
    delete params.attachments;
  }

  if (!params.description) {
    delete params.description;
  }
  if (!params.precondition) {
    delete params.precondition;
  }
  if (!params.evalWorkload) {
    delete params.evalWorkload;
  }

  if (!props.editCase || !params.actualWorkload) {
    delete params.actualWorkload;
  }
  return params;
};


const handleSelectTemplate = (templateData: {templateContent: {description: string, testLayer: TestLayer, testPurpose: TestPurpose, precondition: string, stepView: EnumMessage<CaseStepView>, steps: any[],}}) => {
  const {description, testLayer, testPurpose, precondition, stepView, steps} = templateData.templateContent;
  caseFormData.value.description = description;
  caseFormData.value.testLayer = testLayer;
  caseFormData.value.testPurpose = testPurpose;
  caseFormData.value.precondition = precondition;
  caseFormData.value.stepView = stepView?.value || CaseStepView.TABLE;
  caseFormData.value.steps = steps;
  stepDefaultValue.value = steps;
};

const isLoading = ref(false);

/**
 * Save edited case
 */
const saveEditedCase = async () => {
  if (!props.editCase?.id) {
    return;
  }
  if (utils.deepCompare(originalFormData.value, caseFormData.value)) {
    emits('update:visible', false);
    return;
  }
  const params = prepareFormParams();
  isLoading.value = true;
  const [error] = await testCase.putCase([{ id: props.editCase.id, ...params }]);
  isLoading.value = false;
  if (error) {
    return;
  }
  notification.success(t('actions.tips.modifySuccess'));
  emits('update:visible', false);
  emits('update', props.editCase.id.toString());
};

/**
 * Save new case
 */
const saveNewCase = async () => {
  const params = prepareFormParams();
  isLoading.value = true;
  const [error] = await testCase.addCase([params]);
  isLoading.value = false;
  if (error) {
    return;
  }
  notification.success(t('actions.tips.addSuccess'));

  emits('update');
  if (saveType.value === 'save') {
    emits('update:visible', false);
    resetForm('save');
  }
};

/**
 * Handle file upload
 * @param param - Upload parameter containing file
 */
const handleFileUpload = async ({ file }: { file: File }) => {
  if (!caseFormData.value.attachments || caseFormData.value.attachments.length >= 5 || isLoading.value) {
    return;
  }

  if (file.size! > 1024 * 1024 * MAX_FILE_SIZE_MB) {
    notification.error(t('testCase.messages.fileTooLarge'));
    return;
  }

  isLoading.value = true;
  const [error, { data = [] }] = await upload(file, { bizKey: UPLOAD_TEST_FILE_KEY });
  isLoading.value = false;
  if (error) {
    return;
  }

  if (data && data.length > 0) {
    const attachments = data?.map(item => ({ name: item.name, url: item.url }));
    caseFormData.value.attachments.push(...attachments);
  }
};

/**
 * Delete uploaded file
 * @param index - File index to delete
 */
const removeAttachment = (index: number) => {
  caseFormData.value?.attachments?.splice(index, 1);
};

// Step default values
const stepDefaultValue = ref<{ expectedResult: string; step: string; }[]>([]);

/**
 * Check if date should be disabled
 * @param current - Current date
 * @returns Whether date should be disabled
 */
const disabledDate = (current: dayjs.Dayjs) => {
  if (props.editCase) {
    return false;
  }
  const today = dayjs().startOf('day');
  return current.isBefore(today);
};

/**
 * Validate deadline date
 * @param rule - Validation rule
 * @param value - Date value to validate
 * @returns Promise with validation result
 */
const validateDate = async (_rule: Rule, value: string) => {
  if (!value) {
    return Promise.reject(new Error(t('common.placeholders.selectDeadline')));
  } else if (props.editCase) {
    return Promise.resolve();
  } else if (dayjs(value).isBefore(dayjs(), 'minute')) {
    return Promise.reject(new Error(t('testCase.messages.deadlineMustBeFuture')));
  } else {
    return Promise.resolve();
  }
};

/**
 * Assign current user as tester
 */
const assignTesterToMe = () => {
  if (userInfo.value?.id) {
    caseFormData.value.testerId = userInfo.value.id.toString();
  }
};

// Member management
const projectMembers = ref<any[]>([]);

/**
 * Load project projectMembers
 */
const loadProjectMembers = async () => {
  const [error, { data }] = await project.getProjectMember(projectId.value);
  if (error) {
    return;
  }
  projectMembers.value = (data || []).map(i => {
    return {
      ...i,
      label: i.fullName,
      value: i.id
    };
  });
};

// Plan management
const evalWorkloadMethod = ref<{ value: string; message: string }>();
const planEndDate = ref<string>();

/**
 * Handle plan change and update deadline date
 * @param _value - Plan value (unused)
 * @param option - Selected plan option containing deadline and workload method
 */
const handlePlanChange = (_value: any, option: any) => {
  if (!props.editCase) {
    // For new cases, set deadline based on plan deadline
    if (option?.deadlineDate) {
      if (dayjs(option?.deadlineDate).isAfter(dayjs())) {
        caseFormData.value.deadlineDate = option.deadlineDate;
      } else {
        caseFormData.value.deadlineDate = dayjs().add(2, 'hour').format(DATE_TIME_FORMAT);
      }
    }
  } else {
    // For existing cases, use plan deadline directly
    caseFormData.value.deadlineDate = option?.deadlineDate || '';
  }

  // Adjust deadline to business hours (8 AM - 7 PM)
  if (caseFormData.value.deadlineDate && (dayjs(caseFormData.value.deadlineDate).hour() > 19 || dayjs(caseFormData.value.deadlineDate).hour() < 8)) {
    caseFormData.value.deadlineDate = dayjs(caseFormData.value.deadlineDate).add(12, 'hour').format(DATE_TIME_FORMAT);
  }

  planEndDate.value = caseFormData.value.deadlineDate;
  evalWorkloadMethod.value = option?.evalWorkloadMethod?.value;
};

// Module management
const moduleTreeOptions = ref<any[]>([]);

const softwareVersionParams = {
  filters: [{ value: [SoftwareVersionStatus.NOT_RELEASED, SoftwareVersionStatus.RELEASED], key: 'status', op: 'IN' }]
};

/**
 * Load module tree options
 */
const loadModuleTreeOptions = async () => {
  if (!projectId.value) {
    return;
  }
  const [error, { data }] = await modules.getModuleTree({
    projectId: projectId.value
  });
  if (error) {
    return;
  }
  moduleTreeOptions.value = data || [];
};

// Enum management
const stepViewOpt = ref<{ name: string; key: string }[]>([]);

/**
 * Load enum options
 */
const loadEnumOptions = async () => {
  const data = enumUtils.enumToMessages(EvalWorkloadMethod);
  evalWorkloadMethod.value = data?.filter(item => item.value === EvalWorkloadMethod.STORY_POINT)[0];

  const data1 = enumUtils.enumToMessages(CaseStepView);
  stepViewOpt.value = data1.map(i => ({ name: i.message, key: i.value }));
};

/**
 * Change step view mode
 * @param param - Object containing key
 */
const handleStepViewChange = ({ key }: { key: string }) => {
  caseFormData.value.stepView = key as CaseStepView;
};

// Rich text validation
const conditionRichRef = ref();
const descRichRef = ref();

/**
 * Validate precondition rich text length
 * @returns Promise with validation result
 */
const validatePrecondition = () => {
  if (!caseFormData.value.precondition) {
    return Promise.resolve();
  }
  if (conditionRichRef.value && conditionRichRef.value.getLength() > 2000) {
    return Promise.reject(t('testCase.messages.richTextTooLong'));
  }
  return Promise.resolve();
};

/**
 * Validate description rich text length
 * @returns Promise with validation result
 */
const validateDescriptionription = () => {
  if (!caseFormData.value.description) {
    return Promise.resolve();
  }
  if (descRichRef.value && descRichRef.value.getLength() > 2000) {
    return Promise.reject(t('testCase.messages.richTextTooLong'));
  }
  return Promise.resolve();
};

// UI state management
const isZoom = ref(typeof localStore.get(addCaseSizeKey) === 'boolean' ? localStore.get(addCaseSizeKey) : false);
const style = ref({ width: isZoom.value ? '100%' : '1200px' });

/**
 * Toggle modal zoom state
 */
const handleZoom = () => {
  isZoom.value = !isZoom.value;
  localStore.set(addCaseSizeKey, isZoom.value);
  style.value.width = isZoom.value ? '100%' : '1200px';
};

// Computed properties for form values
const selectedPlanId = computed({
  get: () => caseFormData.value.planId || undefined,
  set: (value: string | undefined) => {
    caseFormData.value.planId = value;
  }
});

const selectedTesterId = computed({
  get: () => caseFormData.value.testerId || undefined,
  set: (value: string | undefined) => {
    caseFormData.value.testerId = value;
  }
});

const selectedDeveloperId = computed({
  get: () => caseFormData.value.developerId || undefined,
  set: (value: string | undefined) => {
    caseFormData.value.developerId = value;
  }
});

const selectedTagIds = computed({
  get: () => caseFormData.value.tagIds?.map(id => id.toString()) || [],
  set: (value: string[]) => {
    caseFormData.value.tagIds = value;
  }
});

const selectedRefTaskIds = computed({
  get: () => caseFormData.value.refTaskIds?.map(id => id.toString()) || [],
  set: (value: string[]) => {
    caseFormData.value.refTaskIds = value;
  }
});

const selectedRefCaseIds = computed({
  get: () => caseFormData.value.refCaseIds?.map(id => id.toString()) || [],
  set: (value: string[]) => {
    caseFormData.value.refCaseIds = value;
  }
});

/**
 * Handle actual workload change
 * @param value - Actual workload value
 */
const handleActualWorkloadChange = (value: string) => {
  if (!value) {
    formRef.value.clearValidate('evalWorkload');
  }
};

/**
 * Handle eval workload change
 * @param value - Eval workload value
 */
const handleEvalWorkloadChange = (value: string) => {
  if (!value) {
    caseFormData.value.actualWorkload = '';
    formRef.value.clearValidate('evalWorkload');
  }
};

/**
 * Validate eval workload
 * @param rule - Validation rule
 * @param value - Value to validate
 * @returns Promise with validation result
 */
const validateEvalWorkload = async (_rule: Rule, value: string) => {
  if (caseFormData.value.actualWorkload) {
    if (!value) {
      return Promise.reject(new Error(t('testCase.messages.pleaseEnterEvalWorkload')));
    } else {
      return Promise.resolve();
    }
  } else {
    return Promise.resolve();
  }
};

const autoSave = () => {
  if (currentCaseDetail.value?.description !== caseFormData.value.description
  || currentCaseDetail.value?.precondition !== caseFormData.value.precondition
  || JSON.stringify(currentCaseDetail.value?.steps) !== JSON.stringify(caseFormData.value.steps)) {
    let validationRuleKeys = [
      'name',
      'planId',
      'testerId',
      'deadlineDate',
      'testLayer',
      'testPurpose',
      'description'
    ];

    if (caseFormData.value.actualWorkload) {
      validationRuleKeys = [...validationRuleKeys, 'evalWorkload'];
    }
    formRef.value.validate(validationRuleKeys)
      .then(async () => {
        const params = prepareFormParams();
        await testCase.putCase([{ id: props.editCase?.id, ...params }]);
        const [error, { data }] = await testCase.getCaseDetail(props.editCase?.id as string);
        if (error) {
          return;
        }
        currentCaseDetail.value = data;
      });
  }
};

let intervalSave: NodeJS.Timeout | null = null;

onMounted(() => {
  watch(() => props.visible, async (newValue) => {
    intervalSave && clearInterval(intervalSave);
    intervalSave = null;
    if (newValue) {
      await loadModuleTreeOptions();
      if (props.editCase) {
        await loadCaseDetails();
        intervalSave = setInterval(() => autoSave(), 30000);
      } else {
        resetForm();
      }
    }
  }, {
    immediate: true
  });
  watch(() => projectId.value, (newValue) => {
    if (newValue) {
      loadProjectMembers();
      loadModuleTreeOptions();
    }
  }, {
    immediate: true
  });

  loadEnumOptions();
});

onBeforeUnmount(() => {
  intervalSave && clearInterval(intervalSave);
  intervalSave = null
});

</script>
<template>
  <Modal
    :title="props.editCase ? t('actions.edit') : t('actions.add')"
    :visible="props.visible"
    :footer="null"
    :style="style"
    class="relative max-w-full"
    @cancel="close">
    <Tooltip :title="isZoom ? t('actions.exitFullScreen') : t('actions.fullScreen')">
      <Icon
        :icon="isZoom?'icon-tuichuzuida':'icon-zuidahua'"
        class="absolute right-10 top-3.5 text-3.5 cursor-pointer"
        @click="handleZoom" />
    </Tooltip>

    <Spin :spinning="isLoading" class="h-full">
      <Form
        :key="caseFormData.id"
        ref="formRef"
        :model="caseFormData"
        size="small"
        layout="vertical"
        class="h-full">
        <div class="flex overflow-y-auto -mr-4.5 pr-4.5 pb-5" :style="{height: isZoom?'100vh':'75vh'}">
          <div class="flex-1">
            <div class="flex items-center justify-between space-x-2">
              <FormItem
                :label="t('common.name')"
                name="name"
                class="flex-1"
                :rules="[{ required: true, message: t('testCase.messages.pleaseEnterCaseName') }]">
                <Input
                  v-model:value="caseFormData.name"
                  size="small"
                  :maxlength="400"
                  :placeholder="t('testCase.messages.enterCaseName')" />
                 
              </FormItem>
              <FormItem label="&nbsp;" name="selectTemplate">
                <DropdownTemplateSelect :templateType="TestTemplateType.TEST_CASE" @change="handleSelectTemplate" />
              </FormItem>
            </div>

            <div class="flex space-x-2">
              <FormItem
                name="testLayer"
                :label="t('common.testLayer')"
                class="flex-1"
                :rules="[{ required: true, message: t('common.placeholders.selectTestLayer') }]">
                <SelectEnum
                  v-model:value="caseFormData.testLayer"
                  enumKey="TestLayer"
                  size="small" />
              </FormItem>

              <FormItem
                name="testPurpose"
                :label="t('common.testPurpose')"
                class="flex-1"
                :rules="[{ required: true, message: t('common.placeholders.selectTestPurpose') }]">
                  <SelectEnum
                    v-model:value="caseFormData.testPurpose"
                    enumKey="TestPurpose"
                    size="small" />
                </FormItem>
            </div>

            <FormItem
              name="precondition"
              :rules="[{validator: validatePrecondition}]">
              <template #label>
                <div class="text-3 flex space-x-2 items-center">
                  <span>{{ t('common.precondition') }}</span>
                  <Hints :text="t('testCase.messages.preconditionHint')" />
                </div>
              </template>

              <RichEditor
                ref="conditionRichRef"
                v-model:value="caseFormData.precondition"
                :height="155"
                :options="{placeholder: t('testCase.messages.enterPrecondition')}" />
            </FormItem>

            <FormItem>
              <template #label>
                <div class="text-3 flex space-x-2 items-center">
                  <span>{{ t('common.testSteps') }}</span>

                  <Dropdown
                    :value="[caseFormData.stepView]"
                    :menuItems="stepViewOpt"
                    @click="handleStepViewChange">
                    <span class="text-theme-special">{{ t('testCase.actions.switchType') }}
                      <Icon icon="icon-xiajiantou" /></span>
                  </Dropdown>
                  <Hints :text="t('testCase.messages.testStepsHint')" class="flex-1" />
                </div>
              </template>

              <CaseSteps
                ref="stepsRef"
                v-model:value="caseFormData.steps"
                :stepView="caseFormData.stepView"
                :defaultValue="stepDefaultValue" />
            </FormItem>

            <FormItem
              name="description"
              :rules="[{validator: validateDescriptionription}]">
              <template #label>
                <div class="text-3 flex space-x-2 items-center">
                  <span>{{ t('common.description') }}</span>
                  <Hints :text="t('testCase.messages.descriptionHint')" />
                </div>
              </template>

              <RichEditor
                ref="descRichRef"
                v-model:value="caseFormData.description"
                :options="{placeholder: t('common.placeholders.inputDescription30')}"
                :height="280"
                class="add-case" />
            </FormItem>
          </div>

          <div style="width: 320px;" class="ml-5 h-full side-form">
            <FormItem
              :label="t('common.plan')"
              name="planId"
              :rules="{ required: true, message: t('common.placeholders.selectOrSearchPlan') }">
              <Select
                v-model:value="selectedPlanId"
                :action="`${TESTER}/func/plan?projectId=${projectId}&fullTextSearch=true`"
                :fieldNames="{ value: 'id', label: 'name' }"
                :readonly="!!props.editCase"
                showSearch
                internal
                :placeholder="t('common.placeholders.selectOrSearchPlan')"
                @change="(value: any, option: any) => handlePlanChange(value, option)">
                <template #option="item">
                  <div class="flex items-center" :title="item.name">
                    <Icon icon="icon-jihua" class="mr-1 text-3.5" />
                    <div style="max-width: 220px;" class="truncate">{{ item.name }}</div>
                  </div>
                </template>
              </Select>
            </FormItem>

            <FormItem
              :label="t('common.module')"
              name="moduleId">
              <TreeSelect
                v-model:value="caseFormData.moduleId"
                dropdownClassName="module_tree"
                :treeData="moduleTreeOptions"
                :fieldNames="{ value: 'id', label: 'name', children: 'children' }"
                :virtual="false"
                size="small"
                showSearch
                allowClear
                :placeholder="t('common.placeholders.selectOrSearchModule')">
                <template #title="item">
                  <div class="flex items-center" :title="item.name">
                    <Icon icon="icon-mokuai" class="mr-1 text-3.5" />
                    <div style="max-width: 220px;" class="truncate">{{ item.name }}</div>
                  </div>
                </template>
              </TreeSelect>
            </FormItem>

            <FormItem
              :label="t('common.tester')"
              name="testerId"
              :rules="{required:true,message: t('common.placeholders.selectTester')}">
              <div class="flex items-center">
                <Select
                  v-model:value="selectedTesterId"
                  :placeholder="t('common.placeholders.selectTester')"
                  :options="projectMembers as any"
                  class="flex-1"
                  size="small" />
                <Button
                  type="link"
                  size="small"
                  class="p-0 h-5 leading-5 ml-1"
                  @click="assignTesterToMe">
                  {{ t('actions.assignToMe') }}
                </Button>
              </div>
            </FormItem>

            <FormItem
              :label="t('common.developer')"
              name="developerId"
              :rules="{required:true,message: t('common.placeholders.selectDeveloper')}">
              <Select
                v-model:value="selectedDeveloperId"
                :placeholder="t('common.placeholders.selectDeveloper')"
                :options="projectMembers as any"
                class="flex-1"
                size="small" />
            </FormItem>

            <FormItem
              name="priority"
              required>
              <template #label>
                <span class="flex items-center">
                  {{ t('common.priority') }}
                  <Tooltip
                    placement="right"
                    arrowPointAtCenter
                    :overlayStyle="{'max-width':'400px'}"
                    :title="t('testCase.messages.priorityHint')">
                    <Icon icon="icon-tishi1" class="text-tips ml-1 cursor-pointer text-3.5" />
                  </Tooltip>
                </span>
              </template>

              <SelectEnum
                v-model:value="caseFormData.priority"
                enumKey="Priority"
                size="small">
                <template #option="record">
                  <TaskPriority :value="record as any" />
                </template>
              </SelectEnum>
            </FormItem>

            <FormItem
              name="evalWorkload"
              :rules="{required: !!caseFormData.actualWorkload, validator: validateEvalWorkload,trigger: 'change' }">
              <template #label>
                <span class="flex items-center">
                  {{ t('common.evalWorkload') }}
                  <Tooltip
                    placement="right"
                    arrowPointAtCenter
                    :overlayStyle="{'max-width':'400px'}"
                    :title="evalWorkloadMethod?.value === EvalWorkloadMethod.STORY_POINT
                      ? t('common.storyPointsHint') : t('common.workHoursHint')">
                    <Icon icon="icon-tishi1" class="text-tips ml-1 cursor-pointer text-3.5" />
                  </Tooltip>
                </span>
              </template>

              <Input
                v-model:value="caseFormData.evalWorkload"
                size="small"
                :disabled="!caseFormData.planId"
                :min="0.1"
                :max="1000"
                :placeholder="t('common.placeholders.workloadRange')"
                dataType="float"
                @blur="handleEvalWorkloadChange($event.target.value)" />
            </FormItem>

            <template v-if="props.editCase">
              <FormItem
                name="actualWorkload">
                <template #label>
                  <span class="flex items-center">
                    {{ t('common.actualWorkload') }}
                    <Tooltip
                      placement="right"
                      arrowPointAtCenter
                      :overlayStyle="{'max-width':'400px'}"
                      :title="evalWorkloadMethod?.value === EvalWorkloadMethod.STORY_POINT
                        ? t('common.actualStoryPointsHint') : t('common.actualWorkHoursHint')">
                      <Icon icon="icon-tishi1" class="text-tips ml-1 cursor-pointer text-3.5" />
                    </Tooltip>
                  </span>
                </template>

                <Input
                  v-model:value="caseFormData.actualWorkload"
                  size="small"
                  :disabled="!caseFormData.planId"
                  :min="0.1"
                  :max="1000"
                  :placeholder="t('common.placeholders.workloadRange')"
                  dataType="float"
                  @change="handleActualWorkloadChange($event.target.value)" />
              </FormItem>
            </template>

            <FormItem
              name="softwareVersion"
              :label="t('common.softwareVersion')">
              <Select
                v-model:value="caseFormData.softwareVersion"
                allowClear
                :placeholder="t('common.placeholders.selectSoftwareVersion')"
                :action="`${TESTER}/software/version?projectId=${projectId}`"
                :params="softwareVersionParams"
                :fieldNames="{value:'name', label: 'name'}">
              </Select>
            </FormItem>

            <FormItem
              name="deadlineDate"
              :rules="{required: true, validator: validateDate,trigger: 'change' }">
              <template #label>
                <span class="flex items-center">
                  {{ t('common.deadlineDate') }}
                  <Tooltip
                    placement="right"
                    arrowPointAtCenter
                    :overlayStyle="{'max-width':'400px'}"
                    :title="t('testCase.messages.deadlineHint')">
                    <Icon icon="icon-tishi1" class="text-tips aml-1 cursor-pointer text-3.5" />
                  </Tooltip>
                </span>
              </template>

              <DatePicker
                v-model:value="caseFormData.deadlineDate"
                :disabledDate="disabledDate"
                :showTime="{hideDisabledOptions: true,format:TIME_FORMAT}"
                :disabled="!caseFormData.planId"
                allowClear
                :placeholder="t('common.placeholders.selectDeadline')"
                size="small"
                type="date"
                class="w-full" />
            </FormItem>

            <FormItem
              name="tagIds">
              <template #label>
                <span>{{ t('common.tag') }}
                  <Tooltip
                    placement="right"
                    arrowPointAtCenter
                    :overlayStyle="{'max-width':'400px'}"
                    :title="t('testCase.messages.tagsHint')">
                    <Icon icon="icon-tishi1" class="text-tips cursor-pointer text-3.5" />
                  </Tooltip>
                </span>
              </template>

              <Select
                v-model:value="selectedTagIds"
                showSearch
                :fieldNames="{label:'name',value:'id'}"
                :maxTagCount="5"
                :maxTagTextLength="15"
                :maxTags="5"
                :allowClear="false"
                :action="`${TESTER}/tag?projectId=${projectId}&fullTextSearch=true`"
                :placeholder="t('common.placeholders.selectTag')"
                mode="multiple">
                <template #option="item">
                  <div class="flex items-center" :title="item.name">
                    <Icon icon="icon-biaoqian1" class="mr-1 text-3.5" />
                    <div style="max-width: 220px;" class="truncate">{{ item.name }}</div>
                  </div>
                </template>
              </Select>
            </FormItem>

            <FormItem
              name="refTaskIds"
              :label="t('common.assocIssues')"
              class="relative">
              <Select
                v-model:value="selectedRefTaskIds"
                showSearch
                internal
                :allowClear="false"
                :fieldNames="{ label: 'name', value: 'id' }"
                :maxTagCount="10"
                :maxTagTextLength="15"
                :maxTags="20"
                :action="`${TESTER}/task?projectId=${projectId}&fullTextSearch=true`"
                :placeholder="t('testCase.messages.maxAssocIssues')"
                mode="multiple">
                <template #option="record">
                  <div class="flex items-center leading-4.5 overflow-hidden">
                    <IconTask :value="record.taskType?.value" class="text-4 flex-shrink-0" />
                    <div class="link truncate ml-1" :title="record.name">
                      {{ record.name }}
                    </div>
                    <div
                      v-if="record.overdue"
                      class="flex-shrink-0 border border-status-error rounded px-0.5 ml-2"
                      style="transform: scale(0.9);color: rgba(245, 34, 45, 100%);line-height: 16px;">
                      <span class="inline-block transform-gpu">{{ t('status.overdue') }}</span>
                    </div>
                  </div>
                </template>
              </Select>
            </FormItem>

            <FormItem
              name="refCaseIds"
              :label="t('common.assocCases')"
              class="relative">
              <Select
                v-model:value="selectedRefCaseIds"
                showSearch
                internal
                :allowClear="false"
                :fieldNames="{ label: 'name', value: 'id' }"
                :maxTagCount="10"
                :maxTagTextLength="15"
                :maxTags="20"
                :action="`${TESTER}/func/case?projectId=${projectId}&fullTextSearch=true`"
                :placeholder="t('testCase.messages.maxAssocCases')"
                mode="multiple">
                <template #option="record">
                  <div class="flex items-center leading-4.5 overflow-hidden">
                    <Icon icon="icon-gongnengyongli" class="text-4 flex-shrink-0" />
                    <div class="link truncate ml-1.5" :title="record.name">
                      {{ record.name }}
                    </div>
                  </div>
                </template>
              </Select>
            </FormItem>

            <FormItem
              :label="t('common.attachment')"
              name="attachments">
              <div
                style="height: 90px; border-color: rgba(0, 119, 255);background-color: rgba(0, 119, 255, 4%);"
                class="border border-dashed rounded flex flex-col px-2 py-1"
                :class="caseFormData.attachments.length?'justify-between':'justify-center'">
                <template v-if="caseFormData.attachments?.length">
                  <div
                    style="height: 286px;scrollbar-gutter: stable;"
                    class="overflow-hidden hover:overflow-y-auto -mr-2 pr-1">
                    <div
                      v-for="(item,index) in caseFormData.attachments"
                      :key="index"
                      :class="{'rounded-b':index===caseFormData.attachments.length-1}"
                      class="flex items-center justify-between text-3 leading-3">
                      <div
                        :title="item.name"
                        class="truncate text-theme-sub-content leading-4 cursor-pointer"
                        style="width: 250px;">
                        {{ item.name }}
                      </div>
                      <Icon
                        icon="icon-qingchu"
                        class="text-theme-special text-theme-text-hover cursor-pointer flex-shrink-0 leading-4 text-3.5"
                        @click="removeAttachment(index)" />
                    </div>
                  </div>

                  <div v-if="caseFormData.attachments.length < 5" class="flex justify-end">
                    <Upload
                      :fileList="[]"
                      name="file"
                      class="-mb-1 mr-1"
                      :customRequest="handleFileUpload">
                      <Icon icon="icon-shangchuan" class="text-theme-special mr-1" />
                      <span class="text-3 leading-3 text-theme-text-hover">{{ t('actions.continueUpload') }}</span>
                    </Upload>
                  </div>
                </template>

                <template v-else>
                  <div class="flex justify-center text-center">
                    <Upload
                      name="file"
                      :fileList="[]"
                      :customRequest="handleFileUpload">
                      <Icon icon="icon-shangchuan" class="mr-1 text-theme-special" />
                      <span class="text-3 text-theme-text-hover">{{ t('actions.upload') }}</span>
                      <span class="text-3 block">
                        {{ t('backlog.edit.messages.fileSizeLimit', { size: MAX_FILE_SIZE_MB }) }}
                      </span>
                    </Upload>
                  </div>
                </template>
              </div>
            </FormItem>
          </div>
        </div>

        <FormItem>
          <div class="flex justify-end mt-5">
            <Button
              type="primary"
              size="small"
              htmlType="submit"
              class="px-3"
              @click="save('save')">
              {{ t('actions.save') }}
            </Button>

            <Button
              v-if="!editCase"
              type="primary"
              size="small"
              htmlType="submit"
              class="px-3 ml-5"
              @click="save('add')">
              {{ t('actions.saveAndContinue') }}
            </Button>

            <Button
              size="small"
              class="ml-5 px-3"
              @click="close">
              {{ t('actions.cancel') }}
            </Button>
          </div>
        </FormItem>
      </Form>
    </Spin>
  </Modal>
</template>
<style scoped>
:deep(.ant-form-item-label) {
  max-width: 720px;
  font-size: 12px;
  font-weight: 600;
}

:deep(.ant-form-item:not(.ant-form-item-has-error)) {
  margin-bottom: 15px;
}

:deep(.ant-form-item .ant-form-item-has-error) {
  margin-bottom: 15px;
}

:deep(.ant-form-item-with-help .ant-form-item-explain) {
  min-height: 20px;
}
</style>
