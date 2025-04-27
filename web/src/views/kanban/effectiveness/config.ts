
export const taskOverViewConfig = [
  [
    {
      name: '待处理',
      dataIndex: 'pendingNum',
      icon: 'icon-daiceshi'
    },
    {
      name: '进行中',
      dataIndex: 'inProgressNum',
      icon: 'icon-renwuceshizhong'
    },
    {
      name: '待确认',
      dataIndex: 'confirmingNum',
      icon: 'icon-daiqueren'
    },
    {
      name: '已取消',
      dataIndex: 'canceledNum',
      icon: 'icon-yiquxiao'
    },
    {
      name: '已完成',
      dataIndex: 'completedNum',
      icon: 'icon-ceshiwancheng'
    }
  ],
  [
    {
      name: '已逾期',
      dataIndex: 'overdueNum',
      icon: 'icon-yiyuqi1'
    },
    {
      name: '一次性完成率',
      dataIndex: 'oneTimePassedRate',
      icon: 'icon-yicitongguoshuai',
      unit: '%'
    },
    {
      name: '评估工作量',
      dataIndex: 'evalWorkload',
      icon: 'icon-pinggugongzuoliang'
    },
    {
      name: '实际工作量',
      dataIndex: 'actualWorkload',
      icon: 'icon-yiceshiyonglishu'
    },
    {
      name: '节省工作量',
      dataIndex: 'savingWorkload',
      icon: 'icon-jieshenggongzuoliang'
    }
  ]
];

export const caseOverViewConfig = [
  [
    {
      name: '待测试',
      dataIndex: 'pendingTestNum',
      icon: 'icon-daiceshi'
    },
    {
      name: '已通过',
      dataIndex: 'passedTestNum',
      icon: 'icon-ceshitongguo'
    },
    {
      name: '未通过',
      dataIndex: 'notPassedTestNum',
      icon: 'icon-ceshiweitongguo'
    },
    {
      name: '阻塞',
      dataIndex: 'blockedTestNum',
      icon: 'icon-zusaizhong'
    },
    {
      name: '忽略',
      dataIndex: 'canceledTestNum',
      icon: 'icon-yiquxiao'
    },
    {
      name: '已逾期',
      dataIndex: 'overdueNum',
      icon: 'icon-yiyuqi1'
    }
  ],
  [
    {
      name: '测试命中率',
      dataIndex: 'testCaseHitRate',
      icon: 'icon-a-yicixingceshitongguoshu',
      unit: '%'
    },
    {
      name: '一次性测试通过率',
      dataIndex: 'oneTimePassedTestRate',
      icon: 'icon-a-yicixingceshitongguoshuai',
      unit: '%'
    },
    {
      name: '一次性评审通过率',
      dataIndex: 'oneTimePassReviewRate',
      icon: 'icon-a-yicixingpingshentongguoshuai',
      unit: '%'
    },
    {
      name: '评估工作量',
      dataIndex: 'evalWorkload',
      icon: 'icon-pinggugongzuoliang'
    },
    {
      name: '实际工作量',
      dataIndex: 'completedWorkload',
      icon: 'icon-pinggugongzuoliang'
    },
    {
      name: '节省工作量',
      dataIndex: 'savingWorkload',
      icon: 'icon-jieshenggongzuoliang'
    }
  ]
];
