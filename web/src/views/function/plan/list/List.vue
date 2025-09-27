<script setup lang="ts">
import { defineAsyncComponent } from 'vue';
import { Avatar, Button, Pagination, Progress } from 'ant-design-vue';
import { UserOutlined } from '@ant-design/icons-vue';
import { Colon, Dropdown, Icon, Image, NoData, Popover } from '@xcan-angus/vue-ui';
import { download } from '@xcan-angus/infra';
import { useI18n } from 'vue-i18n';
import { FuncPlanStatus, FuncPlanPermission } from '@/enums/enums';
import { PlanDetail } from '../types';

const RichText = defineAsyncComponent(() => import('@/components/richEditor/textContent/index.vue'));

// Props definition
interface Props {
  dataList: PlanDetail[];
  loading: boolean;
  total: number;
  pageNo: number;
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
  startPlan: [planData: PlanDetail, itemIndex: number];
  completePlan: [planData: PlanDetail, itemIndex: number];
  blockPlan: [planData: PlanDetail, itemIndex: number];
  deletePlan: [planData: PlanDetail];
  grantPermission: [planData: PlanDetail];
  goToCases: [planData: PlanDetail];
  dropdownClick: [planData: PlanDetail, itemIndex: number, actionKey: string];
  refresh: [];
}>();

const { t } = useI18n();

/**
 * Get plan status style based on status value
 * @param statusValue - Plan status value
 * @returns CSS classes for plan status container
 */
