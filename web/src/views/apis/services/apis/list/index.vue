<script setup lang="ts">
import { computed, defineAsyncComponent, inject, onBeforeUnmount, onMounted, reactive, ref, Ref, watch } from 'vue';
import { AsyncComponent, modal, notification, Scroll, VuexHelper } from '@xcan-angus/vue-ui';
import elementResizeDetector from 'element-resize-detector';
import { TESTER, duration, appContext, PageQuery } from '@xcan-angus/infra';
import { useI18n } from 'vue-i18n';
import { debounce } from 'throttle-debounce';
import ListItem from './ListItem.vue';
import { apis } from '@/api/tester';
import { group } from '@/utils/common';
import { ApisListInfo } from '../types';
import { ApiPermission } from '@/enums/enums';
import { ProjectInfo } from '@/layout/types';

import VirtualApiList from './VirtualApiList.vue';

interface Props {
  dataSource:Array<ApisListInfo>;
  showGroupList:boolean;
  allData: Array<ApisListInfo>;
  searchParams: {[key:string]:any};
  serviceId: string;
  pageType: 'default'|'success';
  spinning: boolean;
  updateData:(value:{id:string;auth:boolean;})=>void;
  groupedBy?: string;
  order?: {
    orderBy:string,
    orderSort: PageQuery.OrderSort
  }
}

const props = withDefaults(defineProps<Props>(), {
  dataSource: () => [],
  showGroupList: false,
  allData: () => [],
  searchParams: undefined,
  serviceId: undefined,
  pageType: 'default',
  spinning: false,
  updateData: undefined,
  groupedBy: 'none',
  order: undefined
});

const emits = defineEmits<{
  (e:'change', page:number):void,
  (e:'loadApis'):void,
  (e:'openMock'),
  (e:'update:pageType', value: 'default'|'success'): void;
  (e:'update:spinning', value: boolean):void;
  (e:'scrollChange', value: any[]):void;
}>();

const AuthorizeModal = defineAsyncComponent(() => import('@/components/AuthorizeModal/index.vue'));
const GenTestScriptModal = defineAsyncComponent(() => import('@/components/script/GenTestScriptModal.vue'));
const DeleteScriptModal = defineAsyncComponent(() => import('@/components/script/DeleteScriptModal.vue'));
const ShareModal = defineAsyncComponent(() => import('@/components/share/index.vue'));
const CreateTestTaskModal = defineAsyncComponent(() => import('@/components/task/CreateTestTaskModal.vue'));
const DelTestTask = defineAsyncComponent(() => import('@/components/task/DeleteTestTaskModal.vue'));
const RestartTestTaskModal = defineAsyncComponent(() => import('@/components/task/RestartTestTaskModal.vue'));
const ReOpenTestTaskModal = defineAsyncComponent(() => import('@/components/task/ReopenTestTaskModal.vue'));

const MoveModal = defineAsyncComponent(() => import('@/views/apis/services/components/MoveModal.vue'));
const StatusModal = defineAsyncComponent(() => import('@/views/apis/services/components/ModifyStatusModal.vue'));
const OptionalExportModal = defineAsyncComponent(() => import('@/views/apis/services/components/ExportOptionalModal.vue'));
const ExecTestModal = defineAsyncComponent(() => import('@/views/apis/services/test/ExecApiTestModal.vue'));

// Counter to trigger Scroll refresh
const refreshNotifyCounter = ref(0);
const projectInfo = inject<Ref<ProjectInfo>>('projectInfo', ref({} as ProjectInfo));
// Currently opened api provided by parent (for syncing active state)
const injectedApi = inject('api', reactive<{id: string, type?: string}>({ id: '' }));

// Resize detector to maintain wrapper height
const resizeDetector = elementResizeDetector({ strategy: 'scroll' });
// eslint-disable-next-line @typescript-eslint/no-empty-function
const changeGroupState = inject<(data: any) => void>('changeGroupState', () => { });

const { useMutations } = VuexHelper;
const { updateCollectNotify, updateFocusNotify } = useMutations(['updateCollectNotify', 'updateFocusNotify'], 'apiStore');

