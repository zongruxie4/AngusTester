import { ref, computed } from 'vue';
import { useI18n } from 'vue-i18n';

/**
 * Drawer management composable
 * Handles drawer state and menu items
 */
export function useDrawer (pageType: any, viewMode: any) {
  const { t } = useI18n();

  const activeDrawerKey = ref('basicInfo');
  const isEditFlag = ref<boolean>(viewMode.value === 'edit');

  /**
   * Drawer menu items configuration
   */
  const drawerMenuItems = computed(() => {
    if (pageType.value === 'create') {
      return [
        {
          key: 'basicInfo',
          name: t('common.basicInfo'),
          icon: 'icon-fuwuxinxi',
          noAuth: true
        }];
    }

    return [
      {
        key: 'basicInfo',
        name: t('common.basicInfo'),
        icon: 'icon-fuwuxinxi',
        noAuth: true
      },
      {
        key: 'execRecord',
        name: t('scriptDetail.tabs.executionRecord'),
        icon: 'icon-zhihangceshi',
        noAuth: true
      },
      {
        key: 'activity',
        icon: 'icon-lishijilu',
        name: t('scriptDetail.tabs.activity'),
        noAuth: true
      }
    ];
  });

  /**
   * Handle edit action
   */
  const handleEdit = (drawerRef: any) => {
    if (typeof drawerRef.value?.open === 'function') {
      drawerRef.value.open('basicInfo');
    }
    isEditFlag.value = true;
  };

  /**
   * Handle cancel action
   */
  const handleCancel = (oldScriptValue: any, scriptValue: any, viewMode: any, pageNo: any, pageSize: any, router: any) => {
    isEditFlag.value = false;
    if (viewMode.value === 'view') {
      scriptValue = oldScriptValue;
      return;
    }

    router.push(`/script?pageNo=${pageNo}&pageSize=${pageSize}`);
  };

  return {
    // Drawer state
    activeDrawerKey,
    isEditFlag,
    drawerMenuItems,

    // Drawer methods
    handleEdit,
    handleCancel
  };
}
