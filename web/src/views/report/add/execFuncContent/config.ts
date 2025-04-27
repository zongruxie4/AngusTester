export const contentTreeData = [
  {
    title: '执行信息',
    key: 'exec',
    children: [
      {
        title: '执行基本信息',
        key: 'basic'
      },
      {
        title: '执行结果',
        key: 'execResult'
      }
    ]
  },
  {
    title: '迭代结果',
    key: 'sprint'
  },
  {
    title: '日志信息',
    key: 'log',
    children: [
      {
        title: '执行调度日志',
        key: 'dispatch',
        tips: '多个节点时只展示第一个执行节点日志。'
      },
      {
        title: '执行采样日志',
        key: 'sampling',
        tips: '多个节点时只展示第一个执行节点日志，最多支持10000行日志信息。'
      }
    ]
  }
];
