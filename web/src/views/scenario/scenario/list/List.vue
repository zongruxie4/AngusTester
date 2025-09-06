<script lang="ts" setup>
import { defineAsyncComponent, inject, onMounted, ref, watch, Ref } from 'vue';
import { Badge, Button, Tooltip, TypographyParagraph } from 'ant-design-vue';
import {
  AsyncComponent,
  AuthorizeModal,
  Dropdown,
  GridList,
  Icon,
  Image,
  modal,
  notification,
  ScriptTypeTag
} from '@xcan-angus/vue-ui';
import { TESTER } from '@xcan-angus/infra';
import { useI18n } from 'vue-i18n';
import { scenario } from '@/api/tester';
import { MenuItem, MenuItemKey, MenuItemPermission, SceneInfo } from './types';

const { t } = useI18n();

type Props = {
  dataSource: SceneInfo[];
  projectId: string;
  userInfo: { id: string; };
  appInfo: { id: string; };
  notify: string;
}

const props = withDefaults(defineProps<Props>(), {
  dataSource: undefined,
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  notify: undefined
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'delete', value: string): void;
  (e: 'clone', value: SceneInfo): void;
}>();

const CreateTestTaskModal = defineAsyncComponent(() => import('@/components/task/createTestModal/index.vue'));
const RestartTestTaskModal = defineAsyncComponent(() => import('@/components/task/restartTestModal/index.vue'));
const ReOpenTestTaskModal = defineAsyncComponent(() => import('@/components/task/reopenTestModal/index.vue'));
const DelTestTask = defineAsyncComponent(() => import('@/components/task/delTestModal/index.vue'));
const ExportScriptModal = defineAsyncComponent(() => import('@/components/script/exportModal/index.vue'));
const ExecTestModal = defineAsyncComponent(() => import('@/views/scenario/scenario/list/ExecTest.vue'));

// eslint-disable-next-line @typescript-eslint/no-empty-function
const deleteTabPane = inject<(data: any) => void>('deleteTabPane', () => { });
const proTypeShowMap = inject<Ref<{[key: string]: boolean}>>('proTypeShowMap', ref({ showTask: true, showBackLog: true, showMeeting: true, showSprint: true, showTasStatistics: true }));

const isAdmin = inject('isAdmin', ref(false));

let actionTimer: NodeJS.Timeout;

const loading = ref(false);
const sceneList = ref<SceneInfo[]>([]);

const selectedId = ref<string>();
const selectedScriptId = ref<string>();
const auth = ref(false);

const toAuthVisible = ref(false);
const createTestTaskVisible = ref(false);
const deleteTaskVisible = ref(false);
const exportVisible = ref(false);
const execTestVisible = ref(false);

const permissionMap = ref<{
  [key: string]: {
    scenarioAuth: boolean;
    permissions: MenuItemPermission[];
  }
}>({});
const dropdownMenuItemsMap = ref<{ [key: string]: MenuItem[] }>({});

const mouseenterHandler = async (data: SceneInfo) => {
  if (actionTimer) {
    clearTimeout(actionTimer);
  }

  // 无权限控制的不需要加载权限数据
  if (isAdmin.value || !data.auth) {
    return;
  }

  const id = data.id;
  const timestamp = permissionMap.value[id] ? 1000 : 300;
  actionTimer = setTimeout(async () => {
    const [error, { data }] = await scenario.getCurrentScenarioAuth(id);
    if (error) {
      return;
    }

    permissionMap.value[id] = {
      ...data,
      permissions: data.permissions?.map(item => item.value)
    };
  }, timestamp);
};

const mouseleaveHandler = () => {
  if (actionTimer) {
    clearTimeout(actionTimer);
  }
};

const toAuth = ({ id, auth: _authFlag }: { auth: boolean; id: string }) => {
  selectedId.value = id;
  toAuthVisible.value = true;
  auth.value = _authFlag;
};

const authFlagChange = ({ auth }: { auth: boolean }) => {
  const data = sceneList.value;
  const targetId = selectedId.value;
  for (let i = 0, len = data.length; i < len; i++) {
    if (data[i].id === targetId) {
      data[i].auth = auth;
      break;
    }
  }
};

