<script lang="ts" setup>
import { computed, inject, nextTick, onMounted, ref, watch } from 'vue';
import {
  DatePicker,
  Hints,
  Icon,
  IconTask,
  Input,
  Modal,
  notification,
  Select,
  SelectEnum,
  Spin,
  TaskPriority,
  Dropdown
} from '@xcan-angus/vue-ui';
import { Button, Form, FormItem, Tooltip, TreeSelect, Upload } from 'ant-design-vue';
import type { Rule } from 'ant-design-vue/es/form';
import { enumLoader, TESTER, upload, localStore, utils } from '@xcan-angus/infra';
import dayjs from 'dayjs';
import RichEditor from '@/components/richEditor/index.vue';
import { funcCase, modules, project } from '@/api/tester';

import { useI18n } from 'vue-i18n';
import { FormState } from './PropsType';
import CaseSteps from './caseSteps.vue';
import { CaseInfoObj, CaseListObj } from '../PropsType';

const { t } = useI18n();

interface Props {
  visible: boolean;
  editCase?:CaseListObj;
  moduleId?: string;
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  editCase: undefined
});

const emits = defineEmits<{(e: 'update:visible', value: boolean):void; (e: 'update', id?:string):void}>();

const userInfo: any = inject('userInfo');
const projectInfo = inject('projectInfo', ref({ id: '' }));
const addCaseSizeKey = `${userInfo.id}${projectInfo.value.id}addFuncCaseSize`;

const formRef = ref();
const formState = ref<FormState>({
  attachments: [],
  deadlineDate: '',
  description: '',
  evalWorkload: '',
  actualWorkload: '',
  moduleId: undefined,
  name: '',
  planId: undefined,
  precondition: '',
  priority: 'MEDIUM',
  steps: [],
  tagIds: [],
  testerId: userInfo?.id,
  refCaseIds: [],
  refTaskIds: [],
  developerId: undefined,
  softwareVersion: undefined,
  stepView: 'TABLE'
});

const oldFormState = ref<FormState>();

const detail = ref<CaseInfoObj>();
const getCaseInfo = async () => {
  const [error, { data }] = await funcCase.getCaseDetail(props.editCase.id);
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
      formState.value[key].refTaskIds = (data.refTaskInfos || []).map(item => {
        return item.id;
      });
    } else if (key === 'refCaseInfos') {
      formState.value[key].refCaseIds = (data.refCaseInfos || []).map(item => {
        return item.id;
      });
    } else if (key === 'actualWorkload') {
      formState.value.actualWorkload = data?.actualWorkload || data?.evalWorkload;
    } else if (key === 'priority') {
      formState.value.priority = data?.priority.value;
    } else if (key === 'precondition') {
      formState.value[key] = getJson(data?.[key]);
    } else if (key === 'stepView') {
      formState.value[key] = data?.[key]?.value || 'TABLE';
    } else {
      if (key === 'moduleId') {
        if (data?.[key] === '-1') {
          data[key] = undefined;
        }
      }
      formState.value[key] = data?.[key];
    }
  });

  detail.value = data;
  stepDefaultValue.value = data.steps;
  oldFormState.value = JSON.parse(JSON.stringify(formState.value));
};

