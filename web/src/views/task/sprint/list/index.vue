<script setup lang="ts">
import { computed, defineAsyncComponent, inject, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Avatar, Button, Pagination, Progress } from 'ant-design-vue';
import { UserOutlined } from '@ant-design/icons-vue';
import {
  AsyncComponent, Colon, Dropdown, Icon, Image, modal, NoData, notification, Popover, Spin
} from '@xcan-angus/vue-ui';
import { appContext, download, ProjectPageQuery, TESTER, utils } from '@xcan-angus/infra';
import ProcessPng from './images/process.png';
import { task } from '@/api/tester';
import { TaskSprintPermission, TaskSprintStatus } from '@/enums/enums';

import { SprintInfo } from '../types';
import { BasicProps } from '@/types/types';

// Component props
const props = withDefaults(defineProps<BasicProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  notify: undefined
});

const { t } = useI18n();

// Lazy-loaded components
const SearchPanel = defineAsyncComponent(() => import('@/views/task/sprint/list/SearchPanel.vue'));
const Introduce = defineAsyncComponent(() => import('@/views/task/sprint/list/Introduce.vue'));
const BurndownChart = defineAsyncComponent(() => import('@/views/task/sprint/list/BurndownChart.vue'));
const MemberProgressModal = defineAsyncComponent(() => import('@/views/task/sprint/list/MemberProgress.vue'));
const WorkCalendarModal = defineAsyncComponent(() => import('@/views/task/sprint/list/WorkCalendar.vue'));
const RichTextEditor = defineAsyncComponent(() => import('@/components/richEditor/textContent/index.vue'));
const AuthorizeModal = defineAsyncComponent(() => import('@/components/AuthorizeModal/index.vue'));

// Injected dependencies
const deleteTabPane = inject<(keys: string[]) => void>('deleteTabPane', () => ({}));

// Computed properties
const isCurrentUserAdmin = computed(() => appContext.isAdmin());

// Component state
const isDataLoaded = ref(false);
const isLoading = ref(false);
const exportLoadingSet = ref<Set<string>>(new Set());
const hasActiveSearch = ref(false);

// Pagination state
const currentPage = ref(1);
const pageSize = ref(5);
const pageSizeOptions = ['5', '10', '15', '20', '30'];
let searchParameters = {
  orderBy: undefined,
  orderSort: undefined,
  filters: []
};

const totalCount = ref(0);
const sprintList = ref<SprintInfo[]>([]);
const sprintPermissionsMap = ref<Map<string, string[]>>(new Map());

// Modal visibility state
const selectedSprint = ref<SprintInfo>();
const isAuthorizeModalVisible = ref(false);
const isBurndownModalVisible = ref(false);
const isProgressModalVisible = ref(false);
const isWorkCalendarModalVisible = ref(false);

/**
 * Handle search panel parameter changes
 * @param searchData - New search parameters from search panel
 */
const handleSearchChange = (searchData) => {
  searchParameters = searchData;
  currentPage.value = 1;
  loadSprintData();
};

/**
 * Handle refresh button click
 */
const handleRefresh = () => {
  currentPage.value = 1;
  sprintPermissionsMap.value.clear();
  loadSprintData();
};

/**
 * Update sprint data in the list after API call
 * @param sprintId - ID of the sprint to update
 * @param index - Index of the sprint in the list
 */
const updateSprintData = async (sprintId: string, index: number) => {
  const [error, response] = await task.getSprintDetail(sprintId);
  isLoading.value = false;
  if (error) {
    return;
  }

  if (response?.data) {
    sprintList.value[index] = response?.data;
  }
};

/**
 * Open burndown chart modal for selected sprint
 * @param sprint - Sprint data to display
 */
const openBurndownChart = (sprint: SprintInfo) => {
  selectedSprint.value = sprint;
  isBurndownModalVisible.value = true;
};

/**
 * Open member progress modal for selected sprint
 * @param sprint - Sprint data to display
 */
const openMemberProgress = (sprint: SprintInfo) => {
  selectedSprint.value = sprint;
  isProgressModalVisible.value = true;
};

