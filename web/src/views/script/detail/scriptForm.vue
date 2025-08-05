<script lang="ts" setup>
import { onMounted, reactive, ref, watch } from 'vue';
import { Input } from '@xcan-angus/vue-ui';
import { Form, FormItem } from 'ant-design-vue';

import SelectEnum from '@/components/SelectEnum/index.vue';
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
        label="脚本类型"
        name="type"
        :rules="[{ required: true, message: '请选择脚本类型' }]">
        <SelectEnum
          v-model:value="formData.type"
          :excludes="excludes"
          enumKey="ScriptType"
          placeholder="请选择脚本类型"
          @change="change" />
      </FormItem>

      <FormItem
        label="脚本名称"
        name="name"
        size="small"
        :rules="[{ required: true, message: '请填写脚本名称' }]">
        <Input
          v-model:value="formData.name"
          :maxlength="200"
          placeholder="请输入脚本名称,最多200字符" />
      </FormItem>
    </Form>

    <div class="leading-5 mt-5">
      <div class="mb-0.5">描述</div>
      <Input
        v-model:value="formData.description"
        placeholder="请输入脚本描述，最多800字符"
        type="textarea"
        :autosize="{ minRows: 4, maxRows: 6 }"
        :maxlength="800" />
    </div>
  </div>
</template>
