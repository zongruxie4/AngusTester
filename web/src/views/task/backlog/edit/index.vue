<script setup lang="ts">
import { computed, defineAsyncComponent, onMounted, reactive, ref, watch } from 'vue';
import { Button, Form, FormItem, Popover, TreeSelect, Upload } from 'ant-design-vue';
import type { Rule } from 'ant-design-vue/es/form';
import {
  AsyncComponent,
  DatePicker,
  Icon,
  IconTask,
  IconText,
  Input,
  Modal,
  notification,
  Select,
  SelectEnum,
  SelectUser,
  TaskPriority,
  Tooltip
} from '@xcan-angus/vue-ui';
import { localStore, upload, TESTER } from '@xcan-angus/infra';
import dayjs, { Dayjs } from 'dayjs';
import { cloneDeep, isEqual } from 'lodash-es';
import { modules, task } from '@/api/tester';

import { FormState } from './PropsType';
import { TaskInfo } from '../../PropsType';

const RichEditor = defineAsyncComponent(() => import('@/components/richEditor/index.vue'));

type Props = {
  projectId: string;
  userInfo: { id: string; };
  appInfo: { id: string; };
  sprintId: string;
  visible: boolean;
  taskId?: string;
  taskType?: 'TASK' | 'API_TEST' | 'BUG' | 'SCENARIO_TEST' | 'STORY' | 'REQUIREMENT' | undefined;
  assigneeId?: string;
  confirmorId?: string;
  moduleId?: string;
  parentTaskId?: string;// 创建子任务
  refCaseIds?: string[];
  name?: string;
  description?: string;
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  sprintId: undefined,
  visible: false,
  taskId: undefined,
  taskType: undefined,
  assigneeId: undefined,
  confirmorId: undefined,
  moduleId: undefined,
  parentTaskId: undefined,
  name: undefined,
  description: undefined,
  refCaseIds: () => []
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void;
  (e: 'update:taskId', value: string | undefined): void;
  (e: 'ok', value?: Partial<TaskInfo>): void;
}>();

const formRef = ref();

const loading = ref<boolean>(false);
const zoomInFlag = ref(false);
const showEditorFlag = ref(false);

const evalWorkloadMethod = ref<'WORKING_HOURS' | 'STORY_POINT' | undefined>('STORY_POINT');
const sprintDeadlineDate = ref<string>();

const moduleTreeData = ref([]);
const getModuleTreeData = async () => {
  if (!props.projectId) {
    return;
  }
  const [error, { data }] = await modules.getModuleTree({
    projectId: props.projectId
  });
  if (error) {
    return;
  }
  moduleTreeData.value = data || [];
};

let oldFormState: FormState | undefined;
const formState = reactive<FormState>({
  projectId: undefined,
  assigneeId: undefined,
  attachments: undefined,
  confirmorId: undefined,
  deadlineDate: undefined,
  description: undefined,
  evalWorkload: undefined,
  actualWorkload: undefined,
  name: undefined,
  priority: undefined,
  parentTaskId: undefined,
  sprintId: undefined,
  moduleId: undefined,
  tagIds: undefined,
  refTaskIds: undefined,
  refCaseIds: undefined,
  targetId: undefined,
  targetParentId: undefined,
  taskType: undefined,
  testType: undefined,
  bugLevel: 'MINOR',
  testerId: undefined,
  missingBugFlag: false,
  softwareVersion: undefined
});

const assigneeDefaultOptions = ref<{[key:string]:{fullName:string;id:string;}}>();
const confirmorDefaultOptions = ref<{[key:string]:{fullName:string;id:string;}}>();

const showContinue = computed(() => {
  return !props.taskId && !props.taskType;
});

const zoomToggle = () => {
  zoomInFlag.value = !zoomInFlag.value;
  localStore.set(zoomInFlagCacheKey.value, zoomInFlag.value);
};

const sprintChange = (_id: string, option: TaskInfo) => {
  formState.deadlineDate = option?.deadlineDate || '';
  sprintDeadlineDate.value = formState.deadlineDate;
  evalWorkloadMethod.value = option?.evalWorkloadMethod?.value;
};

const loadedByIn = (options: TaskInfo[]) => {
  sprintDeadlineDate.value = options?.[0]?.deadlineDate || '';
  formState.deadlineDate = options?.[0]?.deadlineDate || '';
};

const taskTypeChange = () => {
  formState.targetParentId = undefined;
  formState.targetId = undefined;
  if (!formState.testerId && formState.taskType === 'BUG') {
    formState.testerId = userId.value;
  }
};

const assignToMe = (key:'assigneeId'|'confirmorId'|'testerId') => {
  if (key === 'assigneeId') {
    formState.assigneeId = userId.value;
    return;
  }

  if (key === 'confirmorId') {
    formState.confirmorId = userId.value;
  }

  if (key === 'testerId') {
    formState.testerId = userId.value;
  }
};

const evalWorkloadValidateDate = async (_rule: Rule, value: string) => {
  if (!props.taskId) {
    return;
  }

  if (formState.actualWorkload) {
    if (!value) {
      return Promise.reject(new Error('请输入评估工作量'));
    }

    return Promise.resolve();
  }

  return Promise.resolve();
};