// eslint-disable-next-line @typescript-eslint/no-empty-function
const addTabPane = inject<(data: any) => void>('addTabPane', () => { });
// eslint-disable-next-line @typescript-eslint/no-empty-function
const deleteTabPane = inject<(data: any) => void>('deleteTabPane', () => { });

// Refresh recycle bin list on the left side (injected by parent)
// eslint-disable-next-line @typescript-eslint/no-unused-vars, @typescript-eslint/no-empty-function
const refreshRecycleBin = inject('refreshRecycleBin', (_key:'api') => {});

const { t } = useI18n();

const appInfo = ref(appContext.getAccessApp()) as Ref<Record<string, any>>;
const listWrapperRef = ref();
const wrapperHeight = ref(500);
const loading = ref(false);

// Whether to render grouped view (virtualized) or flat scroll view
const isGroupedView = computed(() => props.showGroupList);
const resizeHandler = debounce(duration.resize, () => {
  wrapperHeight.value = listWrapperRef.value ? listWrapperRef.value.clientHeight - 12 : 500;
});

const listData = ref<any[]>([]);

// Handle data change callback from Scroll (flat view)
const handleChange = (newList: any[]) => {
  if (!newList.length && !props.searchParams.filters?.length) {
    emits('update:pageType', 'default');
  } else {
    emits('update:pageType', 'success');
  }

  listData.value = newList;
  emits('update:spinning', false);
  emits('scrollChange', listData.value);
};

const handleOpenMock = () => {
  emits('openMock');
};

// Centralized UI state: active API row, modal visibilities, selected ids
const uiState:{
      shareVisible:boolean,
      apiExportVisible:boolean,
      delVisible:boolean,
      auth:boolean,
      activeApiId:string,
      selectedApiId: string,
      initiatingVisible:boolean,
      infoVisible:boolean,
      interfaceAuthVisible:boolean,
      showButton:boolean,
      type?: 'API' | 'WEBSOCKET',
      name?:string
    } = reactive({
      shareVisible: false, // Share modal visibility
      apiExportVisible: false, // Export modal visibility
      delVisible: false, // Delete modal visibility
      auth: false,
      activeApiId: '', // Currently active API id
      selectedApiId: '', // Currently selected row id for actions
      initiatingVisible: false, // Triggering request visibility flag
      infoVisible: false, // Details visibility
      interfaceAuthVisible: false, // Permission modal visibility
      showButton: false, // Show action buttons
      type: undefined,
      name: ''
    });

// Handle toolbar/menu actions from list rows
const handleClick = (event:string, data:ApisListInfo) => {
  const id = data.id;
  uiState.selectedApiId = id;
  uiState.name = data.endpoint;
  switch (event) {
    case 'edit':
      openApiEditorTab(data);
      break;
    case 'auth':
      uiState.interfaceAuthVisible = true;
      uiState.auth = data.auth;
      break;
    case 'export':
      openExportModal();
      break;
    case 'addFavourite':
      addFavourite(id);
      break;
    case 'cancelFavourite':
      cancelFavourite(id);
      break;
    case 'mock':
      handleOpenMock();
      break;
    case 'share':
      openShareModal();
      break;
    case 'del':
      confirmDeleteApi(id);
      break;
    case 'patchClone':
      cloneApiById(id);
      break;
    case 'remove':
      openMoveModal(data);
      break;
    case 'addFollow':
      addFollow(id);
      break;
    case 'cancelFollow':
      cancelFollow(id);
      break;
    case 'status':
      openStatusModal(data);
      break;
    case 'reTest':
      openRestartTestTaskModal(data);
      break;
    case 'reopen':
      openReopenTestTaskModal(data);
      break;
    case 'deleteTask':
      delTestVisible.value = true;
      uiState.selectedApiId = data.id;
      break;
    case 'setTest':
      openCreateTestTaskModal(data);
      break;
    case 'setTestScript':
      openGenTestScriptModal(data, 'create');
      break;
    case 'updateTestScript':
      openGenTestScriptModal(data, 'update');
      break;
    case 'delTestScript':
      delScriptVisible.value = true;
      uiState.selectedApiId = data.id;
      break;
    case 'funcTestExec':
    case 'perfTestExec':
    case 'stabilityTestExec':
      handleExecTest(event, data.id);
      break;
  }
};

// Open share modal
const openShareModal = () => {
  uiState.shareVisible = true;
};

