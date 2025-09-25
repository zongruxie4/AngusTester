<script lang="ts" setup>
import { ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { notification } from '@xcan-angus/vue-ui';
import { CheckboxGroup, Modal } from 'ant-design-vue';
import { apis, services } from '@/api/tester';
import { CombinedTargetType, enumOptionUtils } from '@xcan-angus/infra';
import { TestType } from '@/enums/enums';

const { t } = useI18n();

// ===== Component Props and Emits =====
interface Props {
  visible: boolean;
  id?: string;
  type: CombinedTargetType.API | CombinedTargetType.SERVICE;
}

const props = withDefaults(defineProps<Props>(), {
  id: ''
});

const emits = defineEmits<{(e: 'update:visible', value: boolean)}>();

// ===== Reactive Data and State =====
const checked = ref([TestType.PERFORMANCE, TestType.STABILITY, TestType.FUNCTIONAL]);
const validated = ref(false);
const loading = ref(false);

// ===== Precomputed Options =====
/**
 * <p>
 * Test type options for the checkbox group.
 * <p>
 * Excludes the CUSTOMIZATION test type from the available options.
 */
const testTypeOptions = enumOptionUtils.loadEnumAsOptions(TestType, {
  excludeValues: [TestType.CUSTOMIZATION]
});

// ===== Event Handlers =====
/**
 * <p>
 * Handles the delete confirmation action.
 * <p>
 * Validates selection, calls the appropriate delete API, and shows success notification.
 * Closes the modal on successful deletion.
 */
const handleDel = async () => {
  if (!checked.value.length) {
    validated.value = true;
    return;
  }
  loading.value = true;
  const [error] = await (
    props.type === CombinedTargetType.API
      ? apis.deleteTestScript(props.id, checked.value)
      : services.delApiScript(props.id, checked.value));
  if (error) {
    return;
  }
  loading.value = false;
  notification.success(t('actions.tips.deleteSuccess'));
  emits('update:visible', false);
};

/**
 * <p>
 * Handles the modal close action.
 * <p>
 * Emits the update:visible event to close the modal.
 */
const handleClose = () => {
  emits('update:visible', false);
};

// ===== Lifecycle Hooks =====
watch(() => props.visible, () => {
  checked.value = [TestType.PERFORMANCE, TestType.STABILITY, TestType.FUNCTIONAL];
  validated.value = false;
}, {
  immediate: true
});

</script>
<template>
  <Modal
    class="delete-script-modal"
    :title="t('commonComp.delScriptModal.title')"
    :confirmLoading="loading"
    :visible="visible"
    :width="500"
    @cancel="handleClose"
    @ok="handleDel">
    <!-- Main content area -->
    <div class="modal-content">
      <!-- Script type selection section -->
      <div class="selection-section">
        <h3 class="section-title">{{ t('commonComp.delScriptModal.scriptType') }}</h3>
        <div class="checkbox-container">
          <CheckboxGroup
            v-model:value="checked"
            :options="testTypeOptions"
            class="script-type-checkboxes" />
        </div>
        <div v-show="validated && !checked.length" class="validation-error">
          {{ t('commonComp.delScriptModal.selectScriptType') }}
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

/* Selection section */
.selection-section {
  padding: 8px 12px;
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: #262626;
  margin: 0 0 12px 0;
  padding-bottom: 8px;
  border-bottom: 1px solid #e8e8e8;
}

.checkbox-container {
  margin-top: 8px;
}

.script-type-checkboxes {
  display: flex;
  flex-direction: row;
  gap: 16px;
  flex-wrap: nowrap;
}

.validation-error {
  font-size: 12px;
  color: #ff4d4f;
  margin-top: 8px;
  padding: 4px 8px;
  background-color: #fff2f0;
  border: 1px solid #ffccc7;
  border-radius: 4px;
}

/* Deep style overrides */
:deep(.ant-checkbox-wrapper) {
  font-size: 12px;
  color: #262626;
  margin-right: 0;
  margin-bottom: 8px;
  padding: 4px 0;
}

/* Remove hover background on checkbox options for clean look */

:deep(.ant-checkbox) {
  margin-right: 8px;
}

:deep(.ant-checkbox-inner) {
  width: 16px;
  height: 16px;
}

:deep(.ant-checkbox-checked .ant-checkbox-inner) {
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