const actualWorkloadChange = (value: string) => {
  if (!value) {
    formRef.value.clearValidate('evalWorkload');
  }
};

const evalWorkloadChange = (value: string) => {
  if (!value) {
    formState.actualWorkload = '';
    formRef.value.clearValidate('evalWorkload');
  }
};

const validateDate = async (_rule: Rule, value: string) => {
  if (dayjs(value).isBefore(dayjs(), 'minute')) {
    return Promise.reject(new Error('截止时间必须是一个未来时间'));
  }

  // 编辑时不校验截止时间是否超过迭代的截止时间
  if (sprintDeadlineDate.value) {
    if (dayjs(value).isAfter(dayjs(sprintDeadlineDate.value), 'seconds')) {
      return Promise.reject(new Error(`超过了迭代截止时间(${sprintDeadlineDate.value})`));
    }
  }

  return Promise.resolve();
};

// 禁用日期组件当前时间之前的年月日
const disabledDate = (current: Dayjs) => {
  const today = dayjs().startOf('day');
  return current.isBefore(today, 'day');
};

const editorChange = (value: string) => {
  formState.description = value;
};

const editorLoading = (value: boolean) => {
  loading.value = value;
};

const upLoad = async function ({ file }: { file: File }) {
  if (!formState.attachments || formState.attachments.length >= 5 || loading.value) {
    return;
  }

  loading.value = true;
  const [error, { data = [] }] = await upload(file.originFileObj, { bizKey: 'angusTesterTaskAttachments' });
  loading.value = false;
  if (error) {
    return;
  }

  if (data && data.length > 0) {
    const attachments = data?.map(item => ({ name: item.name, url: item.url }));
    formState.attachments.push(...attachments);
  }
};

const delFile = (index: number) => {
  formState?.attachments?.splice(index, 1);
};

const isValid = async () => {
  const ruleKeys = [
    'name'
  ];

  if (formState.actualWorkload) {
    ruleKeys.push('evalWorkload');
  }

  return new Promise((resolve) => {
    formRef.value.validate(ruleKeys).then(async () => {
      return resolve(true);
    }).catch((errors:{errorFields:{errors:string[];name:string[];warnings:string;}[]}) => {
      if (errors.errorFields.length === 1) {
        const names = errors.errorFields[0].name;
        if (names.length === 1 && names[0] === 'deadlineDate') {
          return resolve(true);
        }
      }

      return resolve(false);
    });
  });
};

const getParams = () => {
  const params: FormState = {
    projectId: props.projectId,
    sprintId: formState.sprintId,
    name: formState.name,
    taskType: formState.taskType,
    assigneeId: formState.assigneeId,
    deadlineDate: formState.deadlineDate,
    priority: formState.priority,
    parentTaskId: formState.parentTaskId,
    testerId: formState.testerId,
    softwareVersion: formState.softwareVersion
  };

  if (props.parentTaskId) {
    params.parentTaskId = props.parentTaskId;
  }

  if (formState.confirmorId) {
    params.confirmorId = formState.confirmorId;
  }

  if (formState.moduleId && +formState.moduleId > 0) {
    params.moduleId = formState.moduleId;
  }

  if (formState.tagIds?.length) {
    params.tagIds = formState.tagIds;
  }

  if (formState.refTaskIds?.length) {
    params.refTaskIds = formState.refTaskIds;
  }

  if (formState.refCaseIds?.length) {
    params.refCaseIds = formState.refCaseIds;
  }

  if (formState.description) {
    params.description = formState.description;
  }

  if (formState.attachments?.length) {
    params.attachments = formState.attachments;
  }

  if (formState.evalWorkload) {
    params.evalWorkload = formState.evalWorkload;
  }

  if (props.taskId && formState.actualWorkload) {
    params.actualWorkload = formState.actualWorkload;
  }

  if (formState.taskType === 'BUG') {
    params.bugLevel = formState.bugLevel;
    params.missingBugFlag = formState.missingBugFlag;
  }

  if (formState.taskType === 'API_TEST') {
    params.testType = formState.testType;
    params.targetId = formState.targetId;
    params.targetParentId = formState.targetParentId;
  }

  if (formState.taskType === 'SCENARIO_TEST') {
    params.testType = formState.testType;
    params.targetId = formState.targetId;
  }

  return params;
};

const submit = async (continueFlag: boolean) => {
  // 如果是编辑任务,检查提交的数据相对旧数据有没有变动，有变动才发送请求
  if (props.taskId) {
    const equalFlag = isEqual(oldFormState, formState);
    if (equalFlag) {
      emit('update:visible', false);
      return;
    }
  }

  const descriptionValidFlag = !formState.description || formState.description?.length <= 8000;
  const validFlag = await isValid();
  if (!validFlag || !descriptionValidFlag) {
    return;
  }

  if (!props.taskId) {
    createHandler(continueFlag);
    return;
  }

  editHandler();
};

