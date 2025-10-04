<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useI18n } from 'vue-i18n';

import Charts from './Charts.vue';
import { ReviewStatus, enumUtils } from '@xcan-angus/infra';
import { CaseCount } from '@/views/test/case/types';

const { t } = useI18n();

interface Props {
  dataSource:CaseCount;
}

const props = withDefaults(defineProps<Props>(), {
  dataSource: () => ({
    pendingReviewNum: 0,
    failedReviewNum: 0,
    passedReviewNum: 0,
    totalReviewCaseNum: 0,
    totalTestNum: 0,
    passedTestNum: 0,
    notPassedTestNum: 0,
    canceledTestNum: 0,
    blockedTestNum: 0
  })
});

const reviewStatusEnum = ref({});

const testData = computed(() => {
  const result = [
    { name: t('testCase.statisticsPanel.pendingReview'), value: +props.dataSource.pendingReviewNum, enumKey: 'PENDING' },
    { name: t('testCase.statisticsPanel.reviewPassed'), value: +props.dataSource.passedReviewNum, enumKey: 'PASSED' },
    { name: t('testCase.statisticsPanel.reviewNotPassed'), value: +props.dataSource.failedReviewNum, enumKey: 'FAILED' }
  ];
  result.forEach((i) => {
    i.name = reviewStatusEnum.value?.[i.enumKey]?.message || i.name;
  });
  return result;
});

onMounted(() => {
  const data = enumUtils.enumToMessages(ReviewStatus);
  reviewStatusEnum.value = {};
  data?.forEach(i => {
    reviewStatusEnum.value[i.value] = i;
  });
});
</script>
<template>
  <div class="charts-container flex-shrink-0">
    <!-- {{ props.dataSource }} -->
    <Charts
      key="1"
      style="width: 220px;"
      class="chart-item"
      :title="t('testCase.statisticsPanel.reviewStatus')"
      :color="['rgb(154, 154, 154)', 'rgb(92, 200, 0)', 'rgb(255, 95, 27)']"
      :total="+props.dataSource.totalReviewCaseNum"
      :data-source="testData" />
    <!-- <Charts
      key="2"
      style="width: 250px;"
      class="chart-item"
      title="测试状态"
      :color="['rgb(101, 220, 0)', 'rgb(255, 57, 11)', 'rgb(220, 220, 220)', 'rgb(148, 199, 255)']"
      :total="+props.dataSource.totalTestedCaseNum"
      :data-source="taskData" /> -->
  </div>
</template>
<style scoped>
.charts-container {
  height: 150px;
  white-space: nowrap;
}

.chart-item {
  display: inline-block;
}

.chart-item + .chart-item {
  margin-left: 0;
}

@media screen and (min-width: 1480px) {
  .chart-item + .chart-item {
    margin-left: 40px;
  }
}
</style>