const getPlanStatusStyle = (statusValue: string) => {
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
 * Get plan status dot style based on status value
 * @param statusValue - Plan status value
 * @returns CSS classes for plan status dot
 */
const getPlanStatusDotStyle = (statusValue: string) => {
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
 * Starts a plan and emits the event to parent
 * @param planData - The plan data to start
 * @param itemIndex - The index of the item in the list
 */
const handleStartPlan = async (planData: PlanDetail, itemIndex: number) => {
  emit('startPlan', planData, itemIndex);
};

/**
 * Completes a plan and emits the event to parent
 * @param planData - The plan data to complete
 * @param itemIndex - The index of the item in the list
 */
const handleCompletePlan = async (planData: PlanDetail, itemIndex: number) => {
  emit('completePlan', planData, itemIndex);
};

/**
 * Blocks a plan and emits the event to parent
 * @param planData - The plan data to block
 * @param itemIndex - The index of the item in the list
 */
const handleBlockPlan = async (planData: PlanDetail, itemIndex: number) => {
  emit('blockPlan', planData, itemIndex);
};

/**
 * Deletes a plan with confirmation
 * @param planData - The plan data to delete
 */
const handleDeletePlan = async (planData: PlanDetail) => {
  emit('deletePlan', planData);
};

/**
 * Opens the authorization modal for the selected plan
 * @param planData - The plan data
 */
const handleGrantPermission = (planData: PlanDetail) => {
  emit('grantPermission', planData);
};

/**
 * Navigates to cases for the selected plan
 * @param planData - The plan data
 */
const handleGoToCases = (planData: PlanDetail) => {
  emit('goToCases', planData);
};

/**
 * Handles dropdown menu item clicks
 * @param planData - The plan data
 * @param itemIndex - The index of the item in the list
 * @param actionKey - The action key from the dropdown
 */
const handleDropdownClick = (
  planData: PlanDetail,
  itemIndex: number,
  actionKey: 'delete' | 'block' | 'grant'
) => {
  switch (actionKey) {
    case 'delete':
      handleDeletePlan(planData);
      break;
    case 'block':
      handleBlockPlan(planData, itemIndex);
      break;
    case 'grant':
      handleGrantPermission(planData);
      break;
    default:
      emit('dropdownClick', planData, itemIndex, actionKey);
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
    key: 'block',
    icon: 'icon-zanting',
    name: t('actions.block'),
    permission: 'block'
  },
  {
    key: 'grant',
    icon: 'icon-quanxian1',
    name: t('actions.permission')
  }
];
</script>

<template>
  <div>
    <NoData v-if="props.dataList.length === 0" class="flex-1 mt-20" />

    <template v-else>
      <div
        v-for="(item, index) in props.dataList"
        :key="item.id"
        class="mb-3.5 border border-theme-text-box rounded">
        <div class="px-3.5 py-2 flex items-center justify-between bg-theme-form-head w-full relative">
          <div class="truncate" style="width:35%;max-width: 360px;">
            <RouterLink
              class="router-link flex-1 truncate"
              :title="item.name"
              :to="`/function#plans?id=${item.id}`">
              {{ item.name }}
            </RouterLink>
          </div>

          <div class="text-3 whitespace-nowrap">
            <span class="text-theme-title ml-2">{{ item.startDate }}</span>
            <span class="text-theme-sub-content mx-2">{{ t('functionPlan.list.to') }}</span>
            <span class="text-theme-title">{{ item.deadlineDate }}</span>
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

        <!-- Plan information card -->
        <div class="px-4 py-3 bg-theme-bg-subtle/30 border-t border-theme-border-subtle">
          <div class="flex items-center justify-between">
            <!-- Left side: Owner + Status + Case count + Members -->
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
                  <span class="text-xs text-theme-sub-content">{{ t('common.owner') }}</span>
                  <span class="text-sm font-medium text-theme-content truncate max-w-24" :title="item.ownerName">
                    {{ item.ownerName }}
                  </span>
                </div>
              </div>

              <!-- Plan status -->
              <div class="flex items-center space-x-2">
                <div
                  class="px-3 py-1.5 rounded-full text-xs font-semibold shadow-sm border transition-all duration-200 hover:shadow-md"
                  :class="getPlanStatusStyle(item.status?.value)">
                  <div class="flex items-center space-x-1.5">
                    <div
                      class="w-2 h-2 rounded-full"
                      :class="getPlanStatusDotStyle(item.status?.value)">
                    </div>
                    <span>{{ item.status?.message }}</span>
                  </div>
                </div>
              </div>

              <!-- Case count -->
              <div class="flex items-center space-x-2">
                <div class="flex flex-col">
                  <span class="text-xs text-theme-sub-content">{{ t('functionPlan.list.totalCases') }}</span>
                  <span class="text-sm font-medium text-theme-content">
                    {{ item.caseNum }}
                  </span>
                </div>
              </div>

              <!-- Members -->
              <div class="flex items-center space-x-2">
                <div class="flex flex-col">
                  <span class="text-xs text-theme-sub-content">{{ t('common.members') }}</span>
                  <span class="text-sm font-medium text-theme-content">
                    {{ item.members?.length || 0 }} {{ t('organization.user') }}
                  </span>
                </div>

                <div class="flex -space-x-1">
                  <template v-if="item.members?.length">
                    <div
                      v-for="user in item.showMembers.slice(0, 8)"
                      :key="user.id"
                      :title="user.fullName"
                      class="w-6 h-6 rounded-full overflow-hidden ring-1 ring-white shadow-sm">
                      <Image
                        :src="user.avatar"
                        type="avatar"
                        class="w-full h-full" />
                    </div>

                    <Popover
                      v-if="item.members.length > 8"
                      placement="bottomLeft"
                      internal>
                      <template #title>
                        <span class="text-sm font-medium">{{ t('common.members') }} ({{ item.members.length }})</span>
                      </template>

                      <template #content>
                        <div class="grid grid-cols-5 gap-2 max-w-md">
                          <div
                            v-for="_user in item.members"
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
                        +{{ item.members.length - 8 }}
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
                <span class="text-theme-content font-medium truncate max-w-16" :title="item.lastModifiedByName">
                  {{ item.lastModifiedByName }}
                </span>

                <span>{{ t('functionPlan.list.modifiedBy') }}</span>

                <span class="text-theme-sub-content">{{ item.lastModifiedDate }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Divider line -->
        <div class="border-t border-theme-border-subtle/50"></div>

        <div class="px-3.5 flex flex-start justify-between text-3 text-theme-sub-content">
          <div class="flex flex-wrap">
            <div class="flex mt-3">
              <div class="mr-2 whitespace-nowrap">
                <span>{{ t('common.id') }}</span>
                <Colon />
              </div>

              <div class="text-theme-content">{{ item.id || "--" }}</div>
            </div>

            <div class="flex ml-8  mt-3">
              <div class="mr-2 whitespace-nowrap">
                <span>{{ t('functionPlan.list.isReviewLabel') }}</span>
                <Colon />
              </div>
              <div :class="item.review ? 'text-green-600' : 'text-red-500'">{{ item.review ? t('status.on') : t('status.off') }}</div>
            </div>

            <div class="flex ml-8  mt-3">
              <div class="mr-2 whitespace-nowrap">
                <span>{{ t('functionPlan.list.workloadAssessment') }}</span>
                <Colon />
              </div>

              <div class="text-theme-content">{{ item.evalWorkloadMethod.message }}</div>
            </div>

            <div v-if="item.casePrefix" class="flex ml-8 mt-3 relative">
              <div class="mr-2 whitespace-nowrap">
                <span>{{ t('functionPlan.list.casePrefix') }}</span>
                <Colon />
              </div>

              <div
                class="truncate text-theme-content"
                style="max-width: 100px;"
                :title="item.casePrefix">
                {{ item.casePrefix }}
              </div>
            </div>

            <div v-if="item.attachments?.length" class="whitespace-nowrap ml-8 mt-3">
              <span>{{ t('functionPlan.list.attachmentCount') }}</span>

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
        </div>

        <div class="px-3.5 flex justify-between items-start text-3 my-2.5 relative">
          <div
            :title="item.otherInformationText"
            class="truncate mr-8"
            style="max-width: 70%;">
            <RichText
              v-model:textValue="item.otherInformationText"
              :value="item.otherInformation"
              :emptyText="t('common.noDescription')" />
          </div>

          <div class="flex items-center justify-between h-4 leading-5">
            <RouterLink class="flex items-center space-x-1" :to="`/function#plans?id=${item.id}&type=edit`">
              <Icon icon="icon-shuxie" class="text-3.5" />
              <span>{{ t('actions.edit') }}</span>
            </RouterLink>

            <Button
              :disabled="!props.isAdmin && props.permissionsMap.size < 1"
              size="small"
              type="text"
              class="px-0 flex items-center ml-3"
              @click="handleGoToCases(item)">
              <Icon icon="icon-ceshiyongli1" class="mr-0.5" />
              <span>{{ t('functionPlan.list.viewCases') }}</span>
            </Button>

            <Button
              :disabled="(!props.isAdmin && !props.permissionsMap.get(item.id)?.includes(FuncPlanPermission.MODIFY_PLAN))
                || ![FuncPlanStatus.PENDING, FuncPlanStatus.BLOCKED, FuncPlanStatus.COMPLETED].includes(item.status?.value)"
              size="small"
              type="text"
              class="px-0 flex items-center ml-2"
              @click="handleStartPlan(item, index)">
              <Icon icon="icon-kaishi" class="mr-0.5" />
              <span>{{ item.status.value === FuncPlanStatus.COMPLETED ? t('common.restart') : t('actions.start') }}</span>
            </Button>

            <Button
              :disabled="(!props.isAdmin && !props.permissionsMap.get(item.id)?.includes(FuncPlanPermission.MODIFY_PLAN))
                || ![FuncPlanStatus.IN_PROGRESS].includes(item.status?.value)"
              size="small"
              type="text"
              class="px-0 flex items-center ml-2"
              @click="handleCompletePlan(item, index)">
              <Icon icon="icon-yiwancheng" class="mr-0.5" />
              <span>{{ t('actions.complete') }}</span>
            </Button>

            <Dropdown
              class="ml-2"
              :admin="false"
              :menuItems="dropdownMenuItems"
              :permissions="props.dropdownPermissionsMap.get(item.id)"
              @click="handleDropdownClick(item, index, $event.key)">
              <Icon icon="icon-gengduo" class="cursor-pointer outline-none items-center" />
            </Dropdown>
          </div>
        </div>
      </div>

      <Pagination
        v-if="props.total > 4"
        :current="props.pageNo"
        :pageSize="props.pageSize"
        :pageSizeOptions="props.pageSizeOptions"
        :total="props.total"
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
