<script setup lang="ts">
import { defineProps, withDefaults } from 'vue';
import { useI18n } from 'vue-i18n';
import { Avatar, Button, Pagination } from 'ant-design-vue';
import { UserOutlined } from '@ant-design/icons-vue';
import { Icon, Image, NoData, Popover } from '@xcan-angus/vue-ui';
import type { MeetingInfo } from '../types';
import RichTextEditor from '@/components/richEditor/textContent/index.vue';
import { DATE_FORMAT } from '@/utils/constant';
import { TaskMeetingType } from '@/enums/enums';
import dayjs from 'dayjs';

withDefaults(defineProps<{
  meetingList: MeetingInfo[];
  totalCount: number;
  currentPageNumber: number;
  pageSize: number;
  pageSizeOptions: string[];
}>(), {
  meetingList: () => [],
  totalCount: 0,
  currentPageNumber: 1,
  pageSize: 4,
  pageSizeOptions: () => ['4', '10', '15', '20', '30']
});

const emit = defineEmits<{
  (e: 'delete', meeting: MeetingInfo): void;
  (e: 'pageChange', page: number, pageSize: number): void;
}>();

const { t } = useI18n();

const onDelete = (meeting: MeetingInfo) => {
  emit('delete', meeting);
};

const onPageChange = (page: number, pageSize: number) => {
  emit('pageChange', page, pageSize);
};

/**
 * Get meeting type style based on type value enum
 * @param typeValue - Meeting type value from enum
 * @returns CSS classes for meeting type container
 */
const getMeetingTypeStyle = (typeValue: string) => {
  switch (typeValue) {
    case TaskMeetingType.DAILY_STANDUP:
      return 'bg-blue-50 text-blue-700 border-blue-200 hover:bg-blue-100';
    case TaskMeetingType.PLANNING:
      return 'bg-green-50 text-green-700 border-green-200 hover:bg-green-100';
    case TaskMeetingType.REVIEW:
      return 'bg-purple-50 text-purple-700 border-purple-200 hover:bg-purple-100';
    case TaskMeetingType.RETROSPECTIVE:
      return 'bg-orange-50 text-orange-700 border-orange-200 hover:bg-orange-100';
    case TaskMeetingType.BACKLOG_REFINEMENT:
      return 'bg-cyan-50 text-cyan-700 border-cyan-200 hover:bg-cyan-100';
    case TaskMeetingType.OTHER:
      return 'bg-gray-50 text-gray-700 border-gray-200 hover:bg-gray-100';
    default:
      return 'bg-gray-50 text-gray-700 border-gray-200 hover:bg-gray-100';
  }
};

/**
 * Get meeting type dot style based on type value enum
 * @param typeValue - Meeting type value from enum
 * @returns CSS classes for meeting type dot
 */
const getMeetingTypeDotStyle = (typeValue: string) => {
  switch (typeValue) {
    case TaskMeetingType.DAILY_STANDUP:
      return 'bg-blue-500';
    case TaskMeetingType.PLANNING:
      return 'bg-green-500';
    case TaskMeetingType.REVIEW:
      return 'bg-purple-500';
    case TaskMeetingType.RETROSPECTIVE:
      return 'bg-orange-500';
    case TaskMeetingType.BACKLOG_REFINEMENT:
      return 'bg-cyan-500';
    case TaskMeetingType.OTHER:
      return 'bg-gray-500';
    default:
      return 'bg-gray-500';
  }
};
</script>

