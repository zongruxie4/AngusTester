
<script lang="ts" setup>
import { computed, onUpdated, reactive, ref, watchEffect } from 'vue';

// Props: configuration for the virtual list behavior and appearance
interface Props {
  cache: number;
  data: any[];
  style?: string|Record<string, string>;
  showNum?: number;
  width?: number|string;
  height?: number;
  dynamic?: boolean;
  itemHeight?: number;
  className?: string;
}

const props = withDefaults(defineProps<Props>(), {
  cache: 2,
  data: () => ([]),
  style: undefined,
  showNum: 20,
  width: undefined,
  height: 120,
  dynamic: false,
  itemHeight: 58,
  className: undefined
});

// Internal state for current viewport window and cached measurements
interface CacheEntry {
  top: number;
  height: number;
  bottom: number;
  index: number;
}

const state = reactive<{ start: number; end: number; scrollOffset: number; cacheData: CacheEntry[] }>({
  start: 0,
  end: props.showNum || 20,
  scrollOffset: 0,
  cacheData: []
});

const wrapperRef = ref<HTMLElement | null>(null);
const innerRef = ref<HTMLElement | null>(null);
const virtualListRef = ref<HTMLElement | null>(null);

// Compute outer wrapper style from props, parsing string style defensively
const getWrapperStyle = computed(() => {
  const { style, height, width } = props;
  let styleObj: Record<string, string> = {};
  if (typeof style === 'string') {
    try {
      styleObj = JSON.parse(style);
    } catch {
      styleObj = {};
    }
  } else if (style) {
    styleObj = { ...(style as Record<string, string>) };
  }
  return {
    height: `${height}px`,
    width: typeof width === 'number' ? `${width}px` : width,
    ...styleObj
  };
});

// The inner container mimics the full height to enable native scrolling
const getInnerStyle = computed(() => {
  return {
    height: `${getTotalHeight.value}px`,
    width: '100%'
  };
});

// The translated list that positions the visible slice correctly
const getListStyle = computed(() => {
  return {
    willChange: 'transform',
    transform: `translateY(${state.scrollOffset}px)`
  };
});

// Total number of items
const totalItems = computed(() => {
  return props.data.length;
});

// Total scrollable height
const getTotalHeight = computed(() => {
  if (!props.dynamic) return totalItems.value * props.itemHeight;
  return getTopForIndex(totalItems.value);
});

// Number of items that can fit in the viewport (without cache)
const visibleCount = computed(() => {
  return Math.ceil(props.height / props.itemHeight);
});

// The currently visible data window
const visibleData = computed(() => {
  return props.data.slice(state.start, state.end);
});

// Scroll handler: compute next visible range and translate offset
const handleScroll = (e: Event) => {
  const target = e.target as HTMLElement;
  const { scrollTop } = target;
  if (state.scrollOffset === scrollTop) return;
  const { cache, dynamic, itemHeight } = props;
  const cacheCount = Math.max(1, cache);

  let startIndex = dynamic ? findStartIndex(scrollTop) : Math.floor(scrollTop / itemHeight);

  const endIndex = Math.max(0, Math.min(totalItems.value, startIndex + visibleCount.value + cacheCount));

  if (startIndex > cacheCount) {
    startIndex = startIndex - cacheCount;
  }

  // Translate offset to position the visible list correctly
  const offset = dynamic ? getTopForIndex(startIndex) : scrollTop - (scrollTop % itemHeight);

  Object.assign(state, {
    start: Math.max(0, startIndex),
    end: endIndex,
    scrollOffset: offset
  });
};

// Binary search to find the start index for a given scrollTop (dynamic mode)
const findStartIndex = (scrollTop = 0): number => {
  let low = 0;
  let high = state.cacheData.length - 1;
  while (low <= high) {
    const middle = low + Math.floor((high - low) / 2);
    const middleTopValue = getTopForIndex(middle);
    const middleBottomValue = getTopForIndex(middle + 1);

    if (middleTopValue <= scrollTop && scrollTop <= middleBottomValue) {
      return middle;
    } else if (middleBottomValue < scrollTop) {
      low = middle + 1;
    } else if (middleBottomValue > scrollTop) {
      high = middle - 1;
    }
  }
  return Math.min(totalItems.value - visibleCount.value, Math.floor(scrollTop / props.itemHeight));
};

// Get cumulative top position up to the given index using cached measurements when available
const getTopForIndex = (index: number) => {
  const lastIndex = state.cacheData.length - 1;
  if (lastIndex < 0) return 0;

  if (index in state.cacheData) {
    return state.cacheData[index].top;
  } else if ((index - 1) in state.cacheData) {
    return state.cacheData[index - 1].bottom;
  } else if (index > lastIndex) {
    return state.cacheData[lastIndex].bottom + Math.max(0, index - state.cacheData[lastIndex].index) * props.itemHeight;
  } else {
    return index * props.itemHeight;
  }
};

onUpdated(() => {
  if (!props.dynamic) return;
  const childrenList = (virtualListRef.value?.children || []) as unknown as HTMLElement[];
  [...childrenList].forEach((node: HTMLElement, index: number) => {
    const height = node.getBoundingClientRect().height;
    const currentIndex = state.start + index;
    if (!state.cacheData[currentIndex]) return;
    if (state.cacheData[currentIndex].height === height) return;

    state.cacheData[currentIndex].height = height;
    state.cacheData[currentIndex].top = getTopForIndex(currentIndex);
    state.cacheData[currentIndex].bottom = state.cacheData[currentIndex].top + state.cacheData[currentIndex].height;
  });
});

watchEffect(() => {
  visibleData.value.forEach((_, index) => {
    const currentIndex = state.start + index;
    if (currentIndex in state.cacheData) return;
    state.cacheData[currentIndex] = {
      top: currentIndex * props.itemHeight,
      height: props.itemHeight,
      bottom: (currentIndex + 1) * props.itemHeight,
      index: currentIndex
    };
  });
});
</script>

<template>
  <div
    ref="wrapperRef"
    :class="['base-virtual-wrapper', className]"
    :style="getWrapperStyle"
    @scroll="handleScroll">
    <div
      ref="innerRef"
      class="base-virtual-inner"
      :style="getInnerStyle">
      <div
        ref="virtualListRef"
        class="base-virtual-list"
        :style="getListStyle">
        <slot
          v-for="(item, index) in visibleData"
          :key="index + state.start"
          name="default"
          :item="item"
          :index="state.start + index">
        </slot>
      </div>
    </div>
  </div>
</template>

<style scoped>
.base-virtual-wrapper {
  position: relative;
  overflow-y: auto;
}
</style>
