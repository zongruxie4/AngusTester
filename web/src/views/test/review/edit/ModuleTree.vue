<script setup lang="ts">
import { defineAsyncComponent, onMounted, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { AsyncComponent, Icon, Input, notification } from '@xcan-angus/vue-ui';
import { Button, Tree } from 'ant-design-vue';
import { modules } from '@/api/tester';

// Component imports
const CreateModal = defineAsyncComponent(() => import('@/views/project/module/Add.vue'));
const MoveModuleModal = defineAsyncComponent(() => import('@/views/project/module/Move.vue'));

// Type Definitions
type Props = {
  projectId: string;
  userInfo: { id: string; };
  appInfo: { id: string; };
  notify: string;
  moduleId: string;
  projectName: string;
}

// Props and Context
const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  notify: undefined,
  moduleId: '',
  disabled: false
});

const { t } = useI18n();

// Event Emitters
const emits = defineEmits<{(e: 'loadData', value?: string); (e: 'update:moduleId', value: string): void }>();

// Reactive State
const nameInputRef = ref();
const loading = ref(false);
const searchKeywords = ref<string>();
const editingModuleId = ref<string>();
const parentModuleId = ref<string>();
const createModalVisible = ref(false);
const moveModalVisible = ref(false);

/**
 * Handles module selection change
 * <p>
 * Updates selected module ID and emits change event
 */
const handleModuleSelectionChange = (selectedKeys: string[]) => {
  if (!selectedKeys.length) {
    return;
  }
  emits('update:moduleId', selectedKeys[0]);
};

/**
 * Handles successful module creation
 * <p>
 * Emits data reload event with current search keywords
 */
const handleModuleCreationSuccess = () => {
  emits('loadData', searchKeywords.value);
};

/**
 * Cancels module editing mode
 * <p>
 * Clears editing state and exits edit mode
 */
const cancelModuleEditing = () => {
  editingModuleId.value = undefined;
};

/**
 * Handles module name update on enter key press
 * <p>
 * Validates input and submits module name update
 * <p>
 * Shows success notification and refreshes data
 */
const handleModuleNameUpdate = async (moduleId: string, event: { target: { value: string } }) => {
  const newName = event.target.value;
  if (!newName) {
    return;
  }

  loading.value = true;
  const [error] = await modules.updateModule([{ id: moduleId, name: newName }]);
  loading.value = false;
  if (error) {
    return;
  }
  notification.success(t('actions.tips.modifySuccess'));
  editingModuleId.value = undefined;
  emits('loadData', searchKeywords.value);
};

/**
 * Handles module name update on input blur
 * <p>
 * Delays execution to allow for potential cancel operation
 * <p>
 * Calls name update if still in editing mode
 */
const handleModuleNameBlur = (moduleId: string, event: { target: { value: string } }) => {
  setTimeout(() => {
    if (editingModuleId.value === moduleId) {
      handleModuleNameUpdate(moduleId, event);
    }
  }, 300);
};

// Tree Data Processing
const activeModule = ref();

/**
 * Processes tree data with additional metadata
 * <p>
 * Adds level, index, path, and child level information to tree nodes
 * <p>
 * Applies callback function to each processed node
 */
const processTreeData = (treeData: any[], callback = (item: any) => item) => {
  function traverseTree (treeData: any[], level = 0, pathIds: string[] = []) {
    treeData.forEach((item, index) => {
      item.level = level;
      item.index = index;
      item.ids = [...pathIds, item.id];
      item.isLast = index === (treeData.length - 1);
      traverseTree(item.children || [], level + 1, item.ids);
      item.childLevels = (item.children?.length ? Math.max(...item.children.map(child => child.childLevels)) : 0) + 1;
      item = callback(item);
    });
  }

  traverseTree(treeData);
  return treeData;
};

// Data Loading
const moduleTreeData = ref([{ name: t('testCaseReview.editForm.noModuleCases'), id: '-1' }]);

/**
 * Loads module tree data from server
 * <p>
 * Fetches module hierarchy and processes tree structure
 * <p>
 * Updates tree data with processed module information
 */
const loadModuleTreeData = async () => {
  const [error, { data }] = await modules.getModuleTree({
    projectId: props.projectId
  });
  if (error) {
    return;
  }
  moduleTreeData.value = [
    { name: t('testCaseReview.editForm.noModuleCases'), id: '-1' },
    ...processTreeData(data || [])
  ];
};

/**
 * Component initialization
 * <p>
 * Loads module tree data on component mount
 */
onMounted(() => {
  loadModuleTreeData();
});

// Exposed Methods
defineExpose({
  loadDataList: loadModuleTreeData
});
</script>
<template>
  <div class="h-full flex flex-col">
    <div
      :class="{'active': props.moduleId === ''}"
      class="flex items-center space-x-2 tree-title h-9 leading-9 pl-4.5 cursor-pointer all-case"
      @click="handleModuleSelectionChange([''])">
      <Icon icon="icon-liebiaoshitu" class="text-3.5" />
      <span class="flex-1">{{ t('testCaseReview.editForm.allCases') }}</span>
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
      <template
        #title="{key, title, name, id, index, level, isLast, pid, ids, sequence, childLevels, hasEditPermission}">
        <div v-if="editingModuleId === id" class="flex items-center">
          <Input
            ref="nameInputRef"
            :placeholder="t('testCaseReview.editForm.enterModuleName')"
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

  <AsyncComponent :visible="createModalVisible">
    <CreateModal
      v-model:visible="createModalVisible"
      :projectId="props.projectId"
      :pid="parentModuleId"
      @ok="handleModuleCreationSuccess" />
  </AsyncComponent>

  <AsyncComponent :visible="moveModalVisible">
    <MoveModuleModal
      v-model:visible="moveModalVisible"
      :projectId="props.projectId"
      :projectName="props.projectName"
      :module="activeModule"
      @ok="handleModuleCreationSuccess" />
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
