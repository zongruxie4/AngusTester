<script setup lang="ts">
import { defineAsyncComponent, onMounted, toRef, watch, ref, computed, nextTick } from 'vue';
import { useI18n } from 'vue-i18n';
import { AsyncComponent, Icon, IconRefresh, Input, Spin } from '@xcan-angus/vue-ui';
import { Button, Dropdown, Menu, MenuItem, Tree } from 'ant-design-vue';
import { BasicProps } from '@/types/types';

// Composables and types
import { useData, useActions, useTree } from './composables';
import type { ModuleItem } from './types';

// Initialize i18n
const { t } = useI18n();

// Props definition with proper TypeScript support
const props = withDefaults(defineProps<BasicProps>(), {
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

// Refs for height calculation
const containerRef = ref<HTMLElement>();
const headerRef = ref<HTMLElement>();
const searchRef = ref<HTMLElement>();

// Calculate available height for module tree
const moduleTreeHeight = computed(() => {
  if (!containerRef.value || !headerRef.value || !searchRef.value) {
    return 'calc(100vh - 250px)'; // fallback height with 100px reduction
  }

  const containerHeight = containerRef.value.clientHeight;
  const headerHeight = headerRef.value.clientHeight;
  const searchHeight = searchRef.value.clientHeight;
  const padding = 32; // py-4 = 16px top + 16px bottom
  const additionalReduction = 50; // Additional 100px reduction

  const availableHeight = containerHeight - headerHeight - searchHeight - padding - additionalReduction;
  return `${Math.max(availableHeight, 100)}px`;
});

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
const handleDropdownClick = (menu: { key: string | number }, record: ModuleItem & {
  index: number;
  ids: string[];
}): void => {
  handleMenuAction(String(menu.key), record);
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

  // Listen for window resize to recalculate height
  const handleResize = () => {
    nextTick(() => {
      // Trigger reactive update by accessing the computed value
      moduleTreeHeight.value;
    });
  };

  window.addEventListener('resize', handleResize);

  // Cleanup on unmount
  return () => {
    window.removeEventListener('resize', handleResize);
  };
});
</script>
<template>
  <div ref="containerRef" class="w-full h-full bg-white rounded-lg shadow-sm flex flex-col">
    <!-- Header Section -->
    <div ref="headerRef" class="bg-gradient-to-r from-blue-50 to-indigo-50 px-6 py-4 border-b border-gray-100">
      <div class="flex items-center justify-between">
        <div class="flex items-center space-x-3">
          <div class="w-8 h-8 bg-blue-100 rounded-lg flex items-center justify-center">
            <Icon icon="icon-mokuai" class="text-blue-600 text-lg" />
          </div>
          <div>
            <h2 class="text-3.5 font-semibold text-gray-900">{{ t('module.introduce.aboutModule') }}</h2>
            <p class="text-3.5 text-gray-600 mt-1 font-serif">{{ t('module.introduce.description') }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- Content Section -->
    <div class="flex-1 py-4 flex flex-col">
      <!-- Search Section -->
      <div ref="searchRef" class="mb-4 flex items-center justify-between">
        <div class="flex-1 max-w-md">
          <Input
            v-model:value="searchValue"
            :placeholder="t('module.messages.moduleNamePlaceholder')"
            class="w-full"
            size="small"
            trimAll
            :allowClear="true"
            :maxlength="50"
            @change="handleSearchChange">
            <template #prefix>
              <Icon class="text-gray-400" icon="icon-sousuo" />
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
            <span>{{ t('module.actions.addModule') }}</span>
          </Button>

          <IconRefresh @click="handleRefresh">
            <template #default>
              <div class="flex items-center cursor-pointer text-gray-600 space-x-1 hover:text-gray-800 text-xs">
                <Icon icon="icon-shuaxin" class="text-xs" />
                <span>{{ t('actions.refresh') }}</span>
              </div>
            </template>
          </IconRefresh>
        </div>
      </div>

      <!-- Content Area -->
      <div class="flex-1 min-h-0">
        <Spin :spinning="loading" class="h-full">
          <template v-if="loaded">
            <!-- Empty state when no modules exist -->
            <div v-if="!searchedFlag && dataList.length === 0" class="flex flex-col items-center justify-center py-16">
              <div class="w-24 h-24 bg-gray-100 rounded-full flex items-center justify-center mb-4">
                <Icon icon="icon-mokuai" class="text-4xl text-gray-400" />
              </div>
              <h3 class="text-sm font-medium text-gray-900 mb-2">{{ t('module.messages.noModules') }}</h3>
              <p class="text-gray-500 text-xs mb-4 text-center max-w-md">
                {{ props.disabled ? t('module.messages.noModulesDescription') : t('module.messages.noModulesHint') }}
              </p>
              <Button
                v-if="!props.disabled"
                type="primary"
                size="large"
                class="flex items-center space-x-2"
                @click="openCreateModal()">
                <Icon icon="icon-jia" class="text-base" />
                <span>{{ t('module.actions.addModule') }}</span>
              </Button>
            </div>

            <!-- Modules tree -->
            <template v-else>
              <!-- No data state for search results -->
              <div v-if="dataList.length === 0" class="flex flex-col items-center justify-center py-16">
                <div class="w-16 h-16 bg-gray-100 rounded-full flex items-center justify-center mb-4">
                  <Icon icon="icon-sousuo" class="text-2xl text-gray-400" />
                </div>
                <h3 class="text-sm font-medium text-gray-900 mb-2">{{ t('module.messages.noSearchResults') }}</h3>
                <p class="text-gray-500 text-xs">{{ t('module.messages.tryDifferentKeywords') }}</p>
              </div>

              <!-- Module tree display -->
              <div
                v-else
                class="bg-gray-50 rounded-lg p-2 overflow-y-auto"
                :style="{ height: moduleTreeHeight }">
                <Tree
                  :key="dataList.length"
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
                    <div v-if="editId === id" class="flex items-center space-x-2 p-1.5 bg-white rounded border">
                      <Input
                        ref="nameInputRef"
                        :placeholder="t('module.messages.moduleNamePlaceholder')"
                        class="flex-1"
                        size="small"
                        trim
                        :value="name"
                        :allowClear="false"
                        :maxlength="50"
                        @blur="handleEditBlur(id, $event)"
                        @pressEnter="handleEditEnter(id, $event)" />
                      <Button
                        type="link"
                        size="small"
                        class="px-2 text-gray-500 hover:text-gray-700"
                        @click="cancelEdit">
                        {{ t('actions.cancel') }}
                      </Button>
                    </div>

                    <!-- Normal display mode -->
                    <div v-else class="flex items-center space-x-2 tree-title group hover:bg-white hover:shadow-sm rounded-lg p-1.5 transition-all duration-200">
                      <div class="w-6 h-6 bg-blue-100 rounded flex items-center justify-center flex-shrink-0">
                        <Icon icon="icon-mokuai" class="text-blue-600 text-sm" />
                      </div>
                      <div class="flex items-center space-x-2 flex-1 min-w-0">
                        <span class="truncate text-xs font-medium text-gray-900" :title="name">{{ name }}</span>

                        <!-- Action dropdown menu -->
                        <Dropdown :trigger="['click']">
                          <Button
                            v-if="!props.disabled && hasEditPermission"
                            class="hidden group-hover:inline-flex items-center justify-center w-6 h-6 rounded hover:bg-gray-100"
                            type="text"
                            size="small">
                            <Icon icon="icon-more" class="text-gray-400 text-sm" />
                          </Button>
                          <template #overlay>
                            <Menu
                              class="text-sm py-1"
                              @click="handleDropdownClick($event, {name, id, index, ids, level, childLevels, pid})">
                              <MenuItem
                                v-if="canAddSubModule({id, name, level})"
                                key="add"
                                class="flex items-center space-x-2 py-2">
                                <Icon icon="icon-jia" class="text-blue-600 mr-1" />
                                <span>{{ t('actions.addSub') }}</span>
                              </MenuItem>
                              <MenuItem
                                v-if="canMoveUp({id, name, index, pid})"
                                key="up"
                                class="flex items-center space-x-2 py-2">
                                <Icon icon="icon-shangyi" class="text-green-600 mr-1" />
                                <span>{{ index < 1 ? t('actions.moveUp') : t('actions.moveToUpperLevel') }}</span>
                              </MenuItem>
                              <MenuItem
                                v-if="canMoveDown({id, name, isLast})"
                                key="down"
                                class="flex items-center space-x-2 py-2">
                                <Icon icon="icon-xiayi" class="text-green-600 mr-1" />
                                <span>{{ t('actions.moveDown') }}</span>
                              </MenuItem>
                              <MenuItem key="move" class="flex items-center space-x-2 py-2">
                                <Icon icon="icon-yidong" class="text-orange-600 mr-1" />
                                <span>{{ t('actions.move') }}</span>
                              </MenuItem>
                              <MenuItem key="edit" class="flex items-center space-x-2 py-2">
                                <Icon icon="icon-bianji" class="text-blue-600 mr-1" />
                                <span>{{ t('actions.edit') }}</span>
                              </MenuItem>
                              <MenuItem key="del" class="flex items-center space-x-2 py-2 text-red-600">
                                <Icon icon="icon-qingchu" class="text-red-600 mr-1" />
                                <span>{{ t('actions.delete') }}</span>
                              </MenuItem>
                            </Menu>
                          </template>
                        </Dropdown>
                      </div>
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
/* Tree component styling */
:deep(.ant-tree) {
  background-color: transparent;
  font-size: 12px;
}

:deep(.ant-tree .ant-tree-treenode) {
  width: 100%;
  height: auto;
  padding: 1px 0;
  line-height: 1.3;
}

:deep(.ant-tree .ant-tree-treenode.ant-tree-treenode-selected) {
  background-color: transparent;
}

:deep(.ant-tree .ant-tree-treenode:hover) {
  background-color: transparent;
}

:deep(.ant-tree .ant-tree-switcher) {
  width: 14px;
  height: 14px;
  margin-top: 1px;
  line-height: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  top: 14px;
}

:deep(.ant-tree .ant-tree-switcher .ant-tree-switcher-icon) {
  font-size: 16px;
  color: #6b7280;
}

:deep(.ant-tree .ant-tree-node-content-wrapper:hover) {
  background-color: transparent;
}

:deep(.ant-tree .ant-tree-node-content-wrapper) {
  display: flex;
  flex: 1 1 0%;
  flex-direction: column;
  justify-content: center;
  height: auto;
  margin: 0;
  padding: 0;
  line-height: 1.3;
}

:deep(.ant-tree .ant-tree-node-content-wrapper .ant-tree-iconEle) {
  height: auto;
  line-height: 1.3;
  vertical-align: initial;
}

:deep(.ant-tree .ant-tree-node-selected) {
  background-color: transparent;
}

:deep(.ant-tree .ant-tree-indent-unit) {
  width: 20px;
}

:deep(.ant-tree-node-content-wrapper.ant-tree-node-content-wrapper-normal) {
  @apply !flex-1 min-w-0;
}

/* Custom hover effects */
.tree-title:hover {
  transform: translateY(-1px);
}

/* Menu styling */
:deep(.ant-dropdown-menu) {
  border-radius: 8px;
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
  border: 1px solid #e5e7eb;
}

:deep(.ant-dropdown-menu-item) {
  padding: 3px 6px;
  border-radius: 4px;
  margin: 1px 2px;
  transition: all 0.2s ease;
  font-size: 12px;
}

:deep(.ant-dropdown-menu-item:hover) {
  background-color: #f3f4f6;
}

/* Input styling */
:deep(.ant-input) {
  border-radius: 6px;
  border: 1px solid #d1d5db;
  transition: all 0.2s ease;
  font-size: 12px;
}

:deep(.ant-input:focus) {
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

/* Spin styling */
:deep(.ant-spin-container) {
  height: 100%;
  min-height: 0;
}
</style>
