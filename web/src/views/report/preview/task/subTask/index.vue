<script setup lang="ts">
import { computed } from 'vue';
import { Progress } from 'ant-design-vue';
import { useI18n } from 'vue-i18n';
import { ReportContent } from '../PropsType';

const { t } = useI18n();

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

const subTaskInfos = computed(() => {
  return props.dataSource?.content?.task?.subTaskInfos || [];
});

const subTaskProgress = computed(() => {
  return props.dataSource?.content?.task?.subTaskProgress || {};
});

const len = computed(() => {
  let _len = 0;
  if (subTaskInfos.value?.length) {
    _len = subTaskInfos.value?.length;
  }
  return _len;
});
</script>

<template>
  <div>
    <h1 class="text-theme-title font-medium mb-3.5">
      <span id="a5" class="text-4 text-theme-title font-medium">{{ t('reportPreview.serial.5') }}<em class="inline-block w-0.25"></em>{{ t('reportPreview.task.subTask.title') }}</span>
    </h1>

    <div v-if="len>0" class="flex space-x-2 text-3.5 font-semibold">
      <span>{{ t('reportPreview.task.subTask.progress') }} {{ (subTaskProgress?.completedRate || 0) + '%' }}</span>
      <Progress
        :percent="subTaskProgress?.completedRate"
        size="small"
        class="w-30" />
      <span></span>
      <span>{{ subTaskProgress?.completed }}/{{ subTaskProgress?.total }}</span>
    </div>
    <div v-if="len>0" class="border border-solid border-border-input">
      <div class="flex border-b border-solid border-border-input">
        <div
          class="w-37 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
          {{ t('reportPreview.task.subTask.fields.id') }}
        </div>
        <div
          class="w-27 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
          {{ t('reportPreview.task.subTask.fields.code') }}
        </div>
        <div
          class="w-19 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
          {{ t('reportPreview.task.subTask.fields.type') }}
        </div>
        <div
          class="flex-1 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
          {{ t('reportPreview.task.subTask.fields.name') }}
        </div>
        <div
          class="w-20 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
          {{ t('reportPreview.task.subTask.fields.status') }}
        </div>
        <div
          class="w-20 flex items-center bg-blue-table px-1.5 py-1.5">
          {{ t('reportPreview.task.subTask.fields.assignee') }}
        </div>
      </div>

      <div
        v-for="(item,index) in subTaskInfos"
        :key="item.id"
        :class="{'border-b':index<len-1}"
        class="flex border-solid border-border-input">
        <div class="w-37 px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
          {{ item.id }}
        </div>
        <div class="w-27 px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
          {{ item.code }}
        </div>
        <div class="w-19 px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
          {{ item.taskType?.message }}
        </div>
        <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
          {{ item.name }}
        </div>
        <div class="w-20 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
          {{ item?.status?.message }}
        </div>
        <div class="w-20 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
          {{ item.assigneeName }}
        </div>
      </div>
    </div>

    <div v-else class="content-text-container">{{ t('reportPreview.task.subTask.noData') }}</div>
  </div>
</template>

<style scoped>
.content-text-container{
  text-indent: 2em;
}
</style>
