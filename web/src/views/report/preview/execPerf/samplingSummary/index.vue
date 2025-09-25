<script lang="ts" setup>
import { onMounted, ref, watch } from 'vue';
import { utils } from '@xcan-angus/infra';
import { useI18n } from 'vue-i18n';
import { ExecContent, ExecInfo, ExecResult, ReportInfo, SummaryListItem } from '../../PropsType';

const { t } = useI18n();

type Props = {
  projectInfo: { [key: string]: any };
  userInfo: { [key: string]: any };
  appInfo: { [key: string]: any };
  reportInfo: ReportInfo;
  execInfo: ExecInfo;
  execContent: ExecContent[];
  execResult: ExecResult;
  summaryList: SummaryListItem[];
}

const props = withDefaults(defineProps<Props>(), {
  projectInfo: undefined,
  userInfo: undefined,
  appInfo: undefined,
  reportInfo: undefined,
  execInfo: undefined,
  execContent: undefined,
  execResult: undefined,
  summaryList: () => []
});

const sampleSummaryList = ref<SummaryListItem['values']>([]);

onMounted(() => {
  watch(() => props.summaryList, (newValue) => {
    if (newValue.length === 0) {
      sampleSummaryList.value = [];
      return;
    }

    // 后台返回的数据最后一条是最新数据
    const latestValues = newValue[newValue.length - 1].values;
    sampleSummaryList.value = latestValues.map(item => {
      return {
        ...item,
        id: utils.uuid(),
        brps: transformUnit(item.brps),
        bwps: transformUnit(item.bwps)
      };
    });
  }, { immediate: true });
});

const transformUnit = (byteStr: string) => {
  const byte = +byteStr;
  if (byte === 0) {
    return '0B';
  }

  if (byte < 1024) {
    return byte + 'B';
  }

  let size = byte / 1024;
  if (size < 1024) {
    return size.toFixed(2) + 'KB';
  }

  size = byte / 1024 / 1024;
  if (size < 1024) {
    return size.toFixed(2) + 'MB';
  }

  size = byte / 1024 / 1024 / 1024;
  return size.toFixed(2) + 'GB';
};
</script>
<template>
  <div>
    <h1 class="text-theme-title font-medium mb-5">
      <span id="a4" class="text-4 text-theme-title font-medium">{{ t('reportPreview.serial.2') }}<em class="inline-block w-0.25"></em>{{ t('reportPreview.execPerf.samplingSummary.title') }}</span>
    </h1>

    <div class="border border-solid border-border-input">
      <div class="flex border-b border-solid border-border-input">
        <div
          class="flex-1 flex-shrink-0 bg-blue-table px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
          <span>{{ t('common.name') }}</span>
        </div>
        <div
          class="w-16 flex-shrink-0 bg-blue-table px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
          <span>{{ t('reportPreview.execPerf.samplingSummary.fields.samplingCount') }}</span>
        </div>
        <div
          class="w-16 flex-shrink-0 bg-blue-table px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
          <span>{{ t('reportPreview.execPerf.samplingSummary.fields.transactionCount') }}</span>
        </div>

        <div
          class="w-20 flex-shrink-0 bg-blue-table px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
          <span>{{ t('reportPreview.execPerf.samplingSummary.fields.tps') }}</span>
        </div>
        <div
          class="w-14 flex-shrink-0 bg-blue-table px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
          <span>{{ t('reportPreview.execPerf.samplingSummary.fields.errorRate') }}</span>
        </div>

        <div
          class="w-65 flex-shrink-0 bg-blue-table px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
          <span>{{ t('reportPreview.execPerf.samplingSummary.fields.responseTimeStats') }}</span>
        </div>
        <div
          class="w-19 flex-shrink-0 bg-blue-table px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
          <span>{{ t('reportPreview.execPerf.samplingSummary.fields.downloadPerSecond') }}</span>
        </div>
        <div class="w-19 flex-shrink-0 bg-blue-table px-1.5 py-1.5 break-all whitespace-pre-wrap">
          <span>{{ t('reportPreview.execPerf.samplingSummary.fields.uploadPerSecond') }}</span>
        </div>
      </div>

      <div
        v-for="item in sampleSummaryList"
        :key="item.id"
        class="flex border-b border-solid border-border-input last:border-0">
        <div class="flex-1 flex-shrink-0 px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
          {{ item.name }}
        </div>
        <div class="w-16 flex-shrink-0 px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
          {{ item.n }}
        </div>
        <div class="w-16 flex-shrink-0 px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
          {{ item.transactions }}
        </div>

        <div class="w-20 flex-shrink-0 px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
          {{ item.ops }}
        </div>
        <div class="w-14 flex-shrink-0 px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
          {{ item.errorRate }}%
        </div>
        <div class="w-65 flex-shrink-0 px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
          {{ item.tranMean }} / {{ item.tranMin }} / {{ item.tranMax }} / {{ item.tranP50 }} / {{ item.tranP75 }} / {{ item.tranP90 }} / {{ item.tranP99 }} / {{ item.tranP999 }}
        </div>
        <div class="w-19 flex-shrink-0 px-1.5 py-1.5 break-all whitespace-pre-wrap border-r border-solid border-border-input">
          {{ item.brps }}
        </div>
        <div class="w-19 flex-shrink-0 px-1.5 py-1.5 break-all whitespace-pre-wrap">
          {{ item.bwps }}
        </div>
      </div>
    </div>
  </div>
</template>
