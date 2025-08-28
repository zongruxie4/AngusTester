<script lang="ts" setup>
import { onMounted, reactive, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Input } from '@xcan-angus/vue-ui';
import { Form, FormItem } from 'ant-design-vue';

import SelectEnum from '@/components/selectEnum/index.vue';

const { t } = useI18n();
import { ScriptInfo } from '../PropsType';
import { FormState } from './PropsType';

type Props = {
  dataSource: ScriptInfo;
}

const props = withDefaults(defineProps<Props>(), {
  dataSource: undefined
});

const formRef = ref();

const formData = reactive<FormState>({
  name: undefined,
  type: undefined,
  typeName: undefined,
  description: undefined
});

const excludes = (data: { value: string; message: string; }) => {
  if (data.value === 'MOCK_APIS') {
    return true;
  }

  return false;
};

const change = (_value, option) => {
  formData.typeName = option.message;
};

const validate = () => {
  return formRef.value.validate();
};

const getFormData = () => {
  const { name, type, description, typeName } = formData;
  return { name, type, description, typeName };
};

onMounted(() => {
  watch(() => props.dataSource, (newValue) => {
    if (!newValue) {
      return;
    }

    const { name, description, type } = newValue;
    formData.name = name;
    formData.type = type?.value;
    formData.typeName = type?.message;
    formData.description = description;
  }, { immediate: true });
});

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
        :label="t('scriptDetail.form.scriptType')"
        name="type"
        :rules="[{ required: true, message: t('scriptDetail.form.selectScriptType') }]">
        <SelectEnum
          v-model:value="formData.type"
          :excludes="excludes"
          enumKey="ScriptType"
          :placeholder="t('scriptDetail.form.selectScriptType')"
          @change="change" />
      </FormItem>

      <FormItem
        :label="t('scriptDetail.form.scriptName')"
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
      <div class="mb-0.5">{{ t('scriptDetail.form.description') }}</div>
      <Input
        v-model:value="formData.description"
        :placeholder="t('scriptDetail.form.descriptionPlaceholder')"
        type="textarea"
        :autosize="{ minRows: 4, maxRows: 6 }"
        :maxlength="800" />
    </div>
  </div>
</template>
