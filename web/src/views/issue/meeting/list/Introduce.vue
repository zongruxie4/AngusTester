<script lang="ts" setup>
import { onBeforeUnmount, onMounted, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import elementResizeDetector from 'element-resize-detector';

const { t } = useI18n();

const containerRef = ref();
const isTwoColumnLayout = ref(false);

const resizeDetector = elementResizeDetector({ strategy: 'scroll' });

/**
 * Handles container resize events to determine layout columns
 * Switches to two-column layout when container width is 800px or less
 */
const handleContainerResize = () => {
  const containerWidth = containerRef.value.clientWidth;
  isTwoColumnLayout.value = containerWidth <= 800;
};

/**
 * Component mounted lifecycle hook
 * Sets up resize detection and initial layout state
 */
onMounted(() => {
  resizeDetector.listenTo(containerRef.value, handleContainerResize);
  // Set initial layout state based on container width
  isTwoColumnLayout.value = containerRef.value.clientWidth < 600;
});

/**
 * Component before unmount lifecycle hook
 * Cleans up resize detection listener
 */
onBeforeUnmount(() => {
  resizeDetector.removeListener(containerRef.value, handleContainerResize);
});

</script>
<template>
  <div ref="containerRef">
    <div class="text-3.5 font-semibold mb-2.5">
      {{ t('meeting.aboutMeeting') }}
    </div>
    <div class="text-3.5 mb-6 font-serif">
      {{ t('meeting.aboutMeetingDesc') }}
    </div>

    <div class="space-y-6">
      <div class="flex items-start justify-between font-serif">
        <div class="flex items-start space-x-3 flex-1">
          <img src="./images/1.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold">{{ t('meeting.meetingTypes.productGrooming') }}</div>
            <div class="text-3.5">{{ t('meeting.meetingTypes.productGroomingDesc') }}</div>
          </div>
        </div>

        <div class="flex items-start space-x-3 flex-1">
          <img src="./images/2.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold">{{ t('meeting.meetingTypes.sprintPlanning') }}</div>
            <div class="text-3.5">{{ t('meeting.meetingTypes.sprintPlanningDesc') }}</div>
          </div>
        </div>

        <div v-show="!isTwoColumnLayout" class="flex items-start space-x-3 flex-1">
          <img src="./images/3.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold">{{ t('meeting.meetingTypes.dailyStandup') }}</div>
            <div class="text-3.5">{{ t('meeting.meetingTypes.dailyStandupDesc') }}</div>
          </div>
        </div>
      </div>
      <div v-show="isTwoColumnLayout" class="flex items-start justify-between">
        <div class="flex items-start space-x-3 flex-1">
          <img src="./images/3.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold">{{ t('meeting.meetingTypes.dailyStandup') }}</div>
            <div class="text-3.5">{{ t('meeting.meetingTypes.dailyStandupDesc') }}</div>
          </div>
        </div>

        <div class="flex items-start space-x-3 flex-1">
          <img src="./images/4.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold">{{ t('meeting.meetingTypes.sprintReview') }}</div>
            <div class="text-3.5">{{ t('meeting.meetingTypes.sprintReviewDesc') }}</div>
          </div>
        </div>
      </div>

      <div class="flex items-start justify-between">
        <div v-show="!isTwoColumnLayout" class="flex items-start space-x-3 flex-1">
          <img src="./images/4.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold">{{ t('meeting.meetingTypes.sprintReview') }}</div>
            <div class="text-3.5">{{ t('meeting.meetingTypes.sprintReviewDesc') }}</div>
          </div>
        </div>

        <div class="flex items-start space-x-3 flex-1">
          <img src="./images/5.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold">{{ t('meeting.meetingTypes.sprintRetrospective') }}</div>
            <div class="text-3.5">{{ t('meeting.meetingTypes.sprintRetrospectiveDesc') }}</div>
          </div>
        </div>

        <div class="flex items-start space-x-3 flex-1">
          <img src="./images/6.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold">{{ t('meeting.meetingTypes.other') }}</div>
            <div class="text-3.5">{{ t('meeting.meetingTypes.otherDesc') }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
