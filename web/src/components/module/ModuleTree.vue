<script setup lang="ts">
import { computed, defineAsyncComponent, inject, Ref, ref, onMounted, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { appContext, duration, SearchCriteria } from '@xcan-angus/infra';
import { AsyncComponent, Icon, Input, modal, notification } from '@xcan-angus/vue-ui';
import { Button, Dropdown, Menu, MenuItem, Tree } from 'ant-design-vue';
import { debounce } from 'throttle-debounce';
import { modules } from '@/api/tester';
import { ProjectInfo } from '@/layout/types';
import { travelTreeData } from '@/utils/utils';

/**
 * Module item interface for tree display
 */
type ModuleItem = {
  id: string;
  name: string;
  showName?: string;
  showTitle?: string;
}

/**
 * Component props interface for module tree
 */
type Props = {
  projectId: string;
  projectName: string;
  userInfo: { id: string; };
  appInfo: { id: string; };
  notify: string;
  dataList: ModuleItem[];
  moduleId: string;
  readonly: boolean;
}

// COMPONENT PROPS
const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  notify: undefined,
  dataList: () => [],
  readonly: false
});


// Emits
const emits = defineEmits<{(e: 'loadData', value?: string); (e: 'update:moduleId', value?: string):void}>();

// Async components
const CreateModal = defineAsyncComponent(() => import('@/views/project/module/Add.vue'));
const MoveModuleModal = defineAsyncComponent(() => import('@/views/project/module/Move.vue'));

// Composables & injections
const { t } = useI18n();
const projectInfo = inject<Ref<ProjectInfo>>('projectInfo', ref({} as ProjectInfo));
const isAdmin = computed(() => appContext.isAdmin());
const currentUserInfo = ref(appContext.getUser());
const moduleTreeData = ref<ModuleItem[]>([{ name: t('common.noModule'), id: -1 }]);

// REACTIVE STATE
const nameInputRef = ref();
const isLoading = ref(false);
const searchKeywords = ref();
const currentEditId = ref<string>();
const parentModuleId = ref<string>();
const isCreateModalVisible = ref(false);
const isMoveModalVisible = ref(false);
const activeModuleData = ref();

/**
 * Handles module selection change
 * @param selectedKeys - Array of selected module keys
 */
const handleModuleSelectionChange = (selectedKeys: string[]) => {
  if (!selectedKeys.length) {
    return;
  }
  emits('update:moduleId', selectedKeys[0]);
};

/**
 * Handles module search with debounce
 */
const handleModuleSearch = debounce(duration.search, () => {
  loadModuleTreeData();
});

/**
 * Opens create module modal
 */
const handleModuleCreation = () => {
  isCreateModalVisible.value = true;
};

/**
 * Handles module creation completion
 */
const handleModuleCreationComplete = () => {
  loadModuleTreeData();
};

/**
 * Handles module edit by enabling inline editing
 * @param moduleData - Module data to edit
 */
const handleModuleEdit = (moduleData: ModuleItem) => {
  currentEditId.value = moduleData.id;
};

/**
 * Cancels module editing
 */
const handleEditCancel = () => {
  currentEditId.value = undefined;
};

/**
 * Handles module name update on enter key press
 * @param moduleId - ID of module being edited
 * @param event - Input event with new value
 */
const handleModuleNameUpdate = async (moduleId: string, event: { target: { value: string } }) => {
  const newName = event.target.value;
  if (!newName) {
    return;
  }

  isLoading.value = true;
  const [error] = await modules.updateModule([{ id: moduleId, name: newName }]);
  isLoading.value = false;
  if (error) {
    return;
  }
  notification.success(t('actions.tips.updateSuccess'));
  currentEditId.value = undefined;
  loadModuleTreeData();
};

/**
 * Handles module name update on blur event
 * @param moduleId - ID of module being edited
 * @param event - Input event with new value
 */
const handleModuleNameBlur = (moduleId: string, event: { target: { value: string } }) => {
  setTimeout(() => {
    if (currentEditId.value === moduleId) {
      handleModuleNameUpdate(moduleId, event);
    }
  }, 300);
};

/**
 * Handles module deletion with confirmation
 * @param moduleData - Module data to delete
 */
const handleModuleDeletion = (moduleData: ModuleItem) => {
  modal.confirm({
    content: t('issue.list.messages.confirmDeleteModule', { name: moduleData.name }),
    async onOk () {
      const moduleId = moduleData.id;
      const deleteParams = { ids: [moduleId] };
      isLoading.value = true;
      const [error] = await modules.deleteModule(deleteParams);
      isLoading.value = false;
      if (error) {
        return;
      }

      notification.success(t('actions.tips.deleteSuccess'));
      loadModuleTreeData();
    }
  });
};

/**
 * Handles moving module up in hierarchy
 * @param moduleRecord - Module record with position information
 */
