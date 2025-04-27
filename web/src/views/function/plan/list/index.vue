<script setup lang="ts">
import { computed, defineAsyncComponent, inject, onMounted, ref, watch } from 'vue';
import { Avatar, Button, Pagination, Progress } from 'ant-design-vue';
import { UserOutlined } from '@ant-design/icons-vue';
import {
  AsyncComponent,
  AuthorizeModal,
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
import { http, utils, TESTER, download } from '@xcan-angus/tools';
import dayjs from 'dayjs';
import ProcessPng from './images/process.png';

import { PlanInfo } from '../PropsType';

import SearchPanel from '@/views/function/plan/list/searchPanel/index.vue';

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

const Introduce = defineAsyncComponent(() => import('@/views/function/plan/list/introduce/index.vue'));
const ProgressModal = defineAsyncComponent(() => import('@/views/function/plan/components/progress/index.vue'));
const BurnDownModal = defineAsyncComponent(() => import('@/views/function/plan/components/burndownChart/index.vue'));
const WorkCalendarModal = defineAsyncComponent(() => import('@/views/function/plan/components/workCalendar/index.vue'));
const RichText = defineAsyncComponent(() => import('@/components/richEditor/textContent/index.vue'));

const deleteTabPane = inject<(keys: string[]) => void>('deleteTabPane', () => ({}));
const setCaseListPlanParam = inject<(value: any) => void>('setCaseListPlanParam');
const isAdmin = inject('isAdmin', ref(false));

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
  const [error, res] = await http.get(`${TESTER}/func/plan/${id}`);
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
  const [error] = await http.patch(`${TESTER}/func/plan/${id}/start`);
  if (error) {
    loading.value = false;
    return;
  }

  notification.success('计划开始成功');
  setTableData(id, index);
};

const toCompleted = async (data: PlanInfo, index: number) => {
  loading.value = true;
  const id = data.id;
  const [error] = await http.patch(`${TESTER}/func/plan/${id}/end`);
  if (error) {
    loading.value = false;
    return;
  }

  notification.success('计划已完成');
  setTableData(id, index);
};

const toBlock = async (data: PlanInfo, index: number) => {
  loading.value = true;
  const id = data.id;
  const [error] = await http.patch(`${TESTER}/func/plan/${id}/block`);
  if (error) {
    loading.value = false;
    return;
  }

  notification.success('计划标记为阻塞成功');
  setTableData(id, index);
};

const toDelete = async (data: PlanInfo) => {
  modal.confirm({
    content: `确定删除计划【${data.name}】吗？`,
    async onOk () {
      const id = data.id;
      const [error] = await http.del(`${TESTER}/func/plan/${id}`);
      if (error) {
        return;
      }

      notification.success('计划删除成功， 您可以在回收站查看删除后的计划');
      loadData();

      deleteTabPane([id]);
    }
  });
};

const toGrant = (data: PlanInfo) => {
  selectedData.value = data;
  authorizeModalVisible.value = true;
};

const authFlagChange = async ({ authFlag }: { authFlag: boolean }) => {
  const _list = dataList.value;
  const targetId = selectedData.value?.id;
  for (let i = 0, len = _list.length; i < len; i++) {
    if (_list[i].id === targetId) {
      _list[i].authFlag = authFlag;
      break;
    }
  }
};

const toClone = async (data: PlanInfo) => {
  const [error] = await http.patch(`${TESTER}/func/plan/${data.id}/clone`);
  if (error) {
    return;
  }

  notification.success('计划克隆成功');
  loadData();
};

const toResetTestResult = async (data: PlanInfo) => {
  loading.value = true;
  const id = data.id;
  const params = { ids: [id] };
  const [error] = await http.patch(`${TESTER}/func/plan/case/result/reset`, params, { paramsType: true });
  loading.value = false;
  if (error) {
    return;
  }

  notification.success('计划重置测试成功');
};

const toResetReviewResult = async (data: PlanInfo) => {
  loading.value = true;
  const id = data.id;
  const params = { ids: [id] };
  const [error] = await http.patch(`${TESTER}/func/plan/case/review/reset`, params, { paramsType: true });
  loading.value = false;
  if (error) {
    return;
  }

  notification.success('计划重置评审成功');
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

  const [error, res] = await http.get(`${TESTER}/func/plan/search`, params);
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
    adminFlag: true
  };

  return await http.get(`${TESTER}/func/plan/${id}/user/auth/current`, params);
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

