<script setup lang='ts'>
import { computed, defineAsyncComponent, h, inject, nextTick, onBeforeUnmount, onMounted, ref, Ref, watch } from 'vue';
import { LoadingOutlined } from '@ant-design/icons-vue';
import { useI18n } from 'vue-i18n';
import { Button, Spin as ASpin, Tooltip } from 'ant-design-vue';
import {
  AsyncComponent,
  AuthorizeModal,
  ButtonAuth,
  Colon,
  Dropdown,
  Icon,
  IconCopy,
  modal,
  notification,
  SearchPanel,
  Spin,
  Table
} from '@xcan-angus/vue-ui';
import { TESTER } from '@xcan-angus/infra';
import { useRoute } from 'vue-router';

import { MockServiceSource } from '@/enums/enums';
import ViewType from '@/views/mock/viewType/index.vue';
import { MockServiceObj, TableSelection } from './PropsType';
import { mock } from '@/api/tester';
import { getCurrentPage } from '@/utils/utils';
import router from '@/router';

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

const ExportMock = defineAsyncComponent(() => import('@/views/mock/export/index.vue'));

const { t } = useI18n();
const route = useRoute();
const appInfo = inject('appInfo') as Ref<Record<string, any>>;
const projectInfo = inject('projectInfo', ref({ id: '' }));
const projectId = computed(() => {
  return projectInfo?.value?.id;
});

const loading = ref(false);
const params = ref<SearchParam>({ pageNo: 1, pageSize: 10, filters: [] });
const total = ref(0);
const searchChange = (data:{ key: string; value: string; op: string; }[]) => {
  params.value.pageNo = 1;
  params.value.filters = data;
  getList();
};

const tableData = ref<MockServiceObj[]>([]);
const getList = async () => {
  if (loading.value) {
    return;
  }
  loading.value = true;
  const [error, { data = { list: [], total: 0 } }] = await mock.getServiceList({ ...params.value, projectId: projectId.value });
  loading.value = false;
  if (error) {
    return;
  }
  tableData.value = data.list.map(item => {
    let _currentAuthsValue:string[] = [];
    if (!item.auth) {
      if (item.currentAuths?.some(m => m.value === 'GRANT')) {
        _currentAuthsValue = ['EXPORT', 'GRANT'];
      } else {
        _currentAuthsValue = ['EXPORT'];
      }
      if (item.status?.value === 'NOT_STARTED') {
        _currentAuthsValue = [..._currentAuthsValue, 'DELETE'];
      }
    } else {
      _currentAuthsValue = item.currentAuths?.map(m => m.value) || [];
      if (item.status?.value !== 'NOT_STARTED') {
        _currentAuthsValue = _currentAuthsValue.filter(f => f !== 'DELETE');
      }
    }

    return {
      ...item,
      currentAuthsValue: _currentAuthsValue
    };
  });

  total.value = +data.total;
};

const tableChange = (_pagination, _filters, sorter) => {
  const { current, pageSize } = _pagination;
  params.value.pageNo = current;
  params.value.pageSize = pageSize;
  params.value.orderBy = sorter.orderBy;
  params.value.orderSort = sorter.orderSort;
  getList();
};

const pagination = computed(() => {
  return {
    current: params.value.pageNo,
    pageSize: params.value.pageSize,
    total: total.value
  };
});

const lastBatchType = ref<'del' | 'start' | undefined>();

const rouSelection = ref < TableSelection | null >(null);
const onSelectChange = (_selectedRowKeys) => {
  if (!rouSelection.value) {
    return;
  }
  rouSelection.value.selectedRowKeys = _selectedRowKeys;
};

let timersMap:{[key:string]:{count:number;timer?:NodeJS.Timeout }} = {};

const updateStatusById = (id) => {
  if (!timersMap[id]?.count) {
    timersMap[id] = { count: 0, timer: undefined };
  }

  timersMap[id].count++;
  timersMap[id].timer = setTimeout(async () => {
    await getListById([id]);
  }, 2000);
};

