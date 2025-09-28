<script setup lang="ts">
import { Tooltip } from 'ant-design-vue';
import { Modal, Select } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import { AuthObjectType } from '@xcan-angus/infra';
import { useAddMember } from './composables/useAddMember';

interface Props {
  visible: boolean;
  appId: string;
  type: AuthObjectType.USER | AuthObjectType.DEPT | AuthObjectType.GROUP;
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  appId: undefined,
  type: AuthObjectType.USER
});

const emit = defineEmits<{(e: 'update', refresh: boolean): void }>();

const { t } = useI18n();

// Use composable for add member functionality
const {
  // Reactive data
  selectedUserIds,
  userError,
  selectedPolicyIds,
  policyError,

  // Computed properties
  title,
  placeholder,
  selectAction,
  policyAction,
  fieldNames,
  columnLabel,

  // Methods
  userChange,
  policyChange,
  handleSubmit
} = useAddMember(props.type, props.appId);

/**
 * Handle form submission
 */
const handleOk = async () => {
  const success = await handleSubmit(emit);
  if (success) {
    // Reset form fields after successful submission
    selectedUserIds.value = undefined;
    selectedPolicyIds.value = [];
    userError.value = false;
    policyError.value = false;
  }
};

/**
 * Handle modal cancellation
 */
const handleCancel = () => {
  emit('update', false);
  // Reset form fields
  selectedUserIds.value = undefined;
  selectedPolicyIds.value = [];
  userError.value = false;
  policyError.value = false;
};

/**
 * Get popup container for tooltips
 * @param el - The element to get the parent of
 * @returns The parent element or document body
 */
const getPopupContainer = (el: HTMLElement): HTMLElement => {
  if (el.parentElement) {
    return el.parentElement;
  }
  return document.body;
};
</script>
<template>
  <Modal
    :title="t('app.config.members.modal.addMember')"
    :visible="props.visible"
    :centered="true"
    :keyboard="true"
    :width="600"
    @cancel="handleCancel"
    @ok="handleOk">
    <div class="add-member-form">
      <!-- Tips Information -->
      <div class="form-tips">
        <div class="tips-icon">💡</div>
        <div class="tips-content">
          <p class="tips-title">{{ t('app.config.members.tips.title') }}</p>
          <p class="tips-text">{{ t('app.config.members.tips.description', {type: title}) }}</p>
        </div>
      </div>

      <div class="form-section">
        <div class="form-label">{{ columnLabel }}</div>
        <Select
          v-model:value="selectedUserIds"
          :placeholder="placeholder"
          :action="selectAction"
          :fieldNames="fieldNames"
          :error="userError"
          allowClear
          showSearch
          mode="multiple"
          class="w-full"
          optionLabelProp="label"
          @change="userChange">
          <template #option="record">
            <Tooltip
              :title="`ID: ${record[fieldNames.value]}`"
              placement="bottomLeft"
              :getPopupContainer="getPopupContainer">
              {{ record[fieldNames.label] }}
            </Tooltip>
          </template>
        </Select>
      </div>

      <div class="form-section mt-4">
        <div class="form-label">{{ t('organization.policy') }}</div>
        <Select
          v-model:value="selectedPolicyIds"
          :placeholder="t('organization.placeholders.selectPolicy')"
          class="w-full"
          mode="multiple"
          :error="policyError"
          :action="policyAction"
          :fieldNames="{ label: 'name', value: 'id' }"
          allowClear
          showSearch
          @change="policyChange">
        </Select>
      </div>
    </div>
  </Modal>
</template>
<style scoped>
.my-tabs-bordr {
  border-color: var(--content-special-text);
}

.add-member-form {
  padding: 8px 0;
}

.form-section {
  margin-bottom: 16px;
}

.form-label {
  margin-bottom: 6px;
  font-weight: 500;
  color: var(--theme-title);
  font-size: 12px;
}

/* Tips Information Styles */
.form-tips {
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
  border: 1px solid #bae7ff;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 18px;
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.tips-icon {
  font-size: 18px;
  margin-top: 2px;
  flex-shrink: 0;
}

.tips-content {
  flex: 1;
}

.tips-title {
  margin: 0 0 6px 0;
  font-weight: 600;
  font-size: 12px;
  color: var(--text-color, #262626);
  line-height: 1.4;
}

.tips-text {
  margin: 0;
  font-size: 13px;
  color: var(--text-color-secondary, #595959);
  line-height: 1.5;
}
</style>
