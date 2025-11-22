<script setup lang="ts">
import { computed, defineAsyncComponent, onMounted, provide, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { BrowserTab } from '@xcan-angus/vue-ui';
import { IPane, utils } from '@xcan-angus/infra';
import { useI18n } from 'vue-i18n';
import { BasicProps } from '@/types/types';
import { ProjectMenuKey } from '@/views/project/menu';

const { t } = useI18n();

// Props Definition
const props = withDefaults(defineProps<BasicProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined
});

// Async Components
const EvaluationList = defineAsyncComponent(() => import('@/views/project/evaluation/list/index.vue'));
const EvaluationDetail = defineAsyncComponent(() => import('@/views/project/evaluation/detail/index.vue'));
const EvaluationEdit = defineAsyncComponent(() => import('@/views/project/evaluation/edit/index.vue'));

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
 * Initialize the evaluation module and create default tab
 */
const initialize = () => {
  if (typeof browserTabRef.value?.add === 'function') {
    browserTabRef.value.add((existingIds: string[]) => {
      if (!existingIds.includes('evaluationList')) {
        return {
          _id: 'evaluationList',
          value: 'evaluationList',
          name: t('evaluation.title'),
          closable: false
        };
      }
    });
  }
  // Watch for browser tab changes and ensure evaluation list tab exists
  watch(() => browserTabRef.value, () => {
    if (typeof browserTabRef.value?.update === 'function') {
      const tabData = browserTabRef.value.getData().map(item => item.value);
      if (!tabData.includes('evaluationList')) {
        addTabPane({
          _id: 'evaluationList',
          value: 'evaluationList',
          name: t('evaluation.title'),
          closable: false
        });
      } else {
        updateTabPane({
          _id: 'evaluationList',
          value: 'evaluationList',
          name: t('evaluation.title'),
          closable: false
        });
      }
    }
  }, { immediate: true });
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
          value: 'evaluationEdit',
          noCache: true,
          data: { _id: id, id }
        };
      });
    } else {
      browserTabRef.value.add(() => {
        return {
          _id: id + '-detail',
          value: 'evaluationDetail',
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
          name: t('evaluation.actions.addEvaluation'),
          value: 'evaluationEdit',
          noCache: true,
          data: { _id: newId }
        };
      });
    }
  }
  router.replace(`/project#${ProjectMenuKey.EVALUATION}`);
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
  return `evaluation${props.projectId}`;
});

/**
 * Initialize component on mount and set up hash change watcher
 */
onMounted(() => {
  watch(() => route.hash, () => {
    if (!route.hash.startsWith('#evaluation')) {
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
    :userId="props.userInfo?.id as any"
    :storageKey="storageKey"
    @storageKeyChange="handleStorageKeyChange">
    <template #default="record">
      <template v-if="record.value === 'evaluationList'">
        <EvaluationList
          v-bind="record"
          :userInfo="props.userInfo"
          :appInfo="props.appInfo"
          :projectId="props.projectId" />
      </template>

      <template v-else-if="record.value === 'evaluationDetail'">
        <EvaluationDetail
          v-bind="record"
          :userInfo="props.userInfo"
          :appInfo="props.appInfo"
          :projectId="props.projectId" />
      </template>

      <template v-else-if="record.value === 'evaluationEdit'">
        <EvaluationEdit
          v-bind="record"
          :userInfo="props.userInfo"
          :appInfo="props.appInfo"
          :projectId="props.projectId" />
      </template>
    </template>
  </BrowserTab>
</template>

