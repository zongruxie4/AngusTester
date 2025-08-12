<script setup lang="ts">
import { computed, defineAsyncComponent, onMounted, provide, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useI18n } from 'vue-i18n';
import { BrowserTab } from '@xcan-angus/vue-ui';
import { utils } from '@xcan-angus/infra';

type Props = {
  projectId: string;
  userInfo: { id: string; };
  appInfo: { id: string; };
}

interface IPane {

}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined
});

const AnalysisList = defineAsyncComponent(() => import('@/views/task/analysis/list/index.vue'));
const AnalysisEdit = defineAsyncComponent(() => import('@/views/task/analysis/edit/index.vue'));
const AnalysisDetail = defineAsyncComponent(() => import('@/views/task/analysis/detail/index.vue'));

const route = useRoute();
const router = useRouter();
const { t } = useI18n();
const browserTabRef = ref();
const refreshListNotify = ref(0);
const activeKey = ref('analysisList');
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
      if (!ids.includes('analysisList')) {
        return {
          _id: 'analysisList',
          value: 'analysisList',
          name: t('taskAnalysis.title'),
          closable: false // 是否允许关闭，true - 允许关闭，false - 禁止关闭
        };
      }
    });
  }

  hashChange(route.hash);
};

const hashChange = (hash: string) => {
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
          value: 'analysisEdit',
          noCache: true,
          data: { _id: id, id }
        };
      });
    } else {
      browserTabRef.value.add(() => {
        return {
          _id: `analysisDetail_${id}`,
          value: 'analysisDetail',
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
          name: t('taskAnalysis.addAnalysis'),
          value: 'analysisEdit',
          noCache: true,
          data: { _id: id }
        };
      });
    }
  }

  router.replace('/task#analysis');
};

const storageKeyChange = () => {
  initialize();
};

const handleEditSuccess = () => {
  refreshListNotify.value += 1;
};

onMounted(() => {
  watch(() => route.hash, () => {
    if (!route.hash.startsWith('#list')) {
      return;
    }

    hashChange(route.hash);
  });
});

const storageKey = computed(() => {
  if (!props.projectId) {
    return undefined;
  }

  return `analysis_task_${props.projectId}`;
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
    v-model:activeKey="activeKey"
    hideAdd
    class="h-full"
    :userId="props.userInfo.id"
    :storageKey="storageKey"
    @storageKeyChange="storageKeyChange">
    <template #default="record">
      <template v-if="record.value === 'analysisList'">
        <AnalysisList
          :userInfo="props.userInfo"
          :projectId="props.projectId"
          :refreshNotify="refreshListNotify"
          :onShow="activeKey === 'analysisList'" />
      </template>
      <template v-if="record.value==='analysisEdit'">
        <AnalysisEdit
          :data="record.data"
          :userInfo="props.userInfo"
          :projectId="props.projectId"
          @ok="handleEditSuccess" />
      </template>
      <template v-if="record.value==='analysisDetail'">
        <AnalysisDetail
          :data="record.data"
          :userInfo="props.userInfo"
          :projectId="props.projectId"
          :onShow="activeKey === record._id" />
      </template>
    </template>
  </BrowserTab>
</template>
