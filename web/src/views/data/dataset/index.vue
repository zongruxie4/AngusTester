<script setup lang="ts">
import { computed, defineAsyncComponent, onMounted, provide, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { BrowserTab } from '@xcan-angus/vue-ui';
import { utils, IPane, ExtractionSource } from '@xcan-angus/infra';
import { useI18n } from 'vue-i18n';
import { BasicProps } from '@/types/types';

const { t } = useI18n();

const props = withDefaults(defineProps<BasicProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined
});

const DataSetList = defineAsyncComponent(() => import('@/views/data/dataset/list/index.vue'));
const DataSetListDetail = defineAsyncComponent(() => import('@/views/data/dataset/detail/index.vue'));

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
      if (!ids.includes('dataSetList')) {
        return {
          _id: 'dataSetList',
          value: 'dataSetList',
          name: t('dataset.title'),
          closable: false
        };
      }
    });
  }

  // Watch for browser tab changes and ensure case list tab exists
  watch(() => browserTabRef.value, () => {
    if (typeof browserTabRef.value?.update === 'function') {
      const tabData = browserTabRef.value.getData().map(item => item.value);
      if (!tabData.includes('dataSetList')) {
        addTabPane({
          _id: 'dataSetList',
          value: 'dataSetList',
          name: t('dataset.title'),
          closable: false
        });
      } else {
        updateTabPane({
          _id: 'dataSetList',
          value: 'dataSetList',
          name: t('dataset.title'),
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

  const { id, source } = queryParameters;
  if (id) {
    browserTabRef.value.add(() => {
      return {
        _id: id,
        value: 'dataSetDetails',
        data: { _id: id, id, source }
      };
    });
  } else if (source) {
    browserTabRef.value.add(() => {
      let name = t('dataset.actions.addStaticDataset');
      if (source === ExtractionSource.FILE) {
        name = t('dataset.actions.addFileDataset');
      } else if (source === ExtractionSource.JDBC) {
        name = t('dataset.actions.addJdbcDataset');
      }

      const uid = utils.uuid();
      return {
        name,
        _id: uid,
        value: 'dataSetDetails',
        data: { _id: uid, source }
      };
    });
  }

  router.replace('/data#dataSet');
};

const storageKeyChange = () => {
  initialize();
};

const storageKey = computed(() => {
  if (!props.projectId) {
    return undefined;
  }
  return `dataSet${props.projectId}`;
});

onMounted(() => {
  watch(() => route.hash, () => {
    if (!route.hash.startsWith('#dataSet')) {
      return;
    }
    hashChange(route.hash);
  });
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
    :userId="props.userInfo?.id"
    :storageKey="storageKey"
    @storageKeyChange="storageKeyChange">
    <template #default="record">
      <template v-if="record.value === 'dataSetList'">
        <DataSetList
          v-bind="record"
          :userInfo="props.userInfo"
          :appInfo="props.appInfo"
          :projectId="props.projectId" />
      </template>

      <template v-else-if="record.value === 'dataSetDetails'">
        <DataSetListDetail
          v-bind="record"
          :userInfo="props.userInfo"
          :appInfo="props.appInfo"
          :projectId="props.projectId" />
      </template>
    </template>
  </BrowserTab>
</template>
