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
  Ref,
  ref,
  watch
} from 'vue';
import ReconnectingWebSocket from 'reconnecting-websocket';
import { utils, appContext, IPane } from '@xcan-angus/infra';
import { BrowserTab } from '@xcan-angus/vue-ui';
import { useRoute, useRouter } from 'vue-router';
import { useI18n } from 'vue-i18n';

import { setting } from '@/api/gm';
import { ProjectInfo } from '@/layout/types';

const Sidebar = defineAsyncComponent(() => import('@/views/apis/services/sidebar/index.vue'));
const ApiGroup = defineAsyncComponent(() => import('@/views/apis/services/apiGroup/index.vue'));
const ApiItem = defineAsyncComponent(() => import('@/views/apis/services/apiHttp/index.vue'));
const servicesMock = defineAsyncComponent(() => import('@/views/apis/services/mock/servicesMock.vue'));
const apisSocket = defineAsyncComponent(() => import('@/views/apis/services/apiWebSocket/index.vue'));
const Auth = defineAsyncComponent(() => import('@/views/apis/services/auth/index.vue'));
const DataModel = defineAsyncComponent(() => import('@/views/apis/services/dataModel/index.vue'));
const SecurityTestResult = defineAsyncComponent(() => import('@/views/apis/services/securityTestResult/index.vue'));
const SmokeTestResult = defineAsyncComponent(() => import('@/views/apis/services/smokeTestResult/index.vue'));
const QuickStarted = defineAsyncComponent(() => import('@/views/apis/home/quickStarted.vue'));

const { t } = useI18n();

const route = useRoute();
const router = useRouter();

const sidebarRef = ref();
const currentProxyUrl = ref();
const currentProxy = ref();
const ws = ref();
const uuid = ref('');
const responseData = ref('');
const readyState = ref(3);
const responseCount = ref(0);

const userInfo = ref(appContext.getUser());
const projectInfo = inject<Ref<ProjectInfo>>('projectInfo', ref({} as ProjectInfo));
const appInfo = ref(appContext.getAccessApp());

const projectId = computed(() => {
  return projectInfo.value?.id;
});

const createWebSocket = () => {
  ws.value = new ReconnectingWebSocket(currentProxyUrl.value, [],
    {
      maxRetries: 3,
      maxReconnectionDelay: 30000, // max delay in ms between reconnections
      minReconnectionDelay: 30000,
      connectionTimeout: 60000
    });

  ws.value.addEventListener('open', () => {
    readyState.value = ws.value.readyState || 1;
  });

  ws.value.addEventListener('message', (event) => {
    try {
      const response = JSON.parse(event.data);
      if (response?.requestId) {
        uuid.value = response?.requestId;
      } else if (response?.clientId) {
        uuid.value = response.clientId;
      } else {
        uuid.value = '';
      }

      responseData.value = event.data;
      responseCount.value += 1;
    } catch (error) { }
  });

  ws.value.addEventListener('error', () => {
    readyState.value = ws.value?.readyState || 3;
  });
  ws.value.addEventListener('close', () => {
    readyState.value = ws.value?.readyState || 3;
  });
};

const updateWs = () => {
  if (navigator.onLine) {
    if (ws.value?.close) {
      ws.value?.close(1000);
    }
    if (currentProxyUrl.value) {
      ws.value = undefined;
      createWebSocket();
    } else if (currentProxy.value === 'NO_PROXY') {
      ws.value = undefined;
    } else {
      ws.value = { readyState: 4 };
    }
  } else {
    if (currentProxyUrl.value) {
      ws.value = { readyState: 4 };
    } else {
      ws.value = undefined;
    }
  }
};

const loadProxyUrl = async () => {
  const [error, { data }] = await setting.getUserApiProxy();
  if (error) {
    return;
  }

  Object.keys(data).forEach(key => {
    if (data[key].enabled) {
      currentProxyUrl.value = data[key].url;
      currentProxy.value = data[key].name.value;
    }
  });
};

// 保存已经打开的tab信息到localStorage的key
const STORAGE_KEY = computed<string>(() => {
  return `api_browser_tab_${projectInfo.value?.id || ''}`;
});
const tabRef = ref();

const addHandler = () => {
  if (typeof tabRef.value?.add === 'function') {
    tabRef.value.add(() => {
      const key = utils.uuid('api');
      return {
        _id: key, // pane 的key，唯一标识
        pid: key,
        name: t('service.home.addApiTabName'), // pane 的tab文案
        value: 'API',
        closable: true, // 是否允许关闭，true - 允许关闭，false - 禁止关闭
        forceRender: false, // 被隐藏时是否渲染 DOM 结构
        unarchived: true
      };
    });
  }
};

const addTabPane = (data: IPane) => {
  router.replace('/apis#services');
  nextTick(() => {
    if (typeof tabRef.value?.add === 'function' && projectInfo.value.id) {
      tabRef.value.add(() => {
        return data;
      });
    } else {
      sessionStorage.setItem('addTabPane', JSON.stringify(data));
    }
  });
};

const deleteTabPane = (keys: string[]) => {
  if (typeof tabRef.value?.remove === 'function') {
    tabRef.value.remove(keys);
  }
};

