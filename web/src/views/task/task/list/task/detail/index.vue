<script setup lang="ts">
import { computed, defineAsyncComponent, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Button } from 'ant-design-vue';
import { AsyncComponent, modal, notification } from '@xcan-angus/vue-ui';
import { TESTER } from '@xcan-angus/infra';
import { task } from 'src/api/tester';


import { TaskInfo } from '../../../../PropsType';
import { ActionMenuItem } from '../../PropsType';

type Props = {
  projectId: string;
  userInfo: { id: string; };
  appInfo: { id: string; };
  selectedIds: string[];// 批量选中的任务
  dataSource: TaskInfo[];
  editTaskData: TaskInfo;// 编辑弹窗修改的任务，需要同步到详情
  pagination: { current: number; pageSize: number; total: number; };
  menuItemsMap: Map<string, ActionMenuItem[]>;
  loading: boolean;
  loaded: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  selectedIds: () => [],
  dataSource: () => [],
  editTaskData: undefined,
  pagination: () => ({ current: 1, pageSize: 10, total: 0, index: 0 }),
  menuItemsMap: () => new Map(),
  loading: false,
  loaded: false
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (event: 'update:loading', value: boolean): void;
  (event: 'update:selectedIds', value: string[]): void;
  (event: 'edit', value: string): void;
  (event: 'move', value: TaskInfo): void;
  (event: 'delete', value:string): void;
  (event: 'dataChange', value: Partial<TaskInfo>): void;
  (event: 'refreshChange'): void;
  (event: 'splitOk'): void;
  (event: 'paginationChange', value: { current: number; pageSize: number; }): void;
  (event: 'batchAction', type: 'cancel' | 'delete' | 'follow' | 'cancelFollow' | 'favourite' | 'cancelFavourite' | 'move', value: string[]): void;
}>();

const List = defineAsyncComponent(() => import('./list.vue'));
const Details = defineAsyncComponent(() => import('@/views/task/task/list/task/detail/view/index.vue'));
const MoveTaskModal = defineAsyncComponent(() => import('@/views/task/task/list/task/move/index.vue'));

const { t } = useI18n();
const MAX_NUM = 200;

const batchCancelDisabled = ref(false);
const batchDeleteDisabled = ref(false);
const batchMoveFollowDisabled = ref(false);
const batchFavouriteDisabled = ref(false);
const batchCancelFavouriteDisabled = ref(false);
const batchFollowDisabled = ref(false);
const batchCancelFollowDisabled = ref(false);

// 点击选中的任务
const checkedData = ref<TaskInfo>();

const moveModalVisible = ref(false);
const selectedDataMap = ref<{// 批量操作选中的任务
  [key: string]: {
    id: string;
    status: TaskInfo['status']['value'];
    favouriteFlag: boolean;
    followFlag: boolean;
    sprintId?: string;
  }
}>({});

const paginationChange = (data: { current: number; pageSize: number; }) => {
  emit('paginationChange', data);
};

const toChecked = (data: TaskInfo) => {
  checkedData.value = data;
};

const toEdit = (id: string) => {
  emit('edit', id);
};

const deleteOk = (id: string) => {
  emit('delete', id);
};

const toMove = (data: TaskInfo) => {
  emit('move', data);
};

const dataChange = (data: Partial<TaskInfo>) => {
  emit('dataChange', data);
};

const refreshChange = () => {
  emit('refreshChange');
};

const toSelect = (ids: string[]) => {
  const deleteIds = props.dataSource.reduce((prev, cur) => {
    const id = cur.id;
    if (!ids.includes(id)) {
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
  const selectedRowKeys = props.selectedIds.filter(item => !deleteIds.includes(item));

  for (let i = 0, len = ids.length; i < len; i++) {
    if (!selectedRowKeys.includes(ids[i])) {
      selectedRowKeys.push(ids[i]);
    }
  }

  const selectedNum = selectedRowKeys.length;
  if (selectedNum > MAX_NUM) {
    notification.info(t('task.detail.messages.maxBatchOperation', { maxNum: MAX_NUM, selectedNum }));
  }

  emit('update:selectedIds', selectedRowKeys);
};

const cancelBatchOperation = () => {
  emit('update:selectedIds', []);
  selectedDataMap.value = {};
};

const batchCancel = async () => {
  const num = props.selectedIds.length;
  modal.confirm({
    content: t('task.detail.batchActions.confirmCancel', { num }),
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
          notification.success(t('task.detail.batchActions.cancelSuccess', { num }));
          emit('batchAction', 'cancel', ids);
          emit('update:selectedIds', []);
          selectedDataMap.value = {};
          return;
        }

        if (errorNum === num) {
          notification.error(t('task.detail.batchActions.cancelFailed', { num }));
          return;
        }

        const successIds = ids.filter(item => !errorIds.includes(item));
        notification.warning(t('task.detail.batchActions.cancelPartialSuccess', { num: num - errorNum, errorNum }));

        emit('refreshChange');
        emit('batchAction', 'cancel', successIds);

        const remainIds = props.selectedIds.filter((item) => !successIds.includes(item));
        emit('update:selectedIds', remainIds);
        for (let i = 0, len = successIds.length; i < len; i++) {
          delete selectedDataMap.value[successIds[i]];
        }
      });
    }
  });
};

