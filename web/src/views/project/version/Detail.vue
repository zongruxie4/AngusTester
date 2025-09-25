<script setup lang="ts">
import { defineAsyncComponent } from 'vue';
import { Icon, Spin } from '@xcan-angus/vue-ui';
import { Tag } from 'ant-design-vue';
import { useI18n } from 'vue-i18n';
import { useVersionDetail } from './composables/useVersionDetail';
import type { VersionDetailProps, StatusColorConfig } from './types';
import Chart from './Chart.vue';

// Component props with default values
const props = withDefaults(defineProps<VersionDetailProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  data: undefined
});

const { t } = useI18n();

// Async component for task table
const TaskTable = defineAsyncComponent(() => import('@/views/project/version/MyTask.vue'));

// Use version detail composable for data management
const { dataSource, loading, refreshNotify, chartValue } = useVersionDetail(props);

/**
 * Status color configuration for version status tags
 * Maps status values to Ant Design tag colors
 */
const statusColorConfig: StatusColorConfig = {
  ARCHIVED: 'default',
  NOT_RELEASED: 'processing',
  RELEASED: 'success'
};
</script>

<template>
  <Spin :spinning="loading" class="h-full text-3 leading-5 px-5 py-5 overflow-auto">
    <div class="flex itesm-center space-x-3">
      <div class="text-theme-title text-5">
        {{ t('common.version') }} {{ dataSource.name }}
      </div>
      <Tag v-if="dataSource.status?.value" :color="statusColorConfig[dataSource.status?.value]">{{ dataSource.status?.message }}</Tag>
    </div>
    <div class="flex itesm-center space-x-5 mt-2">
      <div class="inline-flex items-center space-x-1">
        <template v-if="dataSource.startDate">
          {{ t('version.detail.startDate') }} {{ dataSource.startDate }}
        </template>
        <template v-else>
          <Icon icon="icon-riqi" /> <span>{{ t('version.detail.noStartDate') }}</span>
        </template>
      </div>
      <div class="inline-flex items-center space-x-1">
        <template v-if="dataSource.releaseDate">
          {{ t('common.releaseDate') }} {{ dataSource.releaseDate }}
        </template>
        <template v-else>
          <Icon icon="icon-riqi" /> <span>{{ t('version.detail.noReleaseDate') }}</span>
        </template>
      </div>
    </div>

    <Chart v-bind="chartValue" class="w-200" />
    <TaskTable
      :projectId="props.projectId"
      :userInfo="props.userInfo"
      :notify="refreshNotify"
      :versionInfo="dataSource" />
  </Spin>
</template>

<style scoped>
.PENDING {
  background-color: rgba(45, 142, 255, 100%);
}

.IN_PROGRESS {
  background-color: rgba(103, 215, 255, 100%);
}

.COMPLETED {
  background-color: rgba(82, 196, 26, 100%);
}

.BLOCKED {
  background-color: rgba(255, 165, 43, 100%);
}

.version-container {
  border: 1px solid var(--border-text-box);
  border-radius: 4px;
}
</style>
