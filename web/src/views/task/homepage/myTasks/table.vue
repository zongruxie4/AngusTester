<script setup lang="ts">
import { computed, inject, onMounted, ref, watch } from 'vue';
import { Button } from 'ant-design-vue';
import { Icon, IconTask, modal, notification, Table, TaskPriority, TaskStatus } from '@xcan-angus/vue-ui';
import { http, utils } from '@xcan-angus/tools';
import { task } from '@/api/tester';

import { getCurrentPage } from '@/utils/utils';
import { TaskInfo } from '../../PropsType';

type Props = {
  projectId: string;
  params: {
    createdBy?: string;
    favouriteBy?: boolean;
    followBy?: boolean;
    confirmorId?: string;
    assigneeId?: string;
    commentBy?: string;
    status?: 'CONFIRMING' | 'PENDING' | 'COMPLETED';
  };
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

const updateRefreshNotify = inject<(value: string) => void>('updateRefreshNotify');

const tableData = ref<TaskInfo[]>();
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

const tableChange = ({ current = 1, pageSize = 10 }, _filters, sorter: { orderBy: string; orderSort: 'ASC' | 'DESC'; }) => {
  orderBy.value = sorter.orderBy;
  orderSort.value = sorter.orderSort;
  pagination.value.current = current;
  pagination.value.pageSize = pageSize;
  loadData();
};

const getParams = () => {
  const { current, pageSize } = pagination.value;
  const params: {
    backlogFlag: false;
    projectId: string;
    pageNo: number;
    pageSize: number;
    createdBy?: string;
    favouriteBy?: boolean;
    followBy?: boolean;
    confirmorId?: string;
    assigneeId?: string;
    status?: 'CONFIRMING' | 'PENDING' | 'COMPLETED';
    orderBy?: string;
    orderSort?: string;
    commentBy?: string;
  } = {
    backlogFlag: false,
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

    if (props.params.commentBy) {
      params.commentBy = props.params.commentBy;
    }

    if (props.params.assigneeId) {
      params.assigneeId = props.params.assigneeId;
    }

    if (props.params.confirmorId) {
      params.confirmorId = props.params.confirmorId;
    }

    if (props.params.status) {
      params.status = props.params.status;
    }
  }

  return params;
};

const loadData = async () => {
  const params = getParams();
  loading.value = true;
  const [error, res] = await task.loadTaskList(params);
  loading.value = false;
  loaded.value = true;
  if (error) {
    return;
  }

  const data = (res?.data || { total: 0, list: [] }) as { total: string; list: TaskInfo[] };
  const total = +data.total;
  pagination.value.total = total;
  emit('update:total', total);

  const pageNo = +params.pageNo;
  const pageSize = +params.pageSize;
  tableData.value = (data.list || []).map((item, index) => {
    const _params = {
      ...params,
      taskId: item.id,
      pageNo: (pageNo - 1) * pageSize + index + 1,
      pageSize: 1,
      total
    };

    return {
      ...item,
      linkUrl: '/task#task?' + http.getURLSearchParams(_params, true)
    };
  });
};

const deleteHandler = (data: TaskInfo) => {
  modal.confirm({
    content: `确定删除任务【${data.name}】吗？`,
    async onOk () {
      const [error] = await task.deleteTask([data.id]);
      if (error) {
        return;
      }

      notification.success('删除任务成功');
      emit('update:deletedNotify', utils.uuid());

      if (typeof updateRefreshNotify === 'function') {
        updateRefreshNotify(utils.uuid());
      }
    }
  });
};

const cancelFavourite = async (data: TaskInfo) => {
  loading.value = true;
  const [error] = await task.cancelFavouriteTask(data.id);
  loading.value = false;
  if (error) {
    return;
  }

  notification.success('任务取消收藏成功');
  loadData();

  if (typeof updateRefreshNotify === 'function') {
    updateRefreshNotify(utils.uuid());
  }
};

const cancelFollow = async (data: TaskInfo) => {
  loading.value = true;
  const [error] = await task.cancelFollowTask(data.id);
  loading.value = false;
  if (error) {
    return;
  }

  notification.success('任务取消关注成功');
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
  }[] = Object.prototype.hasOwnProperty.call(props.params, 'status')
    ? [
        {
          title: '编码',
          dataIndex: 'code',
          ellipsis: true,
          width: '12%'
        },
        {
          title: '名称',
          dataIndex: 'name',
          ellipsis: true,
          sorter: true,
          width: '37%'
        },
        {
          title: '所属迭代',
          dataIndex: 'sprintName',
          ellipsis: true,
          width: '25%'
        },
        {
          title: '优先级',
          dataIndex: 'priority',
          ellipsis: true,
          sorter: true,
          width: '9%'
        },
        {
          title: '截止时间',
          dataIndex: 'deadlineDate',
          ellipsis: true,
          sorter: true,
          width: '17%'
        }
      ]
    : [
        {
          title: '编码',
          dataIndex: 'code',
          ellipsis: true,
          width: '12%'
        },
        {
          title: '名称',
          dataIndex: 'name',
          ellipsis: true,
          sorter: true,
          width: '32%'
        },
        {
          title: '所属迭代',
          dataIndex: 'sprintName',
          ellipsis: true,
          width: '21%'
        },
        {
          title: '优先级',
          dataIndex: 'priority',
          ellipsis: true,
          sorter: true,
          width: '9%'
        },
        {
          title: '状态',
          dataIndex: 'status',
          ellipsis: true,
          width: '9%'
        },
        {
          title: '截止时间',
          dataIndex: 'deadlineDate',
          ellipsis: true,
          sorter: true,
          width: '17%'
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
              <span>您尚未添加任何任务，立即</span>
              <RouterLink to="/task#task" class="ml-1 link">添加任务</RouterLink>
            </template>

            <template v-else-if="!!props.params?.favouriteBy">
              <span>您没有收藏的任务</span>
            </template>

            <template v-else-if="!!props.params?.followBy">
              <span>您没有关注的任务</span>
            </template>

            <template v-else-if="props.params?.assigneeId && props.params?.status === 'PENDING'">
              <span>您没有待处理的任务</span>
            </template>

            <template v-else-if="props.params?.confirmorId && props.params?.status === 'CONFIRMING'">
              <span>您没有待确认的任务</span>
            </template>

            <template v-else-if="props.params?.assigneeId && props.params?.status === 'COMPLETED'">
              <span>您没有已完成的任务</span>
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
          <div v-if="column.dataIndex === 'name'" class="flex items-center">
            <IconTask :value="record.taskType?.value" class="text-4 flex-shrink-0" />
            <RouterLink
              class="link truncate ml-1"
              :title="record.name"
              :to="record.linkUrl">
              {{ record.name }}
            </RouterLink>
            <span
              v-if="record.overdueFlag"
              class="flex-shrink-0 border border-status-error rounded px-0.5 ml-2 mr-2"
              style="color: rgba(245, 34, 45, 100%);line-height: 16px;">
              <span class="inline-block transform-gpu scale-90">已逾期</span>
            </span>
          </div>

          <TaskPriority v-else-if="column.dataIndex === 'priority'" :value="record.priority" />

          <TaskStatus v-else-if="column.dataIndex === 'status'" :value="record.status" />

          <div v-else-if="column.dataIndex === 'scriptType'" class="truncate">
            {{ record.scriptType?.message }}
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

:deep(.ant-pagination) {
  margin-bottom: 0;
}
</style>
