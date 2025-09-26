import { ref, h } from 'vue';
import { LoadingOutlined } from '@ant-design/icons-vue';
import { MockService } from '../types';
import { TESTER } from '@xcan-angus/infra';
import { useI18n } from 'vue-i18n';
import { MockServicePermission, MockServiceSource } from '@/enums/enums';

/**
 * Composable for managing mock service UI state and configurations
 * Handles search options, table columns, menus and other UI-related data
 */
export function useMockUI () {
  const { t } = useI18n();

  // Reactive state for UI elements
  const exportVisible = ref(false);
  const exportData = ref<MockService>();
  const authVisible = ref(false);
  const authData = ref<MockService>();

  const textList = [
    {
      icon: 'icon-shoudongchuangjianMockfuwu',
      name: t('mock.introduce.addMockService'),
      description: t('mock.introduce.addMockServiceDesc')
    },
    {
      icon: 'icon-daoruyiyoufuwujixitongyangli1',
      name: t('mock.introduce.importService'),
      description: t('mock.introduce.importServiceDesc')
    },
    {
      icon: 'icon-daoruxiangmufuwu',
      name: t('mock.introduce.importFile'),
      description: t('mock.introduce.importFileDesc')
    }
  ];

  // Search panel configuration
  const searchOptions = [
    {
      valueKey: 'name',
      type: 'input',
      placeholder: t('common.placeholders.searchKeyword'),
      allowClear: true,
      maxlength: 100
    },
    {
      valueKey: 'source',
      type: 'select-enum',
      enumKey: MockServiceSource,
      placeholder: t('mock.searchPanel.sourcePlaceholder'),
      allowClear: true
    },
    {
      valueKey: 'nodeId',
      type: 'select',
      action: `${TESTER}/node?fullTextSearch=true`,
      maxlength: 100,
      fieldNames: { label: 'name', value: 'id' },
      showSearch: true,
      placeholder: t('mock.searchPanel.nodeIdPlaceholder'),
      allowClear: true
    },
    {
      valueKey: 'createdBy',
      type: 'select-user',
      placeholder: t('mock.searchPanel.createdByPlaceholder'),
      maxlength: 100
    }
  ];

  // Table columns configuration
  const columns = [
    {
      title: t('common.id'),
      dataIndex: 'id',
      width: '8%',
      customCell: () => {
        return { style: 'white-space:nowrap;' };
      }
    },
    {
      title: t('common.name'),
      dataIndex: 'name',
      width: '13%'
    },
    {
      title: t('common.node'),
      dataIndex: 'nodeName',
      customRender: ({ text }: { text: string }): string => text || '--',
      width: '10%'
    },
    {
      title: t('common.address'),
      dataIndex: 'serviceHostUrl',
      customRender: ({ text }: { text: string }): string => text || '--'
    },
    {
      title: t('common.status'),
      dataIndex: 'status',
      width: '8%',
      customCell: () => {
        return { style: 'white-space:nowrap;' };
      }
    },
    {
      title: t('common.source'),
      dataIndex: 'source',
      width: '8%',
      customCell: () => {
        return { style: 'white-space:nowrap;' };
      }
    },
    {
      title: t('common.createdBy'),
      dataIndex: 'createdByName',
      width: '10%',
      groupName: 'createdByName',
      customRender: ({ text }: { text: string }) => text || '--',
      ellipsis: true
    },
    {
      title: t('common.lastModifiedBy'),
      dataIndex: 'lastModifiedByName',
      width: '10%',
      groupName: 'createdByName',
      hide: true,
      customRender: ({ text }: { text: string }) => text || '--',
      ellipsis: true
    },
    {
      title: t('common.createdDate'),
      dataIndex: 'createdDate',
      width: '10%',
      sorter: true,
      groupName: 'createdDate',
      customCell: () => {
        return { style: 'white-space:nowrap;' };
      }
    },
    {
      title: t('common.lastModifiedDate'),
      dataIndex: 'lastModifiedDate',
      width: '10%',
      sorter: true,
      groupName: 'createdDate',
      hide: true,
      customCell: () => {
        return { style: 'white-space:nowrap;' };
      }
    },
    {
      title: t('common.actions'),
      dataIndex: 'action',
      width: 160
    }
  ];

  // Dropdown menu items
  const actionMenus = [
    {
      key: 'del',
      icon: 'icon-qingchu',
      name: t('mock.actions.forceDelete'),
      permission: MockServicePermission.DELETE
    },
    {
      key: 'auth',
      icon: 'icon-quanxian1',
      name: t('actions.permission'),
      permission: MockServicePermission.GRANT
    },
    {
      key: 'export',
      icon: 'icon-daochu',
      name: t('actions.export'),
      permission: MockServicePermission.EXPORT
    },
    {
      key: 'reset',
      icon: 'icon-shuaxin',
      name: t('common.refresh'),
      permission: MockServicePermission.VIEW
    }
  ];

  // Status style mapping
  const statusStyleMap = {
    NOT_STARTED: '#B7BBC2',
    RUNNING: '#52c41a',
    STOPPED: '#abd3ff',
    STARTING: '#FF8100'
  };

  // Loading indicator
  const indicator = h(LoadingOutlined, {
    style: {
      fontSize: '16px'
    },
    spin: true
  });

  /**
   * Handle menu item click
   */
  const handleMenuClick = (
    key: string,
    record: MockService,
    openAuth: (item?: MockService) => void,
    handleExport: (item?: MockService) => void,
    handleResetInstance: (item: MockService) => void,
    forceDelete: (record: MockService) => void
  ) => {
    switch (key) {
      case 'del':
        forceDelete(record);
        break;
      case 'auth':
        openAuth(record);
        break;
      case 'export':
        handleExport(record);
        break;
      case 'reset':
        handleResetInstance(record);
        break;
    }
  };

  /**
   * Handle reset instance
   */
  const handleResetInstance = async (
    item: MockService,
    loading: any,
    modal: any,
    mock: any,
    notification: any
  ) => {
    if (loading.value) {
      return;
    }

    modal.confirm({
      centered: true,
      content: t('mock.refreshInstance_tip'),
      async onOk () {
        loading.value = true;
        const [error] = await mock.syncServiceInstanceConfig(item.id);
        loading.value = false;

        if (error) {
          return;
        }
        notification.success(t('mock.refreshInstance_success'));
      }
    });
  };

  /**
   * Handle export action
   */
  const handleExport = (item?: MockService) => {
    exportData.value = item;
    exportVisible.value = true;
  };

  /**
   * Handle authorization action
   */
  const openAuth = (item?: MockService) => {
    authData.value = item;
    authVisible.value = true;
  };

  /**
   * Handle authorization flag change
   */
  const handleAuthFlagChange = (
    { auth }: { auth: boolean },
    tableData: any,
    authData: any
  ) => {
    const data = tableData.value;
    const targetId = authData.value?.id;

    for (let i = 0, len = data.length; i < len; i++) {
      if (data[i].id === targetId) {
        data[i].auth = auth;
        break;
      }
    }
  };

  return {
    // State
    exportVisible,
    exportData,
    authVisible,
    authData,

    // UI Configurations
    textList,
    searchOptions,
    columns,
    actionMenus,
    statusStyleMap,
    indicator,

    // Methods
    handleMenuClick,
    handleResetInstance,
    handleExport,
    openAuth,
    handleAuthFlagChange
  };
}
