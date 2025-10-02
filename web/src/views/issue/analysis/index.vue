<script setup lang="ts">
import { computed, defineAsyncComponent, onMounted, provide, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useI18n } from 'vue-i18n';
import { BrowserTab } from '@xcan-angus/vue-ui';
import { IPane, utils } from '@xcan-angus/infra';
import { BasicProps } from '@/types/types';

// Component Props & Configuration
const props = withDefaults(defineProps<BasicProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined
});

// Async Component Imports
const AnalysisList = defineAsyncComponent(() => import('@/views/issue/analysis/list/index.vue'));
const AnalysisEdit = defineAsyncComponent(() => import('@/views/issue/analysis/edit/index.vue'));
const AnalysisDetail = defineAsyncComponent(() => import('@/views/issue/analysis/detail/index.vue'));

// Vue Composition API Setup
const route = useRoute();
const router = useRouter();
const { t } = useI18n();

// Reactive State
const browserTabRef = ref();
const refreshListNotify = ref(0);
const activeTabKey = ref('analysisList');

/**
 * Generates a unique storage key for browser tab persistence based on project ID.
 */
const storageKey = computed(() => {
  if (!props.projectId) {
    return undefined;
  }
  return `analysis_task_${props.projectId}`;
});

/**
 * Adds a new tab pane to the browser tab component.
 * @param tabData - The tab pane data to add
 */
const addTabPane = (tabData: IPane) => {
  browserTabRef.value.add(() => {
    return tabData;
  });
};

/**
 * Retrieves tab pane data by key.
 *
 * @param tabKey - The key of the tab to retrieve
 * @returns Array of tab panes or undefined if not found
 */
const getTabPane = (tabKey: string): IPane[] | undefined => {
  return browserTabRef.value.getData(tabKey);
};

/**
 * Removes one or more tab panes from the browser tab component.
 *
 * @param tabKeys - Array of tab keys to remove
 */
const deleteTabPane = (tabKeys: string[]) => {
  browserTabRef.value.remove(tabKeys);
};

/**
 * Updates an existing tab pane with new data.
 *
 * @param tabData - The updated tab pane data
 */
const updateTabPane = (tabData: IPane) => {
  browserTabRef.value.update(tabData);
};

/**
 * Replaces a tab pane with new data while maintaining the same key.
 *
 * @param tabKey - The key of the tab to replace
 * @param newTabData - The new tab data with updated key
 */
const replaceTabPane = (tabKey: string, newTabData: { key: string }) => {
  browserTabRef.value.replace(tabKey, newTabData);
};

/**
 * Initializes the component by setting up the default analysis list tab and processing URL hash.
 */
const initializeComponent = () => {
  if (typeof browserTabRef.value?.add === 'function') {
    browserTabRef.value.add((existingTabIds: string[]) => {
      if (!existingTabIds.includes('analysisList')) {
        return {
          _id: 'analysisList',
          value: 'analysisList',
          name: t('issueAnalysis.title'),
          closable: false
        };
      }
    });
  }

  processUrlHash(route.hash);
};

/**
 * Processes URL hash parameters to determine which tab should be opened.
 *
 * @param urlHash - The URL hash string containing navigation parameters
 */
const processUrlHash = (urlHash: string) => {
  const queryString = urlHash.split('?')[1];
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
    if (type === 'edit') {
      // Open edit tab for existing analysis
      browserTabRef.value.add(() => {
        return {
          _id: id,
          value: 'analysisEdit',
          noCache: true,
          data: { _id: id, id }
        };
      });
    } else {
      // Open detail tab for existing analysis
      browserTabRef.value.add(() => {
        return {
          _id: `analysisDetail_${id}`,
          value: 'analysisDetail',
          data: { _id: id, id }
        };
      });
    }
  } else {
    if (type) {
      // Create new analysis tab
      browserTabRef.value.add(() => {
        const newId = utils.uuid();
        return {
          _id: newId,
          name: t('issueAnalysis.actions.addAnalysis'),
          value: 'analysisEdit',
          noCache: true,
          data: { _id: newId }
        };
      });
    }
  }

  // Clean up URL after processing
  router.replace('/issue#analysis');
};

/**
 * Handles storage key change events from the browser tab component.
 */
const handleStorageKeyChange = () => {
  initializeComponent();
};

/**
 * Handles successful analysis edit operations.
 */
const handleAnalysisEditSuccess = () => {
  refreshListNotify.value += 1;
};

// Lifecycle Hooks
onMounted(() => {
  // Watch for hash changes to handle navigation
  watch(() => route.hash, (newHash) => {
    if (!newHash.startsWith('#list')) {
      return;
    }
    processUrlHash(newHash);
  });
});

// Provide/Inject Setup
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
          :refreshNotify="refreshListNotify"
          :onShow="activeTabKey === 'analysisList'" />
      </template>
      <template v-if="record.value==='analysisEdit'">
        <AnalysisEdit
          :data="record.data"
          :userInfo="props.userInfo"
          :projectId="props.projectId"
          @ok="handleAnalysisEditSuccess" />
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
