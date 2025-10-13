<script setup lang="ts">
import { defineAsyncComponent, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { TabPane, Tabs } from 'ant-design-vue';
import { TaskStatus } from '@/enums/enums';
import { BasicProps } from '@/types/types';

// Props interface for Added component
const props = withDefaults(defineProps<BasicProps>(), {
  projectId: undefined,
  userInfo: undefined,
  notify: undefined
});

const { t } = useI18n();

// Lazy load the table component for better performance
const TaskTable = defineAsyncComponent(() => import('./AddedTable.vue'));

// Notification state for deleted issues
const deletedNotify = ref<string>();

// Task count totals for each tab
const taskCountTotals = {
  pending: ref(0),
  confirming: ref(0),
  completed: ref(0),
  createdBy: ref(0),
  followed: ref(0),
  favourited: ref(0),
  commented: ref(0)
};

/**
 * Query parameters for different task filtering scenarios.
 * <p>
 * Each parameter object defines the criteria for filtering tasks in different tabs.
 * </p>
 */
const taskQueryParams = {
  // Tasks assigned to current user that are pending
  pending: {
    assigneeId: props.userInfo?.id,
    status: TaskStatus.PENDING
  },

  // Tasks that need confirmation by current user
  confirming: {
    confirmerId: props.userInfo?.id,
    status: TaskStatus.CONFIRMING
  },

  // Tasks assigned to current user that are completed
  completed: {
    assigneeId: props.userInfo?.id,
    status: TaskStatus.COMPLETED
  },

  // Tasks created by current user
  createdBy: {
    createdBy: props.userInfo?.id
  },

  // Tasks followed by current user
  followed: {
    followBy: props.userInfo?.id
  },

  // Tasks favourited by current user
  favourited: {
    favouriteBy: props.userInfo?.id
  },

  // Tasks commented by current user
  commented: {
    commentBy: props.userInfo?.id
  }
};
</script>

<template>
  <div>
    <div class="text-3.5 font-semibold mb-1">
      {{ t('issueHome.myIssues.title') }}
    </div>

    <Tabs size="small">
      <!-- Created Tasks Tab: Shows tasks created by current user -->
      <TabPane key="createBy" forceRender>
        <template #tab>
          <div class="flex items-center flex-nowrap">
            <span class="mr-1">{{ t('status.added') }}</span>
            <span>(</span>
            <span>{{ taskCountTotals.createdBy }}</span>
            <span>)</span>
          </div>
        </template>
        <TaskTable
          v-model:total="taskCountTotals.createdBy.value"
          v-model:deletedNotify="deletedNotify"
          :notify="props.notify"
          :projectId="props.projectId"
          :params="taskQueryParams.createdBy" />
      </TabPane>

      <!-- Pending Tasks Tab: Shows tasks assigned to current user that are pending -->
      <TabPane key="pending" forceRender>
        <template #tab>
          <div class="flex items-center flex-nowrap">
            <span class="mr-1">{{ t('status.pending') }}</span>
            <span>(</span>
            <span>{{ taskCountTotals.pending }}</span>
            <span>)</span>
          </div>
        </template>
        <TaskTable
          v-model:total="taskCountTotals.pending.value"
          v-model:deletedNotify="deletedNotify"
          :notify="props.notify"
          :projectId="props.projectId"
          :params="taskQueryParams.pending" />
      </TabPane>

      <!-- Confirming Tasks Tab: Shows tasks that need confirmation by current user -->
      <TabPane key="confirming" forceRender>
        <template #tab>
          <div class="flex items-center flex-nowrap">
            <span class="mr-1">{{ t('status.pendingConfirmation') }}</span>
            <span>(</span>
            <span>{{ taskCountTotals.confirming }}</span>
            <span>)</span>
          </div>
        </template>
        <TaskTable
          v-model:total="taskCountTotals.confirming.value"
          v-model:deletedNotify="deletedNotify"
          :notify="props.notify"
          :projectId="props.projectId"
          :params="taskQueryParams.confirming" />
      </TabPane>

      <!-- Completed Tasks Tab: Shows completed tasks assigned to current user -->
      <TabPane key="completed" forceRender>
        <template #tab>
          <div class="flex items-center flex-nowrap">
            <span class="mr-1">{{ t('status.completed') }}</span>
            <span>(</span>
            <span>{{ taskCountTotals.completed }}</span>
            <span>)</span>
          </div>
        </template>
        <TaskTable
          v-model:total="taskCountTotals.completed.value"
          v-model:deletedNotify="deletedNotify"
          :notify="props.notify"
          :projectId="props.projectId"
          :params="taskQueryParams.completed" />
      </TabPane>

      <!-- Followed Tasks Tab: Shows tasks followed by current user -->
      <TabPane key="follow" forceRender>
        <template #tab>
          <div class="flex items-center flex-nowrap">
            <span class="mr-1">{{ t('status.followed') }}</span>
            <span>(</span>
            <span>{{ taskCountTotals.followed }}</span>
            <span>)</span>
          </div>
        </template>
        <TaskTable
          v-model:total="taskCountTotals.followed.value"
          v-model:deletedNotify="deletedNotify"
          :notify="props.notify"
          :projectId="props.projectId"
          :params="taskQueryParams.followed" />
      </TabPane>

      <!-- Favorited Tasks Tab: Shows tasks favourited by current user -->
      <TabPane key="favorite" forceRender>
        <template #tab>
          <div class="flex items-center flex-nowrap">
            <span class="mr-1">{{ t('status.favourited') }}</span>
            <span>(</span>
            <span>{{ taskCountTotals.favourited }}</span>
            <span>)</span>
          </div>
        </template>
        <TaskTable
          v-model:total="taskCountTotals.favourited.value"
          v-model:deletedNotify="deletedNotify"
          :notify="props.notify"
          :projectId="props.projectId"
          :params="taskQueryParams.favourited" />
      </TabPane>

      <!-- Commented Tasks Tab: Shows tasks commented by current user -->
      <TabPane key="commentBy" forceRender>
        <template #tab>
          <div class="flex items-center flex-nowrap">
            <span class="mr-1">{{ t('status.commented') }}</span>
            <span>(</span>
            <span>{{ taskCountTotals.commented }}</span>
            <span>)</span>
          </div>
        </template>
        <TaskTable
          v-model:total="taskCountTotals.commented.value"
          v-model:deletedNotify="deletedNotify"
          :notify="props.notify"
          :projectId="props.projectId"
          :params="taskQueryParams.commented" />
      </TabPane>
    </Tabs>
  </div>
</template>

<style scoped>
.ant-tabs {
  line-height: 20px;
}

:deep(.ant-tabs-content-holder) {
  min-height: 225px;
}

.ant-tabs-top>:deep(.ant-tabs-nav),
.ant-tabs-bottom>:deep(.ant-tabs-nav),
.ant-tabs-top>:deep(div)>.ant-tabs-nav,
.ant-tabs-bottom>:deep(div)>.ant-tabs-nav {
  margin-bottom: 14px;
}
</style>
