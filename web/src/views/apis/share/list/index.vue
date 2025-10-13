<script setup lang="ts">
import { defineAsyncComponent, inject, onMounted, ref, watch, nextTick } from 'vue';
import { Button, Tag } from 'ant-design-vue';
import { AsyncComponent, Icon, Image, modal, NoData, notification, Spin, Table } from '@xcan-angus/vue-ui';
import { toClipboard, ProjectPageQuery, PageQuery } from '@xcan-angus/infra';
import { apis } from '@/api/tester';
import { useI18n } from 'vue-i18n';

import { ShareInfo } from '../types';
import { BasicProps } from '@/types/types';

const props = withDefaults(defineProps<BasicProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  notify: undefined
});

const SearchPanel = defineAsyncComponent(() => import('@/views/apis/share/list/SearchPanel.vue'));
const Introduce = defineAsyncComponent(() => import('@/views/apis/share/list/Introduce.vue'));
const EditModal = defineAsyncComponent(() => import('@/views/apis/share/Edit.vue'));

const { t } = useI18n();

const deleteTabPane = inject<(keys: string[]) => void>('deleteTabPane', () => ({}));

// UI/loading states
const loaded = ref(false);
const loading = ref(false);
const searchedFlag = ref(false);
const editVisible = ref(false);
// current share id to edit
const selectShareId = ref();

// search/sort params synchronized with SearchPanel and Table change
const searchPanelParams = ref({
  orderBy: undefined as string | undefined,
  orderSort: undefined as PageQuery.OrderSort | undefined,
  filters: [] as Array<any>
});

// basic pagination state
const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0
});
const pageNo = ref(1);

// table data list
const dataList = ref<ShareInfo[]>([]);
// shareId -> permissions (cached)
const permissionsMap = ref<Map<string, string[]>>(new Map());

/**
 * Refresh list from first page, clearing cached permissions
 */
const refresh = () => {
  pagination.value.current = 1;
  permissionsMap.value.clear();
  loadData();
};

/**
 * Handle search panel change and reset to page 1
 */
const searchChange = (data) => {
  pagination.value.current = 1;
  searchPanelParams.value.filters = data.filters;
  loadData();
};

/**
 * Confirm and delete a share record, then refresh list and close related tabs
 */
const toDelete = async (data: ShareInfo) => {
  modal.confirm({
    content: t('apiShare.list.confirmDelete', { name: data.name }),
    async onOk () {
      const id = data.id;
      const [error] = await apis.deleteShare(id);
      if (error) return;
      notification.success(t('actions.tips.deleteSuccess'));
      // if last item on current page removed, step back one page
      if (pagination.value.current > 1 && dataList.value.length === 1) {
        pagination.value.current -= 1;
      }
      await loadData();
      deleteTabPane([id]);
    }
  });
};

/**
 * Update pagination and sort state when table changes
 */
const tableChange = (page, _, sorter) => {
  searchPanelParams.value.orderBy = sorter.orderBy || undefined;
  searchPanelParams.value.orderSort = sorter.orderSort || undefined;
  pagination.value.current = page.current;
  pagination.value.pageSize = page.pageSize;
  loadData();
};

/**
 * Fetch share list data based on current pagination and search params
 */
const loadData = async () => {
  loading.value = true;
  const { current, pageSize } = pagination.value;
  const params: ProjectPageQuery = {
    projectId: String(props.projectId ?? ''),
    pageNo: current,
    pageSize,
    ...searchPanelParams.value
  };

  const [error, res] = await apis.getShareList({ ...params });
  loaded.value = true;
  loading.value = false;

  // mark if user has applied filters or sorting
  searchedFlag.value = !!(params.filters?.length || params.orderBy);

  if (error) {
    pagination.value.total = 0;
    dataList.value = [];
    return;
  }

  const data = res?.data || { total: 0, list: [] };
  pagination.value.total = +data.total;
  dataList.value = (data.list || [] as ShareInfo[]);
};

/**
 * Open edit modal for the selected share (compatible with MouseEvent handler)
 */
const editVersion = (record: { id?: string } | MouseEvent = {}) => {
  // extract id if a record object is passed; ignore when it's a MouseEvent
  const maybeRecord = record as any;
  selectShareId.value = maybeRecord && typeof maybeRecord === 'object' ? maybeRecord.id : undefined;
  editVisible.value = true;
};

/**
 * Copy share link to clipboard; fetch detail first if url is missing
 */
const copyLink = async (record: {id: string; name: string; url?: string} = { name: '', id: '' }) => {
  if (record.url) {
    toClipboard(t('apiShare.messages.copyLinkSuccess', { name: record.name, url: record.url })).then(() => {
      notification.success(t('apiShare.messages.copyToClipboardSuccess'));
    });
    return;
  }
  const [error, { data }] = await apis.getShareDetail(record.id);
  if (error) return;
  record.url = data?.url;
  toClipboard(t('apiShare.messages.copyLinkSuccess', { name: record.name, url: record.url })).then(() => {
    notification.success(t('apiShare.messages.copyToClipboardSuccess'));
  });
};

/**
 * Open share page in a new window by share id
 */
const handleEnterShare = async (shareId: string) => {
  const [error, { data }] = await apis.getShareDetail(shareId);
  if (error) return;
  await nextTick(() => {
    window.open(data.url);
  });
};

onMounted(() => {
  // reload when project changes
  watch(() => props.projectId, () => {
    pageNo.value = 1;
    loadData();
  }, { immediate: true });

  // refresh when notified
  watch(() => props.notify, (newValue) => {
    if (!newValue) return;
    loadData();
  }, { immediate: false });
});

