<script setup lang="ts">
import { computed, defineAsyncComponent, inject, onBeforeUnmount, onMounted, reactive, ref, Ref, watch } from 'vue';
import { AsyncComponent, AuthorizeModal, modal, notification, Scroll, VuexHelper } from '@xcan-angus/vue-ui';
import elementResizeDetector from 'element-resize-detector';
import { TESTER, duration } from '@xcan-angus/tools';
import { useI18n } from 'vue-i18n';
import { debounce } from 'throttle-debounce';
import ListItem from './listItem.vue';
import { apis } from 'src/api/tester';
import { group } from '@/utils/common';
import type { DataSourceType } from '../PropsType';
import VirtualApiList from './virtualApiList.vue';

interface Props {
  dataSource:Array<DataSourceType>;
  showGroupList:boolean;
  allData: Array<DataSourceType>;
  searchParams: {[key:string]:any};
  serviceId: string;
  pageType: 'default'|'success';
  spinning: boolean;
  updateData:(value:{id:string;auth:boolean;})=>void;
  groupedBy?: string;
  order?: {
    orderBy:string,
    orderSort: 'DESC'|'ASC'
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
  groupedBy: 'none'
});

// eslint-disable-next-line func-call-spacing
const emits = defineEmits<{
  (e:'change', page:number):void,
  (e:'loadApis'):void,
  (e:'openMock'),
  (e:'update:pageType', value: 'default'|'success'): void;
  (e:'update:spinning', value: boolean):void;
  (e:'scrollChange', value: any[]):void;
}>();

const MovePop = defineAsyncComponent(() => import('@/views/apis/services/components/moveModal/index.vue'));
const Share = defineAsyncComponent(() => import('@/components/share/index.vue'));
const CreateTestTaskModal = defineAsyncComponent(() => import('@/components/task/createTestModal/index.vue'));
const RestartTestTaskModal = defineAsyncComponent(() => import('@/components/task/restartTestModal/index.vue'));
const ReOpenTestTaskModal = defineAsyncComponent(() => import('@/components/task/reopenTestModal/index.vue'));
const DelTestTask = defineAsyncComponent(() => import('@/components/task/delTestModal/index.vue'));
const StatusModal = defineAsyncComponent(() => import('@/views/apis/services/components/statusModal/index.vue'));
const ExportApiModal = defineAsyncComponent(() => import('@/views/apis/services/sidebar/components/exportServices/index.vue'));
const GenTestScript = defineAsyncComponent(() => import('@/components/script/genTestScriptModal/index.vue'));
const DelTestScript = defineAsyncComponent(() => import('@/components/script/delModal/index.vue'));
const ExecTestModal = defineAsyncComponent(() => import('@/views/apis/services/apiGroup/list/execTest/index.vue'));
const erd = elementResizeDetector({ strategy: 'scroll' });

const notify = ref(0);
const projectInfo = inject('projectInfo');
const api = inject('api', reactive<{id: string, type?: string}>({ id: '' })); // 用于提供给外面当前 打开的 api

// eslint-disable-next-line @typescript-eslint/no-empty-function
const changeGroupState = inject<(data: any) => void>('changeGroupState', () => { });

const { useMutations } = VuexHelper;
const { updateCollectNofify, updateFocusNotify } = useMutations(['updateCollectNofify', 'updateFocusNotify'], 'apiStore');

// eslint-disable-next-line @typescript-eslint/no-empty-function
const addTabPane = inject<(data: any) => void>('addTabPane', () => { });
// eslint-disable-next-line @typescript-eslint/no-empty-function
const deleteTabPane = inject<(data: any) => void>('deleteTabPane', () => { });

// 刷新左侧回收站列表
// eslint-disable-next-line @typescript-eslint/no-unused-vars, @typescript-eslint/no-empty-function
const refreshRecycleBin = inject('refreshRecycleBin', (_key:'api') => {});

const { t } = useI18n();

const appInfo = inject('appInfo') as Ref<Record<string, any>>;
const listWrapperRef = ref();
const wrapperHeight = ref(500);
const loading = ref(false);

const showGroupList1 = computed(() => props.showGroupList);
const resizeHandler = debounce(duration.resize, () => {
  wrapperHeight.value = listWrapperRef.value ? listWrapperRef.value.clientHeight - 12 : 500;
});

