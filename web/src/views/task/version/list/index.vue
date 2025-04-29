<script setup lang="ts">
import { defineAsyncComponent, inject, onMounted, ref, watch } from 'vue';
import { Button, Progress, Tag } from 'ant-design-vue';
import { AsyncComponent, Dropdown, Icon, modal, NoData, notification, Spin, Table } from '@xcan-angus/vue-ui';
import { software } from '@/api/altester';

import { VersionInfo } from '../PropsType';
import SearchPanel from '@/views/task/version/list/searchPanel/index.vue';

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

type OrderByKey = 'createdDate' | 'createdByName';
type OrderSortKey = 'ASC' | 'DESC';

const Introduce = defineAsyncComponent(() => import('@/views/task/version/list/introduce/index.vue'));
const Edit = defineAsyncComponent(() => import('@/views/task/version/edit/index.vue'));
const Merge = defineAsyncComponent(() => import('@/views/task/version/merge/index.vue'));

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
    content: `确定删除版本【${data.name}】吗？`,
    async onOk () {
      const id = data.id;
      const [error] = await software.delete([id]);
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

  const [error, res] = await software.searchList({ ...params });
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
    content: `确认修改版本状态为 “${status.name}” 吗？`,
    async onOk () {
      const [error] = await software.updateStatus(record.id, {
        status: status.key
      });
      if (error) {
        return;
      }
      notification.success('修改成功');
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
    title: '版本',
    dataIndex: 'name',
    width: 80
  },
  {
    title: '状态',
    dataIndex: 'status',
    width: 100
  },
  {
    title: '进度',
    dataIndex: 'progress',
    width: 180
  },
  {
    title: '开始日期',
    dataIndex: 'startDate',
    customRender: ({ text }) => text || '--'

  },
  {
    title: '发布日期',
    dataIndex: 'releaseDate',
    customRender: ({ text }) => text || '--'

  },
  {
    title: '描述',
    dataIndex: 'description',
    width: '14%'
  },
  {
    title: '最后修改人',
    dataIndex: 'lastModifiedByName',
    groupName: 'person'

  },
  {
    title: '添加人',
    dataIndex: 'createdByName',
    groupName: 'person',
    hide: true
  },
  {
    title: '最后修改时间',
    dataIndex: 'lastModifiedDate',
    groupName: 'date'

  },
  {
    title: '添加时间',
    dataIndex: 'createdDate',
    groupName: 'date',
    hide: true

  },
  {
    title: '操作',
    dataIndex: 'actions',
    width: 180
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
      name: '未发布',
      icon: 'icon-baocundaoweiguidang'
    },
    record.status?.value !== 'RELEASED' && {
      key: 'RELEASED',
      name: '发布',
      icon: 'icon-fabu'
    },
    record.status?.value !== 'ARCHIVED' && {
      key: 'ARCHIVED',
      name: '归档',
      icon: 'icon-weiguidang'
    }
  ].filter(Boolean);
};

</script>

<template>
  <div class="flex flex-col h-full overflow-auto px-5 py-5 leading-5 text-3">
    <div class="flex space-x-2">
      <Introduce class="mb-7 flex-1" :showFunc="props.showDetail" />
    </div>

    <div class="text-3.5 font-semibold mb-1">已添加的版本</div>
    <Spin :spinning="loading" class="flex-1 flex flex-col">
      <template v-if="loaded">
        <div v-if="!searchedFlag && dataList.length === 0" class="flex-1 flex flex-col items-center justify-center">
          <img src="../../../../assets/images/nodata.png">
          <div class="flex items-center text-theme-sub-content text-3.5 leading-5 space-x-1">
            <span>您尚未添加任何版本，立即</span>
            <Button type="link" @click="editVersion">添加版本</Button>
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
                  <span v-else class="text-text-sub-content">无描述~</span>
                </template>
                <template v-if="column.dataIndex === 'actions'">
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
