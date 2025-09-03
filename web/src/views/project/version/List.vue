<script setup lang="ts">
import { defineAsyncComponent, inject, onMounted, ref, watch } from 'vue';
import { Button, Progress, Tag } from 'ant-design-vue';
import { AsyncComponent, Dropdown, Icon, modal, NoData, notification, Spin, Table } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import { software } from '@/api/tester';

import { VersionInfo } from './types';
import SearchPanel from '@/views/project/version/SearchPanel.vue';

type Props = {
  projectId: string;
  userInfo: { id: string; };
  appInfo: { id: string; };
  notify: string;
  showDetail: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  notify: undefined,
  showDetail: true
});

const { t } = useI18n();

type OrderByKey = 'createdDate' | 'createdByName';
type OrderSortKey = 'ASC' | 'DESC';

const Introduce = defineAsyncComponent(() => import('@/views/project/version/Introduce.vue'));
const Edit = defineAsyncComponent(() => import('@/views/project/version/Edit.vue'));
const Merge = defineAsyncComponent(() => import('@/views/project/version/Merge.vue'));

const deleteTabPane = inject<(keys: string[]) => void>('deleteTabPane', () => ({}));

const loaded = ref(false);
const loading = ref(false);
const searchedFlag = ref(false);
const editVisible = ref(false);
const selectVersionId = ref();
const mergeVisible = ref(false);

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

const dataList = ref<VersionInfo[]>([]);
const permissionsMap = ref<Map<string, string[]>>(new Map());

const refresh = () => {
  pagination.value.current = 1;
  permissionsMap.value.clear();
  loadData();
};

const searchChange = (data) => {
  pagination.value.current = 1;
  searchPanelParams.value = data;
  loadData();
};

const toDelete = async (data: VersionInfo) => {
  modal.confirm({
    content: t('version.messages.deleteConfirm', { name: data.name }),
    async onOk () {
      const id = data.id;
      const [error] = await software.deleteSoftwareVersion([id]);
      if (error) {
        return;
      }
      notification.success(t('version.messages.deleteSuccess'));
      if (pagination.value.current > 1 && dataList.value.length === 1) {
        pagination.value.current -= 1;
      }

      await loadData();
      deleteTabPane([id]);
    }
  });
};

const paginationChange = (page) => {
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

  const [error, res] = await software.getSoftwareVersionList({ ...params });
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
    const _list = (data.list || [] as VersionInfo[]);
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
  selectVersionId.value = record.id;
  editVisible.value = true;
};

const toMerge = () => {
  mergeVisible.value = true;
};

const changeStatus = async (status, record) => {
  modal.confirm({
    content: t('version.messages.changeStatusConfirm', { status: status.name }),
    async onOk () {
      const [error] = await software.updateSoftwareVersionStatus(record.id, {
        status: status.key
      });
      if (error) {
        return;
      }
      notification.success(t('version.messages.editSuccess'));
      loadData();
    }
  });
};

const handleMergeOk = (formId: string) => {
  if (pagination.value.current > 1 && dataList.value.length === 1) {
    pagination.value.current -= 1;
  }
  deleteTabPane([formId + '-detail']);
  loadData();
};

const columns = [
  {
    title: t('version.columns.version'),
    dataIndex: 'name',
    width: 100
  },
  {
    title: t('version.columns.status'),
    dataIndex: 'status',
    width: 100
  },
  {
    title: t('version.columns.progress'),
    dataIndex: 'progress',
    width: 200
  },
  {
    title: t('version.columns.startDate'),
    dataIndex: 'startDate',
    customRender: ({ text }) => text || '--',
    width: 150
  },
  {
    title: t('version.columns.releaseDate'),
    dataIndex: 'releaseDate',
    customRender: ({ text }) => text || '--',
    width: 150
  },
  {
    title: t('version.columns.description'),
    dataIndex: 'description',
    width: 200
  },
  {
    title: t('version.columns.lastModifier'),
    dataIndex: 'lastModifiedByName',
    groupName: 'person',
    width: 100
  },
  {
    title: t('version.columns.creator'),
    dataIndex: 'createdByName',
    groupName: 'person',
    hide: true,
    width: 100
  },
  {
    title: t('version.columns.lastModifyTime'),
    dataIndex: 'lastModifiedDate',
    groupName: 'date',
    width: 100
  },
  {
    title: t('version.columns.createTime'),
    dataIndex: 'createdDate',
    groupName: 'date',
    hide: true,
    width: 100
  },
  {
    title: t('version.columns.actions'),
    dataIndex: 'actions',
    width: 150
  }
];

