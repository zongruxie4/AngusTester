<script setup lang="ts">
import { defineAsyncComponent, ref } from 'vue';
import { AsyncComponent, Hints, Icon, Image, Input, Scroll, Table } from '@xcan-angus/vue-ui';
import { Button } from 'ant-design-vue';

import { useI18n } from 'vue-i18n';
import { appContext, AuthObjectType } from '@xcan-angus/infra';
import { useMemberData } from './composables/useMemberData';
import { useTableColumns } from './composables/useTableColumns';
import { usePolicyManagement } from './composables/usePolicyManagement';

interface Props {
  activeKey: AuthObjectType;
  appId: string;
}

const props = withDefaults(defineProps<Props>(), {
  activeKey: AuthObjectType.USER,
  appId: ''
});

const isAdmin = appContext.isAdmin();
const { t } = useI18n();
const AddMembers = defineAsyncComponent(() => import('./AddMember.vue'));
const PolicyModal = defineAsyncComponent(() => import('@/views/config/app/PolicyModal.vue'));

// Use composables for data and logic management
const {
  // Reactive data
  tableList,
  reloadNo,
  selectId,
  policyData,
  policyKeyword,
  policyLoading,
  params,
  pagination,

  // Computed properties
  btnName,
  placeholder,
  urlPath,

  // Methods
  handleInputChange,
  handleChange,
  selectMember,
  getTenantTypeName,
  handleCancel,
  loadPolicies
} = useMemberData(props.activeKey, props.appId);

// Use composable for table column configuration
const { columns } = useTableColumns(props.activeKey);

// Reactive state for modals
const addVisible = ref(false);
const authVisible = ref(false);

/**
 * Open the add member modal
 */
const openAdd = () => {
  addVisible.value = true;
};

/**
 * Open the policy authorization modal
 */
const handleAuthorize = () => {
  authVisible.value = true;
};

/**
 * Update the member list after adding members
 * @param value - Whether to refresh the list
 */
const updateList = (value: boolean) => {
  if (value) {
    reloadNo.value++;
  }
  addVisible.value = false;
};

// Use composable for policy management
const {
  policySave
} = usePolicyManagement(
  props.activeKey,
  props.appId,
  selectId, // Pass the selectId ref directly
  props.activeKey === AuthObjectType.USER ? selectId : undefined, // userId
  props.activeKey === AuthObjectType.DEPT ? selectId : undefined, // deptId
  props.activeKey === AuthObjectType.GROUP ? selectId : undefined // groupId
);
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
          <template v-if="activeKey === AuthObjectType.USER">
            <Image
              type="avatar"
              :src="obj.avatar"
              class="w-5 h-5 rounded-full inline-block -mt-1 mr-2" />
          </template>
          <template v-else-if="activeKey === AuthObjectType.DEPT">
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
      <div v-show="!!selectId" class="flex flex-col -mt-7">
        <div class="flex justify-between items-center mb-2">
          <div class="flex-1 min-w-0 truncate">
            <Hints :text="t('app.config.members.list.hints')" />
          </div>
        </div>
        <div class="flex justify-between items-center gap-2">
          <div style="width: 390px;">
            <Input
              v-model:value="policyKeyword"
              :allowClear="true"
              :placeholder="t('organization.placeholders.searchPolicy')" />
          </div>
          <Button
            :disabled="!isAdmin"
            type="primary"
            size="small"
            class="flex items-center flex-none"
            @click="handleAuthorize">
            <Icon icon="icon-jia" class="mr-1" />
            {{ t('app.config.members.actions.authorizePolicy') }}
          </Button>
        </div>
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
              :disabled="props.activeKey === AuthObjectType.USER && record.orgType.value !== AuthObjectType.USER || !isAdmin"
              @click="handleCancel(record.id)">
              <Icon icon="icon-zhongzhi2" class="mr-1" />{{ t('app.config.members.actions.cancelAuth') }}
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
        :appId="props.appId"
        :type="props.activeKey"
        @update="updateList" />
    </AsyncComponent>
    <AsyncComponent :visible="authVisible">
      <PolicyModal
        v-if="authVisible && selectId"
        v-model:visible="authVisible"
        :userId="props.activeKey === AuthObjectType.USER ? selectId : undefined"
        :groupId="props.activeKey === AuthObjectType.GROUP ? selectId : undefined"
        :deptId="props.activeKey === AuthObjectType.DEPT ? selectId : undefined"
        :appId="props.appId"
        :type="props.activeKey"
        @change="(addIds) => policySave(addIds, loadPolicies)" />
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