onMounted(() => {
  wrapperHeight.value = listWrapperRef.value ? listWrapperRef.value.clientHeight - 12 : 500;
  erd.listenTo(listWrapperRef.value, resizeHandler);
});

onBeforeUnmount(() => {
  erd.removeListener(listWrapperRef.value, resizeHandler);
});

const listData = ref<any[]>([]);

const handleChange = (data: any[]) => {
  if (!data.length && !props.searchParams.filters?.length) {
    emits('update:pageType', 'default');
  } else {
    emits('update:pageType', 'success');
  }

  listData.value = data;
  emits('update:spinning', false);
  emits('scrollChange', listData.value);
};

const handleOpenMock = () => {
  emits('openMock');
};

const state:{
      shareVisible:boolean,
      apiExportVisible:boolean,
      delVisible:boolean,
      auth:boolean,
      activeApiId:string,
      id: string,
      initiatingVisible:boolean,
      infoVisible:boolean,
      interfaceAuthVisible:boolean,
      showButton:boolean,
      type?: 'API' | 'WEBSOCKET',
      name?:string
    } = reactive({
      shareVisible: false, // 分享弹框
      apiExportVisible: false, // 导出
      delVisible: false, // 删除弹框
      auth: false,
      activeApiId: '', // 当前 选中行 api的id
      id: '', // 当前按钮操作行 的 id
      initiatingVisible: false, // 发起请求显影判断
      infoVisible: false, // 详情展示
      interfaceAuthVisible: false, // 权限弹框显示
      showButton: false, // 显示按钮
      type: undefined,
      name: ''
    });

const handleClick = (event:string, data:DataSourceType) => {
  const id = data.id;
  state.id = id;
  state.name = data.endpoint;
  switch (event) {
    case 'edit':
      edit(data);
      break;
    case 'auth':
      state.interfaceAuthVisible = true;
      state.auth = data.auth;
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
      share();
      break;
    case 'del':
      deleteConfirm(id);
      break;
    case 'patchClone':
      patchClone(id);
      break;
    case 'remove':
      toMove(data);
      break;
    // case 'execute':
    //   execute(id);
    //   break;
    // case 'test':
    //   test();
    //   break;
    case 'addWatch':
      addWatch(id);
      break;
    case 'cancelWatch':
      cancelWatch(id);
      break;
    case 'status':
      setStatus(data);
      break;
    case 'reTest':
      restartTestTask(data);
      break;
    case 'reopen':
      reopenTestTask(data);
      break;
    case 'deleteTask':
      delTestVisible.value = true;
      state.id = data.id;
      break;
    case 'setTest':
      toSetTest(data);
      break;
    case 'setTestScript':
      setTestScript(data, 'create');
      break;
    case 'updateTestScript':
      setTestScript(data, 'update');
      break;
    case 'delTestScript':
      delScriptVisble.value = true;
      state.id = data.id;
      break;
    case 'funcTestExec':
    case 'perfTestExec':
    case 'stabilityTestExec':
      handleExecTest(event, data.id);
      break;
  }
};

const share = () => {
  state.shareVisible = true;
};

// 是否双击编辑
const isdbClick = ref(false);
// 编辑api
const edit = (value:DataSourceType):void => {
  if (!value) {
    return;
  }
  isdbClick.value = true;
  const { id, summary } = value;
  if (value.protocol?.value?.indexOf('ws') > -1) {
    addTabPane({ _id: id + 'socket', id, name: summary, value: 'socket' });
  } else {
    addTabPane({ _id: id + 'API', id, name: summary, value: 'API' });
  }
  setTimeout(() => {
    isdbClick.value = false;
  }, 500);
};

// 克隆
const patchClone = async (id:string) => {
  const [error] = await apis.cloneApi(id);
  if (error) {
    return;
  }
  notification.success('克隆成功');
  refreshList();
};

// const apiAuths = inject('apiAuths', ref());
// const projectAuths = inject('projectAuths', ref());

const deleteConfirm = (id: string) => {
  modal.confirm({
    centered: true,
    content: t('删除接口会同步删除关联关注、收藏、指标、变量等信息，请确认是否删除？'),
    onOk () {
      deleteInterface(id);
    }
  });
};

