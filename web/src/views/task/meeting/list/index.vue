<script setup lang="ts">
import { defineAsyncComponent, inject, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Avatar, Button, Pagination } from 'ant-design-vue';
import { UserOutlined } from '@ant-design/icons-vue';
import { ProjectPageQuery } from '@xcan-angus/infra';
import { Colon, Icon, Image, modal, NoData, notification, Popover, Spin } from '@xcan-angus/vue-ui';
import { task } from '@/api/tester';
import { MeetingInfo } from '../types';
import { BasicProps } from '@/types/types';

// COMPONENT PROPS
const props = withDefaults(defineProps<BasicProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  notify: undefined
});

// COMPOSABLES & INJECTIONS
const { t } = useI18n();
const deleteTabPane = inject<(keys: string[]) => void>('deleteTabPane', () => ({}));

// ASYNC COMPONENTS
const SearchPanel = defineAsyncComponent(() => import('@/views/task/meeting/list/SearchPanel.vue'));
const Introduce = defineAsyncComponent(() => import('@/views/task/meeting/list/Introduce.vue'));

// REACTIVE STATE
const isDataLoaded = ref(false);
const isLoading = ref(false);
const hasSearchFilters = ref(false);

const searchParameters = ref({
  orderBy: undefined,
  orderSort: undefined,
  filters: []
});

const currentPageNumber = ref(1);
const pageSize = ref(5);
const totalCount = ref(0);
const meetingList = ref<MeetingInfo[]>([]);
const permissionsCache = ref<Map<string, string[]>>(new Map());

// CONSTANTS
const pageSizeOptions = ['5', '10', '15', '20', '30'];

/**
 * Refreshes the meeting list by resetting pagination and clearing cache
 */
const refreshMeetingList = () => {
  currentPageNumber.value = 1;
  permissionsCache.value.clear();
  fetchMeetingList();
};

/**
 * Handles search parameter changes and triggers data reload
 * @param searchData - Search parameters including filters, orderBy, and orderSort
 */
const handleSearchChange = (searchData: any) => {
  currentPageNumber.value = 1;
  searchParameters.value = searchData;
  fetchMeetingList();
};

/**
 * Fetches meeting list data from API with current parameters
 */
const fetchMeetingList = async () => {
  isLoading.value = true;
  const queryParams: ProjectPageQuery = {
    projectId: props.projectId,
    pageNo: currentPageNumber.value,
    pageSize: pageSize.value,
    ...searchParameters.value
  };

  const [error, response] = await task.getMeetingList(queryParams);
  isDataLoaded.value = true;
  isLoading.value = false;

  // Determine if search filters are applied
  hasSearchFilters.value = !!(queryParams.filters?.length || queryParams.orderBy);

  if (error) {
    totalCount.value = 0;
    meetingList.value = [];
    return;
  }

  const responseData = response?.data || { total: 0, list: [] };
  if (responseData) {
    totalCount.value = +responseData.total;
    meetingList.value = (responseData.list || [] as MeetingInfo[]);
  }
};

/**
 * Handles meeting deletion with confirmation modal
 * @param meetingData - Meeting information to be deleted
 */
const handleMeetingDeletion = async (meetingData: MeetingInfo) => {
  modal.confirm({
    content: t('taskMeeting.confirmDelete', { name: meetingData.subject }),
    async onOk () {
      const meetingId = meetingData.id;
      const [error] = await task.deleteMeeting(meetingId);
      if (error) {
        return;
      }

      notification.success(t('taskMeeting.deleteSuccess'));
      await fetchMeetingList();
      deleteTabPane([meetingId]);
    }
  });
};

/**
 * Handles pagination changes and reloads data
 * @param newPageNumber - New page number
 * @param newPageSize - New page size
 */
const handlePaginationChange = (newPageNumber: number, newPageSize: number) => {
  currentPageNumber.value = newPageNumber;
  pageSize.value = newPageSize;
  fetchMeetingList();
};

/**
 * Component mounted lifecycle hook
 * Sets up watchers for projectId and notify prop changes
 */
onMounted(() => {
  // Watch for project changes and reload data
  watch(() => props.projectId, () => {
    currentPageNumber.value = 1;
    fetchMeetingList();
  }, { immediate: true });

  // Watch for notification changes and reload data
  watch(() => props.notify, (newNotifyValue) => {
    if (!newNotifyValue) {
      return;
    }

    fetchMeetingList();
  }, { immediate: false });
});
</script>

