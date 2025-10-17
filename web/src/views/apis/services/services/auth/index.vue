<script setup lang="ts">
import { defineAsyncComponent, onMounted, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { enumUtils, AuthObjectType, EnumMessage } from '@xcan-angus/infra';
import { TabPane, Tabs } from 'ant-design-vue';
import { Hints } from '@xcan-angus/vue-ui';
import { ApiPermission, ServicesPermission } from '@/enums/enums';

const GroupSet = defineAsyncComponent(() => import('@/views/apis/services/services/auth/groupSet/index.vue'));
const AuthSet = defineAsyncComponent(() => import('@/views/apis/services/services/auth/authSet/index.vue'));

interface Props {
  appId: string;
}

const props = withDefaults(defineProps<Props>(), {
  appId: undefined
});

const activeTabKey = ref<AuthObjectType>(AuthObjectType.USER);
const selectedUserId = ref<string>();
const selectedGroupId = ref<string>();
const selectedDeptId = ref<string>();

const { t } = useI18n();

const apiPermissions = ref<EnumMessage<ApiPermission>[]>([]);
const servicePermissions = ref<EnumMessage<ServicesPermission>[]>([]);
const isDataLoaded = ref(false);

/**
 * Load permission enums and convert to message format
 * Initializes permission data for API and service permissions
 */
const loadPermissionEnums = () => {
  apiPermissions.value = enumUtils.enumToMessages(ApiPermission);
  servicePermissions.value = enumUtils.enumToMessages(ServicesPermission);
};

onMounted(() => {
  loadPermissionEnums();
});
</script>

<template>
  <div class="h-full py-3.5">
    <Hints class="mx-3" :text="t('service.authSetting.hints.authDescription')" />
    <Tabs
      v-model:activeKey="activeTabKey"
      size="small"
      style="height: calc(100% - 18px);">
      <TabPane key="user" :tab="t('organization.user')">
        <GroupSet
          :key="AuthObjectType.USER"
          v-model:checkedId="selectedUserId"
          v-model:loaded="isDataLoaded"
          :appId="props.appId"
          :type="AuthObjectType.USER"
          class="flex-shrink-0 flex-grow-0 w-75 mr-4" />
        <AuthSet
          v-if="isDataLoaded"
          :key="AuthObjectType.USER"
          :authObjectId="selectedUserId"
          :apiPermissions="apiPermissions"
          :servicePermissions="servicePermissions"
          :type="AuthObjectType.USER"
          class="flex-1" />
      </TabPane>
      <TabPane key="dept" :tab="t('organization.dept')">
        <GroupSet
          :key="AuthObjectType.DEPT"
          v-model:checkedId="selectedDeptId"
          v-model:loaded="isDataLoaded"
          :appId="props.appId"
          :type="AuthObjectType.DEPT"
          class="flex-shrink-0 flex-grow-0 w-75 mr-4" />
        <AuthSet
          v-if="isDataLoaded"
          :key="AuthObjectType.DEPT"
          :authObjectId="selectedDeptId"
          :type="AuthObjectType.DEPT"
          :apiPermissions="apiPermissions"
          :servicePermissions="servicePermissions"
          class="flex-1" />
      </TabPane>
      <TabPane key="group" :tab="t('organization.group')">
        <GroupSet
          :key="AuthObjectType.GROUP"
          v-model:checkedId="selectedGroupId"
          v-model:loaded="isDataLoaded"
          :appId="props.appId"
          :type="AuthObjectType.GROUP"
          class="flex-shrink-0 flex-grow-0 w-75 mr-4" />
        <AuthSet
          v-if="isDataLoaded"
          :key="AuthObjectType.GROUP"
          :authObjectId="selectedGroupId"
          :type="AuthObjectType.GROUP"
          :apiPermissions="apiPermissions"
          :servicePermissions="servicePermissions"
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