const getListById = async (id) => {
  const [error, { data = { list: [], total: 0 } }] = await mock.getServiceList({ filters: [{ key: 'id', value: [id], op: 'IN' }], projectId: projectId.value });
  if (error) {
    return;
  }

  const newData = data.list[0];
  if (newData.status.value === 'RUNNING' || timersMap[id].count === 16) {
    clearTimeout(timersMap[newData.id].timer);
    delete timersMap[newData.id];
    setData(newData);
    if (newData.status.value === 'RUNNING') {
      notification.success(t('mock.startSuccess'));
    } else {
      notification.warning(t('mock.startFail'));
    }
    return;
  }

  updateStatusById(id);
};

const setData = (newData:MockServiceObj) => {
  for (let i = 0; i < tableData.value.length; i++) {
    const item = tableData.value[i];
    let _currentAuthsValue:string[] = [];
    if (item.id === newData.id) {
      if (!newData.auth) {
        if (newData.currentAuths?.some(m => m.value === 'GRANT')) {
          _currentAuthsValue = ['EXPORT', 'GRANT'];
        } else {
          _currentAuthsValue = ['EXPORT'];
        }
      } else {
        _currentAuthsValue = newData.currentAuths?.map(m => m.value).filter(f => f !== 'DELETE') || [];
      }

      if (newData.status?.value === 'NOT_STARTED') {
        _currentAuthsValue = [..._currentAuthsValue, 'DELETE'];
      }

      tableData.value[i] = newData;
      tableData.value[i].currentAuthsValue = _currentAuthsValue;
      break;
    }
  }
};

// 批量启动
const batchStart = async () => {
  if (!rouSelection.value) {
    rouSelection.value = {
      onChange: onSelectChange,
      getCheckboxProps: (record: MockServiceObj) => ({
        disabled: (record.auth && !record.currentAuthsValue.includes('RUN')) || record.status?.value !== 'NOT_STARTED'
      })
    };
    lastBatchType.value = 'start';
  } else {
    if (lastBatchType.value === 'start') {
      if (!rouSelection.value.selectedRowKeys?.length) {
        rouSelection.value = null;
        return;
      }
    } else {
      rouSelection.value = {
        onChange: onSelectChange,
        getCheckboxProps: (record: MockServiceObj) => ({
          disabled: (record.auth && !record.currentAuthsValue.includes('RUN')) || record.status?.value !== 'NOT_STARTED'
        })
      };
      lastBatchType.value = 'start';
      rouSelection.value.selectedRowKeys = [];
    }
  }

  const _selectKey = rouSelection.value.selectedRowKeys;

  if (loading.value || !_selectKey?.length) {
    return;
  }

  loading.value = true;
  const [error, { data }] = await mock.startService(_selectKey);
  loading.value = false;
  if (error) {
    // TODO 批量失败了要不要给每条数据绑上同一个失败信息
    return;
  }
  const failList = data.filter(item => !item.success);
  if (failList.length) {
    const _failIds = [];
    for (let i = 0; i < failList.length; i++) {
      const failItem = failList[i];
      _failIds.push(failItem.serviceId);
      const matchingItem = tableData.value.find(dataItem => dataItem.id === failItem.serviceId);
      if (matchingItem) {
        matchingItem.failTips = failItem;
      }
    }

    if (failList.length !== data.length) {
      rouSelection.value.selectedRowKeys = rouSelection.value.selectedRowKeys.filter(f => !_failIds.includes(f));
    } else {
      notification.success(t('mock.batchStartFail'));
      return;
    }
  }

  notification.success(t('mock.starting'));
  for (let i = 0; i < tableData.value.length; i++) {
    const _data = tableData.value[i];
    if (rouSelection.value.selectedRowKeys?.includes(_data.id)) {
      _data.status = { value: 'STARTING', message: t('mock.startPending') };
      _data.currentAuthsValue = ['VIEW'];
    }
  }
  const _ids = JSON.parse(JSON.stringify(rouSelection.value.selectedRowKeys));
  rouSelection.value.selectedRowKeys = [];
  rouSelection.value = null;
  updateStatusByIds(_ids);
};

