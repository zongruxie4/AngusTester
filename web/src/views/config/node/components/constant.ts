import { i18n } from '@xcan-angus/infra';

const I18nInstance = i18n.getI18n();
const t = I18nInstance?.global?.t || ((value: string):string => value);

export const formItems = [
  {
    label: t('node.nodeItem.interface.formItems.nodeName'),
    inputType: 'text',
    placeholder: t('node.nodeItem.interface.formItems.inputLimit200'),
    valueKey: 'name',
    required: true,
    maxlength: 200,
    width: '18%'
  },
  {
    label: t('node.nodeItem.interface.formItems.intranetIP'),
    inputType: 'text',
    placeholder: t('node.nodeItem.interface.formItems.inputNodeIP'),
    valueKey: 'ip',
    required: true,
    maxlength: 15,
    width: '18%'
  },
  {
    label: t('node.nodeItem.interface.formItems.publicIP'),
    inputType: 'text',
    placeholder: t('node.nodeItem.interface.formItems.inputNodeIP'),
    valueKey: 'publicIp',
    required: false,
    maxlength: 15,
    width: '18%'
  },
  {
    label: t('node.nodeItem.interface.formItems.domain'),
    inputType: 'text',
    placeholder: t('node.nodeItem.interface.formItems.inputNodeDomain'),
    valueKey: 'domain',
    maxlength: 200,
    width: '33.3%'
  },
  {
    label: t('node.nodeItem.interface.formItems.username'),
    inputType: 'text',
    placeholder: t('node.nodeItem.interface.formItems.inputUsername'),
    valueKey: 'username',
    maxlength: 200,
    width: '18%'
  },
  {
    label: t('node.nodeItem.interface.formItems.password'),
    inputType: 'text',
    type: 'password',
    placeholder: t('node.nodeItem.interface.formItems.inputPassword'),
    valueKey: 'password',
    maxlength: 800,
    width: '18%'
  },
  {
    label: t('node.nodeItem.interface.formItems.sshPort'),
    inputType: 'number',
    placeholder: t('node.nodeItem.interface.formItems.inputSSHPort'),
    valueKey: 'sshPort',
    width: '18%'
  }
];

export const roleOptions = [
  {
    label: t('node.nodeItem.interface.roleOptions.managementNode'),
    value: 'manage'
  },
  {
    label: t('node.nodeItem.interface.roleOptions.pressureTestNode'),
    value: 'test'
  },
  {
    label: t('node.nodeItem.interface.roleOptions.mockNode'),
    value: 'mock'
  },
  {
    label: t('node.nodeItem.interface.roleOptions.applicationNode'),
    value: 'application'
  }
];

export const nodeStatus = [
  {
    label: t('common.enabledStatus'),
    valueName: {
      true: t('status.enabled'),
      false: t('status.disabled')
    },
    status: {
      true: 'success',
      false: 'fail'
    },
    valueKey: 'enabled'
  },
  {
    label: t('node.nodeItem.interface.nodeStatus.agentInstallStatus'),
    valueKey: 'installAgent',
    valueName: {
      true: t('node.nodeItem.interface.nodeStatus.installed'),
      false: t('node.nodeItem.interface.nodeStatus.notInstalled')
    },
    status: {
      true: 'success',
      false: 'fail'
    }
  },
  {
    label: t('node.nodeItem.interface.nodeStatus.connectionStatus'),
    valueKey: 'online',
    valueName: {
      true: t('node.nodeItem.interface.nodeStatus.connected'),
      false: t('node.nodeItem.interface.nodeStatus.notConnected')
    },
    status: {
      true: 'success',
      false: 'fail'
    }
  }
];

export const viewItem = [
  {
    label: t('common.id'),
    valueKey: 'id'
  },
  {
    label: t('node.nodeItem.interface.viewItem.intranetIP'),
    valueKey: 'ip'
  },
  {
    label: t('node.nodeItem.interface.viewItem.publicIP'),
    valueKey: 'publicIp'
  },
  {
    label: t('node.nodeItem.interface.viewItem.source'),
    valueKey: 'sourceName'
  },
  {
    label: t('node.nodeItem.interface.viewItem.role'),
    valueKey: 'roles',
    type: 'tag'
  },
  {
    label: t('node.nodeItem.interface.viewItem.specification'),
    valueKey: 'standard'
  },
  {
    label: t('node.nodeItem.interface.viewItem.system'),
    valueKey: 'os',
    type: 'tag'
  }
];

export const nodeUseProgresses = [
  {
    label: t('node.nodeItem.interface.nodeUseProgresses.cpu'),
    value: 90,
    valueKey: 'cpu'
  },
  {
    label: t('node.nodeItem.interface.nodeUseProgresses.memory'),
    value: 60,
    valueKey: 'memory'
  },
  {
    label: t('node.nodeItem.interface.nodeUseProgresses.fileSystem'),
    value: 45,
    valueKey: 'disk'
  },
  {
    label: t('node.nodeItem.interface.nodeUseProgresses.swapArea'),
    value: 0,
    valueKey: 'swap'
  },
  {
    label: t('actions.upload'),
    value: 29,
    valueKey: 'network-up'
  },
  {
    label: t('actions.download'),
    value: 29,
    valueKey: 'network-down'
  }
];
