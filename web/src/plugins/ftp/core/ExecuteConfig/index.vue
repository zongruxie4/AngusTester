<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';
import { IconRequired, ExecSettingForm } from '@xcan-angus/vue-ui';
import SelectEnum from '@/components/enum/SelectEnum.vue';
import { useI18n } from 'vue-i18n';

import { ScenarioConfig } from '../PropsType';

export interface Props {
  value:ScenarioConfig['script'];
  excludes: (data: { label: string; value: string; }) => boolean;
}

const props = withDefaults(defineProps<Props>(), {
  value: undefined,
  excludes: undefined
});

const { t } = useI18n();

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'scriptTypeChange', value:string);
}>();

const formRef = ref();
const scriptType = ref<string>();
const scriptTypeError = ref(false);

const scriptTypeChange = (value: string) => {
  scriptType.value = value;
  scriptTypeError.value = false;
  setTimeout(() => {
    emit('scriptTypeChange', value);
  }, 16.67);
};

onMounted(() => {
  watch(() => props.value, (newValue) => {
    scriptTypeError.value = false;
    if (!newValue) {
      return;
    }

    scriptType.value = newValue.type;
  }, { immediate: true, deep: false });
});

const isValid = async () => {
  let errorNum = 0;
  if (!scriptType.value) {
    errorNum++;
    scriptTypeError.value = true;
  }

  const configValidData = await formRef.value.isValid();
  if (!configValidData.valid) {
    errorNum++;
  }

  return !errorNum;
};

const getData = () => {
  const data = formRef.value.getData();
  return {
    ...data,
    type: scriptType.value
  };
};

defineExpose({ isValid, getData });
</script>

<template>
  <div>
    <div class="flex items-center pl-12 pr-5 mt-5 space-y-0.5">
      <div class="flex items-center w-35 mr-2.5">
        <IconRequired />
        <span>{{ t('ftpPlugin.executeConfig.scriptType') }}</span>
      </div>
      <SelectEnum
        :value="scriptType"
        :error="scriptTypeError"
        :excludes="props.excludes"
        defaultActiveFirstOption
        enumKey="ScriptType"
        class="w-104"
        @change="scriptTypeChange" />
    </div>
    <ExecSettingForm
      ref="formRef"
      class="p-5"
      isDeep
      style="max-width: 1440px;"
      :scriptType="scriptType"
      :scriptInfo="props.value" />
  </div>
</template>