/**
 * Open work calendar modal for selected sprint
 * @param sprint - Sprint data to display
 */
const openWorkCalendar = (sprint: SprintInfo) => {
  selectedSprint.value = sprint;
  isWorkCalendarModalVisible.value = true;
};

/**
 * Reopen a sprint
 * @param sprint - Sprint data to reopen
 * @param index - Index of the sprint in the list
 */
const reopenSprint = async (sprint: SprintInfo, index: number) => {
  isLoading.value = true;
  const [error] = await task.reopenSprint({
    ids: [sprint.id]
  }, {
    paramsType: true
  });
  isLoading.value = false;
  if (error) {
    return;
  }
  notification.success(t('taskSprint.messages.reopenSuccess'));
  await updateSprintData(sprint.id, index);
};

/**
 * Restart a sprint
 * @param sprint - Sprint data to restart
 * @param index - Index of the sprint in the list
 */
const restartSprint = async (sprint: SprintInfo, index: number) => {
  isLoading.value = true;
  const [error] = await task.restartSprint({ ids: [sprint.id] }, { paramsType: true });
  isLoading.value = false;
  if (error) {
    return;
  }
  notification.success(t('taskSprint.messages.restartSuccess'));
  await updateSprintData(sprint.id, index);
};

/**
 * Start a sprint
 * @param sprint - Sprint data to start
 * @param index - Index of the sprint in the list
 */
const startSprint = async (sprint: SprintInfo, index: number) => {
  isLoading.value = true;
  const sprintId = sprint.id;
  const [error] = await task.startSprint(sprintId);
  isLoading.value = false;
  if (error) {
    isLoading.value = false;
    return;
  }

  notification.success(t('taskSprint.messages.startSuccess'));
  await updateSprintData(sprintId, index);
};

/**
 * Complete a sprint
 * @param sprint - Sprint data to complete
 * @param index - Index of the sprint in the list
 */
const completeSprint = async (sprint: SprintInfo, index: number) => {
  isLoading.value = true;
  const sprintId = sprint.id;
  const [error] = await task.endSprint(sprintId);
  if (error) {
    isLoading.value = false;
    return;
  }

  notification.success(t('taskSprint.messages.completeSuccess'));
  await updateSprintData(sprintId, index);
};

/**
 * Block a sprint
 * @param sprint - Sprint data to block
 * @param index - Index of the sprint in the list
 */
const blockSprint = async (sprint: SprintInfo, index: number) => {
  isLoading.value = true;
  const sprintId = sprint.id;
  const [error] = await task.blockSprint(sprintId);
  if (error) {
    return;
  }

  notification.success(t('taskSprint.messages.blockSuccess'));
  await updateSprintData(sprintId, index);
};

/**
 * Delete a sprint with confirmation
 * @param sprint - Sprint data to delete
 */
const deleteSprint = async (sprint: SprintInfo) => {
  modal.confirm({
    content: t('taskSprint.messages.confirmDelete', { name: sprint.name }),
    async onOk () {
      const sprintId = sprint.id;
      const [error] = await task.deleteSprint(sprintId);
      if (error) {
        return;
      }

      notification.success(t('taskSprint.messages.deleteSuccess'));
      await loadSprintData();
      deleteTabPane([sprintId]);
    }
  });
};

/**
 * Open authorization modal for sprint
 * @param sprint - Sprint data to authorize
 */
const openAuthorizationModal = (sprint: SprintInfo) => {
  selectedSprint.value = sprint;
  isAuthorizeModalVisible.value = true;
};

/**
 * Handle authorization flag change
 * @param authData - Authorization data
 */
const handleAuthFlagChange = async ({ auth }: { auth: boolean }) => {
  const currentSprintList = sprintList.value;
  const targetSprintId = selectedSprint.value?.id;
  for (let i = 0, len = currentSprintList.length; i < len; i++) {
    if (currentSprintList[i].id === targetSprintId) {
      currentSprintList[i].auth = auth;
      break;
    }
  }
};

/**
 * Clone a sprint
 * @param sprint - Sprint data to clone
 */
