import { i18n } from '@xcan-angus/infra';

const t = i18n.getI18n()?.global?.t || ((value: string):string => value);

export const CollapseButtonGroup = [
  {
    label: t('service.apis.collapseButtonGroup.edit'),
    value: 'edit',
    auth: 'VIEW',
    icon: 'icon-bianji',
    disabled: false
  },
  {
    label: t('service.apis.collapseButtonGroup.clone'),
    value: 'patchClone',
    auth: 'VIEW',
    icon: 'icon-fuzhi',
    disabled: false
  },
  {
    label: t('service.apis.collapseButtonGroup.delete'),
    value: 'del',
    icon: 'icon-qingchu',
    auth: 'DELETE',
    disabled: false
  }
];

export const ButtonGroup = [
  {
    name: t('service.apis.buttonGroup.mockApi'),
    key: 'mock',
    permission: 'MOCK',
    icon: 'icon-mockjiedian'
  }, {
    name: t('service.apis.buttonGroup.addWatch'),
    key: 'addWatch',
    permission: 'VIEW',
    disabled: false,
    icon: 'icon-yiguanzhu'
  }, {
    name: t('service.apis.buttonGroup.cancelWatch'),
    key: 'cancelWatch',
    permission: 'VIEW',
    disabled: false,
    icon: 'icon-quxiaoguanzhu'
  }, {
    name: t('service.apis.buttonGroup.addFavourite'),
    key: 'addFavourite',
    permission: 'VIEW',
    disabled: false,
    icon: 'icon-shoucang2'
  }, {
    name: t('service.apis.buttonGroup.cancelFavourite'),
    key: 'cancelFavourite',
    permission: 'VIEW',
    disabled: false,
    icon: 'icon-quxiaoshoucang'
  },
  {
    name: t('service.apis.buttonGroup.remove'),
    key: 'remove',
    permission: 'MODIFY',
    disabled: false,
    icon: 'icon-yidong'
  }, {
    name: t('service.apis.buttonGroup.auth'),
    key: 'auth',
    permission: 'GRANT',
    disabled: false,
    icon: 'icon-quanxian1'
  }, {
    name: t('service.apis.buttonGroup.export'),
    key: 'export',
    permission: 'EXPORT',
    disabled: false,
    icon: 'icon-daochu'
  }, {
    name: t('service.apis.buttonGroup.status'),
    key: 'status',
    permission: 'MODIFY',
    disabled: false,
    icon: 'icon-shuxie'
  },
  {
    name: t('service.apis.buttonGroup.testScript'),
    key: 'testScript',
    permission: 'TEST',
    icon: 'icon-ceshijiaoben',
    children: [
      {
        name: t('service.apis.buttonGroup.generateTestScript'),
        key: 'setTestScript',
        permission: 'TEST',
        disabled: false,
        icon: 'icon-shengchengceshijiaoben',
        tip: t('service.apis.buttonGroup.generateTestScriptTip')
      },
      {
        name: t('service.apis.buttonGroup.deleteTestScript'),
        key: 'delTestScript',
        permission: 'TEST',
        disabled: false,
        icon: 'icon-shanchuceshijiaoben',
        tip: t('service.apis.buttonGroup.deleteTestScriptTip')
      }
    ]
  },
  {
    name: t('service.apis.buttonGroup.exec'),
    key: 'exec',
    disabled: false,
    permission: 'TEST',
    icon: 'icon-ceshijiaoben',
    children: [
      {
        name: t('service.apis.buttonGroup.funcTestExec'),
        key: 'funcTestExec',
        permission: 'TEST',
        icon: 'icon-shengchengceshijiaoben'
      },
      {
        name: t('service.apis.buttonGroup.perfTestExec'),
        key: 'perfTestExec',
        permission: 'TEST',
        icon: 'icon-shanchuceshijiaoben'
      },
      {
        name: t('service.apis.buttonGroup.stabilityTestExec'),
        key: 'stabilityTestExec',
        permission: 'TEST',
        icon: 'icon-shanchuceshijiaoben'
      }
    ]
  },
  {
    name: t('service.apis.buttonGroup.testTask'),
    key: 'testTask',
    permission: 'TEST',
    icon: 'icon-ceshirenwu',
    children: [
      {
        name: t('service.apis.buttonGroup.setTest'),
        key: 'setTest',
        permission: 'TEST',
        disabled: false,
        icon: 'icon-shengchengceshirenwu1',
        tip: t('service.apis.buttonGroup.setTestTip')
      },
      {
        name: t('service.apis.buttonGroup.reTest'),
        key: 'reTest',
        permission: 'TEST',
        disabled: false,
        icon: 'icon-zhongxinkaishiceshi',
        tip: t('service.apis.buttonGroup.reTestTip')
      },
      {
        name: t('service.apis.buttonGroup.reopen'),
        key: 'reopen',
        permission: 'TEST',
        disabled: false,
        icon: 'icon-zhongxindakaiceshirenwu',
        tip: t('service.apis.buttonGroup.reopenTip')
      },
      {
        name: t('service.apis.buttonGroup.deleteTask'),
        key: 'deleteTask',
        icon: 'icon-shanchuceshirenwu1',
        permission: 'TEST',
        disabled: false,
        tip: t('service.apis.buttonGroup.deleteTaskTip')
      }
    ]
  }];
