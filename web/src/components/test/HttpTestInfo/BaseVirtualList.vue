
<script lang="ts" setup>
// Vue core imports
import { ref, computed, reactive, onUpdated, watchEffect } from 'vue';

/**
 * Component props interface for virtual list functionality
 */
interface Props {
  cache: number;
  data: any[];
  style?: string | Record<string, string>;
  showNum?: number;
  width?: number | string;
  height?: number;
  dynamic?: boolean;
  itemHeight?: number;
  className?: string;
}

// Component props with default values
const props = withDefaults(defineProps<Props>(), {
  cache: 2,
  data: () => ([]),
  style: undefined,
  showNum: 20,
  width: undefined,
  height: undefined,
  dynamic: false,
  itemHeight: 58,
  className: undefined
});

// Component state management
const virtualListState = reactive<any>({
  start: 0,
  end: props.showNum || 20,
  scrollOffset: 0,
  cacheData: []
});

// Component references
const virtualListContainerRef = ref();

/**
 * Computed property for wrapper container styles
 */
const wrapperContainerStyle = computed(() => {
  const { style, height, width } = props;
  const styleObject = typeof style === 'string' ? JSON.parse(style) : { ...style };
  return {
    height: `${height}px`,
    width: typeof width === 'number' ? `${width}px` : width,
    ...styleObject
  };
});

/**
 * Computed property for inner container styles
 */
const innerContainerStyle = computed(() => {
  return {
    height: `${totalContainerHeight.value}px`,
    width: '100%'
  };
});

/**
 * Computed property for virtual list styles
 */
const virtualListStyle = computed(() => {
  return {
    willChange: 'transform',
    transform: `translateY(${virtualListState.scrollOffset}px)`
  };
});

// Data and height calculations
const totalDataCount = computed(() => {
  return props.data.length;
});

/**
 * Computed property for total container height
 */
const totalContainerHeight = computed(() => {
  if (!props.dynamic) return totalDataCount.value * props.itemHeight;
  return calculateCurrentItemTop(totalDataCount.value);
});

/**
 * Computed property for visible items count in viewport
 */
const visibleItemsCount = computed(() => {
  return Math.ceil((props.height || 0) / props.itemHeight);
});

/**
 * Computed property for currently visible data items
 */
const visibleDataItems = computed(() => {
  return props.data.slice(virtualListState.start, virtualListState.end);
});

/**
 * Handle scroll events and update virtual list state
 * @param event - Scroll event object
 */
const handleScrollEvent = (event: any) => {
  const { scrollTop } = event.target;
  if (virtualListState.scrollOffset === scrollTop) return;
  const { cache, dynamic, itemHeight } = props;
  const cacheCount = Math.max(1, cache);

  let startIndex = dynamic ? findStartIndexByScrollPosition(scrollTop) : Math.floor(scrollTop / itemHeight);

  const endIndex = Math.max(0, Math.min(totalDataCount.value, startIndex + visibleItemsCount.value + cacheCount));

  if (startIndex > cacheCount) {
    startIndex = startIndex - cacheCount;
  }

  // Calculate scroll offset
  const scrollOffset = dynamic ? calculateCurrentItemTop(startIndex) : scrollTop - (scrollTop % itemHeight);

  Object.assign(virtualListState, {
    start: Math.max(0, startIndex),
    end: endIndex,
    scrollOffset: scrollOffset
  });
};

/**
 * Find start index using binary search algorithm for dynamic height items
 * @param scrollTop - Current scroll position
 * @returns Start index for visible items
 */
const findStartIndexByScrollPosition = (scrollTop = 0): number => {
  let low = 0;
  let high = virtualListState.cacheData.length - 1;
  while (low <= high) {
    const middle = low + Math.floor((high - low) / 2);
    const middleTopValue = calculateCurrentItemTop(middle);
    const middleBottomValue = calculateCurrentItemTop(middle + 1);

    if (middleTopValue <= scrollTop && scrollTop <= middleBottomValue) {
      return middle;
    } else if (middleBottomValue < scrollTop) {
      low = middle + 1;
    } else if (middleBottomValue > scrollTop) {
      high = middle - 1;
    }
  }
  return Math.min(totalDataCount.value - visibleItemsCount.value, Math.floor(scrollTop / props.itemHeight));
};

/**
 * Calculate the top position of an item at the given index
 * @param index - Item index
 * @returns Top position in pixels
 */
const calculateCurrentItemTop = (index: number) => {
  const lastIndex = virtualListState.cacheData.length - 1;

  if (index in virtualListState.cacheData) {
    return virtualListState.cacheData[index].top;
  } else if ((index - 1) in virtualListState.cacheData) {
    return virtualListState.cacheData[index - 1].bottom;
  } else if (index > lastIndex) {
    return virtualListState.cacheData[lastIndex].bottom + Math.max(0, index - virtualListState.cacheData[lastIndex].index) * props.itemHeight;
  } else {
    return index * props.itemHeight;
  }
};

/**
 * Update dynamic item heights after component updates
 */
onUpdated(() => {
  if (!props.dynamic) return;
  const childrenList = virtualListContainerRef.value.children || [];
  [...childrenList].forEach((node: any, index: number) => {
    const height = node.getBoundingClientRect().height;
    const currentIndex = virtualListState.start + index;
    if (virtualListState.cacheData[currentIndex].height === height) return;

    virtualListState.cacheData[currentIndex].height = height;
    virtualListState.cacheData[currentIndex].top = calculateCurrentItemTop(currentIndex);
    virtualListState.cacheData[currentIndex].bottom = virtualListState.cacheData[currentIndex].top + virtualListState.cacheData[currentIndex].height;
  });
});

/**
 * Initialize cache data for visible items
 */
watchEffect(() => {
  visibleDataItems.value.forEach((_, index) => {
    const currentIndex = virtualListState.start + index;
    if (currentIndex in virtualListState.cacheData) return;
    virtualListState.cacheData[currentIndex] = {
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
    :style="wrapperContainerStyle"
    @scroll="handleScrollEvent">
    <div
      ref="innerRef"
      class="base-virtual-inner"
      :style="innerContainerStyle">
      <div
        ref="virtualListContainerRef"
        class="base-virtual-list"
        :style="virtualListStyle">
        <slot
          v-for="(item, index) in visibleDataItems"
          :key="index + virtualListState.start"
          name="default"
          :item="item"
          :index="virtualListState.start + index">
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
