<script setup lang="ts">
import { computed, defineAsyncComponent, inject, onMounted, provide, ref, Ref } from 'vue';
import { Spin } from '@xcan-angus/vue-ui';
import elementResizeDetectorMaker from 'element-resize-detector';
import { appContext, localStore, PageQuery, utils, SearchCriteria } from '@xcan-angus/infra';
import { CaseCount, CaseViewMode } from '@/views/test/case/types';

const StatisticsPanel = defineAsyncComponent(() => import('@/views/test/case/list/statistics/index.vue'));
const CaseList = defineAsyncComponent(() => import('@/views/test/case/list/List.vue'));

interface Props {
  notify?: number;
  queryParams?: PageQuery;
  tabMode?: CaseViewMode;
  tabInfo?: Record<string, any>;
}

const props = withDefaults(defineProps<Props>(), {
  notify: 0,
  queryParams: undefined,
  tabMode: undefined,
  planId: undefined,
  planName: undefined,
  tabInfo: undefined
});

const emits = defineEmits<{(e: 'cacheParams', value: PageQuery): void;
  (e: 'openInfo', value): void;
}>();

// Basic state management
const projectId = inject<Ref<string>>('projectId', ref(''));
const userInfo = ref(appContext.getUser());

// Cache keys for user preferences
const cacheModeKey = computed(() => {
  return `${userInfo?.value?.id}${projectId.value}caseMode`;
});
const cacheCountKey = computed(() => {
  return `${userInfo?.value?.id}${projectId.value}caseCount`;
});

// View state management
const queryParams = ref<PageQuery>();
const isStatisticsCollapsed = ref(typeof localStore.get(cacheCountKey.value) === 'boolean' ? localStore.get(cacheCountKey.value) : true);
const viewMode = ref<CaseViewMode>(localStore.get(cacheModeKey.value) ?? CaseViewMode.flat);

// Data state
const caseCountData = ref<CaseCount>();
const statisticsRefreshNotify = ref<string>('');

/**
 * Toggle statistics panel collapse state
 */
const countChange = () => {
  isStatisticsCollapsed.value = !isStatisticsCollapsed.value;
  localStore.set(cacheCountKey.value, isStatisticsCollapsed.value);
  if (isStatisticsCollapsed.value) {
    funcCaseListRef.value?.loadCaseCount();
  }
};

/**
 * Handle refresh notifications for statistics panel
 */
const handleRefreshChange = () => {
  statisticsRefreshNotify.value = utils.uuid();
};

/**
 * Change view mode and save to local storage
 * @param value - New view mode
 */
const viewModeChange = (value: CaseViewMode) => {
  viewMode.value = value;
  localStore.set(cacheModeKey.value, value);
};

// Component references and loading state
const funcCaseListRef = ref<any>();
const loading = ref(false);

/**
 * Update loading state
 * @param isLoading - Loading state
 */
const updateLoading = (isLoading: boolean): void => {
  loading.value = isLoading;
};

/**
 * Handle opening case info in new tab
 * @param tabParams - Tab parameters
 */
const handleOpenInfo = async (tabParams) => {
  emits('openInfo', tabParams);
};

/**
 * Computed property for statistics panel parameters
 * Builds parameters for statistics API calls
 */
const statisticsParameters = computed(() => {
  const parameters: { projectId: string; filters?: SearchCriteria[] } = {
    projectId: projectId.value
  };
  if (queryParams.value?.filters) {
    parameters.filters = queryParams.value.filters;
  }
  return parameters;
});

// Resize detection for statistics panel
const caseCountRef = ref(null);
const resizeDetector = elementResizeDetectorMaker();
const countHeight = ref(0);

/**
 * Handle element resize
 * @param element - Resized element
 */
const resize = (element: HTMLElement) => {
  countHeight.value = element.offsetHeight;
};

// Component lifecycle
onMounted(() => {
  if (!caseCountRef.value) {
    return;
  }
  resizeDetector.listenTo(caseCountRef.value, resize);

  // Initialize statistics refresh
  handleRefreshChange();
});

// Provide functions to child components
provide('updateLoading', updateLoading as () => void);

// Expose functions to parent components
defineExpose({
  resetParam: () => {
    funcCaseListRef.value && funcCaseListRef.value.resetParam();
  }
});
</script>
<template>
  <Spin :spinning="loading" class="flex flex-col pl-3.5 pt-3.5 h-full overflow-y-auto overflow-x-hidden">
    <StatisticsPanel
      :collapse="!isStatisticsCollapsed"
      :params="statisticsParameters"
      :notify="statisticsRefreshNotify"
      :userInfo="{ id: '' }"
      :appInfo="{ id: '' }"
      :projectId="projectId"
      :class="{ 'mb-3': isStatisticsCollapsed }"
      class="pr-5" />

    <CaseList
      ref="funcCaseListRef"
      :loading="loading"
      :count="caseCountData"
      :viewMode="viewMode"
      :isOpenCount="isStatisticsCollapsed"
      :queryParams="queryParams"
      :notify="props.notify"
      :tabInfo="props.tabInfo"
      @viewModeChange="viewModeChange"
      @countChange="countChange"
      @openInfo="handleOpenInfo"
      @refreshChange="handleRefreshChange" />
  </Spin>
</template>
<style scoped>
.top-count-element {
  height: 0;
  transition: all 0.5s ease-in-out;
}

.top-count-element-expanded {
  height: auto;
  transition: height 0.5s ease-in-out;
}

.fade-enter-from {
  height: 0;
  opacity: 0;
}

.fade-enter-to {
  height: 28px;
  opacity: 1;
}

.fade-enter-active,
.fade-leave-active {
  transition: all 0.3s ease-in-out;
}

.fade-leave-from {
  height: 28px;
  opacity: 1;
}

.fade-leave-to {
  height: 0;
  opacity: 0;
}

:deep(.ant-tabs .ant-tabs-content-holder) {
  height: calc(100% - 40px);
}

:deep(.ant-tabs .ant-tabs-content-holder .ant-tabs-content) {
  @apply h-full;
}

:deep(.ant-tabs .ant-tabs-content-holder .ant-tabs-content .ant-tabs-tabpane) {
  @apply h-full;
}
</style>
