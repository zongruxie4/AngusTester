<script setup lang="ts">
import { onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { analysis } from '@/api/tester';

import { SummaryInfo } from '@/views/issue/home/types';
import { BasicProps } from '@/types/types';

// Props definition
const props = withDefaults(defineProps<BasicProps>(), {
  projectId: undefined,
  userInfo: undefined,
  notify: undefined
});

const { t } = useI18n();

const loading = ref(false);
// Reactive counters grouped by resource
const allSprint = ref('');
const sprintByLast7Days = ref('');
const sprintByLastMonth = ref('');

const allTask = ref('');
const taskByLast7Days = ref('');
const taskByLastMonth = ref('');

const allTag = ref('');
const tagByLast7Days = ref('');
const tagByLastMonth = ref('');

const allBacklog = ref('');
const backlogByLast7Days = ref('');
const backlogByLastMonth = ref('');

const allMeeting = ref('');
const meetingByLast7Days = ref('');
const meetingByLastMonth = ref('');

const loadData = async (): Promise<void> => {
  loading.value = true;
  const params = {
    projectId: props.projectId,
    creatorObjectType: 'USER',
    creatorObjectId: props.userInfo?.id
  };
  const [error, res] = await analysis.getTaskResourceCount(params);
  loading.value = false;
  if (error) {
    return;
  }

  if (res?.data) {
    const data = res.data as SummaryInfo;
    // Map response data to local refs for display
    allSprint.value = data.allSprint;
    sprintByLast7Days.value = data.sprintByLast7Days;
    sprintByLastMonth.value = data.sprintByLastMonth;

    allTask.value = data.allTask;
    taskByLast7Days.value = data.taskByLast7Days;
    taskByLastMonth.value = data.taskByLastMonth;

    allTag.value = data.allTag;
    tagByLast7Days.value = data.tagByLast7Days;
    tagByLastMonth.value = data.tagByLastMonth;

    allBacklog.value = data.allBacklog ?? '0';
    backlogByLast7Days.value = data.backlogByLast7Days ?? '0';
    backlogByLastMonth.value = data.backlogByLastMonth ?? '0';

    allMeeting.value = data.allMeeting ?? '0';
    meetingByLast7Days.value = data.meetingByLast7Days ?? '0';
    meetingByLastMonth.value = data.meetingByLastMonth ?? '0';
  }
};

const reset = () => {
  // Reset all counters when project or notify changes
  allSprint.value = '';
  sprintByLast7Days.value = '';
  sprintByLastMonth.value = '';
  allTask.value = '';
  taskByLast7Days.value = '';
  taskByLastMonth.value = '';
  allTag.value = '';
  tagByLast7Days.value = '';
  tagByLastMonth.value = '';

  allBacklog.value = '';
  backlogByLast7Days.value = '';
  backlogByLastMonth.value = '';

  allMeeting.value = '';
  meetingByLast7Days.value = '';
  meetingByLastMonth.value = '';
};

onMounted(() => {
  watch(() => props.projectId, () => {
    reset();
    loadData();
  }, { immediate: true });

  watch(() => props.notify, (newValue) => {
    if (newValue === undefined || newValue === null || newValue === '') {
      return;
    }

    reset();
    loadData();
  }, { immediate: true });
});
</script>
<template>
  <div>
    <div class="text-3.5 font-semibold mb-3">{{ t('issueHome.myCreationSummary.title') }}</div>
    <div class="flex flex-1 space-x-3.75 justify-start">
      <div class="p-3.5 rounded w-1/3 relative bg-img">
        <div class="space-x-2">
          <span class="text-3.5">{{ t('common.backlog') }}</span>
          <span class="text-4.5 font-semibold">{{ allBacklog }}</span>
        </div>
        <div class="mt-6 flex items-center w-full">
          <div class="w-1/2 flex flex-wrap mr-2 flex-none">
            <span class="mr-2">{{ t('quickSearch.last7Days') }}</span>
            <span class="text-3.5 font-semibold">{{ backlogByLast7Days }}</span>
          </div>
          <div class="w-1/2 flex-none flex flex-wrap">
            <span class="mr-2">{{ t('quickSearch.last30Days') }}</span>
            <span class="text-3.5 font-semibold">{{ backlogByLastMonth }}</span>
          </div>
        </div>
        <img src="./images/tag.png" class="w-15 absolute right-0 top-0 -z-1" />
        <img src="./images/bgtag.png" class="w-full h-full absolute right-0 top-0 -z-2" />
      </div>

      <div class="p-3.5 rounded w-1/3 relative bg-img">
        <div class="space-x-2">
          <span class="text-3.5">{{ t('common.sprint') }}</span>
          <span class="text-4.5 font-semibold">{{ allSprint }}</span>
        </div>
        <div class="mt-6 flex items-center w-full">
          <div class="w-1/2 flex flex-wrap mr-2 flex-none">
            <span class="mr-2">{{ t('quickSearch.last7Days') }}</span>
            <span class="text-3.5 font-semibold">{{ sprintByLast7Days }}</span>
          </div>
          <div class="w-1/2 flex-none flex flex-wrap">
            <span class="mr-2">{{ t('quickSearch.last30Days') }}</span>
            <span class="text-3.5 font-semibold">{{ sprintByLastMonth }}</span>
          </div>
        </div>
        <img src="./images/sprint.png" class="w-15 absolute right-0 top-0 -z-1" />
        <img src="./images/bgsprint.png" class="w-full h-full absolute right-0 top-0 -z-2" />
      </div>

      <div class="p-3.5 rounded w-1/3 relative bg-img">
        <div class="space-x-2">
          <span class="text-3.5">{{ t('common.issue') }}</span>
          <span class="text-4.5 font-semibold">{{ allTask }}</span>
        </div>
        <div class="mt-6 flex items-center w-full">
          <div class="w-1/2 flex flex-wrap mr-2 flex-none">
            <span class="mr-2">{{ t('quickSearch.last7Days') }}</span>
            <span class="text-3.5 font-semibold">{{ taskByLast7Days }}</span>
          </div>
          <div class="w-1/2 flex-none flex flex-wrap">
            <span class="mr-2">{{ t('quickSearch.last30Days') }}</span>
            <span class="text-3.5 font-semibold">{{ taskByLastMonth }}</span>
          </div>
        </div>
        <img src="./images/task.png" class="w-15 absolute right-0 top-0 -z-1" />
        <img src="./images/bgtask.png" class="w-full h-full absolute right-0 top-0 -z-2" />
      </div>

      <div class="p-3.5 rounded w-1/3 relative bg-img">
        <div class="space-x-2">
          <span class="text-3.5">{{ t('common.meeting') }}</span>
          <span class="text-4.5 font-semibold">{{ allMeeting }}</span>
        </div>
        <div class="mt-6 flex items-center w-full">
          <div class="w-1/2 flex flex-wrap mr-2 flex-none">
            <span class="mr-2">{{ t('quickSearch.last7Days') }}</span>
            <span class="text-3.5 font-semibold">{{ meetingByLast7Days }}</span>
          </div>
          <div class="w-1/2 flex-none flex flex-wrap">
            <span class="mr-2">{{ t('quickSearch.last30Days') }}</span>
            <span class="text-3.5 font-semibold">{{ meetingByLastMonth }}</span>
          </div>
        </div>
        <img src="./images/project.png" class="w-15 absolute right-0 top-0 -z-1" />
        <img src="./images/bgproject.png" class="w-full h-full absolute right-0 top-0 -z-2" />
      </div>
    </div>
  </div>
</template>
<style scoped>
.bg-img {
  background-repeat: no-repeat;
  background-size: 100% 100%;
}
</style>
