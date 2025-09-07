<script lang="ts" setup>
import { Tree } from 'ant-design-vue';
import { Icon } from '@xcan-angus/vue-ui';
import { useCategorySelect } from './composables/useCategorySelect';
import type { CategorySelectEmits, CategorySelectProps } from './types';

// Props and emits definition
const props = withDefaults(defineProps<CategorySelectProps>(), {
  category: undefined
});

const emits = defineEmits<CategorySelectEmits>();

// Use category select composable
const { moduleTreeData, categoryIcon, handleSelectKeysChange } = useCategorySelect(props, emits);

</script>

<template>
  <div>
    <Tree
      :treeData="moduleTreeData"
      :selectedKeys="props.category ? [props.category] : []"
      class="flex-1 overflow-auto"
      blockNode
      defaultExpandAll
      :fieldNames="{
        children: 'children',
        title: 'name',
        key: 'value'
      }"
      @select="(selectedKeys: any[]) => handleSelectKeysChange(selectedKeys.map(key => String(key)))">
      <template #title="{name, value}">
        <Icon :icon="categoryIcon[value]" class="text-3.5 mr-1" />
        <span class="flex-1">{{ name }}</span>
      </template>
    </Tree>
  </div>
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
