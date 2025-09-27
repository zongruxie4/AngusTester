import { i18n } from '@xcan-angus/infra';

const t = i18n.getI18n()?.global?.t || ((value: string):string => value);

export const CollapseButtonGroup = [
  {
    label: t('actions.edit'),
    value: 'edit',
    auth: 'VIEW',
    icon: 'icon-bianji',
    disabled: false
  },
  {
    label: t('actions.clone'),
    value: 'patchClone',
    auth: 'VIEW',
    icon: 'icon-fuzhi',
    disabled: false
  },
  {
    label: t('actions.delete'),
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
    name: t('actions.addFollow'),
    key: 'addFollow',
    permission: 'VIEW',
    disabled: false,
    icon: 'icon-yiguanzhu'
  }, {
    name: t('actions.cancelFollow'),
    key: 'cancelFollow',
    permission: 'VIEW',
    disabled: false,
    icon: 'icon-quxiaoguanzhu'
  }, {
    name: t('actions.addFavourite'),
    key: 'addFavourite',
    permission: 'VIEW',
    disabled: false,
    icon: 'icon-shoucang2'
  }, {
    name: t('actions.cancelFavourite'),
    key: 'cancelFavourite',
    permission: 'VIEW',
    disabled: false,
    icon: 'icon-quxiaoshoucang'
  },
  {
    name: t('actions.remove'),
    key: 'remove',
    permission: 'MODIFY',
    disabled: false,
    icon: 'icon-yidong'
  }, {
    name: t('actions.permission'),
    key: 'auth',
    permission: 'GRANT',
    disabled: false,
    icon: 'icon-quanxian1'
  }, {
    name: t('actions.export'),
    key: 'export',
    permission: 'EXPORT',
    disabled: false,
    icon: 'icon-daochu'
  }, {
    name: t('common.status'),
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
    name: t('common.execution'),
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
        name: t('common.reopen'),
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
