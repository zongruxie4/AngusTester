<script setup lang="ts">
import { defineAsyncComponent, inject, onMounted, ref, watch } from 'vue';
import { Button, Tag } from 'ant-design-vue';
import { AsyncComponent, Icon, modal, NoData, notification, Spin, Table, Image, Dropdown } from '@xcan-angus/vue-ui';
import { apis } from '@/api/tester';
import { useI18n } from 'vue-i18n';

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
const { t } = useI18n();
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
    content: t('actions.tips.confirmDelete', { name: data.name }),
    async onOk () {
      const id = data.id;
      const [error] = await apis.deleteDesign([id]);
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

const exportDesign = (record: {id: string; name: string; url?: string}) => {
  selectDesignId.value = record.id;
  exportVisible.value = true;
};

const cloneDesign = async (record: {id: string; name: string; url?: string}) => {
  const [error] = await apis.cloneDesign(record.id);
  if (error) {
    return;
  }
  notification.success(t('actions.tips.cloneSuccess'));
  loadData();
};

const releaseDesign = async (record: {id: string; name: string; url?: string}) => {
  const [error] = await apis.releaseDesign(record.id);
  if (error) {
    return;
  }
  notification.success(t('design.detail.publishSuccess'));
  loadData();
};

const generateService = async (record: {id: string; name: string; url?: string}) => {
  const [error] = await apis.generateServiceFromDesign(record.id);
  if (error) {
    return;
  }
  notification.success(t('design.home.generated'));
  loadData();
};

const handleEnterDesign = async (record) => {
  addTabPane({
    _id: record.id + '-doc',
    designId: record.id,
    value: 'designDocContent',
    name: record.name,
    data: { _id: record.id, ...record }
  });
};

const handleDesign = (record: {id: string; name: string; url?: string}, key) => {
  switch (key) {
    case 'clone':
      return cloneDesign(record);
    case 'publish':
      return releaseDesign(record);
    case 'generate':
      return generateService(record);
    case 'delete':
      return toDelete(record);
    case 'export':
      exportDesign(record);
      break;
    default:
  }
};

const handleImportOk = () => {
  loadData();
};

const columns = [
  {
    title: t('design.home.columns.name'),
    dataIndex: 'name',
    ellipsis: true,
    sorter: true,
    width: '16%'
  },
  {
    title: t('design.home.columns.openapiSpecVersion'),
    dataIndex: 'openapiSpecVersion',
    width: '8%'
  },
  {
    title: t('design.home.columns.status'),
    dataIndex: 'released',
    width: '8%'
  },
  {
    title: t('design.home.columns.designSource'),
    dataIndex: 'designSource',
    width: '8%',
    customRender: ({ text }) => {
      return text?.message || '--';
    }
  },
  {
    title: t('design.home.columns.designService'),
    dataIndex: 'designSourceName',
    width: '10%',
    ellipsis: true
  },
  {
    title: t('design.home.columns.createdBy'),
    dataIndex: 'createdByAvatar',
    width: '10%',
    sorter: true
  },
  {
    title: t('design.home.columns.createdDate'),
    dataIndex: 'createdDate',
    width: '10%',
    sorter: true
  },
  {
    title: t('design.home.columns.lastModifiedBy'),
    dataIndex: 'lastModifiedByName',
    width: '10%',
    groupName: 'lastModifiedByName',
    ellipsis: true
  },
  {
    title: t('design.home.columns.lastModifiedDate'),
    dataIndex: 'lastModifiedDate',
    groupName: 'lastModifiedByName',
    hide: true,
    width: '10%'
  },
  {
    title: t('common.actions'),
    dataIndex: 'actions',
    width: '12%'
  }
];
const moreButton = (record) => {
  return [
    {
      key: 'clone',
      name: t('actions.clone'),
      icon: 'icon-fuzhi'
    },
    {
      key: 'export',
      name: t('actions.export'),
      icon: 'icon-daochu1'
    },
    {
      key: 'publish',
      name: t('actions.publish'),
      icon: 'icon-fabu'
    },
    {
      key: 'generate',
      name: t('design.home.generateService_action'),
      icon: 'icon-fuwu',
      disabled: !!record.designSourceId
    },
    {
      key: 'delete',
      name: t('actions.delete'),
      icon: 'icon-qingchu'
    }
  ];
};
</script>

<template>
  <div class="flex flex-col h-full overflow-auto px-5 py-5 leading-5 text-3">
    <div class="flex space-x-1">
      <Introduce class="mb-5 flex-1" />
    </div>
    <div class="text-3.5 font-semibold mb-1">{{ t('design.home.title') }}</div>
    <Spin :spinning="loading" class="flex-1 flex flex-col">
      <template v-if="loaded">
        <div v-if="!searchedFlag && dataList.length === 0" class="flex-1 flex flex-col items-center justify-center">
          <img src="../../../../assets/images/nodata.png">
          <div class="flex items-center text-theme-sub-content text-3.5 leading-5 space-x-1">
            <span>{{ t('design.home.noDataTip') }}</span>
            <Button type="link" @click="editDesign()">{{ t('design.home.add_action') }}</Button>
          </div>
        </div>

        <template v-else>
          <SearchPanel
            @change="searchChange"
            @refresh="refresh"
            @add="editDesign"
            @import="importDesign"
            @importService="importService" />
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
                    <Image
                      type="avatar"
                      class="w-6 rounded-full mr-1"
                      :src="record.createdByAvatar" />
                    <span class="flex-1 truncate" :tite="record.createdByName">{{ record.createdByName }}</span>
                  </div>
                </template>
                <template v-if="column.dataIndex=== 'released'">
                  <Tag v-if="record.released" color="success">{{ t('design.home.released') }}</Tag>
                  <Tag v-else color="default">{{ t('design.home.unreleased') }}</Tag>
                </template>
                <template v-if="column.dataIndex === 'designSourceName'">
                  <RouterLink
                    v-if="record.designSourceId"
                    class="text-blue-1"
                    :to="`/apis#services?id=${record.designSourceId}&value=group&name=${record.designSourceName}`">
                    {{ record.designSourceName }}
                  </RouterLink>
                  <template v-else>
                    --
                  </template>
                </template>
                <template v-if="column.dataIndex === 'actions'">
                  <Button
                    type="text"
                    size="small"
                    @click="editDesign(record)">
                    <Icon icon="icon-bianji" />
                    {{ t('actions.edit') }}
                  </Button>
                  <Button
                    type="text"
                    size="small"
                    @click="handleEnterDesign(record)">
                    <Icon icon="icon-sheji" />
                    {{ t('design.home.design_action') }}
                  </Button>
                  <Dropdown :menuItems="moreButton(record)" @click="handleDesign(record, $event.key)">
                    <Icon icon="icon-gengduo" class="ml-1" />
                  </Dropdown>
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
        @ok="handleImportOk" />
    </AsyncComponent>
    <AsyncComponent :visible="importServiceVisible">
      <ImportServiceModal
        v-model:visible="importServiceVisible"
        :projectId="props.projectId"
        @ok="handleImportOk" />
    </AsyncComponent>
  </div>
</template>
