<script setup lang="ts">
import { computed, defineAsyncComponent, onBeforeUnmount, onMounted, provide, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { Drawer, Icon, Input, Select } from '@xcan-angus/vue-ui';
import { Button, TabPane, Tabs } from 'ant-design-vue';
import elementResizeDetector from 'element-resize-detector';
import { debounce } from 'throttle-debounce';
import { ApiPermission } from '@/enums/enums';
import { useWebSocket } from './composables/useWebSocket';
import { useUIOptions } from './composables/useUIOptions';

// Lazy-loaded components for better performance
const ApiServer = defineAsyncComponent(() => import('@/views/apis/services/protocol/websocket/Server.vue'));
const Indicator = defineAsyncComponent(() => import('@/components/indicator/index.vue'));
const HttpTestInfo = defineAsyncComponent(() => import('@/components/test/httpTestInfo/index.vue'));
const SocketForm = defineAsyncComponent(() => import('@/views/apis/services/protocol/websocket/SocketForm.vue'));
const MonacoEditor = defineAsyncComponent(() => import('@/components/editor/MonacoEditor/index.vue'));
const Toolbar = defineAsyncComponent(() => import('@/components/layout/toolbar/index.vue'));
const MessageList = defineAsyncComponent(() => import('@/views/apis/services/protocol/websocket/MessageList.vue'));
const Save = defineAsyncComponent(() => import('@/views/apis/services/protocol/websocket/Save.vue'));
const Config = defineAsyncComponent(() => import('@/views/apis/services/protocol/websocket/Config.vue'));
const RequestProxy = defineAsyncComponent(() => import('@/views/config/proxy/EditableRequestProxy.vue'));
const SaveUnarchived = defineAsyncComponent(() => import('@/views/apis/services/protocol/websocket/SaveUnarchived.vue'));
const ShareListVue = defineAsyncComponent(() => import('@/components/share/List.vue'));

interface Props {
  id: string;
  valueObj: Record<string, any>;
  uuid: string;
  ws: WebSocket;
  response: string;
  pid: string;
  responseCount: number;
}

const props = withDefaults(defineProps<Props>(), {
  id: undefined,
  valueObj: undefined,
  uuid: undefined,
  ws: undefined,
  response: undefined,
  pid: undefined,
  responseCount: undefined
});

const { t } = useI18n();
const elementResizeDetectorInstance = elementResizeDetector({ strategy: 'scroll' });

// Component references
const queryParameterFormRef = ref();
const headerParameterFormRef = ref();
const mainWebSocketRef = ref();

const messageListRef = ref();
const toolbarRef = ref();

// Composable integration
const {
  userPermissions,
  editorLanguageOptions,
  currentEditorLanguage,
  isWebSocketConnected,
  isWebSocketClosing,
  isWebSocketConnecting,
  webSocketConnectedTimestamp,
  messageSearchKeywords,
  messageTypeFilter,
  apiConfiguration,
  queryParameters,
  headerParameters,
  endpoint,
  currentServer,
  defaultCurrentServer,
  messageContent,
  toolbarMenuOptions,
  webSocketMessages,
  isConnectButtonDisabled,
  updateQueryParameters,
  updateHeaderParameters,
  packageApiParameters,
  establishWebSocketConnection,
  closeWebSocketConnection,
  sendWebSocketMessage,
  clearAllMessages,
  copyWebSocketUrl,
  handleEndpointBlur,
  loadApiPermissions,
  loadApiInformation,
  requestSettingKey,
  saveApiConfiguration: innerSaveApiConfiguration
} = useWebSocket(props, { queryParameterFormRef, headerParameterFormRef, toolbarRef });

// UI options
const { navigationMenuItems } = useUIOptions(props);

const drawerRef = ref();
const activeDrawerKey = ref();

const activeTabKey = ref('message');
const showParametersPanel = ref(true);

const toolbarHeight = ref(30);
const isToolbarMoving = ref(false);
const maxToolbarHeight = ref(500);

const handleWindowResize = debounce(100, () => {
  maxToolbarHeight.value = (mainWebSocketRef.value as any).clientHeight;
});

// Keep save and archive behavior consistent with the original template
const saveApiConfiguration = async () => {
  if (props.valueObj?.unarchived) {
    drawerRef.value.open('saveUnarchived');
  } else {
    await innerSaveApiConfiguration();
  }
};
const openArchiveDialog = () => {
  drawerRef.value.open('save');
};

provide('close', () => drawerRef.value.close());
provide('auths', computed(() => userPermissions.value));
provide('apiBaseInfo', apiConfiguration);
provide('isUnarchived', computed(() => props.valueObj.unarchived));
onMounted(async () => {
  await loadApiInformation();
  loadApiPermissions();
  elementResizeDetectorInstance.listenTo(mainWebSocketRef.value, handleWindowResize);
});

/**
 * Component before unmount lifecycle hook
 * <p>
 * Cleans up resize listener when component is destroyed
 * </p>
 */
onBeforeUnmount(() => {
  elementResizeDetectorInstance.removeListener(mainWebSocketRef.value, handleWindowResize);
});

/**
 * Provides close function to child components
 */
provide('close', () => {
  drawerRef.value.close();
});
/**
 * Provides user permissions to child components
 */
provide('auths', computed(() => userPermissions.value));
/**
 * Provides API base information to child components
 */
provide('apiBaseInfo', apiConfiguration);
/**
 * Provides unarchived status to child components
 */
provide('isUnarchived', computed(() => props.valueObj.unarchived));
</script>
<template>
  <div
    ref="mainWebSocketRef"
    class="h-full flex"
    :class="{'select-none': isToolbarMoving}">
    <div class="flex flex-1 flex-col min-h-0 min-w-0">
      <div class="flex-1 min-h-0 px-4 pt-4 flex flex-col overflow-hidden">
        <div class="flex items-center w-full ">
          <div class="border flex flex-1 rounded border-theme-text-box">
            <ApiServer
              :id="(apiConfiguration as any).id"
              v-model:currentServer="currentServer"
              :availableServers="(apiConfiguration as any).availableServers || []"
              :defaultCurrentServer="defaultCurrentServer"
              :readonly="isWebSocketConnected" />
            <Input
              v-model:value="endpoint"
              :readonly="isWebSocketConnected"
              :placeholder="t('service.apiWebSocket.form.uriPlaceholder')"
              class="border-0 flex-1"
              @blur="handleEndpointBlur" />
          </div>
          <Button
            v-if="isWebSocketConnected"
            :loading="isWebSocketClosing"
            class="ml-2 !bg-orange-500 text-white border-orange-500 hover:text-white hover:bg-orange-500 hover:border-orange-500"
            @click="() => closeWebSocketConnection()">
            <Icon icon="icon-duankai" class="mr-2" />{{ t('service.apiWebSocket.actions.disconnect') }}
          </Button>
          <Button
            v-else
            type="primary"
            class="ml-2"
            :loading="isWebSocketConnecting"
            :disabled="!props.valueObj?.unarchived && isConnectButtonDisabled"
            @click="establishWebSocketConnection">
            <Icon icon="icon-lianjie2" class="mr-2" />{{ t('service.apiWebSocket.actions.connect') }}
          </Button>
          <template v-if="props.valueObj?.unarchived">
            <Button
              v-if="props.valueObj.id"
              class="ml-2"
              @click="saveApiConfiguration">
              {{ t('actions.save') }}
            </Button>
            <Button
              v-else
              class="ml-2"
              @click="saveApiConfiguration">
              {{ t('service.apiWebSocket.actions.saveToUnarchived') }}
            </Button>
            <Button
              class="ml-2"
              @click="openArchiveDialog">
              {{ t('service.apiWebSocket.actions.archive') }}
            </Button>
            <Button
              class="ml-2"
              @click="copyWebSocketUrl">
              {{ t('service.apiWebSocket.actions.copyUrl') }}
            </Button>
          </template>
          <Button
            v-else
            class="ml-2"
            :disabled="!userPermissions.includes(ApiPermission.MODIFY)"
            @click="saveApiConfiguration">
            <Icon icon="icon-baocun" class="mr-2" />{{ t('actions.save') }}
          </Button>
        </div>
        <div class="flex-1 overflow-auto flex flex-col justify-between">
          <Tabs
            v-model:activeKey="activeTabKey"
            class="overflow-y-auto"
            :class="{'hide-pane': !showParametersPanel}"
            size="small">
            <TabPane key="message" :tab="t('service.apiWebSocket.labels.sendMessage')">
              <MonacoEditor
                v-model:value="messageContent"
                :language="currentEditorLanguage"
                class="w-full h-75 mt-2 border pt-2 rounded" />
              <div class="flex justify-between pt-2">
                <Select
                  v-model:value="currentEditorLanguage"
                  :options="editorLanguageOptions"
                  class="w-40" />
                <Button
                  type="primary"
                  class="h-7 py-0 px-2"
                  :disabled="!isWebSocketConnected"
                  @click="sendWebSocketMessage">
                  <Icon icon="icon-fasong" class="mr-2" />
                  {{ t('actions.send') }}
                </Button>
              </div>
            </TabPane>
            <TabPane
              key="query"
              :tab="t('protocol.queryParameter')"
              :forceRender="true">
              <SocketForm
                ref="queryParameterFormRef"
                key="query"
                in="query"
                :data="queryParameters"
                @change="updateQueryParameters" />
            </TabPane>
            <TabPane
              key="header"
              :tab="t('protocol.requestHeader')"
              :forceRender="true">
              <SocketForm
                ref="headerParameterFormRef"
                key="header"
                :data="headerParameters"
                in="header"
                @change="updateHeaderParameters" />
            </TabPane>
            <TabPane key="config" :tab="t('common.setting')">
              <Config
                :id="(apiConfiguration as any).id"
                v-model:value="(apiConfiguration as any)[requestSettingKey]"
                class="mt-2" />
            </TabPane>
          </Tabs>
        </div>
      </div>
      <Toolbar
        ref="toolbarRef"
        v-model:height="toolbarHeight"
        v-model:moving="isToolbarMoving"
        :max-height="maxToolbarHeight"
        :menus="toolbarMenuOptions">
        <template #actions>
          <div v-show="isWebSocketConnected" class="text-3 text-text-content mr-3 flex items-center">
            <span class="mr-7.5">{{ webSocketConnectedTimestamp }}</span>
            <Icon icon="icon-duihao" class="text-status-success" />
            <span class="ml-1 leading-3">{{ t('status.connected') }}</span>
          </div>
        </template>
        <template #content="{activeMenu}">
          <template v-if="activeMenu === 'message'">
            <div class="px-6 py-2 flex-1 overflow-auto">
              <div class="flex items-center mb-4">
                <Input
                  v-model:value="messageSearchKeywords"
                  class="w-50"
                  :placeholder="t('service.apiWebSocket.form.searchMessagePlaceholder')"
                  size="small">
                  <template #suffix>
                    <Icon icon="icon-sousuo" class="text-4 text-theme-sub-content" />
                  </template>
                </Input>
                <Select
                  v-model:value="messageTypeFilter"
                  class="w-50 ml-2"
                  :placeholder="t('service.apiWebSocket.form.allMessagesPlaceholder')"
                  size="small"
                  :allowClear="true"
                  :options="[{value: 'receive', label: t('service.apiWebSocket.options.receiveMessage')}, {value: 'send', label: t('service.apiWebSocket.options.sendMessage')}]" />
                <span class="flex-1"></span>
                <Button
                  class="h-7 leading-7 py-0 px-3 text-3"
                  @click="clearAllMessages">
                  <Icon icon="icon-qingsao" class="mr-2 text-3 -mt-0.5" />{{ t('actions.clear') }}
                </Button>
              </div>
              <MessageList
                ref="messageListRef"
                :data="webSocketMessages"
                :keyword="messageSearchKeywords"
                :msgType="messageTypeFilter" />
            </div>
          </template>
        </template>
      </Toolbar>
    </div>
    <Drawer
      ref="drawerRef"
      v-model:activeKey="activeDrawerKey"
      :menuItems="navigationMenuItems">
      <template #saveUnarchived>
        <SaveUnarchived
          v-if="activeDrawerKey==='saveUnarchived'"
          class="mt-2 pr-5"
          v-bind="(apiConfiguration as any)"
          :tabKey="props.pid"
          :packageParams="packageApiParameters" />
      </template>
      <template #save>
        <Save
          v-if="activeDrawerKey==='save'"
          class="mt-2 pr-5"
          v-bind="(apiConfiguration as any)"
          :tabKey="props.pid"
          :packageParams="packageApiParameters"
          @ok="loadApiInformation" />
      </template>
      <template #agent>
        <RequestProxy v-if="activeDrawerKey==='agent'" class="pr-5" />
      </template>
      <template #indicator>
        <Indicator
          v-if="activeDrawerKey==='indicator'"
          :id="props.id"
          type="API"
          class="pr-5 pt-2" />
      </template>
      <template #testInfo>
        <HttpTestInfo
          :id="props.id"
          :type="'WEBSOCKET' as any"
          class="pr-5 pt-3" />
      </template>
      <template #share>
        <ShareListVue
          v-if="activeDrawerKey === 'share'"
          :id="props.id"
          :disabled="!userPermissions.includes(ApiPermission.SHARE)"
          class="pr-5 pt-2"
          type="API" />
      </template>
    </Drawer>
  </div>
</template>
<style scoped>
.hide-pane :deep(.ant-tabs-content-holder) {
  height: 0;
  overflow: hidden;
}
</style>