const updateStatusByIds = (ids) => {
  const key = ids.join('');
  if (!timersMap[key]?.count) {
    timersMap[key] = { count: 0, timer: undefined };
  }

  timersMap[key].count++;
  timersMap[key].timer = setTimeout(async () => {
    await getListByIds(ids);
  }, 2000);
};

const getListByIds = async (ids:string[]) => {
  const [error, { data = { list: [], total: 0 } }] = await mock.getServiceList({ filters: [{ key: 'id', value: ids, op: 'IN' }], projectId: projectId.value });
  if (error) {
    return;
  }

  let _ids = JSON.parse(JSON.stringify(ids));
  const newDataList = data.list;
  for (let i = 0; i < newDataList.length; i++) {
    const newData = newDataList[i];
    setData(newData);
    if (newData.status.value === 'RUNNING') {
      notification.success(t('mock.serviceStartSuccess'));
      _ids = _ids.filter(id => id !== newData.id);
      const key = ids.join('');
      if (timersMap[key]) {
        clearTimeout(timersMap[key].timer);
        delete timersMap[key];
      }
    }
  }

  if (_ids.length) {
    const key = ids.join('');
    if (_ids.length === ids.length && timersMap[key] && timersMap[key].count === 16) {
      clearTimeout(timersMap[key].timer);
      delete timersMap[key];
      for (let i = 0; i < newDataList.length; i++) {
        const newData = newDataList[i];
        setData(newData);
      }
      notification.warning(t('mock.startFail'));
    } else {
      updateStatusByIds(_ids);
    }
  }
};

const handleDelete = async (ids:string[]) => {
  modal.confirm({
    centered: true,
    content: t('mock.deleteTip'),
    async onOk () {
      loading.value = true;
      const [error] = await mock.deleteService(ids);
      loading.value = false;
      if (error) {
        return;
      }
      notification.success(t('mock.deleteSuccess'));
      params.value.pageNo = getCurrentPage(params.value.pageNo as number, params.value.pageSize as number, total.value);
      getList();
    }
  });
};

const forceDelete = (record: MockServiceObj) => {
  modal.confirm({
    centered: true,
    content: t('mock.deleteTip'),
    async onOk () {
      loading.value = true;
      const [error] = await mock.deleteServiceByForce([record.id]);
      loading.value = false;
      if (error) {
        return;
      }
      notification.success(t('mock.deleteSuccess'));
      params.value.pageNo = getCurrentPage(params.value.pageNo as number, params.value.pageSize as number, total.value);
      getList();
    }
  });
};

// 批量删除
const batchDelete = async () => {
  if (!rouSelection.value) {
    rouSelection.value = {
      onChange: onSelectChange,
      getCheckboxProps: (record: MockServiceObj) => ({
        disabled: (record.auth && !record.currentAuthsValue.includes('DELETE')) || record.status?.value !== 'NOT_STARTED'
      })
    };
    lastBatchType.value = 'del';
  } else {
    if (lastBatchType.value === 'del') {
      if (!rouSelection.value.selectedRowKeys?.length) {
        rouSelection.value = null;
        return;
      }
    } else {
      rouSelection.value = {
        onChange: onSelectChange,
        getCheckboxProps: (record: MockServiceObj) => ({
          disabled: (record.auth && !record.currentAuthsValue.includes('DELETE')) || record.status?.value !== 'NOT_STARTED'
        })
      };
      lastBatchType.value = 'del';
      rouSelection.value.selectedRowKeys = [];
    }
  }

  const _selectKey = rouSelection.value.selectedRowKeys;

  if (loading.value || !_selectKey?.length) {
    return;
  }

  modal.confirm({
    centered: true,
    content: t('mock.deleteTip'),
    async onOk () {
      loading.value = true;
      const [error] = await mock.deleteService(_selectKey);
      loading.value = false;
      if (error) {
        return;
      }
      notification.success(t('mock.batchDelSuccess'));
      params.value.pageNo = getCurrentPage(params.value.pageNo as number, params.value.pageSize as number, total.value);
      getList();

      if (!rouSelection.value) {
        return;
      }
      rouSelection.value.selectedRowKeys = [];
      rouSelection.value = null;
      lastBatchType.value = 'del';
    }
  });
};

