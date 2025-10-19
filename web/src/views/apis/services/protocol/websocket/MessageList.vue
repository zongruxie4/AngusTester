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

const data = ref<Message[]>([]);

const showData = computed(() => {
  if (props.keyword || props.msgType) {
    return data.value.filter(item => item.content?.includes(props.keyword) && (props.msgType ? item.type === props.msgType : true));
  }
  return data.value;
});

watch(() => props.data, newValue => {
  if (newValue) {
    data.value = newValue;
  }
}, {
  immediate: true
});

const getMsgText = (type) => {
  switch (type) {
    case 'send': return t('service.webSocketMsg.types.send');
    case 'receive': return t('service.webSocketMsg.types.receive');
    case 'connect': return t('service.webSocketMsg.types.connect');
    case 'close': return t('service.webSocketMsg.types.close');
    case 'closeErr': return t('service.webSocketMsg.types.closeErr');
    case 'sendErr': return t('service.webSocketMsg.types.sendErr');
    case 'connectErr': return t('service.webSocketMsg.types.connectErr');
  }
};
</script>
<template>
  <ul class="text-3">
    <li
      v-for="item in showData"
      :key="item.key"
      class="">
      <div class="flex items-center justify-between border-b py-2">
        <div class="flex items-center">
          <Icon v-if="item.type === 'send'" icon="icon-fasongxiaoxi" />
          <Icon v-else-if="item.type === 'receive'" icon="icon-jieshouchenggong" />
          <Icon
            v-else-if="item.type === 'connect'"
            icon="icon-duihao"
            class="text-status-success" />
          <Icon
            v-else-if="item.type === 'close'"
            icon="icon-chahao"
            class="text-gray-text" />
          <Icon
            v-else-if="item.type.includes('Err')"
            icon="icon-tuisongshibai" />
          <span class="ml-2">{{ getMsgText(item.type) }}</span>
        </div>
        <div class="flex items-center">
          <span class="w-40">{{ item.date }}</span>
          <span class="w-20">{{ item.size }}</span>
          <IconCopy :copyText="item.content" />
          <Arrow v-model:open="item.showContent" class="ml-7.5"></Arrow>
        </div>
      </div>
      <div v-show="!!item.showContent" class="bg-gray-2 p-3">
        {{ item.content }}
      </div>
    </li>
  </ul>
</template>