const deleteInterface = async (id: string): Promise<void> => {
  loading.value = true;
  const [error] = await apis.deleteApi({ ids: [id] });
  loading.value = false;
  if (error) {
    return;
  }

  state.activeApiId = '';
  state.type = undefined;
  deleteTabPane([id + 'API', id + 'socket', id + 'execute']);
  refreshRecycleBin('api');
  notification.success('删除成功，您可以在回收站查看删除后的接口');
  refreshList();
};

const moveVisible = ref(false); // 移动弹窗
const movePid = ref<string>();// 当前移动api的目录id
const moveParentName = ref<string>();// 当前移动api的目录名称
// @TODO 缺少项目或服务信息
const toMove = (_api: DataSourceType) => {
  state.id = _api.id;
  movePid.value = _api.serviceId;
  moveParentName.value = _api.serviceName;
  moveVisible.value = true;
};

const moveHandle = () => {
  state.activeApiId = '';
  state.type = undefined;
  refreshList();
  moveCancel();
};

// 移动弹窗关闭
const moveCancel = () => {
  moveVisible.value = false;
  movePid.value = undefined;
  moveParentName.value = undefined;
};

const testVisible = ref(false);
const toSetTest = (_api: DataSourceType) => {
  state.id = _api.id;
  testVisible.value = true;
};

const restartTestVisible = ref(false);
const restartContent = ref('');
const restartTestTask = (item: DataSourceType) => {
  state.id = item.id;
  restartTestVisible.value = true;
  restartContent.value = `将接口下测试任务更新为"待测试"，相关统计计数和状态会被清除，您确认重新开始测试任务【${item.summary}】吗？`;
};

const reopenTestVisible = ref(false);
const reopenContent = ref('');
const reopenTestTask = (item: DataSourceType) => {
  state.id = item.id;
  reopenTestVisible.value = true;
  reopenContent.value = `将接口下测试任务更新为"待测试"，不清理统计计数和状态， 您确认重新打开测试任务【${item.summary}】吗？`;
};

// 删除测试任务
const delTestVisible = ref(false);

// 修改状态 visible
const statusVisible = ref(false);
const statusValue = ref();
const setStatus = (api: DataSourceType) => {
  statusValue.value = api.status?.value || '';
  statusVisible.value = true;
  state.id = api.id;
};

// 生成|更新测试脚本
const testScriptVisible = ref(false);
const setType = ref();
const setTestScript = (api: DataSourceType, generate: 'create'|'update') => {
  testScriptVisible.value = true;
  state.id = api.id;
  setType.value = generate;
};

const authFlagChange = ({ auth }:{auth:boolean}) => {
  if (typeof props.updateData === 'function') {
    props.updateData({ id: state.id, auth });
  }
};

// 打开接口导出弹框
const openExportModal = () => {
  state.apiExportVisible = true;
};

// 收藏
const addFavourite = async (id:string) => {
  const params = [id];
  const [error] = await apis.addFavourite(params);
  if (error) {
    return;
  }
  listData.value.forEach(item => {
    if (item.id === id) {
      item.favouriteFlag = true;
    }
  });
  notification.success('收藏成功');
  refreshList();
  updateCollectNofify();
};

// 取消收藏
const cancelFavourite = async (id:string) => {
  const [error] = await apis.cancelFavourite(id);
  if (error) {
    return;
  }
  listData.value.forEach(item => {
    if (item.id === id) {
      item.favouriteFlag = false;
    }
  });
  notification.success('取消收藏成功');
  refreshList();
  updateCollectNofify();
};

// 增加关注
const addWatch = async (id:string) => {
  const [error] = await apis.addFollow(id);
  if (error) {
    return;
  }
  listData.value.forEach(item => {
    if (item.id === id) {
      item.followFlag = true;
    }
  });
  notification.success('关注成功');
  refreshList();
  updateFocusNotify();
};

// 取消关注
const cancelWatch = async (id:string) => {
  const [error] = await apis.cancelFollow(id);
  if (error) {
    return;
  }
  listData.value.forEach(item => {
    if (item.id === id) {
      item.followFlag = false;
    }
  });
  notification.success('取消关注成功');
  refreshList();
  updateFocusNotify();
};

