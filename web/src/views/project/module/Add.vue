<script setup lang="ts">
// Vue composition API imports
import { ref, computed } from 'vue';
import { useI18n } from 'vue-i18n';

// Ant Design components
import type { ButtonProps } from 'ant-design-vue';

// Custom UI components
import { Input, Modal, notification } from '@xcan-angus/vue-ui';

// Utilities and API
import { modules } from '@/api/tester';
import type { AddModuleProps } from './types';

/**
 * Component for adding new project modules
 * Provides a modal interface for creating modules with validation
 */

// Initialize internationalization
const { t } = useI18n();

// Props definition with proper defaults
const props = withDefaults(defineProps<AddModuleProps>(), {
  projectId: undefined,
  visible: false,
  pid: undefined
});

// Emit events for parent component communication
const emit = defineEmits<{
  /** Updates modal visibility state */
  (e: 'update:visible', value: boolean): void;
  /** Emitted when module is successfully created */
  (e: 'ok', value: { id: string; name: string }): void;
}>();

// Reactive state management
const inputValue = ref<string>('');

/**
 * Computed property for OK button state
 * Manages loading state and validation
 */
const okButtonProps = computed<ButtonProps>(() => ({
  loading: false,
  disabled: !inputValue.value?.trim()
}));

// Reactive loading state for async operations
const isLoading = ref(false);

/**
 * Handles module creation process
 * Validates input, calls API, and manages success/error states
 */
const handleCreateModule = async (): Promise<void> => {
  const trimmedName = inputValue.value?.trim();

  // Validate input before proceeding
  if (!trimmedName) {
    return;
  }

  // Set loading state
  isLoading.value = true;

  try {
    // Prepare API parameters
    const createParams = {
      names: [trimmedName],
      projectId: props.projectId,
      pid: props.pid
    };

    // Call API to create the module
    const [error, response] = await modules.addModule(createParams);

    if (error) {
      console.error('Failed to create module:', error);
      return;
    }

    // Extract created module information
    const createdModule = response?.data?.[0];
    const moduleId = createdModule?.id || '';

    // Emit success event with module details
    emit('ok', {
      id: moduleId,
      name: trimmedName
    });

    // Reset form and close modal
    resetForm();
    closeModal();

    // Show success notification
    notification.success(t('actions.tips.addSuccess'));
  } catch (error) {
    console.error('Unexpected error during module creation:', error);
  } finally {
    isLoading.value = false;
  }
};

/**
 * Handles modal cancellation
 * Resets form state and closes the modal
 */
const handleCancel = (): void => {
  resetForm();
  closeModal();
};

/**
 * Handles input value changes
 * Updates button state based on validation
 */
const handleInputChange = (): void => {
  // The computed property will automatically update button state
  // This function can be extended for additional validation logic
};

/**
 * Handles Enter key press in input field
 * Triggers module creation if input is valid
 */
const handleEnterKey = (): void => {
  if (inputValue.value?.trim()) {
    handleCreateModule();
  }
};

/**
 * Resets the form to initial state
 * Clears input value and any validation errors
 */
const resetForm = (): void => {
  inputValue.value = '';
};

/**
 * Closes the modal by emitting visibility update
 */
const closeModal = (): void => {
  emit('update:visible', false);
};

/**
 * Computed property that combines loading state with validation
 * Used for the OK button props
 */
const combinedOkButtonProps = computed<ButtonProps>(() => ({
  loading: isLoading.value,
  disabled: !inputValue.value?.trim()
}));
</script>

<template>
  <!-- Modal for adding new module -->
  <Modal
    :title="t('module.messages.addModuleTitle')"
    width="500px"
    :visible="props.visible"
    :okButtonProps="combinedOkButtonProps"
    @cancel="handleCancel"
    @ok="handleCreateModule">
    <!-- Input field for module name -->
    <Input
      v-model:value="inputValue"
      :placeholder="t('common.placeholders.inputName')"
      trim
      :allowClear="true"
      :maxlength="50"
      @pressEnter="handleEnterKey"
      @change="handleInputChange" />
  </Modal>
</template>
