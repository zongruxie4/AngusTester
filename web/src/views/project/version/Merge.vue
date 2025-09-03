<script lang="ts" setup>
import { ref } from 'vue';
import { Icon, Modal, Select } from '@xcan-angus/vue-ui';
import { Form, FormItem } from 'ant-design-vue';
import { useI18n } from 'vue-i18n';
import { TESTER } from '@xcan-angus/infra';
import { software } from '@/api/tester';
import type { VersionMergeProps, MergeFormState } from './types';

/**
 * Version merge modal component
 * Handles merging of two versions with validation and confirmation
 */

// Component props with default values
const props = withDefaults(defineProps<VersionMergeProps>(), {
  visible: false,
  projectId: ''
});

const { t } = useI18n();

// Component emits
const emits = defineEmits<{
  (e: 'cancel'): void;
  (e: 'ok', formId: string): void;
  (e: 'update:visible', value: boolean): void;
}>();

// Form state for merge operation
const formState = ref<MergeFormState>({
  formId: undefined,
  toId: undefined
});

const loading = ref(false);
const formRef = ref();

/**
 * Handle modal cancellation
 * Closes modal and emits cancel event
 */
const cancel = (): void => {
  emits('update:visible', false);
  emits('cancel');
};

/**
 * Handle merge operation
 * Validates form and performs version merge
 */
const ok = async (): Promise<void> => {
  formRef.value.validate().then(async () => {
    loading.value = true;
    const [error] = await software.mergeSoftwareVersion({
      ...formState.value
    });
    loading.value = false;

    if (error) {
      return;
    }

    emits('ok', formState.value.formId as string);
    emits('update:visible', false);
  });
};

/**
 * Field names configuration for select components
 * Maps API response fields to select component props
 */
const fieldNames = {
  value: 'id',
  label: 'name'
};
</script>
<template>
  <Modal
    :title="t('version.merge.title')"
    :visible="props.visible"
    :okButtonProps="{
      loading: loading,
      disabled: !formState.formId || !formState.toId || formState.toId === formState.formId
    }"
    @cancel="cancel"
    @ok="ok">
    <div class="border rounded border-status-warn py-5 px-2">
      <Icon icon="icon-jinggao" class="text-status-warn" />
      {{ t('version.messages.mergeWarning') }}
    </div>
    <Form
      ref="formRef"
      :model="formState"
      size="small"
      :labelCol="{ style: { width: '90px' } }"
      class="max-w-242.5 mt-4"
      layout="horizontal">
      <FormItem
        required
        name="formId"
        :label="t('version.merge.mergeVersion')">
        <Select
          v-model:value="formState.formId"
          :fieldNames="fieldNames"
          :lazy="false"
          :defaultActiveFirstOption="true"
          :action="`${TESTER}/software/version?projectId=${projectId}&fullTextSearch=true`" />
      </FormItem>
      <FormItem
        required
        :label="t('version.merge.mergeTo')"
        class="flex-1 !mb-5"
        name="toId">
        <Select
          v-model:value="formState.toId"
          :fieldNames="fieldNames"
          :lazy="false"
          :defaultActiveFirstOption="true"
          :action="`${TESTER}/software/version?projectId=${projectId}&fullTextSearch=true`" />
      </FormItem>
    </Form>
  </Modal>
</template>
