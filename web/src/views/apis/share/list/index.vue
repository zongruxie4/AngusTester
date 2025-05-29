<script setup lang="ts">
import { defineAsyncComponent, inject, onMounted, ref, watch } from 'vue';
import { Button, Tag } from 'ant-design-vue';
import { AsyncComponent, Icon, modal, NoData, notification, Spin, Table } from '@xcan-angus/vue-ui';
import { clipboard } from '@xcan-angus/tools';
import { apis } from '@/api/tester';

import { ShareInfo } from '../PropsType';
import SearchPanel from '@/views/apis/share/list/searchPanel/index.vue';
import { nextTick } from 'process';

const { toClipboard } = clipboard;

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
    content: `确定删除分享【${data.name}】吗？`,
    async onOk () {
      const id = data.id;
      const [error] = await apis.delShare(id);
      if (error) {
        return;
      }
      notification.success('删除成功');
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

  const [error, res] = await apis.loadShareList({ ...params });
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
    toClipboard(`分享“${record.name}”，访问地址：${record.url}`).then(() => {
      notification.success('成功复制到剪贴板');
    });
    return;
  }
  const [error, { data }] = await apis.loadShareInfo(record.id);
  if (error) {
    return;
  }
  record.url = data?.url;
  toClipboard(`分享“${record.name}”，访问地址：${record.url}`).then(() => {
    notification.success('成功复制到剪贴板');
  });
};

const handleEnterShare = async (shareId: string) => {
  const [error, { data }] = await apis.loadShareInfo(shareId);
  if (error) {
    return;
  }
  nextTick(() => {
    window.open(data.url);
  });
};

const columns = [
  {
    title: '名称',
    dataIndex: 'name',
    ellipsis: true,
    sorter: true
  },
  {
    title: '状态',
    dataIndex: 'isExpired',
    width: 80
    // customRender: ({text}) => {
    //   return text ? '已过期' : '未过期'
    // }
  },
  {
    title: '分享人',
    dataIndex: 'createdBy',
    width: 100,
    sorter: true,
    ellipsis: true,
    customRender: ({ record }) => {
      return record.createdByName;
    }
  },
  {
    title: '分享范围',
    dataIndex: 'shareScope',
    width: 100,
    customRender: ({ text }) => text?.message
  },
  {
    title: '分享日期',
    dataIndex: 'createdDate',
    width: 160,
    sorter: true
  },
  {
    title: '到期日期',
    dataIndex: 'expiredDate',
    width: 160,
    sorter: true
  },
  {
    title: '查看次数',
    dataIndex: 'viewNum',
    width: 80
  },
  {
    title: '备注',
    dataIndex: 'remark',
    ellipsis: true,
    width: 100,
  },
  {
    title: '最后修改人',
    dataIndex: 'lastModifiedByName',
    ellipsis: true,
    width: 100
  },
  {
    title: '最后修改时间',
    dataIndex: 'lastModifiedDate',
    width: 140
  },
  {
    title: '操作',
    dataIndex: 'actions',
    width: 200
  }
];
</script>

<template>
  <div class="flex flex-col h-full overflow-auto px-5 py-5 leading-5 text-3">
    <div class="flex space-x-1">
      <Introduce class="mb-5 flex-1" />
    </div>
    <div class="text-3.5 font-semibold mb-1">已添加的分享</div>
    <Spin :spinning="loading" class="flex-1 flex flex-col">
      <template v-if="loaded">
        <div v-if="!searchedFlag && dataList.length === 0" class="flex-1 flex flex-col items-center justify-center">
          <img src="../../../../assets/images/nodata.png">
          <div class="flex items-center text-theme-sub-content text-3.5 leading-5 space-x-1">
            <span>尚未添加任何分享，立即</span>
            <Button type="link" @click="editVersion">添加分享</Button>
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
                <template v-if="column.dataIndex === 'remark'">
                  <template v-if="record.remark">{{record.remark}}</template>
                  <span v-else class="text-sub-content">无~</span>
                </template>
                <template v-if="column.dataIndex === 'isExpired'">
                  <Tag :color="record.expired ? 'error' : 'success'">{{ record.expired ? '已过期' : '未过期' }}</Tag>
                  <!-- <span :class="[record.expired ? 'text-status-error1' : 'text-status-success']">{{ record.expired ? '已过期' : '未过期' }}</span> -->
                </template>
                <template v-if="column.dataIndex === 'actions'">
                  <Button
                    type="text"
                    size="small"
                    @click="copyLink(record)">
                    <Icon icon="icon-fuzhi" class="mr-1" />
                    复制
                  </Button>
                  <Button
                    type="text"
                    size="small"
                    @click="editVersion(record)">
                    <Icon icon="icon-bianji" class="mr-1" />
                    编辑
                  </Button>
                  <Button
                    type="text"
                    size="small"
                    @click="toDelete(record)">
                    <Icon icon="icon-qingchu" class="mr-1" />
                    删除
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
