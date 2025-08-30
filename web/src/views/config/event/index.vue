<script setup lang="ts">
import { Badge, CheckboxGroup, Divider, Popover } from 'ant-design-vue';
import { defineAsyncComponent } from 'vue';
import { AsyncComponent, Hints, Icon, IconRefresh, SearchPanel, Table } from '@xcan-angus/vue-ui';
import { useEventData } from './composables';

const InfoCard = defineAsyncComponent(() => import('./InfoCard.vue'));
const ExpandHead = defineAsyncComponent(() => import('./ExpandHead.vue'));
const ReceiveConfig = defineAsyncComponent(() => import('./ReceiveConfig.vue'));
const Receiver = defineAsyncComponent(() => import('./Receiver.vue'));

// Use composable for event data management
const {
  // Reactive data
  configLoading,
  recordLoading,
  showRecord,
  showConfigure,
  receiverVisible,
  configureVisible,
  state,
  pushSettingList,
  pushRecordList,
  noticeType,

  // Computed properties
  configColumns,
  recordColumns,
  pagination,
  searchLogOpt,

  // Methods
  openReceiveChannel,
  openReceiver,
  changeLogParams,
  changePagination,
  handleChangeNoticeType,
  getReceiver,
  getTargetTypeName,
  getStatus,
  loadPushConfigList,
  loadPushRecordList,
  onMountedInit,

  // i18n
  t
} = useEventData();

// Initialize data on component mount
onMountedInit();

</script>

<template>
  <div class="py-3.5 px-5">
    <Hints :text="t('notification.hints')" class="!leading-4.5" />
    <div class="flex space-x-2 w-250 mt-2">
      <InfoCard
        :name="t('notification.status.summary')"
        :value="state.count.total"
        icon="icon-zonglan" />
      <InfoCard
        :name="t('notification.status.pending')"
        :value="state.count.pushSuccess"
        iconColor="rgba(255, 129, 0, 1)"
        icon="icon-tuisongchenggong" />
      <InfoCard
        :name="t('notification.status.failure')"
        :value="state.count.pushFail"
        icon="icon-tuisongshibai" />
      <InfoCard
        :name="t('notification.status.ignore')"
        :value="state.count.ignore"
        icon="icon-yiquxiao" />
    </div>
    <ExpandHead v-model:visible="showConfigure" class="mt-5">
      <template #title>
        <div>
          <span class="text-theme-title text-3.5 leading-3.5 font-medium">{{ t('notification.config.pushConfig') }}</span>
          <Popover placement="right">
            <template #content>
              <div class="max-w-110">
                {{ t('notification.config.push_config_tip') }}
              </div>
            </template>
            <Icon icon="icon-tishi1" class="text-tips text-3.5 ml-2" />
          </Popover>
        </div>
      </template>
    </ExpandHead>
    <div :class="showConfigure ? 'open-info' : 'stop-info'" class="transition-height duration-500 overflow-hidden">
      <Table
        rowKey="id"
        size="small"
        class="my-3.5"
        :dataSource="pushSettingList"
        :columns="configColumns"
        :scroll="{y:pushSettingList.length>10?'180px':'220px', x: 'max-content', scrollToFirstRowOnChange: false}"
        :loading="configLoading"
        :pagination="pushSettingList?.length > 10">
        <template #bodyCell="{ column, text, record }">
          <template v-if="column.dataIndex === 'allowedChannelTypes'">
            {{ (text || [])?.map(m=>m.message).join('、') }}
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
              :options="noticeType"
              @change="handleChangeNoticeType($event as string[], record.eventCode)" />
          </template>
          <template v-if="column.dataIndex === 'operate'">
            <a class="text-theme-text-hover" @click="openReceiveChannel(record)">{{
              t('notification.config.configReceiveChannel')
            }}</a>
            <Divider type="vertical" />
            <a class="text-theme-text-hover" @click="openReceiver(record)">{{
              t('notification.config.configReceiver')
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
        <span class="text-theme-title text-3.5 leading-3.5 font-medium">{{ t('notification.config.event_record') }}</span>
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
              :status="getStatus(text.value) as any"
              :text="text.message">
            </Badge>
            <Popover
              :overlayStyle="{maxWidth:'500px'}">
              <Icon
                v-if="text?.value === 'PUSH_FAIL' "
                icon="icon-shuoming"
                class="text-3.5 text-theme-sub-content text-theme-text-hover ml-1" />
              <template #title>
                {{ t('notification.errMsg') }}
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