const searchPanelOptions = [
  {
    valueKey: 'name',
    type: 'input',
    placeholder: '查询计划名称、描述',
    allowClear: true,
    maxlength: 100
  },
  {
    valueKey: 'reviewFlag',
    type: 'select',
    allowClear: true,
    options: [{ label: '是', value: true }, { label: '否', value: false }],
    placeholder: '是否评审'
  },
  {
    valueKey: 'status',
    type: 'select-enum',
    enumKey: 'FuncPlanStatus',
    placeholder: '选择状态',
    allowClear: true
  },
  {
    valueKey: 'ownerId',
    type: 'select-user',
    allowClear: true,
    placeholder: '选择负责人',
    maxlength: 100
  },
  {
    valueKey: 'startDate',
    type: 'date',
    valueType: 'start',
    op: 'GREATER_THAN_EQUAL',
    placeholder: '计划开始时间大于等于',
    showTime: { hideDisabledOptions: true, defaultValue: dayjs('00:00:00', 'HH:mm:ss') },
    allowClear: true
  },
  {
    valueKey: 'deadlineDate',
    type: 'date',
    valueType: 'start',
    op: 'LESS_THAN_EQUAL',
    placeholder: '计划截止时间小于等于',
    showTime: { hideDisabledOptions: true, defaultValue: dayjs('00:00:00', 'HH:mm:ss') },
    allowClear: true
  }
];

const dropdownMenuItems = [
  {
    key: 'block',
    icon: 'icon-zusai',
    name: '阻塞',
    permission: 'block'
  },
  {
    key: 'delete',
    icon: 'icon-qingchu',
    name: '删除',
    permission: 'delete'
  },
  {
    key: 'grant',
    icon: 'icon-quanxian1',
    name: '权限',
    permission: 'grant'
  },
  {
    key: 'clone',
    icon: 'icon-fuzhi',
    name: '克隆',
    noAuth: true,
    permission: 'clone'
  },
  {
    key: 'resetTestResult',
    icon: 'icon-zhongzhiceshijieguo',
    name: '重置测试',
    permission: 'resetTestResult'
  },
  {
    key: 'resetReviewResult',
    icon: 'icon-zhongzhipingshenjieguo',
    name: '重置评审',
    permission: 'resetReviewResult'
  },
  {
    key: 'viewBurnDown',
    icon: 'icon-jiankong',
    noAuth: true,
    name: '查看燃尽图'
  },
  {
    key: 'viewProgress',
    icon: 'icon-jiankong',
    noAuth: true,
    name: '查看成员进度'
  },
  {
    key: 'viewWrokCalendar',
    icon: 'icon-jiankong',
    noAuth: true,
    name: '查看工作日历'
  },
  {
    key: 'export',
    icon: 'icon-daochu',
    name: '导出用例',
    permission: 'export'
  }
];

const pageSizeOptions = ['5', '10', '15', '20', '30'];

const sortMenuItems: {
  name: string;
  key: OrderByKey;
  orderSort: OrderSortKey;
}[] = [
  {
    name: '按添加时间',
    key: 'createdDate',
    orderSort: 'DESC'
  },
  {
    name: '按添加人',
    key: 'createdByName',
    orderSort: 'ASC'
  }
];
</script>