const getJson = (value) => {
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

const close = () => {
  emits('update:visible', false);
};

const stepsRef = ref();
const resetForm = (type?:'save') => {
  formRef.value.resetFields(Object.keys(formState.value).filter(item => item !== 'planId' && item !== 'deadlineDate'));
  formState.value.refCaseIds = [];
  formState.value.refTaskIds = [];
  if (type !== 'save') {
    formState.value.steps = [];
    formState.value.description = '';
    stepDefaultValue.value = [];
    formState.value.moduleId = props.moduleId || undefined;
  }
  formRef.value.clearValidate();
};

const saveType = ref('save');
const save = (type: 'save' | 'add') => {
  if (loading.value) {
    return;
  }
  saveType.value = type;
  let _ruleKeys = [
    'name',
    'planId',
    'testerId',
    'deadlineDate'
  ];

  if (formState.value.actualWorkload) {
    _ruleKeys = [..._ruleKeys, 'evalWorkload'];
  }

  const _validateRuleKeys = _ruleKeys;
  formRef.value.validate(_validateRuleKeys)
    .then(async () => {
      if (formState.value.description?.length > 2000) {
        notification.warning('描述内容过长(最多2000字符),请减少内容');
        return;
      }

      if (props.editCase) {
        editSave();
      } else {
        addSave();
      }
    });
};

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
const editSave = async () => {
  if (utils.deepCompare(oldFormState.value, formState.value)) {
    emits('update:visible', false);
    return;
  }
  const params = getParams();
  loading.value = true;
  const [error] = await funcCase.putCase([{ id: props.editCase.id, ...params }]);
  loading.value = false;
  if (error) {
    return;
  }
  notification.success('修改成功');
  emits('update:visible', false);
  emits('update', props.editCase.id);
};
const addSave = async () => {
  const params = getParams();
  loading.value = true;
  const [error] = await funcCase.addCase([params]);
  loading.value = false;
  if (error) {
    return;
  }
  notification.success('添加成功');

  emits('update');
  if (saveType.value === 'save') {
    emits('update:visible', false);
    resetForm('save');
  }
};

const upLoadFile = async ({ file }: { file }) => {
  if (file.size > 100 * 1024 * 1024) {
    notification.error('上传文件或文本大小超过100MB，请重新上传');
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

const delFile = (index: number) => {
  formState.value?.attachments?.splice(index, 1);
};

const stepDefaultValue = ref<{ expectedResult: string; step: string;}[]>([]);

const disabledDate = (current) => {
  if (props.editCase) {
    return false;
  }
  const today = dayjs().startOf('day');
  return current.isBefore(today);
};

// 定义截止时间触发校验是否可以提交，true可以，false不可以（超过计划截止时间的校验可提交）
const validateDate = async (_rule: Rule, value: string) => {
  if (!value) {
    return Promise.reject(new Error('请选择截止时间'));
  } else if (props.editCase) {
    return Promise.resolve();
  } else if (dayjs(value).isBefore(dayjs(), 'minute')) {
    return Promise.reject(new Error('截止时间必须是一个未来时间'));
  } else {
    return Promise.resolve();
  }
};

// 指派测试人为当前登录人
const setTesterForMe = () => {
  if (userInfo?.id) {
    formState.value.testerId = userInfo.id;
  }
};

const members = ref([]);

const loadMembers = async () => {
  const [error, { data }] = await project.getProjectMember(projectInfo.value?.id);
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
  watch(() => projectInfo.value, (newValue) => {
    if (newValue.id) {
      loadMembers();
      getModuleTreeData();
    }
  }, {
    immediate: true
  });

  loadEnums();
});

const planParams = computed(() => {
  return { projectId: projectInfo.value?.id };
});

const evalWorkloadMethod = ref();
const planEndDate = ref<string>();
const planChange = (_value, options) => {
  formState.value.deadlineDate = _value ? options.deadlineDate : '';
  planEndDate.value = options?.deadlineDate;
  evalWorkloadMethod.value = options?.evalWorkloadMethod;
  if (formState.value.deadlineDate && dayjs(formState.value.deadlineDate).isBefore(dayjs())) {
    formState.value.deadlineDate = dayjs().add(2, 'hour').format('YYYY-MM-DD HH:mm:ss');
  }
  if (formState.value.deadlineDate && (dayjs(formState.value.deadlineDate).hour() > 19 || dayjs(formState.value.deadlineDate).hour() < 8)) {
    formState.value.deadlineDate = dayjs(formState.value.deadlineDate).add(12, 'hour').format('YYYY-MM-DD HH:mm:ss');
  }
};

const moduleTreeData = ref([]);
const getModuleTreeData = async () => {
  if (!projectInfo.value?.id) {
    return;
  }
  const [error, { data }] = await modules.getModuleTree({
    projectId: projectInfo.value.id
  });
  if (error) {
    return;
  }
  moduleTreeData.value = data || [];
};

const stepViewOpt = ref([]);
const loadEnums = async () => {
  const [error, data] = await enumLoader.load('EvalWorkloadMethod');
  const [error1, data1] = await enumLoader.load('CaseStepView');
  if (!error) {
    evalWorkloadMethod.value = data?.filter(item => item.value === 'STORY_POINT')[0];
  }
  if (!error1) {
    stepViewOpt.value = data1.map(i => ({ name: i.message, key: i.value }));
  }
};

const changeStepView = ({ key }) => {
  formState.value.stepView = key;
};

const conditionRichRef = ref();
const validateCondition = () => {
  if (!formState.value.precondition) {
    return Promise.resolve();
  }
  if (conditionRichRef.value && conditionRichRef.value.getLength() > 2000) {
    return Promise.reject('富文本字符不能超过2000');
  }
  // if (formState.value.precondition.length > 2000) {
  //   return Promise.reject('富文本字符不能超过2000');
  // }
  return Promise.resolve();
};

const descRichRef = ref();
const validateDesc = () => {
  if (!formState.value.description) {
    return Promise.resolve();
  }
  if (descRichRef.value && descRichRef.value.getLength() > 2000) {
    return Promise.reject('富文本字符不能超过2000');
  }
  // if (formState.value.description.length > 2000) {
  //   return Promise.reject('富文本字符不能超过2000');
  // }
  return Promise.resolve();
};

const isZoom = ref(typeof localStore.get(addCaseSizeKey) === 'boolean' ? localStore.get(addCaseSizeKey) : false);
const style = ref({ width: isZoom.value ? '100%' : '1200px' });
const handleZoom = () => {
  isZoom.value = !isZoom.value;
  localStore.set(addCaseSizeKey, isZoom.value);
  style.value.width = isZoom.value ? '100%' : '1200px';
};

const actualWorkloadChange = (value) => {
  if (!value) {
    formRef.value.clearValidate('evalWorkload');
  }
};

const evalWorkloadChange = (value) => {
  if (!value) {
    formState.value.actualWorkload = '';
    formRef.value.clearValidate('evalWorkload');
  }
};

const evalWorkloadValidateDate = async (_rule: Rule, value: string) => {
  if (formState.value.actualWorkload) {
    if (!value) {
      return Promise.reject(new Error('请输入评估工作量'));
    } else {
      return Promise.resolve();
    }
  } else {
    return Promise.resolve();
  }
};
</script>
<template>
  <Modal
    :title="props.editCase?'编辑功能用例':'添加功能用例'"
    :visible="props.visible"
    :footer="null"
    :style="style"
    class="relative max-w-full"
    @cancel="close">
    <Tooltip :title="isZoom?'恢复':'全屏'">
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
              label="名称"
              name="name"
              :rules="[{ required: true, message:t('请输入用例名称') }]">
              <Input
                v-model:value="formState.name"
                size="small"
                :maxlength="400"
                :placeholder="t('输入用例名称，最多可输入400字符')" />
            </FormItem>
            <FormItem
              name="precondition"
              :rules="[{validator: validateCondition}]">
              <template #label>
                <div class="text-3 flex space-x-2 items-center">
                  <span>前置条件</span> <Hints text="测试执行前需要满足的所有条件或环境配置。" />
                </div>
              </template>
              <RichEditor
                ref="conditionRichRef"
                v-model:value="formState.precondition"
                :height="100"
                placeholder="输入前置条件，最多支持2000字符。" />
              <!-- <Input
                v-model:value="formState.precondition"
                size="small"
                type="textarea"
                :autoSize="{ minRows: 6, maxRows: 6}"
                :maxlength="2000"
                :placeholder="t('输入前置条件，最多支持2000字符。')" /> -->
            </FormItem>
            <FormItem>
              <template #label>
                <div class="text-3 flex space-x-2 items-center">
                  <span>测试步骤</span>
                  <Dropdown
                    :value="[formState.stepView]"
                    :menuItems="stepViewOpt"
                    @click="changeStepView">
                    <span class="text-theme-special">切换类型 <Icon icon="icon-xiajiantou" /></span>
                  </Dropdown>
                  <Hints text="指执行测试用例时需要采取的具体操作步骤与预期结果，作为通过与否的判断标准。" />
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
                  <span>描述</span> <Hints text="对测试用例进行总体的补充说明和背景介绍，帮助理解测试的目的、范围和预期结果。" />
                </div>
              </template>
              <RichEditor
                ref="descRichRef"
                v-model:value="formState.description"
                class="add-case" />
            </FormItem>
          </div>
          <div style="width: 320px;" class="ml-5 h-full">
            <FormItem
              label="所属计划"
              name="planId"
              :rules="{required:true,message:'请选择所属计划'}">
              <Select
                v-model:value="formState.planId"
                :disabled="!!props.editCase"
                :action="`${TESTER}/func/plan?projectId=${projectInfo.id}&fullTextSearch=true`"
                :fieldNames="{ value: 'id', label: 'name' }"
                :params="planParams"
                :lazy="false"
                defaultActiveFirstOption
                showSearch
                placeholder="选择或查询计划"
                loadMode="infinity"
                @change="planChange">
                <template #option="item">
                  <div class="flex items-center" :title="item.name">
                    <Icon icon="icon-jihua" class="mr-1 text-3.5" />
                    <div style="max-width: 220px;" class="truncate">{{ item.name }}</div>
                  </div>
                </template>
              </Select>
            </FormItem>
            <FormItem
              label="所属模块"
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
                placeholder="选择或查模块">
                <template #title="item">
                  <div class="flex items-center" :title="item.name">
                    <Icon icon="icon-mokuai" class="mr-1 text-3.5" />
                    <div style="max-width: 220px;" class="truncate">{{ item.name }}</div>
                  </div>
                </template>
              </TreeSelect>
            </FormItem>
            <FormItem
              label="测试人"
              name="testerId"
              :rules="{required:true,message:'请选择测试人'}">
              <div class="flex items-center">
                <Select
                  v-model:value="formState.testerId"
                  :options="members"
                  class="flex-1"
                  size="small" />
                <Button
                  type="link"
                  size="small"
                  class="p-0 h-5 leading-5 ml-1"
                  @click="setTesterForMe">
                  指派给我
                </Button>
              </div>
            </FormItem>
            <FormItem
              label="开发人"
              name="developerId"
              :rules="{required:true,message:'请选择开发人'}">
              <Select
                v-model:value="formState.developerId"
                :options="members"
                class="flex-1"
                size="small" />
            </FormItem>
            <FormItem
              name="priority"
              required>
              <template #label>
                <span class="flex items-center">
                  优先级
                  <Tooltip
                    placement="right"
                    arrowPointAtCenter
                    :overlayStyle="{'max-width':'400px'}"
                    title="优先级用于标识用例的重要性和紧急程度。对于重要、实时性高或者会阻塞其他活动的用例请使用高优先级。">
                    <Icon icon="icon-tishi1" class="text-tips ml-1 cursor-pointer text-3.5" />
                  </Tooltip>
                </span>
              </template>
              <SelectEnum
                v-model:value="formState.priority"
                enumKey="Priority"
                size="small">
                <template #option="record">
                  <TaskPriority :value="record" />
                </template>
              </SelectEnum>
            </FormItem>
            <FormItem
              name="evalWorkload"
              :rules="{required: formState.actualWorkload, validator: evalWorkloadValidateDate,trigger: 'change' }">
              <template #label>
                <span class="flex items-center">
                  {{ evalWorkloadMethod?.value === 'STORY_POINT'? '评估故事点':'评估工时' }}
                  <Tooltip
                    placement="right"
                    arrowPointAtCenter
                    :overlayStyle="{'max-width':'400px'}"
                    :title="evalWorkloadMethod?.value === 'STORY_POINT'?'工作任务量、综合难度、复杂度等的评估值。':'工作评估所花费的时间，以小时为单位计算。'">
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
                :placeholder="t('最小0.1，最大1000，最多支持2位小数')"
                dataType="float"
                @blur="evalWorkloadChange($event.target.value)" />
            </FormItem>
            <template v-if="props.editCase">
              <FormItem
                name="actualWorkload">
                <template #label>
                  <span class="flex items-center">
                    {{ evalWorkloadMethod?.value === 'STORY_POINT'? '实际故事点':'实际工时' }}
                    <Tooltip
                      placement="right"
                      arrowPointAtCenter
                      :overlayStyle="{'max-width':'400px'}"
                      :title="evalWorkloadMethod?.value === 'STORY_POINT'?'工作任务量、综合难度、复杂度等的实际值。':'工作实际所花费的时间，以小时为单位计算。'">
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
                  :placeholder="t('最小0.1，最大1000，最多支持2位小数')"
                  dataType="float"
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
                :action="`${TESTER}/software/version?projectId=${projectInfo.id}`"
                :params="{filters: [{value: ['NOT_RELEASED', 'RELEASED'], key: 'status', op: 'IN'}]}"
                :fieldNames="{value:'name', label: 'name'}">
              </Select>
            </FormItem>
            <FormItem
              name="deadlineDate"
              :rules="{required: true, validator: validateDate,trigger: 'change' }">
              <template #label>
                <span class="flex items-center">
                  截止时间
                  <Tooltip
                    placement="right"
                    arrowPointAtCenter
                    :overlayStyle="{'max-width':'400px'}"
                    title="用例截止时间不能超过测试计划截止时间。">
                    <Icon icon="icon-tishi1" class="text-tips aml-1 cursor-pointer text-3.5" />
                  </Tooltip>
                </span>
              </template>
              <DatePicker
                v-model:value="formState.deadlineDate"
                :disabledDate="disabledDate"
                :showTime="{hideDisabledOptions: true,format:'HH:mm:ss'}"
                :disabled="!formState.planId"
                allowClear
                placeholder="请选择截止时间"
                size="small"
                type="date"
                class="w-full" />
            </FormItem>
            <FormItem
              name="tagIds">
              <template #label>
                <span>标签
                  <Tooltip
                    placement="right"
                    arrowPointAtCenter
                    :overlayStyle="{'max-width':'400px'}"
                    title="用于分类和快速检索，最多支持关联5个。">
                    <Icon icon="icon-tishi1" class="text-tips cursor-pointer text-3.5" />
                  </Tooltip>
                </span>
              </template>
              <Select
                v-model:value="formState.tagIds"
                showSearch
                :fieldNames="{label:'name',value:'id'}"
                :maxTagCount="5"
                :maxTagTextLength="15"
                :maxTags="5"
                :allowClear="false"
                :action="`${TESTER}/tag?projectId=${projectInfo.id}&fullTextSearch=true`"
                placeholder="选择或查询标签"
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
                :action="`${TESTER}/task?projectId=${projectInfo.id}&fullTextSearch=true`"
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
                :action="`${TESTER}/func/case?projectId=${projectInfo.id}&fullTextSearch=true`"
                placeholder="最多可关联20个用例"
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
              label="附件"
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
                      :customRequest="() => {}"
                      @change="upLoadFile">
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
                      @change="upLoadFile">
                      <Icon icon="icon-shangchuan" class="mr-1 text-theme-special" />
                      <span class="text-3 text-theme-text-hover">上传附件，最多上传5个</span>
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
              {{ t('保存') }}
            </Button>
            <Button
              v-if="!editCase"
              type="primary"
              size="small"
              htmlType="submit"
              class="px-3 ml-5"
              @click="save('add')">
              {{ t('保存并继续添加') }}
            </Button>
            <Button
              size="small"
              class="ml-5 px-3"
              @click="close">
              {{ t('取消') }}
            </Button>
          </div>
        </FormItem>
      </Form>
    </Spin>
  </Modal>
</template>
<style scoped>
:deep(.ant-select.ant-select-sm.ant-select-multiple .ant-select-selector) {
  max-height: 76px;
  overflow-y: auto;
}

:deep(.tox-tinymce-aux > .tox-textarea > textarea::placeholder) {
  color: red !important;  /* 这里可以设置你想要的颜色 */
}
</style>

<style>
.module_tree .ant-select-tree .ant-select-tree-node-content-wrapper {
  min-height: 24px;
  line-height: 24px;
}
</style>
