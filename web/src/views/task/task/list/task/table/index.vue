<script setup lang="ts">
import { defineAsyncComponent, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
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
import { toClipboard } from '@xcan-angus/infra';
import { task } from '@/api/tester';

import { TaskInfo } from '../../../../types';
import { ActionMenuItem } from '../../../types';

const { t } = useI18n();

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

const MoveTaskModal = defineAsyncComponent(() => import('@/views/task/task/list/task/Move.vue'));

const MAX_NUM = 200;

const moveModalVisible = ref(false);
const selectedDataMap = ref<{
  [key: string]: {
    id: string;
    status: TaskInfo['status']['value'];
    favourite: boolean;
    follow: boolean;
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
        favourite: cur.favourite,
        follow: cur.follow
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
    notification.info(t('task.table.messages.maxBatchLimit', { maxNum: MAX_NUM, selectedNum }));
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
    content: t('task.table.messages.cancelConfirm', { num }),
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
          notification.success(t('task.table.messages.cancelNumSuccess', { num }));
          emit('batchAction', 'cancel', ids);
          rowSelection.value.selectedRowKeys = [];
          selectedDataMap.value = {};
          return;
        }

        if (errorNum === num) {
          notification.error(t('task.table.messages.cancelFail', { num }));
          return;
        }

        const successIds = ids.filter(item => !errorIds.includes(item));
        notification.warning(t('task.table.messages.cancelPartial', { successNum: num - errorNum, errorNum }));

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
    content: t('task.table.messages.deleteConfirm', { num }),
    async onOk () {
      const ids = Object.values(selectedDataMap.value).map(item => item.id);
      const [error] = await task.deleteTask(ids);
      if (error) {
        return;
      }

      emit('refreshChange');
      notification.success(t('task.table.messages.deleteSuccess', { num }));
      emit('batchAction', 'delete', ids);
      rowSelection.value.selectedRowKeys = [];
      selectedDataMap.value = {};
    }
  });
};

