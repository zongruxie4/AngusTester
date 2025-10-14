<script setup lang="ts">
// Vue core imports
import { computed, defineAsyncComponent } from 'vue';
import { useI18n } from 'vue-i18n';

// UI component imports
import { NoData, Colon } from '@xcan-angus/vue-ui';
import { Badge } from 'ant-design-vue';

// Infrastructure imports
import { utils } from '@xcan-angus/infra';

// Local imports
import { PipelineConfig } from './PropsType';
import { ExecContent } from '@/components/FunctionTestDetail/PropsType';

const { t } = useI18n();

/**
 * HTTP error interface
 */
interface HttpError {
  exitCode: string;
  message: string;
}

/**
 * Scheduling result interface
 */
interface SchedulingResult {
  success: boolean;
  message: string;
  execId: string;
  console: string[];
  exitCode: string;
  deviceId: string;
}

/**
 * Debug result value interface
 */
interface DebugResultValue {
  sampleContents: ExecContent[];
  task: {
    arguments: {
      ignoreAssertions: boolean;
    };
    pipelines: PipelineConfig[];
  };
  meterMessage?: string;
  meterStatus?: string;
  schedulingResult?: SchedulingResult;
}

/**
 * Component props interface for debug result
 */
interface Props {
  httpError?: HttpError;
  value: DebugResultValue;
}

// Component props with defaults
const props = withDefaults(defineProps<Props>(), {
  value: undefined,
  httpError: undefined
});

// Async component imports
const TransStartTestDetail = defineAsyncComponent(() => import('../FunctionTestDetail/Collapse/TransStart/index.vue'));
const WaitingTimeTestDetail = defineAsyncComponent(() => import('../FunctionTestDetail/Collapse/WaitingTime/index.vue'));
const ThroughputTestDetail = defineAsyncComponent(() => import('../FunctionTestDetail/Collapse/Throughput/index.vue'));
const RendezvousTestDetail = defineAsyncComponent(() => import('../FunctionTestDetail/Collapse/Rendezvous/index.vue'));
const HTTPTestDetail = defineAsyncComponent(() => import('../FunctionTestDetail/Collapse/HTTP/index.vue'));
const TransEndTestDetail = defineAsyncComponent(() => import('../FunctionTestDetail/Collapse/TransEnd/index.vue'));

// Computed properties
const processedPipelines = computed(() => {
  if (!props?.value?.task?.pipelines?.length) {
    return [];
  }

  const pipelineList = props.value.task.pipelines;
  const httpPipelineCount = pipelineList.filter(item => item.target === 'HTTP').length;
  return pipelineList.reduce((previousPipelines, currentPipeline) => {
    const processedPipeline = { ...currentPipeline, linkName: currentPipeline.name, id: utils.uuid() } as any;
    if (httpPipelineCount === 1 && processedPipeline.target === 'HTTP') {
      processedPipeline.linkName = 'Total';
    }
    if (processedPipeline.transactionName) {
      if (!previousPipelines[previousPipelines.length - 1].children?.length) {
        previousPipelines[previousPipelines.length - 1].children = [processedPipeline];
      } else {
        previousPipelines[previousPipelines.length - 1].children.push(processedPipeline);
      }
    } else {
      previousPipelines.push(processedPipeline);
    }

    return previousPipelines;
  }, [] as any[]);
});

const shouldIgnoreAssertions = computed(() => {
  return !!props.value?.task?.arguments?.ignoreAssertions;
});

const sampleContents = computed(() => {
  return props.value?.sampleContents || [];
});

const hasNoData = computed(() => {
  return !props.httpError && !props.value;
});

const schedulingErrorInfo = computed(() => {
  const schedulingResult = props.value?.schedulingResult;
  if (!schedulingResult || schedulingResult.success === true) {
    return undefined;
  }

  return {
    exitCode: schedulingResult.exitCode,
    message: schedulingResult.message
  };
});

