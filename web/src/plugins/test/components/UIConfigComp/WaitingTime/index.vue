<script setup lang="ts">
import { ref, onMounted, watch, watchEffect, computed } from 'vue';
import { useI18n } from 'vue-i18n';
import { Input, Select, Icon, Tooltip } from '@xcan-angus/vue-ui';
import { utils } from '@xcan-angus/infra';

import { WaitingTimeConfig } from './PropsType';
import ActionsGroup from '../ActionsGroup/index.vue';

// Initialize i18n for internationalization
const { t } = useI18n();

/**
 * Wait type definition
 */
type WaitType = 'fixed' | 'random';

/**
 * Component props interface
 */
export interface Props {
  value: WaitingTimeConfig;  // Waiting time configuration data
  repeatNames: string[];     // List of existing names to check for duplicates
  enabled?: boolean;         // Whether waiting time functionality is enabled
}

// Define props with default values
const props = withDefaults(defineProps<Props>(), {
  value: undefined,
  repeatNames: () => [],
  enabled: true
});

/**
 * Event emitters
 */
const emit = defineEmits<{
  (e: 'change', value: Omit<WaitingTimeConfig, 'id'>): void;  // Emit data changes
  (e: 'nameChange', value: string): void;                      // Emit name changes
  (e: 'click', value: 'delete' | 'clone'): void;              // Emit action clicks
  (e: 'enabledChange', value: boolean): void;                  // Emit enabled state changes
  (e: 'renderChange'): void;                                   // Emit render event
}>();

/**
 * Form field reactive references
 */
const enabled = ref(false);              // Whether this waiting time is enabled
const name = ref<string>();              // Waiting time name (must be unique)
const description = ref<string>();       // Optional description
const waitType = ref<WaitType>('fixed'); // Wait type: fixed or random
const maxWaitTimeInMs = ref<string>();   // Maximum wait time in milliseconds
const minWaitTimeInMs = ref<string>();   // Minimum wait time in milliseconds (random mode only)

/**
 * Validation error states
 */
const minWaitTimeInMsError = ref(false);  // Min wait time validation error
const maxWaitTimeInMsError = ref(false);  // Max wait time validation error
const nameError = ref(false);             // Name validation error
const nameRepeatFlag = ref(false);        // Name duplication flag

/**
 * Handle name field change
 * Clears validation errors and emits name change event
 * 
 * @param event - Input change event
 */
const nameChange = (event: any): void => {
  const value = event.target?.value;
  name.value = value;
  nameError.value = false;
  nameRepeatFlag.value = false;
  emit('nameChange', value);
};

/**
 * Handle wait type change (fixed/random)
 * Resets time fields and errors when switching modes
 * 
 * @param value - New wait type
 */
const waitTypeChange = (value: any): void => {
  waitType.value = value as WaitType;
  minWaitTimeInMs.value = undefined;
  minWaitTimeInMsError.value = false;
  maxWaitTimeInMs.value = undefined;
  maxWaitTimeInMsError.value = false;
};

/**
 * Handle max wait time field change
 * Clears validation error
 * 
 * @param event - Input change event
 */
const maxWaitTimeInMsChange = (event: any): void => {
  maxWaitTimeInMs.value = event.target?.value;
  maxWaitTimeInMsError.value = false;
};

/**
 * Handle max wait time field blur
 * Validates max time after user leaves the field
 * 
 * @param event - Input blur event
 */
const maxWaitTimeInMsBlur = (event: any): void => {
  maxWaitTimeInMs.value = event.target?.value;
  maxWaitTimeInMsError.value = false;
  validateMaxTime();
};

/**
 * Validate maximum wait time
 * Checks:
 * - Required for both modes
 * - Must be >= min time (in random mode)
 */
const validateMaxTime = (): void => {
  // Max time is required
  if (utils.isEmpty(maxWaitTimeInMs.value)) {
    maxWaitTimeInMsError.value = true;
    return;
  }

  // If no min time, max is valid
  if (utils.isEmpty(minWaitTimeInMs.value)) {
    maxWaitTimeInMsError.value = false;
    return;
  }

  // In random mode: max must be >= min
  if (+(minWaitTimeInMs.value || 0) > +(maxWaitTimeInMs.value || 0)) {
    maxWaitTimeInMsError.value = true;
    return;
  }

  maxWaitTimeInMsError.value = false;
};

/**
 * Handle min wait time field change
 * Clears validation error
 * 
 * @param event - Input change event
 */
const minWaitTimeInMsChange = (event: any): void => {
  minWaitTimeInMs.value = event.target?.value;
  minWaitTimeInMsError.value = false;
};

/**
 * Handle min wait time field blur
 * Validates min time after user leaves the field
 * 
 * @param event - Input blur event
 */
const minWaitTimeInMsBlur = (event: any): void => {
  const value = event.target?.value;
  minWaitTimeInMs.value = value;
  validateMinTime();
};

