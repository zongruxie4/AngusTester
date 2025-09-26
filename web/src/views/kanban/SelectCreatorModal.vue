<script lang="ts" setup>
import { useI18n } from 'vue-i18n';
import { Modal, Select } from '@xcan-angus/vue-ui';
import { RadioButton, RadioGroup } from 'ant-design-vue';
import type { CreatorSelectionPayload, SelectCreatorModalProps } from './types';
import { useCreatorSelection } from './composables/useCreatorSelection';
import { useCreatorSelectConfig } from './composables/useCreatorSelectConfig';

const { t } = useI18n();

const props = withDefaults(defineProps<SelectCreatorModalProps>(), {
  visible: false
});

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void;
  (e: 'ok', value: CreatorSelectionPayload): void;
}>();

// State & handlers for creator selection
const {
  creatorObjectType,
  creatorObjectId,
  creatorObjectName,
  avatar,
  handleChangeType: resetSelectionOnTypeChange,
  setCreator: onSelectCreator,
  buildPayload
} = useCreatorSelection();

// Dynamic select configuration derived from current type
const { action, placeholder, fieldNames } = useCreatorSelectConfig(creatorObjectType);

/**
 * <p>Close the modal and reset visibility flag in parent component.</p>
 */
const close = (): void => {
  emit('update:visible', false);
};

/**
 * <p>Emit confirmed selection if valid, then close the modal.</p>
 */
const confirmSelection = (): void => {
  const payload = buildPayload();
  if (payload) {
    emit('ok', payload);
  }
  close();
};
</script>
<template>
  <Modal
    :visible="props.visible"
    :title="t('kanban.selectOrganizationPersonnel')"
    @cancel="close"
    @ok="confirmSelection">
    <RadioGroup
      v-model:value="creatorObjectType"
      buttonStyle="solid"
      size="small"
      @change="resetSelectionOnTypeChange">
      <RadioButton value="USER">
        {{ t('organization.user') }}
      </RadioButton>
      <RadioButton value="DEPT">
        {{ t('organization.dept') }}
      </RadioButton>
      <RadioButton value="GROUP">
        {{ t('organization.group') }}
      </RadioButton>
    </RadioGroup>
    <div class="mt-3.5">
      <Select
        :key="creatorObjectType"
        v-model:value="creatorObjectId"
        class="w-100"
        :showSearch="true"
        :placeholder="placeholder"
        :action="action"
        :fieldNames="fieldNames"
        @change="onSelectCreator">
      </Select>
    </div>
  </Modal>
</template>
