<script setup lang="ts">
import { useI18n } from 'vue-i18n';
import { Checkbox, Switch } from 'ant-design-vue';
import { Icon, Input, NoData, Spin } from '@xcan-angus/vue-ui';

import CheckboxGroup from './CheckboxGroup.vue';
import { useAuthData } from './composables/useAuthData';

const { t } = useI18n();

/**
 * Props interface for AuthSet component
 */
type Props = {
  projectId: string;
  authObjectId: string | undefined;
  type: 'user' | 'dept' | 'group';
  permissions: { value: string; label: string }[];
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  authObjectId: undefined,
  type: 'user',
  permissions: () => []
});

// Use composable for authorization data management
const {
  loading,
  idList,
  dataMap,
  permissionsMap,
  enabledLoadingMap,
  searchInputValue,
  containerRef,
  totalPage,
  permissionValues,
  handleSearchInputChange,
  handleScroll,
  handleSwitchChange,
  handleCheckAllChange,
  handleCheckChange,
  init
} = useAuthData(
  props.projectId,
  props.authObjectId,
  props.type,
  props.permissions
);

// Initialize the composable
init();
</script>
<template>
  <div style="width: calc(100% - 316px);" class="text-3 leading-4.5 h-full">
    <Input
      :value="searchInputValue"
      :allowClear="true"
      :placeholder="t('reportHome.globalAuth.authSet.searchPlaceholder')"
      class="mb-2"
      @change="handleSearchInputChange" />
    <div v-if="props.authObjectId" class="flex items-center h-11 pr-1.75 rounded bg-gray-light text-theme-title">
      <div class="flex-1 px-2 truncate">{{ t('reportHome.globalAuth.authSet.name') }}</div>
      <div style="width:70px;" class="flex-shrink-0 px-2">{{ t('reportHome.globalAuth.authSet.permissionControl') }}</div>
      <div style="width:52%">{{ t('reportHome.globalAuth.authSet.permission') }}</div>
    </div>
    <NoData
      v-show="!loading && !idList?.length"
      style="height: calc(100% - 36px);"
      size="small" />
    <Spin
      v-show="loading||!!idList?.length"
      :mask="false"
      :spinning="loading"
      :tip="t('reportHome.globalAuth.authSet.loading')"
      style="height: calc(100% - 76px);">
      <div
        ref="containerRef"
        class="h-full overflow-y-auto"
        @scroll="handleScroll">
        <template v-for="item in idList" :key="item">
          <div class="flex items-center min-h-11 py-1 border-b border-solid cursor-pointer hover:bg-gray-hover border-theme-text-box">
            <div class="flex items-center flex-1 overflow-hidden">
              <Icon icon="icon-jiaoben" class="flex-shrink-0 text-4 ml-1.5" />
              <div class="flex-1 px-2 pt-0.5 truncate" :title="dataMap[item].name">
                <span :data-id="item">{{ dataMap[item].name }}</span>
              </div>
            </div>
            <div style="width:70px;" class="px-2">
              <Switch
                :loading="enabledLoadingMap[item]"
                :checked="dataMap[item].auth"
                size="small"
                @change="handleSwitchChange($event, item)" />
            </div>
            <div style="width:52%" class="flex items-start">
              <Checkbox
                :disabled="permissionsMap[item]?.creatorFlag || dataMap[item]?.auth === false"
                :checked="!!(permissionsMap[item]?.permissions.length === props.permissions.length)"
                :indeterminate="!!(permissionsMap[item]?.permissions.length && permissionsMap[item]?.permissions.length! < props.permissions.length)"
                class="whitespace-nowrap"
                @change="handleCheckAllChange($event, item)">
                {{ t('reportHome.globalAuth.authSet.all') }}
              </Checkbox>
              <CheckboxGroup
                :disabled="permissionsMap[item]?.creatorFlag || dataMap[item]?.auth === false"
                :value="permissionsMap[item]?.permissions"
                :options="props.permissions"
                @change="handleCheckChange($event, item)" />
            </div>
          </div>
        </template>
      </div>
    </Spin>
  </div>
</template>
