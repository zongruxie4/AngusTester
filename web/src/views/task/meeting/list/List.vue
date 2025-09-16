<script setup lang="ts">
import { defineProps, withDefaults } from 'vue';
import { useI18n } from 'vue-i18n';
import { Avatar, Button, Pagination } from 'ant-design-vue';
import { UserOutlined } from '@ant-design/icons-vue';
import { Colon, Icon, Image, NoData, Popover } from '@xcan-angus/vue-ui';
import type { MeetingInfo } from '../types';
import RichTextEditor from "@/components/richEditor/textContent/index.vue";

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

      <div class="px-3.5 flex mt-3 justify-between text-3 text-theme-sub-content">
        <div class="flex leading-5">
          <div class="flex mr-10 items-center">
            <div class="mr-2">
              <span>{{ t('taskMeeting.columns.moderatorLabel') }}</span>
              <Colon />
            </div>
            <div class="w-5 h-5 rounded-full mr-1 overflow-hidden">
              <Image
                class="w-full"
                :src="item.moderator.avatar"
                type="avatar" />
            </div>
            <div
              class="text-theme-content truncate"
              :title="item.moderator.fullName"
              style="max-width: 200px;">
              {{ item.moderator.fullName }}
            </div>
          </div>

          <div class="flex items-center">
            <div class="mr-2">
              <span>{{ t('taskMeeting.columns.participantsLabel') }}</span>
              <Colon />
            </div>

            <template v-if="item.participants?.length">
              <div
                v-for="participant in item.participants"
                :key="participant.id"
                :title="participant.fullName"
                class="w-5 h-5 mr-2 overflow-hidden rounded-full">
                <Image
                  :src="participant.avatar"
                  type="avatar"
                  class="w-full" />
              </div>

              <Popover
                v-if="item.participants.length > 5"
                placement="bottomLeft"
                internal>
                <template #title>
                  <span class="text-3">所有成员</span>
                </template>
                <template #content>
                  <div class="flex flex-wrap" style="max-width: 700px;">
                    <div
                      v-for="participant in item.participants"
                      :key="participant.id"
                      class="flex text-3 leading-5 mr-2 mb-2">
                      <div class="w-5 h-5 rounded-full mr-1 flex-none overflow-hidden">
                        <Image
                          class="w-full"
                          :src="participant.avatar"
                          type="avatar" />
                      </div>
                      <span class="flex-1 truncate">{{ participant.fullName }}</span>
                    </div>
                  </div>
                </template>
                <a class="text-theme-special text-5">...</a>
              </Popover>
            </template>

            <Avatar
              v-else
              size="small"
              style="font-size: 12px;"
              class="w-5 h-5 leading-5">
              <template #icon>
                <UserOutlined />
              </template>
            </Avatar>
          </div>
        </div>

        <div class="ml-8 text-theme-content font-semibold">
          {{ t('taskMeeting.columns.participantsCount', {count: item.participants?.length || 0}) }}
        </div>
      </div>

      <div class="px-3.5 flex flex-start justify-between text-3 text-theme-sub-content">
        <div class="flex flex-wrap">
          <div class="flex mt-3">
            <div class="mr-2 whitespace-nowrap">
              <span>{{ t('taskMeeting.columns.id') }}</span>
              <Colon />
            </div>
            <div class="text-theme-content">{{ item.id || "--" }}</div>
          </div>

          <div class="flex mt-3 ml-8">
            <div class="mr-2 whitespace-nowrap">
              <span>{{ t('taskMeeting.columns.type') }}</span>
              <Colon />
            </div>
            <div class="text-theme-content">{{ item.type?.message }}</div>
          </div>
        </div>

        <div class="flex ml-8 mt-3">
          <div
            class="truncate text-theme-content"
            style="max-width: 100px;"
            :title="item.lastModifiedByName">
            {{ item.lastModifiedByName }}
          </div>
          <div class="mx-2 whitespace-nowrap">{{ t('taskMeeting.columns.lastModifiedBy') }}</div>
          <div class="whitespace-nowrap text-theme-content">
            {{ item.lastModifiedDate }}
          </div>
        </div>
      </div>

      <div class="px-3.5 flex justify-between items-start text-3 my-2.5 relative">
        <div
          :title="item.content"
          class="truncate mr-8"
          style="max-width: 70%;">
          <RichTextEditor
            :value="item.content"
            :emptyText="t('common.noDescription')" />
        </div>
        <div class="flex space-x-3 items-center justify-between h-4 leading-5">
          <RouterLink class="flex items-center space-x-1" :to="`/task#meeting?id=${item.id}&type=edit`">
            <Icon icon="icon-shuxie" class="text-3.5" />
            <span>{{ t('actions.edit') }}</span>
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


