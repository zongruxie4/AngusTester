<script setup lang="ts">
import { computed, defineAsyncComponent, onMounted, provide, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { BrowserTab } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import { IPane } from '@xcan-angus/infra';
import { ApiMenuKey } from '@/views/apis/menu';
import { BasicProps } from '@/types/types';

const List = defineAsyncComponent(() => import('@/views/apis/design/List.vue'));
const DesignDocContent = defineAsyncComponent(() => import('@/views/apis/design/DesignDoc.vue'));

const props = withDefaults(defineProps<BasicProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined
});

const { t } = useI18n();

const route = useRoute();
const router = useRouter();
const browserTabRef = ref();

// Constants
const DESIGN_LIST_KEY = 'designList';
const DESIGN_DOC_VALUE = 'designDocContent';

/**
 * Parse the hash query part into a key-value map.
 */
const parseHashQuery = (hash: string): { [key: string]: string } => {
  const queryString = hash.split('?')[1];
  if (!queryString) {
    return {};
  }
  return queryString.split('&').reduce((prev, cur) => {
    const [key, value] = cur.split('=');
    prev[key] = value;
    return prev;
  }, {} as { [key: string]: string });
};

/**
 * Add a new tab pane into the browser tab container.
 * The provided factory returns pane data which will be appended to the tab list.
 */
const addTabPane = (data: IPane) => {
  browserTabRef.value.add(() => {
    return data;
  });
};

/**
 * Get pane data by a specific key.
 * Returns a list of panes when key matches multiple targets.
 */
const getTabPane = (key: string): IPane[] | undefined => {
  return browserTabRef.value.getData(key);
};

/**
 * Remove one or multiple tab panes by keys.
 * Keys should match the internal pane identifiers.
 */
const removeTabPanes = (keys: string[]) => {
  browserTabRef.value.remove(keys);
};

/**
 * Update the tab pane meta information such as name or closable.
 */
const updateTabPane = (data: IPane) => {
  browserTabRef.value.update(data);
};

/**
 * Replace a tab pane key with a new key while keeping the position.
 */
const replaceTabPane = (key: string, data: { key: string }) => {
  browserTabRef.value.replace(key, data);
};

/**
 * Handle hash navigation for deep linking a design detail.
 * When hash contains id, a new document tab is opened and route hash is reset.
 */
const handleHashNavigation = async (hash: string) => {
  const { id } = parseHashQuery(hash);
  if (id) {
    browserTabRef.value.add(() => {
      return {
        _id: id + '-doc',
        designId: id,
        value: DESIGN_DOC_VALUE,
        data: { _id: id, id }
      };
    });
  }
  await router.replace(`/apis#${ApiMenuKey.DESIGN}`);
};

/**
 * Ensure the design list tab exists; update its title if already present.
 */
const ensureDesignListTab = () => {
  if (typeof browserTabRef.value?.update !== 'function') {
    return;
  }
  const tabData = browserTabRef.value.getData().map(item => item.value);
  if (!tabData.includes(DESIGN_LIST_KEY)) {
    addTabPane({
      _id: DESIGN_LIST_KEY,
      value: DESIGN_LIST_KEY,
      name: t('apiDesign.home.tabTitle'),
      closable: false
    });
  } else {
    updateTabPane({
      _id: DESIGN_LIST_KEY,
      value: DESIGN_LIST_KEY,
      name: t('apiDesign.home.tabTitle'),
      closable: false
    });
  }
};

/**
 * Ensure base tabs exist and synchronize tab title with i18n updates.
 * Also handles initial hash-based navigation.
 */
const initialize = () => {
  if (typeof browserTabRef.value?.add === 'function') {
    browserTabRef.value.add((ids: string[]) => {
      if (!ids.includes(DESIGN_LIST_KEY)) {
        return {
          _id: DESIGN_LIST_KEY,
          value: DESIGN_LIST_KEY,
          name: t('apiDesign.home.tabTitle'),
          closable: false
        };
      }
    });
  }

  // Watch for browser tab changes and ensure case list tab exists
  watch(() => browserTabRef.value, () => {
    ensureDesignListTab();
  }, { immediate: true });

  handleHashNavigation(route.hash);
};

/**
 * Re-initialize when storage key changes to reload persisted tabs.
 */
const handleStorageKeyChange = () => {
  initialize();
};

const storageKey = computed(() => {
  if (!props.projectId) {
    return undefined;
  }
  return `design${props.projectId}`;
});

onMounted(() => {
  watch(() => route.hash, () => {
    if (!route.hash.startsWith('#design')) {
      return;
    }

    handleHashNavigation(route.hash);
  });
});

provide('addTabPane', addTabPane);
provide('getTabPane', getTabPane);
provide('deleteTabPane', removeTabPanes);
provide('removeTabPanes', removeTabPanes);
provide('updateTabPane', updateTabPane);
provide('replaceTabPane', replaceTabPane);
</script>

<template>
  <!--Q8: hideAdd ?-->
  <BrowserTab
    ref="browserTabRef"
    hideAdd
    class="h-full"
    :userId="`${props.userInfo?.id ?? ''}`"
    :storageKey="storageKey"
    @storageKeyChange="handleStorageKeyChange">
    <template #default="record">
      <template v-if="record.value === DESIGN_LIST_KEY">
        <List
          v-bind="record"
          :userInfo="props.userInfo"
          :appInfo="props.appInfo"
          :projectId="props.projectId" />
      </template>

      <template v-else-if="record.value === DESIGN_DOC_VALUE">
        <DesignDocContent
          v-bind="record"
          :userInfo="props.userInfo"
          :appInfo="props.appInfo"
          :projectId="props.projectId" />
      </template>
    </template>
  </BrowserTab>
</template>
