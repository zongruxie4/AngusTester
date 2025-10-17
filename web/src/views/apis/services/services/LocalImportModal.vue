<script setup lang="ts">
import { defineAsyncComponent, inject, ref } from 'vue';
import { Modal } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';

const ImportFile = defineAsyncComponent(() => import('@/views/apis/services/components/Import.vue'));

interface Props {
  visible: boolean,
  serviceId?: string,
  source?:'introduce' | 'global' | 'services';
}
const props = withDefaults(defineProps<Props>(), {
  visible: false,
  serviceId: undefined,
  source: 'introduce'
});

const { t } = useI18n();

// eslint-disable-next-line @typescript-eslint/no-empty-function
const addTabPane = inject<(
  data: { _id: string; id: string; name: string; value: 'group' }
) => void>('addTabPane', () => { });
// eslint-disable-next-line @typescript-eslint/no-empty-function
const refreshSidebar = inject<() => void>('refreshSidebar', () => { });

// eslint-disable-next-line @typescript-eslint/no-unused-vars
const updateApiGroup = inject<(id: string) => void>('updateApiGroup', (_id) => undefined);

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void,
  (e: 'ok'): void
}>();

const closeModal = () => {
  emit('update:visible', false);
};
const handleOk = ({ id, key }: { id: string; key: string }) => {
  addTabPane && addTabPane({ _id: id + 'group', id, name: key, value: 'group' });
  refreshSidebar && refreshSidebar();
  updateApiGroup && updateApiGroup(id);
  emit('ok');
  closeModal();
};

const isLoading = ref<boolean>(false);
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
