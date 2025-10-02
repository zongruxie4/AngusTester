<script setup lang="ts">
import { defineAsyncComponent, inject, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { ProjectPageQuery } from '@xcan-angus/infra';
import { modal, notification, Spin } from '@xcan-angus/vue-ui';
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
const SearchPanel = defineAsyncComponent(() => import('@/views/issue/meeting/list/SearchPanel.vue'));
const Introduce = defineAsyncComponent(() => import('@/views/issue/meeting/list/Introduce.vue'));
const List = defineAsyncComponent(() => import('@/views/issue/meeting/list/List.vue'));

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
const pageSize = ref(4);
const totalCount = ref(0);
const meetingList = ref<MeetingInfo[]>([]);
const permissionsCache = ref<Map<string, string[]>>(new Map());

// CONSTANTS
const pageSizeOptions = ['4', '10', '15', '20', '30'];

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
    content: t('actions.tips.confirmDelete', { name: meetingData.subject }),
    async onOk () {
      const meetingId = meetingData.id;
      const [error] = await task.deleteMeeting(meetingId);
      if (error) {
        return;
      }

      notification.success(t('actions.tips.deleteSuccess'));
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

    <div class="text-3.5 font-semibold mb-1">
      {{ t('meeting.addedMeetings') }}
    </div>
    <Spin :spinning="isLoading" class="flex-1 flex flex-col">
      <template v-if="isDataLoaded">
        <div
          v-if="!hasSearchFilters && meetingList.length === 0"
          class="flex-1 flex flex-col items-center justify-center">
          <img src="../../../../assets/images/nodata.png">
          <div class="flex items-center text-theme-sub-content text-3.5 leading-5 space-x-1">
            <span>{{ t('meeting.notAddedYet') }}</span>
            <RouterLink class="router-link flex-1 truncate" :to="`/issue#meeting?type=ADD`">
              {{ t('meeting.addMeeting') }}
            </RouterLink>
          </div>
        </div>

        <template v-else>
          <SearchPanel
            @change="handleSearchChange"
            @refresh="refreshMeetingList" />

          <List
            :meetingList="meetingList"
            :totalCount="totalCount"
            :currentPageNumber="currentPageNumber"
            :pageSize="pageSize"
            :pageSizeOptions="pageSizeOptions"
            @delete="handleMeetingDeletion"
            @pageChange="handlePaginationChange" />
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
