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

const ReviewList = defineAsyncComponent(() => import('@/views/function/review/list/index.vue'));
const ReviewDetail = defineAsyncComponent(() => import('@/views/function/review/detail/index.vue'));
const ReviewEdit = defineAsyncComponent(() => import('@/views/function/review/edit/index.vue'));

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
      if (!ids.includes('reviewList')) {
        return {
          _id: 'reviewList',
          value: 'reviewList',
          name: t('caseReview.review'),
          closable: false // Whether to allow closing, true - allow closing, false - prohibit closing
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
          value: 'reviewEdit',
          noCache: true,
          data: { _id: id, id }
        };
      });
    } else {
      browserTabRef.value.add(() => {
        return {
          _id: id + '-case',
          value: 'reviewDetails',
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
          name: t('caseReview.addReview'),
          value: 'reviewEdit',
          noCache: true,
          data: { _id: id }
        };
      });
    }
  }

  router.replace('/function#reviews');
};

const storageKeyChange = () => {
  initialize();
};

onMounted(() => {
  watch(() => route.hash, () => {
    if (!route.hash.startsWith('#reviews')) {
      return;
    }

    hashChange(route.hash);
  });
});

const storageKey = computed(() => {
  if (!props.projectId) {
    return undefined;
  }

  return `review${props.projectId}`;
});

// Add specified tabPane
provide('addTabPane', addTabPane);

// Get tabPane
provide('getTabPane', getTabPane);

// Delete specified tabPane
provide('deleteTabPane', deleteTabPane);

// Update specified tabPane
provide('updateTabPane', updateTabPane);

// Replace specified tabPane
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

      <!-- <template v-else-if="record.value === 'reviewDetails'">
        <case
          v-bind="record"
          :userInfo="props.userInfo"
          :appInfo="props.appInfo"
          :projectId="props.projectId" />
      </template>

      <template v-else-if="record.value === 'reviewEdit'">
        <edit
          v-bind="record"
          :userInfo="props.userInfo"
          :appInfo="props.appInfo"
          :projectId="props.projectId" />
      </template> -->
    </template>
  </BrowserTab>
</template>
