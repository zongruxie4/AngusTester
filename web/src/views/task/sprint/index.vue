<script setup lang="ts">
import { computed, defineAsyncComponent, onMounted, provide, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useI18n } from 'vue-i18n';
import { BrowserTab } from '@xcan-angus/vue-ui';
import { IPane, utils } from '@xcan-angus/infra';
import { BasicProps } from '@/types/types';

/**
 * Component props with default values for project information and user context.
 */
const props = withDefaults(defineProps<BasicProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined
});

/**
 * Lazy-loaded components for different sprint views to improve initial load performance.
 */
const SprintList = defineAsyncComponent(() => import('@/views/task/sprint/list/index.vue'));
const SprintDetail = defineAsyncComponent(() => import('@/views/task/sprint/detail/index.vue'));
const SprintEdit = defineAsyncComponent(() => import('@/views/task/sprint/edit/index.vue'));

/**
 * Router and internationalization setup for navigation and localization.
 */
const route = useRoute();
const router = useRouter();
const { t } = useI18n();

/**
 * Reference to the browser tab component for managing tab operations.
 */
const browserTabRef = ref();

/**
 * Adds a new tab pane to the browser tab component.
 * @param tabPaneData - The tab pane data to add
 */
const addTabPane = (tabPaneData: IPane) => {
  browserTabRef.value.add(() => {
    return tabPaneData;
  });
};

/**
 * Retrieves tab pane data by key from the browser tab component.
 * @param tabKey - The key to identify the tab pane
 * @returns The tab pane data or undefined if not found
 */
const getTabPane = (tabKey: string): IPane[] | undefined => {
  return browserTabRef.value.getData(tabKey);
};

/**
 * Removes one or more tab panes from the browser tab component.
 * @param tabKeys - Array of keys identifying tabs to remove
 */
const deleteTabPane = (tabKeys: string[]) => {
  browserTabRef.value.remove(tabKeys);
};

/**
 * Updates an existing tab pane with new data.
 * @param tabPaneData - The updated tab pane data
 */
const updateTabPane = (tabPaneData: IPane) => {
  browserTabRef.value.update(tabPaneData);
};

/**
 * Replaces a tab pane with new data while maintaining the same key.
 * @param tabKey - The key of the tab to replace
 * @param newTabData - The new tab data with key property
 */
const replaceTabPane = (tabKey: string, newTabData: { key: string }) => {
  browserTabRef.value.replace(tabKey, newTabData);
};

/**
 * Initializes the component by setting up the default sprint list tab and processing URL hash.
 */
const initializeComponent = () => {
  // Add default sprint list tab if not already present
  if (typeof browserTabRef.value?.add === 'function') {
    browserTabRef.value.add((existingTabIds: string[]) => {
      if (!existingTabIds.includes('sprintList')) {
        return {
          _id: 'sprintList',
          value: 'sprintList',
          name: t('taskSprint.title'),
          closable: false
        };
      }
    });
  }

  // Process URL hash for navigation
  handleHashChange(route.hash);
};

/**
 * Handles URL hash changes to determine which tab to open based on query parameters.
 * Supports three modes:
 * - Edit mode: Opens sprint edit tab with specific sprint ID
 * - Detail mode: Opens sprint detail tab with specific sprint ID
 * - Create mode: Opens new sprint edit tab
 * @param urlHash - The URL hash containing query parameters
 */
const handleHashChange = (urlHash: string) => {
  // Extract query string from hash
  const queryString = urlHash.split('?')[1];
  if (!queryString) {
    return;
  }

  // Parse query parameters into object
  const queryParameters = queryString.split('&').reduce((accumulator, current) => {
    const [key, value] = current.split('=');
    accumulator[key] = value;
    return accumulator;
  }, {} as { [key: string]: string });

  const { id: sprintId, type: viewType } = queryParameters;

  if (sprintId) {
    // Handle existing sprint (edit or detail view)
    if (viewType === 'edit') {
      browserTabRef.value.add(() => {
        return {
          _id: sprintId,
          value: 'sprintEdit',
          noCache: true,
          data: { _id: sprintId, id: sprintId }
        };
      });
    } else {
      // Default to detail view
      browserTabRef.value.add(() => {
        return {
          _id: sprintId + '-detail',
          value: 'sprintDetails',
          data: { _id: sprintId, id: sprintId }
        };
      });
    }
  } else if (viewType) {
    // Handle new sprint creation
    browserTabRef.value.add(() => {
      const newSprintId = utils.uuid();
      return {
        _id: newSprintId,
        name: t('taskSprint.addSprint'),
        value: 'sprintEdit',
        noCache: true,
        data: { _id: newSprintId }
      };
    });
  }

  // Clean up URL by removing query parameters
  router.replace('/task#sprint');
};

/**
 * Handles storage key changes by reinitializing the component.
 */
const handleStorageKeyChange = () => {
  initializeComponent();
};

/**
 * Sets up hash change watcher on component mount to handle navigation.
 */
onMounted(() => {
  watch(() => route.hash, (newHash) => {
    if (!newHash.startsWith('#sprint')) {
      return;
    }

    handleHashChange(newHash);
  });
});

/**
 * Computed storage key for persisting tab state based on project ID.
 */
const storageKey = computed(() => {
  if (!props.projectId) {
    return undefined;
  }

  return `sprint${props.projectId}`;
});

/**
 * Provides tab management functions to child components for cross-component tab operations.
 */
provide('addTabPane', addTabPane);
provide('getTabPane', getTabPane);
provide('deleteTabPane', deleteTabPane);
provide('updateTabPane', updateTabPane);
provide('replaceTabPane', replaceTabPane);
</script>

<template>
  <BrowserTab
    ref="browserTabRef"
    hideAdd
    class="h-full"
    :userId="props.userInfo?.id"
    :storageKey="storageKey"
    @storageKeyChange="handleStorageKeyChange">
    <template #default="record">
      <template v-if="record.value === 'sprintList'">
        <SprintList
          v-bind="record"
          :userInfo="props.userInfo"
          :appInfo="props.appInfo"
          :projectId="props.projectId" />
      </template>

      <template v-else-if="record.value === 'sprintDetails'">
        <SprintDetail
          v-bind="record"
          :userInfo="props.userInfo"
          :appInfo="props.appInfo"
          :projectId="props.projectId" />
      </template>

      <template v-else-if="record.value === 'sprintEdit'">
        <SprintEdit
          v-bind="record"
          :userInfo="props.userInfo"
          :appInfo="props.appInfo"
          :projectId="props.projectId" />
      </template>
    </template>
  </BrowserTab>
</template>