const handleRefresh = () => {
  getList();
};

// 单条启动停止
const handleUpdateStatus = async (item:MockServiceObj) => {
  if (loading.value) {
    return;
  }
  loading.value = true;
  const [error, { data }] = item.status?.value === 'RUNNING' ? await mock.stopService([item.id]) : await mock.startService([item.id]);
  loading.value = false;
  if (error) {
    item.failTips = { exitCode: error.code, message: error.msg };
    return;
  }
  if (!data[0].success) {
    item.failTips = data[0];
    item.status?.value === 'RUNNING' ? notification.success(t('mock.stop_fail')) : notification.success(t('mock.start_fail'));
    return;
  }

  if (item.status?.value === 'RUNNING') {
    notification.success(t('mock.stop_success'));
    getStopResById(item.id);
  } else {
    notification.warning(t('mock.in_starting'));
    if (item.status?.value === 'NOT_STARTED') {
      item.status = { value: 'STARTING', message: t('mock.startPending') };
      item.currentAuthsValue = ['VIEW'];
    }
    updateStatusById(item.id);
  }
};

const getStopResById = async (id) => {
  const [error, { data = { list: [], total: 0 } }] = await mock.getServiceList({ filters: [{ key: 'id', value: id, op: 'EQUAL' }], projectId: projectId.value });
  if (error) {
    return;
  }

  const newData = data.list[0];

  setData(newData);
};

const exportVisible = ref(false);
const exprotData = ref<MockServiceObj>();
const handleExport = async (item?:MockServiceObj) => {
  exprotData.value = item;
  exportVisible.value = true;
};

const authVisible = ref(false);
const authData = ref<MockServiceObj>();
const openAuth = async (item?:MockServiceObj) => {
  authData.value = item;
  authVisible.value = true;
};

const authFlagChange = ({ auth }:{auth:boolean}) => {
  const data = tableData.value;
  const targetId = authData.value?.id;
  for (let i = 0, len = data.length; i < len; i++) {
    if (data[i].id === targetId) {
      data[i].auth = auth;
      break;
    }
  }
};

// 刷新实例
const handleResetInstance = async (item:MockServiceObj) => {
  if (loading.value) {
    return;
  }
  modal.confirm({
    centered: true,
    content: t('mock.refreshInstance_tip'),
    async onOk () {
      loading.value = true;
      const [error] = await mock.syncServiceInstanceConfig(item.id);
      loading.value = false;
      if (error) {
        return;
      }
      notification.success(t('mock.refreshInstance_success'));
    }
  });
};

const searchPanelRef = ref();
onMounted(() => {
  watch(() => projectId.value, newValue => {
    if (newValue) {
      debugger;
      if (route.query.sid) {
        nextTick(() => {
          searchPanelRef.value.setConfigs([{ valueKey: 'name', value: route.query.sid }]);
        });
        router.replace('/apis#mock');
      } else {
        getList();
      }
    }
  }, {
    immediate: true
  });
});

