<script setup lang="ts">
import { computed, defineAsyncComponent, inject, onMounted, ref, watch } from 'vue';
import { AsyncComponent, notification, Spin, modal } from '@xcan-angus/vue-ui';
import { appContext, TESTER, utils, ProjectPageQuery } from '@xcan-angus/infra';
import { testPlan } from '@/api/tester';
import { useI18n } from 'vue-i18n';
import { BasicProps } from '@/types/types';
import { FuncPlanStatus, FuncPlanPermission } from '@/enums/enums';
import { PlanDetail } from '../types';

import ProcessPng from './images/process.png';

// Async components
const Introduce = defineAsyncComponent(() => import('@/views/test/plan/list/Introduce.vue'));
const SearchPanel = defineAsyncComponent(() => import('@/views/test/plan/list/SearchPanel.vue'));
const List = defineAsyncComponent(() => import('@/views/test/plan/list/List.vue'));

const AuthorizeModal = defineAsyncComponent(() => import('@/components/AuthorizeModal/index.vue'));

// Composables
const { t } = useI18n();

// Props and injects
const props = withDefaults(defineProps<BasicProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  notify: undefined
});

const deleteTabPane = inject<(keys: string[]) => void>('deleteTabPane', () => ({}));
const setCaseListPlanParam = inject<(value: any) => void>('setCaseListPlanParam');

// Computed properties
const isAdmin = computed(() => appContext.isAdmin());

// Reactive data
const isDataLoaded = ref(false);
const isLoading = ref(false);
const searchedFlag = ref(false);

// Search and pagination state
const searchPanelParams = ref({
  orderBy: undefined,
  orderSort: undefined,
  filters: []
});
const pageNo = ref(1);
const pageSize = ref(4);
const pageSizeOptions = ['4', '10', '15', '20', '30'];

// Data state
const total = ref(0);
const dataList = ref<PlanDetail[]>([]);
const permissionsMap = ref<Map<string, string[]>>(new Map());

// Modal state
const selectedData = ref<PlanDetail>();
const authorizeModalVisible = ref(false);

/**
 * Refreshes the data list and resets pagination
 */
const refresh = () => {
  pageNo.value = 1;
  permissionsMap.value.clear();
  loadData();
};

/**
 * Handles search panel parameter changes and reloads data
 * @param data - Search parameters from search panel
 */
const handleSearchChange = (data) => {
  searchPanelParams.value = data;
  pageNo.value = 1;
  loadData();
};

/**
 * Updates table data for a specific plan by index
 * @param id - Plan ID
 * @param index - Index in the data list
 */
const updateTableData = async (id: string, index: number) => {
  const [error, res] = await testPlan.getPlanDetail(id);
  isLoading.value = false;
  if (error) {
    return;
  }

  if (res?.data) {
    const item = res.data as PlanDetail;

    if (item.progress?.completedRate) {
      item.progress.completedRate = item.progress.completedRate.replace(/(\d+\.\d{2})\d+/, '$1');
    }

    if (item.attachments?.length) {
      item.attachments = item.attachments.map(att => ({
        ...att,
        id: utils.uuid()
      }));
    }

    if (item.members) {
      item.showMembers = item.members.slice(0, 10);
    }

    dataList.value.splice(index, 1, item);
  }
};

/**
 * Starts a plan and updates the table data
 * @param data - Plan detail data
 * @param index - Index in the data list
 */
const handleStartPlan = async (data: PlanDetail, index: number) => {
  isLoading.value = true;
  const id = data.id;
  const [error] = await testPlan.startPlan(id);
  if (error) {
    isLoading.value = false;
    return;
  }

  notification.success(t('actions.tips.startSuccess'));
  await updateTableData(id, index);
};

/**
 * Completes a plan and updates the table data
 * @param data - Plan detail data
 * @param index - Index in the data list
 */
const handleCompletePlan = async (data: PlanDetail, index: number) => {
  isLoading.value = true;
  const id = data.id;
  const [error] = await testPlan.endPlan(id);
  if (error) {
    isLoading.value = false;
    return;
  }

  notification.success(t('actions.tips.completeSuccess'));
  await updateTableData(id, index);
};

/**
 * Blocks a plan and updates the table data
 * @param data - Plan detail data
 * @param index - Index in the data list
 */
