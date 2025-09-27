<script setup lang="ts">
import { defineAsyncComponent, onMounted, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { AsyncComponent, Icon, Input, modal, notification } from '@xcan-angus/vue-ui';
import { Button, Tree } from 'ant-design-vue';
import { modules } from '@/api/tester';

const { t } = useI18n();

// Type Definitions
type ModuleItem = {
  id: string;
  name: string;
  showName?: string;
  showTitle?: string;
}

// Props Definition
type Props = {
  projectId: string;
  userInfo: { id: string; };
  appInfo: { id: string; };
  notify: string;
  moduleId: string;
  projectName: string;
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  notify: undefined,
  moduleId: '',
  disabled: false
});

const emits = defineEmits<{(e: 'loadData', value?: string); (e: 'update:moduleId', value: string):void}>();

// Async Components
const CreateModal = defineAsyncComponent(() => import('@/views/project/module/Add.vue'));
const MoveModuleModal = defineAsyncComponent(() => import('@/views/project/module/Move.vue'));

// Reactive Data
const nameInputRef = ref();
const isLoading = ref(false);
const searchKeywords = ref();
const editingModuleId = ref<string>();
const parentModuleId = ref<string>();
const isCreateModalVisible = ref(false);
const isMoveModalVisible = ref(false);
const selectedModuleForMove = ref();
const moduleTreeData = ref([{ name: t('functionBaseline.case.moduleTree.noModuleCases'), id: '-1' }]);

/**
 * Handle module selection change
 * @param selectKeys - Array of selected keys
 */
const handleModuleSelectionChange = (selectKeys) => {
  if (!selectKeys.length) {
    return;
  }
  emits('update:moduleId', selectKeys[0]);
};

/**
 * Open create module modal
 */
const openCreateModal = () => {
  isCreateModalVisible.value = true;
};

/**
 * Handle create modal confirmation
 */
const handleCreateModalConfirm = () => {
  emits('loadData', searchKeywords.value);
};

/**
 * Start editing module name
 * @param data - Module item to edit
 */
const startEditingModule = (data: ModuleItem) => {
  if (props.disabled) {
    return;
  }
  editingModuleId.value = data.id;
};

/**
 * Cancel module editing
 */
const cancelModuleEditing = () => {
  editingModuleId.value = undefined;
};

/**
 * Handle enter key press for module name editing
 * @param id - Module ID
 * @param event - Input event
 */
const handleModuleNameEditConfirm = async (id: string, event: { target: { value: string } }) => {
  const value = event.target.value;
  if (!value) {
    return;
  }

  isLoading.value = true;
  const [error] = await modules.updateModule([{ id, name: value }]);
  isLoading.value = false;
  if (error) {
    return;
  }
  notification.success(t('actions.tips.modifySuccess'));
  editingModuleId.value = undefined;
  emits('loadData', searchKeywords.value);
};

/**
 * Handle input blur for module name editing
 * @param id - Module ID
 * @param event - Input event
 */
const handleModuleNameEditBlur = (id: string, event: { target: { value: string } }) => {
  setTimeout(() => {
    if (editingModuleId.value === id) {
      handleModuleNameEditConfirm(id, event);
    }
  }, 300);
};

/**
 * Delete module with confirmation
 * @param data - Module item to delete
 */
const deleteModule = (data: ModuleItem) => {
  modal.confirm({
    content: t('functionBaseline.case.moduleTree.confirmDeleteModule', { name: data.name }),
    async onOk () {
      const id = data.id;
      const params = { ids: [id] };
      isLoading.value = true;
      const [error] = await modules.deleteModule(params);
      isLoading.value = false;
      if (error) {
        return;
      }

      notification.success(t('functionBaseline.case.moduleTree.deleteModuleSuccess'));
      emits('loadData', searchKeywords.value);
    }
  });
};

/**
 * Move module up in the tree
 * @param record - Module record to move
 */
const moveModuleUp = async (record) => {
  const { index, ids, pid, id } = record;
  let params = {};
  if (index === 0) {
    let targetParent;
    for (const linkId of ids) {
      if (linkId !== record.id) {
        if (targetParent) {
          targetParent = (targetParent.children || []).find(item => item.id === linkId);
        } else {
          targetParent = moduleTreeData.value.find(item => item.id === linkId);
        }
      }
    }
    params = { id, pid: targetParent.pid || '-1', sequence: (+targetParent.sequence) - 1 };
  } else {
    let parentChildren;
    if (ids.length === 1) {
      parentChildren = moduleTreeData.value;
    } else {
      for (const linkId of ids) {
        if (linkId !== record.id) {
          if (parentChildren) {
            parentChildren = parentChildren.find(item => item.id === linkId)?.children || [];
          } else {
            parentChildren = moduleTreeData.value.find(item => item.id === linkId)?.children || [];
          }
        }
      }
    }
    params = { id, sequence: parentChildren[index - 1].sequence - 1 };
  }

  const [error] = await modules.updateModule([params]);
  if (error) {
    return;
  }
  notification.success(t('actions.tips.moveSuccess'));
  emits('loadData', searchKeywords.value);
};

