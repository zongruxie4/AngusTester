<script lang="ts" setup>
import { computed, inject, onMounted, Ref, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Colon, DatePicker, Hints, Select } from '@xcan-angus/vue-ui';
import { Tree } from 'ant-design-vue';
import { GM, AuthObjectType, enumOptionUtils } from '@xcan-angus/infra';
import { CombinedTargetType } from '@/enums/enums';

import { contentTreeData } from './ProjectProcessContentConfig';

const { t } = useI18n();

// Component props definition
interface Props {
  projectId: string;
  contentSetting: {
    creatorObjectType?: string;
    creatorObjectId?: string;
    createdDateStart?: string;
    createdDateEnd?: string;
  };
  disabled: boolean
}

const props = withDefaults(defineProps<Props>(), {
  projectId: '',
  disabled: false
});

// Injected project type visibility map
const proTypeShowMap = inject<Ref<{[key: string]: boolean}>>('proTypeShowMap', ref({ showTask: true, showBackLog: true, showMeeting: true, showSprint: true, showTasStatistics: true }));

// Target type options for select component
const authObjectTypeOpt = enumOptionUtils.loadEnumAsOptions(AuthObjectType);

// Reactive variables for creator object type and ID
const creatorObjectType = ref(AuthObjectType.USER);
const creatorObjectId = ref();

// Date range for filtering
const dateRange = ref();

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
 * Compute tree data based on project type visibility settings
 * @returns Filtered tree data
 */
const showTree = computed(() => {
  if (!proTypeShowMap.value.showTask) {
    return contentTreeData.filter(i => i.key !== 'task');
  }
  return contentTreeData;
});

/**
 * Lifecycle hook - Initialize component
 * Watch for content setting changes and update creator object type/ID and date range
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

      const { createdDateStart, createdDateEnd } = newValue;
      if (createdDateStart && createdDateEnd) {
        dateRange.value = [createdDateStart, createdDateEnd];
      }
    }
  }, {
    immediate: true
  });
});

/**
 * Validate form data
 * @returns Boolean indicating validation always passes for this component
 */
const validate = () => {
  return true;
};

/**
 * Get project process data for report
 * @returns Object containing project process settings
 */
const getData = () => {
  return {
    targetId: props.projectId,
    targetType: CombinedTargetType.PROJECT,
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
    <span>{{ t('reportAdd.projectProcessContent.filter') }}</span>
  </div>
  <div class="mt-2 space-y-2 pl-2">
    <div class="flex flex-1 items-center space-x-2">
      <span class="w-14 text-right">{{ t('reportAdd.projectProcessContent.organization') }}</span><Colon />
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
        :placeholder="t('reportAdd.projectProcessContent.userPlaceholder')"
        :action="`${GM}/user?fullTextSearch=true`"
        :fieldNames="{ label: 'fullName', value: 'id' }">
      </Select>

      <Select
        v-if="creatorObjectType === 'DEPT'"
        v-model:value="creatorObjectId"
        :placeholder="t('reportAdd.projectProcessContent.deptPlaceholder')"
        class="w-50"
        allowClear
        :showSearch="true"
        :action="`${GM}/dept?fullTextSearch=true`"
        :fieldNames="{ label: 'name', value: 'id' }">
      </Select>

      <Select
        v-if="creatorObjectType === 'GROUP'"
        v-model:value="creatorObjectId"
        :placeholder="t('reportAdd.projectProcessContent.groupPlaceholder')"
        class="w-50"
        allowClear
        :showSearch="true"
        :action="`${GM}/group?fullTextSearch=true`"
        :fieldNames="{ label: 'name', value: 'id' }">
      </Select>
      <Hints :text="t('reportAdd.projectProcessContent.organizationHints')" />
    </div>
    <div class="flex flex-1 items-center space-x-2">
      <span class="w-14 text-right">{{ t('reportAdd.projectProcessContent.time') }}</span>
      <Colon />
      <DatePicker
        v-model:value="dateRange"
        showTime
        class="w-72 flex-shrink-0"
        type="date-range" />
      <Hints :text="t('reportAdd.projectProcessContent.timeHints')" />
    </div>
  </div>
  <div class="flex items-center space-x-1 mt-4">
    <span class="h-4 w-1.5 bg-blue-border1"></span>
    <span>{{ t('reportAdd.projectProcessContent.content') }}</span>
    <Hints :text="t('reportAdd.projectProcessContent.contentHints')" />
  </div>
  <Tree
    v-model:checkedKeys="checked"
    class="mt-2 text-3"
    disabled
    :treeData="showTree"
    :defaultExpandAll="true"
    :selectable="false"
    :checkable="true">
    <template #title="{title}">
      <span style="color: rgb(82, 90, 101);">{{ title }}</span>
    </template>
  </Tree>
</template>
