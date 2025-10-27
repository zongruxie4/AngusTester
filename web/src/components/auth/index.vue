<script setup lang="ts">
// Vue core imports
import { ref, computed, onMounted } from 'vue';
import { useI18n } from 'vue-i18n';

// UI component imports
import { Button, Radio, RadioGroup, TabPane, Tabs, CheckboxGroup, Checkbox } from 'ant-design-vue';
import { Icon, Hints, Spin, Select, Table } from '@xcan-angus/vue-ui';

// Infrastructure imports
import { duration, GM, http, enumUtils } from '@xcan-angus/infra';

const { t } = useI18n();

/**
 * Tab key type for authorization tabs
 */
type TabKey = 'USER' | 'DEPT' | 'GROUP'

/**
 * Component props interface for authorization
 */
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
  emptyTextStyle?: { [key: string]: string }; // No data style
  noDataSize: 'large' | 'middle' | 'small';
}

// Component props with defaults
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

// Component events
const emits = defineEmits<{(e: 'change', value: { auth: boolean; }): void; }>();

// Component state
const isPermissionControlled = ref(false);
const activeTabKey = ref<TabKey>('USER');

// Table column configurations
const userTableColumns = [
  {
    key: 'name',
    title: t('common.username'),
    dataIndex: 'name',
    width: 200,
    ellipsis: true
  },
  {
    key: 'auth',
    title: t('actions.permission'),
    dataIndex: 'auth'
  },
  {
    key: 'action',
    title: t('common.actions'),
    dataIndex: 'action',
    width: 100
  }
];

const departmentTableColumns = [
  {
    key: 'name',
    title: t('xcan_authorize.departmentName'),
    dataIndex: 'name',
    width: 200
  },
  {
    key: 'auth',
    title: t('actions.permission'),
    dataIndex: 'auth'
  },
  {
    key: 'action',
    title: t('common.actions'),
    dataIndex: 'action',
    width: 100
  }
];

const groupTableColumns = [
  {
    key: 'name',
    title: t('xcan_authorize.groupName'),
    dataIndex: 'name',
    width: 200
  },
  {
    key: 'auth',
    title: t('actions.permission'),
    dataIndex: 'auth'
  },
  {
    key: 'action',
    title: t('common.actions'),
    dataIndex: 'action',
    width: 100
  }
];

// Loading state
const isLoading = ref(false);

// User authorization state
const userPaginationParams = ref({ pageNo: 1, pageSize: 10 });
const userAuthorizationList = ref([]);
const userTableTotalCount = ref(0);
const userAuthorizationMap = ref<{ [key: string]: string[] }>({});
// Department authorization state
const departmentPaginationParams = ref({ pageNo: 1, pageSize: 10 });
const departmentAuthorizationList = ref([]);
const departmentTableTotalCount = ref(0);
const departmentAuthorizationMap = ref<{ [key: string]: string[] }>({});

// Group authorization state
const groupPaginationParams = ref({ pageNo: 1, pageSize: 10 });
const groupAuthorizationList = ref([]);
const groupTableTotalCount = ref(0);
const groupAuthorizationMap = ref<{ [key: string]: string[] }>({});

// Authorization options
const authorizationOptions = ref<any[]>([]);

// Selected IDs for adding authorization
const selectedUserId = ref<string>();
const selectedDepartmentId = ref<string>();
const selectedGroupId = ref<string>();

/**
 * Load user authorization list
 */
const loadUserAuthorizationList = async () => {
  const requestParams = { ...userPaginationParams.value, authObjectType: activeTabKey.value };
  isLoading.value = true;
  const [error, { data = { list: [], total: 0 } }] = await http.get(props.listUrl, requestParams);
  isLoading.value = false;
  if (error) {
    return;
  }

  userAuthorizationList.value = data.list.map(item => {
    userAuthorizationMap.value[item.id] = item.permissions?.map(item => item.value) || [];
    return { ...item, checkedAll: item.permissions?.length === authorizationOptions.value.length };
  });

  userTableTotalCount.value = data.total;
};

/**
 * Load department authorization list
 */