const batchFavourite = async () => {
  const num = rowSelection.value.selectedRowKeys.length;
  modal.confirm({
    content: t('task.table.messages.favouriteConfirm', { num }),
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
          notification.success(t('task.table.messages.favouriteNumSuccess', { num }));
          emit('batchAction', 'favourite', ids);
          rowSelection.value.selectedRowKeys = [];
          selectedDataMap.value = {};
          return;
        }

        if (errorNum === num) {
          notification.error(t('task.table.messages.favouriteFail', { num }));
          return;
        }

        const successIds = ids.filter(item => !errorIds.includes(item));
        notification.warning(t('task.table.messages.favouritePartial', { successNum: num - errorNum, errorNum }));

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
    content: t('task.table.messages.cancelFavouriteConfirm', { num }),
    async onOk () {
      // 过滤出要取消收藏的任务
      const ids = Object.values(selectedDataMap.value).map(item => item.id);
      const promises: Promise<any>[] = [];
      for (let i = 0, len = ids.length; i < len; i++) {
        promises.push(task.cancelFavouriteTask(ids[i], { silence: true }));
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
          notification.success(t('task.table.messages.cancelFavouriteNumSuccess', { num }));
          emit('batchAction', 'favourite', ids);
          rowSelection.value.selectedRowKeys = [];
          selectedDataMap.value = {};
          return;
        }

        if (errorNum === num) {
          notification.error(t('task.table.messages.cancelFavouriteFail', { num }));
          return;
        }

        const successIds = ids.filter(item => !errorIds.includes(item));
        notification.warning(t('task.table.messages.cancelFavouritePartial', { successNum: num - errorNum, errorNum }));

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
    content: t('task.table.messages.followConfirm', { num }),
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
          notification.success(t('task.table.messages.followNumSuccess', { num }));
          emit('batchAction', 'favourite', ids);
          rowSelection.value.selectedRowKeys = [];
          selectedDataMap.value = {};
          return;
        }

        if (errorNum === num) {
          notification.error(t('task.table.messages.followFail', { num }));
          return;
        }

        const successIds = ids.filter(item => !errorIds.includes(item));
        notification.warning(t('task.table.messages.followPartial', { successNum: num - errorNum, errorNum }));

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
    content: t('task.table.messages.cancelFollowConfirm', { num }),
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
          notification.success(t('task.table.messages.cancelFollowNumSuccess', { num }));
          emit('batchAction', 'favourite', ids);
          rowSelection.value.selectedRowKeys = [];
          selectedDataMap.value = {};
          return;
        }

        if (errorNum === num) {
          notification.error(t('task.table.messages.cancelFollowFail', { num }));
          return;
        }

        const successIds = ids.filter(item => !errorIds.includes(item));
        notification.warning(t('task.table.messages.cancelFollowPartial', { successNum: num - errorNum, errorNum }));

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
    content: t('task.table.messages.deleteTaskConfirm', { name: data.name }),
    async onOk () {
      const [error] = await task.deleteTask([data.id]);
      if (error) {
        return;
      }

      emit('refreshChange');
      emit('delete', data.id);
      notification.success(t('task.table.messages.deleteTaskSuccess'));
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

  notification.success(t('task.table.messages.favouriteSuccess'));
  emit('dataChange', { id: data.id, favourite: true });
};

const toDeleteFavourite = async (data: TaskInfo) => {
  const [error] = await task.cancelFavouriteTask(data.id);
  if (error) {
    return;
  }

  notification.success(t('task.table.messages.cancelFavouriteSuccess'));
  emit('dataChange', { id: data.id, favourite: false });
};

const toFollow = async (data: TaskInfo) => {
  const [error] = await task.followTask(data.id);
  if (error) {
    return;
  }

  notification.success(t('task.table.messages.followSuccess'));
  emit('dataChange', { id: data.id, follow: true });
};

const toDeleteFollow = async (data: TaskInfo) => {
  const [error] = await task.cancelFollowTask(data.id);
  if (error) {
    return;
  }

  notification.success(t('task.table.messages.cancelFollowSuccess'));
  emit('dataChange', { id: data.id, follow: false });
};

const toStart = async (data: TaskInfo) => {
  const id = data.id;
  const [error] = await task.startTask(id);
  if (error) {
    return;
  }

  emit('refreshChange');
  notification.success(t('task.table.messages.startSuccess'));
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
  notification.success(t('task.table.messages.processedSuccess'));
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
  notification.success(t('task.table.messages.reopenSuccess'));
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
  notification.success(t('task.table.messages.restartSuccess'));
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
  notification.success(t('task.table.messages.cancelSuccess'));
  const detailData = await loadData(id);
  emit('dataChange', detailData);
};

const toCopyHref = (data: TaskInfo) => {
  const message = window.location.origin + (data.linkUrl || '');
  toClipboard(message).then(() => {
    notification.success(t('task.table.messages.copySuccess'));
  }).catch(() => {
    notification.error(t('task.table.messages.copyFail'));
  });
};

const loadData = async (id: string): Promise<Partial<TaskInfo>> => {
  emit('update:loading', true);
  const [error, res] = await task.getTaskDetail(id);
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
      favourite: boolean;
      follow: boolean;
      id: string;
      status: string;
    }[];
    for (let i = 0, len = values.length; i < len; i++) {
      const { favourite, follow, status, id } = values[i];
      const menuItems = props.menuItemsMap.get(id) || [];
      const cancelItem = menuItems.find(item => item.key === 'cancel');
      const deleteItem = menuItems.find(item => item.key === 'delete');
      const moveItem = menuItems.find(item => item.key === 'move');

      if (favourite) {
        batchFavouriteDisabled.value = true;
      } else {
        batchCancelFavouriteDisabled.value = true;
      }

      if (follow) {
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
    title: t('task.table.columns.code'),
    dataIndex: 'code',
    width: 120,
    sorter: true,
    ellipsis: true
  },
  {
    title: t('task.table.columns.name'),
    dataIndex: 'name',
    width: '60%',
    sorter: true,
    ellipsis: true
  },
  {
    title: t('task.table.columns.priority'),
    dataIndex: 'priority',
    width: 80,
    sorter: true,
    ellipsis: true
  },
  {
    title: t('task.table.columns.status'),
    dataIndex: 'status',
    width: 80,
    sorter: true,
    ellipsis: true
  },
  {
    title: t('task.table.columns.execResult'),
    dataIndex: 'execResult',
    width: 80,
    sorter: true,
    ellipsis: true
  },
  {
    title: t('task.table.columns.assignee'),
    dataIndex: 'assigneeName',
    width: 100,
    groupName: 'personType',
    ellipsis: true
  },
  {
    title: t('task.table.columns.creator'),
    dataIndex: 'createdByName',
    width: 100,
    hide: true,
    groupName: 'personType',
    ellipsis: true
  },
  {
    title: t('task.table.columns.confirmer'),
    dataIndex: 'confirmerName',
    width: 100,
    hide: true,
    groupName: 'personType',
    ellipsis: true
  },
  {
    title: t('task.table.columns.lastModifier'),
    dataIndex: 'lastModifiedByName',
    width: 100,
    hide: true,
    groupName: 'personType',
    ellipsis: true
  },
  {
    title: t('task.table.columns.executor'),
    dataIndex: 'execUserName',
    width: 100,
    hide: true,
    groupName: 'personType',
    ellipsis: true
  },
  {
    title: t('task.table.columns.workload'),
    dataIndex: 'evalWorkload',
    width: 80,
    sorter: true,
    ellipsis: true
  },
  {
    title: t('task.table.columns.sprint'),
    dataIndex: 'sprintName',
    groupName: 'target',
    width: '40%',
    ellipsis: true
  },
  {
    title: t('task.table.columns.apiScenario'),
    hide: true,
    dataIndex: 'targetName',
    groupName: 'target',
    width: '40%',
    ellipsis: true
  },
  {
    title: t('task.table.columns.service'),
    hide: true,
    dataIndex: 'targetParentName',
    groupName: 'target',
    width: '40%',
    ellipsis: true
  },
  {
    title: t('task.table.columns.failCount'),
    dataIndex: 'failNum',
    width: 105,
    hide: true,
    sorter: true,
    groupName: 'num',
    ellipsis: true
  },
  {
    title: t('task.table.columns.processCount'),
    dataIndex: 'totalNum',
    width: 105,
    sorter: true,
    groupName: 'num',
    ellipsis: true
  },
  {
    title: t('task.table.columns.addTime'),
    dataIndex: 'createdDate',
    width: 140,
    groupName: 'date',
    sorter: true,
    ellipsis: true
  },
  {
    title: t('task.table.columns.deadline'),
    dataIndex: 'deadlineDate',
    width: 140,
    hide: true,
    groupName: 'date',
    sorter: true,
    ellipsis: true
  },
  {
    title: t('task.table.columns.startTime'),
    dataIndex: 'startDate',
    width: 140,
    hide: true,
    groupName: 'date',
    sorter: true,
    ellipsis: true
  },
  {
    title: t('task.table.columns.processTime'),
    dataIndex: 'processedDate',
    width: 140,
    hide: true,
    groupName: 'date',
    sorter: true,
    ellipsis: true
  },
  {
    title: t('task.table.columns.confirmTime'),
    dataIndex: 'confirmedDate',
    width: 140,
    hide: true,
    groupName: 'date',
    sorter: true,
    ellipsis: true
  },
  {
    title: t('task.table.columns.completeTime'),
    dataIndex: 'completedDate',
    width: 140,
    hide: true,
    groupName: 'date',
    sorter: true,
    ellipsis: true
  },
  {
    title: t('task.table.columns.cancelTime'),
    dataIndex: 'canceledDate',
    width: 140,
    hide: true,
    groupName: 'date',
    sorter: true,
    ellipsis: true
  },
  {
    title: t('task.table.columns.execTime'),
    dataIndex: 'execCompletedDate',
    width: 140,
    hide: true,
    groupName: 'date',
    sorter: true,
    ellipsis: true
  },
  {
    title: t('task.table.columns.lastModifyTime'),
    dataIndex: 'lastModifiedDate',
    width: 140,
    hide: true,
    groupName: 'date',
    sorter: true,
    ellipsis: true
  },
  {
    title: t('task.table.columns.operation'),
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
        {{ t('actions.cancel') }}
      </Button>

      <Button
        :disabled="batchDeleteDisabled"
        type="link"
        size="small"
        class="flex items-center px-0 h-5 leading-5"
        @click="batchDelete">
        {{ t('actions.delete') }}
      </Button>

      <Button
        :disabled="batchFavouriteDisabled"
        type="link"
        size="small"
        class="flex items-center px-0 h-5 leading-5"
        @click="batchFavourite">
        {{ t('actions.favourite') }}
      </Button>

      <Button
        :disabled="batchCancelFavouriteDisabled"
        type="link"
        size="small"
        class="flex items-center px-0 h-5 leading-5"
        @click="batchCancelFavourite">
        {{ t('actions.unFavourite') }}
      </Button>

      <Button
        :disabled="batchFollowDisabled"
        type="link"
        size="small"
        class="flex items-center px-0 h-5 leading-5"
        @click="batchFollow">
        {{ t('actions.follow') }}
      </Button>

      <Button
        :disabled="batchCancelFollowDisabled"
        type="link"
        size="small"
        class="flex items-center px-0 h-5 leading-5"
        @click="batchCancelFollow">
        {{ t('actions.unfollow') }}
      </Button>

      <Button
        :disabled="batchMoveFollowDisabled"
        type="link"
        size="small"
        class="flex items-center px-0 h-5 leading-5"
        @click="batchMove">
        {{ t('actions.move') }}
      </Button>

      <Button
        danger
        type="link"
        size="small"
        class="flex items-center px-0 h-5 leading-5"
        @click="cancelBatchOperation">
        <span>{{ t('task.table.batchActions.cancelBatch') }}</span>
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
            v-if="record.overdue"
            class="flex-shrink-0 border border-status-error rounded px-0.5 ml-2 mr-2"
            style="color: rgba(245, 34, 45, 100%);line-height: 16px;">
            <span class="inline-block transform-gpu scale-90">{{ t('task.table.status.overdue') }}</span>
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
            <span>{{ t('actions.edit') }}</span>
          </Button>

          <Button
            :disabled="!!props.menuItemsMap.get(record.id)?.find(item => item.key === 'delete')?.disabled"
            type="text"
            size="small"
            class="flex items-center px-0 mr-2.5"
            @click="toDelete(record)">
            <Icon icon="icon-qingchu" class="mr-1 text-3.5" />
            <span>{{ t('actions.delete') }}</span>
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
