<script lang="ts" setup>
import { computed, inject, nextTick, onMounted, ref, watch, defineAsyncComponent } from 'vue';
import {
  AsyncComponent,
  AuthorizeModal,
  DatePicker,
  Icon,
  Input,
  modal,
  notification,
  SelectUser,
  Spin,
  Tooltip
} from '@xcan-angus/vue-ui';
import { Button, Form, FormItem, Radio, RadioGroup, TabPane, Tabs, Textarea, Upload } from 'ant-design-vue';
import { clipboard, utils, TESTER, enumLoader, upload } from '@xcan-angus/tools';
import type { Rule } from 'ant-design-vue/es/form';
import dayjs from 'dayjs';
import { task } from '@/api/tester';
// import { cloneDeep, isEqual } from 'lodash-es';
import { FormState } from './PropsType';
import { SprintInfo } from '../PropsType';
// import Meetings from './meeting.vue';

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

const RichEditor = defineAsyncComponent(() => import('@/components/richEditor/index.vue'));

const updateTabPane = inject<(data: { [key: string]: any }) => void>('updateTabPane', () => ({}));
const deleteTabPane = inject<(keys: string[]) => void>('deleteTabPane', () => ({}));
const replaceTabPane = inject<(id: string, data: { [key: string]: any }) => void>('replaceTabPane', () => ({}));
const isAdmin = inject('isAdmin', ref(false));

// const meetingsRef = ref();
const formRef = ref();

const evalWorkloadMethodOptions = ref<{ value: string, message: string }[]>([]);
const dataSource = ref<SprintInfo>();

const permissions = ref<string[]>([]);
// const oldFormState = ref<FormState>();
const _startDate = dayjs().format('YYYY-MM-DD HH:mm:ss');
const _deadlineDate = dayjs().add(1, 'month').format('YYYY-MM-DD HH:mm:ss');
const formState = ref<FormState>({
  taskPrefix: '',
  otherInformation: '',
  acceptanceCriteria: '',
  projectId: props.projectId,
  evalWorkloadMethod: 'STORY_POINT',
  name: '',
  ownerId: props.userInfo?.id,
  startDate: _startDate,
  deadlineDate: _deadlineDate,
  attachments: [],
  date: [_startDate, _deadlineDate]
  // meetings: []
});

const ownerDefaultOptions = ref<{[key:string]:{fullName:string;id:string;}}>();

const loading = ref(false);
const authorizeModalVisible = ref(false);

const getParams = () => {
  const params: FormState = { ...formState.value };
  const id = dataSource.value?.id;
  if (id) {
    params.id = id;
  }

  const _date = formState.value.date;
  if (_date) {
    params.startDate = _date[0];
    params.deadlineDate = _date[1];
  }

  if (!params.attachments?.length) {
    delete params.attachments;
  }

  if (!params.otherInformation) {
    delete params.otherInformation;
  }

  if (!params.acceptanceCriteria) {
    delete params.acceptanceCriteria;
  }

  if (!params.taskPrefix) {
    delete params.taskPrefix;
  }
  // const meetings = meetingsRef.value.getData();
  // if (meetings.length) {
  //   params.meetings = meetings;
  // } else {
  //   delete params.meetings;
  // }

  delete params.date;

  return params;
};

const refreshList = () => {
  nextTick(() => {
    updateTabPane({ _id: 'sprintList', notify: utils.uuid() });
  });
};

