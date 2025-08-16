<script lang="ts" setup>
import { onMounted, ref, defineAsyncComponent, provide, watch } from 'vue';
import { cookieUtils, DomainManager, AppOrServiceRoute } from '@xcan-angus/infra';
import ReconnectingWebSocket from 'reconnecting-websocket';
import { useRoute } from 'vue-router';
import { Icon, Spin } from '@xcan-angus/vue-ui';
import '@xcan-angus/rapidoc';
import { useI18n } from 'vue-i18n';
import { shareApis } from 'src/api/tester';

const { t } = useI18n();
const route = useRoute();
const Agent = defineAsyncComponent(() => import('@/views/mock/detail/mockApis/components/agent/index.vue'));
const id = ref();
const pat = ref();

const viewData = ref();
const openapi = ref();
const displayOptions = ref({
  allowDebug: false,
  includeServiceInfo: false,
  schemaStyle: 'table'
});
const loading = ref(false);

const apiProxy = ref();
const currentProxy = ref({});
const currentProxyUrl = ref();
const readyState = ref(-1);

const connectWs = () => {
  window.requestWs = new ReconnectingWebSocket(currentProxyUrl.value, [], {
    maxRetries: 3,
    maxReconnectionDelay: 30000, // max delay in ms between reconnections
    minReconnectionDelay: 30000,
    connectionTimeout: 60000
  });

  window.requestWs.addEventListener('open', () => {
    readyState.value = window.requestWs.readyState || 1;
  });

  window.requestWs.addEventListener('error', () => {
    readyState.value = window.requestWs?.readyState || 3;
  });
  window.requestWs.addEventListener('close', () => {
    readyState.value = window.requestWs?.readyState || 3;
  });
};

const responseErr = ref();

const loadData = async () => {
  loading.value = true;
  const [error, { data }] = await shareApis.getApiShareInfo({
    id: id.value,
    pat: pat.value
  });
  loading.value = false;
  responseErr.value = '';
  if (error) {
    responseErr.value = error.message;
    return;
  }
  viewData.value = data;
  openapi.value = data.openapi;
  displayOptions.value = data.displayOptions;
  displayOptions.value.schemaStyle = displayOptions.value.schemaStyle.toLowerCase();
  apiProxy.value = data.apiProxy;
  if (displayOptions.value.allowDebug) {
    Object.keys(apiProxy.value || {}).forEach(key => {
      if (apiProxy.value[key].enabled) {
        currentProxy.value = apiProxy.value[key].name.value;
        currentProxyUrl.value = apiProxy.value[key].url;
      }
    });
  }
};

const accessToken = ref();
const docOrigin = ref();
onMounted(async () => {
  id.value = route.query.id;
  pat.value = route.query.pat;
  accessToken.value = cookieUtils.getTokenInfo().access_token;
  docOrigin.value = DomainManager.getInstance().getAppDomain(AppOrServiceRoute.tester);
  await loadData();

  watch(() => currentProxyUrl.value, (newValue) => {
    if (newValue) {
      connectWs();
    } else {
      window.requestWs && window.requestWs.close();
      window.requestWs = null;
    }
  }, {
    immediate: true
  });
});

const spread = ref(false);
const toggleSpread = () => {
  spread.value = !spread.value;
};

provide('proxyOptObj', apiProxy);
provide('currentProxy', currentProxy);
provide('currentProxyUrl', currentProxyUrl);
provide('readyState', readyState);

</script>
<template>
  <Spin :spinning="loading" class="h-full">
    <div v-if="(viewData && viewData.expired) || responseErr" class="text-center text-5 font-semibold h-40 leading-40">
      {{ responseErr || '分享已过期' }}
    </div>
    <template v-else-if="openapi">
      <rapi-doc
        :specUrl="openapi"
        specIsContent="true"
        style="padding: 20px;"
        theme="light"
        renderStyle="focused"
        headerColor="#fff"
        updateRoute="false"
        navBgColor="#fff"
        :showInfo="displayOptions.includeServiceInfo"
        bgColor="#fff"
        allowSpecUrlLoad="false"
        allowSpecFileLoad="false"
        allowSpecFileDownload="false"
        :allowTry="displayOptions.allowDebug"
        :schemaStyle="displayOptions.schemaStyle"
        showHeader="false"
        schemaExpandLevel="20">
      </rapi-doc>
      <div v-if="apiProxy && displayOptions.allowDebug" class="absolute top-10 right-0 flex text-3 z-9">
        <div
          class="w-9 h-max transform-gpu translate-y-0.75 space-y-0.5"
          @click="toggleSpread">
          <div
            class="bg-orange-bg text-white flex flex-col items-center rounded-l-xl py-2 h-max cursor-pointer">
            <Icon icon="icon-jiekoudaili" class="text-3.5 leading-3.5" />
            <span style="writing-mode: vertical-lr;" class="mt-1">{{t('apiShare.agentProxyTitle')}}</span>
          </div>
        </div>
        <div
          class="bg-white border-orange-bg rounded transition-all duration-500 box-border overflow-x-hidden overflow-y-auto space-y-2 flex flex-col"
          :class="[spread ? 'w-200 border p-3' : 'w-0 border-0']"
          style="height: 160px;">
          <Agent />
        </div>
      </div>
    </template>
  </Spin>
</template>
