<script setup lang="ts">
import { defineAsyncComponent, onMounted, ref, watch } from 'vue';
import { Button, TableColumnProps } from 'ant-design-vue';
import {
  AsyncComponent,
  Dropdown,
  Icon,
  IconTask,
  modal,
  notification,
  Table,
  TaskPriority,
  TaskStatus
} from '@xcan-angus/vue-ui';
import { clipboard } from '@xcan-angus/tools';
import { task } from '@/api/altester';

import { TaskInfo } from '../../../../PropsType';
import { ActionMenuItem } from '../../PropsType';

type Props = {
  projectId: string;
  selectedIds: string[];// 批量选中的任务
  dataSource: TaskInfo[];
  pagination: { current: number; pageSize: number; total: number; };
  menuItemsMap: Map<string, ActionMenuItem[]>;
  loading: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  selectedIds: () => [],
  dataSource: () => [],
  pagination: () => ({ current: 1, pageSize: 10, total: 0 }),
  menuItemsMap: () => new Map(),
  loading: false
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (event: 'update:selectedIds', value: string[]): void;
  (event: 'update:loading', value: boolean): void;
  (event: 'tableChange', pagination: { current: number; pageSize: number }, sorter: {
    orderBy: string;
    orderSort: 'DESC' | 'ASC'
  }): void;
  (event: 'edit', value: string): void;
  (event: 'move', value: TaskInfo): void;
  (event: 'delete', value: string): void;
  (event: 'dataChange', value: Partial<TaskInfo>): void;
  (event: 'refreshChange'): void;
  (event: 'batchAction', type: 'cancel' | 'delete' | 'follow' | 'cancelFollow' | 'favourite' | 'cancelFavourite' | 'move', value: string[]): void;
}>();

const MoveTaskModal = defineAsyncComponent(() => import('@/views/task/task/list/task/move/index.vue'));

const MAX_NUM = 200;

const moveModalVisible = ref(false);
const selectedDataMap = ref<{
  [key: string]: {
    id: string;
    status: TaskInfo['status']['value'];
    favouriteFlag: boolean;
    followFlag: boolean;
    sprintId: string;
  }
}>({});

const batchCancelDisabled = ref(false);
const batchDeleteDisabled = ref(false);
const batchMoveFollowDisabled = ref(false);
const batchFavouriteDisabled = ref(false);
const batchCancelFavouriteDisabled = ref(false);
const batchFollowDisabled = ref(false);
const batchCancelFollowDisabled = ref(false);

const tableSelect = (keys: string[]) => {
  const deleteIds = props.dataSource.reduce((prev, cur) => {
    const id = cur.id;
    if (!keys.includes(id)) {
      prev.push(cur.id);

      delete selectedDataMap.value[id];
    } else {
      selectedDataMap.value[id] = {
        id,
        status: cur.status.value,
        favouriteFlag: cur.favouriteFlag,
        followFlag: cur.followFlag
      };
    }

    return prev;
  }, [] as string[]);

  // 删除反选的任务
  const selectedRowKeys = rowSelection.value.selectedRowKeys.filter(item => !deleteIds.includes(item));

  for (let i = 0, len = keys.length; i < len; i++) {
    if (!selectedRowKeys.includes(keys[i])) {
      selectedRowKeys.push(keys[i]);
    }
  }

  const selectedNum = selectedRowKeys.length;
  if (selectedNum > MAX_NUM) {
    notification.info(`最大支持批量操作 ${MAX_NUM} 个任务，当前已选中 ${selectedNum} 个任务。`);
  }

  rowSelection.value.selectedRowKeys = selectedRowKeys;
};

const rowSelection = ref<{
  onChange:(key: string[]) => void;
  getCheckboxProps: (data: TaskInfo) => ({ disabled: boolean; });
  selectedRowKeys: string[];
    }>({
      onChange: tableSelect,
      getCheckboxProps: () => {
        return {
          disabled: false
        };
      },
      selectedRowKeys: []
    });

const change = (pagination: { current: number; pageSize: number; }, _filters: { [key: string]: any }[], sorter: {
  orderBy: string;
  orderSort: 'DESC' | 'ASC'
}) => {
  emit('tableChange', pagination, sorter);
};

