import { ref, computed, onBeforeUnmount, onMounted } from 'vue';
import elementResizeDetector from 'element-resize-detector';
import { debounce } from 'throttle-debounce';
import { duration, AuthAppFuncTree, WebTagValue } from '@xcan-angus/infra';

/**
 * Composable for managing menu resize and visibility logic
 */
export function useMenuResize () {
  // Refs
  const containerRef = ref<HTMLElement>();
  const moreRef = ref<HTMLElement>();
  const menuList = ref<AuthAppFuncTree[]>([]);

  // Element resize detector
  const erd = elementResizeDetector({ strategy: 'scroll' });

  /**
   * Calculate more element width
   */
  const moreElementWidth = computed(() => {
    return moreRef.value?.offsetWidth || 0;
  });

  /**
   * Get visible menu items
   */
  const visibleMenuList = computed(() => {
    return menuList.value.filter(item => item.show);
  });

  /**
   * Get dropdown menu items
   */
  const dropdownMenuList = computed(() => {
    return menuList.value.filter(item => !item.show);
  });

  /**
   * Set menu attributes including width calculation
   */
  const setMenuAttributes = (menus: AuthAppFuncTree[]): AuthAppFuncTree[] => {
    const domList: HTMLElement[] = [];

    const processedMenus = menus?.map(item => {
      // Create ghost DOM element to measure text width
      const ghostDom = document.createElement('a');
      ghostDom.innerText = item.showName;
      ghostDom.setAttribute('style',
        'position:absolute;z-index:-999999;color:transparent;background:transparent;padding:0;font-size:14px;'
      );
      document.body.appendChild(ghostDom);
      domList.push(ghostDom);

      // Set width and show properties
      item.width = ghostDom.offsetWidth;
      item.show = true;

      return item;
    });

    // Clean up ghost DOM elements
    domList.forEach(item => {
      document.body.removeChild(item);
    });

    return processedMenus || [];
  };

  /**
   * Filter menus based on conditions
   */
  const filterMenus = (menus: AuthAppFuncTree[]): AuthAppFuncTree[] => {
    return menus.filter(item =>
      item.code !== 'Mock' &&
      (item.enabled || !item.authCtrl) &&
      (item.tags || []).some(tag => tag.name === WebTagValue.DYNAMIC_POSITION)
    );
  };

  /**
   * Handle container resize and adjust menu visibility
   */
  const handleResize = debounce(duration.resize, (): void => {
    const containerWidth = containerRef.value?.offsetWidth || 0;
    let totalWidth = 0;

    for (let i = 0, len = menuList.value.length; i < len; i++) {
      totalWidth += menuList.value[i].width + (i > 0 ? 28 : 0);

      if (totalWidth <= containerWidth) {
        menuList.value[i].show = true;
      } else {
        // Hide remaining items and check if "more" button fits
        for (let j = menuList.value.length; j--;) {
          if (j >= i) {
            menuList.value[j].show = false;
          } else {
            totalWidth -= (menuList.value[j].width + (j > 0 ? 28 : 0));
            if ((totalWidth + moreElementWidth.value + 28) <= containerWidth) {
              break;
            } else {
              menuList.value[j].show = false;
            }
          }
        }
        break;
      }
    }
  });

  /**
   * Initialize menu list with attributes
   */
  const initializeMenuList = (menus: AuthAppFuncTree[]): void => {
    const filteredMenus = filterMenus(menus);
    menuList.value = setMenuAttributes(filteredMenus);
  };

  /**
   * Setup resize listener
   */
  const setupResizeListener = (): void => {
    if (containerRef.value) {
      erd.listenTo(containerRef.value, handleResize);
    }
  };

  /**
   * Remove resize listener
   */
  const removeResizeListener = (): void => {
    if (containerRef.value) {
      erd.removeListener(containerRef.value, handleResize);
    }
  };

  /**
   * Initialize resize detection
   */
  onMounted(() => {
    setupResizeListener();
  });

  /**
   * Cleanup on unmount
   */
  onBeforeUnmount(() => {
    removeResizeListener();
  });

  return {
    // Refs
    containerRef,
    moreRef,
    menuList,

    // Computed
    moreElementWidth,
    visibleMenuList,
    dropdownMenuList,

    // Methods
    initializeMenuList,
    handleResize,
    setupResizeListener,
    removeResizeListener
  };
}
