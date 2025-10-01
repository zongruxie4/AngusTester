<script setup lang="ts">
import { computed, defineAsyncComponent, onMounted, provide, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useI18n } from 'vue-i18n';
import { BrowserTab } from '@xcan-angus/vue-ui';
import { IPane } from '@xcan-angus/infra';
import { BasicProps } from '@/types/types';

const props = withDefaults(defineProps<BasicProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined
});

// ASYNC COMPONENTS
const TaskList = defineAsyncComponent(() => import('@/views/issue/issue/list/index.vue'));
const TaskDetails = defineAsyncComponent(() => import('@/views/issue/issue/detail/index.vue'));

// COMPOSABLES
const route = useRoute();
const router = useRouter();
const { t } = useI18n();

// REACTIVE STATE
const browserTabRef = ref();

const currentSprintId = ref<string>();
const currentSprintName = ref<string>();
const currentTaskId = ref<string>();

/**
 * Generates storage key for browser tab persistence
 * @returns Storage key string or undefined if no projectId
 */
const storageKey = computed(() => {
  if (!props.projectId) {
    return undefined;
  }
  return `task${props.projectId}`;
});

/**
 * Adds a new tab pane to the browser tab component
 * @param tabPaneData - Tab pane configuration data
 */
const addTabPane = (tabPaneData: IPane) => {
  browserTabRef.value.add(() => {
    return tabPaneData;
  });
};

/**
 * Retrieves tab pane data by key
 * @param key - Tab pane identifier key
 * @returns Tab pane data array or undefined
 */
const getTabPane = (key: string): IPane[] | undefined => {
  return browserTabRef.value.getData(key);
};

/**
 * Removes tab panes by their keys
 * @param keys - Array of tab pane keys to remove
 */
const deleteTabPane = (keys: string[]) => {
  browserTabRef.value.remove(keys);
};

/**
 * Updates an existing tab pane with new data
 * @param tabPaneData - Updated tab pane data
 */
const updateTabPane = (tabPaneData: IPane) => {
  browserTabRef.value.update(tabPaneData);
};

/**
 * Replaces a tab pane with new data
 * @param key - Tab pane key to replace
 * @param tabPaneData - New tab pane data
 */
const replaceTabPane = (key: string, tabPaneData: { key: string }) => {
  browserTabRef.value.replace(key, tabPaneData);
};

/**
 * Initializes the task module by setting up default tab and processing hash
 */
const initializeTaskModule = () => {
  if (typeof browserTabRef.value?.add === 'function') {
    browserTabRef.value.add((existingTabIds: string[]) => {
      if (!existingTabIds.includes('taskList')) {
        return {
          _id: 'taskList',
          value: 'taskList',
          name: t('task.title'),
          closable: false
        };
      }
    });
  }

  processRouteHash(route.hash);
};

/**
 * Processes route hash to determine which tab to open
 * @param hash - URL hash string containing query parameters
 */
const processRouteHash = (hash: string) => {
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

  const { sprintId: hashSprintId, sprintName: hashSprintName, taskId: hashTaskId } = queryParameters;
  currentSprintId.value = hashSprintId;
  currentSprintName.value = hashSprintName;
  currentTaskId.value = hashTaskId;

  if (hashSprintId) {
    // Open task list tab for sprint
    browserTabRef.value.add(() => {
      return {
        _id: 'taskList',
        value: 'taskList',
        name: t('task.title'),
        closable: false
      };
    });
  } else {
    if (hashTaskId) {
      // Open task details tab
      browserTabRef.value.add(() => {
        const cleanQuery = queryString.replace(/&taskId=[^&]+/, '');
        return {
          _id: hashTaskId,
          value: 'taskDetails',
          data: { id: hashTaskId, query: cleanQuery }
        };
      });
    }
  }

  // Clean up URL by removing hash parameters
  router.replace('/task#task');
};

/**
 * Handles storage key changes and reinitializes the module
 */
const handleStorageKeyChange = () => {
  initializeTaskModule();
};

/**
 * Component mounted lifecycle hook
 * Sets up route hash watcher for tab navigation
 */
onMounted(() => {
  watch(() => route.hash, (newHash) => {
    if (!newHash.startsWith('#task')) {
      return;
    }

    processRouteHash(newHash);
  });
});

// Provide tab pane management functions to child components
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
    <template #default="tabRecord">
      <template v-if="tabRecord.value === 'taskList'">
        <TaskList
          v-bind="tabRecord"
          :userInfo="props.userInfo"
          :appInfo="props.appInfo"
          :projectId="props.projectId"
          :projectName="props.projectName"
          :sprintId="currentSprintId"
          :sprintName="currentSprintName" />
      </template>

      <template v-else-if="tabRecord.value === 'taskDetails'">
        <TaskDetails
          v-bind="tabRecord"
          :userInfo="props.userInfo"
          :appInfo="props.appInfo"
          :projectId="props.projectId"
          :sprintId="currentSprintId"
          :sprintName="currentSprintName" />
      </template>
    </template>
  </BrowserTab>
</template>
<style scoped>
:deep(.ant-tree) {
  background-color: transparent;
  font-size: 12px;
}

:deep(.ant-tree .ant-tree-treenode) {
  width: 100%;
  height: 36px;
  padding-left: 0;
  line-height: 20px;
}

:deep(.ant-tree .ant-tree-treenode.ant-tree-treenode-selected) {
  background-color: var(--content-tabs-bg-selected);
}

:deep(.ant-tree .ant-tree-treenode:hover) {
  background-color: var(--content-tabs-bg-selected);
}

:deep(.ant-tree .ant-tree-switcher) {
  width: 16px;
  height: 34px;
  margin-top: 2px;
  line-height: 34px;
}

:deep(.ant-tree .ant-tree-node-content-wrapper:hover) {
  background-color: transparent;
}

:deep(.ant-tree .ant-tree-node-content-wrapper) {
  display: flex;
  flex: 1 1 0%;
  flex-direction: column;
  justify-content: center;
  height: 36px;
  margin: 0;
  padding: 0;
  line-height: 36px;
}

:deep(.ant-tree .ant-tree-node-content-wrapper .ant-tree-iconEle) {
  height: 36px;
  line-height: 36px;
  vertical-align: initial;
}

:deep(.ant-tree .ant-tree-node-selected) {
  background-color: transparent;
}
</style>