const cancelBatchOperation = () => {
  rowSelection.value.selectedRowKeys = [];
  selectedDataMap.value = {};
};

const batchCancel = async () => {
  const num = rowSelection.value.selectedRowKeys.length;
  modal.confirm({
    content: `确定取消选中的 ${num} 条任务吗？`,
    async onOk () {
      const ids = Object.values(selectedDataMap.value).map(item => item.id);
      const promises: Promise<any>[] = [];
      for (let i = 0, len = ids.length; i < len; i++) {
        promises.push(task.cancelTask(ids[i], { silence: true }));
      }

      Promise.all(promises).then((res: [Error | null, any][]) => {
        const errorIds: string[] = [];
        for (let i = 0, len = res.length; i < len; i++) {
          if (res[i][0]) {
            errorIds.push(ids[i]);
          }
        }

        const errorNum = errorIds.length;
        if (errorNum === 0) {
          emit('refreshChange');
          notification.success(`选中的 ${num} 条任务全部取消成功`);
          emit('batchAction', 'cancel', ids);
          rowSelection.value.selectedRowKeys = [];
          selectedDataMap.value = {};
          return;
        }

        if (errorNum === num) {
          notification.error(`选中的 ${num} 条任务全部取消失败`);
          return;
        }

        const successIds = ids.filter(item => !errorIds.includes(item));
        notification.warning(`选中的 ${num - errorNum} 条任务取消成功，${errorNum} 条任务取消失败`);

        emit('refreshChange');
        emit('batchAction', 'cancel', successIds);

        rowSelection.value.selectedRowKeys = rowSelection.value.selectedRowKeys.filter((item) => !successIds.includes(item));
        for (let i = 0, len = successIds.length; i < len; i++) {
          delete selectedDataMap.value[successIds[i]];
        }
      });
    }
  });
};

const batchDelete = async () => {
  const num = rowSelection.value.selectedRowKeys.length;
  modal.confirm({
    content: `确定删除选中的 ${num} 条任务吗？`,
    async onOk () {
      const ids = Object.values(selectedDataMap.value).map(item => item.id);
      const [error] = await task.deleteTask(ids);
      if (error) {
        return;
      }

      emit('refreshChange');
      notification.success(`选中的 ${num} 条任务删除成功`);
      emit('batchAction', 'delete', ids);
      rowSelection.value.selectedRowKeys = [];
      selectedDataMap.value = {};
    }
  });
};

const batchFavourite = async () => {
  const num = rowSelection.value.selectedRowKeys.length;
  modal.confirm({
    content: `确定收藏选中的 ${num} 条任务吗？`,
    async onOk () {
      const ids = Object.values(selectedDataMap.value).map(item => item.id);
      const promises: Promise<any>[] = [];
      for (let i = 0, len = ids.length; i < len; i++) {
        promises.push(task.favouriteTask(ids[i], { silence: true }));
      }

      Promise.all(promises).then((res: [Error | null, any][]) => {
        const errorIds: string[] = [];
        for (let i = 0, len = res.length; i < len; i++) {
          if (res[i][0]) {
            errorIds.push(ids[i]);
          }
        }

        const errorNum = errorIds.length;
        if (errorNum === 0) {
          notification.success(`选中的 ${num} 条任务全部收藏成功`);
          emit('batchAction', 'favourite', ids);
          rowSelection.value.selectedRowKeys = [];
          selectedDataMap.value = {};
          return;
        }

        if (errorNum === num) {
          notification.error(`选中的 ${num} 条任务全部收藏失败`);
          return;
        }

        const successIds = ids.filter(item => !errorIds.includes(item));
        notification.warning(`选中的 ${num - errorNum} 条任务收藏成功，${errorNum} 条任务收藏失败`);

        emit('batchAction', 'favourite', successIds);

        rowSelection.value.selectedRowKeys = rowSelection.value.selectedRowKeys.filter((item) => !successIds.includes(item));
        for (let i = 0, len = successIds.length; i < len; i++) {
          delete selectedDataMap.value[successIds[i]];
        }
      });
    }
  });
};

