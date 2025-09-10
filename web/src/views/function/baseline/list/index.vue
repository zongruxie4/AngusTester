<script setup lang="ts">
import { defineAsyncComponent, inject, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Button, Pagination } from 'ant-design-vue';
import { Colon, Icon, modal, NoData, notification, Spin } from '@xcan-angus/vue-ui';
import dayjs from 'dayjs';
import { func } from '@/api/tester';

import SearchPanel from '@/views/function/baseline/list/SearchPanel.vue';
import RichText from '@/components/richEditor/textContent/index.vue';
import {BaselineInfo} from "@/views/function/baseline/types";

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

const Introduce = defineAsyncComponent(() => import('@/views/function/baseline/list/Introduce.vue'));

const deleteTabPane = inject<(keys: string[]) => void>('deleteTabPane', () => ({}));
const isAdmin = inject('isAdmin', ref(false));

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
const dataList = ref<BaselineInfo[]>([]);
const permissionsMap = ref<Map<string, string[]>>(new Map());

const refresh = () => {
  pageNo.value = 1;
  permissionsMap.value.clear();
  loadData();
};

const searchChange = (data) => {
  pageNo.value = 1;
  // filters.value = data;
  searchPanelParams.value = data;
  loadData();
};

const toCompleted = async (data: BaselineInfo) => {
  loading.value = true;
  const id = data.id;
  const [error] = await func.establishBaseline(id);
  loading.value = false;
  if (error) {
    return;
  }
  notification.success(t('functionBaseline.list.baselineEstablished'));
  loadData();
};

