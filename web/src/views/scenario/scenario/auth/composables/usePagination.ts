import { computed, ref } from 'vue';
import { debounce, throttle } from 'throttle-debounce';
import { duration } from '@xcan-angus/infra';
import elementResizeDetector from 'element-resize-detector';

/**
 * Composable for handling pagination and container resizing
 */
export function usePagination (
  loadData: () => Promise<void>,
  reset: () => void
) {
  const containerRef = ref<HTMLElement>();
  const pageNo = ref(1);
  const pageSize = ref(1);
  const total = ref(0);
  const loading = ref(false);

  const erd = elementResizeDetector({ strategy: 'scroll' });

  // Computed properties
  const totalPage = computed(() => Math.ceil(total.value / pageSize.value));

  /**
   * Handle scroll event for infinite loading
   */
  const handleScroll = throttle(duration.scroll, (event: Event) => {
    if (loading.value || pageNo.value >= totalPage.value) {
      return;
    }

    const target = event.target as HTMLElement;
    const LINE_HEIGHT = 44;
    if ((target.scrollTop + target.clientHeight + LINE_HEIGHT) >= target.scrollHeight) {
      pageNo.value += 1;
      loadData();
    }
  });

  /**
   * Handle container resize to adjust page size
   */
  const handleResize = debounce(duration.resize, () => {
    if (!containerRef.value) {
      return;
    }

    const height = containerRef.value.offsetHeight;
    const LINE_HEIGHT = 44;
    let _pageSize = 1;

    if (height > LINE_HEIGHT) {
      const quotient = height / LINE_HEIGHT;
      if (Number.isInteger(quotient)) {
        _pageSize = quotient + 1;
      } else {
        _pageSize = Math.ceil(quotient);
      }
    }

    // Only update if we need more items and have more data to load
    if (pageSize.value < _pageSize && total.value > 0) {
      pageSize.value = _pageSize;
      reset();
      loadData();
    }
  });

  /**
   * Initialize pagination with container height
   */
  const initializePagination = () => {
    if (!containerRef.value) return;

    const height = containerRef.value.offsetHeight;
    const LINE_HEIGHT = 44;
    let _pageSize = 1;

    if (height > LINE_HEIGHT) {
      const quotient = height / LINE_HEIGHT;
      if (Number.isInteger(quotient)) {
        _pageSize = quotient + 1;
      } else {
        _pageSize = Math.ceil(quotient);
      }
    }

    pageSize.value = _pageSize;
  };

  /**
   * Setup resize listener
   */
  const setupResizeListener = () => {
    if (containerRef.value) {
      erd.listenTo(containerRef.value, handleResize);
    }
  };

  /**
   * Remove resize listener
   */
  const removeResizeListener = () => {
    if (containerRef.value) {
      erd.removeListener(containerRef.value, handleResize);
    }
  };

  return {
    // Refs
    containerRef,
    pageNo,
    pageSize,
    total,
    loading,

    // Computed
    totalPage,

    // Methods
    handleScroll,
    handleResize,
    initializePagination,
    setupResizeListener,
    removeResizeListener
  };
}
