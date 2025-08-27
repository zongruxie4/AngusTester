<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { Grid, Hints, Icon, IconRefresh, Input, NoData, PureCard, Spin, FormatHighlight } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import { Pagination, TabPane, Tabs, Tooltip } from 'ant-design-vue';
import { debounce } from 'throttle-debounce';
import { duration } from '@xcan-angus/infra';

import { mock } from '@/api/tester';

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

const { t } = useI18n();
const params = ref<SearchParam>({ pageNo: 1, pageSize: 10, filters: [] });
const total = ref(0);

const tableList = ref<any[]>([]);

const loading = ref(false);
const getUserList = async function () {
  loading.value = true;
  const [error, { data = { list: [], total: 0 } }] = await mock.loadMockApiLogList(props.id, params.value);
  loading.value = false;
  if (error) {
    return;
  }

  tableList.value = data.list;
  total.value = +data.total;
  selectedApi.value = data.list[0];
  getDetail(data.list[0].id);
  detail.value = undefined;
};

const handleSearch = debounce(duration.search, (event:ChangeEvent) => {
  params.value.pageNo = 1;
  const value = event.target.value;
  if (value) {
    params.value.filters = [{ key: 'summary', value: value, op: 'MATCH_END' }];
  } else {
    params.value.filters = [];
  }
  getUserList();
});

const detail = ref();

const requestColumns = ref<any>([[]]);
const requestInfo = ref<any>({});

const responseColumns = ref<any>([[]]);
const responseInfo = ref<any>({});
const getDetail = async (id: string) => {
  requestColumns.value = [[]];
  requestInfo.value = {};
  responseColumns.value = [[]];
  responseInfo.value = {};
  const [error, { data }] = await mock.getMockApiLogDetail(id);
  if (error) {
    return;
  }

  detail.value = data;
  if (data.requestHeaders?.length) {
    for (let i = 0; i < data.requestHeaders.length; i++) {
      const _header = data.requestHeaders[i];
      requestColumns.value[0].push({
        label: _header.name,
        dataIndex: _header.name
      });
    }
    requestInfo.value = Object.assign({}, ...data.requestHeaders.map(header => ({ [header.name]: header.value })));
  }

  detail.value = data;
  if (data.responseHeaders?.length) {
    for (let i = 0; i < data.responseHeaders.length; i++) {
      const _header = data.responseHeaders[i];
      responseColumns.value[0].push({
        label: _header.name,
        dataIndex: _header.name
      });
    }
    responseInfo.value = Object.assign({}, ...data.responseHeaders.map(header => ({ [header.name]: header.value })));
  }
};

const selectedApi = ref();
const handleClick = (item) => {
  selectedApi.value = item;
  getDetail(item.id);
};

const activeKey = ref(0);

onMounted(() => {
  getUserList();
});

const paginationChange = async (page: number, size: number) => {
  params.value.pageNo = page;
  params.value.pageSize = size;
  await getUserList();
};

const gridColumns = [
  [
    {
      label: t('接口名称'),
      dataIndex: 'summary'
    },
    {
      label: t('请求ID'),
      dataIndex: 'requestId'
    },
    {
      label: t('请求时间'),
      dataIndex: 'requestDate'
    },
    {
      label: t('请求地址'),
      dataIndex: 'endpoint'
    },
    {
      label: t('请求方法'),
      dataIndex: 'method'
    },
    {
      label: t('状态码'),
      dataIndex: 'responseStatus'
    }
  ]
];

const currentTabId = ref<string>('pretty');
const tabs = [
  {
    name: '美化格式',
    value: 'pretty'
  },
  {
    name: '原生格式',
    value: 'raw'
  },
  {
    name: '预览',
    value: 'preview'
  }
];

const isActive = (id: string): boolean => {
  return currentTabId.value === id;
};

const contentType = computed(() => {
  return 'json';
});

const handleSelect = (id: string): void => {
  currentTabId.value = id;
};

