<script setup lang="ts">
import { computed, defineAsyncComponent, inject, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
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
import { utils, TESTER, download } from '@xcan-angus/infra';
import ProcessPng from './images/process.png';
import { task } from '@/api/tester';

import { SprintInfo } from '../types';
import SearchPanel from '@/views/task/sprint/list/SearchPanel.vue';

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

const { t } = useI18n();
const AuthorizeModal = defineAsyncComponent(() => import('@/components/AuthorizeModal/index.vue'));
const Introduce = defineAsyncComponent(() => import('@/views/task/sprint/list/Introduce.vue'));
const Burndown = defineAsyncComponent(() => import('@/views/task/sprint/list/BurndownChart.vue'));
const ProgressModal = defineAsyncComponent(() => import('@/views/task/sprint/list/MemberProgress.vue'));
const WorkCalendar = defineAsyncComponent(() => import('@/views/task/sprint/list/WorkCalendar.vue'));
const RichText = defineAsyncComponent(() => import('@/components/richEditor/textContent/index.vue'));

const deleteTabPane = inject<(keys: string[]) => void>('deleteTabPane', () => ({}));
const isAdmin = inject('isAdmin', ref(false));

const loaded = ref(false);
const loading = ref(false);
const exportLoadingSet = ref<Set<string>>(new Set());
const searchedFlag = ref(false);

const pageNo = ref(1);
const pageSize = ref(5);
let searchPanelParams = {
  orderBy: undefined,
  orderSort: undefined,
  filters: []
};
const total = ref(0);
const dataList = ref<SprintInfo[]>([]);
const permissionsMap = ref<Map<string, string[]>>(new Map());

const selectedData = ref<SprintInfo>();
const authorizeModalVisible = ref(false);
const burndownVisible = ref(false); // 燃尽图Visible
const progressVisible = ref(false); // 进度图Visible
const workCalendarVisible = ref(false); // 工作日历visible

const searchChange = (data) => {
  searchPanelParams = data;
  pageNo.value = 1;
  loadData();
};

const refresh = () => {
  pageNo.value = 1;
  permissionsMap.value.clear();
  loadData();
};

const setTableData = async (id: string, index: number) => {
  const [error, res] = await task.getSprintDetail(id);
  loading.value = false;
  if (error) {
    return;
  }

  if (res?.data) {
    dataList.value[index] = res?.data;
  }
};

// 查看燃尽图
const viewBurnDown = (data: SprintInfo) => {
  selectedData.value = data;
  burndownVisible.value = true;
};
// 查看成员进度
const viewProgress = (data: SprintInfo) => {
  selectedData.value = data;
  progressVisible.value = true;
};

const viewWorkCalendar = (data: SprintInfo) => {
  selectedData.value = data;
  workCalendarVisible.value = true;
};

const reopen = async (data: SprintInfo, idx: number) => {
  loading.value = true;
  const [error] = await task.reopenSprint({
    ids: [data.id]
  }, {
    paramsType: true
  });
  loading.value = false;
  if (error) {
    return;
  }
  notification.success(t('taskSprint.messages.reopenSuccess'));
  setTableData(data.id, idx);
};

const restart = async (data: SprintInfo, idx: number) => {
  loading.value = true;
  const [error] = await task.restartSprint({ ids: [data.id] }, { paramsType: true });
  loading.value = false;
  if (error) {
    return;
  }
  notification.success(t('taskSprint.messages.restartSuccess'));
  setTableData(data.id, idx);
};

const toStart = async (data: SprintInfo, index: number) => {
  loading.value = true;
  const id = data.id;
  const [error] = await task.startSprint(id);
  loading.value = false;
  if (error) {
    loading.value = false;
    return;
  }

  notification.success(t('taskSprint.messages.startSuccess'));
  setTableData(id, index);
};

const toCompleted = async (data: SprintInfo, index: number) => {
  loading.value = true;
  const id = data.id;
  const [error] = await task.endSprint(id);
  if (error) {
    loading.value = false;
    return;
  }

  notification.success(t('taskSprint.messages.completeSuccess'));
  setTableData(id, index);
};

const toBlock = async (data: SprintInfo, index: number) => {
  loading.value = true;
  const id = data.id;
  const [error] = await task.blockSprint(id);
  if (error) {
    return;
  }

  notification.success(t('taskSprint.messages.blockSuccess'));
  setTableData(id, index);
};

const toDelete = async (data: SprintInfo) => {
  modal.confirm({
    content: t('taskSprint.messages.confirmDelete', { name: data.name }),
    async onOk () {
      const id = data.id;
      const [error] = await task.deleteSprint(id);
      if (error) {
        return;
      }

      notification.success(t('taskSprint.messages.deleteSuccess'));
      loadData();
      deleteTabPane([id]);
    }
  });
};

const toGrant = (data: SprintInfo) => {
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

const toClone = async (data: SprintInfo) => {
  const [error] = await task.cloneSprint(data.id);
  if (error) {
    return;
  }

  notification.success(t('taskSprint.messages.cloneSuccess'));
  loadData();
};

const toExport = async (data: SprintInfo) => {
  const { id, projectId } = data;
  if (exportLoadingSet.value.has(id)) {
    return;
  }

  exportLoadingSet.value.add(id);
  await download(`${TESTER}/task/export?projectId=${projectId}&sprintId=${id}`);
  exportLoadingSet.value.delete(id);
};

const dropdownClick = (data: SprintInfo, index: number, key: 'clone' | 'block' | 'delete' | 'export' | 'grant'|'viewBurnDown'|'viewProgress'|'reopen'|'restart'|'viewWorkCalendar') => {
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
    case 'export':
      toExport(data);
      break;
    case 'viewBurnDown':
      viewBurnDown(data);
      break;
    case 'viewProgress':
      viewProgress(data);
      break;
    case 'reopen':
      reopen(data, index);
      break;
    case 'restart':
      restart(data, index);
      break;
    case 'viewWorkCalendar':
      viewWorkCalendar(data);
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
    ...searchPanelParams
  };

  const [error, res] = await task.getSprintList(params);
  loaded.value = true;
  loading.value = false;

  if (params.filters?.length || params.orderBy) {
    searchedFlag.value = true;
  } else {
    searchedFlag.value = false;
  }

  if (error) {
    total.value = 0;
    dataList.value = [];
    return;
  }

  const data = res?.data || { total: 0, list: [] };
  if (data) {
    total.value = +data.total;

    const _list = (data.list || [] as SprintInfo[]);
    dataList.value = _list.map(item => {
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

  return await task.getCurrentUserSprintAuth(id, params);
};

onMounted(() => {
  watch(() => props.projectId, () => {
    pageNo.value = 1;
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
      if ((_isAdmin || _permissions.includes('MODIFY_SPRINT')) && ['PENDING', 'IN_PROGRESS'].includes(_status)) {
        tempPermissions.push('block');
      }

      if (_isAdmin || _permissions.includes('DELETE_SPRINT')) {
        tempPermissions.push('delete');
      }

      if (_isAdmin || _permissions.includes('GRANT')) {
        tempPermissions.push('grant');
      }

      if (_isAdmin || _permissions.includes('EXPORT_TASK')) {
        tempPermissions.push('export');
      }
      if (_isAdmin || _permissions.includes('MODIFY_SPRINT')) {
        tempPermissions.push('modify');
      }

      map.set(id, tempPermissions);
    }
  }

  return map;
});

const getDropdownMenuItems = (sprint) => {
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

const pageSizeOptions = ['5', '10', '15', '20', '30'];

</script>

<template>
  <div class="flex flex-col h-full overflow-auto px-5 py-5 leading-5 text-3">
    <div class="flex space-x-2">
      <Introduce class="mb-7 flex-1" />
      <div class="flex flex-col w-145">
        <div class="text-3.5 font-semibold mb-2.5">{{ t('taskSprint.scrumAgileProcess') }}</div>
        <div>
          {{ t('taskSprint.scrumAgileProcessDesc') }}
        </div>
        <div class="flex-1 flex flex-col justify-center">
          <img :src="ProcessPng" class="mt-2 items-center w-4/5" />
        </div>
      </div>
    </div>

    <div class="text-3.5 font-semibold mb-1">{{ t('taskSprint.addedSprints') }}</div>
    <Spin :spinning="loading" class="flex-1 flex flex-col">
      <template v-if="loaded">
        <div v-if="!searchedFlag && dataList.length === 0" class="flex-1 flex flex-col items-center justify-center">
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
            :loading="loading"
            @change="searchChange"
            @refresh="refresh" />
          <!-- <div class="flex items-start justify-between mt-2.5 mb-3.5">
            <searchPanel
              :options="searchPanelOptions"
              class="flex-1 mr-3.5"
              @change="searchChange" />

            <div class="flex items-center space-x-3">
              <Button
                type="primary"
                size="small"
                class="p-0">
                <RouterLink class="flex items-center space-x-1 leading-6.5 px-1.75" :to="`/task#sprint?type=ADD`">
                  <Icon icon="icon-jia" class="text-3.5" />
                  <span>添加迭代</span>
                </RouterLink>
              </Button>

              <DropdownSort
                v-model:orderBy="orderBy"
                v-model:orderSort="orderSort"
                :menuItems="sortMenuItems"
                @click="toSort">
                <div class="flex items-center cursor-pointer text-theme-content space-x-1 text-theme-text-hover">
                  <Icon icon="icon-shunxu" class="text-3.5" />
                  <span>排序</span>
                </div>
              </DropdownSort>

              <IconRefresh
                :loading="loading"
                :disabled="loading"
                @click="refresh">
                <template #default>
                  <div class="flex items-center cursor-pointer text-theme-content space-x-1 text-theme-text-hover">
                    <Icon icon="icon-shuaxin" class="text-3.5" />
                    <span class="ml-1">刷新</span>
                  </div>
                </template>
              </IconRefresh>
            </div>
          </div> -->

          <NoData v-if="dataList.length === 0" class="flex-1" />

          <template v-else>
            <div
              v-for="(item, index) in dataList"
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
                  <span class="mx-2">至</span>
                  <span>{{ item.deadlineDate }}</span>
                </div>

                <div class="flex items-center">
                  <div
                    class="text-3 leading-4 flex items-center flex-none whitespace-nowrap mr-3.5">
                    <div class="h-1.5 w-1.5 rounded-full mr-1" :class="item.status?.value"></div>
                    <div>{{ item.status?.message }}</div>
                  </div>
                  <Progress :percent="item.progress?.completedRate" style="width:150px;" />
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

                <div class="ml-8 text-theme-content">{{ t('taskSprint.taskCount', { count: item.taskNum }) }}</div>
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
                  <div class="mx-2 whitespace-nowrap">{{ t('taskSprint.columns.lastModifiedBy') }}</div>
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
                  <RouterLink class="flex items-center space-x-1" :to="`/task#sprint?id=${item.id}&type=edit`">
                    <Icon icon="icon-shuxie" class="text-3.5" />
                    <span>{{ t('taskSprint.actions.edit') }}</span>
                  </RouterLink>

                  <RouterLink class="flex items-center space-x-1" :to="`/task#task?sprintId=${item.id}&sprintName=${item.name}`">
                    <Icon icon="icon-renwu2" class="text-3.5" />
                    <span>{{ t('taskSprint.actions.viewTasks') }}</span>
                  </RouterLink>

                  <Button
                    :disabled="(!isAdmin && !permissionsMap.get(item.id)?.includes('MODIFY_SPRINT')) || !['PENDING'].includes(item.status?.value)"
                    size="small"
                    type="text"
                    class="px-0 flex items-center"
                    @click="toStart(item, index)">
                    <Icon icon="icon-kaishi" class="mr-0.5" />
                    <span>{{ t('taskSprint.actions.start') }}</span>
                  </Button>

                  <Button
                    :disabled="(!isAdmin && !permissionsMap.get(item.id)?.includes('MODIFY_SPRINT')) || !['IN_PROGRESS'].includes(item.status?.value)"
                    size="small"
                    type="text"
                    class="px-0 flex items-center"
                    @click="toCompleted(item, index)">
                    <Icon icon="icon-yiwancheng" class="mr-0.5" />
                    <span>{{ t('taskSprint.actions.complete') }}</span>
                  </Button>

                  <Dropdown
                    :admin="false"
                    :menuItems="getDropdownMenuItems(item)"
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
      <Burndown
        v-model:visible="burndownVisible"
        :sprintId="selectedData?.id" />
    </AsyncComponent>
    <AsyncComponent :visible="progressVisible">
      <ProgressModal
        v-model:visible="progressVisible"
        :sprintId="selectedData?.id"
        :projectId="props.projectId" />
    </AsyncComponent>

    <AsyncComponent :visible="authorizeModalVisible">
      <AuthorizeModal
        v-model:visible="authorizeModalVisible"
        enumKey="TaskSprintPermission"
        :appId="props.appInfo?.id"
        :listUrl="`${TESTER}/task/sprint/auth?sprintId=${selectedData?.id}`"
        :delUrl="`${TESTER}/task/sprint/auth`"
        :addUrl="`${TESTER}/task/sprint/${selectedData?.id}/auth`"
        :updateUrl="`${TESTER}/task/sprint/auth`"
        :enabledUrl="`${TESTER}/task/sprint/${selectedData?.id}/auth/enabled`"
        :initStatusUrl="`${TESTER}/task/sprint/${selectedData?.id}/auth/status`"
        onTips="开启&quot;有权限控制&quot;后，需要手动授权服务权限后才会有迭代相应操作权限，默认开启&quot;有权限控制&quot;。注意：如果授权对象没有父级项目权限将自动授权查看权限。"
        offTips="开启&quot;无权限控制&quot;后，将允许所有用户公开查看和操作当前迭代，查看用户同时需要有当前迭代父级项目权限。"
        title="迭代权限"
        @change="authFlagChange" />
    </AsyncComponent>

    <AsyncComponent :visible="workCalendarVisible">
      <WorkCalendar
        v-model:visible="workCalendarVisible"
        :sprintId="selectedData?.id"
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
