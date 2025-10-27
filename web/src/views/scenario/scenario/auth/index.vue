<script setup lang="ts">
import { defineAsyncComponent, onMounted, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { enumUtils, AuthObjectType as AuthObjectTypeEnum } from '@xcan-angus/infra';
import { ScenarioPermission } from '@/enums/enums';
import { TabPane, Tabs } from 'ant-design-vue';
import { Hints } from '@xcan-angus/vue-ui';

import type { AuthObjectType, Permission } from './types';

const { t } = useI18n();

// Lazy load components for better performance
const GroupSet = defineAsyncComponent(() => import('@/views/scenario/scenario/auth/GroupSet.vue'));
const AuthSet = defineAsyncComponent(() => import('@/views/scenario/scenario/auth/AuthSet.vue'));

// Component props interface
interface Props {
  projectId: string;
  appId: string;
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  appId: undefined
});

// Reactive state
const activeKey = ref<AuthObjectType>('user');
const checkedUserId = ref<string>();
const checkedGroupId = ref<string>();
const checkedDeptId = ref<string>();
const permissions = ref<Permission[]>([]);
const loaded = ref(false);

/**
 * Load permission enums and convert to permission array
 */
const loadPermissionEnums = () => {
  const enumMessages = enumUtils.enumToMessages(ScenarioPermission);
  permissions.value = enumMessages.map(item => ({
    label: item.message,
    value: item.value
  }));
};

// Lifecycle hooks
onMounted(() => {
  loadPermissionEnums();
});

// Computed properties
const descriptionText = t('scenario.permission.description');
</script>
<!-- TODO 调试权限功能、页面不展示、功能未验证 -->
<template>
  <div class="h-full px-5 py-5">
    <Hints :text="descriptionText" />
    <Tabs
      v-model:activeKey="activeKey"
      size="small"
      style="height: calc(100% - 18px);">
      <TabPane key="user" :tab="t('organization.user')">
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
          :type="AuthObjectTypeEnum.USER"
          :authObjectId="checkedUserId"
          :permissions="permissions"
          :projectId="props.projectId"
          class="flex-1" />
      </TabPane>
      <TabPane key="dept" :tab="t('organization.dept')">
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
          :type="AuthObjectTypeEnum.DEPT"
          :authObjectId="checkedDeptId"
          :permissions="permissions"
          :projectId="props.projectId"
          class="flex-1" />
      </TabPane>
      <TabPane key="group" :tab="t('organization.group')">
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
          :type="AuthObjectTypeEnum.GROUP"
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
