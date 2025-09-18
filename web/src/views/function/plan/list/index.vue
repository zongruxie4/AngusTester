<script setup lang="ts">
import { computed, defineAsyncComponent, inject, onMounted, ref, watch } from 'vue';
import { Avatar, Button, Pagination, Progress } from 'ant-design-vue';
import { UserOutlined } from '@ant-design/icons-vue';
import {
  AsyncComponent,
  Colon,
  Dropdown,
  Icon,
  Image,
  modal,
  NoData,
  notification,
  Popover,
  Spin
} from '@xcan-angus/vue-ui';
import { utils, TESTER, download, appContext } from '@xcan-angus/infra';
import dayjs from 'dayjs';
import ProcessPng from './images/process.png';
import { funcPlan } from '@/api/tester';
import { useI18n } from 'vue-i18n';

import { FuncPlanStatus } from '@/enums/enums';
import { PlanInfo } from '../types';
import SearchPanel from '@/views/function/plan/list/SearchPanel.vue';

const AuthorizeModal = defineAsyncComponent(() => import('@/components/AuthorizeModal/index.vue'));

const { t } = useI18n();

type Props = {
  projectId: string;
  userInfo: { id: string; };
  appInfo: { id: string; };
  notify: string;
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  notify: undefined
});

type OrderByKey = 'createdDate' | 'createdByName';
type OrderSortKey = 'ASC' | 'DESC';

const Introduce = defineAsyncComponent(() => import('@/views/function/plan/list/Introduce.vue'));
const ProgressModal = defineAsyncComponent(() => import('@/views/function/plan/list/MemberProgress.vue'));
const BurnDownModal = defineAsyncComponent(() => import('@/views/function/plan/list/BurndownChart.vue'));
const WorkCalendarModal = defineAsyncComponent(() => import('@/views/function/plan/list/WorkCalendar.vue'));
const RichText = defineAsyncComponent(() => import('@/components/richEditor/textContent/index.vue'));

const deleteTabPane = inject<(keys: string[]) => void>('deleteTabPane', () => ({}));
const setCaseListPlanParam = inject<(value: any) => void>('setCaseListPlanParam');
const isAdmin = computed(() => appContext.isAdmin());

const loaded = ref(false);
const loading = ref(false);
const exportLoadingSet = ref<Set<string>>(new Set());
const searchedFlag = ref(false);

const searchPanelParams = ref({
  orderBy: undefined,
  orderSort: undefined,
  filters: []
});
const pageNo = ref(1);
const pageSize = ref(5);

const total = ref(0);
const dataList = ref<PlanInfo[]>([]);
const permissionsMap = ref<Map<string, string[]>>(new Map());

const selectedData = ref<PlanInfo>();
const authorizeModalVisible = ref(false);
const progressVisible = ref(false);
const burndownVisible = ref(false);
const workCalendarVisible = ref(false);

const refresh = () => {
  pageNo.value = 1;
  permissionsMap.value.clear();
  loadData();
};

const searchChange = (data) => {
  searchPanelParams.value = data;
  pageNo.value = 1;
  loadData();
};

// 查看成员进度
const viewProgress = (data: PlanInfo) => {
  progressVisible.value = true;
  selectedData.value = data;
};

// 查看燃尽图
const viewBurnDown = (data: PlanInfo) => {
  burndownVisible.value = true;
  selectedData.value = data;
};

// 查看工作日历
const viewWrokCalendar = (data: PlanInfo) => {
  workCalendarVisible.value = true;
  selectedData.value = data;
};

const setTableData = async (id: string, index: number) => {
  const [error, res] = await funcPlan.getPlanDetail(id);
  loading.value = false;
  if (error) {
    return;
  }

  if (res?.data) {
    dataList.value[index] = res?.data;
  }
};

const toStart = async (data: PlanInfo, index: number) => {
  loading.value = true;
  const id = data.id;
  const [error] = await funcPlan.startPlan(id);
  if (error) {
    loading.value = false;
    return;
  }

  notification.success(t('functionPlan.list.planStartSuccess'));
  setTableData(id, index);
};

