<script lang="ts" setup>
import { onMounted, ref, watch } from 'vue';
import { Colon, Hints, IconRequired, Select } from '@xcan-angus/vue-ui';
import { Tree } from 'ant-design-vue';
import { TESTER } from '@xcan-angus/tools';
import { contentTreeData } from './config';

interface Props {
  projectId: string;
  contentSetting: {
    targetId?: string;
    planOrplanId?: string;
  };
  disabled: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  projectId: '',
  disabled: false
});

const planId = ref();
const caseId = ref();

const checked = ref<string[]>([]);
contentTreeData.forEach(item => {
  checked.value.push(item.key);
  if (item?.children) {
    checked.value.push(...item.children.map(i => i.key));
  }
});

const handleSprintChange = () => {
  caseId.value = undefined;
};

onMounted(() => {
  watch(() => props.contentSetting, newValue => {
    if (newValue) {
      const { targetId, planOrplanId } = newValue;
      if (targetId) {
        caseId.value = targetId;
      }
      if (planOrplanId) {
        planId.value = planOrplanId;
      }
    }
  }, {
    immediate: true
  });
});
const isValid = ref(false);
const validate = () => {
  isValid.value = true;
  if (!caseId.value || !planId.value) {
    return false;
  }
  return true;
};

defineExpose({
  validate,
  getData: () => {
    isValid.value = false;
    return {
      targetId: caseId.value,
      targetType: 'FUNC_CASE',
      planOrplanId: planId.value
    };
  }
});
</script>
<template>
  <div class="flex items-center space-x-1">
    <span class="h-4 w-1.5 bg-blue-border1"></span>
    <span>过滤</span>
  </div>
  <div class="mt-2  pl-2 space-y-2">
    <div class="flex flex-1 items-center space-x-2">
      <div class="w-15 text-right"><IconRequired class="mr-1" />测试计划</div>
      <Colon />
      <Select
        v-model:value="planId"
        :disabled="!props.projectId || props.disabled"
        :showSearch="true"
        :error="isValid && !planId"
        :lazy="false"
        :defaultActiveFirstOption="true"
        allowClear
        class="w-72"
        placeholder="选择计划"
        :action="`${TESTER}/func/plan?projectId=${props.projectId || ''}&fullTextSearch=true`"
        :fieldNames="{ label: 'name', value: 'id' }"
        @change="handleSprintChange">
      </Select>
    </div>
    <div class="flex flex-1 items-center space-x-2">
      <div class="w-15 text-right"><IconRequired class="mr-1" />用例</div>
      <Colon />
      <Select
        v-model:value="caseId"
        :disabled="!props.projectId || props.disabled"
        :showSearch="true"
        :lazy="false"
        :defaultActiveFirstOption="true"
        :error="isValid && !caseId"
        allowClear
        class="w-72"
        placeholder="选择用例"
        :action="`${TESTER}/func/case?projectId=${props.projectId}&planId=${planId || ''}&fullTextSearch=true`"
        :fieldNames="{ label: 'name', value: 'id' }">
      </Select>
    </div>
  </div>
  <div class="flex items-center space-x-2">
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
    <template #title="{title}">
      <span style="color: rgb(82, 90, 101);">{{ title }}</span>
    </template>
  </Tree>
</template>
