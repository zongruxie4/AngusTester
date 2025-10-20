<script lang="ts" setup>
// Vue core imports
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

/**
 * Component props interface for scenario test summary data
 */
interface Props {
  dataSource: { [key: string]: string };
}

// Component props with default values
const props = withDefaults(defineProps<Props>(), {
  dataSource: () => ({})
});

// Scenario test summary statistics configuration
const scenarioTestSummaryConfig = [
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
      v-for="(statisticsRow, rowIndex) in scenarioTestSummaryConfig"
      :key="rowIndex"
      class="flex space-x-2">
      <div
        v-for="statisticsItem in statisticsRow"
        :key="statisticsItem.dataIndex"
        class="flex flex-1 h-7 leading-7">
        <span
          v-if="statisticsItem.label"
          class="flex-1 text-white px-2 rounded"
          :class="statisticsItem.bgColor">{{ statisticsItem.label }}</span>
        <span v-if="statisticsItem.dataIndex" class="flex-1 bg-gray-light px-2 rounded-r">{{ props.dataSource[statisticsItem.dataIndex] }}</span>
      </div>
    </li>
  </div>
</template>
