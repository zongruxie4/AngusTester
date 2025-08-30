<script setup lang="ts">
import { CheckboxGroup, Form, FormItem, Popover, Select, Tag } from 'ant-design-vue';
import { useI18n } from 'vue-i18n';
import { Modal } from '@xcan-angus/vue-ui';
import { useReceiveConfig } from './composables';

interface Props {
  visible: boolean;
  id: string;
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
  id: ''
});

const emit = defineEmits<{(e: 'update:visible', value: boolean): void }>();

const { t } = useI18n();

// Use composable for receive configuration management
const {
  // Reactive data
  eventTypes,
  selectedType,
  channelValues,

  // Methods
  initReceiveConfig,
  getOptions,
  getPlaceholder,
  saveChannelSetting
} = useReceiveConfig();

/**
 * Handle OK button click
 */
const handleOk = async () => {
  const success = await saveChannelSetting(props.id);
  if (success) {
    emit('update:visible', false);
  }
};

/**
 * Handle cancel button click
 */
const handleCancel = () => {
  emit('update:visible', false);
};

/**
 * Watch for visibility changes
 */
const watchVisible = (newValue: boolean) => {
  if (newValue) {
    initReceiveConfig(props.id);
  }
};

// Watch for visibility changes
watchVisible(props.visible);
</script>

<template>
  <Modal
    :visible="visible"
    :title="t('notification.receiveConfig.title')"
    :centered="true"
    width="800px"
    @ok="handleOk"
    @cancel="handleCancel">
    <Form size="small" :labelCol="{span: 6}">
      <FormItem
        :label="t('notification.receiveConfig.receiveChannel')"
        name="receiveSetting">
        <CheckboxGroup
          :options="eventTypes"
          :value="selectedType"
          disabled>
        </CheckboxGroup>
      </FormItem>
      <div class="border-t border-theme-text-box pt-6 -mt-2">
        <FormItem
          v-for="type in eventTypes"
          :key="type.value"
          :label="type.description"
          :name="type.value">
          <Select
            v-model:value="channelValues[type.value]"
            :fieldNames="{value: 'id', label: 'name'}"
            :options="getOptions(type.value)"
            :placeholder="getPlaceholder(type.value)"
            :disabled="!selectedType.includes(type.value)"
            mode="multiple">
            <template #tagRender="{option, label, closable, onClose}">
              <Popover>
                <template #content>
                  <div class="max-w-60 text-3 leading-4 break-words">{{ option.address }}</div>
                </template>
                <Tag :closable="closable" @close="onClose">{{ label[0] }}</Tag>
              </Popover>
            </template>
          </Select>
        </FormItem>
      </div>
    </Form>
  </Modal>
</template>
