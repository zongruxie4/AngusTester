<script setup lang="ts">
import { computed, defineAsyncComponent, inject, onMounted, ref, toRaw, watch, Ref } from 'vue';
import { AsyncComponent, Icon, Input, Spin, Tooltip, VuexHelper, Select } from '@xcan-angus/vue-ui';
import { XCanDexie, utils, enumUtils, HttpMethod } from '@xcan-angus/infra';
import { Button, Divider, Dropdown } from 'ant-design-vue';
import { useI18n } from 'vue-i18n';

import { ApiPermission, ApiServerSource } from '@/enums/enums';
import { API_EXTENSION_KEY } from '@/utils/apis';
import { ServerInfo } from '@/views/apis/server/types';
import { getDefaultParams } from '@/views/apis/services/protocol/http/utils';
import { API_URI_MAX_LENGTH } from '@/utils/constant';

const ServerInput = defineAsyncComponent(() => import('@/views/apis/services/protocol/http/path/ServerInput.vue'));
const AddCaseModal = defineAsyncComponent(() => import('@/views/apis/services/components/case/AddCaseModal.vue'));

// Define ChangeEvent type for better type safety
interface ChangeEvent {
  target: {
    value: string;
  };
}

interface Props {
  loading: boolean;
  isUnarchivedApi?: boolean;
  id: string;
  endpoint: string;
  availableServers: any[];
  currentServer: ServerInfo;
  defaultCurrentServer: ServerInfo;
  method: HttpMethod;
}

const props = withDefaults(defineProps<Props>(), {
  loading: false,
  isUnarchivedApi: true,
  id: '',
  method: HttpMethod.GET,
  currentServer: () => ({ url: '', id: utils.uuid() }),
  availableServers: () => ([])
});

const { t } = useI18n();

const emit = defineEmits<{
  (e: 'sendRequest'): void;
  (e: 'update:loading', value: boolean): void;
  (e: 'save'): void;
  (e: 'archived'): void;
  (e: 'update:endpoint', value: string): void;
  (e: 'update:currentServer', value: ServerInfo): void;
  (e: 'update:method', value: string): void;
  (e: 'abort'): void;
  (e: 'addQuery', value: Record<string, string>[]): void;
  (e: 'copyUrl'): void;
}>();

const { serverSourceKey, valueKey, idKey } = API_EXTENSION_KEY;

const { useMutations, useState } = VuexHelper;

const auths = inject('auths', ref<string[]>([]));
const { stepVisible, stepKey, stepContent } = useState(['stepVisible', 'stepKey', 'stepContent'], 'guideStore') as { stepVisible: Ref<boolean>; stepKey: Ref<string>; stepContent: Ref<{ title: string; content: string; }> };
const { updateGuideStep } = useMutations(['updateGuideStep'], 'guideStore');

const dexie = new XCanDexie<{ id: string; data: any }>('serverUrl');

const showServerListDrop = ref(false);
const addCaseVisible = ref(false);
const apiMethod = ref<HttpMethod>(HttpMethod.GET);

const currentHttpServer = ref<ServerInfo>({ url: '', id: utils.uuid() });
const serverValue = ref<string>('');
const apiUri = ref<string>(); // uri整体
const serversFromParent = ref<ServerInfo[]>([]);
const serversFromMock = ref<ServerInfo[]>([]);
const serversFromIndexDB = ref<ServerInfo[]>([]);

/**
 * Parse and recognize URI components from endpoint string
 * <p>Extracts server URL, path, and query parameters from a complete endpoint URL</p>
 * <p>Handles both valid URLs and simple endpoint strings</p>
 * @param endpoint - The complete endpoint string to parse
 */