const loadDepartmentAuthorizationList = async () => {
  const requestParams = { ...departmentPaginationParams.value, authObjectType: activeTabKey.value };
  isLoading.value = true;
  const [error, { data = { list: [], total: 0 } }] = await http.get(props.listUrl, requestParams);
  isLoading.value = false;
  if (error) {
    return;
  }

  departmentAuthorizationList.value = data.list.map(item => {
    departmentAuthorizationMap.value[item.id] = item.permissions?.map(item => item.value) || [];
    return { ...item, checkedAll: item.permissions?.length === authorizationOptions.value.length };
  });
  departmentTableTotalCount.value = data.total;
};

/**
 * Load group authorization list
 */
const loadGroupAuthorizationList = async () => {
  const requestParams = { ...groupPaginationParams.value, authObjectType: activeTabKey.value };
  isLoading.value = true;
  const [error, { data = { list: [], total: 0 } }] = await http.get(props.listUrl, requestParams);
  isLoading.value = false;
  if (error) {
    return;
  }

  groupAuthorizationList.value = data.list.map(item => {
    groupAuthorizationMap.value[item.id] = item.permissions?.map(item => item.value) || [];
    return { ...item, checkedAll: item.permissions?.length === authorizationOptions.value?.length };
  });
  groupTableTotalCount.value = data.total;
};

/**
 * Handle tab change event
 */
const handleTabChange = (key: any) => {
  switch (key) {
    case 'USER':
      loadUserAuthorizationList();
      break;
    case 'DEPT':
      loadDepartmentAuthorizationList();
      break;
    case 'GROUP':
      loadGroupAuthorizationList();
      break;
  }
};

/**
 * Handle authorization change event
 */
const handleAuthorizationChange = async (values: string[], record: any) => {
  const previousAuthList = record.permissions.map(item => item.value);
  if (values.includes('RELEASE') && !previousAuthList.includes('MODIFY')) {
    values.push('MODIFY');
  }
  if (!values.includes('MODIFY') && values.includes('RELEASE')) {
    values = values.filter(item => item !== 'RELEASE');
  }
  const requestParams = {
    authObjectId: record.id,
    permissions: values
  };
  isLoading.value = true;
  const [error] = await http.put(`${props.updateUrl}/${record.id}`, requestParams);
  isLoading.value = false;
  if (error) {
    return;
  }
  const selectedAuthOptions = authorizationOptions.value.filter(item => values.includes(item.value));
  record.permissions = selectedAuthOptions;
  if (values.length === authorizationOptions.value.length) {
    record.checkedAll = true;
  } else {
    record.checkedAll = false;
  }
  switch (activeTabKey.value) {
    case 'USER':
      userAuthorizationMap.value[record.id] = values;
      break;
    case 'DEPT':
      departmentAuthorizationMap.value[record.id] = values;
      break;
    case 'GROUP':
      groupAuthorizationMap.value[record.id] = values;
      break;
  }
};

/**
 * Handle check all change event
 */
const handleCheckAllChange = async (checked: boolean, record: any) => {
  const requestParams = {
    authObjectId: record.id,
    permissions: checked ? authorizationOptions.value.map(item => item.value) : ['VIEW']
  };
  isLoading.value = true;
  const [error] = await http.put(`${props.updateUrl}/${record.id}`, requestParams);
  isLoading.value = false;
  if (error) {
    return;
  }

  if (checked) {
    record.permissions = authorizationOptions.value;
  } else {
    const viewAuthOption = authorizationOptions.value.filter(item => item.value === 'VIEW');
    record.permissions = viewAuthOption;
  }

  const authValues = record.permissions?.map(item => item.value);

  switch (activeTabKey.value) {
    case 'USER':
      userAuthorizationMap.value[record.id] = authValues;
      break;
    case 'DEPT':
      departmentAuthorizationMap.value[record.id] = authValues;
      break;
    case 'GROUP':
      groupAuthorizationMap.value[record.id] = authValues;
      break;
  }
};

/**
 * Load authorization enums
 */
const loadAuthorizationEnums = async () => {
  if (authorizationOptions.value.length > 0) {
    return authorizationOptions.value;
  }
  const enumData = enumUtils.enumToMessages(props.enumKey);
  authorizationOptions.value = (enumData || [])?.map(item => {
    if (item.value === 'VIEW') {
      return { label: item.message, value: item.value, disabled: true };
    }
    return { label: item.message, value: item.value };
  }) || [];
};

