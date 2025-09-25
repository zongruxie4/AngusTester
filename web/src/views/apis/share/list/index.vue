<script setup lang="ts">
import { defineAsyncComponent, inject, onMounted, ref, watch } from 'vue';
import { Button, Tag } from 'ant-design-vue';
import { AsyncComponent, Icon, modal, NoData, notification, Spin, Table, Image } from '@xcan-angus/vue-ui';
import { toClipboard } from '@xcan-angus/infra';
import { apis } from '@/api/tester';
import { useI18n } from 'vue-i18n';

import { ShareInfo } from '../PropsType';
import SearchPanel from '@/views/apis/share/list/searchPanel/index.vue';
import { nextTick } from 'process';

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

const Introduce = defineAsyncComponent(() => import('@/views/apis/share/list/introduce/index.vue'));
const EditModal = defineAsyncComponent(() => import('@/views/apis/share/edit/index.vue'));

const deleteTabPane = inject<(keys: string[]) => void>('deleteTabPane', () => ({}));

const loaded = ref(false);
const loading = ref(false);
const searchedFlag = ref(false);
const editVisible = ref(false);
const selectShareId = ref();

const searchPanelParams = ref({
  orderBy: undefined,
  orderSort: undefined,
  filters: []
});

const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0
});
const pageNo = ref(1);

const dataList = ref<ShareInfo[]>([]);
const permissionsMap = ref<Map<string, string[]>>(new Map());

const refresh = () => {
  pagination.value.current = 1;
  permissionsMap.value.clear();
  loadData();
};

const searchChange = (data) => {
  pagination.value.current = 1;
  searchPanelParams.value.filters = data.filters;
  loadData();
};

const toDelete = async (data: ShareInfo) => {
  modal.confirm({
    content: t('apiShare.list.confirmDelete', { name: data.name }),
    async onOk () {
      const id = data.id;
      const [error] = await apis.deleteShare(id);
      if (error) {
        return;
      }
      notification.success(t('actions.tips.deleteSuccess'));
      if (pagination.value.current > 1 && dataList.value.length === 1) {
        pagination.value.current -= 1;
      }

      loadData();
      deleteTabPane([id]);
    }
  });
};

const tableChange = (page, _, sorter) => {
  searchPanelParams.value.orderBy = sorter.orderBy || undefined;
  searchPanelParams.value.orderSort = sorter.orderSort || undefined;
  pagination.value.current = page.current;
  pagination.value.pageSize = page.pageSize;
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
    pageNo: pagination.value.current,
    pageSize: pagination.value.pageSize,
    ...searchPanelParams.value
  };

  const [error, res] = await apis.getShareList({ ...params });
  loaded.value = true;
  loading.value = false;

  if (params.filters?.length || params.orderBy) {
    searchedFlag.value = true;
  } else {
    searchedFlag.value = false;
  }

  if (error) {
    pagination.value.total = 0;
    dataList.value = [];
    return;
  }

  const data = res?.data || { total: 0, list: [] };
  if (data) {
    pagination.value.total = +data.total;
    const _list = (data.list || [] as ShareInfo[]);
    dataList.value = _list;
  }
};

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
});

const editVersion = (record = {}) => {
  selectShareId.value = record.id;
  editVisible.value = true;
};

const copyLink = async (record: {id: string; name: string; url?: string} = { name: '', id: '' }) => {
  if (record.url) {
    toClipboard(t('apiShare.messages.copyLinkSuccess', { name: record.name, url: record.url })).then(() => {
      notification.success(t('apiShare.messages.copyToClipboardSuccess'));
    });
    return;
  }
  const [error, { data }] = await apis.getShareDetail(record.id);
  if (error) {
    return;
  }
  record.url = data?.url;
  toClipboard(t('apiShare.messages.copyLinkSuccess', { name: record.name, url: record.url })).then(() => {
    notification.success(t('apiShare.messages.copyToClipboardSuccess'));
  });
};

const handleEnterShare = async (shareId: string) => {
  const [error, { data }] = await apis.getShareDetail(shareId);
  if (error) {
    return;
  }
  nextTick(() => {
    window.open(data.url);
  });
};

const columns = [
  {
    title: t('common.name'),
    dataIndex: 'name',
    width: '15%',
    ellipsis: true,
    sorter: true
  },
  {
    title: t('common.isExpired'),
    dataIndex: 'isExpired',
    width: '8%'
  },
  {
    title: t('apiShare.list.columns.sharePerson'),
    dataIndex: 'createdByAvatar',
    width: '8%',
    sorter: true,
    ellipsis: true
  },
  {
    title: t('apiShare.list.columns.shareScope'),
    dataIndex: 'shareScope',
    width: '8%',
    customRender: ({ text }) => text?.message
  },
  {
    title: t('common.expiredDate'),
    dataIndex: 'expiredDate',
    width: '9%',
    sorter: true,
    customRender: ({ text }) => text || '--',
    groupName: 'date'
  },
  {
    title: t('apiShare.list.columns.shareDate'),
    dataIndex: 'createdDate',
    width: '9%',
    sorter: true,
    groupName: 'date',
    hide: true
  },
  {
    title: t('apiShare.list.columns.viewCount'),
    dataIndex: 'viewNum',
    width: '8%'
  },
  {
    title: t('common.remark'),
    dataIndex: 'remark',
    ellipsis: true,
    width: '12%'
  },
  {
    title: t('common.lastModifiedBy'),
    dataIndex: 'lastModifiedByName',
    groupName: 'lastModifiedByName',
    ellipsis: true,
    width: '8%'
  },
  {
    title: t('common.lastModifiedDate'),
    dataIndex: 'lastModifiedDate',
    groupName: 'lastModifiedByName',
    hide: true,
    width: '8%'
  },
  {
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
                  <Tag :color="record.expired ? 'error' : 'success'">{{ record.expired ? t('apiShare.list.expired') : t('apiShare.list.notExpired') }}</Tag>
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
        :projectId="props.projectId"
        @ok="loadData" />
    </AsyncComponent>
  </div>
</template>