const toDelete = async (data: BaselineInfo) => {
  modal.confirm({
    content: t('functionBaseline.list.confirmDeleteBaseline', { name: data.name }),
    async onOk () {
      const id = data.id;
      const [error] = await func.deleteBaseline([id]);
      if (error) {
        return;
      }

      notification.success(t('functionBaseline.list.baselineDeletedSuccess'));
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

  const [error, res] = await func.getBaselineList(params);
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

    const _list = (data.list || [] as BaselineInfo[]);
    dataList.value = _list;
  }
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

const searchPanelOptions = [
  {
    valueKey: 'name',
    type: 'input',
    placeholder: t('functionBaseline.list.searchBaselineNameDescription'),
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
    placeholder: t('functionBaseline.list.selectOwner'),
    maxlength: 100
  },
  {
    valueKey: 'startDate',
    type: 'date',
    valueType: 'start',
    op: 'GREATER_THAN_EQUAL',
    placeholder: t('functionBaseline.list.baselineStartTimeGreaterEqual'),
    showTime: { hideDisabledOptions: true, defaultValue: dayjs('00:00:00', 'HH:mm:ss') },
    allowClear: true
  },
  {
    valueKey: 'deadlineDate',
    type: 'date',
    valueType: 'start',
    op: 'LESS_THAN_EQUAL',
    placeholder: t('functionBaseline.list.baselineDeadlineTimeLessEqual'),
    showTime: { hideDisabledOptions: true, defaultValue: dayjs('00:00:00', 'HH:mm:ss') },
    allowClear: true
  }
];

const dropdownMenuItems = [
  {
    key: 'delete',
    icon: 'icon-qingchu',
    name: t('functionBaseline.list.delete'),
    permission: 'delete'
  },
  {
    key: 'clone',
    icon: 'icon-fuzhi',
    name: t('functionBaseline.list.clone'),
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
    name: t('functionBaseline.list.sortByAddTime'),
    key: 'createdDate',
    orderSort: 'DESC'
  },
  {
    name: t('functionBaseline.list.sortByAddPerson'),
    key: 'createdByName',
    orderSort: 'ASC'
  }
];
</script>

<template>
  <div class="flex flex-col h-full overflow-auto px-5 py-5 leading-5 text-3">
    <Introduce class="mb-7" />
    <div class="text-3.5 font-semibold mb-1">{{ t('functionBaseline.list.createdBaselines') }}</div>
    <Spin :spinning="loading" class="flex-1 flex flex-col">
      <template v-if="loaded">
        <div v-if="!searchedFlag && dataList.length === 0" class="flex-1 flex flex-col items-center justify-center">
          <img src="../../../../assets/images/nodata.png">
          <div class="flex items-center text-theme-sub-content text-3.5 leading-5 space-x-1">
            <span>{{ t('functionBaseline.list.noBaselinesAdded') }}</span>
            <RouterLink class="router-link flex-1 truncate" :to="`/function#baseline?type=ADD`">
              {{ t('functionBaseline.list.addBaseline') }}
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
                    :to="`/function#baseline?id=${item.id}`">
                    {{ item.name }}
                  </RouterLink>
                </div>

                <!-- <div class="text-3 whitespace-nowrap">
                  <span class="text-theme-title ml-2">{{ item.startDate }}</span>
                  <span class="text-theme-sub-content mx-2">至</span>
                  <span class="text-theme-title">{{ item.deadlineDate }}</span>
                </div> -->

                <div class="flex">
                  <div
                    class="text-theme-sub-content text-3 leading-4 flex items-center flex-none whitespace-nowrap mr-3.5">
                    <Icon
                      v-if="item.established"
                      icon="icon-duihao-copy"
                      class="mr-1" />
                    <div>{{ item.established ? t('functionBaseline.list.established') : t('functionBaseline.list.notEstablished') }}</div>
                  </div>
                </div>
              </div>
              <!-- <div class="px-3.5 flex mt-3 justify-end text-3 text-theme-sub-content">
                <div class="ml-8 text-theme-content">共{{ item.caseNum || 0 }}条用例</div>
              </div> -->

              <div class="px-3.5 flex flex-start justify-between text-3 text-theme-sub-content">
                <div class="flex flex-wrap space-x-8">
                  <div class="flex mt-3">
                    <div class="mr-2 whitespace-nowrap">
                      <span>ID</span>
                      <Colon />
                    </div>
                    <div class="text-theme-content">{{ item.id || "--" }}</div>
                  </div>

                  <div class="flex mt-3">
                    <div class="mr-2 whitespace-nowrap">
                      <span>{{ t('functionBaseline.list.testPlan') }}</span>
                      <Colon />
                    </div>
                    <div class="text-theme-content">{{ item.planName || "--" }}</div>
                  </div>
                </div>

                <div class="flex ml-8 mt-3">
                  <div
                    class="truncate text-theme-content"
                    style="max-width: 100px;"
                    :title="item.lastModifiedByName">
                    {{ item.lastModifiedByName }}
                  </div>
                  <div class="mx-2 whitespace-nowrap">{{ t('functionBaseline.list.modifiedBy') }}</div>
                  <div class="whitespace-nowrap text-theme-content">
                    {{ item.lastModifiedDate }}
                  </div>
                </div>
              </div>

              <div class="px-3.5 flex justify-between items-end text-3 my-2.5 relative">
                <div class="truncate mr-8" :title="item.description || ''">
                  <template v-if="item.description">
                    <!-- {{ item.description }} -->
                    <RichText :value="item.description" :emptyText="t('functionBaseline.list.noDescription')" />
                  </template>
                  <span v-else class="text-text-sub-content">
                    {{ t('functionBaseline.list.noDescription') }}
                  </span>
                </div>
                <div class="flex space-x-3 items-center justify-between h-4 leading-5">
                  <RouterLink class="flex items-center space-x-1" :to="`/function#baseline?id=${item.id}&type=edit`">
                    <Icon icon="icon-shuxie" class="text-3.5" />
                    <span>{{ t('functionBaseline.list.edit') }}</span>
                  </RouterLink>

                  <Button
                    v-if="!item.established"
                    size="small"
                    type="text"
                    class="px-0 flex items-center space-x-1"
                    @click="toCompleted(item)">
                    <Icon icon="icon-yiwancheng" class="text-3.5" />
                    <span>{{ t('functionBaseline.list.establishBaseline') }}</span>
                  </Button>

                  <Button
                    size="small"
                    type="text"
                    class="px-0 flex items-center space-x-1"
                    @click="toDelete(item)">
                    <Icon icon="icon-yiwancheng" class="text-3.5" />
                    <span>{{ t('functionBaseline.list.delete') }}</span>
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
