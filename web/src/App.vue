<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, provide, ref } from 'vue';
import { ConfigProvider, Denied, Header, NetworkError, NotFound } from '@xcan-angus/vue-ui';
import { http, utils, duration, GM, appContext } from '@xcan-angus/infra';
import { debounce } from 'throttle-debounce';
import { mock } from 'src/api/tester';
import GlobalConstantConfig from '@/globalConstantConfig';

import store from './store';

const windowResizeNotify = ref<string>();
// 通知浏览器窗口大小调整
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
let functionPromise;
const getAllFunctions = async () => {
  if (!allFunction.value.length && !functionPromise) {
    functionPromise = mock.getAllFunction();
  }
  if (functionPromise) {
    functionPromise.then(([error, res]) => {
      if (!error) {
        allFunction.value = res.data.map(i => {
          return {
            ...i,
            constructors: i.constructors ? i.constructors.map(sub => ({ ...sub, name: sub.instance })) : undefined
          };
        });
      }
      functionPromise = undefined;
    });
  }
};

const loadAIAgent = async () => {
  const [error, res] = await http.get(`${GM}/setting/AI_AGENT`);
  if (error) {
    return null;
  }

  return res?.data?.aiAgent;
};

const globalConfigs = ref<{ [key: string]: string|{} }>();
onMounted(async () => {
  window.addEventListener('resize', resizeHandler);
  const envContent = appContext.getContext().env;
  globalConfigs.value = { ...envContent, ...GlobalConstantConfig };
  aiAgent.value = await loadAIAgent();
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

<style>
body,
html {
  min-width: 1280px;
  height: 100%;
  overflow: auto;
  font-size: 16px;
}

#app {
  height: 100%;
  font-family:
    Inter,
    "Apple System",
    "SF Pro SC",
    "SF Pro Display",
    "Helvetica Neue",
    Arial,
    "PingFang SC",
    "Hiragino Sans GB",
    STHeiti,
    "Microsoft YaHei",
    "Microsoft JhengHei",
    "Source Han Sans SC",
    "Noto Sans CJK SC",
    "Source Han Sans CN",
    sans-serif;
  font-size: 14px;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}
</style>
