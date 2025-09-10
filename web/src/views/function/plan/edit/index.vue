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
import {
  EvalWorkloadMethod,
  EnumMessage,
  toClipboard,
  utils,
  TESTER,
  enumUtils,
  upload,
  appContext
} from '@xcan-angus/infra';
import dayjs from 'dayjs';
import { isEqual } from 'lodash-es';
import type { Rule } from 'ant-design-vue/es/form';
import { funcPlan, project } from '@/api/tester';
import { useI18n } from 'vue-i18n';
import { DATE_TIME_FORMAT } from '@/utils/constant';

import {EditFormState, PlanInfo} from '../types';

const { t } = useI18n();

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

const AuthorizeModal = defineAsyncComponent(() => import('@/components/AuthorizeModal/index.vue'));
const TesterSelect = defineAsyncComponent(() => import('./TesterSelect.vue'));
const RichEditor = defineAsyncComponent(() => import('@/components/richEditor/index.vue'));

const updateTabPane = inject<(data: { [key: string]: any }) => void>('updateTabPane', () => ({}));
const deleteTabPane = inject<(keys: string[]) => void>('deleteTabPane', () => ({}));
const replaceTabPane = inject<(id: string, data: { [key: string]: any }) => void>('replaceTabPane', () => ({}));
const isAdmin = computed(() => appContext.isAdmin());

const formRef = ref();

const loading = ref(false);
const dataSource = ref<PlanInfo>();

const testerSelectRef = ref();
const activeTabKey = ref('testingObjectives');

const evalWorkloadMethodOptions = ref<EnumMessage<EvalWorkloadMethod>[]>([]);
const reviewFlagVisible = ref(false);

const permissions = ref<string[]>([]);
const oldFormState = ref<EditFormState>();
const _startDate = dayjs().format(DATE_TIME_FORMAT);
const _deadlineDate = dayjs().add(1, 'month').format(DATE_TIME_FORMAT);
const formState = ref<EditFormState>({
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
    return Promise.reject(new Error(t('functionPlan.editForm.validation.selectPlanTime')));
  } else if (!value[0]) {
    return Promise.reject(new Error(t('functionPlan.editForm.validation.selectStartTime')));
  } else if (!value[1]) {
    return Promise.reject(new Error(t('functionPlan.editForm.validation.selectDeadlineTime')));
  } else {
    return Promise.resolve();
  }
};

