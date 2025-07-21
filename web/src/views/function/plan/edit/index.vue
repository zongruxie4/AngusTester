<script setup lang="ts">
import { computed, defineAsyncComponent, inject, nextTick, onMounted, ref, watch } from 'vue';
import {
  Button,
  Divider,
  Form,
  FormItem,
  Radio,
  RadioGroup,
  Switch,
  TabPane,
  Tabs,
  Upload
} from 'ant-design-vue';
import {
  AsyncComponent,
  AuthorizeModal,
  DatePicker,
  Icon,
  IconRequired,
  Input,
  modal,
  notification,
  Popover,
  Select,
  Spin,
  Tooltip
} from '@xcan-angus/vue-ui';
import { clipboard, utils, TESTER, enumLoader, upload } from '@xcan-angus/tools';
import dayjs from 'dayjs';
import { isEqual } from 'lodash-es';
import type { Rule } from 'ant-design-vue/es/form';
import { funcPlan, project } from '@/api/tester';

import { PlanInfo } from '../PropsType';
import { FormState } from './PropsType';

type Props = {
  projectId: string;
  userInfo: { id: string; };
  appInfo: { id: string; };
  data: {
    _id: string;
    id: string | undefined;
  }
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  data: undefined
});

const TesterSelect = defineAsyncComponent(() => import('./testerSelect.vue'));
const RichEditor = defineAsyncComponent(() => import('@/components/richEditor/index.vue'));

const updateTabPane = inject<(data: { [key: string]: any }) => void>('updateTabPane', () => ({}));
const deleteTabPane = inject<(keys: string[]) => void>('deleteTabPane', () => ({}));
const replaceTabPane = inject<(id: string, data: { [key: string]: any }) => void>('replaceTabPane', () => ({}));
const isAdmin = inject('isAdmin', ref(false));

const formRef = ref();

const loading = ref(false);
const dataSource = ref<PlanInfo>();

const testerSelectRef = ref();
const activeTabKey = ref('testingObjectives');

const evalWorkloadMethodOptions = ref<{ value: string, message: string }[]>([]);
const reviewFlagVisible = ref(false);

const permissions = ref<string[]>([]);
const oldFormState = ref<FormState>();
const _startDate = dayjs().format('YYYY-MM-DD HH:mm:ss');
const _deadlineDate = dayjs().add(1, 'month').format('YYYY-MM-DD HH:mm:ss');
const formState = ref<FormState>({
  projectId: props.projectId,
  casePrefix: '',
  description: '',
  evalWorkloadMethod: 'STORY_POINT',
  name: '',
  ownerId: props.userInfo?.id,
  review: true,
  startDate: _startDate,
  deadlineDate: _deadlineDate,
  attachments: [],
  date: [_startDate, _deadlineDate],
  testingObjectives: '',
  testingScope: '',
  otherInformation: '',
  acceptanceCriteria: '',
  testerResponsibilities: {}
});

const authorizeModalVisible = ref(false);

const validateDate = async (_rule: Rule, value: string) => {
  if (!value) {
    return Promise.reject(new Error('请选择计划时间'));
  } else if (!value[0]) {
    return Promise.reject(new Error('请选择计划开始时间'));
  } else if (!value[1]) {
    return Promise.reject(new Error('请选择计划截止时间'));
  } else {
    return Promise.resolve();
  }
};

const validateTester = async () => {
  if (Object.keys(formState.value.testerResponsibilities).length === 0) {
    return Promise.reject(new Error('请选择测试人员'));
  }
  if (testerSelectRef.value.validate()) {
    return Promise.resolve();
  } else {
    return Promise.reject(new Error('人员重复，请重新选择'));
  }
};

const validateRequired = async (value) => {
  if (formState.value[value.field]) {
    try {
      const valueField = JSON.parse(formState.value[value.field]);
      if (typeof valueField === 'object' && valueField.length > 1) {
        return Promise.resolve();
      }
      if (typeof valueField === 'object' && valueField.length === 1) {
        if (valueField[0].insert.replaceAll('\n', '')) {
          return Promise.resolve();
        }
      }
      return Promise.reject(new Error(value.message));
    } catch {
      return Promise.reject(new Error(value.message));
    }
  }
  return Promise.reject(new Error(value.message));
};

