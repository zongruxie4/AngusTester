<script lang="ts" setup>
import { computed, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Message } from './types';
import { Arrow, Icon, IconCopy } from '@xcan-angus/vue-ui';

interface Props {
  keyword: string;
  msgType: string;
  data: Message[];
}

const props = withDefaults(defineProps<Props>(), {
  keyword: ''
});

const { t } = useI18n();

/**
 * Internal message data array
 */
const messageData = ref<Message[]>([]);

/**
 * Computed property for filtered and displayed messages
 * <p>
 * Filters messages based on search keyword and message type, returns all messages if no filters applied
 * </p>
 */
const filteredMessages = computed(() => {
  if (props.keyword || props.msgType) {
    return messageData.value.filter(message =>
      message.content?.includes(props.keyword) &&
      (props.msgType ? message.type === props.msgType : true)
    );
  }
  return messageData.value;
});

/**
 * Watches for changes in props data
 * <p>
 * Updates internal message data when props change
 * </p>
 */
watch(() => props.data, newValue => {
  if (newValue) {
    messageData.value = newValue;
  }
}, {
  immediate: true
});

/**
 * Gets localized text for message type
 * <p>
 * Returns the appropriate translation key for different WebSocket message types
 * </p>
 * @param messageType - The type of the message
 * @returns Localized text for the message type
 */
const getMessageTypeText = (messageType: string): string => {
  switch (messageType) {
    case 'send': return t('service.webSocketMsg.types.send');
    case 'receive': return t('service.webSocketMsg.types.receive');
    case 'connect': return t('service.webSocketMsg.types.connect');
    case 'close': return t('service.webSocketMsg.types.close');
    case 'closeErr': return t('service.webSocketMsg.types.closeErr');
    case 'sendErr': return t('service.webSocketMsg.types.sendErr');
    case 'connectErr': return t('service.webSocketMsg.types.connectErr');
    default: return messageType;
  }
};
</script>
<template>
  <ul class="text-3">
    <li
      v-for="message in filteredMessages"
      :key="message.key"
      class="message-item">
      <div class="flex items-center justify-between border-b py-2">
        <div class="flex items-center">
          <Icon v-if="message.type === 'send'" icon="icon-fasongxiaoxi" />
          <Icon v-else-if="message.type === 'receive'" icon="icon-jieshouchenggong" />
          <Icon
            v-else-if="message.type === 'connect'"
            icon="icon-duihao"
            class="text-status-success" />
          <Icon
            v-else-if="message.type === 'close'"
            icon="icon-chahao"
            class="text-gray-text" />
          <Icon
            v-else-if="message.type.includes('Err')"
            icon="icon-tuisongshibai" />
          <span class="ml-2">{{ getMessageTypeText(message.type) }}</span>
        </div>
        <div class="flex items-center">
          <span class="w-40">{{ message.date }}</span>
          <span class="w-20">{{ message.size }}</span>
          <IconCopy :copyText="message.content" />
          <Arrow v-model:open="message.showContent" class="ml-7.5"></Arrow>
        </div>
      </div>
      <div v-show="!!message.showContent" class="bg-gray-2 p-3">
        {{ message.content }}
      </div>
    </li>
  </ul>
</template>
