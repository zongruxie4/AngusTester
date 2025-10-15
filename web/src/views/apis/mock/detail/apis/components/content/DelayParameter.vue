<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Input, Select, Validate } from '@xcan-angus/vue-ui';
import { ResponseDelayMode, utils } from '@xcan-angus/infra';

import { DelayData } from './types';

/**
 * <p>Props interface for DelayParameter component</p>
 * <p>Defines the structure of props passed to the component</p>
 */
interface Props {
  value: DelayData;
}

const props = withDefaults(defineProps<Props>(), {
  value: undefined
});

const { t } = useI18n();

/**
 * <p>Current delay mode selection</p>
 * <p>Controls which type of delay is being configured</p>
 */
const delayMode = ref<ResponseDelayMode>(ResponseDelayMode.NONE);

/**
 * <p>Fixed delay time value in milliseconds</p>
 * <p>Used when delay mode is set to FIXED</p>
 */
const fixedTime = ref<string>();

/**
 * <p>Error state for fixed time input validation</p>
 * <p>Indicates if the fixed time value is invalid</p>
 */
const fixedTimeError = ref(false);

/**
 * <p>Maximum random delay time value in milliseconds</p>
 * <p>Used when delay mode is set to RANDOM</p>
 */
const maxRandomTime = ref<string>();

/**
 * <p>Error state for maximum random time input validation</p>
 * <p>Indicates if the maximum random time value is invalid</p>
 */
const maxRandomTimeError = ref(false);

/**
 * <p>Error message for maximum random time validation</p>
 * <p>Displays specific validation error messages</p>
 */
const maxRandomTimeErrorMessage = ref<string>();

/**
 * <p>Minimum random delay time value in milliseconds</p>
 * <p>Used when delay mode is set to RANDOM</p>
 */
const minRandomTime = ref<string>();

/**
 * <p>Error state for minimum random time input validation</p>
 * <p>Indicates if the minimum random time value is invalid</p>
 */
const minRandomTimeError = ref(false);

/**
 * <p>Handles delay mode change event</p>
 * <p>Resets all form fields when delay mode changes</p>
 *
 * @param value - The selected delay mode
 */
const handleDelayModeChange = (value: ResponseDelayMode) => {
  delayMode.value = value;
  resetFormFields();
};

/**
 * <p>Handles fixed time input change event</p>
 * <p>Updates fixed time value and validates input</p>
 *
 * @param event - Input change event
 */
const handleFixedTimeChange = (event: { target: { value: string } }) => {
  const value = event.target.value;
  fixedTime.value = value;
  fixedTimeError.value = !value;
};

/**
 * <p>Handles maximum random time input change event</p>
 * <p>Updates maximum random time value and validates input</p>
 *
 * @param event - Input change event
 */
const handleMaxRandomTimeChange = (event: { target: { value: string } }) => {
  const value = event.target.value;
  maxRandomTime.value = value;
  maxRandomTimeError.value = !value;
  validateRandomTimeRange();
};

/**
 * <p>Handles minimum random time input change event</p>
 * <p>Updates minimum random time value and validates input</p>
 *
 * @param event - Input change event
 */
const handleMinRandomTimeChange = (event: { target: { value: string } }) => {
  const value = event.target.value;
  minRandomTime.value = value;
  minRandomTimeError.value = !value;
  validateRandomTimeRange();
};

/**
 * <p>Validates random time range</p>
 * <p>Ensures maximum time is greater than or equal to minimum time</p>
 */
const validateRandomTimeRange = () => {
  maxRandomTimeErrorMessage.value = undefined;
  if (!utils.isEmpty(maxRandomTime.value) && !utils.isEmpty(minRandomTime.value)) {
    if (+maxRandomTime.value! < +minRandomTime.value!) {
      maxRandomTimeError.value = true;
      maxRandomTimeErrorMessage.value = t('mock.detail.apis.components.delayParameter.maxLessThanMin');
    }
  }
};

/**
 * <p>Resets all form fields to initial state</p>
 * <p>Clears all input values and error states</p>
 */
const resetFormFields = () => {
  fixedTime.value = undefined;
  fixedTimeError.value = false;
  maxRandomTime.value = undefined;
  maxRandomTimeError.value = false;
  maxRandomTimeErrorMessage.value = undefined;
  minRandomTime.value = undefined;
  minRandomTimeError.value = false;
};

/**
 * <p>Resets entire component to initial state</p>
 * <p>Sets delay mode to NONE and clears all fields</p>
 */
const reset = () => {
  delayMode.value = ResponseDelayMode.NONE;
  resetFormFields();
};

/**
 * <p>Determines if fixed time input should be displayed</p>
 * <p>Returns true when delay mode is set to FIXED</p>
 */
const showFixedTimeInput = computed(() => {
  return delayMode.value === ResponseDelayMode.FIXED;
});

/**
 * <p>Determines if random time inputs should be displayed</p>
 * <p>Returns true when delay mode is set to RANDOM</p>
 */
const showRandomTimeInput = computed(() => {
  return delayMode.value === ResponseDelayMode.RANDOM;
});

/**
 * <p>Delay mode options for the select component</p>
 * <p>Provides available delay modes with localized labels</p>
 */
const delayModeOptions: { label: string; value: ResponseDelayMode }[] = [
  { label: t('mock.detail.apis.components.delayParameter.noDelay'), value: ResponseDelayMode.NONE },
  { label: t('mock.detail.apis.components.delayParameter.fixedDelay'), value: ResponseDelayMode.FIXED },
  { label: t('mock.detail.apis.components.delayParameter.randomDelay'), value: ResponseDelayMode.RANDOM }
];

/**
 * <p>Component mounted lifecycle hook</p>
 * <p>Sets up watchers and initializes component state</p>
 */
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

/**
 * <p>Exposes component methods and data to parent components</p>
 * <p>Provides getData and isValid methods for external access</p>
 */
defineExpose({
  /**
   * <p>Gets the current delay configuration data</p>
   * <p>Returns formatted delay data based on current mode</p>
   *
   * @returns DelayData object with current configuration
   */
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

  /**
   * <p>Validates the current form state</p>
   * <p>Checks if all required fields are valid based on delay mode</p>
   *
   * @returns true if form is valid, false otherwise
   */
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
      validateRandomTimeRange();
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
      @change="handleDelayModeChange" />
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
        @change="handleFixedTimeChange" />
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
        @change="handleMinRandomTimeChange" />
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
          @change="handleMaxRandomTimeChange" />
      </Validate>
      <div>ms</div>
    </div>
  </div>
</template>
