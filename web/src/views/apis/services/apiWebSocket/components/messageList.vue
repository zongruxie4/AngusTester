<script lang="ts" setup>
import { computed, ref, watch } from 'vue';
import { Message } from '../PropsType';
import { Arrow, Icon, IconCopy } from '@xcan-angus/vue-ui';

interface Props {
  keyword: string;
  msgType: string;
  data: Message[];
}
const props = withDefaults(defineProps<Props>(), {
  keyword: ''
});

const data = ref<Message[]>([]);

// const addMessage = (msg: Message) => {
//   data.value.push({ ...msg, date: dayjs().format('YYYY-MM-DD HH:mm:ss'), showContent: false });
// };

// const deleteAllMessage = () => {
//   data.value = [];
// };

const getMsgText = (type) => {
  switch (type) {
    case 'send': return '发送请求';
    case 'receive': return '接收消息';
    case 'connect': return '连接成功';
    case 'close': return '连接关闭';
    case 'closeErr': return '关闭失败';
    case 'sendErr': return '发送失败';
    case 'connectErr': return '连接失败';
  }
};

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
