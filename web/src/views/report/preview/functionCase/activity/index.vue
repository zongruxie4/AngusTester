<script setup lang="ts">
import { computed } from 'vue';

import { ReportContent } from '../PropsType';

type Props = {
  projectInfo: { [key: string]: any };
  userInfo: { [key: string]: any };
  appInfo: { [key: string]: any };
  dataSource: ReportContent;
}

const props = withDefaults(defineProps<Props>(), {
  projectInfo: undefined,
  userInfo: undefined,
  appInfo: undefined,
  dataSource: undefined
});

const activities = computed(() => {
  return props.dataSource?.content?.activities;
});

const len = computed(() => {
  let _len = 0;
  if (activities.value?.length) {
    _len = activities.value?.length - 1;
  }
  return _len;
});
</script>

<template>
  <div>
    <h1 class="text-theme-title font-medium mb-3.5">
      <span id="a11" class="text-4 text-theme-title font-medium">十一、<em class="inline-block w-0.25"></em>活动</span>
    </h1>

    <div v-if="len>0" class="border border-solid border-border-input">
      <div class="flex border-b border-solid border-border-input">
        <div
          class="w-35 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
          活动时间
        </div>
        <div
          class="w-27 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
          活动人
        </div>
        <div
          class="flex-1 flex items-center bg-blue-table px-1.5 py-1.5">
          活动描述
        </div>
      </div>

      <div
        v-for="(item,_index) in activities"
        :key="item.id"
        :class="{'border-b':_index<len}"
        class="flex border-solid border-border-input">
        <div class="w-35 px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
          {{ item.optDate }}
        </div>
        <div class="w-27 px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
          {{ item.fullName }}
        </div>
        <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap" v-html="item.detail"></div>
      </div>
    </div>

    <div v-else class="content-text-container">无</div>
  </div>
</template>

<style scoped>
.browser-container {
 text-indent: 2em;
}
</style>
