<script setup lang="ts">
import { defineAsyncComponent, inject } from 'vue';
import { Avatar, Button, Pagination, Progress } from 'ant-design-vue';
import { UserOutlined } from '@ant-design/icons-vue';
import { Colon, Dropdown, Icon, Image, modal, NoData, notification, Popover } from '@xcan-angus/vue-ui';
import { download } from '@xcan-angus/infra';
import { useI18n } from 'vue-i18n';
import { funcPlan } from '@/api/tester';
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
  viewProgress: [planData: PlanDetail];
  viewBurnDown: [planData: PlanDetail];
  viewWorkCalendar: [planData: PlanDetail];
  dropdownClick: [planData: PlanDetail, itemIndex: number, actionKey: string];
  refresh: [];
}>();

const { t } = useI18n();

// Dependency injection
const deleteTabPane = inject<(keys: string[]) => void>('deleteTabPane', () => ({}));

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
  modal.confirm({
    content: t('functionPlan.list.confirmDeletePlan', { name: planData.name }),
    async onOk () {
      const id = planData.id;
      const [error] = await funcPlan.deletePlan(id);
      if (error) {
        return;
      }

      notification.success(t('functionPlan.list.planDeleteSuccess'));
      emit('refresh');

      deleteTabPane([id]);
    }
  });
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
 * Opens the member progress modal for the selected plan
 * @param planData - The plan data
 */
const handleViewProgress = (planData: PlanDetail) => {
  emit('viewProgress', planData);
};

/**
 * Opens the burndown chart modal for the selected plan
 * @param planData - The plan data
 */
const handleViewBurnDown = (planData: PlanDetail) => {
  emit('viewBurnDown', planData);
};

/**
 * Opens the work calendar modal for the selected plan
 * @param planData - The plan data
 */
const handleViewWorkCalendar = (planData: PlanDetail) => {
  emit('viewWorkCalendar', planData);
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
  actionKey: 'delete' | 'block' | 'grant' | 'viewProgress' | 'viewBurnDown' | 'viewWorkCalendar'
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
    case 'viewProgress':
      handleViewProgress(planData);
      break;
    case 'viewBurnDown':
      handleViewBurnDown(planData);
      break;
    case 'viewWorkCalendar':
      handleViewWorkCalendar(planData);
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
    name: t('functionPlan.list.delete'),
    permission: 'delete'
  },
  {
    key: 'block',
    icon: 'icon-zanting',
    name: t('functionPlan.list.block'),
    permission: 'block'
  },
  {
    key: 'grant',
    icon: 'icon-shouquan',
    name: t('functionPlan.list.grant'),
    permission: 'grant'
  },
  {
    key: 'viewProgress',
    icon: 'icon-jindu',
    name: t('functionPlan.list.viewProgress'),
    permission: 'viewProgress'
  },
  {
    key: 'viewBurnDown',
    icon: 'icon-tuibiao',
    name: t('functionPlan.list.viewBurnDown'),
    permission: 'viewBurnDown'
  },
  {
    key: 'viewWorkCalendar',
    icon: 'icon-gongzuori',
    name: t('functionPlan.list.viewWorkCalendar'),
    permission: 'viewWorkCalendar'
  }
];
</script>

