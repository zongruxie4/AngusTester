<script setup lang="ts">
import { useI18n } from 'vue-i18n';
import { Icon } from '@xcan-angus/vue-ui';

import { CaseCount } from '@/views/test/case/types';
import { DataSourceProps } from '@/types/types';

// Composables
const { t } = useI18n();

// Props Definition
const props = withDefaults(defineProps<DataSourceProps<CaseCount>>(), {
  dataSource: undefined
});

/**
 * Formats percentage values to show only two decimal places
 * @param value - The percentage value to format
 * @returns Formatted percentage string
 */
const formatPercentage = (value: string | number | undefined): string => {
  if (!value) return '0';
  const strValue = String(value);
  return strValue.replace(/(\d+\.\d{2})\d+/, '$1');
};
</script>

<template>
  <!-- Statistics cards container -->
  <div class="statistics-container">
    <!-- Progress percentage card -->
    <div class="statistic-card">
      <Icon class="icon" icon="icon-zonglan" />
      <div class="content">
        <div class="value">
          <span>{{ formatPercentage(props.dataSource?.progress) }}</span>
          <span class="unit">%</span>
        </div>
        <div class="label">
          {{ t('common.progress') }}
        </div>
      </div>
    </div>

    <!-- Total cases count card -->
    <div class="statistic-card">
      <Icon class="icon" icon="icon-zongyongli" />
      <div class="content">
        <div class="value">{{ props.dataSource?.totalCaseNum }}</div>
        <div class="label">
          {{ t('testCase.statisticsPanel.totalCaseNum') }}
        </div>
      </div>
    </div>

    <!-- Pending test cases count card -->
    <div class="statistic-card">
      <Icon class="icon" icon="icon-daiceshi" />
      <div class="content">
        <div class="value">{{ props.dataSource?.pendingTestNum }}</div>
        <div class="label">
          {{ t('testCase.statisticsPanel.pendingTestNum') }}
        </div>
      </div>
    </div>

    <!-- Passed test cases count card -->
    <div class="statistic-card">
      <Icon class="icon" icon="icon-ceshitongguo" />
      <div class="content">
        <div class="value">{{ props.dataSource?.passedTestNum }}</div>
        <div class="label">
          {{ t('testCase.statisticsPanel.passedTestNum') }}
        </div>
      </div>
    </div>

    <!-- Not passed test cases count card -->
    <div class="statistic-card">
      <Icon class="icon" icon="icon-ceshiweitongguo" />
      <div class="content">
        <div class="value">{{ props.dataSource?.notPassedTestNum }}</div>
        <div class="label">
          {{ t('testCase.statisticsPanel.notPassedTestNum') }}
        </div>
      </div>
    </div>

    <!-- One-time passed test cases count card -->
    <div class="statistic-card">
      <Icon class="icon" icon="icon-a-yicixingceshitongguoshu" />
      <div class="content">
        <div class="value">{{ props.dataSource?.oneTimePassedTestNum }}</div>
        <div class="label">
          {{ t('testCase.statisticsPanel.oneTimePassedTestNum') }}
        </div>
      </div>
    </div>

    <!-- One-time passed test rate card -->
    <div class="statistic-card">
      <Icon class="icon" icon="icon-a-yicixingceshitongguoshuai" />
      <div class="content">
        <div class="value">
          <span>{{ formatPercentage(props.dataSource?.oneTimePassedTestRate) }}</span>
          <span class="unit">%</span>
        </div>
        <div class="label">
          {{ t('testCase.statisticsPanel.oneTimePassedTestRate') }}
        </div>
      </div>
    </div>

    <!-- Overdue cases count card -->
    <div class="statistic-card">
      <Icon class="icon" icon="icon-yiyuqi1" />
      <div class="content">
        <div class="value">{{ props.dataSource?.overdueNum }}</div>
        <div class="label">
          {{ t('common.counts.overdueCount') }}
        </div>
      </div>
    </div>

    <!-- Blocked test cases count card -->
    <div class="statistic-card">
      <Icon class="icon" icon="icon-zusaizhong" />
      <div class="content">
        <div class="value">{{ props.dataSource?.blockedTestNum }}</div>
        <div class="label">
          {{ t('testCase.statisticsPanel.blockedTestNum') }}
        </div>
      </div>
    </div>

    <!-- Canceled test cases count card -->
    <div class="statistic-card">
      <Icon class="icon" icon="icon-yiquxiao" />
      <div class="content">
        <div class="value">{{ props.dataSource?.canceledTestNum }}</div>
        <div class="label">
          {{ t('testCase.statisticsPanel.canceledTestNum') }}
        </div>
      </div>
    </div>

    <!-- Evaluated workload count card -->
    <div class="statistic-card">
      <Icon class="icon" icon="icon-pinggugongzuoliang" />
      <div class="content">
        <div class="value">{{ props.dataSource?.evalWorkload }}</div>
        <div class="label">
          {{ t('common.evalWorkload') }}
        </div>
      </div>
    </div>

    <!-- Completed workload count card -->
    <div class="statistic-card">
      <Icon class="icon" icon="icon-wanchenggongzuoliang" />
      <div class="content">
        <div class="value">{{ props.dataSource?.completedWorkload }}</div>
        <div class="label">
          {{ t('common.completedWorkload') }}
        </div>
      </div>
    </div>

    <!-- One-time pass review count card -->
    <div class="statistic-card">
      <Icon class="icon" icon="icon-a-yicixingpingshentongguoshu" />
      <div class="content">
        <div class="value">{{ props.dataSource?.oneTimePassReviewNum }}</div>
        <div class="label">
          {{ t('testCase.statisticsPanel.oneTimePassReviewNum') }}
        </div>
      </div>
    </div>

    <!-- One-time pass review rate card -->
    <div class="statistic-card">
      <Icon class="icon" icon="icon-a-yicixingpingshentongguoshuai" />
      <div class="content">
        <div class="value">
          <span>{{ formatPercentage(props.dataSource?.oneTimePassReviewRate) }}</span>
          <span class="unit">%</span>
        </div>
        <div class="label">
          {{ t('testCase.statisticsPanel.oneTimePassReviewRate') }}
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.statistics-container {
  display: flex;
  flex-wrap: wrap;
  line-height: 1.25;
  font-size: 0.75rem;
  gap: 8px;
}

.statistic-card {
  display: flex;
  flex-wrap: nowrap;
  align-items: center;
  padding: 10px;
  border-radius: 8px;
  background-color: rgb(252, 250, 255);
  box-sizing: border-box;
}

.statistic-card .icon {
  flex-shrink: 0;
  font-size: 2.25rem;
}

.statistic-card .content {
  margin-left: 12px;
}

.statistic-card .value {
  font-size: 0.9375rem;
  font-weight: 600;
}

.statistic-card .unit {
  font-size: 0.875rem;
}

.statistic-card .label {
  flex-shrink: 0;
  white-space: nowrap;
  color: var(--theme-sub-content);
}

@media screen and (max-width: 1600px) {
  .statistic-card {
    flex: 0 0 calc((100% - 32px) / 5);
    max-width: calc((100% - 32px) / 5);
    min-width: 0;
  }
}

@media screen and (min-width: 1601px) {
  .statistic-card {
    flex: 0 0 calc((100% - 48px) / 7);
    max-width: calc((100% - 48px) / 7);
    min-width: 0;
  }
}
</style>