<template>
  <div class="flex flex-col h-full overflow-auto px-5 py-5 leading-5 text-3">
    <div class="flex space-x-2">
      <Introduce class="mb-7 flex-1" />
      <div class="flex flex-col w-145">
        <div class="text-3.5 font-semibold mb-2.5">敏捷测试</div>
        <div>
          在敏捷软件开发环境中进行的软件测试，测试与开发的紧密协作，通过持续的反馈和协作，提升软件的质量和满足用户需求。
        </div>
        <div class="flex-1 flex flex-col justify-center">
          <img :src="ProcessPng" class="mt-2 items-center" />
        </div>
      </div>
    </div>

    <div class="text-3.5 font-semibold mb-1">已添加的计划</div>
    <Spin :spinning="loading" class="flex-1 flex flex-col">
      <template v-if="loaded">
        <div v-if="!searchedFlag && dataList.length === 0" class="flex-1 flex flex-col items-center justify-center">
          <img src="../../../../assets/images/nodata.png">
          <div class="flex items-center text-theme-sub-content text-3.5 leading-5 space-x-1">
            <span>您尚未添加任何计划，立即</span>
            <RouterLink class="router-link flex-1 truncate" :to="`/function#plans?type=ADD`">
              添加计划
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
                  <span class="text-theme-sub-content mx-2">至</span>
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
                      <span>负责人</span>
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
                      <span>成员</span>
                      <Colon />
                    </div>

                    <template v-if="item.members?.length">
                      <div
                        v-for="user in item.showMembers"
                        :key="user.id"
                        :title="user.fullname"
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
                          <span class="text-3">所有成员</span>
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
                              <span class="flex-1 truncate">{{ _user.fullname }}</span>
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

                <div class="ml-8 text-theme-content">共{{ item.caseNum }}条用例</div>
              </div>

              <div class="px-3.5 flex flex-start justify-between text-3 text-theme-sub-content">
                <div class="flex flex-wrap">
                  <div class="flex mt-3">
                    <div class="mr-2 whitespace-nowrap">
                      <span>ID</span>
                      <Colon />
                    </div>
                    <div class="text-theme-content">{{ item.id || "--" }}</div>
                  </div>

                  <div class="flex ml-8  mt-3">
                    <div class="mr-2 whitespace-nowrap">
                      <span>是否评审</span>
                      <Colon />
                    </div>
                    <div class="text-theme-content">{{ item.reviewFlag ? '是' : '否' }}</div>
                  </div>

                  <div class="flex ml-8  mt-3">
                    <div class="mr-2 whitespace-nowrap">
                      <span>工作量评估</span>
                      <Colon />
                    </div>
                    <div class="text-theme-content">{{ item.evalWorkloadMethod.message }}</div>
                  </div>

                  <div v-if="item.casePrefix" class="flex ml-8 mt-3 relative">
                    <div class="mr-2 whitespace-nowrap">
                      <span>用例前缀</span>
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
                    <span>附件数</span>
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
                  <div class="mx-2 whitespace-nowrap">修改于</div>
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
                  <RichText :value="item.otherInformation" emptyText="无说明~" />
                </div>
                <div class="flex space-x-3 items-center justify-between h-4 leading-5">
                  <RouterLink class="flex items-center space-x-1" :to="`/function#plans?id=${item.id}&type=edit`">
                    <Icon icon="icon-shuxie" class="text-3.5" />
                    <span>编辑</span>
                  </RouterLink>

                  <Button
                    :disabled="!isAdmin && !permissionsMap.get(item.id)?.includes('VIEW')"
                    size="small"
                    type="text"
                    class="px-0 flex items-center space-x-1"
                    @click="goCase(item)">
                    <Icon icon="icon-ceshiyongli1" class="text-3.5" />
                    <span>查看用例</span>
                  </Button>

                  <Button
                    :disabled="(!isAdmin && !permissionsMap.get(item.id)?.includes('MODIFY_PLAN')) || !['PENDING', 'BLOCKED', 'COMPLETED'].includes(item.status?.value)"
                    size="small"
                    type="text"
                    class="px-0 flex items-center space-x-1"
                    @click="toStart(item, index)">
                    <Icon icon="icon-kaishi" class="text-3.5" />
                    <span>{{ item.status.value === 'COMPLETED' ? '重新开始' : '开始' }}</span>
                  </Button>

                  <Button
                    :disabled="(!isAdmin && !permissionsMap.get(item.id)?.includes('MODIFY_PLAN')) || !['IN_PROGRESS'].includes(item.status?.value)"
                    size="small"
                    type="text"
                    class="px-0 flex items-center space-x-1"
                    @click="toCompleted(item, index)">
                    <Icon icon="icon-yiwancheng" class="text-3.5" />
                    <span>完成</span>
                  </Button>

                  <Dropdown
                    :adminFlag="false"
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
        onTips="开启&quot;有权限控制&quot;后，需要手动授权服务权限后才会有计划相应操作权限，默认开启&quot;有权限控制&quot;。注意：如果授权对象没有父级项目权限将自动授权查看权限。"
        offTips="开启&quot;无权限控制&quot;后，将允许所有用户公开查看和操作当前计划，查看用户同时需要有当前计划父级项目权限。"
        title="计划权限"
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
