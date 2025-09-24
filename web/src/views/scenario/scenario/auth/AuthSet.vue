<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted } from 'vue';
import { useI18n } from 'vue-i18n';
import { Checkbox, Switch } from 'ant-design-vue';
import { Icon, IconText, Input, NoData, Spin } from '@xcan-angus/vue-ui';

import CheckboxGroup from './CheckboxGroup.vue';
import { useAuthData } from './composables/useAuthData';
import { useAuthActions } from './composables/useAuthActions';
import { usePagination } from './composables/usePagination';
import type { AuthSetProps, CheckboxChangeEvent } from './types';

const { t } = useI18n();

// Component props with proper typing
const props = withDefaults(defineProps<AuthSetProps>(), {
  projectId: undefined,
  authObjectId: undefined,
  type: 'user',
  permissions: () => []
});

// Use composables for data management
const {
  loading,
  idList,
  dataMap,
  permissionsMap,
  enabledLoadingMap,
  searchInputValue,
  reset,
  loadScenarioData,
  handleSearchInputChange,
  handleScroll: handleDataScroll,
  updatingMap
} = useAuthData(props.projectId, props.authObjectId, props.type);

// Use composables for pagination
const {
  containerRef,
  handleScroll: handlePaginationScroll,
  initializePagination,
  setupResizeListener,
  removeResizeListener
} = usePagination(loadScenarioData, reset);

// Computed properties
const permissionValues = computed(() => {
  return props.permissions.map(item => item.value);
});

// Use composables for auth actions
const { handleSwitchChange, handleCheckAllChange, handlePermissionChange } = useAuthActions(
  dataMap.value,
  permissionsMap.value,
  enabledLoadingMap.value,
  updatingMap,
  permissionValues.value,
  props.authObjectId,
  props.type
);

// Event handlers with proper typing
const handleSwitchChangeEvent = (checked: boolean, id: string) => {
  handleSwitchChange(checked, id);
};

const handleCheckAllChangeEvent = (event: CheckboxChangeEvent, id: string) => {
  handleCheckAllChange(event, id);
};

const handlePermissionChangeEvent = (permissions: string[], id: string) => {
  handlePermissionChange(permissions, id);
};

// Combined scroll handler
const handleScroll = (event: Event) => {
  handleDataScroll(event);
  handlePaginationScroll(event);
};

// Lifecycle hooks
onMounted(() => {
  nextTick(() => {
    initializePagination();
    loadScenarioData();
    setupResizeListener();
  });
});

onBeforeUnmount(() => {
  removeResizeListener();
});
</script>

<template>
  <div style="width: calc(100% - 316px);" class="text-3 leading-4.5 h-full">
    <!-- Search Input -->
    <Input
      :value="searchInputValue"
      :allowClear="true"
      :placeholder="t('scenario.auth.authSet.searchPlaceholder')"
      class="w-75 mb-3.5"
      @change="(event: any) => handleSearchInputChange(event)">
      <template #suffix>
        <Icon class="text-3.5 cursor-pointer text-theme-content" icon="icon-sousuo" />
      </template>
    </Input>

    <!-- Table Header -->
    <div v-if="props.authObjectId" class="flex items-center h-11 pr-1.75 rounded bg-gray-light text-theme-title">
      <div class="flex-1 px-2 truncate">{{ t('scenario.auth.authSet.table.columns.name') }}</div>
      <div style="width:70px;" class="flex-shrink-0 px-2">{{ t('scenario.auth.authSet.table.columns.permissionControl') }}</div>
      <div style="width:52%">{{ t('actions.permission') }}</div>
    </div>

    <!-- No Data State -->
    <NoData
      v-show="!loading && !idList?.length"
      style="height: calc(100% - 36px);"
      size="small" />

    <!-- Loading and Data List -->
    <Spin
      v-show="loading || !!idList?.length"
      :mask="false"
      :spinning="loading"
      :tip="t('common.loading')"
      style="height: calc(100% - 76px);">
      <div
        ref="containerRef"
        class="h-full overflow-y-auto"
        @scroll="handleScroll">
        <template v-for="item in idList" :key="item.id">
          <div
            class="flex items-center min-h-11 py-1 border-b border-solid cursor-pointer hover:bg-gray-hover border-theme-text-box">
            <!-- Scenario Name -->
            <div class="flex items-center flex-1 overflow-hidden">
              <IconText class="ml-1.5" />
              <div class="flex-1 px-2 pt-0.5 truncate" :title="dataMap[item].name">
                <span :data-id="item">{{ dataMap[item].name }}</span>
              </div>
            </div>

            <!-- Permission Control Switch -->
            <div style="width:70px;" class="px-2">
              <Switch
                :loading="enabledLoadingMap[item]"
                :checked="dataMap[item].auth"
                size="small"
                @change="(checked: any) => handleSwitchChangeEvent(!!checked, item)" />
            </div>

            <!-- Permission Checkboxes -->
            <div style="width:52%" class="flex items-start">
              <Checkbox
                :disabled="permissionsMap[item]?.creatorFlag || dataMap[item]?.auth === false"
                :checked="!!(permissionsMap[item]?.permissions.length === props.permissions.length)"
                :indeterminate="!!(permissionsMap[item]?.permissions.length && permissionsMap[item]?.permissions.length! < props.permissions.length)"
                class="whitespace-nowrap"
                @change="handleCheckAllChangeEvent($event, item)">
                {{ t('actions.selectAll') }}
              </Checkbox>
              <CheckboxGroup
                :disabled="permissionsMap[item]?.creatorFlag || dataMap[item]?.auth === false"
                :value="permissionsMap[item]?.permissions"
                :options="props.permissions"
                @change="handlePermissionChangeEvent($event, item)" />
            </div>
          </div>
        </template>
      </div>
    </Spin>
  </div>
</template>
