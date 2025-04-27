export const bigPeopleInfoColumns = [
  [
    {
      label: '测试人',
      dataIndex: 'testerName'
    },
    {
      label: '开发人',
      dataIndex: 'developerName'
    },
    {
      label: '评审人',
      dataIndex: 'reviewerName'
    },
    {
      label: '添加人',
      dataIndex: 'createdByName'
    },
    {
      label: '最后修改人',
      dataIndex: 'lastModifiedByName'
    }]
];
export const minPeopleInfoColumns = [[bigPeopleInfoColumns[0][0], bigPeopleInfoColumns[0][1], bigPeopleInfoColumns[0][2]], [bigPeopleInfoColumns[0][3], bigPeopleInfoColumns[0][4]]];

export const bigDateInfoColumns = [
  [
    {
      label: '评审时间',
      dataIndex: 'reviewDate'
    },
    {
      label: '测试完成时间',
      dataIndex: 'testResultHandleDate'
    },
    {
      label: '添加时间',
      dataIndex: 'createdDate'
    },
    {
      label: '截止时间',
      dataIndex: 'deadlineDate'
    },
    {
      label: '最后修改时间',
      dataIndex: 'lastModifiedDate'
    }
  ]
];
export const minDateInfoColumns = [
  [
    bigDateInfoColumns[0][0], bigDateInfoColumns[0][1], bigDateInfoColumns[0][2]
  ],
  [bigDateInfoColumns[0][3], bigDateInfoColumns[0][4]]
];

export const bigReviewInfoColumns = [
  [
    {
      label: '是否开启评审',
      dataIndex: 'reviewFlag',
      customRender: ({ text }) => text ? '是' : '否'
    },
    {
      label: '评审次数',
      dataIndex: 'reviewNum'
    },
    {
      label: '评审失败次数',
      dataIndex: 'reviewFailNum'
    },
    {
      label: '是否一次性通过',
      dataIndex: 'oneReviewPass'
    },
    {
      label: '评审意见',
      dataIndex: 'reviewRemark'
    }
  ]
];
export const minReviewInfoColumns = [
  [
    bigReviewInfoColumns[0][0],
    bigReviewInfoColumns[0][1],
    bigReviewInfoColumns[0][2]
  ],
  [
    bigReviewInfoColumns[0][3],
    bigReviewInfoColumns[0][4]
  ]
];

export const bigTestInfoColumns = [
  [
    // {
    //   label: '测试结果',
    //   dataIndex: 'testResult'
    // },
    {
      label: '测试次数',
      dataIndex: 'testNum'
    },
    {
      label: '失败次数',
      dataIndex: 'testFailNum'
    },
    {
      label: '是否一次性通过',
      dataIndex: 'oneTestPass'
    },
    {
      label: '结果备注',
      dataIndex: 'testRemark'
    }
  ]
];
export const minTestInfoColumns = [
  [
    bigTestInfoColumns[0][0],
    bigTestInfoColumns[0][1]
  ],
  [
    bigTestInfoColumns[0][2],
    bigTestInfoColumns[0][3]
  ]
];

export const bigApisInfoColumns = [
  [
    {
      label: 'ID',
      dataIndex: 'apisId'
    },
    {
      label: '名称',
      dataIndex: 'apisSummary'
    },
    {
      label: '方法',
      dataIndex: 'method',
      customRender: ({ text }) => text?.message
    },
    {
      label: '路径',
      dataIndex: 'endpoint'
    },
    {
      label: '项目ID',
      dataIndex: 'apisProjectId'
    },
    {
      label: '项目名称',
      dataIndex: 'apisProjectName'
    }
  ]
];
export const minApisInfoColumns = [
  [
    bigApisInfoColumns[0][0],
    bigApisInfoColumns[0][1],
    bigApisInfoColumns[0][2]
  ], [
    bigApisInfoColumns[0][3],
    bigApisInfoColumns[0][4],
    bigApisInfoColumns[0][5]
  ]
];
