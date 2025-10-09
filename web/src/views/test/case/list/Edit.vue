<script lang="ts" setup>
import { computed, inject, onMounted, ref, watch, Ref } from 'vue';
import {
  DatePicker, Hints, Icon, IconTask, Input, Modal, notification, Select, Spin, Dropdown
} from '@xcan-angus/vue-ui';
import { Button, Form, FormItem, Tooltip, TreeSelect, Upload } from 'ant-design-vue';
import type { Rule } from 'ant-design-vue/es/form';
import {
  EvalWorkloadMethod, Priority, SearchCriteria, enumUtils, TESTER,
  upload, localStore, utils, appContext
} from '@xcan-angus/infra';
import { CaseStepView, SoftwareVersionStatus } from '@/enums/enums';
import dayjs from 'dayjs';
import { testCase, modules, project } from '@/api/tester';
import { DATE_TIME_FORMAT, TIME_FORMAT } from '@/utils/constant';

import { useI18n } from 'vue-i18n';
import { CaseDetail } from '@/views/test/types';
import { CaseEditState } from './types';

import TaskPriority from '@/components/TaskPriority/index.vue';
import RichEditor from '@/components/richEditor/index.vue';
import SelectEnum from '@/components/enum/SelectEnum.vue';
import CaseSteps from '@/views/test/case/list/CaseSteps.vue';

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
const emits = defineEmits<{(e: 'update:visible', value: boolean):void; (e: 'update', id?:string):void}>();

// Basic state management
const userInfo = ref(appContext.getUser());
const projectId = inject<Ref<string>>('projectId', ref(''));
const addCaseSizeKey = `${userInfo.value?.id || ''}${projectId.value}addFuncCaseSize`;

// Form state management
const formRef = ref();

// Extended form state interface to include all form fields
interface ExtendedCaseEditState extends Omit<CaseEditState, 'refIdMap'> {
  refCaseIds: number[];
  refTaskIds: number[];
  softwareVersion?: string;
  id?: string;
}

const formState = ref<ExtendedCaseEditState>({
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
  testerId: userInfo.value?.id || 0,
  refCaseIds: [],
  refTaskIds: [],
  developerId: undefined,
  softwareVersion: undefined,
  stepView: CaseStepView.TABLE,
  caseType: 'FUNC'
});

const oldFormState = ref<ExtendedCaseEditState>();
const caseDetail = ref<CaseDetail>();
// Data fetching functions

/**
 * Fetch case information for editing
 */