const parseEndpointUri = (endpoint: string) => {
  try {
    // Try to parse as a complete URL
    const urlObj = new URL(endpoint);
    serverValue.value = urlObj.origin;
    currentHttpServer.value.url = serverValue.value;
    apiUri.value = urlObj.pathname;

    // Extract query parameters if present
    const queryString = urlObj.search.split('?')[1];
    if (queryString) {
      const queryStrArr = queryString.split('&');
      const queryParam = queryStrArr.map(keyValue => {
        const [key, value] = keyValue.split('=');
        return getDefaultParams({ [valueKey]: value || '', name: key });
      });
      emit('addQuery', queryParam);
      apiUri.value = urlObj.search.split('?')[0];
    }
  } catch {
    // Fallback for simple endpoint strings (not full URLs)
    const queryString = endpoint.split('?')[1];
    if (queryString) {
      const queryStrArr = queryString.split('&');
      const queryParam = queryStrArr.map(keyValue => {
        const [key, value] = keyValue.split('=');
        return getDefaultParams({ [valueKey]: value || '', name: key });
      });
      emit('addQuery', queryParam);
      apiUri.value = endpoint.split('?')[0];
    }
  } finally {
    // Always emit updates regardless of parsing success
    emit('update:currentServer', currentHttpServer.value);
    emit('update:endpoint', apiUri.value || '');
  }
};

/**
 * Handle server selection from dropdown
 * <p>Updates current server and emits change event</p>
 * @param value - The selected server URL
 * @param item - The complete server info object
 */
const handleServerSelection = (value: string, item: ServerInfo) => {
  currentHttpServer.value = JSON.parse(JSON.stringify(item));
  serverValue.value = value;
  emit('update:currentServer', item);
};

const httpMethodOptions = ref<{value: string; label: string}[]>([]);

/**
 * Load HTTP method options from enum
 * <p>Converts HttpMethod enum to dropdown options format</p>
 */
const loadHttpMethodOptions = () => {
  const data = enumUtils.enumToMessages(HttpMethod);
  httpMethodOptions.value = data.map(i => ({ value: i.value, label: i.message }));
};

/**
 * Load server history from IndexedDB
 * <p>Retrieves previously used servers for the current API</p>
 */
const loadServerHistoryFromDB = async () => {
  const [error, res] = await dexie.get(props.id || '000');
  if (error) {
    return;
  }

  serversFromIndexDB.value = (res?.data || []).map(item => {
    return {
      ...item,
      id: utils.uuid()
    };
  });
};

/**
 * Load API server source enumeration
 * <p>Placeholder for future server source enum loading</p>
 */
// eslint-disable-next-line @typescript-eslint/no-empty-function
const loadApiServerSourceEnum = async () => { };

/**
 * Handle API request execution
 * <p>Manages server history and triggers request emission</p>
 * <p>Automatically saves new servers to history (max 10 entries)</p>
 */
const executeApiRequest = async () => {
  // Skip history management if server already exists or no current server
  if (!currentHttpServer.value || serversFromIndexDB.value.find(item => item.url === serverValue.value)) {
    emit('sendRequest');
    return;
  }

  // Add new server to history
  const currentServerCopy = JSON.parse(JSON.stringify(currentHttpServer.value));
  delete currentServerCopy[idKey];
  serversFromIndexDB.value.push({ ...currentServerCopy, url: serverValue.value });

  // Maintain history limit (keep last 10 entries)
  if (serversFromIndexDB.value.length > 10) {
    serversFromIndexDB.value = serversFromIndexDB.value.slice(-10);
  }

  // Save updated history to IndexedDB
  const data = toRaw(serversFromIndexDB.value);
  await dexie.add({ id: props.id || '000', data });
  emit('sendRequest');
};

/**
 * Handle Enter key press on API URI input
 * <p>Triggers request execution if user has debug permission</p>
 */
const handleUriEnterKey = (): void => {
  if (!auths.value.includes(ApiPermission.DEBUG)) {
    return;
  }

  setTimeout(() => {
    executeApiRequest();
  });
};

/**
 * Handle Enter key press on server input
 * <p>Updates current server and triggers request if user has debug permission</p>
 */
const handleServerEnterKey = () => {
  emit('update:currentServer', currentHttpServer.value);
  if (!auths.value.includes(ApiPermission.DEBUG)) {
    return;
  }

  setTimeout(() => {
    executeApiRequest();
  });
};

/**
 * Handle URI input blur event
 * <p>Parses endpoint when user finishes editing URI</p>
 * @param e - The change event from input
 */
const handleUriInputBlur = (e: ChangeEvent): void => {
  const value = e.target.value;
  if (value === props.endpoint) {
    return;
  }

  parseEndpointUri(value);
};

/**
 * Handle server input blur event
 * <p>Updates server URL and hides dropdown with delay</p>
 */
