<script setup lang="ts">
import { computed, defineAsyncComponent } from 'vue';
import { NoData, Colon } from '@xcan-angus/vue-ui';
import { utils } from '@xcan-angus/infra';
import { Badge } from 'ant-design-vue';
import { useI18n } from 'vue-i18n';

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

const { t } = useI18n();

const TransStartTestDetail = defineAsyncComponent(() => import('../FunctionTestDetail/Collapse/TransStart/index.vue'));
const WaitingTimeTestDetail = defineAsyncComponent(() => import('../FunctionTestDetail/Collapse/WaitingTime/index.vue'));
const RendezvousTestDetail = defineAsyncComponent(() => import('../FunctionTestDetail/Collapse/Rendezvous/index.vue'));
const JdbcTestDetail = defineAsyncComponent(() => import('../FunctionTestDetail/Collapse/Jdbc/index.vue'));
const TransEndTestDetail = defineAsyncComponent(() => import('../FunctionTestDetail/Collapse/TransEnd/index.vue'));

const pipelines = computed(() => {
  if (!props?.value?.task?.pipelines?.length) {
    return [];
  }

  const list = props.value.task.pipelines;
  const httpNum = list.filter(item => item.target === 'JDBC').length;
  return list.reduce((prev, cur) => {
    const _cur = { ...cur, linkName: cur.name, id: utils.uuid() };
    if (httpNum === 1 && _cur.target === 'JDBC') {
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
  }, []);
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
  <div v-if="!isEmpty" class="h-full leading-5 space-y-3 p-5 text-3 overflow-auto">
    <div v-if="isError" class="space-y-2">
      <div class="flex items-start">
        <div class="flex items-center w-16 text-theme-sub-content">{{ t('common.debugResult') }}<Colon /></div>
        <Badge status="error" :text="t('status.failed')" />
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
              <RendezvousTestDetail
                v-else-if="_item.target==='RENDEZVOUS'"
                :value="_item"
                class="embed" />
              <JdbcTestDetail
                v-else-if="_item.target==='JDBC'"
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
          <RendezvousTestDetail
            v-else-if="item.target==='RENDEZVOUS'"
            :value="item" />
          <JdbcTestDetail
            v-else-if="item.target==='JDBC'"
            :value="item"
            :content="sampleContents"
            :ignoreAssertions="ignoreAssertions" />
          <TransEndTestDetail
            v-else-if="item.target==='TRANS_END'"
            :value="item" />
        </template>
      </template>
    </template>
  </div>
  <NoData v-else size="small" />
</template>
