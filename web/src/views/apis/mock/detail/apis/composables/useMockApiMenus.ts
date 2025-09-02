import { ref } from 'vue';
import { useI18n } from 'vue-i18n';

/**
 * Composable for managing menu items and dropdown operations
 * Handles context menu and dropdown menu configurations
 */
export function useMockApiMenus() {
  const { t } = useI18n();

  // Context menu trigger
  const trigger = ['contextmenu'];

  // Context menu items for API operations
  const menuItems = ref([
    {
      key: 'clone',
      icon: 'icon-fuzhizujian2',
      name: t('mock.mockApis.menuItems.clone'),
      permission: 'ADD'
    },
    {
      key: 'delete',
      icon: 'icon-fz',
      name: t('mock.mockApis.menuItems.delete'),
      permission: 'DELETE'
    },
    {
      key: 'export',
      icon: 'icon-daochu1',
      name: t('mock.mockApis.menuItems.export'),
      permission: 'EXPORT'
    }
  ]);

  // Dropdown menu items for main actions
  const dropdownMenuItems = ref([
    {
      key: 'copyApi',
      icon: 'icon-fuzhizujian2',
      name: t('mock.mockApis.menuItems.copyApi'),
      noAuth: true
    },
    {
      key: 'linkApi',
      icon: 'icon-yinyong',
      name: t('mock.mockApis.menuItems.linkApi'),
      noAuth: true
    },
    {
      key: 'import',
      icon: 'icon-daoru',
      name: t('mock.mockApis.menuItems.import'),
      noAuth: true
    },
    {
      key: 'importDemo',
      icon: 'icon-daoru',
      name: t('mock.mockApis.menuItems.importDemo'),
      noAuth: true
    }
  ]);

  // Sort menu items for list ordering
  const sortMenuItems = [
    {
      name: t('mock.mockApis.sortMenuItems.createdDate'),
      key: 'createdDate',
      orderSort: 'DESC'
    },
    {
      name: t('mock.mockApis.sortMenuItems.id'),
      key: 'id',
      orderSort: 'ASC'
    }
  ];

  return {
    trigger,
    menuItems,
    dropdownMenuItems,
    sortMenuItems
  };
}
