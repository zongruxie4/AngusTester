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
      name: t('common.debugResult')
    },
    {
      key: 'logs',
      name: t('common.scheduleLog')
    },
    {
      key: 'execLog',
      name: t('common.execLog')
    }
  ];

  const TOOLBAR_EXTRA_MENU_ITEMS: {
    name:string;
    key:string;
  }[] = [
    {
      key: 'toggle',
      name: t('actions.toggle')
    },
    {
      key: 'screen',
      name: t('actions.fullScreen')
    }
  ];

  /**
   * Close toolbar
   */
  const closeToolbar = (toolbarRef: any) => {
    if (typeof toolbarRef.value?.open === 'function') {
      toolbarRef.value.open();
    }
  };

  /**
   * Toggle fullscreen mode
   */
  const toggleFullScreen = (toolbarRef: any) => {
    if (typeof toolbarRef.value?.fullScreen === 'function') {
      toolbarRef.value.fullScreen();
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
