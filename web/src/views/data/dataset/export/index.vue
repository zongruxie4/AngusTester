<script setup lang="ts">
import { Radio, RadioGroup } from 'ant-design-vue';
import { Colon, Modal } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';

import type { ExportModalProps, ExportModalEmits } from './types';
import { useExport } from './composables/useExport';
import { useModal } from './composables/useModal';

/**
 * Dataset Export Modal Component
 * <p>
 * A modal component that allows users to export datasets in different formats (JSON/YAML)
 * </p>
 * <p>
 * The component provides a clean interface for selecting export format and initiating
 * the download process for selected datasets
 * </p>
 */

// Component props definition with default values
const props = withDefaults(defineProps<ExportModalProps>(), {
  visible: false,
  projectId: undefined,
  id: undefined
});

// Component emit events definition
const emit = defineEmits<ExportModalEmits>();

// Internationalization setup
const { t } = useI18n();

// Initialize composables for business logic separation
const {
  selectedFileType,
  isLoading,
  updateFileType,
  executeExport,
  resetExportConfig
} = useExport(props.projectId, props.id);

const {
  closeModal,
  confirmModal,
  cancelModal
} = useModal(emit);

/**
 * Handles the file format change event
 * <p>
 * Updates the selected export format when user changes the radio button selection
 * </p>
 * @param format - The newly selected file format
 */
const handleFileTypeChange = (format: 'JSON' | 'YAML'): void => {
  updateFileType(format);
};

/**
 * Handles the modal confirmation action
 * <p>
 * Executes the export operation and closes the modal upon successful completion
 * </p>
 */
const handleConfirm = async (): Promise<void> => {
  try {
    await executeExport();
    confirmModal();
  } catch (error) {
    // Error handling is done within the composable
    // Modal remains open for user to retry or cancel
  }
};

/**
 * Handles the modal cancellation action
 * <p>
 * Resets the export configuration and closes the modal
 * </p>
 */
const handleCancel = (): void => {
  resetExportConfig();
  cancelModal();
};
</script>

<template>
  <Modal
    :visible="props.visible"
    :width="500"
    :confirmLoading="isLoading"
    :title="t('dataset.exportModal.title')"
    @cancel="handleCancel"
    @ok="handleConfirm">
    <!-- Export format selection section -->
    <div class="flex items-center">
      <!-- Format label with colon separator -->
      <div class="flex items-center mr-3.5">
        <span>{{ t('dataset.exportModal.format') }}</span>
        <Colon />
      </div>

      <!-- File format radio button group -->
      <RadioGroup
        :value="selectedFileType"
        name="fileType"
        @change="handleFileTypeChange">
        <Radio value="JSON">JSON</Radio>
        <Radio value="YAML">YAML</Radio>
      </RadioGroup>
    </div>
  </Modal>
</template>

<style scoped>
/* Component-specific styles can be added here if needed */
</style>
