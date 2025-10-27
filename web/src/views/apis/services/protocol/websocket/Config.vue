<script lang="ts" setup>
import { onMounted, reactive, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Form, FormItem } from 'ant-design-vue';
import { Input } from '@xcan-angus/vue-ui';

interface Props {
  id: string;
  value: Record<string, string>;
}

const props = withDefaults(defineProps<Props>(), {
  id: '',
  value: () => ({})
});

/**
 * Emits configuration updates to parent component
 */
const emits = defineEmits<{(e: 'update:value', value: Record<string, string>): void}>();

const { t } = useI18n();

/**
 * WebSocket configuration form data with default values
 * <p>
 * Contains connection timeout, maximum reconnection attempts, and reconnection interval settings
 * </p>
 */
const webSocketConfig = reactive({
  connectTimeout: '60000',
  maxReconnections: '0',
  reconnectionInterval: '1000'
});

const formRef = ref();

/**
 * Watches for form changes and emits updates to parent component
 * <p>
 * Uses deep watching to detect changes in nested object properties
 * </p>
 */
watch(() => webSocketConfig, () => {
  emits('update:value', webSocketConfig);
}, {
  deep: true
});

/**
 * Initializes form with provided values or defaults
 * <p>
 * Called when component is mounted to set initial configuration values
 * </p>
 */
onMounted(() => {
  webSocketConfig.connectTimeout = props.value?.connectTimeout || '60000';
  webSocketConfig.maxReconnections = props.value?.maxReconnections || '0';
  webSocketConfig.reconnectionInterval = props.value?.reconnectionInterval || '1000';
});

/**
 * Form validation rules for WebSocket configuration
 * <p>
 * Defines required field validation with appropriate error messages
 * </p>
 */
const validationRules: any = {
  connectTimeout: [{
    required: true,
    message: t('service.webSocketSetting.validation.connectTimeout'),
    trigger: 'blur'
  }],
  maxReconnections: [{
    required: true,
    message: t('service.webSocketSetting.validation.maxReconnections'),
    trigger: 'change'
  }],
  reconnectionInterval: [{
    required: true,
    message: t('service.webSocketSetting.validation.reconnectionInterval'),
    trigger: 'change'
  }]
};
</script>
<template>
  <Form
    ref="formRef"
    :model="webSocketConfig"
    labelAlign="left"
    layout="vertical"
    :labelCol="{span: 22}"
    :rules="validationRules">
    <FormItem name="connectTimeout">
      <template #label>
        <p class="text-3">{{ t('service.webSocketSetting.form.connectTimeout.label') }}<span class="text-gray-text">({{ t('service.webSocketSetting.form.connectTimeout.description') }})</span></p>
      </template>
      <Input
        v-model:value="webSocketConfig.connectTimeout"
        :maxlength="40"
        :allowClear="false"
        dataType="number"
        class="rounded w-80"
        :placeholder="t('service.webSocketSetting.form.connectTimeout.placeholder')">
        <template #suffix>
          <span>ms</span>
        </template>
      </Input>
    </FormItem>
    <FormItem name="maxReconnections">
      <template #label>
        <p class="text-3">{{ t('service.webSocketSetting.form.maxReconnections.label') }}<span class="text-gray-text">({{ t('service.webSocketSetting.form.maxReconnections.description') }})</span></p>
      </template>
      <Input
        v-model:value="webSocketConfig.maxReconnections"
        :max="10"
        :min="0"
        :allowClear="false"
        dataType="number"
        class="rounded  w-80"
        :placeholder="t('service.webSocketSetting.form.maxReconnections.placeholder')" />
    </FormItem>
    <FormItem name="reconnectionInterval">
      <template #label>
        <p class="text-3">{{ t('service.webSocketSetting.form.reconnectionInterval.label') }}<span class="text-gray-text">({{ t('service.webSocketSetting.form.reconnectionInterval.description') }})</span></p>
      </template>
      <Input
        v-model:value="webSocketConfig.reconnectionInterval"
        class="rounded-border w-80"
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
