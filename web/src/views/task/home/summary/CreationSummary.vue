<script setup lang="ts">
import { onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { analysis } from '@/api/tester';

import { ResourceInfo } from '@/views/task/home/types';

type Props = {
  projectId: string;
  userInfo: { id: string; };
  notify: string;
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  userInfo: undefined,
  notify: undefined
});

const { t } = useI18n();

const loading = ref(false);
const allSprint = ref('');
const sprintByLastWeek = ref('');
const sprintByLastMonth = ref('');

const allTask = ref('');
const taskByLastWeek = ref('');
const taskByLastMonth = ref('');

const allTag = ref('');
const tagByLastWeek = ref('');
const tagByLastMonth = ref('');

const allBacklog = ref('');
const backlogByLastWeek = ref('');
const backlogByLastMonth = ref('');

const allMeeting = ref('');
const meetingByLastWeek = ref('');
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
    const data = res.data as ResourceInfo;
    allSprint.value = data.allSprint;
    sprintByLastWeek.value = data.sprintByLastWeek;
    sprintByLastMonth.value = data.sprintByLastMonth;

    allTask.value = data.allTask;
    taskByLastWeek.value = data.taskByLastWeek;
    taskByLastMonth.value = data.taskByLastMonth;

    allTag.value = data.allTag;
    tagByLastWeek.value = data.tagByLastWeek;
    tagByLastMonth.value = data.tagByLastMonth;

    allBacklog.value = data.allBacklog;
    backlogByLastWeek.value = data.backlogByLastWeek;
    backlogByLastMonth.value = data.backlogByLastMonth;

    allMeeting.value = data.allMeeting;
    meetingByLastWeek.value = data.meetingByLastWeek;
    meetingByLastMonth.value = data.meetingByLastMonth;
  }
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

const reset = () => {
  allSprint.value = '';
  sprintByLastWeek.value = '';
  sprintByLastMonth.value = '';
  allTask.value = '';
  taskByLastWeek.value = '';
  taskByLastMonth.value = '';
  allTag.value = '';
  tagByLastWeek.value = '';
  tagByLastMonth.value = '';

  allBacklog.value = '';
  backlogByLastWeek.value = '';
  backlogByLastMonth.value = '';

  allMeeting.value = '';
  meetingByLastWeek.value = '';
  meetingByLastMonth.value = '';
};
</script>
<template>
  <div>
    <div class="text-3.5 font-semibold mb-3">{{ t('taskHome.myCreation') }}</div>
    <div class="flex flex-1 space-x-3.75 justify-start">
      <div class="p-3.5 rounded w-1/3 relative bg-img">
        <div class="space-x-2">
          <span>{{ t('taskHome.backlog') }}</span>
          <span>{{ allBacklog }}</span>
        </div>
        <div class="mt-6 flex items-center w-full">
          <div class="w-1/2 flex flex-wrap mr-2 flex-none">
            <span class="mr-2">{{ t('taskHome.last7Days') }}</span>
            <span>{{ backlogByLastWeek }}</span>
          </div>
          <div class="w-1/2 flex-none flex flex-wrap">
            <span class="mr-2">{{ t('taskHome.last30Days') }}</span>
            <span>{{ backlogByLastMonth }}</span>
          </div>
        </div>
        <img src="./images/tag.png" class="w-15 absolute right-0 top-0 -z-1" />
        <img src="./images/bgtag.png" class="w-full h-full absolute right-0 top-0 -z-2" />
      </div>

      <div class="p-3.5 rounded w-1/3 relative bg-img">
        <div class="space-x-2">
          <span>{{ t('taskHome.sprint') }}</span>
          <span>{{ allSprint }}</span>
        </div>
        <div class="mt-6 flex items-center w-full">
          <div class="w-1/2 flex flex-wrap mr-2 flex-none">
            <span class="mr-2">{{ t('taskHome.last7Days') }}</span>
            <span>{{ sprintByLastWeek }}</span>
          </div>
          <div class="w-1/2 flex-none flex flex-wrap">
            <span class="mr-2">{{ t('taskHome.last30Days') }}</span>
            <span>{{ sprintByLastMonth }}</span>
          </div>
        </div>
        <img src="./images/sprint.png" class="w-15 absolute right-0 top-0 -z-1" />
        <img src="./images/bgsprint.png" class="w-full h-full absolute right-0 top-0 -z-2" />
      </div>

      <div class="p-3.5 rounded w-1/3 relative bg-img">
        <div class="space-x-2">
          <span>{{ t('taskHome.task') }}</span>
          <span>{{ allTask }}</span>
        </div>
        <div class="mt-6 flex items-center w-full">
          <div class="w-1/2 flex flex-wrap mr-2 flex-none">
            <span class="mr-2">{{ t('taskHome.last7Days') }}</span>
            <span>{{ taskByLastWeek }}</span>
          </div>
          <div class="w-1/2 flex-none flex flex-wrap">
            <span class="mr-2">{{ t('taskHome.last30Days') }}</span>
            <span>{{ taskByLastMonth }}</span>
          </div>
        </div>
        <img src="./images/task.png" class="w-15 absolute right-0 top-0 -z-1" />
        <img src="./images/bgtask.png" class="w-full h-full absolute right-0 top-0 -z-2" />
      </div>

      <div class="p-3.5 rounded w-1/3 relative bg-img">
        <div class="space-x-2">
          <span>{{ t('taskHome.meeting') }}</span>
          <span>{{ allMeeting }}</span>
        </div>
        <div class="mt-6 flex items-center w-full">
          <div class="w-1/2 flex flex-wrap mr-2 flex-none">
            <span class="mr-2">{{ t('taskHome.last7Days') }}</span>
            <span>{{ meetingByLastWeek }}</span>
          </div>
          <div class="w-1/2 flex-none flex flex-wrap">
            <span class="mr-2">{{ t('taskHome.last30Days') }}</span>
            <span>{{ meetingByLastMonth }}</span>
          </div>
        </div>
        <img src="./images/project.png" class="w-15 absolute right-0 top-0 -z-1" />
        <img src="./images/bgproject.png" class="w-full h-full absolute right-0 top-0 -z-2" />
      </div>

      <!-- <div class="p-3.5 rounded w-1/3 relative bg-img">
        <div class="space-x-2">
          <span>标签</span>
          <span>{{ allTag }}</span>
        </div>
        <div class="mt-6 flex items-center w-full">
          <div class="w-1/2 flex flex-wrap mr-2 flex-none">
            <span class="mr-2">近7天</span>
            <span>{{ tagByLastWeek }}</span>
          </div>
          <div class="w-1/2 flex-none flex flex-wrap">
            <span class="mr-2">近30天</span>
            <span>{{ tagByLastMonth }}</span>
          </div>
        </div>
        <img src="./images/tagSelector.png" class="w-15 absolute right-0 top-0 -z-1" />
        <img src="./images/bgtag.png" class="w-full h-full absolute right-0 top-0 -z-2" />
      </div> -->
    </div>
  </div>
</template>
<style scoped>
.bg-img {
  background-repeat: no-repeat;
  background-size: 100% 100%;
}
</style>
