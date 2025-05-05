<script setup lang="ts">
import { ref } from 'vue';
import { Colon, Scroll } from '@xcan-angus/vue-ui';
import { TESTER } from '@xcan-angus/tools';

import { ExecInfo } from './PropsType';

type Props = {
  projectId: string;
  scriptId: string;
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  scriptId: undefined
});

const dataList = ref<ExecInfo[]>([]);

const scrollChange = (list: ExecInfo[]) => {
  dataList.value = list;
};

const params = {
  pageSize: 5
};

const BG_COLOR = {
  CREATED: 'bg-status-pending',
  PENDING: 'bg-status-orange',
  RUNNING: 'bg-status-process',
  STOPPED: 'bg-status-close',
  FAILED: 'bg-status-error',
  COMPLETED: 'bg-status-success'
};
</script>

<template>
  <Scroll
    ref="scrollRef"
    :params="params"
    :action="`${TESTER}/exec/search?projectId=${props.projectId}&scriptId=${props.scriptId}`"
    class="scroll-container"
    @change="scrollChange">
    <div class="group-container space-y-7.5">
      <div
        v-for="item in dataList"
        :key="item.id"
        class="item-container relative text-3 leading-5 space-y-3.5">
        <div class="relative flex items-start">
          <div class="w-15.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>执行ID</span>
            <Colon class="w-1" />
          </div>

          <div class="whitespace-pre-wrap break-words break-all">{{ item.id }}</div>
        </div>

        <div class="relative flex items-start">
          <div class="w-15.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>执行名称</span>
            <Colon class="w-1" />
          </div>

          <RouterLink
            :to="`/execution/info/${item.id}`"
            target="_blank"
            class="block truncate text-text-link">
            {{ item.name }}
          </RouterLink>
        </div>

        <div class="relative flex items-start">
          <div class="w-15.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>状态</span>
            <Colon class="w-1" />
          </div>

          <div class="flex items-center whitespace-pre-wrap break-words break-all">
            <span class="w-1.5 h-1.5 rounded-xl mr-1" :class="BG_COLOR[item.status?.value]"></span>
            <span>{{ item.status?.message }}</span>
          </div>
        </div>

        <div class="relative flex items-start">
          <div class="w-15.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>开始时间</span>
            <Colon class="w-1" />
          </div>

          <div class="whitespace-pre-wrap break-words break-all">{{ item.actualStartDate || '--' }}</div>
        </div>

        <div class="relative flex items-start">
          <div class="w-15.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>结束时间</span>
            <Colon class="w-1" />
          </div>

          <div class="whitespace-pre-wrap break-words break-all">{{ item.endDate || '--' }}</div>
        </div>
      </div>
    </div>
  </Scroll>
</template>

<style scoped>
.scroll-container>:deep(div:nth-child(2)) {
  padding-right: 14px;
}

.item-container::after {
  content:'';
  display: block;
  position: absolute;
  bottom: -15px;
  width: 100%;
  height: 1px;
  background-color: var(--border-text-box);
}

.item-container:last-child::after {
  display: none;
}
</style>
