import { ref, watch } from 'vue';
import { throttle } from 'throttle-debounce';
import { CtoProps } from '../types';

/**
 * CTO lifecycle management composable
 *
 * @param props - Component props
 * @param loadData - Function to load dashboard data
 * @param resizeCharts - Function to resize charts
 * @param resizeChartsByType - Function to resize charts by type
 * @returns Object containing lifecycle management methods and state
 */
export function useCtoLifecycle (
  props: CtoProps,
  loadData: () => Promise<void>,
  resizeCharts: () => void,
  resizeChartsByType: (countType: 'task' | 'useCase') => void
) {
  /** Flag to indicate if data should be loaded when component becomes visible */
  const shouldNotify = ref(false);

  /** Flag to indicate if charts should be resized when component becomes visible */
  const resizeNotify = ref(false);

  /**
   * Handles window resize events with throttling
   * <p>
   * Resizes charts when component is visible, otherwise sets notification flag
   * </p>
   */
  const handleWindowResize = throttle(500, () => {
    if (!props.onShow) {
      resizeNotify.value = true;
      return;
    }
    resizeCharts();
  });

  /**
   * Refreshes dashboard data
   * <p>
   * Only loads data if projectId is available
   * </p>
   */
  const refresh = async () => {
    if (!props.projectId) {
      return;
    }
    await loadData();
  };

  /**
   * Sets up watchers for component props
   * <p>
   * Watches for changes in filter parameters and triggers data loading
   * </p>
   */
  const setupWatchers = () => {
    // Watch for changes in filter parameters
    watch([
      () => props.createdDateEnd,
      () => props.createdDateStart,
      () => props.creatorObjectId,
      () => props.creatorObjectType,
      () => props.projectId,
      () => props.countType,
      () => props.planId,
      () => props.sprintId
    ], () => {
      if (!props.onShow && props.projectId) {
        shouldNotify.value = true;
        return;
      }

      if (props.projectId) {
        loadData();
      }
    }, {
      immediate: true
    });

    // Watch for changes in countType to resize appropriate charts
    watch(() => props.countType, (newValue) => {
      if (newValue) {
        resizeChartsByType(newValue);
      }
    });

    // Watch for changes in onShow to handle delayed operations
    watch(() => props.onShow, (newValue) => {
      if (newValue && shouldNotify.value && props.projectId) {
        refresh();
        shouldNotify.value = false;
      }

      if (newValue && resizeNotify.value && props.projectId) {
        handleWindowResize();
        resizeNotify.value = false;
      }
    });
  };

  /**
   * Sets up component lifecycle hooks
   * <p>
   * Initializes watchers and event listeners
   * </p>
   */
  const setupLifecycle = () => {
    setupWatchers();

    // Add window resize event listener
    window.addEventListener('resize', handleWindowResize);
  };

  /**
   * Cleans up component lifecycle resources
   * <p>
   * Removes event listeners and resets state
   * </p>
   */
  const cleanup = () => {
    window.removeEventListener('resize', handleWindowResize);
    shouldNotify.value = false;
    resizeNotify.value = false;
  };

  return {
    // State
    shouldNotify,
    resizeNotify,

    // Methods
    handleWindowResize,
    refresh,
    setupLifecycle,
    cleanup
  };
}
