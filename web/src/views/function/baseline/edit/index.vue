<script setup lang="ts">
import { computed, defineAsyncComponent, inject, nextTick, onMounted, ref, watch } from 'vue';
import { Button, Form, FormItem, TabPane, Tabs } from 'ant-design-vue';
import { AsyncComponent, Icon, Input, modal, notification, Select, Spin, Table } from '@xcan-angus/vue-ui';
import { utils, TESTER, enumLoader, duration } from '@xcan-angus/tools';
import { isEqual } from 'lodash-es';
import { debounce } from 'throttle-debounce';
import { func, project } from '@/api/tester';

import { BaselineInfo } from '../PropsType';
import { BaselineCaseInfo, FormState } from './PropsType';

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
const SelectCaseModal = defineAsyncComponent(() => import('./selectCaseModal.vue'));

const updateTabPane = inject<(data: { [key: string]: any }) => void>('updateTabPane', () => ({}));
const deleteTabPane = inject<(keys: string[]) => void>('deleteTabPane', () => ({}));
const replaceTabPane = inject<(id: string, data: { [key: string]: any }) => void>('replaceTabPane', () => ({}));
const baselineId = ref();
const formRef = ref();
const selectModalVisible = ref(false);
const keywords = ref();
const loading = ref(false);
const dataSource = ref<BaselineInfo>();

const activeTabKey = ref('funcCase');

const evalWorkloadMethodOptions = ref<{ value: string, message: string }[]>([]);
const baselineFlagVisible = ref(false);

const oldFormState = ref<FormState>();
const formState = ref<FormState>({
  planId: '',
  description: '',
  name: '',
  caseIds: []
});

const getParams = () => {
  const params: FormState = { ...formState.value, caseIds: caseList.value.map(i => i.id) };
  if (dataSource.value?.id) {
    params.id = dataSource.value.id;
  }

  if (!params.description && !params.id) {
    delete params.description;
  }

  if (editFlag.value) {
    delete params.caseIds;
  }

  delete params.date;

  return params;
};