const createHandler = async (continueFlag = false) => {
  loading.value = true;
  const params = getParams();
  const [error, res] = await task.addTask(params);
  loading.value = false;
  if (error) {
    return;
  }

  notification.success('任务添加成功');
  emit('ok', res?.data);

  if (!continueFlag) {
    cancel();
  }
};

const editHandler = async () => {
  loading.value = true;
  const params = getParams();
  const [error] = await task.putTask(props.taskId, params);
  loading.value = false;
  if (error) {
    return;
  }

  notification.success('任务编辑成功');
  const data = await loadData();
  emit('ok', data);
  cancel();
};

const cancel = () => {
  emit('update:taskId', undefined);
  emit('update:visible', false);
};

const loadData = async (): Promise<Partial<TaskInfo>> => {
  loading.value = true;
  const [error, res] = await task.getTaskDetail(props.taskId);
  loading.value = false;
  if (error || !res?.data) {
    return { id: props.taskId! };
  }

  return res.data;
};

const initialize = () => {
  zoomInFlag.value = !!localStore.get(zoomInFlagCacheKey.value);
};

const resetDefault = () => {
  loading.value = false;
  showEditorFlag.value = false;
  evalWorkloadMethod.value = 'STORY_POINT';
  sprintDeadlineDate.value = undefined;

  oldFormState = undefined;
  formState.attachments = [];
  formState.deadlineDate = dayjs().add(1, 'day').format('YYYY-MM-DD HH:mm:ss');
  if (dayjs(formState.deadlineDate).hour() > 19 || dayjs(formState.deadlineDate).hour() < 8) {
    formState.deadlineDate = dayjs(formState.deadlineDate).add(12, 'hour').format('YYYY-MM-DD HH:mm:ss');
  }
  formState.description = props.description || '';
  formState.evalWorkload = '';
  formState.actualWorkload = '';
  formState.name = props.name || '';
  formState.priority = 'MEDIUM';
  formState.bugLevel = 'MINOR';
  formState.parentTaskId = undefined;
  formState.sprintId = props.sprintId;
  formState.tagIds = [];
  formState.refTaskIds = [];
  formState.refCaseIds = props.refCaseIds || [];
  formState.targetId = undefined;
  formState.targetParentId = undefined;
  formState.taskType = props.taskType || 'TASK';
  formState.testType = 'FUNCTIONAL';
  formState.missingBugFlag = false;
  formState.softwareVersion = undefined;
  formState.assigneeId = props.assigneeId || props.userInfo?.id || undefined;
  formState.testerId = props.taskType === 'BUG' ? props.userInfo?.id : undefined;
  formState.confirmorId = props.confirmorId || undefined;
  if (props.moduleId && +props.moduleId > 0) {
    formState.moduleId = props.moduleId;
  } else {
    formState.moduleId = undefined;
  }
};

onMounted(() => {
  initialize();

  watch(() => props.visible, async () => {
    if (props.visible) {
      await getModuleTreeData();
      if (typeof formRef.value?.clearValidate === 'function') {
        await formRef.value.clearValidate();
      }

      if (!props.taskId) {
        resetDefault();
        return;
      }

      const data = await loadData();
      if (!data) {
        resetDefault();
        return;
      }

      const assigneeId = data.assigneeId;
      formState.assigneeId = assigneeId;
      assigneeDefaultOptions.value = {
        [assigneeId]: {
          fullName: data.assigneeName,
          id: assigneeId
        }
      };

      const confirmorId = data.confirmorId;
      formState.confirmorId = confirmorId;
      confirmorDefaultOptions.value = {
        [confirmorId]: {
          fullName: data.confirmorName,
          id: confirmorId
        }
      };

      formState.attachments = data.attachments || [];
      formState.moduleId = data.moduleId ? (+data.moduleId < 0 ? undefined : data.moduleId) : undefined;
      formState.deadlineDate = data.deadlineDate;
      formState.description = data.description;
      formState.evalWorkload = data.evalWorkload;
      formState.actualWorkload = data.actualWorkload;
      formState.name = data.name;
      formState.priority = data.priority?.value;
      formState.parentTaskId = data.parentTaskId;
      formState.sprintId = data.sprintId;
      formState.tagIds = data.tags?.map(item => item.id);
      formState.refTaskIds = data.refTaskInfos?.map(item => item.id);
      formState.refCaseIds = data.refCaseInfos?.map(item => item.id);
      formState.targetId = data.targetId;
      formState.taskType = data.taskType?.value;
      formState.testType = data.testType?.value;
      formState.targetParentId = data.targetParentId;
      formState.testerId = data.testerId;
      formState.missingBugFlag = data.missingBugFlag || false;
      formState.bugLevel = data.bugLevel?.value || 'MINOR';
      formState.softwareVersion = data.softwareVersion;

      oldFormState = cloneDeep(formState);

      evalWorkloadMethod.value = data.evalWorkloadMethod?.value;
      showEditorFlag.value = true;
    }
  }, { immediate: true });
});

