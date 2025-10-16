<script setup lang="ts">
import {
  computed,
  defineAsyncComponent,
  inject,
  nextTick,
  onBeforeUnmount,
  onMounted,
  provide,
  reactive,
  ref,
  watch,
  type Ref as VueRef
} from 'vue';
import ReconnectingWebSocket from 'reconnecting-websocket';
import { utils, appContext, IPane } from '@xcan-angus/infra';
import { BrowserTab } from '@xcan-angus/vue-ui';
import { useRoute, useRouter } from 'vue-router';
import { useI18n } from 'vue-i18n';
import { ApiMenuKey } from '@/views/apis/menu';

import { setting } from '@/api/gm';
import { ProjectInfo } from '@/layout/types';

// WebSocket connection configuration
const WS_CONFIG = {
  maxRetries: 3,
  maxReconnectionDelay: 30000,
  minReconnectionDelay: 30000,
  connectionTimeout: 60000
} as const;

// WebSocket ready states
const WS_READY_STATES = {
  CONNECTING: 0,
  OPEN: 1,
  CLOSING: 2,
  CLOSED: 3,
  DISCONNECTED: 4
} as const;

// Lazy load components for better performance
const Sidebar = defineAsyncComponent(() => import('@/views/apis/services/sidebar/index.vue'));
const ApiGroup = defineAsyncComponent(() => import('@/views/apis/services/grouping/index.vue'));
const ApiItem = defineAsyncComponent(() => import('@/views/apis/services/apis/http/index.vue'));
const servicesMock = defineAsyncComponent(() => import('@/views/apis/services/mock/MockService.vue'));
const apisSocket = defineAsyncComponent(() => import('@/views/apis/services/apis/websocket/index.vue'));
const Auth = defineAsyncComponent(() => import('@/views/apis/services/auth/index.vue'));
const DataModel = defineAsyncComponent(() => import('@/views/apis/services/model/index.vue'));
const SecurityTestResult = defineAsyncComponent(() => import('@/views/apis/services/test/SecurityTestResult.vue'));
const SmokeTestResult = defineAsyncComponent(() => import('@/views/apis/services/test/SmokeTestResult.vue'));
const QuickStarted = defineAsyncComponent(() => import('@/views/apis/services/QuickStarted.vue'));

// Composables
const { t } = useI18n();
const route = useRoute();
const router = useRouter();

// Template refs
const sidebarRef = ref<InstanceType<typeof Sidebar>>();
const tabRef = ref<any>(); // BrowserTab component ref

// WebSocket related state
const ws = ref<ReconnectingWebSocket>();
const currentProxyUrl = ref<string>();
const currentProxy = ref<string>();
const readyState = ref<number>(WS_READY_STATES.CLOSED);
const uuid = ref<string>('');
const responseData = ref<string>('');
const responseCount = ref<number>(0);

// User and project context
const userInfo = ref(appContext.getUser());
const projectInfo = inject<VueRef<ProjectInfo>>('projectInfo', ref({} as ProjectInfo));
const appInfo = ref(appContext.getAccessApp());

// Computed properties
const projectId = computed(() => projectInfo.value?.id);

/**
 * Creates a new WebSocket connection with reconnection capabilities
 */
const createWebSocket = (): void => {
  if (!currentProxyUrl.value) return;

  ws.value = new ReconnectingWebSocket(currentProxyUrl.value, [], WS_CONFIG);

  ws.value.addEventListener('open', () => {
    readyState.value = ws.value?.readyState ?? WS_READY_STATES.OPEN;
  });

  ws.value.addEventListener('message', (event: MessageEvent) => {
    try {
      const response = JSON.parse(event.data);
      // Extract request ID from different possible fields
      uuid.value = response?.requestId || response?.clientId || '';
      responseData.value = event.data;
      responseCount.value += 1;
    } catch (error) {
      console.warn('Failed to parse WebSocket message:', error);
    }
  });

  ws.value.addEventListener('error', () => {
    readyState.value = ws.value?.readyState ?? WS_READY_STATES.CLOSED;
  });

  ws.value.addEventListener('close', () => {
    readyState.value = ws.value?.readyState ?? WS_READY_STATES.CLOSED;
  });
};

/**
 * Updates WebSocket connection based on network status and proxy configuration
 */
const updateWs = (): void => {
  const isOnline = navigator.onLine;

  // Close existing connection if online
  if (isOnline && ws.value?.close) {
    ws.value.close(1000);
  }

  if (isOnline) {
    if (currentProxyUrl.value) {
      ws.value = undefined;
      createWebSocket();
    } else if (currentProxy.value === 'NO_PROXY') {
      ws.value = undefined;
    } else {
      ws.value = { readyState: WS_READY_STATES.DISCONNECTED } as any;
    }
  } else {
    // Offline state
    if (currentProxyUrl.value) {
      ws.value = { readyState: WS_READY_STATES.DISCONNECTED } as any;
    } else {
      ws.value = undefined;
    }
  }
};

/**
 * Loads and configures proxy URL from user settings
 */
const loadProxyUrl = async (): Promise<void> => {
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
  await loadProxyUrl();

  // Watch for proxy changes and update WebSocket connection
  watch([() => currentProxyUrl.value, () => currentProxy.value], ([newValue]) => {
    if (ws.value?.close) {
      ws.value.close(1000);
    }

    if (newValue) {
      ws.value = undefined;
      createWebSocket();
    } else if (currentProxy.value === 'NO_PROXY') {
      ws.value = undefined;
    } else {
      ws.value = { readyState: WS_READY_STATES.DISCONNECTED } as any;
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
  if (ws.value?.close) {
    ws.value.close(1000);
  }

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
        <QuickStarted class="p-5" />
      </template>

      <!-- Dynamic tab content based on record type -->
      <template #default="record">
        <!-- API Group Management -->
        <template v-if="record.value === 'group'">
          <ApiGroup :serviceId="record.id" :info="record" />
        </template>

        <!-- HTTP API Testing -->
        <template v-if="record.value === 'API'">
          <ApiItem
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

        <!-- Authentication Management -->
        <template v-if="record.value === 'auth'">
          <Auth :appId="appInfo?.id?.toString()" />
        </template>

        <!-- Mock Service Management -->
        <template v-if="record.value === 'mock'">
          <servicesMock :id="record.id" />
        </template>

        <!-- WebSocket API Testing -->
        <template v-if="record.value === 'socket'">
          <apisSocket
            :id="record.id"
            :pid="record._id"
            :ws="ws as any"
            :uuid="uuid"
            :valueObj="record"
            :name="record.name"
            :responseCount="responseCount"
            :response="responseData" />
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