/**
 * Delete authorization
 */
const handleDeleteAuthorization = async (id: string) => {
  isLoading.value = true;
  const [error] = await http.del(`${props.delUrl}/${id}`);
  isLoading.value = false;
  if (error) {
    return;
  }
  switch (activeTabKey.value) {
    case 'USER':
      userPaginationParams.value.pageNo = calculateCurrentPage(userPaginationParams.value.pageNo, userPaginationParams.value.pageSize, userTableTotalCount.value);
      loadUserAuthorizationList();
      break;
    case 'DEPT':
      departmentPaginationParams.value.pageNo = calculateCurrentPage(departmentPaginationParams.value.pageNo, departmentPaginationParams.value.pageSize, departmentTableTotalCount.value);
      loadDepartmentAuthorizationList();
      break;
    case 'GROUP':
      groupPaginationParams.value.pageNo = calculateCurrentPage(groupPaginationParams.value.pageNo, groupPaginationParams.value.pageSize, groupTableTotalCount.value);
      loadGroupAuthorizationList();
      break;
  }
};

/**
 * Calculate current page after deletion
 */
const calculateCurrentPage = (pageNo: number, pageSize: number, total: number): number => {
  if (pageNo === 1 || pageSize >= total) {
    return pageNo;
  }
  const lastPageDataCount = total % pageSize;
  if (lastPageDataCount === 1) {
    return pageNo - 1;
  }

  return pageNo;
};

/**
 * Handle table pagination change
 */
const handleTablePaginationChange = (pagination: any) => {
  const { current, pageSize } = pagination;
  switch (activeTabKey.value) {
    case 'USER':
      userPaginationParams.value.pageNo = current;
      userPaginationParams.value.pageSize = pageSize;
      loadUserAuthorizationList();
      break;
    case 'DEPT':
      departmentPaginationParams.value.pageNo = current;
      departmentPaginationParams.value.pageSize = pageSize;
      loadDepartmentAuthorizationList();
      break;
    case 'GROUP':
      groupPaginationParams.value.pageNo = current;
      groupPaginationParams.value.pageSize = pageSize;
      loadGroupAuthorizationList();
      break;
  }
};

/**
 * Add authorization
 */
const handleAddAuthorization = async () => {
  const requestParams = {
    authObjectType: activeTabKey.value,
    authObjectId: activeTabKey.value === 'USER' ? selectedUserId.value : activeTabKey.value === 'DEPT' ? selectedDepartmentId.value : selectedGroupId.value,
    permissions: ['VIEW']
  };
  isLoading.value = true;
  const [error] = await http.post(props.addUrl, requestParams);
  isLoading.value = false;
  if (error) {
    return;
  }

  loadAuthorizationList();
};

/**
 * Handle enable/disable authorization control
 */
const handleEnableChange = async (auth: boolean) => {
  isLoading.value = true;
  const [error] = await http.patch(`${props.enabledUrl}?enabled=${auth}`);
  isLoading.value = false;
  if (error) {
    return;
  }
  isPermissionControlled.value = auth;

  if (auth) {
    if (!authorizationOptions.value?.length) {
      await loadAuthorizationEnums();
    }

    activeTabKey.value = 'USER';
    loadAuthorizationList();
  }

  emits('change', { auth });
};

/**
 * Load authorization list based on active tab
 */
const loadAuthorizationList = () => {
  switch (activeTabKey.value) {
    case 'USER':
      userPaginationParams.value.pageNo = 1;
      loadUserAuthorizationList();
      selectedUserId.value = undefined;
      break;
    case 'DEPT':
      departmentPaginationParams.value.pageNo = 1;
      loadDepartmentAuthorizationList();
      selectedDepartmentId.value = undefined;
      break;
    case 'GROUP':
      groupPaginationParams.value.pageNo = 1;
      loadGroupAuthorizationList();
      selectedGroupId.value = undefined;
      break;
  }
};

