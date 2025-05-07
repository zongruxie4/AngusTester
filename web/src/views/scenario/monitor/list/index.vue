<script setup lang="ts">
import { defineAsyncComponent, inject, onBeforeUnmount, onMounted, ref, watch } from 'vue';
import { Button, Popover } from 'ant-design-vue';
import { Icon, modal, NoData, Spin } from '@xcan-angus/vue-ui';
import { debounce, throttle } from 'throttle-debounce';
import { useRouter } from 'vue-router';

import { scenario } from '@/api/tester';
import { MonitorInfo } from '../PropsType';
import SearchPanel from '@/views/scenario/monitor/list/searchPanel/index.vue';

const router = useRouter();

type Props = {
  projectId: string;
  userInfo: { id: string; };
  appInfo: { id: string; };
  notify: string;
  onShow: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  notify: undefined,
  onShow: false
});

type OrderByKey = 'createdDate' | 'createdByName';
type OrderSortKey = 'ASC' | 'DESC';

const Introduce = defineAsyncComponent(() => import('@/views/scenario/monitor/list/introduce/index.vue'));

const deleteTabPane = inject<(keys: string[]) => void>('deleteTabPane', () => ({}));
const addTabPane = inject<(keys: string[]) => void>('addTabPane', () => ({}));

const loaded = ref(false);
const loading = ref(false);
const searchedFlag = ref(false);
const dataListWrapRef = ref();

// const orderBy = ref<OrderByKey>();
// const orderSort = ref<OrderSortKey>();

const searchPanelParams = ref({
  orderBy: undefined,
  orderSort: undefined,
  filters: []
});
const pageNo = ref(1);
const pageSize = ref(16);
const total = ref(0);
const dataList = ref<MonitorInfo[]>([]);
const permissionsMap = ref<Map<string, string[]>>(new Map());

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

// 删除
const toDelete = async (data: MonitorInfo) => {
  modal.confirm({
    content: `确定删除监控【${data.name}】吗？`,
    async onOk () {
      const id = data.id;
      const [error] = await scenario.deleteMonitor({
        ids: [id]
      });
      if (error) {
        return;
      }
      pageNo.value = 1;
      loadData();
      deleteTabPane([id, id + '-case']);
    }
  });
};

// 打开编辑
const editMonitor = (data: MonitorInfo) => {
  addTabPane({
    value: 'monitorEdit',
    _id: data.id,
    id: data.id,
    data,
    name: data.name
  });
};

// 打开详情
const handleDetail = (data: MonitorInfo) => {
  addTabPane({
    value: 'monitorDetails',
    _id: data.id + '-case',
    id: data.id,
    data,
    name: data.name
  });
};

const run = async (data: MonitorInfo) => {
  modal.confirm({
    content: `立即执行监控【${data.name}】`,
    async onOk () {
      const id = data.id;
      const [error] = await scenario.runMonitor(id);
      if (error) {
        return;
      }
      pageNo.value = 1;
      loadData();
    }
  });
};

// 监控列表
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

  const [error, res] = await scenario.searchMonitor(params);
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

    const _list = (data.list || [] as MonitorInfo[]);
    dataList.value = _list;
  }
};

const getScenarioDetail = async (scenarioId) => {
  const [error, { data }] = await scenario.loadInfo(scenarioId);
  if (error) {
    return;
  }
  const { id, name, plugin } = data;
  router.push(`/scenario#scenario?id=${id}&name=${name}&plugin=${plugin}&type=detail`);
};

const handleScrollList = throttle(500, (event) => {
  if (dataList.value.length === total.value || loading.value) {
    return;
  }
  const clientHeight = event.currentTarget.clientHeight;
  const scrollHeight = event.currentTarget.scrollHeight;
  const scrollTop = event.currentTarget.scrollTop;
  if (clientHeight + scrollTop + 100 > scrollHeight) {
    pageNo.value += 1;
    loadData();
  }
});

let wrapperHeight = 0;
const handleWindowResize = debounce(300, () => {
  if (!props.onShow) {
    return;
  }
  const height = dataListWrapRef.value?.clientHeight || 0;
  if (height <= wrapperHeight) {
    return;
  }
  wrapperHeight = height;

  if (!dataList.value.length) {
    return;
  }
  if ((height / 160) > 4) {
    const rows = Math.floor(height / 160) + 2;
    pageSize.value = rows * 4;
  } else {
    pageSize.value = 16;
  }
  if (dataList.value.length < pageSize.value) {
    pageNo.value = 1;
    loadData();
  }
});

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

  window.addEventListener('resize', handleWindowResize);
});

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleWindowResize);
});

