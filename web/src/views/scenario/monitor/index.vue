<script setup lang="ts">
import { computed, defineAsyncComponent, onMounted, provide, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRoute, useRouter } from 'vue-router';
import { BrowserTab } from '@xcan-angus/vue-ui';
import { utils } from '@xcan-angus/infra';

import { IPane } from './types';

// ==================== TYPES & INTERFACES ====================
type Props = {
  projectId: string;
  userInfo: { id: string; };
  appInfo: { id: string; };
}

// ==================== PROPS & REACTIVE DATA ====================
const { t } = useI18n();

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined
});

// ==================== ASYNC COMPONENTS ====================
const MonitorList = defineAsyncComponent(() => import('@/views/scenario/monitor/list/index.vue'));
const MonitorDetail = defineAsyncComponent(() => import('@/views/scenario/monitor/detail/index.vue'));
const MonitorEdit = defineAsyncComponent(() => import('@/views/scenario/monitor/edit/index.vue'));

// ==================== ROUTER & REFS ====================
const route = useRoute();
const router = useRouter();
const browserTabRef = ref();
const activeTabKey = ref();

// ==================== COMPUTED PROPERTIES ====================
/**
 * Generate storage key for browser tab persistence based on project ID
 * @returns Storage key string or undefined if no project ID
 */
const storageKey = computed(() => {
  if (!props.projectId) {
    return undefined;
  }
  return `monitor${props.projectId}`;
});

// ==================== TAB MANAGEMENT FUNCTIONS ====================
/**
 * Add a new tab pane to the browser tab component
 * @param tabData - Tab pane data to add
 */
const addTabPane = (tabData: IPane) => {
  browserTabRef.value.add(() => {
    return tabData;
  });
};

/**
 * Get tab pane data by key
 * @param key - Tab key to retrieve
 * @returns Tab pane data or undefined
 */
const getTabPane = (key: string): IPane[] | undefined => {
  return browserTabRef.value.getData(key);
};

/**
 * Delete tab panes by keys
 * @param keys - Array of tab keys to delete
 */
const deleteTabPane = (keys: string[]) => {
  browserTabRef.value.remove(keys);
};

/**
 * Update existing tab pane data
 * @param tabData - Updated tab pane data
 */
const updateTabPane = (tabData: IPane) => {
  browserTabRef.value.update(tabData);
};

/**
 * Replace tab pane with new data
 * @param key - Tab key to replace
 * @param newData - New tab data with key property
 */
const replaceTabPane = (key: string, newData: { key: string }) => {
  browserTabRef.value.replace(key, newData);
};

// ==================== INITIALIZATION FUNCTIONS ====================
/**
 * Initialize the monitor component by adding default tab and processing hash
 */
const initializeComponent = () => {
  // Add default monitor list tab if not already present
  if (typeof browserTabRef.value?.add === 'function') {
    browserTabRef.value.add((existingTabIds: string[]) => {
      if (!existingTabIds.includes('monitorList')) {
        return {
          _id: 'monitorList',
          value: 'monitorList',
          name: t('scenarioMonitor.monitor'),
          closable: false
        };
      }
    });
  }

  // Process current hash for tab navigation
  processHashChange(route.hash);
};

/**
 * Handle storage key change by reinitializing component
 */
const handleStorageKeyChange = () => {
  initializeComponent();
};

// ==================== HASH PROCESSING FUNCTIONS ====================
/**
 * Process hash change to determine which tab to open
 * @param hash - URL hash string containing query parameters
 */
const processHashChange = (hash: string) => {
  // Extract query string from hash
  const queryString = hash.split('?')[1];
  if (!queryString) {
    return;
  }

  // Parse query parameters into object
  const queryParameters = queryString.split('&').reduce((accumulator, current) => {
    const [key, value] = current.split('=');
    accumulator[key] = value;
    return accumulator;
  }, {} as { [key: string]: string });

  const { id, type } = queryParameters;

  // Handle tab creation based on query parameters
  if (id) {
    createTabForExistingMonitor(id, type);
  } else if (type) {
    createTabForNewMonitor();
  }

  // Update router to clean URL
  router.replace('/scenario#monitor');
};

/**
 * Create tab for existing monitor (edit or detail view)
 * @param monitorId - ID of the monitor
 * @param tabType - Type of tab ('edit' or 'detail')
 */
const createTabForExistingMonitor = (monitorId: string, tabType: string) => {
  if (tabType === 'edit') {
    browserTabRef.value.add(() => {
      return {
        _id: monitorId,
        value: 'monitorEdit',
        noCache: true,
        data: { _id: monitorId, id: monitorId }
      };
    });
  } else {
    browserTabRef.value.add(() => {
      return {
        _id: `${monitorId}-case`,
        id: monitorId,
        value: 'monitorDetails',
        data: { _id: monitorId, id: monitorId }
      };
    });
  }
};

/**
 * Create tab for new monitor (edit view)
 */
const createTabForNewMonitor = () => {
  browserTabRef.value.add(() => {
    const newMonitorId = utils.uuid();
    return {
      _id: newMonitorId,
      name: t('scenarioMonitor.addMonitor'),
      value: 'monitorEdit',
      noCache: true,
      data: { _id: newMonitorId }
    };
  });
};

// ==================== LIFECYCLE HOOKS ====================
onMounted(() => {
  // Watch for hash changes to handle tab navigation
  watch(() => route.hash, (newHash) => {
    if (!newHash.startsWith('#monitor')) {
      return;
    }
    processHashChange(newHash);
  });
});

// ==================== PROVIDE INJECTION ====================
// Provide tab management functions to child components
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
    :userId="props.userInfo.id"
    :storageKey="storageKey"
    @storageKeyChange="handleStorageKeyChange">
    <template #default="tabRecord">
      <!-- Monitor List Tab -->
      <template v-if="tabRecord.value === 'monitorList'">
        <MonitorList
          v-bind="tabRecord"
          :userInfo="props.userInfo"
          :appInfo="props.appInfo"
          :projectId="props.projectId"
          :onShow="activeTabKey === 'monitorList'" />
      </template>

      <!-- Monitor Detail Tab -->
      <template v-else-if="tabRecord.value === 'monitorDetails'">
        <MonitorDetail
          v-bind="tabRecord"
          :userInfo="props.userInfo"
          :appInfo="props.appInfo"
          :projectId="props.projectId" />
      </template>

      <!-- Monitor Edit Tab -->
      <template v-else-if="tabRecord.value === 'monitorEdit'">
        <MonitorEdit
          v-bind="tabRecord"
          :userInfo="props.userInfo"
          :appInfo="props.appInfo"
          :projectId="props.projectId" />
      </template>
    </template>
  </BrowserTab>
</template>
