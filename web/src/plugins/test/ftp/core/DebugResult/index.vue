<script setup lang="ts">
import { computed, defineAsyncComponent } from 'vue';
import { NoData, Colon } from '@xcan-angus/vue-ui';
import { utils } from '@xcan-angus/infra';
import { Badge } from 'ant-design-vue';
import { useI18n } from 'vue-i18n';

import { PipelineConfig } from '../PropsType';
import { ExecContent } from '../FunctionTestDetail/PropsType';

interface Props {
  httpError?:{
      exitCode:string;
      message:string;
    };
  value:{
    sampleContents:ExecContent[];
    task:{
      arguments:{
        ignoreAssertions:boolean;
      };
      pipelines:PipelineConfig[];
    };
    meterMessage?:string;
    meterStatus?:string;
    schedulingResult?:{
      success: boolean;
      message: string;
      execId: string;
      console: string[];
      exitCode: string;
      deviceId: string;
    };
  };
}

const props = withDefaults(defineProps<Props>(), {
  value: undefined,
  httpError: undefined
});

const { t } = useI18n();

const DebugResult = defineAsyncComponent(() => import('../FunctionTestDetail/Collapse/FTP/index.vue'));

const pipelines = computed(() => {
  if (!props?.value?.task?.pipelines?.length) {
    return [];
  }

  const list = props.value.task.pipelines;
  return list.reduce((prev, cur) => {
    const _cur = { ...cur, id: utils.uuid() };
    prev.push(_cur);
    return prev;
  }, [] as PipelineConfig[]);
});

const sampleContents = computed(() => {
  return props.value?.sampleContents || [];
});

const isEmpty = computed(() => {
  return !props.httpError && !props.value;
});

const schedulingErrorResult = computed(() => {
  const item = props.value?.schedulingResult;
  if (!item || item.success === true) {
    return undefined;
  }

  return {
    exitCode: item.exitCode,
    message: item.message
  };
});

const meterErrorResult = computed(() => {
  if (!props.value?.meterMessage) {
    return undefined;
  }

  return {
    exitCode: props.value.meterStatus,
    message: props.value.meterMessage
  };
});

const isError = computed(() => {
  return !!props.httpError || !!schedulingErrorResult.value || !!meterErrorResult.value;
});
</script>

<template>
  <div class="h-full leading-5 space-y-3 px-5 py-3 text-3 overflow-auto">
    <template v-if="!isEmpty">
      <div v-if="isError" class="space-y-2">
        <div class="flex items-start">
          <div class="flex items-center w-16 text-theme-sub-content">{{ t('common.debugResult') }}<Colon /></div>
          <Badge status="error" :text="t('ftpPlugin.debugResult.failed')" />
        </div>
        <template v-if="props.httpError">
          <div class="flex items-start">
            <div class="flex items-center w-16 text-theme-sub-content">{{ t('common.exitCode') }}<Colon /></div>
            <div>{{ props.httpError.exitCode }}</div>
          </div>
          <div class="flex items-start">
            <div class="flex items-center w-16 text-theme-sub-content">{{ t('common.failureReason') }}<Colon /></div>
            <div class="max-w-200 break-all whitespace-pre-wrap">{{ props.httpError.message }}</div>
          </div>
        </template>
        <template v-else-if="schedulingErrorResult">
          <div class="flex items-start">
            <div class="flex items-center w-16 text-theme-sub-content">{{ t('common.exitCode') }}<Colon /></div>
            <div>{{ schedulingErrorResult.exitCode }}</div>
          </div>
          <div class="flex items-start">
            <div class="flex items-center w-16 text-theme-sub-content">{{ t('common.failureReason') }}<Colon /></div>
            <div class="max-w-200 break-all whitespace-pre-wrap">{{ schedulingErrorResult.message }}</div>
          </div>
        </template>
        <template v-else-if="meterErrorResult">
          <div class="flex items-start">
            <div class="flex items-center w-16 text-theme-sub-content">{{ t('common.samplingStatus') }}<Colon /></div>
            <div>{{ meterErrorResult.exitCode }}</div>
          </div>
          <div class="flex items-start">
            <div class="flex items-center w-16 text-theme-sub-content">{{ t('common.failureReason') }}<Colon /></div>
            <div class="max-w-200 break-all whitespace-pre-wrap">{{ meterErrorResult.message }}</div>
          </div>
        </template>
      </div>
      <template v-else>
        <DebugResult
          v-for="item in pipelines"
          :key="item.id"
          :value="item"
          :content="sampleContents" />
      </template>
    </template>
    <NoData v-else size="small" />
  </div>
</template>