</script>

<template>
  <div class="flex flex-col h-full overflow-auto px-5 py-5 leading-5 text-3">
    <div class="flex space-x-2">
      <Introduce class="mb-7 flex-1" />
    </div>

    <div class="text-3.5 font-semibold mb-1">已添加的监控</div>
    <Spin :spinning="loading" class="flex-1 flex flex-col">
      <template v-if="loaded">
        <div v-if="!searchedFlag && dataList.length === 0" class="flex-1 flex flex-col items-center justify-center">
          <img src="../../../../assets/images/nodata.png">
          <div class="flex items-center text-theme-sub-content text-3.5 leading-5 space-x-1">
            <span>您尚未添加任何监控，立即</span>
            <RouterLink class="router-link flex-1 truncate" :to="`/scenario#monitor?type=ADD`">
              添加监控
            </RouterLink>
          </div>
        </div>

        <template v-else>
          <SearchPanel
            :projectId="props.projectId"
            @change="searchChange"
            @refresh="refresh" />

          <NoData v-if="dataList.length === 0" class="flex-1" />

          <div
            v-else
            ref="dataListWrapRef"
            class="flex-1"
            style="height: fit-content"
            @scroll="handleScrollList">
            <div class="flex flex-wrap">
              <div
                v-for="(item) in dataList"
                :key="item.id"
                style="width:calc(25% - 8px); margin-right: 8px;"
                class="border rounded py-2 px-4 flex-grow-0 h-37.5 mb-3">
                <div class="text-theme-special text-3.5 font-medium cursor-pointer" @click="handleDetail(item)">{{ item.name }}</div>

                <div class="flex mt-2 justify-between items-center">
                  <div class="inline-flex items-center">
                    <span class="text-6 font-semibold" :class="item.status?.value">{{ item.status?.message }}</span>
                    <Popover>
                      <template #content>
                        <div class="max-w-80">{{ item.failureMessage }}</div>
                      </template>
                      <Icon
                        v-if="item.status?.value === 'FAILURE'"
                        icon="icon-tishi1"
                        class="text-status-warn ml-1" />
                    </Popover>
                  </div>

                  <div class="text-center">
                    <span class="font-semibold text-3.5">{{ item.count?.successRate ? item.count?.successRate + '%' : '--' }}</span>
                    <div>成功率</div>
                  </div>
                  <div class="text-center">
                    <span class="font-semibold text-3.5">{{ item.count?.avgDelayTime || '--' }}</span>
                    <div>平均延迟</div>
                  </div>
                </div>
                <div class="mt-2 inline-flex max-w-full">
                  <template v-if="item.status?.value === 'PENDING'">
                    <span>将运行“</span><a
                      class="text-blue-1 truncate min-w-0 flex-1"
                      :title="item.scenarioName"
                      @click="getScenarioDetail(item.scenarioId)">{{ item.scenarioName }}</a><span>”在 {{ item.nextExecDate }}</span>
                  </template>
                  <template v-else>
                    <span>最后运行“</span><a
                      class="text-blue-1 truncate min-w-0 flex-1"
                      :title="item.scenarioName"
                      @click="getScenarioDetail(item.scenarioId)">{{ item.scenarioName }}</a><span>”在 {{ item.lastMonitorDate }}</span>
                  </template>
                </div>

                <div class="flex justify-between items-center mt-2">
                  <span class="flex-1 min-w-0 truncate" :title="`${item.createdByName} 创建于 ${ item.createdDate }`">{{ item.createdByName }} 创建于 {{ item.createdDate }}</span>
                  <div>
                    <Popover>
                      <template #content>
                        立即执行
                      </template>
                      <Button size="small" type="text">
                        <Icon icon="icon-zhihang" @click="run(item)" />
                      </Button>
                    </Popover>
                    <Button
                      size="small"
                      type="text"
                      @click="editMonitor(item)">
                      <Icon icon="icon-xiugai" />
                    </Button>
                    <Button
                      size="small"
                      type="text"
                      @click="toDelete(item)">
                      <Icon icon="icon-qingchu" />
                    </Button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </template>
      </template>
    </Spin>
  </div>
</template>

<style scoped>

.SUCCESS {
  @apply text-status-success;
}

.PENDING {
  @apply text-status-pending;
}

.FAILURE {
  @apply text-status-error1;
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

</style>
