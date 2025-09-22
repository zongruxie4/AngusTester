<script setup lang="ts">
import { defineAsyncComponent, inject } from 'vue';
import { Avatar, Button, Pagination, Popover, Progress } from 'ant-design-vue';
import { UserOutlined } from '@ant-design/icons-vue';
import { Colon, Dropdown, Icon, Image, modal, NoData, notification } from '@xcan-angus/vue-ui';
import { download } from '@xcan-angus/infra';
import { useI18n } from 'vue-i18n';
import { func } from '@/api/tester';
import { FuncPlanStatus, FuncPlanPermission } from '@/enums/enums';
import { ReviewDetail } from '../types';

const RichEditor = defineAsyncComponent(() => import('@/components/richEditor/index.vue'));

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
    name: t('caseReview.list.delete'),
    permission: 'delete'
  },
  {
    key: 'clone',
    icon: 'icon-fuzhi',
    name: t('caseReview.list.clone'),
    noAuth: true,
    permission: 'clone'
  }
];
</script>
<template>
  <div>
    <NoData v-if="props.reviewList.length === 0" class="flex-1" />

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

          <div class="flex">
            <div
              class="text-theme-sub-content text-3 leading-4 flex items-center flex-none whitespace-nowrap mr-3.5">
              <div class="h-1.5 w-1.5 rounded-full mr-1" :class="item.status?.value"></div>
              <div>{{ item.status?.message }}</div>
            </div>
            <Progress :percent="+item.progress?.completedRate" style="width:150px;" />
          </div>
        </div>

        <div class="px-3.5 flex mt-3 justify-between text-3 text-theme-sub-content">
          <div class="flex leading-5">
            <div class="flex mr-10 items-center">
              <div class="mr-2">
                <span>{{ t('caseReview.list.owner') }}</span>
                <Colon />
              </div>

              <div class="w-5 h-5 rounded-full mr-1 overflow-hidden">
                <Image
                  class="w-full"
                  :src="item.ownerAvatar"
                  type="avatar" />
              </div>

              <div
                class="text-theme-content truncate"
                :title="item.ownerName"
                style="max-width: 200px;">
                {{ item.ownerName }}
              </div>
            </div>

            <div class="flex items-center">
              <div class="mr-2">
                <span>{{ t('caseReview.list.participants') }}</span>
                <Colon />
              </div>

              <template v-if="item.participants?.length">
                <div
                  v-for="user in item.participants.slice(0, 10)"
                  :key="user.id"
                  :title="user.fullName"
                  class="w-5 h-5 mr-2 overflow-hidden rounded-full">
                  <Image
                    :src="user.avatar"
                    type="avatar"
                    class="w-full" />
                </div>

                <Popover
                  v-if="item.participants.length > 5"
                  placement="bottomLeft"
                  internal>
                  <template #title>
                    <span class="text-3">{{ t('caseReview.list.allParticipants') }}</span>
                  </template>

                  <template #content>
                    <div class="flex flex-wrap" style="max-width: 700px;">
                      <div
                        v-for="_user in item.participants"
                        :key="_user.id"
                        class="flex text-3 leading-5 mr-2 mb-2">
                        <div class="w-5 h-5 rounded-full mr-1 flex-none overflow-hidden">
                          <Image
                            class="w-full"
                            :src="_user.avatar"
                            type="avatar" />
                        </div>
                        <span class="flex-1 truncate">{{ _user.fullName }}</span>
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

          <div class="ml-8 text-theme-content">{{ t('caseReview.list.totalCases', {count: item.caseNum}) }}</div>
        </div>

        <div class="px-3.5 flex flex-start justify-between text-3 text-theme-sub-content">
          <div class="flex flex-wrap">
            <div class="flex mt-3">
              <div class="mr-2 whitespace-nowrap">
                <span>{{ t('caseReview.list.id') }}</span>
                <Colon />
              </div>
              <div class="text-theme-content">{{ item.id || "--" }}</div>
            </div>

            <div class="flex mt-3 ml-8">
              <div class="mr-2 whitespace-nowrap">
                <span>{{ t('caseReview.list.testPlan') }}</span>
                <Colon />
              </div>
              <div class="text-theme-content">{{ item.planName || "--" }}</div>
            </div>

            <div v-if="item.attachments?.length" class="whitespace-nowrap ml-8 mt-3">
              <span>{{ t('caseReview.list.attachmentCount') }}</span>
              <Colon />
              <Popover placement="bottomLeft" internal>
                <template #content>
                  <div class="flex flex-col text-3 leading-5 space-y-1">
                    <div
                      v-for="_attachment in item.attachments"
                      :key="_attachment.id"
                      :title="_attachment.name"
                      class="flex-1 px-2 py-1 truncate link"
                      @click="download(_attachment.url)">
                      {{ _attachment.name }}
                    </div>
                  </div>
                </template>
                <span style="color:#1890ff" class="pl-2 pr-2 cursor-pointer">{{ item.attachments?.length }}</span>
              </Popover>
            </div>
          </div>

          <div class="flex ml-8 mt-3">
            <div
              class="truncate text-theme-content"
              style="max-width: 100px;"
              :title="item.lastModifiedByName">
              {{ item.lastModifiedByName }}
            </div>

            <div class="mx-2 whitespace-nowrap">{{ t('caseReview.list.modifiedBy') }}</div>

            <div class="whitespace-nowrap text-theme-content">
              {{ item.lastModifiedDate }}
            </div>
          </div>
        </div>

        <div class="px-3.5 flex justify-between items-start text-3 my-2.5 relative">
          <div
            class="truncate mr-8"
            style="max-width: 70%;">
            <RichEditor
              v-if="item.otherInformation"
              :value="item.otherInformation"
              mode="view" />
          </div>

          <div class="flex space-x-3 items-center justify-between h-4 leading-5">
            <RouterLink class="flex items-center space-x-1" :to="`/function#reviews?id=${item.id}&type=edit`">
              <Icon icon="icon-shuxie" class="text-3.5" />
              <span>{{ t('caseReview.list.edit') }}</span>
            </RouterLink>

            <RouterLink class="flex items-center space-x-1" :to="`/function#reviews?id=${item.id}`">
              <Icon icon="icon-shuxie" class="text-3.5" />
              <span>{{ t('caseReview.list.goToReview') }}</span>
            </RouterLink>

            <Button
              :disabled="(!props.isAdmin && !props.permissionsMap.get(item.id)?.includes(FuncPlanPermission.MODIFY_PLAN))
                || ![FuncPlanStatus.PENDING, FuncPlanStatus.BLOCKED, FuncPlanStatus.COMPLETED].includes(item.status?.value)"
              size="small"
              type="text"
              class="px-0 flex items-center space-x-1"
              @click="startReview(item, index)">
              <Icon icon="icon-kaishi" class="text-3.5" />
              <span>{{ t('caseReview.list.start') }}</span>
            </Button>

            <Button
              :disabled="(!props.isAdmin && !props.permissionsMap.get(item.id)?.includes(FuncPlanPermission.MODIFY_PLAN))
                || ![FuncPlanStatus.IN_PROGRESS].includes(item.status?.value)"
              size="small"
              type="text"
              class="px-0 flex items-center space-x-1"
              @click="completeReview(item, index)">
              <Icon icon="icon-yiwancheng" class="text-3.5" />
              <span>{{ t('caseReview.list.complete') }}</span>
            </Button>

            <Dropdown
              :admin="false"
              :menuItems="dropdownMenuItems"
              :permissions="props.dropdownPermissionsMap.get(item.id)"
              @click="handleDropdownAction(item, index, $event.key)">
              <Icon icon="icon-gengduo" class="cursor-pointer outline-none items-center" />
            </Dropdown>
          </div>
        </div>
      </div>

      <Pagination
        v-if="props.totalCount > 5"
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
