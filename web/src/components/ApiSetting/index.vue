<script setup lang="ts">
import { nextTick, onMounted } from 'vue';
import { Switch } from 'ant-design-vue';
import { Input } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
const { t }  = useI18n();

interface Setting {
  enableParamValidation: boolean;
  connectTimeout: number;
  readTimeout: number;
  maxRedirects: number;
  retryNum: number;
}

interface Props {
  value: Setting;
}

const props = withDefaults(defineProps<Props>(), {
  value: () => ({ enableParamValidation: false, connectTimeout: 6000, readTimeout: 60000, retryNum: 0, maxRedirects: 1 })
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'change', value: Props['value']): void;
  (e: 'rendered', value: true);
}>();

const change = (event, key: keyof Setting) => {
  let value = event.target.value;
  if (!value && ['connectTimeout', 'retryNum', 'readTimeout'].includes(key)) {
    value = 0;
  }
  emit('change', { ...props.value, [key]: value });
};

const onBlur = (key) => {
  if (!props.value[key]) {
    emit('change', { ...props.value, [key]: 0 });
  }
};

onMounted(() => {
  nextTick(() => {
    emit('rendered', true);
  });
});
</script>
<template>
  <div class="leading-5 text-3 space-y-5">
    <div class="flex items-start">
      <div class="flex-1">
        <div class="leading-7 text-theme-title">{{ t('xcan_apiSetting.parameterFormatValidation') }}</div>
        <div class="mt-1 text-theme-sub-content">{{ t('xcan_apiSetting.parameterFormatValidationDesc') }}</div>
      </div>
      <div class="flex-shrink-0 w-55 ml-10">
        <Switch
          :checked="props.value.enableParamValidation"
          class="w-8.5"
          size="small"
          @change="change({target: {value: $event}}, 'enableParamValidation')" />
      </div>
    </div>
    <!-- <div class="flex items-start">
      <div class="flex-1">
        <div class="leading-7 text-theme-title">{{ t('xcan_apiSetting.allowModifyReferencedDataModel') }}</div>
        <div class="mt-1 text-theme-sub-content">
          {{ t('xcan_apiSetting.allowModifyReferencedDataModelDesc1') }}
          {{ t('xcan_apiSetting.allowModifyReferencedDataModelDesc2') }}
          {{ t('xcan_apiSetting.allowModifyReferencedDataModelDesc3') }}
          默认未开启。注意：未单击“保存”修改将不会生效。
        </div>
      </div>
      <div class="flex-shrink-0 w-55 ml-10">
        <Switch
          :checked="props.value.modifyReferencedModel"
          class="w-8.5"
          size="small"
          @change="change($event, 'modifyReferencedModel')" />
      </div>
    </div> -->
    <div class="flex items-start">
      <div class="flex-1">
        <div class="leading-7 text-theme-title">{{ t('xcan_apiSetting.connectionTimeout') }}</div>
        <div class="mt-1 text-theme-sub-content">{{ t('xcan_apiSetting.connectionTimeoutDesc') }}</div>
      </div>
      <div class="flex-shrink-0 w-55 ml-10">
        <Input
          class="w-20"
          :maxlength="16"
          :min="0"
          :max="9007199254740991"
          :value="props.value.connectTimeout"
          placeholder="0 ~ 9007199254740991"
          dataType="number"
          @blur="onBlur('connectTimeout')"
          @change="change($event, 'connectTimeout')" /><span
            class="ml-2">ms</span>
      </div>
    </div>
    <div class="flex items-start">
      <div class="flex-1">
        <div class="leading-7 text-theme-title">{{ t('xcan_apiSetting.readTimeout') }}</div>
        <div class="mt-1 text-theme-sub-content">{{ t('xcan_apiSetting.readTimeoutDesc') }}</div>
      </div>
      <div class="flex-shrink-0 w-55 ml-10">
        <Input
          class="w-20"
          :maxlength="16"
          :min="0"
          :max="9007199254740991"
          :value="props.value.readTimeout"
          placeholder="0 ~ 9007199254740991"
          dataType="number"
          @blur="onBlur('readTimeout')"
          @change="change($event, 'readTimeout')" /><span
            class="ml-2">ms</span>
      </div>
    </div>
    <div class="flex items-start">
      <div class="flex-1">
        <div class="leading-7 text-theme-title">{{ t('xcan_apiSetting.retryCountOnFailure') }}</div>
        <div class="mt-1 text-theme-sub-content">{{ t('xcan_apiSetting.retryCountOnFailureDesc') }}</div>
      </div>
      <div class="flex-shrink-0 w-55 ml-10">
        <Input
          class="w-20"
          :maxlength="1"
          :min="0"
          :max="6"
          :value="props.value.retryNum"
          placeholder="0 ~ 6"
          dataType="number"
          @blur="onBlur('retryNum')"
          @change="change($event, 'retryNum')" /><span class="ml-2">{{ t('xcan_apiSetting.times') }}</span>
      </div>
    </div>
    <!-- <div class="flex items-start">
      <div class="flex-1">
        <div class="leading-7 text-theme-title">{{ t('xcan_apiSetting.retryInterval') }}</div>
        <div class="mt-1 text-theme-sub-content">{{ t('xcan_apiSetting.retryIntervalDesc') }}</div>
      </div>
      <div class="flex-shrink-0 w-55 ml-10">
        <Input
          class="w-20"
          :maxlength="7"
          :min="0"
          :max="1800000"
          :value="props.value.retryInterval"
          placeholder="0 ~ 1800000"
          dataType="number"
          @change="change($event, 'retryInterval')" /><span
            class="ml-2">ms</span>
      </div>
    </div> -->
    <div class="flex items-start">
      <div class="flex-1">
        <div class="leading-7 text-theme-title">{{ t('xcan_apiSetting.maxRedirects') }}</div>
        <div class="mt-1 text-theme-sub-content">{{ t('xcan_apiSetting.maxRedirectsDesc') }}</div>
      </div>
      <div class="flex-shrink-0 w-55 ml-10">
        <Input
          class="w-20"
          :maxlength="2"
          :min="0"
          :max="10"
          :value="props.value.maxRedirects"
          placeholder="0 ~ 10"
          dataType="number"
          @blur="onBlur('maxRedirects')"
          @change="change($event, 'maxRedirects')" /><span class="ml-2">{{ t('xcan_apiSetting.times') }}</span>
      </div>
    </div>
  </div>
</template>
