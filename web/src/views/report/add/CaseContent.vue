<script lang="ts" setup>
import { onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Colon, Hints, IconRequired, Select } from '@xcan-angus/vue-ui';
import { Tree } from 'ant-design-vue';
import { TESTER } from '@xcan-angus/infra';
import { contentTreeData } from './CaseContentConfig';
import { CombinedTargetType } from '@/enums/enums';

const { t } = useI18n();

// Component props definition
interface Props {
  projectId: string;
  contentSetting: {
    targetId?: string;
      planOrPlanId?: string;
  };
  disabled: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  projectId: '',
  disabled: false
});

// Reactive variables for plan ID and case ID
const planId = ref();
const caseId = ref();

// Checked keys for tree component
const checked = ref<string[]>([]);
contentTreeData.forEach(item => {
  checked.value.push(item.key);
  if (item?.children) {
    checked.value.push(...item.children.map(i => i.key));
  }
});

/**
 * Handle plan change event
 * Reset case ID when plan changes
 */
const handlePlanChange = () => {
  caseId.value = undefined;
};

/**
 * Lifecycle hook - Initialize component
 * Watch for content setting changes and update plan/case IDs
 */
onMounted(() => {
  watch(() => props.contentSetting, newValue => {
    if (newValue) {
      const { targetId, planOrPlanId } = newValue;
      if (targetId) {
        caseId.value = targetId;
      }
      if (planOrPlanId) {
        planId.value = planOrPlanId;
      }
    }
  }, {
    immediate: true
  });
});

// Validation state
const isValid = ref(false);

/**
 * Validate if both plan ID and case ID are selected
 * @returns Boolean indicating if validation passes
 */
const validate = () => {
  isValid.value = true;
  return !(!caseId.value || !planId.value);
};

/**
 * Get case data for report
 * @returns Object containing case target ID, type, and plan ID
 */
const getData = () => {
  isValid.value = false;
  return {
    targetId: caseId.value,
    targetType: CombinedTargetType.FUNC_CASE,
    planOrPlanId: planId.value
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
    <span>{{ t('reportAdd.caseContent.filter') }}</span>
  </div>
  <div class="mt-2  pl-2 space-y-2">
    <div class="flex flex-1 items-center space-x-2">
      <div class="w-15 text-right"><IconRequired class="mr-1" />{{ t('reportAdd.caseContent.testPlan') }}</div>
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
        :placeholder="t('reportAdd.caseContent.testPlanPlaceholder')"
        :action="`${TESTER}/func/plan?projectId=${props.projectId || ''}&fullTextSearch=true`"
        :fieldNames="{ label: 'name', value: 'id' }"
        @change="handlePlanChange">
      </Select>
    </div>
    <div class="flex flex-1 items-center space-x-2">
      <div class="w-15 text-right"><IconRequired class="mr-1" />{{ t('reportAdd.caseContent.testCase') }}</div>
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
        :placeholder="t('reportAdd.caseContent.testCasePlaceholder')"
        :action="`${TESTER}/func/case?projectId=${props.projectId}&planId=${planId || ''}&fullTextSearch=true`"
        :fieldNames="{ label: 'name', value: 'id' }">
      </Select>
    </div>
  </div>
  <div class="flex items-center space-x-2">
  </div>
  <div class="flex items-center space-x-1 mt-4">
    <span class="h-4 w-1.5 bg-blue-border1"></span>
    <span>{{ t('reportAdd.caseContent.content') }}</span>
    <Hints :text="t('reportAdd.caseContent.contentHints')" />
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