const batchCancelFavourite = async () => {
  const num = rowSelection.value.selectedRowKeys.length;
  modal.confirm({
    content: `确定取消收藏选中的 ${num} 条任务吗？`,
    async onOk () {
      // 过滤出要取消收藏的任务
      const ids = Object.values(selectedDataMap.value).map(item => item.id);
      const promises: Promise<any>[] = [];
      for (let i = 0, len = ids.length; i < len; i++) {
        promises.push(task.cancelFavouriteTask(ids[i],  { silence: true }));
      }

      Promise.all(promises).then((res: [Error | null, any][]) => {
        const errorIds: string[] = [];
        for (let i = 0, len = res.length; i < len; i++) {
          if (res[i][0]) {
            errorIds.push(ids[i]);
          }
        }

        const errorNum = errorIds.length;
        if (errorNum === 0) {
          notification.success(`选中的 ${num} 条任务全部取消收藏成功`);
          emit('batchAction', 'favourite', ids);
          rowSelection.value.selectedRowKeys = [];
          selectedDataMap.value = {};
          return;
        }

        if (errorNum === num) {
          notification.error(`选中的 ${num} 条任务全部取消收藏失败`);
          return;
        }

        const successIds = ids.filter(item => !errorIds.includes(item));
        notification.warning(`选中的 ${num - errorNum} 条任务取消收藏成功，${errorNum} 条任务取消收藏失败`);

        emit('batchAction', 'favourite', successIds);

        rowSelection.value.selectedRowKeys = rowSelection.value.selectedRowKeys.filter((item) => !successIds.includes(item));
        for (let i = 0, len = successIds.length; i < len; i++) {
          delete selectedDataMap.value[successIds[i]];
        }
      });
    }
  });
};

const batchFollow = async () => {
  const num = rowSelection.value.selectedRowKeys.length;
  modal.confirm({
    content: `确定关注选中的 ${num} 条任务吗？`,
    async onOk () {
      const ids = Object.values(selectedDataMap.value).map(item => item.id);
      const promises: Promise<any>[] = [];
      for (let i = 0, len = ids.length; i < len; i++) {
        promises.push(task.followTask(ids[i], { silence: true }));
      }

      Promise.all(promises).then((res: [Error | null, any][]) => {
        const errorIds: string[] = [];
        for (let i = 0, len = res.length; i < len; i++) {
          if (res[i][0]) {
            errorIds.push(ids[i]);
          }
        }

        const errorNum = errorIds.length;
        if (errorNum === 0) {
          notification.success(`选中的 ${num} 条任务全部关注成功`);
          emit('batchAction', 'favourite', ids);
          rowSelection.value.selectedRowKeys = [];
          selectedDataMap.value = {};
          return;
        }

        if (errorNum === num) {
          notification.error(`选中的 ${num} 条任务全部关注失败`);
          return;
        }

        const successIds = ids.filter(item => !errorIds.includes(item));
        notification.warning(`选中的 ${num - errorNum} 条任务关注成功，${errorNum} 条任务关注失败`);

        emit('batchAction', 'favourite', successIds);

        rowSelection.value.selectedRowKeys = rowSelection.value.selectedRowKeys.filter((item) => !successIds.includes(item));
        for (let i = 0, len = successIds.length; i < len; i++) {
          delete selectedDataMap.value[successIds[i]];
        }
      });
    }
  });
};

const batchCancelFollow = async () => {
  const num = rowSelection.value.selectedRowKeys.length;
  modal.confirm({
    content: `确定取消关注选中的 ${num} 条任务吗？`,
    async onOk () {
      const ids = Object.values(selectedDataMap.value).map(item => item.id);
      const promises: Promise<any>[] = [];
      for (let i = 0, len = ids.length; i < len; i++) {
        promises.push(task.cancelFollowTask(ids[i], { silence: true }));
      }

      Promise.all(promises).then((res: [Error | null, any][]) => {
        const errorIds: string[] = [];
        for (let i = 0, len = res.length; i < len; i++) {
          if (res[i][0]) {
            errorIds.push(ids[i]);
          }
        }

        const errorNum = errorIds.length;
        if (errorNum === 0) {
          notification.success(`选中的 ${num} 条任务全部取消关注成功`);
          emit('batchAction', 'favourite', ids);
          rowSelection.value.selectedRowKeys = [];
          selectedDataMap.value = {};
          return;
        }

        if (errorNum === num) {
          notification.error(`选中的 ${num} 条任务全部取消关注失败`);
          return;
        }

        const successIds = ids.filter(item => !errorIds.includes(item));
        notification.warning(`选中的 ${num - errorNum} 条任务取消关注成功，${errorNum} 条任务取消关注失败`);

        emit('batchAction', 'favourite', successIds);

        rowSelection.value.selectedRowKeys = rowSelection.value.selectedRowKeys.filter((item) => !successIds.includes(item));
        for (let i = 0, len = successIds.length; i < len; i++) {
          delete selectedDataMap.value[successIds[i]];
        }
      });
    }
  });
};