const cancelFavourite = async (id: string) => {
  loading.value = true;
  const [error] = await scenario.deleteScenarioFavorite(id);
  loading.value = false;
  if (error) {
    return;
  }

  const index = dropdownMenuItemsMap.value[id].findIndex(item => item.key === 'cancelFavouriteFlag');
  const data = dropdownMenuItems.find(item => item.key === 'favouriteFlag');
  dropdownMenuItemsMap.value[id].splice(index, 1, data!);
  notification.success(t('scenario.list.messages.cancelFavouriteSuccess'));
};

const cancelFollow = async (id: string) => {
  loading.value = true;
  const [error] = await scenario.deleteScenarioFollow(id);
  loading.value = false;
  if (error) {
    return;
  }

  const index = dropdownMenuItemsMap.value[id].findIndex(item => item.key === 'cancelFollowFlag');
  const data = dropdownMenuItems.find(item => item.key === 'followFlag');
  dropdownMenuItemsMap.value[id].splice(index, 1, data!);
  notification.success(t('scenario.list.messages.cancelFollowSuccess'));
};

const toClone = async (value: SceneInfo) => {
  const [error] = await scenario.cloneScenario(value.id);
  if (error) {
    return;
  }

  notification.success(t('tips.cloneSuccess'));
  emit('clone', value);
};

const deleteScene = ({ name, id }: { name: string; id: string }) => {
  modal.confirm({
    centered: true,
    content: t('scenario.list.messages.deleteConfirm', { name }),
    async onOk () {
      loading.value = true;
      const [error] = await scenario.deleteScenario(id);
      loading.value = false;
      if (error) {
        return;
      }

      deleteTabPane([id]);// 通知已经打开的tabs删除该tab
      const delIdx = sceneList.value.findIndex(i => i.id === id);
      sceneList.value.splice(delIdx, 1);

      emit('delete', id);
      notification.success(t('scenario.list.messages.deleteSuccess'));
    }
  });
};

const deleteTestTask = (id: string) => {
  deleteTaskVisible.value = true;
  selectedId.value = id;
};

const toTest = async (data: SceneInfo) => {
  selectedId.value = data.id;
  selectedScriptId.value = data.scriptId;
  execTestVisible.value = true;
  // loading.value = true;
  // const [error] = await exec.addExecutetByScript({
  //   scriptId: data.scriptId
  // });
  // loading.value = false;
  // if (error) {
  //   return;
  // }

  // notification.success('添加“执行测试”成功，请在“执行”中查看详情和结果');
};

const toExport = (data: SceneInfo) => {
  exportVisible.value = true;
  selectedId.value = data.scriptId;
};

const toFavourite = async (id: string) => {
  loading.value = true;
  const [error] = await scenario.addScenarioFavorite(id);
  loading.value = false;
  if (error) {
    return;
  }

  const index = dropdownMenuItemsMap.value[id].findIndex(item => item.key === 'favouriteFlag');
  const data = dropdownMenuItems.find(item => item.key === 'cancelFavouriteFlag');
  dropdownMenuItemsMap.value[id].splice(index, 1, data!);
  notification.success(t('scenario.list.messages.favouriteSuccess'));
};

const toFollow = async (id: string) => {
  loading.value = true;
  const [error] = await scenario.addScenarioFollow(id);
  loading.value = false;
  if (error) {
    return;
  }

  const index = dropdownMenuItemsMap.value[id].findIndex(item => item.key === 'followFlag');
  const data = dropdownMenuItems.find(item => item.key === 'cancelFollowFlag');
  dropdownMenuItemsMap.value[id].splice(index, 1, data!);
  notification.success(t('scenario.list.messages.followSuccess'));
};

// 重新打开测试任务
const reopenTestTaskVisible = ref(false);
const reopenContent = ref('');
const reopenTestTask = (item) => {
  selectedId.value = item.id;
  reopenTestTaskVisible.value = true;
  reopenContent.value = t('scenario.list.messages.reopenConfirm', { name: item.name });
};

