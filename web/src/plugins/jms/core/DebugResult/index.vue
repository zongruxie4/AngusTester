<script setup lang="ts">
import { computed, defineAsyncComponent } from 'vue';
import { NoData, Colon } from '@xcan-angus/vue-ui';
import { utils } from '@xcan-angus/infra';
import { Badge } from 'ant-design-vue';

import { PipelineConfig } from '../UIConfig/PropsType';
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

const TransStartTestDetail = defineAsyncComponent(() => import('../FunctionTestDetail/Collapse/TransStart/index.vue'));
const WaitingTimeTestDetail = defineAsyncComponent(() => import('../FunctionTestDetail/Collapse/WaitingTime/index.vue'));
const ThroughputTestDetail = defineAsyncComponent(() => import('../FunctionTestDetail/Collapse/Throughput/index.vue'));
const RendezvousTestDetail = defineAsyncComponent(() => import('../FunctionTestDetail/Collapse/Rendezvous/index.vue'));
const HTTPTestDetail = defineAsyncComponent(() => import('../FunctionTestDetail/Collapse/FTP/index.vue'));
const TransEndTestDetail = defineAsyncComponent(() => import('../FunctionTestDetail/Collapse/TransEnd/index.vue'));

const pipelines = computed(() => {
  if (!props?.value?.task?.pipelines?.length) {
    return [];
  }

  const list = props.value.task.pipelines;
  const httpNum = list.filter(item => item.target === 'FTP').length;
  return list.reduce((prev, cur) => {
    const _cur = { ...cur, linkName: cur.name, id: utils.uuid() };
    if (httpNum === 1 && _cur.target === 'FTP') {
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
          <TransStartTestDetail
            v-if="item.target==='TRANS_START'"
            :value="item"
            :content="sampleContents">
            <template v-if="item.children?.length">
              <template v-for="_item in item.children" :key="_item.id">
                <WaitingTimeTestDetail
                  v-if="_item.target==='WAITING_TIME'"
                  :value="_item"
                  class="embed" />
                <ThroughputTestDetail
                  v-else-if="_item.target==='THROUGHPUT'"
                  :value="_item"
                  class="embed" />
                <RendezvousTestDetail
                  v-else-if="_item.target==='RENDEZVOUS'"
                  :value="_item"
                  class="embed" />
                <HTTPTestDetail
                  v-else-if="_item.target==='FTP'"
                  :value="_item"
                  :content="sampleContents"
                  :ignoreAssertions="ignoreAssertions"
                  class="embed" />
                <TransEndTestDetail
                  v-else-if="_item.target==='TRANS_END'"
                  :value="_item" />
              </template>
            </template>
          </TransStartTestDetail>
          <template v-if="!item.transactionName">
            <WaitingTimeTestDetail
              v-if="item.target==='WAITING_TIME'"
              :value="item" />
            <ThroughputTestDetail
              v-else-if="item.target==='THROUGHPUT'"
              :value="item" />
            <RendezvousTestDetail
              v-else-if="item.target==='RENDEZVOUS'"
              :value="item" />
            <HTTPTestDetail
              v-else-if="item.target==='FTP'"
              :value="item"
              :content="sampleContents"
              :ignoreAssertions="ignoreAssertions" />
            <TransEndTestDetail
              v-else-if="item.target==='TRANS_END'"
              :value="item" />
          </template>
        </template>
      </template>
    </template>
    <NoData v-else size="small" />
  </div>
</template>
