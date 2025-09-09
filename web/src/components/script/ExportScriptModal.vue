<script setup lang="ts">
import { ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { Modal, notification } from '@xcan-angus/vue-ui';
import { RadioGroup } from 'ant-design-vue';
import { download, routerUtils, ApiType, ApiUrlBuilder } from '@xcan-angus/infra';

const { t } = useI18n();

// ===== Component Props and Emits =====
interface Props {
  visible: boolean;
  id?: string;
  ids?: string[];
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  id: undefined,
  ids: () => []
});

const emit = defineEmits<{(e: 'update:visible', value: boolean): void }>();

// ===== Reactive Data and State =====
const exportLoading = ref(false);
const format = ref('JSON');

// ===== Computed Properties =====
/**
 * <p>
 * Format type options for the radio group.
 * <p>
 * Provides JSON and YAML format options for script export.
 * @returns Array of format type options
 */
const formatTypes = [{
  label: 'JSON',
  value: 'JSON'
}, {
  label: 'YAML',
  value: 'YAML'
}];

// ===== Event Handlers =====
/**
 * <p>
 * Handles the modal close action.
 * <p>
 * Emits the update:visible event to close the modal.
 */
const handleCancel = () => {
  emit('update:visible', false);
};

/**
 * <p>
 * Handles the export confirmation action.
 * <p>
 * Builds download URLs, downloads the scripts, and handles errors.
 * Closes the modal after completion.
 */
const handleOk = async () => {
  const exportIds = props.ids?.length ? props.ids : (props.id ? [props.id] : []);

  if (exportLoading.value || !exportIds.length) {
    notification.error('The script ID is empty');
    return;
  }

  exportLoading.value = true;
  const routeConfig = routerUtils.getTesterApiRouteConfig(ApiType.API);
  const urls: string[] = exportIds.map((item) => {
    return ApiUrlBuilder.buildApiUrl(routeConfig, `/script/${item}/export?format=${format.value}`);
  });
  const res = await download(urls);
  exportLoading.value = false;
  const totalNum = exportIds.length;
  if (totalNum < 1) {
    const _error = res[0];
    if (_error) {
      notification.error(_error?.message);
    }
  }

  handleCancel();
};
</script>
<template>
  <Modal
    class="export-script-modal"
    :visible="props.visible"
    :confirmLoading="exportLoading"
    :title="t('commonComp.exportScriptModal.title')"
    :width="500"
    @cancel="handleCancel"
    @ok="handleOk">
    <!-- Main content area -->
    <div class="modal-content">
      <!-- Format selection section -->
      <div class="format-section">
        <div class="section-header">
          <h3 class="section-title">{{ t('commonComp.exportScriptModal.format') }}:</h3>
          <div class="radio-container">
            <RadioGroup
              v-model:value="format"
              :options="formatTypes"
              class="format-radio-group" />
          </div>
        </div>
      </div>
    </div>
  </Modal>
</template>

<style scoped>
/* Modal content area */
.modal-content {
  padding: 16px 0;
}

/* Format selection section */
.format-section {
  padding: 5px 16px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: center;
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: #262626;
  margin: 0;
  white-space: nowrap;
  margin-right: 32px;
}

.radio-container {
  flex: 1;
}

.format-radio-group {
  display: flex;
  gap: 16px;
  flex-wrap: nowrap;
}

/* Deep style overrides */
:deep(.ant-radio-wrapper) {
  font-size: 12px;
  margin-right: 0;
}

:deep(.ant-radio) {
  margin-right: 8px;
}

:deep(.ant-radio-inner) {
  width: 16px;
  height: 16px;
}

:deep(.ant-radio-checked .ant-radio-inner) {
  background-color: #1890ff;
  border-color: #1890ff;
}

:deep(.ant-modal-header) {
  border-bottom: 1px solid #f0f0f0;
  padding: 16px 24px;
}

:deep(.ant-modal-title) {
  font-size: 14px;
  font-weight: 600;
  color: #262626;
}

:deep(.ant-modal-body) {
  padding: 24px;
}

:deep(.ant-modal-footer) {
  border-top: 1px solid #f0f0f0;
  padding: 12px 24px;
}
</style>