const validateTester = async () => {
  if (Object.keys(formState.value.testerResponsibilities).length === 0) {
    return Promise.reject(new Error(t('functionPlan.editForm.validation.selectTester')));
  }
  if (testerSelectRef.value.validate()) {
    return Promise.resolve();
  } else {
    return Promise.reject(new Error(t('functionPlan.editForm.validation.duplicatePersonnel')));
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
      Promise.reject(new Error(t('functionPlan.editForm.validation.charLimit2000')));
    }
  }
  if (value.field === 'otherInformation') {
    if (infoRichRef.value && infoRichRef.value.getLength() > 2000) {
      Promise.reject(new Error(t('functionPlan.editForm.validation.charLimit2000')));
    }
  }
  if (value.field === 'acceptanceCriteria') {
    if (criteriaRichRef.value && criteriaRichRef.value.getLength() > 2000) {
      Promise.reject(new Error(t('functionPlan.editForm.validation.charLimit2000')));
    }
  }
  if (value.field === 'testingScope') {
    if (scopeRichRef.value && scopeRichRef.value.getLength() > 2000) {
      Promise.reject(new Error(t('functionPlan.editForm.validation.charLimit2000')));
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
  const params: EditFormState = { ...formState.value };
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

  notification.success(t('functionPlan.editForm.notifications.saveSuccess'));

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

  notification.success(t('functionPlan.editForm.notifications.addSuccess'));

  const _id = props.data?._id;
  const newId = res?.data?.id;
  const name = params.name;
  replaceTabPane(_id, { _id: newId, uiKey: newId, name, data: { _id: newId, id: newId } });
};

const validateMaxLen = (val) => {
  if (formState.value[val.field].length > 2000) {
    return Promise.reject(t('functionPlan.editForm.validation.charLimit2000WithCount'));
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

  notification.success(t('functionPlan.editForm.notifications.planStartSuccess'));
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

  notification.success(t('functionPlan.editForm.notifications.planCompletedSuccess'));
  loadData(id);
  refreshList();
};

const toDelete = async () => {
  const data = dataSource.value;
  if (!data) {
    return;
  }

  modal.confirm({
    content: t('functionPlan.editForm.notifications.confirmDeletePlan', { name: data.name }),
    async onOk () {
      const id = data.id;
      loading.value = true;
      const [error] = await funcPlan.deletePlan(id);
      loading.value = false;
      if (error) {
        return;
      }

      notification.success(t('functionPlan.editForm.notifications.planDeleteSuccess'));
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

  notification.success(t('functionPlan.editForm.notifications.planCloneSuccess'));
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

  notification.success(t('functionPlan.editForm.notifications.planResetTestSuccess'));
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

  notification.success(t('functionPlan.editForm.notifications.planResetReviewSuccess'));
  loadData(id);
  refreshList();
};

const toCopyLink = () => {
  const id = dataSource.value?.id;
  if (!id) {
    return;
  }

  toClipboard(window.location.origin + `/function#plans?id=${id}`).then(() => {
    notification.success(t('functionPlan.editForm.notifications.copyLinkSuccess'));
  }).catch(() => {
    notification.error(t('functionPlan.editForm.notifications.copyLinkFailed'));
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

  debugger;
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
    const startDate = dayjs().format(DATE_TIME_FORMAT);
    const deadlineDate = dayjs().add(1, 'month').format(DATE_TIME_FORMAT);
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

const loadEnums = () => {
  evalWorkloadMethodOptions.value = enumUtils.enumToMessages(EvalWorkloadMethod);
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
  loading.value = false;
  if (error) {
    return;
  }

  permissions.value = (res?.data?.permissions || []).map(item => item.value);
};

const members = ref([]);

const loadMembers = async () => {
  const [error, res] = await project.getProjectMember(props.projectId);
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
        <span>{{ t('functionPlan.editForm.buttons.save') }}</span>
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
          <span>{{ t('functionPlan.editForm.buttons.restart') }}</span>
        </Button>

        <Button
          v-else-if="['PENDING', 'BLOCKED'].includes(status)"
          :disabled="!isAdmin && !permissions.includes('MODIFY_PLAN')"
          size="small"
          type="default"
          class="flex items-center space-x-1"
          @click="toStart">
          <Icon icon="icon-kaishi" class="text-3.5" />
          <span>{{ t('functionPlan.editForm.buttons.start') }}</span>
        </Button>

        <template v-if="status === 'IN_PROGRESS'">
          <Button
            :disabled="!isAdmin && !permissions.includes('MODIFY_PLAN')"
            size="small"
            type="default"
            class="flex items-center space-x-1"
            @click="toCompleted">
            <Icon icon="icon-yiwancheng" class="text-3.5" />
            <span>{{ t('functionPlan.editForm.buttons.complete') }}</span>
          </Button>

          <Button
            :disabled="!isAdmin && !permissions.includes('MODIFY_PLAN')"
            size="small"
            type="default"
            class="flex items-center space-x-1"
            @click="toCompleted">
            <Icon icon="icon-zusai" class="text-3.5" />
            <span>{{ t('functionPlan.editForm.buttons.block') }}</span>
          </Button>
        </template>

        <Button
          :disabled="!isAdmin && !permissions.includes('DELETE_PLAN')"
          type="default"
          size="small"
          class="flex items-center space-x-1"
          @click="toDelete">
          <Icon icon="icon-qingchu" class="text-3.5" />
          <span>{{ t('functionPlan.editForm.buttons.delete') }}</span>
        </Button>

        <Button
          :disabled="!isAdmin && !permissions.includes('GRANT')"
          type="default"
          size="small"
          class="flex items-center space-x-1"
          @click="toGrant">
          <Icon icon="icon-quanxian1" class="text-3.5" />
          <span>{{ t('functionPlan.editForm.buttons.permission') }}</span>
        </Button>

        <Button
          type="default"
          size="small"
          class="flex items-center space-x-1"
          @click="toClone">
          <Icon icon="icon-fuzhizujian2" class="text-3.5" />
          <span>{{ t('functionPlan.editForm.buttons.clone') }}</span>
        </Button>

        <Button
          :disabled="!isAdmin && !permissions.includes('RESET_TEST_RESULT')"
          type="default"
          size="small"
          class="flex items-center space-x-1"
          @click="toResetTestResult">
          <Icon icon="icon-zhongzhiceshijieguo" class="text-3.5" />
          <span>{{ t('functionPlan.editForm.buttons.resetTest') }}</span>
        </Button>

        <Button
          :disabled="!isAdmin && !permissions.includes('RESET_REVIEW_RESULT')"
          type="default"
          size="small"
          class="flex items-center space-x-1"
          @click="toResetReviewResult">
          <Icon icon="icon-zhongzhipingshenjieguo" class="text-3.5" />
          <span>{{ t('functionPlan.editForm.buttons.resetReview') }}</span>
        </Button>

        <Button
          type="default"
          size="small"
          class="flex items-center space-x-1"
          @click="toCopyLink">
          <Icon icon="icon-fuzhi" class="text-3.5" />
          <span>{{ t('functionPlan.editForm.buttons.copyLink') }}</span>
        </Button>

        <Button
          type="default"
          size="small"
          class="flex items-center space-x-1"
          @click="toRefresh">
          <Icon icon="icon-shuaxin" class="text-3.5" />
          <span>{{ t('functionPlan.editForm.buttons.refresh') }}</span>
        </Button>
      </template>

      <Button
        type="default"
        size="small"
        class="flex items-center space-x-1"
        @click="cancelEdit">
        <span>{{ t('functionPlan.editForm.buttons.cancel') }}</span>
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
        :label="t('functionPlan.editForm.form.planName')"
        name="name"
        :rules="{ required: true, message: t('functionPlan.editForm.form.enterPlanName') }">
        <Input
          v-model:value="formState.name"
          size="small"
          :maxlength="200"
          :placeholder="t('functionPlan.editForm.form.planNamePlaceholder')" />
      </FormItem>

      <FormItem
        :label="t('functionPlan.editForm.form.owner')"
        name="ownerId"
        class="relative"
        :rules="{ required: true, message: t('functionPlan.editForm.form.selectOwner') }">
        <Select
          v-model:value="formState.ownerId"
          :options="members"
          size="small"
          :placeholder="t('functionPlan.editForm.form.selectOwnerPlaceholder')" />
        <Tooltip
          placement="right"
          arrowPointAtCenter
          :overlayStyle="{ 'max-width': '400px' }">
          <template #title>
            <div>
              {{ t('functionPlan.editForm.form.ownerTooltip') }}
            </div>
          </template>
          <Icon icon="icon-tishi1" class="text-tips absolute top-1.5 ml-1 text-3.5 z-10 cursor-pointer" />
        </Tooltip>
      </FormItem>

      <FormItem
        :label="t('functionPlan.editForm.form.timePlan')"
        name="date"
        :rules="{ required: true, validator: validateDate, trigger: 'change' }">
        <DatePicker
          v-model:value="formState.date"
          format="YYYY-MM-DD HH:mm:ss"
          :showNow="false"
          :showTime="{ format: 'HH:mm:ss' }"
          :placeholder="[t('functionPlan.editForm.form.startTime'), t('functionPlan.editForm.form.deadlineTime')]"
          type="date-range"
          size="small"
          class="w-full" />
        <Tooltip
          placement="right"
          arrowPointAtCenter
          :overlayStyle="{ 'max-width': '400px' }">
          <template #title>
            <div>{{ t('functionPlan.editForm.form.timePlanTooltip') }}</div>
          </template>
          <Icon icon="icon-tishi1" class="text-tips absolute top-1.5 ml-1 text-3.5 cursor-pointer" />
        </Tooltip>
      </FormItem>

      <FormItem
        :label="t('functionPlan.editForm.form.testers')"
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
              {{ t('functionPlan.editForm.form.testersTooltip') }}
            </div>
          </template>
          <Icon icon="icon-tishi1" class="text-tips absolute top-1.5 ml-1 text-3.5 cursor-pointer" />
        </Tooltip>
      </FormItem>

      <FormItem
        :label="t('functionPlan.editForm.form.isReview')"
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
              <span class="mr-2">{{ t('functionPlan.editForm.form.reviewCloseConfirm') }}</span>
              <a class="text-theme-special" @click="handleReviewFlagOk">{{ t('functionPlan.editForm.form.confirm') }}</a>
              <Divider type="vertical" />
              <a class="text-theme-special" @click="handleReviewFlagCancel">{{ t('functionPlan.editForm.form.cancel') }}</a>
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
              {{ t('functionPlan.editForm.form.reviewTooltip') }}
            </div>
          </template>
          <Icon icon="icon-tishi1" class="text-tips mt-0.25 ml-1 text-3.5 cursor-pointer" />
        </Tooltip>
      </FormItem>

      <FormItem
        :label="t('functionPlan.editForm.form.workloadAssessment')"
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
              {{ t('functionPlan.editForm.form.workloadAssessmentTooltip') }}
            </div>
          </template>
          <Icon icon="icon-tishi1" class="text-tips text-3.5 cursor-pointer" />
        </Tooltip>
      </FormItem>

      <FormItem
        :label="t('functionPlan.editForm.form.casePrefix')"
        name="casePrefix"
        class="relative flex-1">
        <Input
          v-model:value="formState.casePrefix"
          size="small"
          :readonly="!!dataSource?.id"
          :disabled="!!dataSource?.id"
          :maxlength="40"
          :placeholder="t('functionPlan.editForm.form.casePrefixPlaceholder')" />
        <Tooltip
          placement="right"
          arrowPointAtCenter
          :overlayStyle="{ 'max-width': '400px' }">
          <template #title>
            <div>{{ t('functionPlan.editForm.form.casePrefixTooltip') }}</div>
          </template>
          <Icon icon="icon-tishi1" class="text-tips absolute top-1.5 ml-1 text-3.5 cursor-pointer" />
        </Tooltip>
      </FormItem>

      <FormItem :label="t('functionPlan.editForm.form.attachments')">
        <div class="flex items-center mt-0.5">
          <Upload
            :fileList="[]"
            name="file"
            accept=".jpg,.bmp,.png,.gif,.txt,.docx,.jpeg,.rar,.zip,.doc,.xlsx,.xls,.pdf"
            :customRequest="upLoadFile">
            <a class="text-theme-special text-theme-text-hover text-3 flex items-center leading-5 h-5 mt-0.5">
              <Icon icon="icon-lianjie1" class="mr-1" />
              <span class="whitespace-nowrap">{{ t('functionPlan.editForm.form.uploadAttachments') }}</span>
            </a>
          </Upload>
          <Popover
            placement="right"
            arrowPointAtCenter
            :overlayStyle="{ 'max-width': '400px' }">
            <template #content>
              <div class="text-3 text-theme-sub-content leading-4 break-all">
                {{ t('functionPlan.editForm.form.attachmentsTooltip') }}
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
              <span>{{ t('functionPlan.editForm.form.testingObjectives') }}</span>
            </div>
          </template>
          <FormItem
            label=""
            class="!mb-5"
            name="testingObjectives"
            :rules="[{ validator: validateRequired, message: t('functionPlan.editForm.form.enterTestingObjectives') }, {validator: validateMaxLength}]">
            <RichEditor
              ref="objectiveRichRef"
              v-model:value="formState.testingObjectives"
              :options="{placeholder: t('functionPlan.editForm.form.testingObjectivesPlaceholder')}" />
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
            <span>{{ t('functionPlan.editForm.form.testingScope') }}</span>
          </template>
          <FormItem
            label=""
            name="testingScope"
            class="!mb-5"
            :rules="[{ validator: validateRequired, message: t('functionPlan.editForm.form.enterTestingScope') }, {validator: validateMaxLength}]">
            <RichEditor
              ref="scopeRichRef"
              v-model:value="formState.testingScope"
              :options="{placeholder: t('functionPlan.editForm.form.testingScopePlaceholder')}" />
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
          :tab="t('functionPlan.editForm.form.acceptanceCriteria')"
          forceRender>
          <FormItem
            label=""
            name="acceptanceCriteria"
            :rules="[{validator: validateMaxLength}]">
            <RichEditor
              ref="criteriaRichRef"
              v-model:value="formState.acceptanceCriteria"
              :options="{placeholder: t('functionPlan.editForm.form.acceptanceCriteriaPlaceholder')}" />
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
          :tab="t('functionPlan.editForm.form.otherInformation')"
          forceRender>
          <FormItem
            label=""
            name="otherInformation"
            :rules="[{validator: validateMaxLength}]">
            <RichEditor
              ref="infoRichRef"
              v-model:value="formState.otherInformation"
              :options="{placeholder: t('functionPlan.editForm.form.otherInformationPlaceholder')}" />
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
        :onTips="t('functionPlan.editForm.permissionModal.onTips')"
        :offTips="t('functionPlan.editForm.permissionModal.offTips')"
        :title="t('functionPlan.editForm.permissionModal.title')"
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
