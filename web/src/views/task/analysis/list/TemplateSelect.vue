<script lang="ts" setup>
import { onMounted, ref } from 'vue';
import { Tree } from 'ant-design-vue';
import { enumUtils } from '@xcan-angus/infra';
import { AnalysisTaskTemplate, AnalysisTaskTemplateDesc } from '@/enums/enums';
import { Icon } from '@xcan-angus/vue-ui';
import { TemplateIconConfig } from './types';
import { useI18n } from 'vue-i18n';

interface Props {
  template: string;
  templateData: {value: string; message: string}[];
  templateDesc: {value: string; message: string}[];
}

const props = withDefaults(defineProps<Props>(), {
  template: undefined,
  templateData: () => []
});
const { t } = useI18n();

const emits = defineEmits<{(e: 'update:template', value: string):void;
  (e: 'update:templateData', value: {value: string; message: string}[]):void;
  (e:'update:templateDesc', value: {value: string; message: string}[]):void}>();
const moduleTreeData = ref<{name: string; value: string}[]>([{ name: t('taskAnalysis.all'), value: '' }]);

const loadOpt = () => {
  const data = enumUtils.enumToMessages(AnalysisTaskTemplate);
  moduleTreeData.value.push(...(data || []).map(item => ({ ...item, name: item.message })));
  emits('update:templateData', data);
  const desc = enumUtils.enumToMessages(AnalysisTaskTemplateDesc);
  emits('update:templateDesc', desc);
};

const handleSelectKeysChange = (value) => {
  emits('update:template', value[0]);
};

onMounted(() => {
  loadOpt();
});

</script>
<template>
  <div>
    <Tree
      :treeData="moduleTreeData"
      :selectedKeys="[props.template]"
      class="flex-1"
      blockNode
      defaultExpandAll
      :fieldNames="{
        children: 'children',
        title: 'name',
        key: 'value'
      }"
      @select="handleSelectKeysChange">
      <template #title="{key, title, name, value, index, level, isLast, pid, ids, sequence, childLevels, hasEditPermission}">
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
