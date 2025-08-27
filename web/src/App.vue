<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, provide, ref } from 'vue';
import { ConfigProvider, Denied, Header, NetworkError, NotFound } from '@xcan-angus/vue-ui';
import { utils, duration, appContext } from '@xcan-angus/infra';
import { debounce } from 'throttle-debounce';
import { mock } from 'src/api/tester';
import GlobalConstantConfig from '@/globalConstantConfig';
import { ai } from '@/api/gm';

import store from './store';

// Import global styles
import '@/assets/styles/global.css';

const windowResizeNotify = ref<string>();
const resizeHandler = debounce(duration.resize, () => {
  windowResizeNotify.value = utils.uuid();
});

const aiAgent = ref<{
  agentId: string;
  chatIframe: string;
  enabled: true;
  extensions: { [key: string]: any };
  provider: string;
}>();

const status = computed(() => {
  return store.state.statusCode;
});

const allFunction = ref([]);
let functionPromise: any;
const getAllFunctions = async () => {
  if (!allFunction.value.length && !functionPromise) {
    functionPromise = mock.getAllFunctions();
  }
  if (functionPromise) {
    functionPromise.then((res) => {
      if (res && res.length > 0) {
        allFunction.value = res;
      }
      functionPromise = undefined;
    });
  }
};

const globalConfigs = ref<{ [key: string]: string|{} }>();
onMounted(async () => {
  window.addEventListener('resize', resizeHandler);
  const envContent = appContext.getContext().env;
  globalConfigs.value = { ...envContent, ...GlobalConstantConfig };
  aiAgent.value = await ai.getAIAgentSetting();
});

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeHandler);
});

const aiEnabled = computed(() => {
  return !!aiAgent.value?.enabled;
});

provide('aiEnabled', aiEnabled);
provide('aiAgent', aiAgent);
provide('globalConfigs', globalConfigs);
provide('allFunction', allFunction);
provide('getAllFunctions', getAllFunctions);
provide('windowResizeNotify', windowResizeNotify);
</script>

<template>
  <ConfigProvider>
    <template v-if="status === 200">
      <RouterView />
    </template>
    <template v-else>
      <Header :menus="appContext.getAccessAppFuncTree() || []" :codeMap="appContext.getAccessAppFuncCodeMap()" />
      <div style="height: calc(100% - 54px);" class="overflow-auto flex justify-center items-center">
        <template v-if="status === 403">
          <Denied />
        </template>
        <template v-else-if="status === 404">
          <NotFound />
        </template>
        <template v-if="status === 405">
          <Denied />
        </template>
        <template v-else-if="status === 500">
          <NetworkError />
        </template>
      </div>
    </template>
  </ConfigProvider>
</template>

<style scoped>
/* Component-specific styles only */
/* Global styles are now imported from global.css */
</style>