<template>
  <div>
    <NoData v-if="props.dataList.length === 0" class="flex-1" />

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

        <div class="px-3.5 flex mt-3 justify-between text-3 text-theme-sub-content">
          <div class="flex leading-5">
            <div class="flex mr-10 items-center">
              <div class="mr-2">
                <span>{{ t('functionPlan.list.owner') }}</span>
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
                <span>{{ t('functionPlan.list.members') }}</span>
                <Colon />
              </div>

              <template v-if="item.members?.length">
                <div
                  v-for="user in item.showMembers"
                  :key="user.id"
                  :title="user.fullName"
                  class="w-5 h-5 mr-2 overflow-hidden rounded-full">
                  <Image
                    :src="user.avatar"
                    type="avatar"
                    class="w-full" />
                </div>

                <Popover
                  v-if="item.members.length > 10"
                  placement="bottomLeft"
                  internal>
                  <template #title>
                    <span class="text-3">{{ t('functionPlan.list.allMembers') }}</span>
                  </template>
                  <template #content>
                    <div class="flex flex-wrap" style="max-width: 700px;">
                      <div
                        v-for="_user in item.members"
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

          <div class="ml-8 text-theme-content">{{ t('functionPlan.list.totalCases', { count: item.caseNum }) }}</div>
        </div>

        <div class="px-3.5 flex flex-start justify-between text-3 text-theme-sub-content">
          <div class="flex flex-wrap">
            <div class="flex mt-3">
              <div class="mr-2 whitespace-nowrap">
                <span>{{ t('functionPlan.list.id') }}</span>
                <Colon />
              </div>
              <div class="text-theme-content">{{ item.id || "--" }}</div>
            </div>

            <div class="flex ml-8  mt-3">
              <div class="mr-2 whitespace-nowrap">
                <span>{{ t('functionPlan.list.isReviewLabel') }}</span>
                <Colon />
              </div>
              <div class="text-theme-content">{{ item.review ? t('status.yes') : t('status.no') }}</div>
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

          <div class="flex ml-8 mt-3">
            <div
              class="truncate text-theme-content"
              style="max-width: 100px;"
              :title="item.lastModifiedByName">
              {{ item.lastModifiedByName }}
            </div>
            <div class="mx-2 whitespace-nowrap">{{ t('functionPlan.list.modifiedBy') }}</div>
            <div class="whitespace-nowrap text-theme-content">
              {{ item.lastModifiedDate }}
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
              :emptyText="t('functionPlan.list.noDescription')" />
          </div>
          <div class="flex space-x-3 items-center justify-between h-4 leading-5">
            <RouterLink class="flex items-center space-x-1" :to="`/function#plans?id=${item.id}&type=edit`">
              <Icon icon="icon-shuxie" class="text-3.5" />
              <span>{{ t('functionPlan.list.edit') }}</span>
            </RouterLink>

            <Button
              :disabled="!props.isAdmin && !props.permissionsMap.get(item.id)?.includes('VIEW')"
              size="small"
              type="text"
              class="px-0 flex items-center space-x-1"
              @click="handleGoToCases(item)">
              <Icon icon="icon-ceshiyongli1" class="text-3.5" />
              <span>{{ t('functionPlan.list.viewCases') }}</span>
            </Button>

            <Button
              :disabled="(!props.isAdmin && !props.permissionsMap.get(item.id)?.includes(FuncPlanPermission.MODIFY_PLAN)) || ![FuncPlanStatus.PENDING, FuncPlanStatus.BLOCKED, FuncPlanStatus.COMPLETED].includes(item.status?.value)"
              size="small"
              type="text"
              class="px-0 flex items-center space-x-1"
              @click="handleStartPlan(item, index)">
              <Icon icon="icon-kaishi" class="text-3.5" />
              <span>{{ item.status.value === FuncPlanStatus.COMPLETED ? t('functionPlan.list.restart') : t('functionPlan.list.start') }}</span>
            </Button>

            <Button
              :disabled="(!props.isAdmin && !props.permissionsMap.get(item.id)?.includes(FuncPlanPermission.MODIFY_PLAN)) || ![FuncPlanStatus.IN_PROGRESS].includes(item.status?.value)"
              size="small"
              type="text"
              class="px-0 flex items-center space-x-1"
              @click="handleCompletePlan(item, index)">
              <Icon icon="icon-yiwancheng" class="text-3.5" />
              <span>{{ t('functionPlan.list.complete') }}</span>
            </Button>

            <Dropdown
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
        v-if="props.total > 5"
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
