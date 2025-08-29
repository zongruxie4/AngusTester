<script setup lang="ts">
import { computed, defineAsyncComponent, inject, reactive, ref, watch } from 'vue';
import { AsyncComponent, Hints, Icon, Image, Input, Scroll, Table } from '@xcan-angus/vue-ui';
import { Button } from 'ant-design-vue';

import { useI18n } from 'vue-i18n';
import { debounce } from 'throttle-debounce';
import { appContext, duration, GM } from '@xcan-angus/infra';

import { auth } from 'src/api/gm';
import { Params, Policy, TableData } from './interface';

interface Props {
  activeKey: 'USER' | 'DEPT' | 'GROUP' ;
  appId: string;
}

const props = withDefaults(defineProps<Props>(), {
  activeKey: 'USER',
  appId: ''
});

const appInfo = ref(appContext.getAccessApp());
const isAdmin = inject('isAdmin', ref(false));
const { t } = useI18n();
const AddMembers = defineAsyncComponent(() => import('./addMembers.vue'));
const PolicyModal = defineAsyncComponent(() => import('@/views/config/appInfo/policyModal/index.vue'));

const tableList = ref<TableData[]>([]);
const reloadNo = ref(0); // 提示刷新 memberlist
const selectId = ref(''); // 选中的用户、部门、组
const policyData = ref<Policy[]>([]); // 策略数据 list
const addVisible = ref(false);
const authVisible = ref(false);
const policyKeyword = ref();
const policyLoading = ref(false);
const btnName = computed(() => {
  switch (props.activeKey) {
    case 'USER': return t('appConfig.scrollMembers.buttons.addMember');
    case 'DEPT': return t('appConfig.scrollMembers.buttons.addDept');
    case 'GROUP': return t('appConfig.scrollMembers.buttons.addGroup');
  }
  return '';
});

const params: Params = reactive({
  filters: []
});
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0
});

const handleInputChange = debounce(duration.search, (event: ChangeEvent) => {
  const value = event.target.value;
  if (value) {
    params.filters[0] = { key: props.activeKey === 'USER' ? 'fullName' : 'name', op: 'MATCH_END', value: value.trim() };
  } else {
    params.filters = [];
  }
});

const openAdd = () => {
  addVisible.value = true;
};

const handleAuthorize = () => {
  authVisible.value = true;
};

const updateList = (value: boolean) => {
  if (value) {
    reloadNo.value++;
  }
  addVisible.value = false;
};

// 用户 部门 组  直接授权
const policySave = async (addIds:string[]) => {
  if (!addIds.length) {
    return;
  }
  if (props.activeKey === 'USER') {
    await addUserPolicy(addIds);
    return;
  }

  if (props.activeKey === 'DEPT') {
    await addDeptPolicy(addIds);
    return;
  }

  await addGroupPolicy(addIds);
};

// 用户添加策略
const addUserPolicy = async (_addIds:string[]) => {
  if (policyLoading.value) {
    return;
  }
  policyLoading.value = true;
  await auth.createUserPolicy(selectId.value, _addIds);
  policyLoading.value = false;
  loadPolicys();
};

// 部门添加策略
const addDeptPolicy = async (_addIds:string[]) => {
  if (policyLoading.value) {
    return;
  }
  policyLoading.value = true;
  await auth.addPolicyByDept(selectId.value, _addIds);
  policyLoading.value = false;
  loadPolicys();
};

// 组策略
const addGroupPolicy = async (_addIds:string[]) => {
  if (policyLoading.value) {
    return;
  }
  policyLoading.value = true;
  await auth.createGroupPolicy(selectId.value, _addIds);
  policyLoading.value = false;
  loadPolicys();
};

const placeholder = computed(() => {
  if (props.activeKey === 'USER') {
    return t('appConfig.scrollMembers.placeholders.searchMember');
  }
  if (props.activeKey === 'DEPT') {
    return t('appConfig.scrollMembers.placeholders.searchDept');
  }
  return t('appConfig.scrollMembers.placeholders.searchGroup');
});