const statusColorConfig = {
  ARCHIVED: 'default',
  NOT_RELEASED: 'processing',
  RELEASED: 'success'
};

const getMenus = (record) => {
  return [
    record.status?.value !== 'NOT_RELEASED' && {
      key: 'NOT_RELEASED',
      name: t('version.status.notReleased'),
      icon: 'icon-baocundaoweiguidang'
    },
    record.status?.value !== 'RELEASED' && {
      key: 'RELEASED',
      name: t('version.status.released'),
      icon: 'icon-fabu'
    },
    record.status?.value !== 'ARCHIVED' && {
      key: 'ARCHIVED',
      name: t('version.status.archived'),
      icon: 'icon-weiguidang'
    }
  ].filter(Boolean);
};

</script>

<template>
  <div class="flex flex-col h-full overflow-auto px-5 py-5 leading-5 text-3">
    <div class="flex space-x-2">
      <Introduce class="flex-1" :class="{'mb-2': props.showDetail, 'mb-2': !props.showDetail}" :showFunc="props.showDetail" />
    </div>

    <div class="flex items-center space-x-2">
      <span class="text-3.5 font-semibold mb-1.5">{{ t('version.list.addedVersions') }}</span>
    </div>
    <Spin :spinning="loading" class="flex-1 flex flex-col">
      <template v-if="loaded">
        <div v-if="!searchedFlag && dataList.length === 0" class="flex-1 flex flex-col items-center justify-center">
          <img src="../../../assets/images/nodata.png">
          <div class="flex items-center text-theme-sub-content text-3.5 leading-5 space-x-1">
            <span>{{ t('version.list.noVersions') }}</span>
            <Button type="link" @click="editVersion">{{ t('version.actions.addVersion') }}</Button>
          </div>
        </div>

        <template v-else>
          <SearchPanel
            @change="searchChange"
            @refresh="refresh"
            @toMerge="toMerge"
            @add="editVersion" />
          <NoData v-if="dataList.length === 0" class="flex-1" />
          <template v-else>
            <Table
              :columns="columns"
              :dataSource="dataList"
              :pagination="pagination"
              @change="paginationChange">
              <template #bodyCell="{column, record}">
                <template v-if="column.dataIndex === 'name'">
                  <RouterLink
                    v-if="props.showDetail"
                    class="router-link"
                    :title="record.name"
                    :to="`/task#version?id=${record.id}`">
                    {{ record.name }}
                  </RouterLink>
                </template>
                <template v-if="column.dataIndex === 'status'">
                  <Tag :color="statusColorConfig[record.status?.value]">
                    {{ record.status?.message }}
                  </Tag>
                </template>
                <template v-if="column.dataIndex === 'description'">
                  <template v-if="record.description">{{ record.description }}</template>
                  <span v-else class="text-text-sub-content">{{ t('version.list.noDescription') }}</span>
                </template>
                <template v-if="column.dataIndex === 'actions'">
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
                  <Dropdown
                    noAuth
                    :menuItems="getMenus(record)"
                    @click="changeStatus($event, record)">
                    <Button size="small" type="text">
                      <Icon icon="icon-gengduo" />
                    </Button>
                  </Dropdown>
                </template>
                <template v-if="column.dataIndex === 'progress'">
                  <Progress size="small" :percent="record.progress?.completedRate || 0" />
                </template>
              </template>
            </Table>
          </template>
        </template>
      </template>
    </Spin>
    <AsyncComponent :visible="editVisible">
      <Edit
        v-model:visible="editVisible"
        :versionId="selectVersionId"
        :projectId="props.projectId"
        @ok="loadData" />
    </AsyncComponent>
    <AsyncComponent :visible="mergeVisible">
      <Merge
        v-model:visible="mergeVisible"
        :projectId="props.projectId"
        @ok="handleMergeOk" />
    </AsyncComponent>
  </div>
</template>

<style scoped>
.PENDING {
  background-color: rgba(45, 142, 255, 100%);
}

.IN_PROGRESS {
  background-color: rgba(103, 215, 255, 100%);
}

.COMPLETED {
  background-color: rgba(82, 196, 26, 100%);
}

.BLOCKED {
  background-color: rgba(255, 165, 43, 100%);
}

:deep(.ant-progress-outer) {
  width: 100px;
}

.router-link,
.link {
  color: #1890ff;
  cursor: pointer;
}

.link:hover {
  border-radius: 4px;
  background-color: rgba(239, 240, 243, 100%);
}

:deep(.ant-popover-inner-content) {
  padding: 8px 4px;
}
</style>