const cloneSprint = async (sprint: SprintInfo) => {
  const [error] = await task.cloneSprint(sprint.id);
  if (error) {
    return;
  }
  notification.success(t('taskSprint.messages.cloneSuccess'));
  await loadSprintData();
};

/**
 * Export sprint data
 * @param sprint - Sprint data to export
 */
const exportSprint = async (sprint: SprintInfo) => {
  const { id, projectId } = sprint;
  if (exportLoadingSet.value.has(id)) {
    return;
  }

  exportLoadingSet.value.add(id);
  await download(`${TESTER}/task/export?projectId=${projectId}&sprintId=${id}`);
  exportLoadingSet.value.delete(id);
};

/**
 * Handle dropdown menu item click
 * @param sprint - Sprint data
 * @param index - Index of the sprint in the list
 * @param action - Action key to perform
 */
const handleDropdownClick = (
  sprint: SprintInfo,
  index: number,
  action: 'clone' | 'block' | 'delete' | 'export' | 'grant' | 'viewBurnDown' | 'viewProgress' | 'reopen' | 'restart' | 'viewWorkCalendar'
) => {
  switch (action) {
    case 'block':
      blockSprint(sprint, index);
      break;
    case 'delete':
      deleteSprint(sprint);
      break;
    case 'grant':
      openAuthorizationModal(sprint);
      break;
    case 'clone':
      cloneSprint(sprint);
      break;
    case 'export':
      exportSprint(sprint);
      break;
    case 'viewBurnDown':
      openBurndownChart(sprint);
      break;
    case 'viewProgress':
      openMemberProgress(sprint);
      break;
    case 'reopen':
      reopenSprint(sprint, index);
      break;
    case 'restart':
      restartSprint(sprint, index);
      break;
    case 'viewWorkCalendar':
      openWorkCalendar(sprint);
      break;
  }
};

/**
 * Handle pagination change
 * @param pageNo - New page number
 * @param newPageSize - New page size
 */
const handlePaginationChange = (pageNo: number, newPageSize: number) => {
  currentPage.value = pageNo;
  pageSize.value = newPageSize;
  loadSprintData();
};

/**
 * Load sprint data from API
 */
const loadSprintData = async () => {
  isLoading.value = true;
  const params: ProjectPageQuery = {
    projectId: props.projectId,
    pageNo: currentPage.value,
    pageSize: pageSize.value,
    ...searchParameters
  };

  const [error, response] = await task.getSprintList(params);
  isDataLoaded.value = true;
  isLoading.value = false;

  hasActiveSearch.value = !!(params.filters?.length || params.orderBy);

  if (error) {
    totalCount.value = 0;
    sprintList.value = [];
    return;
  }

  const data = response?.data || { total: 0, list: [] };
  if (data) {
    totalCount.value = +data.total;

    const _list = (data.list || [] as SprintInfo[]);
    sprintList.value = _list.map(item => {
      if (item.progress?.completedRate) {
        item.progress.completedRate = +item.progress.completedRate.replace(/(\d+\.\d{2})\d+/, '$1');
      }

      if (item.attachments?.length) {
        item.attachments = item.attachments.map(item => {
          return {
            ...item,
            id: utils.uuid()
          };
        });
      }

      if (item.members) {
        item.showMembers = item.members.slice(0, 10);
      }
      return item;
    });

    // Load permissions for non-admin users
    if (!isCurrentUserAdmin.value) {
      for (let i = 0, len = _list.length; i < len; i++) {
        const id = _list[i].id;
        if (!sprintPermissionsMap.value.get(id)) {
          const [_error, _res] = await loadSprintPermissions(id);
          if (!_error) {
            const _permissions = (_res?.data?.permissions || []).map(item => item.value);
            sprintPermissionsMap.value.set(id, _permissions);
          }
        }
      }
    }
  }
};

/**
 * Load sprint permissions for a specific sprint
 * @param sprintId - ID of the sprint
 * @returns Promise with permission data
 */
const loadSprintPermissions = async (sprintId: string) => {
  const params = {
    admin: true
  };
  return await task.getCurrentUserSprintAuth(sprintId, params);
};