// Computed properties
const paginationConfig = computed(() => {
  switch (activeTabKey.value) {
    case 'USER':
      return {
        current: userPaginationParams.value.pageNo,
        pageSize: userPaginationParams.value.pageSize,
        total: userTableTotalCount.value
      };
    case 'GROUP':
      return {
        current: groupPaginationParams.value.pageNo,
        pageSize: groupPaginationParams.value.pageSize,
        total: groupTableTotalCount.value
      };
    case 'DEPT':
      return {
        current: departmentPaginationParams.value.pageNo,
        pageSize: departmentPaginationParams.value.pageSize,
        total: departmentTableTotalCount.value
      };
    default: return {
      current: userPaginationParams.value.pageNo,
      pageSize: userPaginationParams.value.pageSize,
      total: userTableTotalCount.value
    };
  }
});

/**
 * Initialize authorization status
 */
const initializeAuthorizationStatus = async () => {
  isLoading.value = true;
  const [error, { data }] = await http.get(`${props.initStatusUrl}`);
  isLoading.value = false;
  if (error) {
    return;
  }

  if (typeof data === 'boolean' && data) {
    if (!authorizationOptions.value?.length) {
      await loadAuthorizationEnums();
    }
    isPermissionControlled.value = data;
    activeTabKey.value = 'USER';
    loadAuthorizationList();
  }
};

