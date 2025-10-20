<script setup lang="ts">
import { ref, onMounted, watch, watchEffect } from 'vue';
import { useI18n } from 'vue-i18n';
import { Input, Icon, Tooltip } from '@xcan-angus/vue-ui';
import { utils } from '@xcan-angus/infra';

import { RendezvousConfig } from './PropsType';
import ActionsGroup from '../ActionsGroup/index.vue';

// Initialize i18n for internationalization
const { t } = useI18n();

/**
 * Component props interface
 */
export interface Props {
  value: RendezvousConfig;  // Rendezvous configuration data
  repeatNames: string[];    // List of existing names to check for duplicates
  enabled?: boolean;        // Whether rendezvous functionality is enabled
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
  (e: 'change', value: Omit<RendezvousConfig, 'id'>): void;  // Emit data changes
  (e: 'nameChange', value: string): void;                     // Emit name changes
  (e: 'click', value: 'delete' | 'clone'): void;             // Emit action clicks
  (e: 'enabledChange', value: boolean): void;                 // Emit enabled state changes
  (e: 'renderChange'): void;                                  // Emit render event
}>();

/**
 * Form field reactive references
 */
const enabled = ref(false);           // Whether this rendezvous is enabled
const name = ref<string>();           // Rendezvous name (must be unique)
const description = ref<string>();    // Optional description
const threads = ref<string>();        // Number of threads to wait for
const timeoutInMs = ref<string>();    // Timeout in milliseconds

/**
 * Validation error states
 */
const timeoutInMsError = ref(false);  // Timeout validation error
const threadsError = ref(false);      // Thread count validation error
const nameError = ref(false);         // Name validation error
const nameRepeatFlag = ref(false);    // Name duplication flag

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
 * Handle thread count field change
 * Clears validation error
 * 
 * @param event - Input change event
 */
const threadsChange = (event: any): void => {
  threads.value = event.target?.value;
  threadsError.value = false;
};

/**
 * Handle timeout field change
 * Clears validation error
 * 
 * @param event - Input change event
 */
const timeoutInMsChange = (event: any): void => {
  timeoutInMs.value = event.target?.value;
  timeoutInMsError.value = false;
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
 * Populates form fields with prop values
 */
const initializedData = (): void => {
  if (!props.value) {
    return;
  }

  const {
    name: _name,
    description: _description,
    enabled: _enabled,
    threads: _threads,
    timeoutInMs: _timeoutInMs
  } = props.value;
  
  name.value = _name;
  description.value = _description;
  enabled.value = _enabled;
  threads.value = _threads;
  timeoutInMs.value = _timeoutInMs;
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
 * Checks for required fields and duplicate names
 * 
 * @returns true if all validations pass, false otherwise
 */
const isValid = (): boolean => {
  // Clear previous errors
  nameError.value = false;
  threadsError.value = false;
  timeoutInMsError.value = false;
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

  // Validate threads (required)
  if (utils.isEmpty(threads.value)) {
    errorNum++;
    threadsError.value = true;
  }

  // Validate timeout (required)
  if (utils.isEmpty(timeoutInMs.value)) {
    errorNum++;
    timeoutInMsError.value = true;
  }

  return !errorNum;
};

/**
 * Get rendezvous configuration data
 * Returns configuration object without ID
 * 
 * @returns Rendezvous configuration (excluding ID)
 */
const getData = (): Omit<RendezvousConfig, 'id'> => {
  return {
    beforeName: '',
    transactionName: '',
    description: description.value || '',
    enabled: enabled.value,
    name: name.value || '',
    target: 'RENDEZVOUS',
    threads: threads.value || '',
    timeoutInMs: timeoutInMs.value || ''
  };
};

/**
 * Expose methods for parent component access
 */
defineExpose({
  isValid,    // Validate form fields
  getData,    // Get configuration data
  
  /**
   * Get rendezvous name
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
</script>

<template>
  <!-- 
    Rendezvous configuration row
    - Light gray background for visual grouping
    - Reduced opacity when disabled
    - Rounded corners for modern appearance
  -->
  <div 
    :class="{ 'opacity-70': !enabled && props.enabled }" 
    class="h-11.5 flex items-center pl-9.5 pr-3 rounded bg-gray-light">
    <!-- Rendezvous icon -->
    <Icon 
      class="flex-shrink-0 mr-3 text-4" 
      icon="icon-jihedian1" />
    
    <!-- Form fields container -->
    <div class="flex-1 flex items-center space-x-5 mr-5">
      <!-- Name field with duplicate name tooltip -->
      <Tooltip
        :title="t('httpPlugin.uiConfig.rendezvous.duplicateName')"
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
          class="point-name-input"
          :placeholder="t('common.placeholders.searchKeyword')"
          @change="nameChange" />
      </Tooltip>
      
      <!-- User count field (number of threads to wait for) -->
      <div class="flex items-center space-x-2 text-theme-content">
        <div>{{ t('httpPlugin.uiConfig.rendezvous.userCount') }}</div>
        <Input
          :value="threads"
          :maxlength="7"
          :min="1"
          :max="7200000"
          :error="threadsError"
          trimAll
          dataType="number"
          class="w-25"
          placeholder="1 ~ 7200000"
          @change="threadsChange" />
      </div>
      
      <!-- Timeout field (max wait time in milliseconds) -->
      <div class="flex items-center space-x-2">
        <div>{{ t('httpPlugin.uiConfig.rendezvous.waitTimeout') }}</div>
        <Input
          :value="timeoutInMs"
          :maxlength="7"
          :min="1"
          :max="7200000"
          :error="timeoutInMsError"
          trimAll
          dataType="number"
          class="w-25"
          placeholder="1 ~ 7200000"
          @change="timeoutInMsChange" />
        <div>ms</div>
      </div>
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
.point-name-input {
  flex: 0 0 calc((100% - (124px))*2/5);
}

.child-drag-container .point-name-input {
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
</style>
