<script lang="ts" setup>
import { useI18n } from 'vue-i18n';
import { Input } from '@xcan-angus/vue-ui';
import { Form, FormItem } from 'ant-design-vue';
import { DataSourceProps } from '@/types/types';

import { ScriptInfo } from '../types';
import { useScriptForm } from './composables/useScriptForm';

import SelectEnum from '@/components/form/enum/SelectEnum.vue';

const { t } = useI18n();

const props = withDefaults(defineProps<DataSourceProps<ScriptInfo>>(), {
  dataSource: undefined
});

// Use script form composable
const {
  formRef,
  formData,
  excludes,
  handleTypeChange,
  validate,
  getFormData
} = useScriptForm(props);

// Expose methods to parent component
defineExpose({ getFormData, validate });
</script>
<template>
  <div class="overflow-auto font-semibold">
    <Form
      ref="formRef"
      :model="formData"
      layout="vertical"
      size="small">
      <FormItem
        :label="t('common.scriptType')"
        name="type"
        :rules="[{ required: true, message: t('common.placeholders.selectScriptType') }]">
        <SelectEnum
          v-model:value="formData.type"
          :excludes="excludes"
          enumKey="ScriptType"
          :placeholder="t('common.placeholders.selectScriptType')"
          @change="handleTypeChange" />
      </FormItem>

      <FormItem
        :label="t('common.scriptName')"
        name="name"
        size="small"
        :rules="[{ required: true, message: t('scriptHome.messages.namePlaceholder') }]">
        <Input
          v-model:value="formData.name"
          :maxlength="200"
          :placeholder="t('scriptHome.messages.namePlaceholder')" />
      </FormItem>
    </Form>

    <div class="leading-5 mt-5">
      <div class="mb-0.5">{{ t('common.description') }}</div>
      <Input
        v-model:value="formData.description"
        :placeholder="t('scriptHome.messages.descriptionPlaceholder')"
        type="textarea"
        :autosize="{ minRows: 6, maxRows: 10 }"
        :maxlength="800" />
    </div>
  </div>
</template>