const editOk = async () => {
  // const equalFlag = isEqual(oldFormState.value, formState.value);
  // if (equalFlag) {
  //   return;
  // }
  const params = getParams();

  loading.value = true;
  const [error] = await task.putSprint(params);
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
  const [error, res] = await task.addSprint(params);
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

const criteriaRichRef = ref();
const infoRichRef = ref();
const validateMaxLen = (val) => {
  if (val.field === 'acceptanceCriteria') {
    debugger;
    if (criteriaRichRef.value && criteriaRichRef.value.getLength() > 2000) {
      return Promise.reject('字符不能超过2000个字符');
    }
  }

  if (val.field === 'otherInformation') {
    if (infoRichRef.value && infoRichRef.value.getLength() > 2000) {
      return Promise.reject('字符不能超过2000个字符');
    }
  }
  // if (formState.value[val.field].length > 2000) {
  //   return Promise.reject('字符不能超过2000个字符');
  // }
  return Promise.resolve();
};

const ok = async () => {
  formRef.value.validate().then(async () => {
    // Promise.all([formRef.value.validate(), meetingsRef.value.validate()]).then(async ([, meetingData]) => {
    // if (meetingData.length) {
    //   formState.value.meetings = meetingData;
    // } else {
    //   delete formState.value.meetings;
    // }
    if (!editFlag.value) {
      await addOk();
    } else {
      await editOk();
    }
    refreshList();
  });
};

const toStart = async () => {
  const id = dataSource.value?.id;
  if (!id) {
    return;
  }

  loading.value = true;
  const [error] = await task.startSprint(id);
  loading.value = false;
  if (error) {
    return;
  }

  notification.success('迭代开始成功');
  loadData(id);
  refreshList();
};

const toCompleted = async () => {
  const id = dataSource.value?.id;
  if (!id) {
    return;
  }

  loading.value = true;
  const [error] = await task.endSprint(id);
  loading.value = false;
  if (error) {
    return;
  }

  notification.success('迭代已完成');
  loadData(id);
  refreshList();
};

const toDelete = async () => {
  const data = dataSource.value;
  if (!data) {
    return;
  }

  modal.confirm({
    content: `确定删除迭代【${data.name}】吗？`,
    async onOk () {
      const id = data.id;
      loading.value = true;
      const [error] = await task.delSprint(id);
      loading.value = false;
      if (error) {
        return;
      }

      notification.success('迭代删除成功， 您可以在回收站查看删除后的迭代');
      deleteTabPane([props.data._id]);
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
  const [error] = await task.cloneSprint(id);
  loading.value = false;
  if (error) {
    return;
  }

  notification.success('迭代克隆成功');
  refreshList();
};

const toCopyLink = () => {
  const id = dataSource.value?.id;
  if (!id) {
    return;
  }

  clipboard.toClipboard(window.location.origin + `/task#sprint?id=${id}`).then(() => {
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

const cancel = () => {
  deleteTabPane([props.data._id]);
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
      'MODIFY_SPRINT',
      'DELETE_SPRINT',
      'ADD_TASK',
      'MODIFY_TASK',
      'DELETE_TASK',
      'EXPORT_TASK',
      'REVIEW',
      'TEST',
      'GRANT',
      'VIEW'
    ];

    return;
  }

  const params = {
    adminFlag: true
  };
  loading.value = true;
  const [error, res] = await task.getCurrentUserSprintAuth(id, params);
  loading.value = false;
  if (error) {
    return;
  }

  const { taskSprintAuthFlag, permissions: _permissions } = res?.data || { taskSprintAuthFlag: true, permissions: [] };
  if (!taskSprintAuthFlag) {
    permissions.value = [
      'MODIFY_SPRINT',
      'DELETE_SPRINT',
      'ADD_TASK',
      'MODIFY_TASK',
      'DELETE_TASK',
      'EXPORT_TASK',
      'REVIEW',
      'TEST',
      'VIEW'];

    if (_permissions.includes('GRANT')) {
      permissions.value.push('GRANT');
    }
  } else {
    permissions.value = (_permissions || []).map(item => item.value);
  }
};

const loadData = async (id: string) => {
  loading.value = true;
  const [error, res] = await task.getSprintInfo(id);
  loading.value = false;
  if (error) {
    return;
  }

  const data = res?.data as SprintInfo;
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

const setFormData = (data: SprintInfo) => {
  if (!data) {
    const startDate = dayjs().format('YYYY-MM-DD HH:mm:ss');
    const deadlineDate = dayjs().add(1, 'month').format('YYYY-MM-DD HH:mm:ss');
    formState.value = {
      startDate,
      deadlineDate,
      taskPrefix: '',
      otherInformation: '',
      acceptanceCriteria: '',
      projectId: props.projectId,
      evalWorkloadMethod: 'STORY_POINT',
      name: '',
      ownerId: props.userInfo?.id,
      attachments: [],
      date: [startDate, deadlineDate]
      // meetings: []
    };

    return;
  }

  const {
    taskPrefix = '',
    otherInformation = '',
    acceptanceCriteria = '',
    projectId = '',
    evalWorkloadMethod,
    name = '',
    ownerId = '',
    ownerName = '',
    attachments = '',
    startDate = '',
    deadlineDate = ''
    // meetings = []
  } = data;

  formState.value.taskPrefix = taskPrefix;
  formState.value.otherInformation = otherInformation;
  formState.value.acceptanceCriteria = acceptanceCriteria;
  formState.value.projectId = projectId;
  formState.value.evalWorkloadMethod = evalWorkloadMethod?.value || '';
  formState.value.name = name;
  formState.value.ownerId = ownerId;
  ownerDefaultOptions.value = { ownerId: { fullName: ownerName, id: ownerId } };
  formState.value.startDate = startDate;
  formState.value.deadlineDate = deadlineDate;
  formState.value.attachments = attachments || [];
  formState.value.date = [startDate, deadlineDate];
  // formState.value.meetings = meetings;

  // oldFormState.value = cloneDeep(formState.value);
};

onMounted(() => {
  loadEnums();

  watch(() => props.data, async (newValue) => {
    const id = newValue?.id;
    if (!id) {
      return;
    }

    loadPermissions(id);
    loadData(id);
  }, { immediate: true });
});

const validateDate = async (_rule: Rule, value: string) => {
  if (!value) {
    return Promise.reject(new Error('请选择迭代时间'));
  } else if (!value[0]) {
    return Promise.reject(new Error('请选择迭代开始时间'));
  } else if (!value[1]) {
    return Promise.reject(new Error('请选择迭代截止时间'));
  } else {
    return Promise.resolve();
  }
};

const upLoadFile = async ({ file }: { file: File }) => {
  const attachments = formState.value.attachments || [];
  if (attachments.length >= 10) {
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

const status = computed(() => {
  return dataSource.value?.status?.value;
});

const editFlag = computed(() => {
  return !!props.data?.id;
});

const editDisabled = computed(() => {
  if (dataSource.value && ['IN_PROGRESS', 'COMPLETED'].includes(dataSource.value?.status?.value)) {
    return true;
  }

  return false;
});
const autoSize = {
  minRows: 12,
  maxRows: 20
};
</script>
<template>
  <Spin :spinning="loading" class="h-full text-3 leading-5 px-5 py-5 overflow-auto">
    <div class="flex items-center space-x-2.5 mb-5">
      <Button
        :disabled="!isAdmin && !permissions.includes('MODIFY_SPRINT')"
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
          :disabled="!isAdmin && !permissions.includes('MODIFY_SPRINT')"
          size="small"
          type="default"
          class="flex items-center space-x-1"
          @click="toStart">
          <Icon icon="icon-kaishi" class="text-3.5" />
          <span>重新开始</span>
        </Button>

        <Button
          v-else-if="['PENDING', 'BLOCKED'].includes(status)"
          :disabled="!isAdmin && !permissions.includes('MODIFY_SPRINT')"
          size="small"
          type="default"
          class="flex items-center space-x-1"
          @click="toStart">
          <Icon icon="icon-kaishi" class="text-3.5" />
          <span>开始</span>
        </Button>

        <template v-if="status === 'IN_PROGRESS'">
          <Button
            :disabled="!isAdmin && !permissions.includes('MODIFY_SPRINT')"
            size="small"
            type="default"
            class="flex items-center space-x-1"
            @click="toCompleted">
            <Icon icon="icon-yiwancheng" class="text-3.5" />
            <span>完成</span>
          </Button>

          <Button
            :disabled="!isAdmin && !permissions.includes('MODIFY_SPRINT')"
            size="small"
            type="default"
            class="flex items-center space-x-1"
            @click="toCompleted">
            <Icon icon="icon-zusai" class="text-3.5" />
            <span>阻塞</span>
          </Button>
        </template>

        <Button
          :disabled="!isAdmin && !permissions.includes('DELETE_SPRINT')"
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
        class="flex items-center"
        @click="cancel">
        <span>取消</span>
      </Button>
    </div>

    <Form
      ref="formRef"
      :model="formState"
      size="small"
      :labelCol="{ style: { width: '75px' } }"
      class="max-w-242.5"
      layout="horizontal">
      <FormItem
        label="名称"
        name="name"
        :rules="{ required: true, message: '请输入迭代名称' }">
        <Input
          v-model:value="formState.name"
          size="small"
          :maxlength="100"
          placeholder="输入迭代名称，最多可输入100字符" />
      </FormItem>

      <FormItem
        label="时间"
        name="date"
        :rules="{ required: true, validator: validateDate, trigger: 'change' }">
        <DatePicker
          v-model:value="formState.date"
          format="YYYY-MM-DD HH:mm:ss"
          :showNow="false"
          :showTime="{ format: 'HH:mm:ss' }"
          type="date-range"
          size="small"
          class="w-full" />
      </FormItem>

      <FormItem
        label="负责人"
        name="ownerId"
        class="relative"
        :rules="{ required: true, message: '请选择负责人' }">
        <SelectUser
          v-model:value="formState.ownerId"
          size="small"
          placeholder="选择负责人"
          :defaultOptions="ownerDefaultOptions"
          :action="`${TESTER}/project/${props.projectId}/member/user`"
          :maxlength="80" />
        <Tooltip
          placement="right"
          arrowPointAtCenter
          :overlayStyle="{ 'max-width': '400px' }">
          <template #title>
            <div>通过为迭代设置负责人，可以明确责任和权利，更好地促进任务的完成和进度控制，如：解决问题、推进进度、协作团队成员等。</div>
          </template>
          <Icon icon="icon-tishi1" class="text-tips absolute top-1.5 ml-1 text-3.5 cursor-pointer" />
        </Tooltip>
      </FormItem>

      <FormItem
        label="任务前缀"
        name="taskPrefix"
        class="relative">
        <Input
          v-model:value="formState.taskPrefix"
          size="small"
          :readonly="!!dataSource?.id"
          :disabled="!!dataSource?.id"
          :maxlength="40"
          placeholder="输入任务前缀，最多可输入40个字符" />
        <Tooltip
          placement="right"
          arrowPointAtCenter
          :overlayStyle="{ 'max-width': '400px' }">
          <template #title>
            <div>设置后，迭代下任务名称会以该前缀作为开头。</div>
          </template>
          <Icon icon="icon-tishi1" class="text-tips absolute top-1.5 ml-1 text-3.5 cursor-pointer" />
        </Tooltip>
      </FormItem>

      <FormItem label="工作量评估" name="evalWorkloadMethod">
        <RadioGroup v-model:value="formState.evalWorkloadMethod" :disabled="editDisabled">
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

      <FormItem label="附件">
        <div class="flex items-center">
          <Upload
            :fileList="[]"
            name="file"
            :customRequest="() => { }"
            @change="upLoadFile">
            <a class="text-theme-special text-theme-text-hover text-3 flex items-center leading-5 h-5 mt-0.5">
              <Icon icon="icon-lianjie1" class="mr-1" /> <span class="whitespace-nowrap">上传附件</span>
            </a>
          </Upload>
          <Tooltip :overlayStyle="{ 'max-width': '400px' }">
            <template #title>
              <div class="text-3 text-theme-sub-content leading-4 break-all">
                支持的格式："jpg","bmp","png","gif","txt","docx","jpeg","rar","zip","doc","xlsx","xls","pdf"；最多上传10个附件。
              </div>
            </template>
            <Icon icon="icon-tishi1" class="text-tips ml-2 -mt-0.25 text-3.5 cursor-pointer" />
          </Tooltip>
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
          class="flex items-center text-3 leading-5 pb-1 px-3 space-x-5 bg-gray-100 first:rounded-t first:pt-2 last:rounded-b last:pb-2">
          <div class="flex-1 truncate">{{ item.name }}</div>
          <Button
            type="text"
            size="small"
            class="flex-shrink-0 flex items-center justify-center px-0 leading-5 h-5">
            <Icon
              icon="icon-qingchu"
              class="text-3.5"
              @click="delFile(index)" />
          </Button>
        </div>
      </FormItem>
      <Tabs size="small" class="pl-5">
        <TabPane key="acceptanceCriteria" tab="验收标准">
          <FormItem
            class="!mb-5"
            name="acceptanceCriteria"
            :rules="[{validator: validateMaxLen}]">
            <RichEditor
              ref="criteriaRichRef"
              v-model:value="formState.acceptanceCriteria"
              :options="{placeholder: '明确计软件产品交付的具体条件和标准。'}" />
            <!-- <Textarea
              v-model:value="formState.acceptanceCriteria"
              size="small"
              showCount
              :autoSize="autoSize"
              :maxlength="2000"
              placeholder="明确计软件产品交付的具体条件和标准。" /> -->
          </FormItem>
        </TabPane>
        <TabPane key="otherInformation" tab="其他说明">
          <FormItem
            class="!mb-5"
            name="otherInformation"
            :rules="[{validator: validateMaxLen}]">
            <RichEditor
              ref="infoRichRef"
              v-model:value="formState.otherInformation"
              :options="{placeholder: '其他说明，如迭代策略、风险评估和管理等。'}" />
            <!-- <Textarea
              v-model:value="formState.otherInformation"
              size="small"
              showCount
              :autoSize="autoSize"
              :maxlength="2000"
              placeholder="其他说明，如迭代策略、风险评估和管理等。" /> -->
          </FormItem>
        </TabPane>
      </Tabs>
    </Form>

    <AsyncComponent :visible="authorizeModalVisible">
      <AuthorizeModal
        v-model:visible="authorizeModalVisible"
        enumKey="TaskSprintPermission"
        :appId="props.appInfo?.id"
        :listUrl="`${TESTER}/task/sprint/auth?sprintId=${dataSource?.id}`"
        :delUrl="`${TESTER}/task/sprint/auth`"
        :addUrl="`${TESTER}/task/sprint/${dataSource?.id}/auth`"
        :updateUrl="`${TESTER}/task/sprint/auth`"
        :enabledUrl="`${TESTER}/task/sprint/${dataSource?.id}/auth/enabled`"
        :initStatusUrl="`${TESTER}/task/sprint/${dataSource?.id}/auth/status`"
        onTips="开启&quot;有权限控制&quot;后，需要手动授权服务权限后才会有迭代相应操作权限，默认开启&quot;有权限控制&quot;。注意：如果授权对象没有父级项目权限将自动授权查看权限。"
        offTips="开启&quot;无权限控制&quot;后，将允许所有用户公开查看和操作当前迭代，查看用户同时需要有当前迭代父级项目权限。"
        title="迭代权限"
        @change="authFlagChange" />
    </AsyncComponent>
  </Spin>
</template>

<style scoped>
:deep(.ant-form-item-label>label::after) {
  margin-right: 10px;
}

.ant-tabs-small>:deep(.ant-tabs-nav) .ant-tabs-tab {
  padding-top: 0;
}
</style>
