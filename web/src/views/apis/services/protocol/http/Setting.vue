<script setup lang="ts">
import { Switch } from 'ant-design-vue';
import { Input } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';

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
const { t } = useI18n();
const props = withDefaults(defineProps<Props>(), {
  value: () => ({ enableParamValidation: false, connectTimeout: 6000, readTimeout: 60000, retryNum: 0, maxRedirects: 1 })
});


const emit = defineEmits<{
  (e: 'change', value: Props['value']): void;
}>();

const change = (event:ChangeEvent, key: keyof Setting) => {
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
          @change="change({target: {value: $event}} as ChangeEvent, 'enableParamValidation')" />
      </div>
    </div>
    <!-- <div class="flex items-start">
      <div class="flex-1">
        <div class="leading-7 text-text-title">允许修改引用数据模型</div>
        <div class="mt-1 text-text-sub-content">
          启用后修改引用模型($ref)将同步修改引用模型模板。
          如果引用模型模板被其他接口使用，其他接口也会被间接修改；
          未启用时，修改数据模型会生成副本并删除与原数据模型引用关系，这将引起生成OpenAPI文档和代码中冗余增多。
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
          @blur="onBlur('connectTimeout')"
          @change="change($event, 'connectTimeout')" /><span
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
          @blur="onBlur('readTimeout')"
          @change="change($event, 'readTimeout')" /><span
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
          @blur="onBlur('retryNum')"
          @change="change($event, 'retryNum')" /><span class="ml-2">{{ t('service.apiSetting.units.times') }}</span>
      </div>
    </div>
    <!-- <div class="flex items-start">
      <div class="flex-1">
        <div class="leading-7 text-text-title">重试间隔</div>
        <div class="mt-1 text-text-sub-content">请求失败时的重试间隔。默认200毫秒，最大允许30分钟。</div>
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
          @blur="onBlur('maxRedirects')"
          @change="change($event, 'maxRedirects')" /><span class="ml-2">{{ t('service.apiSetting.units.times') }}</span>
      </div>
    </div>
  </div>
</template>
