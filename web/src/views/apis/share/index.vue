<script setup lang="ts">
import { computed, defineAsyncComponent, onMounted, provide, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { BrowserTab } from '@xcan-angus/vue-ui';
import { utils, IPane } from '@xcan-angus/infra';
import { apis } from '@/api/tester';
import { useI18n } from 'vue-i18n';
import { ApiMenuKey } from '@/views/apis/types';

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

const List = defineAsyncComponent(() => import('@/views/apis/share/list/index.vue'));
// const Detail = defineAsyncComponent(() => import('@/views/apis/share/detail/index.vue'));

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
      if (!ids.includes('shareList')) {
        return {
          _id: 'shareList',
          value: 'shareList',
          name: t('apiShare.title'),
          closable: false
        };
      }
    });
  }

  // Watch for browser tab changes and ensure case list tab exists
  watch(() => browserTabRef.value, () => {
    if (typeof browserTabRef.value?.update === 'function') {
      const tabData = browserTabRef.value.getData().map(item => item.value);
      if (!tabData.includes('shareList')) {
        addTabPane({
          _id: 'shareList',
          value: 'shareList',
          name: t('apiShare.title'),
          closable: false
        });
      } else {
        updateTabPane({
          _id: 'shareList',
          value: 'shareList',
          name: t('apiShare.title'),
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

  const { id, type, name } = queryParameters;
  if (name || id) {
    if (type === 'edit') {
      browserTabRef.value.add(() => {
        return {
          _id: id,
          value: 'shareEdit',
          noCache: true,
          data: { _id: id, id }
        };
      });
    } else {
      if (!id) {
        const [_error, { data = {} }] = await apis.getShareList({
          projectId: props.projectId,
          name
        });
        if (data?.list?.[0]) {
          browserTabRef.value.add(() => {
            return {
              _id: data?.list?.[0].id + '-detail',
              value: 'shareDetails',
              data: { _id: data?.list?.[0].id, id: data?.list?.[0].id }
            };
          });
          router.replace(`/apis#${ApiMenuKey.SHARE}`);
        }
        return;
      }
      browserTabRef.value.add(() => {
        return {
          _id: id + '-detail',
          value: 'shareDetails',
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
          name: t('apiShare.addShare'),
          value: 'shareEdit',
          noCache: true,
          data: { _id: id }
        };
      });
    }
  }
  router.replace(`/apis#${ApiMenuKey.SHARE}`);
};

const storageKeyChange = () => {
  initialize();
};

onMounted(() => {
  watch(() => route.hash, () => {
    if (!route.hash.startsWith('#share')) {
      return;
    }

    hashChange(route.hash);
  });
});

const storageKey = computed(() => {
  if (!props.projectId) {
    return undefined;
  }
  return `share${props.projectId}`;
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
      <template v-if="record.value === 'shareList'">
        <List
          v-bind="record"
          :userInfo="props.userInfo"
          :appInfo="props.appInfo"
          :projectId="props.projectId" />
      </template>

      <template v-else-if="record.value === 'shareDetails'">
        <!-- <Detail
          v-bind="record"
          :userInfo="props.userInfo"
          :appInfo="props.appInfo"
          :projectId="props.projectId" /> -->
      </template>
    </template>
  </BrowserTab>
</template>
