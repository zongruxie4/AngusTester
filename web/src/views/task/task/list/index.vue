<script setup lang="ts">
import { computed, defineAsyncComponent, inject, onMounted, ref, watch, Ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { useRouter } from 'vue-router';
import { AsyncComponent, NoData, notification, Spin } from '@xcan-angus/vue-ui';
import { http, utils, TESTER, download } from '@xcan-angus/infra';
import { isEqual } from 'lodash-es';
import { modules, task } from '@/api/tester';

import { getCurrentPage } from '@/utils/utils';
import { TaskInfo } from '../../PropsType';
import { ActionMenuItem, travelTreeData } from './PropsType';
// eslint-disable-next-line import/no-absolute-path
import Template from '/file/Import_Task_Template.xlsx?url';

type SprintPermissionKey = 'MODIFY_SPRINT' | 'DELETE_SPRINT' | 'ADD_TASK' | 'MODIFY_TASK' | 'DELETE_TASK' | 'EXPORT_TASK' | 'RESTART_TASK' | 'REOPEN_TASK' | 'GRANT'

type Props = {
  sprintId: string;
  sprintName: string;
  projectId: string;
  userInfo: { id: string; };
  appInfo: { id: string; };
  notify: string;
  projectName: string;
}

const props = withDefaults(defineProps<Props>(), {
  sprintId: undefined,
  sprintName: undefined,
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  notify: undefined
});

const StatisticsPanel = defineAsyncComponent(() => import('@/views/task/task/list/statisticsPanel/index.vue'));
const SearchPanel = defineAsyncComponent(() => import('@/views/task/task/list/searchPanel/index.vue'));
const Edit = defineAsyncComponent(() => import('@/views/task/task/list/task/edit/index.vue'));
const Move = defineAsyncComponent(() => import('@/views/task/task/list/task/move/index.vue'));
const FlowChart = defineAsyncComponent(() => import('@/views/task/task/list/task/flowChart/index.vue'));
const Table = defineAsyncComponent(() => import('@/views/task/task/list/task/table/index.vue'));
const DetailView = defineAsyncComponent(() => import('@/views/task/task/list/task/detail/index.vue'));
const KanbanView = defineAsyncComponent(() => import('@/views/task/task/list/task/kanban/index.vue'));
const GanttView = defineAsyncComponent(() => import('@/views/task/task/list/task/gantt/index.vue'));
const Upload = defineAsyncComponent(() => import('@/views/task/task/list/task/upload/index.vue'));
const ModuleTree = defineAsyncComponent(() => import('./moduleTree.vue'));

const { t } = useI18n();
const deleteTabPane = inject<(value: string[]) => void>('deleteTabPane');
const isAdmin = inject('isAdmin', ref(false));
const proTypeShowMap = inject<Ref<{[key: string]: boolean}>>('proTypeShowMap', ref({ showTask: true, showBackLog: true, showMeeting: true, showSprint: true, showTasStatistics: true }));

const router = useRouter();

const countRefreshNotify = ref<string>();// 单独刷新统计图表
const refreshNotify = ref<string>();

const boardsGroupKey = ref<'none' | 'assigneeName' | 'lastModifiedByName' | 'taskType'>('none');
const boardsOrderBy = ref<'priority' | 'deadlineDate' | 'createdByName' | 'assigneeName'>();
const boardsOrderSort = ref<'DESC' | 'ASC'>();

const collapseFlag = ref(true);// 初始折叠，如果没有手动折叠，则会在初始化完成后自动展开
const viewMode = ref<'table' | 'detail' | 'kanban' | 'gantt'>('detail');
const moduleFlag = ref(false);
const flowChartVisible = ref(false);
const exporting = ref(false);

const loaded = ref(false);
const loading = ref(false);
const orderBy = ref<string>();
const orderSort = ref<'DESC' | 'ASC'>();
const filters = ref<{ key: string; op: string; value: boolean | string | string[]; }[]>([]);
const pagination = ref<{ current: number; pageSize: number; total: number; }>({ current: 1, pageSize: 10, total: 0 });
const taskList = ref<TaskInfo[]>([]);
const sprintPermissionsMap = ref<Map<string, SprintPermissionKey[]>>(new Map());

const selectedTaskName = ref<string>();
const selectedTaskSprintId = ref<string>();
const selectedTaskId = ref<string>();
const taskModalVisible = ref(false);
const moveModalVisible = ref(false);
const uploadTaskVisible = ref(false);

const searchSprintId = ref<string>();
const editTaskData = ref<TaskInfo>();// 编辑弹窗的任务

const selectedIds = ref<string[]>([]);// 批量选中的任务

const prevParams = ref<{[key:string]:any}>();

const getParams = () => {
  const params: {
    backlog?: boolean,
    projectId: string;
    pageNo: number;
    pageSize: number;
    filters?: { key: string; op: string; value: boolean | string | string[]; }[];
    orderBy?: string;
    orderSort?: 'DESC' | 'ASC';
  } = {
    backlog: proTypeShowMap.value.showBackLog ? false : undefined,
    projectId: props.projectId,
    pageNo: pagination.value.current,
    pageSize: pagination.value.pageSize
  };

  if (filters.value.length) {
    params.filters = filters.value;
  }

  if (orderSort.value) {
    params.orderBy = orderBy.value;
    params.orderSort = orderSort.value;
  }

  return params;
};

const loadData = async () => {
  if (!viewMode.value || !['detail', 'table'].includes(viewMode.value)) {
    return;
  }

  loading.value = false;
  const params = getParams();
  const [error, res] = await task.getTaskList({ ...params, moduleId: moduleId.value });
  loading.value = false;
  loaded.value = true;
  if (error) {
    return false;
  }

  const data = (res?.data || { total: '0', list: [] }) as { total: string; list: TaskInfo[] };
  const total = +data.total;
  pagination.value.total = total;

  const pageNo = +params.pageNo;
  const pageSize = +params.pageSize;
  const _list = (data.list || []);
  const newList: TaskInfo[] = [];
  const sprintIdSet = new Set<string>();
  for (let i = 0, len = _list.length; i < len; i++) {
    const item = _list[i];
    const _params = {
      ...params,
      taskId: item.id,
      pageNo: (pageNo - 1) * pageSize + i + 1,
      pageSize: 1,
      total
    };

    newList.push({
      ...item,
      linkUrl: '/task#task?' + http.getURLSearchParams(_params, true)
    });

    sprintIdSet.add(item.sprintId);
  }

  taskList.value = newList;

  // 除了页码还有其他条件不一致，说明搜索条件发生改变，需要把选中的任务重置
  if (prevParams.value) {
    delete prevParams.value.pageNo;
    delete params.pageNo;
    if (!isEqual(prevParams.value, params)) {
      selectedIds.value = [];
    }
  }

  prevParams.value = params;

  sprintPermissionsMap.value.clear();
  const sprintIds = Array.from(sprintIdSet);
  // 管理员拥有所有权限，无需加载权限
  if (!isAdmin.value && proTypeShowMap.value.showSprint) {
    for (let i = 0, len = sprintIds.length; i < len; i++) {
      const id = sprintIds[i];
      if (id) {
        const [_error, _res] = await loadPermissions(id);
        if (!_error) {
          const _permissions = (_res?.data || []).map(item => item.value);
          sprintPermissionsMap.value.set(id, _permissions);
        }
      }
    }
  } else {
    for (let i = 0, len = sprintIds.length; i < len; i++) {
      const id = sprintIds[i];
      sprintPermissionsMap.value.set(id, [
        'MODIFY_SPRINT',
        'DELETE_SPRINT',
        'ADD_TASK',
        'MODIFY_TASK',
        'DELETE_TASK',
        'EXPORT_TASK',
        'RESTART_TASK',
        'REOPEN_TASK',
        'GRANT'
      ]);
    }
  }
};

const loadPermissions = async (id: string) => {
  const params = {
    admin: true
  };

  return await task.getUserSprintAuth(id, props.userInfo?.id, params);
};

const searchPanelChange = (data: { key: string; op: string; value: boolean | string | string[]; }[]) => {
  filters.value = data;
  pagination.value.current = 1;
  loadData();
};

const tableChange = ({ current, pageSize }: { current: number; pageSize: number; },
  sorter: { orderBy: string; orderSort: 'DESC' | 'ASC' }) => {
  pagination.value.current = current;
  pagination.value.pageSize = pageSize;

  orderBy.value = sorter.orderBy;
  orderSort.value = sorter.orderSort;

  loadData();
};

const paginationChange = (data: { current: number; pageSize: number; }) => {
  pagination.value.current = data.current;
  pagination.value.pageSize = data.pageSize;
  loadData();
};

const toCreate = () => {
  // 设置当前搜索条件中选中的迭代
  const item = filters.value.find(item => item.key === 'sprintId');
  searchSprintId.value = item?.value as string;
  taskModalVisible.value = true;
};

// 打开上传任务弹窗
const openUploadTaskModal = () => {
  uploadTaskVisible.value = true;
};

const cancelUploadTask = () => {
  uploadTaskVisible.value = false;
};

const handleUploadTaskOk = () => {
  loadData();
};

// 导出任务模板
const handdleExportTemplate = async () => {
  const a = document.createElement('a');
  a.style.display = 'none';
  a.href = Template;
  a.download = 'Import_Task_Template.xlsx';
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
};

const toExport = async () => {
  if (exporting.value) {
    notification.info(t('task.messages.exportingInProgress'));
    return;
  }

  exporting.value = true;
  const url = `${TESTER}/task/export?` + http.getURLSearchParams({ projectId: props.projectId, filters: filters.value }, true);
  await download(url);
  exporting.value = false;
};

const refreshChange = () => {
  // 通知统计面板刷新
  countRefreshNotify.value = utils.uuid();
};

// 通知统计面板刷新
const splitOk = () => {
  countRefreshNotify.value = utils.uuid();
  loadData();
};

const viewModeChange = (value:'table' | 'detail' | 'kanban' | 'gantt') => {
  if (['kanban', 'gantt'].includes(viewMode.value) && ['table', 'detail'].includes(value)) {
    viewMode.value = value;
    pagination.value.current = 1;
    loadData();
  }

  viewMode.value = value;
};

const toEdit = (id: string) => {
  selectedTaskId.value = id;
  toCreate();
};

const editOk = (data: TaskInfo, addFlag = false) => {
  if (addFlag) {
    // 新增任务，刷新表格
    loadData();
    return;
  }

  // 编辑任务，查询任务详情替换表格的数据
  if (data) {
    setTableData(data);
  }
};

const deleteOk = (id: string) => {
  pagination.value.current = getCurrentPage(pagination.value.current, pagination.value.pageSize, pagination.value.total);
  loadData();
  if (typeof deleteTabPane === 'function') {
    deleteTabPane([id]);
  }
};

const toMove = (data: TaskInfo) => {
  selectedTaskSprintId.value = data.sprintId;
  selectedTaskName.value = data.name;
  selectedTaskId.value = data.id;
  moveModalVisible.value = true;
};

const moveTaskOk = () => {
  selectedTaskSprintId.value = undefined;
  selectedTaskName.value = undefined;
  selectedTaskId.value = undefined;
  pagination.value.current = getCurrentPage(pagination.value.current, pagination.value.pageSize, pagination.value.total);
  loadData();
};

const batchAction = (key: 'cancel' | 'delete' | 'follow' | 'favourite' | 'move', ids: string[]) => {
  switch (key) {
    case 'cancel':
      batchCancel(ids);
      break;
    case 'delete':
      batchDelete(ids);
      break;
    case 'favourite':
      batchFavourite();
      break;
    case 'move':
      batchMove(ids);
      break;
    default:
      break;
  }
};

const batchCancel = async (ids: string[]) => {
  // 搜索条件没有任务状态条件就直接刷新列表
  const hasStatusCondition = filters.value.find(item => item.key === 'status');
  if (hasStatusCondition) {
    batchActionOk(ids);
    return;
  }

  loadData();
};

const batchDelete = async (ids: string[]) => {
  batchActionOk(ids);
};

const batchFavourite = async () => {
  // 更新当前列表数据
  loadData();
};

const batchMove = (ids: string[]) => {
  // 搜索条件没有迭代条件就直接刷新列表
  const hasStatusCondition = filters.value.find(item => item.key === 'sprintId');
  if (hasStatusCondition) {
    batchActionOk(ids);
    return;
  }

  loadData();
};

const batchActionOk = (ids: string[]) => {
  const { current, pageSize, total } = pagination.value;
  const totalPage = Math.ceil(total / pageSize);
  const remainder = total % pageSize;

  const deleteNum = ids.length;
  const deletePages = Math.floor(deleteNum / pageSize);
  const deleteRemainder = deleteNum % pageSize;

  if ((deleteRemainder === 0 || remainder === 0) || (deleteRemainder < remainder)) {
    if (current + deletePages <= totalPage) {
      // 通知列表刷新
      refreshNotify.value = utils.uuid();
      // 通知统计面板刷新
      countRefreshNotify.value = utils.uuid();
      return;
    }

    pagination.value.current = current - (current + deletePages - totalPage) || 1;
    // 通知列表刷新
    refreshNotify.value = utils.uuid();
    // 通知统计面板刷新
    countRefreshNotify.value = utils.uuid();
    return;
  }

  if (deleteRemainder >= remainder) {
    if (current + deletePages + 1 <= totalPage) {
      // 通知列表刷新
      refreshNotify.value = utils.uuid();
      // 通知统计面板刷新
      countRefreshNotify.value = utils.uuid();
      return;
    }

    // 通知列表刷新
    refreshNotify.value = utils.uuid();
    // 通知统计面板刷新
    countRefreshNotify.value = utils.uuid();
  }
};

const setTableData = (data: Partial<TaskInfo>) => {
  if (data) {
    const index = taskList.value.findIndex(item => item.id === data.id);
    if (taskList.value[index]) {
      taskList.value[index] = { ...taskList.value[index], ...data };
      editTaskData.value = taskList.value[index];
    }
  }
};

// 模块相关
const moduleTreeData = ref([{ name: t('task.list.noModuleTask'), id: '-1' }]);
const moduleId = ref();
const loadModuleTree = async (keywords?: string) => {
  const [error, { data }] = await modules.getModuleTree({
    projectId: props.projectId,
    filters: keywords
      ? [{
          value: keywords,
          op: 'MATCH_END',
          key: 'name'
        }]
      : []
  });
  if (error) {
    return;
  }
  moduleTreeData.value = [{ name: t('task.list.noModuleTask'), id: '-1' }, ...travelTreeData(data || [])];
  if (moduleId.value && keywords && !moduleTreeData.value.find(item => item.id === moduleId.value)) {
    moduleId.value = '';
  }
};

onMounted(async () => {
  router.replace('/task#task');

  await loadModuleTree();

  watch(() => props.projectId, () => {
    moduleId.value = '';
    loadModuleTree();
  });

  watch(() => refreshNotify.value, (newValue) => {
    if (newValue === undefined || newValue === null || newValue === '') {
      return;
    }

    loadData();
  }, { immediate: true });

  watch(() => moduleFlag.value, () => {
    if (moduleFlag.value) {
      moduleId.value = '';
    } else {
      moduleId.value = undefined;
    }
  });

  watch(() => moduleId.value, () => {
    // 通知列表刷新
    refreshNotify.value = utils.uuid();
    // 通知统计面板刷新
    countRefreshNotify.value = utils.uuid();
  });
});

const menuItemsMap = computed<Map<string, ActionMenuItem[]>>(() => {
  const map = new Map<string, ActionMenuItem[]>();
  const list = taskList.value || [];
  for (let i = 0, len = list.length; i < len; i++) {
    const item = list[i];
    const status = item.status?.value;
    const sprintId = item.sprintId;
    const sprintAuth = item.sprintAuth;

    const permissions = sprintPermissionsMap.value.get(sprintId) || [];
    const { currentAssociateType, confirmorId, assigneeId, createdBy } = item;

    const userId = props.userInfo?.id;
    const isAdministrator = !!currentAssociateType?.map(item => item.value).includes('SYS_ADMIN' || 'APP_ADMIN');
    const isConfirmor = confirmorId === userId;
    const isAssignee = assigneeId === userId;
    // const isCreator = createdBy === userId;

    const menuItems: ActionMenuItem[] = [
      {
        name: t('task.actions.edit'),
        key: 'edit',
        icon: 'icon-shuxie',
        disabled: !isAdministrator && !permissions.includes('MODIFY_TASK') && sprintAuth,
        hide: true
      },
      {
        name: t('task.actions.delete'),
        key: 'delete',
        icon: 'icon-qingchu',
        disabled: !isAdministrator && !permissions.includes('DELETE_TASK') && sprintAuth,
        hide: true
      },
      {
        name: t('task.actions.split'),
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
        name: t('task.actions.cancel'),
        key: 'cancel',
        icon: 'icon-zhongzhi2',
        disabled: !isAdministrator && !permissions.includes('MODIFY_TASK') && sprintAuth,
        hide: false
      });
    }

    const { favourite, follow } = item;
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

    map.set(item.id, menuItems);
  }

  return map;
});

