import { nextTick, watch } from 'vue';
import elementResizeDetector from 'element-resize-detector';
import type { AuthSetProps } from '../types';

export function useAuthSetLifecycle (
  props: AuthSetProps,
  containerRef: any,
  resizeHandler: () => void,
  loadData: () => void,
  reset: () => void
) {
  const LINE_HEIGHT = 44;
  const erd = elementResizeDetector({ strategy: 'scroll' });

  /**
   * <p>
   * Calculates optimal page size based on container height
   * </p>
   *
   * @param height - Container height in pixels
   * @returns Calculated page size
   */
  const calculatePageSize = (height: number): number => {
    if (height <= LINE_HEIGHT) {
      return 1;
    }

    const quotient = height / LINE_HEIGHT;
    return Number.isInteger(quotient) ? quotient + 1 : Math.ceil(quotient);
  };

  /**
   * <p>
   * Initializes page size and loads data on component mount
   * </p>
   */
  const initializeOnMount = () => {
    nextTick(() => {
      if (!containerRef.value) {
        return;
      }

      const height = containerRef.value.offsetHeight;
      const calculatedPageSize = calculatePageSize(height);

      // Update page size if needed
      if (typeof window !== 'undefined' && window.pageSize) {
        window.pageSize = calculatedPageSize;
      }

      loadData();

      // Set up resize listener
      erd.listenTo(containerRef.value, resizeHandler);
    });
  };

  /**
   * <p>
   * Sets up watchers for prop changes
   * </p>
   */
  const setupWatchers = () => {
    watch(() => props.authObjectId, (newValue) => {
      reset();
      if (!newValue) {
        return;
      }
      loadData();
    });
  };

  /**
   * <p>
   * Cleanup function for component unmount
   * </p>
   */
  const cleanup = () => {
    erd.removeListener(containerRef.value, resizeHandler);
  };

  return {
    initializeOnMount,
    setupWatchers,
    cleanup
  };
}
