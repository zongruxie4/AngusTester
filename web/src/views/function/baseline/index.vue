<script setup lang="ts">
import { computed, defineAsyncComponent, onMounted, provide, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { BrowserTab } from '@xcan-angus/vue-ui';
import { utils, IPane } from '@xcan-angus/infra';
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
const BaselineList = defineAsyncComponent(() => import('@/views/function/baseline/list/index.vue'));
const BaselineEdit = defineAsyncComponent(() => import('@/views/function/baseline/edit/index.vue'));
const BaselineCases = defineAsyncComponent(() => import('@/views/function/baseline/case/index.vue'));

// Router and UI References
const route = useRoute();
const router = useRouter();
const browserTabRef = ref();

/**
 * Add a new tab pane
 * @param data - Tab pane data
 */
const addTabPane = (data: IPane) => {
  browserTabRef.value.add(() => {
    return data;
  });
};

/**
 * Get tab pane data by key
 * @param key - Tab pane key
 * @returns Tab pane data array or undefined
 */
const getTabPane = (key: string): IPane[] | undefined => {
  return browserTabRef.value.getData(key);
};

/**
 * Delete tab panes by keys
 * @param keys - Array of tab pane keys to delete
 */
const deleteTabPane = (keys: string[]) => {
  browserTabRef.value.remove(keys);
};

/**
 * Update tab pane data
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
 * Initialize baseline module
 */
const initializeBaselineModule = () => {
  if (typeof browserTabRef.value?.add === 'function') {
    browserTabRef.value.add((ids: string[]) => {
      if (!ids.includes('baselineList')) {
        return {
          _id: 'baselineList',
          value: 'baselineList',
          name: t('functionBaseline.name'),
          closable: false
        };
      }
    });
  }

  handleHashChange(route.hash);
};

/**
 * Handle hash change and route to appropriate tab
 * @param hash - URL hash string
 */
const handleHashChange = (hash: string) => {
  const queryString = hash.split('?')[1];
  if (!queryString) {
    return;
  }

  const queryParameters = queryString.split('&').reduce((prev, cur) => {
    const [key, value] = cur.split('=');
    prev[key] = value;
    return prev;
  }, {} as { [key: string]: string });

  const { id, type } = queryParameters;
  if (id) {
    if (type === 'edit') {
      browserTabRef.value.add(() => {
        return {
          _id: id,
          value: 'baselineEdit',
          noCache: true,
          data: { _id: id, id }
        };
      });
    } else {
      browserTabRef.value.add(() => {
        return {
          _id: id + '-case',
          value: 'baselineDetails',
          data: { _id: id, id }
        };
      });
    }
  } else {
    if (type) {
      browserTabRef.value.add(() => {
        const id = utils.uuid();
        return {
          _id: id,
          name: t('functionBaseline.list.addBaseline'),
          value: 'baselineEdit',
          noCache: true,
          data: { _id: id }
        };
      });
    }
  }

  router.replace('/function#baseline');
};

/**
 * Handle storage key change
 */
const handleStorageKeyChange = () => {
  initializeBaselineModule();
};

/**
 * Get storage key for browser tab persistence
 */
const browserTabStorageKey = computed(() => {
  if (!props.projectId) {
    return undefined;
  }

  return `baseline-${props.projectId}`;
});

/**
 * Initialize component on mount
 */
onMounted(() => {
  watch(() => route.hash, () => {
    if (!route.hash.startsWith('#baseline')) {
      return;
    }
    handleHashChange(route.hash);
  });
});

// Provide dependencies to child components
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
    :userId="props.userInfo.id"
    :storageKey="browserTabStorageKey"
    @storageKeyChange="handleStorageKeyChange">
    <template #default="record">
      <template v-if="record.value === 'baselineList'">
        <BaselineList
          v-bind="record"
          :userInfo="props.userInfo"
          :appInfo="props.appInfo"
          :projectId="props.projectId" />
      </template>
      <template v-if="record.value === 'baselineEdit'">
        <BaselineEdit
          v-bind="record"
          :userInfo="props.userInfo"
          :appInfo="props.appInfo"
          :projectId="props.projectId" />
      </template>
      <template v-if="record.value === 'baselineDetails'">
        <BaselineCases
          v-bind="record"
          :userInfo="props.userInfo"
          :appInfo="props.appInfo"
          :projectId="props.projectId" />
      </template>
    </template>
  </BrowserTab>
</template>
