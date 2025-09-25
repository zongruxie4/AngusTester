<script setup lang="ts">
import { defineAsyncComponent, inject } from 'vue';
import { Avatar, Button, Pagination, Popover, Progress } from 'ant-design-vue';
import { UserOutlined } from '@ant-design/icons-vue';
import { Colon, Dropdown, Icon, Image, modal, NoData, notification } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import { func } from '@/api/tester';
import { FuncPlanStatus, FuncPlanPermission } from '@/enums/enums';
import { ReviewDetail } from '../types';

const RichText = defineAsyncComponent(() => import('@/components/richEditor/textContent/index.vue'));

// Props definition
interface Props {
  reviewList: ReviewDetail[];
  loading: boolean;
  totalCount: number;
  currentPage: number;
  pageSize: number;
  pageSizeOptions: string[];
  permissionsMap: Map<string, string[]>;
  dropdownPermissionsMap: Map<string, string[]>;
  isAdmin: boolean;
}

const props = defineProps<Props>();

// Emits definition
const emit = defineEmits<{
  paginationChange: [newPageNo: number, newPageSize: number];
  startReview: [reviewData: ReviewDetail, itemIndex: number];
  completeReview: [reviewData: ReviewDetail, itemIndex: number];
  dropdownAction: [reviewData: ReviewDetail, itemIndex: number, actionKey: string];
  refresh: [];
}>();

const { t } = useI18n();

// Dependency injection
const deleteTabPane = inject<(keys: string[]) => void>('deleteTabPane', () => ({}));

/**
 * Get review status style based on status value
 * @param statusValue - Review status value
 * @returns CSS classes for review status container
 */
const getReviewStatusStyle = (statusValue: string) => {
  switch (statusValue) {
    case FuncPlanStatus.PENDING:
      return 'bg-gray-50 text-gray-700 border-gray-200 hover:bg-gray-100';
    case FuncPlanStatus.IN_PROGRESS:
      return 'bg-blue-50 text-blue-700 border-blue-200 hover:bg-blue-100';
    case FuncPlanStatus.COMPLETED:
      return 'bg-green-50 text-green-700 border-green-200 hover:bg-green-100';
    case FuncPlanStatus.BLOCKED:
      return 'bg-red-50 text-red-700 border-red-200 hover:bg-red-100';
    default:
      return 'bg-gray-50 text-gray-700 border-gray-200 hover:bg-gray-100';
  }
};

/**
 * Get review status dot style based on status value
 * @param statusValue - Review status value
 * @returns CSS classes for review status dot
 */
const getReviewStatusDotStyle = (statusValue: string) => {
  switch (statusValue) {
    case FuncPlanStatus.PENDING:
      return 'bg-gray-500';
    case FuncPlanStatus.IN_PROGRESS:
      return 'bg-blue-500';
    case FuncPlanStatus.COMPLETED:
      return 'bg-green-500';
    case FuncPlanStatus.BLOCKED:
      return 'bg-red-500';
    default:
      return 'bg-gray-500';
  }
};

/**
 * Starts a review and emits the event to parent
 * @param reviewData - The review data to start
 * @param itemIndex - The index of the item in the list
 */
const startReview = async (reviewData: ReviewDetail, itemIndex: number) => {
  emit('startReview', reviewData, itemIndex);
};

/**
 * Completes a review and emits the event to parent
 * @param reviewData - The review data to complete
 * @param itemIndex - The index of the item in the list
 */
const completeReview = async (reviewData: ReviewDetail, itemIndex: number) => {
  emit('completeReview', reviewData, itemIndex);
};

/**
 * Deletes a review with confirmation
 * @param reviewData - The review data to delete
 */
const deleteReview = async (reviewData: ReviewDetail) => {
  modal.confirm({
    content: t('caseReview.list.confirmDeleteReview', { name: reviewData.name }),
    async onOk () {
      const reviewId = reviewData.id;
      const [error] = await func.deleteReview(reviewId);
      if (error) {
        return;
      }

      notification.success(t('caseReview.list.reviewDeletedSuccess'));
      emit('refresh');

      deleteTabPane([reviewId]);
    }
  });
};

/**
 * Clones a review and emits refresh event
 * @param reviewData - The review data to clone
 */
const cloneReview = async (reviewData: ReviewDetail) => {
  const [error] = await func.cloneReview(reviewData.id);
  if (error) {
    return;
  }

  notification.success(t('caseReview.list.reviewClonedSuccess'));
  emit('refresh');
};

/**
 * Handles dropdown menu item clicks
 * @param reviewData - The review data
 * @param itemIndex - The index of the item in the list
 * @param actionKey - The action key from the dropdown
 */
