import { useI18n } from 'vue-i18n';

/**
 * Dropdown menus configuration composable
 * Defines menu items for various dropdowns in the dataset list view
 */
export function useDropdownMenus () {
  const { t } = useI18n();

  /**
   * Button dropdown menu items for dataset creation
   * Provides options for creating different types of datasets
   */
  const buttonDropdownMenuItems = [
    {
      name: t('dataset.list.buttonDropdown.fileExtractDataset'),
      key: 'file',
      noAuth: true
    },
    {
      name: t('dataset.list.buttonDropdown.jdbcExtractDataset'),
      key: 'jdbc',
      noAuth: true
    }
  ];

  /**
   * Table dropdown menu items for dataset actions
   * Provides options for actions that can be performed on a dataset
   */
  const tableDropdownMenuItems = [
    {
      name: t('dataset.list.tableDropdown.previewData'),
      key: 'preview',
      icon: 'icon-zhengyan'
    },
    {
      name: t('actions.export'),
      key: 'export',
      icon: 'icon-daochu1'
    },
    {
      name: t('actions.clone'),
      key: 'clone',
      icon: 'icon-fuzhi'
    }
  ];

  /**
   * Handle table dropdown menu click
   * Routes actions to appropriate handlers based on menu item selection
   */
  const handleTableDropdownClick = (
    menuItem: { key: 'preview' | 'export' | 'clone' },
    data: any, // Dataset item
    openPreviewModal: (data: any) => void,
    openExportModal: (id?: string) => void,
    cloneDataset: (data: any, t: (key: string) => string) => Promise<void>
  ) => {
    const key = menuItem.key;
    if (key === 'preview') {
      openPreviewModal(data);
      return;
    }

    if (key === 'export') {
      openExportModal(data.id);
      return;
    }

    if (key === 'clone') {
      cloneDataset(data, t);
    }
  };

  return {
    // Menu configurations
    buttonDropdownMenuItems,
    tableDropdownMenuItems,

    // Methods
    handleTableDropdownClick
  };
}
