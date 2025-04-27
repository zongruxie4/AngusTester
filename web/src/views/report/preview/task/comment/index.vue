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

const taskType = computed(() => {
  return props.dataSource?.content?.task?.taskType?.value;
});

const index = computed(() => {
  if (taskType.value === 'API_TEST' || taskType.value === 'SCENARIO_TEST') {
    return 13;
  }

  return 10;
});

const indexText = computed(() => {
  if (index.value === 10) {
    return '十';
  }

  return '十';
});

const treeToList = (result:ReportContent['content']['comments'], list:ReportContent['content']['comments'], userName?:string) => {
  if (list.length) {
    for (let i = 0, len = list.length; i < len; i++) {
      const item = list[i];
      result.push({
        ...item,
        parentName: userName
      });

      const children = item.children || [];
      if (children.length) {
        treeToList(result, children, item.userName);
      }
    }
  }
};

const comments = computed(() => {
  const result:ReportContent['content']['comments'] = [];
  const list = props.dataSource?.content?.comments;
  if (list?.length) {
    treeToList(result, list);
  }

  return result;
});

const len = computed(() => {
  let _len = 0;
  if (comments.value?.length) {
    _len = comments.value?.length - 1;
  }
  return _len;
});
</script>

<template>
  <div>
    <h1 class="text-theme-title font-medium mb-3.5">
      <span :id="`a${index}`" class="text-4 text-theme-title font-medium">{{ indexText }}、<em class="inline-block w-0.25"></em>评论</span>
    </h1>

    <div v-if="len>0" class="border border-solid border-border-input">
      <div class="flex border-b border-solid border-border-input">
        <div
          class="w-35 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
          评论时间
        </div>
        <div
          class="w-27 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
          评论人
        </div>
        <div
          class="flex-1 flex items-center bg-blue-table px-1.5 py-1.5">
          评论内容
        </div>
      </div>

      <div
        v-for="(item,_index) in comments"
        :key="item.id"
        :class="{'border-b':_index<len}"
        class="flex border-solid border-border-input">
        <div class="w-35 px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
          {{ item.createdDate }}
        </div>
        <div class="w-27 px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
          {{ item.userName }}
        </div>
        <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
          <template v-if="item.parentName">
            <span class="mr-1">回复</span>
            <span>{{ item.parentName }}</span>
            <span class="mr-2.5">:</span>
          </template>
          <span>{{ item.content }}</span>
        </div>
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
