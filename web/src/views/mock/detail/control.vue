<script setup lang="ts">
import { computed, defineAsyncComponent, onMounted, ref } from 'vue';
import { Icon, IconRefresh, Input, IntervalTimestamp, PureCard, Table } from '@xcan-angus/vue-ui';
import { Tooltip } from 'ant-design-vue';
import { duration, CTRL } from '@xcan-angus/tools';
import { debounce } from 'throttle-debounce';
import { useI18n } from 'vue-i18n';
import { analysis, mock as mockAltester } from '@/api/altester';

type FilterOp = 'EQUAL' | 'NOT_EQUAL' | 'GREATER_THAN' | 'GREATER_THAN_EQUAL' | 'LESS_THAN' | 'LESS_THAN_EQUAL' | 'CONTAIN' | 'NOT_CONTAIN' | 'MATCH_END' | 'MATCH' | 'IN' | 'NOT_IN'
type Filters = { key: string, value: string | boolean | string[], op: FilterOp };
type SearchParam = {
    pageNo: number;
    pageSize: number;
    filters?: Filters[];
    orderBy?: string;
    orderSort?: 'ASC' | 'DESC';
    [key: string]: any;
};

interface Props {
  id:string;
}

const props = withDefaults(defineProps<Props>(), {
  id: undefined
});

const CacheChart = defineAsyncComponent(() => import('@/components/chart/mock/cacheChart.vue'));
const RamChart = defineAsyncComponent(() => import('@/components/chart/mock/ramChart.vue'));
const CpuChart = defineAsyncComponent(() => import('@/components/chart/mock/cpuChart.vue'));

const { t } = useI18n();

const cacheChartData = ref<{
  xData: string[],
  numData: number[],
  totalData: number[],
  usedData: number[],
  maxTotalData:number;
  maxNumData:number;
}>({
  xData: [],
  numData: [],
  totalData: [],
  usedData: [],
  maxTotalData: 0,
  maxNumData: 0
});

const ramChartData = ref<{
  xData: string[],
  submitData: number[],
  totalData: number[],
  usedData: number[]
}>({
  xData: [],
  submitData: [],
  totalData: [],
  usedData: []
});

const cpuChartData = ref<{
  xData: string[],
  totalData: number[],
  systemCpuUsed: number[],
  processCpuUsed: number[]
}>({
  xData: [],
  totalData: [],
  systemCpuUsed: [],
  processCpuUsed: []
});

const getChartData = async (data, params, type) => {
  cacheChartData.value = {
    xData: [],
    numData: [0, 0],
    totalData: [0, 0],
    usedData: [0, 0],
    maxTotalData: 0,
    maxNumData: 0
  };

  ramChartData.value = {
    xData: [],
    submitData: [0, 0],
    totalData: [0, 0],
    usedData: [0, 0]
  };

  cpuChartData.value = {
    xData: [],
    totalData: [0, 0],
    systemCpuUsed: [0, 0],
    processCpuUsed: [0, 0]
  };

  if (!data.length) {
    const startTime = params.filters.filter(item => item.key === 'timestamp' && item.op === 'GREATER_THAN_EQUAL')[0].value;
    const endTime = params.filters.filter(item => item.key === 'timestamp' && item.op === 'LESS_THAN_EQUAL')[0].value;
    const _startTime = type === 'select' ? startTime.split(' ')[1] : startTime.replace(' ', '\n');
    const _endTime = type === 'select' ? endTime.split(' ')[1] : endTime.replace(' ', '\n');
    const _tiemData = [_startTime, _endTime];
    cacheChartData.value.xData = _tiemData;
    ramChartData.value.xData = _tiemData;
    cpuChartData.value.xData = _tiemData;
    return;
  }

  cacheChartData.value = {
    xData: [],
    numData: [],
    totalData: [],
    usedData: [],
    maxTotalData: 0,
    maxNumData: 0
  };

  ramChartData.value = {
    xData: [],
    submitData: [],
    totalData: [],
    usedData: []
  };

  cpuChartData.value = {
    xData: [],
    totalData: [],
    systemCpuUsed: [],
    processCpuUsed: []
  };

  for (let i = 0; i < data.length; i++) {
    const item = data[i];
    const _time = type === 'select' ? item.timestamp.split(' ')[1] : item.timestamp.replace(' ', '\n');
    const values = item.cvsJvm.split(',');

    cacheChartData.value.xData.push(_time);
    ramChartData.value.xData.push(_time);
    cpuChartData.value.xData.push(_time);

    const numValue = +values[0];
    cacheChartData.value.numData.push(numValue);
    if (numValue > cacheChartData.value.maxNumData) {
      cacheChartData.value.maxNumData = numValue;
    }

    const usedValue = parseFloat(values[1]) / 1024 / 1024 / 1024;
    cacheChartData.value.usedData.push(+usedValue.toFixed(2));

    const totalValue = parseFloat(values[2]) / 1024 / 1024 / 1024;
    cacheChartData.value.totalData.push(+totalValue.toFixed(2));
    if (totalValue > cacheChartData.value.maxTotalData) {
      cacheChartData.value.maxTotalData = totalValue;
    }

    cacheChartData.value.numData.push(numValue);
    if (numValue > cacheChartData.value.maxNumData) {
      cacheChartData.value.maxNumData = numValue;
    }

    const ramUsedData = parseFloat(values[3]) / 1024 / 1024 / 1024;
    ramChartData.value.usedData.push(+ramUsedData.toFixed(2));

    const ramSubmitData = parseFloat(values[4]) / 1024 / 1024 / 1024;
    ramChartData.value.submitData.push(+ramSubmitData.toFixed(2));

    const ramtotaltData = parseFloat(values[5]) / 1024 / 1024 / 1024;
    ramChartData.value.totalData.push(+ramtotaltData.toFixed(2));

    const cpu = item.cvsProcessor.split(',');
    cpuChartData.value.totalData.push(+cpu[0]);
    cpuChartData.value.systemCpuUsed.push(+cpu[1]);
    cpuChartData.value.processCpuUsed.push(+cpu[2]);
  }
};

