<script setup lang="ts">
import { nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue';
import { Checkbox, Switch } from 'ant-design-vue';
import elementResizeDetector from 'element-resize-detector';
import { Icon, Input, NoData, Spin } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import CheckboxGroup from './CheckboxGroup.vue';
import { useAuthData } from './composables/useAuthData';
import { useAuthActions } from './composables/useAuthActions';
import { AuthSetProps } from './types';

const { t } = useI18n();

/**
 * <p>
 * Component props with default values.
 * </p>
 */
const props = withDefaults(defineProps<AuthSetProps>(), {
  projectId: undefined,
  authObjectId: undefined,
  type: 'user',
  permissions: () => []
});

// Element resize detector instance
const erd = elementResizeDetector({ strategy: 'scroll' });
const containerRef = ref<HTMLElement>();

/**
 * <p>
 * Initialize authentication data management using composable.
 * Handles data loading, pagination, and search functionality.
 * </p>
 */
const {
  loading,
  idList,
  dataMap,
  permissionsMap,
  enabledLoadingMap,
  searchInputValue,
  updatingMap,
  searchInputChange,
  handleScroll,
  resizeHandler,
  initialize
} = useAuthData(props.projectId, props.authObjectId, props.type);

/**
 * <p>
 * Initialize authentication actions using composable.
 * Handles permission updates, enabling/disabling scripts.
 * </p>
 */
const {
  switchChange,
  checkAllChange,
  checkChange
} = useAuthActions(
  dataMap,
  permissionsMap,
  enabledLoadingMap,
  updatingMap,
  props.authObjectId,
  props.type,
  props.permissions
);

/**
 * <p>
 * Component lifecycle: Initialize on mount.
 * Sets up resize detection and initializes data loading.
 * </p>
 */
onMounted(() => {
  nextTick(() => {
    if (!containerRef.value) {
      return;
    }

    const height = containerRef.value.offsetHeight;
    initialize(height);
    erd.listenTo(containerRef.value, () => resizeHandler(containerRef.value?.offsetHeight || 0));
  });
});

// Watch for authObjectId changes to reinitialize when needed
watch(() => props.authObjectId, (newValue) => {
  if (newValue && containerRef.value) {
    nextTick(() => {
      const height = containerRef.value?.offsetHeight;
      if (height) {
        initialize(height);
      }
    });
  }
});

/**
 * <p>
 * Component lifecycle: Cleanup on unmount.
 * Removes resize listener to prevent memory leaks.
 * </p>
 */
onBeforeUnmount(() => {
  if (containerRef.value) {
    erd.removeListener(containerRef.value, () => resizeHandler(containerRef.value?.offsetHeight || 0));
  }
});
</script>

<template>
  <div style="width: calc(100% - 316px);" class="text-3 leading-4.5 h-full">
    <!-- Search input for filtering scripts -->
    <Input
      :value="searchInputValue"
      :allowClear="true"
      :placeholder="t('scriptHome.globalAuth.authSet.searchPlaceholder')"
      class="mb-2"
      @change="(e) => searchInputChange(e)" />

    <!-- Header row with column titles -->
    <div v-if="props.authObjectId" class="flex items-center h-11 pr-1.75 rounded bg-gray-light text-theme-title">
      <div class="flex-1 px-2 truncate">{{ t('scriptHome.globalAuth.authSet.name') }}</div>
      <div style="width:70px;" class="flex-shrink-0 px-2">{{ t('scriptHome.globalAuth.authSet.authPlaceholder') }}</div>
      <div style="width:52%">{{ t('scriptHome.globalAuth.authSet.auth') }}</div>
    </div>

    <!-- No data state -->
    <NoData
      v-show="!loading && !idList?.length"
      style="height: calc(100% - 36px);"
      size="small" />
    {{ permissionsMap }}

    <!-- Loading and data container -->
    <Spin
      v-show="loading||!!idList?.length"
      :mask="false"
      :spinning="loading"
      :tip="t('common.loading')"
      style="height: calc(100% - 76px);">
      <div
        ref="containerRef"
        class="h-full overflow-y-auto"
        @scroll="handleScroll">
        <!-- Script list items -->
        <template v-for="item in idList" :key="item">
          <div class="flex items-center min-h-11 py-1 border-b border-solid cursor-pointer hover:bg-gray-hover border-theme-text-box">
            <!-- Script name and icon -->
            <div class="flex items-center flex-1 overflow-hidden">
              <Icon icon="icon-jiaoben" class="flex-shrink-0 text-4 ml-1.5" />
              <div class="flex-1 px-2 pt-0.5 truncate" :title="dataMap[item].name">
                <span :data-id="item">{{ dataMap[item].name }}</span>
              </div>
            </div>

            <!-- Enable/disable switch -->
            <div style="width:70px;" class="px-2">
              <Switch
                :loading="enabledLoadingMap[item]"
                :checked="dataMap[item].auth"
                size="small"
                @change="(checked: any) => switchChange(!!checked, item)" />
            </div>

            <!-- Permission checkboxes -->
            <div style="width:52%" class="flex items-start">
              <Checkbox
                :disabled="permissionsMap[item]?.creatorFlag || dataMap[item]?.auth === false"
                :checked="permissionsMap[item]?.permissions.length === props.permissions.length"
                :indeterminate="!!(permissionsMap[item]?.permissions.length
                  && permissionsMap[item]?.permissions.length! < props.permissions.length)"
                class="whitespace-nowrap"
                @change="(event: { target: { checked: boolean } }) => checkAllChange(event, item)">
                {{ t('scriptHome.globalAuth.authSet.all') }}
              </Checkbox>
              <CheckboxGroup
                :disabled="permissionsMap[item]?.creatorFlag || dataMap[item]?.auth === false"
                :value="permissionsMap[item]?.permissions"
                :options="props.permissions"
                @change="(permissions: string[]) => checkChange(permissions, item)" />
            </div>
          </div>
        </template>
      </div>
    </Spin>
  </div>
</template>
