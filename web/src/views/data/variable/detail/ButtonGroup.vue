<script setup lang="ts">
import { useI18n } from 'vue-i18n';
import { Button } from 'ant-design-vue';
import { Icon } from '@xcan-angus/vue-ui';
import { ButtonGroupAction, ButtonGroupProps } from './types';

const { t } = useI18n();

const props = withDefaults(defineProps<ButtonGroupProps>(), {
  editFlag: false,
  okButtonDisabled: false
});

/**
 * Component emit definition
 * Defines the events that this component can emit to its parent
 */
// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  /**
   * Emit click event with the action type
   * @param event - The event name ('click')
   * @param action - The action type that was clicked
   */
  (e: 'click', action: ButtonGroupAction): void;
}>();

/**
 * Handle button click events
 * Emits the appropriate action to the parent component
 *
 * @param action - The action type to emit
 */
const handleClick = (action: ButtonGroupAction) => {
  emit('click', action);
};
</script>

<template>
  <!-- Button group container -->
  <div class="flex items-center space-x-2.5">
    <!-- Save button - always visible -->
    <!-- Primary action for saving variable changes -->
    <Button
      :disabled="props.okButtonDisabled"
      type="primary"
      size="small"
      class="flex items-center space-x-1"
      @click="handleClick('ok')">
      <Icon icon="icon-dangqianxuanzhong" class="text-3.5" />
      <span>
        {{ t('actions.save') }}
      </span>
    </Button>

    <!-- Edit mode buttons - only visible when editing an existing variable -->
    <template v-if="props.editFlag">
      <!-- Delete button - removes the variable -->
      <Button
        type="default"
        size="small"
        class="flex items-center space-x-1"
        @click="handleClick('delete')">
        <Icon icon="icon-qingchu" class="text-3.5" />
        <span>
          {{ t('actions.delete') }}
        </span>
      </Button>

      <!-- Export button - exports the variable configuration -->
      <Button
        type="default"
        size="small"
        class="flex items-center space-x-1"
        @click="handleClick('export')">
        <Icon icon="icon-fuzhizujian2" class="text-3.5" />
        <span>
          {{ t('actions.export') }}
        </span>
      </Button>

      <!-- Clone button - creates a copy of the variable -->
      <Button
        type="default"
        size="small"
        class="flex items-center space-x-1"
        @click="handleClick('clone')">
        <Icon icon="icon-fuzhizujian2" class="text-3.5" />
        <span>
          {{ t('actions.clone') }}
        </span>
      </Button>

      <!-- Copy link button - copies the variable URL to clipboard -->
      <Button
        type="default"
        size="small"
        class="flex items-center space-x-1"
        @click="handleClick('copyLink')">
        <Icon icon="icon-fuzhi" class="text-3.5" />
        <span>
          {{ t('actions.copyLink') }}
        </span>
      </Button>

      <!-- Refresh button - reloads variable data -->
      <Button
        type="default"
        size="small"
        class="flex items-center space-x-1"
        @click="handleClick('refresh')">
        <Icon icon="icon-shuaxin" class="text-3.5" />
        <span>
          {{ t('actions.refresh') }}
        </span>
      </Button>
    </template>
  </div>
</template>