const dropdownPermissionsMap = computed(() => {
  const map = new Map<string, string[]>();
  if (sprintList.value.length) {
    const _isAdmin = isCurrentUserAdmin.value;
    const _permissionsMap = sprintPermissionsMap.value;
    const list = sprintList.value;
    for (let i = 0, len = list.length; i < len; i++) {
      const { id, status } = list[i];
      const _permissions: string[] = _permissionsMap.get(id) || [];
      const tempPermissions: string[] = [];
      const _status = status.value;

      if ((_isAdmin || _permissions.includes(TaskSprintPermission.MODIFY_SPRINT)) &&
        [TaskSprintStatus.PENDING, TaskSprintStatus.IN_PROGRESS].includes(_status)) {
        tempPermissions.push('block');
      }
      if (_isAdmin || _permissions.includes(TaskSprintPermission.DELETE_SPRINT)) {
        tempPermissions.push('delete');
      }
      if (_isAdmin || _permissions.includes(TaskSprintPermission.GRANT)) {
        tempPermissions.push('grant');
      }
      if (_isAdmin || _permissions.includes(TaskSprintPermission.EXPORT_TASK)) {
        tempPermissions.push('export');
      }
      if (_isAdmin || _permissions.includes(TaskSprintPermission.MODIFY_SPRINT)) {
        tempPermissions.push('modify');
      }
      map.set(id, tempPermissions);
    }
  }
  return map;
});

const getDropdownMenuItems = () => {
  return [
    {
      key: 'block',
      icon: 'icon-zusai',
      name: t('taskSprint.dropdownMenu.block'),
      permission: 'block'
    },
    {
      key: 'delete',
      icon: 'icon-qingchu',
      name: t('taskSprint.dropdownMenu.delete'),
      permission: 'delete'
    },
    {
      key: 'grant',
      icon: 'icon-quanxian1',
      name: t('taskSprint.dropdownMenu.permission'),
      permission: 'grant'
    },
    {
      key: 'clone',
      icon: 'icon-fuzhi',
      name: t('taskSprint.dropdownMenu.clone'),
      noAuth: true,
      permission: 'clone'
    },
    {
      key: 'reopen',
      icon: 'icon-zhongxindakai',
      name: t('taskSprint.dropdownMenu.reopen'),
      noAuth: true,
      permission: 'modify'
    },
    {
      key: 'restart',
      icon: 'icon-zhongxinkaishi',
      name: t('taskSprint.dropdownMenu.restart'),
      noAuth: true,
      permission: 'modify'
    },
    {
      key: 'viewBurnDown',
      icon: 'icon-jiankong',
      noAuth: true,
      name: t('taskSprint.dropdownMenu.viewBurndown')
    },
    {
      key: 'viewProgress',
      icon: 'icon-jiankong',
      noAuth: true,
      name: t('taskSprint.dropdownMenu.viewProgress')
    },
    {
      key: 'viewWorkCalendar',
      icon: 'icon-jiankong',
      noAuth: true,
      name: t('taskSprint.dropdownMenu.viewWorkCalendar')
    },
    {
      key: 'export',
      icon: 'icon-daochu',
      name: t('taskSprint.dropdownMenu.export'),
      permission: 'export'
    }
  ].filter(Boolean);
};

onMounted(() => {
  watch(() => props.projectId, () => {
    currentPage.value = 1;
    loadSprintData();
  }, { immediate: true });

  watch(() => props.notify, (newValue) => {
    if (!newValue) {
      return;
    }

    loadSprintData();
  }, { immediate: false });
});
</script>