const statisticsParams = computed(() => {
  const param = { };
  if (proTypeShowMap.value.showBackLog) {
    param.backlog = false;
  }
  if (filters.value.length) {
    param.filters = filters.value;
  }
  if (moduleId.value) {
    param.moduleId = moduleId.value;
  }

  return param;
});
</script>

<template>
  <Spin :spinning="loading" class="flex flex-col pl-3.5 pt-3.5 h-full overflow-y-auto overflow-x-hidden">
    <StatisticsPanel
      :collapse="collapseFlag"
      :params="statisticsParams"
      :notify="countRefreshNotify"
      :userInfo="props.userInfo"
      :appInfo="props.appInfo"
      :projectId="props.projectId"
      :class="{ 'mb-3': !collapseFlag }"
      class="pr-5" />

    <div class="flex h-full">
      <div class="flex-shrink-0 h-full overflow-hidden pb-3 bg-gray-1 text-3" :class="{'w-65 mr-2': moduleFlag , 'w-0': !moduleFlag}">
        <ModuleTree
          v-model:moduleId="moduleId"
          :projectId="props.projectId"
          :projectName="props.projectName"
          :dataList="moduleTreeData"
          @loadData="loadModuleTree" />
      </div>
      <div class="flex-1 flex flex-col overflow-x-hidden">
        <SearchPanel
          v-model:collapse="collapseFlag"
          v-model:visible="flowChartVisible"
          v-model:moduleFlag="moduleFlag"
          v-model:groupKey="boardsGroupKey"
          v-model:orderBy="boardsOrderBy"
          v-model:orderSort="boardsOrderSort"
          :viewMode="viewMode"
          :userInfo="props.userInfo"
          :appInfo="props.appInfo"
          :projectId="props.projectId"
          :sprintId="props.sprintId"
          :sprintName="props.sprintName"
          class="mb-1.5 pr-5"
          @change="searchPanelChange"
          @add="toCreate"
          @export="toExport"
          @uploadTask="openUploadTaskModal"
          @exportTemplate="handdleExportTemplate"
          @viewModeChange="viewModeChange" />
        <KanbanView
          v-if="viewMode === 'kanban'"
          v-model:loading="loading"
          v-model:moduleId="moduleId"
          :groupKey="boardsGroupKey"
          :orderBy="boardsOrderBy"
          :orderSort="boardsOrderSort"
          :filters="filters"
          :projectId="props.projectId"
          :userInfo="props.userInfo"
          :appInfo="props.appInfo"
          class="pr-5"
          @refreshChange="refreshChange" />
        <GanttView
          v-else-if="viewMode === 'gantt'"
          v-model:loading="loading"
          v-model:moduleId="moduleId"
          :filters="filters"
          :projectId="props.projectId"
          :userInfo="props.userInfo"
          :appInfo="props.appInfo"
          class="pr-5" />
        <template v-else-if="loaded">
          <div v-if="!taskList.length" class="flex-1 pr-5 h-full flex items-center justify-center">
            <NoData />
          </div>

          <Table
            v-else-if="viewMode === 'table'"
            v-model:loading="loading"
            v-model:selectedIds="selectedIds"
            :projectId="props.projectId"
            :dataSource="taskList"
            :pagination="pagination"
            :menuItemsMap="menuItemsMap"
            class="pr-5"
            @tableChange="tableChange"
            @edit="toEdit"
            @move="toMove"
            @delete="deleteOk"
            @dataChange="setTableData"
            @batchAction="batchAction"
            @refreshChange="refreshChange" />

          <DetailView
            v-else-if="viewMode === 'detail'"
            v-model:loading="loading"
            v-model:selectedIds="selectedIds"
            :projectId="props.projectId"
            :userInfo="props.userInfo"
            :appInfo="props.appInfo"
            :dataSource="taskList"
            :editTaskData="editTaskData"
            :pagination="pagination"
            :menuItemsMap="menuItemsMap"
            :loaded="loaded"
            @edit="toEdit"
            @move="toMove"
            @delete="deleteOk"
            @dataChange="setTableData"
            @paginationChange="paginationChange"
            @batchAction="batchAction"
            @refreshChange="refreshChange"
            @splitOk="splitOk" />
        </template>
      </div>
    </div>

    <AsyncComponent :visible="taskModalVisible">
      <Edit
        v-model:visible="taskModalVisible"
        v-model:taskId="selectedTaskId"
        :sprintId="searchSprintId"
        :projectId="props.projectId"
        :userInfo="props.userInfo"
        :appInfo="props.appInfo"
        :moduleId="moduleId === '-1' ? undefined : moduleId"
        @ok="editOk" />
    </AsyncComponent>

    <AsyncComponent :visible="flowChartVisible">
      <FlowChart v-model:visible="flowChartVisible" />
    </AsyncComponent>

    <AsyncComponent :visible="moveModalVisible">
      <Move
        v-model:visible="moveModalVisible"
        :sprintId="selectedTaskSprintId"
        :taskIds="selectedTaskId"
        :taskName="selectedTaskName"
        :projectId="props.projectId"
        @ok="moveTaskOk" />
    </AsyncComponent>
    <AsyncComponent :visible="uploadTaskVisible">
      <Upload
        v-model:visible="uploadTaskVisible"
        :downloadTemplate="handdleExportTemplate"
        @cancel="cancelUploadTask"
        @ok="handleUploadTaskOk" />
    </AsyncComponent>
  </Spin>
</template>
