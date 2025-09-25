<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { Button, Radio, RadioGroup, TabPane, Tabs, CheckboxGroup, Checkbox } from 'ant-design-vue';
import { duration, GM, http, enumUtils } from '@xcan-angus/infra';

import { Icon, Hints, Spin, Select, Table } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
const { t } = useI18n();

type Tabkey = 'USER' | 'DEPT' | 'GROUP'

interface Props {
  appId: string;
  disabled: boolean;
  enumKey: any;
  listUrl: string;
  delUrl: string;
  addUrl: string;
  updateUrl: string;
  enabledUrl: string;
  initStatusUrl: string;
  auth: boolean;
  onTips: string;
  offTips: string;
  emptyTextStyle?: { [key: string]: string };// 无数据样式
  noDataSize: 'large' | 'middle' | 'small';
}

const props = withDefaults(defineProps<Props>(), {
  appId: '',
  disabled: false,
  enumKey: '',
  listUrl: '',
  delUrl: '',
  addUrl: '',
  updateUrl: '',
  enabledUrl: '',
  initStatusUrl: '',
  auth: false,
  onTips: '',
  offTips: '',
  emptyTextStyle: undefined,
  noDataSize: 'middle'
});

const emits = defineEmits<{(e: 'change', value: { auth: boolean; }): void; }>();

const controled = ref(false);
const activeKey = ref<Tabkey>('USER');

const userColumns = [
  {
    title: t('common.username'),
    dataIndex: 'name',
    width: 200,
    ellipsis: true
  },
  {
    title: t('actions.permission'),
    dataIndex: 'auth'
  },
  {
    title: t('common.actions'),
    dataIndex: 'action',
    width: 100
  }
];
const deptColumns = [
  {
    title: t('xcan_authorize.departmentName'),
    dataIndex: 'name',
    width: 200
  },
  {
    title: t('actions.permission'),
    dataIndex: 'auth'
  },
  {
    title: t('common.actions'),
    dataIndex: 'action',
    width: 100
  }
];
const groupColumns = [
  {
    title: t('xcan_authorize.groupName'),
    dataIndex: 'name',
    width: 200
  },
  {
    title: t('actions.permission'),
    dataIndex: 'auth'
  },
  {
    title: t('common.actions'),
    dataIndex: 'action',
    width: 100
  }
];

const loading = ref(false);
const userParams = ref({ pageNo: 1, pageSize: 10 });
const userAutList = ref([]);
const userTableTotal = ref(0);
const userAuthMap = ref<{ [key: string]: string[] }>({});
const getUserAuthList = async () => {
  const _params = { ...userParams.value, authObjectType: activeKey.value };
  loading.value = true;
  const [error, { data = { list: [], total: 0 } }] = await http.get(props.listUrl, _params);
  loading.value = false;
  if (error) {
    return;
  }

  userAutList.value = data.list.map(item => {
    userAuthMap.value[item.id] = item.permissions?.map(item => item.value) || [];
    return { ...item, checkedAll: item.permissions?.length === authOptions.value.length };
  });

  userTableTotal.value = data.total;
};

const deptParams = ref({ pageNo: 1, pageSize: 10 });
const deptAutList = ref([]);
const deptTableTotal = ref(0);
const deptAuthMap = ref<{ [key: string]: string[] }>({});
const getDeptAuthList = async () => {
  const _params = { ...deptParams.value, authObjectType: activeKey.value };
  loading.value = true;
  const [error, { data = { list: [], total: 0 } }] = await http.get(props.listUrl, _params);
  loading.value = false;
  if (error) {
    return;
  }

  deptAutList.value = data.list.map(item => {
    deptAuthMap.value[item.id] = item.permissions?.map(item => item.value) || [];
    return { ...item, checkedAll: item.permissions?.length === authOptions.value.length };
  });
  deptTableTotal.value = data.total;
};

const groupParams = ref({ pageNo: 1, pageSize: 10 });
const groupAutList = ref([]);
const groupTableTotal = ref(0);
const groupAuthMap = ref<{ [key: string]: string[] }>({});
const getGroupAuthList = async () => {
  const _params = { ...groupParams.value, authObjectType: activeKey.value };
  loading.value = true;
  const [error, { data = { list: [], total: 0 } }] = await http.get(props.listUrl, _params);
  loading.value = false;
  if (error) {
    return;
  }

  groupAutList.value = data.list.map(item => {
    groupAuthMap.value[item.id] = item.permissions?.map(item => item.value) || [];
    return { ...item, checkedAll: item.permissions?.length === authOptions.value?.length };
  });
  groupTableTotal.value = data.total;
};

