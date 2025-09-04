<script setup lang="ts">
// Vue composition API imports
import { defineAsyncComponent, onMounted, toRef, watch } from 'vue';
import { useI18n } from 'vue-i18n';

// Custom UI components
import { AsyncComponent, Icon, IconRefresh, Input, NoData, Spin } from '@xcan-angus/vue-ui';

// Ant Design components
import { Button, Dropdown, Menu, MenuItem, Tree } from 'ant-design-vue';

// Composables and types
import { useData, useActions, useTree } from './composables';
import type { ModuleItem, ModuleProps } from './types';

// Initialize i18n
const { t } = useI18n();

// Props definition with proper TypeScript support
const props = withDefaults(defineProps<ModuleProps>(), {
  projectId: '',
  userInfo: () => ({ id: '' }),
  appInfo: () => ({ id: '' }),
  notify: '',
  disabled: false,
  projectName: ''
});

// Async component definitions for better code splitting
const CreateModal = defineAsyncComponent(() => import('./Add.vue'));
const MoveModuleModal = defineAsyncComponent(() => import('./Move.vue'));

// Convert props to reactive references for composables
const projectIdRef = toRef(props, 'projectId');

// Initialize composables with proper data flow
const {
  dataList,
  loading,
  loaded,
  searchedFlag,
  total,
  fetchModuleTree,
  refreshData,
  resetData,
  searchModules
} = useData(projectIdRef);

const {
  editId,
  modalVisible,
  moveVisible,
  activeModule,
  currentParentId,
  createModule,
  updateModule,
  deleteModule,
  startEdit,
  cancelEdit,
  saveEdit,
  openCreateModal,
  closeModals,
  handleMenuAction
} = useActions(refreshData);

const {
  searchValue,
  nameInputRef,
  handleSearchChange,
  clearSearch,
  canMoveUp,
  canMoveDown,
  canAddSubModule,
  processTreeData,
  findModuleById
} = useTree(dataList, searchModules);

// Event handlers for component interactions

/**
 * Handles the refresh button click
 * Resets pagination and fetches fresh data
 */
const handleRefresh = async (): Promise<void> => {
  await refreshData();
};

/**
 * Handles successful module creation
 * Refreshes the data to show the new module
 */
const handleCreateSuccess = async (): Promise<void> => {
  await refreshData();
  closeModals();
};

/**
 * Handles Enter key press during inline editing
 * Saves the edited module name
 */
const handleEditEnter = async (id: string, event: { target: { value: string } }): Promise<void> => {
  const newName = event.target.value?.trim();
  if (!newName) {
    return;
  }

  await saveEdit(id, newName);
};

/**
 * Handles blur event during inline editing
 * Auto-saves the changes after a short delay
 */
const handleEditBlur = (id: string, event: { target: { value: string } }): void => {
  setTimeout(() => {
    if (editId.value === id) {
      handleEditEnter(id, event);
    }
  }, 300);
};

/**
 * Handles dropdown menu clicks for module actions
 * Dispatches to appropriate action handler
 */
const handleDropdownClick = (menu: { key: string }, record: ModuleItem & {
  index: number;
  ids: string[];
}): void => {
  handleMenuAction(menu.key, record);
};

/**
 * Handles module move completion
 * Refreshes data and closes modal
 */
const handleMoveComplete = async (): Promise<void> => {
  await refreshData();
  closeModals();
};

/**
 * Resets all component state
 * Used when switching projects or initializing
 */
const resetComponentState = (): void => {
  resetData();
  closeModals();
  searchValue.value = '';
};

// Lifecycle hooks and watchers

/**
 * Watches for project ID changes and initializes data
 * Resets state and fetches new data when project changes
 */
