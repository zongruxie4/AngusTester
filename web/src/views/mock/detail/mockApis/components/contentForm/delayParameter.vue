<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { utils } from '@xcan-angus/tools';
import { Input, Select, Validate } from '@xcan-angus/vue-ui';

import { DelayData, DelayMode } from './PropsType';

interface Props {
  value: DelayData;
}

const props = withDefaults(defineProps<Props>(), {
  value: undefined
});

const delayMode = ref<DelayMode>('NONE');
const fixedTime = ref<string>();
const fixedTimeError = ref(false);
const maxRandomTime = ref<string>();
const maxRandomTimeError = ref(false);
const maxRandomTimeErrorMessage = ref<string>();
const minRandomTime = ref<string>();
const minRandomTimeError = ref(false);

const delayChange = (value: DelayMode) => {
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
      maxRandomTimeErrorMessage.value = '最大值小于最小值';
    }
  }
};

const reset = () => {
  delayMode.value = 'NONE';
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
    if (mode === 'NONE') {
      return;
    }

    if (mode === 'RANDOM') {
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
  return delayMode.value === 'FIXED';
});

const showRandomTimeInput = computed(() => {
  return delayMode.value === 'RANDOM';
});

const delayModeOptions: { label: string; value: DelayMode }[] = [
  { label: '无延迟', value: 'NONE' },
  { label: '固定延迟', value: 'FIXED' },
  { label: '随机延迟', value: 'RANDOM' }
];

defineExpose({
  getData: (): DelayData => {
    const mode = delayMode.value;
    if (mode === 'NONE') {
      return { mode };
    }

    if (mode === 'FIXED') {
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
    if (delayMode.value === 'NONE') {
      return true;
    }

    if (delayMode.value === 'FIXED') {
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
      <div>至</div>
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