const execTestVisible = ref(false);
const selectedApisId = ref<string>();
const execTips = ref<string>();
const execModalTitle = ref<string>();
const okAction = ref<string>();

const execTestTipConfig = {
  funcTestExec: '执行当前接口功能测试，如果测试脚本不存在，将根据接口自动生成功能测试脚本，如果存在则启动对应功能测试脚本。',
  perfTestExec: '执行当前接口性能测试，如果测试脚本不存在，将根据接口自动生成性能测试脚本，如果存在则启动对应性能测试脚本。',
  stabilityTestExec: '执行当前接口稳定性测试，如果测试脚本不存在，将根据接口自动生成稳定性测试脚本，如果存在则启动对应稳定性测试脚本。'
};

const execModalTitleConfig = {
  funcTestExec: '执行功能测试',
  perfTestExec: '执行性能测试',
  stabilityTestExec: '执行稳定性测试'
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
  selectedApisId.value = id;
  execTestVisible.value = true;
  execTips.value = execTestTipConfig[type];
  execModalTitle.value = execModalTitleConfig[type];
  okAction.value = getOkAction(type, id);
  // modal.confirm({
  //   content: execTestTipConfig[type],
  //   async onOk () {
  //     let testTypes = '';
  //     switch (type) {
  //       case 'funcTestExec':
  //         testTypes = 'FUNCTIONAL';
  //         break;
  //       case 'perfTestExec':
  //         testTypes = 'PERFORMANCE';
  //         break;
  //       case 'stabilityTestExec':
  //         testTypes = 'STABILITY';
  //         break;
  //     }
  //     const [error] = await http.put(`${TESTER}/apis/${id}/exec?testTypes=${testTypes}`);
  //     if (error) {
  //       return;
  //     }
  //     notification.success('执行成功');
  //   }
  // });
};

const delScriptVisble = ref(false);

const changeStatus = () => {
  refreshList();
};
// 点击详情
const showInfo = (id:string, api: { protocol: { value: string | string[]; }; }) => {
  setTimeout(() => {
    if (isdbClick.value) {
      return;
    }
    isdbClick.value = false;
    if (id === state.activeApiId) {
      state.activeApiId = '';
      state.type = undefined;
    } else {
      state.activeApiId = id;
      state.type = api.protocol?.value?.includes('ws') ? 'WEBSOCKET' : 'API';
    }
  }, 300);
};

const refreshList = () => {
  if (props.groupedBy) {
    emits('loadApis');
  } else {
    notify.value++;
  }
};

watch(() => state.activeApiId, () => {
  changeGroupState({ id: state.activeApiId, type: state.type });
});

watch(() => api.id, () => {
  if (api.id !== state.activeApiId) {
    state.activeApiId = api.id;
  }
});

watch(() => loading.value, newValue => {
  emits('update:spinning', newValue);
});

defineExpose({
  activeApiId: state.activeApiId,
  updateScrollList: () => {
    refreshList();
  }
});

