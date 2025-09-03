import { ref, computed } from 'vue';
import { useI18n } from 'vue-i18n';

/**
 * <p>Composable for managing drawer menu configuration and state.</p>
 * <p>Provides menu items and active key management for different drawer types.</p>
 *
 * @returns Object containing drawer menu configuration and state management
 */
export function useDrawerMenu () {
  const { t } = useI18n();

  // Drawer reference and active key
  const drawerRef = ref();
  const activeDrawerKey = ref<string>();

  /**
   * <p>Menu items for space information drawer.</p>
   * <p>Includes basic info and space capacity sections.</p>
   */
  const spaceDrawerMenu = computed(() => {
    return [
      {
        icon: 'icon-fuwuxinxi',
        name: t('fileSpace.drawer.basicInfo'),
        key: 'info'
      },
      {
        icon: 'icon-rongliang',
        name: t('fileSpace.drawer.spaceCapacity'),
        key: 'size'
      }
    ];
  });

  /**
   * <p>Menu items for file information drawer.</p>
   * <p>Includes basic info section for files and directories.</p>
   */
  const fileDrawerMenu = computed(() => {
    return [
      {
        icon: 'icon-fuwuxinxi',
        name: t('fileSpace.drawer.basicInfo'),
        key: 'info'
      }
    ];
  });

  /**
   * <p>Dynamic menu items based on selected row data.</p>
   * <p>Conditionally shows menu items based on available data.</p>
   *
   * @param selectedRow - Currently selected row data
   * @param selectedRowKey - Currently selected row key
   */
  const dynamicDrawerMenu = computed(() => {
    return (selectedRow: any, selectedRowKey: string) => {
      const { quotaSize } = selectedRow || {};
      return [
        selectedRowKey && {
          icon: 'icon-fuwuxinxi',
          name: t('fileSpace.drawer.basicInfo'),
          key: 'info'
        },
        selectedRowKey && {
          icon: 'icon-rongliang',
          name: selectedRowKey
            ? t('fileSpace.drawer.spaceCapacity') + (quotaSize?.value ? quotaSize.value + quotaSize.unit?.message : '')
            : t('fileSpace.drawer.accountStorageCapacity'),
          key: 'size'
        }
      ].filter(Boolean);
    };
  });

  /**
   * <p>Open drawer with specified menu key.</p>
   *
   * @param key - Menu key to open
   */
  const openDrawer = (key: string) => {
    if (drawerRef.value) {
      drawerRef.value.open(key);
    }
  };

  /**
   * <p>Close drawer and reset active key.</p>
   */
  const closeDrawer = () => {
    if (drawerRef.value) {
      drawerRef.value.close();
    }
    activeDrawerKey.value = undefined;
  };

  /**
   * <p>Set active drawer key.</p>
   *
   * @param key - Menu key to set as active
   */
  const setActiveKey = (key: string) => {
    activeDrawerKey.value = key;
  };

  return {
    // State
    drawerRef,
    activeDrawerKey,

    // Computed
    spaceDrawerMenu,
    fileDrawerMenu,
    dynamicDrawerMenu,

    // Methods
    openDrawer,
    closeDrawer,
    setActiveKey
  };
}
