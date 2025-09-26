<script lang="ts" setup>
import { DatePicker, Input, Modal } from '@xcan-angus/vue-ui';
import { Form, FormItem, Textarea } from 'ant-design-vue';
import { useI18n } from 'vue-i18n';
import { useVersionEdit } from './composables/useVersionEdit';
import type { VersionEditProps } from './types';

// Component props with default values
const props = withDefaults(defineProps<VersionEditProps>(), {
  visible: false,
  versionId: undefined,
  projectId: ''
});

const { t } = useI18n();

// Component emits
// eslint-disable-next-line func-call-spacing
const emits = defineEmits<{
  (e: 'cancel'): void;
  (e: 'ok'): void;
  (e: 'update:visible', value: boolean): void;
}>();

// Use version edit composable for form management
const { formState, loading, formRef, cancel, ok } = useVersionEdit(props);

// Override emits in composable
const handleCancel = () => {
  emits('update:visible', false);
  emits('cancel');
};

const handleOk = async () => {
  await ok();
  emits('ok');
  emits('update:visible', false);
};
</script>
<template>
  <Modal
    :title="props.versionId ? t('version.actions.editVersion') : t('version.actions.addVersion')"
    :visible="props.visible"
    :width="650"
    @cancel="handleCancel"
    @ok="handleOk">
    <Form
      ref="formRef"
      :model="formState"
      size="small"
      :labelCol="{ style: { width: '100px', textAlign: 'right', fontWeight: 'bold' } }"
      class="max-w-262.5"
      layout="horizontal">
      <FormItem
        required
        name="name"
        :label="t('common.name')">
        <Input
          v-model:value="formState.name"
          :maxlength="40"
          :placeholder="t('version.edit.versionNamePlaceholder')" />
      </FormItem>
      <div class="flex space-x-2">
        <FormItem
          :label="t('common.startDate')"
          class="flex-1 min-w-0"
          name="date">
          <div class="flex items-center space-x-1">
            <DatePicker
              v-model:value="formState.startDate"
              showToday
              showTime
              class="flex-1 min-w-0" />
          </div>
        </FormItem>
        <FormItem
          :label="t('common.releaseDate')"
          class="flex-1 min-w-0"
          name="time">
          <div class="w-full flex items-center space-x-1">
            <DatePicker
              v-model:value="formState.releaseDate"
              showTime
              class="flex-1" />
          </div>
        </FormItem>
      </div>
      <FormItem
        :label="t('common.description')"
        class="flex-1 !mb-5"
        name="content">
        <Textarea
          v-model:value="formState.description"
          :maxlength="200"
          :placeholder="t('version.edit.descriptionPlaceholder')">
          </Textarea>
      </FormItem>
    </Form>
  </Modal>
</template>
