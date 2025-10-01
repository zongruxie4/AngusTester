<script setup lang="ts">
import { useI18n } from 'vue-i18n';
import { Icon } from '@xcan-angus/vue-ui';

import { TaskCount } from '../../types';

/**
 * Component props interface for Count component
 */
type Props = {
  dataSource: TaskCount;
}

// Composables
const { t } = useI18n();

// Props Definition
const props = withDefaults(defineProps<Props>(), {
  dataSource: undefined
});

/**
 * Formats percentage values to show only two decimal places
 * @param value - The percentage value to format
 * @returns Formatted percentage string
 */
const formatPercentage = (value: string | undefined): string => {
  if (!value) return '0';
  return value.replace(/(\d+\.\d{2})\d+/, '$1');
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

    <!-- Total tasks count card -->
    <div class="statistic-card">
      <Icon class="icon" icon="icon-meimiaoshiwushu" />
      <div class="content">
        <div class="value">{{ props.dataSource?.totalTaskNum }}</div>
        <div class="label">
          {{ t('task.list.statistics.totalTasks') }}
        </div>
      </div>
    </div>

    <!-- Pending tasks count card -->
    <div class="statistic-card">
      <Icon class="icon" icon="icon-daiceshi" />
      <div class="content">
        <div class="value">{{ props.dataSource?.pendingNum }}</div>
        <div class="label">
          {{ t('task.list.statistics.pendingTasks') }}
        </div>
      </div>
    </div>

    <!-- In progress tasks count card -->
    <div class="statistic-card">
      <Icon class="icon" icon="icon-renwuceshizhong" />
      <div class="content">
        <div class="value">{{ props.dataSource?.inProgressNum }}</div>
        <div class="label">
          {{ t('task.list.statistics.inProgressTasks') }}
        </div>
      </div>
    </div>

    <!-- Confirming tasks count card -->
    <div class="statistic-card">
      <Icon class="icon" icon="icon-daiqueren" />
      <div class="content">
        <div class="value">{{ props.dataSource?.confirmingNum }}</div>
        <div class="label">
          {{ t('task.list.statistics.confirmingTasks') }}
        </div>
      </div>
    </div>

    <!-- Completed tasks count card -->
    <div class="statistic-card">
      <Icon class="icon" icon="icon-ceshiwancheng" />
      <div class="content">
        <div class="value">{{ props.dataSource?.completedNum }}</div>
        <div class="label">
          {{ t('task.list.statistics.completedTasks') }}
        </div>
      </div>
    </div>

    <!-- Canceled tasks count card -->
    <div class="statistic-card">
      <Icon class="icon" icon="icon-yiquxiao" />
      <div class="content">
        <div class="value">{{ props.dataSource?.canceledNum }}</div>
        <div class="label">
          {{ t('task.list.statistics.canceledTasks') }}
        </div>
      </div>
    </div>

    <!-- Overdue tasks count card -->
    <div class="statistic-card">
      <Icon class="icon" icon="icon-yiyuqi1" />
      <div class="content">
        <div class="value">{{ props.dataSource?.overdueNum }}</div>
        <div class="label">
          {{ t('task.list.statistics.overdueTasks') }}
        </div>
      </div>
    </div>

    <!-- Evaluated workload count card -->
    <div class="statistic-card">
      <Icon class="icon" icon="icon-yiceshiyonglishu" />
      <div class="content">
        <div class="value">{{ props.dataSource?.evalWorkload }}</div>
        <div class="label">
          {{ t('task.list.statistics.evalWorkload') }}
        </div>
      </div>
    </div>

    <!-- Completed workload count card -->
    <div class="statistic-card">
      <Icon class="icon" icon="icon-chenggongshu" />
      <div class="content">
        <div class="value">{{ props.dataSource?.completedWorkload }}</div>
        <div class="label">
          {{ t('task.list.statistics.completedWorkload') }}
        </div>
      </div>
    </div>

    <!-- One-time passed count card -->
    <div class="statistic-card">
      <Icon class="icon" icon="icon-yicixingtongguoshu" />
      <div class="content">
        <div class="value">{{ props.dataSource?.oneTimePassedNum }}</div>
        <div class="label">
          {{ t('task.list.statistics.oneTimePassedNum') }}
        </div>
      </div>
    </div>

    <!-- One-time passed rate card -->
    <div class="statistic-card">
      <Icon class="icon" icon="icon-yicitongguoshuai" />
      <div class="content">
        <div class="value">
          <span>{{ formatPercentage(props.dataSource?.oneTimePassedRate) }}</span>
          <span class="unit">%</span>
        </div>
        <div class="label">
          {{ t('task.list.statistics.oneTimePassedRate') }}
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
}

.statistic-card {
  display: flex;
  flex-wrap: nowrap;
  align-items: center;
  margin-right: 8px;
  margin-bottom: 8px;
  padding: 10px;
  border-radius: 8px;
  background-color: rgb(252, 250, 255);
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
    width: calc((100% - 32px) / 4);
  }
}

@media screen and (min-width: 1601px) {
  .statistic-card {
    width: calc((100% - 48px) / 6);
  }
}
</style>