const apisCount = ref({
  apisNum: '0',
  requestNum: '0',
  pushbackNum: '0',
  simulateErrorNum: '0',
  successNum: '0',
  exceptionNum: '0'
});

const getAnalysisMockService = async () => {
  const [error, { data }] = await analysis.getAnalysisMockService(props.id);
  if (error) { return; }
  apisCount.value = data;
};

const tableLoading = ref(false);

const tableParams = ref<SearchParam>({ pageNo: 1, pageSize: 10, mockServiceId: props.id, filters: [] });
const total = ref(0);

const handleChange = debounce(duration.search, (value) => {
  if (value) {
    tableParams.value.filters = [{ key: 'summary', op: 'MATCH_END', value: value }];
  } else {
    tableParams.value.filters = [];
  }
  getList();
});

const tableData = ref([]);
const getList = async () => {
  if (tableLoading.value) {
    return;
  }
  tableLoading.value = true;
  const [error, { data = { list: [], total: 0 } }] = await mockAltester.loadMockApisSearch(tableParams.value);
  tableLoading.value = false;
  if (error) {
    return;
  }
  tableData.value = data.list;
  total.value = +data.total;
};

const tableChange = (_pagination, _filters, sorter) => {
  const { current, pageSize } = _pagination;
  tableParams.value.pageNo = current;
  tableParams.value.pageSize = pageSize;
  tableParams.value.orderBy = sorter.orderBy;
  tableParams.value.orderSort = sorter.orderSort;
  getList();
};

const handleRefresh = () => {
  getList();
};

const pagination = computed(() => {
  return {
    current: tableParams.value.pageNo,
    pageSize: tableParams.value.pageSize,
    total: total.value
  };
});

onMounted(() => {
  getAnalysisMockService();
  getList();
});

const columns = [
  {
    title: t('接口名称'),
    dataIndex: 'summary',
    width: '15%',
    ellipsis: true
  },
  {
    title: t('接口信息'),
    dataIndex: 'method',
    width: '35%'
  },
  {
    title: t('请求数'),
    dataIndex: 'requestNum',
    customRender: ({ text }):string => text || '--',
    width: '10%'
  },
  {
    title: t('模拟异常数'),
    dataIndex: 'simulateErrorNum',
    width: '10%'
  },
  {
    title: t('回推数'),
    dataIndex: 'pushbackNum',
    width: '10%'
  },
  {
    title: t('成功数'),
    dataIndex: 'successNum',
    customRender: ({ text }):string => text || '--',
    width: '10%'
  },
  {
    title: t('异常数'),
    dataIndex: 'exceptionNum',
    width: '10%'
  }
];

const mockServiceCount = [
  {
    name: '接口数',
    key: 'apisNum',
    icon: 'icon-jiekoushu'
  },
  {
    name: '请求数',
    key: 'requestNum',
    icon: 'icon-qingqiushu'
  },
  {
    name: '模拟异常数',
    key: 'simulateErrorNum',
    icon: 'icon-moniyichangshu'
  },
  {
    name: '回推数',
    key: 'pushbackNum',
    icon: 'icon-huituishu'
  },
  {
    name: '成功数',
    key: 'successNum',
    icon: 'icon-chenggongshu1'
  },
  {
    name: '异常数',
    key: 'exceptionNum',
    icon: 'icon-yichangshu1'
  }
];

const countIconColor = {
  apisNum: 'text-status-pending',
  requestNum: 'text-status-process',
  pushbackNum: 'text-status-orange',
  simulateErrorNum: 'text-status-error1',
  successNum: 'text-status-success',
  exceptionNum: 'text-status-error'
};

