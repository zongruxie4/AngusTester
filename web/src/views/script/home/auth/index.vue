<script setup lang="ts">
import { defineAsyncComponent, onMounted, ref, watch } from 'vue';
import { enumUtils } from '@xcan-angus/infra';
import { ScriptPermission } from '@/enums/enums';
import { TabPane, Tabs } from 'ant-design-vue';
import { Hints, Modal } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import { IndexProps, Permission, AuthObjectType } from './types';

const { t } = useI18n();

// Lazy load components for better performance
const GroupSet = defineAsyncComponent(() => import('./GroupSet.vue'));
const AuthSet = defineAsyncComponent(() => import('./AuthSet.vue'));

/**
 * <p>
 * Component props with default values.
 * </p>
 */
const props = withDefaults(defineProps<IndexProps>(), {
  projectId: undefined,
  appId: undefined,
  visible: false
});

/**
 * <p>
 * Component emits definition for visibility control.
 * </p>
 */
const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void;
}>();

// Tab and selection state
const activeKey = ref<AuthObjectType>('user');
const checkedUserId = ref<string>();
const checkedGroupId = ref<string>();
const checkedDeptId = ref<string>();

// Permissions and loading state
const permissions = ref<Permission[]>([]);
const loaded = ref(false);

/**
 * <p>
 * Closes the modal by emitting visibility update.
 * </p>
 */
const cancel = () => {
  emit('update:visible', false);
};

/**
 * <p>
 * Loads permission enums and converts them to the required format.
 * Maps enum values to permission objects with labels and values.
 * </p>
 */
const loadEnums = () => {
  const res = enumUtils.enumToMessages(ScriptPermission);
  permissions.value = res.map(item => ({ 
    label: item.message, 
    value: item.value 
  }));
};

/**
 * <p>
 * Component lifecycle: Initialize on mount.
 * Sets up watchers for visibility changes and loads permissions.
 * </p>
 */
onMounted(() => {
  watch(() => props.visible, (newValue) => {
    if (!newValue) {
      return;
    }

    // Reset state when modal becomes visible
    activeKey.value = 'user';
    checkedUserId.value = undefined;
    checkedGroupId.value = undefined;
    checkedDeptId.value = undefined;
    loaded.value = false;

    // Load permission enums
    loadEnums();
  }, { immediate: true });
});

// Internationalization text
const text = t('scriptHome.globalAuth.description');

// Modal body style configuration
const bodyStyle = {
  padding: '0 20px',
  height: 'calc(100% - 70px)'
};
</script>

<template>
  <Modal
    :title="t('scriptHome.globalAuth.title')"
    :footer="false"
    :visible="props.visible"
    :bodyStyle="bodyStyle"
    style="width: 98%;height: 95%;"
    wrapClassName="authorize-modal-wrapper"
    @cancel="cancel">
    <div class="h-full pt-2">
      <!-- Description hints -->
      <Hints :text="text" />
      
      <!-- Tab navigation -->
      <Tabs
        v-model:activeKey="activeKey"
        size="small"
        style="height: calc(100% - 18px);">
        
        <!-- User tab -->
        <TabPane key="user" :tab="t('scriptHome.globalAuth.tabs.user')">
          <GroupSet
            key="user"
            v-model:checkedId="checkedUserId"
            v-model:loaded="loaded"
            type="user"
            class="flex-shrink-0 flex-grow-0 w-75 mr-4"
            :visible="props.visible"
            :appId="props.appId" />
          <AuthSet
            v-if="loaded"
            key="user"
            type="user"
            class="flex-1"
            :projectId="props.projectId"
            :authObjectId="checkedUserId"
            :permissions="permissions" />
        </TabPane>
        
        <!-- Department tab -->
        <TabPane key="dept" :tab="t('scriptHome.globalAuth.tabs.dept')">
          <GroupSet
            key="dept"
            v-model:checkedId="checkedDeptId"
            v-model:loaded="loaded"
            type="dept"
            class="flex-shrink-0 flex-grow-0 w-75 mr-4"
            :visible="props.visible"
            :appId="props.appId" />
          <AuthSet
            v-if="loaded"
            key="dept"
            type="dept"
            class="flex-1"
            :projectId="props.projectId"
            :authObjectId="checkedDeptId"
            :permissions="permissions" />
        </TabPane>
        
        <!-- Group tab -->
        <TabPane key="group" :tab="t('scriptHome.globalAuth.tabs.group')">
          <GroupSet
            key="group"
            v-model:checkedId="checkedGroupId"
            v-model:loaded="loaded"
            type="group"
            class="flex-shrink-0 flex-grow-0 w-75 mr-4"
            :visible="props.visible"
            :appId="props.appId" />
          <AuthSet
            v-if="loaded"
            key="group"
            type="group"
            class="flex-1"
            :projectId="props.projectId"
            :authObjectId="checkedGroupId"
            :permissions="permissions" />
        </TabPane>
      </Tabs>
    </div>
  </Modal>
</template>

<style>
.authorize-modal-wrapper .ant-modal-content {
  height: 100%;
}

.authorize-modal-wrapper .ant-tabs .ant-tabs-nav {
  margin: 0;
}

.authorize-modal-wrapper .ant-tabs .ant-tabs-tabpane {
  display: flex;
  padding: 8px 0;
}
</style>
