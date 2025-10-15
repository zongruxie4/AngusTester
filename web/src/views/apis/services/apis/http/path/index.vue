<script setup lang="ts">
import { computed, defineAsyncComponent, inject, onMounted, ref, toRaw, watch } from 'vue';
import { AsyncComponent, Icon, Input, Spin, Tooltip, VuexHelper, Select } from '@xcan-angus/vue-ui';
import { XCanDexie, utils, enumUtils, HttpMethod } from '@xcan-angus/infra';
import { Button, Divider, Dropdown } from 'ant-design-vue';
import { useI18n } from 'vue-i18n';

import { API_EXTENSION_KEY } from '@/utils/apis';
import { getDefaultParams } from '@/views/apis/services/apis/http/RequestParameter';
import { HttpServer } from './PropsType';
import { Method } from '../interface';

import ServerInput from './ServerInput.vue';

const AddCaseModal = defineAsyncComponent(() => import('@/views/apis/services/components/case/AddCaseModal.vue'));

const { serverSourceKey, valueKey, idKey } = API_EXTENSION_KEY;

const { useMutations, useState } = VuexHelper;

interface Props {
  loading: boolean;
  isUnarchivedApi?: boolean;
  id: string;
  endpoint: string;
  availableServers: any[];
  currentServer: HttpServer;
  defaultCurrentServer: HttpServer;
  method: Method;
}

const props = withDefaults(defineProps<Props>(), {
  loading: false,
  isUnarchivedApi: true,
  id: '',
  method: 'GET',
  currentServer: () => ({ url: '', id: utils.uuid() }),
  availableServers: () => ([])
});
const { t } = useI18n();

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'sendRequest'): void;
  (e: 'update:loading', value: boolean): void;
  (e: 'save'): void;
  (e: 'archived'): void;
  (e: 'update:endpoint', value: string): void;
  (e: 'update:currentServer', value: HttpServer): void;
  (e: 'update:method', value: string): void;
  (e: 'abort'): void;
  (e: 'addQuery', value: Record<string, string>[]): void;
  (e: 'copyUrl'): void;
}>();

const globalConfigs = inject('globalConfigs', { VITE_URI_MAX_LENGTH: 800 });
const auths = inject('auths', ref<string[]>([]));
const { stepVisible, stepKey, stepContent }: { stepVisible: boolean; stepKey: string; stepContent: { title: string; content: string; } } = useState(['stepVisible', 'stepKey', 'stepContent'], 'guideStore');
const { updateGuideStep } = useMutations(['updateGuideStep'], 'guideStore');

const dexie = new XCanDexie<{ id: string; data: any }>('serverUrl');

const showServerListDrop = ref(false);
const addCaseVisible = ref(false); // 生成用例弹窗visible
const apiMethod = ref<Method>('GET');

const currentHttpServer = ref<HttpServer>({ url: '', id: utils.uuid() });
const serverValue = ref<string>('');
const apiUri = ref<string>(); // uri整体
const serversFromParent = ref<HttpServer[]>([]);
const serversFromMock = ref<HttpServer[]>([]);
const serversFromIndexDB = ref<HttpServer[]>([]);

const recognizeUri = (endpoint: string) => {
  try {
    const urlObj = new URL(endpoint);
    serverValue.value = urlObj.origin;
    currentHttpServer.value.url = serverValue.value;
    apiUri.value = urlObj.pathname;

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
    emit('update:currentServer', currentHttpServer.value);
    emit('update:endpoint', apiUri.value || '');
  }
};

const handleSelectServer = (value: string, item: HttpServer) => {
  currentHttpServer.value = JSON.parse(JSON.stringify(item));
  serverValue.value = value;
  emit('update:currentServer', item);
};

const httpMethodOpt = ref<{value: string; label: string}[]>([]);
const loadHttpMethodOpt = () => {
  const data = enumUtils.enumToMessages(HttpMethod);
  httpMethodOpt.value = data.map(i => ({ value: i.value, label: i.message }));
};