// 重新开始测试
const restartTestTaskVisible = ref(false);
const restartContent = ref('');
const restartTestTask = (item) => {
  selectedId.value = item.id;
  restartTestTaskVisible.value = true;
  restartContent.value = t('scenario.list.messages.restartConfirm', { name: item.name });
};

const createTestTask = (id: string): void => {
  selectedId.value = id;
  createTestTaskVisible.value = true;
};

const menuItemClick = (key: MenuItemKey, data: SceneInfo): void => {
  if (key === 'auth') {
    toAuth(data);
    return;
  }

  if (key === 'cancelFavouriteFlag') {
    cancelFavourite(data.id);
    return;
  }

  if (key === 'cancelFollowFlag') {
    cancelFollow(data.id);
    return;
  }

  if (key === 'deleteTestTask') {
    deleteTestTask(data.id);
    return;
  }

  if (key === 'performTesting') {
    toTest(data);
    return;
  }

  if (key === 'delete') {
    deleteScene(data);
    return;
  }

  if (key === 'export') {
    toExport(data);
    return;
  }

  if (key === 'favouriteFlag') {
    toFavourite(data.id);
    return;
  }

  if (key === 'followFlag') {
    toFollow(data.id);
    return;
  }

  if (key === 'reopenTestTask') {
    reopenTestTask(data);
    return;
  }

  if (key === 'restartTestTask') {
    restartTestTask(data);
    return;
  }

  if (key === 'createTestTask') {
    createTestTask(data.id);
  }
};

onMounted(() => {
  watch(() => props.dataSource, (newValue) => {
    if (!newValue.length) {
      sceneList.value = [];
      return;
    }

    for (let i = 0, len = newValue.length; i < len; i++) {
      const { id, followFlag, favouriteFlag } = newValue[i];
      dropdownMenuItemsMap.value[id] = dropdownMenuItems.filter(item => {
        const key = item.key;
        if (key === 'cancelFavouriteFlag') {
          return favouriteFlag;
        }

        if (key === 'cancelFollowFlag') {
          return followFlag;
        }

        if (key === 'favouriteFlag') {
          return !favouriteFlag;
        }

        if (key === 'followFlag') {
          return !followFlag;
        }

        if (!proTypeShowMap.value.showTask && ['restartTestTask', 'createTestTask', 'reopenTestTask', 'deleteTestTask'].includes(key)) {
          return false;
        }

        return true;
      });
    }

    sceneList.value = newValue;
  }, { immediate: true });
});

const resBgColorMap = {
  CREATED: 'rgba(45, 142, 255, 1)',
  PENDING: 'rgba(45, 142, 255, 1)',
  RUNNING: 'rgba(103, 215, 255, 1)',
  STOPPED: 'rgba(245, 34, 45, 1)',
  FAILED: 'rgba(245, 34, 45, 1)',
  TIMEOUT: 'rgba(245, 34, 45, 1)',
  COMPLETED: 'rgba(82, 196, 26, 1)',
  '': 'gray'
};

