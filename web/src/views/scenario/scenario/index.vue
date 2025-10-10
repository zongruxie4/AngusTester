<script setup lang="ts">
import { computed, defineAsyncComponent, onMounted, provide, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRoute, useRouter } from 'vue-router';
import { BrowserTab } from '@xcan-angus/vue-ui';
import { utils, IPane } from '@xcan-angus/infra';

// Types and Props Definition
const { t } = useI18n();

type Props = {
  projectId: string;
  userInfo: { id: string; };
  appInfo: { id: string; };
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined
});

interface ScenarioIPane extends IPane{
  /** Scenario information required for opening scenario plugins */
  scenarioInfo?: {
    /** Scenario ID */
    id: string;
    /** Scenario name */
    name: string;
  };
}

// Core scenario components
const Auth = defineAsyncComponent(() => import('@/views/scenario/scenario/auth/index.vue'));
const List = defineAsyncComponent(() => import('@/views/scenario/scenario/list/index.vue'));
const Detail = defineAsyncComponent(() => import('@/views/scenario/scenario/detail/index.vue'));

// Plugin configuration components
const HttpConfig = defineAsyncComponent(() => import('@/views/scenario/scenario/plugins/Http.vue'));
const WebSocketConfig = defineAsyncComponent(() => import('@/views/scenario/scenario/plugins/WebSocket.vue'));
const JdbcConfig = defineAsyncComponent(() => import('@/views/scenario/scenario/plugins/Jdbc.vue'));
const FtpConfig = defineAsyncComponent(() => import('@/views/scenario/scenario/plugins/Ftp.vue'));
const LdapConfig = defineAsyncComponent(() => import('@/views/scenario/scenario/plugins/Ldap.vue'));
const MailConfig = defineAsyncComponent(() => import('@/views/scenario/scenario/plugins/Mail.vue'));
const SmtpConfig = defineAsyncComponent(() => import('@/views/scenario/scenario/plugins/Smtp.vue'));
const TcpConfig = defineAsyncComponent(() => import('@/views/scenario/scenario/plugins/Tcp.vue'));

// Router and State Management
const route = useRoute();
const router = useRouter();
const browserTabRef = ref();

// Refresh notification state for triggering child component updates
const refreshNotificationId = ref<string>();

/**
 * Generate storage key for browser tab persistence based on project ID
 */
const storageKey = computed(() => {
  if (!props.projectId) {
    return undefined;
  }
  return `scenario${props.projectId}`;
});

/**
 * Trigger refresh notification to update child components
 */
const triggerRefreshNotification = () => {
  refreshNotificationId.value = utils.uuid();
};

/**
 * Add a new tab pane to the browser tab component
 * @param tabData - Tab pane data to add
 */
const addTabPane = (tabData: ScenarioIPane) => {
  browserTabRef.value.add(() => {
    return tabData;
  });
};

/**
 * Get tab pane data by key
 * @param tabKey - Key to identify the tab pane
 * @returns Tab pane data or undefined if not found
 */
const getTabPane = (tabKey: string): ScenarioIPane[] | undefined => {
  return browserTabRef.value.getData(tabKey);
};

/**
 * Delete tab panes by their keys
 * @param tabKeys - Array of tab keys to delete
 */
const deleteTabPane = (tabKeys: string[]) => {
  browserTabRef.value.remove(tabKeys);
};

/**
 * Update existing tab pane data
 * @param tabData - Updated tab pane data
 */
const updateTabPane = (tabData: ScenarioIPane) => {
  browserTabRef.value.update(tabData);
};

/**
 * Replace tab pane with new data
 * @param tabKey - Key of tab to replace
 * @param newTabData - New tab data with key property
 */
const replaceTabPane = (tabKey: string, newTabData: { key: string }) => {
  browserTabRef.value.replace(tabKey, newTabData);
};

/**
 * Initialize the browser tab component with default scenario list tab
 */
const initializeBrowserTabs = () => {
  if (typeof browserTabRef.value?.add === 'function') {
    browserTabRef.value.add((existingTabIds: string[]) => {
      // Add default scenario list tab if it doesn't exist
      if (!existingTabIds.includes('scenarioList')) {
        return {
          _id: 'scenarioList',
          value: 'scenarioList',
          name: t('scenario.title'),
          closable: false // Prevent closing the main scenario list tab
        };
      }
    });
  }

  handleHashChange(route.hash);
};

/**
 * Handle URL hash changes to open specific tabs
 * @param hash - URL hash string containing query parameters
 */