const tabChange = (key: string) => {
  switch (key) {
    case 'USER':
      getUserAuthList();
      break;
    case 'DEPT':
      getDeptAuthList();
      break;
    case 'GROUP':
      getGroupAuthList();
      break;
  }
};

const authChange = async (values: string[], record) => {
  const lastAuthList = record.permissions.map(item => item.value);
  if (values.includes('RELEASE') && !lastAuthList.includes('MODIFY')) {
    values.push('MODIFY');
  }
  if (!values.includes('MODIFY') && values.includes('RELEASE')) {
    values = values.filter(item => item !== 'RELEASE');
  }
  const _params = {
    authObjectId: record.id,
    permissions: values
  };
  loading.value = true;
  const [error] = await http.put(`${props.updateUrl}/${record.id}`, _params);
  loading.value = false;
  if (error) {
    return;
  }
  const checkAuth = authOptions.value.filter(item => values.includes(item.value));
  record.permissions = checkAuth;
  if (values.length === authOptions.value.length) {
    record.checkedAll = true;
  } else {
    record.checkedAll = false;
  }
  switch (activeKey.value) {
    case 'USER':
      userAuthMap.value[record.id] = values;
      break;
    case 'DEPT':
      deptAuthMap.value[record.id] = values;
      break;
    case 'GROUP':
      groupAuthMap.value[record.id] = values;
      break;
  }
};

const checkAllChange = async (checked: boolean, record) => {
  const _params = {
    authObjectId: record.id,
    permissions: checked ? authOptions.value.map(item => item.value) : ['VIEW']
  };
  loading.value = true;
  const [error] = await http.put(`${props.updateUrl}/${record.id}`, _params);
  loading.value = false;
  if (error) {
    return;
  }

  if (checked) {
    record.permissions = authOptions.value;
  } else {
    const viewAuth = authOptions.value.filter(item => item.value === 'VIEW');
    record.permissions = viewAuth;
  }

  const _authValues = record.permissions?.map(item => item.value);

  switch (activeKey.value) {
    case 'USER':
      userAuthMap.value[record.id] = _authValues;
      break;
    case 'DEPT':
      deptAuthMap.value[record.id] = _authValues;
      break;
    case 'GROUP':
      groupAuthMap.value[record.id] = _authValues;
      break;
  }
};

const authOptions = ref([]);
const loadEnums = async () => {
  const data = enumUtils.enumToMessages(props.enumKey);
  authOptions.value = (data || [])?.map(item => {
    if (item.value === 'VIEW') {
      return { label: item.message, value: item.value, disabled: true };
    }
    return { label: item.message, value: item.value };
  }) || [];
};

const delAuth = async (id: string) => {
  loading.value = true;
  const [error] = await http.del(`${props.delUrl}/${id}`);
  loading.value = false;
  if (error) {
    return;
  }
  switch (activeKey.value) {
    case 'USER':
      userParams.value.pageNo = getCurrentPage(userParams.value.pageNo, userParams.value.pageSize, userTableTotal.value);
      getUserAuthList();
      break;
    case 'DEPT':
      deptParams.value.pageNo = getCurrentPage(deptParams.value.pageNo, deptParams.value.pageSize, deptTableTotal.value);
      getDeptAuthList();
      break;
    case 'GROUP':
      groupParams.value.pageNo = getCurrentPage(groupParams.value.pageNo, groupParams.value.pageSize, groupTableTotal.value);
      getGroupAuthList();
      break;
  }
};

const getCurrentPage = (pageNo: number, pageSize: number, total: number): number => {
  if (pageNo === 1 || pageSize >= total) {
    return pageNo;
  }
  const lastPageData = total % pageSize;
  if (lastPageData === 1) {
    return pageNo - 1;
  }

  return pageNo;
};

const tableChange = (_pagination) => {
  const { current, pageSize } = _pagination;
  switch (activeKey.value) {
    case 'USER':
      userParams.value.pageNo = current;
      userParams.value.pageSize = pageSize;
      getUserAuthList();
      break;
    case 'DEPT':
      deptParams.value.pageNo = current;
      deptParams.value.pageSize = pageSize;
      getDeptAuthList();
      break;
    case 'GROUP':
      groupParams.value.pageNo = current;
      groupParams.value.pageSize = pageSize;
      getGroupAuthList();
      break;
  }
};

