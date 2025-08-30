<script lang="ts" setup>
import { onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Colon, Hints, IconRequired, Select } from '@xcan-angus/vue-ui';
import { Tree } from 'ant-design-vue';
import { TESTER } from '@xcan-angus/infra';
import { contentTreeData } from './config';

const { t } = useI18n();

interface Props {
  projectId: string;
  contentSetting: {
    targetId?: string;
    planOrSprintId?: string;
  };
  disabled: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  projectId: '',
  disabled: false
});

const sprintId = ref();
const taskId = ref();

const checked = ref<string[]>([]);
contentTreeData.forEach(item => {
  checked.value.push(item.key);
  if (item.children) {
    checked.value.push(...item.children.map(i => i.key));
  }
});

const handleSprintChange = () => {
  taskId.value = undefined;
};

onMounted(() => {
  watch(() => props.contentSetting, newValue => {
    if (newValue) {
      const { targetId, planOrSprintId } = newValue;
      if (targetId) {
        taskId.value = targetId;
      }
      if (planOrSprintId) {
        sprintId.value = planOrSprintId;
      }
    }
  }, {
    immediate: true
  });
});
const isValid = ref(false);
const validate = () => {
  isValid.value = true;
  if (!taskId.value || !sprintId.value) {
    return false;
  }
  return true;
};

defineExpose({
  validate,
  getData: () => {
    isValid.value = false;
    return {
      targetId: taskId.value,
      targetType: 'TASK',
      planOrSprintId: sprintId.value
    };
  }
});
</script>
<template>
  <div class="flex items-center space-x-1">
    <span class="h-4 w-1.5 bg-blue-border1"></span>
    <span>{{ t('reportAdd.taskContent.filter') }}</span>
  </div>
  <div class="mt-2  pl-2 space-y-2">
    <div class="flex flex-1 items-center space-x-2">
      <div class="w-10 text-right">
        <IconRequired />
        {{ t('reportAdd.taskContent.sprint') }}
      </div>
      <Colon />
      <Select
        v-model:value="sprintId"
        :disabled="!props.projectId || props.disabled"
        :showSearch="true"
        :error="isValid && !sprintId"
        allowClear
        class="w-72"
        :placeholder="t('reportAdd.taskContent.sprintPlaceholder')"
        :action="`${TESTER}/task/sprint?projectId=${props.projectId || ''}&fullTextSearch=true`"
        :fieldNames="{ label: 'name', value: 'id' }"
        @change="handleSprintChange">
      </Select>
    </div>
    <div class="flex flex-1 items-center space-x-2">
      <div class="w-10 text-right">
        <IconRequired />
        {{ t('reportAdd.taskContent.task') }}
      </div>
      <Colon />
      <Select
        v-model:value="taskId"
        :disabled="!props.projectId || props.disabled"
        :showSearch="true"
        :lazy="false"
        :defaultActiveFirstOption="true"
        :error="isValid && !taskId"
        allowClear
        class="w-72"
        :placeholder="t('reportAdd.taskContent.taskPlaceholder')"
        :action="`${TESTER}/task?projectId=${props.projectId}&sprintId=${sprintId || ''}&fullTextSearch=true`"
        :fieldNames="{ label: 'name', value: 'id' }">
      </Select>
    </div>
  </div>
  <div class="flex items-center space-x-2">
  </div>
  <div class="flex items-center space-x-1 mt-4">
    <span class="h-4 w-1.5 bg-blue-border1"></span>
    <span>{{ t('reportAdd.taskContent.content') }}</span>
    <Hints :text="t('reportAdd.taskContent.contentHints')" />
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
