<script setup lang="ts">
import { computed } from 'vue';
import dayjs from 'dayjs';
import duration from 'dayjs/plugin/duration';
import { useI18n } from 'vue-i18n';
import { ReportContent } from '../../../PropsType';

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

dayjs.extend(duration);

const indicatorPerf = computed(() => {
  return props.dataSource?.content?.testResult?.indicatorPerf;
});

const sampleSummary = computed(() => {
  return props.dataSource?.content?.testResult?.sampleSummary;
});

const nodeUsageSummary = computed(() => {
  return props.dataSource?.content?.testResult?.nodeUsageSummary;
});

const durationResult = computed(() => {
  const d1 = sampleSummary.value?.duration;
  if (!d1) {
    return '0';
  }

  let _result = '';
  const d2 = indicatorPerf.value?.duration;
  if (d2.includes('ms')) {
    _result = dayjs.duration(+d1).asMilliseconds() + 'ms';
  } else if (d2.includes('s')) {
    _result = dayjs.duration(+d1).asSeconds().toFixed(2) + 's';
  } else if (d2.includes('min')) {
    _result = dayjs.duration(+d1).asMinutes().toFixed(2) + 'min';
  } else if (d2.includes('h')) {
    _result = dayjs.duration(+d1).asHours().toFixed(2) + 'h';
  } else if (d2.includes('d')) {
    _result = dayjs.duration(+d1).asDays().toFixed(2) + 'd';
  }

  return _result.replace(/.00/g, '');
});

const artResult = computed(() => {
  const percentile = indicatorPerf.value?.percentile.value;
  let percentitleKey = '';
  switch (percentile) {
    case 'ALL':
      percentitleKey = 'tranMax';
      break;
    case 'P50':
      percentitleKey = 'tranP50';
      break;
    case 'P75':
      percentitleKey = 'tranP75';
      break;
    case 'P90':
      percentitleKey = 'tranP90';
      break;
    case 'P95':
      percentitleKey = 'tranP95';
      break;
    case 'P99':
      percentitleKey = 'tranP99';
      break;
    case 'P999':
      percentitleKey = 'tranP999';
      break;
    default:
      percentitleKey = '';
  }

  return sampleSummary.value[percentitleKey];
});
</script>

