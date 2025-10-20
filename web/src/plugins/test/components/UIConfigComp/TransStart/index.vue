<script setup lang="ts">
import { ref, onMounted, watch, watchEffect } from 'vue';
import { useI18n } from 'vue-i18n';
import { Collapse, CollapsePanel } from 'ant-design-vue';
import { Input, Icon, Tooltip } from '@xcan-angus/vue-ui';
import { utils } from '@xcan-angus/infra';

import { TransStartConfig } from './PropsType';
import ActionsGroup from '../ActionsGroup/index.vue';

// Initialize i18n for internationalization
const { t } = useI18n();

/**
 * Component props interface
 */
export interface Props {
  value: TransStartConfig;  // Transaction start configuration data
  repeatNames: string[];    // List of existing names to check for duplicates
}

// Define props with default values
const props = withDefaults(defineProps<Props>(), {
  value: undefined,
  repeatNames: () => []
});

/**
 * Event emitters
 */
const emit = defineEmits<{
  (e: 'change', value: Omit<TransStartConfig, 'id'>): void;  // Emit data changes
  (e: 'nameChange', value: string): void;                     // Emit name changes
  (e: 'click', value: 'delete' | 'clone'): void;             // Emit action clicks
  (e: 'enabledChange', value: boolean): void;                 // Emit enabled state changes
  (e: 'renderChange'): void;                                  // Emit render event
}>();

/**
 * Unique identifier for this component instance
 * Used as collapse panel key
 */
const UUID = utils.uuid();

/**
 * Form field reactive references
 */
const activeKey = ref<string>(UUID);   // Active collapse panel key (controls expand/collapse)
const enabled = ref(false);            // Whether this transaction is enabled
const name = ref<string>();            // Transaction name (must be unique)
const description = ref<string>();     // Optional transaction description

/**
 * Validation error states
 */
const nameError = ref(false);          // Name validation error
const nameRepeatFlag = ref(false);     // Name duplication flag

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
 * Handle collapse panel expand/collapse
 * Updates active key to control panel visibility
 * 
 * @param _open - Whether to expand or collapse
 */
const openChange = (_open: boolean): void => {
  if (_open) {
    // Expand: set active key to this panel's UUID
    activeKey.value = UUID;
    return;
  }

  // Collapse: clear active key
  activeKey.value = '';
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
    enabled: _enabled 
  } = props.value;
  
  name.value = _name;
  description.value = _description;
  enabled.value = _enabled;
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
 * Checks for required name field and duplicate names
 * 
 * @returns true if validation passes, false otherwise
 */
const isValid = (): boolean => {
  // Clear previous errors
  nameError.value = false;
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

  return !errorNum;
};

/**
 * Get transaction start configuration data
 * Returns configuration object without ID
 * 
 * @returns TransStart configuration (excluding ID)
 */
const getData = (): Omit<TransStartConfig, 'id'> => {
  return {
    beforeName: '',
    transactionName: '',
    description: description.value || '',
    enabled: enabled.value,
    name: name.value || '',
    target: 'TRANS_START'
  };
};

/**
 * Expose methods for parent component access
 */
defineExpose({
  getData,    // Get configuration data
  isValid,    // Validate form fields
  
  /**
   * Get transaction name
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
    Transaction start collapsible panel
    - Light blue header background
    - Reduced opacity when disabled
    - Collapsible section for description field
  -->
  <Collapse
    :activeKey="activeKey"
    :class="{ 'opacity-70': !enabled }"
    class="trans-collapse-container text-3"
    style="background-color: #fff;"
    :bordered="false">
    <CollapsePanel
      :key="UUID"
      :showArrow="false"
      collapsible="disabled">
      <!-- Panel header: transaction configuration row -->
      <template #header>
        <div class="flex items-center flex-nowrap w-full whitespace-nowrap">
          <!-- Transaction icon -->
          <Icon 
            class="flex-shrink-0 mr-3 text-4" 
            icon="icon-shiwu" />
          
          <!-- Name field with duplicate name tooltip -->
          <div class="flex-1 flex items-center">
            <Tooltip
              :title="t('httpPlugin.uiConfig.transStart.duplicateName')"
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
                style="width: calc((100% - (144px))*2/5);"
                :placeholder="t('common.placeholders.searchKeyword')"
                @change="nameChange" />
            </Tooltip>
          </div>
          
          <!-- Action buttons: enable/disable, clone, delete, expand/collapse -->
          <ActionsGroup
            v-model:enabled="enabled"
            :open="activeKey === UUID"
            :arrowVisible="true"
            @openChange="openChange"
            @enabledChange="enabledChange"
            @click="actionClick" />
        </div>
      </template>
      
      <!-- Panel content: description field (injected via slot) -->
      <slot name="default"></slot>
    </CollapsePanel>
  </Collapse>
</template>

<style scoped>
.ant-collapse.trans-collapse-container {
  line-height: 20px;
}

.ant-collapse-borderless.trans-collapse-container > :deep(.ant-collapse-item) {
  border: none;
  border-radius: 4px;
}

.ant-collapse.trans-collapse-container> :deep(.ant-collapse-item)>.ant-collapse-header {
  align-items: center;
  height: 46px;
  padding: 0 12px 0 38px;
  border-radius: 4px;
  background-color: rgba(232, 240, 251, 100%);
  line-height: 20px;
}

.trans-collapse-container > :deep(.ant-collapse-item) > .ant-collapse-content > .ant-collapse-content-box {
  padding: 0;
  background-color: #fff;
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
