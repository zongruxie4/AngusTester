<script setup lang="ts">
import { defineAsyncComponent, onMounted } from 'vue';
import { TabPane, Tabs } from 'ant-design-vue';
import { Hints, Modal } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';

import { useIndex } from './composables/useIndex';
import type { IndexProps, IndexEmits } from './types';

const { t } = useI18n();

// Async component imports
const GroupSet = defineAsyncComponent(() => import('@/views/data/file/auth/GroupSet.vue'));
const AuthSet = defineAsyncComponent(() => import('@/views/data/file/auth/AuthSet.vue'));

// Component props with default values
const props = withDefaults(defineProps<IndexProps>(), {
  appId: undefined,
  visible: false
});

// Component emits
const emit = defineEmits<IndexEmits>();

// Use the index composable for logic separation
const {
  // State
  activeKey,
  checkedUserId,
  checkedGroupId,
  checkedDeptId,
  permissions,
  loaded,

  // Methods
  cancel,
  setupWatchers
} = useIndex(props, emit);

// Lifecycle hooks
onMounted(() => {
  setupWatchers();
});

// Computed values
const text = t('common.description');

const bodyStyle = {
  padding: '0 20px',
  height: 'calc(100% - 70px)'
};
</script>

<template>
  <Modal
    :title="t('file.globalAuth.title')"
    :footer="false"
    :visible="props.visible"
    :bodyStyle="bodyStyle"
    style="width: 98%;height: 95%;"
    wrapClassName="authorize-modal-wrapper"
    @cancel="cancel">
    <div class="h-full pt-2">
      <!-- Description Hints -->
      <Hints :text="text" />

      <!-- Tab Navigation -->
      <Tabs
        v-model:activeKey="activeKey"
        size="small"
        style="height: calc(100% - 18px);">
        <!-- User Tab -->
        <TabPane key="user" :tab="t('organization.user')">
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
            :authObjectId="checkedUserId"
            :permissions="permissions" />
        </TabPane>

        <!-- Department Tab -->
        <TabPane key="dept" :tab="t('organization.dept')">
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
            :authObjectId="checkedDeptId"
            :permissions="permissions" />
        </TabPane>

        <!-- Group Tab -->
        <TabPane key="group" :tab="t('organization.group')">
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
            :authObjectId="checkedGroupId"
            :permissions="permissions" />
        </TabPane>
      </Tabs>
    </div>
  </Modal>
</template>

<style>
/**
 * <p>
 * Global styles for the authorize modal wrapper
 * </p>
 */

/* Modal content height */
.authorize-modal-wrapper .ant-modal-content {
  height: 100%;
}

/* Tab navigation styling */
.authorize-modal-wrapper .ant-tabs .ant-tabs-nav {
  margin: 0;
}

/* Tab pane layout */
.authorize-modal-wrapper .ant-tabs .ant-tabs-tabpane {
  display: flex;
  padding: 8px 0;
}
</style>
