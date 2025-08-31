<script lang="ts" setup>
import { computed, inject, ref, watch } from 'vue';
import type { TreeProps } from 'ant-design-vue';
import { Tree } from 'ant-design-vue';
import { Icon, Modal } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';

import { space } from '@/api/storage';
import store from '@/store';

const { t } = useI18n();

interface Props {
  visible: boolean,
  moveIds?: string[]
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  moveIds: undefined
});

const emit = defineEmits<{(e: 'ok', target: {targetSpaceId: string, targetDirectoryId?: string}): void, (e: 'update:visible', value: boolean): void}>();

const projectInfo = inject('projectInfo', ref({ id: '' }));
const projectId = computed(() => {
  return projectInfo.value?.id;
});

const expandedKeys = ref<string[]>([]);
const selectedKeys = ref<string[]>([]);

const target = ref<{targetSpaceId?: string, targetDirectoryId?: string}>({});

const treeData = ref<TreeProps['treeData']>([]);
const loadSpace = async () => {
  const [error, res = { data: {} }] = await space.getSpaceList({ appCode: 'AngusTester', pageSize: store.state.maxPageSize, projectId: projectId.value });
  if (error) {
    return;
  }
  treeData.value = (res.data.list || []).map(data => ({ ...data, type: 'SPACE' })).slice();
};

const onLoadData:TreeProps['loadData'] = treeNode => {
  const spaceId = treeNode.type?.value === 'DIRECTORY' ? treeNode.spaceId : treeNode.id;
  const parentDirectoryId = treeNode.type?.value === 'DIRECTORY' ? treeNode.id : '-1';
  return space.getFileList({ spaceId, parentDirectoryId, filters: [{ key: 'type', value: 'DIRECTORY', op: 'EQUAL' }], pageSize: store.state.maxPageSize, pageNo: 1 })
    .then(([error, res]) => {
      if (error) {
        return;
      }
      if (treeNode.dataRef?.children) {
        return;
      }

      if (treeNode.dataRef) {
        treeNode.dataRef.children = (res.data.list || []).map(data => ({ ...data, spaceId: spaceId, isLeaf: data.summary.subDirectoryNum === '0' })).filter(data => !props.moveIds?.includes(data.id));
      }
      treeData.value = [...treeData.value];
    });
};

const confirm = () => {
  if (selectedKeys.value.length) {
    emit('ok', target.value as {targetSpaceId: string, targetDirectoryId?: string});
  }
};

const cancel = () => {
  emit('update:visible', false);
};

watch(() => props.visible, newValue => {
  if (newValue) {
    loadSpace();
  } else {
    expandedKeys.value = [];
    treeData.value = [];
    selectedKeys.value = [];
  }
}, {
  immediate: true
});

const handleSelect = (_selectedKeys, { selected, selectedNodes }) => {
  if (selected) {
    const { type, id, spaceId } = selectedNodes[0];
    target.value = type === 'SPACE' ? { targetSpaceId: id } : { targetSpaceId: spaceId, targetDirectoryId: id };
  } else {
    target.value = {};
  }
};

</script>
<template>
  <Modal
    :visible="visible"
    :title="t('fileSpace.moveModal.title')"
    :okButtonProps="{
      disabled: !selectedKeys.length
    }"
    :destroyOnClose="true"
    @ok="confirm"
    @cancel="cancel">
    <Tree
      v-model:expandedKeys="expandedKeys"
      v-model:selectedKeys="selectedKeys"
      showIcon
      class="space-tree"
      :height="400"
      :fieldNames="{children:'children', title:'name', key:'id'}"
      :loadData="onLoadData"
      :treeData="treeData"
      @select="handleSelect">
      <template #icon="{type}">
        <template v-if="type==='SPACE'">
          <Icon icon="icon-kongjian" class="text-4" />
        </template>
        <template v-else>
          <Icon icon="icon-wenjianjia" class="text-4" />
        </template>
      </template>
    </Tree>
  </Modal>
</template>
<style>
.space-tree.ant-tree .ant-tree-treenode {
  max-width: 480px;
  white-space: nowrap;
}

.space-tree.ant-tree .ant-tree-treenode .ant-tree-node-content-wrapper {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
