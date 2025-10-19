
<script setup lang="ts">
import { ref } from 'vue';
import { Colon, Modal, notification } from '@xcan-angus/vue-ui';
import { RadioGroup } from 'ant-design-vue';
import { ApiType, ApiUrlBuilder, download, routerUtils } from '@xcan-angus/infra';
import { useI18n } from 'vue-i18n';

/**
 * Export format option interface
 */
interface FormatOption {
  label: string;  // Display label for the format
  value: string;  // Format value (JSON or YAML)
}

/**
 * Component props interface
 */
interface Props {
  visible: boolean;         // Controls modal visibility
  id: string | undefined;   // Script ID to export
}

// Define props with default values
const props = withDefaults(defineProps<Props>(), {
  visible: false,
  id: undefined
});

// Initialize i18n for internationalization
const { t } = useI18n();

/**
 * Event emitters
 */
const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void;
}>();

/**
 * Close the modal
 * Emits update:visible event with false
 */
const handleCancel = (): void => {
  emit('update:visible', false);
};

/**
 * State management
 */
const exportLoading = ref(false);          // Loading state during export
const format = ref<string>('JSON');        // Selected export format (default: JSON)

/**
 * Available export format options
 */
const formatTypes: FormatOption[] = [
  {
    label: 'JSON',
    value: 'JSON'
  },
  {
    label: 'YAML',
    value: 'YAML'
  }
];

/**
 * Handle export confirmation
 * Builds the export URL and triggers file download
 */
const handleOk = async (): Promise<void> => {
  // Prevent multiple simultaneous exports or export without ID
  if (exportLoading.value || !props.id) {
    return;
  }

  exportLoading.value = true;
  
  // Build export URL using route config
  const routeConfig = routerUtils.getTesterApiRouteConfig(ApiType.API);
  const exportUrl = ApiUrlBuilder.buildApiUrl(
    routeConfig,
    `/script/${props.id}/export?format=${format.value}`
  );
  
  // Trigger file download
  const [error] = await download(exportUrl);
  exportLoading.value = false;
  
  // Handle download error
  if (error) {
    notification.error(error.message);
    return;
  }

  // Close modal on success
  handleCancel();
};
</script>

<template>
  <!-- Export script modal dialog -->
  <Modal
    :visible="props.visible"
    :confirmLoading="exportLoading"
    :title="t('ftpPlugin.exportScriptModal.title')"
    @cancel="handleCancel"
    @ok="handleOk">
    
    <!-- Format selection section -->
    <div class="mt-1.5">
      <!-- Format label -->
      <span class="mr-3.5">
        {{ t('ftpPlugin.exportScriptModal.format') }}
        <Colon class="ml-1" />
      </span>
      
      <!-- Radio button group for format selection -->
      <RadioGroup 
        v-model:value="format" 
        :options="formatTypes" />
    </div>
  </Modal>
</template>