// Table columns definition
const columns = [
  {
    key: 'name',
    title: t('common.name'),
    dataIndex: 'name',
    width: '15%',
    ellipsis: true,
    sorter: true
  },
  {
    key: 'isExpired',
    title: t('common.isExpired'),
    dataIndex: 'isExpired',
    width: '8%'
  },
  {
    key: 'createdByAvatar',
    title: t('apiShare.list.columns.sharePerson'),
    dataIndex: 'createdByAvatar',
    width: '8%',
    sorter: true,
    ellipsis: true
  },
  {
    key: 'shareScope',
    title: t('apiShare.list.columns.shareScope'),
    dataIndex: 'shareScope',
    width: '8%',
    customRender: ({ text }) => text?.message
  },
  {
    key: 'expiredDate',
    title: t('common.expiredDate'),
    dataIndex: 'expiredDate',
    width: '9%',
    sorter: true,
    customRender: ({ text }) => text || '--',
    groupName: 'date'
  },
  {
    key: 'createdDate',
    title: t('apiShare.list.columns.shareDate'),
    dataIndex: 'createdDate',
    width: '9%',
    sorter: true,
    groupName: 'date',
    hide: true
  },
  {
    key: 'viewNum',
    title: t('apiShare.list.columns.viewCount'),
    dataIndex: 'viewNum',
    width: '8%'
  },
  {
    key: 'remark',
    title: t('common.remark'),
    dataIndex: 'remark',
    ellipsis: true,
    width: '12%'
  },
  {
    key: 'lastModifiedByName',
    title: t('common.lastModifiedBy'),
    dataIndex: 'lastModifiedByName',
    groupName: 'lastModifiedByName',
    ellipsis: true,
    width: '8%'
  },
  {
    key: 'lastModifiedDate',
    title: t('common.lastModifiedDate'),
    dataIndex: 'lastModifiedDate',
    groupName: 'lastModifiedByName',
    hide: true,
    width: '8%'
  },
  {
    key: 'actions',
    title: t('common.actions'),
    dataIndex: 'actions',
    width: '15%'
  }
];
</script>

<template>
  <div class="flex flex-col h-full overflow-auto px-5 py-5 leading-5 text-3">
    <div class="flex space-x-1">
      <Introduce class="mb-5 flex-1" />
    </div>
    <div class="text-3.5 font-semibold mb-1">{{ t('apiShare.list.title') }}</div>
    <Spin :spinning="loading" class="flex-1 flex flex-col">
      <template v-if="loaded">
        <div v-if="!searchedFlag && dataList.length === 0" class="flex-1 flex flex-col items-center justify-center">
          <img src="../../../../assets/images/nodata.png">
          <div class="flex items-center text-theme-sub-content text-3.5 leading-5 space-x-1">
            <span>{{ t('common.noData') }}</span>
            <Button type="link" @click="editVersion">{{ t('apiShare.list.addShare') }}</Button>
          </div>
        </div>

        <template v-else>
          <SearchPanel
            @change="searchChange"
            @refresh="refresh"
            @add="editVersion" />
          <NoData v-if="dataList.length === 0" class="flex-1" />
          <template v-else>
            <Table
              :columns="columns"
              :dataSource="dataList"
              :pagination="pagination"
              noDataSize="small"
              :noDataText="t('common.noData')"
              @change="tableChange">
              <template #bodyCell="{column, record}">
                <template v-if="column.dataIndex === 'name'">
                  <Button
                    type="link"
                    size="small"
                    class="px-0"
                    @click="handleEnterShare(record.id)">
                    {{ record.name }}
                  </Button>
                </template>

                <template v-if="column.dataIndex === 'createdByAvatar'">
                  <div class="inline-flex items-center ">
                    <Image
                      type="avatar"
                      class="w-4 rounded-full mr-1"
                      :src="record.createdByAvatar" />
                    <span class="flex-1 truncate" :title="record.createdByName">{{ record.createdByName }}</span>
                  </div>
                </template>

                <template v-if="column.dataIndex === 'remark'">
                  <template v-if="record.remark">{{ record.remark }}</template>
                  <span v-else class="text-sub-content">{{ t('common.noRemark') }}</span>
                </template>

                <template v-if="column.dataIndex === 'isExpired'">
                  <Tag :color="record.expired ? 'error' : 'success'">
                    {{ record.expired ? t('apiShare.list.expired') : t('apiShare.list.notExpired') }}
                  </Tag>
                </template>

                <template v-if="column.dataIndex === 'actions'">
                  <Button
                    type="text"
                    size="small"
                    @click="copyLink(record)">
                    <Icon icon="icon-fuzhi" class="mr-1" />
                    {{ t('actions.copy') }}
                  </Button>
                  <Button
                    type="text"
                    size="small"
                    @click="editVersion(record)">
                    <Icon icon="icon-bianji" class="mr-1" />
                    {{ t('actions.edit') }}
                  </Button>
                  <Button
                    type="text"
                    size="small"
                    @click="toDelete(record)">
                    <Icon icon="icon-qingchu" class="mr-1" />
                    {{ t('actions.delete') }}
                  </Button>
                </template>
              </template>
            </Table>
          </template>
        </template>
      </template>
    </Spin>
    <AsyncComponent :visible="editVisible">
      <EditModal
        v-model:visible="editVisible"
        :shareId="selectShareId"
        :projectId="String(props.projectId)"
        @ok="loadData" />
    </AsyncComponent>
  </div>
</template>
