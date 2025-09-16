<script setup lang="ts">
import { defineAsyncComponent } from 'vue';
import { useI18n } from 'vue-i18n';
import { Colon, NoData } from '@xcan-angus/vue-ui';

const props = defineProps<{ meetings: any[] }>();

const { t } = useI18n();

const RichEditor = defineAsyncComponent(() => import('@/components/richEditor/index.vue'));
</script>

<template>
  <div>
    <div
      v-for="item in props.meetings"
      :key="item.id"
      class="text-3 leading-5 space-y-2.5 py-2.5 px-3.5 mb-3.5 last:mb-0 meeting-container">
      <div class="text-theme-title font-medium">{{ (item as any).subject }}</div>

      <div class="flex items-start space-x-5">
        <div class="w-1/2 flex items-start">
          <div class="w-15.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('taskSprint.meeting.type') }}</span>
            <Colon class="w-1" />
          </div>

          <div class="whitespace-pre-wrap break-words break-all">{{ item.type?.message }}</div>
        </div>

        <div class="w-1/2 flex items-start">
          <div class="w-15.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('taskSprint.meeting.date') }}</span>
            <Colon class="w-1" />
          </div>

          <div class="whitespace-pre-wrap break-words break-all">{{ item.date }}</div>
        </div>
      </div>

      <div class="flex items-start space-x-5">
        <div class="w-1/2 flex items-start">
          <div class="w-15.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('taskSprint.meeting.time') }}</span>
            <Colon class="w-1" />
          </div>

          <div class="text-3 whitespace-nowrap">
            <span>{{ item.startTime }}</span>
            <span class="mx-2">-</span>
            <span>{{ item.endTime }}</span>
          </div>
        </div>

        <div class="w-1/2 flex items-start">
          <div class="w-15.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('taskSprint.meeting.location') }}</span>
            <Colon class="w-1" />
          </div>

          <div class="whitespace-pre-wrap break-words break-all">{{ item.location }}</div>
        </div>
      </div>

      <div class="flex items-start space-x-5">
        <div class="w-1/2 flex items-start">
          <div class="w-15.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('taskSprint.meeting.moderator') }}</span>
            <Colon class="w-1" />
          </div>

        <div class="whitespace-pre-wrap break-words break-all">{{ item.moderatorName }}</div>
        </div>

        <div class="w-1/2 flex items-start">
          <div class="w-15.5 flex items-center whitespace-nowrap flex-shrink-0">
            <span>{{ t('taskSprint.meeting.participants') }}</span>
            <Colon class="w-1" />
          </div>

          <div class="whitespace-pre-wrap break-words break-all">{{ item.participantNames }}</div>
        </div>
      </div>

      <div class="flex items-start">
        <div class="w-15.5 flex items-center whitespace-nowrap flex-shrink-0">
          <span>{{ t('taskSprint.meeting.content') }}</span>
          <Colon class="w-1" />
        </div>
        <RichEditor :value="item.content" mode="view" />
      </div>
    </div>

    <NoData v-if="!props.meetings?.length" size="small" class="mt-20" />
  </div>
</template>

<style scoped>
.meeting-container {
  border: 1px solid var(--border-text-box);
  border-radius: 4px;
  background-color: #fafafa;
}
</style>


