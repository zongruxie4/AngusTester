export const contentTreeData = [
  {
    title: '服务信息',
    key: 'serviceDetail'
  },
  {
    title: '接口分组统计',
    key: 'apisGroupedTotal'
  },
  {
    title: '测试结果汇总',
    key: 'serviceTotal',
    children: [
      {
        title: '测试进度',
        key: 'testProcess'
      },
      {
        title: '测试接口统计',
        key: 'testApi'
      },
      {
        title: '测试状态统计',
        key: 'testStatus'
      }
    ]
  }
];
