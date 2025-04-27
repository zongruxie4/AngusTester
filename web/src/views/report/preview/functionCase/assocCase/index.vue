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

const refCaseInfos = computed(() => {
  return props.dataSource?.content?.cases?.assocCases || [];
});

const len = computed(() => {
  let _len = 0;
  if (refCaseInfos.value?.length) {
    _len = refCaseInfos.value?.length;
  }
  return _len;
});
</script>

<template>
  <div>
    <h1 class="text-theme-title font-medium mb-3.5">
      <span id="a10" class="text-4 text-theme-title font-medium">十、<em class="inline-block w-0.25"></em>关联用例</span>
    </h1>

    <div v-if="len>0" class="border border-solid border-border-input">
      <div class="flex border-b border-solid border-border-input">
        <div
          class="w-37 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
          ID
        </div>
        <div
          class="w-27 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
          编码
        </div>
        <div
          class="flex-1 flex items-center bg-blue-table px-1.5 py-1.5">
          名称
        </div>
        <div
          class="w-25 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
          测试状态
        </div>
        <div
          class="w-25 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
          评审状态
        </div>
        <div
          class="w-25 flex items-center bg-blue-table px-1.5 py-1.5 ">
          测试人
        </div>
      </div>

      <div
        v-for="(item,index) in refCaseInfos"
        :key="item.id"
        :class="{'border-b':index < len -1}"
        class="flex border-solid border-border-input">
        <div class="w-37 px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
          {{ item.id }}
        </div>
        <div class="w-27 px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
          {{ item.code }}
        </div>
        <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
          {{ item.name }}
        </div>
        <div class="w-25 px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
          {{ item.testResult?.message }}
        </div>
        <div class="w-25 px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
          {{ item.reviewStatus?.message }}
        </div>
        <div class="w-25 px-1.5 py-1.5 break-all whitespace-pre-wrap ">
          {{ item.testerName }}
        </div>
      </div>
    </div>

    <div v-else class="content-text-container">无</div>
  </div>
</template>

<style scoped>
.content-text-container{
  text-indent: 2em;
}
</style>
