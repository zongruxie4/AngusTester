export const contentTreeData = [
  {
    title: '迭代信息',
    key: 'sprintDetail',
    children: [
      {
        title: '基本信息',
        key: 'basic'
      },
      {
        title: '验收标准',
        key: 'taskDetail'
      },
      {
        title: '会议记录',
        key: 'meetings'
      },
      {
        title: '其他说明',
        key: 'others'
      }
    ]
  },
  {
    title: '研发任务',
    key: 'task',
    children: [
      {
        title: '任务汇总结果',
        key: 'taskTotal'
      },
      {
        title: '燃尽图',
        key: 'taskburndown'
      },
      {
        title: '分组统计',
        key: 'groupedTotal'
      }
    ]
  },
  {
    title: '经办人汇总结果',
    key: 'assigneeId',
    children: [
      {
        title: '任务汇总结果',
        key: 'assigneeId_taskTotal'
      },
      {
        title: '燃尽图',
        key: 'assigneeId_burndown'
      },
      {
        title: '分组统计',
        key: 'assigneeId_groupedTotal'
      }
    ]
  }
];