// Flag to detect double click for edit action
const isDoubleClick = ref(false);
// Open API editor tab (double click guard)
const openApiEditorTab = (value:ApisListInfo):void => {
  if (!value) {
    return;
  }
  isDoubleClick.value = true;
  const { id, summary } = value;
  if (value.protocol?.value?.indexOf('ws') > -1) {
    addTabPane({ _id: id + 'socket', id, name: summary, value: 'socket' });
  } else {
    addTabPane({ _id: id + 'API', id, name: summary, value: 'API' });
  }
  setTimeout(() => {
    isDoubleClick.value = false;
  }, 500);
};

// Clone API by id
const cloneApiById = async (id:string) => {
  const [error] = await apis.cloneApi(id);
  if (error) {
    return;
  }
  notification.success(t('actions.tips.cloneSuccess'));
  refreshList();
};

// Ask for confirmation before deleting API
const confirmDeleteApi = (id: string) => {
  modal.confirm({
    centered: true,
    content: t('actions.tips.confirmDataDelete'),
    onOk () {
      performDeleteApi(id);
    }
  });
};

// Perform API deletion, then refresh panels and lists
const performDeleteApi = async (id: string): Promise<void> => {
  loading.value = true;
  const [error] = await apis.deleteApi({ ids: [id] });
  loading.value = false;
  if (error) {
    return;
  }

  uiState.activeApiId = '';
  uiState.type = undefined;
  deleteTabPane([id + 'API', id + 'socket', id + 'execute']);
  refreshRecycleBin('api');
  notification.success(t('actions.tips.deleteSuccess'));
  refreshList();
};

const moveVisible = ref(false); // Move modal visibility
const moveParentId = ref<string>(); // Current API's folder id
const moveParentName = ref<string>(); // Current API's folder name
// Open move modal with selected API context
const openMoveModal = (_api: ApisListInfo) => {
  uiState.selectedApiId = _api.id;
  moveParentId.value = _api.serviceId;
  moveParentName.value = _api.serviceName;
  moveVisible.value = true;
};

// Handle move confirmation
const handleMoveOk = () => {
  uiState.activeApiId = '';
  uiState.type = undefined;
  refreshList();
  handleMoveCancel();
};

// Close move modal
const handleMoveCancel = () => {
  moveVisible.value = false;
  moveParentId.value = undefined;
  moveParentName.value = undefined;
};

const testVisible = ref(false);
// Open create test task modal
const openCreateTestTaskModal = (_api: ApisListInfo) => {
  uiState.selectedApiId = _api.id;
  testVisible.value = true;
};

const restartTestVisible = ref(false);
const restartContent = ref('');
// Open restart test task confirmation modal
const openRestartTestTaskModal = (item: ApisListInfo) => {
  uiState.selectedApiId = item.id;
  restartTestVisible.value = true;
  restartContent.value = t('service.apiList.confirm.restartTestTask', { summary: item.summary });
};

const reopenTestVisible = ref(false);
const reopenContent = ref('');
// Open reopen test task confirmation modal
const openReopenTestTaskModal = (item: ApisListInfo) => {
  uiState.selectedApiId = item.id;
  reopenTestVisible.value = true;
  reopenContent.value = t('service.apiList.confirm.reopenTestTask', { summary: item.summary });
};

// Delete test task
const delTestVisible = ref(false);

// Open status change modal
const statusVisible = ref(false);
const statusValue = ref();
const openStatusModal = (api: ApisListInfo) => {
  statusValue.value = api.status?.value || '';
  statusVisible.value = true;
  uiState.selectedApiId = api.id;
};

// Generate or update test scripts
const testScriptVisible = ref(false);
// 'create' | 'update' for script generation action
const testScriptActionType = ref();
const openGenTestScriptModal = (api: ApisListInfo, generate: 'create'|'update') => {
  testScriptVisible.value = true;
  uiState.selectedApiId = api.id;
  testScriptActionType.value = generate;
};

// Update list data when permission toggled in modal
const authFlagChange = ({ auth }:{auth:boolean}) => {
  if (typeof props.updateData === 'function') {
    props.updateData({ id: uiState.selectedApiId, auth });
  }
};

// Open API export modal
const openExportModal = () => {
  uiState.apiExportVisible = true;
};

