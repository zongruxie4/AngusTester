<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, provide, ref } from 'vue';
import { ConfigProvider, Denied, Header, NetworkError, NotFound } from '@xcan-angus/vue-ui';
import { utils, duration, appContext } from '@xcan-angus/infra';
import { debounce } from 'throttle-debounce';
import { mock } from '@/api/tester';
import GlobalConstantConfig from '@/globalConstant';
import { ai } from '@/api/gm';

import store from './store';

// Import global styles
import '@/assets/styles/global.css';

/** AI Agent configuration interface */
interface AIAgentConfig {
  agentId: string;
  chatIframe: string;
  enabled: boolean;
  extensions: Record<string, unknown>;
  provider: string;
}

/** Function item interface */
interface FunctionItem {
  id: string;
  name: string;
  [key: string]: unknown;
}

/** Global configuration type */
type GlobalConfig = Record<string, string | number | boolean | object>;

/** Window resize notification UUID for triggering reactive updates */
const windowResizeNotify = ref<string>();

/** AI agent configuration */
const aiAgent = ref<AIAgentConfig>();

/** All available functions list */
const allFunction = ref<FunctionItem[]>([]);

/** Global application configurations */
const globalConfigs = ref<GlobalConfig>();

/** Function loading promise to prevent duplicate requests */
let functionPromise: Promise<FunctionItem[]> | null = null;

/** Current application status code */
const status = computed(() => store.state.statusCode);

/** Whether AI features are enabled */
const aiEnabled = computed(() => Boolean(aiAgent.value?.enabled));

/** Debounced window resize handler */
const resizeHandler = debounce(duration.resize, () => {
  windowResizeNotify.value = utils.uuid();
});

/**
 * Get all available functions with caching
 * Prevents duplicate API calls by using a promise cache
 */
const getAllFunctions = async (): Promise<void> => {
  // Return early if functions already loaded or request in progress
  if (allFunction.value.length > 0 || functionPromise) {
    return;
  }

  try {
    functionPromise = mock.getAllFunctions();
    const result = await functionPromise;
    if (result && result.length > 0) {
      allFunction.value = result;
    }
  } catch (error) {
    console.error('Failed to fetch functions:', error);
  } finally {
    functionPromise = null;
  }
};

/**
 * Initialize application configurations and settings
 */
const initializeApp = async (): Promise<void> => {
  try {
    // Setup window resize listener
    window.addEventListener('resize', resizeHandler);

    // Load global configurations
    const envContent = appContext.getContext().env;
    globalConfigs.value = { ...envContent, ...GlobalConstantConfig };

    // Load AI agent settings
    aiAgent.value = await ai.getAIAgentSetting();
  } catch (error) {
    console.error('Failed to initialize app:', error);
  }
};

onMounted(() => {
  initializeApp();
});

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeHandler);
});

// Provide reactive dependencies to child components
provide('windowResizeNotify', windowResizeNotify);
provide('globalConfigs', globalConfigs);
provide('allFunction', allFunction);
provide('getAllFunctions', getAllFunctions);
provide('aiEnabled', aiEnabled);
provide('aiAgent', aiAgent);
</script>

<template>
  <ConfigProvider>
    <!-- Main application content when status is OK -->
    <template v-if="status === 200">
      <RouterView />
    </template>

    <!-- Error states with header and centered content -->
    <template v-else>
      <Header
        :menus="appContext.getAccessAppFuncTree() || []"
        :codeMap="appContext.getAccessAppFuncCodeMap()" />

      <div class="h-[calc(100%-54px)] overflow-auto flex justify-center items-center">
        <!-- Access denied -->
        <Denied v-if="status === 403 || status === 405" />

        <!-- Page not found -->
        <NotFound v-else-if="status === 404" />

        <!-- Server error -->
        <NetworkError v-else-if="status === 500" />
      </div>
    </template>
  </ConfigProvider>
</template>

<style scoped>
/* Component-specific styles only */
/* Global styles are imported from global.css */
</style>
