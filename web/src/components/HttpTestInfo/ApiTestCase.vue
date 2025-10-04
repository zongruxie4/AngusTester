<script lang="ts" setup>
// Vue core imports
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

/**
 * Component props interface for API test case statistics
 */
interface Props {
  dataSource: { [key: string]: string };
}

// Component props with default values
const props = withDefaults(defineProps<Props>(), {
  dataSource: () => ({})
});

// Test case statistics configuration
const testCaseStatisticsConfig = [
  [
    { label: t('status.total'), dataIndex: 'totalNum', bgColor: 'bg-blue-1' },
    { label: t('status.passed'), dataIndex: 'successNum', bgColor: 'bg-status-success' }
  ],
  [
    { label: t('status.failed'), dataIndex: 'failNum', bgColor: 'bg-status-error' },
    { label: t('status.disabled'), dataIndex: 'disabledNum', bgColor: 'bg-gray-icon' }
  ]
];

</script>
<template>
  <div class="space-y-2 text-3">
    <li
      v-for="(statisticsRow, rowIndex) in testCaseStatisticsConfig"
      :key="rowIndex"
      class="flex space-x-2">
      <div
        v-for="statisticsItem in statisticsRow"
        :key="statisticsItem.dataIndex"
        class="flex flex-1 h-7 leading-7">
        <span class="flex-1 text-white px-2 rounded" :class="statisticsItem.bgColor">{{ statisticsItem.label }}</span>
        <span class="flex-1 bg-gray-light px-2 rounded-r">{{ props.dataSource[statisticsItem.dataIndex] }}</span>
      </div>
    </li>
  </div>
</template>
