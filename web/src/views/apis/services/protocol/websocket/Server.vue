<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, toRaw, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Icon } from '@xcan-angus/vue-ui';
import { XCanDexie } from '@xcan-angus/infra';
import { Dropdown, Menu, MenuItem } from 'ant-design-vue';
import { API_EXTENSION_KEY } from '@/utils/apis';

import ServerInput from '@/views/apis/services/protocol/http/path/ServerInput.vue';

type HttpServer = {
  url: string;
  variables?: any,
  description?: string
  'x-xc-serverSource'?: string;
}

interface Props {
  isUnarchivedApi?: boolean;
  id: string;
  availableServers: any[];
  currentServer: HttpServer;
  defaultCurrentServer: HttpServer;
  readonly: boolean;
}

const { t } = useI18n();
const props = withDefaults(defineProps<Props>(), {
  isUnarchivedApi: true,
  id: '',
  method: 'GET',
  currentServer: () => ({ url: '' }),
  availableServers: () => ([]),
  readonly: false
});

const emit = defineEmits<{
  (e: 'sendRequest'):void;
  (e: 'save'): void;
  (e: 'update:currentServer', value: HttpServer): void;
  (e: 'update:method', value: string): void;
  (e: 'abort'): void;
}>();

const dexie = new XCanDexie<{id:string;data: any}>('serverUrl');

const { serverSourceKey } = API_EXTENSION_KEY;
const showServerListDrop = ref(false);
const currentServer = ref<HttpServer>({ url: '' });
const serverValue = ref<string>(props.currentServer?.url || '');
const serversFromParent = ref<HttpServer[]>([]);
const serversFromMock = ref<HttpServer[]>([]);
const serversFromIndexDB = ref<HttpServer[]>([]);

watch(() => props.currentServer, newValue => {
  currentServer.value = JSON.parse(JSON.stringify(newValue));
  serverValue.value = newValue.url;
}, {
  immediate: true
});

watch(() => props.availableServers, newValue => {
  serversFromParent.value = (newValue || []).filter(i => i[serverSourceKey] === 'PARENT_SERVERS').map(i => {
    return {
      ...i,
      ...i.extentions
    };
  });
  serversFromMock.value = (newValue || []).filter(i => i[serverSourceKey] === 'MOCK_SERVICE').map(i => {
    return {
      ...i,
      ...i.extentions
    };
  });
}, {
  immediate: true
});

const serverListOpt = computed(() => {
  return [...serversFromParent.value, ...serversFromIndexDB.value, ...serversFromMock.value];
});

const handleSelectServer = (value: string, item: HttpServer) => {
  currentServer.value = JSON.parse(JSON.stringify(item));
  serverValue.value = value;
  emit('update:currentServer', item);
};

// 前端数据库存储的 server
const loadServerFromDB = async () => {
  const [error, resp] = await dexie.get(props.id || '000');
  if (error) {
    return;
  }
  serversFromIndexDB.value = resp?.data || [];
};

// SERVER 来源枚举
// eslint-disable-next-line @typescript-eslint/no-empty-function
const loadApiServerSourceEnum = async () => {
};

const handleRequest = async () => {
  if (!currentServer.value || serversFromIndexDB.value.find(i => i.url === serverValue.value)) {
    emit('sendRequest');
    return;
  }
  serversFromIndexDB.value.push({ ...currentServer.value, url: serverValue.value });
  if (serversFromIndexDB.value.length > 10) {
    serversFromIndexDB.value = serversFromIndexDB.value.slice(-10);
  }
  const data = toRaw(serversFromIndexDB.value);
  await dexie.add({ id: props.id || '000', data });
  emit('sendRequest');
};

const handleServerEnter = () => {
  emit('update:currentServer', currentServer.value);
  setTimeout(() => {
    handleRequest();
  });
};

const handleServerBlur = () => {
  currentServer.value.url = serverValue.value;
  emit('update:currentServer', currentServer.value);
  setTimeout(() => {
    showServerListDrop.value = false;
  }, 150);
};

const handleFocus = () => {
  showServerListDrop.value = true;
};

onMounted(() => {
  loadServerFromDB();
  loadApiServerSourceEnum();
});

onBeforeUnmount(() => {
  showServerListDrop.value = false;
});
</script>
<template>
  <Dropdown :visible="showServerListDrop && !!serverListOpt.length">
    <ServerInput
      v-model:value="serverValue"
      :valueObj="currentServer"
      :readonly="props.readonly"
      class="w-100"
      @enterPress="handleServerEnter"
      @focus="handleFocus"
      @handleBlur="handleServerBlur" />
    <template #overlay>
      <Menu>
        <div class="px-2 text-3">
          <div v-show="props.defaultCurrentServer?.url">
            <div class="border-t border-border-divider border-dashed relative my-3.5">
              <div class="absolute -top-2.5 whitespace-nowrap bg-bg-table-head px-2 rounded leading-5 text-text-title font-medium"><Icon icon="icon-dangqian" class="mr-1" />{{ t('service.webSocketServer.labels.current') }}</div>
            </div>
            <MenuItem
              :key="props.defaultCurrentServer?.url"
              style="max-width: 380px;"
              @click="handleSelectServer(props.defaultCurrentServer?.url, props.defaultCurrentServer)">
              {{ props.defaultCurrentServer?.url }}
            </MenuItem>
          </div>
          <div v-show="!!serversFromParent.length">
            <div class="border-t border-border-divider border-dashed relative mb-3.5 mt-5">
              <div class="absolute -top-2.5 whitespace-nowrap bg-bg-table-head px-2 rounded leading-5 text-text-title font-medium"><Icon icon="icon-changjingguanli" class="mr-1" />{{ t('service.webSocketServer.labels.public') }}</div>
            </div>
            <MenuItem
              v-for="item in serversFromParent"
              :key="item.url"
              style="max-width: 380px;"
              @click="handleSelectServer(item.url, item)">
              {{ item.url }}
            </MenuItem>
          </div>
          <div v-show="!!serversFromMock.length">
            <div class="border-t border-border-divider border-dashed relative mb-3.5 mt-5">
              <div class="absolute -top-2.5 whitespace-nowrap bg-bg-table-head px-2 rounded leading-5 text-text-title font-medium"><Icon icon="icon-changjingguanli" class="mr-1" />{{ t('service.webSocketServer.labels.mock') }}</div>
            </div>
            <MenuItem
              v-for="item in serversFromMock"
              :key="item.url"
              style="max-width: 380px;"
              @click="handleSelectServer(item.url, item)">
              {{ item.url }}
            </MenuItem>
          </div>
          <div v-show="!!serversFromIndexDB.length">
            <div class="border-t border-border-divider border-dashed relative mb-3.5 mt-5">
              <div class="absolute -top-2.5 whitespace-nowrap bg-bg-table-head px-2 rounded leading-5 text-text-title font-medium"><Icon icon="icon-lishijilu" class="mr-1" />{{ t('service.webSocketServer.labels.history') }}</div>
            </div>
            <MenuItem
              v-for="item in serversFromIndexDB"
              :key="item.url"
              style="max-width: 380px;"
              @click="handleSelectServer(item.url, item)">
              {{ item.url }}
            </MenuItem>
          </div>
        </div>
      </Menu>
    </template>
  </Dropdown>
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
</style>
