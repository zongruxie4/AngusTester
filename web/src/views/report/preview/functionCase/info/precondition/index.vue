<script setup lang="ts">
import { computed } from 'vue';

import { ReportContent } from '../../PropsType';

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

const precondition = computed(() => {
  return props.dataSource?.content?.cases?.precondition;
});
</script>

<template>
  <div>
    <h1 class="text-theme-title font-medium mb-3.5">
      <span id="a4" class="text-4 text-theme-title font-medium">四、<em class="inline-block w-0.25"></em>前置条件</span>
    </h1>

    <div v-if="!!precondition?.length" class="break-all whitespace-pre-wrap">{{ precondition }}</div>

    <div v-else class="content-text-container ">无</div>
  </div>
</template>

<style scoped>
.content-text-container{
  text-indent: 2em;
}
</style>
