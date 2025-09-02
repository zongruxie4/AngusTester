import { ref, h } from 'vue';
import { LoadingOutlined } from '@ant-design/icons-vue';
import { MockServiceObj } from '../types';
import { MockServiceSource } from '@/enums/enums';
import { TESTER } from '@xcan-angus/infra';

/**
 * Composable for managing mock service UI state and configurations
 * Handles search options, table columns, menus and other UI-related data
 */
export function useMockUI () {
  const t = (key: string) => key; // Mock i18n function

  // Reactive state for UI elements
  const exportVisible = ref(false);
  const exprotData = ref<MockServiceObj>();
  const authVisible = ref(false);
  const authData = ref<MockServiceObj>();

  // Search panel configuration
  const searchOptions = [
    {
      valueKey: 'name',
      type: 'input',
      placeholder: t('mock.searchPanel.namePlaceholder'),
      allowClear: true,
      maxlength: 100
    },
    {
      valueKey: 'nodeIp',
      type: 'input',
      op: 'EQUAL',
      placeholder: t('mock.searchPanel.nodeIpPlaceholder'),
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
    },
    {
      valueKey: 'createdDate',
      type: 'date-range',
      placeholder: [t('mock.searchPanel.createdDatePlaceholder1'), t('mock.searchPanel.createdDatePlaceholder2')],
      allowClear: true,
      showTime: true
    }
  ];

  // Table columns configuration
  const columns = [
    {
      title: t('mock.columns.id'),
      dataIndex: 'id',
      width: '10%',
      customCell: () => {
        return { style: 'white-space:nowrap;' };
      }
    },
    {
      title: t('mock.columns.name'),
      dataIndex: 'name',
      width: '13%'
    },
    {
      title: t('mock.columns.node'),
      dataIndex: 'nodeName',
      customRender: ({ text }: { text: string }): string => text || '--',
      width: '10%'
    },
    {
      title: t('mock.columns.address'),
      dataIndex: 'serviceHostUrl',
      customRender: ({ text }: { text: string }): string => text || '--'
    },
    {
      title: t('mock.columns.status'),
      dataIndex: 'status',
      width: '7%',
      customCell: () => {
        return { style: 'white-space:nowrap;' };
      }
    },
    {
      title: t('mock.columns.source'),
      dataIndex: 'source',
      width: '7%',
      customCell: () => {
        return { style: 'white-space:nowrap;' };
      }
    },
    {
      title: t('mock.columns.createdBy'),
      dataIndex: 'createdByName',
      width: '8%',
      groupName: 'createdByName',
      customRender: ({ text }: { text: string }) => text || '--',
      ellipsis: true
    },
    {
      title: t('mock.columns.lastModifiedBy'),
      dataIndex: 'lastModifiedByName',
      width: '8%',
      groupName: 'createdByName',
      hide: true,
      customRender: ({ text }: { text: string }) => text || '--',
      ellipsis: true
    },
    {
      title: t('mock.columns.createdDate'),
      dataIndex: 'createdDate',
      width: '10%',
      sorter: true,
      groupName: 'createdDate',
      customCell: () => {
        return { style: 'white-space:nowrap;' };
      }
    },
    {
      title: t('mock.columns.lastModifiedDate'),
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
      title: t('mock.columns.action'),
      dataIndex: 'action',
      width: 140
    }
  ];

  // Dropdown menu items
  const menus = [
    {
      key: 'del',
      icon: 'icon-qingchu',
      name: t('mock.actions.delete'),
      permission: 'DELETE',
      disabled: true
    },
    {
      key: 'auth',
      icon: 'icon-quanxian1',
      name: t('mock.actions.authority'),
      permission: 'GRANT'
    },
    {
      key: 'export',
      icon: 'icon-daochu',
      name: t('actions.export'),
      permission: 'EXPORT'
    },
    {
      key: 'reset',
      icon: 'icon-shuaxin',
      name: t('mock.actions.refresh'),
      permission: 'MODIFY'
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
    record: MockServiceObj,
    openAuth: (item?: MockServiceObj) => void,
    handleExport: (item?: MockServiceObj) => void,
    handleResetInstance: (item: MockServiceObj) => void,
    forceDelete: (record: MockServiceObj) => void
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
    item: MockServiceObj,
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
  const handleExport = (item?: MockServiceObj) => {
    exprotData.value = item;
    exportVisible.value = true;
  };

  /**
   * Handle authorization action
   */
  const openAuth = (item?: MockServiceObj) => {
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
    exprotData,
    authVisible,
    authData,

    // UI Configurations
    searchOptions,
    columns,
    menus,
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
