<script setup lang="ts">
import { onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Icon } from '@xcan-angus/vue-ui';

interface Props {
  dataSource: {[key: string]: string}
}

const { t } = useI18n();
const props = withDefaults(defineProps<Props>(), {
  dataSource: () => ({})
});

// Configuration for test case statistics display
const configInfo = [
  [{ label: t('status.total'), dataIndex: 'totalNum', bgColor: 'bg-blue-1' },
    { label: t('status.passed'), dataIndex: 'successNum', bgColor: 'bg-status-success' },
    { label: t('status.notPassed'), dataIndex: 'failNum', bgColor: 'bg-status-error' },
    { label: t('status.disabled'), dataIndex: 'disabledNum', bgColor: 'bg-gray-icon' }]
];

// Configuration for test case type icons
const CaseTypeIconConfig = {
  SMOKE: 'icon-maoyanceshi',
  SECURITY: 'icon-anquanceshi',
  USER_DEFINED: 'icon-zidingyiceshi'
};

// Test case statistics data
const statisticsData = ref({});

// Test case results list
const caseResult = ref([]);

// Initialize component and watch for data changes
onMounted(() => {
  watch(() => props.dataSource, newValue => {
    statisticsData.value = newValue.caseSummary || {};
    caseResult.value = newValue.caseResults || [];
  }, {
    immediate: true
  });
});

</script>
<template>
  <div class="font-semibold mt-5 mb-2">{{ t('common.testCases') }}</div>
  <div class="space-y-2 text-3">
    <!-- Display test case statistics -->
    <div
      v-for="(line, idx) in configInfo"
      :key="idx"
      class="flex space-x-2">
      <div
        v-for="item in line"
        :key="item.dataIndex"
        class="flex h-6 leading-6 w-40">
        <span class="flex-1 text-white  px-2 rounded" :class="item.bgColor">{{ item.label }}</span>
        <span class="flex-1 bg-gray-light px-2 rounded-r">{{ statisticsData[item.dataIndex] }}</span>
      </div>
    </div>

    <!-- Display individual test case results -->
    <div class="font-semibold mt-5 mb-2">{{ t('execution.testResult.testedCases') }}</div>
    <div class="text-3 space-y-1">
      <div
        v-for="item in caseResult"
        :key="item.id"
        class="border border-gray-light rounded bg-gray-light">
        <div class="px-1 flex h-6 items-center">
          <!-- Display case type icon -->
          <Icon :icon="CaseTypeIconConfig[item.caseType?.value] || 'icon-jiekouyongli2'" class="mr-1 text-4" />

          <!-- Display case name or summary -->
          <span class="min-w-10 truncate flex-1" :title="item.apisName || item.caseName || item.summary">
            {{ item.apisName || item.caseName || item.summary }}
          </span>

          <!-- Display case status -->
          <span
            v-if="!item.enabled"
            class="px-2 rounded">{{ t('status.disabled') }}</span>
          <span
            v-else-if="item.passed"
            class="px-2 rounded text-status-success">{{ t('status.passed') }}</span>
          <span v-else class="px-2 rounded text-status-error ">
            {{ t('execution.testResult.notPassedWithReason') }}{{ item.failureMessage }}
          </span>
        </div>
      </div>
    </div>
  </div>
</template>
