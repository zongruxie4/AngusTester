<script setup lang="ts">
import { useI18n } from 'vue-i18n';
import { Switch } from 'ant-design-vue';
import { Arrow, Icon } from '@xcan-angus/vue-ui';

// Initialize i18n for internationalization
const { t } = useI18n();

/**
 * Action type for click events
 */
type ActionType = 'delete' | 'clone';

/**
 * Component props interface
 */
export interface Props {
  enabled: boolean;      // Whether the item is enabled/active
  open: boolean;         // Whether the item is expanded/collapsed
  arrowVisible: boolean; // Whether to show the expand/collapse arrow
}

// Define props with default values
const props = withDefaults(defineProps<Props>(), {
  enabled: false,      // Default: disabled
  open: true,          // Default: expanded
  arrowVisible: true   // Default: arrow visible
});

/**
 * Event emitters
 */
const emit = defineEmits<{
  (e: 'update:enabled', value: boolean): void;   // v-model update for enabled state
  (e: 'update:open', value: boolean): void;      // v-model update for open state
  (e: 'openChange', value: boolean): void;       // Open state change event
  (e: 'enabledChange', value: boolean): void;    // Enabled state change event
  (e: 'click', value: ActionType): void;         // Action button click event
}>();

/**
 * Handle enabled/disabled state change
 * Emits both v-model update and change event
 * 
 * @param checked - New enabled state
 */
const enabledChange = (checked: any): void => {
  const value = Boolean(checked);
  emit('update:enabled', value);
  emit('enabledChange', value);
};

/**
 * Handle expand/collapse state change
 * Emits both v-model update and change event
 * 
 * @param value - New open state
 */
const openChange = (value: boolean): void => {
  emit('update:open', value);
  emit('openChange', value);
};

/**
 * Handle action button click (clone or delete)
 * Emits click event with action type
 * 
 * @param value - Action type ('clone' or 'delete')
 */
const click = (value: ActionType): void => {
  emit('click', value);
};
</script>

<template>
  <div class="flex items-center flex-shrink-0 space-x-3">
    <!-- Enable/disable toggle switch -->
    <Switch
      :checked="props.enabled"
      size="small"
      @change="enabledChange" />

    <!-- Clone/copy button -->
    <div 
      class="flex items-center cursor-pointer hover:text-text-link-hover" 
      :title="t('actions.copy')">
      <Icon
        icon="icon-fuzhi"
        class="text-3.5"
        @click="click('clone')" />
    </div>

    <!-- Delete/remove button -->
    <div 
      class="flex items-center cursor-pointer hover:text-text-link-hover" 
      :title="t('actions.delete')">
      <Icon
        icon="icon-qingchu"
        class="text-3.5"
        @click="click('delete')" />
    </div>

    <!-- Expand/collapse arrow (conditionally visible) -->
    <Arrow
      :open="props.open"
      :class="{ invisible: !props.arrowVisible }"
      @change="openChange" />
  </div>
</template>
