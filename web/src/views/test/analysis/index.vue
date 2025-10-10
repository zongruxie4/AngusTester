<script setup lang="ts">
import { computed, defineAsyncComponent, onMounted, provide, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { BrowserTab } from '@xcan-angus/vue-ui';
import { utils, IPane } from '@xcan-angus/infra';
import { useI18n } from 'vue-i18n';
import { BasicProps } from '@/types/types';

// Component setup
const { t } = useI18n();

const props = withDefaults(defineProps<BasicProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined
});

// Async components
const AnalysisList = defineAsyncComponent(() => import('@/views/test/analysis/list/index.vue'));
const AnalysisEdit = defineAsyncComponent(() => import('@/views/test/analysis/edit/index.vue'));
const AnalysisDetail = defineAsyncComponent(() => import('@/views/test/analysis/detail/index.vue'));

// Router and refs
const route = useRoute();
const router = useRouter();
const browserTabRef = ref();
const activeTabKey = ref('analysisList');

// Reactive state
const listRefreshTrigger = ref(0);

/**
 * Generate storage key for browser tab persistence.
 */
const storageKey = computed(() => {
  if (!props.projectId) {
    return undefined;
  }
  return `analysis_case_${props.projectId}`;
});

/**
 * Add a new tab pane to the browser tab component.
 * @param data - Tab pane data to add
 */
const addTabPane = (data: IPane) => {
  browserTabRef.value.add(() => {
    return data;
  });
};

/**
 * Get tab pane data by key.
 * @param key - Tab pane key
 * @returns Tab pane data array or undefined
 */
const getTabPane = (key: string): IPane[] | undefined => {
  return browserTabRef.value.getData(key);
};

/**
 * Delete tab panes by keys.
 * @param keys - Array of tab pane keys to delete
 */
const deleteTabPane = (keys: string[]) => {
  browserTabRef.value.remove(keys);
};

/**
 * Update existing tab pane data.
 * @param data - Updated tab pane data
 */
const updateTabPane = (data: IPane) => {
  browserTabRef.value.update(data);
};

/**
 * Replace tab pane with new data.
 * @param key - Tab pane key to replace
 * @param data - New tab pane data
 */
const replaceTabPane = (key: string, data: { key: string }) => {
  browserTabRef.value.replace(key, data);
};

/**
 * Initialize the browser tab component with default analysis list tab.
 */
const initializeBrowserTabs = () => {
  if (typeof browserTabRef.value?.add === 'function') {
    browserTabRef.value.add((existingIds: string[]) => {
      if (!existingIds.includes('analysisList')) {
        return {
          _id: 'analysisList',
          value: 'analysisList',
          name: t('testAnalysis.title'),
          closable: false
        };
      }
    });
  }

  handleHashChange(route.hash);
};

/**
 * Handle hash change events to manage tab navigation.
 * @param hash - URL hash string
 */
const handleHashChange = (hash: string) => {
  const queryString = hash.split('?')[1];
  if (!queryString) {
    return;
  }

  // Parse query parameters from hash
  const queryParameters = queryString.split('&').reduce((accumulator, current) => {
    const [key, value] = current.split('=');
    accumulator[key] = value;
    return accumulator;
  }, {} as { [key: string]: string });

  const { id, type } = queryParameters;

  if (id) {
    // Handle existing analysis (edit or detail view)
    if (type === 'edit') {
      browserTabRef.value.add(() => {
        return {
          _id: id,
          value: 'analysisEdit',
          noCache: true,
          data: { _id: id, id }
        };
      });
    } else {
      browserTabRef.value.add(() => {
        return {
          _id: `analysisDetail_${id}`,
          value: 'analysisDetail',
          data: { _id: id, id }
        };
      });
    }
  } else {
    // Handle new analysis creation
    if (type) {
      browserTabRef.value.add(() => {
        const newId = utils.uuid();
        return {
          _id: newId,
          name: t('testAnalysis.actions.addAnalysis'),
          value: 'analysisEdit',
          noCache: true,
          data: { _id: newId }
        };
      });
    }
  }

  router.replace('/function#analysis');
};

/**
 * Handle storage key change event.
 */
const handleStorageKeyChange = () => {
  initializeBrowserTabs();
};

/**
 * Handle successful analysis edit completion.
 */
const handleEditSuccess = () => {
  listRefreshTrigger.value += 1;
};

// Lifecycle hooks
onMounted(() => {
  // Watch for hash changes to handle tab navigation
  watch(() => route.hash, () => {
    if (!route.hash.startsWith('#list')) {
      return;
    }
    handleHashChange(route.hash);
  });
});

// Provide injection
provide('addTabPane', addTabPane);
provide('getTabPane', getTabPane);
provide('deleteTabPane', deleteTabPane);
provide('updateTabPane', updateTabPane);
provide('replaceTabPane', replaceTabPane);
</script>
<template>
  <BrowserTab
    ref="browserTabRef"
    v-model:activeKey="activeTabKey"
    hideAdd
    class="h-full"
    :userId="props.userInfo?.id"
    :storageKey="storageKey"
    @storageKeyChange="handleStorageKeyChange">
    <template #default="record">
      <template v-if="record.value === 'analysisList'">
        <AnalysisList
          :userInfo="props.userInfo"
          :projectId="props.projectId"
          :refreshNotify="listRefreshTrigger"
          :onShow="activeTabKey === 'analysisList'" />
      </template>
      <template v-if="record.value==='analysisEdit'">
        <AnalysisEdit
          :data="record.data"
          :userInfo="props.userInfo"
          :projectId="props.projectId"
          @ok="handleEditSuccess" />
      </template>
      <template v-if="record.value==='analysisDetail'">
        <AnalysisDetail
          :data="record.data"
          :userInfo="props.userInfo"
          :projectId="props.projectId"
          :onShow="activeTabKey === record._id" />
      </template>
    </template>
  </BrowserTab>
</template>
