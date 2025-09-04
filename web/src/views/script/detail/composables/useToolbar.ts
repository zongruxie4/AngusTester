import { ref } from 'vue';
import { useI18n } from 'vue-i18n';

/**
 * Toolbar management composable
 * Handles toolbar state and actions
 */
export function useToolbar () {
  const toolbarActiveKey = ref<'debugResult' | 'logs' | undefined>(undefined);
  const height = ref(34);
  const isFull = ref(false);
  const isOpen = ref(false);
  const isMoving = ref(false);

  const { t } = useI18n();

  const TOOLBAR_MENU_ITEMS: {
    name:string;
    key:string;
  }[] = [
    {
      key: 'debugResult',
      name: t('scriptDetail.toolbar.debugResult')
    },
    {
      key: 'logs',
      name: t('scriptDetail.toolbar.logs')
    },
    {
      key: 'execLog',
      name: t('scriptDetail.toolbar.execLog')
    }
  ];

  const TOOLBAR_EXTRA_MENU_ITEMS: {
    name:string;
    key:string;
  }[] = [
    {
      key: 'toggle',
      name: t('scriptDetail.toolbar.toggle')
    },
    {
      key: 'screen',
      name: t('scriptDetail.toolbar.screen')
    }
  ];

  /**
   * Close toolbar
   */
  const closeToolbar = (toolbarRef: any) => {
    if (typeof toolbarRef?.open === 'function') {
      toolbarRef.open();
    }
  };

  /**
   * Toggle fullscreen mode
   */
  const toggleFullScreen = (toolbarRef: any) => {
    if (typeof toolbarRef?.fullScreen === 'function') {
      toolbarRef.fullScreen();
    }
  };

  return {
    TOOLBAR_MENU_ITEMS,
    TOOLBAR_EXTRA_MENU_ITEMS,

    // Toolbar state
    toolbarActiveKey,
    height,
    isFull,
    isOpen,
    isMoving,

    // Toolbar methods
    closeToolbar,
    toggleFullScreen
  };
}
