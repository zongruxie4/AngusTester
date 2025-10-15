<script setup lang="ts">
import { defineAsyncComponent, inject, ref } from 'vue';
import { Modal } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';

interface Props {
  visible: boolean,
  serviceId?: string,
  source?:'introduce' | 'global' | 'projectOrService';
}
const props = withDefaults(defineProps<Props>(), {
  visible: false,
  serviceId: undefined,
  source: 'introduce'
});
const { t } = useI18n();
const ImportFile = defineAsyncComponent(() => import('@/views/apis/services/components/Import.vue'));

// eslint-disable-next-line @typescript-eslint/no-empty-function
const addTabPane = inject<(data: any) => void>('addTabPane', () => { });
// eslint-disable-next-line @typescript-eslint/no-empty-function
const refreshSidebar = inject<() => void>('refreshSidebar', () => { });

// const updateInterface = inject('updateInterface', {
//   reloadKey: 0,
//   serviceId: ''
// });
// eslint-disable-next-line @typescript-eslint/no-explicit-any, @typescript-eslint/no-unused-vars
const updateApiGroup = inject('updateApiGroup', (_id) => undefined);

// eslint-disable-next-line func-call-spacing
const emits = defineEmits<{
  (e: 'update:visible', value: boolean): void,
  (e: 'ok'): void
}>();

const closeModal = () => {
  emits('update:visible', false);
};
const handleOk = ({ id, key }) => {
  addTabPane({ _id: id + 'group', id, name: key, value: 'group' });
  refreshSidebar();
  updateApiGroup(id);
  emits('ok');
  closeModal();
};

const isLoading = ref(false);

</script>
<template>
  <Modal
    :title="t('service.sidebar.localImportModal.title')"
    :visible="visible"
    :footer="false"
    @cancel="closeModal">
    <template #default>
      <ImportFile
        ref="importFileRef"
        v-model:loading="isLoading"
        :serviceId="serviceId"
        :source="props.source"
        @close="closeModal"
        @ok="handleOk" />
    </template>
  </Modal>
</template>
