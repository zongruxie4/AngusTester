<script setup lang="ts">
import { computed, defineAsyncComponent, ref } from 'vue';
import { TabPane, Tabs } from 'ant-design-vue';
import { useI18n } from 'vue-i18n';
import type { MyTaskProps, TaskInfo } from './types';

// Component props with default values
const props = withDefaults(defineProps<MyTaskProps>(), {
  projectId: undefined,
  userInfo: undefined,
  notify: undefined,
  versionId: undefined
});

const { t } = useI18n();

// Async component for task table
const Table = defineAsyncComponent(() => import('./MyTaskTable.vue'));

// Notification state for task deletion
const deletedNotify = ref<string>();

/**
 * Compute all tasks from version info
 * Flattens taskByStatus object into single array
 */
const allTasks = computed((): TaskInfo[] => {
  const dataSource = props.versionInfo?.taskByStatus || {};
  return Object.keys(dataSource).reduce((pre: TaskInfo[], current: string) => {
    pre.push(...(dataSource[current] || []));
    return pre;
  }, []);
});

/**
 * Compute total count of all tasks
 * Returns total number of tasks across all statuses
 */
const currentTotal = computed((): number => {
  return allTasks.value.length || 0;
});

/**
 * Compute completed tasks count
 * Returns number of tasks with COMPLETED status
 */
const completedTotal = computed((): number => {
  return props.versionInfo?.taskByStatus?.COMPLETED?.length || 0;
});

/**
 * Compute processing tasks count
 * Returns number of tasks with PROCESSING status
 */
const processingTotal = computed((): number => {
  return props.versionInfo?.taskByStatus?.PROCESSING?.length || 0;
});

/**
 * Compute confirming tasks count
 * Returns number of tasks with CONFIRMING status
 */
const confirmingTotal = computed((): number => {
  return props.versionInfo?.taskByStatus?.CONFIRMING?.length || 0;
});

/**
 * Compute pending tasks count
 * Returns number of tasks with PENDING status
 */
const pendingTotal = computed((): number => {
  return props.versionInfo?.taskByStatus?.PENDING?.length || 0;
});

/**
 * Compute canceled tasks count
 * Returns number of tasks with CANCELED status
 */
const cancelTotal = computed((): number => {
  return props.versionInfo?.taskByStatus?.CANCELED?.length || 0;
});
</script>

<template>
  <div>
    <Tabs size="small">
      <TabPane key="current">
        <template #tab>
          <div class="flex items-center space-x-1">
            <span class="text-8 font-semibold text-text-title">{{ currentTotal }}</span>
            <div class="leading-3.5">
              <span>{{ t('version.taskTable.currentVersion') }}</span>
              <div>{{ t('version.taskTable.units.tasks') }}</div>
            </div>
          </div>
        </template>
        <Table
          v-model:deletedNotify="deletedNotify"
          :notify="props.notify"
          :projectId="props.projectId"
          :taskList="allTasks" />
      </TabPane>

      <TabPane key="completed">
        <template #tab>
          <div class="flex items-center space-x-1">
            <span class="text-8 font-semibold text-status-success">{{ completedTotal }}</span>
            <div class="leading-3.5">
              <span>{{ t('common.issue') }}</span>
              <div>{{ t('status.completed') }}</div>
            </div>
          </div>
        </template>
        <Table
          v-model:deletedNotify="deletedNotify"
          :notify="props.notify"
          :projectId="props.projectId"
          :taskList="props.versionInfo?.taskByStatus?.COMPLETED" />
      </TabPane>

      <TabPane key="processing">
        <template #tab>
          <div class="flex items-center space-x-1">
            <span class="text-8 font-semibold text-status-warn">{{ processingTotal }}</span>
            <div class="leading-3.5">
              <span>{{ t('common.issue') }}</span>
              <div>{{ t('status.inProgress') }}</div>
            </div>
          </div>
        </template>
        <Table
          v-model:deletedNotify="deletedNotify"
          :notify="props.notify"
          :projectId="props.projectId"
          :taskList="props.versionInfo?.taskByStatus?.PROCESSING" />
      </TabPane>

      <TabPane key="confirming">
        <template #tab>
          <div class="flex items-center  space-x-1">
            <span class="text-8 font-semibold text-status-pending">{{ confirmingTotal }}</span>
            <div class="leading-3.5">
              <span>{{ t('common.issue') }}</span>
              <div>{{ t('status.pendingConfirmation') }}</div>
            </div>
          </div>
        </template>
        <Table
          v-model:deletedNotify="deletedNotify"
          :notify="props.notify"
          :projectId="props.projectId"
          :taskList="props.versionInfo?.taskByStatus?.CONFIRMING" />
      </TabPane>

      <TabPane key="pending">
        <template #tab>
          <div class="flex items-center  space-x-1">
            <span class="text-8 font-semibold text-blue-light">{{ pendingTotal }}</span>
            <div class="leading-3.5">
              <span>{{ t('common.issue') }}</span>
              <div>{{ t('status.pending') }}</div>
            </div>
          </div>
        </template>
        <Table
          v-model:deletedNotify="deletedNotify"
          :notify="props.notify"
          :projectId="props.projectId"
          :taskList="props.versionInfo?.taskByStatus?.PENDING" />
      </TabPane>

      <TabPane key="canceled">
        <template #tab>
          <div class="flex items-center  space-x-1">
            <span class="text-8 font-semibold text-gray-3">{{ cancelTotal }}</span>
            <div class="leading-3.5">
              <span>{{ t('common.issue') }}</span>
              <div>{{ t('status.canceled') }}</div>
            </div>
          </div>
        </template>
        <Table
          v-model:deletedNotify="deletedNotify"
          :notify="props.notify"
          :projectId="props.projectId"
          :taskList="props.versionInfo?.taskByStatus?.CANCELED" />
      </TabPane>
    </Tabs>
  </div>
</template>

<style scoped>
.ant-tabs {
  line-height: 20px;
}

:deep(.ant-tabs-content-holder) {
  min-height: 166px;
}

.ant-tabs-top>:deep(.ant-tabs-nav),
.ant-tabs-bottom>:deep(.ant-tabs-nav),
.ant-tabs-top>:deep(div)>.ant-tabs-nav,
.ant-tabs-bottom>:deep(div)>.ant-tabs-nav {
  margin-bottom: 14px;
}
</style>
