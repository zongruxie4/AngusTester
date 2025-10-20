import { ref, computed } from 'vue';
import { useI18n } from 'vue-i18n';
import { navs, menus } from './constants';

/**
 * UI state management composable
 * <p>
 * Manages component UI state including tabs, drawers, and toolbars.
 * </p>
 */
export function useUIState (props: {
  id?: string;
  valueObj: Record<string, any>;
}) {
  const { t } = useI18n();

  // Tab state
  const activeTabKey = ref('debug');
  const activeKey = ref('parameters');
  const currentTab = ref<string>('');

  // Drawer state
  const activeDrawerKey = ref();
  const shareVisible = ref(false);

  // Toolbar state
  const height = ref<number>(34);
  const maxHeight = ref(0);
  const moving = ref(false);

  // Error state
  const errorTitle = ref(t('service.apis.errors.requestErrorWithProxy'));

  // Computed - navigation menus
  const myNavs = computed(() => {
    if (props.valueObj.unarchived || !props.id) {
      const archivedNav = ['performance', 'variable', 'share', 'test', 'case', 'activity', 'apiMock'];
      return navs.filter(nav => !archivedNav.includes(nav.value)).map(i => ({ ...i, key: i.value }));
    }
    const unarchivedNav = ['saveUnarchived'];
    return navs.filter(nav => !unarchivedNav.includes(nav.value)).map(i => {
      if (i.value === 'save') {
        return {
          ...i,
          key: i.value,
          disabled: false, // permissions should be provided from outside
          name: t('actions.save')
        };
      }
      return {
        ...i,
        key: i.value,
        disabled: false // permissions should be provided from outside
      };
    });
  });

  // Computed - toolbar menus
  const toolbarMenus = computed(() => {
    // Menus may depend on whether WebSocket is available
    return menus;
  });

  /**
   * Handle share
   * <p>
   * Open share dialog
   * </p>
   */
  const handleShare = () => {
    shareVisible.value = true;
  };

  /**
   * Open response toolbar
   * <p>
   * Open response toolbar; if not expanded or no current tab, default to 'response'
   * </p>
   */
  const openToolBar = (toolbarRef: any) => {
    if (!toolbarRef.value.isSpread || !currentTab.value) {
      toolbarRef.value.handleSelected({ value: 'response' });
    }
    if (height.value < 300) {
      height.value = 300;
    }
  };

  /**
   * Handle toolbar menu change
   * <p>
   * Update current tab when selecting different toolbar menu
   * </p>
   * @param menuKey - Selected menu key
   */
  const toolbarChange = (menuKey: string) => {
    currentTab.value = menuKey;
  };

  /**
   * Set error title
   * <p>
   * Set different error titles based on whether WebSocket is available
   * </p>
   * @param hasWebSocket - Whether there is a WebSocket connection
   */
  const setErrTitle = (hasWebSocket: boolean) => {
    if (hasWebSocket) {
      errorTitle.value = t('service.apis.errors.requestErrorWithProxy');
    } else {
      errorTitle.value = t('service.apis.errors.requestErrorWithCors');
    }
  };

  /**
   * Close drawer
   * <p>
   * Close the currently opened drawer
   * </p>
   * @param drawerRef - Drawer ref
   */
  const closeDrawer = (drawerRef: any) => {
    drawerRef.value.close();
  };

  /**
   * Create resize handler
   * <p>
   * Debounced resize handler to update max height of main wrapper on window resize
   * </p>
   * @param mainWrapper - Main wrapper ref
   */
  const createResizeHandler = (mainWrapper: any) => {
    const resizeHandler = () => {
      if (mainWrapper.value) {
        maxHeight.value = mainWrapper.value.clientHeight;
      }
    };
    return resizeHandler;
  };

  /**
   * Reset UI state
   * <p>
   * Reset all UI-related states to initial values
   * </p>
   */
  const resetUIState = () => {
    activeTabKey.value = 'debug';
    activeKey.value = 'parameters';
    currentTab.value = '';
    activeDrawerKey.value = undefined;
    shareVisible.value = false;
    height.value = 34;
    maxHeight.value = 0;
    moving.value = false;
    errorTitle.value = t('service.apis.errors.requestErrorWithProxy');
  };

  return {
    // State
    activeTabKey,
    activeKey,
    currentTab,
    activeDrawerKey,
    shareVisible,
    height,
    maxHeight,
    moving,
    errorTitle,

    // Computed
    myNavs,
    toolbarMenus,

    // Methods
    handleShare,
    openToolBar,
    toolbarChange,
    setErrTitle,
    closeDrawer,
    createResizeHandler,
    resetUIState
  };
}
