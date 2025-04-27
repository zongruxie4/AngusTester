export const contentTreeData = [
  {
    title: '接口基本信息',
    key: 'apisBasic'
  },
  {
    title: '接口测试汇总结果',
    key: 'apisTestt'
  },
  {
    title: '接口测试结果详细信息',
    key: 'apisDetail',
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
            title: '测试用例统计',
            key: 'cases'
          },
          {
            title: '测试用例结果明细',
            key: 'casesDetail'
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
