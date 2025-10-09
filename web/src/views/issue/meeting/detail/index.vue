<script setup lang="ts">
import { inject, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Colon, Spin } from '@xcan-angus/vue-ui';
import dayjs from 'dayjs';
import { issue } from '@/api/tester';
import { MeetingInfo } from '../types';
import { DATE_FORMAT, TIME_FORMAT } from '@/utils/constant';
import { BasicProps } from '@/types/types';

import RichEditor from '@/components/richEditor/index.vue';

// COMPONENT PROPS
const props = withDefaults(defineProps<BasicProps>(), {
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
  const [error, response] = await issue.getMeetingDetail(meetingId);
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
      class="bg-white rounded-lg shadow-sm p-6 mb-6 meeting-container">
      <!-- Meeting subject -->
      <div class="text-theme-title font-medium text-xl mb-6 pb-4 border-b border-gray-200">
        {{ meetingDataSource?.subject }}
      </div>

      <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
        <!-- Meeting type and sprint -->
        <div class="space-y-4">
          <div class="flex items-start">
            <div
              class="w-24 flex items-center whitespace-nowrap flex-shrink-0 text-gray-900 font-medium text-3 text-right">
              <span>{{ t('common.type') }}</span>
              <Colon class="w-1 mx-1" />
            </div>

            <div class="whitespace-pre-wrap break-words break-all text-gray-900 text-3">
              {{ meetingDataSource?.type?.message }}
            </div>
          </div>

          <div class="flex items-start">
            <div
              class="w-24 flex items-center whitespace-nowrap flex-shrink-0 text-gray-900 font-medium text-3 text-right">
              <span>{{ t('common.sprint') }}</span>
              <Colon class="w-1 mx-1" />
            </div>

            <div class="whitespace-pre-wrap break-words break-all text-gray-900 text-3">
              {{ meetingDataSource?.sprintName || '--' }}
            </div>
          </div>
        </div>

        <!-- Meeting date and time -->
        <div class="space-y-4">
          <div class="flex items-start">
            <div
              class="w-24 flex items-center whitespace-nowrap flex-shrink-0 text-gray-900 font-medium text-3 text-right">
              <span>{{ t('meeting.columns.date') }}</span>
              <Colon class="w-1 mx-1" />
            </div>

            <div class="whitespace-pre-wrap break-words break-all text-gray-900 text-3">
              {{ meetingDataSource?.date?.format(DATE_FORMAT) }}
            </div>
          </div>

          <div class="flex items-start">
            <div
              class="w-24 flex items-center whitespace-nowrap flex-shrink-0 text-gray-900 font-medium text-3 text-right">
              <span>{{ t('meeting.columns.time') }}</span>
              <Colon class="w-1 mx-1" />
            </div>

            <div class="text-3 whitespace-nowrap text-gray-900">
              <span>{{ meetingDataSource?.startTime }}</span>
              <span class="mx-2 text-gray-400">-</span>
              <span>{{ meetingDataSource?.endTime }}</span>
            </div>
          </div>
        </div>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-2 gap-4 mt-4">
        <!-- Meeting location and moderator -->
        <div class="space-y-4">
          <div class="flex items-start">
            <div
              class="w-24 flex items-center whitespace-nowrap flex-shrink-0 text-gray-900 font-medium text-3 text-right">
              <span>{{ t('meeting.columns.location') }}</span>
              <Colon class="w-1 mx-1" />
            </div>

            <div class="whitespace-pre-wrap break-words break-all text-gray-900 text-3">
              {{ meetingDataSource?.location || '--' }}
            </div>
          </div>

          <div class="flex items-start">
            <div
              class="w-24 flex items-center whitespace-nowrap flex-shrink-0 text-gray-900 font-medium text-3 text-right">
              <span>{{ t('meeting.columns.moderator') }}</span>
              <Colon class="w-1 mx-1" />
            </div>

            <div class="whitespace-pre-wrap break-words break-all text-gray-900 text-3">
              {{ meetingDataSource?.moderator?.fullName }}
            </div>
          </div>
        </div>

        <!-- Meeting participants -->
        <div class="space-y-4">
          <div class="flex items-start">
            <div
              class="w-24 flex items-center whitespace-nowrap flex-shrink-0 text-gray-900 font-medium text-3 text-right">
              <span>{{ t('meeting.columns.participants') }}</span>
              <Colon class="w-1 mx-1" />
            </div>

            <div class="whitespace-pre-wrap break-words break-all text-gray-900 text-3">
              {{ meetingDataSource?.participantNames || '--' }}
            </div>
          </div>
        </div>
      </div>

      <!-- Meeting content -->
      <div class="mt-6 pt-3 border-t border-gray-200">
        <div class="mt-2">
          <RichEditor
            :value="meetingDataSource?.content"
            mode="view" />
        </div>
      </div>
    </div>
  </Spin>
</template>

<style scoped>
.meeting-container {
  border: 1px solid var(--border-text-box);
}
</style>
