<script setup lang="ts">
import { computed, defineAsyncComponent, inject, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Button, Popover, TabPane, Tabs } from 'ant-design-vue';
import { Icon, modal, notification, Spin } from '@xcan-angus/vue-ui';
import {toClipboard, http, utils, duration, appContext} from '@xcan-angus/infra';
import { debounce } from 'throttle-debounce';
import { cloneDeep } from 'lodash-es';
import { task } from '@/api/tester';

import { TaskInfo } from '../../../../../types';
import { ActionMenuItem } from '../../../../types';

type TabPaneKey = 'basicInfo' | 'remark' | 'testInfo' | 'comments' | 'activity';
type SprintPermissionKey = 'MODIFY_SPRINT' | 'DELETE_SPRINT' | 'ADD_TASK' | 'MODIFY_TASK' | 'DELETE_TASK' | 'EXPORT_TASK' | 'RESTART_TASK' | 'REOPEN_TASK' | 'GRANT'

type Props = {
  editTaskData: TaskInfo;
  projectId: string;
  userInfo: { id: string; };
  appInfo: { id: string; };
  id: string;
  menuItems: ActionMenuItem[];
  type: 'details' | 'list';// 页面视图，case-单独的详情页面；list-列表视图下的详情
  linkUrl: string;// 本页面的超链接地址
  notify: string;
}