const dropdownMenuItems: readonly MenuItem[] = [
  { key: 'followFlag', name: t('scenario.list.actions.follow'), permission: 'VIEW', icon: 'icon-yiguanzhu' },
  { key: 'cancelFollowFlag', name: t('scenario.list.actions.cancelFollow'), permission: 'VIEW', icon: 'icon-quxiaoguanzhu' },
  { key: 'favouriteFlag', name: t('scenario.list.actions.favourite'), permission: 'VIEW', icon: 'icon-yishoucang' },
  { key: 'cancelFavouriteFlag', name: t('scenario.list.actions.cancelFavourite'), permission: 'VIEW', icon: 'icon-quxiaoshoucang' },
  { key: 'auth', name: t('scenario.list.actions.auth'), permission: 'GRANT', icon: 'icon-quanxian1' },
  { key: 'export', name: t('scenario.list.actions.export'), permission: 'EXPORT', icon: 'icon-daochu' },
  // { key: 'performTesting', name: '执行测试', permission: 'TEST', icon: 'icon-zhihangceshi' },
  { key: 'delete', name: t('scenario.list.actions.delete'), permission: 'DELETE', icon: 'icon-qingchu' },
  { key: 'createTestTask', name: t('scenario.list.actions.createTestTask'), permission: 'TEST', icon: 'icon-shengchengceshirenwu1', tip: '生成功能、性能和稳定性测试任务。' },
  { key: 'restartTestTask', name: t('scenario.list.actions.restartTestTask'), permission: 'TEST', icon: 'icon-zhongxinkaishiceshi', tip: '将任务更新为`待处理`，相关统计计数和状态会被清除。' },
  { key: 'reopenTestTask', name: t('scenario.list.actions.reopenTestTask'), permission: 'TEST', icon: 'icon-zhongxindakaiceshirenwu', tip: '将任务状态更新为`待处理`、 不清理统计计数和状态。' },
  { key: 'deleteTestTask', name: t('scenario.list.actions.deleteTestTask'), permission: 'TEST', icon: 'icon-shanchuceshirenwu1', tip: '删除接口对应功能、性能和稳定性测试任务。' }
];
</script>
<template>
  <div class="text-3 leading-5">
    <div class="flex flex-wrap">
      <GridList :itemWidth="328" :dataSource="sceneList">
        <template #default="record">
          <div class="h-38.5 px-3 py-2.5 border rounded border-theme-text-box">
            <div class="flex">
              <RouterLink
                :to="record.detailLink"
                :title="record.name"
                class="block mb-2 truncate text-3 font-bold text-theme-special text-theme-text-hover flex-1 min-w-0">
                {{ record.name }}
              </RouterLink>
              <div>
                <Badge :text="record.lastExecStatus?.message || t('scenario.list.table.noExecute')" :color="resBgColorMap[record.lastExecStatus?.value || '']" />
                <template v-if="record?.lastExecStatus?.value !== 'COMPLETED' && record?.lastExecFailureMessage">
                  <Tooltip
                    placement="topLeft"
                    arrowPointAtCenter
                    :overlayStyle="{ 'max-width': '400px' }">
                    <Icon icon="icon-tishi1" class="ml-1 text-3.5 text-status-warn cursor-pointer" />
                    <template #title>
                      <div>
                        {{ record?.lastExecFailureMessage }}
                      </div>
                    </template>
                  </Tooltip>
                </template>
              </div>
            </div>
            <div v-if="!record.description" class="h-9 leading-4.5 mb-2.5 text-theme-sub-content">{{ t('scenario.list.table.noDescription') }}</div>
            <TypographyParagraph
              v-else
              class="h-9 leading-4.5 mb-2.5 text-theme-sub-content"
              :content="record.description"
              :ellipsis="{ tooltip: record.description, rows: 2 }" />

            <div class="flex items-center space-x-3 mb-2.5 text-theme-sub-content">
              <Image
                :src="record.avatar"
                type="avatar"
                class="flex-shrink-0 w-6 h-6 rounded-xl" />
              <div class="flex items-center space-x-3">
                <span>{{ record.createdByName }}</span>
                <span>{{ t('scenario.list.table.createdBy') }}{{ record.createdDate }}</span>
              </div>
            </div>

            <div class="flex items-center justify-between">
              <div class="flex-shrink-0 flex space-x-2">
                <div class="bg-tag">{{ record.plugin }}</div>
                <ScriptTypeTag class="bg-tag" :value="record.scriptType" />
              </div>
              <div
                class="flex items-center justify-end space-x-1.75"
                @mouseenter="mouseenterHandler(record)"
                @mouseleave="mouseleaveHandler">
                <Button
                  :disabled="record.auth && permissionMap[record.id] && permissionMap[record.id].scenarioAuth && !permissionMap[record.id].permissions?.includes('TEST')"
                  type="text"
                  size="small"
                  class="flex items-center justify-center p-0 leading-5 w-5 h-5 !border-0"
                  title="执行测试"
                  @click="toTest(record)">
                  <Icon icon="icon-zhihangceshi" class="text-3.5" />
                </Button>

                <Button
                  :disabled="record.auth && permissionMap[record.id] && permissionMap[record.id].scenarioAuth && !permissionMap[record.id].permissions?.includes('MODIFY')"
                  type="text"
                  size="small"
                  class="flex items-center justify-center p-0 leading-5 w-5 h-5 !border-0"
                  title="编辑">
                  <RouterLink :to="record.nameLinkUrl" class="w-full h-full flex items-center justify-center">
                    <Icon icon="icon-shuxie" class="text-3.5" />
                  </RouterLink>
                </Button>

                <Button
                  :disabled="record.auth && permissionMap[record.id] && permissionMap[record.id].scenarioAuth && !permissionMap[record.id].permissions?.includes('VIEW')"
                  type="text"
                  size="small"
                  class="flex items-center justify-center p-0 leading-5 w-5 h-5 !border-0"
                  title="克隆"
                  @click="toClone(record)">
                  <Icon icon="icon-fuzhi" class="text-3.5" />
                </Button>

                <Dropdown
                  :noAuth="!record.auth"
                  :permissions="permissionMap[record.id]?.permissions"
                  :authFlagKey="['scenarioAuth']"
                  :menuItems="dropdownMenuItemsMap[record.id]"
                  @click="menuItemClick($event.key, record)">
                  <Button
                    type="text"
                    size="small"
                    class="flex items-center justify-center p-0 leading-5 w-5 h-5 !border-0">
                    <Icon icon="icon-gengduo" class="text-3.5" />
                  </Button>
                </Dropdown>
              </div>
            </div>
          </div>
        </template>
      </GridList>
    </div>

    <AsyncComponent :visible="toAuthVisible">
      <AuthorizeModal
        v-model:visible="toAuthVisible"
        enumKey="ScenarioPermission"
        :appId="props.appInfo?.id"
        :listUrl="`${TESTER}/scenario/auth?scenarioId=${selectedId}`"
        :delUrl="`${TESTER}/scenario/auth`"
        :addUrl="`${TESTER}/scenario/${selectedId}/auth`"
        :updateUrl="`${TESTER}/scenario/auth`"
        :enabledUrl="`${TESTER}/scenario/${selectedId}/auth/enabled`"
        :initStatusUrl="`${TESTER}/scenario/${selectedId}/auth/status`"
        :onTips="t('scenario.list.tips.authOn')"
        :offTips="t('scenario.list.tips.authOff')"
        :title="t('scenario.list.tips.authTitle')"
        @change="authFlagChange" />
    </AsyncComponent>

    <AsyncComponent :visible="createTestTaskVisible">
      <CreateTestTaskModal
        v-model:id="selectedId"
        v-model:visible="createTestTaskVisible"
        :infoText="t('scenario.list.tips.testTaskInfo')"
        type="SCENARIO" />
    </AsyncComponent>

    <!-- 重新开始测试 -->
    <AsyncComponent :visible="restartTestTaskVisible">
      <RestartTestTaskModal
        v-model:visible="restartTestTaskVisible"
        v-model:id="selectedId"
        :content="restartContent"
        type="SCENARIO" />
    </AsyncComponent>

    <!-- 重新打开测试任务 -->
    <AsyncComponent :visible="reopenTestTaskVisible">
      <ReOpenTestTaskModal
        v-model:visible="reopenTestTaskVisible"
        v-model:id="selectedId"
        :content="reopenContent"
        type="SCENARIO" />
    </AsyncComponent>

    <AsyncComponent :visible="deleteTaskVisible">
      <DelTestTask
        :id="selectedId"
        v-model:visible="deleteTaskVisible"
        type="SCENARIO" />
    </AsyncComponent>

    <AsyncComponent :visible="exportVisible">
      <ExportScriptModal v-model:visible="exportVisible" :ids="[selectedId]" />
    </AsyncComponent>

    <AsyncComponent :visible="execTestVisible">
      <ExecTestModal
        v-model:scenarioId="selectedId"
        v-model:visible="execTestVisible"
        :okAction="`${TESTER}/scenario/${selectedId}/exec`"
        :scriptId="selectedScriptId" />
    </AsyncComponent>
  </div>
</template>

<style scoped>
.bg-tag {
  flex-shrink: 0;
  padding: 0 7px;
  border-radius: 10px;
  background-color: rgba(15, 159, 255, 75%);
  color: #fff;
  line-height: 20px;
}
</style>