/**
 * Move module down in the tree
 * @param record - Module record to move
 */
const moveModuleDown = async (record) => {
  const { index, ids, id } = record;
  let parentChildren;
  if (ids.length === 1) {
    parentChildren = moduleTreeData.value;
  } else {
    for (const linkId of ids) {
      if (linkId !== record.id) {
        if (parentChildren) {
          parentChildren = parentChildren.find(item => item.id === linkId)?.children || [];
        } else {
          parentChildren = moduleTreeData.value.find(item => item.id === linkId)?.children || [];
        }
      }
    }
  }
  const params = { id, sequence: +parentChildren[index + 1].sequence + 1 };
  const [error] = await modules.updateModule([params]);
  if (error) {
    return;
  }
  notification.success(t('actions.tips.moveSuccess'));
  emits('loadData', searchKeywords.value);
};

/**
 * Open move module modal
 * @param record - Module record to move
 */
const openMoveModuleModal = (record) => {
  selectedModuleForMove.value = record;
  isMoveModalVisible.value = true;
};

/**
 * Process tree data with additional properties
 * @param treeData - Raw tree data
 * @param callback - Optional callback function for each item
 * @returns Processed tree data
 */
const processTreeData = (treeData, callback = (item) => item) => {
  function traverseTree (treeData, level = 0, ids: string[] = []) {
    treeData.forEach((item, idx) => {
      item.level = level;
      item.index = idx;
      item.ids = [...ids, item.id];
      item.isLast = idx === (treeData.length - 1);
      traverseTree(item.children || [], level + 1, item.ids),
      item.childLevels = (item.children?.length ? Math.max(...item.children.map(i => i.childLevels)) : 0) + 1;
      item = callback(item);
    });
  }
  traverseTree(treeData);
  return treeData;
};

/**
 * Load module tree data
 */
const loadModuleTreeData = async () => {
  const [error, { data }] = await modules.getModuleTree({
    projectId: props.projectId
  });
  if (error) {
    return;
  }
  moduleTreeData.value = [{ name: t('functionBaseline.case.moduleTree.noModuleCases'), id: '-1' }, ...processTreeData(data || [])];
};

/**
 * Initialize component data on mount
 */
onMounted(() => {
  loadModuleTreeData();
});

// Expose methods for parent component
defineExpose({
  loadDataList: loadModuleTreeData
});
</script>
<template>
  <div class="h-full flex flex-col">
    <!-- All Cases -->
    <div
      :class="{'active': props.moduleId === ''}"
      class="flex items-center space-x-2 tree-title h-9 leading-9 pl-4.5 cursor-pointer all-case"
      @click="handleModuleSelectionChange([''])">
      <Icon icon="icon-liebiaoshitu" class="text-3.5" />
      <span class="flex-1">{{ t('functionBaseline.case.moduleTree.allCases') }}</span>
    </div>

    <!-- Module Tree -->
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
        <div v-if="editingModuleId === id" class="flex items-center">
          <Input
            ref="nameInputRef"
            :placeholder="t('functionBaseline.case.moduleTree.enterModuleName')"
            class="flex-1 mr-2 bg-white"
            trim
            :value="name"
            :allowClear="false"
            :maxlength="50"
            @blur="handleModuleNameEditBlur(id, $event)"
            @pressEnter="handleModuleNameEditConfirm(id, $event)" />
          <Button
            type="link"
            size="small"
            class="px-0 py-0 mr-1"
            @click="cancelModuleEditing">
            {{ t('actions.cancel') }}
          </Button>
        </div>
        <div v-else class="flex items-center space-x-2 tree-title">
          <Icon v-if="id !== '-1'" icon="icon-mokuai" />
          <Icon
            v-else
            icon="icon-liebiaoshitu"
            class="text-3.5" />
          <span class="flex-1">{{ name }}</span>
        </div>
      </template>
    </Tree>
  </div>

  <!-- Create Modal -->
  <AsyncComponent :visible="isCreateModalVisible">
    <CreateModal
      v-model:visible="isCreateModalVisible"
      :projectId="props.projectId"
      :pid="parentModuleId"
      @ok="handleCreateModalConfirm" />
  </AsyncComponent>

  <!-- Move Modal -->
  <AsyncComponent :visible="isMoveModalVisible">
    <MoveModuleModal
      v-model:visible="isMoveModalVisible"
      :projectId="props.projectId"
      :projectName="props.projectName"
      :module="selectedModuleForMove"
      @ok="handleCreateModalConfirm" />
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