const columns = computed(() => {
  return [
    {
      title: t('appConfig.scrollMembers.table.columns.policyId'),
      dataIndex: 'id',
      width: '10%'
    },
    {
      title: t('appConfig.scrollMembers.table.columns.policyName'),
      dataIndex: 'name',
      ellipsis: true,
      width: '12%'
    },
    {
      title: t('appConfig.scrollMembers.table.columns.policyCode'),
      dataIndex: 'code',
      width: '18%',
      ellipsis: true
    },
    {
      title: t('appConfig.scrollMembers.table.columns.policyDescription'),
      dataIndex: 'description',
      ellipsis: true
    },
    props.activeKey === 'USER' && {
      title: t('appConfig.scrollMembers.table.columns.policySource'),
      dataIndex: 'source',
      width: '10%'
    },
    {
      title: t('appConfig.scrollMembers.table.columns.joinTime'),
      dataIndex: 'createdDate',
      width: 140
    },
    {
      title: t('appConfig.scrollMembers.table.columns.action'),
      dataIndex: 'action',
      align: 'center',
      width: 90
    }
  ].filter(Boolean);
});
const policyModalType = computed(() => {
  if (props.activeKey === 'USER') {
    return 'User';
  }

  if (props.activeKey === 'DEPT') {
    return 'Dept';
  }

  return 'Group';
});

const urlPath = computed(() => {
  switch (props.activeKey) {
    case 'USER': return `${GM}/app/${appInfo.value.id}/auth/user`;
    case 'DEPT': return `${GM}/app/${appInfo.value.id}/auth/dept`;
    case 'GROUP': return `${GM}/app/${appInfo.value.id}/auth/group`;
  }
  return '';
});

const handleChange = val => {
  tableList.value = val;
  if (!selectId.value && val.length) {
    selectId.value = tableList.value[0].id;
  }
};

const selectMember = (id: string) => {
  selectId.value = id;
};

const getTenantTypeName = (record) => {
  const result: string[] = [];
  if (record?.currentDefault) {
    result.push(t('appConfig.scrollMembers.table.sources.appDefault'));
  }
  if (record?.openAuth) {
    result.push(t('appConfig.scrollMembers.table.sources.openAuth'));
  }
  return result.join(',');
};

// 取消授权API
const deletePolicyApiConfig = {
  USER: auth.deleteUserPolicy,
  DEPT: auth.delPolicyByDept,
  GROUP: auth.deleteGroupPolicy
};

// 取消授权
const handleCancel = async (policyIds) => {
  if (policyLoading.value) {
    return;
  }
  policyLoading.value = true;
  await deletePolicyApiConfig[props.activeKey](selectId.value, policyIds);
  policyLoading.value = false;
  if (policyData.value.length === 1) {
    selectId.value = '';
    reloadNo.value++;
    return;
  }
  loadPolicys();
};

// 添加授权策略API
const policyApiConfig = {
  USER: auth.getUserPolicyList,
  GROUP: auth.getGroupPolicy,
  DEPT: auth.getDeptPolicy
};

// 策略列表
const loadPolicys = async () => {
  if (policyLoading.value) {
    return;
  }
  policyLoading.value = true;
  const { current, pageSize } = pagination;
  const filters = policyKeyword.value ? [{ key: 'name', value: policyKeyword.value, op: 'MATCH_END' }] : [];
  const [error, res] = await policyApiConfig[props.activeKey](selectId.value, { appId: appInfo.value.id, pageNo: current, pageSize, filters });
  policyLoading.value = false;
  if (error) {
    return;
  }
  policyData.value = res.data.list || [];
  pagination.total = +res.data.total;
};

watch(() => selectId.value, newValue => {
  policyData.value = [];
  if (newValue) {
    loadPolicys();
  }
});