const toCompleted = async (data: PlanInfo, index: number) => {
  loading.value = true;
  const id = data.id;
  const [error] = await funcPlan.endPlan(id);
  if (error) {
    loading.value = false;
    return;
  }

  notification.success(t('functionPlan.list.planCompletedSuccess'));
  setTableData(id, index);
};

const toBlock = async (data: PlanInfo, index: number) => {
  loading.value = true;
  const id = data.id;
  const [error] = await funcPlan.blockPlan(id);
  if (error) {
    loading.value = false;
    return;
  }

  notification.success(t('functionPlan.list.planBlockedSuccess'));
  setTableData(id, index);
};

const toDelete = async (data: PlanInfo) => {
  modal.confirm({
    content: t('functionPlan.list.confirmDeletePlan', { name: data.name }),
    async onOk () {
      const id = data.id;
      const [error] = await funcPlan.deletePlan(id);
      if (error) {
        return;
      }

      notification.success(t('functionPlan.list.planDeleteSuccess'));
      loadData();

      deleteTabPane([id]);
    }
  });
};

const toGrant = (data: PlanInfo) => {
  selectedData.value = data;
  authorizeModalVisible.value = true;
};

const authFlagChange = async ({ auth }: { auth: boolean }) => {
  const _list = dataList.value;
  const targetId = selectedData.value?.id;
  for (let i = 0, len = _list.length; i < len; i++) {
    if (_list[i].id === targetId) {
      _list[i].auth = auth;
      break;
    }
  }
};

const toClone = async (data: PlanInfo) => {
  const [error] = await funcPlan.clonePlan(data.id);
  if (error) {
    return;
  }

  notification.success(t('functionPlan.list.planCloneSuccess'));
  loadData();
};

const toResetTestResult = async (data: PlanInfo) => {
  loading.value = true;
  const id = data.id;
  const params = { ids: [id] };
  const [error] = await funcPlan.resetCaseResult(params);
  loading.value = false;
  if (error) {
    return;
  }

  notification.success(t('functionPlan.list.planResetTestSuccess'));
};

const toResetReviewResult = async (data: PlanInfo) => {
  loading.value = true;
  const id = data.id;
  const params = { ids: [id] };
  const [error] = await funcPlan.resetCaseReview(params);
  loading.value = false;
  if (error) {
    return;
  }

  notification.success(t('functionPlan.list.planResetReviewSuccess'));
};

const toExport = async (data: PlanInfo) => {
  const { id, projectId } = data;
  if (exportLoadingSet.value.has(id)) {
    return;
  }

  exportLoadingSet.value.add(id);
  await download(`${TESTER}/func/case/export?projectId=${projectId}&planId=${id}`);
  exportLoadingSet.value.delete(id);
};

const dropdownClick = (data: PlanInfo, index: number, key: 'clone' | 'block' | 'delete' | 'export' | 'grant' | 'resetTestResult' | 'resetReviewResult' | 'viewBurnDown' | 'viewProgress' | 'viewWrokCalendar') => {
  switch (key) {
    case 'block':
      toBlock(data, index);
      break;
    case 'delete':
      toDelete(data);
      break;
    case 'grant':
      toGrant(data);
      break;
    case 'clone':
      toClone(data);
      break;
    case 'resetTestResult':
      toResetTestResult(data);
      break;
    case 'resetReviewResult':
      toResetReviewResult(data);
      break;
    case 'export':
      toExport(data);
      break;
    case 'viewBurnDown':
      viewBurnDown(data);
      break;
    case 'viewProgress':
      viewProgress(data);
      break;
    case 'viewWrokCalendar':
      viewWrokCalendar(data);
      break;
  }
};

const paginationChange = (_pageNo: number, _pageSize: number) => {
  pageNo.value = _pageNo;
  pageSize.value = _pageSize;
  loadData();
};