const userId = ref<string>();
const deptId = ref<string>();
const groupId = ref<string>();
const addAuth = async () => {
  const _params = {
    authObjectType: activeKey.value,
    authObjectId: activeKey.value === 'USER' ? userId.value : activeKey.value === 'DEPT' ? deptId.value : groupId.value,
    permissions: ['VIEW']
  };
  loading.value = true;
  const [error] = await http.post(props.addUrl, _params);
  loading.value = false;
  if (error) {
    return;
  }

  loadList();
};

const enableChange = async (auth: boolean) => {
  loading.value = true;
  const [error] = await http.patch(`${props.enabledUrl}?enabled=${auth}`);
  loading.value = false;
  if (error) {
    return;
  }
  controled.value = auth;

  if (auth) {
    if (!authOptions.value?.length) {
      await loadEnums();
    }

    activeKey.value = 'USER';
    loadList();
  }

  emits('change', { auth });
};

const loadList = () => {
  switch (activeKey.value) {
    case 'USER':
      userParams.value.pageNo = 1;
      getUserAuthList();
      userId.value = undefined;
      break;
    case 'DEPT':
      deptParams.value.pageNo = 1;
      getDeptAuthList();
      deptId.value = undefined;
      break;
    case 'GROUP':
      groupParams.value.pageNo = 1;
      getGroupAuthList();
      groupId.value = undefined;
      break;
  }
};

const pagination = computed(() => {
  switch (activeKey.value) {
    case 'USER':
      return {
        current: userParams.value.pageNo,
        pageSize: userParams.value.pageSize,
        total: userTableTotal.value
      };
    case 'GROUP':
      return {
        current: groupParams.value.pageNo,
        pageSize: groupParams.value.pageSize,
        total: groupTableTotal.value
      };
    case 'DEPT':
      return {
        current: deptParams.value.pageNo,
        pageSize: deptParams.value.pageSize,
        total: deptTableTotal.value
      };
    default: return {
      current: userParams.value.pageNo,
      pageSize: userParams.value.pageSize,
      total: userTableTotal.value
    };
  }
});

const initStatus = async () => {
  loading.value = true;
  const [error, { data }] = await http.get(`${props.initStatusUrl}`);
  loading.value = false;
  if (error) {
    return;
  }

  if (typeof data === 'boolean' && data) {
    if (!authOptions.value?.length) {
      await loadEnums();
    }
    controled.value = data;
    activeKey.value = 'USER';
    loadList();
  }
};

