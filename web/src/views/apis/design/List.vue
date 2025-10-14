<script setup lang="ts">
import { defineAsyncComponent, inject, onMounted, ref, watch } from 'vue';
import { Button, Tag } from 'ant-design-vue';
import { AsyncComponent, Dropdown, Icon, modal, NoData, notification, Spin, Table } from '@xcan-angus/vue-ui';
import { apis } from '@/api/tester';
import { useI18n } from 'vue-i18n';
import { ProjectPageQuery } from '@xcan-angus/infra';
import { ApiMenuKey } from '@/views/apis/menu';
import { BasicProps } from '@/types/types';
import { ApiDesignInfo } from './types';

const SearchPanel = defineAsyncComponent(() => import('@/views/apis/design/SearchPanel.vue'));
const Introduce = defineAsyncComponent(() => import('@/views/apis/design/Introduce.vue'));
const EditModal = defineAsyncComponent(() => import('@/views/apis/design/Edit.vue'));
const ExportModal = defineAsyncComponent(() => import('@/views/apis/design/Export.vue'));
const ImportModal = defineAsyncComponent(() => import('@/views/apis/design/Import.vue'));
const ImportServiceModal = defineAsyncComponent(() => import('@/views/apis/design/ImportService.vue'));

const { t } = useI18n();

const props = withDefaults(defineProps<BasicProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  notify: undefined
});

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

const dataList = ref<ApiDesignInfo[]>([]);
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

const toDelete = async (data: ApiDesignInfo) => {
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

      await loadData();
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
  const params: ProjectPageQuery = {
    projectId: props.projectId,
    pageNo: pagination.value.current,
    pageSize: pagination.value.pageSize,
    ...searchPanelParams.value
  };

  const [error, res] = await apis.getDesignList({ ...params });
  loaded.value = true;
  loading.value = false;

  searchedFlag.value = !!(params.filters?.length || params.orderBy);

  if (error) {
    pagination.value.total = 0;
    dataList.value = [];
    return;
  }

  const data = res?.data || { total: 0, list: [] };
  if (data) {
    pagination.value.total = +data.total;
    dataList.value = data.list || [] as ApiDesignInfo[];
  }
};

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
  await loadData();
};

const releaseDesign = async (record: {id: string; name: string; url?: string}) => {
  const [error] = await apis.releaseDesign(record.id);
  if (error) {
    return;
  }
  notification.success(t('actions.tips.publishSuccess'));
  await loadData();
};

const generateService = async (record: {id: string; name: string; url?: string}) => {
  const [error] = await apis.generateServiceFromDesign(record.id);
  if (error) {
    return;
  }
  notification.success(t('actions.tips.generateSuccess'));
  await loadData();
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

const columns = [
  {
    title: t('common.name'),
    dataIndex: 'name',
    ellipsis: true,
    sorter: true
  },
  {
    title: t('apiDesign.columns.openapiSpecVersion'),
    dataIndex: 'openapiSpecVersion',
    width: 120
  },
  {
    title: t('common.status'),
    dataIndex: 'released',
    width: 120
  },
  {
    title: t('common.source'),
    dataIndex: 'designSource',
    customRender: ({ text }) => {
      return text?.message || '--';
    },
    width: 130
  },
  {
    title: t('apiDesign.columns.designService'),
    dataIndex: 'designSourceName',
    ellipsis: true,
    width: 160
  },
  {
    title: t('common.creator'),
    dataIndex: 'createdByName',
    groupName: 'createdByName',
    sorter: true,
    width: 130
  },
  {
    title: t('common.createdDate'),
    dataIndex: 'createdDate',
    groupName: 'createdByName',
    hide: true,
    sorter: true,
    width: 130
  },
  {
    title: t('common.modifier'),
    dataIndex: 'lastModifiedByName',
    groupName: 'lastModifiedByName',
    ellipsis: true,
    width: 130
  },
  {
    title: t('common.lastModifiedDate'),
    dataIndex: 'lastModifiedDate',
    groupName: 'lastModifiedByName',
    hide: true,
    width: 130
  },
  {
    title: t('common.actions'),
    dataIndex: 'actions',
    width: 100
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
      name: t('apiDesign.actions.generateService'),
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
      <Introduce class="mb-2 flex-1" />
    </div>
    <div class="text-3.5 font-semibold mb-1">{{ t('apiDesign.addedDesigns') }}</div>
    <Spin :spinning="loading" class="flex-1 flex flex-col">
      <template v-if="loaded">
        <div v-if="!searchedFlag && dataList.length === 0" class="flex-1 flex flex-col items-center justify-center">
          <img src="../../../assets/images/nodata.png">
          <div class="flex items-center text-theme-sub-content text-3.5 leading-5 space-x-1">
            <span>{{ t('apiDesign.notAddedYet') }}</span>
            <Button type="link" @click="editDesign()">{{ t('apiDesign.actions.add') }}</Button>
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
              :minSize="5"
              rowKey="id"
              size="small"
              @change="tableChange">
              <template #bodyCell="{column, record}">
                <template v-if="column.dataIndex === 'name'">
                  <a class="text-blue-1 truncate" @click="handleEnterDesign(record)">{{ record.name }}</a>
                </template>
                <template v-if="column.dataIndex=== 'released'">
                  <Tag v-if="record.released" color="success">{{ t('apiDesign.messages.released') }}</Tag>
                  <Tag v-else color="default">{{ t('apiDesign.messages.unreleased') }}</Tag>
                </template>
                <template v-if="column.dataIndex === 'designSourceName'">
                  <RouterLink
                    v-if="record.designSourceId"
                    class="text-blue-1"
                    :to="`/apis#${ApiMenuKey.SERVICES}?id=${record.designSourceId}&value=group&name=${record.designSourceName}`">
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
                    {{ t('apiDesign.actions.design') }}
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
