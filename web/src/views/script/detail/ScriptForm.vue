<script lang="ts" setup>
import { useI18n } from 'vue-i18n';
import { Input } from '@xcan-angus/vue-ui';
import { Form, FormItem } from 'ant-design-vue';

import SelectEnum from '@/components/enum/SelectEnum.vue';
import { ScriptInfo } from '../types';
import { useScriptForm } from './composables/useScriptForm';

const { t } = useI18n();

type Props = {
  dataSource: ScriptInfo;
}

const props = withDefaults(defineProps<Props>(), {
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
  <div class="overflow-auto">
    <Form
      ref="formRef"
      :model="formData"
      layout="vertical"
      size="small">
      <FormItem
        :label="t('common.scriptType')"
        name="type"
        :rules="[{ required: true, message: t('scriptDetail.form.selectScriptType') }]">
        <SelectEnum
          v-model:value="formData.type"
          :excludes="excludes"
          enumKey="ScriptType"
          :placeholder="t('scriptDetail.form.selectScriptType')"
          @change="handleTypeChange" />
      </FormItem>

      <FormItem
        :label="t('common.scriptName')"
        name="name"
        size="small"
        :rules="[{ required: true, message: t('scriptDetail.form.enterScriptName') }]">
        <Input
          v-model:value="formData.name"
          :maxlength="200"
          :placeholder="t('scriptDetail.form.namePlaceholder')" />
      </FormItem>
    </Form>

    <div class="leading-5 mt-5">
      <div class="mb-0.5">{{ t('common.description') }}</div>
      <Input
        v-model:value="formData.description"
        :placeholder="t('scriptDetail.form.descriptionPlaceholder')"
        type="textarea"
        :autosize="{ minRows: 4, maxRows: 6 }"
        :maxlength="800" />
    </div>
  </div>
</template>