onMounted(() => {
  if (props.initStatusUrl) {
    initStatus();
  }
});
</script>
<template>
  <Spin
    :spinning="loading"
    class="p-2"
    :delay="duration.delay">
    <div>
      <RadioGroup
        :value="controled"
        :disabled="props.disabled"
        @change="(e) => enableChange(e.target.value)">
        <Radio :value="false">{{ t('xcan_authorize.noPermissionControl') }}</Radio>
        <Radio :value="true">{{ t('xcan_authorize.hasPermissionControl') }}</Radio>
      </RadioGroup>
    </div>
    <Hints class="mt-2 mb-2" :text="controled ? props.onTips : props.offTips" />
    <div v-show="controled">
      <Tabs
        v-model:activeKey="activeKey"
        size="small"
        @change="tabChange">
        <TabPane key="USER" :tab="t('xcan_authorize.user')">
          <div class="flex justify-between -mt-1">
            <Select
              v-model:value="userId"
              :action="`${GM}/app/${props.appId}/auth/user`"
              :fieldNames="{ label: 'fullName', value: 'id' }"
              :placeholder="t('xcan_authorize.selectUser')"
              showSearch
              allowClear
              class="w-full mr-5"
              style="max-width: 400px;" />
            <Button
              :disabled="!userId"
              class="flex items-center"
              size="small"
              type="primary"
              @click="addAuth">
              <template #icon>
                <Icon icon="icon-jia" class="mr-1" />
              </template>
              {{ t('xcan_authorize.add') }}
            </Button>
          </div>
          <Table
            class="mt-2 text-3 leading-3"
            size="small"
            :columns="userColumns"
            :dataSource="userAutList"
            :pagination="pagination"
            :emptyTextStyle="props.emptyTextStyle"
            :noDataSize="props.noDataSize"
            rowKey="id"
            @change="tableChange">
            <template #bodyCell="{ column, record }">
              <div v-if="column.dataIndex === 'auth'">
                <CheckboxGroup
                  v-model:value="userAuthMap[record.id]"
                  :options="authOptions"
                  :disabled="record.creator"
                  @change="(values) => authChange(values as string[], record)">
                </CheckboxGroup>
              </div>
              <div v-if="column.dataIndex === 'action'" class="flex items-center whitespace-nowrap">
                <Checkbox
                  v-model:checked="record.checkedAll"
                  :disabled="record.creator"
                  @change="(e) => checkAllChange(e.target.checked, record)">
                  {{ t('xcan_authorize.all') }}
                </Checkbox>
                <Button
                  type="link"
                  size="small"
                  class="px-2"
                  :disabled="record.creator"
                  @click="delAuth(record.id)">
                  <Icon icon="icon-qingchu" class="text-4" />
                </Button>
              </div>
            </template>
          </Table>
        </TabPane>
        <TabPane key="DEPT" :tab="t('xcan_authorize.department')">
          <div class="flex justify-between -mt-1">
            <Select
              v-model:value="deptId"
              :action="`${GM}/app/${props.appId}/auth/dept`"
              :fieldNames="{ label: 'name', value: 'id' }"
              :placeholder="t('xcan_authorize.selectDepartment')"
              showSearch
              allowClear
              class="w-full mr-5"
              style="max-width: 400px;" />
            <Button
              class="flex items-center"
              size="small"
              type="primary"
              :disabled="!deptId"
              @click="addAuth">
              <template #icon>
                <Icon icon="icon-jia" class="mr-1" />
              </template>
              {{ t('xcan_authorize.add') }}
            </Button>
          </div>
          <Table
            class="mt-2 text-3 leading-3"
            size="small"
            :columns="deptColumns"
            :dataSource="deptAutList"
            :pagination="pagination"
            :emptyTextStyle="props.emptyTextStyle"
            :noDataSize="props.noDataSize"
            rowKey="id"
            @change="tableChange">
            <template #bodyCell="{ column, record }">
              <div v-if="column.dataIndex === 'auth'">
                <CheckboxGroup
                  v-model:value="deptAuthMap[record.id]"
                  :options="authOptions"
                  :disabled="record.creator"
                  @change="(values) => authChange(values as string[], record)">
                </CheckboxGroup>
              </div>
              <div v-if="column.dataIndex === 'action'" class="flex items-center whitespace-nowrap">
                <Checkbox
                  v-model:checked="record.checkedAll"
                  :disabled="record.creator"
                  @change="(e) => checkAllChange(e.target.checked, record)">
                  {{ t('xcan_authorize.all') }}
                </Checkbox>
                <Button
                  type="link"
                  size="small"
                  class="px-2"
                  :disabled="record.creator"
                  @click="delAuth(record.id)">
                  <Icon icon="icon-qingchu" class="text-4" />
                </Button>
              </div>
            </template>
          </Table>
        </TabPane>
        <TabPane key="GROUP" :tab="t('xcan_authorize.group')">
          <div class="flex justify-between -mt-1">
            <Select
              v-model:value="groupId"
              :action="`${GM}/app/${props.appId}/auth/group`"
              :fieldNames="{ label: 'name', value: 'id' }"
              :placeholder="t('xcan_authorize.selectGroup')"
              showSearch
              allowClear
              class="w-full mr-5"
              style="max-width: 400px;" />
            <Button
              class="flex items-center"
              size="small"
              type="primary"
              :disabled="!groupId"
              @click="addAuth">
              <template #icon>
                <Icon icon="icon-jia" class="mr-1" />
              </template>
              {{ t('xcan_authorize.add') }}
            </Button>
          </div>
          <Table
            class="mt-2 text-3 leading-3"
            size="small"
            :columns="groupColumns"
            :dataSource="groupAutList"
            :pagination="pagination"
            :emptyTextStyle="props.emptyTextStyle"
            :noDataSize="props.noDataSize"
            rowKey="id"
            @change="tableChange">
            <template #bodyCell="{ column, record }">
              <div v-if="column.dataIndex === 'auth'">
                <CheckboxGroup
                  v-model:value="groupAuthMap[record.id]"
                  :options="authOptions"
                  :disabled="record.creator"
                  @change="(values) => authChange(values as string[], record)">
                </CheckboxGroup>
              </div>
              <div v-if="column.dataIndex === 'action'" class="flex items-center whitespace-nowrap">
                <Checkbox
                  v-model:checked="record.checkedAll"
                  :disabled="record.creator"
                  @change="(e) => checkAllChange(e.target.checked, record)">
                  {{ t('xcan_authorize.all') }}
                </Checkbox>
                <Button
                  type="link"
                  size="small"
                  class="px-2"
                  :disabled="record.creator"
                  @click="delAuth(record.id)">
                  <Icon icon="icon-qingchu" class="text-4" />
                </Button>
              </div>
            </template>
          </Table>
        </TabPane>
      </Tabs>
    </div>
  </Spin>
</template>
