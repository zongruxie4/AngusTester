<script lang="ts" setup>
import { onMounted, reactive, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Form, FormItem } from 'ant-design-vue';
import { Input } from '@xcan-angus/vue-ui';

interface Props {
  id: string;
  value: Record<string, string>;
}

const { t } = useI18n();
const props = withDefaults(defineProps<Props>(), {
  id: '',
  value: () => ({})
});

const emits = defineEmits<{(e: 'update:value', value: Record<string, string>): void}>();
const form = reactive({
  connectTimeout: '60000',
  maxReconnections: '0',
  reconnectionInterval: '200'
});
const formRef = ref();
const rules = {
  connectTimeout: [{
    required: true, message: t('service.webSocketSetting.validation.connectTimeout'), trigger: 'blur'
  }],
  maxReconnections: [{
    required: true, message: t('service.webSocketSetting.validation.maxReconnections'), trigger: 'change'
  }],
  reconnectionInterval: [{
    required: true, message: t('service.webSocketSetting.validation.reconnectionInterval'), trigger: 'change'
  }]
};

watch(() => form, () => {
  emits('update:value', form);
}, {
  deep: true
});

onMounted(() => {
  form.connectTimeout = props.value?.connectTimeout || '0';
  form.maxReconnections = props.value?.maxReconnections || '0';
  form.reconnectionInterval = props.value?.reconnectionInterval || '1000';
});

</script>
<template>
  <Form
    ref="formRef"
    :model="form"
    labelAlign="left"
    :labelCol="{span: 22}"
    :rules="rules">
    <FormItem name="connectTimeout">
      <template #label>
        <p class="text-3">{{ t('common.description') }})</span></p>
      </template>
      <Input
        v-model:value="form.connectTimeout"
        :maxlength="40"
        :allowClear="false"
        dataType="number"
        class="rounded"
        :placeholder="t('service.webSocketSetting.form.connectTimeout.placeholder')">
        <template #suffix>
          <span>ms</span>
        </template>
      </Input>
    </FormItem>
    <FormItem name="maxReconnections">
      <template #label>
        <p class="text-3">{{ t('common.description') }})</span></p>
      </template>
      <Input
        v-model:value="form.maxReconnections"
        :max="10"
        :min="0"
        :allowClear="false"
        dataType="number"
        class="rounded"
        :placeholder="t('service.webSocketSetting.form.maxReconnections.placeholder')" />
    </FormItem>
    <FormItem name="reconnectionInterval">
      <template #label>
        <p class="text-3">{{ t('common.description') }})</span></p>
      </template>
      <Input
        v-model:value="form.reconnectionInterval"
        class="rounded-border"
        dataType="number"
        :placeholder="t('service.webSocketSetting.form.reconnectionInterval.placeholder')"
        :allowClear="false">
        <template #suffix>
          <span>ms</span>
        </template>
      </Input>
    </FormItem>
  </Form>
</template>