const handleBlockPlan = async (data: PlanDetail, index: number) => {
  isLoading.value = true;
  const id = data.id;
  const [error] = await testPlan.blockPlan(id);
  if (error) {
    isLoading.value = false;
    return;
  }

  notification.success(t('actions.tips.blockSuccess'));
  await updateTableData(id, index);
};

/**
 * Deletes a plan with confirmation
 * @param planData - The plan data to delete
 */
const handleDeletePlan = async (planData: PlanDetail) => {
  modal.confirm({
    content: t('actions.tips.confirmDelete', { name: planData.name }),
    async onOk () {
      const id = planData.id;
      const [error] = await testPlan.deletePlan(id);
      if (error) {
        return;
      }

      notification.success(t('actions.tips.deleteSuccess'));
      refresh();

      deleteTabPane([id]);
    }
  });
};

/**
 * Opens the authorization modal for the selected plan
 * @param data - Plan detail data
 */
const handleGrantPermission = (data: PlanDetail) => {
  selectedData.value = data;
  authorizeModalVisible.value = true;
};

/**
 * Handles authorization flag change and updates the data list
 * @param auth - Authorization status
 */
const handleAuthFlagChange = async ({ auth }: { auth: boolean }) => {
  const _list = dataList.value;
  const targetId = selectedData.value?.id;
  for (let i = 0, len = _list.length; i < len; i++) {
    if (_list[i].id === targetId) {
      _list[i].auth = auth;
      break;
    }
  }
};

/**
 * Navigates to cases for the selected plan
 * @param data - Plan detail data
 */
const handleGoToCases = (data: PlanDetail) => {
  setCaseListPlanParam(data);
};

/**
 * Handles pagination changes and reloads data
 * @param _pageNo - New page number
 * @param _pageSize - New page size
 */
const handlePaginationChange = (_pageNo: number, _pageSize: number) => {
  pageNo.value = _pageNo;
  pageSize.value = _pageSize;
  loadData();
};

/**
 * Loads plan list data from API with search and pagination parameters
 */