onMounted(() => {
  watch(projectIdRef, (newProjectId) => {
    resetComponentState();

    if (!newProjectId) {
      return;
    }

    // Fetch initial data for the new project
    fetchModuleTree();
  }, {
    immediate: true
  });
});
</script>
<template>
  <div class="w-full h-full leading-5 text-xs overflow-auto">
    <!-- Module description section -->
    <div class="space-y-4">
      <div class="space-y-2">
        <div class="flex items-center space-x-2">
          <div class="w-1 h-4 bg-gradient-to-b from-green-500 to-green-600 rounded-full"></div>
          <span class="text-3.5 font-semibold">{{ t('module.about') }}</span>
        </div>
        <div class="text-3.5 text-gray-700 ml-3">{{ t('module.aboutDescription') }}</div>
      </div>

      <div class="space-y-2">
        <div class="flex items-center space-x-2">
          <div class="w-1 h-4 bg-gradient-to-b from-purple-500 to-purple-600 rounded-full"></div>
          <span class="text-3.5 font-semibold text-gray-600">{{ t('module.addedModules') }}</span>
        </div>

        <Spin
          :spinning="loading"
          class="flex flex-col min-h-96">
          <template v-if="loaded">
            <!-- Empty state when no modules exist -->
            <div v-if="!searchedFlag && dataList.length === 0" class="flex-1 flex flex-col items-center justify-center py-12">
              <img src="../../../assets/images/nodata.png" class="w-32 h-32 mb-4">
              <div v-if="!props.disabled" class="flex items-center text-gray-500 text-xs">
                <span>{{ t('module.noModules') }}</span>
                <Button
                  type="link"
                  size="small"
                  class="text-xs py-0 px-1 text-blue-600"
                  @click="openCreateModal()">
                  {{ t('module.addModule') }}
                </Button>
              </div>
              <div v-else class="text-gray-500 text-xs">{{ t('module.noModulesDescription') }}</div>
            </div>

            <!-- Modules list with search and actions -->
            <template v-else>
              <div class="flex items-center justify-between mt-2 mb-2">
                <div class="flex items-center">
                  <Input
                    v-model:value="searchValue"
                    :placeholder="t('module.moduleNamePlaceholder')"
                    class="w-64 mr-3"
                    trimAll
                    :allowClear="true"
                    :maxlength="50"
                    @change="handleSearchChange">
                    <template #suffix>
                      <Icon class="text-xs cursor-pointer text-gray-400" icon="icon-sousuo" />
                    </template>
                  </Input>
                </div>

                <div class="flex items-center space-x-3">
                  <Button
                    v-if="!props.disabled"
                    type="primary"
                    size="small"
                    class="flex items-center space-x-1 text-xs"
                    @click="openCreateModal()">
                    <Icon icon="icon-jia" class="text-xs" />
                    <span>{{ t('module.addModule') }}</span>
                  </Button>

                  <IconRefresh @click="handleRefresh">
                    <template #default>
                      <div class="flex items-center cursor-pointer text-gray-600 space-x-1 hover:text-gray-800 text-xs">
                        <Icon icon="icon-shuaxin" class="text-xs" />
                        <span>{{ t('module.refresh') }}</span>
                      </div>
                    </template>
                  </IconRefresh>
                </div>
              </div>

              <!-- No data state for search results -->
              <NoData v-if="dataList.length === 0" class="flex-1 py-12" />

              <!-- Module tree display -->
              <div v-else class="mt-2">
                <Tree
                  :treeData="dataList"
                  blockNode
                  defaultExpandAll
                  :fieldNames="{
                    children: 'children',
                    title: 'name',
                    key: 'id'
                  }">
                  <template #title="{name, id, index, level, isLast, pid, ids, childLevels, hasEditPermission}">
                    <!-- Inline edit mode -->
                    <div v-if="editId === id" class="flex items-center">
                      <Input
                        ref="nameInputRef"
                        :placeholder="t('module.moduleNamePlaceholder')"
                        class="flex-1 mr-2 bg-white text-xs"
                        trim
                        :value="name"
                        :allowClear="false"
                        :maxlength="50"
                        @blur="handleEditBlur(id, $event)"
                        @pressEnter="handleEditEnter(id, $event)" />
                      <Button
                        type="link"
                        size="small"
                        class="px-0 py-0 mr-1 text-xs"
                        @click="cancelEdit">
                        {{ t('module.cancel') }}
                      </Button>
                    </div>

                    <!-- Normal display mode -->
                    <div v-else class="flex items-center space-x-2 tree-title group">
                      <Icon icon="icon-mokuai" class="text-xs text-gray-500" />
                      <span class="flex-1 truncate min-w-0 text-xs text-gray-700" :title="name">{{ name }}</span>

                      <!-- Action dropdown menu -->
                      <Dropdown :trigger="['click']">
                        <Button
                          v-if="!props.disabled && hasEditPermission"
                          class="hidden group-hover:inline-block"
                          type="text"
                          size="small">
                          <Icon icon="icon-more" class="text-xs text-gray-400" />
                        </Button>
                        <template #overlay>
                          <Menu class="text-xs" @click="handleDropdownClick($event, {name, id, index, ids, level, childLevels, pid})">
                            <MenuItem v-if="canAddSubModule({level})" key="add">
                              <Icon icon="icon-jia" class="text-xs" />
                              {{ t('module.newSubModule') }}
                            </MenuItem>
                            <MenuItem v-if="canMoveUp({index, pid})" key="up">
                              <Icon icon="icon-shangyi" class="text-xs" />
                              {{ index < 1 ? t('module.moveUp') : t('module.moveUpOne') }}
                            </MenuItem>
                            <MenuItem v-if="canMoveDown({isLast})" key="down">
                              <Icon icon="icon-xiayi" class="text-xs" />
                              {{ t('module.moveDown') }}
                            </MenuItem>
                            <MenuItem key="move">
                              <Icon icon="icon-yidong" class="text-xs" />
                              {{ t('module.move') }}
                            </MenuItem>
                            <MenuItem key="edit">
                              <Icon icon="icon-bianji" class="text-xs" />
                              {{ t('actions.edit') }}
                            </MenuItem>
                            <MenuItem key="del">
                              <Icon icon="icon-qingchu" class="text-xs" />
                              {{ t('actions.delete') }}
                            </MenuItem>
                          </Menu>
                        </template>
                      </Dropdown>
                    </div>
                  </template>
                </Tree>
              </div>
            </template>
          </template>
        </Spin>
      </div>
    </div>

    <!-- Modals for create and move operations -->
    <AsyncComponent :visible="modalVisible">
      <CreateModal
        v-model:visible="modalVisible"
        :projectId="props.projectId"
        :pid="currentParentId"
        @ok="handleCreateSuccess" />
    </AsyncComponent>

    <AsyncComponent :visible="moveVisible">
      <MoveModuleModal
        v-model:visible="moveVisible"
        :projectId="props.projectId"
        :projectName="props.projectName"
        :module="activeModule"
        @ok="handleMoveComplete" />
    </AsyncComponent>
  </div>
