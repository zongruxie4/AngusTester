<script setup lang="ts">
import { Ref, inject, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Colon, Modal, Spin, SelectApi } from '@xcan-angus/vue-ui';
import { RadioGroup } from 'ant-design-vue';
import { useExportModal } from './composables';
import { MockService } from './types';

interface Props {
  visible: boolean;
  mockService?: MockService;
}

const props = withDefaults(defineProps<Props>(), {
  visible: true,
  mockService: undefined
});

const emit = defineEmits<{(e: 'update:visible', value: boolean): void }>();

// Inject project information
const projectId = inject<Ref<string>>('projectId', ref(''));

// Use export modal composable
const {
  format,
  loading,
  treeProps,
  scrollProps,
  formatTypes,
  handleApiSelectionChange,
  handleExport,
  executeExport
} = useExportModal(projectId.value, props.mockService);

const { t } = useI18n();

/**
 * Handle modal confirmation
 * Execute the export operation
 */
const handleOk = async () => {
  await executeExport(props.mockService?.id);
  emit('update:visible', false);
};

/**
 * Handle modal cancellation
 * Close the modal
 */
const handleCancel = () => {
  emit('update:visible', false);
};

// Watch for visibility changes to reset state
watch(() => props.visible, handleExport, {
  immediate: true
});
</script>

<template>
  <Modal
    :visible="props.visible"
    :width="800"
    :title="t('mock.exportModal.title')"
    @cancel="handleCancel"
    @ok="handleOk">
    <Spin :spinning="loading">
      <div class="flex items-center">
        <span>{{ t('mock.exportModal.format') }}<Colon class="ml-1 mr-3.5" /></span>
        <RadioGroup
          v-model:value="format"
          :options="formatTypes">
        </RadioGroup>
      </div>
      <SelectApi
        v-if="props.visible"
        style="height: 448px;"
        class="mt-2"
        mode="multiple"
        :fields=" [
          { key: 'endpoint', name: t('mock.exportModal.fields.endpoint') },
          { key: 'summary', name: t('mock.exportModal.fields.summary') }
        ]"
        treeLabel=""
        :treeProps="treeProps"
        :scrollProps="scrollProps"
        @change="handleApiSelectionChange" />
    </Spin>
  </Modal>
</template>

<style scoped>
:deep(.checkbox-border-black .ant-checkbox-inner) {
  border-color: #d3dce6;
}
</style>
