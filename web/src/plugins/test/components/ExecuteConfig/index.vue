
<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { IconRequired, SelectEnum } from '@xcan-angus/vue-ui';
import ExecSettingForm from '@/components/exec/ExecSettingForm/index.vue';
import { ScriptType } from '@xcan-angus/infra';

import { ScenarioConfig } from '@/plugins/test/types/index';

// Initialize i18n for internationalization
const { t } = useI18n();

/**
 * Filter function type for enum options
 * Accepts data with either 'label' or 'message' property
 */
type EnumFilterFunction = (data: { label?: string; message?: string; value: string; }) => boolean;

/**
 * Component props interface
 */
export interface Props {
  value?: ScenarioConfig['script'];  // Script configuration data
  excludes?: EnumFilterFunction;     // Filter function to exclude specific script types
}

// Define props with default values
const props = withDefaults(defineProps<Props>(), {
  value: undefined,
  excludes: undefined
});

/**
 * Event emitters
 */
const emit = defineEmits<{
  (e: 'scriptTypeChange', value: string): void;
}>();

/**
 * Form and state reactive references
 */
const formRef = ref();                    // Reference to ExecSettingForm component
const scriptType = ref<string>();         // Selected script type
const scriptTypeError = ref(false);       // Script type validation error state

/**
 * Handle script type selection change
 * Updates local state, clears error, and emits change event with slight delay
 * for smooth UI updates
 *
 * @param value - Selected script type value
 */
const scriptTypeChange = (value: string): void => {
  scriptType.value = value;
  scriptTypeError.value = false;

  // Emit change event after ~1 frame delay (16.67ms) for smoother UI updates
  setTimeout(() => {
    emit('scriptTypeChange', value);
  }, 16.67);
};

/**
 * Component lifecycle hook
 * Sets up watcher for prop value changes to sync script type
 */
onMounted(() => {
  // Watch for value prop changes and update script type
  watch(() => props.value, (newValue) => {
    scriptTypeError.value = false;

    if (!newValue) {
      return;
    }

    scriptType.value = newValue.type;
  }, {
    immediate: true,  // Execute immediately on mount
    deep: false       // Shallow watch is sufficient
  });
});

/**
 * Validate form data
 * Checks both script type selection and execution settings form validity
 *
 * @returns Promise that resolves to true if valid, false otherwise
 */
const isValid = async (): Promise<boolean> => {
  let errorNum = 0;

  // Validate script type is selected
  if (!scriptType.value) {
    errorNum++;
    scriptTypeError.value = true;
  }

  // Validate execution settings form
  const configValidData = await formRef.value.isValid();
  if (!configValidData.valid) {
    errorNum++;
  }

  return !errorNum;
};

/**
 * Get complete configuration data
 * Combines script type with execution settings form data
 *
 * @returns Complete script configuration object
 */
const getData = () => {
  const data = formRef.value.getData();
  return {
    ...data,
    type: scriptType.value
  };
};

/**
 * Expose methods for parent component access
 */
defineExpose({ isValid, getData });
</script>

<template>
  <!-- Main configuration container -->
  <div>
    <!-- Script type selection section -->
    <div class="flex items-center pl-12 pr-5 mt-5 space-y-0.5">
      <!-- Label with required indicator -->
      <div class="flex items-center w-35 mr-2.5">
        <IconRequired />
        <span>{{ t('common.scriptType') }}</span>
      </div>

      <!-- Script type enum selector -->
      <SelectEnum
        :value="scriptType"
        :error="scriptTypeError"
        :excludes="props.excludes"
        defaultActiveFirstOption
        :enumKey="ScriptType"
        class="w-104"
        @change="scriptTypeChange" />
    </div>

    <!-- Execution settings form (dynamic based on script type) -->
    <ExecSettingForm
      ref="formRef"
      class="p-5"
      isDeep
      style="max-width: 1440px;"
      :scriptType="scriptType || ''"
      :scriptInfo="props.value" />
  </div>
</template>