const handleHashChange = (hash: string) => {
  const queryString = hash.split('?')[1];
  if (!queryString) {
    return;
  }

  // Parse query parameters from hash
  const queryParameters = queryString.split('&').reduce((accumulator, current) => {
    const [key, value] = current.split('=');
    accumulator[key] = value;
    return accumulator;
  }, {} as { [key: string]: string });

  const { id, name, plugin, type } = queryParameters;

  // Handle detail tab opening
  if (id && name && plugin && type === 'detail') {
    browserTabRef.value.add(() => {
      return {
        _id: id + 'detail',
        name,
        value: 'detail',
        data: { scenarioId: id, name }
      };
    });
  } else if (id && name && plugin) {
    // Handle plugin configuration tab opening
    browserTabRef.value.add(() => {
      return {
        _id: id,
        name,
        value: plugin,
        scenarioInfo: { id, name }
      };
    });
  }

  // Clean up URL by removing hash parameters
  router.replace('/scenario#scenario');
};

/**
 * Handle storage key changes and reinitialize tabs
 */
const handleStorageKeyChange = () => {
  initializeBrowserTabs();
};

onMounted(() => {
  // Watch for hash changes to handle tab navigation
  watch(() => route.hash, () => {
    if (!route.hash.startsWith('#scenario')) {
      return;
    }
    handleHashChange(route.hash);
  }, {
    immediate: true
  });
});

// Provide Functions for Child Components
provide('updateRefreshNotify', triggerRefreshNotification);
provide('addTabPane', addTabPane);
provide('getTabPane', getTabPane);
provide('deleteTabPane', deleteTabPane);
provide('updateTabPane', updateTabPane);
provide('replaceTabPane', replaceTabPane);
</script>
<template>
  <BrowserTab
    ref="browserTabRef"
    hideAdd
    class="h-full"
    :userId="props.userInfo?.id"
    :storageKey="storageKey"
    @storageKeyChange="handleStorageKeyChange">
    <template #default="record">
      <template v-if="record.value === 'scenarioList'">
        <List
          :notify="refreshNotificationId"
          :userInfo="props.userInfo"
          :appInfo="props.appInfo"
          :projectId="props.projectId" />
      </template>
      <template v-if="record.value === 'Http'">
        <HttpConfig
          :tabKey="record._id"
          :scenarioInfo="record.scenarioInfo"
          :userInfo="props.userInfo"
          :appInfo="props.appInfo"
          :projectId="props.projectId" />
      </template>
      <template v-if="record.value === 'WebSocket'">
        <WebSocketConfig
          :tabKey="record._id"
          :scenarioInfo="record.scenarioInfo"
          :userInfo="props.userInfo"
          :appInfo="props.appInfo"
          :projectId="props.projectId" />
      </template>
      <template v-if="record.value === 'Jdbc'">
        <JdbcConfig
          :tabKey="record._id"
          :scenarioInfo="record.scenarioInfo"
          :userInfo="props.userInfo"
          :appInfo="props.appInfo"
          :projectId="props.projectId" />
      </template>
      <template v-if="record.value === 'Ftp'">
        <FtpConfig
          :tabKey="record._id"
          :scenarioInfo="record.scenarioInfo"
          :userInfo="props.userInfo"
          :appInfo="props.appInfo"
          :projectId="props.projectId" />
      </template>
      <template v-if="record.value === 'Ldap'">
        <LdapConfig
          :tabKey="record._id"
          :scenarioInfo="record.scenarioInfo"
          :userInfo="props.userInfo"
          :appInfo="props.appInfo"
          :projectId="props.projectId" />
      </template>
      <template v-if="record.value === 'Mail'">
        <MailConfig
          :tabKey="record._id"
          :scenarioInfo="record.scenarioInfo"
          :userInfo="props.userInfo"
          :appInfo="props.appInfo"
          :projectId="props.projectId" />
      </template>
      <template v-if="record.value === 'Smtp'">
        <SmtpConfig
          :tabKey="record._id"
          :scenarioInfo="record.scenarioInfo"
          :userInfo="props.userInfo"
          :appInfo="props.appInfo"
          :projectId="props.projectId" />
      </template>
      <template v-if="record.value === 'Tcp'">
        <TcpConfig
          :tabKey="record._id"
          :scenarioInfo="record.scenarioInfo"
          :userInfo="props.userInfo"
          :appInfo="props.appInfo"
          :projectId="props.projectId" />
      </template>
      <template v-if="record.value === 'authorization'">
        <Auth :appId="appInfo?.id" :projectId="props.projectId" />
      </template>
      <template v-if="record.value === 'detail'">
        <Detail
          v-bind="record"
          :userInfo="props.userInfo"
          :appInfo="props.appInfo"
          :projectId="props.projectId" />
      </template>
    </template>
  </BrowserTab>
</template>
