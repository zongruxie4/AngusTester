<script setup lang="ts">
import {
  computed, defineAsyncComponent, inject, nextTick, onBeforeUnmount, onMounted,
  provide, reactive, ref, watch, type Ref as VueRef
} from 'vue';
import { utils, appContext, IPane } from '@xcan-angus/infra';
import { BrowserTab } from '@xcan-angus/vue-ui';
import { useRoute, useRouter } from 'vue-router';
import { useI18n } from 'vue-i18n';
import { ApiMenuKey } from '@/views/apis/menu';

import { setting } from '@/api/gm';
import { ProjectInfo } from '@/layout/types';
import { createAngusWebSocketProxy, type WebSocketEventHandlers } from '@/utils/apis/angusProxy';

// Lazy load components for better performance
const Sidebar = defineAsyncComponent(() => import('@/views/apis/services/sidebar/index.vue'));
const ApiGrouping = defineAsyncComponent(() => import('@/views/apis/services/grouping/index.vue'));
const HttpApi = defineAsyncComponent(() => import('@/views/apis/services/apis/http/index.vue'));
const WebSocketApi = defineAsyncComponent(() => import('@/views/apis/services/apis/websocket/index.vue'));
const MockService = defineAsyncComponent(() => import('@/views/apis/services/mock/MockService.vue'));
const Auth = defineAsyncComponent(() => import('@/views/apis/services/auth/index.vue'));
const DataModel = defineAsyncComponent(() => import('@/views/apis/services/model/index.vue'));
const SecurityTestResult = defineAsyncComponent(() => import('@/views/apis/services/test/SecurityTestResult.vue'));
const SmokeTestResult = defineAsyncComponent(() => import('@/views/apis/services/test/SmokeTestResult.vue'));
const DefaultQuickStarted = defineAsyncComponent(() => import('@/views/apis/services/grouping/home/DefaultQuickStarted.vue'));

// Composables
const { t } = useI18n();
const route = useRoute();
const router = useRouter();

// Template refs
const sidebarRef = ref<InstanceType<typeof Sidebar>>();
const tabRef = ref<any>(); // BrowserTab component ref

// WebSocket related state
const angusProxy = createAngusWebSocketProxy();
const currentProxyUrl = ref<string>();
const currentProxy = ref<string>();
const responseData = ref<string>('');

// Computed properties for WebSocket state
const ws = computed(() => angusProxy.getWebSocket());
const readyState = computed(() => angusProxy.getReadyState());
const uuid = computed(() => angusProxy.getCurrentUuid());
const responseCount = computed(() => angusProxy.getResponseCount());

// User and project context
const userInfo = ref(appContext.getUser());
const projectInfo = inject<VueRef<ProjectInfo>>('projectInfo', ref({} as ProjectInfo));
const appInfo = ref(appContext.getAccessApp());

// Computed properties
const projectId = computed(() => projectInfo.value?.id);

/**
 * WebSocket event handlers
 */
const wsEventHandlers: WebSocketEventHandlers = {
  onMessage: (data: string) => {
    responseData.value = data;
  }
};

/**
 * Updates WebSocket connection based on network status and proxy configuration
 */
const updateWs = (): void => {
  const isOnline = navigator.onLine;
  angusProxy.updateConnection(isOnline, currentProxyUrl.value, currentProxy.value);
};

/**
 * Loads URL and configures proxy connection from user settings
 */
const setupProxyConnection = async (): Promise<void> => {
  const [error, { data }] = await setting.getUserApiProxy();
  if (error) {
    console.error('Failed to load proxy configuration:', error);
    return;
  }

  // Find the first enabled proxy configuration
  const enabledProxy = Object.values(data).find((proxy: any) => proxy.enabled);
  if (enabledProxy) {
    currentProxyUrl.value = (enabledProxy as any).url;
    currentProxy.value = (enabledProxy as any).name.value;

    // Connect WebSocket with the new proxy URL
    if (currentProxyUrl.value) {
      angusProxy.connect(currentProxyUrl.value, wsEventHandlers);
    }
  }
};

