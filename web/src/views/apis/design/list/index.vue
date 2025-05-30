<script setup lang="ts">
import { defineAsyncComponent, inject, onMounted, ref, watch } from 'vue';
import { Button, Tag } from 'ant-design-vue';
import {AsyncComponent, Icon, modal, NoData, notification, Spin, Table, Image, Dropdown} from '@xcan-angus/vue-ui';
import { apis } from '@/api/tester';

import { DesignInfo } from '../PropsType';
import SearchPanel from '@/views/apis/design/list/searchPanel/index.vue';

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

const Introduce = defineAsyncComponent(() => import('@/views/apis/design/list/introduce/index.vue'));
const EditModal = defineAsyncComponent(() => import('@/views/apis/design/edit/index.vue'));
const ExportModal = defineAsyncComponent(() => import('@/views/apis/design/export/index.vue'));
const ImportModal = defineAsyncComponent(() => import('@/views/apis/design/import/index.vue'));
const ImportServiceModal = defineAsyncComponent(() => import('@/views/apis/design/importService/index.vue'));

const deleteTabPane = inject<(keys: string[]) => void>('deleteTabPane', () => ({}));
const addTabPane = inject<(keys: string[]) => void>('addTabPane', () => ({}));

const loaded = ref(false);
const loading = ref(false);
const searchedFlag = ref(false);
const editVisible = ref(false);
const selectDesignId = ref();
const exportVisible = ref(false);
const importVisible = ref(false);
const importServiceVisible = ref(false);

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

const dataList = ref<DesignInfo[]>([]);
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