/**
 * Validate minimum wait time
 * Checks:
 * - Required in random mode
 * - Must be <= max time (in random mode)
 */
const validateMinTime = (): void => {
  // Min time is required in random mode
  if (utils.isEmpty(minWaitTimeInMs.value)) {
    minWaitTimeInMsError.value = true;
    return;
  }

  // If no max time, min is valid (max will be validated separately)
  if (utils.isEmpty(maxWaitTimeInMs.value)) {
    minWaitTimeInMsError.value = false;
    return;
  }

  // In random mode: min must be <= max
  if (+(minWaitTimeInMs.value || 0) > +(maxWaitTimeInMs.value || 0)) {
    maxWaitTimeInMsError.value = true;
    return;
  }

  minWaitTimeInMsError.value = false;
};

/**
 * Handle enabled state change
 * Updates local state and emits change event
 * 
 * @param _enabled - New enabled state
 */
const enabledChange = (_enabled: boolean): void => {
  enabled.value = _enabled;
  emit('enabledChange', _enabled);
};

/**
 * Handle action button click (clone or delete)
 * Emits click event to parent component
 * 
 * @param value - Action type
 */
const actionClick = (value: 'delete' | 'clone'): void => {
  emit('click', value);
};

/**
 * Initialize form data from props
 * Determines wait type based on whether min time is present
 * 
 * Logic:
 * - If only max time exists: Fixed wait mode
 * - If both min and max exist: Random wait mode
 */
const initializedData = (): void => {
  if (!props.value) {
    return;
  }

  const {
    maxWaitTimeInMs: _maxWaitTimeInMs,
    minWaitTimeInMs: _minWaitTimeInMs,
    name: _name,
    description: _description,
    enabled: _enabled
  } = props.value;

  // Determine wait type: if only max time, it's fixed; otherwise random
  if (!utils.isEmpty(_maxWaitTimeInMs) && (_minWaitTimeInMs === undefined || _minWaitTimeInMs === null)) {
    waitType.value = 'fixed';
  } else {
    waitType.value = 'random';
  }

  name.value = _name;
  description.value = _description;
  enabled.value = _enabled;
  maxWaitTimeInMs.value = _maxWaitTimeInMs;
  minWaitTimeInMs.value = _minWaitTimeInMs;
};

/**
 * Component mount lifecycle hook
 * Emits render event, initializes data, and sets up watchers
 */
onMounted(() => {
  // Emit render change event
  emit('renderChange');
  
  // Initialize form with prop data
  initializedData();
  
  /**
   * Watch for duplicate name changes
   * Marks name as error if it appears in repeatNames list
   */
  watch(() => props.repeatNames, (newValue) => {
    if (name.value && newValue.includes(name.value)) {
      nameError.value = true;
      nameRepeatFlag.value = true;
      return;
    }

    // Clear error if name was previously duplicate but no longer is
    if (nameRepeatFlag.value) {
      nameError.value = false;
      nameRepeatFlag.value = false;
    }
  });

  /**
   * Watch for any data changes and emit to parent
   */
  watchEffect(() => {
    const data = getData();
    emit('change', data);
  });
});

/**
 * Validate form fields
 * Checks for required name field, duplicate names, and time values
 * 
 * @returns true if all validations pass, false otherwise
 */
const isValid = (): boolean => {
  // Clear previous errors
  nameError.value = false;
  maxWaitTimeInMsError.value = false;
  minWaitTimeInMsError.value = false;
  nameRepeatFlag.value = false;
  
  let errorNum = 0;
  
  // Validate name (required and unique)
  if (!name.value) {
    errorNum++;
    nameError.value = true;
  } else {
    if (props.repeatNames.includes(name.value)) {
      nameError.value = true;
      nameRepeatFlag.value = true;
    }
  }
  
  // Validate max wait time (required for both modes)
  validateMaxTime();
  
  // Validate min wait time (only for random mode)
  if (waitType.value === 'random') {
    validateMinTime();
  }

  return !errorNum && !maxWaitTimeInMsError.value && !minWaitTimeInMsError.value;
};

/**
 * Get waiting time configuration data
 * Returns configuration object without ID
 * Includes minWaitTimeInMs only in random mode
 * 
 * @returns WaitingTime configuration (excluding ID)
 */
const getData = (): Omit<WaitingTimeConfig, 'id'> => {
  const data: Omit<WaitingTimeConfig, 'id'> = {
    beforeName: '',
    transactionName: '',
    description: description.value || '',
    enabled: enabled.value,
    maxWaitTimeInMs: maxWaitTimeInMs.value || '',
    name: name.value || '',
    target: 'WAITING_TIME'
  };

  // Include min time only in random mode
  if (waitType.value === 'random') {
    data.minWaitTimeInMs = minWaitTimeInMs.value;
  }

  return data;
};

/**
 * Expose methods for parent component access
 */
