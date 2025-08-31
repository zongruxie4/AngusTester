<script lang="ts" setup>
import { onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Colon, DatePicker, Hints, IconRequired, Select } from '@xcan-angus/vue-ui';
import { Tree } from 'ant-design-vue';
import { TESTER, GM } from '@xcan-angus/infra';
import { contentTreeData } from './config';

const { t } = useI18n();

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

const targetTypeOpt = [
  {
    value: 'USER',
    label: t('reportAdd.sprintContent.user')
  },
  {
    value: 'DEPT',
    label: t('reportAdd.sprintContent.dept')
  },
  {
    value: 'GROUP',
    label: t('reportAdd.sprintContent.group')
  }
];

const creatorObjectType = ref('USER');
const creatorObjectId = ref();

const dateRange = ref();
const sprintId = ref();

const handleTypeChange = () => {
  creatorObjectId.value = undefined;
};

const checked = ref<string[]>([]);
contentTreeData.forEach(item => {
  checked.value.push(item.key);
  if (item.children) {
    checked.value.push(...item.children.map(i => i.key));
  }
});

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

const isValid = ref(false);
const validate = () => {
  isValid.value = true;
  if (!sprintId.value) {
    return false;
  }
  return true;
};

defineExpose({
  validate,
  getData: () => {
    isValid.value = false;
    return {
      targetId: sprintId.value,
      targetType: 'TASK_SPRINT',
      planOrSprintId: sprintId.value,
      creatorObjectType: creatorObjectId.value ? creatorObjectType.value : undefined,
      creatorObjectId: creatorObjectId.value,
      createdDateStart: dateRange.value?.[0] || undefined,
      createdDateEnd: dateRange.value?.[1] || undefined
    };
  }
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
        :options="targetTypeOpt"
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