const batchDelete = async () => {
  const num = props.selectedIds.length;
  modal.confirm({
    content: t('task.detail.batchActions.confirmDelete', { num }),
    async onOk () {
      const ids = Object.values(selectedDataMap.value).map(item => item.id);
      const [error] = await task.deleteTask(`${TESTER}/task`, ids);
      if (error) {
        return;
      }

      emit('refreshChange');
      notification.success(t('task.detail.batchActions.deleteSuccess', { num }));
      emit('batchAction', 'delete', ids);
      emit('update:selectedIds', []);
      selectedDataMap.value = {};
    }
  });
};

const batchFavourite = async () => {
  const num = props.selectedIds.length;
  modal.confirm({
    content: t('task.detail.batchActions.confirmFavourite', { num }),
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
          notification.success(t('task.detail.batchActions.favouriteSuccess', { num }));
          emit('batchAction', 'favourite', ids);
          emit('update:selectedIds', []);
          selectedDataMap.value = {};
          return;
        }

        if (errorNum === num) {
          notification.error(t('task.detail.batchActions.favouriteFailed', { num }));
          return;
        }

        const successIds = ids.filter(item => !errorIds.includes(item));
        notification.warning(t('task.detail.batchActions.favouritePartialSuccess', { num: num - errorNum, errorNum }));

        emit('batchAction', 'favourite', successIds);

        const remainIds = props.selectedIds.filter((item) => !successIds.includes(item));
        emit('update:selectedIds', remainIds);

        for (let i = 0, len = successIds.length; i < len; i++) {
          delete selectedDataMap.value[successIds[i]];
        }
      });
    }
  });
};

const batchCancelFavourite = async () => {
  const num = props.selectedIds.length;
  modal.confirm({
    content: t('task.detail.batchActions.confirmCancelFavourite', { num }),
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
          notification.success(t('task.detail.batchActions.cancelFavouriteSuccess', { num }));
          emit('batchAction', 'favourite', ids);
          emit('update:selectedIds', []);
          selectedDataMap.value = {};
          return;
        }

        if (errorNum === num) {
          notification.error(t('task.detail.batchActions.cancelFavouriteFailed', { num }));
          return;
        }

        const successIds = ids.filter(item => !errorIds.includes(item));
        notification.warning(t('task.detail.batchActions.cancelFavouritePartialSuccess', { num: num - errorNum, errorNum }));

        emit('batchAction', 'favourite', successIds);

        const remainIds = props.selectedIds.filter((item) => !successIds.includes(item));
        emit('update:selectedIds', remainIds);

        for (let i = 0, len = successIds.length; i < len; i++) {
          delete selectedDataMap.value[successIds[i]];
        }
      });
    }
  });
};

const batchFollow = async () => {
  const num = props.selectedIds.length;
  modal.confirm({
    content: t('task.detail.batchActions.confirmFollow', { num }),
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
          notification.success(t('task.detail.batchActions.cancelFollowSuccess', { num }));
          emit('batchAction', 'favourite', ids);
          emit('update:selectedIds', []);
          selectedDataMap.value = {};
          return;
        }

        if (errorNum === num) {
          notification.error(t('task.detail.batchActions.cancelFollowFailed', { num }));
          return;
        }

        const successIds = ids.filter(item => !errorIds.includes(item));
        notification.warning(t('task.detail.batchActions.cancelFollowPartialSuccess', { num: num - errorNum, errorNum }));

        emit('batchAction', 'favourite', successIds);

        const remainIds = props.selectedIds.filter((item) => !successIds.includes(item));
        emit('update:selectedIds', remainIds);

        for (let i = 0, len = successIds.length; i < len; i++) {
          delete selectedDataMap.value[successIds[i]];
        }
      });
    }
  });
};