// Add to favourites
const addFavourite = async (id:string) => {
  const params = [id];
  const [error] = await apis.addFavourite(params);
  if (error) {
    return;
  }
  listData.value.forEach(item => {
    if (item.id === id) {
      item.favourite = true;
    }
  });
  notification.success(t('service.apiList.messages.favouriteSuccess'));
  refreshList();
  updateCollectNotify();
};

// Remove from favourites
const cancelFavourite = async (id:string) => {
  const [error] = await apis.cancelFavourite(id);
  if (error) {
    return;
  }
  listData.value.forEach(item => {
    if (item.id === id) {
      item.favourite = false;
    }
  });
  notification.success(t('service.apiList.messages.unfavouriteSuccess'));
  refreshList();
  updateCollectNotify();
};

// Follow
const addFollow = async (id:string) => {
  const [error] = await apis.addFollow(id);
  if (error) {
    return;
  }
  listData.value.forEach(item => {
    if (item.id === id) {
      item.follow = true;
    }
  });
  notification.success(t('service.apiList.messages.followSuccess'));
  refreshList();
  updateFocusNotify();
};

// Unfollow
const cancelFollow = async (id:string) => {
  const [error] = await apis.cancelFollow(id);
  if (error) {
    return;
  }
  listData.value.forEach(item => {
    if (item.id === id) {
      item.follow = false;
    }
  });
  notification.success(t('service.apiList.messages.unfollowSuccess'));
  refreshList();
  updateFocusNotify();
};

const execTestVisible = ref(false);
// Target API id for execution actions
const execTargetApiId = ref<string>();
const execTips = ref<string>();
const execModalTitle = ref<string>();
const okAction = ref<string>();

const execTestTipConfig = {
  funcTestExec: t('service.apiList.execTest.tips.funcTestExec'),
  perfTestExec: t('service.apiList.execTest.tips.perfTestExec'),
  stabilityTestExec: t('service.apiList.execTest.tips.stabilityTestExec')
};

const execModalTitleConfig = {
  funcTestExec: t('service.apiList.execTest.titles.funcTestExec'),
  perfTestExec: t('service.apiList.execTest.titles.perfTestExec'),
  stabilityTestExec: t('service.apiList.execTest.titles.stabilityTestExec')
};

const getOkAction = (type:'funcTestExecSmoke'|'funcTestExecSecurity'|'funcTestExec'|'perfTestExec'|'stabilityTestExec', id:string) => {
  switch (type) {
    case 'funcTestExec':
      return `${TESTER}/apis/${id}/exec?testTypes=FUNCTIONAL`;
    case 'perfTestExec':
      return `${TESTER}/apis/${id}/exec?testTypes=PERFORMANCE`;
    case 'stabilityTestExec':
      return `${TESTER}/apis/${id}/exec?testTypes=STABILITY`;
  }
};

const handleExecTest = async (type, id: string) => {
  execTargetApiId.value = id;
  execTestVisible.value = true;
  execTips.value = execTestTipConfig[type];
  execModalTitle.value = execModalTitleConfig[type];
  okAction.value = getOkAction(type, id);
};

const delScriptVisible = ref(false);

// Refresh list after status updated
const refreshAfterStatusChange = () => {
  refreshList();
};

// Toggle expand/collapse for an API row (single click)
const toggleApiDetails = (id:string, api: { protocol: { value: string | string[]; }; }) => {
  setTimeout(() => {
    if (isDoubleClick.value) {
      return;
    }
    isDoubleClick.value = false;
    if (id === uiState.activeApiId) {
      uiState.activeApiId = '';
      uiState.type = undefined;
    } else {
      uiState.activeApiId = id;
      uiState.type = api.protocol?.value?.includes('ws') ? 'WEBSOCKET' : 'API';
    }
  }, 300);
};

const refreshList = () => {
  if (props.groupedBy) {
    emits('loadApis');
  } else {
    refreshNotifyCounter.value++;
  }
};

watch(() => uiState.activeApiId, () => {
  changeGroupState({ id: uiState.activeApiId, type: uiState.type });
});

watch(() => injectedApi.id, () => {
  if (injectedApi.id !== uiState.activeApiId) {
    uiState.activeApiId = injectedApi.id;
  }
});

