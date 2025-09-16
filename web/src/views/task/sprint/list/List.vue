
<script setup lang="ts">
import { useI18n } from 'vue-i18n';
import { Avatar, Button, Pagination, Progress } from 'ant-design-vue';
import { UserOutlined } from '@ant-design/icons-vue';
import { Colon, Dropdown, Icon, Image, NoData, Popover } from '@xcan-angus/vue-ui';
import { download } from '@xcan-angus/infra';
import { TaskSprintPermission, TaskSprintStatus } from '@/enums/enums';
import { SprintInfo } from '../types';
import RichTextEditor from '@/components/richEditor/textContent/index.vue';

const { t } = useI18n();

// Component props
interface Props {
  sprintList: SprintInfo[];
  totalCount: number;
  currentPage: number;
  pageSize: number;
  pageSizeOptions: string[];
  isCurrentUserAdmin: boolean;
  sprintPermissionsMap: Map<string, string[]>;
  dropdownPermissionsMap: Map<string, string[]>;
  getDropdownMenuItems: () => any[];
  handleDropdownClick: (sprint: SprintInfo, index: number, action: 'clone' | 'block' | 'delete' | 'export' | 'grant' | 'viewBurnDown' | 'viewProgress' | 'reopen' | 'restart' | 'viewWorkCalendar') => void;
  startSprint: (sprint: SprintInfo, index: number) => void;
  completeSprint: (sprint: SprintInfo, index: number) => void;
  handlePaginationChange: (pageNo: number, newPageSize: number) => void;
}

const props = defineProps<Props>();
</script>

<template>
  <div>
    <NoData v-if="props.sprintList.length === 0" class="flex-1 mt-20" />

    <template v-else>
      <div
        v-for="(item, index) in props.sprintList"
        :key="item.id"
        class="mb-3.5 border border-theme-text-box rounded">
        <div class="px-3.5 py-2 flex items-center justify-between bg-theme-form-head w-full relative">
          <div class="truncate" style="width:35%;max-width: 360px;">
            <RouterLink
              class="router-link"
              :title="item.name"
              :to="`/task#sprint?id=${item.id}`">
              {{ item.name }}
            </RouterLink>
          </div>

          <div class="text-3 whitespace-nowrap text-theme-sub-content">
            <span>{{ item.startDate }}</span>
            <span class="mx-2">-</span>
            <span>{{ item.deadlineDate }}</span>
          </div>

          <div class="flex items-center">
            <div
              class="text-3 leading-4 flex items-center flex-none whitespace-nowrap mr-3.5">
              <div class="h-1.5 w-1.5 rounded-full mr-1" :class="item.status?.value"></div>
              <div>{{ item.status?.message }}</div>
            </div>
            <Progress :percent="Number(item.progress?.completedRate) || 0" style="width:150px;" />
          </div>
        </div>

        <div class="px-3.5 flex mt-3 justify-between text-3 text-theme-sub-content">
          <div class="flex leading-5">
            <div class="flex mr-10 items-center">
              <div class="mr-2">
                <span>{{ t('taskSprint.columns.owner') }}</span>
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
                <span>{{ t('taskSprint.columns.members') }}</span>
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
                    <span class="text-3">{{ t('taskSprint.allMembers') }}</span>
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

          <div class="ml-8 text-theme-content">
            {{ t('taskSprint.taskCount', {count: item.taskNum}) }}
          </div>
        </div>

        <div class="px-3.5 flex flex-start justify-between text-3 text-theme-sub-content">
          <div class="flex flex-wrap">
            <div class="flex mt-3">
              <div class="mr-2 whitespace-nowrap">
                <span>{{ t('taskSprint.columns.id') }}</span>
                <Colon />
              </div>
              <div class="text-theme-content">{{ item.id || "--" }}</div>
            </div>

            <div class="flex ml-8  mt-3">
              <div class="mr-2 whitespace-nowrap">
                <span>{{ t('taskSprint.columns.workloadAssessment') }}</span>
                <Colon />
              </div>
              <div class="text-theme-content">{{ item.evalWorkloadMethod.message }}</div>
            </div>

            <div v-if="item.taskPrefix" class="flex ml-8 mt-3 relative">
              <div class="mr-2 whitespace-nowrap">
                <span>{{ t('taskSprint.columns.taskPrefix') }}</span>
                <Colon />
              </div>
              <div
                class="truncate text-theme-content"
                style="max-width: 100px;"
                :title="item.taskPrefix">
                {{ item.taskPrefix }}
              </div>
            </div>

            <div v-if="item.attachments?.length" class="whitespace-nowrap ml-8 mt-3">
              <span>{{ t('taskSprint.columns.attachmentCount') }}</span>
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
                <span style="color:#1890ff" class="pl-2 pr-2 cursor-pointer">
                  {{ item.attachments?.length }}
                </span>
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
            <div class="mx-2 whitespace-nowrap">
              {{ t('taskSprint.columns.lastModifiedBy') }}
            </div>
            <div class="whitespace-nowrap text-theme-content">
              {{ item.lastModifiedDate }}
            </div>
          </div>
        </div>

        <div class="px-3.5 flex justify-between items-start text-3 my-2.5 relative">
          <div
            :title="item.otherInformation"
            class="truncate mr-8"
            style="max-width: 70%;">
            <RichTextEditor :value="item.otherInformation" emptyText="无说明~" />
          </div>
          <div class="flex space-x-3 items-center justify-between h-4 leading-5">
            <RouterLink class="flex items-center space-x-1" :to="`/task#sprint?id=${item.id}&type=edit`">
              <Icon icon="icon-shuxie" class="text-3.5" />
              <span>{{ t('taskSprint.actions.edit') }}</span>
            </RouterLink>

            <RouterLink
              class="flex items-center space-x-1"
              :to="`/task#task?sprintId=${item.id}&sprintName=${item.name}`">
              <Icon icon="icon-renwu2" class="text-3.5" />
              <span>{{ t('taskSprint.actions.viewTasks') }}</span>
            </RouterLink>

            <Button
              :disabled="(!props.isCurrentUserAdmin && !props.sprintPermissionsMap.get(item.id)?.includes(TaskSprintPermission.MODIFY_SPRINT))
                || ![TaskSprintStatus.PENDING].includes(item.status?.value)"
              size="small"
              type="text"
              class="px-0 flex items-center"
              @click="props.startSprint(item, index)">
              <Icon icon="icon-kaishi" class="mr-0.5" />
              <span>{{ t('taskSprint.actions.start') }}</span>
            </Button>

            <Button
              :disabled="(!props.isCurrentUserAdmin && !props.sprintPermissionsMap.get(item.id)?.includes(TaskSprintPermission.MODIFY_SPRINT))
                || ![TaskSprintStatus.IN_PROGRESS].includes(item.status?.value)"
              size="small"
              type="text"
              class="px-0 flex items-center"
              @click="props.completeSprint(item, index)">
              <Icon icon="icon-yiwancheng" class="mr-0.5" />
              <span>{{ t('taskSprint.actions.complete') }}</span>
            </Button>

            <Dropdown
              :admin="false"
              :menuItems="props.getDropdownMenuItems()"
              :permissions="props.dropdownPermissionsMap.get(item.id)"
              @click="props.handleDropdownClick(item, index, $event.key)">
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
        @change="props.handlePaginationChange" />
    </template>
  </div>
</template>

<style scoped>
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
