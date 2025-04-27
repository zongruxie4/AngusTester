export const contentTreeData = [
  {
    title: '场景基本信息',
    key: 'scenario'
  },
  {
    title: '场景测试汇总结果',
    key: 'scenarioTotal'

  },
  {
    title: '场景测试结果详细信息',
    key: 'scenarioTestDetail',
    children: [
      {
        title: '功能测试结果',
        key: 'funcTest',
        children: [
          {
            title: '测试基本信息',
            key: 'info'
          },
          {
            title: '测试接口统计',
            key: 'apis'
          },
          {
            title: '测试接口结果明细',
            key: 'apisDetail'
          }
        ]
      },
      {
        title: '性能测试结果',
        key: 'perfTest',
        children: [
          {
            title: '测试基本信息',
            key: 'info'
          },
          {
            title: '详细测试结果',
            key: 'detail'
          },
          {
            title: '测试指标',
            key: 'indicator'
          }
        ]
      },
      {
        title: '稳定性测试结果',
        key: 'stabilityTest',
        children: [
          {
            title: '测试基本信息',
            key: 'info'
          },
          {
            title: '详细测试结果',
            key: 'detail'
          },
          {
            title: '测试指标',
            key: 'indicator'
          }
        ]
      }
    ]
  }
];