<template>
  <div class="flex flex-col h-full overflow-auto px-5 py-5 leading-5 text-3">
    <div class="flex">
      <Introduce class="mb-7 flex-1" />
      <div class="flex flex-col w-145 ml-15">
        <div class="flex-1 flex flex-col justify-center">
          <img :src="ProcessPng" class="mt-2 items-center w-5/6" />
        </div>
      </div>
    </div>

    <div class="text-3.5 font-semibold mb-1">{{ t('taskSprint.addedSprints') }}</div>
    <Spin :spinning="isLoading" class="flex-1 flex flex-col">
      <template v-if="isDataLoaded">
        <div v-if="!hasActiveSearch && sprintList.length === 0" class="flex-1 flex flex-col items-center justify-center">
          <img src="../../../../assets/images/nodata.png">
          <div class="flex items-center text-theme-sub-content text-3.5 leading-5 space-x-1">
            <span>{{ t('taskSprint.notAddedYet') }}</span>
            <RouterLink class="router-link flex-1 truncate" :to="`/task#sprint?type=ADD`">
              {{ t('taskSprint.addSprint') }}
            </RouterLink>
          </div>
        </div>

        <template v-else>
          <SearchPanel
            :loading="isLoading"
            @change="handleSearchChange"
            @refresh="handleRefresh" />

          <NoData v-if="sprintList.length === 0" class="flex-1" />

          <template v-else>
            <div
              v-for="(item, index) in sprintList"
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
                    :disabled="(!isCurrentUserAdmin && !sprintPermissionsMap.get(item.id)?.includes(TaskSprintPermission.MODIFY_SPRINT))
                      || ![TaskSprintStatus.PENDING].includes(item.status?.value)"
                    size="small"
                    type="text"
                    class="px-0 flex items-center"
                    @click="startSprint(item, index)">
                    <Icon icon="icon-kaishi" class="mr-0.5" />
                    <span>{{ t('taskSprint.actions.start') }}</span>
                  </Button>

                  <Button
                    :disabled="(!isCurrentUserAdmin && !sprintPermissionsMap.get(item.id)?.includes(TaskSprintPermission.MODIFY_SPRINT))
                      || ![TaskSprintStatus.IN_PROGRESS].includes(item.status?.value)"
                    size="small"
                    type="text"
                    class="px-0 flex items-center"
                    @click="completeSprint(item, index)">
                    <Icon icon="icon-yiwancheng" class="mr-0.5" />
                    <span>{{ t('taskSprint.actions.complete') }}</span>
                  </Button>

                  <Dropdown
                    :admin="false"
                    :menuItems="getDropdownMenuItems()"
                    :permissions="dropdownPermissionsMap.get(item.id)"
                    @click="handleDropdownClick(item, index, $event.key)">
                    <Icon icon="icon-gengduo" class="cursor-pointer outline-none items-center" />
                  </Dropdown>
                </div>
              </div>
            </div>

            <Pagination
              v-if="totalCount > 5"
              :current="currentPage"
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

    <AsyncComponent :visible="isBurndownModalVisible">
      <BurndownChart
        v-model:visible="isBurndownModalVisible"
        :sprintId="selectedSprint?.id" />
    </AsyncComponent>
    <AsyncComponent :visible="isProgressModalVisible">
      <MemberProgressModal
        v-model:visible="isProgressModalVisible"
        :sprintId="selectedSprint?.id"
        :projectId="props.projectId" />
    </AsyncComponent>

    <AsyncComponent :visible="isAuthorizeModalVisible">
      <AuthorizeModal
        v-model:visible="isAuthorizeModalVisible"
        enumKey="TaskSprintPermission"
        :appId="props.appInfo?.id"
        :listUrl="`${TESTER}/task/sprint/auth?sprintId=${selectedSprint?.id}`"
        :delUrl="`${TESTER}/task/sprint/auth`"
        :addUrl="`${TESTER}/task/sprint/${selectedSprint?.id}/auth`"
        :updateUrl="`${TESTER}/task/sprint/auth`"
        :enabledUrl="`${TESTER}/task/sprint/${selectedSprint?.id}/auth/enabled`"
        :initStatusUrl="`${TESTER}/task/sprint/${selectedSprint?.id}/auth/status`"
        :onTips="t('taskSprint.anthModal.onTips')"
        :offTips="t('taskSprint.anthModal.offTips')"
        :title="t('taskSprint.anthModal.title')"
        @change="handleAuthFlagChange" />
    </AsyncComponent>

    <AsyncComponent :visible="isWorkCalendarModalVisible">
      <WorkCalendarModal
        v-model:visible="isWorkCalendarModalVisible"
        :sprintId="selectedSprint?.id"
        :projectId="props.projectId" />
    </AsyncComponent>
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
