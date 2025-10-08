<script setup lang="ts">
import { computed, defineAsyncComponent, inject, Ref, ref } from 'vue';
import { duration, appContext } from '@xcan-angus/infra';
import { AsyncComponent, Icon, Input, modal, notification } from '@xcan-angus/vue-ui';
import { Button, Dropdown, Menu, MenuItem, Tree } from 'ant-design-vue';
import { debounce } from 'throttle-debounce';
import { useI18n } from 'vue-i18n';
import { modules } from '@/api/tester';
import { ProjectInfo } from '@/layout/types';

// Async component imports
const CreateModal = defineAsyncComponent(() => import('@/views/project/module/Add.vue'));
const MoveModal = defineAsyncComponent(() => import('@/views/project/module/Move.vue'));

// Type definitions
type TagItem = {
  id: number;
  name: string;
  showName?: string;
  showTitle?: string;
}

// Component props interface
type Props = {
  projectId: number;
  userInfo: { id: number; };
  appInfo: { id: number; };
  notify: string;
  dataList: TagItem[];
  moduleId: number;
  projectName: string;
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  notify: undefined,
  disabled: false,
  dataList: () => []
});

// Component emits
const emits = defineEmits<{(e: 'loadData', value?: string); (e: 'update:moduleId', value: string):void}>();

const { t } = useI18n();

// Basic state management
const projectInfo = inject<Ref<ProjectInfo>>('projectInfo', ref({} as ProjectInfo));
const isAdmin = computed(() => appContext.isAdmin());
const userInfo = ref(appContext.getUser());

// UI state management
const nameInputRef = ref();
const loading = ref(false);
const keywords = ref<string>();
const editId = ref<number>();
const pid = ref<number>();
const modalVisible = ref(false);
const moveVisible = ref(false);

// Event handlers

/**
 * Handle module selection change
 * @param selectKeys - Selected module keys
 */
const handleSelectKeysChange = (selectKeys: string[]) => {
  if (!selectKeys.length) {
    return;
  }
  emits('update:moduleId', selectKeys[0]);
};

/**
 * Handle module search with debounce
 */
const handleSearchModule = debounce(duration.search, () => {
  emits('loadData', keywords.value);
});

/**
 * Open create module modal
 */
const toCreate = () => {
  modalVisible.value = true;
};

/**
 * Handle create module success
 */
const createOk = () => {
  emits('loadData', keywords.value);
};

/**
 * Start editing module name
 * @param data - Module data
 */
const toEdit = (data: TagItem) => {
  if (props.disabled) {
    return;
  }
  editId.value = data.id;
};

/**
 * Cancel editing module name
 */
const cancelEdit = () => {
  editId.value = undefined;
};

/**
 * Handle module name edit completion
 * @param id - Module ID
 * @param event - Input event
 */
const pressEnter = async (id: number, event: { target: { value: string } }) => {
  const value = event.target.value;
  if (!value) {
    return;
  }

  loading.value = true;
  const [error] = await modules.updateModule([{ id, name: value }]);
  loading.value = false;
  if (error) {
    return;
  }
  notification.success(t('actions.tips.editSuccess'));
  editId.value = undefined;
  emits('loadData', keywords.value);
};

/**
 * Handle input blur event with delay
 * @param id - Module ID
 * @param event - Input event
 */
const handleBlur = (id: number, event: { target: { value: string } }) => {
  setTimeout(() => {
    if (editId.value === id) {
      pressEnter(id, event);
    }
  }, 300);
};

/**
 * Delete module with confirmation
 * @param data - Module data
 */
const toDelete = (data: TagItem) => {
  modal.confirm({
    content: t('testCase.moduleTree.confirmDeleteModule', { name: data.name }),
    async onOk () {
      const id = data.id;
      const params = { ids: [id] };
      loading.value = true;
      const [error] = await modules.deleteModule(params);
      loading.value = false;
      if (error) {
        return;
      }

      notification.success(t('testCase.moduleTree.deleteModuleSuccess'));
      emits('loadData', keywords.value);
    }
  });
};

/**
 * Move module up in hierarchy
 * @param record - Module record with position info
 */
