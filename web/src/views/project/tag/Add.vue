<script setup lang="ts">
// Vue composition API imports
import { ref, computed } from 'vue';
import { useI18n } from 'vue-i18n';

// Ant Design components
import type { ButtonProps } from 'ant-design-vue';

// Custom UI components
import { Input, Modal, notification } from '@xcan-angus/vue-ui';

// API imports and utils
import { tag } from '@/api/tester';
import type { AddTagProps } from './types';

/**
 * Component for adding new project tags
 * Provides a modal interface for creating tags with validation
 */

// Initialize internationalization
const { t } = useI18n();

// Props definition with proper defaults
const props = withDefaults(defineProps<AddTagProps>(), {
  projectId: '',
  visible: false
});

// Emit events for parent component communication

const emit = defineEmits<{
  /** Updates modal visibility state */
  (e: 'update:visible', value: boolean): void;
  /** Emitted when tag is successfully created */
  (e: 'ok', value: { id: string; name: string }): void;
}>();

// Reactive state management
const inputValue = ref<string>('');
const isLoading = ref(false);

/**
 * Computed property for OK button state
 * Manages loading state and validation
 */
const okButtonProps = computed<ButtonProps>(() => ({
  loading: isLoading.value,
  disabled: !inputValue.value?.trim()
}));

/**
 * Handles tag creation process
 * Validates input, calls API, and manages success/error states
 */
const handleCreateTag = async (): Promise<void> => {
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
      projectId: props.projectId
    };

    // Call API to create the tag
    const [error, response] = await tag.addTag(createParams);

    if (error) {
      console.error('Failed to create tag:', error);
      return;
    }

    // Extract created tag information
    const createdTag = response?.data?.[0];
    const tagId = createdTag?.id || '';

    // Emit success event with tag details
    emit('ok', {
      id: tagId,
      name: trimmedName
    });

    // Reset form and close modal
    resetForm();
    closeModal();

    // Show success notification
    notification.success(t('actions.tips.addSuccess'));
  } catch (error) {
    console.error('Unexpected error during tag creation:', error);
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
 * Triggers tag creation if input is valid
 */
const handleEnterKey = (): void => {
  if (inputValue.value?.trim()) {
    handleCreateTag();
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
 * Validates tag name input
 * Checks for various validation rules
 *
 * @param name - Tag name to validate
 * @returns Boolean indicating if the name is valid
 */
const validateTagName = (name: string): boolean => {
  const trimmedName = name?.trim();
  if (!trimmedName) {
    return false;
  }

  // Check length limits
  if (trimmedName.length > 50) {
    return false;
  }

  // Additional validation can be added here
  // For example: special characters, reserved words, etc.
  return true;
};
</script>

<template>
  <!-- Modal for adding new tag -->
  <Modal
    :title="t('tag.messages.addTagTitle')"
    width="500px"
    :visible="props.visible"
    :okButtonProps="okButtonProps"
    @cancel="handleCancel"
    @ok="handleCreateTag">
    <!-- Input field for tag name -->
    <Input
      v-model:value="inputValue"
      :placeholder="t('tag.messages.tagNamePlaceholder')"
      trim
      :allowClear="true"
      :maxlength="50"
      @pressEnter="handleEnterKey"
      @change="handleInputChange" />
  </Modal>
</template>
