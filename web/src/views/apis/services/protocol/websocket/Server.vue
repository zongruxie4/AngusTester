<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, toRaw, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Icon } from '@xcan-angus/vue-ui';
import { XCanDexie, HttpMethod } from '@xcan-angus/infra';
import { Dropdown, Menu, MenuItem } from 'ant-design-vue';
import { API_EXTENSION_KEY } from '@/utils/apis';
import { ServerInfo } from '@/views/apis/server/types';
import { ApiServerSource } from '@/enums/enums';

import ServerInput from '@/views/apis/services/protocol/http/path/ServerInput.vue';

interface Props {
  isUnarchivedApi?: boolean;
  id: string;
  availableServers: any[];
  currentServer: ServerInfo;
  defaultCurrentServer: ServerInfo;
  readonly: boolean;
}

const { t } = useI18n();

const props = withDefaults(defineProps<Props>(), {
  isUnarchivedApi: true,
  id: '',
  method: HttpMethod.GET,
  currentServer: () => ({ url: '' }),
  availableServers: () => ([]),
  readonly: false
});

const emit = defineEmits<{
  (e: 'sendRequest'):void;
  (e: 'save'): void;
  (e: 'update:currentServer', value: ServerInfo): void;
  (e: 'update:method', value: string): void;
  (e: 'abort'): void;
}>();

const dexie = new XCanDexie<{id:string;data: any}>('serverUrl');

const { serverSourceKey } = API_EXTENSION_KEY;

const isServerDropdownVisible = ref(false);

const selectedServer = ref<ServerInfo>({ url: '' });

const serverUrlInput = ref<string>(props.currentServer?.url || '');

/**
 * Server lists from different sources
 */
const parentServers = ref<ServerInfo[]>([]);
const mockServers = ref<ServerInfo[]>([]);
const historicalServers = ref<ServerInfo[]>([]);

/**
 * Combined list of all available servers for dropdown display
 * <p>
 * Merges servers from parent, mock, and historical sources in priority order
 * </p>
 */
const availableServerOptions = computed(() => {
  return [...parentServers.value, ...historicalServers.value, ...mockServers.value];
});

/**
 * Handles server selection from dropdown
 * <p>
 * Updates the selected server and emits the change to parent component
 * </p>
 * @param serverUrl - The selected server URL
 * @param serverInfo - The complete server information object
 */
const handleServerSelection = (serverUrl: string, serverInfo: ServerInfo) => {
  selectedServer.value = JSON.parse(JSON.stringify(serverInfo));
  serverUrlInput.value = serverUrl;
  emit('update:currentServer', serverInfo);
};

/**
 * Loads server history from IndexedDB
 * <p>
 * Retrieves previously used servers for the current API and populates the historical servers list
 * </p>
 */
const loadServerHistoryFromDB = async () => {
  const [error, response] = await dexie.get(props.id || '000');
  if (error) {
    console.warn('Failed to load server history from database:', error);
    return;
  }
  historicalServers.value = response?.data || [];
};

/**
 * Handles request sending with server history management
 * <p>
 * Sends the request and optionally saves the current server to history if it's not already stored
 * </p>
 */
const handleRequestSubmission = async () => {
  if (!selectedServer.value || historicalServers.value.find(server => server.url === serverUrlInput.value)) {
    emit('sendRequest');
    return;
  }

  // Add current server to history
  historicalServers.value.push({ ...selectedServer.value, url: serverUrlInput.value });

  // Maintain history limit of 10 servers
  if (historicalServers.value.length > 10) {
    historicalServers.value = historicalServers.value.slice(-10);
  }

  // Save updated history to database
  const historyData = toRaw(historicalServers.value);
  await dexie.add({ id: props.id || '000', data: historyData });
  emit('sendRequest');
};

/**
 * Handles server input enter key press
 * <p>
 * Updates the current server and triggers request submission
 * </p>
 */
const handleServerInputEnter = () => {
  emit('update:currentServer', selectedServer.value);
  setTimeout(() => {
    handleRequestSubmission();
  });
};

/**
 * Handles server input blur event
 * <p>
 * Updates server URL and hides the dropdown with a slight delay
 * </p>
 */
const handleServerInputBlur = () => {
  selectedServer.value.url = serverUrlInput.value;
  emit('update:currentServer', selectedServer.value);
  setTimeout(() => {
    isServerDropdownVisible.value = false;
  }, 150);
};

/**
 * Handles server input focus event
 * <p>
 * Shows the server dropdown when input is focused
 * </p>
 */