<template>
  <div class="mb-4">
    <h3 class="flex items-center space-x-2.5 mb-1.5">
      <span id="a10" class="flex items-center space-x-1.5"><em
        class="inline-block w-1.25 h-1.25 rounded bg-gray-500"></em><span>{{ t('reportPreview.task.info.testing.perf.title') }}</span></span>
    </h3>
    <div class="border border-solid border-border-input">
      <div class="flex border-b border-solid border-border-input">
        <div class="w-37 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
          {{ t('reportPreview.task.info.testing.perf.resultInfo.testParams') }}
        </div>
        <div class="flex-1 flex items-center bg-blue-table px-1.5 py-1.5 border-r border-solid border-border-input">
          {{ t('reportPreview.task.info.testing.perf.resultInfo.testMetrics') }}
        </div>
        <div class="flex-1 flex items-center bg-blue-table px-1.5 py-1.5">
          {{ t('common.status') }}
        </div>
      </div>

      <div class="flex border-b border-solid border-border-input">
        <div class="w-37 px-1.5 py-1.5 flex-shrink-0 flex items-center break-all whitespace-pre-wrap border-r border-solid border-border-input">
          {{ t('reportPreview.task.info.testing.perf.resultInfo.fields.concurrency') }}
        </div>
        <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
          {{ indicatorPerf?.threads }}
        </div>
        <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
          {{ sampleSummary?.threadPoolSize }}
        </div>
      </div>

      <div class="flex border-b border-solid border-border-input">
        <div class="w-37 px-1.5 py-1.5 flex-shrink-0 flex items-center break-all whitespace-pre-wrap border-r border-solid border-border-input">
          {{ t('reportPreview.task.info.testing.perf.resultInfo.fields.testDuration') }}
        </div>
        <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
          {{ indicatorPerf?.duration }}
        </div>
        <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
          {{ durationResult }}
        </div>
      </div>

      <div class="flex border-b border-solid border-border-input">
        <div class="w-37 px-1.5 py-1.5 flex-shrink-0 flex items-center break-all whitespace-pre-wrap border-r border-solid border-border-input">
          {{ t('reportPreview.task.info.testing.perf.resultInfo.fields.rampUpConcurrency') }}
        </div>
        <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
          {{ indicatorPerf?.rampUpThreads }}
        </div>
        <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
          {{ sampleSummary?.rampUpThreads }}
        </div>
      </div>

      <div class="flex border-b border-solid border-border-input">
        <div class="w-37 px-1.5 py-1.5 flex-shrink-0 flex items-center break-all whitespace-pre-wrap border-r border-solid border-border-input">
          {{ t('reportPreview.task.info.testing.perf.resultInfo.fields.rampUpDuration') }}
        </div>
        <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
          {{ indicatorPerf?.rampUpInterval }}
        </div>
        <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap">
          {{ sampleSummary?.rampUpInterval }}
        </div>
      </div>

      <div class="flex border-b border-solid border-border-input">
        <div class="w-37 px-1.5 py-1.5 flex-shrink-0 flex items-center break-all whitespace-pre-wrap border-r border-solid border-border-input">
          {{ t('protocol.responseTime') }}
        </div>
        <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
          {{ indicatorPerf?.percentile?.value + '<=' + indicatorPerf?.art }}
        </div>
        <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap flex items-center">
          <span>{{ artResult }}</span>
          <span v-if="+artResult > +indicatorPerf?.art" class="ml-0.5">
            <svg
              t="1725158770315"
              class="icon"
              viewBox="0 0 1024 1024"
              version="1.1"
              xmlns="http://www.w3.org/2000/svg"
              p-id="14543"
              width="12"
              height="13"><path
                d="M576.28571416 299.47142833V962H447.71428584V305.77142833L248.42857168 499.52857168 158.42857168 405.73571416 512 62l353.57142832 343.73571416-90 87.49285752z"
                fill="#F5222D"
                p-id="14544"></path></svg>
          </span>
          <span v-else-if="+artResult < +indicatorPerf?.art" class="ml-0.5">
            <svg
              t="1725158626606"
              class="icon"
              viewBox="0 0 1024 1024"
              version="1.1"
              xmlns="http://www.w3.org/2000/svg"
              p-id="14262"
              width="12"
              height="13"><path
                d="M576.28571416 724.52857167V62H447.71428584v656.22857168L248.42857168 524.47142832 158.42857168 618.26428584 512 962l353.57142832-343.73571416-90-87.49285752z"
                fill="#52C41A"
                p-id="14263"></path></svg>
          </span>
        </div>
      </div>

      <div class="flex border-b border-solid border-border-input">
        <div class="w-37 px-1.5 py-1.5 flex-shrink-0 flex items-center break-all whitespace-pre-wrap border-r border-solid border-border-input">
          {{ t('reportPreview.task.info.testing.perf.resultInfo.fields.tps') }}
        </div>
        <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
          {{ indicatorPerf?.tps }}
        </div>
        <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap flex items-center">
          <span>{{ sampleSummary?.tps }}</span>
          <span v-if="+sampleSummary?.tps > +indicatorPerf?.tps" class="ml-0.5">
            <svg
              t="1725158770315"
              class="icon"
              viewBox="0 0 1024 1024"
              version="1.1"
              xmlns="http://www.w3.org/2000/svg"
              p-id="14543"
              width="12"
              height="13"><path
                d="M576.28571416 299.47142833V962H447.71428584V305.77142833L248.42857168 499.52857168 158.42857168 405.73571416 512 62l353.57142832 343.73571416-90 87.49285752z"
                fill="#F5222D"
                p-id="14544"></path></svg>
          </span>
          <span v-else-if="+sampleSummary?.tps < +indicatorPerf?.tps" class="ml-0.5">
            <svg
              t="1725158626606"
              class="icon"
              viewBox="0 0 1024 1024"
              version="1.1"
              xmlns="http://www.w3.org/2000/svg"
              p-id="14262"
              width="12"
              height="13"><path
                d="M576.28571416 724.52857167V62H447.71428584v656.22857168L248.42857168 524.47142832 158.42857168 618.26428584 512 962l353.57142832-343.73571416-90-87.49285752z"
                fill="#52C41A"
                p-id="14263"></path></svg>
          </span>
        </div>
      </div>

      <div class="flex border-b border-solid border-border-input">
        <div class="w-37 px-1.5 py-1.5 flex-shrink-0 flex items-center break-all whitespace-pre-wrap border-r border-solid border-border-input">
          {{ t('reportPreview.task.info.testing.perf.resultInfo.fields.errorRate') }}
        </div>
        <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input">
          {{ indicatorPerf?.errorRate }}
        </div>
        <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap flex items-center">
          <span>{{ sampleSummary?.errorRate }}</span>
          <span v-if="+sampleSummary?.errorRate > +indicatorPerf?.errorRate" class="ml-0.5">
            <svg
              t="1725158770315"
              class="icon"
              viewBox="0 0 1024 1024"
              version="1.1"
              xmlns="http://www.w3.org/2000/svg"
              p-id="14543"
              width="12"
              height="13"><path
                d="M576.28571416 299.47142833V962H447.71428584V305.77142833L248.42857168 499.52857168 158.42857168 405.73571416 512 62l353.57142832 343.73571416-90 87.49285752z"
                fill="#F5222D"
                p-id="14544"></path></svg>
          </span>
          <span v-else-if="+sampleSummary?.errorRate < +indicatorPerf?.errorRate" class="ml-0.5">
            <svg
              t="1725158626606"
              class="icon"
              viewBox="0 0 1024 1024"
              version="1.1"
              xmlns="http://www.w3.org/2000/svg"
              p-id="14262"
              width="12"
              height="13"><path
                d="M576.28571416 724.52857167V62H447.71428584v656.22857168L248.42857168 524.47142832 158.42857168 618.26428584 512 962l353.57142832-343.73571416-90-87.49285752z"
                fill="#52C41A"
                p-id="14263"></path></svg>
          </span>
        </div>
      </div>

      <div class="flex">
        <div class="w-37 px-1.5 py-1.5 flex-shrink-0 flex items-center break-all whitespace-pre-wrap border-r border-solid border-border-input">
          {{ t('reportPreview.task.info.testing.perf.systemLoad.title') }}
        </div>
        <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap space-y-1">
          <div class="flex-1 flex items-center space-x-1">
            <span>{{ t('reportPreview.task.info.testing.perf.systemLoad.cpuUsage') }}</span>
            <span>{{ '<=' }}</span>
            <span>{{ indicatorPerf?.cpu }}%</span>
          </div>

          <div class="flex-1 flex items-center space-x-1">
            <span>{{ t('reportPreview.task.info.testing.perf.systemLoad.memoryUsage') }}</span>
            <span>{{ '<=' }}</span>
            <span>{{ indicatorPerf?.memory }}%</span>
          </div>

          <div class="flex-1 flex items-center space-x-1">
            <span>{{ t('reportPreview.task.info.testing.perf.systemLoad.diskUsage') }}</span>
            <span>{{ '<=' }}</span>
            <span>{{ indicatorPerf?.disk }}%</span>
          </div>

          <div class="flex-1 flex items-center space-x-1">
            <span>{{ t('reportPreview.task.info.testing.perf.systemLoad.networkUsage') }}</span>
            <span>{{ '<=' }}</span>
            <span>{{ indicatorPerf?.network }}MB</span>
          </div>
        </div>
        <div class="flex-1 px-1.5 py-1.5 break-all  whitespace-pre-wrap border-r border-solid border-border-input space-y-1">
          <div class="flex items-center">
            <div class="flex-1 flex items-center space-x-1">
              <span>{{ t('reportPreview.task.info.testing.perf.systemLoad.cpuUsage') }}</span>
              <span>{{ nodeUsageSummary?.meanCpu ? `${nodeUsageSummary?.meanCpu}%` : '--' }}</span>
            </div>
            <div class="flex-1 flex items-center space-x-1">
              <span>{{ t('chart.max') }}</span>
              <span>{{ nodeUsageSummary?.maxCpu ? `${nodeUsageSummary?.maxCpu}%` : '--' }}</span>
            </div>
          </div>

          <div class="flex items-center">
            <div class="flex-1 flex items-center space-x-1">
              <span>{{ t('reportPreview.task.info.testing.perf.systemLoad.memoryUsage') }}</span>
              <span>{{ nodeUsageSummary?.meanMemory ? `${nodeUsageSummary?.meanMemory}%` : '--' }}</span>
            </div>
            <div class="flex-1 flex items-center space-x-1">
              <span>{{ t('chart.max') }}</span>
              <span>{{ nodeUsageSummary?.maxMemory ? `${nodeUsageSummary?.maxMemory}%` : '--' }}</span>
            </div>
          </div>

          <div class="flex items-center">
            <div class="flex-1 flex items-center space-x-1">
              <span>{{ t('reportPreview.task.info.testing.perf.systemLoad.diskUsage') }}</span>
              <span>{{ nodeUsageSummary?.meanFilesystem ? `${nodeUsageSummary?.meanFilesystem}%` : '--' }}</span>
            </div>
            <div class="flex-1 flex items-center space-x-1">
              <span>{{ t('chart.max') }}</span>
              <span>{{ nodeUsageSummary?.maxFilesystem ? `${nodeUsageSummary?.maxFilesystem}%` : '--' }}</span>
            </div>
          </div>

          <div class="flex items-center">
            <div class="flex-1 flex items-center space-x-1">
              <span>{{ t('reportPreview.task.info.testing.perf.systemLoad.networkUsage') }}</span>
              <span>{{ nodeUsageSummary?.meanNetwork ? `${nodeUsageSummary?.meanNetwork}MB` : '--' }}</span>
            </div>
            <div class="flex-1 flex items-center space-x-1">
              <span>{{ t('chart.max') }}</span>
              <span>{{ nodeUsageSummary?.maxNetwork ? `${nodeUsageSummary?.maxNetwork}MB` : '--' }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.content-text-container {
  text-indent: 2em;
}
</style>