defineExpose({
  isValid,    // Validate form fields
  getData,    // Get configuration data
  
  /**
   * Get waiting time name
   * @returns Current name value
   */
  getName: (): string => {
    return name.value || '';
  },
  
  /**
   * Validate if name is duplicate
   * @param value - List of existing names to check against
   * @returns true if name is unique, false if duplicate
   */
  validateRepeatName: (value: string[]): boolean => {
    if (name.value && value.includes(name.value)) {
      nameError.value = true;
      nameRepeatFlag.value = true;
      return false;
    }

    return true;
  }
});

/**
 * Computed wait type options for select dropdown
 * Returns localized options for fixed and random wait modes
 */
const waitTypeoptions = computed(() => [
  { label: t('httpPlugin.uiConfig.waitingTime.fixedWait'), value: 'fixed' },
  { label: t('httpPlugin.uiConfig.waitingTime.randomWait'), value: 'random' }
]);
</script>

<template> 
  <div 
    :class="{ 'opacity-70': !enabled && props.enabled }" 
    class="h-11.5 flex items-center pl-9.5 pr-3 rounded bg-gray-light">
    <!-- Waiting time icon -->
    <Icon 
      class="flex-shrink-0 text-4 mr-3" 
      icon="icon-dengdaishijian" />
    
    <!-- Form fields container -->
    <div class="flex-1 flex items-center space-x-5 mr-5">
      <!-- Name field with duplicate name tooltip -->
      <Tooltip
        :title="t('httpPlugin.uiConfig.waitingTime.duplicateName')"
        internal
        placement="right"
        destroyTooltipOnHide
        :visible="nameRepeatFlag">
        <Input
          :value="name"
          :title="name"
          :maxlength="400"
          :error="nameError"
          trim
          class="time-name-input"
          :placeholder="t('common.placeholders.searchKeyword')"
          @change="nameChange" />
      </Tooltip>
      
      <!-- Wait type selector (fixed/random) -->
      <Select
        :value="waitType"
        :border="false"
        :options="waitTypeoptions"
        class="w-25 flex-shrink-0"
        @change="waitTypeChange" />
      
      <!-- Fixed wait mode: single time input -->
      <template v-if="waitType === 'fixed'">
        <div class="flex items-center space-x-2">
          <!-- Max wait time input (0-7200000 ms) -->
          <Input
            :value="maxWaitTimeInMs"
            :maxlength="7"
            :min="0"
            :max="7200000"
            :error="maxWaitTimeInMsError"
            trimAll
            dataType="number"
            class="w-25"
            placeholder="0 ~ 7200000"
            @change="maxWaitTimeInMsChange"
            @blur="maxWaitTimeInMsBlur" />
          <div>ms</div>
        </div>
      </template>
      
      <!-- Random wait mode: min and max time inputs -->
      <template v-else>
        <div class="flex items-center space-x-2">
          <!-- Min wait time input (0-7200000 ms) -->
          <Input
            :value="minWaitTimeInMs"
            :maxlength="7"
            :min="0"
            :max="7200000"
            :error="minWaitTimeInMsError"
            trimAll
            dataType="number"
            class="w-25"
            placeholder="0 ~ 7200000"
            @change="minWaitTimeInMsChange"
            @blur="minWaitTimeInMsBlur" />
          
          <!-- "to" separator -->
          <div>{{ t('httpPlugin.uiConfig.waitingTime.to') }}</div>
          
          <!-- Max wait time input (0-7200000 ms) -->
          <Input
            :value="maxWaitTimeInMs"
            :maxlength="7"
            :min="0"
            :max="7200000"
            :error="maxWaitTimeInMsError"
            trimAll
            dataType="number"
            class="w-25"
            placeholder="0 ~ 7200000"
            @change="maxWaitTimeInMsChange"
            @blur="maxWaitTimeInMsBlur" />
          <div>ms</div>
        </div>
      </template>
    </div>
    
    <!-- Action buttons: enable/disable, clone, delete -->
    <ActionsGroup
      v-model:enabled="enabled"
      :open="false"
      :arrowVisible="false"
      @enabledChange="enabledChange"
      @click="actionClick" />
  </div>
</template>

<style scoped>
.time-name-input {
  flex: 0 0 calc((100% - (124px))*2/5);
}

.child-drag-container .time-name-input {
  flex: 0 0 calc((100% - (134px))*2/5);
}

.bg-gray-light {
  background-color: rgba(239, 240, 243, 100%);
}

.ant-input-affix-wrapper {
  border-color: #fff;
  background-color: #fff;
}

.ant-input-affix-wrapper:not(.error):hover,
.ant-input-affix-wrapper:not(.error):focus,
.ant-input-affix-wrapper-focused:not(.error) {
  border-color: #40a9ff;
}

.ant-select :deep(.ant-select-selector) {
  border-color: #fff;
  background-color: #fff;
}
</style>
