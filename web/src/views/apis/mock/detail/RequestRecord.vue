<script setup lang="ts">
import { ref, watch } from 'vue';
import { Grid, Hints, Icon, IconRefresh, Input, NoData, PureCard, Spin } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import { Pagination, TabPane, Tabs, Tooltip } from 'ant-design-vue';
import { useRequestRecords } from './composables/useRequestRecords';
import { useRecordDetail } from './composables/useRecordDetail';
import { useGridColumns } from './composables/useGridColumns';

interface Props {
  id: string;
}

const props = withDefaults(defineProps<Props>(), {
  id: undefined
});

const { t } = useI18n();

// Use composables to manage different aspects of request records
const {
  recordList,
  total,
  loading,
  selectedRecord,
  handleSearch,
  handlePaginationChange,
  handleRecordSelect,
  refreshRecords
} = useRequestRecords(props.id);

const {
  detail,
  requestColumns,
  requestInfo,
  responseColumns,
  responseInfo,
  contentType,
  loadRecordDetail
} = useRecordDetail();

const { gridColumns, formatTabs, httpMethodColors } = useGridColumns();

// Tab state for response body formatting
const activeTabKey = ref(0);
const currentFormatType = ref<string>('pretty');

/**
 * Handle format tab selection for response body display.
 */
const handleFormatSelect = (formatId: string): void => {
  currentFormatType.value = formatId;
};

/**
 * Check if a format tab is currently active.
 */
const isFormatActive = (formatId: string): boolean => {
  return currentFormatType.value === formatId;
};

