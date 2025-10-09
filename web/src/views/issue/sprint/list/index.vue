<script setup lang="ts">
import { computed, defineAsyncComponent, inject, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import {
  AsyncComponent, modal, notification, Spin
} from '@xcan-angus/vue-ui';
import { appContext, download, ProjectPageQuery, TESTER, utils } from '@xcan-angus/infra';
import ProcessPng from './images/process.png';
import { issue } from '@/api/tester';
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
const SearchPanel = defineAsyncComponent(() => import('@/views/issue/sprint/list/SearchPanel.vue'));
const Introduce = defineAsyncComponent(() => import('@/views/issue/sprint/list/Introduce.vue'));
const BurndownChart = defineAsyncComponent(() => import('@/views/issue/sprint/list/BurndownChart.vue'));
const MemberProgressModal = defineAsyncComponent(() => import('@/views/issue/sprint/list/MemberProgress.vue'));
const WorkCalendarModal = defineAsyncComponent(() => import('@/views/issue/sprint/list/WorkCalendar.vue'));
const AuthorizeModal = defineAsyncComponent(() => import('@/components/AuthorizeModal/index.vue'));
const SprintList = defineAsyncComponent(() => import('@/views/issue/sprint/list/List.vue'));

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
const pageSize = ref(4);
const pageSizeOptions = ['4', '10', '15', '20', '30'];
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
  const [error, response] = await issue.getSprintDetail(sprintId);
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
  const [error] = await issue.reopenSprint({
    ids: [sprint.id]
  }, {
    paramsType: true
  });
  isLoading.value = false;
  if (error) {
    return;
  }
  notification.success(t('actions.tips.reopenSuccess'));
  await updateSprintData(sprint.id, index);
};

/**
 * Restart a sprint
 * @param sprint - Sprint data to restart
 * @param index - Index of the sprint in the list
 */
const restartSprint = async (sprint: SprintInfo, index: number) => {
  isLoading.value = true;
  const [error] = await issue.restartSprint({ ids: [sprint.id] }, { paramsType: true });
  isLoading.value = false;
  if (error) {
    return;
  }
  notification.success(t('actions.tips.restartSuccess'));
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
  const [error] = await issue.startSprint(sprintId);
  isLoading.value = false;
  if (error) {
    isLoading.value = false;
    return;
  }

  notification.success(t('actions.tips.startSuccess'));
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
  const [error] = await issue.endSprint(sprintId);
  if (error) {
    isLoading.value = false;
    return;
  }

  notification.success(t('actions.tips.completeSuccess'));
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
  const [error] = await issue.blockSprint(sprintId);
  if (error) {
    return;
  }

  notification.success(t('actions.tips.blockSuccess'));
  await updateSprintData(sprintId, index);
};

/**
 * Delete a sprint with confirmation
 * @param sprint - Sprint data to delete
 */
const deleteSprint = async (sprint: SprintInfo) => {
  modal.confirm({
    content: t('actions.tips.confirmDelete', { name: sprint.name }),
    async onOk () {
      const sprintId = sprint.id;
      const [error] = await issue.deleteSprint(sprintId);
      if (error) {
        return;
      }

      notification.success(t('actions.tips.deleteSuccess'));
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
  const [error] = await issue.cloneSprint(sprint.id);
  if (error) {
    return;
  }
  notification.success(t('actions.tips.cloneSuccess'));
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
  action: string
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

  const [error, response] = await issue.getSprintList(params);
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
  return await issue.getCurrentUserSprintAuth(sprintId, params);
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
      name: t('actions.block'),
      permission: 'block'
    },
    {
      key: 'delete',
      icon: 'icon-qingchu',
      name: t('actions.delete'),
      permission: 'delete'
    },
    {
      key: 'grant',
      icon: 'icon-quanxian1',
      name: t('actions.permission'),
      permission: 'grant'
    },
    {
      key: 'clone',
      icon: 'icon-fuzhi',
      name: t('actions.clone'),
      noAuth: true,
      permission: 'clone'
    },
    {
      key: 'reopen',
      icon: 'icon-zhongxindakai',
      name: t('actions.reopen'),
      noAuth: true,
      permission: 'modify'
    },
    {
      key: 'restart',
      icon: 'icon-zhongxinkaishi',
      name: t('actions.restart'),
      noAuth: true,
      permission: 'modify'
    },
    {
      key: 'viewBurnDown',
      icon: 'icon-jiankong',
      noAuth: true,
      name: t('sprint.actions.dropdownMenu.viewBurndown')
    },
    {
      key: 'viewProgress',
      icon: 'icon-jiankong',
      noAuth: true,
      name: t('sprint.actions.dropdownMenu.viewProgress')
    },
    {
      key: 'viewWorkCalendar',
      icon: 'icon-jiankong',
      noAuth: true,
      name: t('sprint.actions.dropdownMenu.viewWorkCalendar')
    },
    {
      key: 'export',
      icon: 'icon-daochu',
      name: t('actions.export'),
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

    <div class="text-3.5 font-semibold mb-1">{{ t('sprint.addedSprints') }}</div>
    <Spin :spinning="isLoading" class="flex-1 flex flex-col">
      <template v-if="isDataLoaded">
        <div v-if="!hasActiveSearch && sprintList.length === 0" class="flex-1 flex flex-col items-center justify-center">
          <img src="../../../../assets/images/nodata.png">
          <div class="flex items-center text-theme-sub-content text-3.5 leading-5 space-x-1">
            <span>{{ t('sprint.notAddedYet') }}</span>
            <RouterLink class="router-link flex-1 truncate" :to="`/issue#sprint?type=ADD`">
              {{ t('sprint.actions.addSprint') }}
            </RouterLink>
          </div>
        </div>

        <template v-else>
          <SearchPanel
            :loading="isLoading"
            @change="handleSearchChange"
            @refresh="handleRefresh" />

          <SprintList
            :sprintList="sprintList"
            :totalCount="totalCount"
            :currentPage="currentPage"
            :pageSize="pageSize"
            :pageSizeOptions="pageSizeOptions"
            :isCurrentUserAdmin="isCurrentUserAdmin"
            :sprintPermissionsMap="sprintPermissionsMap"
            :dropdownPermissionsMap="dropdownPermissionsMap"
            :getDropdownMenuItems="getDropdownMenuItems"
            :handleDropdownClick="handleDropdownClick"
            :startSprint="startSprint"
            :completeSprint="completeSprint"
            :handlePaginationChange="handlePaginationChange" />
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
        :enumKey="TaskSprintPermission"
        :appId="props.appInfo?.id"
        :listUrl="`${TESTER}/task/sprint/auth?sprintId=${selectedSprint?.id}`"
        :delUrl="`${TESTER}/task/sprint/auth`"
        :addUrl="`${TESTER}/task/sprint/${selectedSprint?.id}/auth`"
        :updateUrl="`${TESTER}/task/sprint/auth`"
        :enabledUrl="`${TESTER}/task/sprint/${selectedSprint?.id}/auth/enabled`"
        :initStatusUrl="`${TESTER}/task/sprint/${selectedSprint?.id}/auth/status`"
        :onTips="t('sprint.anthModal.onTips')"
        :offTips="t('sprint.anthModal.offTips')"
        :title="t('sprint.anthModal.title')"
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