// 需要更新项目列表上的项目名称
const updateProjectInfo = reactive({
  id: '',
  name: ''
});

const updateTabPane = (data: IPane) => {
  if (typeof tabRef.value?.update === 'function') {
    tabRef.value.update(data);
  }
  if (data.pid.includes('group')) {
    updateProjectInfo.id = data.pid.replace('group', '');
    if (data.name) {
      updateProjectInfo.name = data.name;
    }
  }
};

const replaceTabPane = (key: string, data: { key: string }) => {
  if (typeof tabRef.value?.put === 'function') {
    tabRef.value.replace(key, data);
  }
};

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

onMounted(() => {
  loadProxyUrl();

  watch([() => currentProxyUrl.value, () => currentProxy.value], ([newValue]) => {
    if (ws.value?.close) {
      ws.value?.close(1000);
    }
    if (newValue) {
      ws.value = undefined;
      createWebSocket();
    } else if (currentProxy.value === 'NO_PROXY') {
      ws.value = undefined;
    } else {
      ws.value = { readyState: 4 };
    }
  }, { immediate: true });

  if (navigator.connection) {
    navigator.connection.addEventListener('change', updateWs);
  }

  const fullPath = decodeURI(route.fullPath);
  const queryStr = fullPath.split('?')[1];
  if (queryStr) {
    const result:{[key:string]: string} = {};
    const querries = queryStr.split('&');
    querries.forEach(query => {
      const keyValue = query.split('=');
      result[keyValue[0]] = keyValue[1];
    });
    if ((['API', 'group'].includes(result.value)) && result.id && result.name) {
      addTabPane({
        _id: result.id + result.value,
        id: result.id,
        name: result.name,
        value: result.value,
        closable: true,
        shouldCheckId: result.value === 'group'
      });
    }
  }
});

onBeforeUnmount(() => {
  ws.value && ws.value.close && ws.value.close(1000);
  if (navigator.connection) {
    navigator.connection.removeEventListener('change', updateWs);
  }
});

// 刷新左侧项目/服务列表
const refreshSidebar = () => {
  if (typeof sidebarRef.value?.refresh === 'function') {
    sidebarRef.value.refresh();
  }
};

// 刷新左侧未归档列表
const refreshUnarchived = () => {
  if (typeof sidebarRef.value?.refreshUnarchived === 'function') {
    sidebarRef.value.refreshUnarchived();
  }
};

const apiGroupReloadConfig = reactive({
  reloadId: '',
  reloadKey: 0
});
const updateApiGroup = (projectId) => {
  apiGroupReloadConfig.reloadId = projectId;
  apiGroupReloadConfig.reloadKey += 1;
};

// 刷新左侧项目/服务列表
provide('refreshSidebar', refreshSidebar);

// 刷新左侧未归档列表
provide('refreshUnarchived', refreshUnarchived);

// 添加指定的tabPane
provide('addTabPane', addTabPane);

// 删除指定的tabPane
provide('deleteTabPane', deleteTabPane);

// 更新指定的tabPane
provide('updateTabPane', updateTabPane);

// 替换指定tabPane
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

// @TODO 需要把所有用到的地方重构
provide('updateHosts', reactive({
  reloadId: '',
  reloadKey: 0
}));

defineExpose({
  addTabPane,
  deleteTabPane,
  updateTabPane,
  replaceTabPane,
  updateApiGroup,
  refreshSidebar,
  refreshUnarchived
});
</script>
<template>
  <div class="flex-1 flex h-full border-l min-w-0">
    <Sidebar ref="sidebarRef" />
    <BrowserTab
      v-if="projectId"
      ref="tabRef"
      :key="STORAGE_KEY"
      class="flex-1"
      :storageKey="STORAGE_KEY"
      @add="addHandler">
      <template #empty>
        <QuickStarted class="p-5" />
      </template>
      <template #default="record">
        <template v-if="record.value === 'group'">
          <ApiGroup :serviceId="record.id" :info="record" />
        </template>
        <template v-if="record.value === 'API'">
          <ApiItem
            :id="record.id"
            :valueObj="record"
            :ws="ws"
            :uuid="uuid"
            :response="responseData"
            :pid="record._id"
            :userInfo="userInfo"
            :appInfo="appInfo"
            :projectId="projectId" />
        </template>
        <template v-if="record.value === 'auth'">
          <Auth :appId="appInfo?.id" />
        </template>
        <template v-if="record.value === 'mock'">
          <servicesMock :id="record.id" />
        </template>
        <template v-if="record.value === 'socket'">
          <apisSocket
            :id="record.id"
            :pid="record._id"
            :ws="ws"
            :uuid="uuid"
            :valueObj="record"
            :name="record.name"
            :responseCount="responseCount"
            :response="responseData" />
        </template>
        <template v-if="record.value === 'model'">
          <DataModel
            :id="record.id"
            :pid="record._id"
            :type="record.type"
            :name="record.name"
            :data="record.data" />
        </template>
        <template v-if="record.value === 'auth_test'">
          <SecurityTestResult />
        </template>
        <template v-if="record.value === 'smoke_test'">
          <SmokeTestResult />
        </template>
      </template>
    </BrowserTab>
  </div>
</template>
