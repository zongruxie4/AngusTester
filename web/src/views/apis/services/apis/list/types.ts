import { i18n } from '@xcan-angus/infra';
import { ApiPermission } from '@/enums/enums';

const t = i18n.getI18n()?.global?.t || ((value: string):string => value);

export const CollapseButtonGroup = [
  {
    label: t('actions.edit'),
    value: 'edit',
    auth: ApiPermission.VIEW,
    icon: 'icon-bianji',
    disabled: false
  },
  {
    label: t('actions.clone'),
    value: 'patchClone',
    auth: ApiPermission.VIEW,
    icon: 'icon-fuzhi',
    disabled: false
  },
  {
    label: t('actions.delete'),
    value: 'del',
    icon: 'icon-qingchu',
    auth: ApiPermission.DELETE,
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
    permission: ApiPermission.VIEW,
    disabled: false,
    icon: 'icon-yiguanzhu'
  }, {
    name: t('actions.cancelFollow'),
    key: 'cancelFollow',
    permission: ApiPermission.VIEW,
    disabled: false,
    icon: 'icon-quxiaoguanzhu'
  }, {
    name: t('actions.addFavourite'),
    key: 'addFavourite',
    permission: ApiPermission.VIEW,
    disabled: false,
    icon: 'icon-shoucang2'
  }, {
    name: t('actions.cancelFavourite'),
    key: 'cancelFavourite',
    permission: ApiPermission.VIEW,
    disabled: false,
    icon: 'icon-quxiaoshoucang'
  },
  {
    name: t('actions.remove'),
    key: 'remove',
    permission: ApiPermission.MODIFY,
    disabled: false,
    icon: 'icon-yidong'
  }, {
    name: t('actions.permission'),
    key: 'auth',
    permission: ApiPermission.GRANT,
    disabled: false,
    icon: 'icon-quanxian1'
  }, {
    name: t('actions.export'),
    key: 'export',
    permission: ApiPermission.EXPORT,
    disabled: false,
    icon: 'icon-daochu'
  }, {
    name: t('common.status'),
    key: 'status',
    permission: ApiPermission.MODIFY,
    disabled: false,
    icon: 'icon-shuxie'
  },
  {
    name: t('service.apis.buttonGroup.testScript'),
    key: 'testScript',
    permission: ApiPermission.TEST,
    icon: 'icon-ceshijiaoben',
    children: [
      {
        name: t('service.apis.buttonGroup.generateTestScript'),
        key: 'setTestScript',
        permission: ApiPermission.TEST,
        disabled: false,
        icon: 'icon-shengchengceshijiaoben',
        tip: t('service.apis.buttonGroup.generateTestScriptTip')
      },
      {
        name: t('service.apis.buttonGroup.deleteTestScript'),
        key: 'delTestScript',
        permission: ApiPermission.TEST,
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
    permission: ApiPermission.TEST,
    icon: 'icon-ceshijiaoben',
    children: [
      {
        name: t('service.apis.buttonGroup.funcTestExec'),
        key: 'funcTestExec',
        permission: ApiPermission.TEST,
        icon: 'icon-shengchengceshijiaoben'
      },
      {
        name: t('service.apis.buttonGroup.perfTestExec'),
        key: 'perfTestExec',
        permission: ApiPermission.TEST,
        icon: 'icon-shanchuceshijiaoben'
      },
      {
        name: t('service.apis.buttonGroup.stabilityTestExec'),
        key: 'stabilityTestExec',
        permission: ApiPermission.TEST,
        icon: 'icon-shanchuceshijiaoben'
      }
    ]
  },
  // {
  //   name: t('service.apis.buttonGroup.testTask'),
  //   key: 'testTask',
  //   permission: ApiPermission.TEST,
  //   icon: 'icon-ceshirenwu',
  //   children: [
  //     {
  //       name: t('service.apis.buttonGroup.setTest'),
  //       key: 'setTest',
  //       permission: ApiPermission.TEST,
  //       disabled: false,
  //       icon: 'icon-shengchengceshirenwu1',
  //       tip: t('service.apis.buttonGroup.setTestTip')
  //     },
  //     {
  //       name: t('service.apis.buttonGroup.reTest'),
  //       key: 'reTest',
  //       permission: ApiPermission.TEST,
  //       disabled: false,
  //       icon: 'icon-zhongxinkaishiceshi',
  //       tip: t('service.apis.buttonGroup.reTestTip')
  //     },
  //     {
  //       name: t('actions.reopen'),
  //       key: 'reopen',
  //       permission: ApiPermission.TEST,
  //       disabled: false,
  //       icon: 'icon-zhongxindakaiceshirenwu',
  //       tip: t('service.apis.buttonGroup.reopenTip')
  //     },
  //     {
  //       name: t('service.apis.buttonGroup.deleteTask'),
  //       key: 'deleteTask',
  //       icon: 'icon-shanchuceshirenwu1',
  //       permission: ApiPermission.TEST,
  //       disabled: false,
  //       tip: t('service.apis.buttonGroup.deleteTaskTip')
  //     }
  //   ]
  // }
  ];
