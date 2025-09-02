import { ref } from 'vue';

/**
 * Toolbar management composable
 * Handles toolbar state and actions
 */
export function useToolbar() {
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