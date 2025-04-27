// 添加执行菜单选项
export const reportMenus = [
  {
    key: 'PROJECT',
    label: '项目报告',
    type: 'group',
    icon: 'icon-xiangmu',
    children: [
      {
        key: 'PROJECT_PROGRESS',
        label: '项目进度报告',
        description: '报告项目任务、功能测试、接口测试、场景测试四部分完成进度。'
      }
    ]
  },
  {
    key: 'TASK',
    label: '任务报告',
    icon: 'icon-renwuceshibaogao',
    type: 'group',
    children: [
      {
        key: 'TASK_SPRINT',
        description: '报告迭代信息、研发任务完成进度，迭代和经办人汇总统计信息。',
        label: '迭代报告'
      },
      {
        key: 'TASK',
        label: '任务报告',
        description: '报告单个任务处理结果及详情信息。'
      }
    ]
  },
  {
    key: 'FUNCTIONAL',
    label: '功能测试报告',
    type: 'group',
    icon: 'icon-gongnengceshibaogao',
    children: [
      {
        key: 'FUNC_TESTING_PLAN',
        description: '报告测试计划信息、用例测试进度、测试计划和测试人汇总统计信息。',
        label: '测试计划报告'
      },
      {
        key: 'FUNC_TESTING_CASE',
        description: '报告单个用例测试结果及详情信息。',
        label: '测试用例报告'
      }
    ]
  },
  {
    key: 'APIS',
    label: '接口测试报告',
    type: 'group',
    icon: 'icon-jiekou',
    children: [
      {
        key: 'SERVICES_TESTING_RESULT',
        description: '报告服务信息、汇总统计及服务下接口测试结果信息。',
        label: '服务测试结果报告'
      },
      {
        key: 'APIS_TESTING_RESULT',
        description: '报告接口信息、功能测试、性能测试、稳定性测试结果。',
        label: '接口测试结果报告'
      }
    ]
  },
  {
    key: 'SCENARIO',
    label: '场景测试报告',
    icon: 'icon-changjingguanli',
    type: 'group',
    children: [
      {
        key: 'SCENARIO_TESTING_RESULT',
        description: '报告场景信息、功能测试、性能测试、稳定性测试结果。',
        label: '场景测试结果报告'
      }
    ]
  },
  {
    key: 'EXECUTION',
    label: '执行报告',
    icon: 'icon-zhihang',
    type: 'group',
    children: [
      {
        key: 'EXEC_FUNCTIONAL_RESULT',
        description: '报告执行功能测试采样指标详细结果。',
        label: '执行功能测试报告'
      },
      {
        key: 'EXEC_PERFORMANCE_RESULT',
        description: '报告执行性能测试采样指标详细结果。',
        label: '执行性能测试报告'
      },
      {
        key: 'EXEC_STABILITY_RESULT',
        description: '报告执行稳定性测试采样指标详细结果。',
        label: '执行稳定性测试报告'
      },
      {
        key: 'EXEC_CUSTOMIZATION_RESULT',
        description: '报告执行自定义测试采样指标详细结果。',
        label: '执行自定义测试报告'
      }
    ]
  }
];
