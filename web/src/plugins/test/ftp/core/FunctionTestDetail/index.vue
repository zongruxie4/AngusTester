<script setup lang="ts">
import { computed, onMounted, ref, watch, defineAsyncComponent } from 'vue';
import { utils } from '@xcan-angus/infra';
import { GroupText } from '@xcan-angus/vue-ui';
import { Timeline, TimelineItem } from 'ant-design-vue';
import { cloneDeep } from 'lodash-es';
import { useI18n } from 'vue-i18n';

import { ExecInfo, ExecContent } from './PropsType';

import Collapse from './Collapse/index.vue';
const TestBasicInfo = defineAsyncComponent(() => import('@/components/TestBasicInfo/index.vue'));

export interface Props {
  execInfo: ExecInfo;
  execContent: ExecContent[];
  exception: {
    codeName: string;
    messageName: string;
    code: string;
    message: string;
  };
}

const props = withDefaults(defineProps<Props>(), {
  execInfo: undefined,
  execContent: undefined,
  exception: undefined
});

const { t } = useI18n();

const execContentList = ref<ExecContent[]>([]);
const totalAssertionNum = ref(0);
const disabledAssertionNum = ref(0);
const ignoreAssertionNum = ref(0);
const successAssertionNum = ref(0);

onMounted(() => {
  watch(() => props.execContent, (newValue) => {
    if (!newValue?.length) {
      execContentList.value = [];
      return;
    }

    const tempList = cloneDeep(newValue || '[]');
    totalAssertionNum.value = 0;
    disabledAssertionNum.value = 0;
    ignoreAssertionNum.value = 0;
    successAssertionNum.value = 0;
    for (let i = 0, len = tempList.length; i < len; i++) {
      const item = tempList[i];
      const assertions = item.content?.assertions;
      let _successNum = 0;
      let enabledAssertions = [];
      if (assertions) {
        totalAssertionNum.value += assertions.length;
        disabledAssertionNum.value += assertions.filter(item => !item.enabled).length;
        ignoreAssertionNum.value += assertions.filter(item => item?.ignore).length;

        enabledAssertions = assertions.filter(item => item.enabled && !item?.ignore);
        _successNum = enabledAssertions.filter(item => item?.result?.failure === false).length;
        successAssertionNum.value += _successNum;
      }
    }

    execContentList.value = tempList.sort((a, b) => {
      return (+a.iteration) - (+b.iteration);
    });
  }, { immediate: true });
});

const execContentMap = computed(() => {
  return execContentList.value.reduce((prev, cur) => {
    if (!prev[cur.iteration]?.length) {
      prev[cur.iteration] = [cur];
    } else {
      prev[cur.iteration].push(cur);
    }

    return prev;
  }, {});
});

const pipelines = computed(() => {
  const _pipelines = props.execInfo?.task?.pipelines || [];
  return _pipelines?.reduce((prev, cur) => {
    const _cur = { ...cur, id: utils.uuid() };
    prev.push(_cur);
    return prev;
  }, []);
});

const iterations = computed(() => {
  return execContentList.value?.reduce((prev, cur) => {
    if (!prev.includes(cur.iteration)) {
      prev.push(cur.iteration);
    }

    return prev;
  }, []) || [];
});

const duration = computed(() => {
  const duration = props.execInfo?.sampleSummaryInfo?.duration;
  if (!duration) {
    return '--';
  }

  return utils.formatTime(+duration).match(/(\d+\.?\d*)([^\d]+)/);
});

const planIterationNum = computed(() => {
  return props.execInfo?.iterations || '1';
});

const iterationNum = computed(() => {
  return props.execInfo?.sampleSummaryInfo?.iterations || '--';
});

const planRequestNum = computed(() => {
  const _iterationNum = +planIterationNum.value;
  let httpNum = 0;
  const pipelines = props.execInfo?.task?.pipelines;
  if (pipelines?.length) {
    httpNum = pipelines.filter(item => item.enabled).length;
  }

  return _iterationNum * httpNum;
});

const requestNum = computed(() => {
  return props.execInfo?.sampleSummaryInfo?.n || '--';
});

const tranMean = computed(() => {
  const _tranMean = props.execInfo?.sampleSummaryInfo?.tranMean;
  if (!_tranMean) {
    return '--';
  }

  return _tranMean + 'ms';
});

const tranMin = computed(() => {
  const _tranMin = props.execInfo?.sampleSummaryInfo?.tranMin;
  if (!_tranMin) {
    return '--';
  }

  return _tranMin + 'ms';
});

const tranMax = computed(() => {
  const _tranMax = props.execInfo?.sampleSummaryInfo?.tranMax;
  if (!_tranMax) {
    return '--';
  }

  return _tranMax + 'ms';
});

