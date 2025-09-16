<script setup lang="ts">
import { defineAsyncComponent, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Colon, NoData } from '@xcan-angus/vue-ui';

const props = defineProps<{ meetings: any[] }>();

const { t } = useI18n();

const RichEditor = defineAsyncComponent(() => import('@/components/richEditor/index.vue'));

// Expanded rows state: default expand the first meeting
const expandedIds = ref<Set<string>>(new Set());

const expandFirstIfNeeded = () => {
  if (!props.meetings?.length) {
    expandedIds.value.clear();
    return;
  }
  // If nothing expanded, expand the first item
  if (expandedIds.value.size === 0) {
    expandedIds.value.add(props.meetings[0].id);
  }
};

const isExpanded = (id: string) => expandedIds.value.has(id);
const toggleExpand = (id: string) => {
  if (expandedIds.value.has(id)) {
    expandedIds.value.delete(id);
  } else {
    expandedIds.value.add(id);
  }
  // trigger reactivity for Set
  expandedIds.value = new Set(expandedIds.value);
};

watch(() => props.meetings, () => {
  expandFirstIfNeeded();
}, { immediate: true });
</script>

<template>
  <div>
    <div
      v-for="item in props.meetings"
      :key="item.id"
      class="bg-white rounded-lg shadow-sm p-6 mb-6 meeting-container">
      <!-- Meeting subject & toggle -->
      <div class="flex items-center justify-between mb-6 pb-4 border-b border-gray-200">
        <div class="text-theme-title font-medium text-xl">
          {{ (item as any).subject }}
        </div>
        <button
          class="text-3 text-blue-500 hover:text-blue-600 hover:underline"
          @click="toggleExpand(item.id)">
          {{ isExpanded(item.id) ? t('actions.collapse') : t('actions.expand') }}
        </button>
      </div>

      <div v-show="isExpanded(item.id)" class="grid grid-cols-1 md:grid-cols-2 gap-6">
        <!-- Meeting type and date -->
        <div class="space-y-4">
          <div class="flex items-start">
            <div class="w-24 flex items-center whitespace-nowrap flex-shrink-0 text-gray-900 font-medium text-3 text-right">
              <span>{{ t('taskSprint.meeting.type') }}</span>
              <Colon class="w-1 mx-1" />
            </div>

            <div class="whitespace-pre-wrap break-words break-all text-gray-900 text-3">
              {{ item.type?.message }}
            </div>
          </div>

          <div class="flex items-start">
            <div class="w-24 flex items-center whitespace-nowrap flex-shrink-0 text-gray-900 font-medium text-3 text-right">
              <span>{{ t('taskSprint.meeting.date') }}</span>
              <Colon class="w-1 mx-1" />
            </div>

            <div class="whitespace-pre-wrap break-words break-all text-gray-900 text-3">
              {{ item.date }}
            </div>
          </div>
        </div>

        <!-- Meeting time and location -->
        <div class="space-y-4">
          <div class="flex items-start">
            <div class="w-24 flex items-center whitespace-nowrap flex-shrink-0 text-gray-900 font-medium text-3 text-right">
              <span>{{ t('taskSprint.meeting.time') }}</span>
              <Colon class="w-1 mx-1" />
            </div>

            <div class="text-3 whitespace-nowrap text-gray-900">
              <span>{{ item.startTime }}</span>
              <span class="mx-2 text-gray-400">-</span>
              <span>{{ item.endTime }}</span>
            </div>
          </div>

          <div class="flex items-start">
            <div class="w-24 flex items-center whitespace-nowrap flex-shrink-0 text-gray-900 font-medium text-3 text-right">
              <span>{{ t('taskSprint.meeting.location') }}</span>
              <Colon class="w-1 mx-1" />
            </div>

            <div class="whitespace-pre-wrap break-words break-all text-gray-900 text-3">
              {{ item.location || '--' }}
            </div>
          </div>
        </div>
      </div>

      <div v-show="isExpanded(item.id)" class="grid grid-cols-1 md:grid-cols-2 gap-4 mt-4">
        <!-- Moderator -->
        <div class="space-y-4">
          <div class="flex items-start">
            <div class="w-24 flex items-center whitespace-nowrap flex-shrink-0 text-gray-900 font-medium text-3 text-right">
              <span>{{ t('taskSprint.meeting.moderator') }}</span>
              <Colon class="w-1 mx-1" />
            </div>

            <div class="whitespace-pre-wrap break-words break-all text-gray-900 text-3">
              {{ item.moderatorName || '--' }}
            </div>
          </div>
        </div>

        <!-- Participants -->
        <div class="space-y-4">
          <div class="flex items-start">
            <div class="w-24 flex items-center whitespace-nowrap flex-shrink-0 text-gray-900 font-medium text-3 text-right">
              <span>{{ t('taskSprint.meeting.participants') }}</span>
              <Colon class="w-1 mx-1" />
            </div>

            <div class="whitespace-pre-wrap break-words break-all text-gray-900 text-3">
              {{ item.participantNames || '--' }}
            </div>
          </div>
        </div>
      </div>

      <!-- Meeting content -->
      <div v-show="isExpanded(item.id)" class="mt-6 pt-3 border-t border-gray-200">
        <div class="mt-2">
          <RichEditor :value="item.content" mode="view" />
        </div>
      </div>
    </div>

    <NoData v-if="!props.meetings?.length" size="small" class="mt-20" />
  </div>
</template>

<style scoped>
.meeting-container {
  border: 1px solid var(--border-text-box);
}
</style>