const getCaseInfo = async () => {
  if (!props.editCase?.id) {
    return;
  }
  const [error, { data }] = await testCase.getCaseDetail(props.editCase.id);
  if (error) {
    return;
  }
  Object.keys(formState.value).forEach((key) => {
    if (key === 'tagIds') {
      formState.value.tagIds = (data.tags || []).map(item => {
        return item.id;
      });
    } else if (key === 'attachments') {
      formState.value[key] = data[key] || [];
    } else if (key === 'refTaskInfos') {
      formState.value.refTaskIds = (data.refTaskInfos || []).map(item => {
        return item.id;
      });
    } else if (key === 'refCaseInfos') {
      formState.value.refCaseIds = (data.refCaseInfos || []).map(item => {
        return item.id;
      });
    } else if (key === 'actualWorkload') {
      formState.value.actualWorkload = data?.actualWorkload || data?.evalWorkload;
    } else if (key === 'priority') {
      formState.value.priority = data?.priority?.value || Priority.MEDIUM;
    } else if (key === 'precondition') {
      formState.value[key] = getJson(data?.[key]) || '';
    } else if (key === 'stepView') {
      formState.value[key] = data?.[key]?.value || 'TABLE';
    } else {
      if (key === 'moduleId') {
        if (data?.[key] === '-1') {
          data[key] = undefined;
        }
      }
      (formState.value as any)[key] = data?.[key];
    }
  });

  caseDetail.value = data;
  stepDefaultValue.value = data.steps;
  oldFormState.value = JSON.parse(JSON.stringify(formState.value));
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
  formRef.value.resetFields(Object.keys(formState.value).filter(item => item !== 'planId' && item !== 'deadlineDate'));
  formState.value.refCaseIds = [];
  formState.value.refTaskIds = [];
  if (type !== 'save') {
    formState.value.steps = [];
    formState.value.description = '';
    stepDefaultValue.value = [];
    // Set module ID if provided and valid, otherwise undefined to show placeholder
    if (props.moduleId && +props.moduleId > 0) {
      formState.value.moduleId = Number(props.moduleId);
    } else {
      formState.value.moduleId = undefined;
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
  if (loading.value) {
    return;
  }
  saveType.value = type;
  let validationRuleKeys = [
    'name',
    'planId',
    'testerId',
    'deadlineDate'
  ];

  if (formState.value.actualWorkload) {
    validationRuleKeys = [...validationRuleKeys, 'evalWorkload'];
  }

  formRef.value.validate(validationRuleKeys)
    .then(async () => {
      if (formState.value.description?.length > 2000) {
        notification.warning(t('testCase.messages.richTextTooLong'));
        return;
      }

      if (props.editCase) {
        await editSave();
      } else {
        await addSave();
      }
    });
};

/**
 * Prepare form parameters for API calls
 * @returns Cleaned form parameters
 */
const getParams = () => {
  const params = JSON.parse(JSON.stringify(formState.value));
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

const loading = ref(false);

/**
 * Save edited case
 */
const editSave = async () => {
  if (!props.editCase?.id) {
    return;
  }
  if (utils.deepCompare(oldFormState.value, formState.value)) {
    emits('update:visible', false);
    return;
  }
  const params = getParams();
  loading.value = true;
  const [error] = await testCase.putCase([{ id: props.editCase.id, ...params }]);
  loading.value = false;
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
const addSave = async () => {
  const params = getParams();
  loading.value = true;
  const [error] = await testCase.addCase([params]);
  loading.value = false;
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
const upLoadFile = async ({ file }: { file: any }) => {
  if (file.size > 100 * 1024 * 1024) {
    notification.error(t('testCase.messages.fileTooLarge'));
    return;
  }

  if (formState.value.attachments?.length >= 5 || loading.value) {
    return;
  }

  loading.value = true;
  const [error, { data = [] }] = await upload(file.originFileObj, { bizKey: 'angusTesterCaseAttachments' });
  loading.value = false;
  if (error) {
    return;
  }

  if (data && data.length > 0) {
    const newData = data?.map(item => ({ name: item.name, url: item.url }));
    formState.value.attachments.push(...newData);
  }
};

/**
 * Delete uploaded file
 * @param index - File index to delete
 */
const delFile = (index: number) => {
  formState.value?.attachments?.splice(index, 1);
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
const setTesterForMe = () => {
  if (userInfo.value?.id) {
    formState.value.testerId = userInfo.value.id;
  }
};

// Member management
const members = ref<any[]>([]);

/**
 * Load project members
 */
const loadMembers = async () => {
  const [error, { data }] = await project.getProjectMember(projectId.value);
  if (error) {
    return;
  }
  members.value = (data || []).map(i => {
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
const planChange = (_value: any, option: any) => {
  if (!props.editCase) {
    // For new cases, set deadline based on plan deadline
    if (option?.deadlineDate) {
      if (dayjs(option?.deadlineDate).isAfter(dayjs())) {
        formState.value.deadlineDate = option.deadlineDate;
      } else {
        formState.value.deadlineDate = dayjs().add(2, 'hour').format(DATE_TIME_FORMAT);
      }
    }
  } else {
    // For existing cases, use plan deadline directly
    formState.value.deadlineDate = option?.deadlineDate || '';
  }

  // Adjust deadline to business hours (8 AM - 7 PM)
  if (formState.value.deadlineDate && (dayjs(formState.value.deadlineDate).hour() > 19 || dayjs(formState.value.deadlineDate).hour() < 8)) {
    formState.value.deadlineDate = dayjs(formState.value.deadlineDate).add(12, 'hour').format(DATE_TIME_FORMAT);
  }

  planEndDate.value = formState.value.deadlineDate;
  evalWorkloadMethod.value = option?.evalWorkloadMethod?.value;
};

// Module management
const moduleTreeData = ref<any[]>([]);

/**
 * Load module tree data
 */
const getModuleTreeData = async () => {
  if (!projectId.value) {
    return;
  }
  const [error, { data }] = await modules.getModuleTree({
    projectId: projectId.value
  });
  if (error) {
    return;
  }
  moduleTreeData.value = data || [];
};

// Enum management
const stepViewOpt = ref<{ name: string; key: string }[]>([]);

/**
 * Load enum options
 */
const loadEnums = async () => {
  const data = enumUtils.enumToMessages(EvalWorkloadMethod);
  evalWorkloadMethod.value = data?.filter(item => item.value === EvalWorkloadMethod.STORY_POINT)[0];

  const data1 = enumUtils.enumToMessages(CaseStepView);
  stepViewOpt.value = data1.map(i => ({ name: i.message, key: i.value }));
};

/**
 * Change step view mode
 * @param param - Object containing key
 */
const changeStepView = ({ key }: { key: string }) => {
  formState.value.stepView = key as CaseStepView;
};

// Rich text validation
const conditionRichRef = ref();
const descRichRef = ref();

/**
 * Validate precondition rich text length
 * @returns Promise with validation result
 */
const validateCondition = () => {
  if (!formState.value.precondition) {
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
const validateDesc = () => {
  if (!formState.value.description) {
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
const planIdValue = computed({
  get: () => formState.value.planId || undefined,
  set: (value: number | undefined) => {
    formState.value.planId = value ? Number(value) : undefined;
  }
});

const testerIdValue = computed({
  get: () => formState.value.testerId || undefined,
  set: (value: number | undefined) => {
    formState.value.testerId = value ? Number(value) : undefined;
  }
});

const developerIdValue = computed({
  get: () => formState.value.developerId || undefined,
  set: (value: number | undefined) => {
    formState.value.developerId = value ? Number(value) : undefined;
  }
});

const tagIdsValue = computed({
  get: () => formState.value.tagIds?.map(id => id.toString()) || [],
  set: (value: string[]) => {
    formState.value.tagIds = value.map(id => Number(id));
  }
});

const refTaskIdsValue = computed({
  get: () => formState.value.refTaskIds?.map(id => id.toString()) || [],
  set: (value: string[]) => {
    formState.value.refTaskIds = value.map(id => Number(id));
  }
});

const refCaseIdsValue = computed({
  get: () => formState.value.refCaseIds?.map(id => id.toString()) || [],
  set: (value: string[]) => {
    formState.value.refCaseIds = value.map(id => Number(id));
  }
});

/**
 * Handle actual workload change
 * @param value - Actual workload value
 */
const actualWorkloadChange = (value: string) => {
  if (!value) {
    formRef.value.clearValidate('evalWorkload');
  }
};

/**
 * Handle eval workload change
 * @param value - Eval workload value
 */
const evalWorkloadChange = (value: string) => {
  if (!value) {
    formState.value.actualWorkload = '';
    formRef.value.clearValidate('evalWorkload');
  }
};

/**
 * Validate eval workload
 * @param rule - Validation rule
 * @param value - Value to validate
 * @returns Promise with validation result
 */
const evalWorkloadValidateDate = async (_rule: Rule, value: string) => {
  if (formState.value.actualWorkload) {
    if (!value) {
      return Promise.reject(new Error(t('testCase.messages.pleaseEnterEvalWorkload')));
    } else {
      return Promise.resolve();
    }
  } else {
    return Promise.resolve();
  }
};

onMounted(() => {
  watch(() => props.visible, async (newValue) => {
    if (newValue) {
      await getModuleTreeData();
      if (props.editCase) {
        getCaseInfo();
      } else {
        resetForm();
      }
    }
  }, {
    immediate: true
  });
  watch(() => projectId.value, (newValue) => {
    if (newValue) {
      loadMembers();
      getModuleTreeData();
    }
  }, {
    immediate: true
  });

  loadEnums();
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

    <Spin :spinning="loading" class="h-full">
      <Form
        :key="formState.id"
        ref="formRef"
        :model="formState"
        size="small"
        layout="vertical"
        class="h-full">
        <div class="flex overflow-y-auto -mr-4.5 pr-4.5 pb-5" :style="{height: isZoom?'100vh':'75vh'}">
          <div class="flex-1">
            <FormItem
              :label="t('common.name')"
              name="name"
              :rules="[{ required: true, message: t('testCase.messages.pleaseEnterCaseName') }]">
              <Input
                v-model:value="formState.name"
                size="small"
                :maxlength="400"
                :placeholder="t('testCase.messages.enterCaseName')" />
            </FormItem>

            <FormItem
              name="precondition"
              :rules="[{validator: validateCondition}]">
              <template #label>
                <div class="text-3 flex space-x-2 items-center">
                  <span>{{ t('common.precondition') }}</span>
                  <Hints :text="t('testCase.messages.preconditionHint')" />
                </div>
              </template>

              <RichEditor
                ref="conditionRichRef"
                v-model:value="formState.precondition"
                :height="155"
                :options="{placeholder: t('testCase.messages.enterPrecondition')}" />
            </FormItem>

            <FormItem>
              <template #label>
                <div class="text-3 flex space-x-2 items-center">
                  <span>{{ t('common.testSteps') }}</span>

                  <Dropdown
                    :value="[formState.stepView]"
                    :menuItems="stepViewOpt"
                    @click="changeStepView">
                    <span class="text-theme-special">{{ t('testCase.actions.switchType') }}
                      <Icon icon="icon-xiajiantou" /></span>
                  </Dropdown>
                  <Hints :text="t('testCase.messages.testStepsHint')" class="flex-1" />
                </div>
              </template>

              <CaseSteps
                ref="stepsRef"
                v-model:value="formState.steps"
                :stepView="formState.stepView"
                :defaultValue="stepDefaultValue" />
            </FormItem>

            <FormItem
              name="description"
              :rules="[{validator: validateDesc}]">
              <template #label>
                <div class="text-3 flex space-x-2 items-center">
                  <span>{{ t('common.description') }}</span>
                  <Hints :text="t('testCase.messages.descriptionHint')" />
                </div>
              </template>

              <RichEditor
                ref="descRichRef"
                v-model:value="formState.description"
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
                v-model:value="planIdValue"
                :action="`${TESTER}/func/plan?projectId=${projectId}&fullTextSearch=true`"
                :fieldNames="{ value: 'id', label: 'name' }"
                :readonly="!!props.editCase"
                showSearch
                internal
                :placeholder="t('common.placeholders.selectOrSearchPlan')"
                @change="(value: any, option: any) => planChange(value, option)">
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
                v-model:value="formState.moduleId"
                dropdownClassName="module_tree"
                :treeData="moduleTreeData"
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
                  v-model:value="testerIdValue"
                  :placeholder="t('common.placeholders.selectTester')"
                  :options="members as any"
                  class="flex-1"
                  size="small" />
                <Button
                  type="link"
                  size="small"
                  class="p-0 h-5 leading-5 ml-1"
                  @click="setTesterForMe">
                  {{ t('actions.assignToMe') }}
                </Button>
              </div>
            </FormItem>

            <FormItem
              :label="t('common.developer')"
              name="developerId"
              :rules="{required:true,message: t('common.placeholders.selectDeveloper')}">
              <Select
                v-model:value="developerIdValue"
                :placeholder="t('common.placeholders.selectDeveloper')"
                :options="members as any"
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
                v-model:value="formState.priority"
                enumKey="Priority"
                size="small">
                <template #option="record">
                  <TaskPriority :value="record as any" />
                </template>
              </SelectEnum>
            </FormItem>

            <FormItem
              name="evalWorkload"
              :rules="{required: !!formState.actualWorkload, validator: evalWorkloadValidateDate,trigger: 'change' }">
              <template #label>
                <span class="flex items-center">
                  {{ t('common.evalWorkloadMethod') }}
                  <Tooltip
                    placement="right"
                    arrowPointAtCenter
                    :overlayStyle="{'max-width':'400px'}"
                    :title="evalWorkloadMethod?.value === EvalWorkloadMethod.STORY_POINT
                      ? t('testCase.messages.storyPointsHint') : t('testCase.messages.workHoursHint')">
                    <Icon icon="icon-tishi1" class="text-tips ml-1 cursor-pointer text-3.5" />
                  </Tooltip>
                </span>
              </template>

              <Input
                v-model:value="formState.evalWorkload"
                size="small"
                :disabled="!formState.planId"
                :min="0.1"
                :max="1000"
                :placeholder="t('testCase.messages.minMaxDecimal')"
                dataType="float"
                @blur="evalWorkloadChange($event.target.value)" />
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
                        ? t('testCase.messages.actualWorkloadsHint') : t('testCase.messages.actualWorkloadHint')">
                      <Icon icon="icon-tishi1" class="text-tips ml-1 cursor-pointer text-3.5" />
                    </Tooltip>
                  </span>
                </template>

                <Input
                  v-model:value="formState.actualWorkload"
                  size="small"
                  :disabled="!formState.planId"
                  :min="0.1"
                  :max="1000"
                  :placeholder="t('testCase.messages.minMaxDecimal')"
                  dataType="float"
                  @change="actualWorkloadChange($event.target.value)" />
              </FormItem>
            </template>

            <FormItem
              name="softwareVersion"
              :label="t('common.softwareVersion')">
              <Select
                v-model:value="formState.softwareVersion"
                allowClear
                :placeholder="t('common.placeholders.selectSoftwareVersion')"
                :action="`${TESTER}/software/version?projectId=${projectId}`"
                :params="{filters: [{value: [SoftwareVersionStatus.NOT_RELEASED, SoftwareVersionStatus.RELEASED], key: 'status', op: SearchCriteria.OpEnum.In}]}"
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
                v-model:value="formState.deadlineDate"
                :disabledDate="disabledDate"
                :showTime="{hideDisabledOptions: true,format:TIME_FORMAT}"
                :disabled="!formState.planId"
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
                v-model:value="tagIdsValue"
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
                v-model:value="refTaskIdsValue"
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
              :label="t('testCase.messages.assocCases')"
              class="relative">
              <Select
                v-model:value="refCaseIdsValue"
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
                :class="formState.attachments.length?'justify-between':'justify-center'">
                <template v-if="formState.attachments?.length">
                  <div style="height: 286px;scrollbar-gutter: stable;" class="overflow-hidden hover:overflow-y-auto -mr-2 pr-1">
                    <div
                      v-for="(item,index) in formState.attachments"
                      :key="index"
                      :class="{'rounded-b':index===formState.attachments.length-1}"
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
                        @click="delFile(index)" />
                    </div>
                  </div>

                  <div v-if="formState.attachments.length < 5" class="flex justify-end">
                    <Upload
                      :fileList="[]"
                      name="file"
                      class="-mb-1 mr-1"
                      :customRequest="upLoadFile">
                      <Icon icon="icon-shangchuan" class="text-theme-special mr-1" />
                      <span class="text-3 leading-3 text-theme-text-hover">{{ t('actions.continueUpload') }}</span>
                    </Upload>
                  </div>
                </template>

                <template v-else>
                  <div class="flex justify-center">
                    <Upload
                      name="file"
                      :fileList="[]"
                      :customRequest="upLoadFile">
                      <Icon icon="icon-shangchuan" class="mr-1 text-theme-special" />
                      <span class="text-3 text-theme-text-hover">{{ t('actions.upload') }}</span>
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
