<script setup lang="ts">
import { computed, inject, onMounted, ref, watch } from 'vue';
import { Button } from 'ant-design-vue';
import { HttpMethodText, Icon, modal, notification, Table } from '@xcan-angus/vue-ui';
import { utils } from '@xcan-angus/infra';
import { apis } from 'src/api/tester';

import { getCurrentPage } from '@/utils/utils';
import { ApiItem } from './PropsType';

type Props = {
  projectId: string;
  params: { createdBy?: string; favouriteBy?: boolean; followBy?: boolean; };
  total: number;
  notify: string;
  deletedNotify: string;
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  params: undefined,
  total: 0,
  notify: undefined,
  deletedNotify: undefined
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'update:total', value: number): void;
  (e: 'update:deletedNotify', value: string): void;
}>();

// eslint-disable-next-line @typescript-eslint/no-empty-function
const addTabPane = inject<(data: any) => void>('addTabPane', () => { });

// eslint-disable-next-line @typescript-eslint/no-empty-function
const deleteTabPane = inject<(data: any) => void>('deleteTabPane', () => { });

const updateRefreshNotify = inject<(value: string) => void>('updateRefreshNotify');

const tableData = ref<ApiItem[]>();
const loading = ref(false);
const loaded = ref(false);
const orderBy = ref<string>();
const orderSort = ref<'ASC' | 'DESC'>();
const pagination = ref<{
  total: number;
  current: number;
  pageSize: number;
  showSizeChanger: false;
  size: 'small';
  showTotal:(value: number) => string;
    }>({
      total: 0,
      current: 1,
      pageSize: 5,
      showSizeChanger: false,
      size: 'small',
      showTotal: (total: number) => {
        if (typeof pagination.value === 'object') {
          const totalPage = Math.ceil(total / pagination.value.pageSize);
          return `第${pagination.value.current}/${totalPage}页`;
        }
      }
    });

const toCreateApi = () => {
  addTabPane({ name: '添加API', value: 'API', _id: utils.uuid() + 'API' });
};

const openApi = (api) => {
  addTabPane({ name: api.apisName || api.summary, value: 'API', id: api.apisId || api.id, _id: (api.apisId || api.id) + 'API' });
};

const tableChange = ({ current = 1, pageSize = 10 }, _filters, sorter: { orderBy: string; orderSort: 'ASC' | 'DESC'; }) => {
  orderBy.value = sorter.orderBy;
  orderSort.value = sorter.orderSort;
  pagination.value.current = current;
  pagination.value.pageSize = pageSize;
  loadData();
};

const loadData = async () => {
  loading.value = true;
  const { current, pageSize } = pagination.value;
  const params: {
    projectId: string;
    pageNo: number;
    pageSize: number;
    createdBy?: string;
    favouriteBy?: boolean;
    followBy?: boolean;
    orderBy?: string;
    orderSort?: string;
  } = {
    projectId: props.projectId,
    pageNo: current,
    pageSize
  };

  if (orderSort.value) {
    params.orderBy = orderBy.value;
    params.orderSort = orderSort.value;
  }

  if (props.params) {
    if (props.params.createdBy) {
      params.createdBy = props.params.createdBy;
    }

    if (props.params.favouriteBy) {
      params.favouriteBy = props.params.favouriteBy;
    }

    if (props.params.followBy) {
      params.followBy = props.params.followBy;
    }
  }
  const [error, res] = await apis.getApiList(params);
  loading.value = false;
  loaded.value = true;
  if (error) {
    return;
  }

  const data = res?.data;
  tableData.value = data?.list;
  const total = +(data?.total || 0);
  pagination.value.total = total;
  emit('update:total', total);
};

const deleteHandler = (data: ApiItem) => {
  modal.confirm({
    content: `确定删除接口【${data.summary}】吗？`,
    async onOk () {
      const id = data.id;
      const params = { ids: [id] };
      const [error] = await apis.deleteApi(params);
      if (error) {
        return;
      }

      notification.success('删除接口成功');
      emit('update:deletedNotify', utils.uuid());

      // 删除已经打开的tabpane
      deleteTabPane([id + 'API', id + 'socket', id + 'execute']);

      if (typeof updateRefreshNotify === 'function') {
        updateRefreshNotify(utils.uuid());
      }
    }
  });
};

const cancelFavourite = async (data: ApiItem) => {
  loading.value = true;
  const [error] = await apis.cancelFavourite(data.id);
  loading.value = false;
  if (error) {
    return;
  }

  notification.success('接口取消收藏成功');
  loadData();

  if (typeof updateRefreshNotify === 'function') {
    updateRefreshNotify(utils.uuid());
  }
};

const cancelFollow = async (data: ApiItem) => {
  loading.value = true;
  const [error] = await apis.cancelFollow(data.id);
  loading.value = false;
  if (error) {
    return;
  }

  notification.success('接口取消关注成功');
  loadData();

  if (typeof updateRefreshNotify === 'function') {
    updateRefreshNotify(utils.uuid());
  }
};

onMounted(() => {
  watch(() => props.projectId, () => {
    loadData();
  }, { immediate: true });

  watch(() => props.notify, (newValue) => {
    if (newValue === undefined || newValue === null || newValue === '') {
      return;
    }

    loadData();
  }, { immediate: true });

  watch(() => props.deletedNotify, (newValue) => {
    if (newValue === undefined || newValue === null || newValue === '') {
      return;
    }

    pagination.value.current = getCurrentPage(pagination.value.current, pagination.value.pageSize, pagination.value.total);
    loadData();
  }, { immediate: true });
});

