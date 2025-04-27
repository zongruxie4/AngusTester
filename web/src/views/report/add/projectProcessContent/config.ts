export const contentTreeData = [
  {
    title: '项目信息',
    key: 'projectInfo'
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
        title: '任务燃尽图',
        key: 'taskburndown'
      },
      {
        title: '任务分组统计',
        key: 'taskgroup'
      }
      // {
      //   title: '任务结果明细',
      //   key: 'taskDetail'
      // }
    ]
  },
  {
    title: '功能测试',
    key: 'func',
    children: [
      {
        title: '用例测试汇总结果',
        key: 'funcTotal'
      },
      {
        title: '用例评审汇总结果',
        key: 'funcReviewTotal'
      },
      {
        title: '用例燃尽图',
        key: 'caseburndown'
      },
      {
        title: '用例分组统计',
        key: 'casegroup'
      }
      // {
      //   title: '用例测试结果明细',
      //   key: 'funcDetail'
      // }
    ]
  },
  {
    title: '接口测试',
    key: 'apis',
    children: [
      {
        title: '接口测试汇总结果',
        key: 'apisTotal'
      },
      {
        title: '接口分组统计',
        key: 'apisgroup'
      }
      // {
      //   title: '接口测试结果明细',
      //   key: 'apisDetail'
      // }
    ]
  },
  {
    title: '场景测试',
    key: 'scenario',
    children: [
      {
        title: '场景测试汇总结果',
        key: 'scenarioTotal'
      },
      {
        title: '场景分组统计',
        key: 'scenariogroup'
      }
      // {
      //   title: '场景测试结果明细',
      //   key: 'scenarioDetail'
      // }
    ]
  }
];
