<script setup lang="ts">
import { computed, ref } from 'vue';
import { TESTER } from '@xcan-angus/tools';
import { Popover, Scroll } from '@xcan-angus/vue-ui';
import DOMPurify from 'dompurify';

import { RemarkInfo } from './PropsType';

interface Props {
    id:string,
    top: number,
    left: number,
    visible: boolean,
}
const props = withDefaults(defineProps<Props>(), {
  id: '',
  top: 0,
  left: 0,
  visible: false
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
    (e: 'change', val: RemarkInfo),
    (e: 'update:visible', val: boolean),
}>();

const taskList = ref<RemarkInfo[]>([]);
const scrollChange = (_data: RemarkInfo[]) => {
  taskList.value = _data?.map(item => {
    return {
      ...item,
      content: DOMPurify.sanitize(item.content)
    };
  });
  if (!_data.length) {
    emit('update:visible', false);
  }
};

const onClick = (_data: RemarkInfo) => {
  emit('change', _data);
  emit('update:visible', false);
};

const style = computed(() => {
  return {
    top: props.top + 'px',
    left: props.left + 'px'
  };
});

const params = computed(() => {
  return {
    taskId: props.id
  };
});
const fieldNames = {
  label: 'content',
  value: 'id'
};
</script>
<template>
  <Teleport to="body">
    <Popover
      :visible="props.visible"
      placement="rightTop"
      arrowPointAtCenter
      overlayClassName="rich-editor-popover">
      <template #content>
        <Scroll
          style="height: 160px;"
          :action="`${TESTER}/task/remark`"
          :fieldNames="fieldNames"
          :params="params"
          :pageSize="6"
          :transition="false"
          class="w-50"
          @change="scrollChange">
          <button
            v-for="item in taskList"
            :key="item.id"
            class="flex items-center w-full text-3 leading-3 px-2 pt-1.75 pb-2.5 rounded outline-none border-none cursor-pointer hover:hover:bg-gray-light-a text-theme-content"
            @click="onClick(item)">
            <span class="relative top-0.5 truncate" v-html="item.content"></span>
          </button>
        </Scroll>
      </template>
      <div :style="style" class="absolute -z-99 h-3 opacity-0">@</div>
    </Popover>
  </Teleport>
</template>
<style>
.rich-editor-popover .ant-popover-inner-content {
  padding: 8px 4px;
}
</style>