const moveUp = async (record: any) => {
  const { index, ids, pid, id } = record;
  let params = {};
  if (index === 0) {
    let targetParent;
    for (const linkId of ids) {
      if (linkId !== record.id) {
        if (targetParent) {
          targetParent = (targetParent.children || []).find(item => item.id === linkId);
        } else {
          targetParent = props.dataList.find(item => item.id === linkId);
        }
      }
    }
    params = { id, pid: targetParent.pid || '-1', sequence: (+targetParent.sequence) - 1 };
  } else {
    let parentChildren;
    if (ids.length === 1) {
      parentChildren = props.dataList;
    } else {
      for (const linkId of ids) {
        if (linkId !== record.id) {
          if (parentChildren) {
            parentChildren = parentChildren.find(item => item.id === linkId)?.children || [];
          } else {
            parentChildren = props.dataList.find(item => item.id === linkId)?.children || [];
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
  emits('loadData', keywords.value);
};

/**
 * Move module down in hierarchy
 * @param record - Module record with position info
 */
const moveDown = async (record: any) => {
  const { index, ids, id } = record;
  let parentChildren;
  if (ids.length === 1) {
    parentChildren = props.dataList;
  } else {
    for (const linkId of ids) {
      if (linkId !== record.id) {
        if (parentChildren) {
          parentChildren = parentChildren.find(item => item.id === linkId)?.children || [];
        } else {
          parentChildren = props.dataList.find(item => item.id === linkId)?.children || [];
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
  emits('loadData', keywords.value);
};

// Context menu management
const activeModule = ref<any>();

/**
 * Open move level modal
 * @param record - Module record
 */
const moveLevel = (record: any) => {
  activeModule.value = record;
  moveVisible.value = true;
};

/**
 * Handle context menu click
 * @param menu - Menu item
 * @param record - Module record
 */
const onMenuClick = (menu: any, record: any) => {
  if (menu.key === 'edit') {
    toEdit(record);
  } else if (menu.key === 'add') {
    pid.value = record.id;
    toCreate();
  } else if (menu.key === 'del') {
    toDelete(record);
  } else if (menu.key === 'up') {
    moveUp(record);
  } else if (menu.key === 'down') {
    moveDown(record);
  } else if (menu.key === 'move') {
    moveLevel(record);
  }
};
</script>
<template>
  <div class="h-full flex flex-col">
    <div class="flex justify-between h-11 space-x-4 p-2">
      <Input
        v-model:value="keywords"
        :placeholder="t('testCase.moduleTree.searchModule')"
        @change="handleSearchModule" />
      <Button
        :disabled="!isAdmin && projectInfo?.createdBy !== userInfo?.id && projectInfo.ownerId !== userInfo?.id"
        type="primary"
        size="small"
        @click="onMenuClick({key: 'add'}, {id: undefined})">
        <Icon icon="icon-jia" />
        {{ t('testCase.moduleTree.addModule') }}
      </Button>
    </div>

    <div
      :class="{'active': props.moduleId === -1}"
      class="flex items-center space-x-2 tree-title h-9 leading-9 pl-4.5 cursor-pointer all-case"
      @click="handleSelectKeysChange(['-1'])">
      <Icon icon="icon-liebiaoshitu" class="text-3.5" />
      <span class="flex-1">{{ t('testCase.moduleTree.allCases') }}</span>
    </div>

    <Tree
      :treeData="props.dataList"
      :selectedKeys="[props.moduleId]"
      class="flex-1 overflow-auto"
      blockNode
      defaultExpandAll
      :fieldNames="{
        children: 'children',
        title: 'name',
        key: 'id'
      }"
      @select="handleSelectKeysChange">
      <template #title="{key, title, name, id, index, level, isLast, pid, ids, sequence, childLevels, hasEditPermission}">
        <div v-if="editId === id" class="flex items-center">
          <Input
            ref="nameInputRef"
            :placeholder="t('testCase.moduleTree.enterModuleName')"
            class="flex-1 mr-2 bg-white"
            trim
            :value="name"
            :allowClear="false"
            :maxlength="50"
            @blur="handleBlur(id, $event)"
            @pressEnter="pressEnter(id, $event)" />
          <Button
            type="link"
            size="small"
            class="px-0 py-0 mr-1"
            @click="cancelEdit">
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
          <Dropdown :trigger="['click']">
            <Button
              v-if="id !== '-1' && hasEditPermission"
              type="text"
              size="small"
              class="hidden gengduo"
              @click.stop>
              <Icon icon="icon-more" class="text-3.5" />
            </Button>
            <template #overlay>
              <Menu class="w-50" @click="onMenuClick($event, {name, id, index, ids, pid, childLevels})">
                <MenuItem v-if="level < 4" key="add">
                  <Icon icon="icon-jia" />
                  {{ t('testCase.moduleTree.createSubModule') }}
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

  <AsyncComponent :visible="modalVisible">
    <CreateModal
      v-model:visible="modalVisible"
      :projectId="props.projectId"
      :pid="pid"
      @ok="createOk" />
  </AsyncComponent>

  <AsyncComponent :visible="moveVisible">
    <MoveModal
      v-model:visible="moveVisible"
      :projectId="props.projectId"
      :projectName="props.projectName"
      :module="activeModule"
      @ok="createOk" />
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

</style>
