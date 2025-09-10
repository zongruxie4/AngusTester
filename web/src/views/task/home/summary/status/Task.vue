<script setup lang="ts">
import { defineAsyncComponent, onMounted, ref, watch } from 'vue';
import { Icon } from '@xcan-angus/vue-ui';

import { ResourceInfo } from '@/views/task/home/types';

type Props = {
  dataSource: ResourceInfo;
}

const props = withDefaults(defineProps<Props>(), {
  dataSource: undefined
});

const ChartPie = defineAsyncComponent(() => import('./TaskChartPie.vue'));

const total = ref<string>('0');
const overduetTtal = ref<string>('0');
const pendingTotal = ref<string>('0');
const progressingTotal = ref<string>('0');
const completedTotal = ref<string>('0');
const canceledTotal = ref<string>('0');
const confirmingTotal = ref<string>('0');

onMounted(() => {
  watch(() => props.dataSource, (newValue) => {
    if (newValue === undefined) {
      return;
    }

    total.value = newValue.allTask;
    overduetTtal.value = newValue.taskByOverdue;
    pendingTotal.value = newValue.taskByStatus?.PENDING;
    progressingTotal.value = newValue.taskByStatus?.IN_PROGRESS;
    completedTotal.value = newValue.taskByStatus?.COMPLETED;
    canceledTotal.value = newValue.taskByStatus?.CANCELED;
    confirmingTotal.value = newValue.taskByStatus?.CONFIRMING;
  }, { immediate: true });
});
</script>

<template>
  <div class="rounded border border-solid border-theme-text-box pt-2">
    <div class="font-semibold px-4">
      <span class="mr-2">总任务</span>
      <span class="text-4">{{ total }}</span>
    </div>
    <div class="flex items-center pr-2">
      <div class="flex-1 flex items-center justify-center flex-wrap content-center transform-gpu translate-y-2">
        <div class="item-container flex items-center space-x-2.5 justify-center mb-4">
          <Icon icon="icon-daiceshi" class="text-10 flex-shrink-0" />
          <div class="whitespace-nowrap space-y-1">
            <div class="text-theme-sub-content">待处理</div>
            <div class="text-4">{{ pendingTotal }}</div>
          </div>
        </div>

        <div class="item-container flex items-center space-x-2.5 justify-center mb-4">
          <Icon icon="icon-daiqueren" class="text-10 flex-shrink-0" />
          <div class="whitespace-nowrap space-y-1">
            <div class="text-theme-sub-content">待确认</div>
            <div class="text-4">{{ confirmingTotal }}</div>
          </div>
        </div>

        <div class="item-container flex items-center space-x-2.5 justify-center mb-4">
          <Icon icon="icon-yiyuqi1" class="text-10 flex-shrink-0" />
          <div class="whitespace-nowrap space-y-1">
            <div class="text-theme-sub-content">逾期<span class="inline-block" style="width:1em;"></span></div>
            <div class="text-4">{{ overduetTtal }}</div>
          </div>
        </div>

        <div class="item-container flex items-center space-x-2.5 justify-center mb-4">
          <Icon icon="icon-renwuceshizhong" class="text-10 flex-shrink-0" />
          <div class="whitespace-nowrap space-y-1">
            <div class="text-theme-sub-content">处理中</div>
            <div class="text-4">{{ progressingTotal }}</div>
          </div>
        </div>

        <div class="item-container flex items-center space-x-2.5 justify-center mb-4">
          <Icon icon="icon-jiaobenzhuyezhihang" class="text-10 flex-shrink-0" />
          <div class="whitespace-nowrap space-y-1">
            <div class="text-theme-sub-content">已完成</div>
            <div class="text-4">{{ completedTotal }}</div>
          </div>
        </div>

        <div class="item-container flex items-center space-x-2.5 justify-center mb-4">
          <Icon icon="icon-yiquxiao" class="text-10 flex-shrink-0" />
          <div class="whitespace-nowrap space-y-1">
            <div class="text-theme-sub-content">已取消</div>
            <div class="text-4">{{ canceledTotal }}</div>
          </div>
        </div>
      </div>

      <ChartPie :dataSource="props.dataSource" class="flex-1" />
    </div>
  </div>
</template>

<style scoped>
.item-container {
  width: 33.33%;
}

@media screen and (max-width: 1800px) {
  .item-container {
    width: 50%;
  }
}
</style>
