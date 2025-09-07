<script lang="ts" setup>
import { defineAsyncComponent, inject, onMounted, Ref, ref, watch } from 'vue';
import { Badge, Button, Tooltip, TypographyParagraph } from 'ant-design-vue';
import { AsyncComponent, AuthorizeModal, Dropdown, GridList, Icon, Image, ScriptTypeTag, notification, modal } from '@xcan-angus/vue-ui';
import { TESTER } from '@xcan-angus/infra';
import { useI18n } from 'vue-i18n';
import { scenario } from '@/api/tester';
import { ScenarioPermission, ExecStatus } from '@/enums/enums';

// Import composables
import { useScenarioModals, useScenarioPermissions } from './composables';

import { MenuItem, MenuItemKey, ScenarioInfo } from './types';

const { t } = useI18n();

type Props = {
  dataSource: ScenarioInfo[];
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
  (e: 'clone', value: ScenarioInfo): void;
}>();

// Async components
const CreateTestTaskModal = defineAsyncComponent(() => import('@/components/task/createTestModal/index.vue'));
const RestartTestTaskModal = defineAsyncComponent(() => import('@/components/task/restartTestModal/index.vue'));
const ReOpenTestTaskModal = defineAsyncComponent(() => import('@/components/task/reopenTestModal/index.vue'));
const DelTestTask = defineAsyncComponent(() => import('@/components/task/delTestModal/index.vue'));
const ExportScriptModal = defineAsyncComponent(() => import('@/components/script/exportModal/index.vue'));
const ExecTestModal = defineAsyncComponent(() => import('@/views/scenario/scenario/list/ExecTest.vue'));

// Inject dependencies
// eslint-disable-next-line @typescript-eslint/no-empty-function
const deleteTabPane = inject<(data: any) => void>('deleteTabPane', () => { });
const proTypeShowMap = inject<Ref<{[key: string]: boolean}>>('proTypeShowMap', ref({ showTask: true, showBackLog: true, showMeeting: true, showSprint: true, showTasStatistics: true }));

// Initialize composables
const {
  permissionMap,
  handleMouseEnter,
  handleMouseLeave,
  hasPermission,
  filterMenuItems
} = useScenarioPermissions();

const {
  toAuthVisible,
  createTestTaskVisible,
  deleteTaskVisible,
  exportVisible,
  execTestVisible,
  reopenTestTaskVisible,
  restartTestTaskVisible,
  selectedId,
  selectedScriptId,
  reopenContent,
  restartContent,
  openAuthModal,
  openExecTestModal,
  openExportModal,
  openCreateTestTaskModal,
  openDeleteTestTaskModal,
  openReopenTestTaskModal,
  openRestartTestTaskModal
} = useScenarioModals();

// Component state
const loading = ref(false);
const scenarioList = ref<ScenarioInfo[]>([]);
const dropdownMenuItemsMap = ref<{ [key: string]: MenuItem[] }>({});

// Dropdown menu items
const dropdownMenuItems: readonly MenuItem[] = [
  { key: 'follow', name: t('scenario.list.actions.follow'), permission: 'VIEW', icon: 'icon-yiguanzhu' },
  { key: 'cancelFollow', name: t('scenario.list.actions.cancelFollow'), permission: 'VIEW', icon: 'icon-quxiaoguanzhu' },
  { key: 'favourite', name: t('scenario.list.actions.favourite'), permission: 'VIEW', icon: 'icon-yishoucang' },
  { key: 'cancelFavourite', name: t('scenario.list.actions.cancelFavourite'), permission: 'VIEW', icon: 'icon-quxiaoshoucang' },
  { key: 'auth', name: t('scenario.list.actions.auth'), permission: 'GRANT', icon: 'icon-quanxian1' },
  { key: 'export', name: t('scenario.list.actions.export'), permission: 'EXPORT', icon: 'icon-daochu' },
  { key: 'delete', name: t('scenario.list.actions.delete'), permission: 'DELETE', icon: 'icon-qingchu' },
  {
    key: 'createTestTask',
    name: t('scenario.list.actions.createTestTask'),
    permission: 'TEST',
    icon: 'icon-shengchengceshirenwu1',
    tip: '生成功能、性能和稳定性测试任务。'
  },
  {
    key: 'restartTestTask',
    name: t('scenario.list.actions.restartTestTask'),
    permission: 'TEST',
    icon: 'icon-zhongxinkaishiceshi',
    tip: '将任务更新为`待处理`，相关统计计数和状态会被清除。'
  },
  {
    key: 'reopenTestTask',
    name: t('scenario.list.actions.reopenTestTask'),
    permission: 'TEST',
    icon: 'icon-zhongxindakaiceshirenwu',
    tip: '将任务状态更新为`待处理`、 不清理统计计数和状态。'
  },
  {
    key: 'deleteTestTask',
    name: t('scenario.list.actions.deleteTestTask'),
    permission: 'TEST',
    icon: 'icon-shanchuceshirenwu1',
    tip: '删除接口对应功能、性能和稳定性测试任务。'
  }
];

