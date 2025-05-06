<script setup lang="ts">
import { Badge, CheckboxGroup, Divider, Popover } from 'ant-design-vue';
import { computed, defineAsyncComponent, onMounted, reactive, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { AsyncComponent, Hints, Icon, IconRefresh, SearchPanel, Table } from '@xcan-angus/vue-ui';
import DOMPurify from 'dompurify';
import { enumLoader } from '@xcan-angus/tools';

import { _configColumns, _recordColumns, PushRecord, PushSetting } from './interface';
import { event } from '@/api/event';
import { analysis } from '@/api/aas';
import { setting } from '@/api/comm';

const InfoCard = defineAsyncComponent(() => import('./infoCard.vue'));
const ExpandHead = defineAsyncComponent(() => import('./expand-head.vue'));
const ReceiveConfig = defineAsyncComponent(() => import('./receiveConfig.vue'));
const Receiver = defineAsyncComponent(() => import('./receiver.vue'));

const configLoading = ref(false);
const recordLoading = ref(false);
const showRecord = ref(true);
const showConfigure = ref(true);
const receiverVisible = ref(false);
const configureVisible = ref(false);

const params: { pageNo: number, pageSize: number } = reactive({ pageNo: 1, pageSize: 10 });
const searchParams = ref<{key: string, value: any, op: string}[]>([]);
const state: {
  count: {
    pushFail: string,
    pushSuccess: string,
    ignore: string,
    total: string
  },
  selectedItem:PushSetting
} = reactive({
  count: {
    pushFail: '0',
    pushSuccess: '0',
    ignore: '0',
    total: '0'
  },
  selectedItem: {} as PushSetting
});

const pushSettingList = ref<PushSetting[]>([]);
const pushRecordList = ref<PushRecord[]>([]);
const pageTotal = ref(0);

const targetTypeEnums = ref<{value: string; message: string}[]>([]);
const NoticeType = ref<{value: string; message: string}[]>([]);
const eventDataNoticeType = ref<{eventCode: string; noticeTypes: {value: string; message: string}[]}[]>([]);

const { t } = useI18n();

const init = async () => {
  await loadEnums();
  await loadEventNoticeTypeByEventCode();
  loadPushConfigList();
  loadStatistics();
  loadPushRecordList();
};

const loadEnums = async () => {
  const [_err1, data1] = await enumLoader.load('CombinedTargetType');
  const [_err2, data2] = await enumLoader.load('NoticeType');
  targetTypeEnums.value = data1 || [];
  NoticeType.value = (data2 || []).map(i => {
    return {
      ...i,
      label: i.message
    };
  });
};

const loadEventNoticeTypeByEventCode = async () => {
  const [error, { data }] = await event.getEventNoticeType();
  if (error) {
    return;
  }
  eventDataNoticeType.value = data || [];
};

const getTargetTypeName = (value: string) => {
  const target = targetTypeEnums.value.find(i => i.value === value);
  return target?.message || '  ';
};

const loadStatistics = async () => {
  // const [error, { data = { pushFail: '0', pushSuccess: '0', pushing: '0', unPush: '0' } }] = await event.loadStatistics();
  const [error, { data = { push_status: {} } }] = await analysis.loadCustomizationSummary({
    aggregates: [
      {
        column: 'id',
        function: 'COUNT'
      }
    ],
    groupBy: 'STATUS',
    groupByColumns: ['type', 'push_status'],
    name: 'Event',
    appCode: 'AngusTester'
  });
  if (error) {
    return;
  }
  const { PUSH_SUCCESS = { COUNT_id: 0, TOTAL_COUNT_id: 0 }, PUSH_FAIL = { COUNT_id: 0, TOTAL_COUNT_id: 0 }, IGNORED = { COUNT_id: 0, TOTAL_COUNT_id: 0 } } = data.push_status;
  state.count.pushSuccess = PUSH_SUCCESS.COUNT_id;
  state.count.ignore = IGNORED.COUNT_id;
  state.count.pushFail = PUSH_FAIL.COUNT_id;
  state.count.total = (+PUSH_SUCCESS.TOTAL_COUNT_id || +PUSH_FAIL.TOTAL_COUNT_id || +IGNORED.TOTAL_COUNT_id || 0) + '';
};

const loadPushConfigList = async () => {
  configLoading.value = true;
  const [error, res] = await event.loadPushConfigList({
    appCode: 'AngusTester',
    pageSize: 2000
  });
  configLoading.value = false;
  if (error) {
    return;
  }

  pushSettingList.value = res.data.list?.map(item => {
    const noticeTypesObj = eventDataNoticeType.value.find(i => i.eventCode === item.eventCode)?.noticeTypes || [];
    return {
      ...item,
      pushMsg: DOMPurify.sanitize(item.pushMsg),
      noticeTypes: noticeTypesObj.map(i => i.value)
    };
  }) || [];
};

const loadPushRecordList = async () => {
  if (recordLoading.value) {
    return;
  }
  recordLoading.value = true;
  const [error, { data = { list: [], total: 0 } }] = await event.loadPushRecordList({ ...params, filters: searchParams.value, appCode: 'AngusTester' });
  recordLoading.value = false;
  if (error) {
    return;
  }

  pushRecordList.value = data.list;
  pageTotal.value = +data.total;
};

// 接收通道
const openReceiveChannel = (record:PushSetting) => {
  state.selectedItem = record;
  configureVisible.value = true;
};

// 接收人
const openReceiver = (record:PushSetting) => {
  state.selectedItem = record;
  receiverVisible.value = true;
};

// 查询条件变更
const changeLogParams = (value) => {
  searchParams.value = value;
  params.pageNo = 1;
  loadPushRecordList();
};

const configcolumns = computed(() => {
  return _configColumns.map((item) => {
    return {
      ...item,
      title: t(item.title)
    };
  });
});

const recordColumns = computed(() => {
  return _recordColumns.map((item) => {
    return {
      ...item,
      title: t(item.title)
    };
  });
});

const pagination = computed(() => {
  return {
    current: params.pageNo,
    pageSize: params.pageSize,
    total: pageTotal.value
  };
});

const changePagination = (_pagination) => {
  const { current, pageSize } = _pagination;
  params.pageNo = current;
  params.pageSize = pageSize;
  loadPushRecordList();
};

const handleChangeNoticeType = async (typesValue, eventCode) => {
  const params = pushSettingList.value.map(i => {
    return {
      eventCode: i.eventCode,
      noticeTypes: i.eventCode === eventCode ? typesValue : i.noticeTypes
    };
  });
  const [error] = await setting.putEventNotice(params);

  if (error) {
    return;
  }

  pushSettingList.value.forEach(item => {
    if (item.eventCode === eventCode) {
      item.noticeTypes = typesValue;
    }
  });
};

const getReceiver = (receiveSetting) => {
  const receiverTypes: string[] = receiveSetting?.receivers?.receiverTypes?.map(m => m.message) || [];
  const otherUser: string[] = receiveSetting?.receivers?.receivers?.map(m => m.fullName) || [];
  return [...otherUser, ...receiverTypes].join('、');
};

const searchLogOpt = [
  {
    valueKey: 'code',
    type: 'input',
    placeholder: '查询事件描述或编码',
    allowClear: true
  },
  {
    valueKey: 'pushStatus',
    type: 'select-enum',
    enumKey: 'EventPushStatus',
    placeholder: '选择推送状态',
    allowClear: true
  },
  {
    type: 'date-range',
    valueKey: 'createdDate',
    placeholder: ['开始时间', '结束时间']
  }
];

const obj:{
  [key:string]:string
} = {
  UN_PUSH: 'default',
  PUSHING: 'processing',
  PUSH_SUCCESS: 'success',
  PUSH_FAIL: 'error'
};

const getStatus = (key:string):string => {
  return obj[key];
};

onMounted(() => {
  init();
});

</script>

<template>
  <div class="py-3.5 px-5">
    <Hints text="AngusTester 事件配置，为用户提供了即时的信息更新和状态反馈，帮助团队快速响应和处理潜在的问题。" class="!leading-4.5" />
    <div class="flex space-x-2 w-250 mt-2">
      <InfoCard
        :name="t('总共')"
        :value="state.count.total"
        icon="icon-zonglan" />
      <InfoCard
        :name="t('settingNotification.title.t2')"
        :value="state.count.pushSuccess"
        iconColor="rgba(255, 129, 0, 1)"
        icon="icon-tuisongchenggong" />
      <InfoCard
        :name="t('settingNotification.title.t3')"
        :value="state.count.pushFail"
        icon="icon-tuisongshibai" />
      <InfoCard
        :name="t('忽略')"
        :value="state.count.ignore"
        icon="icon-yiquxiao" />
    </div>
    <expand-head v-model:visible="showConfigure" class="mt-5">
      <template #title>
        <div>
          <span class="text-theme-title text-3.5 leading-3.5 font-medium">{{ t('settingNotification.title.t4') }}</span>
          <Popover placement="right">
            <template #content>
              <div class="max-w-110">
                配置通知方式可以确保相关人员在事件发生时及时收到信息，提高反应速度。
              </div>
            </template>
            <Icon icon="icon-tishi1" class="text-tips text-3.5 ml-2" />
          </Popover>
        </div>
      </template>
    </expand-head>
    <div :class="showConfigure ? 'open-info' : 'stop-info'" class="transition-height duration-500 overflow-hidden">
      <Table
        rowKey="id"
        size="small"
        class="my-3.5"
        :dataSource="pushSettingList"
        :columns="configcolumns"
        :scroll="{y:pushSettingList.length>10?'180px':'220px'}"
        :loading="configLoading"
        :pagination="pushSettingList?.length > 10">
        <template #bodyCell="{ column, text, record }">
          <template v-if="column.dataIndex === 'allowedChannelTypes'">
            {{ text?.map(m=>m.message).join('、') }}
          </template>
          <template v-if="column.dataIndex === 'receiveSetting'">
            {{ getReceiver(text) }}
          </template>
          <template v-if="column.dataIndex === 'targetType'">
            {{ getTargetTypeName(record.targetType) }}
          </template>
          <template v-if="column.dataIndex === 'noticeType'">
            <CheckboxGroup
              :value="record.noticeTypes"
              :options="NoticeType"
              @change="handleChangeNoticeType($event, record.eventCode)" />
          </template>
          <template v-if="column.dataIndex === 'operate'">
            <a class="text-theme-text-hover" @click="openReceiveChannel(record)">{{
              t('settingNotification.title.t6')
            }}</a>
            <Divider type="vertical" />
            <a class="text-theme-text-hover" @click="openReceiver(record)">{{
              t('settingNotification.title.t7')
            }}</a>
          </template>
        </template>
      </Table>
      <AsyncComponent :visible="configureVisible">
        <ReceiveConfig
          v-if="configureVisible"
          :id="state.selectedItem.id"
          v-model:visible="configureVisible" />
      </AsyncComponent>
      <AsyncComponent :visible="receiverVisible">
        <Receiver
          v-if="receiverVisible"
          v-model:visible="receiverVisible"
          :selectedItem="state.selectedItem"
          @refresh="loadPushConfigList" />
      </AsyncComponent>
    </div>
    <expand-head v-model:visible="showRecord" class="mt-5">
      <template #title>
        <span class="text-theme-title text-3.5 leading-3.5 font-medium">{{ t('事件记录') }}</span>
      </template>
    </expand-head>
    <div :class="showRecord ? 'open-record' : 'stop-record'" class="transition-height duration-500 overflow-hidden">
      <div class="flex justify-between items-center my-2">
        <SearchPanel
          :options="searchLogOpt"
          @change="changeLogParams" />
        <IconRefresh
          class="text-4"
          :loading="recordLoading"
          @click="loadPushRecordList" />
      </div>
      <Table
        rowKey="id"
        size="small"
        :dataSource="pushRecordList"
        :columns="recordColumns"
        :loading="recordLoading"
        :pagination="pagination"
        @change="changePagination">
        <template #bodyCell="{ column, text, record }">
          <template v-if="column.dataIndex === 'pushStatus'">
            <Badge
              :status="getStatus(text.value)"
              :text="text.message">
            </Badge>
            <Popover
              :overlayStyle="{maxWidth:'500px'}">
              <Icon
                v-if="text?.value === 'PUSH_FAIL' "
                icon="icon-shuoming"
                class="text-3.5 text-theme-sub-content text-theme-text-hover ml-1" />
              <template #title>
                {{ t('errMsg') }}
              </template>
              <template #content>
                <div class="max-h-100 max-w-150 overflow-auto" v-html="record.pushMsg"></div>
              </template>
            </Popover>
          </template>
          <template v-if="column.dataIndex === 'type'">
            {{ text.message }}
          </template>
        </template>
      </Table>
    </div>
  </div>
</template>
<style scoped>
:deep(.ant-table-thead .ant-table-cell) {
  border: 0 !important;
}

.open-info {
  max-height: 300px;
}

.stop-info {
  max-height: 0;
}

.open-record {
  max-height: 500px;
}

.stop-record {
  max-height: 0;
}
</style>
