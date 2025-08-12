<script setup lang="ts">
import { defineAsyncComponent, inject, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Avatar, Button, Pagination } from 'ant-design-vue';
import { UserOutlined } from '@ant-design/icons-vue';
import { Colon, Icon, Image, modal, NoData, notification, Popover, Spin } from '@xcan-angus/vue-ui';
import { task } from '@/api/tester';

import { MeetingInfo } from '../PropsType';
import SearchPanel from '@/views/task/meeting/list/searchPanel/index.vue';

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
const Introduce = defineAsyncComponent(() => import('@/views/task/meeting/list/introduce/index.vue'));

const deleteTabPane = inject<(keys: string[]) => void>('deleteTabPane', () => ({}));

const loaded = ref(false);
const loading = ref(false);
const searchedFlag = ref(false);

// const orderBy = ref<OrderByKey>();
// const orderSort = ref<OrderSortKey>();

const searchPanelParams = ref({
  orderBy: undefined,
  orderSort: undefined,
  filters: []
});
const pageNo = ref(1);
const pageSize = ref(5);
// const filters = ref<{ key: string; op: string; value: string; }[]>([]);
const total = ref(0);
const dataList = ref<MeetingInfo[]>([]);
const permissionsMap = ref<Map<string, string[]>>(new Map());
// const toSort = (value: {
//   orderBy: OrderByKey;
//   orderSort: OrderSortKey;
// }): void => {
//   orderBy.value = value.orderBy;
//   orderSort.value = value.orderSort;
//   pageNo.value = 1;
//   loadData();
// };

const refresh = () => {
  pageNo.value = 1;
  permissionsMap.value.clear();
  loadData();
};

const searchChange = (data) => {
  pageNo.value = 1;
  searchPanelParams.value = data;
  loadData();
};

const toDelete = async (data: MeetingInfo) => {
  modal.confirm({
    content: t('taskMeeting.confirmDelete', { name: data.subject }),
    async onOk () {
      const id = data.id;
      const [error] = await task.deleteMeeting(id);
      if (error) {
        return;
      }

      notification.success(t('taskMeeting.deleteSuccess'));
      loadData();
      deleteTabPane([id]);
    }
  });
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

  // if (orderSort.value) {
  //   params.orderBy = orderBy.value;
  //   params.orderSort = orderSort.value;
  // }

  // if (filters.value.length) {
  //   params.filters = filters.value;
  // }

  const [error, res] = await task.getMeetingList(params);
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

    const _list = (data.list || [] as MeetingInfo[]);
    dataList.value = _list;
  }
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

// const searchPanelOptions = [
//   {
//     valueKey: 'subject',
//     type: 'input',
//     placeholder: '查询会议主题',
//     allowClear: true,
//     maxlength: 100
//   },
// ];

const pageSizeOptions = ['5', '10', '15', '20', '30'];

// const sortMenuItems: {
//   name: string;
//   key: OrderByKey;
//   orderSort: OrderSortKey;
// }[] = [
//   {
//     name: '按添加时间',
//     key: 'createdDate',
//     orderSort: 'DESC'
//   },
//   {
//     name: '按添加人',
//     key: 'createdByName',
//     orderSort: 'ASC'
//   }
// ];
</script>

