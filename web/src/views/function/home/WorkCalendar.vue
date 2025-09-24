<script setup lang="ts">
import { onMounted, ref, watch } from 'vue';
import { Badge, Calendar } from 'ant-design-vue';
import { AsyncComponent, Icon, Popover, Select } from '@xcan-angus/vue-ui';
import { TESTER } from '@xcan-angus/infra';
import { Dayjs } from 'dayjs';
import { analysis } from '@/api/tester';
import { useI18n } from 'vue-i18n';
import { DATE_TIME_FORMAT } from '@/utils/constant';

import { CaseInfo, PlanProps } from '@/views/function/types';

import TestResult from '@/components/TestResult/index.vue';

// Type Definitions
type CalendarDataMap = { [key: string]: CaseInfo[] };

// Props and Composables
const props = withDefaults(defineProps<PlanProps>(), {
  projectId: undefined,
  userInfo: undefined,
  notify: undefined,
  planId: undefined
});

const { t } = useI18n();

// Reactive State
const isDataLoaded = ref(false);
const calendarDataMap = ref<CalendarDataMap>({});
const selectedUserId = ref();

/**
 * <p>Loads work calendar data from the API.</p>
 * <p>Fetches work summary data grouped by day for the selected user and project.</p>
 */
const loadCalendarData = async () => {
  const queryParams = {
    projectId: props.projectId,
    userId: selectedUserId.value,
    planId: props.planId
  };
  const [error, res] = await analysis.getFuncTesterWorkSummary(queryParams);
  isDataLoaded.value = true;
  if (error) {
    return;
  }
  calendarDataMap.value = res?.data?.groupByDay;
};

/**
 * <p>Gets non-overdue cases for a specific date.</p>
 * <p>Filters cases that are not overdue for the given calendar date.</p>
 */
const getNonOverdueCases = (currentDate: Dayjs) => {
  const dateString = currentDate.format(DATE_TIME_FORMAT).split(' ')[0];
  if (calendarDataMap.value[dateString]) {
    return calendarDataMap.value[dateString].filter(item => !item.overdue);
  }
  return [];
};

/**
 * <p>Gets overdue cases for a specific date.</p>
 * <p>Filters cases that are overdue for the given calendar date.</p>
 */
const getOverdueCases = (currentDate: Dayjs) => {
  const dateString = currentDate.format(DATE_TIME_FORMAT).split(' ')[0];
  if (calendarDataMap.value[dateString]) {
    return calendarDataMap.value[dateString].filter(item => item.overdue);
  }
  return [];
};

/**
 * <p>Calculates total number of active cases.</p>
 * <p>Counts cases that are not canceled for workload calculation.</p>
 */
const calculateTotalCases = (caseList: any[]) => {
  return (caseList || []).filter(item => item.testResult?.value !== 'CANCELED').length;
};

/**
 * <p>Calculates number of completed cases.</p>
 * <p>Counts cases that have passed testing.</p>
 */
const calculateCompletedCases = (caseList: any[]) => {
  return (caseList || []).filter(item => item.testResult?.value === 'PASSED').length;
};

/**
 * <p>Calculates number of remaining cases.</p>
 * <p>Returns the difference between total and completed cases.</p>
 */
const calculateRemainingCases = (caseList: any[]) => {
  const totalCases = calculateTotalCases(caseList);
  const completedCases = calculateCompletedCases(caseList);
  return totalCases - completedCases;
};

// Lifecycle
onMounted(() => {
  watch([() => props.projectId, () => props.planId], ([newValue]) => {
    if (!newValue) {
      return;
    }
    if (!selectedUserId.value) {
      selectedUserId.value = props.userInfo?.id;
    }

    loadCalendarData();
  }, { immediate: true });

  watch(() => props.notify, (newValue) => {
    if (newValue === undefined || newValue === null || newValue === '') {
      return;
    }

    loadCalendarData();
  }, { immediate: true });

  watch(() => selectedUserId.value, () => {
    loadCalendarData();
  });
});
</script>

