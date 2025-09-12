<script setup lang="ts">
import { computed, defineAsyncComponent, onMounted, provide, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useI18n } from 'vue-i18n';
import { BrowserTab } from '@xcan-angus/vue-ui';
import { IPane, utils } from '@xcan-angus/infra';
import { BasicProps } from '@/types/types';

/**
 * <p>
 * Component props with default values for project information and user context.
 * </p>
 */
const props = withDefaults(defineProps<BasicProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined
});

/**
 * <p>
 * Lazy-loaded components for different sprint views to improve initial load performance.
 * </p>
 */
const SprintList = defineAsyncComponent(() => import('@/views/task/sprint/list/index.vue'));
const SprintDetail = defineAsyncComponent(() => import('@/views/task/sprint/detail/index.vue'));
const SprintEdit = defineAsyncComponent(() => import('@/views/task/sprint/edit/index.vue'));

/**
 * <p>
 * Router and internationalization setup for navigation and localization.
 * </p>
 */
const route = useRoute();
const router = useRouter();
const { t } = useI18n();

/**
 * <p>
 * Reference to the browser tab component for managing tab operations.
 * </p>
 */
const browserTabRef = ref();

/**
 * <p>
 * Adds a new tab pane to the browser tab component.
 * </p>
 * @param tabPaneData - The tab pane data to add
 */
const addTabPane = (tabPaneData: IPane) => {
  browserTabRef.value.add(() => {
    return tabPaneData;
  });
};

/**
 * <p>
 * Retrieves tab pane data by key from the browser tab component.
 * </p>
 * @param tabKey - The key to identify the tab pane
 * @returns The tab pane data or undefined if not found
 */
const getTabPane = (tabKey: string): IPane[] | undefined => {
  return browserTabRef.value.getData(tabKey);
};

/**
 * <p>
 * Removes one or more tab panes from the browser tab component.
 * </p>
 * @param tabKeys - Array of keys identifying tabs to remove
 */
const deleteTabPane = (tabKeys: string[]) => {
  browserTabRef.value.remove(tabKeys);
};

/**
 * <p>
 * Updates an existing tab pane with new data.
 * </p>
 * @param tabPaneData - The updated tab pane data
 */
const updateTabPane = (tabPaneData: IPane) => {
  browserTabRef.value.update(tabPaneData);
};

/**
 * <p>
 * Replaces a tab pane with new data while maintaining the same key.
 * </p>
 * @param tabKey - The key of the tab to replace
 * @param newTabData - The new tab data with key property
 */
const replaceTabPane = (tabKey: string, newTabData: { key: string }) => {
  browserTabRef.value.replace(tabKey, newTabData);
};

/**
 * <p>
 * Initializes the component by setting up the default sprint list tab and processing URL hash.
 * </p>
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
 * <p>
 * Handles URL hash changes to determine which tab to open based on query parameters.
 * </p>
 * <p>
 * Supports three modes:
 * - Edit mode: Opens sprint edit tab with specific sprint ID
 * - Detail mode: Opens sprint detail tab with specific sprint ID
 * - Create mode: Opens new sprint edit tab
 * </p>
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
 * <p>
 * Handles storage key changes by reinitializing the component.
 * </p>
 */
const handleStorageKeyChange = () => {
  initializeComponent();
};

/**
 * <p>
 * Sets up hash change watcher on component mount to handle navigation.
 * </p>
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
 * <p>
 * Computed storage key for persisting tab state based on project ID.
 * </p>
 */
const storageKey = computed(() => {
  if (!props.projectId) {
    return undefined;
  }

  return `sprint${props.projectId}`;
});

/**
 * <p>
 * Provides tab management functions to child components for cross-component tab operations.
 * </p>
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