<template>
  <div class="flex flex-col h-full overflow-auto px-5 py-5 leading-5 text-3">
    <div class="flex space-x-2">
      <Introduce class="mb-7 flex-1" />
    </div>

    <div class="text-3.5 font-semibold mb-1">{{ t('taskMeeting.addedMeetings') }}</div>
    <Spin :spinning="loading" class="flex-1 flex flex-col">
      <template v-if="loaded">
        <div v-if="!searchedFlag && dataList.length === 0" class="flex-1 flex flex-col items-center justify-center">
          <img src="../../../../assets/images/nodata.png">
          <div class="flex items-center text-theme-sub-content text-3.5 leading-5 space-x-1">
            <span>{{ t('taskMeeting.notAddedYet') }}</span>
            <RouterLink class="router-link flex-1 truncate" :to="`/task#meeting?type=ADD`">
              {{ t('taskMeeting.addMeeting') }}
            </RouterLink>
          </div>
        </div>

        <template v-else>
          <SearchPanel
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
                <RouterLink class="flex items-center space-x-1 leading-6.5 px-1.75" :to="`/task#meetings?type=ADD`">
                  <Icon icon="icon-jia" class="text-3.5" />
                  <span>添加会议</span>
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
                    :title="item.subject"
                    :to="`/task#meeting?id=${item.id}`">
                    {{ item.subject }}
                  </RouterLink>
                </div>
              </div>

              <div class="px-3.5 flex mt-3 justify-between text-3 text-theme-sub-content">
                <div class="flex leading-5">
                  <div class="flex mr-10 items-center">
                    <div class="mr-2">
                      <span>{{ t('taskMeeting.columns.moderatorLabel') }}</span>
                      <Colon />
                    </div>
                    <div class="w-5 h-5 rounded-full mr-1 overflow-hidden">
                      <Image
                        class="w-full"
                        :src="item.moderator.avatar"
                        type="avatar" />
                    </div>
                    <div
                      class="text-theme-content truncate"
                      :title="item.moderator.fullName"
                      style="max-width: 200px;">
                      {{ item.moderator.fullName }}
                    </div>
                  </div>

                  <div class="flex items-center">
                    <div class="mr-2">
                      <span>{{ t('taskMeeting.columns.participantsLabel') }}</span>
                      <Colon />
                    </div>

                    <template v-if="item.participants?.length">
                      <div
                        v-for="user in item.participants"
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
                          <span class="text-3">所有成员</span>
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

                <div class="ml-8 text-theme-content">{{ t('taskMeeting.columns.participantsCount', { count: item.participants?.length || 0 }) }}</div>
              </div>

              <div class="px-3.5 flex flex-start justify-between text-3 text-theme-sub-content">
                <div class="flex flex-wrap">
                  <div class="flex mt-3">
                    <div class="mr-2 whitespace-nowrap">
                      <span>{{ t('taskMeeting.columns.id') }}</span>
                      <Colon />
                    </div>
                    <div class="text-theme-content">{{ item.id || "--" }}</div>
                  </div>

                  <div class="flex mt-3 ml-8">
                    <div class="mr-2 whitespace-nowrap">
                      <span>{{ t('taskMeeting.columns.type') }}</span>
                      <Colon />
                    </div>
                    <div class="text-theme-content">{{ item.type?.message }}</div>
                  </div>
                </div>

                <div class="flex ml-8 mt-3">
                  <div
                    class="truncate text-theme-content"
                    style="max-width: 100px;"
                    :title="item.lastModifiedByName">
                    {{ item.lastModifiedByName }}
                  </div>
                  <div class="mx-2 whitespace-nowrap">{{ t('taskMeeting.columns.lastModifiedBy') }}</div>
                  <div class="whitespace-nowrap text-theme-content">
                    {{ item.lastModifiedDate }}
                  </div>
                </div>
              </div>

              <div class="px-3.5 flex justify-between items-start text-3 my-2.5 relative">
                <div
                  :title="item.description"
                  class="truncate mr-8"
                  style="max-width: 70%;">
                  {{ item.otherInformation }}
                </div>
                <div class="flex space-x-3 items-center justify-between h-4 leading-5">
                  <RouterLink class="flex items-center space-x-1" :to="`/task#meeting?id=${item.id}&type=edit`">
                    <Icon icon="icon-shuxie" class="text-3.5" />
                    <span>{{ t('actions.edit') }}</span>
                  </RouterLink>
                  <Button
                    type="text"
                    size="small"
                    @click="toDelete(item)">
                    <Icon icon="icon-qingchu" />
                    {{ t('actions.delete') }}
                  </Button>
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
