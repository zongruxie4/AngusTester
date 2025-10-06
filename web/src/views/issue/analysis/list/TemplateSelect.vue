<script lang="ts" setup>
import { onMounted, ref } from 'vue';
import { Tree } from 'ant-design-vue';
import { EnumMessage, enumUtils } from '@xcan-angus/infra';
import { AnalysisTaskTemplate, AnalysisTaskTemplateDesc } from '@/enums/enums';
import { Icon } from '@xcan-angus/vue-ui';
import { TemplateIconConfig } from './types';
import { useI18n } from 'vue-i18n';

// Type Definitions
interface Props {
  template: string;
  templateData: EnumMessage<AnalysisTaskTemplate>[];
  templateDesc: EnumMessage<AnalysisTaskTemplateDesc>[];
}

// Props and Emits
const props = withDefaults(defineProps<Props>(), {
  template: undefined,
  templateData: () => []
});

const { t } = useI18n();

// eslint-disable-next-line func-call-spacing
const emits = defineEmits<{
  (e: 'update:template', value: string): void;
  (e: 'update:templateData', value: EnumMessage<AnalysisTaskTemplate>[]): void;
  (e: 'update:templateDesc', value: EnumMessage<AnalysisTaskTemplateDesc>[]): void
}>();

/**
 * Tree data for template selection with all available options
 */
const templateTreeData = ref<{ name: string; value: string; key: string }[]>([
  { name: t('issueAnalysis.all'), value: '', key: '' }
]);

/**
 * Handle template selection change
 * @param {any[]} selectedKeys - Array of selected keys
 */
const handleTemplateSelectionChange = (selectedKeys: any[]) => {
  emits('update:template', selectedKeys[0]);
};

/**
 * Load template options and descriptions from enums
 */
const loadTemplateOptions = () => {
  // Load analysis task templates
  const templateOptions = enumUtils.enumToMessages(AnalysisTaskTemplate) as EnumMessage<AnalysisTaskTemplate>[];
  templateTreeData.value.push(...(templateOptions || []).map(item => ({
    ...item,
    name: item.message,
    key: item.value
  })));
  emits('update:templateData', templateOptions);

  // Load template descriptions
  const templateDescriptions = enumUtils.enumToMessages(AnalysisTaskTemplateDesc) as EnumMessage<AnalysisTaskTemplateDesc>[];
  emits('update:templateDesc', templateDescriptions);
};

// Lifecycle Hooks
onMounted(() => {
  loadTemplateOptions();
});
</script>
<template>
  <div>
    <Tree
      :treeData="templateTreeData"
      :selectedKeys="[props.template]"
      class="flex-1"
      blockNode
      defaultExpandAll
      :fieldNames="{
        children: 'children',
        title: 'name',
        key: 'value'
      }"
      @select="handleTemplateSelectionChange">
      <template #title="{ name, value }">
        <Icon :icon="TemplateIconConfig[value]" class="text-3.5 mr-1" />
        <span class="flex-1">{{ name }}</span>
      </template>
    </Tree>
  </div>
</template>
<style scoped>

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
  padding: 0
}

:deep(.ant-tree .ant-tree-treenode.ant-tree-treenode-selected) {
  background-color: var(--content-tabs-bg-selected);
}

:deep(.ant-tree .ant-tree-treenode:hover) {
  background-color: var(--content-tabs-bg-selected);
}

:deep(.ant-tree .ant-tree-switcher) {
  width: 16px;
  height: 30px;
  margin-top: 2px;
  line-height: 30px;
}

:deep(.ant-tree .ant-tree-node-content-wrapper:hover) {
  background-color: transparent;
}

:deep(.ant-tree .ant-tree-node-content-wrapper) {
  display: flex;
  flex: 1 1 0%;
  flex-direction: column;
  justify-content: center;
  height: 30px;
  margin: 0;
  padding: 0;
  line-height: 30px;
}

:deep(.ant-tree .ant-tree-node-content-wrapper .ant-tree-iconEle) {
  height: 30px;
  line-height: 30px;
  vertical-align: initial;
}

:deep(.ant-tree .ant-tree-node-selected) {
  background-color: transparent;
}

:deep(.ant-tree .ant-tree-indent-unit) {
  width: 18px;
}

</style>
