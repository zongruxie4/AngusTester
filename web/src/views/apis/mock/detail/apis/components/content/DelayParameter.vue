<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Input, Select, Validate } from '@xcan-angus/vue-ui';
import { ResponseDelayMode, utils } from '@xcan-angus/infra';

import { DelayData } from './types';

interface Props {
  value: DelayData;
}

const props = withDefaults(defineProps<Props>(), {
  value: undefined
});

const { t } = useI18n();

const delayMode = ref<ResponseDelayMode>(ResponseDelayMode.NONE);
const fixedTime = ref<string>();
const fixedTimeError = ref(false);
const maxRandomTime = ref<string>();
const maxRandomTimeError = ref(false);
const maxRandomTimeErrorMessage = ref<string>();
const minRandomTime = ref<string>();
const minRandomTimeError = ref(false);

const delayChange = (value: ResponseDelayMode) => {
  delayMode.value = value;
  fixedTime.value = undefined;
  fixedTimeError.value = false;
  maxRandomTime.value = undefined;
  maxRandomTimeError.value = false;
  maxRandomTimeErrorMessage.value = undefined;
  minRandomTime.value = undefined;
  minRandomTimeError.value = false;
};

const fixedTimeChange = (event: { target: { value: string } }) => {
  const value = event.target.value;
  fixedTime.value = value;
  fixedTimeError.value = !value;
};

const maxRandomTimeChange = (event: { target: { value: string } }) => {
  const value = event.target.value;
  maxRandomTime.value = value;
  maxRandomTimeError.value = !value;
  validateMaxRandomTime();
};

const minRandomTimeChange = (event: { target: { value: string } }) => {
  const value = event.target.value;
  minRandomTime.value = value;
  minRandomTimeError.value = !value;
  validateMaxRandomTime();
};

const validateMaxRandomTime = () => {
  maxRandomTimeErrorMessage.value = undefined;
  if (!utils.isEmpty(maxRandomTime.value) && !utils.isEmpty(minRandomTime.value)) {
    if (+maxRandomTime.value! < +minRandomTime.value!) {
      maxRandomTimeError.value = true;
      maxRandomTimeErrorMessage.value = t('mock.detail.apis.components.delayParameter.maxLessThanMin');
    }
  }
};

const reset = () => {
  delayMode.value = ResponseDelayMode.NONE;
  fixedTime.value = undefined;
  fixedTimeError.value = false;
  maxRandomTime.value = undefined;
  maxRandomTimeError.value = false;
  maxRandomTimeErrorMessage.value = undefined;
  minRandomTime.value = undefined;
  minRandomTimeError.value = false;
};

onMounted(() => {
  watch(() => props.value, (newValue) => {
    reset();
    if (!newValue) {
      return;
    }

    const {
      mode,
      fixedTime: _fixedTime,
      maxRandomTime: _maxRandomTime,
      minRandomTime: _minRandomTime
    } = newValue;
    delayMode.value = mode;
    if (mode === ResponseDelayMode.NONE) {
      return;
    }

    if (mode === ResponseDelayMode.RANDOM) {
      if (_minRandomTime) {
        minRandomTime.value = _minRandomTime.replace(/ms/g, '');
      }
      if (_maxRandomTime) {
        maxRandomTime.value = _maxRandomTime.replace(/ms/g, '');
      }
      return;
    }

    if (_fixedTime) {
      fixedTime.value = _fixedTime.replace(/ms/g, '');
    }
  }, { immediate: true });
});

const showFixedTimeInput = computed(() => {
  return delayMode.value === ResponseDelayMode.FIXED;
});

const showRandomTimeInput = computed(() => {
  return delayMode.value === ResponseDelayMode.RANDOM;
});

const delayModeOptions: { label: string; value: ResponseDelayMode }[] = [
  { label: t('mock.detail.apis.components.delayParameter.noDelay'), value: ResponseDelayMode.NONE },
  { label: t('mock.detail.apis.components.delayParameter.fixedDelay'), value: ResponseDelayMode.FIXED },
  { label: t('mock.detail.apis.components.delayParameter.randomDelay'), value: ResponseDelayMode.RANDOM }
];

defineExpose({
  getData: (): DelayData => {
    const mode = delayMode.value;
    if (mode === ResponseDelayMode.NONE) {
      return { mode };
    }

    if (mode === ResponseDelayMode.FIXED) {
      return {
        mode,
        fixedTime: (fixedTime.value || '') + 'ms'
      };
    }

    return {
      mode,
      maxRandomTime: (maxRandomTime.value || '') + 'ms',
      minRandomTime: (minRandomTime.value || '') + 'ms'
    };
  },
  isValid: (): boolean => {
    if (delayMode.value === ResponseDelayMode.NONE) {
      return true;
    }

    if (delayMode.value === ResponseDelayMode.FIXED) {
      if (!fixedTimeError.value) {
        fixedTimeError.value = !fixedTime.value;
      }
      return !fixedTimeError.value;
    }

    if (!maxRandomTimeError.value) {
      maxRandomTimeError.value = !maxRandomTime.value;
      validateMaxRandomTime();
    }
    if (!minRandomTimeError.value) {
      minRandomTimeError.value = !minRandomTime.value;
    }
    return !minRandomTimeError.value && !maxRandomTimeError.value;
  }
});
</script>

<template>
  <div class="flex items-center space-x-4">
    <Select
      v-model:value="delayMode"
      :options="delayModeOptions"
      class="w-30"
      @change="delayChange" />
    <div v-if="showFixedTimeInput" class="flex items-center space-x-2">
      <Input
        :value="fixedTime"
        :maxlength="7"
        :min="0"
        :max="7200000"
        :error="fixedTimeError"
        trimAll
        class="w-30"
        dataType="number"
        placeholder="0 ~ 7200000"
        @change="fixedTimeChange" />
      <div class="text-text-sub-content">ms</div>
    </div>
    <div v-if="showRandomTimeInput" class="flex items-center space-x-2">
      <Input
        :value="minRandomTime"
        :maxlength="7"
        :min="0"
        :max="7200000"
        :error="minRandomTimeError"
        trimAll
        class="w-30"
        dataType="number"
        placeholder="0 ~ 7200000"
        @change="minRandomTimeChange" />
      <div>{{ t('mock.detail.apis.components.delayParameter.to') }}</div>
      <Validate
        :text="maxRandomTimeErrorMessage"
        fixed
        class="w-30">
        <Input
          :value="maxRandomTime"
          :maxlength="7"
          :min="0"
          :max="7200000"
          :error="maxRandomTimeError"
          trimAll
          dataType="number"
          placeholder="0 ~ 7200000"
          @change="maxRandomTimeChange" />
      </Validate>
      <div>ms</div>
    </div>
  </div>
</template>