const taskTypeExcludes = (data: { value: TaskInfo['taskType']['value']; message: string }) => {
  const value = data.value;
  const type = formState.taskType;
  if (props.taskId) {
    if (type === 'API_TEST') {
      return value !== 'API_TEST';
    }

    if (type === 'SCENARIO_TEST') {
      return value !== 'SCENARIO_TEST';
    }

    return ['API_TEST', 'SCENARIO_TEST'].includes(value);
  }

  return false;
};

const taskIdExcludes = (data: { id: string }) => {
  return props.taskId === data.id;
};

const userId = computed(() => {
  return props.userInfo?.id;
});

const title = computed(() => {
  if (props.taskId) {
    return '编辑任务';
  }

  return '添加任务';
});

const taskTypeReadonly = computed(() => {
  return !!(props.taskId && (formState.taskType === 'API_TEST' || formState.taskType === 'SCENARIO_TEST')) || !!props.taskType;
});

const showEditor = computed(() => {
  return props.visible && (showEditorFlag.value || !props.taskId);
});

const showTestType = computed(() => {
  const taskType = formState.taskType;
  if (!taskType) {
    return false;
  }

  return ['SCENARIO_TEST', 'API_TEST'].includes(taskType);
});

const descRichRef = ref();
const validateDesc = async () => {
  if (descRichRef.value && descRichRef.value.getLength() > 6000) {
    return Promise.reject(new Error('描述最大支持6000个字符'));
  }
  return Promise.resolve();
};

// const descriptionError = computed(() => {
//   if (!formState.description) {
//     return false;
//   }

//   return formState.description.length > 8000;
// });

const modalStyle = computed(() => {
  if (zoomInFlag.value) {
    return {
      width: '100%'
    };
  }

  return {
    width: '1000px'
  };
});

const formStyle = computed(() => {
  if (zoomInFlag.value) {
    return {
      height: '86vh'
    };
  }

  return {
    height: '75vh',
    minHeight: '665px'
  };
});

const zoomInFlagCacheKey = computed(() => {
  return `${props.userInfo?.id}${props?.projectId}${btoa('modalSize')}`;
});

const EDTIOR_OPTIONS = {
  menubar: false,
  height: 340
};

