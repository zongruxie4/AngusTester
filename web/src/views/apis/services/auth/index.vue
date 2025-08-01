<script setup lang="ts">
import { defineAsyncComponent, onMounted, ref } from 'vue';
import { enumUtils } from '@xcan-angus/infra';
import { TabPane, Tabs } from 'ant-design-vue';
import { Hints } from '@xcan-angus/vue-ui';

const GroupSet = defineAsyncComponent(() => import('@/views/apis/services/auth/groupSet/index.vue'));
const AuthSet = defineAsyncComponent(() => import('@/views/apis/services/auth/authSet/index.vue'));

interface Props {
  appId: string;
}

const props = withDefaults(defineProps<Props>(), {
  appId: undefined
});

const activeKey = ref<'user' | 'dept' | 'group'>('user');
const checkedUserId = ref<string>();
const checkedGroupId = ref<string>();
const checkedDeptId = ref<string>();

const apisPermissions = ref<{ value: string, description: string }[]>([]);
const servicesPermissions = ref<{ value: string, description: string }[]>([]);
const loaded = ref(false);

const loadEnums = () => {
  const d1 = enumUtils.enumToMessages('ApiPermission');
  apisPermissions.value = d1 || [];
  const d2 = enumUtils.enumToMessages('ServicesPermission');
  servicesPermissions.value = d2 || [];
};

onMounted(() => {
  loadEnums();
});
</script>

<template>
  <div class="h-full py-3.5">
    <Hints class="mx-3" text="授权给指定的“项目、服务、接口”操作权限给用户、部门、组，授予权限时”查看”权限是必须的。用户只能授权自己有授权权限的项目/服务/接口。" />
    <Tabs
      v-model:activeKey="activeKey"
      size="small"
      style="height: calc(100% - 18px);">
      <TabPane key="user" tab="用户">
        <GroupSet
          key="user"
          v-model:checkedId="checkedUserId"
          v-model:loaded="loaded"
          :appId="props.appId"
          type="user"
          class="flex-shrink-0 flex-grow-0 w-75 mr-4" />
        <AuthSet
          v-if="loaded"
          key="user"
          :authObjectId="checkedUserId"
          :apiPermissions="apisPermissions"
          :projectPermissions="servicesPermissions"
          type="USER"
          class="flex-1" />
      </TabPane>
      <TabPane key="dept" tab="部门">
        <GroupSet
          key="dept"
          v-model:checkedId="checkedDeptId"
          v-model:loaded="loaded"
          :appId="props.appId"
          type="dept"
          class="flex-shrink-0 flex-grow-0 w-75 mr-4" />
        <AuthSet
          v-if="loaded"
          key="dept"
          :authObjectId="checkedDeptId"
          type="DEPT"
          :apiPermissions="apisPermissions"
          :projectPermissions="servicesPermissions"
          class="flex-1" />
      </TabPane>
      <TabPane key="group" tab="组">
        <GroupSet
          key="group"
          v-model:checkedId="checkedGroupId"
          v-model:loaded="loaded"
          :appId="props.appId"
          type="group"
          class="flex-shrink-0 flex-grow-0 w-75 mr-4" />
        <AuthSet
          v-if="loaded"
          key="group"
          :authObjectId="checkedGroupId"
          type="GROUP"
          :apiPermissions="apisPermissions"
          :projectPermissions="servicesPermissions"
          class="flex-1" />
      </TabPane>
    </Tabs>
  </div>
</template>
<style scoped>
.ant-tabs.ant-tabs-top {
  margin-top: 0;
  margin-right: 12px;
  margin-left: 12px;
}

.ant-tabs :deep(.ant-tabs-nav) {
  margin: 0;
}

.ant-tabs :deep(.ant-tabs-tabpane) {
  display: flex;
  padding: 8px 0;
}

.ant-tabs.ant-tabs-top :deep(.ant-tabs-nav-wrap) {
  margin-right: 20px;
  margin-left: 20px;
}
</style>
