<script setup lang="ts">
import { computed, defineAsyncComponent, onMounted, provide, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { BrowserTab } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import { utils } from '@xcan-angus/infra';
import { software } from '@/api/tester';

import { IPane } from './PropsType';

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

const List = defineAsyncComponent(() => import('@/views/task/version/list/index.vue'));
const Detail = defineAsyncComponent(() => import('@/views/task/version/detail/index.vue'));

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
      if (!ids.includes('versionList')) {
        return {
          _id: 'versionList',
          value: 'versionList',
          name: t('taskVersion.title'),
          closable: false // 是否允许关闭，true - 允许关闭，false - 禁止关闭
        };
      }
    });
  }

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
          value: 'versionEdit',
          noCache: true,
          data: { _id: id, id }
        };
      });
    } else {
      if (!id) {
        const [_error, { data = {} }] = await software.getSoftwareVersionList({
          projectId: props.projectId,
          name
        });
        if (data?.list?.[0]) {
          browserTabRef.value.add(() => {
            return {
              _id: data?.list?.[0].id + '-detail',
              value: 'versionDetails',
              data: { _id: data?.list?.[0].id, id: data?.list?.[0].id }
            };
          });
          router.replace('/task#version');
        }
        return;
      }
      browserTabRef.value.add(() => {
        return {
          _id: id + '-detail',
          value: 'versionDetails',
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
          name: t('taskVersion.form.addVersion'),
          value: 'versionEdit',
          noCache: true,
          data: { _id: id }
        };
      });
    }
  }

  router.replace('/task#version');
};

const storageKeyChange = () => {
  initialize();
};

onMounted(() => {
  watch(() => route.hash, () => {
    if (!route.hash.startsWith('#version')) {
      return;
    }

    hashChange(route.hash);
  });
});

const storageKey = computed(() => {
  if (!props.projectId) {
    return undefined;
  }
  return `version${props.projectId}`;
});

// 添加指定的tabPane
provide('addTabPane', addTabPane);

// 获取tabPane
provide('getTabPane', getTabPane);

// 删除指定的tabPane
provide('deleteTabPane', deleteTabPane);

// 更新指定的tabPane
provide('updateTabPane', updateTabPane);

// 替换指定tabPane
provide('replaceTabPane', replaceTabPane);
</script>
<template>
  <BrowserTab
    ref="browserTabRef"
    hideAdd
    class="h-full"
    :userId="props.userInfo.id"
    :storageKey="storageKey"
    @storageKeyChange="storageKeyChange">
    <template #default="record">
      <template v-if="record.value === 'versionList'">
        <List
          v-bind="record"
          :userInfo="props.userInfo"
          :appInfo="props.appInfo"
          :projectId="props.projectId" />
      </template>

      <template v-else-if="record.value === 'versionDetails'">
        <Detail
          v-bind="record"
          :userInfo="props.userInfo"
          :appInfo="props.appInfo"
          :projectId="props.projectId" />
      </template>
    </template>
  </BrowserTab>
</template>
