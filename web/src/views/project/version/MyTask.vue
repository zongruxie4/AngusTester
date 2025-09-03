<script setup lang="ts">
import { computed, defineAsyncComponent, ref } from 'vue';
import { TabPane, Tabs } from 'ant-design-vue';
import { useI18n } from 'vue-i18n';

type Props = {
  projectId: string;
  userInfo: { id: string; };
  notify: string;
  versionId: string;
  versionInfo: any
}

const { t } = useI18n();

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  userInfo: undefined,
  notify: undefined,
  versionId: undefined
});

const Table = defineAsyncComponent(() => import('./MyTaskTable.vue'));

const deletedNotify = ref<string>();

const allTasks = computed(() => {
  const dataSource = props.versionInfo?.taskByStatus || {};
  return Object.keys(dataSource).reduce((pre, current) => {
    pre.push(...dataSource[current]);
    return pre;
  }, []);
});

const currentTotal = computed(() => {
  return allTasks.value.length || 0;
});

const completedTotal = computed(() => {
  return props.versionInfo?.taskByStatus?.COMPLETED?.length || 0;
});
const processingTotal = computed(() => {
  return props.versionInfo?.taskByStatus?.PROCESSING?.length || 0;
});
const confirmingTotal = computed(() => {
  return props.versionInfo?.taskByStatus?.CONFIRMING?.length || 0;
});
const pendingTotal = computed(() => {
  return props.versionInfo?.taskByStatus?.PENDING?.length || 0;
});

const cancelTotal = computed(() => {
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
              <span>{{ t('taskVersion.taskTable.tabs.current') }}</span>
              <div>{{ t('taskVersion.taskTable.units.tasks') }}</div>
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
              <span>{{ t('taskVersion.taskTable.units.task') }}</span>
              <div>{{ t('taskVersion.taskTable.tabs.completed') }}</div>
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
              <span>{{ t('taskVersion.taskTable.units.task') }}</span>
              <div>{{ t('taskVersion.taskTable.tabs.processing') }}</div>
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
              <span>{{ t('taskVersion.taskTable.units.task') }}</span>
              <div>{{ t('taskVersion.taskTable.tabs.confirming') }}</div>
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
              <span>{{ t('taskVersion.taskTable.units.task') }}</span>
              <div>{{ t('taskVersion.taskTable.tabs.pending') }}</div>
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
              <span>{{ t('taskVersion.taskTable.units.task') }}</span>
              <div>{{ t('taskVersion.taskTable.tabs.canceled') }}</div>
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
