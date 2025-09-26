<script setup lang="ts">
import { computed, defineAsyncComponent, inject, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Button, TabPane, Tabs } from 'ant-design-vue';
import { Icon, notification, Spin } from '@xcan-angus/vue-ui';
import { appContext, download, enumUtils, TESTER, toClipboard, utils } from '@xcan-angus/infra';
import dayjs from 'dayjs';
import { TaskSprintPermission } from '@/enums/enums';
import { task } from '@/api/tester';
import { SprintInfo } from '../types';
import { DATE_FORMAT, TIME_FORMAT } from '@/utils/constant';
import { BasicProps } from '@/types/types';

// Props Definition
const props = withDefaults(defineProps<BasicProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  data: undefined
});

// Async Components
const RichEditor = defineAsyncComponent(() => import('@/components/richEditor/index.vue'));
const Meeting = defineAsyncComponent(() => import('./Meeting.vue'));
const BasicInfo = defineAsyncComponent(() => import('./BasicInfo.vue'));
const BurnDownChart = defineAsyncComponent(() => import('@/views/task/sprint/detail/BurndownChart.vue'));
const MembersProgress = defineAsyncComponent(() => import('@/views/task/sprint/detail/MemberProgress.vue'));
const WorkCalendar = defineAsyncComponent(() => import('@/views/task/home/WorkCalendar.vue'));

// Composables
const { t } = useI18n();

// Injections
const updateTabPane = inject<(data: { [key: string]: any }) => void>('updateTabPane', () => ({}));
const replaceTabPane = inject<(key: string, data: { [key: string]: any }) => void>('replaceTabPane', () => ({}));
const deleteTabPane = inject<(key: string, data: { [key: string]: any }) => void>('deleteTabPane', () => ({}));

/**
 * Checks if current user is admin
 */
const isAdmin = computed(() => appContext.isAdmin());

/**
 * Sprint information data
 */
const sprintData = ref<SprintInfo>();

/**
 * User permissions for current sprint
 */
const userPermissions = ref<TaskSprintPermission[]>([]);

/**
 * Sprint completion rate percentage
 */
const completionRate = ref(0);

/**
 * Loading state for data operations
 */
const isLoading = ref(false);

/**
 * Loading state for export operations
 */
const isExporting = ref(false);

/**
 * Current sprint ID from data source
 */
const sprintId = computed(() => {
  return sprintData.value?.id;
});

// Attachments are now handled inside BasicInfo component

/**
 * Formatted meetings data with processed dates and times
 */
const meetings = computed<any[]>(() => {
  return sprintData.value?.meetings?.map(raw => {
    const date = dayjs(raw.date).format(DATE_FORMAT);

    const time = raw.time.split('~');
    const startTime = dayjs(time[0]).format(TIME_FORMAT);
    const endTime = dayjs(time[1]).format(TIME_FORMAT);

    const participantNames = raw.participants.map(p => p.fullName).join(',');
    return {
      ...raw,
      subject: (raw as any).subject || '',
      date,
      startTime,
      endTime,
      moderatorName: raw.moderator?.fullName,
      participantNames,
      id: utils.uuid()
    };
  }) || [];
});

/**
 * Loads user permissions for the current sprint
 * @param id - Sprint ID
 */
const loadUserPermissions = async (id: string) => {
  if (isAdmin.value) {
    userPermissions.value = enumUtils.getEnumValues(TaskSprintPermission);
    return;
  }
  const params = { admin: true };
  isLoading.value = true;
  const [error, res] = await task.getCurrentUserSprintAuth(id, params);
  isLoading.value = false;
  if (error) {
    return;
  }

  const { taskSprintAuth, permissions: _permissions } = res?.data || { taskSprintAuth: true, permissions: [] };
  if (!taskSprintAuth) {
    userPermissions.value = enumUtils.getEnumValues(TaskSprintPermission);
    if (_permissions.includes(TaskSprintPermission.GRANT)) {
      userPermissions.value.push(TaskSprintPermission.GRANT);
    }
  } else {
    userPermissions.value = (_permissions || []).map(item => item.value);
  }
};

/**
 * Loads sprint detail data from API
 * @param id - Sprint ID
 */
const loadSprintData = async (id: string) => {
  isLoading.value = true;
  const [error, res] = await task.getSprintDetail(id);
  isLoading.value = false;
  if (error) {
    return;
  }

  const data = res?.data as SprintInfo;
  if (!data) {
    return;
  }

  sprintData.value = data;
  if (sprintData.value.progress?.completedRate) {
    completionRate.value = +sprintData.value.progress.completedRate.replace(/(\d+\.\d{2})\d+/, '$1');
  }

  const name = data.name;
  if (name && typeof updateTabPane === 'function') {
    updateTabPane({ name, _id: id + '-detail' });
  }
};

/**
 * Navigates to sprint edit page
 */
const navigateToEdit = () => {
  replaceTabPane(sprintId.value + '-detail', {
    _id: sprintId.value,
    value: 'sprintEdit',
    noCache: true,
    data: sprintData
  });
};

/**
 * Exports sprint tasks to file
 */
