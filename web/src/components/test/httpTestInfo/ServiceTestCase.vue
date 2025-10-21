<script lang="ts" setup>
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

interface Props {
  dataSource: { [key: string]: string };
}

// Component props with default values
const props = withDefaults(defineProps<Props>(), {
  dataSource: () => ({})
});

// Service test case statistics configuration
const serviceTestCaseStatisticsConfig = [
  [
    { label: t('xcan_httpTestInfo.totalTestCount'), dataIndex: 'enabledTestNum' },
    { label: t('xcan_httpTestInfo.functionalTestCount'), dataIndex: 'enabledFuncTestNum' }
  ],
  [
    { label: t('xcan_httpTestInfo.performanceTestCount'), dataIndex: 'enabledPerfTestNum' },
    { label: t('xcan_httpTestInfo.stabilityTestCount'), dataIndex: 'enabledStabilityTestNum' }
  ]
];
</script>
<template>
  <div class="space-y-2 text-3">
    <li
      v-for="(statisticsRow, rowIndex) in serviceTestCaseStatisticsConfig"
      :key="rowIndex"
      class="flex space-x-2">
      <div
        v-for="statisticsItem in statisticsRow"
        :key="statisticsItem.dataIndex"
        class="flex flex-1 h-7 leading-7">
        <span class="flex-1 text-white bg-blue-6 px-2 rounded">{{ statisticsItem.label }}</span>
        <span class="flex-1 bg-gray-light pl-2 rounded-r">{{ props.dataSource[statisticsItem.dataIndex] || '--' }}</span>
      </div>
    </li>
  </div>
</template>