const objectiveRichRef = ref();
const scopeRichRef = ref();
const criteriaRichRef = ref();
const infoRichRef = ref();
const validateMaxLength = async (value) => {
  if (value.field === 'testingObjectives') {
    if (objectiveRichRef.value && objectiveRichRef.value.getLength() > 2000) {
      Promise.reject(new Error('字符不能超过2000'));
    }
  }
  if (value.field === 'otherInformation') {
    if (infoRichRef.value && infoRichRef.value.getLength() > 2000) {
      Promise.reject(new Error('字符不能超过2000'));
    }
  }
  if (value.field === 'acceptanceCriteria') {
    if (criteriaRichRef.value && criteriaRichRef.value.getLength() > 2000) {
      Promise.reject(new Error('字符不能超过2000'));
    }
  }
  if (value.field === 'testingScope') {
    if (scopeRichRef.value && scopeRichRef.value.getLength() > 2000) {
      Promise.reject(new Error('字符不能超过2000'));
    }
  }
  return Promise.resolve();
};

const changeTester = (data) => {
  formState.value.testerResponsibilities = data;
};

const upLoadFile = async (file) => {
  if (loading.value) {
    return;
  }

  const attachments = formState.value.attachments || [];
  if (attachments.length >= 10) {
    return;
  }

  loading.value = true;
  const [error, { data = [] }] = await upload(file.file, { bizKey: 'angusTesterCaseAttachments' });
  loading.value = false;
  if (error) {
    return;
  }

  if (data && data.length > 0) {
    const newData = data?.map(item => ({ name: item.name, url: item.url }));
    if (formState.value.attachments) {
      formState.value.attachments.push(...newData);
    } else {
      formState.value.attachments = newData;
    }
  }
};

const delFile = (index: number) => {
  formState.value?.attachments?.splice(index, 1);
};

const reviewFlagChange = (checked) => {
  if (checked) {
    formState.value.review = true;
    return;
  }

  reviewFlagVisible.value = true;
};

const handleReviewFlagOk = () => {
  formState.value.review = false;
  reviewFlagVisible.value = false;
};

const handleReviewFlagCancel = () => {
  reviewFlagVisible.value = false;
};

const getParams = () => {
  const params: FormState = { ...formState.value };
  if (dataSource.value?.id) {
    params.id = dataSource.value.id;
  }

  const _date = formState.value.date;
  if (_date) {
    params.startDate = _date[0];
    params.deadlineDate = _date[1];
  }

  if (!params.attachments?.length) {
    delete params.attachments;
  }

  if (!params.description) {
    delete params.description;
  }

  if (!params.casePrefix) {
    delete params.casePrefix;
  }

  delete params.date;

  return params;
};

const refreshList = () => {
  nextTick(() => {
    updateTabPane({ _id: 'planList', notify: utils.uuid() });
  });
};

const editOk = async () => {
  const equalFlag = isEqual(oldFormState.value, formState.value);
  if (equalFlag) {
    return;
  }

  const params = getParams();
  loading.value = true;
  const [error] = await funcPlan.putPlan(params);
  loading.value = false;
  if (error) {
    return;
  }

  notification.success('修改成功');

  const id = params.id;
  const name = params.name;
  updateTabPane({ _id: id, name });
  // 更新数据名称
  if (dataSource.value) {
    dataSource.value.name = name;
  }
};

const addOk = async () => {
  const params = getParams();
  loading.value = true;
  const [error, res] = await funcPlan.addPlan(params);
  loading.value = false;
  if (error) {
    return;
  }

  notification.success('添加成功');

  const _id = props.data?._id;
  const newId = res?.data?.id;
  const name = params.name;
  replaceTabPane(_id, { _id: newId, uiKey: newId, name, data: { _id: newId, id: newId } });
};

const validateMaxLen = (val) => {
  if (formState.value[val.field].length > 2000) {
    return Promise.reject('字符不能超过2000个字符');
  }
  return Promise.resolve();
};