const searchoptions = [
  {
    valueKey: 'name',
    type: 'input',
    placeholder: t('mock.searchPanel.namePlaceholder'),
    allowClear: true,
    maxlength: 100
  },
  {
    valueKey: 'nodeIp',
    type: 'input',
    op: 'EQUAL',
    placeholder: t('mock.searchPanel.nodeIpPlaceholder'),
    allowClear: true,
    maxlength: 100
  },
  {
    valueKey: 'source',
    type: 'select-enum',
    enumKey: MockServiceSource,
    placeholder: t('mock.searchPanel.sourcePlaceholder'),
    allowClear: true
  },
  {
    valueKey: 'nodeId',
    type: 'select',
    action: `${TESTER}/node?fullTextSearch=true`,
    maxlength: 100,
    fieldNames: { label: 'name', value: 'id' },
    showSearch: true,
    placeholder: t('mock.searchPanel.nodeIdPlaceholder'),
    allowClear: true
  },
  {
    valueKey: 'createdBy',
    type: 'select-user',
    placeholder: t('mock.searchPanel.createdByPlaceholder'),
    maxlength: 100
  },
  {
    valueKey: 'createdDate',
    type: 'date-range',
    placeholder: [t('mock.searchPanel.createdDatePlaceholder1'), t('mock.searchPanel.createdDatePlaceholder2')],
    allowClear: true,
    showTime: true
  }
];

const columns = [
  {
    title: t('mock.columns.id'),
    dataIndex: 'id',
    width: '10%',
    customCell: () => {
      return { style: 'white-space:nowrap;' };
    }
  },
  {
    title: t('mock.columns.name'),
    dataIndex: 'name',
    width: '13%'
  },
  {
    title: t('mock.columns.node'),
    dataIndex: 'nodeName',
    customRender: ({ text }):string => text || '--',
    width: '10%'
  },
  {
    title: t('mock.columns.address'),
    dataIndex: 'serviceHostUrl',
    customRender: ({ text }):string => text || '--'
  },
  {
    title: t('mock.columns.status'),
    dataIndex: 'status',
    width: '7%',
    customCell: () => {
      return { style: 'white-space:nowrap;' };
    }
  },
  {
    title: t('mock.columns.source'),
    dataIndex: 'source',
    width: '7%',
    customCell: () => {
      return { style: 'white-space:nowrap;' };
    }
  },
  {
    title: t('mock.columns.createdBy'),
    dataIndex: 'createdByName',
    width: '8%',
    groupName: 'createdByName',
    customRender: ({ text }) => text || '--',
    ellipsis: true
  },
  {
    title: t('mock.columns.lastModifiedBy'),
    dataIndex: 'lastModifiedByName',
    width: '8%',
    groupName: 'createdByName',
    hide: true,
    customRender: ({ text }) => text || '--',
    ellipsis: true
  },
  {
    title: t('mock.columns.createdDate'),
    dataIndex: 'createdDate',
    width: '10%',
    sorter: true,
    groupName: 'createdDate',
    customCell: () => {
      return { style: 'white-space:nowrap;' };
    }
  },
  {
    title: t('mock.columns.lastModifiedDate'),
    dataIndex: 'lastModifiedDate',
    width: '10%',
    sorter: true,
    groupName: 'createdDate',
    hide: true,
    customCell: () => {
      return { style: 'white-space:nowrap;' };
    }
  },
  {
    title: t('mock.columns.action'),
    dataIndex: 'action',
    width: 140
  }
];

const menus = [
  {
    key: 'del',
    icon: 'icon-qingchu',
    name: t('mock.actions.delete'),
    permission: 'DELETE',
    disabled: true
  },
  {
    key: 'auth',
    icon: 'icon-quanxian1',
    name: t('mock.actions.authority'),
    permission: 'GRANT'
  },
  {
    key: 'export',
    icon: 'icon-daochu',
    name: t('actions.export'),
    permission: 'EXPORT'
  },
  {
    key: 'reset',
    icon: 'icon-shuaxin',
    name: t('mock.actions.refresh'),
    permission: 'MODIFY'
  }
];

const handleClick = (key:string, record:MockServiceObj) => {
  switch (key) {
    case 'del':
      forceDelete(record);
      break;
    case 'auth':
      openAuth(record);
      break;
    case 'export':
      handleExport(record);
      break;
    case 'reset':
      handleResetInstance(record);
      break;
  }
};

const indicator = h(LoadingOutlined, {
  style: {
    fontSize: '16px'
  },
  spin: true
});