const batchCancelFollow = async () => {
  const num = props.selectedIds.length;
  modal.confirm({
    content: t('task.detail.batchActions.cancelFollowConfirm', { num }),
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
          notification.success(t('task.detail.batchActions.cancelFollowNumSuccess', { num }));
          emit('batchAction', 'favourite', ids);
          emit('update:selectedIds', []);
          selectedDataMap.value = {};
          return;
        }

        if (errorNum === num) {
          notification.error(t('task.detail.batchActions.cancelFollowFail', { num }));
          return;
        }

        const successIds = ids.filter(item => !errorIds.includes(item));
        notification.warning(t('task.detail.batchActions.cancelFollowPartialSuccess', { num: num - errorNum, errorNum }));

        emit('batchAction', 'favourite', successIds);

        const remainIds = props.selectedIds.filter((item) => !successIds.includes(item));
        emit('update:selectedIds', remainIds);

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

const batchMoveTaskOk = async (_sprintId:string, taskIds:[]) => {
  emit('batchAction', 'move', taskIds);
  emit('update:selectedIds', []);
  selectedDataMap.value = {};
};

onMounted(() => {
  watch(() => props.dataSource, (newValue) => {
    if (!newValue?.length) {
      return;
    }

    if (!checkedData.value) {
      checkedData.value = newValue[0];
      return;
    }

    const _id = checkedData.value.id;
    const existFlag = !!newValue.find(item => item.id === _id);
    if (!existFlag) {
      checkedData.value = newValue[0];
    }
  }, { immediate: true, deep: true });

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

const menuItems = computed(() => {
  const id = checkedData.value?.id;
  if (!id) {
    return [];
  }

  return props.menuItemsMap.get(id);
});
</script>
<template>
  <div class="flex-1 overflow-y-hidden">
    <div
      :visible="!!selectedIds.length"
      class="btn-group-container flex items-center transition-all duration-300 space-x-2.5">
      <Button
        :disabled="batchCancelDisabled"
        type="link"
        size="small"
        class="flex items-center px-0 h-5 leading-5"
        @click="batchCancel">
        {{ t('task.detail.batchActions.cancel') }}
      </Button>

      <Button
        :disabled="batchDeleteDisabled"
        type="link"
        size="small"
        class="flex items-center px-0 h-5 leading-5"
        @click="batchDelete">
        {{ t('task.detail.batchActions.delete') }}
      </Button>

      <Button
        :disabled="batchFavouriteDisabled"
        type="link"
        size="small"
        class="flex items-center px-0 h-5 leading-5"
        @click="batchFavourite">
        {{ t('task.detail.batchActions.favourite') }}
      </Button>

      <Button
        :disabled="batchCancelFavouriteDisabled"
        type="link"
        size="small"
        class="flex items-center px-0 h-5 leading-5"
        @click="batchCancelFavourite">
        {{ t('task.detail.batchActions.cancelFavourite') }}
      </Button>

      <Button
        :disabled="batchFollowDisabled"
        type="link"
        size="small"
        class="flex items-center px-0 h-5 leading-5"
        @click="batchFollow">
        {{ t('task.detail.batchActions.follow') }}
      </Button>

      <Button
        :disabled="batchCancelFollowDisabled"
        type="link"
        size="small"
        class="flex items-center px-0 h-5 leading-5"
        @click="batchCancelFollow">
        {{ t('task.detail.batchActions.cancelFollow') }}
      </Button>

      <Button
        :disabled="batchMoveFollowDisabled"
        type="link"
        size="small"
        class="flex items-center px-0 h-5 leading-5"
        @click="batchMove">
        {{ t('task.detail.batchActions.move') }}
      </Button>

      <Button
        danger
        type="link"
        size="small"
        class="flex items-center px-0 h-5 leading-5"
        @click="cancelBatchOperation">
        <span>{{ t('task.detail.batchActions.cancelBatchOperation') }}</span>
        <span class="ml-1">({{ selectedIds.length }})</span>
      </Button>
    </div>

    <div class="h-full relative flex-1 flex items-start overflow-hidden leading-5 detail-container">
      <List
        :dataSource="props.dataSource"
        :pagination="props.pagination"
        :checkedId="checkedData?.id"
        :selectedIds="props.selectedIds"
        @paginationChange="paginationChange"
        @checked="toChecked"
        @select="toSelect" />

      <AsyncComponent :visible="!!checkedData">
        <Details
          :id="checkedData?.id"
          :projectId="props.projectId"
          :userInfo="props.userInfo"
          :appInfo="props.appInfo"
          :menuItems="menuItems"
          :editTaskData="editTaskData"
          :linkUrl="checkedData?.linkUrl"
          type="list"
          @edit="toEdit"
          @move="toMove"
          @delete="deleteOk"
          @dataChange="dataChange"
          @refreshChange="refreshChange"
          @splitOk="emit('splitOk')" />
      </AsyncComponent>
    </div>

    <AsyncComponent :visible="moveModalVisible">
      <MoveTaskModal
        v-model:visible="moveModalVisible"
        :taskIds="props.selectedIds"
        :projectId="props.projectId"
        @ok="batchMoveTaskOk" />
    </AsyncComponent>
  </div>
</template>

<style scoped>
.detail-container::after {
  content: '';
  display: block;
  position: absolute;
  top: 0;
  left: 0;
  width: calc(100% - 20px);
  height: 1px;
  background-color: var(--border-text-box);
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
