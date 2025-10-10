<script setup lang="ts">
import { computed, defineAsyncComponent, onMounted, provide, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { BrowserTab } from '@xcan-angus/vue-ui';
import { IPane, utils } from '@xcan-angus/infra';
import { useI18n } from 'vue-i18n';
import { BasicProps } from '@/types/types';

const { t } = useI18n();

// Props Definition
const props = withDefaults(defineProps<BasicProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined
});

// Async Components
const PlanList = defineAsyncComponent(() => import('@/views/test/plan/list/index.vue'));
const PlanDetail = defineAsyncComponent(() => import('@/views/test/plan/detail/index.vue'));
const PlanEdit = defineAsyncComponent(() => import('@/views/test/plan/edit/index.vue'));

// Router and UI References
const route = useRoute();
const router = useRouter();
const browserTabRef = ref();

// Tab Management Methods
/**
 * Add a new tab pane to the browser tab component
 * @param data - Tab pane data to add
 */
const addTabPane = (data: IPane) => {
  browserTabRef.value.add(() => {
    return data;
  });
};

/**
 * Retrieve tab pane data by key
 * @param key - Tab pane key to search for
 * @returns Tab pane data array or undefined if not found
 */
const getTabPane = (key: string): IPane[] | undefined => {
  return browserTabRef.value.getData(key);
};

/**
 * Remove tab panes by their keys
 * @param keys - Array of tab pane keys to delete
 */
const deleteTabPane = (keys: string[]) => {
  browserTabRef.value.remove(keys);
};

/**
 * Update existing tab pane data
 * @param data - Updated tab pane data
 */
const updateTabPane = (data: IPane) => {
  browserTabRef.value.update(data);
};

/**
 * Replace tab pane with new data
 * @param key - Tab pane key to replace
 * @param data - New tab pane data
 */
const replaceTabPane = (key: string, data: { key: string }) => {
  browserTabRef.value.replace(key, data);
};

/**
 * Initialize the plan module and create default tab
 */
const initialize = () => {
  if (typeof browserTabRef.value?.add === 'function') {
    browserTabRef.value.add((existingIds: string[]) => {
      if (!existingIds.includes('planList')) {
        return {
          _id: 'planList',
          value: 'planList',
          name: t('common.plan'),
          closable: false
        };
      }
    });
  }
  handleHashChange(route.hash);
};

/**
 * Handle URL hash changes and route to appropriate tab
 * @param hash - URL hash string containing query parameters
 */
const handleHashChange = (hash: string) => {
  const queryString = hash.split('?')[1];
  if (!queryString) {
    return;
  }

  const queryParameters = queryString.split('&').reduce((previous, current) => {
    const [key, value] = current.split('=');
    previous[key] = value;
    return previous;
  }, {} as { [key: string]: string });

  const { id, type } = queryParameters;
  if (id) {
    if (type === 'edit') {
      browserTabRef.value.add(() => {
        return {
          _id: id,
          value: 'planEdit',
          noCache: true,
          data: { _id: id, id }
        };
      });
    } else {
      browserTabRef.value.add(() => {
        return {
          _id: id + '-case',
          value: 'planDetails',
          data: { _id: id, id }
        };
      });
    }
  } else {
    if (type) {
      browserTabRef.value.add(() => {
        const newId = utils.uuid();
        return {
          _id: newId,
          name: t('testPlan.actions.addPlan'),
          value: 'planEdit',
          noCache: true,
          data: { _id: newId }
        };
      });
    }
  }
  router.replace('/test#plans');
};

/**
 * Handle storage key change event from browser tab component
 */
const handleStorageKeyChange = () => {
  initialize();
};

/**
 * Generate storage key for browser tab persistence
 */
const storageKey = computed(() => {
  if (!props.projectId) {
    return undefined;
  }
  return `plan${props.projectId}`;
});

/**
 * Initialize component on mount and set up hash change watcher
 */
onMounted(() => {
  watch(() => route.hash, () => {
    if (!route.hash.startsWith('#plans')) {
      return;
    }
    handleHashChange(route.hash);
  });
});

// Dependency Injection
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
      <template v-if="record.value === 'planList'">
        <PlanList
          v-bind="record"
          :userInfo="props.userInfo"
          :appInfo="props.appInfo"
          :projectId="props.projectId" />
      </template>

      <template v-else-if="record.value === 'planDetails'">
        <PlanDetail
          v-bind="record"
          :userInfo="props.userInfo"
          :appInfo="props.appInfo"
          :projectId="props.projectId" />
      </template>

      <template v-else-if="record.value === 'planEdit'">
        <PlanEdit
          v-bind="record"
          :userInfo="props.userInfo"
          :appInfo="props.appInfo"
          :projectId="props.projectId" />
      </template>
    </template>
  </BrowserTab>
</template>