watch(() => policyKeyword.value, debounce(duration.search, () => {
  pagination.current = 1;
  loadPolicys();
}));
</script>
<template>
  <div class="flex py-2">
    <div class="border w-70">
      <div class="flex justify-between my-2 px-2">
        <Input
          :maxlength="50"
          :placeholder="placeholder"
          allowClear
          @change="handleInputChange">
          <template #suffix>
            <Icon icon="icon-sousuo" class="text-4 text-theme-sub-content" />
          </template>
        </Input>
        <Button
          type="primary"
          size="small"
          class="flex items-center ml-2"
          :disabled="!isAdmin"
          @click="openAdd()">
          <Icon icon="icon-jia" class="mr-1" />{{ btnName }}
        </Button>
      </div>
      <Scroll
        :action="urlPath"
        :lineHeight="32"
        :notify="reloadNo"
        :params="params"
        class="w-70 h-100 text-3"
        @change="handleChange">
        <div
          v-for="obj in tableList"
          :key="obj.id"
          class="leading-9 cursor-pointer px-3 truncate"
          :class="{'bg-gray-bg': obj.id === selectId}"
          :title="obj.name || obj.fullName"
          @click="selectMember(obj.id)">
          <template v-if="activeKey === 'USER'">
            <Image
              type="avatar"
              :src="obj.avatar"
              class="w-5 h-5 rounded-full inline-block -mt-1 mr-2" />
          </template>
          <template v-else-if="activeKey === 'DEPT'">
            <Icon icon="icon-bumen" class="text-3.5 leading-3.5 -mt-0.5 flex-shrink-0" />
          </template>
          <template v-else>
            <Icon icon="icon-zu" class="text-3.5 leading-3.5 -mt-0.5 flex-shrink-0" />
          </template>
          {{ obj.name || obj.fullName }}
        </div>
      </Scroll>
    </div>
    <div class="flex-1 pl-2">
      <div v-show="!!selectId" class="flex items-center justify-between">
        <Input
          v-model:value="policyKeyword"
          :allowClear="true"
          :placeholder="t('appConfig.scrollMembers.placeholders.searchPolicy')"
          class="w-70" />
        <div class="flex-1 min-w-0 truncate px-2">
          <Hints :text="t('appConfig.scrollMembers.hints')" />
        </div>
        <Button
          :disabled="!isAdmin"
          type="primary"
          size="small"
          class="flex items-center"
          @click="handleAuthorize">
          <Icon icon="icon-jia" class="mr-1" />
          {{ t('appConfig.scrollMembers.buttons.authorizePolicy') }}
        </Button>
      </div>
      <Table
        :columns="columns"
        :dataSource="policyData"
        :loading="policyLoading"
        :pagination="pagination"
        class="mt-2">
        <template #bodyCell="{record, column}">
          <template v-if="column.dataIndex === 'action'">
            <Button
              type="text"
              size="small"
              class="flex items-center"
              :disabled="props.activeKey === 'USER' && record.orgType.value !== 'USER' || !isAdmin"
              @click="handleCancel(record.id)">
              <Icon icon="icon-zhongzhi2" class="mr-1" />{{ t('appConfig.scrollMembers.table.actions.cancelAuth') }}
            </Button>
          </template>
          <template v-if="column.dataIndex === 'source'">
            {{ record.orgType.value !== 'TENANT' ? record.orgType.message : getTenantTypeName(record) }}
          </template>
        </template>
      </Table>
    </div>
    <AsyncComponent :visible="addVisible">
      <AddMembers
        v-if="addVisible"
        :visible="addVisible"
        :appId="appInfo.id"
        :type="props.activeKey"
        @update="updateList" />
    </AsyncComponent>
    <AsyncComponent :visible="authVisible">
      <PolicyModal
        v-if="authVisible"
        v-model:visible="authVisible"
        :userId="selectId"
        :groupId="selectId"
        :deptId="selectId"
        :appId="appInfo.id"
        :type="policyModalType"
        @change="policySave" />
    </AsyncComponent>
  </div>
</template>
<style scoped>
:deep(.ant-tabs-tab-btn) {
  width: 100%;
  text-align: center;
}

:deep(.ant-tabs-nav) {
  margin: 0;
}

:deep(.ant-table.ant-table-bordered > .ant-table-container > .ant-table-content > table) {
  border-top: 0;
}
</style>