</template>
<style scoped>

:deep(.ant-tree) {
  background-color: transparent;
  font-size: 12px;
}

:deep(.ant-tree .ant-tree-treenode) {
  width: 100%;
  height: 32px;
  padding-left: 0;
  line-height: 18px;
}

:deep(.ant-tree .ant-tree-treenode.ant-tree-treenode-selected) {
  background-color: #f8fafc;
}

:deep(.ant-tree .ant-tree-treenode:hover) {
  background-color: #f8fafc;
}

:deep(.ant-tree .ant-tree-switcher) {
  width: 16px;
  height: 30px;
  margin-top: 1px;
  line-height: 30px;
}

:deep(.ant-tree .ant-tree-node-content-wrapper:hover) {
  background-color: transparent;
}

:deep(.ant-tree .ant-tree-node-content-wrapper) {
  display: flex;
  flex: 1 1 0%;
  flex-direction: column;
  justify-content: center;
  height: 32px;
  margin: 0;
  padding: 0;
  line-height: 32px;
}

:deep(.ant-tree .ant-tree-node-content-wrapper .ant-tree-iconEle) {
  height: 32px;
  line-height: 32px;
  vertical-align: initial;
}

:deep(.ant-tree .ant-tree-node-selected) {
  background-color: transparent;
}

:deep(.ant-tree .ant-tree-indent-unit) {
  width: 18px;
}

:deep(.ant-tree-node-content-wrapper.ant-tree-node-content-wrapper-normal) {
  @apply !flex-1 min-w-0;
}
</style>
