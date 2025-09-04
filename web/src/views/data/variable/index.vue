<script setup lang="ts">
import { computed, defineAsyncComponent, onMounted, provide, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { BrowserTab } from '@xcan-angus/vue-ui';
import { utils } from '@xcan-angus/infra';
import { useI18n } from 'vue-i18n';

import { IPane } from './types';

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

const VariableList = defineAsyncComponent(() => import('@/views/data/variable/list/index.vue'));
const VariableDetail = defineAsyncComponent(() => import('@/views/data/variable/detail/index.vue'));

const route = useRoute();
const router = useRouter();
const browserTabRef = ref();
const variableListRef = ref();

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
      if (!ids.includes('variableList')) {
        return {
          _id: 'variableList',
          value: 'variableList',
          name: t('dataVariable.title'),
          closable: false
        };
      }
    });
  }

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

  const { id, source } = queryParameters;
  if (id) {
    browserTabRef.value.add(() => {
      return {
        _id: id,
        value: 'variableDetails',
        noCache: true,
        data: { _id: id, id, source }
      };
    });
  } else if (source) {
    browserTabRef.value.add(() => {
      let name = t('dataVariable.addVariable.static');
      if (source === 'FILE') {
        name = t('dataVariable.addVariable.file');
      } else if (source === 'HTTP') {
        name = t('dataVariable.addVariable.http');
      } else if (source === 'JDBC') {
        name = t('dataVariable.addVariable.jdbc');
      }

      const uid = utils.uuid();
      return {
        name,
        _id: uid,
        value: 'variableDetails',
        noCache: true,
        data: { _id: uid, source }
      };
    });
  }

  router.replace('/data#variables');
};

const storageKeyChange = () => {
  initialize();
};

const refreshList = () => {
  variableListRef.value && variableListRef.value.loadData();
};

onMounted(() => {
  watch(() => route.hash, () => {
    if (!route.hash.startsWith('#variables')) {
      return;
    }
    hashChange(route.hash);
  });
});

const storageKey = computed(() => {
  if (!props.projectId) {
    return undefined;
  }
  return `variable${props.projectId}`;
});

provide('addTabPane', addTabPane);
provide('getTabPane', getTabPane);
provide('deleteTabPane', deleteTabPane);
provide('updateTabPane', updateTabPane);
provide('replaceTabPane', replaceTabPane);
provide('refreshList', refreshList);
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
      <template v-if="record.value === 'variableList'">
        <VariableList
          v-bind="record"
          ref="variableListRef"
          :userInfo="props.userInfo"
          :appInfo="props.appInfo"
          :projectId="props.projectId" />
      </template>

      <template v-else-if="record.value === 'variableDetails'">
        <VariableDetail
          v-bind="record"
          :userInfo="props.userInfo"
          :appInfo="props.appInfo"
          :projectId="props.projectId" />
      </template>
    </template>
  </BrowserTab>
</template>