const batchMove = () => {
  moveModalVisible.value = true;
};

const batchMoveTaskOk = async (_sprintId: string, taskIds: []) => {
  emit('batchAction', 'move', taskIds);
  rowSelection.value.selectedRowKeys = [];
  selectedDataMap.value = {};
};

const toEdit = (id: string) => {
  emit('edit', id);
};

const toDelete = (data: TaskInfo) => {
  modal.confirm({
    content: `确定删除任务【${data.name}】吗？`,
    async onOk () {
      const [error] = await task.deleteTask([data.id]);
      if (error) {
        return;
      }

      emit('refreshChange');
      emit('delete', data.id);
      notification.success('任务删除成功，您可以在回收站查看删除后的任务');
    }
  });
};

const dropdownClick = (menuItem: ActionMenuItem, data: TaskInfo) => {
  const key = menuItem.key;
  if (key === 'favourite') {
    toFavourite(data);
    return;
  }

  if (key === 'cancelFavourite') {
    toDeleteFavourite(data);
    return;
  }

  if (key === 'follow') {
    toFollow(data);
    return;
  }

  if (key === 'cancelFollow') {
    toDeleteFollow(data);
    return;
  }

  if (key === 'start') {
    toStart(data);
    return;
  }

  if (key === 'processed') {
    toProcessed(data);
    return;
  }

  if (key === 'completed') {
    toCompleted(data);
    return;
  }

  if (key === 'uncompleted') {
    toUncomplete(data);
    return;
  }

  if (key === 'reopen') {
    toReopen(data);
    return;
  }

  if (key === 'restart') {
    toRestart(data);
    return;
  }

  if (key === 'cancel') {
    toCancel(data);
    return;
  }

  if (key === 'move') {
    toMove(data);
    return;
  }

  if (key === 'copyLink') {
    toCopyHref(data);
  }
};

const toFavourite = async (data: TaskInfo) => {
  const [error] = await task.favouriteTask(data.id);
  if (error) {
    return;
  }

  notification.success('任务收藏成功');
  emit('dataChange', { id: data.id, favouriteFlag: true });
};

const toDeleteFavourite = async (data: TaskInfo) => {
  const [error] = await task.cancelFavouriteTask(data.id);
  if (error) {
    return;
  }

  notification.success('任务取消收藏成功');
  emit('dataChange', { id: data.id, favouriteFlag: false });
};

const toFollow = async (data: TaskInfo) => {
  const [error] = await task.followTask(data.id);
  if (error) {
    return;
  }

  notification.success('任务关注成功');
  emit('dataChange', { id: data.id, followFlag: true });
};

const toDeleteFollow = async (data: TaskInfo) => {
  const [error] = await task.cancelFollowTask(data.id);
  if (error) {
    return;
  }

  notification.success('任务取消关注成功');
  emit('dataChange', { id: data.id, followFlag: false });
};

const toStart = async (data: TaskInfo) => {
  const id = data.id;
  const [error] = await task.startProcessing(id);
  if (error) {
    return;
  }

  emit('refreshChange');
  notification.success('任务开始处理成功');
  const detailData = await loadData(id);
  emit('dataChange', detailData);
};

const toProcessed = async (data: TaskInfo) => {
  const id = data.id;
  const [error] = await task.processedTask(id);
  if (error) {
    return;
  }

  emit('refreshChange');
  notification.success('任务已处理成功');
  const detailData = await loadData(id);
  emit('dataChange', detailData);
};