const loadData = async () => {
  isLoading.value = true;
  const params: ProjectPageQuery = {
    projectId: props.projectId,
    pageNo: pageNo.value,
    pageSize: pageSize.value,
    ...searchPanelParams.value
  };

  const [error, res] = await testPlan.getPlanList(params);
  isDataLoaded.value = true;
  isLoading.value = false;

  searchedFlag.value = !!(params.filters?.length || params.orderBy);

  if (error) {
    dataList.value = [];
    return;
  }

  const data = res?.data;
  if (data) {
    total.value = +data.total;

    const _list = (data.list || [] as PlanDetail[]);
    dataList.value = _list.map(item => {
      if (item.progress?.completedRate) {
        item.progress.completedRate = item.progress.completedRate.replace(/(\d+\.\d{2})\d+/, '$1');
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
    if (!isAdmin.value) {
      for (let i = 0, len = _list.length; i < len; i++) {
        const id = _list[i].id;
        if (!permissionsMap.value.get(id)) {
          const [_error, _res] = await loadPermissions(id);
          if (!_error) {
            const _permissions = (_res?.data?.permissions || []).map(item => item.value);
            permissionsMap.value.set(id, _permissions);
          }
        }
      }
    }
  }
};

/**
 * Loads permissions for a specific plan
 * @param id - Plan ID
 * @returns Promise with permission data
 */
const loadPermissions = async (id: string) => {
  const params = {
    admin: true
  };
  return await testPlan.getCurrentAuthByPlanId(id, params);
};

/**
 * Resets pagination and clears data list
 */
const resetData = () => {
  pageNo.value = 1;
  dataList.value = [];
};

// computed properties
const dropdownPermissionsMap = computed(() => {
  const map = new Map<string, string[]>();
  if (dataList.value.length) {
    const _isAdmin = isAdmin.value;
    const _permissionsMap = permissionsMap.value;
    const list = dataList.value;
    for (let i = 0, len = list.length; i < len; i++) {
      const { id, status } = list[i];
      const _permissions: string[] = _permissionsMap.get(id) || [];
      const tempPermissions: string[] = [];
      const _status = status.value;
      if ((_isAdmin || _permissions.includes(FuncPlanPermission.MODIFY_PLAN)) && [FuncPlanStatus.PENDING, FuncPlanStatus.IN_PROGRESS].includes(_status)) {
        tempPermissions.push('block');
      }

      if (_isAdmin || _permissions.includes(FuncPlanPermission.DELETE_PLAN)) {
        tempPermissions.push('delete');
      }

      if (_isAdmin || _permissions.includes(FuncPlanPermission.GRANT)) {
        tempPermissions.push('grant');
      }

      if (_isAdmin || _permissions.includes(FuncPlanPermission.RESET_TEST_RESULT)) {
        tempPermissions.push('resetTestResult');
      }

      if (_isAdmin || _permissions.includes(FuncPlanPermission.RESET_REVIEW_RESULT)) {
        tempPermissions.push('resetReviewResult');
      }

      if (_isAdmin || _permissions.includes(FuncPlanPermission.EXPORT_CASE)) {
        tempPermissions.push('export');
      }

      map.set(id, tempPermissions);
    }
  }
  return map;
});

// lifecycle hooks
onMounted(() => {
  watch(() => props.projectId, () => {
    resetData();
    loadData();
  }, { immediate: true });

  watch(() => props.notify, (newValue) => {
    if (!newValue) {
      return;
    }

    loadData();
  }, { immediate: false });
});
</script>

<template>
  <div class="flex flex-col h-full overflow-auto px-5 py-5 leading-5 text-3">
    <div class="flex">
      <Introduce class="mb-7 flex-1" />
      <div class="flex flex-col w-155 ml-10">
        <div class="flex-1 flex flex-col justify-center">
          <img :src="ProcessPng" class="mt-2 items-center" />
        </div>
      </div>
    </div>

    <div class="text-3.5 font-semibold mb-1">{{ t('testPlan.addedPlans') }}</div>
    <Spin :spinning="isLoading" class="flex-1 flex flex-col">
      <template v-if="isDataLoaded">
        <div v-if="!searchedFlag && dataList.length === 0" class="flex-1 flex flex-col items-center justify-center">
          <img src="../../../../assets/images/nodata.png">
          <div class="flex items-center text-theme-sub-content text-3.5 leading-5 space-x-1">
            <span>{{ t('testPlan.noPlans') }}</span>
            <RouterLink class="router-link flex-1 truncate" :to="`/test#plans?type=ADD`">
              {{ t('testPlan.actions.addPlan') }}
            </RouterLink>
          </div>
        </div>

        <template v-else>
          <SearchPanel
            :loading="isLoading"
            :userId="props.userInfo?.id"
            @change="handleSearchChange"
            @refresh="refresh" />

          <List
            :dataList="dataList"
            :loading="isLoading"
            :total="total"
            :pageNo="pageNo"
            :pageSize="pageSize"
            :pageSizeOptions="pageSizeOptions"
            :permissionsMap="permissionsMap"
            :dropdownPermissionsMap="dropdownPermissionsMap"
            :isAdmin="isAdmin"
            @paginationChange="handlePaginationChange"
            @startPlan="handleStartPlan"
            @completePlan="handleCompletePlan"
            @blockPlan="handleBlockPlan"
            @deletePlan="handleDeletePlan"
            @grantPermission="handleGrantPermission"
            @goToCases="handleGoToCases"
            @refresh="refresh" />
        </template>
      </template>
    </Spin>

    <AsyncComponent :visible="authorizeModalVisible">
      <AuthorizeModal
        v-model:visible="authorizeModalVisible"
        enumKey="FuncPlanPermission"
        :appId="props.appInfo?.id"
        :listUrl="`${TESTER}/func/plan/auth?planId=${selectedData?.id}`"
        :delUrl="`${TESTER}/func/plan/auth`"
        :addUrl="`${TESTER}/func/plan/${selectedData?.id}/auth`"
        :updateUrl="`${TESTER}/func/plan/auth`"
        :enabledUrl="`${TESTER}/func/plan/${selectedData?.id}/auth/enabled`"
        :initStatusUrl="`${TESTER}/func/plan/${selectedData?.id}/auth/status`"
        :onTips="t('testPlan.list.permissionControlOnTips')"
        :offTips="t('testPlan.list.permissionControlOffTips')"
        :title="t('common.permission')"
        @change="handleAuthFlagChange" />
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