const handleServerInputBlur = () => {
  currentHttpServer.value.url = serverValue.value;
  emit('update:currentServer', currentHttpServer.value);
  setTimeout(() => {
    showServerListDrop.value = false;
  }, 150);
};

/**
 * Handle server input focus event
 * <p>Shows server dropdown when user focuses on input</p>
 */
const handleServerInputFocus = () => {
  showServerListDrop.value = true;
};

/**
 * Handle Enter key press on path input
 * <p>Blurs input when Enter is pressed to trigger validation</p>
 * @param event - The keyboard event
 */
const handlePathEnterKey = (event: KeyboardEvent): void => {
  const code = event.code.toLowerCase();
  if (!['numpadenter', 'enter'].includes(code)) {
    return;
  }

  (event.target as HTMLElement)?.blur();
};

/**
 * Handle request abort
 * <p>Emits abort event to cancel ongoing request</p>
 */
const handleRequestAbort = () => {
  emit('abort');
};

/**
 * Handle URL copy action
 * <p>Emits copyUrl event to copy current URL to clipboard</p>
 */
const handleCopyUrl = async () => {
  emit('copyUrl');
};

/**
 * Handle guide step progression
 * <p>Manages user guide flow and triggers appropriate actions</p>
 * @param key - The guide step key
 */
const handleGuideStep = (key: string) => {
  if (key === 'debugApiThree') {
    executeApiRequest();
    const guideTimer = setTimeout(() => {
      updateGuideStep({ visible: true, key });
      clearTimeout(guideTimer);
    }, 300);
  } else {
    if (key === 'debugApiSix') {
      emit('archived');
    }
    updateGuideStep({ visible: true, key });
  }
};

/**
 * Open add case modal
 * <p>Shows the modal for generating test cases</p>
 */
const openAddCaseModal = () => {
  addCaseVisible.value = true;
};

/**
 * Computed property for server list options
 * <p>Combines servers from all sources: parent, history, and mock</p>
 */
const serverListOptions = computed(() => {
  return [...serversFromParent.value, ...serversFromIndexDB.value, ...serversFromMock.value];
});