const toUncomplete = async (data: TaskInfo) => {
  const id = data.id;
  const [error] = await task.confirmTask(id, 'FAIL');
  if (error) {
    return;
  }

  emit('refreshChange');
  const detailData = await loadData(id);
  emit('dataChange', detailData);
};

const toCompleted = async (data: TaskInfo) => {
  const id = data.id;
  const [error] = await task.confirmTask(id, 'SUCCESS');
  if (error) {
    return;
  }

  emit('refreshChange');
  const detailData = await loadData(id);
  emit('dataChange', detailData);
};

const toReopen = async (data: TaskInfo) => {
  const id = data.id;
  const [error] = await task.reopenTask(id);
  if (error) {
    return;
  }

  emit('refreshChange');
  notification.success('任务重新打开成功');
  const detailData = await loadData(id);
  emit('dataChange', detailData);
};

const toRestart = async (data: TaskInfo) => {
  const id = data.id;
  const [error] = await task.restartTask(id);
  if (error) {
    return;
  }

  emit('refreshChange');
  notification.success('任务重新开始成功');
  const detailData = await loadData(id);
  emit('dataChange', detailData);
};

const toMove = (data: TaskInfo) => {
  emit('move', data);
};

const toCancel = async (data: TaskInfo) => {
  const id = data.id;
  const [error] = await task.cancelTask(id);
  if (error) {
    return;
  }

  emit('refreshChange');
  notification.success('任务取消成功');
  const detailData = await loadData(id);
  emit('dataChange', detailData);
};

const toCopyHref = (data: TaskInfo) => {
  const message = window.location.origin + (data.linkUrl || '');
  clipboard.toClipboard(message).then(() => {
    notification.success('复制成功');
  }).catch(() => {
    notification.error('复制失败');
  });
};

const loadData = async (id: string): Promise<Partial<TaskInfo>> => {
  emit('update:loading', true);
  const [error, res] = await task.loadTaskInfo(id);
  emit('update:loading', false);
  if (error || !res?.data) {
    return { id };
  }

  return res.data;
};

onMounted(() => {
  watch(() => props.selectedIds, (newValue) => {
    rowSelection.value.selectedRowKeys = newValue;
  }, { immediate: true });

  watch(() => rowSelection.value.selectedRowKeys, (newValue) => {
    emit('update:selectedIds', newValue);
  });

  watch(() => selectedDataMap.value, (newValue) => {
    batchCancelDisabled.value = false;
    batchDeleteDisabled.value = false;
    batchMoveFollowDisabled.value = false;
    batchFavouriteDisabled.value = false;
    batchCancelFavouriteDisabled.value = false;
    batchFollowDisabled.value = false;
    batchCancelFollowDisabled.value = false;

    const values = (Object.values(newValue) || []) as {
      favouriteFlag: boolean;
      followFlag: boolean;
      id: string;
      status: string;
    }[];
    for (let i = 0, len = values.length; i < len; i++) {
      const { favouriteFlag, followFlag, status, id } = values[i];
      const menuItems = props.menuItemsMap.get(id) || [];
      const cancelItem = menuItems.find(item => item.key === 'cancel');
      const deleteItem = menuItems.find(item => item.key === 'delete');
      const moveItem = menuItems.find(item => item.key === 'move');

      if (favouriteFlag) {
        batchFavouriteDisabled.value = true;
      } else {
        batchCancelFavouriteDisabled.value = true;
      }

      if (followFlag) {
        batchFollowDisabled.value = true;
      } else {
        batchCancelFollowDisabled.value = true;
      }

      if (['CANCELED', 'COMPLETED'].includes(status) || cancelItem?.disabled) {
        batchCancelDisabled.value = true;
      }

      if (deleteItem?.disabled) {
        batchDeleteDisabled.value = true;
      }

      if (moveItem?.disabled) {
        batchMoveFollowDisabled.value = true;
      }
    }
  }, { immediate: true, deep: true });
});

