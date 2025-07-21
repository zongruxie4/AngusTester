<script setup lang="ts">
import { defineAsyncComponent, inject, ref } from 'vue';
import { duration } from '@xcan-angus/tools';
import { AsyncComponent, Icon, Input, modal, notification } from '@xcan-angus/vue-ui';
import { Button, Dropdown, Menu, MenuItem, Tree } from 'ant-design-vue';
import { debounce } from 'throttle-debounce';
import { modules } from '@/api/tester';

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
  dataList: TagItem[];
  moduleId: string;
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

const emits = defineEmits<{(e: 'loadData', value?: string); (e: 'update:moduleId', value: string):void}>();

const CreateModal = defineAsyncComponent(() => import('@/views/project/project/edit/module/add.vue'));
const MoveModuleModal = defineAsyncComponent(() => import('@/views/project/project/edit/module/move.vue'));

const projectInfo = inject('projectInfo', ref({}));
const isAdmin = inject('isAdmin', ref(false));
const tenantInfo = inject('tenantInfo', ref({}));

const nameInputRef = ref();

const loading = ref(false);
const handleSelectKeysChange = (selectKeys) => {
  if (!selectKeys.length) {
    return;
  }
  emits('update:moduleId', selectKeys[0]);
};

const handleSearchModule = debounce(duration.search, () => {
  emits('loadData', keywords.value);
});

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
  notification.success('修改成功');
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
    content: `确定删除模块【${data.name}】吗？`,
    async onOk () {
      const id = data.id;
      const params = { ids: [id] };
      loading.value = true;
      const [error] = await modules.deleteModule(params);
      loading.value = false;
      if (error) {
        return;
      }

      notification.success('删除模块成功');
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
          targetParent = props.dataList.find(item => item.id === linkId);
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
    params = {
      id,
      sequence: parentChildren[index - 1].sequence - 1
    };
  }

  const [error] = await modules.updateModule([params]);
  if (error) {
    return;
  }
  notification.success('移动成功');
  emits('loadData', keywords.value);
};

const moveDown = async (record) => {
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
  const params = {
    id,
    sequence: +parentChildren[index + 1].sequence + 1
  };
  const [error] = await modules.updateModule([params]);
  if (error) {
    return;
  }
  notification.success('移动成功');
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
</script>
<template>
  <div class="h-full flex flex-col">
    <div class="flex justify-between h-11 space-x-4 p-2">
      <Input
        v-model:value="keywords"
        placeholder="查询模块"
        @change="handleSearchModule" />
      <Button
        :disabled="!isAdmin && projectInfo?.createdBy !== tenantInfo?.id && projectInfo.ownerId !== tenantInfo?.id"
        type="primary"
        size="small"
        @click="onMenuClick({key: 'add'}, {id: undefined})">
        <Icon icon="icon-jia" />
        添加模块
      </Button>
    </div>
    <div
      :class="{'active': props.moduleId === ''}"
      class="flex items-center space-x-2 tree-title h-9 leading-9 pl-4.5 cursor-pointer all-case"
      @click="handleSelectKeysChange([''])">
      <Icon icon="icon-liebiaoshitu" class="text-3.5" />
      <span class="flex-1">全部用例</span>
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
            placeholder="请输入模块名称"
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
            取消
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
                  新建子模块
                </MenuItem>
                <MenuItem v-if="index > 0 || +pid > 0" key="up">
                  <Icon icon="icon-shangyi" />
                  {{ index < 1 ? '移到上一层' : '上移' }}
                </MenuItem>
                <MenuItem v-if="!isLast" key="down">
                  <Icon icon="icon-xiayi" />
                  下移
                </MenuItem>
                <MenuItem key="move">
                  <Icon icon="icon-yidong" />
                  移动
                </MenuItem>
                <MenuItem key="edit">
                  <Icon icon="icon-bianji" />
                  编辑
                </MenuItem>
                <MenuItem key="del">
                  <Icon icon="icon-qingchu" />
                  删除
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

:deep(.ant-tree-node-content-wrapper.ant-tree-node-content-wrapper-normal) {
  @apply !flex-1 min-w-0;
}

</style>