onMounted(() => {
  // Initialize component data
  loadHttpMethodOptions();
  loadServerHistoryFromDB();
  loadApiServerSourceEnum();

  watch(() => props.endpoint, (newValue) => {
    apiUri.value = newValue;
  }, {
    immediate: true
  });

  watch(() => props.currentServer, (newValue) => {
    currentHttpServer.value = JSON.parse(JSON.stringify(newValue));
    serverValue.value = newValue.url;
  }, {
    immediate: true
  });

  watch(() => props.method, (newValue) => {
    apiMethod.value = newValue;
  }, {
    immediate: true
  });

  watch(() => apiMethod.value, (newValue) => {
    emit('update:method', newValue);
  }, {
    immediate: true
  });

  watch(() => props.availableServers, (newValue) => {
    serversFromParent.value = (newValue || [])
      .filter(item => item[serverSourceKey] === ApiServerSource.PARENT_SERVERS).map(item => {
        return {
          ...item,
          ...item.extentions,
          id: utils.uuid()
        };
      });

    serversFromMock.value = (newValue || [])
      .filter(item => item[serverSourceKey] === ApiServerSource.MOCK_SERVICE).map(item => {
        return {
          ...item,
          ...item.extentions,
          id: utils.uuid()
        };
      });
  }, {
    immediate: true
  });
});
</script>
<template>
  <div class="flex flex-nowrap whitespace-nowrap justify-between ">
    <div class="flex flex-nowrap whitespace-nowrap border overflow-hidden rounded flex-1">
      <Select
        v-model:value="apiMethod"
        class="method-select w-23"
        style="height:30px;"
        :allowClear="false"
        size="middle"
        :options="httpMethodOptions" />
      <Divider type="vertical" class="h-auto mx-0" />
      <Dropdown :visible="showServerListDrop && !!serverListOptions.length">
        <ServerInput
          v-model:value="serverValue"
          :valueObj="currentHttpServer"
          class="w-1/3"
          @enterPress="handleServerEnterKey"
          @focus="handleServerInputFocus"
          @handleBlur="handleServerInputBlur" />
        <template #overlay>
          <div class="px-3.5 py-3.5 text-3 bg-white rounded leading-5 space-y-4" style="box-shadow: 0 3px 6px -4px #0000001f, 0 6px 16px #00000014, 0 9px 28px 8px #0000000d;">
            <div v-if="props.defaultCurrentServer?.url">
              <div class="flex items-center mb-1 leading-6">
                <div class="flex items-center whitespace-nowrap bg-bg-table-head px-2 mr-2 rounded text-text-title font-medium">
                  <Icon icon="icon-dangqian" class="mr-1" />
                  <span>{{ t('service.apiServerPath.labels.current') }}</span>
                </div>
                <div class="flex-1 h-0.25 border-t border-dashed border-border-divider"></div>
              </div>
              <div class="leading-7 px-2 cursor-pointer hover:bg-bg-hover rounded" @click="handleServerSelection(props.defaultCurrentServer?.url, props.defaultCurrentServer)">
                {{ props.defaultCurrentServer?.url }}
              </div>
            </div>

            <div v-if="!!serversFromParent.length">
              <div class="flex items-center mb-1 leading-6">
                <div class="flex items-center whitespace-nowrap bg-bg-table-head px-2 mr-2 rounded text-text-title font-medium">
                  <Icon icon="icon-dangqian" class="mr-1" />
                  <span>{{ t('service.apiServerPath.labels.public') }}</span>
                </div>
                <div class="flex-1 h-0.25 border-t border-dashed border-border-divider"></div>
              </div>

              <div>
                <div
                  v-for="item in serversFromParent"
                  :key="item.id"
                  class="leading-7 px-2 cursor-pointer hover:bg-bg-hover rounded"
                  @click="handleServerSelection(item.url, item)">
                  {{ item.url }}
                </div>
              </div>
            </div>

            <div v-if="!!serversFromMock.length">
              <div class="flex items-center mb-1 leading-6">
                <div class="flex items-center whitespace-nowrap bg-bg-table-head px-2 mr-2 rounded text-text-title font-medium">
                  <Icon icon="icon-dangqian" class="mr-1" />
                  <span>{{ t('service.apiServerPath.labels.mock') }}</span>
                </div>
                <div class="flex-1 h-0.25 border-t border-dashed border-border-divider"></div>
              </div>

              <div
                v-for="item in serversFromMock"
                :key="item.id"
                class="leading-7 px-2 cursor-pointer hover:bg-bg-hover rounded"
                @click="handleServerSelection(item.url, item)">
                {{ item.url }}
              </div>
            </div>

            <div v-if="!!serversFromIndexDB.length">
              <div class="flex items-center mb-1 leading-6">
                <div class="flex items-center whitespace-nowrap bg-bg-table-head px-2 mr-2 rounded text-text-title font-medium">
                  <Icon icon="icon-dangqian" class="mr-1" />
                  <span>{{ t('service.apiServerPath.labels.history') }}</span>
                </div>
                <div class="flex-1 h-0.25 border-t border-dashed border-border-divider"></div>
              </div>

              <div
                v-for="item in serversFromIndexDB"
                :key="item.id"
                class="leading-7 px-2 cursor-pointer hover:bg-bg-hover rounded"
                @click="handleServerSelection(item.url, item)">
                {{ item.url }}
              </div>
            </div>
          </div>
        </template>
      </Dropdown>
      <Input
        v-model:value="apiUri"
        :maxlength="API_URI_MAX_LENGTH"
        :allowClear="false"
        trim
        class="input-container min-w-25"
        placeholder="接口路径，以“/”开始"
        size="middle"
        @blur="(e: FocusEvent) => handleUriInputBlur(e as any)"
        @pressEnter="handleUriEnterKey"
        @keypress="handlePathEnterKey" />
    </div>
    <div class="flex flex-nowrap whitespace-nowrap flex-freeze ml-3 space-x-2">
      <Tooltip
        :visible="stepVisible && stepKey === 'debugApiTwo'"
        placement="bottomLeft"
        destroyTooltipOnHide>
        <template #title>
          <div class="p-2 text-3">
            <div class="text-4 text-text-title">{{ stepContent.title }}</div>
            <div class="mt-2">{{ stepContent.content }}</div>
            <div class="flex justify-end mt-5">
              <Button
                size="small"
                type="primary"
                @click="handleGuideStep('debugApiThree')">
                {{ t('actions.nextStep') }}
              </Button>
            </div>
          </div>
        </template>
        <Button
          v-if="!props.loading"
          type="primary"
          :disabled="!auths.includes(ApiPermission.DEBUG)"
          @click="executeApiRequest">
          <template #icon>
            <Icon class="mr-2" icon="icon-fasong" />
          </template>{{ t('service.apiServerPath.actions.sendRequest') }}
        </Button>
        <Spin
          v-else
          indicator="circle"
          class="cursor-pointer"
          :spinning="props.loading"
          @click="handleRequestAbort">
          <Button type="primary" danger>
            <template #icon>
              <Icon class="mr-2" icon="icon-duankai" />
            </template>
            <span>{{ t('service.apiServerPath.actions.abortRequest') }}</span>
          </Button>
        </Spin>
      </Tooltip>
      <template v-if="props.isUnarchivedApi">
        <template v-if="props.id">
          <Button
            class="ml-2"
            :disabled="!auths.includes(ApiPermission.MODIFY)"
            @click="emit('save')">
            {{ t('actions.save') }}
          </Button>
          <Button
            class="ml-2"
            :disabled="!auths.includes(ApiPermission.MODIFY)"
            @click="emit('archived')">
            {{ t('service.apiServerPath.actions.archive') }}
          </Button>
        </template>
        <template v-else>
          <Button
            class="ml-2"
            :disabled="!auths.includes(ApiPermission.MODIFY)"
            @click="emit('save')">
            {{ t('service.apiServerPath.actions.saveToUnarchived') }}
          </Button>
          <Tooltip
            :visible="stepVisible && stepKey === 'debugApiFour'"
            placement="bottomRight"
            destroyTooltipOnHide>
            <template #title>
              <div class="p-2 text-3">
                <div class="text-4 text-text-title">{{ stepContent.title }}</div>
                <div class="mt-2">{{ stepContent.content }}</div>
                <div class="flex justify-end mt-5">
                  <Button
                    size="small"
                    type="primary"
                    @click="handleGuideStep('debugApiSix')">
                    {{ t('actions.nextStep') }}
                  </Button>
                </div>
              </div>
            </template>
            <Button
              class="ml-2"
              :disabled="!auths.includes(ApiPermission.MODIFY)"
              @click="emit('archived')">
              {{ t('service.apiServerPath.actions.archive') }}
            </Button>
          </Tooltip>
        </template>
      </template>
      <template v-else>
        <Button
          class="ml-2"
          :disabled="!auths.includes(ApiPermission.MODIFY)"
          @click="emit('save')">
          <template #icon>
            <Icon class="mr-2" icon="icon-baocun" />
          </template>
          {{ t('actions.save') }}
        </Button>
        <Button
          class="ml-2"
          :disabled="!auths.includes(ApiPermission.MODIFY)"
          @click="openAddCaseModal">
          {{ t('service.apiServerPath.actions.generateCase') }}
        </Button>
      </template>
      <Button @click="handleCopyUrl">
        {{ t('service.apiServerPath.actions.copyUrl') }}
      </Button>
      <AsyncComponent :visible="addCaseVisible">
        <AddCaseModal
          v-model:visible="addCaseVisible"
          :apisId="props.id">
        </addcasemodal>
      </AsyncComponent>
    </div>
  </div>
</template>
<style scoped>
:deep(.ant-dropdown-menu-item) {
  padding: 2px !important;
  font-size: 12px !important;
  line-height: 20px;
}

:deep(.ant-dropdown-menu-item .ant-dropdown-menu-title-content) {
  word-break: break-all;
  white-space: break-spaces;
}

:deep(.method-select .ant-select-selector) {
  border: none !important;
}

:deep(.method-select.ant-select:not(.ant-select-disabled):hover .ant-select-selector) {
  border-right: none !important;
}

.input-container {
  position: relative;
  flex: 1 1 30%;
  padding-right: 12px;
  padding-left: 13px;
  border-style: none;
}

.input-container+.input-container {
  flex: 1 1 55%;
}

.input-container:focus,
.ant-input-affix-wrapper-focused {
  box-shadow: none;
}
</style>
../RequestParams/interface
