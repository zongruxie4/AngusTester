<script setup lang="ts">
import { defineAsyncComponent, inject, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { AsyncComponent, Icon, notification, Toggle, ResponseStatus } from '@xcan-angus/vue-ui';
import { Button } from 'ant-design-vue';
import { LoadingOutlined } from '@ant-design/icons-vue';

// Import composables
import { useApiDebug } from './composables/useApiDebug';
import { useDebugUI } from './composables/useDebugUI';

import UrlForm from '@/views/apis/mock/detail/apis/components/urlForm/index.vue';

const { t } = useI18n();

// Component imports
const Agent = defineAsyncComponent(() => import('@/views/apis/mock/detail/apis/components/agent/index.vue'));
const InputGroup = defineAsyncComponent(() => import('@/views/apis/mock/detail/apis/components/contentForm/InputGroup.vue'));
const RequestBody = defineAsyncComponent(() => import('@/views/apis/mock/detail/apis/components/contentForm/requestBody.vue'));

/**
 * Props interface for Debug APIs component
 */
interface Props {
  serviceOptions: { label: string, value: string }[];
  mockAPIInfo?: {
    id?: string;
    method?: string;
    endpoint?: string;
  };
}

const props = withDefaults(defineProps<Props>(), {
  serviceOptions: () => ([]),
  mockAPIInfo: () => ({})
});

// Component references
const urlRef = ref();
const inputGroupRef = ref();
const requestBodyRef = ref();

// Initialize composables
const {
  debugging,
  responseContent,
  openHeader,
  openBody,
  loadStatusEnum,
  stopDebug,
  processFormData,
  buildRequestUrl,
  processRequestBody,
  sendWebSocketRequest,
  sendHttpRequest,
  onHttpResponse,
  watchWebSocketResponse
} = useApiDebug();

const {
  spread,
  showDebug,
  toggleSpread,
  changeShowDebug
} = useDebugUI();

// Form state
const method = ref();
const server = ref();
const endpoint = ref();
const contentType = ref<string | undefined>(undefined);

/**
 * Send API request
 */
const sendRequest = async () => {
  if (!urlRef.value?.isValid()) {
    return;
  }

  debugging.value = true;

  // Get parameters from form
  const parameters: any[] = [];
  if (typeof inputGroupRef.value?.getData === 'function') {
    parameters.push(...inputGroupRef.value.getData());
  }

  // Process form data
  const { queryData, headerData, cookieData } = processFormData(parameters);

  // Build request URL
  const apiHref = buildRequestUrl(server.value, endpoint.value, queryData);

  // Process request body
  let bodyObj;
  if (typeof requestBodyRef.value?.getData === 'function') {
    bodyObj = requestBodyRef.value.getData();
  }
  const { bodyContent, bodyOpenApi } = processRequestBody(bodyObj, headerData);

  // Send request via WebSocket or HTTP
  const WS = inject('WS', ref());
  if (WS.value && WS.value.readyState === 1) {
    const parameters = [...queryData, ...headerData, ...cookieData];
    const requestData = {
      parameters,
      method: method.value,
      server: { url: server.value },
      requestBody: bodyOpenApi,
      endpoint: endpoint.value
    };
    sendWebSocketRequest(requestData);
  } else if (WS.value && WS.value.readyState !== 1) {
    notification.error(t('mock.mockApis.debugApis.notifications.proxyNotConnected'));
    debugging.value = false;
  } else {
    // Prepare headers for HTTP request
    const header: Record<string, string> = {};
    if (headerData.length) {
      headerData.forEach(item => {
        if (item.name) {
          header[item.name] = item.value;
        }
      });
    }
    if (cookieData.length) {
      const cookieStr = cookieData.filter(i => i.name).map(item => {
        return `${item.name}=${item.value}`;
      }).join('; ');
      header.Cookie = cookieStr;
    }
    if (contentType.value) {
      header['Content-Type'] = contentType.value;
    }

    // Send HTTP request
    const config = {
      responseType: 'blob' as const,
      url: apiHref,
      method: method.value,
      data: bodyContent || true,
      headers: {
        accept: '*/*',
        ...header
      }
    };

    const resp = await sendHttpRequest(config);
    await onHttpResponse(resp);
  }
};

// Watch for mock API info changes
watch(() => props.mockAPIInfo.id, () => {
  method.value = props.mockAPIInfo.method || 'GET';
  endpoint.value = props.mockAPIInfo.endpoint || '';
  contentType.value = undefined;
  responseContent.value = undefined;
}, {
  immediate: true
});

// Initialize WebSocket response watcher
onMounted(() => {
  loadStatusEnum();
  watchWebSocketResponse();
});
</script>
<template>
  <div class="absolute top-10 right-0 flex text-3 z-9">
    <div
      class="w-9 h-max transform-gpu translate-y-0.75 space-y-0.5"
      @click="toggleSpread">
      <div
        v-show="!spread || showDebug"
        class="bg-status-success text-white flex flex-col items-center rounded-l-xl py-2 h-max cursor-pointer"
        @click="changeShowDebug(true)">
        <Icon icon="icon-zhihangceshi" class="text-3.5 leading-3.5" />
        <span style="writing-mode: vertical-lr;" class="mt-1">{{ t('mock.mockApis.debugApis.testRequest') }}</span>
      </div>
      <div
        v-show="!spread || !showDebug"
        class="bg-orange-bg text-white flex flex-col items-center rounded-l-xl py-2 h-max cursor-pointer"
        @click="changeShowDebug(false)">
        <Icon icon="icon-jiekoudaili" class="text-3.5 leading-3.5" />
        <span style="writing-mode: vertical-lr;" class="mt-1">{{ t('mock.mockApis.debugApis.proxy') }}</span>
      </div>
    </div>
    <div
      class="bg-white border-status-success rounded transition-all duration-500 box-border overflow-x-hidden overflow-y-auto space-y-5"
      :class="[spread && showDebug ? 'w-200 border p-3' : 'w-0 border-0']"
      style="height: 70vh;">
      <span class="font-semibold">{{ t('mock.mockApis.debugApis.testRequest') }}</span>
      <UrlForm
        ref="urlRef"
        v-model:method="method"
        v-model:endpoint="endpoint"
        v-model:server="server"
        :options="props.serviceOptions" />
      <InputGroup ref="inputGroupRef" />
      <RequestBody ref="requestBodyRef" />
      <div>
        <template v-if="debugging">
          <Button
            size="small"
            @click="stopDebug">
            <LoadingOutlined />
            {{ t('mock.mockApis.debugApis.stop') }}
          </Button>
        </template>
        <template v-else>
          <Button
            size="small"
            type="primary"
            @click="sendRequest">
            {{ t('mock.mockApis.debugApis.send') }}
          </Button>
        </template>
      </div>
      <div>
        <div class="space-x-2 mb-1.5">
          <span class="font-semibold">{{ t('mock.mockApis.debugApis.response') }}</span>
          <ResponseStatus v-if="responseContent?.status > 0" :status="responseContent?.status" />
        </div>
        <div class="min-h-50 bg-gray-light rounded whitespace-break-spaces space-y-2 p-2">
          <Toggle
            v-if="responseContent"
            v-model:open="openHeader"
            class="text-3"
            title="Response Header">
            <div>
              <div
                v-for="(header, idx) in responseContent.responseHeader"
                :key="idx"
                class="flex span-x-2">
                <label class="text-text-sub-content">{{ header.key }}: </label>
                <span class="text-text-content"> {{ header.value }}</span>
              </div>
            </div>
          </Toggle>
          <Toggle
            v-if="responseContent"
            v-model:open="openBody"
            title="Response Body">
            <div>
              {{ responseContent?.responseBody }}
            </div>
          </Toggle>
        </div>
      </div>
    </div>
    <div
      class="bg-white border-orange-bg rounded transition-all duration-500 box-border overflow-x-hidden overflow-y-auto space-y-2 flex flex-col"
      :class="[spread && !showDebug ? 'w-200 border p-3' : 'w-0 border-0']"
      style="height: 160px;">
      <AsyncComponent :visible="spread && !showDebug">
        <Agent />
      </AsyncComponent>
    </div>
  </div>
</template>
<style scoped>
:deep(.toggle-container) .toggle-title {
  @apply text-3;
}

</style>