const handleModuleMoveUp = async (moduleRecord: any) => {
  const { index, ids, pid, id } = moduleRecord;
  let moveParams = {};

  if (index === 0) {
    let targetParent;
    for (const linkId of ids) {
      if (linkId !== moduleRecord.id) {
        if (targetParent) {
          targetParent = (targetParent.children || []).find(item => item.id === linkId);
        } else {
          targetParent = props.dataList.find(item => item.id === linkId);
        }
      }
    }
    moveParams = {
      id,
      pid: targetParent.pid || -1,
      sequence: (+targetParent.sequence) - 1
    };
  } else {
    let parentChildren;
    if (ids.length === 1) {
      parentChildren = props.dataList;
    } else {
      for (const linkId of ids) {
        if (linkId !== moduleRecord.id) {
          if (parentChildren) {
            parentChildren = parentChildren.find(item => item.id === linkId)?.children || [];
          } else {
            parentChildren = props.dataList.find(item => item.id === linkId)?.children || [];
          }
        }
      }
    }
    moveParams = {
      id,
      sequence: parentChildren[index - 1].sequence - 1
    };
  }

  const [error] = await modules.updateModule([moveParams]);
  if (error) {
    return;
  }
  notification.success(t('actions.tips.moveSuccess'));
  loadModuleTreeData();
};

/**
 * Handles moving module down in hierarchy
 * @param moduleRecord - Module record with position information
 */
const handleModuleMoveDown = async (moduleRecord: any) => {
  const { index, ids, id } = moduleRecord;
  let parentChildren;

  if (ids.length === 1) {
    parentChildren = props.dataList;
  } else {
    for (const linkId of ids) {
      if (linkId !== moduleRecord.id) {
        if (parentChildren) {
          parentChildren = parentChildren.find(item => item.id === linkId)?.children || [];
        } else {
          parentChildren = props.dataList.find(item => item.id === linkId)?.children || [];
        }
      }
    }
  }

  const moveParams = {
    id,
    sequence: +parentChildren[index + 1].sequence + 1
  };

  const [error] = await modules.updateModule([moveParams]);
  if (error) {
    return;
  }
  notification.success(t('actions.tips.moveSuccess'));
  loadModuleTreeData();
};

/**
 * Handles moving module to different level
 * @param moduleRecord - Module record to move
 */
const handleModuleLevelMove = (moduleRecord: any) => {
  activeModuleData.value = moduleRecord;
  isMoveModalVisible.value = true;
};

/**
 * Handles context menu click events
 * @param menuItem - Clicked menu item
 * @param moduleRecord - Module record associated with menu
 */
const handleContextMenuClick = (menuItem: any, moduleRecord: any) => {
  if (menuItem.key === 'edit') {
    handleModuleEdit(moduleRecord);
  } else if (menuItem.key === 'add') {
    parentModuleId.value = moduleRecord.id;
    handleModuleCreation();
  } else if (menuItem.key === 'del') {
    handleModuleDeletion(moduleRecord);
  } else if (menuItem.key === 'up') {
    handleModuleMoveUp(moduleRecord);
  } else if (menuItem.key === 'down') {
    handleModuleMoveDown(moduleRecord);
  } else if (menuItem.key === 'move') {
    handleModuleLevelMove(moduleRecord);
  }
};

/**
 * Load module tree data
 */
 const loadModuleTreeData = async () => {
  const [error, { data }] = await modules.getModuleTree({
    projectId: props.projectId,
    filters: searchKeywords.value
      ? [{ value: searchKeywords.value, op: SearchCriteria.OpEnum.Match, key: 'name' }]
      : []
  });
  if (error) {
    return;
  }
  moduleTreeData.value = [{ name: t('common.noModule'), id: -1 }, ...travelTreeData(data || [])];
  if (props.moduleId && searchKeywords.value && !moduleTreeData.value.find(item => item.id === props.moduleId)) {
    emits('update:moduleId', undefined);
  }
};

/**
 * Initialize component data on mount
 */
onMounted(() => {
  loadModuleTreeData();

  watch(() => props.projectId, () => {
    loadModuleTreeData();
  });
});

