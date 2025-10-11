<script setup lang="ts">
import { computed, defineAsyncComponent, onMounted, provide, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { BrowserTab } from '@xcan-angus/vue-ui';
import { utils, IPane } from '@xcan-angus/infra';
import { useI18n } from 'vue-i18n';

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
const { t } = useI18n();

const ServerList = defineAsyncComponent(() => import('@/views/apis/server/list/index.vue'));
const ServerDetail = defineAsyncComponent(() => import('@/views/apis/server/edit/index.vue'));

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

   // Watch for browser tab changes and ensure case list tab exists
   watch(() => browserTabRef.value, () => {
    if (typeof browserTabRef.value?.update === 'function') {
      const tabData = browserTabRef.value.getData().map(item => item.value);
      if (!tabData.includes('serverList')) {
        addTabPane({
          _id: 'serverList',
          value: 'serverList',
          name: t('server.home.tabTitle'),
          closable: false
        });
      } else {
        updateTabPane({
          _id: 'serverList',
          value: 'serverList',
          name: t('server.home.tabTitle'),
          closable: false
        });
      }
    }
  }, { immediate: true });

  hashChange(route.hash);
};

const hashChange = (hash:string) => {
  const queryString = hash.split('?')[1];
  if (!queryString) {
    return;
  }

  const queryParameters = queryString.split('&').reduce((prev, cur) => {
    const [key, value] = cur.split('=');
    prev[key] = value;
    return prev;
  }, {} as { [key: string]: string });

  const { serviceId, serverId, source } = queryParameters;
  if (serviceId && serverId) {
    browserTabRef.value.add(() => {
      return {
        _id: serverId,
        value: 'serverDetails',
        noCache: false,
        data: { _id: serverId, serviceId, serverId, source }
      };
    });
  } else if (source) {
    browserTabRef.value.add(() => {
      const uid = utils.uuid();
      return {
        name: t('server.home.newTab'),
        _id: uid,
        value: 'serverDetails',
        noCache: false,
        data: { _id: uid, source }
      };
    });
  }

  router.replace('/apis#server');
};

const storageKeyChange = () => {
  initialize();
};

onMounted(() => {
  watch(() => route.hash, () => {
    if (!route.hash.startsWith('#server')) {
      return;
    }

    hashChange(route.hash);
  });
});

const storageKey = computed(() => {
  if (!props.projectId) {
    return undefined;
  }

  return `server${props.projectId}`;
});

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
    :storageKey="storageKey"
    @storageKeyChange="storageKeyChange">
    <template #default="record">
      <template v-if="record.value === 'serverList'">
        <ServerList
          v-bind="record"
          :userInfo="props.userInfo"
          :appInfo="props.appInfo"
          :projectId="props.projectId" />
      </template>

      <template v-else-if="record.value === 'serverDetails'">
        <ServerDetail
          v-bind="record"
          :userInfo="props.userInfo"
          :appInfo="props.appInfo"
          :projectId="props.projectId" />
      </template>
    </template>
  </BrowserTab>
</template>
