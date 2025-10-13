<script setup lang="ts">
import { computed, defineAsyncComponent, onMounted, provide, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useI18n } from 'vue-i18n';
import { BrowserTab } from '@xcan-angus/vue-ui';
import { IPane, utils } from '@xcan-angus/infra';
import { BasicProps } from '@/types/types';
import { IssueMenuKey } from '@/views/issue/menu';

// COMPONENT PROPS
const props = withDefaults(defineProps<BasicProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined
});

// ASYNC COMPONENTS
const MeetingList = defineAsyncComponent(() => import('@/views/issue/meeting/list/index.vue'));
const MeetingDetail = defineAsyncComponent(() => import('@/views/issue/meeting/detail/index.vue'));
const MeetingEdit = defineAsyncComponent(() => import('@/views/issue/meeting/edit/index.vue'));

// COMPOSABLES
const route = useRoute();
const router = useRouter();
const { t } = useI18n();

// REACTIVE STATE
const browserTabRef = ref();

/**
 * Generates storage key for browser tab persistence
 * @returns Storage key string or undefined if no projectId
 */
const storageKey = computed(() => {
  if (!props.projectId) {
    return undefined;
  }
  return `meeting${props.projectId}`;
});

/**
 * Adds a new tab pane to the browser tab component
 * @param tabPaneData - Tab pane configuration data
 */
const addTabPane = (tabPaneData: IPane) => {
  browserTabRef.value.add(() => {
    return tabPaneData;
  });
};

/**
 * Retrieves tab pane data by key
 * @param key - Tab pane identifier key
 * @returns Tab pane data array or undefined
 */
const getTabPane = (key: string): IPane[] | undefined => {
  return browserTabRef.value.getData(key);
};

/**
 * Removes tab panes by their keys
 * @param keys - Array of tab pane keys to remove
 */
const deleteTabPane = (keys: string[]) => {
  browserTabRef.value.remove(keys);
};

/**
 * Updates an existing tab pane with new data
 * @param tabPaneData - Updated tab pane data
 */
const updateTabPane = (tabPaneData: IPane) => {
  browserTabRef.value.update(tabPaneData);
};

/**
 * Replaces a tab pane with new data
 * @param key - Tab pane key to replace
 * @param tabPaneData - New tab pane data
 */
const replaceTabPane = (key: string, tabPaneData: { key: string }) => {
  browserTabRef.value.replace(key, tabPaneData);
};

/**
 * Initializes the meeting module by setting up default tab and processing hash
 */
const initializeMeetingModule = () => {
  if (typeof browserTabRef.value?.add === 'function') {
    browserTabRef.value.add((existingTabIds: string[]) => {
      if (!existingTabIds.includes('meetingList')) {
        return {
          _id: 'meetingList',
          value: 'meetingList',
          name: t('meeting.title'),
          closable: false
        };
      }
    });
  }

  // Watch for browser tab changes and ensure case list tab exists
  watch(() => browserTabRef.value, () => {
    if (typeof browserTabRef.value?.update === 'function') {
      const tabData = browserTabRef.value.getData().map(item => item.value);
      if (!tabData.includes('meetingList')) {
        addTabPane({
          _id: 'meetingList',
          value: 'meetingList',
          name: t('meeting.title'),
          closable: false
        });
      } else {
        updateTabPane({
          _id: 'meetingList',
          value: 'meetingList',
          name: t('meeting.title'),
          closable: false
        });
      }
    }
  }, { immediate: true });
  processRouteHash(route.hash);
};

/**
 * Processes route hash to determine which tab to open
 * @param hash - URL hash string containing query parameters
 */
const processRouteHash = (hash: string) => {
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

  const { id, type } = queryParameters;

  if (id) {
    if (type === 'edit') {
      // Open meeting edit tab
      browserTabRef.value.add(() => {
        return {
          _id: id,
          value: 'meetingEdit',
          noCache: true,
          data: { _id: id, id }
        };
      });
    } else {
      // Open meeting detail tab
      browserTabRef.value.add(() => {
        return {
          _id: id + '-detail',
          value: 'meetingDetails',
          data: { _id: id, id }
        };
      });
    }
  } else {
    if (type) {
      // Open new meeting creation tab
      browserTabRef.value.add(() => {
        const newMeetingId = utils.uuid();
        return {
          _id: newMeetingId,
          name: t('meeting.actions.addMeeting'),
          value: 'meetingEdit',
          noCache: true,
          data: { _id: newMeetingId }
        };
      });
    }
  }

  // Clean up URL by removing hash parameters
  router.replace(`/issue#${IssueMenuKey.MEETING}`);
};

/**
 * Handles storage key changes and reinitializes the module
 */
const handleStorageKeyChange = () => {
  initializeMeetingModule();
};

/**
 * Component mounted lifecycle hook
 * Sets up route hash watcher for tab navigation
 */
onMounted(() => {
  watch(() => route.hash, (newHash) => {
    if (!newHash.startsWith('#meeting')) {
      return;
    }

    processRouteHash(newHash);
  });
});

// Provide tab pane management functions to child components
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
    <template #default="tabRecord">
      <template v-if="tabRecord.value === 'meetingList'">
        <MeetingList
          v-bind="tabRecord"
          :userInfo="props.userInfo"
          :appInfo="props.appInfo"
          :projectId="props.projectId" />
      </template>

      <template v-else-if="tabRecord.value === 'meetingDetails'">
        <MeetingDetail
          v-bind="tabRecord"
          :userInfo="props.userInfo"
          :appInfo="props.appInfo"
          :projectId="props.projectId" />
      </template>

      <template v-else-if="tabRecord.value === 'meetingEdit'">
        <MeetingEdit
          v-bind="tabRecord"
          :userInfo="props.userInfo"
          :appInfo="props.appInfo"
          :projectId="props.projectId" />
      </template>
    </template>
  </BrowserTab>
</template>
