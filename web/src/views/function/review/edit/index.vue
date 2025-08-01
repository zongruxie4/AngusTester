<script setup lang="ts">
import { computed, defineAsyncComponent, inject, nextTick, onMounted, ref, watch } from 'vue';
import { Button, Form, FormItem, TabPane, Tabs, Upload } from 'ant-design-vue';
import {
  AsyncComponent,
  Icon,
  Input,
  modal,
  notification,
  Popover,
  ReviewStatus,
  Select,
  Spin,
  Table
} from '@xcan-angus/vue-ui';
import { utils, TESTER, enumUtils, upload, duration } from '@xcan-angus/infra';
import dayjs from 'dayjs';
import { isEqual } from 'lodash-es';
import { debounce } from 'throttle-debounce';
import RichEditor from '@/components/richEditor/index.vue';
import { func, funcPlan, project } from '@/api/tester';

import { ReviewInfo } from '../PropsType';
import { FormState, ReviewCaseInfo } from './PropsType';

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

const SelectCaseModal = defineAsyncComponent(() => import('./selectCaseModal.vue'));

const updateTabPane = inject<(data: { [key: string]: any }) => void>('updateTabPane', () => ({}));
const deleteTabPane = inject<(keys: string[]) => void>('deleteTabPane', () => ({}));
const replaceTabPane = inject<(id: string, data: { [key: string]: any }) => void>('replaceTabPane', () => ({}));
const isAdmin = inject('isAdmin', ref(false));
const reviewId = ref();
const formRef = ref();
const selectModalVisible = ref(false);
const keywords = ref();
const loading = ref(false);
const dataSource = ref<ReviewInfo>();

const activeTabKey = ref('funcCase');

const evalWorkloadMethodOptions = ref<{ value: string, description: string }[]>([]);
const reviewFlagVisible = ref(false);

const permissions = ref<string[]>([]);
const oldFormState = ref<FormState>();
const _deadlineDate = dayjs().add(1, 'month').format('YYYY-MM-DD HH:mm:ss');
const formState = ref<FormState>({
  planId: undefined,
  description: '',
  name: '',
  ownerId: props.userInfo?.id,
  deadlineDate: _deadlineDate,
  attachments: [],
  caseIds: [],
  participantIds: []
});

