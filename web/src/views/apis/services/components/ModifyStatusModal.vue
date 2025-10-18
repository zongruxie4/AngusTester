<script lang="ts" setup>
import { ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Hints, Modal, notification } from '@xcan-angus/vue-ui';
import { apis, services } from '@/api/tester';
import { ApiStatus } from '@/enums/enums';

import SelectEnum from '@/components/enum/SelectEnum.vue';

interface Props {
  visible: boolean;
  value?: string;
  type: 'SERVICE'|'API';
  id: string;
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  value: '',
  type: 'API'
});

const { t } = useI18n();

// Configuration objects for different entity types
const tipConfig = {
  SERVICE: t('service.statusModal.tips.serviceStatusChange'),
  API: ''
};

const publishTipConfig = {
  SERVICE: t('service.statusModal.tips.servicePublish'),
  API: t('service.statusModal.tips.apiPublish')
};

// API configuration mapping for different entity types
const setStatusApiConfig = {
  SERVICE: services.patchStatus,
  API: apis.patchApiStatus
};

const emits = defineEmits<{
  (e: 'update:visible', value: boolean):void;
  (e: 'confirm', value: {value: string, message: string}):void
}>();

// Reactive references for status value and name
const statusValue = ref<string>('');
const statusName = ref<string>('');

/**
 * Close the modal dialog
 */
const close = () => {
  emits('update:visible', false);
};

/**
 * Handle status change event from SelectEnum component
 * @param _value - Selected status value
 * @param option - Selected option object
 */
const handleStatusChange = (_value: string, option?: { label: string; value: string }) => {
  if (option) {
    statusName.value = option.label;
  }
};

/**
 * Submit the status change request
 * @returns Promise that resolves when submission completes
 */
const submit = async () => {
  // No change, just close the modal
  if (statusValue.value === props.value) {
    close();
    return;
  }

  // Call appropriate API based on entity type
  const [error] = await setStatusApiConfig[props.type]({ id: props.id, status: statusValue.value });
  if (error) {
    return;
  }

  // Notify success and emit confirmation event
  notification.success(t('service.statusModal.messages.updateStatusSuccess'));
  emits('confirm', { value: statusValue.value, message: statusName.value });
  close();
};

// Watch for visibility changes to initialize status value
watch(() => props.visible, (newValue) => {
  if (newValue) {
    statusValue.value = props.value || '';
  }
}, {
  immediate: true
});
</script>
<template>
  <Modal
    :title="t('service.statusModal.title')"
    :visible="props.visible"
    :width="400"
    @cancel="close"
    @ok="submit">
    <Hints
      v-show="tipConfig[props.type]"
      class="text-3 mb-3"
      :text="tipConfig[props.type]">
    </Hints>
    <Hints
      v-show="statusValue === ApiStatus.RELEASED"
      class="text-3 mb-3"
      :text="publishTipConfig[props.type]">
    </Hints>
    <div class="flex items-center">
      <span class="w-10">
        {{ t('common.status') }}
      </span>
      <SelectEnum
        v-model:value="statusValue"
        enumKey="ApiStatus"
        class="flex-1"
        @change="handleStatusChange" />
    </div>
  </Modal>
</template>