const UPLOAD_OPTIONS = { bizKey: 'angusTesterTaskAttachments' };
</script>
<template>
  <Modal
    :title="title"
    :centered="true"
    :style="modalStyle"
    :visible="props.visible"
    class="relative max-w-full"
    @cancel="cancel">
    <Tooltip :title="zoomInFlag ? '缩小' : '全屏'">
      <Icon
        :icon="zoomInFlag ? 'icon-tuichuzuida' : 'icon-zuidahua'"
        class="absolute right-10 top-3.5 text-3.5 cursor-pointer"
        @click="zoomToggle" />
    </Tooltip>

    <Form
      ref="formRef"
      :model="formState"
      :labelCol="{style: {width: '90px'}}"
      layout="vertical"
      class="overflow-y-auto overflow-x-hidden"
      size="small"
      :style="formStyle">
      <div class="flex">
        <div class="flex-1 pr-8">
          <FormItem
            name="name"
            label="名称"
            :rules="{ required: true, message: '任务名称，最大支持200字符' }">
            <Input
              v-model:value="formState.name"
              trim
              :maxlength="200"
              placeholder="任务名称，最大支持200字符" />
          </FormItem>

          <div class="flex space-x-4">
            <FormItem
              v-if="!!formState.taskType"
              name="taskType"
              class="flex-1/2"
              required>
              <template #label>
                类型
                <Popover>
                  <template #content>
                    <div class="flex items-center leading-5">
                      <div class="space-y-2 flex-shrink-0">
                        <div class="flex items-center">
                          <IconTask value="REQUIREMENT" class="mr-1 text-4" />
                          <span>需求</span>
                        </div>
                        <div class="flex items-center">
                          <IconTask value="STORY" class="mr-1 text-4" />
                          <span>故事</span>
                        </div>
                        <div class="flex items-center">
                          <IconTask value="TASK" class="mr-1 text-4" />
                          <span>任务</span>
                        </div>
                        <div class="flex items-center">
                          <IconTask value="BUG" class="mr-1 text-4" />
                          <span>缺陷</span>
                        </div>
                        <div class="flex items-center">
                          <IconTask value="API_TEST" class="mr-1 text-4" />
                          <span>接口测试</span>
                        </div>
                        <div class="flex items-center">
                          <IconTask value="SCENARIO_TEST" class="mr-1 text-4" />
                          <span>场景测试</span>
                        </div>
                      </div>
                      <div class="ml-3.5 space-y-2">
                        <div>用户对业务、功能等的期望或要求，描述有广泛性。</div>
                        <div>用户想要实现的具体目标或期望，通常以简洁、可理解的语言进行描述，如：作为 &lt;角色&gt;，我希望 &lt;目标&gt;，以便 &lt;收益&gt;。</div>
                        <div>一般工作事项或活动</div>
                        <div>程序错误或漏洞</div>
                        <div>接口的功能、性能、稳定性测试任务</div>
                        <div>场景的功能、性能、稳定性测试任务</div>
                      </div>
                    </div>
                  </template>
                  <Icon icon="icon-tishi1" class="text-tips ml-1 cursor-pointer text-3.5" />
                </Popover>
              </template>
              <SelectEnum
                v-model:value="formState.taskType"
                :allowClear="false"
                :excludes="taskTypeExcludes"
                :readonly="taskTypeReadonly"
                internal
                enumKey="TaskType"
                placeholder="请选择任务类型"
                style="width: 280px;"
                @change="taskTypeChange">
                <template #option="record">
                  <div class="flex items-center">
                    <IconTask :value="record.value" class="text-4 flex-shrink-0" />
                    <span class="ml-1">{{ record.message }}</span>
                  </div>
                </template>
              </SelectEnum>
            </FormItem>
            <FormItem
              name="priority"
              label="优先级"
              class="flex-1/2"
              required>
              <SelectEnum
                v-model:value="formState.priority"
                :allowClear="false"
                internal
                enumKey="Priority"
                placeholder="请选择优先级">
                <template #option="record">
                  <TaskPriority :value="record" />
                </template>
              </SelectEnum>
            </FormItem>
          </div>

          <template v-if="formState.taskType === 'BUG'">
            <div class="flex space-x-4">
              <FormItem
                name="bugLevel"
                label="缺陷等级"
                class="flex-1/2">
                <SelectEnum
                  v-model:value="formState.bugLevel"
                  enumKey="BugLevel"
                  style="width: 280px;"
                  :allowClear="false"
                  :lazy="false" />
              </FormItem>
              <FormItem
                name="missingBugFlag"
                label="是否漏测缺陷"
                class="flex-1/2">
                <Select
                  v-model:value="formState.missingBugFlag"
                  :options="[{value: true, label: '是'}, {value: false, label: '否'}]">
                </Select>
              </FormItem>
            </div>
          </template>

          <div v-if="formState.taskType === 'SCENARIO_TEST'" class="flex space-x-4">
            <FormItem
              name="targetId"
              label="场景"
              class="flex-1 min-w-0"
              :rules="{ required: true, message: '请选择场景' }">
              <Select
                v-model:value="formState.targetId"
                showSearch
                internal
                placeholder="请选择场景"
                :fieldNames="{ label: 'name', value: 'id' }"
                :action="`${TESTER}/scenario?projectId=${props.projectId}&fullTextSearch=true`"
                :readonly="!!props.taskId" />
            </FormItem>

            <FormItem
              v-if="showTestType"
              name="testType"
              label="测试类型"
              class="flex-1"
              required>
              <SelectEnum
                v-model:value="formState.testType"
                :allowClear="false"
                internal
                enumKey="TestType"
                placeholder="请选择测试类型"
                style="width: 280px;" />
            </FormItem>
          </div>

          <template v-if="formState.taskType === 'API_TEST'">
            <FormItem
              name="targetParentId"
              label="所属服务"
              :rules="{ required: true, message: '请选择服务' }">
              <Select
                v-model:value="formState.targetParentId"
                :action="`${TESTER}/services?projectId=${props.projectId}&fullTextSearch=true`"
                :fieldNames="{ value: 'id', label: 'name' }"
                :readonly="!!props.taskId"
                :lazy="false"
                internal
                defaultActiveFirstOption
                showSearch
                placeholder="选择或查询服务">
                <template #option="record">
                  <div class="text-3 leading-3 flex items-center h-6.5">
                    <IconText
                      class="mr-1"
                      style="width:16px;height: 16px;"
                      :text="record.targetType?.value === 'PROJECT' ? 'P' : 'S'"
                      :class="record.targetType?.value === 'PROJECT' ? 'bg-blue-badge-p' : 'bg-blue-badge-s'" />
                    <div :title="record.name" class="truncate">{{ record.name }}</div>
                  </div>
                </template>
              </Select>
            </FormItem>

            <div class="flex space-x-4">
              <FormItem
                label="接口"
                name="targetId"
                class="flex-1 min-w-0"
                :rules="{ required: true, message: '请选择接口' }">
                <Select
                  v-model:value="formState.targetId"
                  showSearch
                  internal
                  placeholder="请选择接口"
                  :fieldNames="{ label: 'summary', value: 'id' }"
                  :action="`${TESTER}/apis?projectId=${props.projectId}&serviceId=${formState.targetParentId}&fullTextSearch=true`"
                  :readonly="!!props.taskId || !formState.targetParentId" />
              </FormItem>

              <FormItem
                v-if="showTestType"
                name="testType"
                label="测试类型"
                class="flex-1"
                required>
                <SelectEnum
                  v-model:value="formState.testType"
                  :allowClear="false"
                  internal
                  enumKey="TestType"
                  placeholder="请选择测试类型"
                  style="width: 280px;" />
              </FormItem>
            </div>
          </template>

          <div class="flex space-x-4">
            <FormItem
              name="assigneeId"
              class="flex-1/2"
              :rules="{ required: true, message: '选择经办人' }">
              <template #label>
                经办人<Popover placement="rightTop">
                  <template #content>
                    <div class="text-3 text-theme-sub-content max-w-75 leading-4">
                      负责实际执行分配的工作任务。
                    </div>
                  </template>
                  <Icon icon="icon-tishi1" class="text-tips ml-1 text-3.5" />
                </Popover>
              </template>
              <div class="flex items-center ">
                <SelectUser
                  v-model:value="formState.assigneeId"
                  placeholder="选择经办人"
                  internal
                  class="flex-1 min-w-0"
                  :defaultOptions="assigneeDefaultOptions"
                  :action="`${TESTER}/project/${props.projectId}/member/user`"
                  :maxlength="80" />
                <Button
                  size="small"
                  type="link"
                  class="p-0 h-5 leading-5 ml-1"
                  @click="assignToMe('assigneeId')">
                  指派给我
                </Button>
              </div>
            </FormItem>

            <FormItem name="confirmorId" class="flex-1/2">
              <template #label>
                确认人<Popover placement="rightTop">
                  <template #content>
                    <div class="text-3 text-theme-sub-content max-w-75 leading-4">
                      负责核实和验证工作结果的人员，指定后会自动进入确认流程。
                    </div>
                  </template>
                  <Icon icon="icon-tishi1" class="text-tips ml-1 text-3.5" />
                </Popover>
              </template>
              <div class="flex items-center">
                <SelectUser
                  v-model:value="formState.confirmorId"
                  placeholder="选择确认人"
                  internal
                  allowClear
                  class="flex-1 min-w-0"
                  :defaultOptions="confirmorDefaultOptions"
                  :action="`${TESTER}/project/${props.projectId}/member/user`"
                  :maxlength="80" />
                <Button
                  size="small"
                  type="link"
                  class="p-0 h-5 leading-5 ml-1"
                  @click="assignToMe('confirmorId')">
                  指派给我
                </Button>
              </div>
            </FormItem>
          </div>

          <div class="flex space-x-4">
            <FormItem
              label="截止时间"
              name="deadlineDate"
              class="flex-1/2"
              :rules="{ required: true, validator: validateDate }">
              <DatePicker
                v-model:value="formState.deadlineDate"
                :showNow="false"
                :disabledDate="disabledDate"
                :showTime="{ hideDisabledOptions: true, format: 'HH:mm:ss' }"
                type="date"
                size="small"
                showToday
                class="w-full" />
            </FormItem>

            <FormItem name="confirmorId" class="flex-1/2">
              <template #label>
                测试人<Popover placement="rightTop">
                  <template #content>
                    <div class="text-3 text-theme-sub-content max-w-75 leading-4">
                      负责测试和验证产品功能或服务质量的人员。
                    </div>
                  </template>
                  <Icon icon="icon-tishi1" class="text-tips ml-1 text-3.5" />
                </Popover>
              </template>
              <div class="flex items-center">
                <SelectUser
                  v-model:value="formState.testerId"
                  placeholder="选择测试人"
                  internal
                  allowClear
                  class="flex-1 min-w-0"
                  :defaultOptions="confirmorDefaultOptions"
                  :action="`${TESTER}/project/${props.projectId}/member/user`"
                  :maxlength="80" />
                <Button
                  size="small"
                  type="link"
                  class="p-0 h-5 leading-5 ml-1"
                  @click="assignToMe('testerId')">
                  指派给我
                </Button>
              </div>
            </FormItem>
          </div>

          <FormItem
            name="description"
            label="描述"
            :rules="{validator: validateDesc}">
            <AsyncComponent :visible="showEditor">
              <RichEditor
                ref="descRichRef"
                :value="formState.description"
                :options="{placeholder: '任务描述，最大支持6000个字符'}"
                :height="340"
                @change="editorChange"
                @loadingChange="editorLoading" />
            </AsyncComponent>
          </FormItem>
        </div>
        <div class="w-80  pl-2 border-l">
          <FormItem
            label="所属迭代"
            name="sprintId">
            <Select
              v-model:value="formState.sprintId"
              :action="`${TESTER}/task/sprint?projectId=${props.projectId}&fullTextSearch=true`"
              :fieldNames="{ value: 'id', label: 'name' }"
              :readonly="!!props.taskId"
              showSearch
              internal
              placeholder="选择或查询迭代"
              @change="sprintChange">
              <template #option="record">
                <div class="flex items-center" :title="record.name">
                  <Icon icon="icon-jihua" class="mr-1 text-4" />
                  <div style="max-width: 220px;" class="truncate">{{ record.name }}</div>
                </div>
              </template>
            </Select>
          </FormItem>

          <FormItem label="所属模块" name="moduleId">
            <TreeSelect
              v-model:value="formState.moduleId"
              :treeData="moduleTreeData"
              :fieldNames="{ value: 'id', label: 'name', children: 'children' }"
              :getPopupContainer="getPopupContainer"
              :virtual="false"
              size="small"
              showSearch
              allowClear
              placeholder="选择或查模块">
              <template #title="item">
                <div class="flex items-center" :title="item.name">
                  <Icon icon="icon-mokuai" class="mr-1 text-3.5" />
                  <div style="max-width: 220px;" class="truncate">{{ item.name }}</div>
                </div>
              </template>
            </TreeSelect>
          </FormItem>

          <FormItem label="父任务" name="parentTaskId">
            <Select
              v-if="!!props.parentTaskId"
              :readonly="true"
              :value="props.parentTaskId"
              :fieldNames="{ label: 'name', value: 'id' }"
              :action="`${TESTER}/task?projectId=${props.projectId}&fullTextSearch=true`">
              <template #option="record">
                <div class="flex items-center">
                  <IconTask :value="record.taskType?.value" class="text-4 flex-shrink-0" />
                  <span class="ml-1.5">{{ record.name }}</span>
                </div>
              </template>
            </Select>

            <Select
              v-else
              v-model:value="formState.parentTaskId"
              showSearch
              internal
              placeholder="请选择父任务"
              :excludes="taskIdExcludes"
              :fieldNames="{ label: 'name', value: 'id' }"
              :action="`${TESTER}/task?projectId=${props.projectId}&fullTextSearch=true`">
              <template #option="record">
                <div class="flex items-center">
                  <IconTask :value="record.taskType?.value" class="text-4 flex-shrink-0" />
                  <span class="ml-1.5">{{ record.name }}</span>
                </div>
              </template>
            </Select>
          </FormItem>

          <FormItem
            name="evalWorkload"
            :rules="{ required: formState.actualWorkload, validator: evalWorkloadValidateDate, trigger: 'change' }">
            <template #label>
              {{ evalWorkloadMethod === 'STORY_POINT' ? '评估故事点' : '评估工时' }}
              <Popover placement="rightTop">
                <template #content>
                  <div class="text-3 text-theme-sub-content max-w-75 leading-4">
                    {{ evalWorkloadMethod === 'STORY_POINT' ? '工作任务量、综合难度、复杂度等的评估值。' : '工作评估所花费的时间，以小时为单位计算。' }}
                  </div>
                </template>
                <Icon icon="icon-tishi1" class="text-tips ml-1 cursor-pointer text-3.5" />
              </Popover>
            </template>
            <Input
              v-model:value="formState.evalWorkload"
              size="small"
              dataType="float"
              trimAll
              :min="0.1"
              :max="1000"
              placeholder="最小0.1，最大1000，最多支持2位小数"
              @blur="evalWorkloadChange($event.target.value)" />
          </FormItem>

          <template v-if="!!props.taskId">
            <FormItem name="actualWorkload">
              <template #label>
                {{ evalWorkloadMethod === 'STORY_POINT' ? '实际故事点' : '实际工时' }}
                <Popover placement="rightTop">
                  <template #content>
                    <div class="text-3 text-theme-sub-content max-w-75 leading-4">
                      {{ evalWorkloadMethod === 'STORY_POINT' ? '工作任务量、综合难度、复杂度等的实际值。' : '工作实际所花费的时间，以小时为单位计算。' }}
                    </div>
                  </template>
                  <Icon icon="icon-tishi1" class="text-tips ml-1 cursor-pointer text-3.5" />
                </Popover>
              </template>
              <Input
                v-model:value="formState.actualWorkload"
                class="w-70"
                size="small"
                dataType="float"
                trimAll
                placeholder="最小0.1，最大1000，最多支持2位小数"
                :min="0.1"
                :max="1000"
                @change="actualWorkloadChange($event.target.value)" />
            </FormItem>
          </template>

          <FormItem
            name="softwareVersion"
            label="软件版本">
            <Select
              v-model:value="formState.softwareVersion"
              allowClear
              placeholder="请选择所属版本"
              :action="`${TESTER}/software/version?projectId=${props.projectId}`"
              :params="{filters: [{value: ['NOT_RELEASED', 'RELEASED'], key: 'status', op: 'IN'}]}"
              :fieldNames="{value:'name', label: 'name'}">
            </Select>
          </FormItem>

          <FormItem
            name="tagIds"
            class="relative">
            <template #label>
              标签<Popover placement="rightTop">
                <template #content>
                  <div class="text-3 text-theme-sub-content max-w-75 leading-4">
                    “任务标签”可以帮助您组织有共同点的任务，例如“项目名称、迭代版本、业务标识”等。
                  </div>
                </template>
                <Icon icon="icon-tishi1" class="text-tips ml-1 text-3.5" />
              </Popover>
            </template>
            <Select
              v-model:value="formState.tagIds"
              showSearch
              internal
              :fieldNames="{ label: 'name', value: 'id' }"
              :maxTagCount="5"
              :maxTagTextLength="15"
              :maxTags="5"
              :allowClear="false"
              :action="`${TESTER}/tag?projectId=${props.projectId}&fullTextSearch=true`"
              placeholder="最多可添加5个标签"
              mode="multiple"
              notFoundContent="请联系管理员，前往”应用管理“-”任务管理“-”任务标签“配置任务标签。" />
          </FormItem>

          <FormItem
            name="refTaskIds"
            label="关联任务"
            class="relative">
            <Select
              v-model:value="formState.refTaskIds"
              showSearch
              internal
              :allowClear="false"
              :fieldNames="{ label: 'name', value: 'id' }"
              :maxTagCount="10"
              :maxTagTextLength="15"
              :maxTags="20"
              :action="`${TESTER}/task?projectId=${props.projectId}&fullTextSearch=true`"
              placeholder="最多可关联20个任务"
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
                    <span class="inline-block transform-gpu">已逾期</span>
                  </div>
                </div>
              </template>
            </Select>
          </FormItem>

          <FormItem
            name="refCaseIds"
            label="关联用例"
            class="relative">
            <Select
              v-model:value="formState.refCaseIds"
              showSearch
              internal
              :allowClear="false"
              :fieldNames="{ label: 'name', value: 'id' }"
              :maxTagCount="10"
              :maxTagTextLength="15"
              :maxTags="20"
              :action="`${TESTER}/func/case?projectId=${props.projectId}&fullTextSearch=true`"
              placeholder="最多可关联20个用例"
              mode="multiple">
              <template #option="record">
                <div class="flex items-center leading-4.5 overflow-hidden">
                  <Icon icon="icon-gongnengyongli" class="text-4 flex-shrink-0" />
                  <div class="link truncate ml-1" :title="record.name">
                    {{ record.name }}
                  </div>
                  <div
                    v-if="record.overdue"
                    class="flex-shrink-0 border border-status-error rounded px-0.5 ml-2"
                    style="transform: scale(0.9);color: rgba(245, 34, 45, 100%);line-height: 16px;">
                    <span class="inline-block transform-gpu">已逾期</span>
                  </div>
                </div>
              </template>
            </Select>
          </FormItem>

          <FormItem label="附件">
            <!-- <div style="height: 90px; border-color: rgba(0, 119, 255);background-color: rgba(0, 119, 255, 4%);"
              class="border border-dashed rounded px-2 py-1">
              <Upload
                :fileList="[]"
                name="file"
                class="w-full"
                :customRequest="() => { }"
                @change="upLoad">
                <div class="max-w-75">
                  <div class="flex items-center justify-center text-center mt-1">
                    <Icon icon="icon-lianjie1" class="mr-1 text-3 text-theme-special" />
                    <span class="whitespace-nowrap text-3">添加附件,最多上传5个附件</span>
                  </div>
                  <div class="mt-1 text-3 text-theme-sub-content whitespace-normal">支持的格式："jpg", "bmp", "png", "jpeg", "docx", "gif", "txt", "rar", "zip", "doc", "xlsx", "xls", "pdf"</div>
                </div>
              </Upload>
            </div> -->
            <div
              style="height: 60px; border-color: rgba(0, 119, 255);background-color: rgba(0, 119, 255, 4%);"
              class="border border-dashed rounded flex flex-col px-2 py-1"
              :class="formState?.attachments?.length?'justify-between':'justify-center'">
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
                    :customRequest="() => {}"
                    @change="upLoad">
                    <Icon icon="icon-shangchuan" class="text-theme-special mr-1" />
                    <span class="text-3 leading-3 text-theme-text-hover">继续上传</span>
                  </Upload>
                </div>
              </template>
              <template v-else>
                <div class="flex justify-center">
                  <Upload
                    name="file"
                    :fileList="[]"
                    :customRequest="() => {}"
                    @change="upLoad">
                    <Icon icon="icon-shangchuan" class="mr-1 text-theme-special" />
                    <span class="text-3 text-theme-text-hover">上传附件，最多上传5个</span>
                  </Upload>
                </div>
              </template>
            </div>
          </FormItem>

          <!-- <FormItem
            v-if="formState?.attachments?.length"
            name="attachments"
            :colon="false"
            label=" "
            class="-mt-4">
            <div class="leading-4 text-3 space-y-1.5 rounded px-3 py-2 bg-gray-bg">
              <div
                v-for="(item, index) in formState.attachments"
                :key="index"
                class="flex items-center justify-between">
                <div class="flex-1 truncate">{{ item.name }}</div>
                <Icon
                  icon="icon-qingchu"
                  class="text-3.5 text-theme-text-hover cursor-pointer flex-shrink-0"
                  @click="delFile(index)" />
              </div>
            </div>
          </FormItem> -->
        </div>
      </div>
    </Form>

    <template #footer>
      <Button
        class="text-3 leading-3"
        size="small"
        @click="cancel">
        取消
      </Button>
      <Button
        v-if="showContinue"
        type="primary"
        class="text-3 leading-3"
        size="small"
        :disabled="loading"
        @click="submit(true)">
        保存并继续添加
      </Button>
      <Button
        type="primary"
        class="text-3 leading-3"
        size="small"
        :disabled="loading"
        @click="submit(false)">
        确定
      </Button>
    </template>
  </Modal>
</template>

<style scoped>
:deep(.ant-form-item-label) {
  max-width: 120px;
  font-size: 12px;
}

:deep(.ant-form-item:not(.ant-form-item-has-error)) {
  margin-bottom: 20px;
}

:deep(.ant-form-item .ant-form-item-has-error) {
  margin-bottom: 20px;
}

:deep(.ant-form-item-with-help .ant-form-item-explain) {
  min-height: 20px;
}
</style>
