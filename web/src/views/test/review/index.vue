<script setup lang="ts">
import { computed, defineAsyncComponent, onMounted, provide, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useI18n } from 'vue-i18n';
import { BrowserTab } from '@xcan-angus/vue-ui';
import { utils, IPane } from '@xcan-angus/infra';
import { BasicProps } from '@/types/types';

// Props and Component Definitions
const props = withDefaults(defineProps<BasicProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined
});

// Async component imports for lazy loading
const ReviewList = defineAsyncComponent(() => import('@/views/test/review/list/index.vue'));
const ReviewDetail = defineAsyncComponent(() => import('@/views/test/review/detail/index.vue'));
const ReviewEdit = defineAsyncComponent(() => import('@/views/test/review/edit/index.vue'));

// Router and UI Setup
const route = useRoute();
const router = useRouter();
const { t } = useI18n();
const browserTabRef = ref();

/**
 * Adds a new tab pane to the browser tab component
 * @param data - The pane data to add
 */
const addTabPane = (data: IPane) => {
  browserTabRef.value.add(() => {
    return data;
  });
};

/**
 * Retrieves tab pane data by key
 * @param key - The key to search for
 * @returns Array of pane data or undefined
 */
const getTabPane = (key: string): IPane[] | undefined => {
  return browserTabRef.value.getData(key);
};

/**
 * Removes tab panes by their keys
 * @param keys - Array of keys to remove
 */
const deleteTabPane = (keys: string[]) => {
  browserTabRef.value.remove(keys);
};

/**
 * Updates an existing tab pane with new data
 * @param data - The updated pane data
 */
const updateTabPane = (data: IPane) => {
  browserTabRef.value.update(data);
};

/**
 * Replaces a tab pane with new data
 * @param key - The key of the pane to replace
 * @param data - The new pane data
 */
const replaceTabPane = (key: string, data: { key: string }) => {
  browserTabRef.value.replace(key, data);
};

/**
 * Initializes the component by adding the default review list tab
 * and processing any hash parameters
 */
const initialize = () => {
  if (typeof browserTabRef.value?.add === 'function') {
    browserTabRef.value.add((existingIds: string[]) => {
      if (!existingIds.includes('reviewList')) {
        return {
          _id: 'reviewList',
          value: 'reviewList',
          name: t('common.review'),
          closable: false
        };
      }
    });
  }

  processHashParameters(route.hash);
};

/**
 * Processes URL hash parameters to determine which tab to open
 * @param hash - The URL hash string
 */
const processHashParameters = (hash: string) => {
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
      openEditTab(id);
    } else {
      openDetailTab(id);
    }
  } else {
    if (type) {
      openNewEditTab();
    }
  }

  router.replace('/function#reviews');
};

/**
 * Opens the edit tab for a specific review
 * @param reviewId - The ID of the review to edit
 */
const openEditTab = (reviewId: string) => {
  browserTabRef.value.add(() => {
    return {
      _id: reviewId,
      value: 'reviewEdit',
      noCache: true,
      data: { _id: reviewId, id: reviewId }
    };
  });
};

/**
 * Opens the detail tab for a specific review
 * @param reviewId - The ID of the review to view
 */
const openDetailTab = (reviewId: string) => {
  browserTabRef.value.add(() => {
    return {
      _id: reviewId + '-case',
      value: 'reviewDetails',
      data: { _id: reviewId, id: reviewId }
    };
  });
};

/**
 * Opens a new edit tab for creating a new review
 */
const openNewEditTab = () => {
  browserTabRef.value.add(() => {
    const newId = utils.uuid();
    return {
      _id: newId,
      name: t('caseReview.addReview'),
      value: 'reviewEdit',
      noCache: true,
      data: { _id: newId }
    };
  });
};

/**
 * Handles storage key changes by reinitializing the component
 */
const handleStorageKeyChange = () => {
  initialize();
};

// Computed Properties
const storageKey = computed(() => {
  if (!props.projectId) {
    return undefined;
  }
  return `review${props.projectId}`;
});

// Lifecycle and Watchers
onMounted(() => {
  watch(() => route.hash, () => {
    if (!route.hash.startsWith('#reviews')) {
      return;
    }
    processHashParameters(route.hash);
  });
});

// Provide/Inject Setup
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
    :storageKey="storageKey"
    @storageKeyChange="handleStorageKeyChange">
    <template #default="record">
      <template v-if="record.value === 'reviewList'">
        <ReviewList
          v-bind="record"
          :userInfo="props.userInfo"
          :appInfo="props.appInfo"
          :projectId="props.projectId" />
      </template>
      <template v-else-if="record.value === 'reviewEdit'">
        <ReviewEdit
          v-bind="record"
          :userInfo="props.userInfo"
          :appInfo="props.appInfo"
          :projectId="props.projectId" />
      </template>
      <template v-else-if="record.value === 'reviewDetails'">
        <ReviewDetail
          v-bind="record"
          :userInfo="props.userInfo"
          :appInfo="props.appInfo"
          :projectId="props.projectId" />
      </template>
    </template>
  </BrowserTab>
</template>