const data = ref<Record<string, any>[]>([]);
watch(() => [props.allData, props.groupedBy], () => {
  data.value = [];
  if (props.groupedBy !== 'tag') {
    const objData = group(props.dataSource, props.groupedBy);
    Object.keys(objData).forEach(key => {
      data.value.push({
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
        if (props.order?.orderSort === 'DESC') {
          return b.toLowerCase().localeCompare(a.toLowerCase());
        } else {
          return a.toLowerCase().localeCompare(b.toLowerCase());
        }
      });
    }
    tagsArr.forEach(key => {
      const list = resultObj[key];
      data.value.push({
        type: 'group',
        tag: 'key',
        spread: true,
        name: key === 'null_tag' ? '其他' : key,
        childrenNum: list.length,
        key
      }, ...list);
    });
  }
}, {
  immediate: true
});

</script>

<template>
  <div ref="listWrapperRef">
    <template v-if="showGroupList1">
      <VirtualApiList
        :dataSource="data"
        :showNum="30"
        :updateData="props.updateData"
        :groupedBy="props.groupedBy"
        :height="wrapperHeight"
        :activeApiId="state.activeApiId"
        @handleClick="handleClick"
        @edit="edit"
        @showInfo="showInfo"
        @loadApis="emits('loadApis')"
        @openMock="handleOpenMock" />
    </template>
    <template v-if="!showGroupList1">
      <Scroll
        v-model:spinning="loading"
        :action="`${TESTER}/services/${props.serviceId}/apis?infoScope=DETAIL&fullTextSearch=true`"
        class="h-full scroll-wrapper"
        :lineHeight="58"
        :notify="notify"
        :transition="false"
        :params="props.searchParams"
        @change="handleChange">
        <ListItem
          v-for="(item, index) in listData"
          :key="item.id"
          :item="item"
          :index="index"
          :activeApiId="state.activeApiId"
          @handleClick="handleClick"
          @edit="edit"
          @showInfo="showInfo" />
      </Scroll>
    </template>
    <AsyncComponent :visible="moveVisible">
      <MovePop
        :id="state.id"
        type="api"
        :visible="moveVisible"
        :pid="movePid"
        :projectId="projectInfo?.id"
        :parentName="moveParentName"
        @ok="moveHandle"
        @cancel="moveCancel" />
    </AsyncComponent>
    <AsyncComponent :visible="state.shareVisible">
      <Share
        v-if="state.shareVisible"
        :id="state.id"
        v-model:visible="state.shareVisible"
        :name="state.name"
        source="all"
        type="API" />
    </AsyncComponent>
    <AsyncComponent :visible="state.interfaceAuthVisible">
      <AuthorizeModal
        v-model:visible="state.interfaceAuthVisible"
        enumKey="ApiPermission"
        :appId="appInfo?.id"
        :listUrl="`${TESTER}/apis/auth?apisId=${state.id}`"
        :delUrl="`${TESTER}/apis/auth`"
        :addUrl="`${TESTER}/apis/${state.id}/auth`"
        :updateUrl="`${TESTER}/apis/auth`"
        :enabledUrl="`${TESTER}/apis/${state.id}/auth/enabled`"
        :initStatusUrl="`${TESTER}/apis/${state.id}/auth/status`"
        onTips="开启&quot;有权限控制&quot;后，需要手动授权服务权限后才会有接口相应操作权限，默认开启&quot;有权限控制&quot;。注意：如果授权对象没有父级项目/服务权限将自动授权查看权限。"
        offTips="开启&quot;无权限控制&quot;后，将允许所有用户公开查看和操作当前接口，查看用户同时需要有当前接口父级项目或服务权限。"
        title="接口权限"
        @change="authFlagChange" />
    </AsyncComponent>
    <AsyncComponent :visible="testVisible">
      <CreateTestTaskModal
        v-model:id="state.id"
        v-model:visible="testVisible"
        infoText="接口测试任务不存在时生成对应任务，如果任务已存在则覆盖当前测试信息。"
        type="API" />
    </AsyncComponent>
    <!-- 重新开始测试 -->
    <AsyncComponent :visible="restartTestVisible">
      <RestartTestTaskModal
        v-model:visible="restartTestVisible"
        v-model:id="state.id"
        :content="restartContent"
        type="API" />
    </AsyncComponent>
    <!-- 重新打开测试任务 -->
    <AsyncComponent :visible="reopenTestVisible">
      <ReOpenTestTaskModal
        v-model:visible="reopenTestVisible"
        v-model:id="state.id"
        :content="reopenContent"
        type="API" />
    </AsyncComponent>
    <AsyncComponent :visible="delTestVisible">
      <DelTestTask
        :id="state.id"
        v-model:visible="delTestVisible"
        type="API" />
    </AsyncComponent>
    <AsyncComponent :visible="statusVisible">
      <StatusModal
        :id="state.id"
        v-model:visible="statusVisible"
        :value="statusValue"
        @confirm="changeStatus" />
    </AsyncComponent>
    <AsyncComponent :visible="state.apiExportVisible">
      <ExportApiModal
        v-if="state.apiExportVisible"
        :id="state.id"
        v-model:visible="state.apiExportVisible"
        type="API" />
    </AsyncComponent>
    <AsyncComponent :visible="testScriptVisible">
      <GenTestScript
        :id="state.id"
        v-model:visible="testScriptVisible"
        :setType="setType"
        type="API" />
    </AsyncComponent>
    <AsyncComponent :visible="delScriptVisble">
      <DelTestScript
        :id="state.id"
        v-model:visible="delScriptVisble"
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
