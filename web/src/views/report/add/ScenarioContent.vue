<script lang="ts" setup>
import { onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Colon, Hints, IconRequired, Select } from '@xcan-angus/vue-ui';
import { Tree } from 'ant-design-vue';
import { TESTER } from '@xcan-angus/infra';
import { CombinedTargetType } from '@/enums/enums';

import { contentTreeData } from './ScenarioContentConfig';

const { t } = useI18n();

// Component props definition
interface Props {
  projectId: string;
  contentSetting: {
    targetId: string;
  };
  disabled: boolean
}

const props = withDefaults(defineProps<Props>(), {
  projectId: '',
  disabled: false
});

// Reactive variable for scenario ID
const scenarioId = ref();

// Field names for select component
const fieldNames = {
  label: 'name',
  value: 'id'
};

// Checked keys for tree component
const checked = ref<string[]>([]);
contentTreeData.forEach(item => {
  checked.value.push(item.key);
  if (item.children) {
    checked.value.push(...item.children.map(i => i.key));
  }
});

/**
 * Lifecycle hook - Initialize component
 * Watch for content setting changes and update scenario ID
 */
onMounted(() => {
  watch(() => props.contentSetting, newValue => {
    if (newValue?.targetId) {
      scenarioId.value = newValue.targetId;
    }
  }, {
    immediate: true
  });
});

// Validation state
const valid = ref(false);

/**
 * Validate if scenario ID is selected
 * @returns Boolean indicating if validation passes
 */
const validate = () => {
  valid.value = true;
  return !!scenarioId.value;
};

/**
 * Get scenario data for report
 * @returns Object containing scenario target ID and type
 */
const getData = () => {
  valid.value = false;
  return {
    targetId: scenarioId.value,
    targetType: CombinedTargetType.SCENARIO
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
    <span>{{ t('reportAdd.scenarioContent.filter') }}</span>
  </div>
  <div class="flex mt-2 pl-2">
    <div class="inline-flex flex-1 items-center space-x-2">
      <div class="w-10 text-right"><IconRequired class="mr-1" />{{ t('common.scenario') }}</div>
      <Colon />
      <Select
        v-model:value="scenarioId"
        :placeholder="t('common.placeholders.selectScenario')"
        :showSearch="true"
        :error="valid && !scenarioId"
        :disabled="!props.projectId || props.disabled"
        :action="`${TESTER}/scenario?projectId=${props.projectId}&fullTextSearch=true`"
        :lazy="false"
        :defaultActiveFirstOption="true"
        :fieldNames="fieldNames"
        class="w-50" />
    </div>
  </div>
  <div class="flex items-center space-x-1 mt-4">
    <span class="h-4 w-1.5 bg-blue-border1"></span>
    <span>{{ t('reportAdd.scenarioContent.content') }}</span>
    <Hints :text="t('reportAdd.scenarioContent.contentHints')" />
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