const meterErrorInfo = computed(() => {
  if (!props.value?.meterMessage) {
    return undefined;
  }

  return {
    exitCode: props.value.meterStatus,
    message: props.value.meterMessage
  };
});

const hasErrors = computed(() => {
  return !!props.httpError || !!schedulingErrorInfo.value || !!meterErrorInfo.value;
});
</script>

<template>
  <div class="h-full leading-5 space-y-3 px-5 py-3 text-3 overflow-auto">
    <template v-if="!hasNoData">
      <div v-if="hasErrors" class="space-y-2">
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
        <template v-else-if="schedulingErrorInfo">
          <div class="flex items-start">
            <div class="flex items-center w-16 text-theme-sub-content">{{ t('common.exitCode') }}<Colon /></div>
            <div>{{ schedulingErrorInfo.exitCode }}</div>
          </div>
          <div class="flex items-start">
            <div class="flex items-center w-16 text-theme-sub-content">{{ t('common.failureReason') }}<Colon /></div>
            <div class="max-w-200 break-all whitespace-pre-wrap">{{ schedulingErrorInfo.message }}</div>
          </div>
        </template>
        <template v-else-if="meterErrorInfo">
          <div class="flex items-start">
            <div class="flex items-center w-16 text-theme-sub-content">{{ t('common.samplingStatus') }}<Colon /></div>
            <div>{{ meterErrorInfo.exitCode }}</div>
          </div>
          <div class="flex items-start">
            <div class="flex items-center w-16 text-theme-sub-content">{{ t('common.failureReason') }}<Colon /></div>
            <div class="max-w-200 break-all whitespace-pre-wrap">{{ meterErrorInfo.message }}</div>
          </div>
        </template>
      </div>
      <template v-else>
        <template v-for="pipelineItem in processedPipelines" :key="pipelineItem.id">
          <TransStartTestDetail
            v-if="pipelineItem.target==='TRANS_START'"
            :value="pipelineItem"
            :content="sampleContents">
            <template v-if="(pipelineItem as any).children?.length">
              <template v-for="childItem in (pipelineItem as any).children" :key="childItem.id">
                <WaitingTimeTestDetail
                  v-if="childItem.target==='WAITING_TIME'"
                  :value="childItem"
                  class="embed" />
                <ThroughputTestDetail
                  v-else-if="childItem.target==='THROUGHPUT'"
                  :value="childItem"
                  class="embed" />
                <RendezvousTestDetail
                  v-else-if="childItem.target==='RENDEZVOUS'"
                  :value="childItem"
                  class="embed" />
                <HTTPTestDetail
                  v-else-if="childItem.target==='HTTP'"
                  :value="childItem"
                  :content="sampleContents"
                  :ignoreAssertions="shouldIgnoreAssertions"
                  class="embed" />
                <TransEndTestDetail
                  v-else-if="childItem.target==='TRANS_END'"
                  :value="childItem" />
              </template>
            </template>
          </TransStartTestDetail>
          <template v-if="!(pipelineItem as any).transactionName">
            <WaitingTimeTestDetail
              v-if="pipelineItem.target==='WAITING_TIME'"
              :value="pipelineItem" />
            <ThroughputTestDetail
              v-else-if="pipelineItem.target==='THROUGHPUT'"
              :value="pipelineItem" />
            <RendezvousTestDetail
              v-else-if="pipelineItem.target==='RENDEZVOUS'"
              :value="pipelineItem" />
            <HTTPTestDetail
              v-else-if="pipelineItem.target==='HTTP'"
              :value="pipelineItem"
              :content="sampleContents"
              :ignoreAssertions="shouldIgnoreAssertions" />
            <TransEndTestDetail
              v-else-if="pipelineItem.target==='TRANS_END'"
              :value="pipelineItem" />
          </template>
        </template>
      </template>
    </template>
    <NoData v-else size="small" />
  </div>
</template>
