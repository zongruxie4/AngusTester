<script lang="ts" setup>
import { inject, ref, Ref } from 'vue';
import { Tree } from 'ant-design-vue';
import { Icon, Modal } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import { useMove } from './composables/useMove';

interface Props {
  visible: boolean,
  moveIds?: string[]
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  moveIds: undefined
});

const emit = defineEmits<{(e: 'ok', target: {targetSpaceId: string, targetDirectoryId?: string}): void, (e: 'update:visible', value: boolean): void}>();

const { t } = useI18n();

// Inject project information
const projectId = inject<Ref<string>>('projectId', ref(''));

// Use the move composable
const { expandedKeys, selectedKeys, target, treeData, loadNodeData, handleSelect, resetTreeState, init } = useMove(props, projectId.value);

// Initialize the composable
init();

/**
 * Confirm move operation
 */
const confirm = (): void => {
  if (selectedKeys.value.length) {
    emit('ok', target.value as {targetSpaceId: string, targetDirectoryId?: string});
  }
};

/**
 * Cancel move operation
 */
const cancel = (): void => {
  emit('update:visible', false);
};
</script>
<template>
  <Modal
    :visible="visible"
    :title="t('file.moveModal.title')"
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
      :loadData="loadNodeData"
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
