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

const configInfo = [
  [{ label: t('execution.testResult.total'), dataIndex: 'totalNum', bgColor: 'bg-blue-1' }, { label: t('execution.testResult.passed'), dataIndex: 'successNum', bgColor: 'bg-status-success' }, { label: t('execution.testResult.notPassed'), dataIndex: 'failNum', bgColor: 'bg-status-error' }, { label: t('execution.testResult.notEnabled'), dataIndex: 'disabledNum', bgColor: 'bg-gray-icon' }]
//   [],
];
const CaseTypeIconConfig = {
  SMOKE: 'icon-maoyanceshi',
  SECURITY: 'icon-anquanceshi',
  USER_DEFINED: 'icon-zidingyiceshi'
};

const statisticsData = ref({});
const caseResult = ref([]);
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
  <div class="font-semibold mt-5 mb-2">{{ t('execution.testResult.testCases') }}</div>
  <div class="space-y-2 text-3">
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
    <div class="font-semibold mt-5 mb-2">{{ t('execution.testResult.testedCases') }}</div>
    <div class="text-3 space-y-1">
      <div
        v-for="item in caseResult"
        :key="item.id"
        class="border border-gray-light rounded bg-gray-light">
        <!-- <Tooltip placement="rightTop"> -->
        <!-- <template #title>
            <div class="max-w-100 overflow-y-auto">
              <Grid
                :dataSource="item.samplingSummary || item"
                :columns="columns" />
            </div>
          </template> -->
        <div class="px-1 flex h-6 items-center">
          <Icon :icon="CaseTypeIconConfig[item.caseType?.value] || 'icon-jiekouyongli2'" class="mr-1 text-4" />
          <span class="min-w-10 truncate flex-1" :title="item.apisName || item.caseName || item.summary">{{ item.apisName || item.caseName || item.summary }}</span>
          <span
            v-if="!item.enabled"
            class="px-2 rounded">{{ t('execution.testResult.notEnabled') }}</span>
          <span
            v-else-if="item.passed"
            class="px-2 rounded text-status-success">{{ t('execution.testResult.passed') }}</span>
          <span v-else class="px-2 rounded text-status-error ">
            {{ t('execution.testResult.notPassedWithReason') }}{{ item.failureMessage }}
          </span>
        </div>
        <!-- </Tooltip> -->
      </div>
    </div>
  </div>
</template>
