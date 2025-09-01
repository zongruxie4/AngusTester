<script setup lang="ts">
import { Radio, RadioGroup } from 'ant-design-vue';
import { Colon, Modal } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';

// Import types and composables
import type { ExportModalProps, ExportModalEmits } from './types';
import { useExport } from './composables/useExport';
import { useModal } from './composables/useModal';

/**
 * <p>Export Modal Component</p>
 * <p>Provides a modal interface for exporting variable data in different formats</p>
 * <p>Supports JSON and YAML export formats with optional variable ID filtering</p>
 */

// Component props with default values
const props = withDefaults(defineProps<ExportModalProps>(), {
  visible: false,
  projectId: undefined,
  id: undefined
});

// Component emits for parent communication
const emit = defineEmits<ExportModalEmits>();

// Internationalization setup
const { t } = useI18n();

// Initialize composables
const {
  exportConfig,
  updateFileFormat,
  executeExport
} = useExport();

const {
  handleCancel,
  handleOk
} = useModal(emit);

/**
 * <p>Handle export confirmation</p>
 * <p>Executes the export operation and closes the modal on success</p>
 */
const handleExportConfirm = async (): Promise<void> => {
  try {
    await executeExport(props.projectId, props.id);
    // Close modal after successful export
    handleOk('export_success');
  } catch (error) {
    // Handle export error - modal stays open for user to retry
    console.error('Export failed:', error);
  }
};
</script>

<template>
  <Modal
    :visible="props.visible"
    :width="500"
    :confirmLoading="exportConfig.loading"
    :title="t('dataVariable.exportModal.title')"
    @cancel="handleCancel"
    @ok="handleExportConfirm">
    
    <!-- Export format selection section -->
    <div class="flex items-center">
      <div class="flex items-center mr-3.5">
        <span>{{ t('dataVariable.exportModal.format') }}</span>
        <Colon />
      </div>
      
      <!-- File format radio buttons -->
      <RadioGroup 
        :value="exportConfig.fileType" 
        name="fileType"
        @update:value="updateFileFormat">
        <Radio value="JSON">JSON</Radio>
        <Radio value="YAML">YAML</Radio>
      </RadioGroup>
    </div>
  </Modal>
</template>

<style scoped>
/* Component-specific styles can be added here if needed */
</style>
