<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue';
import { Checkbox, Switch } from 'ant-design-vue';
import { Icon, Input, NoData, Spin } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';

import CheckboxGroup from './CheckboxGroup.vue';
import { useAuthSet } from './composables/useAuthSet';
import { useAuthSetLifecycle } from './composables/useAuthSetLifecycle';
import type { AuthSetProps } from './types';

const { t } = useI18n();

// Component props with default values
const props = withDefaults(defineProps<AuthSetProps>(), {
  authObjectId: undefined,
  type: 'user',
  permissions: () => []
});

// Template refs
const containerRef = ref<HTMLElement>();

// Use composables for logic separation
const {
  // State
  loading,
  idList,
  dataMap,
  permissionsMap,
  enabledLoadingMap,
  searchInputValue,

  // Methods
  searchInputChange,
  switchChange,
  checkAllChange,
  checkChange,
  handleScroll,
  resizeHandler,
  reset,
  loadData
} = useAuthSet(props);

// Use lifecycle composable
const { initializeOnMount, setupWatchers, cleanup } = useAuthSetLifecycle(
  props,
  containerRef,
  resizeHandler,
  loadData,
  reset
);

// Lifecycle hooks
onMounted(() => {
  initializeOnMount();
  setupWatchers();
});

onBeforeUnmount(() => {
  cleanup();
});
</script>

<template>
  <div style="width: calc(100% - 316px);" class="text-3 leading-4.5 h-full">
    <!-- Search Input -->
    <Input
      :value="searchInputValue"
      :allowClear="true"
      :placeholder="t('fileSpace.globalAuth.authSet.searchPlaceholder')"
      class="mb-2"
      @change="searchInputChange" />

    <!-- Table Headers -->
    <div
      v-if="props.authObjectId"
      class="flex items-center h-11 pr-1.75 rounded bg-gray-light">
      <div class="flex-1 px-2 truncate">
        {{ t('common.name') }}
      </div>
      <div style="width:70px;" class="flex-shrink-0 px-2">
        {{ t('fileSpace.globalAuth.authSet.headers.permissionControl') }}
      </div>
      <div style="width:52%">
        {{ t('fileSpace.globalAuth.authSet.headers.permissions') }}
      </div>
    </div>

    <!-- No Data State -->
    <NoData
      v-show="!loading && !idList?.length"
      style="height: calc(100% - 36px);"
      size="small" />

    <!-- Loading and Data Container -->
    <Spin
      v-show="loading || !!idList?.length"
      :mask="false"
      :spinning="loading"
      :tip="t('fileSpace.globalAuth.authSet.loadingTip')"
      style="height: calc(100% - 76px);">
      <div
        ref="containerRef"
        class="h-full overflow-y-auto"
        @scroll="handleScroll">
        <!-- Space Items List -->
        <template v-for="item in idList" :key="item">
          <div class="flex items-center min-h-11 py-1 border-b border-solid cursor-pointer hover:bg-gray-hover border-theme-text-box">
            <!-- Space Name Section -->
            <div class="flex items-center flex-1 overflow-hidden">
              <Icon icon="icon-kongjian" class="flex-shrink-0 text-4 ml-1.5" />
              <div class="flex-1 px-2 pt-0.5 truncate" :title="dataMap[item]?.name">
                <span :data-id="item">{{ dataMap[item]?.name }}</span>
              </div>
            </div>

            <!-- Permission Control Switch -->
            <div style="width:70px;" class="px-2">
              <Switch
                :loading="enabledLoadingMap[item]"
                :checked="dataMap[item]?.auth"
                size="small"
                @change="(checked: boolean) => switchChange(checked, item)" />
            </div>

            <!-- Permissions Section -->
            <div style="width:52%" class="flex items-start">
              <!-- All Permissions Checkbox -->
              <Checkbox
                :disabled="permissionsMap[item]?.creatorFlag || dataMap[item]?.auth === false"
                :checked="!!(permissionsMap[item]?.permissions.length === props.permissions.length)"
                :indeterminate="!!(permissionsMap[item]?.permissions.length && permissionsMap[item]?.permissions.length! < props.permissions.length)"
                class="whitespace-nowrap"
                @change="(event) => checkAllChange(event, item)">
                {{ t('fileSpace.globalAuth.authSet.allPermissions') }}
              </Checkbox>

              <!-- Individual Permissions -->
              <CheckboxGroup
                :disabled="permissionsMap[item]?.creatorFlag || dataMap[item]?.auth === false"
                :value="permissionsMap[item]?.permissions"
                :options="props.permissions"
                @change="(permissions) => checkChange(permissions, item)" />
            </div>
          </div>
        </template>
      </div>
    </Spin>
  </div>
</template>

<style scoped>
/**
 * <p>
 * Component-specific styles for the AuthSet component
 * </p>
 */

/* Ensure proper spacing and layout */

.leading-4\.5 {
  line-height: 1.125rem;
}

/* Hover effects for interactive elements */
.hover\:bg-gray-hover:hover {
  background-color: rgba(239, 240, 243, 0.5);
}

/* Border styling */
.border-theme-text-box {
  border-color: var(--theme-text-box);
}
</style>