// Execution status color mapping
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

// Watch for data source changes
onMounted(() => {
  watch(() => props.dataSource, (newValue) => {
    if (!newValue.length) {
      scenarioList.value = [];
      return;
    }

    for (let i = 0, len = newValue.length; i < len; i++) {
      const { id, follow, favourite } = newValue[i];
      dropdownMenuItemsMap.value[id] = filterMenuItems(dropdownMenuItems, newValue[i], proTypeShowMap.value);
    }

    scenarioList.value = newValue;
  }, { immediate: true });
});

// Event handlers
const authFlagChange = ({ auth: authFlag }: { auth: boolean }) => {
  const data = scenarioList.value;
  const targetId = selectedId.value;
  for (let i = 0, len = data.length; i < len; i++) {
    if (data[i].id === targetId) {
      data[i].auth = authFlag;
      break;
    }
  }
};

const menuItemClick = (key: MenuItemKey, data: ScenarioInfo): void => {
  switch (key) {
    case 'auth':
      openAuthModal(data.id, data.auth);
      break;
    case 'cancelFavourite':
      toggleScenarioFavorite(data.id, true);
      break;
    case 'cancelFollow':
      toggleScenarioFollow(data.id, true);
      break;
    case 'deleteTestTask':
      openDeleteTestTaskModal(data.id);
      break;
    case 'performTesting':
      openExecTestModal(data.id, data.scriptId);
      break;
    case 'delete':
      deleteScenario(data.name, data.id);
      break;
    case 'export':
      openExportModal(data.scriptId);
      break;
    case 'favourite':
      toggleScenarioFavorite(data.id, false);
      break;
    case 'follow':
      toggleScenarioFollow(data.id, false);
      break;
    case 'reopenTestTask':
      openReopenTestTaskModal(data.id, t('scenario.list.messages.reopenConfirm', { name: data.name }));
      break;
    case 'restartTestTask':
      openRestartTestTaskModal(data.id, t('scenario.list.messages.restartConfirm', { name: data.name }));
      break;
    case 'createTestTask':
      openCreateTestTaskModal(data.id);
      break;
  }
};

// Scenario actions
const toggleScenarioFavorite = async (id: string, isFavorite: boolean) => {
  loading.value = true;
  if (isFavorite) {
    const [error] = await scenario.deleteScenarioFavorite(id);
    if (error) {
      loading.value = false;
      return;
    }

    const index = dropdownMenuItemsMap.value[id].findIndex(item => item.key === 'cancelFavourite');
    const data = dropdownMenuItems.find(item => item.key === 'favourite');
    dropdownMenuItemsMap.value[id].splice(index, 1, data!);
  } else {
    const [error] = await scenario.addScenarioFavorite(id);
    if (error) {
      loading.value = false;
      return;
    }

    const index = dropdownMenuItemsMap.value[id].findIndex(item => item.key === 'favourite');
    const data = dropdownMenuItems.find(item => item.key === 'cancelFavourite');
    dropdownMenuItemsMap.value[id].splice(index, 1, data!);
  }
  loading.value = false;

  const message = isFavorite
    ? t('scenario.list.messages.cancelFavouriteSuccess')
    : t('scenario.list.messages.favouriteSuccess');

  // Assuming notification is available globally or imported
  notification.success(message);
};

