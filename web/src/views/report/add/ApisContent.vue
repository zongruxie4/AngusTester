<script lang="ts" setup>
import { onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Colon, Hints, IconRequired, Select } from '@xcan-angus/vue-ui';
import { Tree } from 'ant-design-vue';
import { TESTER } from '@xcan-angus/infra';
import { contentTreeData } from './ApisContentConfig';
import { apis } from '@/api/tester';
import { CombinedTargetType } from '@/enums/enums';

const { t } = useI18n();

// Component props definition
interface Props {
  projectId: string;
  contentSetting: {
    targetId: string;
  };
  disabled: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  projectId: '',
  disabled: false
});

// Reactive variables for service ID and API ID
const serviceId = ref();
const apisId = ref();

// Field names for select components
const fieldNames = {
  label: 'name',
  value: 'id'
};

const apisFieldNames = {
  label: 'summary',
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
 * Load service ID based on API ID
 * Fetches API details and sets the corresponding service ID
 */
const loadServiceId = async () => {
  const [error, { data }] = await apis.getApiDetail(apisId.value);
  if (error) {
    return;
  }
  serviceId.value = data?.serviceId;
};

/**
 * Lifecycle hook - Initialize component
 * Watch for content setting changes and update API ID and service ID
 */
onMounted(() => {
  watch(() => props.contentSetting, newValue => {
    if (newValue?.targetId) {
      apisId.value = newValue.targetId;
      loadServiceId();
    }
  }, {
    immediate: true
  });
});

// Validation state
const valid = ref(false);

/**
 * Validate if API ID is selected
 * @returns Boolean indicating if validation passes
 */
const validate = () => {
  valid.value = true;
  return !!apisId.value;
};

/**
 * Get API data for report
 * @returns Object containing API settings
 */
const getData = () => {
  valid.value = false;
  return {
    targetId: apisId.value,
    targetType: CombinedTargetType.API
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
    <span>{{ t('reportAdd.apisContent.filter') }}</span>
  </div>
  <div class="flex mt-2 pl-2">
    <div class="inline-flex flex-1 items-center space-x-2">
      <span>
        {{ t('reportAdd.apisContent.service') }}
      </span><Colon />
      <Select
        v-model:value="serviceId"
        :placeholder="t('reportAdd.apisContent.servicePlaceholder')"
        :disabled="!props.projectId || props.disabled"
        :action="`${TESTER}/services?projectId=${props.projectId}&fullTextSearch=true`"
        :lazy="false"
        :defaultActiveFirstOption="true"
        :fieldNames="fieldNames"
        class="w-50" />
      <span>
        <IconRequired />
        {{ t('reportAdd.apisContent.api') }}
      </span><Colon />
      <Select
        v-model:value="apisId"
        :placeholder="t('reportAdd.apisContent.apiPlaceholder')"
        :showSearch="true"
        :error="valid && !apisId"
        :disabled="!projectId || props.disabled"
        :action="`${TESTER}/apis?projectId=${props.projectId}&serviceId=${serviceId}&fullTextSearch=true`"
        :lazy="false"
        :defaultActiveFirstOption="true"
        :fieldNames="apisFieldNames"
        class="w-50" />
    </div>
  </div>
  <div class="flex items-center space-x-1 mt-4">
    <span class="h-4 w-1.5 bg-blue-border1"></span>
    <span>{{ t('reportAdd.apisContent.content') }}</span>
    <Hints :text="t('reportAdd.apisContent.contentHints')" />
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