const toDelete = async (data: DesignInfo) => {
  modal.confirm({
    content: `确定删除设计【${data.name}】吗？`,
    async onOk () {
      const id = data.id;
      const [error] = await apis.deleteDesign([id]);
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

  const [error, res] = await apis.getDesignList({ ...params });
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
    const _list = data.list || [] as DesignInfo[];
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

const importDesign = () => {
  importVisible.value = true;
};

const importService = () => {
  importServiceVisible.value = true;
};

const editDesign = (record: {id?: string} = {}) => {
  selectDesignId.value = record?.id;
  editVisible.value = true;
};

const exportDesign = (record: {id: string; name: string; url?: string})  => {
  selectDesignId.value = record.id;
  exportVisible.value = true;
}

const cloneDesign = async (record: {id: string; name: string; url?: string})  => {
  const [error] = await apis.cloneDesign(record.id);
  if (error) {
    return;
  }
  notification.success('克隆成功');
  loadData();
}

const releaseDesign = async (record: {id: string; name: string; url?: string})  => {
  const [error] = await apis.releaseDesign(record.id);
  if (error) {
    return;
  }
  notification.success('发布成功');
  loadData();
}

const generateService = async (record: {id: string; name: string; url?: string})  => {
  const [error] = await apis.generateServiceFromDesign(record.id);
  if (error) {
    return;
  }
  notification.success('已生成服务');
  loadData();
}

const handleEnterDesign = async (record) => {
  addTabPane({
    _id: record.id + '-doc',
    designId: record.id,
    value: 'designDocContent',
    name: record.name,
    data: { _id: record.id, ...record }
  })
};

const handleDesign = (record: {id: string; name: string; url?: string}, key) => {
  switch (key) {
    case 'clone':
      return cloneDesign(record);
    case 'publish':
      return releaseDesign(record);
    case 'generate':
      return generateService(reocrd);
    case 'delete':
      return toDelete(reocrd);
    default:
      return;
  }
}

const handleImportOk = () => {
  loadData();
};

const columns = [
  {
    title: '名称',
    dataIndex: 'name',
    ellipsis: true,
    sorter: true,
    width: 100
  },
  {
    title: '规范版本',
    dataIndex: 'openapiSpecVersion',
    width: 60
  },
  {
    title: '状态',
    dataIndex: 'released',
    width: 100
  },
  {
    title: '来源',
    dataIndex: 'designSource',
    width: 100,
    customRender: ({text}) => {
      return text?.message || '--';
    }
  },
  {
    title: '关联服务',
    dataIndex: 'designSourceName',
    width: 100,
    ellipsis: true,
    customRender: ({text}) => {
      return text || '--';
    }
  },
  {
    title: '添加人',
    dataIndex: 'createdByAvatar',
    width: 100,
    sorter: true
  },
  {
    title: '添加时间',
    dataIndex: 'createdDate',
    width: 140,
    sorter: true
  },
  {
    title: '最后修改人',
    dataIndex: 'lastModifiedByName',
    width: 100,
    ellipsis: true,
  },
  {
    title: '最后修改时间',
    dataIndex: 'lastModifiedDate',
    width: 140
  },
  {
    title: '操作',
    dataIndex: 'actions',
    width: 160
  }
];
const moreButton = (record) => {
  return [
  {
    key: 'clone',
    name: '克隆',
    icon: 'icon-fuzhi'
  },
  {
    key: 'export',
    name: '导出',
    icon: 'icon-daochu1'
  },
  {
    key: 'publish',
    name: '发布',
    icon: 'icon-fabu'
  },
  {
    key: 'generate',
    name: '生成服务',
    icon: 'icon-fuwu',
    disabled: !!record.designSourceId
  },
  {
    key: 'delete',
    name: '删除',
    icon: 'icon-qingchu'
  }
]};
</script>

<template>
  <div class="flex flex-col h-full overflow-auto px-5 py-5 leading-5 text-3">
    <div class="flex space-x-1">
      <Introduce class="mb-5 flex-1" />
    </div>
    <div class="text-3.5 font-semibold mb-1">已添加的设计</div>
    <Spin :spinning="loading" class="flex-1 flex flex-col">
      <template v-if="loaded">
        <div v-if="!searchedFlag && dataList.length === 0" class="flex-1 flex flex-col items-center justify-center">
          <img src="../../../../assets/images/nodata.png">
          <div class="flex items-center text-theme-sub-content text-3.5 leading-5 space-x-1">
            <span>尚未添加任何设计，立即</span>
            <Button type="link" @click="editDesign">添加设计</Button>
          </div>
        </div>

        <template v-else>
          <SearchPanel
            @change="searchChange"
            @refresh="refresh"
            @add="editDesign"
            @import="importDesign"
            @importService="importService"/>
          <NoData v-if="dataList.length === 0" class="flex-1" />
          <template v-else>
            <Table
              :columns="columns"
              :dataSource="dataList"
              :pagination="pagination"
              @change="tableChange">
              <template #bodyCell="{column, record}">
                <template v-if="column.dataIndex === 'name'">
                  <a class="text-blue-1 truncate" @click="handleEnterDesign(record)">{{ record.name }}</a>
                </template>
                <template v-if="column.dataIndex === 'createdByAvatar'">
                  <div class="inline-flex items-center truncate">
                    <Image type="avatar" class="w-6 rounded-full mr-1" :src="record.createdByAvatar" />
                    <span class="flex-1 truncate" :tite="record.createdByName">{{record.createdByName}}</span>
                  </div>
                </template>
                <template v-if="column.dataIndex=== 'released'">
                  <Tag v-if="record.released" color="success">已发布</Tag>
                  <Tag v-else color="default">草稿</Tag>
                </template>
                <template v-if="column.dataIndex === 'actions'">
                  <Button
                    type="text"
                    size="small"
                    @click="editDesign(record)">
                    <Icon icon="icon-bianji"  />
                    编辑
                  </Button>
                  <Button
                    type="text"
                    size="small"
                    @click="handleEnterDesign(record)">
                    <Icon icon="icon-sheji" />
                    设计
                  </Button>
                  <Dropdown :menuItems="moreButton(record)" @click="handleDesign(record, $event.key)">
                    <Icon icon="icon-gengduo" class="ml-1" />
                  </Dropdown>
<!--                  <Button-->
<!--                    type="text"-->
<!--                    size="small"-->
<!--                    @click="cloneDesign(record)">-->
<!--                    <Icon icon="icon-fuzhi" />-->
<!--                    克隆-->
<!--                  </Button>-->
<!--                  <Button-->
<!--                    type="text"-->
<!--                    size="small"-->
<!--                    @click="exportDesign(record)">-->
<!--                    <Icon icon="icon-daochu1"  />-->
<!--                    导出-->
<!--                  </Button>-->
<!--                  <Button-->
<!--                    type="text"-->
<!--                    size="small"-->
<!--                    @click="releaseDesign(record)">-->
<!--                    <Icon icon="icon-fabu"  />-->
<!--                    发布-->
<!--                  </Button>-->
<!--                  <Button-->
<!--                    type="text"-->
<!--                    size="small"-->
<!--                    :disabled="!!record.designSourceId"-->
<!--                    @click="generateService(record)">-->
<!--                    <Icon icon="icon-fuwu" />-->
<!--                    生成服务-->
<!--                  </Button>-->
<!--                  <Button-->
<!--                    type="text"-->
<!--                    size="small"-->
<!--                    @click="toDelete(record)">-->
<!--                    <Icon icon="icon-qingchu" class="mr-1" />-->
<!--                    删除-->
<!--                  </Button>-->
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
        :designId="selectDesignId"
        :projectId="props.projectId"
        @ok="loadData" />
    </AsyncComponent>
    <AsyncComponent :visible="exportVisible">
      <ExportModal
        v-model:visible="exportVisible"
        :designId="selectDesignId" />
    </AsyncComponent>
    <AsyncComponent :visible="importVisible">
      <ImportModal
        v-model:visible="importVisible"
        :projectId="props.projectId"
        @ok="handleImportOk"/>
    </AsyncComponent>
    <AsyncComponent :visible="importServiceVisible">
      <ImportServiceModal
        v-model:visible="importServiceVisible"
        :projectId="props.projectId"
        @ok="handleImportOk"/>
    </AsyncComponent>
  </div>
</template>
