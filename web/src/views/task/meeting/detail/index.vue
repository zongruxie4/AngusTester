<script setup lang="ts">
import { inject, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Colon, Spin } from '@xcan-angus/vue-ui';
import dayjs from 'dayjs';
import { task } from '@/api/tester';
import { MeetingInfo } from '../types';
import { DATE_FORMAT, TIME_FORMAT } from '@/utils/constant';

import RichEditor from '@/components/richEditor/index.vue';

/**
 * Component props interface for meeting detail view
 */
type Props = {
  projectId: string;
  userInfo: { id: string; };
  appInfo: { id: string; };
  data: {
    _id: string;
    id: string | undefined;
  }
}

// COMPONENT PROPS
const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  data: undefined
});

// COMPOSABLES & INJECTIONS
const { t } = useI18n();
const updateTabPane = inject<(data: { [key: string]: any }) => void>('updateTabPane', () => ({}));

// REACTIVE STATE
const meetingDataSource = ref<MeetingInfo>();
const isLoading = ref(false);

/**
 * Parses meeting time string and formats start/end times
 * @param timeString - Time string in format "startTime~endTime"
 * @returns Object containing formatted start and end times
 */
const parseMeetingTimeRange = (timeString: string) => {
  const timeParts = (timeString || '').split('~');
  const startTime = dayjs(timeParts[0]).format(TIME_FORMAT);
  const endTime = dayjs(timeParts[1]).format(TIME_FORMAT);
  return { startTime, endTime };
};

/**
 * Extracts and joins participant names into a comma-separated string
 * @param participants - Array of participant objects with fullName property
 * @returns Comma-separated string of participant names
 */
const extractParticipantNames = (participants: { fullName: string }[]) => {
  return participants.map(participant => participant.fullName).join(',');
};

/**
 * Fetches meeting detail data from API and updates component state
 * @param meetingId - Unique identifier for the meeting
 */
const fetchMeetingDetails = async (meetingId: string) => {
  // Prevent duplicate API requests
  if (isLoading.value) {
    return;
  }

  isLoading.value = true;
  const [error, response] = await task.getMeetingDetail(meetingId);
  isLoading.value = false;

  if (error) {
    return;
  }

  const meetingData = response?.data as MeetingInfo;
  if (!meetingData) {
    return;
  }

  // Process and format meeting data for display
  const formattedDate = dayjs(meetingData.date);
  const { startTime, endTime } = parseMeetingTimeRange(meetingData.time || '');
  const participantNames = extractParticipantNames(meetingData.participants);

  // Update reactive data source with processed information
  meetingDataSource.value = {
    ...meetingData,
    date: formattedDate,
    startTime,
    endTime,
    moderatorName: meetingData.moderator?.fullName,
    participantNames
  } as MeetingInfo & { moderatorName: string; participantNames: string; };

  // Update browser tab title with meeting subject
  const subject = meetingData.subject;
  if (subject && typeof updateTabPane === 'function') {
    updateTabPane({ name: subject, _id: meetingId + '-detail' });
  }
};

/**
 * Component mounted lifecycle hook
 * Sets up watcher for data prop changes to load meeting details
 */
onMounted(() => {
  watch(() => props.data, async (newValue, oldValue) => {
    const currentMeetingId = newValue?.id;
    if (!currentMeetingId) {
      return;
    }

    const previousMeetingId = oldValue?.id;
    if (currentMeetingId === previousMeetingId) {
      return;
    }

    await fetchMeetingDetails(currentMeetingId);
  }, { immediate: true });
});

</script>

<template>
  <Spin :spinning="isLoading" class="h-full text-3 leading-5 px-5 py-5 overflow-auto">
    <div
      :key="meetingDataSource?.id"
      class="text-3 leading-5 space-y-2.5 py-2.5 px-3.5 mb-3.5 last:mb-0 meeting-container">
      <!-- Meeting subject -->
      <div class="text-theme-title font-medium">
        {{ meetingDataSource?.subject }}
      </div>

      <!-- Meeting type and sprint -->
      <div class="flex items-start space-x-5">
        <div class="w-1/2 flex items-start">
          <div class="w-15.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('taskMeeting.columns.type') }}</span>
            <Colon class="w-1" />
          </div>

          <div class="whitespace-pre-wrap break-words break-all">
            {{ meetingDataSource?.type?.message }}
          </div>
        </div>

        <div class="w-1/2 flex items-start">
          <div class="w-15.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('taskMeeting.columns.sprint') }}</span>
            <Colon class="w-1" />
          </div>

          <div class="whitespace-pre-wrap break-words break-all">
            {{ meetingDataSource?.sprintName || '--' }}
          </div>
        </div>
      </div>

      <!-- Meeting date and time -->
      <div class="flex items-start space-x-5">
        <div class="w-1/2 flex items-start">
          <div class="w-15.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('taskMeeting.columns.date') }}</span>
            <Colon class="w-1" />
          </div>

          <div class="whitespace-pre-wrap break-words break-all">
            {{ meetingDataSource?.date?.format(DATE_FORMAT) }}
          </div>
        </div>
        <div class="w-1/2 flex items-start">
          <div class="w-15.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('taskMeeting.columns.time') }}</span>
            <Colon class="w-1" />
          </div>

          <div class="text-3 whitespace-nowrap">
            <span>{{ meetingDataSource?.startTime }}</span>
            <span class="mx-2">至</span>
            <span>{{ meetingDataSource?.endTime }}</span>
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

          <div class="whitespace-pre-wrap break-words break-all">
            {{ meetingDataSource?.location || '--' }}
          </div>
        </div>
        <div class="w-1/2 flex items-start">
          <div class="w-15.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('taskMeeting.columns.moderator') }}</span>
            <Colon class="w-1" />
          </div>

          <div class="whitespace-pre-wrap break-words break-all">
            {{ meetingDataSource?.moderator?.fullName }}
          </div>
        </div>
      </div>

      <!-- Meeting participants -->
      <div class="flex items-start">
        <div class="w-15.5 flex items-center whitespace-nowrap flex-shrink-0">
          <span>{{ t('taskMeeting.columns.participants') }}</span>
          <Colon class="w-1" />
        </div>

        <div class="whitespace-pre-wrap break-words break-all">
          {{ meetingDataSource?.participantNames }}
        </div>
      </div>

      <!-- Meeting content -->
      <div class="flex items-start">
        <div class="w-15.5 flex items-center whitespace-nowrap flex-shrink-0">
          <span>{{ t('taskMeeting.columns.content') }}</span>
          <Colon class="w-1" />
        </div>

        <RichEditor
          :value="meetingDataSource?.content"
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