// Storage key for persisting tab state in localStorage
const STORAGE_KEY = computed<string>(() =>
  `api_browser_tab_${projectInfo.value?.id || ''}`
);

/**
 * Handler for adding new API tabs
 */
const addHandler = (): void => {
  if (typeof tabRef.value?.add === 'function') {
    tabRef.value.add(() => {
      const key = utils.uuid('api');
      return {
        _id: key,
        pid: key,
        name: t('service.home.addApiTabName'),
        value: 'API',
        closable: true,
        forceRender: false,
        unarchived: true
      };
    });
  }
};

/**
 * Adds a new tab pane to the browser tab component
 */
const addTabPane = (data: IPane): void => {
  router.replace(`/apis#${ApiMenuKey.SERVICES}`);
  nextTick(() => {
    if (typeof tabRef.value?.add === 'function' && projectInfo.value.id) {
      tabRef.value.add(() => data);
    } else {
      // Store in session storage if tab component is not ready
      sessionStorage.setItem('addTabPane', JSON.stringify(data));
    }
  });
};

/**
 * Removes tab panes by their keys
 */
const deleteTabPane = (keys: string[]): void => {
  if (typeof tabRef.value?.remove === 'function') {
    tabRef.value.remove(keys);
  }
};

// Project info for updating project list
const updateProjectInfo = reactive({
  id: '',
  name: ''
});

/**
 * Updates an existing tab pane
 */
const updateTabPane = (data: IPane): void => {
  if (typeof tabRef.value?.update === 'function') {
    tabRef.value.update(data);
  }

  // Update project info if it's a group tab
  if (data.pid.includes('group')) {
    updateProjectInfo.id = data.pid.replace('group', '');
    if (data.name) {
      updateProjectInfo.name = data.name;
    }
  }
};

/**
 * Replaces a tab pane with new data
 */
const replaceTabPane = (key: string, data: { key: string }): void => {
  if (typeof tabRef.value?.replace === 'function') {
    tabRef.value.replace(key, data);
  }
};

/**
 * Handles URL query parameters to restore tabs from deep links
 */
const handleUrlQueryParams = (): void => {
  const fullPath = decodeURI(route.fullPath);
  const queryStr = fullPath.split('?')[1];

  if (!queryStr) return;

  const queryParams = new URLSearchParams(queryStr);
  const result = Object.fromEntries(queryParams.entries());

  if (['API', 'group'].includes(result.value) && result.id && result.name) {
    addTabPane({
      _id: result.id + result.value,
      id: result.id,
      name: result.name,
      value: result.value,
      closable: true,
      shouldCheckId: result.value === 'group'
    });
  }
};

/**
 * Refreshes the sidebar project/service list
 */
const refreshSidebar = (): void => {
  if (typeof sidebarRef.value?.refresh === 'function') {
    sidebarRef.value.refresh();
  }
};

/**
 * Refreshes the unarchived list in the sidebar
 */
const refreshUnarchived = (): void => {
  if (typeof sidebarRef.value?.refreshUnarchived === 'function') {
    sidebarRef.value.refreshUnarchived();
  }
};

// API group reload configuration for triggering updates
const apiGroupReloadConfig = reactive({
  reloadId: '',
  reloadKey: 0
});

/**
 * Updates API group configuration to trigger reload
 */
const updateApiGroup = (projectId: string): void => {
  apiGroupReloadConfig.reloadId = projectId;
  apiGroupReloadConfig.reloadKey += 1;
};

// Watch for tab component readiness and restore pending tab data
watch(() => tabRef.value, () => {
  if (tabRef.value) {
    nextTick(() => {
      const addTabData = sessionStorage.getItem('addTabPane');
      if (addTabData) {
        addTabPane(JSON.parse(addTabData));
        sessionStorage.removeItem('addTabPane');
      }
    });
  }
});

/**
 * Component mounted lifecycle hook
 */
