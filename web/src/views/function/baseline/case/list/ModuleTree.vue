<script setup lang="ts">
import { defineAsyncComponent, onMounted, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { AsyncComponent, Icon, Input, modal, notification } from '@xcan-angus/vue-ui';
import { Button, Tree } from 'ant-design-vue';
import { modules } from '@/api/tester';

const { t } = useI18n();

type TagItem = {
  id: string;
  name: string;
  showName?: string;
  showTitle?: string;
}

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

const CreateModal = defineAsyncComponent(() => import('@/views/project/module/Add.vue'));
const MoveModuleModal = defineAsyncComponent(() => import('@/views/project/module/Move.vue'));

// const projectInfo = inject('projectInfo', ref({}));
// const isAdmin = inject('isAdmin', ref(false));
// const tenantInfo = inject('tenantInfo', ref({}));

const nameInputRef = ref();

const loading = ref(false);
const handleSelectKeysChange = (selectKeys) => {
  if (!selectKeys.length) {
    return;
  }
  emits('update:moduleId', selectKeys[0]);
};

// const handleSearchModule = debounce(duration.search, () => {
//   emits('loadData', keywords.value);
// });

const keywords = ref();
const editId = ref<string>();
const pid = ref<string>();
const modalVisible = ref(false);
const moveVsible = ref(false);

const toCreate = () => {
  modalVisible.value = true;
};

const createOk = () => {
  emits('loadData', keywords.value);
};

const toEdit = (data: TagItem) => {
  if (props.disabled) {
    return;
  }
  editId.value = data.id;
};

const cancelEdit = () => {
  editId.value = undefined;
};

const pressEnter = async (id: string, event: { target: { value: string } }) => {
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
  notification.success(t('functionBaseline.case.moduleTree.modifySuccess'));
  editId.value = undefined;
  emits('loadData', keywords.value);
};

const hadleblur = (id: string, event: { target: { value: string } }) => {
  setTimeout(() => {
    if (editId.value === id) {
      pressEnter(id, event);
    }
  }, 300);
};

// 删除弹框
const toDelete = (data: TagItem) => {
  modal.confirm({
    content: t('functionBaseline.case.moduleTree.confirmDeleteModule', { name: data.name }),
    async onOk () {
      const id = data.id;
      const params = { ids: [id] };
      loading.value = true;
      const [error] = await modules.deleteModule(params);
      loading.value = false;
      if (error) {
        return;
      }

      notification.success(t('functionBaseline.case.moduleTree.deleteModuleSuccess'));
      emits('loadData', keywords.value);
    }
  });
};

const moveUp = async (record) => {
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
    params = {
      id,
      pid: targetParent.pid || '-1',
      sequence: (+targetParent.sequence) - 1
    };
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
    params = {
      id,
      sequence: parentChildren[index - 1].sequence - 1
    };
  }

  const [error] = await modules.updateModule([params]);
  if (error) {
    return;
  }
  notification.success(t('functionBaseline.case.moduleTree.moveSuccess'));
  emits('loadData', keywords.value);
};

const moveDown = async (record) => {
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
  const params = {
    id,
    sequence: +parentChildren[index + 1].sequence + 1
  };
  const [error] = await modules.updateModule([params]);
  if (error) {
    return;
  }
  notification.success(t('functionBaseline.case.moduleTree.moveSuccess'));
  emits('loadData', keywords.value);
};

const activeModule = ref();
const moveLevel = (record) => {
  activeModule.value = record;
  moveVsible.value = true;
};

const onMenuClick = (menu, record) => {
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

const travelTreeData = (treeData, callback = (item) => item) => {
  function travel (treeData, level = 0, ids: string[] = []) {
    treeData.forEach((item, idx) => {
      item.level = level;
      item.index = idx;
      item.ids = [...ids, item.id];
      item.isLast = idx === (treeData.length - 1);
      travel(item.children || [], level + 1, item.ids),
      item.childLevels = (item.children?.length ? Math.max(...item.children.map(i => i.childLevels)) : 0) + 1;
      item = callback(item);
    });
  }
  travel(treeData);
  return treeData;
};

const moduleTreeData = ref([{ name: t('functionBaseline.case.moduleTree.noModuleCases'), id: '-1' }]);

const loadDataList = async () => {
  const [error, { data }] = await modules.getModuleTree({
    projectId: props.projectId
  });
  if (error) {
    return;
  }
  moduleTreeData.value = [{ name: t('functionBaseline.case.moduleTree.noModuleCases'), id: '-1' }, ...travelTreeData(data || [])];
};

onMounted(() => {
  loadDataList();
});

defineExpose({
  loadDataList
});
</script>
<template>
  <div class="h-full flex flex-col">
    <div
      :class="{'active': props.moduleId === ''}"
      class="flex items-center space-x-2 tree-title h-9 leading-9 pl-4.5 cursor-pointer all-case"
      @click="handleSelectKeysChange([''])">
      <Icon icon="icon-liebiaoshitu" class="text-3.5" />
      <span class="flex-1">{{ t('functionBaseline.case.moduleTree.allCases') }}</span>
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
      @select="handleSelectKeysChange">
      <template #title="{key, title, name, id, index, level, isLast, pid, ids, sequence, childLevels, hasEditPermission}">
        <div v-if="editId === id" class="flex items-center">
          <Input
            ref="nameInputRef"
            :placeholder="t('functionBaseline.case.moduleTree.enterModuleName')"
            class="flex-1 mr-2 bg-white"
            trim
            :value="name"
            :allowClear="false"
            :maxlength="50"
            @blur="hadleblur(id, $event)"
            @pressEnter="pressEnter(id, $event)" />
          <Button
            type="link"
            size="small"
            class="px-0 py-0 mr-1"
            @click="cancelEdit">
            {{ t('functionBaseline.case.moduleTree.cancel') }}
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
  <AsyncComponent :visible="modalVisible">
    <CreateModal
      v-model:visible="modalVisible"
      :projectId="props.projectId"
      :pid="pid"
      @ok="createOk" />
  </AsyncComponent>
  <AsyncComponent :visible="moveVsible">
    <MoveModuleModal
      v-model:visible="moveVsible"
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
