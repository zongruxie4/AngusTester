<script setup lang="ts">
import { computed, defineAsyncComponent, onMounted, provide, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useI18n } from 'vue-i18n';
import { BrowserTab } from '@xcan-angus/vue-ui';
import { utils, IPane } from '@xcan-angus/infra';

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

const SprintList = defineAsyncComponent(() => import('@/views/task/sprint/list/index.vue'));
const SprintDetail = defineAsyncComponent(() => import('@/views/task/sprint/detail/index.vue'));
const SprintEdit = defineAsyncComponent(() => import('@/views/task/sprint/edit/index.vue'));

const route = useRoute();
const router = useRouter();
const { t } = useI18n();
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
      if (!ids.includes('sprintList')) {
        return {
          _id: 'sprintList',
          value: 'sprintList',
          name: t('taskSprint.title'),
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
          value: 'sprintEdit',
          noCache: true,
          data: { _id: id, id }
        };
      });
    } else {
      browserTabRef.value.add(() => {
        return {
          _id: id + '-detail',
          value: 'sprintDetails',
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
          name: t('taskSprint.addSprint'),
          value: 'sprintEdit',
          noCache: true,
          data: { _id: id }
        };
      });
    }
  }

  router.replace('/task#sprint');
};

const storageKeyChange = () => {
  initialize();
};

onMounted(() => {
  watch(() => route.hash, () => {
    if (!route.hash.startsWith('#sprint')) {
      return;
    }

    hashChange(route.hash);
  });
});

const storageKey = computed(() => {
  if (!props.projectId) {
    return undefined;
  }

  return `sprint${props.projectId}`;
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
      <template v-if="record.value === 'sprintList'">
        <SprintList
          v-bind="record"
          :userInfo="props.userInfo"
          :appInfo="props.appInfo"
          :projectId="props.projectId" />
      </template>

      <template v-else-if="record.value === 'sprintDetails'">
        <SprintDetail
          v-bind="record"
          :userInfo="props.userInfo"
          :appInfo="props.appInfo"
          :projectId="props.projectId" />
      </template>

      <template v-else-if="record.value === 'sprintEdit'">
        <SprintEdit
          v-bind="record"
          :userInfo="props.userInfo"
          :appInfo="props.appInfo"
          :projectId="props.projectId" />
      </template>
    </template>
  </BrowserTab>
</template>