const textColor = {
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
  <Hints text="当前请求记录是Mock服务收集的Mock接口调用日志，系统1秒内最大采集3条请求日志。每个Mock接口只保留最近100条请求记录。注意：如需要开启或关闭请求日志收集，请在“设置”->“记录请求日志”配置，开启后会降低Mock接口性能。" class="mb-2" />
  <PureCard class="p-3.5 flex" style="height: calc(100% - 28px);">
    <Spin
      :spinning="loading"
      class="w-80 border-r border-border-divider h-full flex flex-col">
      <div class="pr-3.5 mb-2 flex items-center">
        <Input placeholder="查询接口名称、路径" @change="handleSearch">
          <template #suffix>
            <Icon icon="icon-sousuo" class="text-4 text-theme-sub-content" />
          </template>
        </Input>
        <IconRefresh
          :loading="loading"
          class="ml-2 text-3.75"
          @click="getList" />
      </div>
      <template v-if="tableList.length > 0">
        <div
          class="relative text-3 leading-3 flex-1 overflow-hidden hover:overflow-y-auto pr-1.75"
          style="scrollbar-gutter: stable;">
          <div
            v-for="item,index in tableList"
            :key="item.id"
            class="p-2 space-y-3.5 cursor-pointer"
            :class="{ 'border-b border-border-divider': index < tableList.length - 1, 'bg-bg-selected border-l-2 border-selected': selectedApi?.id === item?.id }"
            @click="handleClick(item)">
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
              <div :class="textColor[item?.method]">{{ item?.method }}</div>
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
          :current="params.pageNo"
          :pageSize="params.pageSize"
          :total="total"
          :hideOnSinglePage="false"
          :showTotal="false"
          :showSizeChanger="false"
          showLessItems
          size="small"
          class="mt-2 mr-2"
          @change="paginationChange" />
      </template>
      <template v-else>
        <NoData class="flex-1" />
      </template>
    </Spin>
    <Tabs
      v-model:activeKey="activeKey"
      size="small"
      class="flex-1 ml-3.5 h-full -mr-3.5">
      <TabPane
        :key="0"
        :tab="t('基本信息')"
        class="h-full">
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
        :tab="t('请求信息')"
        class="flex-1 flex flex-col"
        forceRender>
        <template v-if="detail">
          <div class="text-3 text-text-title pl-1.25 mb-1">请求头</div>
          <Grid
            :columns="requestColumns"
            :dataSource="requestInfo"
            :colon="false"
            labelClass="text-text-sub-content"
            labelSpacing="80px"
            marginBottom="0px"
            class="-ml-2 grid-row" />
          <div class="text-3 text-text-title pl-1.25 mt-5 mb-1">请求体</div>
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
        :tab="t('响应信息')"
        class="flex-1 flex flex-col"
        forceRender>
        <template v-if="detail">
          <div class="text-3 text-text-title pl-1.25 mb-1">响应头</div>
          <Grid
            :columns="responseColumns"
            :dataSource="responseInfo"
            :colon="false"
            labelClass="text-text-sub-content"
            labelSpacing="80px"
            marginBottom="0px"
            class="-ml-2 grid-row" />
          <div class="text-3 text-text-title pl-1.25 mt-5 mb-2">响应体</div>
          <template v-if="detail?.responseBody">
            <div class="flex mb-3 flex-freeze-auto items-center rounded text-3 text-text-sub-content">
              <div
                v-for="item in tabs"
                :key="item.value"
                :class="{ 'text-text-link bg-gray-light rounded': isActive(item.value) }"
                class="flex justify-center items-center min-w-20 h-7 px-3 cursor-pointer bg-gray-50"
                @click="handleSelect(item.value)">
                {{ item.name }}
              </div>
            </div>
            <template v-if="detail.responseBody">
              <FormatHighlight
                class="flex-1 overflow-y-auto px-2"
                :dataSource="detail.responseBody"
                :dataType="contentType"
                :format="currentTabId" />
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