// Expose methods for parent component
defineExpose({
  loadDataList: loadModuleTreeData
});
</script>
<template>
  <div class="h-full flex flex-col">
    <div v-if="!readonly" class="flex justify-between h-11 space-x-4 p-2">
      <Input
        v-model:value="searchKeywords"
        :placeholder="t('common.placeholders.searchKeyword')"
        @change="handleModuleSearch" />
      <Button
        :disabled="!isAdmin && projectInfo?.createdBy !== currentUserInfo?.id && projectInfo.ownerId !== currentUserInfo?.id"
        type="primary"
        size="small"
        @click="handleContextMenuClick({key: 'add'}, {id: undefined})">
        <Icon icon="icon-jia" />
        {{ t('actions.add') }}
      </Button>
    </div>

    <div
      :class="{'active': props.moduleId === undefined}"
      class="flex items-center space-x-2 tree-title h-9 leading-9 pl-4.5 cursor-pointer all-case"
      @click="handleModuleSelectionChange([])">
      <Icon icon="icon-liebiaoshitu" class="text-3.5" />
      <span class="flex-1">{{ t('common.all') }}</span>
    </div>

    <Tree
      :treeData="moduleTreeData"
      :selectedKeys="[props.moduleId]"
      class="flex-1 overflow-auto"
      blockNode
      defaultExpandAll
      :fieldNames="{
        children: 'children',
        title: 'name',
        key: 'id'
      }"
      @select="handleModuleSelectionChange">
      <template #title="{key, title, name, id, index, level, isLast, pid, ids, sequence, childLevels, hasEditPermission}">
        <div v-if="currentEditId === id" class="flex items-center">
          <Input
            ref="nameInputRef"
            :placeholder="t('common.placeholders.inputName')"
            class="flex-1 mr-2 bg-white"
            trim
            :value="name"
            :allowClear="false"
            :maxlength="50"
            @blur="handleModuleNameBlur(id, $event)"
            @pressEnter="handleModuleNameUpdate(id, $event)" />
          <Button
            type="link"
            size="small"
            class="px-0 py-0 mr-1"
            @click="handleEditCancel">
            {{ t('actions.cancel') }}
          </Button>
        </div>
        <div v-else class="flex items-center space-x-2 tree-title">
          <Icon v-if="id !== '-1'" icon="icon-mokuai" />
          <Icon
            v-else
            icon="icon-liebiaoshitu"
            class="text-3.5" />
          <span class="flex-1 min-w-0 truncate" :title="name">{{ name }}</span>
          <Dropdown v-if="!readonly" :trigger="['click']">
            <Button
              v-if="id !== '-1' && hasEditPermission"
              type="text"
              size="small"
              class="hidden gengduo"
              @click.stop>
              <Icon icon="icon-more" class="text-3.5" />
            </Button>
            <template #overlay>
              <Menu class="w-50" @click="handleContextMenuClick($event, {name, id, index, ids, pid, childLevels})">
                <MenuItem v-if="level < 4" key="add">
                  <Icon icon="icon-jia" />
                  {{ t('actions.addSub') }}
                </MenuItem>
                <MenuItem v-if="index > 0 || +pid > 0" key="up">
                  <Icon icon="icon-shangyi" />
                  {{ index < 1 ? t('actions.moveToUpperLevel') : t('actions.moveUp') }}
                </MenuItem>
                <MenuItem v-if="!isLast" key="down">
                  <Icon icon="icon-xiayi" />
                  {{ t('actions.moveDown') }}
                </MenuItem>
                <MenuItem key="move">
                  <Icon icon="icon-yidong" />
                  {{ t('actions.move') }}
                </MenuItem>
                <MenuItem key="edit">
                  <Icon icon="icon-bianji" />
                  {{ t('actions.edit') }}
                </MenuItem>
                <MenuItem key="del">
                  <Icon icon="icon-qingchu" />
                  {{ t('actions.delete') }}
                </MenuItem>
              </Menu>
            </template>
          </Dropdown>
        </div>
      </template>
    </Tree>
  </div>

  <AsyncComponent :visible="isCreateModalVisible">
    <CreateModal
      v-model:visible="isCreateModalVisible"
      :projectId="props.projectId"
      :pid="parentModuleId"
      @ok="handleModuleCreationComplete" />
  </AsyncComponent>

  <AsyncComponent :visible="isMoveModalVisible">
    <MoveModuleModal
      v-model:visible="isMoveModalVisible"
      :projectId="props.projectId"
      :projectName="props.projectName"
      :module="activeModuleData"
      @ok="handleModuleCreationComplete" />
  </AsyncComponent>
</template>
<style scoped>
.tree-title:hover .gengduo {
  display: inline-block;
}

:deep(.ant-tree .ant-tree-indent-unit) {
  width: 18px;
}

.all-case:hover, .all-case.active {
  background-color: var(--content-tabs-bg-selected);
}

:deep(.ant-tree-node-content-wrapper.ant-tree-node-content-wrapper-normal) {
  @apply !flex-1 min-w-0;
}

:deep(.ant-tree) {
  background-color: transparent;
  font-size: 12px;
}

:deep(.ant-tree .ant-tree-treenode) {
  width: 100%;
  height: 36px;
  padding-left: 0;
  line-height: 20px;
}

:deep(.ant-tree .ant-tree-treenode.ant-tree-treenode-selected) {
  background-color: var(--content-tabs-bg-selected);
}

:deep(.ant-tree .ant-tree-treenode:hover) {
  background-color: var(--content-tabs-bg-selected);
}

:deep(.ant-tree .ant-tree-switcher) {
  width: 16px;
  height: 34px;
  margin-top: 2px;
  line-height: 34px;
}

:deep(.ant-tree .ant-tree-node-content-wrapper:hover) {
  background-color: transparent;
}

:deep(.ant-tree .ant-tree-node-content-wrapper) {
  display: flex;
  flex: 1 1 0%;
  flex-direction: column;
  justify-content: center;
  height: 36px;
  margin: 0;
  padding: 0;
  line-height: 36px;
}

:deep(.ant-tree .ant-tree-node-content-wrapper .ant-tree-iconEle) {
  height: 36px;
  line-height: 36px;
  vertical-align: initial;
}

:deep(.ant-tree .ant-tree-node-selected) {
  background-color: transparent;
}

:deep(.ant-tree .ant-tree-indent-unit) {
  width: 18px;
}

</style>