const handleServerInputFocus = () => {
  isServerDropdownVisible.value = true;
};

/**
 * Watches for changes in current server prop
 * <p>
 * Updates the selected server and input value when parent component changes the current server
 * </p>
 */
watch(() => props.currentServer, newValue => {
  selectedServer.value = JSON.parse(JSON.stringify(newValue));
  serverUrlInput.value = newValue.url;
}, {
  immediate: true
});

/**
 * Watches for changes in available servers prop
 * <p>
 * Categorizes available servers by source (parent, mock) and updates respective lists
 * </p>
 */
watch(() => props.availableServers, newValue => {
  parentServers.value = (newValue || [])
    .filter(server => server[serverSourceKey] === ApiServerSource.PARENT_SERVERS).map(server => {
      return {
        ...server,
        ...server.extentions
      };
    });
  mockServers.value = (newValue || [])
    .filter(server => server[serverSourceKey] === ApiServerSource.MOCK_SERVICE).map(server => {
      return {
        ...server,
        ...server.extentions
      };
    });
}, {
  immediate: true
});

/**
 * Component mounted lifecycle hook
 * <p>
 * Initializes server history and API server source enumeration
 * </p>
 */
onMounted(() => {
  loadServerHistoryFromDB();
});

/**
 * Component before unmount lifecycle hook
 * <p>
 * Cleans up UI state when component is being destroyed
 * </p>
 */
onBeforeUnmount(() => {
  isServerDropdownVisible.value = false;
});
</script>
<template>
  <Dropdown :visible="isServerDropdownVisible && !!availableServerOptions.length">
    <ServerInput
      v-model:value="serverUrlInput"
      :valueObj="selectedServer"
      :readonly="props.readonly"
      class="w-100"
      @enterPress="handleServerInputEnter"
      @focus="handleServerInputFocus"
      @handleBlur="handleServerInputBlur" />
    <template #overlay>
      <Menu>
        <div class="px-2 text-3">
          <div v-show="props.defaultCurrentServer?.url">
            <div class="border-t border-border-divider border-dashed relative my-3.5">
              <div class="absolute -top-2.5 whitespace-nowrap bg-bg-table-head px-2 rounded leading-5 text-text-title font-medium">
                <Icon icon="icon-dangqian" class="mr-1" />{{ t('service.apiServerPath.labels.current') }}
              </div>
            </div>
            <MenuItem
              :key="props.defaultCurrentServer?.url"
              style="max-width: 380px;"
              @click="handleServerSelection(props.defaultCurrentServer?.url, props.defaultCurrentServer)">
              {{ props.defaultCurrentServer?.url }}
            </MenuItem>
          </div>
          <div v-show="!!parentServers.length">
            <div class="border-t border-border-divider border-dashed relative mb-3.5 mt-5">
              <div class="absolute -top-2.5 whitespace-nowrap bg-bg-table-head px-2 rounded leading-5 text-text-title font-medium">
                <Icon icon="icon-changjingguanli" class="mr-1" />{{ t('service.apiServerPath.labels.public') }}
              </div>
            </div>
            <MenuItem
              v-for="server in parentServers"
              :key="server.url"
              style="max-width: 380px;"
              @click="handleServerSelection(server.url, server)">
              {{ server.url }}
            </MenuItem>
          </div>
          <div v-show="!!mockServers.length">
            <div class="border-t border-border-divider border-dashed relative mb-3.5 mt-5">
              <div class="absolute -top-2.5 whitespace-nowrap bg-bg-table-head px-2 rounded leading-5 text-text-title font-medium">
                <Icon icon="icon-changjingguanli" class="mr-1" />{{ t('service.apiServerPath.labels.mock') }}
              </div>
            </div>
            <MenuItem
              v-for="server in mockServers"
              :key="server.url"
              style="max-width: 380px;"
              @click="handleServerSelection(server.url, server)">
              {{ server.url }}
            </MenuItem>
          </div>
          <div v-show="!!historicalServers.length">
            <div class="border-t border-border-divider border-dashed relative mb-3.5 mt-5">
              <div class="absolute -top-2.5 whitespace-nowrap bg-bg-table-head px-2 rounded leading-5 text-text-title font-medium">
                <Icon icon="icon-lishijilu" class="mr-1" />{{ t('service.apiServerPath.labels.history') }}
              </div>
            </div>
            <MenuItem
              v-for="server in historicalServers"
              :key="server.url"
              style="max-width: 380px;"
              @click="handleServerSelection(server.url, server)">
              {{ server.url }}
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