const columns: ({
  hide?: boolean,
  groupName?: string
} & TableColumnProps)[] = [
  {
    title: '编号',
    dataIndex: 'code',
    width: 120,
    sorter: true,
    ellipsis: true
  },
  {
    title: '名称',
    dataIndex: 'name',
    width: '60%',
    sorter: true,
    ellipsis: true
  },
  {
    title: '优先级',
    dataIndex: 'priority',
    width: 80,
    sorter: true,
    ellipsis: true
  },
  {
    title: '状态',
    dataIndex: 'status',
    width: 80,
    sorter: true,
    ellipsis: true
  },
  {
    title: '执行结果',
    dataIndex: 'execResult',
    width: 80,
    sorter: true,
    ellipsis: true
  },
  {
    title: '经办人',
    dataIndex: 'assigneeName',
    width: 100,
    groupName: 'personType',
    ellipsis: true
  },
  {
    title: '添加人',
    dataIndex: 'createdByName',
    width: 100,
    hide: true,
    groupName: 'personType',
    ellipsis: true
  },
  {
    title: '确认人',
    dataIndex: 'confirmorName',
    width: 100,
    hide: true,
    groupName: 'personType',
    ellipsis: true
  },
  {
    title: '最后修改人',
    dataIndex: 'lastModifiedByName',
    width: 100,
    hide: true,
    groupName: 'personType',
    ellipsis: true
  },
  {
    title: '执行人',
    dataIndex: 'execUserName',
    width: 100,
    hide: true,
    groupName: 'personType',
    ellipsis: true
  },
  {
    title: '工作量',
    dataIndex: 'evalWorkload',
    width: 80,
    sorter: true,
    ellipsis: true
  },
  {
    title: '所属迭代',
    dataIndex: 'sprintName',
    groupName: 'target',
    width: '40%',
    ellipsis: true
  },
  {
    title: '接口/场景',
    hide: true,
    dataIndex: 'targetName',
    groupName: 'target',
    width: '40%',
    ellipsis: true
  },
  {
    title: '服务',
    hide: true,
    dataIndex: 'targetParentName',
    groupName: 'target',
    width: '40%',
    ellipsis: true
  },
  {
    title: '失败次数',
    dataIndex: 'failNum',
    width: 105,
    hide: true,
    sorter: true,
    groupName: 'num',
    ellipsis: true
  },
  {
    title: '处理次数',
    dataIndex: 'totalNum',
    width: 105,
    sorter: true,
    groupName: 'num',
    ellipsis: true
  },
  {
    title: '添加时间',
    dataIndex: 'createdDate',
    width: 140,
    groupName: 'date',
    sorter: true,
    ellipsis: true
  },
  {
    title: '截止时间',
    dataIndex: 'deadlineDate',
    width: 140,
    hide: true,
    groupName: 'date',
    sorter: true,
    ellipsis: true
  },
  {
    title: '开始时间',
    dataIndex: 'startDate',
    width: 140,
    hide: true,
    groupName: 'date',
    sorter: true,
    ellipsis: true
  },
  {
    title: '处理时间',
    dataIndex: 'processedDate',
    width: 140,
    hide: true,
    groupName: 'date',
    sorter: true,
    ellipsis: true
  },
  {
    title: '确认时间',
    dataIndex: 'confirmedDate',
    width: 140,
    hide: true,
    groupName: 'date',
    sorter: true,
    ellipsis: true
  },
  {
    title: '完成时间',
    dataIndex: 'completedDate',
    width: 140,
    hide: true,
    groupName: 'date',
    sorter: true,
    ellipsis: true
  },
  {
    title: '取消时间',
    dataIndex: 'canceledDate',
    width: 140,
    hide: true,
    groupName: 'date',
    sorter: true,
    ellipsis: true
  },
  {
    title: '执行时间',
    dataIndex: 'execCompletedDate',
    width: 140,
    hide: true,
    groupName: 'date',
    sorter: true,
    ellipsis: true
  },
  {
    title: '最后修改时间',
    dataIndex: 'lastModifiedDate',
    width: 140,
    hide: true,
    groupName: 'date',
    sorter: true,
    ellipsis: true
  },
  {
    title: '操作',
    dataIndex: 'action',
    align: 'center',
    width: 140
  }
];

