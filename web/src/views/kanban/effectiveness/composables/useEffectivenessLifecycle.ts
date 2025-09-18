import { nextTick, ref, watch } from 'vue';
import { throttle } from 'throttle-debounce';
import { EffectivenessProps } from '../types';

/**
 * <p>
 * Effectiveness lifecycle management composable
 * </p>
 *
 * @param props - Component props
 * @param loadData - Function to load dashboard data
 * @param resizeCharts - Function to resize charts
 * @returns Object containing lifecycle management methods and state
 */
export function useEffectivenessLifecycle (
  props: EffectivenessProps,
  loadData: () => Promise<void>,
  resizeCharts: () => void
) {
  /** Flag to indicate if data should be loaded when component becomes visible */
  const shouldNotify = ref(false);

  /** Flag to indicate if charts should be resized when component becomes visible */
  const resizeNotify = ref(false);

  /** Flag to indicate if screen size is normal */
  const isNormalSize = ref(true);

  /**
   * <p>
   * Handles window resize events with throttling
   * </p>
   * <p>
   * Resizes charts when component is visible, otherwise sets notification flag
   * </p>
   */
  const handleWindowResize = throttle(500, () => {
    if (!props.onShow) {
      resizeNotify.value = true;
      return;
    }

    // Update screen size flag
    if (window.document.body.clientWidth < 1440) {
      isNormalSize.value = false;
    } else {
      isNormalSize.value = true;
    }

    nextTick(() => {
      resizeCharts();
    });
  });

  /**
   * <p>
   * Refreshes dashboard data
   * </p>
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
   * <p>
   * Sets up watchers for component props
   * </p>
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
        nextTick(() => {
          // Resize task type chart when count type changes
          // This is handled in the main component
        });
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
   * <p>
   * Sets up component lifecycle hooks
   * </p>
   * <p>
   * Initializes watchers and event listeners
   * </p>
   */
  const setupLifecycle = () => {
    // Initialize screen size flag
    if (window.document.body.clientWidth < 1440) {
      isNormalSize.value = false;
    } else {
      isNormalSize.value = true;
    }

    setupWatchers();

    // Add window resize event listener
    window.addEventListener('resize', handleWindowResize);
  };

  /**
   * <p>
   * Cleans up component lifecycle resources
   * </p>
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
    isNormalSize,

    // Methods
    handleWindowResize,
    refresh,
    setupLifecycle,
    cleanup
  };
}
