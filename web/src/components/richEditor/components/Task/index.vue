<script setup lang="ts">
import { computed, ref } from 'vue';
import { TESTER } from '@xcan-angus/infra';
import { Icon, Popover, Scroll } from '@xcan-angus/vue-ui';

import { TaskInfo } from './PropsType';

interface Props {
  top: number,
  left: number,
  visible: boolean,
}
const props = withDefaults(defineProps<Props>(), {
  top: 0,
  left: 0,
  visible: false
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'change', val: TaskInfo),
  (e: 'update:visible', val: boolean),
}>();

const taskList = ref<TaskInfo[]>([]);
const scrollChange = (_data: TaskInfo[]) => {
  taskList.value = _data;
  if (!_data.length) {
    emit('update:visible', false);
  }
};

const onClick = (_data: TaskInfo) => {
  emit('change', _data);
  emit('update:visible', false);
};

const style = computed(() => {
  return {
    top: props.top + 'px',
    left: props.left + 'px'
  };
});

const fieldNames = {
  label: 'name',
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
          :action="`${TESTER}/task/`"
          :fieldNames="fieldNames"
          :pageSize="6"
          :transition="false"
          class="w-50"
          @change="scrollChange">
          <button
            v-for="item in taskList"
            :key="item.id"
            class="flex items-center w-full text-3 leading-3 px-2 pt-1.75 pb-2.5 rounded outline-none border-none cursor-pointer hover:hover:bg-gray-light-a text-theme-content"
            @click="onClick(item)">
            <Icon
              v-if="item.taskType.value==='API_TEST'"
              class="text-5 flex-shrink-0"
              icon="icon-jiekouceshi" />
            <Icon
              v-if="item.taskType.value==='SCENARIO_TEST'"
              class="text-5 flex-shrink-0"
              icon="icon-changjingceshi" />
            <Icon
              v-if="item.taskType.value==='BUG'"
              class="text-5 flex-shrink-0"
              icon="icon-quexian" />
            <Icon
              v-if="item.taskType.value==='STORY'"
              class="text-5 flex-shrink-0"
              icon="icon-gushi" />
            <Icon
              v-if="item.taskType.value==='TASK'"
              class="text-5 flex-shrink-0"
              icon="icon-renwu1" />
            <span :title="item.name" class="relative top-0.5 ml-1.5 truncate">{{ item.name }}</span>
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
