<script setup lang="ts">
import { computed, defineAsyncComponent } from 'vue';
import { NoData, Colon } from '@xcan-angus/vue-ui';
import { utils } from '@xcan-angus/tools';
import { Badge } from 'ant-design-vue';

import { PipelineConfig } from '../UIConfig/PropsType.ts';
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

const MAILTestDetail = defineAsyncComponent(() => import('../FunctionTestDetail/Collapse/Mail/index.vue'));

const pipelines = computed(() => {
  if (!props?.value?.task?.pipelines?.length) {
    return [];
  }

  const list = props.value.task.pipelines;
  const httpNum = list.filter(item => item.target === 'MAIL').length;
  return list.reduce((prev, cur) => {
    const _cur = { ...cur, linkName: cur.name, id: utils.uuid() };
    if (httpNum === 1 && _cur.target === 'MAIL') {
      _cur.linkName = 'Total';
    }
    if (_cur.transactionName) {
      if (!prev[prev.length - 1].children?.length) {
        prev[prev.length - 1].children = [_cur];
      } else {
        prev[prev.length - 1].children.push(_cur);
      }
    } else {
      prev.push(_cur);
    }

    return prev;
  }, [] as PipelineConfig[]);
});

const ignoreAssertions = computed(() => {
  return !!props.value?.task?.arguments?.ignoreAssertions;
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
          <div class="flex items-center w-16 text-theme-sub-content">调试结果<Colon /></div>
          <Badge status="error" text="失败" />
        </div>
        <template v-if="props.httpError">
          <div class="flex items-start">
            <div class="flex items-center w-16 text-theme-sub-content">退出码<Colon /></div>
            <div>{{ props.httpError.exitCode }}</div>
          </div>
          <div class="flex items-start">
            <div class="flex items-center w-16 text-theme-sub-content">失败原因<Colon /></div>
            <div class="max-w-200 break-all whitespace-pre-wrap">{{ props.httpError.message }}</div>
          </div>
        </template>
        <template v-else-if="schedulingErrorResult">
          <div class="flex items-start">
            <div class="flex items-center w-16 text-theme-sub-content">退出码<Colon /></div>
            <div>{{ schedulingErrorResult.exitCode }}</div>
          </div>
          <div class="flex items-start">
            <div class="flex items-center w-16 text-theme-sub-content">失败原因<Colon /></div>
            <div class="max-w-200 break-all whitespace-pre-wrap">{{ schedulingErrorResult.message }}</div>
          </div>
        </template>
        <template v-else-if="meterErrorResult">
          <div class="flex items-start">
            <div class="flex items-center w-16 text-theme-sub-content">采样状态<Colon /></div>
            <div>{{ meterErrorResult.exitCode }}</div>
          </div>
          <div class="flex items-start">
            <div class="flex items-center w-16 text-theme-sub-content">失败原因<Colon /></div>
            <div class="max-w-200 break-all whitespace-pre-wrap">{{ meterErrorResult.message }}</div>
          </div>
        </template>
      </div>
      <template v-else>
        <template v-for="item in pipelines" :key="item.id">
          <MAILTestDetail
            v-if="item.target==='MAIL'"
            :value="item"
            :content="sampleContents"
            :ignoreAssertions="ignoreAssertions" />
        </template>
      </template>
    </template>
    <NoData v-else size="small" />
  </div>
</template>