const loadData = async () => {
  loading.value = true;
  const params: {
    projectId: string;
    pageNo: number;
    pageSize: number;
    orderBy?: OrderByKey;
    orderSort?: OrderSortKey;
    filters?: { key: string; op: string; value: string; }[];
  } = {
    projectId: props.projectId,
    pageNo: pageNo.value,
    pageSize: pageSize.value,
    ...searchPanelParams.value
  };

  const [error, res] = await funcPlan.getPlanList(params);
  loaded.value = true;
  loading.value = false;

  if (params.filters?.length || params.orderBy) {
    searchedFlag.value = true;
  } else {
    searchedFlag.value = false;
  }

  if (error) {
    dataList.value = [];
    return;
  }

  const data = res?.data;
  if (data) {
    total.value = +data.total;

    const _list = (data.list || [] as PlanInfo[]);
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

    // 管理员拥有所有权限，无需加载权限
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

const loadPermissions = async (id: string) => {
  const params = {
    admin: true
  };

  return await funcPlan.getCurrentAuthByPlanId(id, params);
};

const goCase = (plan: PlanInfo) => {
  setCaseListPlanParam({ ...plan, planId: plan.id, planName: plan.name });
};

const reset = () => {
  pageNo.value = 1;
  dataList.value = [];
};

onMounted(() => {
  watch(() => props.projectId, () => {
    reset();
    loadData();
  }, { immediate: true });

  watch(() => props.notify, (newValue) => {
    if (!newValue) {
      return;
    }

    loadData();
  }, { immediate: false });
});

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
      if ((_isAdmin || _permissions.includes('MODIFY_PLAN')) && ['PENDING', 'IN_PROGRESS'].includes(_status)) {
        tempPermissions.push('block');
      }

      if (_isAdmin || _permissions.includes('DELETE_PLAN')) {
        tempPermissions.push('delete');
      }

      if (_isAdmin || _permissions.includes('GRANT')) {
        tempPermissions.push('grant');
      }

      if (_isAdmin || _permissions.includes('RESET_TEST_RESULT')) {
        tempPermissions.push('resetTestResult');
      }

      if (_isAdmin || _permissions.includes('RESET_REVIEW_RESULT')) {
        tempPermissions.push('resetReviewResult');
      }

      if (_isAdmin || _permissions.includes('EXPORT_CASE')) {
        tempPermissions.push('export');
      }

      map.set(id, tempPermissions);
    }
  }

  return map;
});

const dropdownMenuItems = [
  {
    key: 'block',
    icon: 'icon-zusai',
    name: t('functionPlan.list.block'),
    permission: 'block'
  },
  {
    key: 'delete',
    icon: 'icon-qingchu',
    name: t('functionPlan.list.delete'),
    permission: 'delete'
  },
  {
    key: 'grant',
    icon: 'icon-quanxian1',
    name: t('functionPlan.list.permission'),
    permission: 'grant'
  },
  {
    key: 'clone',
    icon: 'icon-fuzhi',
    name: t('functionPlan.list.clone'),
    noAuth: true,
    permission: 'clone'
  },
  {
    key: 'resetTestResult',
    icon: 'icon-zhongzhiceshijieguo',
    name: t('functionPlan.list.resetTest'),
    permission: 'resetTestResult'
  },
  {
    key: 'resetReviewResult',
    icon: 'icon-zhongzhipingshenjieguo',
    name: t('functionPlan.list.resetReview'),
    permission: 'resetReviewResult'
  },
  {
    key: 'viewBurnDown',
    icon: 'icon-jiankong',
    noAuth: true,
    name: t('functionPlan.list.viewBurnDown')
  },
  {
    key: 'viewProgress',
    icon: 'icon-jiankong',
    noAuth: true,
    name: t('functionPlan.list.viewProgress')
  },
  {
    key: 'viewWrokCalendar',
    icon: 'icon-jiankong',
    noAuth: true,
    name: t('functionPlan.list.viewWorkCalendar')
  },
  {
    key: 'export',
    icon: 'icon-daochu',
    name: t('functionPlan.list.exportCase'),
    permission: 'export'
  }
];

const pageSizeOptions = ['5', '10', '15', '20', '30'];

</script>