const columns = computed(() => {
  const _columns: {
    title: string;
    dataIndex: string;
    ellipsis?: boolean;
    sorter?: boolean;
    width?: string | number;
    actionKey?: 'createdBy' | 'favouriteBy' | 'followBy';
  }[] = [
    {
      title: 'ID',
      dataIndex: 'id',
      ellipsis: true,
      width: '16%'
    },
    {
      title: '名称',
      dataIndex: 'summary',
      ellipsis: true,
      sorter: true
    },
    {
      title: '路径',
      dataIndex: 'endpoint',
      ellipsis: true
    },
    {
      title: '状态',
      dataIndex: 'status',
      width: '7%'
    },
    {
      title: '添加时间',
      dataIndex: 'createdDate',
      ellipsis: true,
      sorter: true,
      width: '14%'
    }
  ];

  const actionColumn: {
    title: string;
    dataIndex: string;
    ellipsis?: boolean;
    sorter?: boolean;
    width?: string | number;
    actionKey?: 'favouriteBy' | 'followBy';
  } = {
    title: '操作',
    dataIndex: 'action',
    width: 50
  };

  const _params = props.params;
  if (_params) {
    if (_params.favouriteBy) {
      actionColumn.actionKey = 'favouriteBy';
    } else if (_params.followBy) {
      actionColumn.actionKey = 'followBy';
    }
  }

  _columns.push(actionColumn);

  return _columns;
});

const emptyTextStyle = {
  margin: '14px auto',
  height: 'auto'
};
</script>

<template>
  <div>
    <template v-if="loaded">
      <template v-if="!tableData?.length">
        <div class="flex-1 flex flex-col items-center justify-center">
          <img class="w-27.5" src="../../../../assets/images/nodata.png">
          <div class="flex items-center text-theme-sub-content text-3 leading-5">
            <template v-if="!!props.params?.createdBy">
              <span>尚未添加任何接口，立即</span>
              <Button
                type="link"
                size="small"
                class="py-0 px-1 h-5 leading-5"
                @click="toCreateApi">
                添加接口
              </Button>
            </template>

            <template v-else-if="!!props.params?.favouriteBy">
              <span>没有收藏的接口</span>
            </template>

            <template v-else-if="!!props.params?.followBy">
              <span>没有关注的接口</span>
            </template>
          </div>
        </div>
      </template>

      <Table
        v-else
        :dataSource="tableData"
        :columns="columns"
        :pagination="pagination"
        :loading="loading"
        :emptyTextStyle="emptyTextStyle"
        :minSize="5"
        rowKey="id"
        size="small"
        @change="tableChange">
        <template #bodyCell="{ record, column }">
          <div
            v-if="column.dataIndex === 'summary'"
            class="link truncate"
            :title="record.summary"
            @click="openApi(record)">
            {{ record.summary }}
          </div>

          <div
            v-else-if="column.dataIndex === 'endpoint'"
            :title="record.endpoint"
            class="truncate">
            <HttpMethodText :value="record.method" />
            <span class="truncate">{{ record.endpoint }}</span>
          </div>

          <div
            v-else-if="column.dataIndex === 'status'"
            :title="record.status?.message"
            :class="'api-status-' + record.status?.value"
            class="truncate">
            {{ record.status?.message }}
          </div>

          <div v-else-if="column.dataIndex === 'action'">
            <template v-if="column.actionKey === 'favouriteBy'">
              <Button
                title="取消收藏"
                size="small"
                type="text"
                class="space-x-1 flex items-center py-0 px-1"
                @click="cancelFavourite(record)">
                <Icon icon="icon-quxiaoshoucang" class="text-3.5 cursor-pointer text-theme-text-hover" />
              </Button>
            </template>

            <template v-else-if="column.actionKey === 'followBy'">
              <Button
                title="取消关注"
                size="small"
                type="text"
                class="space-x-1 flex items-center py-0 px-1"
                @click="cancelFollow(record)">
                <Icon icon="icon-quxiaoguanzhu" class="text-3.5 cursor-pointer text-theme-text-hover" />
              </Button>
            </template>

            <template v-else>
              <Button
                title="删除"
                size="small"
                type="text"
                class="space-x-1 flex items-center py-0 px-1"
                @click="deleteHandler(record)">
                <Icon icon="icon-qingchu" class="text-3.5 cursor-pointer text-theme-text-hover" />
              </Button>
            </template>
          </div>
        </template>
      </Table>
    </template>
  </div>
</template>

<style scoped>
.link {
  color: #1890ff;
  cursor: pointer;
}

.api-status-UNKNOWN {
  color: rgba(140, 140, 140, 100%);
}

.api-status-IN_DESIGN {
  color: rgba(255, 129, 0, 100%);
}

.api-status-IN_DEV {
  color: rgba(0, 119, 255, 100%);
}

.api-status-DEV_COMPLETED {
  color: rgba(82, 196, 26, 100%);
}

.api-status-RELEASED {
  color: rgb(56, 158, 13, 100%);
}

:deep(.ant-pagination) {
  margin-bottom: 0;
}
</style>
