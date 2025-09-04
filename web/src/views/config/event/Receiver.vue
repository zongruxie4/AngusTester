<script setup lang="ts">
import { onMounted } from 'vue';
import { useI18n } from 'vue-i18n';
import { PushSetting } from './types';
import { Modal, SelectUser } from '@xcan-angus/vue-ui';
import { Checkbox, CheckboxGroup, Form, FormItem } from 'ant-design-vue';
import { useReceiver } from './composables';

interface Props {
  visible: boolean,
  selectedItem: PushSetting
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  selectedItem: () => ({} as PushSetting)
});

const emit = defineEmits<{(e: 'update:visible', value: boolean): void, (e: 'refresh'): void }>();

const { t } = useI18n();

// Use composable for receiver management
const {
  // Reactive data
  receiversType,
  otherReceiversType,
  users,
  noticeTypeOpt,
  noticeType,
  receivingMethods,

  // Methods
  initReceiver,
  saveReceiver
} = useReceiver();

/**
 * Initialize receiver configuration
 */
const init = () => {
  initReceiver(props.selectedItem);
};

/**
 * Handle OK button click
 */
const handleOk = async () => {
  const success = await saveReceiver(props.selectedItem.id);
  if (success) {
    emit('update:visible', false);
    emit('refresh');
  }
};

/**
 * Handle cancel button click
 */
const handleCancel = () => {
  emit('update:visible', false);
};

// Initialize on component mount
onMounted(() => {
  init();
});
</script>

<template>
  <Modal
    :visible="props.visible"
    :title="t('event.receiver.title')"
    :centered="true"
    width="600px"
    @ok="handleOk"
    @cancel="handleCancel">
    <Form
      ref="formRef"
      size="small"
      layout="vertical">
      <FormItem :label="t('event.receiver.receiveMethod')">
        <CheckboxGroup v-model:value="receivingMethods" class="my-2">
          <Checkbox
            v-for="r in receiversType"
            :key="r.value"
            :value="r.value">
            {{ r.message }}
          </Checkbox>
        </CheckboxGroup>
        <div class="flex text-3">
          <span class="mr-2 leading-7">{{ otherReceiversType.message }}</span>
          <SelectUser
            v-model:value="users"
            :allowClear="false"
            :placeholder="t('event.receiver.selectReceiver')"
            class="flex-1"
            mode="multiple"
            size="small"
            style="width: 100%;"
            internal
            showSearch />
        </div>
      </FormItem>
      <FormItem :label="t('event.receiver.notificationMethod')">
        <CheckboxGroup
          v-model:value="noticeType"
          :options="noticeTypeOpt"
          class="my-2" />
      </FormItem>
    </Form>
  </Modal>
</template>