const handleDropdownAction = (
  reviewData: ReviewDetail,
  itemIndex: number,
  actionKey: 'clone' | 'block' | 'delete' | 'export' | 'grant' | 'resetTestResult' | 'resetReviewResult' | 'viewBurnDown' | 'viewProgress'
) => {
  switch (actionKey) {
    case 'delete':
      deleteReview(reviewData);
      break;
    case 'clone':
      cloneReview(reviewData);
      break;
    default:
      emit('dropdownAction', reviewData, itemIndex, actionKey);
      break;
  }
};

/**
 * Handles pagination changes and emits the event to parent
 * @param newPageNo - The new page number
 * @param newPageSize - The new page size
 */
const handlePaginationChange = (newPageNo: number, newPageSize: number) => {
  emit('paginationChange', newPageNo, newPageSize);
};

// Configuration Constants
const dropdownMenuItems = [
  {
    key: 'delete',
    icon: 'icon-qingchu',
    name: t('actions.delete'),
    permission: 'delete'
  },
  {
    key: 'clone',
    icon: 'icon-fuzhi',
    name: t('actions.clone'),
    noAuth: true,
    permission: 'clone'
  }
];
</script>
<template>
  <div>
    <NoData v-if="props.reviewList.length === 0" class="flex-1 mt-20" />

    <template v-else>
      <div
        v-for="(item, index) in props.reviewList"
        :key="item.id"
        class="mb-3.5 border border-theme-text-box rounded">
        <div class="px-3.5 py-2 flex items-center justify-between bg-theme-form-head w-full relative">
          <div class="truncate" style="width:35%;max-width: 360px;">
            <RouterLink
              class="router-link flex-1 truncate"
              :title="item.name"
              :to="`/function#reviews?id=${item.id}`">
              {{ item.name }}
            </RouterLink>
          </div>

          <div class="flex items-center">
            <div
              class="text-3 leading-4 flex items-center flex-none whitespace-nowrap mr-3.5">
              <div class="h-1.5 w-1.5 rounded-full mr-1" :class="item.status?.value"></div>
              <div>{{ item.status?.message }}</div>
            </div>
            <Progress :percent="+item.progress?.completedRate" style="width:150px;" />
          </div>
        </div>

        <!-- Review information card -->
        <div class="px-4 py-3 bg-theme-bg-subtle/30 border-t border-theme-border-subtle">
          <div class="flex items-center justify-between">
            <!-- Left side: Owner + Status + Case count + Participants -->
            <div class="flex items-center space-x-16">
              <!-- Owner -->
              <div class="flex items-center space-x-2">
                <div class="w-6 h-6 rounded-full overflow-hidden ring-1 ring-theme-border">
                  <Image
                    class="w-full h-full"
                    :src="item.ownerAvatar"
                    type="avatar" />
                </div>
                <div class="flex flex-col">
                  <span class="text-xs text-theme-sub-content">{{ t('caseReview.list.owner') }}</span>
                  <span class="text-sm font-medium text-theme-content truncate max-w-24" :title="item.ownerName">
                    {{ item.ownerName }}
                  </span>
                </div>
              </div>

              <!-- Review status -->
              <div class="flex items-center space-x-2">
                <div
                  class="px-3 py-1.5 rounded-full text-xs font-semibold shadow-sm border transition-all duration-200 hover:shadow-md"
                  :class="getReviewStatusStyle(item.status?.value)">
                  <div class="flex items-center space-x-1.5">
                    <div
                      class="w-2 h-2 rounded-full"
                      :class="getReviewStatusDotStyle(item.status?.value)">
                    </div>
                    <span>{{ item.status?.message }}</span>
                  </div>
                </div>
              </div>

              <!-- Case count -->
              <div class="flex items-center space-x-2">
                <div class="flex flex-col">
                  <span class="text-xs text-theme-sub-content">{{ t('caseReview.list.totalCases') }}</span>
                  <span class="text-sm font-medium text-theme-content">
                    {{ item.caseNum }}
                  </span>
                </div>
              </div>

              <!-- Participants -->
              <div class="flex items-center space-x-2">
                <div class="flex flex-col">
                  <span class="text-xs text-theme-sub-content">{{ t('caseReview.list.participants') }}</span>
                  <span class="text-sm font-medium text-theme-content">
                    {{ item.participants?.length || 0 }} 人
                  </span>
                </div>
                <div class="flex -space-x-1">
                  <template v-if="item.participants?.length">
                    <div
                      v-for="user in item.participants.slice(0, 8)"
                      :key="user.id"
                      :title="user.fullName"
                      class="w-6 h-6 rounded-full overflow-hidden ring-1 ring-white shadow-sm">
                      <Image
                        :src="user.avatar"
                        type="avatar"
                        class="w-full h-full" />
                    </div>
                    <Popover
                      v-if="item.participants.length > 8"
                      placement="bottomLeft"
                      internal>
                      <template #title>
                        <span class="text-sm font-medium">{{ t('caseReview.list.participants') }} ({{ item.participants.length }})</span>
                      </template>
                      <template #content>
                        <div class="grid grid-cols-5 gap-2 max-w-md">
                          <div
                            v-for="_user in item.participants"
                            :key="_user.id"
                            class="flex flex-col items-center space-y-1 p-2">
                            <div class="w-8 h-8 rounded-full overflow-hidden">
                              <Image
                                class="w-full h-full"
                                :src="_user.avatar"
                                type="avatar" />
                            </div>
                            <span class="text-xs text-theme-content text-center truncate w-full" :title="_user.fullName">{{ _user.fullName }}</span>
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
              <div class="flex items-center space-x-2 text-xs text-theme-sub-content">
                <span class="text-theme-content font-medium truncate max-w-16" :title="item.lastModifiedByName">
                  {{ item.lastModifiedByName }}
                </span>
                <span>{{ t('caseReview.list.modifiedBy') }}</span>
                <span class="text-theme-sub-content">{{ item.lastModifiedDate }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Divider line -->
        <div class="border-t border-theme-border-subtle/50"></div>

        <!-- Second row: ID/Plan + Description + Action buttons -->
        <div class="px-4 py-2 bg-theme-bg-subtle/10">
          <div class="flex flex-col">
            <!-- Top sub-row: ID + Test Plan -->
            <div class="flex text-3 text-theme-sub-content">
              <div class="flex items-center mr-8 mt-1">
                <span>{{ t('common.id') }}</span>
                <Colon />
                <span class="text-theme-content ml-2">{{ item.id || "--" }}</span>
              </div>
              <div class="flex items-center mr-8 mt-1">
                <span>{{ t('caseReview.list.testPlan') }}</span>
                <Colon />
                <span class="text-theme-content ml-2 truncate max-w-48" :title="item.planName">{{ item.planName || "--" }}</span>
              </div>
            </div>

            <!-- Bottom sub-row: Description + Action buttons in the same line -->
            <div class="flex justify-between items-start text-3 mt-2 relative">
              <div
                class="truncate mr-8"
                :title="item.description"
                style="max-width: 70%;">
                <RichText
                  v-model:textValue="item.descriptionText"
                  :value="item.description"
                  :emptyText="t('common.noDescription')" />
              </div>

              <div class="flex items-center justify-between h-4 leading-5">
                <RouterLink class="flex items-center space-x-1" :to="`/function#reviews?id=${item.id}&type=edit`">
                  <Icon icon="icon-shuxie" class="text-3.5" />
                  <span>{{ t('actions.edit') }}</span>
                </RouterLink>

                <RouterLink class="flex items-center ml-3" :to="`/function#reviews?id=${item.id}`">
                  <Icon icon="icon-shuxie" class="mr-0.5" />
                  <span>{{ t('caseReview.list.goToReview') }}</span>
                </RouterLink>

                <Button
                  :disabled="(!props.isAdmin && !props.permissionsMap.get(item.id)?.includes(FuncPlanPermission.MODIFY_PLAN))
                    || ![FuncPlanStatus.PENDING, FuncPlanStatus.BLOCKED, FuncPlanStatus.COMPLETED].includes(item.status?.value)"
                  size="small"
                  type="text"
                  class="px-0 flex items-center ml-2"
                  @click="startReview(item, index)">
                  <Icon icon="icon-kaishi" class="mr-0.5" />
                  <span>{{ t('actions.start') }}</span>
                </Button>

                <Button
                  :disabled="(!props.isAdmin && !props.permissionsMap.get(item.id)?.includes(FuncPlanPermission.MODIFY_PLAN))
                    || ![FuncPlanStatus.IN_PROGRESS].includes(item.status?.value)"
                  size="small"
                  type="text"
                  class="px-0 flex items-center ml-2"
                  @click="completeReview(item, index)">
                  <Icon icon="icon-yiwancheng" class="mr-0.5" />
                  <span>{{ t('actions.complete') }}</span>
                </Button>

                <Dropdown
                  class="ml-2"
                  :admin="false"
                  :menuItems="dropdownMenuItems"
                  :permissions="props.dropdownPermissionsMap.get(item.id)"
                  @click="handleDropdownAction(item, index, $event.key)">
                  <Icon icon="icon-gengduo" class="cursor-pointer outline-none items-center" />
                </Dropdown>
              </div>
            </div>
          </div>
        </div>
      </div>

      <Pagination
        v-if="props.totalCount > 4"
        :current="props.currentPage"
        :pageSize="props.pageSize"
        :pageSizeOptions="props.pageSizeOptions"
        :total="props.totalCount"
        :hideOnSinglePage="false"
        showSizeChanger
        size="default"
        class="text-right"
        @change="handlePaginationChange" />
    </template>
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

:deep(.ant-progress-text) {
  margin-left: 8px;
}

:deep(.ant-progress-inner) {
  background-color: #d5d5d5;
}

.router-link {
  color: #1890ff;
  cursor: pointer;
}

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
