<script setup lang="ts">
import { Switch } from 'ant-design-vue';
import { Input } from '@xcan-angus/vue-ui';

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
        <div class="leading-7 text-text-title">参数格式验证</div>
        <div class="mt-1 text-text-sub-content">启用后会先验证参数，只有在参数验证通过后才会发送请求，默认未开启。</div>
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
        <div class="leading-7 text-text-title">连接超时</div>
        <div class="mt-1 text-text-sub-content">指定客户端和服务器建立连接的最长等待时间，0为永不超时，默认为6秒。</div>
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
        <div class="leading-7 text-text-title">读超时</div>
        <div class="mt-1 text-text-sub-content">指定建立连接后客户端未收到服务器关闭连接的最长等待时间，0为永不超时，默认为60秒。</div>
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
        <div class="leading-7 text-text-title">失败时重试次数</div>
        <div class="mt-1 text-text-sub-content">请求失败时的重试次数。默认不重试，最多允许重试6次。</div>
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
          @change="change($event, 'retryNum')" /><span class="ml-2">次</span>
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
        <div class="leading-7 text-text-title">最大重定向次数</div>
        <div class="mt-1 text-text-sub-content">请求返回3xx状态码时的重定向次数。默认1次，最多允许10次。</div>
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
          @change="change($event, 'maxRedirects')" /><span class="ml-2">次</span>
      </div>
    </div>
  </div>
</template>
