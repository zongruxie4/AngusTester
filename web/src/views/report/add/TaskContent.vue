<script lang="ts" setup>
import { onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Colon, Hints, IconRequired, Select } from '@xcan-angus/vue-ui';
import { Tree } from 'ant-design-vue';
import { TESTER } from '@xcan-angus/infra';
import { CombinedTargetType } from '@/enums/enums';

import { contentTreeData } from './TaskContentConfig';

const { t } = useI18n();

// Component props definition
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

// Reactive variables for sprint ID and task ID
const sprintId = ref();
const taskId = ref();

// Checked keys for tree component
const checked = ref<string[]>([]);
contentTreeData.forEach(item => {
  checked.value.push(item.key);
  if (item.children) {
    checked.value.push(...item.children.map(i => i.key));
  }
});

/**
 * Handle sprint change event
 * Reset task ID when sprint changes
 */
const handleSprintChange = () => {
  taskId.value = undefined;
};

/**
 * Lifecycle hook - Initialize component
 * Watch for content setting changes and update sprint ID and task ID
 */
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

// Validation state
const isValid = ref(false);

/**
 * Validate if both sprint ID and task ID are selected
 * @returns Boolean indicating if validation passes
 */
const validate = () => {
  isValid.value = true;
  return !(!taskId.value || !sprintId.value);
};

/**
 * Get task data for report
 * @returns Object containing task settings
 */
const getData = () => {
  isValid.value = false;
  return {
    targetId: taskId.value,
    targetType: CombinedTargetType.TASK,
    planOrSprintId: sprintId.value
  };
};

// Expose methods to parent component
defineExpose({
  validate,
  getData
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
        {{ t('common.sprint') }}
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
        {{ t('common.issue') }}
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
