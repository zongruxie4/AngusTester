<script lang="ts" setup>
import { onBeforeUnmount, onMounted, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import elementResizeDetector from 'element-resize-detector';

// COMPOSABLES
const { t } = useI18n();

// REACTIVE STATE
const containerRef = ref();
const isTwoColumnLayout = ref(false);

// RESIZE DETECTION
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
    <div class="text-3.5 font-semibold mb-2.5">{{ t('taskMeeting.aboutMeeting') }}</div>
    <div class="mb-6">
      <div>{{ t('taskMeeting.aboutMeetingDesc') }}</div>
    </div>

    <div class="space-y-6">
      <div class="flex items-start justify-between">
        <div class="flex items-start space-x-3 flex-1">
          <img src="./images/1.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold">{{ t('taskMeeting.meetingTypes.productGrooming') }}</div>
            <div>{{ t('taskMeeting.meetingTypes.productGroomingDesc') }}</div>
          </div>
        </div>

        <div class="flex items-start space-x-3 flex-1">
          <img src="./images/2.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold">{{ t('taskMeeting.meetingTypes.sprintPlanning') }}</div>
            <div>{{ t('taskMeeting.meetingTypes.sprintPlanningDesc') }}</div>
          </div>
        </div>

        <div v-show="!isTwoColumnLayout" class="flex items-start space-x-3 flex-1">
          <img src="./images/3.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold">{{ t('taskMeeting.meetingTypes.dailyStandup') }}</div>
            <div>{{ t('taskMeeting.meetingTypes.dailyStandupDesc') }}</div>
          </div>
        </div>
      </div>
      <div v-show="isTwoColumnLayout" class="flex items-start justify-between">
        <div class="flex items-start space-x-3 flex-1">
          <img src="./images/3.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold">{{ t('taskMeeting.meetingTypes.dailyStandup') }}</div>
            <div>{{ t('taskMeeting.meetingTypes.dailyStandupDesc') }}</div>
          </div>
        </div>
        <div class="flex items-start space-x-3 flex-1">
          <img src="./images/4.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold">{{ t('taskMeeting.meetingTypes.sprintReview') }}</div>
            <div>{{ t('taskMeeting.meetingTypes.sprintReviewDesc') }}</div>
          </div>
        </div>
      </div>

      <div class="flex items-start justify-between">
        <div v-show="!isTwoColumnLayout" class="flex items-start space-x-3 flex-1">
          <img src="./images/4.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold">{{ t('taskMeeting.meetingTypes.sprintReview') }}</div>
            <div>{{ t('taskMeeting.meetingTypes.sprintReviewDesc') }}</div>
          </div>
        </div>

        <div class="flex items-start space-x-3 flex-1">
          <img src="./images/5.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold">{{ t('taskMeeting.meetingTypes.sprintRetrospective') }}</div>
            <div>{{ t('taskMeeting.meetingTypes.sprintRetrospectiveDesc') }}</div>
          </div>
        </div>

        <div class="flex items-start space-x-3 flex-1">
          <img src="./images/6.png" class="w-10 flex-shrink-0 transform-gpu translate-y-0.5">
          <div class="space-y-1 pr-10">
            <div class="text-3.5 font-semibold">{{ t('taskMeeting.meetingTypes.other') }}</div>
            <div>{{ t('taskMeeting.meetingTypes.otherDesc') }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