const toggleScenarioFollow = async (id: string, isFollowing: boolean) => {
  loading.value = true;
  if (isFollowing) {
    const [error] = await scenario.deleteScenarioFollow(id);
    if (error) {
      loading.value = false;
      return;
    }

    const index = dropdownMenuItemsMap.value[id].findIndex(item => item.key === 'cancelFollow');
    const data = dropdownMenuItems.find(item => item.key === 'follow');
    dropdownMenuItemsMap.value[id].splice(index, 1, data!);
  } else {
    const [error] = await scenario.addScenarioFollow(id);
    if (error) {
      loading.value = false;
      return;
    }

    const index = dropdownMenuItemsMap.value[id].findIndex(item => item.key === 'follow');
    const data = dropdownMenuItems.find(item => item.key === 'cancelFollow');
    dropdownMenuItemsMap.value[id].splice(index, 1, data!);
  }
  loading.value = false;

  const message = isFollowing
    ? t('scenario.list.messages.cancelFollowSuccess')
    : t('scenario.list.messages.followSuccess');

  // Assuming notification is available globally or imported
  notification.success(message);
};

const toClone = async (value: ScenarioInfo) => {
  const [error] = await scenario.cloneScenario(value.id);
  if (error) {
    return;
  }

  // Assuming notification is available globally or imported
  // notification.success(t('tips.cloneSuccess'));
  emit('clone', value);
};

const deleteScenario = async (name: string, id: string) => {
  // Assuming modal is available globally or imported
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

      deleteTabPane([id]);
      const delIdx = scenarioList.value.findIndex(i => i.id === id);
      scenarioList.value.splice(delIdx, 1);

      emit('delete', id);
      // notification.success(t('scenario.list.messages.deleteSuccess'));
    }
  });
};
</script>

<template>
  <div class="text-3 leading-5">
    <div class="flex flex-wrap">
      <GridList :itemWidth="328" :dataSource="scenarioList">
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
                <Badge
                  :text="record.lastExecStatus?.message || t('scenario.list.table.noExecute')"
                  :color="resBgColorMap[record.lastExecStatus?.value || '']" />
                <template v-if="record?.lastExecStatus?.value !== ExecStatus.COMPLETED && record?.lastExecFailureMessage">
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
            <div v-if="!record.description" class="h-9 leading-4.5 mb-2.5 text-theme-sub-content">
              {{ t('scenario.list.table.noDescription') }}
            </div>
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
                @mouseenter="handleMouseEnter(record)"
                @mouseleave="handleMouseLeave">
                <Button
                  :disabled="record.auth && permissionMap[record.id]
                    && permissionMap[record.id].scenarioAuth
                    && !hasPermission(record.id, ScenarioPermission.TEST)"
                  type="text"
                  size="small"
                  class="flex items-center justify-center p-0 leading-5 w-5 h-5 !border-0"
                  title="执行测试"
                  @click="openExecTestModal(record.id, record.scriptId)">
                  <Icon icon="icon-zhihangceshi" class="text-3.5" />
                </Button>

                <Button
                  :disabled="record.auth && permissionMap[record.id]
                    && permissionMap[record.id].scenarioAuth
                    && !hasPermission(record.id, ScenarioPermission.MODIFY)"
                  type="text"
                  size="small"
                  class="flex items-center justify-center p-0 leading-5 w-5 h-5 !border-0"
                  title="编辑">
                  <RouterLink :to="record.nameLinkUrl" class="w-full h-full flex items-center justify-center">
                    <Icon icon="icon-shuxie" class="text-3.5" />
                  </RouterLink>
                </Button>

                <Button
                  :disabled="record.auth && permissionMap[record.id]
                    && permissionMap[record.id].scenarioAuth
                    && !hasPermission(record.id, ScenarioPermission.VIEW)"
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

    <AsyncComponent :visible="restartTestTaskVisible">
      <RestartTestTaskModal
        v-model:visible="restartTestTaskVisible"
        v-model:id="selectedId"
        :content="restartContent"
        type="SCENARIO" />
    </AsyncComponent>

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
