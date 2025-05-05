<script setup lang="ts">
import { onMounted, ref, watch } from 'vue';
import { Badge, Calendar } from 'ant-design-vue';
import { AsyncComponent, Icon, IconTask, Popover, Select, TaskStatus } from '@xcan-angus/vue-ui';
import { TESTER } from '@xcan-angus/tools';
import { Dayjs } from 'dayjs';
import { analysis } from '@/api/tester';

import { DataItem } from './PropsTyps';

type Props = {
  projectId: string;
  userInfo: { id: string; };
  notify: string;
  sprintId?: string;
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  userInfo: undefined,
  notify: undefined,
  sprintId: undefined
});

const loaded = ref(false);
const dataMap = ref<{ [key: string]: DataItem[] }>({});
const userId = ref();

const loadData = async () => {
  const params = {
    projectId: props.projectId,
    userId: userId.value,
    sprintId: props.sprintId || undefined
  };
  const [error, res] = await analysis.getTaskWorkSummary(params);
  loaded.value = true;
  if (error) {
    return;
  }

  dataMap.value = res?.data?.groupByDay;
};

const getList = (current: Dayjs) => {
  const dateString = current.format('YYYY-MM-DD HH:mm:ss').split(' ')[0];
  if (dataMap.value[dateString]) {
    return dataMap.value[dateString].filter(item => !item.overdueFlag);
  }

  return [];
};

const getOverdueList = (current: Dayjs) => {
  const dateString = current.format('YYYY-MM-DD HH:mm:ss').split(' ')[0];
  if (dataMap.value[dateString]) {
    return dataMap.value[dateString].filter(item => item.overdueFlag);
  }

  return [];
};

const getTotalNum = (list:any[]) => {
  return (list || []).filter(item => item.status?.value !== 'CANCELED').length;
};

const geCompletedNum = (list:any[]) => {
  return (list || []).filter(item => item.status?.value === 'COMPLETED').length;
};

const geRemainNum = (list:any[]) => {
  const totalNum = (list || []).filter(item => item.status?.value !== 'CANCELED').length;
  const completedNum = (list || []).filter(item => item.status?.value === 'COMPLETED').length;
  return totalNum - completedNum;
};

onMounted(() => {
  watch([() => props.projectId, () => props.sprintId], ([newValue]) => {
    if (!newValue) {
      return;
    }
    if (!userId.value) {
      userId.value = props.userInfo?.id;
    }

    loadData();
  }, { immediate: true });

  watch(() => props.notify, (newValue) => {
    if (newValue === undefined || newValue === null || newValue === '') {
      return;
    }

    loadData();
  }, { immediate: true });

  watch(() => userId.value, () => {
    loadData();
  });
});
</script>

<template>
  <div class="relative">
    <div class="absolute top-1.5 left-0 text-3.5 font-semibold">
      <!-- 我的工作日历 -->
      <Select
        v-model:value="userId"
        class="w-30"
        :action="`${TESTER}/project/${props.projectId}/member/user`"
        :fieldNames="{value: 'id', label: 'fullName'}" />
    </div>
    <AsyncComponent :visible="loaded">
      <Calendar size="small">
        <template #dateCellRender="{ current }: { current: Dayjs }">
          <template v-if="getList(current).length">
            <div class="flex items-center text-3">
              <Icon icon="icon-xiaoqi" class="text-status-success text-3.5 mr-1" />
              <Popover
                placement="right"
                overlayClassName="calendar-popover-container">
                <Badge
                  class="text-3"
                  :overflowCount="99"
                  :count="getList(current).length" />
                <template #content>
                  <div class="flex items-center flex-nowrap space-x-5 mb-1.5">
                    <div class="flex-shrink-0 space-x-1">
                      <span>工作量</span>
                      <span>{{ getTotalNum(getList(current)) }}</span>
                    </div>
                    <div class="flex-shrink-0 space-x-1">
                      <span>已完成</span>
                      <span>{{ geCompletedNum(getList(current)) }}</span>
                    </div>
                    <div class="flex-shrink-0 space-x-1">
                      <span>剩余</span>
                      <span>{{ geRemainNum(getList(current)) }}</span>
                    </div>
                  </div>
                  <div
                    v-for="item in getList(current)"
                    :key="item.id"
                    class="flex items-center mb-1 last:mb-0">
                    <IconTask :value="item.taskType?.value" class="text-4 flex-shrink-0 mr-1.5" />
                    <div class="flex-1 min-w-25 max-w-80 truncate mr-2.5">{{ item.name }}</div>
                    <TaskStatus :value="item.status" />
                  </div>
                </template>
              </Popover>
            </div>
          </template>

          <template v-if="getOverdueList(current).length">
            <div class="flex items-center text-3">
              <Icon icon="icon-xiaoqi" class="text-status-error text-3.5 mr-1" />
              <Popover placement="right" overlayClassName="calendar-popover-container">
                <Badge
                  class="text-3"
                  :overflowCount="99"
                  :count="getOverdueList(current).length" />
                <template #content>
                  <div class="flex items-center flex-nowrap space-x-5 mb-1.5">
                    <div class="flex-shrink-0 space-x-1">
                      <span>工作量</span>
                      <span>{{ getTotalNum(getOverdueList(current)) }}</span>
                    </div>
                    <div class="flex-shrink-0 space-x-1">
                      <span>已完成</span>
                      <span>{{ geCompletedNum(getOverdueList(current)) }}</span>
                    </div>
                    <div class="flex-shrink-0 space-x-1">
                      <span>剩余</span>
                      <span>{{ geRemainNum(getOverdueList(current)) }}</span>
                    </div>
                  </div>

                  <div
                    v-for="item in getOverdueList(current)"
                    :key="item.id"
                    class="flex items-center mb-1 last:mb-0">
                    <IconTask :value="item.taskType?.value" class="text-4 flex-shrink-0 mr-1.5" />
                    <div class="flex-1 min-w-25 max-w-80 truncate mr-2.5">{{ item.name }}</div>
                    <div class="flex items-center">
                      <TaskStatus :value="item.status" />
                      <span
                        class="flex-shrink-0 border border-status-error rounded px-0.5 ml-2"
                        style="color: rgba(245, 34, 45, 100%);line-height: 16px;">
                        <span class="inline-block transform-gpu scale-90">已逾期</span>
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
