<script setup lang="ts">
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';
import { ReportContent } from '../PropsType';

type Props = {
  projectInfo: { [key: string]: any };
  userInfo: { [key: string]: any };
  appInfo: { [key: string]: any };
  dataSource: ReportContent;
}

const { t } = useI18n();

const props = withDefaults(defineProps<Props>(), {
  projectInfo: undefined,
  userInfo: undefined,
  appInfo: undefined,
  dataSource: undefined
});

const refCaseInfos = computed(() => {
  return props.dataSource?.content?.task?.refCaseInfos || [];
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
      <span id="a7" class="text-4 text-theme-title font-medium">
        {{ t('reportPreview.serial.7') }}、
        <em class="inline-block w-0.25"></em>
        {{ t('reportPreview.task.assocCase.title') }}
      </span>
    </h1>

    <div v-if="len>0" class="border border-solid border-border-input">
      <div class="flex border-b border-solid border-border-input">
        <div
          class="w-37 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
          {{ t('common.id') }}
        </div>
        <div
          class="w-27 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
          {{ t('common.code') }}
        </div>
        <div
          class="flex-1 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
          {{ t('common.name') }}
        </div>
        <div
          class="w-25 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
          {{ t('common.testStatus') }}
        </div>
        <div
          class="w-25 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
          {{ t('common.reviewStatus') }}
        </div>
        <div
          class="w-25 flex items-center bg-blue-table px-1.5 py-1.5 ">
          {{ t('common.tester') }}
        </div>
      </div>

      <div
        v-for="(item,index) in refCaseInfos"
        :key="item.id"
        :class="{'border-b':index<len-1}"
        class="flex border-solid border-border-input">
        <div class="w-37 px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
          {{ item.id }}
        </div>
        <div class="w-27 px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
          {{ item.code }}
        </div>
        <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
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

    <div v-else class="content-text-container">{{ t('common.noData') }}</div>
  </div>
</template>

<style scoped>
.content-text-container{
  text-indent: 2em;
}
</style>