onMounted(async () => {
  // Load proxy configuration
  await setupProxyConnection();

  // Watch for proxy changes and update WebSocket connection
  watch([() => currentProxyUrl.value, () => currentProxy.value], ([newValue]) => {
    if (newValue) {
      angusProxy.connect(newValue, wsEventHandlers);
    } else if (currentProxy.value === 'NO_PROXY') {
      angusProxy.disconnect();
    } else {
      angusProxy.updateConnection(navigator.onLine, currentProxyUrl.value, currentProxy.value);
    }
  }, { immediate: true });

  // Listen for network connection changes
  if ((navigator as any).connection) {
    (navigator as any).connection.addEventListener('change', updateWs);
  }

  // Handle URL query parameters for tab restoration
  handleUrlQueryParams();
});

/**
 * Component cleanup before unmounting
 */
onBeforeUnmount(() => {
  // Close WebSocket connection
  angusProxy.disconnect();

  // Remove network connection listener
  if ((navigator as any).connection) {
    (navigator as any).connection.removeEventListener('change', updateWs);
  }
});

// Provide functions and state to child components
provide('refreshSidebar', refreshSidebar);
provide('refreshUnarchived', refreshUnarchived);
provide('addTabPane', addTabPane);
provide('deleteTabPane', deleteTabPane);
provide('updateTabPane', updateTabPane);
provide('replaceTabPane', replaceTabPane);

provide('updateApiGroup', updateApiGroup);
provide('readyState', readyState);
provide('currentProxyUrl', currentProxyUrl);
provide('currentProxy', currentProxy);
provide('updateProjectInfo', updateProjectInfo);
provide('updateInterface', apiGroupReloadConfig);

provide('WS', ws);
provide('requestId', uuid);
provide('responseData', responseData);

// Expose methods for parent component access
defineExpose({
  addTabPane,
  deleteTabPane,
  updateTabPane,
  replaceTabPane,
  updateApiGroup,
  refreshSidebar,
  refreshUnarchived
});

// TODO: Refactor all usage locations
provide('updateHosts', reactive({
  reloadId: '',
  reloadKey: 0
}));
</script>
<template>
  <div class="flex-1 flex h-full border-l min-w-0">
    <!-- Sidebar for project/service navigation -->
    <Sidebar ref="sidebarRef" />

    <!-- Main content area with tabbed interface -->
    <BrowserTab
      v-if="projectId"
      ref="tabRef"
      :key="STORAGE_KEY"
      class="flex-1"
      :storageKey="STORAGE_KEY"
      @add="addHandler">
      <!-- Empty state when no tabs are open -->
      <template #empty>
        <DefaultQuickStarted class="p-5" />
      </template>

      <!-- Dynamic tab content based on record type -->
      <template #default="record">
        <!-- API Group Management -->
        <template v-if="record.value === 'group'">
          <ApiGrouping :serviceId="record.id" :info="record" />
        </template>

        <!-- HTTP API -->
        <template v-if="record.value === 'API'">
          <HttpApi
            :id="record.id"
            :valueObj="record"
            :ws="ws as any"
            :uuid="uuid"
            :response="responseData"
            :pid="record._id"
            :userInfo="userInfo as any"
            :appInfo="appInfo as any"
            :projectId="projectId?.toString()" />
        </template>

        <!-- WebSocket API -->
        <template v-if="record.value === 'socket'">
          <WebSocketApi
            :id="record.id"
            :pid="record._id"
            :ws="ws as any"
            :uuid="uuid"
            :valueObj="record"
            :name="record.name"
            :responseCount="responseCount"
            :response="responseData" />
        </template>

        <!-- Authentication Management -->
        <template v-if="record.value === 'auth'">
          <Auth :appId="appInfo?.id?.toString()" />
        </template>

        <!-- Mock Service Management -->
        <template v-if="record.value === 'mock'">
          <MockService :id="record.id" />
        </template>

        <!-- Data Model Management -->
        <template v-if="record.value === 'model'">
          <DataModel
            :id="record.id"
            :pid="record._id"
            :type="record.type"
            :name="record.name"
            :data="record.data" />
        </template>

        <!-- Security Test Results -->
        <template v-if="record.value === 'auth_test'">
          <SecurityTestResult />
        </template>

        <!-- Smoke Test Results -->
        <template v-if="record.value === 'smoke_test'">
          <SmokeTestResult />
        </template>
      </template>
    </BrowserTab>
  </div>
</template>
