<script setup lang="ts">
import { inject, ref, Ref } from 'vue';
import { Input, Modal, notification } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';

import { services } from '@/api/tester';

interface Props {
  visible:boolean,
  pid?: string
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  pid: undefined
});

const { t } = useI18n();

const emit = defineEmits<{
  (e:'update:visible', val:boolean):void;
  (e:'ok'):void;
}>();

// Inject function to add a new tab pane in the parent component
// eslint-disable-next-line @typescript-eslint/no-explicit-any, @typescript-eslint/no-unused-vars, @typescript-eslint/no-empty-function
const addTabPane = inject('addTabPane', (_data:{[key:string]:any}) => { });

// Inject function to refresh the sidebar in the parent component
// eslint-disable-next-line @typescript-eslint/no-empty-function
const refreshSidebar = inject('refreshSidebar', () => { });

// Inject project ID from the parent component
const projectId = inject<Ref<string>>('projectId', ref(''));

// Ref for the input value of the service name
const serviceNameInput = ref<string>();

// Ref for tracking the loading state during service creation
const isCreatingService = ref(false);

/**
 * Handle the creation of a new service
 * 1. Validate the input
 * 2. Send request to create service
 * 3. Update UI with the new service
 * 4. Show success notification
 */
const handleCreateService = async () => {
  // Validate that the service name is not empty
  if (!serviceNameInput.value?.trim().length) {
    return;
  }

  // Prepare parameters for service creation
  const serviceCreationParams = {
    name: serviceNameInput.value.trim(),
    projectId: projectId.value
  };

  // Set loading state to true during the API call
  isCreatingService.value = true;

  // Call the API to create a new service
  const [error, response] = await services.addServices(serviceCreationParams);

  // Reset loading state after the API call
  isCreatingService.value = false;

  // Handle error case
  if (error) {
    return;
  }

  // Extract service data from the response
  const { data } = response;
  const serviceId = data.id;
  const serviceName = serviceCreationParams.name;

  // Add a new tab pane for the created service
  addTabPane({
    id: serviceId,
    name: serviceName,
    _id: serviceId + 'group',
    value: 'group'
  });

  // Refresh the sidebar to show the new service
  refreshSidebar();

  // Emit ok event to notify parent component
  emit('ok');

  // Close the modal
  handleCloseModal();

  // Show success notification
  notification.success(t('actions.tips.addSuccess'));
};

/**
 * Handle closing the modal and resetting the input
 */
const handleCloseModal = () => {
  serviceNameInput.value = undefined;
  emit('update:visible', false);
};
</script>
<template>
  <Modal
    :title="t('service.service.addServiceModal.title')"
    :visible="props.visible"
    :confirmLoading="isCreatingService"
    @ok="handleCreateService"
    @cancel="handleCloseModal">
    <Input
      v-model:value="serviceNameInput"
      :allowClear="false"
      :placeholder="t('common.placeholders.searchKeyword')"
      :maxLength="100"
      trim />
  </Modal>
</template>
