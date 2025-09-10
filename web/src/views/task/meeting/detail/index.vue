<script setup lang="ts">
// Import necessary modules and components
import { inject, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Colon, Spin } from '@xcan-angus/vue-ui';
import dayjs from 'dayjs';
import RichEditor from '@/components/richEditor/index.vue';
import { task } from '@/api/tester';

// Import types and constants
import { MeetingInfo } from '../types';
import { DATE_FORMAT, TIME_FORMAT } from '@/utils/constant';

// Define component props
type Props = {
  projectId: string;
  userInfo: { id: string; };
  appInfo: { id: string; };
  data: {
    _id: string;
    id: string | undefined;
  }
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  data: undefined
});

// Initialize i18n and inject updateTabPane function
const { t } = useI18n();
const updateTabPane = inject<(data: { [key: string]: any }) => void>('updateTabPane', () => ({}));

// Reactive data
const dataSource = ref<MeetingInfo>();
const loading = ref(false);

// Format meeting time information
const formatMeetingTime = (timeString: string) => {
  const timeParts = (timeString || '').split('~');
  const startTime = dayjs(timeParts[0]).format(TIME_FORMAT);
  const endTime = dayjs(timeParts[1]).format(TIME_FORMAT);
  return { startTime, endTime };
};

// Format participant names
const formatParticipantNames = (participants: { fullName: string }[]) => {
  return participants.map(participant => participant.fullName).join(',');
};

// Load meeting detail data
const loadMeetingDetail = async (id: string) => {
  // Prevent duplicate requests
  if (loading.value) {
    return;
  }

  loading.value = true;
  const [error, res] = await task.getMeetingDetail(id);
  loading.value = false;
  if (error) {
    return;
  }

  const meetingData = res?.data as MeetingInfo;
  if (!meetingData) {
    return;
  }

  // Format date and time
  const formattedDate = dayjs(meetingData.date);
  const { startTime, endTime } = formatMeetingTime(meetingData.time || '');
  const participantNames = formatParticipantNames(meetingData.participants);

  // Update data source
  dataSource.value = {
    ...meetingData,
    date: formattedDate,
    startTime,
    endTime,
    moderatorName: meetingData.moderator?.fullName,
    participantNames
  } as MeetingInfo & { moderatorName: string; participantNames: string; };

  // Update tab pane title
  const subject = meetingData.subject;
  if (subject && typeof updateTabPane === 'function') {
    updateTabPane({ name: subject, _id: id + '-detail' });
  }
};

// Watch for data changes and load meeting details
onMounted(() => {
  watch(() => props.data, async (newValue, oldValue) => {
    const meetingId = newValue?.id;
    if (!meetingId) {
      return;
    }

    const oldId = oldValue?.id;
    if (meetingId === oldId) {
      return;
    }

    await loadMeetingDetail(meetingId);
  }, { immediate: true });
});

</script>

<template>
  <Spin :spinning="loading" class="h-full text-3 leading-5 px-5 py-5 overflow-auto">
    <div
      :key="dataSource?.id"
      class="text-3 leading-5 space-y-2.5 py-2.5 px-3.5 mb-3.5 last:mb-0 meeting-container">
      <!-- Meeting subject -->
      <div class="text-theme-title font-medium"> {{ dataSource?.subject }}</div>

      <!-- Meeting type and sprint -->
      <div class="flex items-start space-x-5">
        <div class="w-1/2 flex items-start">
          <div class="w-15.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('taskMeeting.columns.type') }}</span>
            <Colon class="w-1" />
          </div>

          <div class="whitespace-pre-wrap break-words break-all">{{ dataSource?.type?.message }}</div>
        </div>

        <div class="w-1/2 flex items-start">
          <div class="w-15.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('taskMeeting.columns.sprint') }}</span>
            <Colon class="w-1" />
          </div>

          <div class="whitespace-pre-wrap break-words break-all">{{ dataSource?.sprintName || '--' }}</div>
        </div>
      </div>

      <!-- Meeting date and time -->
      <div class="flex items-start space-x-5">
        <div class="w-1/2 flex items-start">
          <div class="w-15.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('taskMeeting.columns.date') }}</span>
            <Colon class="w-1" />
          </div>

          <div class="whitespace-pre-wrap break-words break-all">{{ dataSource?.date?.format(DATE_FORMAT) }}</div>
        </div>
        <div class="w-1/2 flex items-start">
          <div class="w-15.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('taskMeeting.columns.time') }}</span>
            <Colon class="w-1" />
          </div>

          <div class="text-3 whitespace-nowrap">
            <span>{{ dataSource?.startTime }}</span>
            <span class="mx-2">至</span>
            <span>{{ dataSource?.endTime }}</span>
          </div>
        </div>
      </div>

      <!-- Meeting location and moderator -->
      <div class="flex items-start space-x-5">
        <div class="w-1/2 flex items-start">
          <div class="w-15.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('taskMeeting.columns.location') }}</span>
            <Colon class="w-1" />
          </div>

          <div class="whitespace-pre-wrap break-words break-all">{{ dataSource?.location || '--' }}</div>
        </div>
        <div class="w-1/2 flex items-start">
          <div class="w-15.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('taskMeeting.columns.moderator') }}</span>
            <Colon class="w-1" />
          </div>

          <div class="whitespace-pre-wrap break-words break-all">{{ dataSource?.moderator?.fullName }}</div>
        </div>
      </div>

      <!-- Meeting participants -->
      <div class="flex items-start">
        <div class="w-15.5 flex items-center whitespace-nowrap flex-shrink-0">
          <span>{{ t('taskMeeting.columns.participants') }}</span>
          <Colon class="w-1" />
        </div>

        <div class="whitespace-pre-wrap break-words break-all">{{ dataSource?.participantNames }}</div>
      </div>

      <!-- Meeting content -->
      <div class="flex items-start">
        <div class="w-15.5 flex items-center whitespace-nowrap flex-shrink-0">
          <span>{{ t('taskMeeting.columns.content') }}</span>
          <Colon class="w-1" />
        </div>

        <RichEditor
          :value="dataSource?.content"
          mode="view" />
      </div>
    </div>
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
}
</style>