<template>
  <div class="flex flex-col h-full overflow-auto px-5 py-5 leading-5 text-3">
    <div class="flex space-x-2">
      <Introduce class="mb-7 flex-1" />
    </div>

    <div class="text-3.5 font-semibold mb-1">{{ t('taskMeeting.addedMeetings') }}</div>
    <Spin :spinning="isLoading" class="flex-1 flex flex-col">
      <template v-if="isDataLoaded">
        <div v-if="!hasSearchFilters && meetingList.length === 0" class="flex-1 flex flex-col items-center justify-center">
          <img src="../../../../assets/images/nodata.png">
          <div class="flex items-center text-theme-sub-content text-3.5 leading-5 space-x-1">
            <span>{{ t('taskMeeting.notAddedYet') }}</span>
            <RouterLink class="router-link flex-1 truncate" :to="`/task#meeting?type=ADD`">
              {{ t('taskMeeting.addMeeting') }}
            </RouterLink>
          </div>
        </div>

        <template v-else>
          <SearchPanel
            @change="handleSearchChange"
            @refresh="refreshMeetingList" />
          <NoData v-if="meetingList.length === 0" class="flex-1" />

          <template v-else>
            <div
              v-for="meetingItem in meetingList"
              :key="meetingItem.id"
              class="mb-3.5 border border-theme-text-box rounded">
              <div class="px-3.5 py-2 flex items-center justify-between bg-theme-form-head w-full relative">
                <div class="truncate" style="width:35%;max-width: 360px;">
                  <RouterLink
                    class="router-link"
                    :title="meetingItem.subject"
                    :to="`/task#meeting?id=${meetingItem.id}`">
                    {{ meetingItem.subject }}
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
                        :src="meetingItem.moderator.avatar"
                        type="avatar" />
                    </div>
                    <div
                      class="text-theme-content truncate"
                      :title="meetingItem.moderator.fullName"
                      style="max-width: 200px;">
                      {{ meetingItem.moderator.fullName }}
                    </div>
                  </div>

                  <div class="flex items-center">
                    <div class="mr-2">
                      <span>{{ t('taskMeeting.columns.participantsLabel') }}</span>
                      <Colon />
                    </div>

                    <template v-if="meetingItem.participants?.length">
                      <div
                        v-for="participant in meetingItem.participants"
                        :key="participant.id"
                        :title="participant.fullName"
                        class="w-5 h-5 mr-2 overflow-hidden rounded-full">
                        <Image
                          :src="participant.avatar"
                          type="avatar"
                          class="w-full" />
                      </div>

                      <Popover
                        v-if="meetingItem.participants.length > 5"
                        placement="bottomLeft"
                        internal>
                        <template #title>
                          <span class="text-3">所有成员</span>
                        </template>
                        <template #content>
                          <div class="flex flex-wrap" style="max-width: 700px;">
                            <div
                              v-for="participant in meetingItem.participants"
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

                <div class="ml-8 text-theme-content">
                  {{ t('taskMeeting.columns.participantsCount', { count: meetingItem.participants?.length || 0 }) }}
                </div>
              </div>

              <div class="px-3.5 flex flex-start justify-between text-3 text-theme-sub-content">
                <div class="flex flex-wrap">
                  <div class="flex mt-3">
                    <div class="mr-2 whitespace-nowrap">
                      <span>{{ t('taskMeeting.columns.id') }}</span>
                      <Colon />
                    </div>
                    <div class="text-theme-content">{{ meetingItem.id || "--" }}</div>
                  </div>

                  <div class="flex mt-3 ml-8">
                    <div class="mr-2 whitespace-nowrap">
                      <span>{{ t('taskMeeting.columns.type') }}</span>
                      <Colon />
                    </div>
                    <div class="text-theme-content">{{ meetingItem.type?.message }}</div>
                  </div>
                </div>

                <div class="flex ml-8 mt-3">
                  <div
                    class="truncate text-theme-content"
                    style="max-width: 100px;"
                    :title="meetingItem.lastModifiedByName">
                    {{ meetingItem.lastModifiedByName }}
                  </div>
                  <div class="mx-2 whitespace-nowrap">{{ t('taskMeeting.columns.lastModifiedBy') }}</div>
                  <div class="whitespace-nowrap text-theme-content">
                    {{ meetingItem.lastModifiedDate }}
                  </div>
                </div>
              </div>

              <div class="px-3.5 flex justify-between items-start text-3 my-2.5 relative">
                <div
                  :title="meetingItem.description"
                  class="truncate mr-8"
                  style="max-width: 70%;">
                  {{ meetingItem.otherInformation }}
                </div>
                <div class="flex space-x-3 items-center justify-between h-4 leading-5">
                  <RouterLink class="flex items-center space-x-1" :to="`/task#meeting?id=${meetingItem.id}&type=edit`">
                    <Icon icon="icon-shuxie" class="text-3.5" />
                    <span>{{ t('actions.edit') }}</span>
                  </RouterLink>
                  <Button
                    type="text"
                    size="small"
                    @click="handleMeetingDeletion(meetingItem)">
                    <Icon icon="icon-qingchu" />
                    {{ t('actions.delete') }}
                  </Button>
                </div>
              </div>
            </div>

            <Pagination
              v-if="totalCount > 5"
              :current="currentPageNumber"
              :pageSize="pageSize"
              :pageSizeOptions="pageSizeOptions"
              :total="totalCount"
              :hideOnSinglePage="false"
              showSizeChanger
              size="default"
              class="text-right"
              @change="handlePaginationChange" />
          </template>
        </template>
      </template>
    </Spin>
  </div>
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

:deep(.ant-progress-outer) {
  width: 100px;
}

.router-link,
.link {
  color: #1890ff;
  cursor: pointer;
}

.link:hover {
  border-radius: 4px;
  background-color: rgba(239, 240, 243, 100%);
}

:deep(.ant-popover-inner-content) {
  padding: 8px 4px;
}
</style>
