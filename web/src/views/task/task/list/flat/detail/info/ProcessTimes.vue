<script setup lang="ts">
import { computed } from 'vue';
import { Toggle } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';

import { TaskInfo } from '@/views/task/types';

type Props = {
  projectId: string;
  userInfo: { id: string; fullName: string; };
  appInfo: { id: string; };
  dataSource: TaskInfo;
  taskId: string;
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  dataSource: undefined,
  taskId: undefined
});

const { t } = useI18n();

// Derived values
const totalProcessCount = computed(() => +(props.dataSource?.totalNum || 0));
const failedProcessCount = computed(() => +(props.dataSource?.failNum || 0));
const onePassStatusText = computed(() => {
  if (totalProcessCount.value <= 0) return '--';
  return failedProcessCount.value === 0 ? t('task.detailInfo.basic.columns.yes') : t('task.detailInfo.basic.columns.no');
});
</script>

<template>
  <Toggle>
    <template #title>
      <div class="text-3.5">{{ t('task.detailInfo.basic.processCount') }}</div>
    </template>

    <template #default>
      <div class="text-3 leading-5 space-y-2.5 pt-2 pl-5.5">
        <div class="relative flex items-start">
          <div class="w-24.5 flex items-start whitespace-pre-wrap flex-shrink-0">
            <span>{{ t('task.detailInfo.basic.columns.totalProcessCount') }}</span>
          </div>
          <div class="font-medium whitespace-pre-wrap break-words break-all">{{ totalProcessCount }}</div>
        </div>

        <div class="relative flex items-start">
          <div class="w-24.5 flex items-start whitespace-pre-wrap flex-shrink-0">
            <span>{{ t('task.detailInfo.basic.columns.failedProcessCount') }}</span>
          </div>
          <div class="font-medium whitespace-pre-wrap break-words break-all">{{ failedProcessCount }}</div>
        </div>

        <div class="relative flex items-start">
          <div class="w-24.5 flex items-start whitespace-pre-wrap flex-shrink-0">
            <span>{{ t('task.detailInfo.basic.columns.onePass') }}</span>
          </div>
          <div class="font-medium whitespace-pre-wrap break-words break-all">{{ onePassStatusText }}</div>
        </div>
      </div>
    </template>
  </Toggle>
</template>