const props = withDefaults(defineProps<Props>(), {
  editTaskData: undefined,
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  id: undefined,
  menuItems: () => [],
  type: 'list',
  linkUrl: '',
  notify: undefined
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (event: 'edit', value: string): void;
  (event: 'move', value: TaskInfo): void;
  (event: 'delete', value: string): void;
  (event: 'dataChange', value: Partial<TaskInfo>): void;
  (event: 'refreshChange'): void;
  (event: 'splitOk'): void;
}>();

const BasicInfo = defineAsyncComponent(() => import('@/views/task/task/list/task/flat/view/info/index.vue'));
const TestInfo = defineAsyncComponent(() => import('@/views/task/task/list/task/flat/view/testing/index.vue'));
const Remark = defineAsyncComponent(() => import('@/views/task/task/list/task/flat/view/Remark.vue'));
const Activity = defineAsyncComponent(() => import('@/views/task/task/list/task/flat/view/Activity.vue'));
const Comment = defineAsyncComponent(() => import('@/views/task/task/list/task/flat/view/Comment.vue'));
const SubTask = defineAsyncComponent(() => import('@/views/task/task/list/task/flat/view/SubTask.vue'));
const SplitTask = defineAsyncComponent(() => import('@/views/task/backlog/SplitTask.vue'));
const AssocCaseTab = defineAsyncComponent(() => import('@/views/task/task/list/task/flat/view/AssocCase.vue'));
const AssocTaskTab = defineAsyncComponent(() => import('@/views/task/task/list/task/flat/view/AssocTask.vue'));

const { t } = useI18n();
const updateTabPane = inject<(data: { [key: string]: any }) => void>('updateTabPane', () => ({}));
const replaceTabPane = inject<(id: string, data: { [key: string]: any }) => void>('replaceTabPane', () => ({}));
const deleteTabPane = inject<(value: string[]) => void>('deleteTabPane');
const windowResizeNotify = inject('windowResizeNotify', ref<string>());
const isAdmin = computed(() => appContext.isAdmin());

const domId = utils.uuid('a');
let containerDom: HTMLElement | null;
const largePageLayout = ref<boolean>();

const refreshNotify = ref<string>();
const remarkNotify = ref<string>();
const activityNotify = ref<string>();
const commentNotify = ref<string>();

const loading = ref(false);
const taskInfo = ref<TaskInfo>();
const sprintPermissions = ref<SprintPermissionKey[]>([]);

const activeKey = ref<TabPaneKey>('basicInfo');
const fullScreenFlag = ref(false);

const splitTaskVisible = ref(false);
const selectedTaskInfo = ref<TaskInfo>();

const toggleFullScreen = () => {
  fullScreenFlag.value = !fullScreenFlag.value;
};

const toEdit = () => {
  emit('edit', props.id);
};

const toSplit = () => {
  selectedTaskInfo.value = cloneDeep(taskInfo.value);
  splitTaskVisible.value = true;
};

const splitCancel = () => {
  selectedTaskInfo.value = undefined;
};

const splitOk = async () => {
  selectedTaskInfo.value = undefined;
  emit('splitOk');
  loadData();
};

const toMove = () => {
  if (!taskInfo.value) {
    return;
  }

  emit('move', taskInfo.value);
};

const toDelete = () => {
  if (!taskInfo.value) {
    return;
  }

  const { name, id } = taskInfo.value;
  modal.confirm({
    content: t('task.detail.messages.confirmDelete', { name }),
    async onOk () {
      const [error] = await task.deleteTask([id]);
      if (error) {
        return;
      }

      emit('refreshChange');
      emit('delete', id);
      notification.success(t('task.detail.messages.deleteSuccess'));
    }
  });
};

const toFavourite = async () => {
  if (!taskInfo.value) {
    return;
  }

  const { id } = taskInfo.value;
  const [error] = await task.favouriteTask(id);
  if (error) {
    return;
  }

  notification.success(t('task.detail.messages.favouriteSuccess'));
  emitDataChange({ id: id, favourite: true });
};

const toDeleteFavourite = async () => {
  if (!taskInfo.value) {
    return;
  }

  const { id } = taskInfo.value;
  const [error] = await task.cancelFavouriteTask(id);
  if (error) {
    return;
  }

  notification.success(t('task.detail.messages.cancelFavouriteSuccess'));
  emitDataChange({ id: id, favourite: false });
};

const toFollow = async () => {
  if (!taskInfo.value) {
    return;
  }

  const { id } = taskInfo.value;
  const [error] = await task.followTask(id);
  if (error) {
    return;
  }

  notification.success(t('task.detail.messages.followSuccess'));
  emitDataChange({ id: id, follow: true });
};

const toDeleteFollow = async () => {
  if (!taskInfo.value) {
    return;
  }

  const { id } = taskInfo.value;
  const [error] = await task.cancelFollowTask(id);
  if (error) {
    return;
  }

  notification.success(t('task.detail.messages.cancelFollowSuccess'));
  emitDataChange({ id: id, follow: false });
};

const toStart = async () => {
  if (!taskInfo.value) {
    return;
  }

  const { id } = taskInfo.value;
  const [error] = await task.startTask(id);
  if (error) {
    return;
  }

  emit('refreshChange');
  notification.success(t('task.messages.startSuccess'));
  const detailData = await loadData();
  emitDataChange(detailData);
};

const toProcessed = async () => {
  if (!taskInfo.value) {
    return;
  }

  const { id } = taskInfo.value;
  const [error] = await task.processedTask(id);
  if (error) {
    return;
  }

  emit('refreshChange');
  notification.success(t('task.table.messages.processedSuccess'));
  const detailData = await loadData();
  emitDataChange(detailData);
};

const toUncomplete = async () => {
  if (!taskInfo.value) {
    return;
  }

  const { id } = taskInfo.value;
  const [error] = await task.confirmTask(id, 'FAIL');
  if (error) {
    return;
  }

  emit('refreshChange');
  const detailData = await loadData();
  emitDataChange(detailData);
};

const toCompleted = async () => {
  if (!taskInfo.value) {
    return;
  }

  const { id } = taskInfo.value;
  const [error] = await task.confirmTask(id, 'SUCCESS');
  if (error) {
    return;
  }

  emit('refreshChange');
  const detailData = await loadData();
  emitDataChange(detailData);
};

const toReopen = async () => {
  if (!taskInfo.value) {
    return;
  }

  const { id } = taskInfo.value;
  const [error] = await task.reopenTask(id);
  if (error) {
    return;
  }

  emit('refreshChange');
  notification.success(t('task.table.messages.reopenSuccess'));
  const detailData = await loadData();
  emitDataChange(detailData);
};

const toRestart = async () => {
  if (!taskInfo.value) {
    return;
  }

  const { id } = taskInfo.value;
  const [error] = await task.restartTask(id);
  if (error) {
    return;
  }

  emit('refreshChange');
  notification.success(t('task.table.messages.restartSuccess'));
  const detailData = await loadData();
  emitDataChange(detailData);
};

const toCancel = async () => {
  if (!taskInfo.value) {
    return;
  }

  const { id } = taskInfo.value;
  const [error] = await task.cancelTask(id);
  if (error) {
    return;
  }

  emit('refreshChange');
  notification.success(t('task.table.messages.cancelSuccess'));
  const detailData = await loadData();
  emitDataChange(detailData);
};

const toCopyHref = () => {
  const message = window.location.origin + props.linkUrl;
  toClipboard(message).then(() => {
    notification.success(t('task.detail.messages.copyLinkSuccess'));
  }).catch(() => {
    notification.error(t('task.detail.messages.copyLinkFailed'));
  });
};

const basicInfoChange = (data: Partial<TaskInfo>) => {
  if (taskInfo.value) {
    taskInfo.value = { ...taskInfo.value, ...data };
  }

  emitDataChange(data);
  emitUpdateTabPane(data);
};

const emitDataChange = (data: Partial<TaskInfo>) => {
  taskInfo.value = { ...taskInfo.value, ...data };
  emit('dataChange', data);
};

const emitUpdateTabPane = (data: Partial<TaskInfo>) => {
  const { id, name } = data;
  if (id && name && typeof updateTabPane === 'function') {
    updateTabPane({ name, _id: id });
  }
};

const fetchPrev = async () => {
  fetchNewData(+queryParameterMap.value.pageNo - 1);
};

const fetchNext = async () => {
  fetchNewData(+queryParameterMap.value.pageNo + 1);
};

const fetchNewData = async (pageNo: number) => {
  const params: { [key: string]: any; } = Object.entries(queryParameterMap.value).reduce((prev, cur) => {
    if (!['total'].includes(cur[0])) {
      prev[cur[0]] = cur[1];
    }

    return prev;
  }, {});

  params.pageNo = pageNo;
  const queryStr = http.getURLSearchParams(params, true);
  const [error, res] = await task.getTaskList(params);
  if (error) {
    return;
  }

  const data = res?.data;
  if (data) {
    // 更新tabPane存储的信息
    const total = data.total;
    const item = data?.list?.[0];
    if (item) {
      const { id, name } = item;
      // 更新会触发props.id的监听，所以不用手动加载详情数据并替换
      replaceTabPane(props.id, { _id: id, uiKey: id, name, data: { id, query: queryStr + '&total=' + total } });
    }
  }
};

const activeKeyChange = (key: TabPaneKey) => {
  if (key === 'remark') {
    refreshRemark();
    return;
  }

  if (key === 'activity') {
    refreshActivity();
    return;
  }

  if (key === 'comments') {
    refreshComment();
  }
};

const toRefresh = () => {
  loadData();

  refreshNotify.value = utils.uuid();

  if (activeKey.value === 'remark') {
    refreshRemark();
    return;
  }

  if (activeKey.value === 'activity') {
    refreshActivity();
    return;
  }

  if (activeKey.value === 'comments') {
    refreshComment();
  }
};

const refreshRemark = () => {
  remarkNotify.value = refreshNotify.value;
};

const refreshActivity = () => {
  activityNotify.value = refreshNotify.value;
};

const refreshComment = () => {
  commentNotify.value = refreshNotify.value;
};

const loadData = async (): Promise<Partial<TaskInfo>> => {
  const id = props.id;
  loading.value = true;
  const [error, res] = await task.getTaskDetail(id);
  loading.value = false;
  if (error) {
    if (error.ext?.eKey === 'resource_not_found') {
      if (typeof deleteTabPane === 'function') {
        deleteTabPane([id]);
      }

      return { id };
    }

    return { id };
  }

  if (!res?.data) {
    return { id };
  }

  const data = (res?.data || { id }) as TaskInfo;

  taskInfo.value = {
    ...data
  };

  // 更新tabpane的名称
  emitUpdateTabPane(data);

  return data;
};

const loadPermissions = async (id: string | undefined) => {
  if (!isAdmin.value && id) {
    const params = {
      admin: true
    };

    const [error, res] = await task.getUserSprintAuth(id, props.userInfo?.id, params);
    if (error) {
      return;
    }

    const data = (res?.data || []).map(item => item.value);
    sprintPermissions.value = data;
  } else {
    sprintPermissions.value = [
      'MODIFY_SPRINT',
      'DELETE_SPRINT',
      'ADD_TASK',
      'MODIFY_TASK',
      'DELETE_TASK',
      'EXPORT_TASK',
      'RESTART_TASK',
      'REOPEN_TASK',
      'GRANT'
    ];
  }
};

const refreshChange = async () => {
  const data = await loadData();
  if (!props.menuItems?.length) {
    loadPermissions(data.sprintId);
  }
};

const resizeHandler = debounce(duration.resize, () => {
  if (!containerDom) {
    containerDom = document.getElementById(domId);
  }

  if (!containerDom) {
    return;
  }

  largePageLayout.value = containerDom.offsetWidth >= 960;
});

onMounted(() => {
  resizeHandler();

  watch(() => props.id, async (newValue) => {
    if (!newValue) {
      return;
    }

    activeKey.value = 'basicInfo';

    const data = await loadData();
    if (!props.menuItems?.length) {
      loadPermissions(data.sprintId);
    }
  }, { immediate: true });

  watch(() => props.editTaskData, async (newValue) => {
    if (!newValue || props.id !== newValue.id) {
      return;
    }

    taskInfo.value = newValue;
  }, { immediate: true });

  watch(() => windowResizeNotify.value, (newValue) => {
    if (newValue === undefined || newValue === null || newValue === '') {
      return;
    }

    resizeHandler();
  }, { immediate: true });

  watch(() => props.notify, async (newValue) => {
    if (newValue === undefined || newValue === null || newValue === '') {
      return;
    }

    const data = await loadData();
    if (!props.menuItems?.length) {
      loadPermissions(data.sprintId);
    }
  }, { immediate: true });
});

const queryParameterMap = computed(() => {
  if (!props.linkUrl) {
    return {};
  }

  const queryStr = props.linkUrl.split('?')[1];
  if (queryStr) {
    return queryStr.split('&').reduce((prev, cur) => {
      const [key, value] = cur.split('=');
      prev[key] = value;
      return prev;
    }, {} as { [key: string]: any; });
  }

  return {};
});

const prevBtnDisabled = computed(() => {
  const { pageNo } = queryParameterMap.value;
  if (pageNo === undefined || pageNo === null || pageNo === '') {
    return true;
  }

  return +pageNo === 1;
});

const nextBtnDisabled = computed(() => {
  const { pageNo, total } = queryParameterMap.value;
  if (pageNo === undefined || pageNo === null || pageNo === '' ||
    total === undefined || total === null || total === ''
  ) {
    return true;
  }

  return +pageNo === +total;
});

const showTestInfo = computed(() => {
  const taskType = taskInfo.value?.taskType?.value;
  if (!taskType) {
    return false;
  }

  return ['API_TEST', 'SCENARIO_TEST'].includes(taskType);
});

const menuItemsMap = computed(() => {
  const map: { [key: string]: ActionMenuItem } = {};
  const data = taskInfo.value;
  if (data) {
    const status = data.status?.value;
    const { currentAssociateType, confirmorId, assigneeId, favourite, follow, sprintAuth } = data;

    const userId = props.userInfo?.id;
    const isAdministrator = !!currentAssociateType?.map(item => item.value).includes('SYS_ADMIN' || 'APP_ADMIN');
    const isConfirmor = confirmorId === userId;
    const isAssignee = assigneeId === userId;
    // const isCreator = createdBy === userId;

    let permissions = props.menuItems || [];
    if (sprintPermissions.value?.length) {
      permissions = sprintPermissions.value;
    }

    const menuItems: ActionMenuItem[] = [
      {
        name: t('actions.edit'),
        key: 'edit',
        icon: 'icon-shuxie',
        disabled: !isAdministrator && !permissions.includes('MODIFY_TASK') && sprintAuth,
        hide: true
      },
      {
        name: t('actions.delete'),
        key: 'delete',
        icon: 'icon-qingchu',
        disabled: !isAdministrator && !permissions.includes('DELETE_TASK') && sprintAuth,
        hide: true
      },
      {
        name: t('task.detail.actions.split'),
        key: 'split',
        icon: 'icon-guanlianziyuan',
        disabled: !isAdministrator && !permissions.includes('MODIFY_TASK') && sprintAuth,
        hide: true
      }
    ];

    if (status === 'PENDING') {
      menuItems.push({
        name: t('task.actions.start'),
        key: 'start',
        icon: 'icon-kaishi',
        disabled: !isAdministrator && !isAssignee,
        hide: false
      });
    }

    if (status === 'IN_PROGRESS') {
      menuItems.push({
        name: t('task.actions.complete'),
        key: 'processed',
        icon: 'icon-yichuli',
        disabled: !isAdministrator && !isAssignee,
        hide: false
      });
    }

    if (status === 'CONFIRMING') {
      menuItems.push({
        name: t('task.actions.confirmComplete'),
        key: 'completed',
        icon: 'icon-yiwancheng',
        disabled: !isAdministrator && !isConfirmor,
        hide: false
      });

      menuItems.push({
        name: t('task.actions.confirmIncomplete'),
        key: 'uncompleted',
        icon: 'icon-shibaiyuanyin',
        disabled: !isAdministrator && !isConfirmor,
        hide: false
      });
    }

    if (status === 'CANCELED' || status === 'COMPLETED') {
      menuItems.push({
        name: t('task.actions.reopen'),
        key: 'reopen',
        icon: 'icon-zhongxindakaiceshirenwu',
        disabled: !isAdministrator && !permissions.includes('REOPEN_TASK') && !isAssignee,
        hide: false,
        tip: t('task.tips.reopenTip')
      });

      menuItems.push({
        name: t('task.actions.restart'),
        key: 'restart',
        icon: 'icon-zhongxinkaishiceshi',
        disabled: !isAdministrator && !permissions.includes('RESTART_TASK'),
        hide: false,
        tip: t('task.tips.restartTip')
      });
    }

    if (status !== 'CANCELED' && status !== 'COMPLETED') {
      menuItems.push({
        name: t('actions.cancel'),
        key: 'cancel',
        icon: 'icon-zhongzhi2',
        disabled: !isAdministrator && !permissions.includes('MODIFY_TASK') && sprintAuth,
        hide: false
      });
    }

    if (favourite) {
      menuItems.push({
        name: t('task.actions.unfavorite'),
        key: 'cancelFavourite',
        icon: 'icon-quxiaoshoucang',
        disabled: false,
        hide: false
      });
    } else {
      menuItems.push({
        name: t('task.actions.favorite'),
        key: 'favourite',
        icon: 'icon-yishoucang',
        disabled: false,
        hide: false
      });
    }

    if (follow) {
      menuItems.push({
        name: t('task.actions.unfollow'),
        key: 'cancelFollow',
        icon: 'icon-quxiaoguanzhu',
        disabled: false,
        hide: false
      });
    } else {
      menuItems.push({
        name: t('task.actions.follow'),
        key: 'follow',
        icon: 'icon-yiguanzhu',
        disabled: false,
        hide: false
      });
    }

    menuItems.push({
      name: t('task.actions.move'),
      key: 'move',
      icon: 'icon-yidong',
      disabled: !isAdministrator && !permissions.includes('MODIFY_TASK') && sprintAuth,
      hide: false
    });

    menuItems.push({
      name: t('task.copyLink'),
      key: 'copyLink',
      icon: 'icon-fuzhi',
      disabled: false,
      hide: false
    });

    for (let i = 0, len = menuItems.length; i < len; i++) {
      const item = menuItems[i];
      map[item.key] = item;
    }
  }

  return map;
});

const className = computed(() => {
  return fullScreenFlag.value ? 'fixed-full' : '';
});

const getRefTaskNum = (type = 'TASK') => {
  return (taskInfo.value?.refTaskInfos || []).filter(item => item.taskType.value === type).length || 0;
};
</script>

<template>
  <Spin
    :id="domId"
    :spinning="loading"
    :class="className"
    class="flex-1 h-full pt-3.5 pl-3.5 pb-3 overflow-hidden">
    <div class="flex items-start justify-between pr-5">
      <div class="flex items-start flex-wrap space-y-b-2 space-x-r-2.5">
        <Button
          v-if="menuItemsMap.start"
          :disabled="!!menuItemsMap.start?.disabled"
          type="primary"
          size="small"
          class="flex items-center"
          @click="toStart">
          <Icon class="mr-1 flex-shrink-0 text-3.5" icon="icon-kaishi" />
          <span>{{ t('task.actions.start') }}</span>
        </Button>

        <Button
          v-if="menuItemsMap.processed"
          :disabled="!!menuItemsMap.processed?.disabled"
          type="primary"
          size="small"
          class="flex items-center"
          @click="toProcessed">
          <Icon class="mr-1 flex-shrink-0 text-3.5" icon="icon-kaishi" />
          <span>{{ t('task.actions.complete') }}</span>
        </Button>

        <Button
          v-if="menuItemsMap.completed"
          :disabled="!!menuItemsMap.completed?.disabled"
          type="primary"
          size="small"
          class="flex items-center"
          @click="toCompleted">
          <Icon class="mr-1 flex-shrink-0 text-3.5" icon="icon-kaishi" />
          <span>{{ t('task.actions.confirmComplete') }}</span>
        </Button>

        <Button
          v-if="menuItemsMap.uncompleted"
          :disabled="!!menuItemsMap.uncompleted?.disabled"
          type="primary"
          size="small"
          class="flex items-center"
          @click="toUncomplete">
          <Icon class="mr-1 flex-shrink-0 text-3.5" icon="icon-kaishi" />
          <span>{{ t('task.actions.confirmIncomplete') }}</span>
        </Button>

        <Button
          v-if="menuItemsMap.reopen"
          :disabled="!!menuItemsMap.reopen?.disabled"
          type="primary"
          size="small"
          class="flex items-center"
          @click="toReopen">
          <Icon class="mr-1 flex-shrink-0 text-3.5" icon="icon-kaishi" />
          <span>{{ t('task.actions.reopen') }}</span>
          <Popover placement="bottom">
            <template #content>
              <div class="text-3 text-theme-sub-content max-w-75 leading-4">
                {{ t('task.tips.reopenTip') }}
              </div>
            </template>
            <Icon icon="icon-tishi1" class="text-tips ml-1" />
          </Popover>
        </Button>

        <Button
          v-if="menuItemsMap.restart"
          :disabled="!!menuItemsMap.restart?.disabled"
          type="primary"
          size="small"
          class="flex items-center"
          @click="toRestart">
          <Icon class="mr-1 flex-shrink-0 text-3.5" icon="icon-kaishi" />
          <span>{{ t('task.actions.restart') }}</span>
          <Popover placement="bottom">
            <template #content>
              <div class="text-3 text-theme-sub-content max-w-75 leading-4">
                {{ t('task.tips.restartTip') }}
              </div>
            </template>
            <Icon icon="icon-tishi1" class="text-tips ml-1" />
          </Popover>
        </Button>

        <Button
          v-if="menuItemsMap.edit"
          :disabled="!!menuItemsMap.edit?.disabled"
          size="small"
          class="flex items-center"
          @click="toEdit">
          <Icon class="mr-1 flex-shrink-0 text-3.5" icon="icon-shuxie" />
          <span>{{ t('task.detail.actions.edit') }}</span>
        </Button>

        <Button
          v-if="menuItemsMap.split"
          :disabled="!!menuItemsMap.split?.disabled"
          size="small"
          class="flex items-center"
          @click="toSplit">
          <Icon class="mr-1 flex-shrink-0 text-3.5" icon="icon-guanlianziyuan" />
          <span>{{ t('task.detail.actions.split') }}</span>
        </Button>

        <Button
          v-if="menuItemsMap.cancel"
          :disabled="!!menuItemsMap.cancel?.disabled"
          size="small"
          class="flex items-center"
          @click="toCancel">
          <Icon class="mr-1 flex-shrink-0 text-3.5" icon="icon-zhongzhi2" />
          <span>{{ t('actions.cancel') }}</span>
        </Button>

        <Button
          v-if="menuItemsMap.delete"
          :disabled="!!menuItemsMap.delete?.disabled"
          size="small"
          class="flex items-center"
          @click="toDelete">
          <Icon class="mr-1 flex-shrink-0 text-3.5" icon="icon-qingchu" />
          <span>{{ t('actions.delete') }}</span>
        </Button>

        <Button
          v-if="menuItemsMap.favourite"
          :disabled="!!menuItemsMap.favourite?.disabled"
          size="small"
          class="flex items-center"
          @click="toFavourite">
          <Icon class="mr-1 flex-shrink-0 text-3.5" icon="icon-yishoucang" />
          <span>{{ t('task.actions.favorite') }}</span>
        </Button>

        <Button
          v-if="menuItemsMap.cancelFavourite"
          :disabled="!!menuItemsMap.cancelFavourite?.disabled"
          size="small"
          class="flex items-center"
          @click="toDeleteFavourite">
          <Icon class="mr-1 flex-shrink-0 text-3.5" icon="icon-quxiaoshoucang" />
          <span>{{ t('task.actions.unfavorite') }}</span>
        </Button>

        <Button
          v-if="menuItemsMap.follow"
          :disabled="!!menuItemsMap.follow?.disabled"
          size="small"
          class="flex items-center"
          @click="toFollow">
          <Icon class="mr-1 flex-shrink-0 text-3.5" icon="icon-yiguanzhu" />
          <span>{{ t('task.actions.follow') }}</span>
        </Button>

        <Button
          v-if="menuItemsMap.cancelFollow"
          :disabled="!!menuItemsMap.cancelFollow?.disabled"
          size="small"
          class="flex items-center"
          @click="toDeleteFollow">
          <Icon class="mr-1 flex-shrink-0 text-3.5" icon="icon-quxiaoguanzhu" />
          <span>{{ t('task.actions.unfollow') }}</span>
        </Button>

        <Button
          v-if="menuItemsMap.move"
          :disabled="!!menuItemsMap.move?.disabled"
          size="small"
          class="flex items-center"
          @click="toMove">
          <Icon class="mr-1 flex-shrink-0 text-3.5" icon="icon-yidong" />
          <span>{{ t('task.detail.actions.move') }}</span>
        </Button>

        <Button
          size="small"
          class="flex items-center"
          @click="toCopyHref">
          <Icon class="mr-1 flex-shrink-0 text-3.5" icon="icon-fuzhi" />
          <span>{{ t('task.detail.actions.copyLink') }}</span>
        </Button>

        <!-- <Button
          type="default"
          size="small"
          class="flex items-center"
          @click="toRefresh">
          <Icon class="mr-1 flex-shrink-0 text-3.5" icon="icon-shuaxin" />
          <span>{{ t('task.detail.actions.refresh') }}</span>
        </Button> -->

        <Button
          v-if="props.type === 'list'"
          type="default"
          size="small"
          class="flex items-center"
          @click="toggleFullScreen">
          <template v-if="fullScreenFlag">
            <Icon class="mr-1 flex-shrink-0 text-3.5" icon="icon-tuichuzuida" />
            <span>{{ t('task.detail.actions.exitFullScreen') }}</span>
          </template>

          <template v-else>
            <Icon class="mr-1 flex-shrink-0 text-3.5" icon="icon-zuidahua" />
            <span>{{ t('task.detail.actions.fullScreen') }}</span>
          </template>
        </Button>
      </div>

      <div v-if="props.type === 'details' && !(prevBtnDisabled && nextBtnDisabled)" class="flex items-start">
        <Button
          size="small"
          class="flex items-center mr-2.5"
          :disabled="prevBtnDisabled"
          @click="fetchPrev">
          <Icon class="mr-1 flex-shrink-0 text-3.5" icon="icon-chakanshangyitiao" />
          <span>{{ prevBtnDisabled ? t('task.detail.noMore') : t('task.detail.previousTask') }}</span>
        </Button>
        <Button
          size="small"
          class="flex items-center"
          :disabled="nextBtnDisabled"
          @click="fetchNext">
          <Icon class="mr-1 flex-shrink-0 text-3.5" icon="icon-chakanxiayitiao" />
          <span>{{ nextBtnDisabled ? t('task.detail.noMore') : t('task.detail.nextTask') }}</span>
        </Button>
      </div>
    </div>

    <Tabs
      v-model:activeKey="activeKey"
      size="small"
      style="height: calc(100% - 36px);"
      @change="activeKeyChange">
      <template #rightExtra>
        <Button
          type="link"
          size="small"
          class="flex items-center mr-4"
          @click="toRefresh">
          <Icon class="mr-1 flex-shrink-0 text-3.5" icon="icon-shuaxin" />
          <span>{{ t('task.detail.actions.refresh') }}</span>
        </Button>
      </template>
      <TabPane key="basicInfo" :tab="t('task.detail.tabs.basicInfo')">
        <BasicInfo
          v-if="largePageLayout === false || largePageLayout === true"
          v-model:loading="loading"
          :projectId="props.projectId"
          :userInfo="props.userInfo"
          :appInfo="props.appInfo"
          :dataSource="taskInfo"
          :largePageLayout="largePageLayout"
          @change="basicInfoChange" />
      </TabPane>

      <TabPane key="taskChild">
        <template #tab>
          <div class="inline-flex">
            <span>{{ t('task.detail.tabs.subTask') }}</span>
            <span>({{ taskInfo?.subTaskInfos?.length || 0 }})</span>
          </div>
        </template>
        <SubTask
          v-model:loading="loading"
          :projectId="props.projectId"
          :userInfo="props.userInfo"
          :appInfo="props.appInfo"
          :taskInfo="taskInfo"

          @refreshChange="refreshChange" />
      </TabPane>

      <TabPane key="asscoTask">
        <template #tab>
          <div class="inline-flex">
            <span>{{ t('task.detail.tabs.assocTask') }}</span>
            <span>({{ getRefTaskNum('TASK') }})</span>
          </div>
        </template>
        <AssocTaskTab
          key="TASK"
          :projectId="props.projectId"
          :userInfo="props.userInfo"
          :appInfo="props.appInfo"
          :dataSource="taskInfo?.refTaskInfos || []"
          :taskId="props.id"
          :title="t('task.detail.assocTaskTab.title')"
          :tips="t('task.detail.assocTaskTab.tips')"
          taskType="TASK"
          @editSuccess="loadData" />
      </TabPane>
      <TabPane key="asscoRequirements">
        <template #tab>
          <div class="inline-flex">
            <span>{{ t('task.detail.asscoRequirements.title') }}</span>
            <span>({{ getRefTaskNum('REQUIREMENT') }})</span>
          </div>
        </template>
        <AssocTaskTab
          key="REQUIREMENT"
          :projectId="props.projectId"
          :userInfo="props.userInfo"
          :appInfo="props.appInfo"
          :dataSource="taskInfo?.refTaskInfos || []"
          :taskId="props.id"
          :title="t('task.detail.asscoRequirements.title')"
          :tips="t('task.detail.asscoRequirements.tips')"
          taskType="REQUIREMENT"
          @editSuccess="loadData" />
      </TabPane>
      <TabPane key="asscoStory">
        <template #tab>
          <div class="inline-flex">
            <span>{{ t('task.detail.asscoStory.title') }}</span>
            <span>({{ getRefTaskNum('STORY') }})</span>
          </div>
        </template>
        <AssocTaskTab
          key="STORY"
          :projectId="props.projectId"
          :userInfo="props.userInfo"
          :appInfo="props.appInfo"
          :dataSource="taskInfo?.refTaskInfos || []"
          :taskId="props.id"
          :title="t('task.detail.asscoStory.title')"
          :tips="t('task.detail.asscoStory.tips')"
          taskType="STORY"
          @editSuccess="loadData" />
      </TabPane>
      <TabPane key="asscoBug">
        <template #tab>
          <div class="inline-flex">
            <span>{{ t('task.detail.asscoBug.title') }}</span>
            <span>({{ getRefTaskNum('BUG') }})</span>
          </div>
        </template>
        <AssocTaskTab
          key="BUG"
          :projectId="props.projectId"
          :userInfo="props.userInfo"
          :appInfo="props.appInfo"
          :dataSource="taskInfo?.refTaskInfos || []"
          :taskId="props.id"
          :title="t('task.detail.asscoBug.title')"
          :tips="t('task.detail.asscoBug.tips')"
          taskType="BUG"
          @editSuccess="loadData" />
      </TabPane>
      <TabPane key="asscoCase">
        <template #tab>
          <div class="inline-flex">
            <span>{{ t('task.detail.asscoCase.title') }}</span>
            <span>({{ taskInfo?.refCaseInfos?.length || 0 }})</span>
          </div>
        </template>
        <AssocCaseTab
          :projectId="props.projectId"
          :userInfo="props.userInfo"
          :appInfo="props.appInfo"
          :dataSource="taskInfo?.refCaseInfos || []"
          :taskId="props.id"
          @editSuccess="loadData" />
      </TabPane>
      <TabPane key="asscoApiTest">
        <template #tab>
          <div class="inline-flex">
            <span>{{ t('task.detail.asscoApiTest.title') }}</span>
            <span>({{ getRefTaskNum('API_TEST') }})</span>
          </div>
        </template>
        <AssocTaskTab
          key="API_TEST"
          :projectId="props.projectId"
          :userInfo="props.userInfo"
          :appInfo="props.appInfo"
          :dataSource="taskInfo?.refTaskInfos || []"
          :taskId="props.id"
          :title="t('task.detail.asscoApiTest.title')"
          :tips="t('task.detail.asscoApiTest.tips')"
          taskType="API_TEST"
          @editSuccess="loadData" />
      </TabPane>
      <TabPane key="asscoScenTest">
        <template #tab>
          <div class="inline-flex">
            <span>{{ t('task.detail.asscoScenarioTest.title') }}</span>
            <span>({{ getRefTaskNum('SCENARIO_TEST') }})</span>
          </div>
        </template>
        <AssocTaskTab
          key="SCENARIO_TEST"
          :projectId="props.projectId"
          :userInfo="props.userInfo"
          :appInfo="props.appInfo"
          :dataSource="taskInfo?.refTaskInfos || []"
          :taskId="props.id"
          :title="t('task.detail.asscoScenarioTest.title')"
          :tips="t('task.detail.asscoScenarioTest.tips')"
          taskType="SCENARIO_TEST"
          @editSuccess="loadData" />
      </TabPane>

      <TabPane
        v-if="showTestInfo"
        key="testInfo"
        :tab="t('task.detail.tabs.testInfo')">
        <TestInfo
          :projectId="props.projectId"
          :userInfo="props.userInfo"
          :appInfo="props.appInfo"
          :dataSource="taskInfo"
          :largePageLayout="largePageLayout" />
      </TabPane>

      <TabPane key="remark">
        <template #tab>
          <div class="inline-flex">
            <span>{{ t('task.detail.tabs.remark') }}</span>
            <span>({{ taskInfo?.remarkNum || 0 }})</span>
          </div>
        </template>
        <Remark
          :id="props.id"
          v-model:notify="remarkNotify"
          :projectId="props.projectId"
          :userInfo="props.userInfo"
          :appInfo="props.appInfo" />
      </TabPane>

      <TabPane key="comments">
        <template #tab>
          <div class="inline-flex">
            <span>{{ t('task.detail.tabs.comments') }}</span>
            <span>({{ taskInfo?.commentNum || 0 }})</span>
          </div>
        </template>
        <Comment
          :id="props.id"
          :notify="commentNotify"
          :projectId="props.projectId"
          :userInfo="props.userInfo"
          :appInfo="props.appInfo" />
      </TabPane>

      <TabPane key="activity">
        <template #tab>
          <div class="inline-flex">
            <span>{{ t('task.detail.tabs.activity') }}</span>
            <span>({{ taskInfo?.activityNum || 0 }})</span>
          </div>
        </template>
        <Activity
          :id="props.id"
          :notify="activityNotify"
          :projectId="props.projectId"
          :userInfo="props.userInfo"
          :appInfo="props.appInfo" />
      </TabPane>
    </Tabs>

    <AsyncComponent :visible="splitTaskVisible">
      <SplitTask
        v-model:visible="splitTaskVisible"
        :projectId="props.projectId"
        :userInfo="props.userInfo"
        :appInfo="props.appInfo"
        :taskInfo="selectedTaskInfo"
        @ok="splitOk"
        @cancel="splitCancel" />
    </AsyncComponent>
  </Spin>
</template>

<style scoped>
.fixed-full {
  position: fixed;
  inset: 0;
  z-index: 999;
  padding: 20px 0 20px 20px;
  background-color: #fff;
}
:deep(.ant-tabs-nav::before) {
  border: 0;
}

:deep(.ant-tabs-top>.ant-tabs-nav::before) {
  right: 20px;
}

.space-x-r-2\.5>*+* {
  margin-right: 10px;
}

.space-y-b-2>*+* {
  margin-bottom: 10px;
}

.space-x-r-2\.5>*:first-child {
  margin-right: 10px;
}

.space-y-b-2>*:first-child {
  margin-bottom: 10px;
}
</style>
