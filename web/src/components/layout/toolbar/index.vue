<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue';
import { Icon } from '@xcan-angus/vue-ui';

import { throttle } from 'throttle-debounce';

interface Props {
  height: number,
  maxHeight: number,
  menus: {
  name: string;
  value: string;
}[]
}

const props = withDefaults(defineProps<Props>(), {
  height: 0,
  menus: () => ([])
});


const emits = defineEmits<{
  (e: 'update:height', value: number | string): void;
  (e: 'update:tab', value: string): void;
  (e: 'update:moving', value: boolean): void;
  (e: 'change', value: string): void;
}>();

const height = ref(34);
const isSpread = ref(false);
const isFullScreen = ref(false);
const currentMenu = ref<string>();

const handleToggle = (): void => {
  if (isSpread.value) {
    emits('update:height', 34);
  } else {
    emits('update:height', 300);
  }
};

const handleZoom = (): void => {
  isFullScreen.value = !isFullScreen.value;
  if (isFullScreen.value) {
    height.value = props.maxHeight;
    emits('update:height', props.maxHeight);
  } else {
    height.value = 300;
    emits('update:height', 300);
  }
};

const handleSelected = (menu: {
  name: string;
  value: string;
}): void => {
  currentMenu.value = menu.value;
  if (!isSpread.value) {
    isSpread.value = true;
    emits('update:height', 300);
  }
  emits('update:tab', menu.value);
  emits('change', menu.value);
};

const spreadIcon = computed(() => {
  return isSpread.value ? 'icon-shouqi' : 'icon-spread';
});

const fullScreenIcon = computed(() => {
  return isFullScreen.value ? 'icon-tuichuzuida' : 'icon-zuidahua';
});

const contentStyle = computed(() => {
  return {
    height: height.value + 'px'
  };
});

const moving = ref(false); // 记录当前是否在拖拽中
let startY = 0; // 拖拽的起始位置

// 拖拽开始
const handleMouseDown = (e) => {
  moving.value = true;
  emits('update:moving', moving.value);
  startY = e.screenY as number;
  document.addEventListener('mousemove', handleMouseMove);
};
// 拖拽移动中
const handleMouseMove = throttle(10, (e) => {
  if (moving.value) {
    const moveH = startY - e.screenY;
    const h = height.value + moveH;
    height.value = h < 34
      ? 34
      : h > props.maxHeight
        ? props.maxHeight
        : h;
    startY = e.screenY;
    emits('update:height', height.value);
  }
});
// 拖拽结束
const handleMouseUp = () => {
  if (moving.value) {
    moving.value = false;
    emits('update:moving', moving.value);
    document.removeEventListener('mousemove', handleMouseMove);
  }
};
onBeforeUnmount(() => {
  document.removeEventListener('mouseup', handleMouseUp);
});
onMounted(() => {
  watch(() => props.height, (newValue) => {
    if (typeof newValue === 'number') {
      if (newValue > 34) {
        isSpread.value = true;
      } else {
        isSpread.value = false;
      }
      if (props.maxHeight === newValue || newValue > props.maxHeight) {
        isFullScreen.value = true;
      } else {
        isFullScreen.value = false;
      }
    }

    height.value = newValue;
  }, { immediate: true });

  document.addEventListener('mouseup', handleMouseUp);
});

defineExpose({ isSpread, handleToggle, handleSelected, currentMenu: currentMenu.value });
</script>
<template>
  <div
    :style="contentStyle"
    :class="{'toolbar-container': !moving}"
    style="z-index: 99;top: 0;"
    class="relative flex flex-col z-99 w-full flex-shrink-0 flex-grow-0 border-t overflow-hidden bg-white border-theme-text-box">
    <div
      class="flex items-center flex-shrink-0 flex-grow-0 justify-between h-8.5 px-5 border-b border-gray-light-b cursor-ns-resize bg-gray-light"
      @mousedown="handleMouseDown"
      @dblclick.stop="handleZoom">
      <ul class="flex flex-nowrap whitespace-nowrap overflow-hidden overflow-ellipsis select-none">
        <li
          v-for="item in menus"
          :key="item.value"
          :class="{ 'text-theme-special': currentMenu === item.value }"
          class="mr-7.5 text-3 leading-3 text-text-sub-content cursor-pointer hover:text-text-link-hover"
          @click="handleSelected(item)">
          {{ item.name }}
          <slot :name="item.value"></slot>
        </li>
      </ul>
      <div class="flex items-center">
        <div class="flex items-center flex-nowrap whitespace-nowrap ml-7.5">
          <slot name="actions"></slot>
          <Icon
            class="mr-7.5"
            :icon="spreadIcon"
            @click.stop="handleToggle" />
          <Icon :icon="fullScreenIcon" @click.stop="handleZoom" />
        </div>
      </div>
    </div>
    <slot name="content" :activeMenu="currentMenu"></slot>
  </div>
</template>
<style scoped>
.toolbar-container {
  @apply transition-all duration-500;
}

.icon {
  @apply text-3.5 leading-3.5 text-text-sub-content cursor-pointer;
}
</style>
