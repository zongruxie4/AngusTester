<script setup lang="ts">
import { computed, defineAsyncComponent, inject, onMounted, ref, watch } from 'vue';
import { Avatar, Button, Pagination, Popover, Progress } from 'ant-design-vue';
import { UserOutlined } from '@ant-design/icons-vue';
import { Colon, Dropdown, Icon, Image, modal, NoData, notification, Spin } from '@xcan-angus/vue-ui';
import { utils, download, appContext } from '@xcan-angus/infra';
import dayjs from 'dayjs';
import { func } from '@/api/tester';
import { useI18n } from 'vue-i18n';

import { ReviewInfo } from '../types';

import SearchPanel from '@/views/function/review/list/SearchPanel.vue';

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

const { t } = useI18n();

type OrderByKey = 'createdDate' | 'createdByName';
type OrderSortKey = 'ASC' | 'DESC';

const Introduce = defineAsyncComponent(() => import('@/views/function/review/list/Introduce.vue'));
const RichContent = defineAsyncComponent(() => import('@/components/richEditor/textContent/index.vue'));

const deleteTabPane = inject<(keys: string[]) => void>('deleteTabPane', () => ({}));
const isAdmin = computed(() => appContext.isAdmin());

const loaded = ref(false);
const loading = ref(false);
const searchedFlag = ref(false);

const pageNo = ref(1);
const pageSize = ref(5);
const searchPanelParams = ref({
  orderBy: undefined,
  orderSort: undefined,
  filters: []
});
// const filters = ref<{ key: string; op: string; value: string; }[]>([]);
const total = ref(0);
const dataList = ref<ReviewInfo[]>([]);
const permissionsMap = ref<Map<string, string[]>>(new Map());

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

const setTableData = async (id: string, index: number) => {
  loading.value = true;
  const [error, res] = await func.getReviewDetail(id);
  loading.value = false;
  if (error) {
    return;
  }

  if (res?.data) {
    dataList.value[index] = res?.data;
  }
};

const toStart = async (data: ReviewInfo, index: number) => {
  loading.value = true;
  const id = data.id;
  const [error] = await func.startReview(id);
  loading.value = false;
  if (error) {
    return;
  }

  notification.success(t('caseReview.list.reviewStartedSuccess'));
  setTableData(id, index);
};

const toCompleted = async (data: ReviewInfo, index: number) => {
  loading.value = true;
  const id = data.id;
  const [error] = await func.endReview(id);
  loading.value = false;
  if (error) {
    return;
  }

  notification.success(t('caseReview.list.reviewCompletedSuccess'));
  setTableData(id, index);
};

const toDelete = async (data: ReviewInfo) => {
  modal.confirm({
    content: t('caseReview.list.confirmDeleteReview', { name: data.name }),
    async onOk () {
      const id = data.id;
      const [error] = await func.deleteReview(id);
      if (error) {
        return;
      }

      notification.success(t('caseReview.list.reviewDeletedSuccess'));
      loadData();

      deleteTabPane([id]);
    }
  });
};

const toClone = async (data: ReviewInfo) => {
  const [error] = await func.cloneReview(data.id);
  if (error) {
    return;
  }

  notification.success(t('caseReview.list.reviewClonedSuccess'));
  loadData();
};

const dropdownClick = (data: ReviewInfo, index: number, key: 'clone' | 'block' | 'delete' | 'export' | 'grant' | 'resetTestResult' | 'resetReviewResult'|'viewBurnDown'|'viewProgress') => {
  switch (key) {
    case 'delete':
      toDelete(data);
      break;
    case 'clone':
      toClone(data);
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

  const [error, res] = await func.getReviewList(params);
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

    const _list = (data.list || [] as ReviewInfo[]);
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
        item.showMembers = item.members.slice(0, 5);
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

  return await func.getReviewAuthByPlanId(id, params);
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
    placeholder: t('caseReview.list.searchReviewNameDescription'),
    allowClear: true,
    maxlength: 100
  },
  // {
  //   valueKey: 'status',
  //   type: 'select-enum',
  //   enumKey: 'FuncReviewStatus',
  //   placeholder: '选择状态',
  //   allowClear: true
  // },
  {
    valueKey: 'ownerId',
    type: 'select-user',
    allowClear: true,
    placeholder: t('caseReview.list.selectOwner'),
    maxlength: 100
  },
  {
    valueKey: 'startDate',
    type: 'date',
    valueType: 'start',
    op: 'GREATER_THAN_EQUAL',
    placeholder: t('caseReview.list.reviewStartTimeGreaterEqual'),
    showTime: { hideDisabledOptions: true, defaultValue: dayjs('00:00:00', 'HH:mm:ss') },
    allowClear: true
  },
  {
    valueKey: 'deadlineDate',
    type: 'date',
    valueType: 'start',
    op: 'LESS_THAN_EQUAL',
    placeholder: t('caseReview.list.reviewDeadlineTimeLessEqual'),
    showTime: { hideDisabledOptions: true, defaultValue: dayjs('00:00:00', 'HH:mm:ss') },
    allowClear: true
  }
];

const dropdownMenuItems = [
  {
    key: 'delete',
    icon: 'icon-qingchu',
    name: t('caseReview.list.delete'),
    permission: 'delete'
  },
  {
    key: 'clone',
    icon: 'icon-fuzhi',
    name: t('caseReview.list.clone'),
    noAuth: true,
    permission: 'clone'
  }
];

const pageSizeOptions = ['5', '10', '15', '20', '30'];