const ok = async () => {
  formRef.value.validate().then(async () => {
    if (!editFlag.value) {
      await addOk();
    } else {
      await editOk();
    }

    refreshList();
  }).catch((error) => {
    if (error.errorFields.length === 1 && error.errorFields[0].name[0] === 'testingScope') {
      activeTabKey.value = 'testingScope';
    }
  });
};

const toStart = async () => {
  const id = dataSource.value?.id;
  if (!id) {
    return;
  }

  loading.value = true;
  const [error] = await funcPlan.startPlan(id);
  loading.value = false;
  if (error) {
    return;
  }

  notification.success('计划开始成功');
  loadData(id);
  refreshList();
};

const toCompleted = async () => {
  const id = dataSource.value?.id;
  if (!id) {
    return;
  }

  loading.value = true;
  const [error] = await funcPlan.endPlan(id);
  loading.value = false;
  if (error) {
    return;
  }

  notification.success('计划已完成');
  loadData(id);
  refreshList();
};

const toDelete = async () => {
  const data = dataSource.value;
  if (!data) {
    return;
  }

  modal.confirm({
    content: `确定删除计划【${data.name}】吗？`,
    async onOk () {
      const id = data.id;
      loading.value = true;
      const [error] = await funcPlan.deletePlan(id);
      loading.value = false;
      if (error) {
        return;
      }

      notification.success('计划删除成功， 您可以在回收站查看删除后的计划');
      deleteTabPane([id]);
      refreshList();
    }
  });
};

const toGrant = () => {
  authorizeModalVisible.value = true;
};

const authFlagChange = async () => {
  const id = dataSource.value?.id;
  if (!id) {
    return;
  }

  loadData(id);
  refreshList();
};

const toClone = async () => {
  const id = dataSource.value?.id;
  if (!id) {
    return;
  }

  loading.value = true;
  const [error] = await funcPlan.clonePlan(id);
  loading.value = false;
  if (error) {
    return;
  }

  notification.success('计划克隆成功');
  refreshList();
};

const toResetTestResult = async () => {
  const id = dataSource.value?.id;
  if (!id) {
    return;
  }

  const params = { ids: [id] };
  loading.value = true;
  const [error] = await funcPlan.resetCaseResult(params, { paramsType: true });
  loading.value = false;
  if (error) {
    return;
  }

  notification.success('计划重置测试成功');
  loadData(id);
  refreshList();
};

const toResetReviewResult = async () => {
  const id = dataSource.value?.id;
  if (!id) {
    return;
  }

  const params = { ids: [id] };
  loading.value = true;
  const [error] = await funcPlan.resetCaseReview(params, { paramsType: true });
  loading.value = false;
  if (error) {
    return;
  }

  notification.success('计划重置评审成功');
  loadData(id);
  refreshList();
};

const toCopyLink = () => {
  const id = dataSource.value?.id;
  if (!id) {
    return;
  }

  clipboard.toClipboard(window.location.origin + `/function#plans?id=${id}`).then(() => {
    notification.success('复制链接成功');
  }).catch(() => {
    notification.error('复制链接失败');
  });
};

const toRefresh = () => {
  const id = dataSource.value?.id;
  if (!id) {
    return;
  }

  loadData(id);
};

const loadData = async (id: string) => {
  if (loading.value) {
    return;
  }

  loading.value = true;
  const [error, res] = await funcPlan.getPlanDetail(id);
  loading.value = false;
  if (error) {
    return;
  }

  const data = res?.data as PlanInfo;
  if (!data) {
    return;
  }

  dataSource.value = data;
  setFormData(data);

  const name = data.name;
  if (name && typeof updateTabPane === 'function') {
    updateTabPane({ name, _id: id });
  }
};