<template>
  <NoData v-if="meetingList.length === 0" class="flex-1" />

  <template v-else>
    <div
      v-for="item in meetingList"
      :key="item.id"
      class="mb-3.5 border border-theme-text-box rounded">
      <div class="px-3.5 py-2 flex items-center justify-between bg-theme-form-head w-full relative">
        <div class="truncate" style="width:35%;max-width: 360px;">
          <RouterLink
            class="router-link"
            :title="item.subject"
            :to="`/task#meeting?id=${item.id}`">
            {{ item.subject }}
          </RouterLink>
        </div>
      </div>

      <!-- Meeting information card -->
      <div class="px-4 py-3 bg-theme-bg-subtle/30 border-t border-theme-border-subtle">
        <div class="flex items-center justify-between">
          <!-- Left side: Moderator + Meeting type + Meeting time + Participants -->
          <div class="flex items-center space-x-16">
            <!-- Moderator -->
            <div class="flex items-center space-x-2">
              <div class="w-6 h-6 rounded-full overflow-hidden ring-1 ring-theme-border">
                <Image
                  class="w-full h-full"
                  :src="item.moderator.avatar"
                  type="avatar" />
              </div>
              <div class="flex flex-col">
                <span class="text-xs text-theme-sub-content">{{ t('taskMeeting.columns.moderatorLabel') }}</span>
                <span class="text-sm font-medium text-theme-content truncate max-w-24" :title="item.moderator.fullName">
                  {{ item.moderator.fullName }}
                </span>
              </div>
            </div>

            <!-- Meeting type -->
            <div class="flex items-center space-x-2">
              <div
                class="px-3 py-1.5 rounded-full text-xs font-semibold shadow-sm border transition-all duration-200 hover:shadow-md"
                :class="getMeetingTypeStyle(item.type?.value)">
                <div class="flex items-center space-x-1.5">
                  <div
                    class="w-2 h-2 rounded-full"
                    :class="getMeetingTypeDotStyle(item.type?.value)">
                  </div>
                  <span>{{ item.type?.message }}</span>
                </div>
              </div>
            </div>

            <!-- Meeting time -->
            <div class="flex items-center space-x-2">
              <div class="flex flex-col">
                <span class="text-xs text-theme-sub-content">{{ t('taskMeeting.columns.date') }}</span>
                <span class="text-sm font-medium text-theme-content">
                  {{ item.date ? dayjs(item.date).format(DATE_FORMAT) : '--' }}
                </span>
              </div>
            </div>

            <!-- Participants -->
            <div class="flex items-center space-x-2">
              <div class="flex flex-col">
                <span class="text-xs text-theme-sub-content">{{ t('taskMeeting.columns.participants') }}</span>
                <span class="text-sm font-medium text-theme-content">
                  {{ item.participants?.length || 0 }} {{ t('taskMeeting.columns.participantUnit') }}
                </span>
              </div>
              <div class="flex -space-x-1">
                <template v-if="item.participants?.length">
                  <div
                    v-for="participant in item.participants.slice(0, 8)"
                    :key="participant.id"
                    :title="participant.fullName"
                    class="w-6 h-6 rounded-full overflow-hidden ring-1 ring-white shadow-sm">
                    <Image
                      :src="participant.avatar"
                      type="avatar"
                      class="w-full h-full" />
                  </div>
                  <Popover
                    v-if="item.participants.length > 8"
                    placement="bottomLeft"
                    internal>
                    <template #title>
                      <span class="text-sm font-medium">{{ t('taskMeeting.columns.participants') }} ({{ item.participants.length }})</span>
                    </template>
                    <template #content>
                      <div class="grid grid-cols-5 gap-2 max-w-md">
                        <div
                          v-for="participant in item.participants"
                          :key="participant.id"
                          class="flex flex-col items-center space-y-1 p-2">
                          <div class="w-8 h-8 rounded-full overflow-hidden">
                            <Image
                              class="w-full h-full"
                              :src="participant.avatar"
                              type="avatar" />
                          </div>
                          <span class="text-xs text-theme-content text-center truncate w-full" :title="participant.fullName">{{ participant.fullName }}</span>
                        </div>
                      </div>
                    </template>
                    <div class="w-6 h-6 rounded-full bg-theme-primary/20 flex items-center justify-center text-xs font-bold text-theme-primary ring-1 ring-white shadow-sm cursor-pointer">
                      +{{ item.participants.length - 8 }}
                    </div>
                  </Popover>
                </template>
                <Avatar
                  v-else
                  size="small"
                  class="w-6 h-6">
                  <template #icon>
                    <UserOutlined />
                  </template>
                </Avatar>
              </div>
            </div>
          </div>

          <!-- Right side: Last modified info -->
          <div class="flex items-center">
            <!-- Last modified -->
            <div class="flex items-center space-x-2 text-xs text-theme-sub-content">
              <span>{{ t('common.lastModifiedBy') }}</span>
              <span class="text-theme-content font-medium truncate max-w-16" :title="item.lastModifiedByName">
                {{ item.lastModifiedByName }}
              </span>
              <span class="text-theme-sub-content">{{ item.lastModifiedDate }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Divider line -->
      <div class="border-t border-theme-border-subtle/50"></div>

      <div class="px-3.5 flex justify-between items-start text-3 my-2.5 relative">
        <div
          :title="item.contentText"
          class="truncate mr-8"
          style="max-width: 70%;">
          <RichTextEditor
            v-model:textValue="item.contentText"
            :value="item.content"
            :emptyText="t('common.noDescription')" />
        </div>
        <div class="flex space-x-3 items-center justify-between h-4 leading-5">
          <RouterLink class="flex items-center space-x-1" :to="`/task#meeting?id=${item.id}&type=edit`">
            <Icon icon="icon-shuxie" class="text-3.5" />
            <span>{{ t('common.edit') }}</span>
          </RouterLink>
          <Button
            type="text"
            size="small"
            @click="onDelete(item)">
            <Icon icon="icon-qingchu" />
            <span>{{ t('actions.delete') }}</span>
          </Button>
        </div>
      </div>
    </div>

    <Pagination
      v-if="totalCount > 4"
      :current="currentPageNumber"
      :pageSize="pageSize"
      :pageSizeOptions="pageSizeOptions"
      :total="totalCount"
      :hideOnSinglePage="false"
      showSizeChanger
      size="default"
      class="text-right"
      @change="onPageChange" />
  </template>
</template>

<style scoped>
.router-link,
.link {
  color: #1890ff;
  cursor: pointer;
}
</style>
