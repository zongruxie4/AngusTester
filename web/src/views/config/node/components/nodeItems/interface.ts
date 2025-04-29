
export const formItems = [
  {
    label: '节点名称',
    inputType: 'text',
    placeholder: '输入限制200字符以内',
    valueKey: 'name',
    required: true,
    maxlength: 200,
    width: '18%'
  },
  {
    label: '内网IP',
    inputType: 'text',
    placeholder: '输入节点IP地址',
    valueKey: 'ip',
    required: true,
    maxlength: 15,
    width: '18%'
  },
  {
    label: '公网IP',
    inputType: 'text',
    placeholder: '输入节点IP地址',
    valueKey: 'publicIp',
    required: false,
    maxlength: 15,
    width: '18%'
  },
  {
    label: '域名',
    inputType: 'text',
    placeholder: '输入节点域名',
    valueKey: 'domain',
    maxlength: 200,
    width: '33.3%'
  },
  {
    label: '用户名',
    inputType: 'text',
    placeholder: '输入用户名',
    valueKey: 'username',
    maxlength: 200,
    width: '18%'
  },
  {
    label: '密码',
    inputType: 'text',
    type: 'password',
    placeholder: '输入密码',
    valueKey: 'password',
    maxlength: 800,
    width: '18%'
  },
  {
    label: 'SSH端口',
    inputType: 'number',
    placeholder: '输入SSH端口',
    valueKey: 'sshPort',
    width: '18%'
  }
];

export const roleOptions = [
  {
    label: '管理节点',
    value: 'manage'
  },
  {
    label: '压测节点',
    value: 'test'
  },
  {
    label: 'Mock节点',
    value: 'mock'
  },
  {
    label: '应用节点',
    value: 'application'
  }
];

export const nodeStatus = [
  {
    label: '启用状态：',
    valueName: {
      true: '已启用',
      false: '未启用'
    },
    status: {
      true: 'success',
      false: 'fail'
    },
    valueKey: 'enabled'
  },
  {
    label: '代理安装状态：',
    valueKey: 'installAgentFlag',
    valueName: {
      true: '已安装',
      false: '未安装'
    },
    status: {
      true: 'success',
      false: 'fail'
    }
  },
  {
    label: '连接状态：',
    valueKey: 'onlineFlag',
    valueName: {
      true: '已连接',
      false: '未连接'
    },
    status: {
      true: 'success',
      false: 'fail'
    }
  }
];

export const viewItem = [
  {
    label: 'ID: ',
    valueKey: 'id'
  },
  {
    label: '内网IP: ',
    valueKey: 'ip'
  },
  {
    label: '公网IP: ',
    valueKey: 'publicIp'
  },
  {
    label: '来源: ',
    valueKey: 'sourceName'
  },
  {
    label: '角色: ',
    valueKey: 'roles',
    type: 'tag'
  },
  {
    label: '规格: ',
    valueKey: 'standard'
  },
  {
    label: '系统: ',
    valueKey: 'os',
    type: 'tag'
  }
];

export const nodeUseProgresses = [
  {
    label: 'CPU',
    value: 90,
    valueKey: 'cpu'
  },
  {
    label: '内存',
    value: 60,
    valueKey: 'memory'
  },
  {
    label: '文件系统',
    value: 45,
    valueKey: 'disk'
  },
  {
    label: '交换区',
    value: 0,
    valueKey: 'swap'
  },
  {
    label: '上传',
    value: 29,
    valueKey: 'network-up'
  },
  {
    label: '下载',
    value: 29,
    valueKey: 'network-down'
  }
];