onBeforeUnmount(() => {
  const keys = Object.keys(timersMap);
  if (!keys.length) {
    return;
  }

  for (let i = 0; i < keys.length; i++) {
    const item = keys[i];
    if (timersMap[item]?.timer) {
      clearTimeout(timersMap[item].timer);
    }
  }

  timersMap = {};
});

const statusStyleMap = {
  NOT_STARTED: '#B7BBC2',
  RUNNING: '#52c41a',
  STOPPED: '#abd3ff',
  STARTING: '#FF8100'
};
</script>
<template>
  <Spin :spinning="loading" class="w-full h-full py-5 px-5 overflow-y-auto flex flex-col">
    <ViewType class="mb-5" />
    <div class="flex items-start justify-between mb-3.5">
      <SearchPanel
        ref="searchPanelRef"
        :options="searchoptions"
        :width="284"
        class="flex-1 mr-2"
        @change="searchChange" />
      <div class="space-x-2.5">
        <Button
          type="primary"
          size="small"
          href="/mockservice/add"
          class="inline-flex space-x-1">
          <Icon icon="icon-jia" class="text-3.5" />
          {{t('mock.actions.addMock')}}
        </Button>
        <!-- <ButtonAuth
          code="MockServiceAdd"
          type="primary"
          href="/mockservice/add"
          iconStyle="font-size:14px;"
          icon="icon-jia" /> -->
        <Button
          size="small"
          @click="batchStart ">
          <Icon icon="icon-qidong" class="mr-1 text-3.5" />
          <span>{{t('mock.actions.start')}}</span>
        </Button>
        <Button
          size="small"
          @click="batchDelete">
          <Icon icon="icon-qingchu" class="mr-1 text-3.5" />
          <span>{{t('actions.delete')}}</span>
        </Button>
        <ButtonAuth
          code="MockServiceExport"
          icon="icon-daochu1"
          iconStyle="font-size:14px;"
          @click="handleExport(undefined)" />
        <Button
          size="small"
          :disabled="loading"
          @click="handleRefresh">
          <Icon icon="icon-shuaxin" class="text-3.5 mr-1" />
          <span>{{t('actions.refresh')}}</span>
        </Button>
      </div>
    </div>
    <Table
      rowKey="id"
      :columns="columns"
      :dataSource="tableData"
      :rowSelection="rouSelection"
      :pagination="pagination"
      @change="tableChange">
      <template #bodyCell="{ column,text, record }">
        <template v-if="column.dataIndex === 'name'">
          <template v-if="(!record.auth || record.currentAuthsValue.includes('VIEW')) ">
            <RouterLink
              :to="`/mockservice/${record.id}/apis`"
              class="text-text-link hover:text-text-link-hover cursor-pointer break-all">
              {{ text }}
            </RouterLink>
          </template>
          <template v-else>
            {{ text }}
          </template>
        </template>
        <template v-if="column.dataIndex === 'nodeName'">
          <RouterLink
            :to="`/node/detail/${record.nodeId}`"
            class="text-text-link hover:text-text-link-hover cursor-pointer break-all">
            {{ text }}
          </RouterLink>
        </template>
        <template v-if="column.dataIndex === 'serviceHostUrl'">
          <div>
            <div class="flex items-start">
              <span>{{ text }}</span>
              <span title="复制"><IconCopy class="ml-2 -mt-0.5 text-3.5" :copyText="text" /></span>
            </div>
            <div v-if="text && record?.serviceDomainUrl">
              <span>{{ record?.serviceDomainUrl }}</span>
              <span title="复制"><IconCopy class="ml-2 -mt-0.5 text-3.5" :copyText="record?.serviceDomainUrl" /></span>
            </div>
          </div>
        </template>
        <template v-if="column.dataIndex === 'status'">
          <div class="flex items-center">
            <div class="flex items-center">
              <div class="w-1.5 h-1.5 rounded-xl mr-1" :style="'background-color:' + statusStyleMap[text.value]"></div>
              <span>{{ text?.message || "--" }}</span>
            </div>
            <tmeplate v-if="text.value === 'STARTING'">
              <ASpin :indicator="indicator" class="mx-1" />
            </tmeplate>
            <template v-if="record?.failTips">
              <Tooltip
                placement="topLeft"
                arrowPointAtCenter
                :overlayStyle="{'max-width': '400px'}">
                <template #title>
                  <div class="flex items-center text-text-content">
                    <span>{{ record?.failTips.message }}<span v-if="record.failTips?.exitCode !== null || record.failTips?.exitCode !== ''" class="ml-2">(退出码<Colon class="mr-1" />{{ record.failTips.exitCode }})</span></span>
                  </div>
                  <div v-if="record.failTips?.console?.length">
                    <div class="mt-2">控制台<Colon /></div>
                    <div
                      v-for="(item,index) in record.failTips.console"
                      :key="index"
                      class="leading-4 mt-1">
                      {{ item }}
                    </div>
                  </div>
                </template>
                <Icon icon="icon-tishi1" class="ml-1 text-3.5 text-status-error1 cursor-pointer" />
              </Tooltip>
            </template>
          </div>
        </template>
        <template v-if="column.dataIndex === 'source'">
          {{ record.source?.message }}
        </template>
        <template v-if="column.dataIndex === 'action'">
          <div class="flex items-center">
            <a
              v-if="(!record.auth || record.currentAuthsValue.includes('RUN')) && record.status?.value !== 'STARTING'"
              class="ml-2 cursor-pointer flex items-center text-3"
              @click="handleUpdateStatus(record)">
              <Icon :icon="record.status?.value !== 'RUNNING'?'icon-qidong':'icon-zhongzhi2'" class="mr-1" />{{ record.status?.value !== 'RUNNING'? t('mock.actions.start') :t('mock.actions.stop')  }}
            </a>
            <a
              v-else
              class="ml-2 flex items-center text-3"
              disabled>
              <Icon :icon="record.status?.value !== 'RUNNING'?'icon-qidong':'icon-zhongzhi2'" class="mr-1" />{{ record.status?.value !== 'RUNNING'? t('mock.actions.start') :t('mock.actions.stop') }}
            </a>
            <a
              v-if="(!record.auth || record.currentAuthsValue.includes('DELETE')) && record.status?.value === 'NOT_STARTED' "
              class="mx-2 cursor-pointer flex items-center"
              @click="handleDelete([record.id])">
              <Icon icon="icon-qingchu" class="mr-1 text-3.5" />删除
            </a>
            <a
              v-else
              class="mx-2 flex items-center"
              disabled>
              <Icon icon="icon-qingchu" class="mr-1 text-3.5" />删除
            </a>
            <Dropdown
              :admin="false"
              :menuItems="menus"
              :permissions="record.currentAuthsValue"
              @click="handleClick($event.key, record)">
              <Icon icon="icon-gengduo" class="cursor-pointer outline-none items-center" />
            </Dropdown>
          </div>
        </template>
      </template>
    </Table>
  </Spin>
  <AsyncComponent :visible="exportVisible">
    <ExportMock
      v-model:visible="exportVisible"
      :mockService="exprotData"
      type="APIS" />
  </AsyncComponent>
  <AsyncComponent :visible="authVisible">
    <AuthorizeModal
      v-model:visible="authVisible"
      enumKey="MockServicePermission"
      :appId="appInfo?.id"
      :listUrl="`${TESTER}/mock/service/${authData?.id}/auth`"
      :delUrl="`${TESTER}/mock/service/auth`"
      :addUrl="`${TESTER}/mock/service/${authData?.id}/auth`"
      :updateUrl="`${TESTER}/mock/service/auth`"
      :enabledUrl="`${TESTER}/mock/service/${authData?.id}/auth/enabled`"
      :initStatusUrl="`${TESTER}/mock/service/${authData?.id}/auth/status`"
      :onTips="t('mock.auth.onTip')"
      :offTips="t('mock.auth.offTips')"
      :title="t('mock.auth.title')"
      @change="authFlagChange" />
  </AsyncComponent>
</template>