const setFormData = (data: PlanInfo) => {
  reviewFlagVisible.value = false;
  if (!data) {
    const startDate = dayjs().format('YYYY-MM-DD HH:mm:ss');
    const deadlineDate = dayjs().add(1, 'month').format('YYYY-MM-DD HH:mm:ss');
    formState.value = {
      casePrefix: '',
      description: '',
      evalWorkloadMethod: 'STORY_POINT',
      name: '',
      review: true,
      startDate,
      deadlineDate,
      attachments: [],
      date: [startDate, deadlineDate],
      ownerId: props.userInfo?.id,
      projectId: props.projectId,
      testingObjectives: '',
      testingScope: '',
      otherInformation: '',
      acceptanceCriteria: '',
      testerResponsibilities: {}
    };

    return;
  }

  const {
    casePrefix = '',
    description = '',
    projectId = '',
    evalWorkloadMethod,
    name = '',
    ownerId = '',
    attachments = '',
    startDate = '',
    deadlineDate = '',
    review = false,
    testingObjectives = '',
    testingScope = '',
    otherInformation = '',
    acceptanceCriteria = '',
    testerResponsibilities = {}
  } = data;

  formState.value.casePrefix = casePrefix;
  formState.value.description = description;
  formState.value.projectId = projectId;
  formState.value.deadlineDate = deadlineDate;
  formState.value.evalWorkloadMethod = evalWorkloadMethod?.value || '';
  formState.value.name = name;
  formState.value.ownerId = ownerId;
  formState.value.review = review;
  formState.value.startDate = startDate;
  formState.value.attachments = attachments || [];
  formState.value.date = [startDate, deadlineDate];

  formState.value.testingObjectives = getJson(testingObjectives);
  formState.value.testingScope = getJson(testingScope);
  formState.value.otherInformation = getJson(otherInformation);
  formState.value.acceptanceCriteria = getJson(acceptanceCriteria);
  formState.value.testerResponsibilities = testerResponsibilities || {};
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

const loadEnums = async () => {
  const [error, data] = await enumLoader.load('EvalWorkloadMethod');
  if (error) {
    return;
  }

  evalWorkloadMethodOptions.value = data as { message: string; value: string; }[];
};

const loadPermissions = async (id: string) => {
  if (isAdmin.value) {
    permissions.value = [
      'MODIFY_PLAN',
      'DELETE_PLAN',
      'ADD_CASE',
      'MODIFY_CASE',
      'DELETE_CASE',
      'EXPORT_CASE',
      'REVIEW',
      'RESET_REVIEW_RESULT',
      'TEST',
      'RESET_TEST_RESULT',
      'GRANT',
      'VIEW'
    ];

    return;
  }

  const params = {
    admin: true
  };
  loading.value = true;
  const [error, res] = await funcPlan.getCurrentAuthByPlanId(id, params);
  loading.value = true;
  if (error) {
    return;
  }

  permissions.value = (res?.data?.permissions || []).map(item => item.value);
};

const members = ref([]);

const loadMembers = async () => {
  const [error, res] = await project.getMemberUser(props.projectId);
  if (error) {
    return;
  }

  const data = res?.data || [];
  members.value = (data || []).map(i => {
    return {
      ...i,
      label: i.fullName,
      value: i.id
    };
  });
};

onMounted(() => {
  loadEnums();
  loadMembers();

  watch(() => props.data, async (newValue, oldValue) => {
    const id = newValue?.id;
    if (!id) {
      return;
    }

    const oldId = oldValue?.id;
    if (id === oldId) {
      return;
    }

    await loadPermissions(id);
    await loadData(id);
  }, { immediate: true });
});

const status = computed(() => {
  return dataSource.value?.status?.value;
});

const editFlag = computed(() => {
  return !!props.data?.id;
});

const editDisabled = computed(() => {
  if (dataSource.value && ['IN_PROGRESS', 'COMPLETED'].includes(status.value)) {
    return true;
  }

  return false;
});

const cancelEdit = async () => {
  deleteTabPane([props.data._id]);
};

const autoSize = {
  minRows: 12,
  maxRows: 20
};
</script>

<template>
  <Spin :spinning="loading" class="h-full text-3 leading-5 px-5 py-5 overflow-auto">
    <div class="flex items-center space-x-2.5 mb-5">
      <Button
        :disabled="!isAdmin && !permissions.includes('MODIFY_PLAN')"
        type="primary"
        size="small"
        class="flex items-center space-x-1"
        @click="ok">
        <Icon icon="icon-dangqianxuanzhong" class="text-3.5" />
        <span>保存</span>
      </Button>

      <template v-if="editFlag">
        <Button
          v-if="status === 'COMPLETED'"
          :disabled="!isAdmin && !permissions.includes('MODIFY_PLAN')"
          size="small"
          type="default"
          class="flex items-center space-x-1"
          @click="toStart">
          <Icon icon="icon-kaishi" class="text-3.5" />
          <span>重新开始</span>
        </Button>

        <Button
          v-else-if="['PENDING', 'BLOCKED'].includes(status)"
          :disabled="!isAdmin && !permissions.includes('MODIFY_PLAN')"
          size="small"
          type="default"
          class="flex items-center space-x-1"
          @click="toStart">
          <Icon icon="icon-kaishi" class="text-3.5" />
          <span>开始</span>
        </Button>

        <template v-if="status === 'IN_PROGRESS'">
          <Button
            :disabled="!isAdmin && !permissions.includes('MODIFY_PLAN')"
            size="small"
            type="default"
            class="flex items-center space-x-1"
            @click="toCompleted">
            <Icon icon="icon-yiwancheng" class="text-3.5" />
            <span>完成</span>
          </Button>

          <Button
            :disabled="!isAdmin && !permissions.includes('MODIFY_PLAN')"
            size="small"
            type="default"
            class="flex items-center space-x-1"
            @click="toCompleted">
            <Icon icon="icon-zusai" class="text-3.5" />
            <span>阻塞</span>
          </Button>
        </template>

        <Button
          :disabled="!isAdmin && !permissions.includes('DELETE_PLAN')"
          type="default"
          size="small"
          class="flex items-center space-x-1"
          @click="toDelete">
          <Icon icon="icon-qingchu" class="text-3.5" />
          <span>删除</span>
        </Button>

        <Button
          :disabled="!isAdmin && !permissions.includes('GRANT')"
          type="default"
          size="small"
          class="flex items-center space-x-1"
          @click="toGrant">
          <Icon icon="icon-quanxian1" class="text-3.5" />
          <span>权限</span>
        </Button>

        <Button
          type="default"
          size="small"
          class="flex items-center space-x-1"
          @click="toClone">
          <Icon icon="icon-fuzhizujian2" class="text-3.5" />
          <span>克隆</span>
        </Button>

        <Button
          :disabled="!isAdmin && !permissions.includes('RESET_TEST_RESULT')"
          type="default"
          size="small"
          class="flex items-center space-x-1"
          @click="toResetTestResult">
          <Icon icon="icon-zhongzhiceshijieguo" class="text-3.5" />
          <span>重置测试</span>
        </Button>

        <Button
          :disabled="!isAdmin && !permissions.includes('RESET_REVIEW_RESULT')"
          type="default"
          size="small"
          class="flex items-center space-x-1"
          @click="toResetReviewResult">
          <Icon icon="icon-zhongzhipingshenjieguo" class="text-3.5" />
          <span>重置评审</span>
        </Button>

        <Button
          type="default"
          size="small"
          class="flex items-center space-x-1"
          @click="toCopyLink">
          <Icon icon="icon-fuzhi" class="text-3.5" />
          <span>复制链接</span>
        </Button>

        <Button
          type="default"
          size="small"
          class="flex items-center space-x-1"
          @click="toRefresh">
          <Icon icon="icon-shuaxin" class="text-3.5" />
          <span>刷新</span>
        </Button>
      </template>

      <Button
        type="default"
        size="small"
        class="flex items-center space-x-1"
        @click="cancelEdit">
        <span>取消</span>
      </Button>
    </div>

    <Form
      ref="formRef"
      :model="formState"
      :labelCol="{ style: { width: '75px' } }"
      class="max-w-242.5"
      size="small"
      layout="horizontal">
      <FormItem
        label="计划名称"
        name="name"
        :rules="{ required: true, message: '请输入计划名称' }">
        <Input
          v-model:value="formState.name"
          size="small"
          :maxlength="200"
          :placeholder="'计划简要概述，最多支持200个字符'" />
      </FormItem>

      <FormItem
        label="负责人"
        name="ownerId"
        class="relative"
        :rules="{ required: true, message: '请选择负责人' }">
        <Select
          v-model:value="formState.ownerId"
          :options="members"
          size="small"
          placeholder="选择负责人" />
        <Tooltip
          placement="right"
          arrowPointAtCenter
          :overlayStyle="{ 'max-width': '400px' }">
          <template #title>
            <div>
              通过为计划设置负责人，可以明确测试的责任和权利，更好地促进任务的完成和进度控制，如：解决问题、推进进度、协作团队成员、识别和管理风险等。
            </div>
          </template>
          <Icon icon="icon-tishi1" class="text-tips absolute top-1.5 ml-1 text-3.5 z-10 cursor-pointer" />
        </Tooltip>
      </FormItem>

      <FormItem
        label="时间计划"
        name="date"
        :rules="{ required: true, validator: validateDate, trigger: 'change' }">
        <DatePicker
          v-model:value="formState.date"
          format="YYYY-MM-DD HH:mm:ss"
          :showNow="false"
          :showTime="{ format: 'HH:mm:ss' }"
          :placeholder="['开始时间', '截止时间']"
          type="date-range"
          size="small"
          class="w-full" />
        <Tooltip
          placement="right"
          arrowPointAtCenter
          :overlayStyle="{ 'max-width': '400px' }">
          <template #title>
            <div>确定测试活动的开始时间和结束时间，以确保在项目周期内完成所有测试。</div>
          </template>
          <Icon icon="icon-tishi1" class="text-tips absolute top-1.5 ml-1 text-3.5 cursor-pointer" />
        </Tooltip>
      </FormItem>

      <FormItem
        label="测试人员"
        name="testerResponsibilities"
        class="relative"
        :rules="[{ required: true }, { validator: validateTester }]">
        <TesterSelect
          ref="testerSelectRef"
          class="inline-block w-full"
          :value="formState.testerResponsibilities"
          :members="dataSource?.members"
          :membersOptions="members"
          @change="changeTester" />
        <Tooltip
          placement="right"
          arrowPointAtCenter
          :overlayStyle="{ 'max-width': '400px' }">
          <template #title>
            <div>
              明确参与本次测试计划的测试人员，只允许测试人员参与测试活动。确定测试人员的角色，负责测试的范围，或执行哪些测试任务和测试用例，避免责任模糊和任务遗漏。
            </div>
          </template>
          <Icon icon="icon-tishi1" class="text-tips absolute top-1.5 ml-1 text-3.5 cursor-pointer" />
        </Tooltip>
      </FormItem>

      <FormItem
        label="是否评审"
        required
        class="flex-1"
        name="review">
        <Tooltip
          :visible="reviewFlagVisible"
          placement="right"
          arrowPointAtCenter
          :overlayStyle="{ 'max-width': '430px' }">
          <template #title>
            <div>
              <span class="mr-2">关闭后会自动清除用例评审状态（不包括评审次数计数），确认是否继续？</span>
              <a class="text-theme-special" @click="handleReviewFlagOk">确认</a>
              <Divider type="vertical" />
              <a class="text-theme-special" @click="handleReviewFlagCancel">取消</a>
            </div>
          </template>
          <Switch
            :checked="formState.review"
            :disabled="editDisabled"
            size="small"
            @change="reviewFlagChange" />
        </Tooltip>
        <Tooltip
          placement="right"
          arrowPointAtCenter
          :overlayStyle="{ 'max-width': '400px' }">
          <template #title>
            <div>
              开启后，团队内部可通过审核过程评估测试用例、测试步骤和测试结果等方面的质量和有效性，并且开启后只有用例评审通过后才可以进入测试，默认开启。
            </div>
          </template>
          <Icon icon="icon-tishi1" class="text-tips mt-0.25 ml-1 text-3.5 cursor-pointer" />
        </Tooltip>
      </FormItem>

      <FormItem
        label="工作量评估"
        name="evalWorkloadMethod"
        class="flex-1">
        <RadioGroup
          v-model:value="formState.evalWorkloadMethod"
          :disabled="editDisabled"
          class="mt-0.5">
          <Radio
            v-for="item in evalWorkloadMethodOptions"
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
            <div>
              指定工作量评估方式。工时是指每个人员完成一项工作实际所花费的时间，
              以小时为单位计算；故事点是基于工作任务量、综合难度、复杂度等的评估值，使用故事点能强制团队细分任务中问题，具有灵活、敏捷度高的特点，推荐使用。
            </div>
          </template>
          <Icon icon="icon-tishi1" class="text-tips text-3.5 cursor-pointer" />
        </Tooltip>
      </FormItem>

      <FormItem
        label="用例前缀"
        name="casePrefix"
        class="relative flex-1">
        <Input
          v-model:value="formState.casePrefix"
          size="small"
          :readonly="!!dataSource?.id"
          :disabled="!!dataSource?.id"
          :maxlength="40"
          :placeholder="'用例前缀，最多可输入40个字符'" />
        <Tooltip
          placement="right"
          arrowPointAtCenter
          :overlayStyle="{ 'max-width': '400px' }">
          <template #title>
            <div>设置后计划下用例名会以该前缀开头，不允许修改。</div>
          </template>
          <Icon icon="icon-tishi1" class="text-tips absolute top-1.5 ml-1 text-3.5 cursor-pointer" />
        </Tooltip>
      </FormItem>

      <FormItem label="附件">
        <div class="flex items-center mt-0.5">
          <Upload
            :fileList="[]"
            name="file"
            accept=".jpg,.bmp,.png,.gif,.txt,.docx,.jpeg,.rar,.zip,.doc,.xlsx,.xls,.pdf"
            :customRequest="upLoadFile">
            <a class="text-theme-special text-theme-text-hover text-3 flex items-center leading-5 h-5 mt-0.5">
              <Icon icon="icon-lianjie1" class="mr-1" />
              <span class="whitespace-nowrap">上传附件</span>
            </a>
          </Upload>
          <Popover
            placement="right"
            arrowPointAtCenter
            :overlayStyle="{ 'max-width': '400px' }">
            <template #content>
              <div class="text-3 text-theme-sub-content leading-4 break-all">
                其他文档，如：需求说明书、参考资料、系统架构图、测试规范、技术文档等。支持的格式："jpg","bmp","png","gif","txt","docx","jpeg","rar","zip","doc","xlsx","xls","pdf"；最多上传10个附件。
              </div>
            </template>
            <Icon icon="icon-tishi1" class="text-tips ml-1 -mt-0.25 text-3.5 cursor-pointer" />
          </Popover>
        </div>
      </FormItem>

      <FormItem
        v-if="formState?.attachments?.length"
        :colon="false"
        label=" "
        class="-mt-5">
        <div
          v-for="(item, index) in formState.attachments"
          :key="index"
          :class="{ 'rounded-t pt-2': index === 0, 'rounded-b': index === formState.attachments.length - 1 }"
          class="flex items-center justify-between text-3 leading-3 pb-2 px-3 bg-gray-100">
          <div class="w-150 truncate text-theme-sub-content leading-4">{{ item.name }}</div>
          <Icon
            icon="icon-qingchu"
            class="text-theme-special text-theme-text-hover cursor-pointer flex-shrink-0 leading-4 text-3.5"
            @click="delFile(index)" />
        </div>
      </FormItem>

      <Tabs
        v-model:activeKey="activeTabKey"
        size="small"
        class="pl-5 planEditTab">
        <TabPane key="testingObjectives" forceRender>
          <template #tab>
            <div class="text-right pr-2">
              <IconRequired />
              <span>测试目标</span>
            </div>
          </template>
          <FormItem
            label=""
            class="!mb-5"
            name="testingObjectives"
            :rules="[{ validator: validateRequired, message: '请输入测试目标' }, {validator: validateMaxLength}]">
            <RichEditor
              ref="objectiveRichRef"
              v-model:value="formState.testingObjectives"
              :options="{placeholder: '界定测试活动所涵盖的具体内容和范围，包括测试哪些功能模块、平台、版本等。'}" />
            <!-- <Textarea
              v-model:value="formState.testingObjectives"
              size="small"
              showCount
              :autoSize="autoSize"
              :maxlength="2000"
              placeholder="测试活动的具体目的和期望达成的结果，帮助测试团队聚焦于测试的核心目标。" /> -->
          </FormItem>
        </TabPane>
        <TabPane key="testingScope" forceRender>
          <template #tab>
            <IconRequired />
            <span>测试范围</span>
          </template>
          <FormItem
            label=""
            name="testingScope"
            class="!mb-5"
            :rules="[{ validator: validateRequired, message: '请输入测试范围' }, {validator: validateMaxLength}]">
            <RichEditor
              ref="scopeRichRef"
              v-model:value="formState.testingScope"
              :options="{placeholder: '界定测试活动所涵盖的具体内容和范围，包括测试哪些功能模块、平台、版本等。'}" />
            <!-- <Textarea
              v-model:value="formState.testingScope"
              size="small"
              showCount
              :autoSize="autoSize"
              :maxlength="2000"
              placeholder="界定测试活动所涵盖的具体内容和范围，包括测试哪些功能模块、平台、版本等。" /> -->
          </FormItem>
        </TabPane>
        <TabPane
          key="acceptanceCriteria"
          tab="验收标准"
          forceRender>
          <FormItem
            label=""
            name="acceptanceCriteria"
            :rules="[{validator: validateMaxLength}]">
            <RichEditor
              ref="criteriaRichRef"
              v-model:value="formState.acceptanceCriteria"
              :options="{placeholder: '明确计软件产品交付的具体条件和标准。'}" />
            <!-- <Textarea
              v-model:value="formState.acceptanceCriteria"
              size="small"
              class="mb-5"
              showCount
              :autoSize="autoSize"
              :maxlength="2000"
              placeholder="明确计软件产品交付的具体条件和标准。" /> -->
          </FormItem>
        </TabPane>
        <TabPane
          key="otherInformation"
          tab="其他说明"
          forceRender>
          <FormItem
            label=""
            name="otherInformation"
            :rules="[{validator: validateMaxLength}]">
            <RichEditor
              ref="infoRichRef"
              v-model:value="formState.otherInformation"
              :options="{placeholder: '其他说明，如测试策略、风险评估和管理等。'}" />
            <!-- <Textarea
              v-model:value="formState.otherInformation"
              size="small"
              class="mb-5"
              showCount
              :autoSize="autoSize"
              :maxlength="2000"
              placeholder="其他说明，如测试策略、风险评估和管理等。" /> -->
          </FormItem>
        </TabPane>
      </Tabs>
    </Form>

    <AsyncComponent :visible="authorizeModalVisible">
      <AuthorizeModal
        v-model:visible="authorizeModalVisible"
        enumKey="FuncPlanPermission"
        :appId="props.appInfo?.id"
        :listUrl="`${TESTER}/func/plan/auth?planId=${dataSource?.id}`"
        :delUrl="`${TESTER}/func/plan/auth`"
        :addUrl="`${TESTER}/func/plan/${dataSource?.id}/auth`"
        :updateUrl="`${TESTER}/func/plan/auth`"
        :enabledUrl="`${TESTER}/func/plan/${dataSource?.id}/auth/enabled`"
        :initStatusUrl="`${TESTER}/func/plan/${dataSource?.id}/auth/status`"
        onTips="开启&quot;有权限控制&quot;后，需要手动授权服务权限后才会有计划相应操作权限，默认开启&quot;有权限控制&quot;。注意：如果授权对象没有父级项目权限将自动授权查看权限。"
        offTips="开启&quot;无权限控制&quot;后，将允许所有用户公开查看和操作当前计划，查看用户同时需要有当前计划父级项目权限。"
        title="计划权限"
        @change="authFlagChange" />
    </AsyncComponent>
  </Spin>
</template>

<style scoped>
:deep(.ant-form-item-label>label::after) {
  margin-right: 10px;
}

.ant-tabs-small > :deep(.ant-tabs-nav) .ant-tabs-tab {
  padding-top: 0;
}
</style>
