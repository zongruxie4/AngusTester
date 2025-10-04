<script setup lang="ts">
import { computed, defineAsyncComponent, onMounted, ref, watch } from 'vue';
import { Spin, notification } from '@xcan-angus/vue-ui';
import { utils, appContext, ProjectPageQuery } from '@xcan-angus/infra';
import { useI18n } from 'vue-i18n';
import { func } from '@/api/tester';
import { FuncPlanStatus, FuncPlanPermission } from '@/enums/enums';

import { BasicProps } from '@/types/types';
import { ReviewDetail } from '../types';

const Introduce = defineAsyncComponent(() => import('@/views/test/review/list/Introduce.vue'));
const SearchPanel = defineAsyncComponent(() => import('@/views/test/review/list/SearchPanel.vue'));
const List = defineAsyncComponent(() => import('@/views/test/review/list/List.vue'));

// Props and Component Definitions
const props = withDefaults(defineProps<BasicProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  notify: undefined
});

const { t } = useI18n();

// Dependency Injection and Computed Properties
const isAdmin = computed(() => appContext.isAdmin());

// State Management
const loaded = ref(false);
const loading = ref(false);
const searchedFlag = ref(false);
const searchParams = ref({
  orderBy: undefined,
  orderSort: undefined,
  filters: []
});

const currentPage = ref(1);
const pageSize = ref(4);
const totalCount = ref(0);
const reviewList = ref<ReviewDetail[]>([]);
const permissionsMap = ref<Map<string, string[]>>(new Map());

// Configuration Constants
const pageSizeOptions = ['4', '10', '15', '20', '30'];

/**
 * Refreshes the review list by resetting pagination and reloading data
 */
const refresh = () => {
  currentPage.value = 1;
  permissionsMap.value.clear();
  loadReviewData();
};

/**
 * Handles search parameter changes and reloads data
 * @param searchData - The new search parameters
 */
const handleSearchChange = (searchData) => {
  searchParams.value = searchData;
  currentPage.value = 1;
  loadReviewData();
};

/**
 * Updates a specific review item in the list with fresh data
 * @param reviewId - The ID of the review to update
 * @param itemIndex - The index of the item in the list
 */
const updateReviewItem = async (reviewId: string, itemIndex: number) => {
  loading.value = true;
  const [error, response] = await func.getReviewDetail(reviewId);
  loading.value = false;
  if (error) {
    return;
  }

  if (response?.data) {
    reviewList.value[itemIndex] = response?.data;
  }
};

/**
 * Starts a review and updates the list
 * @param reviewData - The review data to start
 * @param itemIndex - The index of the item in the list
 */
const startReview = async (reviewData: ReviewDetail, itemIndex: number) => {
  loading.value = true;
  const reviewId = reviewData.id;
  const [error] = await func.startReview(reviewId);
  loading.value = false;
  if (error) {
    return;
  }

  notification.success(t('actions.tips.startSuccess'));
  await updateReviewItem(reviewId, itemIndex);
};

/**
 * Completes a review and updates the list
 * @param reviewData - The review data to complete
 * @param itemIndex - The index of the item in the list
 */
const completeReview = async (reviewData: ReviewDetail, itemIndex: number) => {
  loading.value = true;
  const reviewId = reviewData.id;
  const [error] = await func.endReview(reviewId);
  loading.value = false;
  if (error) {
    return;
  }

  notification.success(t('common.actions.completeSuccess'));
  await updateReviewItem(reviewId, itemIndex);
};

/**
 * Handles pagination changes and reloads data
 * @param newPageNo - The new page number
 * @param newPageSize - The new page size
 */
const handlePaginationChange = (newPageNo: number, newPageSize: number) => {
  currentPage.value = newPageNo;
  pageSize.value = newPageSize;
  loadReviewData();
};

/**
 * Loads review list data from the API
 */
const loadReviewData = async () => {
  loading.value = true;
  const queryParams: ProjectPageQuery = {
    projectId: props.projectId,
    pageNo: currentPage.value,
    pageSize: pageSize.value,
    ...searchParams.value
  };

  const [error, response] = await func.getReviewList(queryParams);
  loaded.value = true;
  loading.value = false;

  searchedFlag.value = !!(queryParams.filters?.length || queryParams.orderBy);
  if (error) {
    reviewList.value = [];
    return;
  }

  const responseData = response?.data;
  if (responseData) {
    totalCount.value = +responseData.total;

    const reviewItems = (responseData.list || [] as ReviewDetail[]);
    reviewList.value = reviewItems.map(processReviewItem);

    if (!isAdmin.value) {
      await loadPermissionsForReviews(reviewItems);
    }
  }
};

/**
 * Processes a review item to format data and add computed properties
 * @param item - The review item to process
 * @returns The processed review item
 */
const processReviewItem = (item: ReviewDetail): ReviewDetail => {
  if (item.progress?.completedRate) {
    item.progress.completedRate = item.progress.completedRate.replace(/(\d+\.\d{2})\d+/, '$1');
  }

  if (item.attachments?.length) {
    item.attachments = item.attachments.map(attachment => {
      return {
        ...attachment,
        id: utils.uuid()
      };
    });
  }

  if (item.members) {
    item.showMembers = item.members.slice(0, 5);
  }

  return item;
};