const EXEC_RESULT_COLOR = {
  FAIL: '#F5222D',
  SUCCESS: '#52C41A'
};
</script>
<template>
  <div>
    <div
      :visible="!!props.selectedIds.length"
      class="btn-group-container flex items-center transition-all duration-300 space-x-2.5">
      <Button
        :disabled="batchCancelDisabled"
        type="link"
        size="small"
        class="flex items-center px-0 h-5 leading-5"
        @click="batchCancel">
        取消
      </Button>

      <Button
        :disabled="batchDeleteDisabled"
        type="link"
        size="small"
        class="flex items-center px-0 h-5 leading-5"
        @click="batchDelete">
        删除
      </Button>

      <Button
        :disabled="batchFavouriteDisabled"
        type="link"
        size="small"
        class="flex items-center px-0 h-5 leading-5"
        @click="batchFavourite">
        收藏
      </Button>

      <Button
        :disabled="batchCancelFavouriteDisabled"
        type="link"
        size="small"
        class="flex items-center px-0 h-5 leading-5"
        @click="batchCancelFavourite">
        取消收藏
      </Button>

      <Button
        :disabled="batchFollowDisabled"
        type="link"
        size="small"
        class="flex items-center px-0 h-5 leading-5"
        @click="batchFollow">
        关注
      </Button>

      <Button
        :disabled="batchCancelFollowDisabled"
        type="link"
        size="small"
        class="flex items-center px-0 h-5 leading-5"
        @click="batchCancelFollow">
        取消关注
      </Button>

      <Button
        :disabled="batchMoveFollowDisabled"
        type="link"
        size="small"
        class="flex items-center px-0 h-5 leading-5"
        @click="batchMove">
        移动
      </Button>

      <Button
        danger
        type="link"
        size="small"
        class="flex items-center px-0 h-5 leading-5"
        @click="cancelBatchOperation">
        <span>取消批量操作</span>
        <span class="ml-1">({{ props.selectedIds.length }})</span>
      </Button>
    </div>
    <Table
      :columns="columns"
      :dataSource="props.dataSource"
      :pagination="props.pagination"
      :rowSelection="rowSelection"
      rowKey="id"
      @change="change">
      <template #bodyCell="{ text, record, column }">
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

        <div v-else-if="column.dataIndex === 'evalWorkload'">
          {{ text }}
        </div>

        <TaskPriority v-else-if="column.dataIndex === 'priority'" :value="text" />

        <TaskStatus v-else-if="column.dataIndex === 'status'" :value="text" />

        <div v-else-if="column.dataIndex === 'execResult'" :style="'color:' + EXEC_RESULT_COLOR[text?.value]">
          {{ text?.message || '--' }}
        </div>

        <div v-else-if="column.dataIndex === 'action'" class="flex items-center">
          <Button
            :disabled="!!props.menuItemsMap.get(record.id)?.find(item => item.key === 'edit')?.disabled"
            type="text"
            size="small"
            class="flex items-center px-0 mr-2.5"
            @click="toEdit(record.id)">
            <Icon icon="icon-shuxie" class="mr-1 text-3.5" />
            <span>编辑</span>
          </Button>

          <Button
            :disabled="!!props.menuItemsMap.get(record.id)?.find(item => item.key === 'delete')?.disabled"
            type="text"
            size="small"
            class="flex items-center px-0 mr-2.5"
            @click="toDelete(record)">
            <Icon icon="icon-qingchu" class="mr-1 text-3.5" />
            <span>删除</span>
          </Button>

          <Dropdown :menuItems="props.menuItemsMap.get(record.id)" @click="dropdownClick($event, record)">
            <Button
              type="text"
              size="small"
              class="flex items-center px-0">
              <Icon icon="icon-gengduo" />
            </Button>
          </Dropdown>
        </div>
      </template>
    </Table>

    <AsyncComponent :visible="moveModalVisible">
      <MoveTaskModal
        v-model:visible="moveModalVisible"
        :taskIds="rowSelection.selectedRowKeys"
        :projectId="props.projectId"
        @ok="batchMoveTaskOk" />
    </AsyncComponent>
  </div>
</template>
<style scoped>
.link {
  color: #1890ff;
  cursor: pointer;
}

.type-icon {
  position: absolute;
  top: -2px;
  left: 0;
  font-size: 18px;
}

.btn-group-container {
  height: 0;
  overflow: hidden;
  opacity: 0;
}

.btn-group-container[visible="true"] {
  height: 28px;
  opacity: 1;
}
</style>
