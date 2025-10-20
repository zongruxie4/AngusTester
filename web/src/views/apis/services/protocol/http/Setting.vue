<script setup lang="ts">
import { Switch } from 'ant-design-vue';
import { Input } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import { RequestSetting } from '@/views/apis/services/protocol/http/types';

interface Props {
  value: RequestSetting;
}

const props = withDefaults(defineProps<Props>(), {
  value: () => ({
    enableParamValidation: false,
    connectTimeout: 6000,
    readTimeout: 60000,
    retryNum: 0,
    maxRedirects: 1
  })
});

const { t } = useI18n();

const emit = defineEmits<{
  (e: 'change', value: Props['value']): void;
}>();

/**
 * Handles setting value change
 * <p>
 * Updates the setting value when input changes
 * </p>
 * @param event - Change event from input
 * @param key - Setting key to update
 */
const handleSettingChange = (event: ChangeEvent, key: keyof RequestSetting) => {
  let newValue = (event.target as HTMLInputElement).value;
  if (!newValue && ['connectTimeout', 'retryNum', 'readTimeout'].includes(key)) {
    newValue = '0';
  }
  emit('change', { ...props.value, [key]: newValue });
};

/**
 * Handles input blur event
 * <p>
 * Sets default value to 0 if input is empty
 * </p>
 * @param key - Setting key to update
 */
const handleInputBlur = (key: keyof RequestSetting) => {
  if (!props.value[key]) {
    emit('change', { ...props.value, [key]: 0 });
  }
};
</script>
<template>
  <div class="leading-5 text-3 space-y-5">
    <div class="flex items-start">
      <div class="flex-1">
        <div class="leading-7 text-text-title">{{ t('service.apiSetting.labels.paramValidation') }}</div>
        <div class="mt-1 text-text-sub-content">{{ t('service.apiSetting.descriptions.paramValidation') }}</div>
      </div>
      <div class="flex-shrink-0 w-55 ml-10">
        <Switch
          :checked="props.value.enableParamValidation"
          class="w-8.5"
          size="small"
          @change="handleSettingChange({target: {value: $event}} as ChangeEvent, 'enableParamValidation')" />
      </div>
    </div>
    <div class="flex items-start">
      <div class="flex-1">
        <div class="leading-7 text-text-title">{{ t('service.apiSetting.labels.connectTimeout') }}</div>
        <div class="mt-1 text-text-sub-content">{{ t('service.apiSetting.descriptions.connectTimeout') }}</div>
      </div>
      <div class="flex-shrink-0 w-55 ml-10">
        <Input
          class="w-20"
          :maxlength="16"
          :min="0"
          :max="9007199254740991"
          :value="props.value.connectTimeout"
          :placeholder="t('service.apiSetting.form.timeoutPlaceholder')"
          dataType="number"
          @blur="handleInputBlur('connectTimeout')"
          @change="handleSettingChange($event, 'connectTimeout')" /><span
            class="ml-2">{{ t('service.apiSetting.units.milliseconds') }}</span>
      </div>
    </div>
    <div class="flex items-start">
      <div class="flex-1">
        <div class="leading-7 text-text-title">{{ t('service.apiSetting.labels.readTimeout') }}</div>
        <div class="mt-1 text-text-sub-content">{{ t('service.apiSetting.descriptions.readTimeout') }}</div>
      </div>
      <div class="flex-shrink-0 w-55 ml-10">
        <Input
          class="w-20"
          :maxlength="16"
          :min="0"
          :max="9007199254740991"
          :value="props.value.readTimeout"
          :placeholder="t('service.apiSetting.form.timeoutPlaceholder')"
          dataType="number"
          @blur="handleInputBlur('readTimeout')"
          @change="handleSettingChange($event, 'readTimeout')" /><span
            class="ml-2">{{ t('service.apiSetting.units.milliseconds') }}</span>
      </div>
    </div>
    <div class="flex items-start">
      <div class="flex-1">
        <div class="leading-7 text-text-title">{{ t('service.apiSetting.labels.retryNum') }}</div>
        <div class="mt-1 text-text-sub-content">{{ t('service.apiSetting.descriptions.retryNum') }}</div>
      </div>
      <div class="flex-shrink-0 w-55 ml-10">
        <Input
          class="w-20"
          :maxlength="1"
          :min="0"
          :max="6"
          :value="props.value.retryNum"
          :placeholder="t('service.apiSetting.form.retryNumPlaceholder')"
          dataType="number"
          @blur="handleInputBlur('retryNum')"
          @change="handleSettingChange($event, 'retryNum')" /><span class="ml-2">{{ t('service.apiSetting.units.times') }}</span>
      </div>
    </div>
    <div class="flex items-start">
      <div class="flex-1">
        <div class="leading-7 text-text-title">{{ t('service.apiSetting.labels.maxRedirects') }}</div>
        <div class="mt-1 text-text-sub-content">{{ t('service.apiSetting.descriptions.maxRedirects') }}</div>
      </div>
      <div class="flex-shrink-0 w-55 ml-10">
        <Input
          class="w-20"
          :maxlength="2"
          :min="0"
          :max="10"
          :value="props.value.maxRedirects"
          :placeholder="t('service.apiSetting.form.maxRedirectsPlaceholder')"
          dataType="number"
          @blur="handleInputBlur('maxRedirects')"
          @change="handleSettingChange($event, 'maxRedirects')" /><span class="ml-2">{{ t('service.apiSetting.units.times') }}</span>
      </div>
    </div>
  </div>
</template>