/**
 * Loads permissions for multiple reviews
 * @param reviewItems - Array of review items to load permissions for
 */
const loadPermissionsForReviews = async (reviewItems: ReviewDetail[]) => {
  for (let i = 0, len = reviewItems.length; i < len; i++) {
    const reviewId = reviewItems[i].id;
    if (!permissionsMap.value.get(reviewId)) {
      const [error, response] = await loadReviewPermissions(reviewId);
      if (!error) {
        const permissions = (response?.data?.permissions || []).map(permission => permission.value);
        permissionsMap.value.set(reviewId, permissions);
      }
    }
  }
};

/**
 * Loads permissions for a specific review
 * @param reviewId - The ID of the review to load permissions for
 * @returns Promise with error and response data
 */
const loadReviewPermissions = async (reviewId: string) => {
  const params = {
    admin: true
  };
  return await func.getReviewAuthByPlanId(reviewId, params);
};

/**
 * Resets the component state
 */
const resetComponent = () => {
  currentPage.value = 1;
  reviewList.value = [];
};

/**
 * Computed property that maps review IDs to their available dropdown permissions
 */
const dropdownPermissionsMap = computed(() => {
  const resultPermissionsMap = new Map<string, string[]>();
  if (reviewList.value.length) {
    const isAdminUser = isAdmin.value;
    const userPermissionsMap = permissionsMap.value;
    const reviews = reviewList.value;

    for (let i = 0, len = reviews.length; i < len; i++) {
      const { id, status } = reviews[i];
      const userPermissions: string[] = userPermissionsMap.get(id) || [];
      const availablePermissions: string[] = [];
      const reviewStatus = status.value;

      if ((isAdminUser || userPermissions.includes(FuncPlanPermission.MODIFY_PLAN)) &&
        [FuncPlanStatus.PENDING, FuncPlanStatus.IN_PROGRESS].includes(reviewStatus)) {
        availablePermissions.push('block');
      }
      if (isAdminUser || userPermissions.includes(FuncPlanPermission.DELETE_PLAN)) {
        availablePermissions.push('delete');
      }
      if (isAdminUser || userPermissions.includes(FuncPlanPermission.GRANT)) {
        availablePermissions.push('grant');
      }
      if (isAdminUser || userPermissions.includes(FuncPlanPermission.RESET_TEST_RESULT)) {
        availablePermissions.push('resetTestResult');
      }
      if (isAdminUser || userPermissions.includes(FuncPlanPermission.RESET_REVIEW_RESULT)) {
        availablePermissions.push('resetReviewResult');
      }
      if (isAdminUser || userPermissions.includes(FuncPlanPermission.EXPORT_CASE)) {
        availablePermissions.push('export');
      }
      resultPermissionsMap.set(id, availablePermissions);
    }
  }
  return resultPermissionsMap;
});

// Lifecycle and Watchers
onMounted(() => {
  watch(() => props.projectId, () => {
    resetComponent();
    loadReviewData();
  }, { immediate: true });

  watch(() => props.notify, (newNotifyValue) => {
    if (!newNotifyValue) {
      return;
    }
    loadReviewData();
  }, { immediate: false });
});
</script>

<template>
  <div class="flex flex-col h-full overflow-auto px-5 py-5 leading-5 text-3">
    <Introduce class="mb-7" />
    <div class="text-3.5 font-semibold mb-1">{{ t('testCaseReview.addedReviews') }}</div>
    <Spin :spinning="loading" class="flex-1 flex flex-col">
      <template v-if="loaded">
        <div v-if="!searchedFlag && reviewList.length === 0" class="flex-1 flex flex-col items-center justify-center">
          <img src="../../../../assets/images/nodata.png">
          <div class="flex items-center text-theme-sub-content text-3.5 leading-5 space-x-1">
            <span>{{ t('testCaseReview.noReviewsAdded') }}</span>
            <RouterLink class="router-link flex-1 truncate" :to="`/test#reviews?type=ADD`">
              {{ t('testCaseReview.actions.addReview') }}
            </RouterLink>
          </div>
        </div>

        <template v-else>
          <SearchPanel
            :loading="loading"
            @refresh="refresh"
            @change="handleSearchChange" />

          <List
            :reviewList="reviewList"
            :loading="loading"
            :totalCount="totalCount"
            :currentPage="currentPage"
            :pageSize="pageSize"
            :pageSizeOptions="pageSizeOptions"
            :permissionsMap="permissionsMap"
            :dropdownPermissionsMap="dropdownPermissionsMap"
            :isAdmin="isAdmin"
            @paginationChange="handlePaginationChange"
            @startReview="startReview"
            @completeReview="completeReview"
            @refresh="refresh" />
        </template>
      </template>
    </Spin>
  </div>
</template>

<style scoped>
</style>