const exportSprintTasks = async () => {
  if (!sprintData.value) {
    return;
  }

  const { id, projectId } = sprintData.value;
  if (isExporting.value) {
    return;
  }

  isExporting.value = false;
  await download(`${TESTER}/task/export?projectId=${projectId}&sprintId=${id}`);
  isExporting.value = true;
};

/**
 * Copies sprint link to clipboard
 */
const copySprintLink = () => {
  const message = window.location.origin + '/task#sprint?id=' + sprintId.value;
  toClipboard(message).then(() => {
    notification.success(t('taskSprint.messages.copyLinkSuccess'));
  }).catch(() => {
    notification.error(t('taskSprint.messages.copyLinkFailed'));
  });
};

/**
 * Refreshes sprint data
 */
const refreshSprintData = () => {
  const id = sprintId.value;
  if (!id) {
    return;
  }
  loadSprintData(id);
};

/**
 * Closes the current tab pane
 */
const closeTabPane = () => {
  if (props.data?.id) {
    deleteTabPane(props.data.id + '-detail', {});
  }
};

// Lifecycle Hooks
onMounted(() => {
  watch(() => props.data, async (newValue) => {
    const id = newValue?.id;
    if (!id) {
      return;
    }

    await loadUserPermissions(id);
    await loadSprintData(id);
  }, { immediate: true });
});
</script>

<template>
  <Spin :spinning="isLoading" class="h-full text-3 leading-5 px-5 py-5 overflow-auto">
    <div class="flex items-start flex-wrap space-y-b-2 space-x-2.5 mb-3.5">
      <Button
        :disabled="!isAdmin && !userPermissions.includes(TaskSprintPermission.MODIFY_SPRINT)"
        type="default"
        size="small"
        class="flex items-center space-x-1"
        @click="navigateToEdit">
        <Icon icon="icon-shuxie" class="text-3.5" />
        <span>{{ t('common.edit') }}</span>
      </Button>

      <Button
        type="default"
        size="small"
        class="p-0">
        <RouterLink
          class="flex items-center space-x-1 leading-6.5 px-1.75"
          :to="`/task#task?sprintId=${sprintId}&sprintName=${sprintData?.name}`">
          <Icon icon="icon-renwu2" class="text-3.5" />
          <span>{{ t('taskSprint.actions.viewTasks') }}</span>
        </RouterLink>
      </Button>

      <Button
        :disabled="!isAdmin && !userPermissions.includes(TaskSprintPermission.EXPORT_TASK)"
        type="default"
        size="small"
        class="flex items-center space-x-1"
        @click="exportSprintTasks">
        <Icon icon="icon-daochu" class="text-3.5" />
        <span>{{ t('taskSprint.actions.exportTasks') }}</span>
      </Button>

      <Button
        size="small"
        class="flex items-center"
        @click="copySprintLink">
        <Icon class="mr-1 flex-shrink-0" icon="icon-fuzhi" />
        <span>{{ t('actions.copyLink') }}</span>
      </Button>

      <Button
        type="default"
        size="small"
        class="flex items-center"
        @click="refreshSprintData">
        <Icon class="mr-1 flex-shrink-0" icon="icon-shuaxin" />
        <span>{{ t('common.refresh') }}</span>
      </Button>

      <Button
        type="default"
        size="small"
        class="flex items-center"
        @click="closeTabPane">
        <Icon class="mr-1 flex-shrink-0" icon="icon-zhongzhi2" />
        <span>{{ t('common.cancel') }}</span>
      </Button>
    </div>

    <BasicInfo :sprintData="sprintData" :completionRate="completionRate" />

    <Tabs size="small">
      <TabPane key="acceptanceCriteria" :tab="t('taskSprint.columns.acceptanceCriteria')">
        <div class="space-y-1 whitespace-pre-wrap break-words break-all">
          <!-- {{ dataSource?.acceptanceCriteria }} -->
          <RichEditor
            v-if="sprintData?.acceptanceCriteria"
            :value="sprintData?.acceptanceCriteria"
            mode="view" />
        </div>
      </TabPane>
      <TabPane key="otherInformation" :tab="t('taskSprint.columns.otherInformation')">
        <div class="space-y-1 whitespace-pre-wrap break-words break-all">
          <!-- {{ dataSource?.otherInformation }} -->
          <RichEditor
            v-if="sprintData?.otherInformation"
            :value="sprintData?.otherInformation"
            mode="view" />
        </div>
      </TabPane>
      <TabPane key="chart" :tab="t('chart.burndown.title')">
        <BurnDownChart :sprintId="sprintId" />
      </TabPane>
      <TabPane key="progress" :tab="t('taskSprint.columns.memberProgress')">
        <MembersProgress :sprintId="sprintId" :projectId="props.projectId" />
      </TabPane>
      <TabPane key="workCalendar" :tab="t('taskSprint.columns.workCalendar')">
        <WorkCalendar
          :projectId="props.projectId"
          :userInfo="props.userInfo"
          :sprintId="sprintId" />
      </TabPane>
      <TabPane key="meetings" :tab="t('taskSprint.columns.meetingRecords')">
        <Meeting :meetings="meetings" />
      </TabPane>
    </Tabs>
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

.meeting-container {
  border: 1px solid var(--border-text-box);
  border-radius: 4px;
  background-color: #fafafa;
}
</style>
