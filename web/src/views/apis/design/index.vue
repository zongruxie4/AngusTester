<script setup lang="ts">
import { computed, defineAsyncComponent, onMounted, provide, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { BrowserTab } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import { IPane } from '@xcan-angus/infra';
import { ApiMenuKey } from '@/views/apis/types';

type Props = {
  projectId: string;
  userInfo: { id: string; };
  appInfo: { id: string; };
}

const { t } = useI18n();
const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined
});

const List = defineAsyncComponent(() => import('@/views/apis/design/list/index.vue'));
const DesignDocContent = defineAsyncComponent(() => import('@/views/apis/design/apiDesignDoc/index.vue'));

const route = useRoute();
const router = useRouter();
const browserTabRef = ref();

const addTabPane = (data: IPane) => {
  browserTabRef.value.add(() => {
    return data;
  });
};

const getTabPane = (key: string): IPane[] | undefined => {
  return browserTabRef.value.getData(key);
};

const deleteTabPane = (keys: string[]) => {
  browserTabRef.value.remove(keys);
};

const updateTabPane = (data: IPane) => {
  browserTabRef.value.update(data);
};

const replaceTabPane = (key: string, data: { key: string }) => {
  browserTabRef.value.replace(key, data);
};

const initialize = () => {
  if (typeof browserTabRef.value?.add === 'function') {
    browserTabRef.value.add((ids: string[]) => {
      if (!ids.includes('designList')) {
        return {
          _id: 'designList',
          value: 'designList',
          name: t('design.home.tabTitle'),
          closable: false
        };
      }
    });
  }

  // Watch for browser tab changes and ensure case list tab exists
  watch(() => browserTabRef.value, () => {
    if (typeof browserTabRef.value?.update === 'function') {
      const tabData = browserTabRef.value.getData().map(item => item.value);
      if (!tabData.includes('designList')) {
        addTabPane({
          _id: 'designList',
          value: 'designList',
          name: t('design.home.tabTitle'),
          closable: false
        });
      } else {
        updateTabPane({
          _id: 'designList',
          value: 'designList',
          name: t('design.home.tabTitle'),
          closable: false
        });
      }
    }
  }, { immediate: true });

  hashChange(route.hash);
};

const hashChange = async (hash: string) => {
  const queryString = hash.split('?')[1];
  if (!queryString) {
    return;
  }

  const queryParameters = queryString.split('&').reduce((prev, cur) => {
    const [key, value] = cur.split('=');
    prev[key] = value;
    return prev;
  }, {} as { [key: string]: string });

  const { id } = queryParameters;
  if (id) {
    browserTabRef.value.add(() => {
      return {
        _id: id + '-doc',
        designId: id,
        value: 'designDocContent',
        data: { _id: id, id }
      };
    });
  }
  router.replace(`/apis#${ApiMenuKey.DESIGN}`);
};

const storageKeyChange = () => {
  initialize();
};

onMounted(() => {
  watch(() => route.hash, () => {
    if (!route.hash.startsWith('#design')) {
      return;
    }

    hashChange(route.hash);
  });
});

const storageKey = computed(() => {
  if (!props.projectId) {
    return undefined;
  }
  return `design${props.projectId}`;
});

provide('addTabPane', addTabPane);

provide('getTabPane', getTabPane);

provide('deleteTabPane', deleteTabPane);

provide('updateTabPane', updateTabPane);

provide('replaceTabPane', replaceTabPane);
</script>

<template>
  <!--Q8: hideAdd ?-->
  <BrowserTab
    ref="browserTabRef"
    hideAdd
    class="h-full"
    :userId="props.userInfo?.id"
    :storageKey="storageKey"
    @storageKeyChange="storageKeyChange">
    <template #default="record">
      <template v-if="record.value === 'designList'">
        <List
          v-bind="record"
          :userInfo="props.userInfo"
          :appInfo="props.appInfo"
          :projectId="props.projectId" />
      </template>

      <template v-else-if="record.value === 'designDocContent'">
        <DesignDocContent
          v-bind="record"
          :userInfo="props.userInfo"
          :appInfo="props.appInfo"
          :projectId="props.projectId" />
      </template>
    </template>
  </BrowserTab>
</template>
