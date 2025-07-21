<script lang="ts" setup>
import { onMounted, ref, watch } from 'vue';
import { Colon, Hints, IconRequired, Select } from '@xcan-angus/vue-ui';
import { Tree } from 'ant-design-vue';
import { TESTER } from '@xcan-angus/tools';
import { contentTreeData } from './config';

interface Props {
  projectId: string;
  contentSetting: {
    targetId: string;
  };
  execType: 'TEST_CUSTOMIZATION'| 'TEST_PERFORMANCE'| 'TEST_STABILITY';
  disabled: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  projectId: '',
  disabled: false
});

const execId = ref();
const fieldNames = {
  label: 'name',
  value: 'id'
};
const checked = ref<string[]>([]);
contentTreeData.forEach(item => {
  checked.value.push(item.key);
  if (item.children) {
    checked.value.push(...item.children.map(i => i.key));
  }
});

const execParams = {
  filters: [
    {
      key: 'plugin',
      value: ['Http', 'Jdbc', 'WebSocket', 'Tcp', 'Ldap', 'Mail', 'Smtp', 'Ftp'],
      op: 'IN'
    },
    {
      key: 'status',
      value: 'COMPLETED',
      op: 'EQUAL'
    }
  ]
};

onMounted(() => {
  watch(() => props.contentSetting, newValue => {
    if (newValue?.targetId) {
      execId.value = newValue.targetId;
    }
  }, {
    immediate: true
  });
});
const valid = ref(false);
const validate = () => {
  valid.value = true;
  if (execId.value) {
    return true;
  }
  return false;
};

defineExpose({
  validate,
  getData: () => {
    valid.value = false;
    return {
      targetId: execId.value,
      targetType: 'EXECUTION'
    };
  }
});
</script>
<template>
  <div class="flex items-center space-x-1">
    <span class="h-4 w-1.5 bg-blue-border1"></span>
    <span>过滤</span>
  </div>
  <div class="flex mt-2 pl-2">
    <div class="inline-flex flex-1 items-center space-x-2">
      <div class="w-10 text-right"><IconRequired class="mr-1" />执行</div>
      <Colon />
      <Select
        v-model:value="execId"
        placeholder="选择执行"
        :showSearch="true"
        :error="valid && !execId"
        :disabled="!props.projectId || props.disabled"
        :action="`${TESTER}/exec?projectId=${props.projectId}&scriptType=${props.execType || ''}&fullTextSearch=true`"
        :params="execParams"
        :lazy="false"
        :defaultActiveFirstOption="true"
        :fieldNames="fieldNames"
        class="w-50" />
    </div>
  </div>
  <div class="flex items-center space-x-1 mt-4">
    <span class="h-4 w-1.5 bg-blue-border1"></span>
    <span>内容</span>
    <Hints text="以下是报告输出内容目录信息。" />
  </div>
  <Tree
    v-model:checkedKeys="checked"
    class="mt-2 text-3"
    disabled
    :treeData="contentTreeData"
    :defaultExpandAll="true"
    :selectable="false"
    :checkable="true">
    <template #title="{title, tips}">
      <div class="flex items-start space-x-2">
        <span style="color: rgb(82, 90, 101);">{{ title }}</span>
        <Hints
          v-if="tips"
          :text="tips"
          class="leading-6 items-center" />
      </div>
    </template>
  </Tree>
</template>
