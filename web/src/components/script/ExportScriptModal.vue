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
  ids: string[];
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
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
  label: t('commonComp.exportScriptModal.json'),
  value: 'JSON'
}, {
  label: t('commonComp.exportScriptModal.yaml'),
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
  if (exportLoading.value || !props.ids?.length) {
    return;
  }

  exportLoading.value = true;
  const routeConfig = routerUtils.getTesterApiRouteConfig(ApiType.API);
  const urls: string[] = props.ids.map((item) => {
    return ApiUrlBuilder.buildApiUrl(routeConfig, `/script/${item}/export?format=${format.value}`);
  });
  const res = await download(urls);
  exportLoading.value = false;
  const totalNum = props.ids.length;
  if (totalNum > 1) {
    // Multiple files exported successfully
  } else {
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
        <h3 class="section-title">{{ t('commonComp.exportScriptModal.format') }}</h3>
        <div class="radio-container">
          <RadioGroup
            v-model:value="format"
            :options="formatTypes"
            class="format-radio-group" />
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

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: #262626;
  margin: 0 0 12px 0;
  padding-bottom: 8px;
  border-bottom: 1px solid #e8e8e8;
}

.radio-container {
  margin-top: 8px;
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

/* Responsive design */
@media (max-width: 768px) {
  .format-section {
    padding: 12px;
  }

  .format-radio-group {
    flex-direction: column;
    gap: 12px;
  }
}
</style>