const bgColor = {
  GET: 'text-http-get',
  POST: 'text-http-post',
  DELETE: 'text-http-delete',
  PATCH: 'text-http-patch',
  PUT: 'text-http-put',
  OPTIONS: 'text-http-options',
  HEAD: 'text-http-head',
  TRACE: 'text-http-trace'
};
</script>
<template>
  <PureCard class="py-3.5">
    <div class="flex justify-end pr-15">
      <IntervalTimestamp
        :action="`${CTRL}/mock/service/${props.id}/metrics`"
        @change="getChartData" />
    </div>
    <div class="flex h-60 space-x-2 mt-2">
      <CacheChart
        class="w-1/3"
        :xData="cacheChartData.xData"
        :numData="cacheChartData.numData"
        :totalData="cacheChartData.totalData"
        :usedData="cacheChartData.usedData"
        :maxNumData="cacheChartData.maxNumData"
        :maxTotalData="cacheChartData.maxTotalData" />
      <RamChart
        class="w-1/3"
        :xData="ramChartData.xData"
        :submitData="ramChartData.submitData"
        :totalData="ramChartData.totalData"
        :usedData="ramChartData.usedData" />
      <CpuChart
        class="w-1/3"
        :xData="cpuChartData.xData"
        :totalData="cpuChartData.totalData"
        :systemCpuUsed="cpuChartData.systemCpuUsed"
        :processCpuUsed="cpuChartData.processCpuUsed" />
    </div>
  </PureCard>
  <PureCard class="p-3.5 mt-2">
    <div class="flex text-3 space-x-3">
      <div
        v-for="(item,index) in mockServiceCount"
        :key="index"
        class="rounded p-2 flex-1 flex justify-around space-x-2 items-center"
        style="min-width: 140px;background-color: rgba(15, 159, 255, 10%);">
        <Icon
          :icon="item.icon"
          class="text-5"
          :class="countIconColor[item.key]" />
        <span class="text-text-sub-content">{{ item.name }}</span>
        <span class="text-text-title font-semibold">{{ apisCount[item.key] }}</span>
      </div>
    </div>
  </PureCard>
  <PureCard class="p-3.5 mt-2">
    <div class="flex items-center text-3 mb-2 justify-between">
      <Input
        allowClear
        class="w-60"
        placeholder="查询接口名称、路径"
        @change="handleChange($event.target.value)" />
      <IconRefresh
        :loading="tableLoading"
        class="text-4.5"
        @click="handleRefresh" />
    </div>
    <Table
      rowKey="id"
      :loading="tableLoading"
      :columns="columns"
      :dataSource="tableData"
      :pagination="pagination"
      @change="tableChange">
      <template #headerCell="{title, column}">
        <template v-if="column.dataIndex === 'requestNum'">
          {{ title }}
          <Tooltip
            title="调用Mock接口次数。"
            placement="top"
            arrowPointAtCenter
            :overlayStyle="{'max-width': '600px'}">
            <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1" />
          </Tooltip>
        </template>
        <template v-if="column.dataIndex === 'simulateErrorNum'">
          {{ title }}
          <Tooltip
            title="返回Http状态码为4xx和5xx响应次数。"
            placement="top"
            arrowPointAtCenter
            :overlayStyle="{'max-width': '600px'}">
            <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1" />
          </Tooltip>
        </template>
        <template v-if="column.dataIndex === 'pushbackNum'">
          {{ title }}
          <Tooltip
            title="Mock服务向用户配置回推接口推送请求次数。"
            placement="top"
            arrowPointAtCenter
            :overlayStyle="{'max-width': '600px'}">
            <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1" />
          </Tooltip>
        </template>
        <template v-if="column.dataIndex === 'successNum'">
          {{ title }}
          <Tooltip
            title="返回状态码为2xx请求次数。"
            placement="top"
            arrowPointAtCenter
            :overlayStyle="{'max-width': '600px'}">
            <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1" />
          </Tooltip>
        </template>
        <template v-if="column.dataIndex === 'exceptionNum'">
          {{ title }}
          <Tooltip
            title="Mock接口配置错误或程序自身导致的错误。"
            placement="top"
            arrowPointAtCenter
            :overlayStyle="{'max-width': '600px'}">
            <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1" />
          </Tooltip>
        </template>
      </template>
      <template #bodyCell="{text, record, column}">
        <template v-if="column.dataIndex === 'method'">
          <div class="flex">
            <div :class="bgColor[text.value]" class="leading-5 text-center flex-none">{{ text?.message || '--' }}</div>
            <div class="flex-1 truncate ml-3.5" :title="record.endpoint">{{ record.endpoint }}</div>
          </div>
        </template>
      </template>
    </Table>
  </PureCard>
</template>
