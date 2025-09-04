import { ref } from 'vue';
import { useI18n } from 'vue-i18n';

export const TOOLBAR_MENU_ITEMS: {
  name:string;
  key:string;
}[] = [
  {
    key: 'debugResult',
    name: '调试结果'
  },
  {
    key: 'logs',
    name: '调度日志'
  },
  {
    key: 'execLog',
    name: '执行日志'
  }
];

export const TOOLBAR_EXTRA_MENU_ITEMS: {
  name:string;
  key:string;
}[] = [
  {
    key: 'toggle',
    name: '展开收起'
  },
  {
    key: 'screen',
    name: '全屏'
  }
];

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