const sortMenuItems: {
  name: string;
  key: OrderByKey;
  orderSort: OrderSortKey;
}[] = [
  {
    name: t('caseReview.list.sortByAddTime'),
    key: 'createdDate',
    orderSort: 'DESC'
  },
  {
    name: t('caseReview.list.sortByAddPerson'),
    key: 'createdByName',
    orderSort: 'ASC'
  }
];
</script>

<template>
  <div class="flex flex-col h-full overflow-auto px-5 py-5 leading-5 text-3">
    <Introduce class="mb-7" />
    <div class="text-3.5 font-semibold mb-1">{{ t('caseReview.list.addedReviews') }}</div>
    <Spin :spinning="loading" class="flex-1 flex flex-col">
      <template v-if="loaded">
        <div v-if="!searchedFlag && dataList.length === 0" class="flex-1 flex flex-col items-center justify-center">
          <img src="../../../../assets/images/nodata.png">
          <div class="flex items-center text-theme-sub-content text-3.5 leading-5 space-x-1">
            <span>{{ t('caseReview.list.noReviewsAdded') }}</span>
            <RouterLink class="router-link flex-1 truncate" :to="`/function#reviews?type=ADD`">
              {{ t('caseReview.list.addReview') }}
            </RouterLink>
          </div>
        </div>

        <template v-else>
          <SearchPanel
            :loading="loading"
            @refresh="refresh"
            @change="searchChange" />

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
                    :to="`/function#reviews?id=${item.id}`">
                    {{ item.name }}
                  </RouterLink>
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
                      <span>{{ t('caseReview.list.owner') }}</span>
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
                      <span>{{ t('caseReview.list.participants') }}</span>
                      <Colon />
                    </div>

                    <template v-if="item.participants?.length">
                      <div
                        v-for="user in item.participants.slice(0, 10)"
                        :key="user.id"
                        :title="user.fullName"
                        class="w-5 h-5 mr-2 overflow-hidden rounded-full">
                        <Image
                          :src="user.avatar"
                          type="avatar"
                          class="w-full" />
                      </div>

                      <Popover
                        v-if="item.participants.length > 5"
                        placement="bottomLeft"
                        internal>
                        <template #title>
                          <span class="text-3">{{ t('caseReview.list.allParticipants') }}</span>
                        </template>
                        <template #content>
                          <div class="flex flex-wrap" style="max-width: 700px;">
                            <div
                              v-for="_user in item.participants"
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

                <div class="ml-8 text-theme-content">{{ t('caseReview.list.totalCases', { count: item.caseNum }) }}</div>
              </div>

              <div class="px-3.5 flex flex-start justify-between text-3 text-theme-sub-content">
                <div class="flex flex-wrap">
                  <div class="flex mt-3">
                    <div class="mr-2 whitespace-nowrap">
                      <span>{{ t('caseReview.list.id') }}</span>
                      <Colon />
                    </div>
                    <div class="text-theme-content">{{ item.id || "--" }}</div>
                  </div>

                  <div class="flex mt-3 ml-8">
                    <div class="mr-2 whitespace-nowrap">
                      <span>{{ t('caseReview.list.testPlan') }}</span>
                      <Colon />
                    </div>
                    <div class="text-theme-content">{{ item.planName || "--" }}</div>
                  </div>

                  <div v-if="item.attachments?.length" class="whitespace-nowrap ml-8 mt-3">
                    <span>{{ t('caseReview.list.attachmentCount') }}</span>
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
                  <div class="mx-2 whitespace-nowrap">{{ t('caseReview.list.modifiedBy') }}</div>
                  <div class="whitespace-nowrap text-theme-content">
                    {{ item.lastModifiedDate }}
                  </div>
                </div>
              </div>

              <div class="px-3.5 flex justify-between items-start text-3 my-2.5 relative">
                <div
                  class="truncate mr-8"
                  style="max-width: 70%;">
                  <otherInformation :value="item.otherInformation" />
                </div>
                <div class="flex space-x-3 items-center justify-between h-4 leading-5">
                  <RouterLink class="flex items-center space-x-1" :to="`/function#reviews?id=${item.id}&type=edit`">
                    <Icon icon="icon-shuxie" class="text-3.5" />
                    <span>{{ t('caseReview.list.edit') }}</span>
                  </RouterLink>

                  <RouterLink class="flex items-center space-x-1" :to="`/function#reviews?id=${item.id}`">
                    <Icon icon="icon-shuxie" class="text-3.5" />
                    <span>{{ t('caseReview.list.goToReview') }}</span>
                  </RouterLink>

                  <Button
                    :disabled="(!isAdmin && !permissionsMap.get(item.id)?.includes('MODIFY_PLAN')) || !['PENDING', 'BLOCKED', 'COMPLETED'].includes(item.status?.value)"
                    size="small"
                    type="text"
                    class="px-0 flex items-center space-x-1"
                    @click="toStart(item, index)">
                    <Icon icon="icon-kaishi" class="text-3.5" />
                    <span>{{ t('caseReview.list.start') }}</span>
                  </Button>

                  <Button
                    :disabled="(!isAdmin && !permissionsMap.get(item.id)?.includes('MODIFY_PLAN')) || !['IN_PROGRESS'].includes(item.status?.value)"
                    size="small"
                    type="text"
                    class="px-0 flex items-center space-x-1"
                    @click="toCompleted(item, index)">
                    <Icon icon="icon-yiwancheng" class="text-3.5" />
                    <span>{{ t('caseReview.list.complete') }}</span>
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