<template>
  <div class="flex flex-col h-full overflow-auto px-5 py-5 leading-5 text-3">
    <div class="flex space-x-2">
      <Introduce class="mb-7 flex-1" />
      <div class="flex flex-col w-145">
        <div class="text-3.5 font-semibold mb-2.5">{{ t('functionPlan.list.agileTesting') }}</div>
        <div>
          {{ t('functionPlan.list.agileTestingDesc') }}
        </div>
        <div class="flex-1 flex flex-col justify-center">
          <img :src="ProcessPng" class="mt-2 items-center" />
        </div>
      </div>
    </div>

    <div class="text-3.5 font-semibold mb-1">{{ t('functionPlan.list.addedPlans') }}</div>
    <Spin :spinning="loading" class="flex-1 flex flex-col">
      <template v-if="loaded">
        <div v-if="!searchedFlag && dataList.length === 0" class="flex-1 flex flex-col items-center justify-center">
          <img src="../../../../assets/images/nodata.png">
          <div class="flex items-center text-theme-sub-content text-3.5 leading-5 space-x-1">
            <span>{{ t('functionPlan.list.noPlans') }}</span>
            <RouterLink class="router-link flex-1 truncate" :to="`/function#plans?type=ADD`">
              {{ t('functionPlan.list.addPlan') }}
            </RouterLink>
          </div>
        </div>

        <template v-else>
          <SearchPanel
            :loading="loading"
            @change="searchChange"
            @refresh="refresh" />
          <NoData v-if="dataList.length === 0" class="flex-1" />

          <template v-else>
            <div
              v-for="(item, index) in dataList"
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
                    :disabled="!isAdmin && !permissionsMap.get(item.id)?.includes('VIEW')"
                    size="small"
                    type="text"
                    class="px-0 flex items-center space-x-1"
                    @click="goCase(item)">
                    <Icon icon="icon-ceshiyongli1" class="text-3.5" />
                    <span>{{ t('functionPlan.list.viewCases') }}</span>
                  </Button>

                  <Button
                    :disabled="(!isAdmin && !permissionsMap.get(item.id)?.includes('MODIFY_PLAN')) || !['PENDING', 'BLOCKED', 'COMPLETED'].includes(item.status?.value)"
                    size="small"
                    type="text"
                    class="px-0 flex items-center space-x-1"
                    @click="toStart(item, index)">
                    <Icon icon="icon-kaishi" class="text-3.5" />
                    <span>{{ item.status.value === 'COMPLETED' ? t('functionPlan.list.restart') : t('functionPlan.list.start') }}</span>
                  </Button>

                  <Button
                    :disabled="(!isAdmin && !permissionsMap.get(item.id)?.includes('MODIFY_PLAN')) || !['IN_PROGRESS'].includes(item.status?.value)"
                    size="small"
                    type="text"
                    class="px-0 flex items-center space-x-1"
                    @click="toCompleted(item, index)">
                    <Icon icon="icon-yiwancheng" class="text-3.5" />
                    <span>{{ t('functionPlan.list.complete') }}</span>
                  </Button>

                  <Dropdown
                    :admin="false"
                    :menuItems="dropdownMenuItems"
                    :permissions="dropdownPermissionsMap.get(item.id)"
                    @click="dropdownClick(item, index, $event.key)">
                    <Icon icon="icon-gengduo" class="cursor-pointer outline-none items-center" />
                  </Dropdown>
                </div>
              </div>
            </div>

            <Pagination
              v-if="total > 5"
              :current="pageNo"
              :pageSize="pageSize"
              :pageSizeOptions="pageSizeOptions"
              :total="total"
              :hideOnSinglePage="false"
              showSizeChanger
              size="default"
              class="text-right"
              @change="paginationChange" />
          </template>
        </template>
      </template>
    </Spin>
    <AsyncComponent :visible="burndownVisible">
      <BurnDownModal
        v-model:visible="burndownVisible"
        :planId="selectedData?.id" />
    </AsyncComponent>

    <AsyncComponent :visible="progressVisible">
      <ProgressModal
        v-model:visible="progressVisible"
        :planId="selectedData?.id"
        :projectId="props.projectId" />
    </AsyncComponent>

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
        :onTips="t('functionPlan.list.permissionControlOnTips')"
        :offTips="t('functionPlan.list.permissionControlOffTips')"
        :title="t('functionPlan.list.planPermission')"
        @change="authFlagChange" />
    </AsyncComponent>

    <AsyncComponent :visible="workCalendarVisible">
      <WorkCalendarModal
        v-model:visible="workCalendarVisible"
        :projectId="props.projectId"
        :planId="selectedData.id" />
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
