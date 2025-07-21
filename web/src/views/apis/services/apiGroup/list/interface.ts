export const CollapseButtonGroup = [
  {
    label: '编辑',
    value: 'edit',
    auth: 'VIEW',
    icon: 'icon-bianji',
    disabled: false
  },
  {
    label: '克隆',
    value: 'patchClone',
    auth: 'VIEW',
    icon: 'icon-fuzhi',
    disabled: false
  },
  {
    label: '删除',
    value: 'del',
    icon: 'icon-qingchu',
    auth: 'DELETE',
    disabled: false
  }
];
export const ButtonGroup = [
  {
    name: 'Mock接口',
    key: 'mock',
    permission: 'MOCK',
    icon: 'icon-mockjiedian'
  }, {
    name: '关注',
    key: 'addWatch',
    permission: 'VIEW',
    disabled: false,
    icon: 'icon-yiguanzhu'
  }, {
    name: '取消关注',
    key: 'cancelWatch',
    permission: 'VIEW',
    disabled: false,
    icon: 'icon-quxiaoguanzhu'
  }, {
    name: '收藏',
    key: 'addFavourite',
    permission: 'VIEW',
    disabled: false,
    icon: 'icon-shoucang2'
  }, {
    name: '取消收藏',
    key: 'cancelFavourite',
    permission: 'VIEW',
    disabled: false,
    icon: 'icon-quxiaoshoucang'
  },
  {
    name: '移动',
    key: 'remove',
    permission: 'MODIFY',
    disabled: false,
    icon: 'icon-yidong'
  }, {
    name: '权限',
    key: 'auth',
    permission: 'GRANT',
    disabled: false,
    icon: 'icon-quanxian1'
  }, {
    name: '导出',
    key: 'export',
    permission: 'EXPORT',
    disabled: false,
    icon: 'icon-daochu'
  }, {
    name: '修改状态',
    key: 'status',
    permission: 'MODIFY',
    disabled: false,
    icon: 'icon-shuxie'
  },
  {
    name: '测试脚本',
    key: 'testScript',
    permission: 'TEST',
    icon: 'icon-ceshijiaoben',
    children: [
      {
        name: '生成测试脚本',
        key: 'setTestScript',
        permission: 'TEST',
        disabled: false,
        icon: 'icon-shengchengceshijiaoben',
        tip: '生成接口功能、性能和稳定性测试脚本。'
      },
      {
        name: '删除测试脚本',
        key: 'delTestScript',
        permission: 'TEST',
        disabled: false,
        icon: 'icon-shanchuceshijiaoben',
        tip: '删除接口功能、性能和稳定性测试脚本。'
      }
    ]
  },
  {
    name: '执行测试',
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
    name: '测试任务',
    key: 'testTask',
    permission: 'TEST',
    icon: 'icon-ceshirenwu',
    children: [
      {
        name: '生成测试任务',
        key: 'setTest',
        permission: 'TEST',
        disabled: false,
        icon: 'icon-shengchengceshirenwu1',
        tip: '生成功能、性能和稳定性测试任务。'
      },
      {
        name: '重新开始测试任务',
        key: 'reTest',
        permission: 'TEST',
        disabled: false,
        icon: 'icon-zhongxinkaishiceshi',
        tip: '将任务更新为`待处理`，相关统计计数和状态会被清除。'
      },
      {
        name: '重新打开测试任务',
        key: 'reopen',
        permission: 'TEST',
        disabled: false,
        icon: 'icon-zhongxindakaiceshirenwu',
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
  }];
