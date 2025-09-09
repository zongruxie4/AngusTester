<script lang="ts" setup>
import { ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { Modal, notification } from '@xcan-angus/vue-ui';
import { apis, scenario, services } from '@/api/tester';
import { CombinedTargetType } from '@xcan-angus/infra';

const { t } = useI18n();

// ===== Component Props and Emits =====
interface Props {
  visible: boolean;
  content: string;
  type: CombinedTargetType.API | CombinedTargetType.SERVICE | CombinedTargetType.SCENARIO;
  id: string;
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  content: undefined,
  type: CombinedTargetType.SERVICE,
  id: undefined
});

// eslint-disable-next-line func-call-spacing
const emits = defineEmits<{
  (e: 'update:visible', value: boolean): void;
  (e: 'ok'): void;
}>();

// ===== API Configuration =====
/**
 * <p>
 * Maps target types to their corresponding restart API methods.
 * <p>
 * Provides a centralized way to handle different API calls based on the target type.
 */
const RESTART_API_MAP = {
  API: apis.restartTestTask,
  SERVICE: services.resetTestTask,
  SCENARIO: scenario.restartTestTask
};

// ===== Reactive Data and State =====
const isRestarting = ref(false);

// ===== Event Handlers =====
/**
 * <p>
 * Handles the modal close action.
 * <p>
 * Emits the update:visible event to close the modal.
 */
const handleModalClose = () => {
  emits('update:visible', false);
};

/**
 * <p>
 * Handles the restart confirmation action.
 * <p>
 * Calls the appropriate restart API, shows success notification, and emits the ok event.
 * Closes the modal on successful restart.
 */
const handleRestartConfirm = async () => {
  isRestarting.value = true;
  const [error] = await RESTART_API_MAP[props.type](props.id);
  isRestarting.value = false;
  if (error) {
    return;
  }

  emits('ok');
  emits('update:visible', false);
  notification.success(t('commonComp.restartTaskTestModal.restartSuccess'));
};

</script>
<template>
  <Modal
    :title="t('commonComp.restartTaskTestModal.title')"
    :width="580"
    :visible="props.visible"
    :confirmLoading="isRestarting"
    @ok="handleRestartConfirm"
    @cancel="handleModalClose">
    <div class="mb-5">{{ props.content }}</div>
  </Modal>
</template>
