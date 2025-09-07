<script lang="ts" setup>
import { onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Colon, DatePicker, Hints, IconRequired, Select } from '@xcan-angus/vue-ui';
import { Tree } from 'ant-design-vue';
import { AuthObjectType, enumOptionUtils, GM, TESTER } from '@xcan-angus/infra';
import { CombinedTargetType } from '@/enums/enums';

import { contentTreeData } from './SprintContentConfig';

const { t } = useI18n();

// Component props definition
interface Props {
  projectId: string;
  contentSetting: {
    creatorObjectType?: string;
    creatorObjectId?: string;
    createdDateStart?: string;
    createdDateEnd?: string;
    targetId?: string;
  };
  disabled: boolean
}

const props = withDefaults(defineProps<Props>(), {
  projectId: '',
  disabled: false
});

// Target type options for select component
const authObjectTypeOpt = enumOptionUtils.loadEnumAsOptions(AuthObjectType);

// Reactive variables for creator object type and ID
const creatorObjectType = ref(AuthObjectType.USER);
const creatorObjectId = ref();

// Date range and sprint ID
const dateRange = ref();
const sprintId = ref();

/**
 * Handle creator object type change
 * Reset creator object ID when type changes
 */
const handleTypeChange = () => {
  creatorObjectId.value = undefined;
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
 * Watch for content setting changes and update creator object type/ID, date range, and sprint ID
 */
onMounted(() => {
  watch(() => props.contentSetting, newValue => {
    if (newValue) {
      if (newValue.creatorObjectType) {
        creatorObjectType.value = newValue.creatorObjectType;
      }
      if (newValue.creatorObjectId) {
        creatorObjectId.value = newValue.creatorObjectId;
      }

      const { createdDateStart, createdDateEnd, targetId } = newValue;
      if (createdDateStart && createdDateEnd) {
        dateRange.value = [createdDateStart, createdDateEnd];
      }
      if (targetId) {
        sprintId.value = targetId;
      }
    }
  }, {
    immediate: true
  });
});

// Validation state
const isValid = ref(false);

/**
 * Validate if sprint ID is selected
 * @returns Boolean indicating if validation passes
 */
const validate = () => {
  isValid.value = true;
  return sprintId.value;
};

/**
 * Get sprint data for report
 * @returns Object containing sprint settings
 */
const getData = () => {
  isValid.value = false;
  return {
    targetId: sprintId.value,
    targetType: CombinedTargetType.TASK_SPRINT,
    planOrSprintId: sprintId.value,
    creatorObjectType: creatorObjectId.value ? creatorObjectType.value : undefined,
    creatorObjectId: creatorObjectId.value,
    createdDateStart: dateRange.value?.[0] || undefined,
    createdDateEnd: dateRange.value?.[1] || undefined
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
    <span>{{ t('reportAdd.sprintContent.filter') }}</span>
  </div>
  <div class="mt-2  pl-2 space-y-2">
    <div class="flex flex-1 items-center space-x-2">
      <div class="w-12 text-right">
        <IconRequired />
        {{ t('reportAdd.sprintContent.sprint') }}
      </div>
      <Colon />
      <Select
        v-model:value="sprintId"
        :showSearch="true"
        :error="isValid && !sprintId"
        :disabled="!props.projectId || props.disabled"
        allowClear
        class="w-72"
        :lazy="false"
        :defaultActiveFirstOption="true"
        :placeholder="t('reportAdd.sprintContent.sprintPlaceholder')"
        :action="`${TESTER}/task/sprint?projectId=${props.projectId}&fullTextSearch=true`"
        :fieldNames="{ label: 'name', value: 'id' }">
      </Select>
    </div>
    <div class="flex flex-1 items-center space-x-2">
      <span class="w-12 text-right">{{ t('reportAdd.sprintContent.organization') }}</span><Colon />
      <Select
        v-model:value="creatorObjectType"
        :options="authObjectTypeOpt"
        class="w-20"
        @change="handleTypeChange" />
      <Select
        v-if="creatorObjectType === 'USER'"
        v-model:value="creatorObjectId"
        :showSearch="true"
        allowClear
        class="w-50"
        :placeholder="t('reportAdd.sprintContent.userPlaceholder')"
        :action="`${GM}/user?fullTextSearch=true`"
        :fieldNames="{ label: 'fullName', value: 'id' }">
      </Select>

      <Select
        v-if="creatorObjectType === 'DEPT'"
        v-model:value="creatorObjectId"
        :placeholder="t('reportAdd.sprintContent.deptPlaceholder')"
        class="w-50"
        allowClear
        :showSearch="true"
        :action="`${GM}/dept?fullTextSearch=true`"
        :fieldNames="{ label: 'name', value: 'id' }">
      </Select>

      <Select
        v-if="creatorObjectType === 'GROUP'"
        v-model:value="creatorObjectId"
        :placeholder="t('reportAdd.sprintContent.groupPlaceholder')"
        class="w-50"
        allowClear
        :showSearch="true"
        :action="`${GM}/group?fullTextSearch=true`"
        :fieldNames="{ label: 'name', value: 'id' }">
      </Select>
      <Hints :text="t('reportAdd.sprintContent.organizationHints')" />
    </div>
    <div class="flex flex-1 items-center space-x-2">
      <span class="w-12 text-right">{{ t('reportAdd.sprintContent.time') }}</span><Colon />
      <DatePicker
        v-model:value="dateRange"
        showTime
        class="w-72 flex-shrink-0"
        type="date-range" />
      <Hints :text="t('reportAdd.sprintContent.timeHints')" />
    </div>
  </div>
  <div class="flex items-center space-x-2">
  </div>
  <div class="flex items-center space-x-1 mt-4">
    <span class="h-4 w-1.5 bg-blue-border1"></span>
    <span>{{ t('reportAdd.sprintContent.content') }}</span>
    <Hints :text="t('reportAdd.sprintContent.contentHints')" />
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
