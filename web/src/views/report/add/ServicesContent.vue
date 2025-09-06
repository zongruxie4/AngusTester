<script lang="ts" setup>
import { onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Colon, Hints, IconRequired, Select } from '@xcan-angus/vue-ui';
import { Tree } from 'ant-design-vue';
import { TESTER } from '@xcan-angus/infra';
import { contentTreeData } from './ServicesContentConfig';

const { t } = useI18n();

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

const serviceId = ref();
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

onMounted(() => {
  watch(() => props.contentSetting, newValue => {
    if (newValue?.targetId) {
      serviceId.value = newValue.targetId;
    }
  }, {
    immediate: true
  });
});
const valid = ref(false);
const validate = () => {
  valid.value = true;
  if (serviceId.value) {
    return true;
  }
  return false;
};

defineExpose({
  validate,
  getData: () => {
    valid.value = false;
    return {
      targetId: serviceId.value,
      targetType: 'SERVICE'
    };
  }
});
</script>
<template>
  <div class="flex items-center space-x-1">
    <span class="h-4 w-1.5 bg-blue-border1"></span>
    <span>{{ t('reportAdd.servicesContent.filter') }}</span>
  </div>
  <div class="flex mt-2 pl-2">
    <div class="inline-flex flex-1 items-center space-x-2">
      <div class="w-10 text-right"><IconRequired class="mr-1" />{{ t('reportAdd.servicesContent.service') }}</div>
      <Colon />
      <Select
        v-model:value="serviceId"
        :placeholder="t('reportAdd.servicesContent.servicePlaceholder')"
        :error="valid && !serviceId"
        :disabled="!props.projectId || props.disabled"
        :action="`${TESTER}/services?projectId=${props.projectId}&fullTextSearch=true`"
        :lazy="false"
        :defaultActiveFirstOption="true"
        :fieldNames="fieldNames"
        class="w-50" />
    </div>
  </div>
  <div class="flex items-center space-x-1 mt-4">
    <span class="h-4 w-1.5 bg-blue-border1"></span>
    <span>{{ t('reportAdd.servicesContent.content') }}</span>
    <Hints :text="t('reportAdd.servicesContent.contentHints')" />
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
