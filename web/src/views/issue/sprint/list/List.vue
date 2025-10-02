
<script setup lang="ts">
import { useI18n } from 'vue-i18n';
import { Avatar, Button, Pagination, Progress } from 'ant-design-vue';
import { UserOutlined } from '@ant-design/icons-vue';
import { Dropdown, Icon, Image, NoData, Popover } from '@xcan-angus/vue-ui';
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

/**
 * Get sprint status style based on status value
 * @param statusValue - Sprint status value
 * @returns CSS classes for sprint status container
 */
const getSprintStatusStyle = (statusValue: string) => {
  switch (statusValue) {
    case TaskSprintStatus.PENDING:
      return 'bg-gray-50 text-gray-700 border-gray-200 hover:bg-gray-100';
    case TaskSprintStatus.IN_PROGRESS:
      return 'bg-blue-50 text-blue-700 border-blue-200 hover:bg-blue-100';
    case TaskSprintStatus.COMPLETED:
      return 'bg-green-50 text-green-700 border-green-200 hover:bg-green-100';
    case TaskSprintStatus.BLOCKED:
      return 'bg-red-50 text-red-700 border-red-200 hover:bg-red-100';
    default:
      return 'bg-gray-50 text-gray-700 border-gray-200 hover:bg-gray-100';
  }
};

/**
 * Get sprint status dot style based on status value
 * @param statusValue - Sprint status value
 * @returns CSS classes for sprint status dot
 */
const getSprintStatusDotStyle = (statusValue: string) => {
  switch (statusValue) {
    case TaskSprintStatus.PENDING:
      return 'bg-gray-500';
    case TaskSprintStatus.IN_PROGRESS:
      return 'bg-blue-500';
    case TaskSprintStatus.COMPLETED:
      return 'bg-green-500';
    case TaskSprintStatus.BLOCKED:
      return 'bg-red-500';
    default:
      return 'bg-gray-500';
  }
};
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
              :to="`/issue#sprint?id=${item.id}`">
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

        <!-- Sprint information card -->
        <div class="px-4 py-3 bg-theme-bg-subtle/30 border-t border-theme-border-subtle">
          <div class="flex items-center justify-between">
            <!-- Left side: Owner + Status + Task count + Members -->
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

              <!-- Sprint status -->
              <div class="flex items-center space-x-2">
                <div
                  class="px-3 py-1.5 rounded-full text-xs font-semibold shadow-sm border transition-all duration-200 hover:shadow-md"
                  :class="getSprintStatusStyle(item.status?.value)">
                  <div class="flex items-center space-x-1.5">
                    <div
                      class="w-2 h-2 rounded-full"
                      :class="getSprintStatusDotStyle(item.status?.value)">
                    </div>
                    <span>{{ item.status?.message }}</span>
                  </div>
                </div>
              </div>

              <!-- Task count -->
              <div class="flex items-center space-x-2">
                <div class="flex flex-col">
                  <span class="text-xs text-theme-sub-content">{{ t('common.counts.issueCount') }}</span>
                  <span class="text-sm font-medium text-theme-content">
                    {{ item.taskNum }}
                  </span>
                </div>
              </div>

              <!-- Members -->
              <div class="flex items-center space-x-2">
                <div class="flex flex-col">
                  <span class="text-xs text-theme-sub-content">{{ t('common.members') }}</span>
                  <span class="text-sm font-medium text-theme-content">
                    {{ item.members?.length || 0 }} 人
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
                            v-for="user in item.members"
                            :key="user.id"
                            class="flex flex-col items-center space-y-1 p-2">
                            <div class="w-8 h-8 rounded-full overflow-hidden">
                              <Image
                                class="w-full h-full"
                                :src="user.avatar"
                                type="avatar" />
                            </div>
                            <span class="text-xs text-theme-content text-center truncate w-full" :title="user.fullName">{{ user.fullName }}</span>
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
            :title="item.otherInformationText"
            class="truncate mr-8"
            style="max-width: 70%;">
            <RichTextEditor
              v-model:textValue="item.otherInformationText"
              :value="item.otherInformation"
              :emptyText="t('tcommon.noDescription')" />
          </div>
          <div class="flex items-center justify-between h-4 leading-5">
            <RouterLink class="flex items-center space-x-1" :to="`/issue#sprint?id=${item.id}&type=edit`">
              <Icon icon="icon-shuxie" class="text-3.5" />
              <span>{{ t('actions.edit') }}</span>
            </RouterLink>

            <RouterLink
              class="flex items-center space-x-1 ml-3"
              :to="`/issue#issue?sprintId=${item.id}&sprintName=${item.name}`">
              <Icon icon="icon-renwu2" class="text-3.5" />
              <span>{{ t('sprint.actions.viewIssues') }}</span>
            </RouterLink>

            <Button
              :disabled="(!props.isCurrentUserAdmin
                && !props.sprintPermissionsMap.get(item.id)?.includes(TaskSprintPermission.MODIFY_SPRINT))
                || ![TaskSprintStatus.PENDING].includes(item.status?.value)"
              size="small"
              type="text"
              class="px-0 flex items-center ml-2"
              @click="props.startSprint(item, index)">
              <Icon icon="icon-kaishi" class="mr-0.5" />
              <span>{{ t('actions.start') }}</span>
            </Button>

            <Button
              :disabled="(!props.isCurrentUserAdmin
                && !props.sprintPermissionsMap.get(item.id)?.includes(TaskSprintPermission.MODIFY_SPRINT))
                || ![TaskSprintStatus.IN_PROGRESS].includes(item.status?.value)"
              size="small"
              type="text"
              class="px-0 flex items-center ml-2"
              @click="props.completeSprint(item, index)">
              <Icon icon="icon-yiwancheng" class="mr-0.5" />
              <span>{{ t('actions.complete') }}</span>
            </Button>

            <Dropdown
              class="ml-2"
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
        v-if="props.totalCount > 4"
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
