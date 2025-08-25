<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { utils } from '@xcan-angus/infra';
import { GroupText, TestBasicInfo } from '@xcan-angus/vue-ui';
import { Timeline, TimelineItem } from 'ant-design-vue';

import { ExecInfo, ExecContent } from './PropsType';

import Collapse from './Collapse/index.vue';

const { t } = useI18n();

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

    const tempList = JSON.parse(JSON.stringify(newValue || '[]'));
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
  const httpNum = _pipelines.filter(item => item.target === 'HTTP').length;
  return _pipelines?.reduce((prev, cur) => {
    const _cur = { ...cur, linkName: cur.name, id: utils.uuid() };
    if (httpNum === 1 && _cur.target === 'HTTP') {
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
    httpNum = pipelines.filter(item => item.target === 'HTTP' && item.enabled).length;
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
  return props.execInfo?.status?.value === 'RUNNING' ? t('httPlugin.functionTestDetail.pending') : false;
});

const texts = computed(() => [
  t('httPlugin.functionTestDetail.texts.success'),
  t('httPlugin.functionTestDetail.texts.ignore'),
  t('httPlugin.functionTestDetail.texts.block'),
  t('httPlugin.functionTestDetail.texts.total')
]);

const timeTexts = computed(() => [
  t('httPlugin.functionTestDetail.timeTexts.minimum'),
  t('httPlugin.functionTestDetail.timeTexts.average'),
  t('httPlugin.functionTestDetail.timeTexts.maximum')
]);
</script>
<template>
  <div class="px-5">
    <TestBasicInfo
      :value="props.execInfo"
      :exception="props.exception"
      class="mb-5" />

    <div class="flex items-center text-3 leading-5 space-x-3 mb-8">
      <div
        class="border border-l-4 rounded p-3.5 space-y-1.5 boder-border-divider"
        style="width: calc((100% - 48px) / 5);border-left:4px solid rgba(129, 154, 218, 100%);">
        <div class="text-text-title text-4 font-semibold" style="color:rgba(129, 154, 218, 100%);">
          {{ duration?.[1] }}<span class="text-3.25 ml-0.5">{{ duration?.[2] }}</span>
        </div>
        <div>{{ t('httPlugin.functionTestDetail.basicInfo.duration') }}</div>
      </div>

      <div
        class="border border-l-4 rounded p-3.5 space-y-1.5 boder-border-divider"
        style="width: calc((100% - 48px) / 5);border-left:4px solid rgba(3, 185, 208, 100%);">
        <div class="flex items-center text-text-title text-4 font-semibold" style="color:rgba(3, 185, 208, 100%);">
          <span>{{ iterationNum }}</span>
          <em class="not-italic inline-block w-0.5 h-3.5 mx-1.5 rounded" style="transform: rotate(25deg);background-color: rgba(3, 185, 208, 100%);"></em>
          <span>{{ planIterationNum }}</span>
        </div>
        <div>{{ t('httPlugin.functionTestDetail.basicInfo.iteration') }}</div>
      </div>

      <div
        class="border border-l-4 rounded p-3.5 space-y-1.5 boder-border-divider"
        style="width: calc((100% - 48px) / 5);border-left:4px solid rgba(3, 206, 92, 100%);">
        <div class="flex items-center text-text-title text-4 font-semibold" style="color:rgba(3, 206, 92, 100%);">
          <span>{{ requestNum }}</span>
          <em class="not-italic inline-block w-0.5 h-3.5 mx-1.5 rounded" style="transform: rotate(25deg);background-color: rgba(3, 206, 92, 100%);"></em>
          <span>{{ planRequestNum }}</span>
        </div>
        <div>{{ t('httPlugin.functionTestDetail.basicInfo.request') }}</div>
      </div>

      <div
        class="border border-l-4 rounded p-3.5 space-y-1.5 boder-border-divider"
        style="width: calc((100% - 48px) / 5);border-left:4px solid rgba(45, 142, 255, 100%);">
        <div class="flex items-center text-text-title text-4 font-semibold whitespace-nowrap overflow-hidden" style="color:rgba(45, 142, 255, 100%);">
          <span>{{ successAssertionNum }}</span>
          <em class="not-italic inline-block w-0.5 h-3.5 mx-2 rounded" style="background-color: rgba(45, 142, 255, 100%);"></em>
          <span>{{ ignoreAssertionNum }}</span>
          <em class="not-italic inline-block w-0.5 h-3.5 mx-2 rounded" style="background-color: rgba(45, 142, 255, 100%);"></em>
          <span>{{ disabledAssertionNum }}</span>
          <em class="not-italic inline-block w-0.5 h-3.5 mx-2 rounded" style="background-color: rgba(45, 142, 255, 100%);"></em>
          <span>{{ totalAssertionNum }}</span>
        </div>
        <div class="whitespace-nowrap overflow-hidden">
          {{ t('common.assertion') }}（<GroupText :texts="texts" class="text-theme-sub-content" />）
        </div>
      </div>

      <div
        class="border border-l-4 rounded p-3.5 space-y-1.5 boder-border-divider"
        style="width: calc((100% - 48px) / 5);border-left:4px solid rgba(255, 129, 0, 100%);">
        <div class="flex items-center text-text-title text-4 font-semibold whitespace-nowrap overflow-hidden" style="color:rgba(255, 129, 0, 100%);">
          <span>{{ tranMin }}</span>
          <em class="not-italic inline-block w-0.5 h-3.5 mx-2 rounded" style="background-color: rgba(255, 129, 0, 100%);"></em>
          <span>{{ tranMean }}</span>
          <em class="not-italic inline-block w-0.5 h-3.5 mx-2 rounded" style="background-color: rgba(255, 129, 0, 100%);"></em>
          <span>{{ tranMax }}</span>
        </div>
        <div class="whitespace-nowrap overflow-hidden">
          {{ t('httPlugin.functionTestDetail.basicInfo.responseTime') }}（<GroupText :texts="timeTexts" class="text-theme-sub-content" />）
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
          :iterations="item"
          :ignoreAssertions="props.execInfo?.ignoreAssertions" />
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

</style>
