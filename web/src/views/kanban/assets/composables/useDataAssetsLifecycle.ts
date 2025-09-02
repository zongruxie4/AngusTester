import { ref, watch, inject } from 'vue';
import { throttle } from 'throttle-debounce';
import elementResizeDetector from 'element-resize-detector';
import { DataAssetsProps, ProTypeShowMap } from '../types';

/**
 * <p>
 * Data assets lifecycle management composable
 * </p>
 * <p>
 * Manages component lifecycle, window resize events, and responsive behavior
 * </p>
 * 
 * @param props - Component props
 * @param loadGrowthTrendData - Function to load growth trend data
 * @param loadAllData - Function to load all data
 * @param refresh - Function to refresh data
 * @param resizeAllCharts - Function to resize all charts
 * @param handleRightSideResize - Function to handle right side chart resizing
 * @returns Object containing lifecycle management methods and state
 */
export function useDataAssetsLifecycle(
  props: DataAssetsProps,
  loadGrowthTrendData: (targetType: string) => Promise<void>,
  loadAllData: () => Promise<void>,
  refresh: () => Promise<void>,
  resizeAllCharts: () => void,
  handleRightSideResize: (width: number) => void
) {
  /** Project type show map from inject */
  const proTypeShowMap = inject<ProTypeShowMap>('proTypeShowMap', {
    showTask: true,
    showSprint: true
  });

  /** Target type for growth trend */
  const targetType = ref('TASK');
  
  /** Chart column count for responsive layout */
  const echartsCol = ref(2);
  
  /** Right side container width percentage */
  const rightPercent = ref('calc(25% - 24px)');
  
  /** Resize notification flag */
  const resizeNotify = ref(false);
  
  /** Should notify flag for data loading */
  const shouldNotify = ref(false);
  
  /** Right side container reference */
  const rightWrapRef = ref<HTMLElement>();

  /** Element resize detector instance */
  const erd = elementResizeDetector({ strategy: 'scroll' });

  /**
   * <p>
   * Handles window resize events
   * </p>
   * <p>
   * Updates chart column count and triggers chart resizing
   * </p>
   */
  const handleWindowResize = throttle(500, () => {
    if (!props.onShow) {
      resizeNotify.value = true;
      return;
    }
    
    if (window.document.body.clientWidth < 1560) {
      echartsCol.value = 2;
    } else {
      echartsCol.value = 3;
    }
    
    resizeAllCharts();
  });

  /**
   * <p>
   * Handles right side container resize
   * </p>
   * <p>
   * Adjusts chart configurations based on container width
   * </p>
   */
  const handleRightContainerResize = () => {
    if (rightWrapRef.value) {
      handleRightSideResize(rightWrapRef.value.clientWidth);
    }
  };

  /**
   * <p>
   * Sets up watchers for reactive data changes
   * </p>
   * <p>
   * Watches for changes in project type show map, target type, and other props
   * </p>
   */
  const setupWatchers = () => {
    // Watch for project type show map changes
    watch(() => proTypeShowMap.showTask, () => {
      if (!proTypeShowMap.showTask && targetType.value === 'TASK') {
        targetType.value = 'FUNC';
      }
    }, {
      immediate: true
    });

    // Watch for target type changes
    watch(() => targetType.value, () => {
      if (props.projectId) {
        loadGrowthTrendData(targetType.value);
      }
    });

    // Watch for project type show map changes
    watch(() => proTypeShowMap.showTask, () => {
      // Trigger chart initialization if needed
    }, {
      deep: true
    });

    // Watch for prop changes
    watch([
      () => props.createdDateEnd,
      () => props.createdDateStart,
      () => props.creatorObjectId,
      () => props.creatorObjectType,
      () => props.projectId
    ], () => {
      if (!props.onShow && props.projectId) {
        shouldNotify.value = true;
        return;
      }
      
      if (props.projectId) {
        loadAllData();
      }
    }, {
      immediate: true
    });

    // Watch for onShow changes
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
   * Sets up component lifecycle
   * </p>
   * <p>
   * Initializes watchers, event listeners, and responsive behavior
   * </p>
   */
  const setupLifecycle = () => {
    // Set initial chart column count
    if (window.document.body.clientWidth >= 1560) {
      echartsCol.value = 3;
    }

    // Setup watchers
    setupWatchers();

    // Add window resize listener
    window.addEventListener('resize', handleWindowResize);

    // Setup element resize detector for right side container
    if (rightWrapRef.value) {
      erd.listenTo(rightWrapRef.value, handleRightContainerResize);
    }
  };

  /**
   * <p>
   * Cleans up component lifecycle
   * </p>
   * <p>
   * Removes event listeners and cleanup resources
   * </p>
   */
  const cleanup = () => {
    window.removeEventListener('resize', handleWindowResize);
    
    if (rightWrapRef.value) {
      erd.removeListener(rightWrapRef.value, handleRightContainerResize);
    }
  };

  return {
    // Reactive state
    proTypeShowMap,
    targetType,
    echartsCol,
    rightPercent,
    resizeNotify,
    shouldNotify,
    rightWrapRef,
    
    // Methods
    handleWindowResize,
    handleRightContainerResize,
    setupWatchers,
    setupLifecycle,
    cleanup
  };
}
