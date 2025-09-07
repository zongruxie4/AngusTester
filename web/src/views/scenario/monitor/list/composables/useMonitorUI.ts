import type { Ref } from 'vue';
import { onBeforeUnmount, onMounted, ref } from 'vue';
import { debounce, throttle } from 'throttle-debounce';
import type { MonitorInfo, UseMonitorUIReturn } from '../types';

export function useMonitorUI (
  dataList: Ref<MonitorInfo[]>,
  total: Ref<number>,
  loading: Ref<boolean>,
  pageNo: Ref<number>,
  pageSize: Ref<number>,
  onShow: Ref<boolean>,
  loadData: () => Promise<void>
): UseMonitorUIReturn {
  // UI refs
  const dataListWrapRef = ref<HTMLElement>();

  // Track wrapper height for responsive pagination
  let wrapperHeight = 0;

  /**
   * Handle scroll events for infinite loading
   * @param event - Scroll event
   */
  const handleScrollList = throttle(500, (event: Event) => {
    if (dataList.value.length === total.value || loading.value) {
      return;
    }

    const target = event.currentTarget as HTMLElement;
    const clientHeight = target.clientHeight;
    const scrollHeight = target.scrollHeight;
    const scrollTop = target.scrollTop;

    // Load more data when near bottom
    if (clientHeight + scrollTop + 100 > scrollHeight) {
      pageNo.value += 1;
      loadData();
    }
  });

  /**
   * Handle window resize for responsive pagination
   * Adjusts page size based on container height
   */
  const handleWindowResize = debounce(300, () => {
    if (!onShow.value) {
      return;
    }

    const height = dataListWrapRef.value?.clientHeight || 0;
    if (height <= wrapperHeight) {
      return;
    }

    wrapperHeight = height;

    if (!dataList.value.length) {
      return;
    }

    // Calculate optimal page size based on container height
    if ((height / 160) > 4) {
      const rows = Math.floor(height / 160) + 2;
      pageSize.value = rows * 4;
    } else {
      pageSize.value = 16;
    }

    // Reload data if current data is less than new page size
    if (dataList.value.length < pageSize.value) {
      pageNo.value = 1;
      loadData();
    }
  });

  // Event listeners setup
  onMounted(() => {
    window.addEventListener('resize', handleWindowResize);
  });

  onBeforeUnmount(() => {
    window.removeEventListener('resize', handleWindowResize);
  });

  return {
    dataListWrapRef,
    handleScrollList,
    handleWindowResize
  };
}