<template>
  <div class="relative">
    <div class="absolute top-1.5 left-0 text-3.5 font-semibold">
      <Select
        v-model:value="selectedUserId"
        class="w-30"
        :action="`${TESTER}/project/${props.projectId}/member/user`"
        :fieldNames="{value: 'id', label: 'fullName'}" />
    </div>
    <AsyncComponent :visible="isDataLoaded">
      <Calendar size="small">
        <template #dateCellRender="{ current }: { current: Dayjs }">
          <template v-if="getNonOverdueCases(current).length">
            <div class="flex items-center text-3">
              <Icon icon="icon-xiaoqi" class="text-status-success text-3.5 mr-1" />
              <Popover placement="right" overlayClassName="calendar-popover-container">
                <Badge
                  class="text-3"
                  :overflowCount="99"
                  :count="getNonOverdueCases(current).length" />
                <template #content>
                  <div class="flex items-center flex-nowrap space-x-5 mb-1.5">
                    <div class="flex-shrink-0 space-x-1">
                      <span>{{ t('common.workload') }}</span>
                      <span>{{ calculateTotalCases(getNonOverdueCases(current)) }}</span>
                    </div>
                    <div class="flex-shrink-0 space-x-1">
                      <span>{{ t('chart.burndown.completed') }}</span>
                      <span>{{ calculateCompletedCases(getNonOverdueCases(current)) }}</span>
                    </div>
                    <div class="flex-shrink-0 space-x-1">
                      <span>{{ t('chart.burndown.remaining') }}</span>
                      <span>{{ calculateRemainingCases(getNonOverdueCases(current)) }}</span>
                    </div>
                  </div>
                  <div
                    v-for="item in getNonOverdueCases(current)"
                    :key="item.id"
                    class="flex items-center mb-1 last:mb-0">
                    <Icon icon="icon-gongnengyongli" class="text-4 flex-shrink-0 mr-1.5" />
                    <div class="flex-1 min-w-25 max-w-80 truncate mr-2.5">{{ item.name }}</div>
                    <TestResult :value="item.testResult" />
                  </div>
                </template>
              </Popover>
            </div>
          </template>

          <template v-if="getOverdueCases(current).length">
            <div class="flex items-center text-3">
              <Icon icon="icon-xiaoqi" class="text-status-error text-3.5 mr-1" />
              <Popover placement="right" overlayClassName="calendar-popover-container">
                <Badge
                  class="text-3"
                  :overflowCount="99"
                  :count="getOverdueCases(current).length" />
                <template #content>
                  <div class="flex items-center flex-nowrap space-x-5 mb-1.5">
                    <div class="flex-shrink-0 space-x-1">
                      <span>{{ t('common.workload') }}</span>
                      <span>{{ calculateTotalCases(getOverdueCases(current)) }}</span>
                    </div>
                    <div class="flex-shrink-0 space-x-1">
                      <span>{{ t('chart.burndown.completed') }}</span>
                      <span>{{ calculateCompletedCases(getOverdueCases(current)) }}</span>
                    </div>
                    <div class="flex-shrink-0 space-x-1">
                      <span>{{ t('chart.burndown.remaining') }}</span>
                      <span>{{ calculateRemainingCases(getOverdueCases(current)) }}</span>
                    </div>
                  </div>
                  <div
                    v-for="item in getOverdueCases(current)"
                    :key="item.id"
                    class="flex items-center mb-1 last:mb-0">
                    <Icon icon="icon-gongnengyongli" class="text-4 flex-shrink-0 mr-1.5" />
                    <div class="flex-1 min-w-25 max-w-80 truncate mr-2.5">{{ item.name }}</div>
                    <div class="flex items-center">
                      <TestResult :value="item.testResult" />
                      <span
                        class="flex-shrink-0 border border-status-error rounded px-0.5 ml-2"
                        style="color: rgba(245, 34, 45, 100%);line-height: 16px;">
                        <span class="inline-block transform-gpu scale-90">{{ t('status.overdue') }}</span>
                      </span>
                    </div>
                  </div>
                </template>
              </Popover>
            </div>
          </template>
        </template>
      </Calendar>
    </AsyncComponent>
  </div>
</template>
<style>
.calendar-popover-container .ant-popover-content .ant-popover-inner {
  max-height: 264px;
  padding: 12px 0;
  overflow: hidden;
  line-height: 20px;
}

.calendar-popover-container .ant-popover-content .ant-popover-inner-content {
  max-height: 240px;
  padding: 0 16px;
  overflow: auto;
}
</style>

<style scoped>
.ant-badge :deep(.ant-badge-count) {
  height: 16px;
  padding: 0 6px;
  border: none;
  background-color: rgba(239, 240, 243, 100%);
  color: var(--content-text-sub-content);
  line-height: 16px;
}

:deep(.ant-picker-calendar-full) .ant-picker-calendar-header {
padding-top:0;
}

:deep(.ant-picker-calendar-full) .ant-picker-calendar-mode-switch {
  display: none;
}

:deep(.ant-picker-calendar-full) .ant-radio-button-wrapper {
  height: 28px;
  font-size: 12px;
  line-height: 26px;
}

:deep(.ant-picker-calendar-full) .ant-select-single .ant-select-selector {
  height: 28px;
}

:deep(.ant-picker-calendar-full) .ant-select-single .ant-select-selector .ant-select-selection-item {
  line-height: 26px;
}

:deep(.ant-picker-calendar-full) .ant-picker-panel {
  border: 1px solid var(--border-text-box);
  border-radius: 4px;
}

:deep(.ant-picker-calendar-full) .ant-picker-panel .ant-picker-body {
  padding: 0;
}

:deep(.ant-picker-calendar-full) .ant-picker-panel .ant-picker-body .ant-picker-content thead {
  height: 40px;
  background-color: rgba(247, 248, 251, 100%);
}

:deep(.ant-picker-calendar-full) .ant-picker-panel .ant-picker-body .ant-picker-content thead th {
  padding: 0 14px;
  border-right: 1px solid var(--border-text-box);
  line-height: 20px;
}

:deep(.ant-picker-calendar-full) .ant-picker-panel .ant-picker-body .ant-picker-content thead th:last-child {
  border-right: none;
}

:deep(.ant-picker-calendar-full) .ant-picker-cell {
  border-right: 1px solid var(--border-text-box);
}

:deep(.ant-picker-calendar-full) .ant-picker-cell:last-child {
  border-right: none;
}

:deep(.ant-picker-calendar-full) .ant-picker-panel .ant-picker-calendar-date {
  margin: 0;
  padding: 0;
  border-top-width: 1px;
}

:deep(.ant-picker-calendar-full) .ant-picker-panel .ant-picker-calendar-date-value {
  position: absolute;
  top: 50%;
  right: 14px;
  transform: translateY(-50%);
  line-height: 20px;
}

:deep(.ant-picker-calendar-full) .ant-picker-panel .ant-picker-calendar-date-content {
  display: flex;
  flex-direction: column;
  justify-content: center;
  height: 44px;
  padding-left: 10px;
}

:deep(.ant-picker-calendar-full) .ant-picker-panel .ant-picker-calendar-date-content>div+div {
  margin-top: 2px;
}
</style>