watch(() => loading.value, newValue => {
  emits('update:spinning', newValue);
});

// Grouped data source for virtual list (contains group headers + items)
const groupedData = ref<any[]>([]);
watch(() => [props.allData, props.groupedBy], () => {
  groupedData.value = [];
  if (props.groupedBy !== 'tag') {
    const objData = group(props.dataSource, props.groupedBy);
    Object.keys(objData).forEach(key => {
      groupedData.value.push({
        type: 'group',
        spread: true,
        createdByName: objData[key][0].createdByName,
        avatar: objData[key][0].avatar,
        method: objData[key][0].method,
        ownerName: objData[key][0].ownerName,
        key,
        childrenNum: objData[key].length
      }, ...objData[key]);
    });
  } else {
    const resultObj: Record<string, any[]> = {};
    props.dataSource.forEach((item) => {
      if (item.tags && item.tags.length) {
        item.tags.forEach(tag => {
          if (!resultObj[tag]) {
            resultObj[tag] = [item];
          } else {
            resultObj[tag].push(item);
          }
        });
      } else {
        if (!resultObj.null_tag) {
          resultObj.null_tag = [item];
        } else {
          resultObj.null_tag.push(item);
        }
      }
    });
    const tagsArr = Object.keys(resultObj);
    if (props.order?.orderBy === 'summary') {
      tagsArr.sort((a, b) => {
        if (props.order?.orderSort === PageQuery.OrderSort.Desc) {
          return b.toLowerCase().localeCompare(a.toLowerCase());
        } else {
          return a.toLowerCase().localeCompare(b.toLowerCase());
        }
      });
    }
    tagsArr.forEach(key => {
      const list = resultObj[key];
      groupedData.value.push({
        type: 'group',
        tag: 'key',
        spread: true,
        name: key === 'null_tag' ? t('service.apiList.template.other') : key,
        childrenNum: list.length,
        key
      }, ...list);
    });
  }
}, {
  immediate: true
});

onMounted(() => {
  wrapperHeight.value = listWrapperRef.value ? listWrapperRef.value.clientHeight - 12 : 500;
  resizeDetector.listenTo(listWrapperRef.value, resizeHandler);
});

onBeforeUnmount(() => {
  resizeDetector.removeListener(listWrapperRef.value, resizeHandler);
});

