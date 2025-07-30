<script setup lang="ts">
import { nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue';
import { debounce } from 'throttle-debounce';
import { duration } from '@xcan-angus/infra';

export interface Props {
  itemWidth: number;// 每个元素的宽度
  dataSource: { [key: string]: any }[];
  spacing?: number;// 元素间隔
  marginBottom?: number;// 元素垂直间隔
}

const props = withDefaults(defineProps<Props>(), {
  itemWidth: 0,
  dataSource: () => [],
  spacing: 14,
  marginBottom: 14
});

const containerRef = ref<HTMLElement>();
const scrollbarWidth = ref(7);
const styleMap = ref({});
const canRender = ref(false);
const containerWidth = ref();

const getScrollbarWidth = () => {
  // 创建一个临时的 div 元素
  const outer = document.createElement('div');
  outer.style.visibility = 'hidden';
  outer.style.overflow = 'scroll'; // 强制出现滚动条
  outer.style.width = '100px'; // 设置宽度
  document.body.appendChild(outer);

  // 创建一个内部的 div 元素
  const inner = document.createElement('div');
  inner.style.width = '100%'; // 设置宽度为100%
  outer.appendChild(inner);

  // 计算滚动条的宽度
  const scrollbarWidth = outer.offsetWidth - inner.offsetWidth;

  // 移除临时元素
  outer.parentNode.removeChild(outer);

  return Math.floor(scrollbarWidth);
};

const resizeHandler = debounce(duration.resize, () => {
  if (!containerRef.value) {
    return;
  }

  // -1用来修复获取到dom的小数点自动取整问题
  let width = Math.floor(Math.floor(containerRef.value.clientWidth) - Math.floor((parseInt(getComputedStyle(containerRef.value).paddingLeft)) + Math.floor(parseInt(getComputedStyle(containerRef.value).paddingRight))) - scrollbarWidth.value - 1);
  if (width < 100) {
    width = containerWidth.value;
  }
  containerWidth.value = width;
  containerWidth.value = width;
  let index = 1;
  while (width >= (props.itemWidth + props.spacing) * index - props.spacing) {
    index++;
  }

  index--;
  if (index === 1) {
    const list = props.dataSource || [];
    for (let i = 0, len = list.length; i < len; i++) {
      let marginBottom = props.marginBottom;
      if (i === len - 1) {
        marginBottom = 0;
      }

      styleMap.value[list[i].server['x-xc-id']] = {
        width: '100%',
        flexGrow: 0,
        marginBottom: `${marginBottom}px`,
        marginRight: '0'
      };
    }
  } else {
    const x = Math.floor(Math.floor((width - Math.floor(props.itemWidth * index) + props.spacing - Math.floor(props.spacing * index))) / index);
    const _width = Math.floor(props.itemWidth + x);
    const list = props.dataSource || [];
    for (let i = 0, len = list.length; i < len; i++) {
      let spacing = props.spacing;
      if (i > 0 && (i % index) === (index - 1)) {
        spacing = 0;
      }

      let marginBottom = props.marginBottom;
      if (i > len - index) {
        marginBottom = 0;
      }

      styleMap.value[list[i].server['x-xc-id']] = {
        width: `${_width}px`,
        flexGrow: 0,
        marginBottom: `${marginBottom}px`,
        marginRight: `${spacing}px`
      };
    }
  }

  canRender.value = true;
});

onMounted(() => {
  scrollbarWidth.value = getScrollbarWidth();

  nextTick(() => {
    window.addEventListener('resize', resizeHandler);

    watch(() => props.dataSource, () => {
      resizeHandler();
    }, { deep: true, immediate: true });
  });
});

onBeforeUnmount(() => {
  if (containerRef.value) {
    window.addEventListener('resize', resizeHandler);
  }
});
</script>

<template>
  <div ref="containerRef" class="w-full flex items-start justify-start flex-wrap">
    <template v-if="canRender">
      <div
        v-for="(item) in props.dataSource"
        :key="item.server['x-xc-id']"
        :style="styleMap[item.server['x-xc-id']]">
        <slot name="default" v-bind="item"></slot>
      </div>
    </template>
  </div>
</template>
