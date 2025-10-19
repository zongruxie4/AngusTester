<script setup lang="ts">
import { defineAsyncComponent } from 'vue';
import { Modal } from '@xcan-angus/vue-ui';

import { DataSetDetail } from '../types';
import { useI18n } from 'vue-i18n';
import { BasicDataSourceProps } from '@/types/types';
const { t } = useI18n();

const props = withDefaults(defineProps<BasicDataSourceProps<DataSetDetail>>(), {
  projectId: undefined,
  visible: false,
  dataSource: undefined
});

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void;
}>();

const PreviewData = defineAsyncComponent(() => import('@/views/data/dataset/detail/PreviewData.vue'));

const cancel = () => {
  emit('update:visible', false);
};
</script>

<template>
  <Modal
    :title="t('commonData.previewData.title')"
    :visible="props.visible"
    :width="900"
    destroyOnClose
    class="overflow-auto-modal-container"
    :footer="null"
    @cancel="cancel">
    <PreviewData :projectId="props.projectId" :dataSource="props.dataSource" />
  </Modal>
</template>

<style>
.ant-modal.overflow-auto-modal-container .ant-modal-content {
  overflow: hidden;
}

.ant-modal.overflow-auto-modal-container .ant-modal-content .ant-modal-body {
  max-height: calc(95vh - 84px);
  overflow: auto;
}
</style>
