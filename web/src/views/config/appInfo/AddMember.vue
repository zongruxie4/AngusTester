<script setup lang="ts">
import { Tooltip } from 'ant-design-vue';
import { Grid, Modal, Select } from '@xcan-angus/vue-ui';
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

/**
 * Grid columns configuration
 */
const gridColumns = [
  [
    {
      label: columnLabel.value,
      dataIndex: 'users'
    },
    {
      label: t('app.config.addMembers.columns.policy'),
      dataIndex: 'policys'
    }
  ]
];
</script>
<template>
  <Modal
    :title="title"
    :visible="props.visible"
    :centered="true"
    :keyboard="true"
    :width="600"
    @cancel="handleCancel"
    @ok="handleOk">
    <Grid :columns="gridColumns" marginBottom="24px">
      <template #users>
        <Select
          v-model:value="selectedUserIds"
          :placeholder="placeholder"
          :action="selectAction"
          :fieldNames="fieldNames"
          :error="userError"
          allowClear
          showSearch
          mode="multiple"
          class="w-full -mt-1.5"
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
      </template>
      <template #policys>
        <Select
          v-model:value="selectedPolicyIds"
          :placeholder="t('app.config.addMembers.placeholders.selectPolicy')"
          class="w-full -mt-1.5"
          mode="multiple"
          :error="policyError"
          :action="policyAction"
          :fieldNames="{ label: 'name', value: 'id' }"
          allowClear
          showSearch
          @change="policyChange">
        </Select>
      </template>
    </Grid>
  </Modal>
</template>
<style scoped>
.my-tabs-bordr {
  border-color: var(--content-special-text);
}
</style>
