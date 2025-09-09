<script lang="ts" setup>
import { ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { Icon, Modal, notification } from '@xcan-angus/vue-ui';
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
    class="restart-test-modal-container"
    :title="t('commonComp.restartTaskTestModal.title')"
    :width="600"
    :visible="props.visible"
    :confirmLoading="isRestarting"
    @ok="handleRestartConfirm"
    @cancel="handleModalClose">
    <!-- Confirmation information area -->
    <div class="confirm-section">
      <div class="confirm-icon">
        <Icon icon="icon-restart" />
      </div>
      <div class="confirm-content">
        <p class="confirm-message">{{ props.content }}</p>
      </div>
    </div>
  </Modal>
</template>

<style scoped>
/* Confirmation information area */
.confirm-section {
  display: flex;
  align-items: flex-start;
  padding: 20px;
  background-color: #e6f7ff;
  border: 1px solid #91d5ff;
  border-radius: 6px;
}

.confirm-icon {
  color: #1890ff;
  font-size: 24px;
  margin-right: 16px;
  margin-top: 2px;
  flex-shrink: 0;
}

.confirm-content {
  flex: 1;
}

.confirm-message {
  font-size: 14px;
  color: #595959;
  line-height: 1.6;
  margin: 0;
  white-space: pre-line;
}

/* Responsive design */
@media (max-width: 768px) {
  .confirm-section {
    padding: 16px;
  }

  .confirm-icon {
    font-size: 20px;
    margin-right: 12px;
  }
}
</style>
