<script setup lang="ts">
import { defineAsyncComponent, onMounted, ref } from 'vue';
import { enumUtils } from '@xcan-angus/infra';
import { TabPane, Tabs } from 'ant-design-vue';
import { Hints } from '@xcan-angus/vue-ui';

const GroupSet = defineAsyncComponent(() => import('@/views/scenario/scenario/auth/groupSet/index.vue'));
const AuthSet = defineAsyncComponent(() => import('@/views/scenario/scenario/auth/authSet/index.vue'));

interface Props {
  projectId:string;
  appId: string;
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  appId: undefined
});

const activeKey = ref<'user'|'dept'|'group'>('user');
const checkedUserId = ref<string>();
const checkedGroupId = ref<string>();
const checkedDeptId = ref<string>();

const permissions = ref<{value:string, label:string}[]>([]);
const loaded = ref(false);

const loadEnums = () => {
  const res = enumUtils.enumToMessages('ScenarioPermission');
  if (res?.length) {
    permissions.value = res.map(item => ({ label: item.message, value: item.value }));
  }
};

onMounted(() => {
  loadEnums();
});

const text = '对指定的用户配置场景的数据操作权限。通过配置权限可以更加高效的在团队协作间完成调试、管理、测试等接口工作。';
</script>

<template>
  <div class="h-full px-5 py-5">
    <Hints :text="text" />
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
          :projectId="props.projectId"
          type="user"
          class="flex-shrink-0 flex-grow-0 w-75 mr-4" />
        <AuthSet
          v-if="loaded"
          key="user"
          type="user"
          :authObjectId="checkedUserId"
          :permissions="permissions"
          :projectId="props.projectId"
          class="flex-1" />
      </TabPane>
      <TabPane key="dept" tab="部门">
        <GroupSet
          key="dept"
          v-model:checkedId="checkedDeptId"
          v-model:loaded="loaded"
          :appId="props.appId"
          :projectId="props.projectId"
          type="dept"
          class="flex-shrink-0 flex-grow-0 w-75 mr-4" />
        <AuthSet
          v-if="loaded"
          key="dept"
          type="dept"
          :authObjectId="checkedDeptId"
          :permissions="permissions"
          :projectId="props.projectId"
          class="flex-1" />
      </TabPane>
      <TabPane key="group" tab="组">
        <GroupSet
          key="group"
          v-model:checkedId="checkedGroupId"
          v-model:loaded="loaded"
          :appId="props.appId"
          :projectId="props.projectId"
          type="group"
          class="flex-shrink-0 flex-grow-0 w-75 mr-4" />
        <AuthSet
          v-if="loaded"
          key="group"
          type="group"
          :authObjectId="checkedGroupId"
          :permissions="permissions"
          :projectId="props.projectId"
          class="flex-1" />
      </TabPane>
    </Tabs>
  </div>
</template>
<style scoped>
.ant-tabs.ant-tabs-top {
  margin-top: 0;
}

.ant-tabs :deep(.ant-tabs-nav) {
  margin: 0;
}

.ant-tabs :deep(.ant-tabs-tabpane) {
  display: flex;
  padding: 14px 0;
}
</style>
