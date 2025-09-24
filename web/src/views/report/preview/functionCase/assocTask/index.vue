<script setup lang="ts">
import { computed } from 'vue';
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

const refTaskInfos = computed(() => {
  return props.dataSource?.content?.cases?.assocTasks || [];
});

const len = computed(() => {
  let _len = 0;
  if (refTaskInfos.value?.length) {
    _len = refTaskInfos.value?.length;
  }
  return _len;
});
</script>

<template>
  <div>
    <h1 class="text-theme-title font-medium mb-3.5">
      <span id="a9" class="text-4 text-theme-title font-medium">{{ t('reportPreview.serial.9') }}<em class="inline-block w-0.25"></em>{{ t('reportPreview.functionCase.assocTask.title') }}</span>
    </h1>

    <div v-if="len>0" class="border border-solid border-border-input">
      <div class="flex border-b border-solid border-border-input">
        <div
          class="w-37 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
          {{ t('common.ID') }}
        </div>
        <div
          class="w-27 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
          {{ t('common.code') }}
        </div>
        <div
          class="w-22 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
          {{ t('common.type') }}
        </div>
        <div
          class="flex-1 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
          {{ t('common.name') }}
        </div>
        <div
          class="w-20 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
          {{ t('common.status') }}
        </div>
        <div
          class="w-20 flex items-center bg-blue-table px-1.5 py-1.5">
          {{ t('common.assignee') }}
        </div>
      </div>

      <div
        v-for="(item,index) in refTaskInfos"
        :key="item.id"
        :class="{'border-b':index<len-1}"
        class="flex border-solid border-border-input">
        <div class="w-37 px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
          {{ item.id }}
        </div>
        <div class="w-27 px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
          {{ item.code }}
        </div>
        <div class="w-22 px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
          {{ item.taskType?.message }}
        </div>
        <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
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

    <div v-else class="content-text-container">{{ t('common.noData') }}</div>
  </div>
</template>

<style scoped>
.content-text-container{
  text-indent: 2em;
}
</style>
