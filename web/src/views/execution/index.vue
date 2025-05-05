<script setup lang="ts">
import { computed, defineAsyncComponent, inject, nextTick, onBeforeUnmount, onMounted, Ref, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import {
  Colon,
  Dropdown,
  Icon,
  Input,
  modal,
  NoData,
  notification,
  ScriptTypeTag,
  ShortDuration,
  Spin,
  Tooltip
} from '@xcan-angus/vue-ui';
import { http, site, TESTER } from '@xcan-angus/tools';
import { Pagination, Progress, Switch } from 'ant-design-vue';
import dayjs from 'dayjs';

import { getCurrentPage } from '@/utils/utils';
import { getCurrentDuration } from '@/utils';
import { ExecuteListObj } from './PropsType';

type OrderByKey = 'createdDate' | 'createdByName';
type OrderSortKey = 'ASC' | 'DESC';

const NavigationHeader = defineAsyncComponent(() => import('./components/navigationHeader.vue'));
const SearchPanel = defineAsyncComponent(() => import('@/views/execution/searchPanel/index.vue'));

const userInfo = inject<Ref<{ id: string }>>('tenantInfo');
const projectInfo = inject<Ref<{ id: string; avatar: string; name: string; }>>('projectInfo', ref({ id: '', avatar: '', name: '' }));

const nameInputRefs = ref<{ [key: string]: any }>({});
const priorityInputRefs = ref<{ [key: string]: any }>({});
const reportIntervalInputRefs = ref<{ [key: string]: any }>({});
const durationInputRefs = ref<{ [key: string]: any }>({});
const threadInputRefs = ref<{ [key: string]: any }>({});
const iterationsInputRefs = ref<{ [key: string]: any }>({});

const router = useRouter();
const route = useRoute();

const isPrivate = ref(false);

const loaded = ref(false);
const loading = ref(false);
const refinshLoading = ref(false);
const orderBy = ref<OrderByKey>();
const orderSort = ref<OrderSortKey>();
const pagination = ref({
  current: 1,
  pageSize: 5,
  total: 0
});
const filters = ref<{ key: string; op: string; value: boolean | string | string[]; }[]>([]);
const dataList = ref<ExecuteListObj[]>([]);

const total = ref(0);
const execIds = ref<string[]>([]);
const reportInterval = ref(3);

let timer: NodeJS.Timeout | null = null;

const toSort = (data: { orderBy: OrderByKey; orderSort: OrderSortKey; }) => {
  orderBy.value = data.orderBy;
  orderSort.value = data.orderSort;
  pagination.value.current = 1;
  loadDataList();
};

const searchPanelChange = (data: { key: string; op: string; value: boolean | string | string[]; }[]) => {
  filters.value = data;
  pagination.value.current = 1;
  loadDataList();
};

const hasStartDate = (startAtDate: string) => {
  if (!startAtDate) {
    return false;
  }

  const givenTime = dayjs(startAtDate);
  const currentTime = dayjs();

  if (givenTime.isBefore(currentTime) || givenTime.isSame(currentTime)) {
    return false;
  } else {
    return true;
  }
};

const splitTime = (str: string): [string, string] => {
  const number = str.replace(/\D/g, '');
  const unit = str.replace(/\d/g, '');
  return [number, letterMap[unit]];
};

const setTimeoutTimes = (item: ExecuteListObj) => {
  if (item.reportInterval) {
    const time = +splitTime(item.reportInterval)[0];
    if (reportInterval.value === 3) {
      if (time > 3) {
        reportInterval.value = time;
      }
    } else if (reportInterval.value > 3) {
      if (time < reportInterval.value) {
        reportInterval.value = time;
      }
    }
  }
};

const getMessage = (item: ExecuteListObj): { code: string, message: string, codeName: string, messageName: string } | undefined => {
  if (item?.lastSchedulingResult?.length) {
    const foundItem = item.lastSchedulingResult.find(f => !f.success);
    if (foundItem) {
      return { code: foundItem.exitCode, message: foundItem.message, codeName: '退出码', messageName: '失败原因' };
    }

    if (item?.meterMessage) {
      return { code: item.meterStatus, message: item.meterMessage, codeName: '采样状态', messageName: '失败原因' };
    }

    return undefined;
  }

  if (item?.meterMessage) {
    return { code: item.meterStatus, message: item.meterMessage, codeName: '采样状态', messageName: '失败原因' };
  }

  return undefined;
};

const getStrokeColor = (value: string, status: string) => {
  const num = Number(value?.slice(-1) === '%' ? value.slice(0, -1) : value);
  if (num === 100) {
    return 'rgba(82, 196, 26, 1)';
  }

  switch (status) {
    case 'CREATED': // 已添加
      return 'rgba(45, 142, 255, 1)';
    case 'PENDING': // 调度中
      return 'rgba(45, 142, 255, 1)';
    case 'RUNNING': // 执行中
      return 'rgba(45, 142, 255, 1)';
    case 'STOPPED': // 已停止
      return 'rgba(255, 77, 79, 1)';
    case 'FAILED': // 失败
      return 'rgba(255, 77, 79, 1)';
    case 'COMPLETED': // 已完成
      return 'rgba(45, 142, 255, 1)';
    case 'TIMEOUT': // 失败
      return 'rgba(255, 77, 79, 1)';
  }
};

const loadDataListByIds = async (isRefinsh: boolean): Promise<void> => {
  if (refinshLoading.value && !isRefinsh) {
    return;
  }

  if (!execIds.value.length && timer) {
    clearTimeout(timer);
    return;
  }

  const params = { filters: [{ key: 'id', op: 'IN', value: execIds.value }], projectId: projectInfo.value?.id };
  if (isRefinsh) {
    loading.value = true;
  }
  refinshLoading.value = true;
  const [error, { data = { list: [], total: 0 } }] = await http.get(`${TESTER}/exec`, params);
  refinshLoading.value = false;
  if (isRefinsh) {
    loading.value = false;
  }
  if (error) {
    return;
  }

  if (data.list?.length) {
    execIds.value = [];
    for (let i = 0; i < data.list.length; i++) {
      const dataItem = data.list[i];
      const errMessage = getMessage(dataItem);
      const _hasStartDate = hasStartDate(dataItem?.startAtDate);
      setTimeoutTimes(dataItem);
      const executeItemIndex = dataList.value.findIndex(item => item.id === dataItem.id);
      if (executeItemIndex !== -1) {
        dataList.value[executeItemIndex] = { ...dataItem, errMessage };
      }

      if (['CREATED', 'PENDING', 'RUNNING'].includes(dataItem?.status.value) && !_hasStartDate) {
        execIds.value.push(dataItem.id);
      }
    }
  }

  if (timer) {
    clearTimeout(timer);
  }
  if (execIds.value.length) {
    timer = setTimeout(async () => {
      await loadDataListByIds(false);
    }, reportInterval.value * 1000);
  }
};

const loadDataList = async (): Promise<void> => {
  if (loading.value) {
    return;
  }

  const { current, pageSize } = pagination.value;
  const params: {
    pageNo: number;
    pageSize: number;
    projectId: string;
    filters?: {
      key: string;
      op: string;
      value: boolean | string | string[];
    }[];
    orderBy?: OrderByKey;
    orderSort?: OrderSortKey;
  } = {
    pageNo: current,
    pageSize,
    projectId: projectId.value
  };

  if (filters.value?.length) {
    params.filters = filters.value;
  }

  if (orderSort.value) {
    params.orderBy = orderBy.value;
    params.orderSort = orderSort.value;
  }

  loading.value = true;
  const [error, { data = { list: [], total: 0 } }] = await http.get(`${TESTER}/exec`, params);
  loading.value = false;
  loaded.value = true;
  if (error) {
    return;
  }

  execIds.value = [];
  dataList.value = data.list.map(item => {
    const _hasStartDate = hasStartDate(item?.startAtDate);
    if (['CREATED', 'PENDING', 'RUNNING'].includes(item?.status.value) && !_hasStartDate) {
      execIds.value.push(item.id);
    }
    setTimeoutTimes(item);
    const errMessage = getMessage(item);
    const others = {
      ...item,
      errMessage,
      editname: false,
      editpriority: false,
      editreportInterval: false,
      editstartDate: false,
      editstartAtDate: false,
      edititerations: false,
      editduration: false
    };
    const _actionPermission: string[] = [];
    if (item.hasOperationPermission) {
      if (['CREATED', 'PENDING', 'RUNNING'].includes(item?.status.value) && !_hasStartDate) {
        _actionPermission.push('stopNow');
      } else {
        _actionPermission.push('edit');
      }

      if (!['PENDING'].includes(item?.status.value)) {
        _actionPermission.push('delete');
      }
    }

    const durationStrokeColor = getStrokeColor(item.currentDurationProgress, item.status?.value);
    const iterationStrokeColor = getStrokeColor(item.currentIterationsProgress, item.status?.value);

    return { ...others, actionPermission: _actionPermission, durationStrokeColor, iterationStrokeColor };
  });

  total.value = +data.total;

  if (timer) {
    clearTimeout(timer);
  }

  if (execIds.value.length) {
    timer = setTimeout(async () => {
      await loadDataListByIds(false);
    }, reportInterval.value * 1000);
  }
};

const showTotal = (total: number) => {
  const totalPage = Math.ceil(total / pagination.value.pageSize);
  return `共${total}条记录,第${pagination.value.current} / ${totalPage}页`;
};

const updateName = (item: ExecuteListObj) => {
  item.editname = true;
  nextTick(() => {
    nameInputRefs.value?.[item.id].focus();
  });
};

const editName = async (name: string, item: ExecuteListObj) => {
  if (name === item.name || loading.value) {
    item.editname = false;
    return;
  }

  loading.value = true;
  const [error] = await http.put(`${TESTER}/exec/${item.id}/config`, { name });
  loading.value = false;
  if (error) {
    return;
  }

  item.editname = false;
  item.name = name;
  notification.success('修改成功');
};

const updateThread = (item: ExecuteListObj) => {
  item.editThread = true;
  nextTick(() => {
    threadInputRefs.value?.[item.id].focus();
  });
};

const threadsMax = (item: ExecuteListObj) => {
  return ['MOCK_DATA', 'MOCK_APIS'].includes(item?.scriptType?.value) ? 1000 : 10000;
};

const editThread = async (value: string, item: ExecuteListObj) => {
  if ((!item?.thread && !value) || item.thread === value || loading.value) {
    item.editThread = false;
    return;
  }

  loading.value = true;
  const [error] = await http.put(`${TESTER}/exec/${item.id}/config`, { thread: value });
  loading.value = false;
  if (error) {
    return;
  }

  item.thread = value;
  item.editThread = false;
  notification.success('修改成功');
};

const updateDuration = (item: ExecuteListObj) => {
  item.editduration = true;
  nextTick(() => {
    durationInputRefs.value?.[item.id].inputRef?.focus();
  });
};

const editDuration = async (value: string, item: ExecuteListObj) => {
  if ((!item?.duration && !value) || item.duration === value || loading.value) {
    item.editduration = false;
    return;
  }

  loading.value = true;
  const [error] = await http.put(`${TESTER}/exec/${item.id}/config`, { duration: value });
  loading.value = false;
  if (error) {
    return;
  }

  item.duration = value;
  item.editduration = false;
  notification.success('修改成功');
};

const updateIterations = (item: ExecuteListObj) => {
  item.edititerations = true;
  nextTick(() => {
    iterationsInputRefs.value?.[item.id].focus();
  });
};

const editIterations = async (value: string, item: ExecuteListObj) => {
  if ((!item?.iterations && !value) || item.iterations === value || loading.value) {
    item.edititerations = false;
    return;
  }

  loading.value = true;
  const [error] = await http.put(`${TESTER}/exec/${item.id}/config`, { iterations: value });
  loading.value = false;
  if (error) {
    return;
  }
  item.iterations = value;
  item.edititerations = false;
  notification.success('修改成功');
};

const getNumFixed = (str: string) => {
  return str ? Number(str).toFixed(2) : '0';
};

const updatePriority = (item: ExecuteListObj) => {
  item.editpriority = true;
  nextTick(() => {
    priorityInputRefs.value?.[item.id].focus();
  });
};

const editPriority = async (value: string, item: ExecuteListObj) => {
  if ((item?.priority && item.priority === value) || loading.value) {
    item.editpriority = false;
    return;
  }

  loading.value = true;
  const [error] = await http.put(`${TESTER}/exec/${item.id}/config`, { priority: value });
  loading.value = false;
  if (error) {
    return;
  }

  item.editpriority = false;
  item.priority = value;
  notification.success('修改成功');
};

const updateReportInterval = (item: ExecuteListObj) => {
  item.editreportInterval = true;
  nextTick(() => {
    reportIntervalInputRefs.value?.[item.id].inputRef?.focus();
  });
};

const editReportInterval = async (value: string, item: ExecuteListObj) => {
  if ((!item?.reportInterval && !value) || item.reportInterval === value || loading.value) {
    item.editreportInterval = false;
    return;
  }

  loading.value = true;
  const [error] = await http.put(`${TESTER}/exec/${item.id}/config`, { reportInterval: value });
  loading.value = false;
  if (error) {
    return;
  }

  item.editreportInterval = false;
  item.reportInterval = value;
  notification.success('修改成功');
};

const handleIgnoreAssertions = async (value, item: ExecuteListObj) => {
  if (loading.value) {
    return;
  }

  loading.value = true;
  const [error] = await http.put(`${TESTER}/exec/${item.id}/config`, { ignoreAssertions: value });
  loading.value = false;
  if (error) {
    return;
  }

  item.editstartDate = false;
  item.ignoreAssertions = value;
  notification.success('修改成功');
};

const handleUpdateTestResult = async (value, item: ExecuteListObj) => {
  if (loading.value) {
    return;
  }

  loading.value = true;
  const [error] = await http.put(`${TESTER}/exec/${item.id}/config`, { updateTestResult: value });
  loading.value = false;
  if (error) {
    return;
  }

  item.editstartDate = false;
  item.updateTestResult = value;
  notification.success('修改成功');
};

const handleRestart = async (item: ExecuteListObj) => {
  if (loading.value) {
    return;
  }

  item.errMessage = undefined;
  item.editname = false;
  item.editpriority = false;
  item.editstartDate = false;
  item.editduration = false;
  item.editreportInterval = false;
  item.edititerations = false;
  const params = {
    broadcast: true,
    id: item.id
  };

  loading.value = true;
  const [error, { data }] = await http.post(`${TESTER}/exec/start`, params);
  loading.value = false;
  if (error) {
    let errMessage;
    if (error?.code || error?.message) {
      errMessage = { code: error.code, message: error.message, codeName: '退出码', messageName: '失败原因' };
    }
    item.errMessage = errMessage;

    return;
  }

  const currItemDataList = data.filter(f => f.execId === item.id);
  if (currItemDataList.length) {
    const successFalseItem = currItemDataList.find(f => f.success);
    if (successFalseItem) {
      notification.success('启动成功');
    } else {
      notification.error(currItemDataList[0].message);
      item.errMessage = { code: currItemDataList[0]?.exitCode, message: currItemDataList[0]?.message, codeName: '退出码', messageName: '失败原因' };
    }
  }

  if (!execIds.value.includes(item.id)) {
    execIds.value.push(item.id);
  }

  await loadDataListByIds(true);
};

const handleStop = async (item: ExecuteListObj) => {
  if (loading.value) {
    return;
  }

  item.errMessage = undefined;
  const params = {
    broadcast: true,
    id: item.id
  };
  loading.value = true;
  const [error, { data }] = await http.post(`${TESTER}/exec/stop`, params);
  loading.value = false;
  if (error) {
    let errMessage;
    if (error?.code || error?.message) {
      errMessage = { code: error.code, message: error.message, codeName: '退出码', messageName: '失败原因' };
    }
    item.errMessage = errMessage;
    return;
  }

  const currItemDataList = data.filter(f => f.execId === item.id);
  if (currItemDataList.length) {
    const successFalseItem = currItemDataList.find(f => f.success);
    if (successFalseItem) {
      notification.success('停止成功');
    } else {
      notification.error(currItemDataList[0].message);
      item.errMessage = { code: currItemDataList[0]?.exitCode, message: currItemDataList[0]?.message, codeName: '退出码', messageName: '失败原因' };
    }
  }

  if (!execIds.value.includes(item.id)) {
    execIds.value.push(item.id);
  }

  await loadDataListByIds(true);
};

const handleDelete = async (item: ExecuteListObj) => {
  if (loading.value) {
    return;
  }

  modal.confirm({
    centered: true,
    content: `确定删除【${item.name}】吗？`,
    async onOk () {
      loading.value = true;
      const [error] = await http.del(`${TESTER}/exec`, [item.id], { dataType: true });
      loading.value = false;
      if (error) {
        return;
      }
      notification.success('删除成功');
      pagination.value.current = getCurrentPage(pagination.value.current, pagination.value.pageSize, total.value);
      loadDataList();
    }
  });
};

const dropdownClick = (key: string, item: ExecuteListObj) => {
  switch (key) {
    case 'edit':
      router.push(`/execution/edit/${item.id}`);
      break;
    case 'viewLog':
      router.push(`/execution/info/${item.id}?&pageNo=${pagination.value.current}&tab=log`);
      break;
    case 'delete':
      handleDelete(item);
      break;
  }
};

const paginationChange = (pageNo: number, pageSize: number) => {
  pagination.value.current = pageNo;
  pagination.value.pageSize = pageSize;
  loadDataList();
};

const initialize = () => {
  const query = route.query;
  if (query) {
    const { pageNo } = query;
    if (pageNo) {
      pagination.value.current = +pageNo as number;
      router.replace('/execution');
    }
  }
};

onMounted(async () => {
  isPrivate.value = await site.isPrivate();
  watch(() => projectId.value, (newValue) => {
    if (!newValue) {
      return;
    }

    initialize();
  }, { immediate: true });
});

onBeforeUnmount(() => {
  if (timer) {
    clearTimeout(timer);
    timer = null;
  }
});

const projectId = computed(() => {
  return projectInfo.value?.id;
});

const letterMap = {
  ms: '毫秒',
  s: '秒',
  min: '分钟',
  h: '小时',
  d: '天'
};

const scriptTypes = {
  TEST_PERFORMANCE: {
    text: 'P',
    color: 'bg-bg-performance'
  },
  TEST_FUNCTIONALITY: {
    text: 'F',
    color: 'bg-bg-function'
  },
  TEST_STABILITY: {
    text: 'S',
    color: 'bg-bg-stability'
  },
  MOCK_DATA: {
    text: 'M',
    color: 'bg-bg-mock'
  },
  TEST_CUSTOMIZATION: {
    text: 'C',
    color: 'bg-bg-custom'
  }
};

const resBgColorMap = {
  CREATED: 'bg-status-pending',
  PENDING: 'bg-status-pending',
  RUNNING: 'bg-status-process',
  STOPPED: 'bg-status-error',
  FAILED: 'bg-status-error',
  TIMEOUT: 'bg-status-error',
  COMPLETED: 'bg-status-success'
};

const dropdownMockMenuItems = [
  {
    key: 'viewLog',
    icon: 'icon-zhihangcanshu',
    name: '查看调度日志',
    noAuth: true
  },
  {
    key: 'delete',
    icon: 'icon-qingchu',
    name: '删除',
    permission: 'delete'
  }
];

const dropdownMenuItems = [
  {
    key: 'edit',
    icon: 'icon-shuxie',
    name: '修改执行配置',
    permission: 'edit'
  },
  {
    key: 'viewLog',
    icon: 'icon-zhihangcanshu',
    name: '查看调度日志',
    noAuth: true
  },
  {
    key: 'delete',
    icon: 'icon-qingchu',
    name: '删除',
    permission: 'delete'
  }
];

const selectProps = {
  style: { width: '60px' }
};

const reportIntervalSelectProps = {
  ...selectProps,
  excludes: ({ message }): boolean => {
    return ['ms', 'min', 'h', 'd'].includes(message);
  }
};
</script>
<template>
  <Spin
    :spinning="loading"
    :delay="0"
    class="w-full h-full py-5 px-5 text-3 overflow-y-auto"
    style="scrollbar-gutter: stable;">
    <NavigationHeader class="mb-3" />
    <SearchPanel
      :projectId="projectId"
      :userInfo="userInfo"
      class="mb-3.5"
      @sort="toSort"
      @change="searchPanelChange" />
    <template v-if="loaded">
      <div v-if="dataList?.length" class="flex-1 overflow-y-auto space-y-3.5">
        <div
          v-for="item in dataList"
          :key="item.id"
          class="border border-border-divider rounded">
          <div class="px-5 flex items-center bg-bg-table-head border-b border-border-divider py-0.25 w-full space-x-2">
            <div
              class="w-5 h-5 rounded-full flex items-center justify-center text-white flex-none"
              :class="scriptTypes[item?.scriptType?.value]?.color">
              {{ scriptTypes[item?.scriptType?.value]?.text }}
            </div>
            <div class="flex-none flex space-x-2" style="width: calc(100% - 562px)">
              <div class="flex items-center h-7 justify-between  pr-4" style="width:30%">
                <div class="flex items-center mr-2" style="width:calc(100% - 32px)">
                  <template v-if="!item?.editname">
                    <RouterLink
                      :to="`/execution/info/${item.id}?pageNo=${pagination.current}`"
                      class="text-3 font-medium truncate"
                      :title="item?.name">
                      {{ item?.name }}
                    </RouterLink>
                    <template
                      v-if="!['CREATED', 'PENDING', 'RUNNING'].includes(item?.status?.value) && item?.hasOperationPermission">
                      <Icon
                        icon="icon-shuxie"
                        class="text-text-link ml-2 cursor-pointer -mt-0.25 flex-none"
                        @click="updateName(item)" />
                    </template>
                  </template>
                  <template v-else>
                    <Input
                      :ref="el => { nameInputRefs[item.id] = el }"
                      :value="item.name"
                      size="small"
                      :maxlength="100"
                      @blur="editName($event.target.value, item)" />
                  </template>
                </div>
                <div style="width:42px" class="flex-none">
                  <tmeplate v-if="item?.trial">
                    <div
                      class="px-2 h-4 rounded-full text-3 flex-none leading-4 border border-status-success text-status-success whitespace-nowrap">
                      试用
                    </div>
                  </tmeplate>
                </div>
              </div>
              <div class="flex items-center" style="width:13%;max-width:90px;">
                <span
                  class="px-3 py-0.25 rounded-2xl text-center truncate cursor-pointer"
                  :title="item?.plugin"
                  style="min-width:60px;background-color:rgba(0, 119, 255, 10%);color:rgba(0, 119, 255, 100%)">{{
                    item?.plugin
                  }}</span>
              </div>
              <div class="flex items-center" style="width:13%">
                <ScriptTypeTag :value="item?.scriptType" class="transform-gpu -translate-y-0.25" />
              </div>
              <div class="flex items-center whitespace-nowrap" style="width:13%">
                <div class="w-1.5 h-1.5 mr-1 rounded" :class="resBgColorMap[item.status?.value]"></div>
                <span>{{ item?.status?.message }}</span>
                <template v-if="item?.status?.value !== 'COMPLETED' && item?.errMessage">
                  <Tooltip
                    placement="topLeft"
                    arrowPointAtCenter
                    :overlayStyle="{ 'max-width': '400px' }">
                    <Icon icon="icon-tishi1" class="ml-1 text-3.5 text-status-warn cursor-pointer" />
                    <template #title>
                      <div class="flex">
                        <div class="mr-2">
                          <div class="whitespace-nowrap mb-1">
                            {{ item?.errMessage?.codeName }}
                            <Colon />
                          </div>
                          <div class="whitespace-nowrap">
                            {{ item?.errMessage?.messageName }}
                            <Colon />
                          </div>
                        </div>
                        <div>
                          <div class="mb-1">{{ item?.errMessage?.code }}</div>
                          <div class="leading-5">{{ item?.errMessage?.message }}</div>
                        </div>
                      </div>
                    </template>
                  </Tooltip>
                </template>
              </div>
              <div class="flex items-center" style="width:20%">
                <span class="whitespace-nowrap">并发数
                  <Colon class="mr-1.5" />
                </span>
                <div class="whitespace-nowrap flex-1">
                  {{ item?.sampleSummaryInfo?.threadPoolActiveSize || '0' }}/
                  <template v-if="!item?.editThread">
                    {{ item?.thread }}
                    <template
                      v-if="!['CREATED', 'PENDING', 'RUNNING'].includes(item?.status?.value) && item?.hasOperationPermission && item?.scriptType?.value !== 'TEST_FUNCTIONALITY'">
                      <Icon
                        icon="icon-shuxie"
                        class="text-text-link ml-2 cursor-pointer -mt-0.25 flex-none"
                        @click="updateThread(item)" />
                    </template>
                  </template>
                  <template v-else>
                    <Input
                      :ref="el => { threadInputRefs[item.id] = el }"
                      :value="item?.thread"
                      :min="1"
                      :max="threadsMax(item)"
                      class="w-25"
                      dataType="number"
                      size="small"
                      @blur="editThread($event.target.value, item)" />
                  </template>
                </div>
              </div>
            </div>
            <div class="flex items-center text-text-sub-content font-medium flex-none space-x-2 justify-end">
              <div style="width:150px">
                <span>ID
                  <Colon class="mr-1.5" />
                </span>
                <span class="text-text-content"> {{ item?.id || "--" }}</span>
              </div>
              <div style="width:180px">
                <span class="whitespace-nowrap">开始时间
                  <Colon class="mr-1.5" />
                </span>
                <span class="text-text-content  whitespace-nowrap">{{ item?.actualStartDate || "--" }}</span>
              </div>
              <div style="width:180px">
                <span class="whitespace-nowrap">结束时间
                  <Colon class="mr-1.5" />
                </span>
                <span class="text-text-content">{{ item?.endDate || "--" }}</span>
              </div>
            </div>
          </div>
          <div class="py-0.5 px-5 text-text-content flex items-center space-x-3.5">
            <div class="flex space-x-3.5 justify-between items-center" style="width: calc(100% - 68px)">
              <div class="flex flex-col justify-center flex-none" style="width:20%">
                <div
                  v-if="item?.durationProgress"
                  class="mb-1"
                  style="max-width:260px;">
                  <div class="flex justify-center items-center">
                    <div>{{ getCurrentDuration(item?.currentDuration, item?.duration) }}</div>
                    <div class="mx-1">/</div>
                    <div class="truncate flex items-center relative z-0 h-7">
                      <template v-if="!item?.editduration">
                        {{ splitTime(item?.duration)[0] + splitTime(item?.duration)[1] }}
                        <template
                          v-if="!['CREATED', 'PENDING', 'RUNNING'].includes(item?.status?.value) && item?.hasOperationPermission">
                          <Icon
                            icon="icon-shuxie"
                            class="text-text-link ml-2 cursor-pointer -mt-0.25"
                            @click="updateDuration(item)" />
                        </template>
                      </template>
                      <template v-else>
                        <ShortDuration
                          :ref="el => { durationInputRefs[item.id] = el }"
                          :value="item.duration"
                          size="small"
                          class="flex-1"
                          style="max-width: 180px;"
                          :selectProps="selectProps"
                          placeholder="执行时长"
                          @blur="editDuration($event, item)" />
                      </template>
                    </div>
                  </div>
                  <Progress
                    :showInfo="false"
                    :width="90"
                    :strokeWidth="10"
                    :strokeColor="item.durationStrokeColor"
                    :percent="item?.currentDurationProgress ? +item.currentDurationProgress : 0">
                  </Progress>
                </div>
                <div v-if="item?.iterationsProgress" style="max-width:260px;">
                  <div class="flex justify-center items-center -mb-1">
                    <div>{{ +item?.currentIterations || "0" }}</div>
                    <div class="mx-1">/</div>
                    <div class="truncate flex items-center relative z-0 h-7" :class="item?.edititerations ? 'w-30 ' : ''">
                      <template v-if="!item?.edititerations">
                        {{ +item?.iterations || "--" }}
                        <template
                          v-if="!['CREATED', 'PENDING', 'RUNNING'].includes(item?.status?.value) && item?.hasOperationPermission">
                          <Icon
                            icon="icon-shuxie"
                            class="text-text-link ml-2 cursor-pointer -mt-0.25"
                            @click="updateIterations(item)" />
                        </template>
                      </template>
                      <template v-else>
                        <Input
                          :ref="el => { iterationsInputRefs[item.id] = el }"
                          :value="item.iterations"
                          :min="1"
                          :max="item?.scriptType?.value === 'TEST_FUNCTIONALITY' ? 10 : 100000000"
                          dataType="number"
                          size="small"
                          class="absolute top-0 h-5 z-9"
                          @blur="editIterations($event.target.value, item)" />
                      </template>
                    </div>
                  </div>
                  <Progress
                    :showInfo="false"
                    :width="90"
                    :strokeWidth="10"
                    :strokeColor="item.iterationStrokeColor"
                    :percent="item?.currentIterationsProgress ? +item.currentIterationsProgress : 0">
                  </Progress>
                </div>
              </div>
              <div style="width:38%" class="flex-none">
                <div class="flex items-center space-x-5 justify-between text-3" style="max-width:438px;">
                  <div class="rounded">
                    <div class="flex items-center border border-border-divider rounded">
                      <div
                        class="px-2.25 py-1.5  rounded-l text-text-sub-content flex items-center"
                        style="min-width: 90px;">
                        <Icon class="mr-1 text-4.5 text-status-pending" icon="icon-QPS" /><span>QPS</span>
                      </div>
                      <div class="px-3.75 w-28.5 text-right rounded-r">
                        {{ getNumFixed(item?.sampleSummaryInfo?.ops) }}
                      </div>
                    </div>
                    <div class="flex items-center border border-border-divider rounded mt-2">
                      <div
                        class="px-2.25 py-1.5 rounded-l text-text-sub-content flex items-center"
                        style="min-width: 90px;">
                        <Icon class="mr-1 text-4.5 text-status-success" icon="icon-RT" /><span>AVG/RT</span>
                      </div>
                      <div class="px-3.75 w-28.5 text-right rounded-r">
                        {{
                          getNumFixed(item?.sampleSummaryInfo?.tranMean)+'ms' }}
                      </div>
                    </div>
                  </div>
                  <div>
                    <div class="flex items-center border border-border-divider rounded">
                      <div
                        class="px-2.25 py-1.5  rounded-l text-text-sub-content flex items-center"
                        style="min-width: 90px;">
                        <Icon class="mr-1 text-4.5 text-status-warn" icon="icon-TPS" /><span>TPS</span>
                      </div>
                      <div class="px-3.75 w-28.5 text-right rounded-r">
                        {{ getNumFixed(item?.sampleSummaryInfo?.tps) }}
                      </div>
                    </div>
                    <div class="flex items-center border border-border-divider rounded mt-2">
                      <div
                        class="px-2.25 py-1.5 rounded-l text-text-sub-content flex items-center"
                        style="min-width: 90px;">
                        <Icon class="mr-1 text-4.5 text-status-error" icon="icon-cuowushuai1" /><span>错误率</span>
                      </div>
                      <div class="px-3.75 w-28.5 text-right rounded-r">
                        {{
                          item?.sampleSummaryInfo?.errorRate ? item.sampleSummaryInfo.errorRate+'%':'0%' }}
                      </div>
                    </div>
                  </div>
                </div>
              </div>
              <div class="flex leading-7 justify-center mx-5 flex-none" style="width:40%">
                <div class="flex w-1/2">
                  <div class="text-text-sub-content space-y-1 flex-none w-15">
                    <div class="whitespace-nowrap h-7">
                      脚本名称
                      <Colon />
                    </div>
                    <div class="whitespace-nowrap h-7">
                      优先级
                      <Colon />
                    </div>
                    <div class="whitespace-nowrap h-7">
                      修改人
                      <Colon />
                    </div>
                  </div>
                  <div class="ml-2 space-y-1" style="width: calc(100% - 68px);min-width:160px">
                    <div class="truncate h-7" :title="item?.scriptName">
                      <template v-if="item?.scriptName">
                        <RouterLink
                          :to="`/script/detail/${item?.scriptId}?type=view`"
                          class="text-text-link hover:text-text-link-hover cursor-pointer break-all">
                          {{ item?.scriptName }}
                        </RouterLink>
                      </template>
                      <template v-else>--</template>
                    </div>
                    <div class="flex items-center relative z-0 h-7" :class="item?.editpriority ? 'w-30' : ''">
                      <template v-if="!item?.editpriority">
                        {{ item?.priority || "--" }}
                        <template
                          v-if="!['CREATED', 'PENDING', 'RUNNING'].includes(item?.status?.value) && item?.hasOperationPermission">
                          <Icon
                            icon="icon-shuxie"
                            class="text-text-link ml-2 cursor-pointer -mt-0.25"
                            @click="updatePriority(item)" />
                        </template>
                      </template>
                      <template v-else>
                        <Input
                          :ref="el => { priorityInputRefs[item.id] = el }"
                          :value="item.priority"
                          :min="1"
                          :max="2147483647"
                          dataType="number"
                          size="small"
                          class="absolute top-0 h-5 z-9"
                          @blur="editPriority($event.target.value, item)" />
                      </template>
                    </div>
                    <div class="h-7 flex items-center">
                      <div class="truncate cursor-pointer" :title="item?.lastModifiedByName">
                        {{ item?.lastModifiedByName || "--" }}
                      </div>
                      <span class="ml-2 flex-none">{{ item?.lastModifiedDate || "--" }}修改</span>
                    </div>
                  </div>
                </div>
                <div class="flex ml-5 flex-1 ">
                  <div class="text-text-sub-content leading-7 space-y-1">
                    <div class="whitespace-nowrap h-7">
                      报告间隔
                      <Colon />
                    </div>
                    <div class="whitespace-nowrap h-7">
                      忽略断言
                      <Colon />
                    </div>
                    <div class="whitespace-nowrap h-7">
                      更新测试结果
                      <Colon />
                    </div>
                  </div>
                  <div class="ml-2 space-y-1 2xl:min-w-35">
                    <div
                      class="truncate flex items-center relative z-0 h-7"
                      :class="item?.editreportInterval ? 'w-25 2xl:w-30' : ''">
                      <template v-if="!item?.editreportInterval">
                        <template v-if="item?.reportInterval">
                          {{ splitTime(item.reportInterval)[0] + splitTime(item.reportInterval)[1] }}
                        </template>
                        <template v-else>
                          --
                        </template>
                        <template
                          v-if="!['CREATED', 'PENDING', 'RUNNING'].includes(item?.status?.value) && item?.hasOperationPermission">
                          <Icon
                            icon="icon-shuxie"
                            class="text-text-link ml-2 cursor-pointer -mt-0.25"
                            @click="updateReportInterval(item)" />
                        </template>
                      </template>
                      <template v-else>
                        <ShortDuration
                          :ref="el => { reportIntervalInputRefs[item.id] = el }"
                          :value="item.reportInterval"
                          :max="300"
                          size="small"
                          class="flex-1"
                          :selectProps="reportIntervalSelectProps"
                          @blur="editReportInterval($event, item)" />
                      </template>
                    </div>
                    <div
                      class="truncate flex items-center relative z-0 h-7"
                      :class="item?.editreportInterval ? 'w-25' : ''">
                      <Switch
                        :checked="!!item?.ignoreAssertions"
                        :disabled="['CREATED', 'PENDING', 'RUNNING'].includes(item?.status?.value) || !item?.hasOperationPermission"
                        size="small"
                        class="w-8"
                        @change="handleIgnoreAssertions($event, item)" />
                    </div>
                    <div
                      class="truncate flex items-center relative z-0 h-7"
                      :class="item?.editreportInterval ? 'w-25' : ''">
                      <Switch
                        :checked="item?.updateTestResult"
                        :disabled="['CREATED', 'PENDING', 'RUNNING'].includes(item?.status?.value) || !item?.hasOperationPermission || !item.canUpdateTestResult"
                        size="small"
                        class="w-8"
                        @change="handleUpdateTestResult($event, item)" />
                    </div>
                  </div>
                </div>
              </div>
            </div>
            <div class="w-15 flex-none flex flex-col items-center justify-center space-y-2">
              <template
                v-if="['CREATED', 'STOPPED', 'FAILED', 'COMPLETED', 'TIMEOUT'].includes(item?.status.value) && item?.hasOperationPermission">
                <a class="flex items-center" @click="handleRestart(item)">
                  <Icon icon="icon-huifu" class="mr-2" /><span>启动</span>
                </a>
              </template>
              <template v-else>
                <span class="flex items-center text-theme-disabled cursor-not-allowed">
                  <Icon icon="icon-huifu" class="mr-2" /><span>启动</span>
                </span>
              </template>
              <template v-if="['PENDING', 'RUNNING'].includes(item?.status.value) && item?.hasOperationPermission">
                <a class="flex items-center" @click="handleStop(item)">
                  <Icon icon="icon-jinyong" class="mr-2 transform -rotate-45" /><span>停止</span>
                </a>
              </template>
              <template v-else>
                <span class="flex items-center text-theme-disabled cursor-not-allowed">
                  <Icon icon="icon-jinyong" class="mr-2 transform -rotate-45" /><span>停止</span>
                </span>
              </template>
              <Dropdown
                v-if="item.scriptType?.value === 'MOCK_APIS'"
                :menuItems="dropdownMockMenuItems"
                :permissions="item.actionPermission"
                @click="dropdownClick($event.key, item)">
                <Icon icon="icon-gengduo" class="cursor-pointer outline-none items-center" />
              </Dropdown>
              <Dropdown
                v-else
                :menuItems="dropdownMenuItems"
                :permissions="item.actionPermission"
                @click="dropdownClick($event.key, item)">
                <Icon icon="icon-gengduo" class="cursor-pointer outline-none items-center" />
              </Dropdown>
            </div>
          </div>
        </div>
        <Pagination
          v-if="total > 5"
          :current="pagination.current"
          :pageSize="pagination.pageSize"
          :total="total"
          :hideOnSinglePage="false"
          :showTotal="showTotal"
          :showSizeChanger="false"
          size="middle"
          class="justify-end"
          @change="paginationChange" />
      </div>

      <div v-else-if="!loading">
        <NoData class="my-20" />
      </div>
    </template>
  </Spin>
</template>
<style scoped>
:deep(.ant-progress-steps-item) {
  width: 4px !important;
}
</style>
