import { i18n } from '@xcan-angus/infra';

const t = i18n.getI18n()?.global?.t || ((value: string): string => value);

export const actions = [
  {
    name: t('service.sidebar.actions.addApi'),
    icon: 'icon-tianjiajiekou',
    key: 'add',
    permission: 'ADD',
    disabled: false,
    children: [
      {
        name: t('service.sidebar.actions.httpApi'),
        // icon: 'icon-tianjiajiekou',
        key: 'addApi',
        permission: 'ADD',
        disabled: false
      },
      {
        name: t('service.sidebar.actions.webSocketApi'),
        // icon: 'icon-tianjiajiekou',
        key: 'addSocket',
        permission: 'ADD',
        disabled: false
      }
    ]
  },
  {
    name: t('service.sidebar.actions.syncConfig'),
    icon: 'icon-peizhifuwutongbu',
    key: 'sync-config',
    permission: 'MODIFY',
    disabled: false
  },
  {
    name: t('service.sidebar.actions.securityConfig'),
    icon: 'icon-renzhengtou',
    key: 'authentication-config',
    permission: 'MODIFY',
    disabled: false
  },
  {
    name: t('service.sidebar.actions.serverConfig'),
    icon: 'icon-host',
    key: 'server-config',
    permission: 'MODIFY',
    disabled: false
  },
  {
    name: t('service.sidebar.actions.localImport'),
    icon: 'icon-daoru',
    key: 'local-import',
    permission: 'ADD',
    disabled: false
  },
  {
    name: t('service.sidebar.actions.exportApi'),
    icon: 'icon-daochujiekou',
    key: 'export-apis',
    permission: 'EXPORT',
    disabled: false
  },
  {
    name: t('service.sidebar.actions.mockService'),
    permission: 'VIEW',
    key: 'mock',
    icon: 'icon-mockjiedian',
    disabled: false
  },
  {
    name: t('actions.rename'),
    icon: 'icon-bianji',
    key: 'rename',
    permission: 'RENAME',
    disabled: false
  },
  {
    name: t('actions.delete'),
    icon: 'icon-qingchu',
    key: 'delete',
    permission: 'DELETE',
    disabled: false
  },
  {
    name: t('service.sidebar.actions.translate'),
    icon: 'icon-yuyan',
    key: 'translate',
    disabled: false
  },
  {
    name: t('actions.clone'),
    icon: 'icon-fuzhi',
    key: 'clone',
    permission: 'CLONE',
    disabled: false
  },
  {
    name: t('actions.permission'),
    icon: 'icon-quanxian1',
    key: 'auth',
    permission: 'GRANT',
    disabled: false
  },
  {
    name: t('service.sidebar.actions.modifyStatus'),
    key: 'setStatus',
    icon: 'icon-fabu',
    permission: 'MODIFY',
    disabled: false
  },
  {
    name: t('service.sidebar.actions.batchModifyParams'),
    key: 'batchModify',
    icon: 'icon-xiugai',
    permission: 'MODIFY',
    disabled: false,
    children: [
      {
        name: t('service.sidebar.actions.batchAddParams'),
        key: 'batchAddParams',
        icon: 'icon-piliangtianjiacanshu',
        permission: 'MODIFY'
      },
      {
        name: t('service.sidebar.actions.batchModifyParams'),
        key: 'batchModifyParams',
        icon: 'icon-piliangxiugaicanshu',
        permission: 'MODIFY'
      },
      {
        name: t('service.sidebar.actions.batchDelParams'),
        key: 'batchDelParams',
        icon: 'icon-piliangshanchucanshu',
        permission: 'MODIFY'
      },
      {
        name: t('service.sidebar.actions.batchEnabledParams'),
        key: 'batchEnabledParams',
        icon: 'icon-piliangqiyongcanshu',
        permission: 'MODIFY'
      },
      {
        name: t('service.sidebar.actions.batchDisabledParams'),
        key: 'batchDisabledParams',
        icon: 'icon-piliangjinyongcanshu',
        permission: 'MODIFY'
      },
      {
        name: t('service.sidebar.actions.batchModifyAuth'),
        key: 'batchModifyAuth',
        icon: 'icon-piliangxiugairenzheng',
        permission: 'MODIFY'
      },
      {
        name: t('service.sidebar.actions.batchModifyServer'),
        key: 'batchModifyServer',
        icon: 'icon-piliangxiugaifuwuqi',
        permission: 'MODIFY'
      },
      {
        name: t('service.sidebar.actions.batchLinkVariable'),
        key: 'batchLinkVariable',
        icon: 'icon-piliangyinyongbianliang',
        permission: 'MODIFY'
      },
      {
        name: t('service.sidebar.actions.batchDelVariable'),
        key: 'batchDelVariable',
        icon: 'icon-piliangquxiaoyinyongbianliang',
        permission: 'MODIFY'
      },
      {
        name: t('service.sidebar.actions.batchLinkDataSet'),
        key: 'batchLinkDataSet',
        icon: 'icon-piliangyinyongshujuji',
        permission: 'MODIFY'
      },
      {
        name: t('service.sidebar.actions.batchDelDataSet'),
        key: 'batchDelDataSet',
        icon: 'icon-piliangquxiaoyinyongshujuji',
        permission: 'MODIFY'
      }
    ]
  },
  {
    name: t('service.sidebar.actions.apiTestScript'),
    key: 'testScript',
    disabled: false,
    permission: 'TEST',
    icon: 'icon-ceshijiaoben',
    children: [
      {
        name: t('service.sidebar.actions.generateTestScript'),
        key: 'setTestScript',
        permission: 'TEST',
        icon: 'icon-shengchengceshijiaoben',
        tip: t('service.sidebar.actions.generateTestScriptTip')
      },
      {
        name: t('service.sidebar.actions.deleteTestScript'),
        key: 'delTestScript',
        permission: 'TEST',
        icon: 'icon-shanchuceshijiaoben',
        tip: t('service.sidebar.actions.deleteTestScriptTip')
      }
    ]
  },
  {

    name: t('service.sidebar.actions.enableDisableTest'),
    key: 'enabledTest',
    disabled: false,
    permission: 'MODIFY',
    icon: 'icon-zhibiao'
  },
  {
    name: t('service.sidebar.actions.execServiceTest'),
    key: 'execService',
    disabled: false,
    permission: 'TEST',
    icon: 'icon-ceshijiaoben',
    children: [
      {
        name: t('service.sidebar.actions.execSmokeTest'),
        key: 'funcTestExecSmoke',
        permission: 'TEST',
        icon: 'icon-gengxinceshijiaoben'
      },
      {
        name: t('service.sidebar.actions.execSecurityTest'),
        key: 'funcTestExecSecurity',
        permission: 'TEST',
        icon: 'icon-shanchuceshijiaoben'
      }
    ]
  },
  {
    name: t('service.sidebar.actions.execApiTest'),
    key: 'exec',
    disabled: false,
    permission: 'TEST',
    icon: 'icon-ceshijiaoben',
    children: [
      {
        name: t('service.sidebar.actions.execFuncTest'),
        key: 'funcTestExec',
        permission: 'TEST',
        icon: 'icon-shengchengceshijiaoben'
      },
      {
        name: t('service.sidebar.actions.execPerfTest'),
        key: 'perfTestExec',
        permission: 'TEST',
        icon: 'icon-shanchuceshijiaoben'
      },
      {
        name: t('service.sidebar.actions.execStabilityTest'),
        key: 'stabilityTestExec',
        permission: 'TEST',
        icon: 'icon-shanchuceshijiaoben'
      }
    ]
  },
  {
    name: t('service.sidebar.actions.apiTestTask'),
    key: 'testTask',
    disabled: false,
    permission: 'TEST',
    icon: 'icon-ceshirenwu',
    children: [
      {
        name: t('service.sidebar.actions.generateTestTask'),
        key: 'setTest',
        icon: 'icon-shengchengceshirenwu1',
        permission: 'TEST',
        disabled: false,
        tip: t('service.sidebar.actions.generateTestTaskTip')
      },
      {
        name: t('service.sidebar.actions.restartTestTask'),
        key: 'reTest',
        icon: 'icon-zhongxinkaishiceshi',
        permission: 'TEST',
        disabled: false,
        tip: t('service.sidebar.actions.restartTestTaskTip')
      },
      {
        name: t('service.sidebar.actions.reopenTestTask'),
        key: 'reopen',
        icon: 'icon-zhongxindakaiceshirenwu',
        permission: 'TEST',
        disabled: false,
        tip: t('service.sidebar.actions.reopenTestTaskTip')
      },
      {
        name: t('service.sidebar.actions.deleteTestTask'),
        key: 'deleteTask',
        icon: 'icon-shanchuceshirenwu1',
        permission: 'TEST',
        disabled: false,
        tip: t('service.sidebar.actions.deleteTestTaskTip')
      }
    ]
  }
];

export type SP = 'S' | 'P';

export type TargetTypeValue = 'SERVICE'|'PROJECT'

export interface ServiceProject {
  name: string,
  id: string,
  children: ServiceProject[],
  editable: boolean,
  auth:boolean;
  spread?: boolean,
  active?: boolean,
  pid?: string;
  level?: number;
  targetType: {
    value: TargetTypeValue;
    message?: string;
  }
}

export interface ModalsConfig {
  syncModalVisible: boolean,
  serverUrlModalVisible: boolean,
  importModalVisible: boolean,
  authenticatModalVisible: boolean,
  exportInterfaceModalVisible: boolean,
  testScriptVisible: boolean;
  shareModalVisible: boolean,
  authModalVisible: boolean,
  activeId: string,
  activeName: string,
  statusVisible: boolean;
  auth: boolean;
  type?: 'SERVICE';
  selectedNode?:ServiceProject;
  delTestScriptVisible: boolean;
  enabeldApiTestVisible: boolean;
}

export type UnarchivedItem = {
  id: string;
  protocol: {
    value: string;
    message: string;
  },
  method: string;
  endpoint: string;
  summary: string;
  createdDate: string;
  lastModifiedDate: string;
}
