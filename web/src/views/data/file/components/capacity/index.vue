<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue';
import { Button, Progress, Spin } from 'ant-design-vue';
import { Icon } from '@xcan-angus/vue-ui';

import { space } from '@/api/storage';
import { store } from '@/api/store';
import { SourceType } from './type';
import { appContext } from '@xcan-angus/infra';

interface Props {
  id: string
}

const props = withDefaults(defineProps<Props>(), {});

const state = reactive<{
  loading: boolean,
  source: SourceType
}>({
  loading: false,
  source: {
  }
});

const percent = ref(0);
const couldDiskUrl = ref();

const loadCloudDiskPayUrl = async () => {
  const isPrivate = appContext.isPrivateEdition();
  if (isPrivate) {
    return;
  }
  const [error, res] = await store.getStoreList({ code: 'CloudDisk' });
  if (error) {
    return;
  }
  couldDiskUrl.value = res.data.list?.[0].pricingUrl;
};

const load = async () => {
  state.loading = true;
  const [error, res] = await space.getSpaceDetail(props.id);
  state.loading = false;
  if (error) {
    return;
  }
  state.source = res.data.summary;
  percent.value = Math.round(+(state.source.usage || 0) * 10000) / 100;
};

// const getSpaceName = computed(() => {
//   return props.id ? '空间容量：' : '账户存储容量：';
// });

watch(() => props.id, newValue => {
  if (!newValue) {
    return;
  }
  load();
}, {
  immediate: true
});

onMounted(() => {
  loadCloudDiskPayUrl();
});

</script>
<template>
  <div class="text-3 text-center px-4">
    <Spin
      tip="Loading..."
      :spinning="state.loading">
      <!-- <div class="mb-12.5 text-left">
        <span class="text-theme-title font-medium mr-2.5">{{ getSpaceName }}</span>
        <span>
          {{ state.source?.quotaSize?.value || '' }}{{ state.source?.quotaSize?.unit?.message || '' }}
        </span>
      </div> -->
      <Progress
        type="circle"
        class="mt-12"
        :percent="percent"
        :stroke-width="4"
        :width="100"
        strokeColor="#52C41A">
        <template #format>
          <p class="text-3 text-theme-sub-content">已使用</p>
          <p class="text-6 my-4 text-theme-title font-semibold">{{ percent }}%</p>
        </template>
      </Progress>
      <Progress
        :percent="percent"
        :showInfo="false"
        :stroke-width="6"
        class="mt-7 block" />
      <p class="text-theme-sub-content">{{ state.source?.usedSize }} / {{ state.source?.quotaSize?.value || '' }}{{ state.source?.quotaSize?.unit?.message || '' }}</p>
      <div v-if="couldDiskUrl" class="py-2 px-3 rounded bg-gray-light text-theme-special mt-5 cursor-pointer inline-block">
        <Button
          :href="couldDiskUrl"
          type="link"
          size="small"
          target="_blank">
          <Icon icon="icon-shangchuan" class="mr-2" />升级容量
        </Button>
      </div>
    </Spin>
  </div>
</template>