defineExpose({
  activeApiId: uiState.activeApiId,
  updateScrollList: () => {
    refreshList();
  }
});
</script>
<template>
  <div ref="listWrapperRef">
    <template v-if="isGroupedView">
      <VirtualApiList
        :dataSource="groupedData"
        :showNum="30"
        :updateData="props.updateData"
        :groupedBy="props.groupedBy"
        :height="wrapperHeight"
        :activeApiId="uiState.activeApiId"
        @handleClick="handleClick"
        @edit="openApiEditorTab"
        @showInfo="toggleApiDetails"
        @loadApis="emits('loadApis')"
        @openMock="handleOpenMock" />
    </template>
    <template v-if="!isGroupedView">
      <Scroll
        v-model:spinning="loading"
        :action="`${TESTER}/services/${props.serviceId}/apis?infoScope=DETAIL&fullTextSearch=true`"
        class="h-full scroll-wrapper"
        :lineHeight="58"
        :notify="refreshNotifyCounter"
        :transition="false"
        :params="props.searchParams"
        @change="handleChange">
        <ListItem
          v-for="(item, index) in listData"
          :key="item.id"
          :item="item"
          :index="index"
          :activeApiId="uiState.activeApiId"
          @handleClick="handleClick"
          @edit="openApiEditorTab"
          @showInfo="toggleApiDetails" />
      </Scroll>
    </template>
    <AsyncComponent :visible="moveVisible">
      <MoveModal
        :id="uiState.selectedApiId"
        type="api"
        :visible="moveVisible"
        :pid="moveParentId"
        :projectId="projectInfo?.id.toString()"
        :parentName="moveParentName"
        @ok="handleMoveOk"
        @cancel="handleMoveCancel" />
    </AsyncComponent>
    <AsyncComponent :visible="uiState.shareVisible">
      <ShareModal
        v-if="uiState.shareVisible"
        :id="uiState.selectedApiId"
        v-model:visible="uiState.shareVisible"
        :name="uiState.name"
        source="all"
        type="API" />
    </AsyncComponent>
    <AsyncComponent :visible="uiState.interfaceAuthVisible">
      <AuthorizeModal
        v-model:visible="uiState.interfaceAuthVisible"
        :enumKey="ApiPermission"
        :appId="appInfo?.id"
        :listUrl="`${TESTER}/apis/auth?apisId=${uiState.selectedApiId}`"
        :delUrl="`${TESTER}/apis/auth`"
        :addUrl="`${TESTER}/apis/${uiState.selectedApiId}/auth`"
        :updateUrl="`${TESTER}/apis/auth`"
        :enabledUrl="`${TESTER}/apis/${uiState.selectedApiId}/auth/enabled`"
        :initStatusUrl="`${TESTER}/apis/${uiState.selectedApiId}/auth/status`"
        :onTips="t('service.apiList.template.permission.onTips')"
        :offTips="t('service.apiList.template.permission.offTips')"
        :title="t('service.apiList.template.permission.title')"
        @change="authFlagChange" />
    </AsyncComponent>
    <AsyncComponent :visible="testVisible">
      <CreateTestTaskModal
        v-model:id="uiState.selectedApiId"
        v-model:visible="testVisible"
        :infoText="t('service.apiList.template.testTask.infoText')"
        type="API" />
    </AsyncComponent>
    <AsyncComponent :visible="restartTestVisible">
      <RestartTestTaskModal
        v-model:visible="restartTestVisible"
        v-model:id="uiState.selectedApiId"
        :content="restartContent"
        type="API" />
    </AsyncComponent>
    <AsyncComponent :visible="reopenTestVisible">
      <ReOpenTestTaskModal
        v-model:visible="reopenTestVisible"
        v-model:id="uiState.selectedApiId"
        :content="reopenContent"
        type="API" />
    </AsyncComponent>
    <AsyncComponent :visible="delTestVisible">
      <DelTestTask
        :id="uiState.selectedApiId"
        v-model:visible="delTestVisible"
        type="API" />
    </AsyncComponent>
    <AsyncComponent :visible="statusVisible">
      <StatusModal
        :id="uiState.selectedApiId"
        v-model:visible="statusVisible"
        :value="statusValue"
        @confirm="refreshAfterStatusChange" />
    </AsyncComponent>
    <AsyncComponent :visible="uiState.apiExportVisible">
      <OptionalExportModal
        v-if="uiState.apiExportVisible"
        :id="uiState.selectedApiId"
        v-model:visible="uiState.apiExportVisible"
        type="API" />
    </AsyncComponent>
    <AsyncComponent :visible="testScriptVisible">
      <GenTestScriptModal
        :id="uiState.selectedApiId"
        v-model:visible="testScriptVisible"
        :setType="testScriptActionType"
        type="API" />
    </AsyncComponent>
    <AsyncComponent :visible="delScriptVisible">
      <DeleteScriptModal
        :id="uiState.selectedApiId"
        v-model:visible="delScriptVisible"
        type="API" />
    </AsyncComponent>
    <AsyncComponent :visible="execTestVisible">
      <ExecTestModal
        v-model:serviceId="props.serviceId"
        v-model:visible="execTestVisible"
        :title="execModalTitle"
        :tips="execTips"
        :okAction="okAction" />
    </AsyncComponent>
  </div>
</template>

  <style scoped>

    .ant-collapse-borderless {
      @apply bg-white;
    }

    .ant-collapse-borderless > .ant-collapse-item:last-child,
    .ant-collapse-borderless > .ant-collapse-item:last-child .ant-collapse-header {
      @apply rounded;
    }

    :deep(.ant-btn-text) {
      @apply text-text-sub-content;
    }

    :deep(.ant-btn) {
      @apply text-3 leading-3 px-2.25;
    }

    :deep(.ant-list-item) {
      @apply p-0 border-b-0;
    }

    :deep(.scroll-wrapper) > div+div {
      padding-right: 8px;
    }

    .show {
      @apply block;
    }

    .show1 {
      @apply hidden;
    }

    @media (max-width: 1300px) {
      .show {
        @apply hidden;
      }

      .show1 {
        @apply block;
      }
    }
  </style>