// Watch for selected record changes to load details
watch(selectedRecord, (newRecord) => {
  if (newRecord?.id) {
    loadRecordDetail(newRecord.id);
    detail.value = undefined; // Reset detail display during loading
  }
}, { immediate: true });
</script>
<template>
  <!-- TODO 请求记录分页不生效 -->
  <Hints :text="t('mock.detail.requestRecord.hints')" class="mb-2 text-3.5" />
  <PureCard class="p-3.5 flex" style="height: calc(100% - 28px);">
    <Spin
      :spinning="loading"
      class="w-80 border-r border-border-divider h-full flex flex-col mr-3">
      <div class="pr-3.5 mb-2 flex items-center">
        <Input :placeholder="t('mock.detail.monitor.searchApiNamePath')" @change="(e) => handleSearch(e.target.value)">
          <template #suffix>
            <Icon icon="icon-sousuo" class="text-4 text-theme-sub-content" />
          </template>
        </Input>
        <IconRefresh
          :loading="loading"
          class="ml-2 text-3.75"
          @click="refreshRecords" />
      </div>
      <template v-if="recordList.length > 0">
        <div
          class="relative text-3 leading-3 flex-1 overflow-hidden hover:overflow-y-auto pr-1.75"
          style="scrollbar-gutter: stable;">
          <div
            v-for="item,index in recordList"
            :key="item.id"
            class="p-2 space-y-3.5 cursor-pointer"
            :class="{ 'border-b border-border-divider': index < recordList.length - 1, 'bg-bg-selected border-l-2 border-selected': selectedRecord?.id === item?.id }"
            @click="handleRecordSelect(item)">
            <div class="flex items-center justify-between space-x-5">
              <div class="truncate font-medium">
                <Tooltip
                  :title="item?.summary"
                  placement="bottomLeft">
                  {{ item?.summary }}
                </Tooltip>
              </div>
              <div
                class="whitespace-nowrap flex items-center"
                :class="[item.responseStatus?.substring(0, 2) === '20' ? 'text-status-success' : 'text-status-error']">
                <Icon
                  :icon="item.responseStatus === '200' ? 'icon-right' : 'icon-cuowu'"
                  class="mr-1" />{{ item.responseStatus }}
              </div>
            </div>
            <div class="flex items-center space-x-3.5">
              <div :class="httpMethodColors[item?.method]">{{ item?.method }}</div>
              <div class="truncate">
                <Tooltip
                  :title="item?.endpoint"
                  placement="bottomLeft">
                  {{ item?.endpoint }}
                </Tooltip>
              </div>
            </div>
          </div>
        </div>
        <Pagination
          :current="1"
          :pageSize="10"
          :total="total"
          :hideOnSinglePage="false"
          :showSizeChanger="false"
          showLessItems
          size="small"
          class="mt-2 mr-2"
          @change="handlePaginationChange" />
      </template>
      <template v-else>
        <NoData class="flex-1" />
      </template>
    </Spin>
    <Tabs
      v-model:activeKey="activeTabKey"
      size="small"
      class="flex-1 ml-3.5 h-full -mr-3.5">
      <TabPane
        :key="0"
        :tab="t('mock.detail.requestRecord.basicInfo')"
        class="h-full font-semibold">
        <template v-if="detail">
          <Grid
            :columns="gridColumns"
            :dataSource="detail"
            :colon="false"
            labelSpacing="80px"
            marginBottom="0px"
            class="-ml-2 grid-row">
          </Grid>
        </template>
        <template v-else>
          <NoData />
        </template>
      </TabPane>
      <TabPane
        :key="1"
        :tab="t('mock.detail.requestRecord.requestInfo')"
        class="flex-1 flex flex-col font-semibold"
        forceRender>
        <template v-if="detail">
          <div class="text-3 text-text-title pl-1.25 mb-1">{{ t('mock.detail.requestRecord.requestHeader') }}</div>
          <Grid
            :columns="requestColumns"
            :dataSource="requestInfo"
            :colon="false"
            labelClass="text-text-sub-content"
            labelSpacing="80px"
            marginBottom="0px"
            class="-ml-2 grid-row" />
          <div class="text-3 text-text-title pl-1.25 mt-5 mb-1">{{ t('mock.detail.requestRecord.requestBody') }}</div>
          <template v-if="detail?.requestBody">
            <div class="bg-bg-table-head text-3 text-text-content p-2 rounded-sm" style="min-height: 34px;">
              {{ detail.requestBody }}
            </div>
          </template>
          <template v-else>
            <div class="bg-bg-table-head text-3 text-text-sub-content px-2 py-1.75 rounded-sm">
              &lt;No body&gt;
            </div>
          </template>
        </template>
        <template v-else>
          <NoData />
        </template>
      </TabPane>
      <TabPane
        :key="2"
        :tab="t('mock.detail.requestRecord.responseInfo')"
        class="flex-1 flex flex-col font-semibold"
        forceRender>
        <template v-if="detail">
          <div class="text-3 text-text-title pl-1.25 mb-1">{{ t('mock.detail.requestRecord.responseHeader') }}</div>
          <Grid
            :columns="responseColumns"
            :dataSource="responseInfo"
            :colon="false"
            labelClass="text-text-sub-content"
            labelSpacing="80px"
            marginBottom="0px"
            class="-ml-2 grid-row" />
          <div class="text-3 text-text-title pl-1.25 mt-5 mb-2">{{ t('mock.detail.requestRecord.responseBody') }}</div>
          <template v-if="detail?.responseBody">
            <div class="flex mb-3 flex-freeze-auto items-center rounded text-3 text-text-sub-content">
              <div
                v-for="item in formatTabs"
                :key="item.value"
                :class="{ 'text-text-link bg-gray-light rounded': isFormatActive(item.value) }"
                class="flex justify-center items-center min-w-20 h-7 px-3 cursor-pointer bg-gray-50"
                @click="handleFormatSelect(item.value)">
                {{ item.name }}
              </div>
            </div>
            <template v-if="detail.responseBody">
              <div class="flex-1 overflow-y-auto px-2 bg-bg-table-head text-3 text-text-content p-2 rounded-sm whitespace-pre-wrap">
                {{ detail.responseBody }}
              </div>
            </template>
          </template>
          <template v-else>
            <div class="bg-bg-table-head text-3 text-text-sub-content px-2 py-1.75 rounded-sm">
              &lt;No body&gt;
            </div>
          </template>
        </template>
        <template v-else>
          <NoData />
        </template>
      </TabPane>
    </Tabs>
  </PureCard>
</template>
<style scoped>
.ant-tabs > :deep(.ant-tabs-nav) .ant-tabs-tab {
  padding: 5px 0 4px;
}

.border-selected {
  border-left-color: var(--border-divider-selected);
}

:deep(.grid-row > div) {
  margin-bottom: 0;
  padding-right: 8px;
  padding-left: 8px;
}

:deep(.grid-row > div >:first-child) {
  padding: 2px 8px;
}

:deep(.grid-row > div:nth-child(2n)) {
  background-color: var(--table-header-bg);
}

.ant-tabs > :deep(.ant-tabs-nav .ant-tabs-tab) {
  padding: 5px 0 4px;
}

.bg-gray-light {
  background-color: #eaf8ff;
}
</style>
