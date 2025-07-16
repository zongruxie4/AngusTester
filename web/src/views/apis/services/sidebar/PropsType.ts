export const actions = [
  {
    name: '添加接口',
    icon: 'icon-tianjiajiekou',
    key: 'add',
    permission: 'ADD',
    disabled: false,
    children: [
      {
        name: 'Http接口',
        // icon: 'icon-tianjiajiekou',
        key: 'addApi',
        permission: 'ADD',
        disabled: false
      },
      {
        name: 'WebSocket接口',
        // icon: 'icon-tianjiajiekou',
        key: 'addSocket',
        permission: 'ADD',
        disabled: false
      }
    ]
  },
  // {
  //   name: '添加服务',
  //   icon: 'icon-chuangjianfuwu',
  //   key: 'addServive',
  //   permission: 'ADD',
  //   disabled: false
  // },
  {
    name: '同步配置',
    icon: 'icon-peizhifuwutongbu',
    key: 'sync-config',
    permission: 'MODIFY',
    disabled: false
  },
  {
    name: '安全方案配置',
    icon: 'icon-renzhengtou',
    key: 'authentication-config',
    permission: 'MODIFY',
    disabled: false
  },
  {
    name: '服务器配置',
    icon: 'icon-host',
    key: 'server-config',
    permission: 'MODIFY',
    disabled: false
  },
  {
    name: '本地导入',
    icon: 'icon-daoru',
    key: 'local-import',
    permission: 'ADD',
    disabled: false
  },
  {
    name: '导出接口',
    icon: 'icon-daochujiekou',
    key: 'export-apis',
    permission: 'EXPORT',
    disabled: false
  },
  {
    name: 'Mock服务',
    permission: 'VIEW',
    key: 'mock',
    icon: 'icon-mockjiedian',
    disabled: false
  },
  {
    name: '重命名',
    icon: 'icon-bianji',
    key: 'rename',
    permission: 'RENAME',
    disabled: false
  },
  // {
  //   name: '移动',
  //   icon: 'icon-yidong',
  //   key: 'move',
  //   permission: 'MODIFY',
  //   disabled: false
  // },
  {
    name: '删除',
    icon: 'icon-qingchu',
    key: 'delete',
    permission: 'DELETE',
    disabled: false
  },
  {
    name: '翻译',
    icon: 'icon-yuyan',
    key: 'translate',
    disabled: false
  },
  {
    name: '克隆',
    icon: 'icon-fuzhi',
    key: 'clone',
    permission: 'CLONE',
    disabled: false
  },
  // {
  //   name: '分享',
  //   icon: 'icon-fenxiang',
  //   key: 'share',
  //   permission: 'SHARE',
  //   disabled: false
  // },
  {
    name: '权限',
    icon: 'icon-quanxian1',
    key: 'auth',
    permission: 'GRANT',
    disabled: false
  },
  {
    name: '修改状态',
    key: 'setStatus',
    icon: 'icon-fabu',
    permission: 'MODIFY',
    disabled: false
  },
  {
    name: '批量修改参数',
    key: 'batchModify',
    icon: 'icon-xiugai',
    permission: 'MODIFY',
    disabled: false,
    children: [
      {
        name: '批量添加参数',
        key: 'batchAddParams',
        icon: 'icon-piliangtianjiacanshu',
        permission: 'MODIFY'
      },
      {
        name: '批量修改参数',
        key: 'batchModifyParams',
        icon: 'icon-piliangxiugaicanshu',
        permission: 'MODIFY'
      },
      {
        name: '批量删除参数',
        key: 'batchDelParams',
        icon: 'icon-piliangshanchucanshu',
        permission: 'MODIFY'
      },
      {
        name: '批量启用参数',
        key: 'batchEnabledParams',
        icon: 'icon-piliangqiyongcanshu',
        permission: 'MODIFY'
      },
      {
        name: '批量禁用参数',
        key: 'batchDisabledParams',
        icon: 'icon-piliangjinyongcanshu',
        permission: 'MODIFY'
      },
      {
        name: '批量修改认证',
        key: 'batchModifyAuth',
        icon: 'icon-piliangxiugairenzheng',
        permission: 'MODIFY'
      },
      {
        name: '批量修改服务器',
        key: 'batchModifyServer',
        icon: 'icon-piliangxiugaifuwuqi',
        permission: 'MODIFY'
      },
      {
        name: '批量引用变量',
        key: 'batchLinkVariable',
        icon: 'icon-piliangyinyongbianliang',
        permission: 'MODIFY'
      },
      {
        name: '批量取消变量引用',
        key: 'batchDelVariable',
        icon: 'icon-piliangquxiaoyinyongbianliang',
        permission: 'MODIFY'
      },
      {
        name: '批量引用数据集',
        key: 'batchLinkDataSet',
        icon: 'icon-piliangyinyongshujuji',
        permission: 'MODIFY'
      },
      {
        name: '批量取消数据集引用',
        key: 'batchDelDataSet',
        icon: 'icon-piliangquxiaoyinyongshujuji',
        permission: 'MODIFY'
      }
    ]
  },
  {
    name: '接口测试脚本',
    key: 'testScript',
    disabled: false,
    permission: 'TEST',
    icon: 'icon-ceshijiaoben',
    children: [
      {
        name: '生成测试脚本',
        key: 'setTestScript',
        permission: 'TEST',
        icon: 'icon-shengchengceshijiaoben',
        tip: '自动生成接口功能、性能和稳定性测试脚本。'
      },
      // {
      //   name: '更新测试脚本',
      //   key: 'updateTestScript',
      //   permission: 'TEST',
      //   icon: 'icon-gengxinceshijiaoben',
      //   tip: '更新接口对应性能和稳定性测试脚本'
      // },
      {
        name: '删除测试脚本',
        key: 'delTestScript',
        permission: 'TEST',
        icon: 'icon-shanchuceshijiaoben',
        tip: '删除接口功能、性能和稳定性测试脚本。'
      }
    ]
  },
  {

    name: '启用或禁用测试',
    key: 'enabledTest',
    disabled: false,
    permission: 'MODIFY',
    icon: 'icon-zhibiao'
    // children: [
    //   {
    //     name: '启用功能测试',
    //     key: 'enabledFunc',
    //     permission: 'MODIFY',
    //     icon: 'icon-qiyong'
    //   },
    //   {
    //     name: '禁用功能测试',
    //     key: 'disabledFunc',
    //     permission: 'MODIFY',
    //     icon: 'icon-jinyong'
    //   },
    //   {
    //     name: '启用性能测试',
    //     key: 'enabledPerf',
    //     permission: 'MODIFY',
    //     icon: 'icon-qiyong'
    //   },
    //   {
    //     name: '禁用性能测试',
    //     key: 'disabledPerf',
    //     permission: 'MODIFY',
    //     icon: 'icon-jinyong'
    //   },
    //   {
    //     name: '启用稳定性测试',
    //     key: 'enabledStability',
    //     permission: 'MODIFY',
    //     icon: 'icon-qiyong'
    //   },
    //   {
    //     name: '禁用稳定性测试',
    //     key: 'disabledStability',
    //     permission: 'MODIFY',
    //     icon: 'icon-jinyong'
    //   }
    // ]
  },
  {
    name: '执行服务测试',
    key: 'execService',
    disabled: false,
    permission: 'TEST',
    icon: 'icon-ceshijiaoben',
    children: [
      {
        name: '执行冒烟测试',
        key: 'funcTestExecSmoke',
        permission: 'TEST',
        icon: 'icon-gengxinceshijiaoben'
      },
      {
        name: '执行安全测试',
        key: 'funcTestExecSecurity',
        permission: 'TEST',
        icon: 'icon-shanchuceshijiaoben'
      }
    ]
  },
  {
    name: '执行接口测试',
    key: 'exec',
    disabled: false,
    permission: 'TEST',
    icon: 'icon-ceshijiaoben',
    children: [
      {
        name: '执行功能测试',
        key: 'funcTestExec',
        permission: 'TEST',
        icon: 'icon-shengchengceshijiaoben'
      },
      {
        name: '执行性能测试',
        key: 'perfTestExec',
        permission: 'TEST',
        icon: 'icon-shanchuceshijiaoben'
      },
      {
        name: '执行稳定性测试',
        key: 'stabilityTestExec',
        permission: 'TEST',
        icon: 'icon-shanchuceshijiaoben'
      }
    ]
  },
  {
    name: '接口测试任务',
    key: 'testTask',
    disabled: false,
    permission: 'TEST',
    icon: 'icon-ceshirenwu',
    children: [
      {
        name: '生成测试任务',
        key: 'setTest',
        icon: 'icon-shengchengceshirenwu1',
        permission: 'TEST',
        disabled: false,
        tip: '生成功能、性能和稳定性测试任务。'
      },
      {
        name: '重新开始测试任务',
        key: 'reTest',
        icon: 'icon-zhongxinkaishiceshi',
        permission: 'TEST',
        disabled: false,
        tip: '将任务更新为`待处理`，相关统计计数和状态会被清除。'
      },
      {
        name: '重新打开测试任务',
        key: 'reOpen',
        icon: 'icon-zhongxindakaiceshirenwu',
        permission: 'TEST',
        disabled: false,
        tip: '将任务状态更新为`待处理`、 不清理统计计数和状态。'
      },
      {
        name: '删除测试任务',
        key: 'deleteTask',
        icon: 'icon-shanchuceshirenwu1',
        permission: 'TEST',
        disabled: false,
        tip: '删除接口对应功能、性能和稳定性测试任务，包括测试脚本。'
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

export interface EditData {
  show?: boolean,
  type: SP,
  name: string,
  pid?: string
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