const pending = computed(() => {
  return props.execInfo?.status?.value === 'RUNNING' ? t('status.pending') : false;
});

const timeTexts = computed(() => [
  t('chart.min'),
  t('chart.average'),
  t('chart.max')
]);
</script>
<template>
  <div class="px-5">
    <TestBasicInfo
      :value="props.execInfo"
      :exception="props.exception"
      :hasIgnoreAssertions="false"
      class="mb-5" />

    <div class="flex items-center text-3 leading-5 space-x-3 mb-8">
      <div
        class="flex-1 border border-l-4 rounded p-3.5 space-y-1.5 boder-border-divider"
        style="border-left:4px solid rgba(129, 154, 218, 100%);">
        <div class="text-text-title text-4 font-semibold" style="color:rgba(129, 154, 218, 100%);">
          {{ duration?.[1] }}<span class="text-3.25 ml-0.5">{{ duration?.[2] }}</span>
        </div>
        <div class="exec-basic-data-card-title">{{ t('common.duration') }}</div>
      </div>

      <div
        class="flex-1 border border-l-4 rounded p-3.5 space-y-1.5 boder-border-divider"
        style="border-left:4px solid rgba(3, 185, 208, 100%);">
        <div class="flex items-center text-text-title text-4 font-semibold" style="color:rgba(3, 185, 208, 100%);">
          <span>{{ iterationNum }}</span>
          <em class="not-italic inline-block w-0.5 h-3.5 mx-1.5 rounded" style="transform: rotate(25deg);background-color: rgba(3, 185, 208, 100%);"></em>
          <span>{{ planIterationNum }}</span>
        </div>
        <div class="exec-basic-data-card-title">{{ t('ftpPlugin.functionTestDetail.basicInfo.iteration') }}</div>
      </div>

      <div
        class="flex-1 border border-l-4 rounded p-3.5 space-y-1.5 boder-border-divider"
        style="border-left:4px solid rgba(3, 206, 92, 100%);">
        <div class="flex items-center text-text-title text-4 font-semibold" style="color:rgba(3, 206, 92, 100%);">
          <span>{{ requestNum }}</span>
          <em class="not-italic inline-block w-0.5 h-3.5 mx-1.5 rounded" style="transform: rotate(25deg);background-color: rgba(3, 206, 92, 100%);"></em>
          <span>{{ planRequestNum }}</span>
        </div>
        <div class="exec-basic-data-card-title">{{ t('protocol.request') }}</div>
      </div>

      <div
        class="flex-1 border border-l-4 rounded p-3.5 space-y-1.5 boder-border-divider"
        style="border-left:4px solid rgba(255, 129, 0, 100%);">
        <div class="flex items-center text-text-title text-4 font-semibold whitespace-nowrap overflow-hidden" style="color:rgba(255, 129, 0, 100%);">
          <span>{{ tranMin }}</span>
          <em class="not-italic inline-block w-0.5 h-3.5 mx-2 rounded" style="background-color: rgba(255, 129, 0, 100%);"></em>
          <span>{{ tranMean }}</span>
          <em class="not-italic inline-block w-0.5 h-3.5 mx-2 rounded" style="background-color: rgba(255, 129, 0, 100%);"></em>
          <span>{{ tranMax }}</span>
        </div>
        <div class="whitespace-nowrap overflow-hidden exec-basic-data-card-title">
          {{ t('ftpPlugin.functionTestDetail.basicInfo.responseTime') }}（<GroupText :texts="timeTexts" class="text-theme-sub-content" />）
        </div>
      </div>
    </div>

    <Timeline :pending="pending">
      <TimelineItem
        v-for="item in iterations"
        :key="item">
        <Collapse
          :pipelines="pipelines"
          :execContent="execContentMap[item]"
          :iterations="item" />
      </TimelineItem>
    </Timeline>
  </div>
</template>

<style scoped>
:deep(.ant-timeline-item) {
  padding-bottom: 0;
  font-size: 12px;
  line-height: 20px;
}

:deep(.ant-timeline-item-pending) .ant-timeline-item-content {
  top: -16px;
}

:deep(.ant-timeline-item-content) {
  top: -28px;
}

:deep(.ant-timeline-item-tail) {
  left:6px;
  border-width: 1px;
  border-style: dashed;
}

:deep(.ant-timeline-item-head) {
  position: relative;
  width: 15px;
  height: 15px;
}

:deep(.ant-timeline-item-head)::after {
  content: '';
  display: block;
  position: absolute;
  top: 3px;
  left: 3px;
  width: 5px;
  height: 5px;
  border-radius: 5px;
  background-color: #1890ff;
}

:deep(.ant-timeline-item-pending) .ant-timeline-item-head::after{
  display: none;
}
.exec-basic-data-card-title {
  @apply text-3.5 font-semibold;
}

</style>
