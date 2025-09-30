<script lang="ts" setup>
import { defineAsyncComponent, inject, onMounted, Ref, ref, watch } from 'vue';
import { Badge, Button, Tooltip, TypographyParagraph } from 'ant-design-vue';
import { AsyncComponent, Dropdown, GridList, Icon, Image, ScriptTypeTag } from '@xcan-angus/vue-ui';
import { CombinedTargetType, TESTER } from '@xcan-angus/infra';
import { useI18n } from 'vue-i18n';
import { ScenarioPermission, ExecStatus } from '@/enums/enums';
import { BasicDataSourceProps } from '@/types/types';

// Import composables
import { useScenarioModals, useScenarioPermissions, useScenarioActions, useScenarioMenuItems } from './composables';

import { MenuItem, MenuItemKey, ScenarioInfo } from './types';

const { t } = useI18n();

const props = withDefaults(defineProps<BasicDataSourceProps<ScenarioInfo[]>>(), {
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
const AuthorizeModal = defineAsyncComponent(() => import('@/components/AuthorizeModal/index.vue'));
const CreateTestTaskModal = defineAsyncComponent(() => import('@/components/task/CreateTestModal.vue'));
const RestartTestTaskModal = defineAsyncComponent(() => import('@/components/task/RestartTestModal.vue'));
const ReopenTestTaskModal = defineAsyncComponent(() => import('@/components/task/ReopenTestModal.vue'));
const DeleteTestTask = defineAsyncComponent(() => import('@/components/task/DeleteTestModal.vue'));
const ExportScriptModal = defineAsyncComponent(() => import('@/components/script/ExportScriptModal.vue'));
const ExecTestModal = defineAsyncComponent(() => import('@/views/scenario/scenario/list/ExecTest.vue'));

// Inject dependencies
// eslint-disable-next-line @typescript-eslint/no-empty-function
const deleteTabPane = inject<(data: any) => void>('deleteTabPane', () => { });
const proTypeShowMap = inject<Ref<{[key: string]: boolean}>>('proTypeShowMap', ref({ showTask: true, showBackLog: true, showMeeting: true, showSprint: true, showTasStatistics: true }));

// Component state
const scenarioList = ref<ScenarioInfo[]>([]);
const dropdownMenuItemsMap = ref<{ [key: string]: MenuItem[] }>({});

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

const {
  dropdownMenuItems
} = useScenarioMenuItems();

const {
  toggleScenarioFavorite,
  toggleScenarioFollow,
  toClone,
  deleteScenario,
  authFlagChange
} = useScenarioActions(
  ref(props.projectId),
  // eslint-disable-next-line @typescript-eslint/no-empty-function
  () => {}, // addTabPane - not used in this component
  deleteTabPane,
  scenarioList,
  dropdownMenuItemsMap,
  dropdownMenuItems,
  selectedId
);

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
      const { id } = newValue[i];
      dropdownMenuItemsMap.value[id] = filterMenuItems(dropdownMenuItems.value, newValue[i], proTypeShowMap.value);
    }

    scenarioList.value = newValue;
  }, { immediate: true });
});

// Event handlers

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
      deleteScenario(data.name, data.id, emit);
      break;
    case 'export':
      openExportModal(data.scriptId);
      break;
    case 'addFavourite':
      toggleScenarioFavorite(data.id, false);
      break;
    case 'addFollow':
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

// Scenario actions - now handled by useScenarioActions composable
</script>

<template>
  <div class="text-3 leading-5">
    <div class="flex flex-wrap">
      <GridList :itemWidth="328" :dataSource="scenarioList">
        <template #default="record">
          <div class="h-38.5 px-3 py-2.5 border rounded border-theme-text-box">
            <div class="flex">
              <RouterLink
                :to="record.detailLinkUrl"
                :title="record.name"
                class="block mb-2 truncate text-3.5 font-bold text-theme-special text-theme-text-hover flex-1 min-w-0">
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
              {{ t('common.noDescription') }}
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
                <span>{{ t('common.createdBy') }}&nbsp;{{ record.createdDate }}</span>
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
                  :title="t('scenario.list.tooltips.executeTest')"
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
                  :title="t('actions.edit')">
                  <RouterLink :to="record.editLinkUrl" class="w-full h-full flex items-center justify-center">
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
                  :title="t('actions.clone')"
                  @click="toClone(record, emit)">
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
        :enumKey="ScenarioPermission"
        :appId="props.appInfo?.id"
        :listUrl="`${TESTER}/scenario/auth?scenarioId=${selectedId || ''}`"
        :delUrl="`${TESTER}/scenario/auth`"
        :addUrl="`${TESTER}/scenario/${selectedId || ''}/auth`"
        :updateUrl="`${TESTER}/scenario/auth`"
        :enabledUrl="`${TESTER}/scenario/${selectedId || ''}/auth/enabled`"
        :initStatusUrl="`${TESTER}/scenario/${selectedId || ''}/auth/status`"
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
        :type="CombinedTargetType.SCENARIO" />
    </AsyncComponent>

    <AsyncComponent :visible="restartTestTaskVisible">
      <RestartTestTaskModal
        v-model:visible="restartTestTaskVisible"
        v-model:id="selectedId"
        :content="restartContent"
        :type="CombinedTargetType.SCENARIO" />
    </AsyncComponent>

    <AsyncComponent :visible="reopenTestTaskVisible">
      <ReopenTestTaskModal
        v-model:visible="reopenTestTaskVisible"
        v-model:id="selectedId"
        :content="reopenContent"
        :type="CombinedTargetType.SCENARIO" />
    </AsyncComponent>

    <AsyncComponent :visible="deleteTaskVisible">
      <DeleteTestTask
        :id="selectedId"
        v-model:visible="deleteTaskVisible"
        :type="CombinedTargetType.SCENARIO" />
    </AsyncComponent>

    <AsyncComponent :visible="exportVisible">
      <ExportScriptModal v-model:visible="exportVisible" :ids="selectedId ? [selectedId] : []" />
    </AsyncComponent>

    <AsyncComponent :visible="execTestVisible">
      <ExecTestModal
        v-model:scenarioId="selectedId"
        v-model:visible="execTestVisible"
        :okAction="`${TESTER}/scenario/${selectedId || ''}/exec`"
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
