<script setup lang="ts">
import { computed, defineAsyncComponent, inject, onMounted, provide, ref, Ref } from 'vue';
import { Spin } from '@xcan-angus/vue-ui';
import elementResizeDetectorMaker from 'element-resize-detector';
import { localStore, PageQuery } from '@xcan-angus/infra';

import { CaseCount, CaseViewMode } from '@/views/function/case/types';

const Statistics = defineAsyncComponent(() => import('@/views/function/case/list/statistics/index.vue'));
const PieChart = defineAsyncComponent(() => import('@/views/function/case/list/statistics/Chart.vue'));
const CaseList = defineAsyncComponent(() => import('@/views/function/case/list/case/index.vue'));

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

// Inject project information
const projectId = inject<Ref<string>>('projectId', ref(''));

const userInfo: any = inject('userInfo');

// 存储展示模式的key
const cacheModeKey = computed(() => {
  return `${userInfo.id}${projectId.value}caseMode`;
});
// 存储展开收起的key
const cacheCountKey = computed(() => {
  return `${userInfo.id}${projectId.value}caseCount`;
});

const queryParams = ref<PageQuery>();
const isOpenCount = ref(typeof localStore.get(cacheCountKey.value) === 'boolean' ? localStore.get(cacheCountKey.value) : true);
const viewMode = ref<CaseViewMode>(localStore.get(cacheModeKey.value) ?? CaseViewMode.flat);

const caseCountData = ref<CaseCount>();

const countChange = () => {
  isOpenCount.value = !isOpenCount.value;
  localStore.set(cacheCountKey.value, isOpenCount.value);
  if (isOpenCount.value) {
    funcCaseListRef.value?.loadCaseCount();
  }
};

const viewModeChange = (value: CaseViewMode) => {
  viewMode.value = value;
  localStore.set(cacheModeKey.value, value);
};

const funcCaseListRef = ref<any>();

const loading = ref(false);
const updateLoading = (_loading: boolean): void => {
  loading.value = _loading;
};

const handleOpenInfo = async (tabParams) => {
  emits('openInfo', tabParams);
};

const caseCountRef = ref(null);
const resizeDetector = elementResizeDetectorMaker();
const countHeight = ref(0);
const resize = (element) => {
  countHeight.value = element.offsetHeight;
};

onMounted(() => {
  if (!caseCountRef.value) {
    return;
  }
  resizeDetector.listenTo(caseCountRef.value, resize);
});

provide('updateLoading', updateLoading as () => void);

defineExpose({
  resetParam: () => {
    funcCaseListRef.value && funcCaseListRef.value.resetParam();
  }
});
</script>
<template>
  <Spin
    :spinning="loading"
    class="flex flex-col h-full overflow-y-auto overflow-x-hidden">
    <div
      ref="caseCountRef"
      class="flex items-center top-count-element mx-3.5 justify-between"
      :class="{ 'top-count-element-expanded': isOpenCount }">
      <template v-if="isOpenCount">
        <Statistics :dataSource="caseCountData" class="" />
        <PieChart :dataSource="caseCountData" />
      </template>
    </div>

    <div class="p-3.5 flex-1 min-h-0">
      <CaseList
        ref="funcCaseListRef"
        :count="caseCountData"
        :viewMode="viewMode"
        :isOpenCount="isOpenCount"
        :queryParams="queryParams"
        :notify="props.notify"
        :tabInfo="props.tabInfo"
        @viewModeChange="viewModeChange"
        @countChange="countChange"
        @openInfo="handleOpenInfo" />
    </div>
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