const refreshList = () => {
  nextTick(() => {
    updateTabPane({ _id: 'baselineList', notify: utils.uuid() });
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
  const [error] = await func.updateBaseline(params);
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
  const [error, res] = await func.addBaseline(params);
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
    content: `确定删除基线【${data.name}】吗？`,
    async onOk () {
      const id = data.id;
      loading.value = true;
      const [error] = await func.deleteBaseline(id);
      loading.value = false;
      if (error) {
        return;
      }

      notification.success('基线删除成功， 您可以在回收站查看删除后的基线');
      deleteTabPane([id, id + '_detail']);
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
  const [error, res] = await func.getBaselineInfo(id);
  loading.value = false;
  if (error) {
    return;
  }

  const data = res?.data as BaselineInfo;
  if (!data) {
    return;
  }
  // await loadPermissions(data.planId);

  dataSource.value = data;
  setFormData(data);

  const name = data.name;
  if (name && typeof updateTabPane === 'function') {
    updateTabPane({ name, _id: id });
  }
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

const setFormData = (data: BaselineInfo) => {
  baselineFlagVisible.value = false;
  if (!data) {
    formState.value = {
      planId: '',
      description: '',
      name: '',
      caseIds: []
    };
    return;
  }

  const {
    description = '',
    name = '',
    planId,
    caseIds
  } = data;

  formState.value.description = getJson(description);
  formState.value.name = name;
  formState.value.caseIds = caseIds;
  formState.value.planId = planId;
  oldFormState.value = JSON.parse(JSON.stringify(formState.value));
};

const loadEnums = async () => {
  const [error, data] = await enumLoader.load('EvalWorkloadMethod');
  if (error) {
    return;
  }

  evalWorkloadMethodOptions.value = data as { message: string; value: string; }[];
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

const handleChangePlanId = () => {
  caseList.value = [];
};

const loadCaseList = async () => {
  const [error, { data }] = await func.searchBaselineCase(baselineId.value, {
    filters: keywords.value ? [{ value: keywords.value, key: 'caseName', op: 'MATCH_END' }] : [],
    pageNo: pagination.value.current,
    pageSize: pagination.value.pageSize,
    projectId: props.projectId
  });

  if (error) {
    return;
  }
  caseList.value = data?.list || [];
  pagination.value.total = +data.total || 0;
};

const onKeywordChange = debounce(duration.search, () => {
  if (!baselineId.value) {
    return;
  }
  pagination.value.current = 1;
  loadCaseList();
});

const addBaselineCase = () => {
  selectModalVisible.value = true;
};

const handleAddCase = async (caseIds: string[], cases: BaselineCaseInfo[]) => {
  if (baselineId.value) {
    const [error] = await func.addBaselineCase(baselineId.value, caseIds);
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
const caseList = ref<BaselineCaseInfo[]>([]);
const columns = [
  {
    title: '用例ID',
    dataIndex: 'id'
  },
  {
    title: '名称',
    dataIndex: 'name'
  },
  {
    title: '操作',
    dataIndex: 'action'
  }
];

const delCase = async (record: BaselineCaseInfo) => {
  if (baselineId.value) {
    modal.confirm({
      title: `确认删除【${record.name}】吗？`,
      async onOk () {
        const [error] = await func.deleteBaselineCaseByCaseIdInBaseline([record.id]);
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

const richEditorRef = ref();
const validateDesc = () => {
  if (!formState.value.description) {
    return Promise.resolve();
  }
  if (richEditorRef.value && richEditorRef.value.getLength() > 2000) {
    return Promise.reject('字符不能超过2000');
  }
  // if (formState.value.description.length > 2000) {
  //   return Promise.reject('字符不能超过2000');
  // }
  return Promise.resolve();
};

const pageChange = ({ current, pageSize }) => {
  pagination.value.current = current;
  pagination.value.pageSize = pageSize;
  loadCaseList();
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

    baselineId.value = id;

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
        type="primary"
        size="small"
        class="flex items-center space-x-1"
        @click="ok">
        <Icon icon="icon-dangqianxuanzhong" class="text-3.5" />
        <span>保存</span>
      </Button>

      <template v-if="editFlag">
        <Button
          type="default"
          size="small"
          class="flex items-center space-x-1"
          @click="toDelete">
          <Icon icon="icon-qingchu" class="text-3.5" />
          <span>删除</span>
        </Button>
      </template>

      <Button
        type="default"
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
        :rules="{ required: true, message: '请输入基线名称' }">
        <Input
          v-model:value="formState.name"
          size="small"
          :maxlength="200"
          :placeholder="'基线简要概述，最多支持200个字符'" />
      </FormItem>
      <FormItem
        label="测试计划"
        name="planId"
        :rules="{ required: true, message: '请选择测试计划' }">
        <Select
          v-model:value="formState.planId"
          size="small"
          :disabled="!!baselineId"
          :action="`${TESTER}/func/plan?projectId=${props.projectId}&fullTextSearch=true`"
          :fieldNames="{value: 'id', label: 'name'}"
          :placeholder="'选择测试计划'"
          @change="handleChangePlanId" />
      </FormItem>

      <FormItem
        label="描述"
        name="description"
        :rules="[{validator: validateDesc}]">
        <RichEditor
          ref="richEditorRef"
          v-model:value="formState.description"
          placeholder="基线简要概述，最多支持2000个字符"
          :height="100" />
        <!-- <Textarea
          v-model:value="formState.description"
          size="small"
          :maxlength="2000"
          :placeholder="'基线简要概述，最多支持2000个字符'" /> -->
      </FormItem>

      <Tabs
        v-model:activeKey="activeTabKey"
        size="small"
        class="pl-5 baselineEditTab">
        <TabPane
          key="funcCase"
          forceRender
          tab="基线用例">
          <div class="flex justify-between mb-3">
            <Input
              v-model:value="keywords"
              :disabled="!baselineId"
              placeholder="输入查询名称"
              class="w-50"
              @change="onKeywordChange" />
            <Button
              :disabled="!formState.planId || dataSource?.established"
              size="small"
              type="primary"
              @click="addBaselineCase">
              <Icon icon="icon-jia" class="mr-1" />
              添加基线用例
            </Button>
          </div>
          <Table
            :columns="columns"
            :dataSource="caseList"
            :pagination="baselineId ? pagination : false"
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
            </template>
          </Table>
        </TabPane>
      </Tabs>
    </Form>
    <AsyncComponent :visible="selectModalVisible">
      <SelectCaseModal
        v-model:visible="selectModalVisible"
        :planId="formState.planId"
        :baselineId="baselineId"
        :projectId="props.projectId"
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