const uploadOptions = { bizKey: 'angusTesterCaseAttachments' };
const editorInit = {
  menubar: false,
  height: 368,
  selector: 'textarea', // 选择器，指定要转换为富文本编辑器的textarea元素
  content_style: `
  body { font-size: 12px; }
    #tinymce.mce-content-body[data-mce-placeholder]:not(.mce-visualblocks)::before {
    color: #BFBFBF !important;
  }
  `,
  placeholder: '请输入备注，最多支持2000字符，包括文件上传后URL地址长度' // 设置内容区的占位文字提示
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

const getParams = () => {
  const params: FormState = { ...formState.value, caseIds: caseList.value.map(i => i.id) };
  if (dataSource.value?.id) {
    params.id = dataSource.value.id;
  }

  if (!params.attachments?.length) {
    delete params.attachments;
  }

  if (!params.description) {
    delete params.description;
  }

  delete params.date;

  return params;
};

const refreshList = () => {
  nextTick(() => {
    updateTabPane({ _id: 'reviewList', notify: utils.uuid() });
  });
};

const editOk = async () => {
  const equalFlag = isEqual(oldFormState.value, formState.value);
  if (equalFlag) {
    return;
  }

  const params = getParams();
  delete params.planId;
  loading.value = true;
  const [error] = await func.putReview(params);
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
  const [error, res] = await func.addReview(params);
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

const descRichRef = ref();
const validateDesc = async () => {
  if (descRichRef.value && descRichRef.value.getLength() > 2000) {
    return Promise.reject(new Error('字符不能超过2000'));
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
  });
};

const toDelete = async () => {
  const data = dataSource.value;
  if (!data) {
    return;
  }

  modal.confirm({
    content: `确定删除评审【${data.name}】吗？`,
    async onOk () {
      const id = data.id;
      loading.value = true;
      const [error] = await func.deleteReview(id);
      loading.value = false;
      if (error) {
        return;
      }

      notification.success('评审删除成功， 您可以在回收站查看删除后的评审');
      deleteTabPane([id]);
      refreshList();
    }
  });
};

const cancel = () => {
  deleteTabPane([props.data._id]);
};

const loadData = async (id: string) => {
  if (loading.value) {
    return;
  }

  loading.value = true;
  const [error, res] = await func.getReviewDetail(id);
  loading.value = false;
  if (error) {
    return;
  }

  const data = res?.data as ReviewInfo;
  if (!data) {
    return;
  }
  await loadPermissions(data.planId);

  dataSource.value = data;
  setFormData(data);

  const name = data.name;
  if (name && typeof updateTabPane === 'function') {
    updateTabPane({ name, _id: id });
  }
};

const setFormData = (data: ReviewInfo) => {
  reviewFlagVisible.value = false;
  if (!data) {
    formState.value = {
      planId: '',
      description: '',
      name: '',
      ownerId: props.userInfo?.id,
      deadlineDate: _deadlineDate,
      attachments: [],
      caseIds: [],
      participantIds: []
    };

    return;
  }

  const {
    description = '',
    name = '',
    ownerId = '',
    attachments = '',
    deadlineDate = '',
    planId,
    caseIds,

    participants = []
  } = data;

  formState.value.description = description;
  formState.value.deadlineDate = deadlineDate;
  formState.value.name = name;
  formState.value.ownerId = ownerId;
  formState.value.caseIds = caseIds;
  formState.value.planId = planId;
  formState.value.participantIds = participants.map(i => i.id);

  participants.forEach(i => {
    if (!members.value.find(m => m.id === i.id)) {
      members.value.push({
        ...m,
        value: m.id,
        label: m.fullName
      });
    }
  });

  formState.value.attachments = attachments || [];
  oldFormState.value = JSON.parse(JSON.stringify(formState.value));
};

const loadEnums = () => {
  const data = enumUtils.enumToMessages('EvalWorkloadMethod');
  evalWorkloadMethodOptions.value = data as { description: string; value: string; }[];
};

const loadPermissions = async (id: string) => {
  if (isAdmin.value) {
    permissions.value = [
      // 'MODIFY_PLAN',
      // 'DELETE_PLAN',
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

const members = ref<{id: string; fullName: string; value: string; label: string}[]>([]);

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

const handleChangePlanId = () => {
  caseList.value = [];
};

const loadCaseList = async () => {
  const [error, { data }] = await func.getReviewCaseList({
    reviewId: reviewId.value,
    filters: keywords.value ? [{ value: keywords.value, key: 'caseName', op: 'MATCH_END' }] : [],
    pageNo: pagination.value.current,
    pageSize: pagination.value.pageSize
  });

  if (error) {
    return;
  }
  caseList.value = data?.list || [];
  pagination.value.total = +data.total || 0;
};

const onKeywordChange = debounce(duration.search, () => {
  if (!reviewId.value) {
    return;
  }
  pagination.value.current = 1;
  loadCaseList();
});

const addReviewCase = () => {
  selectModalVisible.value = true;
};

const handleAddCase = async (caseIds: string[], cases: ReviewCaseInfo[]) => {
  if (reviewId.value) {
    const [error] = await func.addReviewCase({
      caseIds: caseIds,
      reviewId: reviewId.value
    });
    if (error) {
      return;
    }
    selectModalVisible.value = false;
    loadCaseList();
  } else {
    selectModalVisible.value = false;
    caseList.value.push(...cases);
  }
};

const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0
});
const caseList = ref<ReviewCaseInfo[]>([]);
const columns = [
  {
    title: '用例ID',
    dataIndex: 'caseId',
    customRender: ({ record }) => {
      if (reviewId.value) {
        return record.caseId;
      }
      return record.id;
    }
  },
  {
    title: '名称',
    dataIndex: 'caseName',
    customRender: ({ record }) => {
      if (reviewId.value) {
        return record.caseInfo.name;
      }
      return record.name;
    }
  },
  {
    title: '评审状态',
    dataIndex: 'reviewStatus'
  },
  {
    title: '操作',
    dataIndex: 'action'
  }
];

const delCase = async (record: ReviewCaseInfo) => {
  if (reviewId.value) {
    modal.confirm({
      title: `确认删除【${record.name}】吗？`,
      async onOk () {
        const [error] = await func.deleteReviewCase([record.id]);
        if (error) {
          return;
        }
        if (pagination.value.current !== 1 && caseList.value.length === 1) {
          pagination.value.current -= 1;
        }
        loadCaseList();
      }
    });
  } else {
    caseList.value = caseList.value.filter(i => i.id !== record.id);
  }
};

const pageChange = ({ current, pageSize }) => {
  pagination.value.current = current;
  pagination.value.pageSize = pageSize;
  loadCaseList();
};

onMounted(async () => {
  loadEnums();
  await loadMembers();

  watch(() => props.data, async (newValue, oldValue) => {
    const id = newValue?.id;
    if (!id) {
      return;
    }

    const oldId = oldValue?.id;
    if (id === oldId) {
      return;
    }

    reviewId.value = id;

    // await loadPermissions(id);
    await loadData(id);
    await loadCaseList();
  }, { immediate: true });
});
const editFlag = computed(() => {
  return !!props.data?.id;
});
</script>

<template>
  <Spin :spinning="loading" class="h-full text-3 leading-5 px-5 py-5 overflow-auto">
    <div class="flex items-center space-x-2.5 mb-5">
      <Button
        :disabled="!isAdmin && !permissions.includes('REVIEW')"
        type="primary"
        size="small"
        class="flex items-center space-x-1"
        @click="ok">
        <Icon icon="icon-dangqianxuanzhong" class="text-3.5" />
        <span>保存</span>
      </Button>

      <template v-if="editFlag">
        <Button
          :disabled="!isAdmin && !permissions.includes('REVIEW')"
          type="default"
          size="small"
          class="flex items-center space-x-1"
          @click="toDelete">
          <Icon icon="icon-qingchu" class="text-3.5" />
          <span>删除</span>
        </Button>
        <Button
          :disabled="!isAdmin && !permissions.includes('REVIEW')"
          type="default"
          size="small"
          :href="`/function#reviews?id=${reviewId}`"
          class="flex items-center space-x-1">
          <Icon icon="icon-pingshen" class="text-3.5" />
          <span>立即评审</span>
        </Button>
      </template>

      <Button
        size="small"
        class="flex items-center space-x-1"
        @click="cancel">
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
        label="名称"
        name="name"
        :rules="{ required: true, message: '请输入评审名称' }">
        <Input
          v-model:value="formState.name"
          size="small"
          :maxlength="200"
          :placeholder="'评审简要概述，最多支持200个字符'" />
      </FormItem>
      <FormItem
        label="测试计划"
        name="planId"
        :rules="{ required: true, message: '请选择测试计划' }">
        <Select
          v-model:value="formState.planId"
          size="small"
          :disabled="!!reviewId"
          :action="`${TESTER}/func/plan?projectId=${props.projectId}&review=true&fullTextSearch=true`"
          :fieldNames="{value: 'id', label: 'name'}"
          :placeholder="'选择测试计划'"
          @change="handleChangePlanId" />
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
      </FormItem>

      <FormItem
        label="参与人员"
        name="participantIds"
        class="relative"
        :rules="{ required: true, message: '请选择参与人员' }">
        <Select
          v-model:value="formState.participantIds"
          :options="members"
          mode="multiple"
          size="small"
          placeholder="选择参与人员" />
      </FormItem>

      <FormItem label="附件">
        <div class="flex items-center mt-0.5">
          <Upload
            v-if="!formState?.attachments || formState?.attachments?.length < 10"
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
        class="pl-5 reviewEditTab">
        <TabPane
          key="funcCase"
          forceRender
          tab="评审用例">
          <div class="flex justify-between mb-3">
            <Input
              v-model:value="keywords"
              :disabled="!reviewId"
              placeholder="输入查询名称"
              class="w-50"
              @change="onKeywordChange" />
            <Button
              :disabled="!formState.planId || (reviewId && !permissions.includes('REVIEW')) || dataSource?.status?.value === 'COMPLETED'"
              size="small"
              type="primary"
              @click="addReviewCase">
              <Icon icon="icon-jia" class="mr-1" />
              添加评审用例
            </Button>
          </div>
          <Table
            :columns="columns"
            :dataSource="caseList"
            :pagination="reviewId ? pagination : false"
            rowKey="id"
            size="small"
            noDataSize="small"
            @change="pageChange">
            <template #bodyCell="{record, column}">
              <template v-if="column.dataIndex === 'action'">
                <Button
                  type="text"
                  size="small"
                  @click="delCase(record)">
                  <Icon icon="icon-qingchu" />
                  删除
                </Button>
              </template>
              <template v-if="column.dataIndex === 'reviewStatus'">
                <ReviewStatus :value="record.reviewStatus" />
              </template>
            </template>
          </Table>
        </TabPane>
        <TabPane key="description" tab="评审说明">
          <!-- <Textarea
            v-model:value="formState.description"
            :rows="8"
            showCount
            placeholder="评审说明。"
            :maxlength="2000" /> -->
          <FormItem name="description" :rules="[{validator: validateDesc}]">
            <RichEditor
              ref="descRichRef"
              v-model:value="formState.description"
              class="review-description" />
          </FormItem>
        </TabPane>
      </Tabs>
    </Form>
    <AsyncComponent :visible="selectModalVisible">
      <SelectCaseModal
        v-model:visible="selectModalVisible"
        :projectId="props.projectId"
        :planId="formState.planId"
        :reviewId="reviewId"
        @ok="handleAddCase" />
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