// Component initialization
onMounted(() => {
  loadAuthorizationEnums()
  if (props.initStatusUrl) {
    initializeAuthorizationStatus();
  }
});
</script>
<template>
  <Spin
    :spinning="isLoading"
    class="p-2"
    :delay="duration.delay">
    <div>
      <RadioGroup
        :value="isPermissionControlled"
        :disabled="props.disabled"
        @change="(e) => handleEnableChange(e.target.value)">
        <Radio :value="false">{{ t('xcan_authorize.noPermissionControl') }}</Radio>
        <Radio :value="true">{{ t('xcan_authorize.hasPermissionControl') }}</Radio>
      </RadioGroup>
    </div>
    <Hints class="mt-2 mb-2" :text="isPermissionControlled ? props.onTips : props.offTips" />
    <div v-show="isPermissionControlled">
      <Tabs
        v-model:activeKey="activeTabKey"
        size="small"
        @change="handleTabChange">
        <TabPane key="USER" :tab="t('organization.user')">
          <div class="flex justify-between -mt-1">
            <Select
              v-model:value="selectedUserId"
              :action="`${GM}/app/${props.appId}/auth/user`"
              :fieldNames="{ label: 'fullName', value: 'id' }"
              :placeholder="t('organization.placeholders.selectUser')"
              showSearch
              allowClear
              class="w-full mr-5"
              style="max-width: 400px;" />
            <Button
              :disabled="!selectedUserId"
              class="flex items-center"
              size="small"
              type="primary"
              @click="handleAddAuthorization">
              <template #icon>
                <Icon icon="icon-jia" class="mr-1" />
              </template>
              {{ t('actions.add') }}
            </Button>
          </div>
          <Table
            class="mt-2 text-3 leading-3"
            size="small"
            :columns="userTableColumns"
            :dataSource="userAuthorizationList"
            :pagination="paginationConfig"
            :emptyTextStyle="props.emptyTextStyle"
            :noDataSize="props.noDataSize"
            :noDataText="t('common.noData')"
            rowKey="id"
            @change="handleTablePaginationChange">
            <template #bodyCell="{ column, record }">
              <div v-if="column.dataIndex === 'auth'">
                <CheckboxGroup
                  v-model:value="userAuthorizationMap[record.id]"
                  :options="authorizationOptions"
                  :disabled="record.creator"
                  @change="(values) => handleAuthorizationChange(values as string[], record)">
                </CheckboxGroup>
              </div>
              <div v-if="column.dataIndex === 'action'" class="flex items-center whitespace-nowrap">
                <Checkbox
                  v-model:checked="record.checkedAll"
                  :disabled="record.creator"
                  @change="(e) => handleCheckAllChange(e.target.checked, record)">
                  {{ t('common.all') }}
                </Checkbox>
                <Button
                  type="link"
                  size="small"
                  class="px-2"
                  :disabled="record.creator"
                  @click="handleDeleteAuthorization(record.id)">
                  <Icon icon="icon-qingchu" class="text-4" />
                </Button>
              </div>
            </template>
          </Table>
        </TabPane>
        <TabPane key="DEPT" :tab="t('organization.dept')">
          <div class="flex justify-between -mt-1">
            <Select
              v-model:value="selectedDepartmentId"
              :action="`${GM}/app/${props.appId}/auth/dept`"
              :fieldNames="{ label: 'name', value: 'id' }"
              :placeholder="t('organization.placeholders.selectDept')"
              showSearch
              allowClear
              class="w-full mr-5"
              style="max-width: 400px;" />
            <Button
              class="flex items-center"
              size="small"
              type="primary"
              :disabled="!selectedDepartmentId"
              @click="handleAddAuthorization">
              <template #icon>
                <Icon icon="icon-jia" class="mr-1" />
              </template>
              {{ t('actions.add') }}
            </Button>
          </div>
          <Table
            class="mt-2 text-3 leading-3"
            size="small"
            :columns="departmentTableColumns"
            :dataSource="departmentAuthorizationList"
            :pagination="paginationConfig"
            :emptyTextStyle="props.emptyTextStyle"
            :noDataSize="props.noDataSize"
            :noDataText="t('common.noData')"
            rowKey="id"
            @change="handleTablePaginationChange">
            <template #bodyCell="{ column, record }">
              <div v-if="column.dataIndex === 'auth'">
                <CheckboxGroup
                  v-model:value="departmentAuthorizationMap[record.id]"
                  :options="authorizationOptions"
                  :disabled="record.creator"
                  @change="(values) => handleAuthorizationChange(values as string[], record)">
                </CheckboxGroup>
              </div>
              <div v-if="column.dataIndex === 'action'" class="flex items-center whitespace-nowrap">
                <Checkbox
                  v-model:checked="record.checkedAll"
                  :disabled="record.creator"
                  @change="(e) => handleCheckAllChange(e.target.checked, record)">
                  {{ t('common.all') }}
                </Checkbox>
                <Button
                  type="link"
                  size="small"
                  class="px-2"
                  :disabled="record.creator"
                  @click="handleDeleteAuthorization(record.id)">
                  <Icon icon="icon-qingchu" class="text-4" />
                </Button>
              </div>
            </template>
          </Table>
        </TabPane>
        <TabPane key="GROUP" :tab="t('organization.group')">
          <div class="flex justify-between -mt-1">
            <Select
              v-model:value="selectedGroupId"
              :action="`${GM}/app/${props.appId}/auth/group`"
              :fieldNames="{ label: 'name', value: 'id' }"
              :placeholder="t('organization.placeholders.selectGroup')"
              showSearch
              allowClear
              class="w-full mr-5"
              style="max-width: 400px;" />
            <Button
              class="flex items-center"
              size="small"
              type="primary"
              :disabled="!selectedGroupId"
              @click="handleAddAuthorization">
              <template #icon>
                <Icon icon="icon-jia" class="mr-1" />
              </template>
              {{ t('actions.add') }}
            </Button>
          </div>
          <Table
            class="mt-2 text-3 leading-3"
            size="small"
            :columns="groupTableColumns"
            :dataSource="groupAuthorizationList"
            :pagination="paginationConfig"
            :emptyTextStyle="props.emptyTextStyle"
            :noDataSize="props.noDataSize"
            :noDataText="t('common.noData')"
            rowKey="id"
            @change="handleTablePaginationChange">
            <template #bodyCell="{ column, record }">
              <div v-if="column.dataIndex === 'auth'">
                <CheckboxGroup
                  v-model:value="groupAuthorizationMap[record.id]"
                  :options="authorizationOptions"
                  :disabled="record.creator"
                  @change="(values) => handleAuthorizationChange(values as string[], record)">
                </CheckboxGroup>
              </div>
              <div v-if="column.dataIndex === 'action'" class="flex items-center whitespace-nowrap">
                <Checkbox
                  v-model:checked="record.checkedAll"
                  :disabled="record.creator"
                  @change="(e) => handleCheckAllChange(e.target.checked, record)">
                  {{ t('common.all') }}
                </Checkbox>
                <Button
                  type="link"
                  size="small"
                  class="px-2"
                  :disabled="record.creator"
                  @click="handleDeleteAuthorization(record.id)">
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