// 前端数据库存储的 server
const loadServerFromDB = async () => {
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

// SERVER 来源枚举
// eslint-disable-next-line @typescript-eslint/no-empty-function
const loadApiServerSourceEnum = async () => { };

const handleRequest = async () => {
  if (!currentHttpServer.value || serversFromIndexDB.value.find(item => item.url === serverValue.value)) {
    emit('sendRequest');
    return;
  }

  const _currentServer = JSON.parse(JSON.stringify(currentHttpServer.value));
  delete _currentServer[idKey];
  serversFromIndexDB.value.push({ ..._currentServer, url: serverValue.value });

  if (serversFromIndexDB.value.length > 10) {
    serversFromIndexDB.value = serversFromIndexDB.value.slice(-10);
  }

  const data = toRaw(serversFromIndexDB.value);
  await dexie.add({ id: props.id || '000', data });
  emit('sendRequest');
};

// apiUri 回车
const handleEnter = (): void => {
  if (!auths.value.includes('DEBUG')) {
    return;
  }

  setTimeout(() => {
    handleRequest();
  });
};

// currentServer 回车
const handleServerEnter = () => {
  emit('update:currentServer', currentHttpServer.value);
  if (!auths.value.includes('DEBUG')) {
    return;
  }

  setTimeout(() => {
    handleRequest();
  });
};

const handleUriBlur = (e: ChangeEvent): void => {
  const value = e.target.value;
  if (value === props.endpoint) {
    return;
  }

  recognizeUri(value);
};

const handleServerBlur = () => {
  currentHttpServer.value.url = serverValue.value;
  emit('update:currentServer', currentHttpServer.value);
  setTimeout(() => {
    showServerListDrop.value = false;
  }, 150);
};

const handleFocus = () => {
  showServerListDrop.value = true;
};

const pathEnterHandler = (event: KeyboardEvent): void => {
  const code = event.code.toLowerCase();
  if (!['numpadenter', 'enter'].includes(code)) {
    return;
  }

  event.target.blur();
};

const handleAbort = () => {
  emit('abort');
};

const copyUrl = async () => {
  emit('copyUrl');
};

const guideStep = (key: string) => {
  if (key === 'debugApiThree') {
    handleRequest();
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

const openCaseModal = () => {
  addCaseVisible.value = true;
};

onMounted(() => {
  loadHttpMethodOpt();
  loadServerFromDB();
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
    serversFromParent.value = (newValue || []).filter(item => item[serverSourceKey] === 'PARENT_SERVERS').map(item => {
      return {
        ...item,
        ...item.extentions,
        id: utils.uuid()
      };
    });

    serversFromMock.value = (newValue || []).filter(item => item[serverSourceKey] === 'MOCK_SERVICE').map(item => {
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

const serverListOpt = computed(() => {
  return [...serversFromParent.value, ...serversFromIndexDB.value, ...serversFromMock.value];
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
        size="normal"
        :options="httpMethodOpt" />
      <Divider type="vertical" class="h-auto mx-0" />
      <Dropdown :visible="showServerListDrop && !!serverListOpt.length">
        <ServerInput
          v-model:value="serverValue"
          :valueObj="currentHttpServer"
          class="w-1/3"
          @enterPress="handleServerEnter"
          @focus="handleFocus"
          @handleBlur="handleServerBlur" />
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
              <div class="leading-7 px-2 cursor-pointer hover:bg-bg-hover rounded" @click="handleSelectServer(props.defaultCurrentServer?.url, props.defaultCurrentServer)">
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
                  @click="handleSelectServer(item.url, item)">
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
                @click="handleSelectServer(item.url, item)">
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
                @click="handleSelectServer(item.url, item)">
                {{ item.url }}
              </div>
            </div>
          </div>
        </template>
      </Dropdown>
      <Input
        v-model:value="apiUri"
        :maxlength="globalConfigs.VITE_URI_MAX_LENGTH"
        :allowClear="false"
        trim
        class="input-container min-w-25"
        placeholder="接口路径，以 “ / ” 起始"
        size="default"
        @blur="handleUriBlur"
        @pressEnter="handleEnter"
        @keypress="pathEnterHandler" />
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
                @click="guideStep('debugApiThree')">
                {{ t('actions.nextStep') }}
              </Button>
            </div>
          </div>
        </template>
        <Button
          v-if="!props.loading"
          type="primary"
          :disabled="!auths.includes('DEBUG')"
          @click="handleRequest">
          <template #icon>
            <Icon class="mr-2" icon="icon-fasong" />
          </template>{{ t('service.apiServerPath.actions.sendRequest') }}
        </Button>
        <Spin
          v-else
          indicator="circle"
          class="cursor-pointer"
          :spinning="props.loading"
          @click="handleAbort">
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
            :disabled="!auths.includes('MODIFY')"
            @click="emit('save')">
            {{ t('actions.save') }}
          </Button>
          <Button
            class="ml-2"
            :disabled="!auths.includes('MODIFY')"
            @click="emit('archived')">
            {{ t('service.apiServerPath.actions.archive') }}
          </Button>
        </template>
        <template v-else>
          <Button
            class="ml-2"
            :disabled="!auths.includes('MODIFY')"
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
                    @click="guideStep('debugApiSix')">
                    {{ t('actions.nextStep') }}
                  </Button>
                </div>
              </div>
            </template>
            <Button
              class="ml-2"
              :disabled="!auths.includes('MODIFY')"
              @click="emit('archived')">
              {{ t('service.apiServerPath.actions.archive') }}
            </Button>
          </Tooltip>
        </template>
      </template>
      <template v-else>
        <Button
          class="ml-2"
          :disabled="!auths.includes('MODIFY')"
          @click="emit('save')">
          <template #icon>
            <Icon class="mr-2" icon="icon-baocun" />
          </template>
          {{ t('actions.save') }}
        </Button>
        <Button
          class="ml-2"
          :disabled="!auths.includes('MODIFY')"
          @click="openCaseModal">
          {{ t('service.apiServerPath.actions.generateCase') }}
        </Button>
      </template>
      <Button @click="copyUrl">
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
