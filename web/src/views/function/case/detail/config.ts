import { i18n } from '@xcan-angus/infra';
const t = i18n.getI18n()?.global?.t || ((value: string) => value);
export const bigPeopleInfoColumns = [
  [
    {
      label: t('functionCase.detail.caseColumConfig.tester'),
      dataIndex: 'testerName'
    },
    {
      label: t('functionCase.detail.caseColumConfig.developer'),
      dataIndex: 'developerName'
    },
    {
      label: t('functionCase.detail.caseColumConfig.reviewer'),
      dataIndex: 'reviewerName'
    },
    {
      label: t('functionCase.detail.caseColumConfig.creator'),
      dataIndex: 'createdByName'
    },
    {
      label: t('functionCase.detail.caseColumConfig.lastModifier'),
      dataIndex: 'lastModifiedByName'
    }]
];
export const minPeopleInfoColumns = [[bigPeopleInfoColumns[0][0], bigPeopleInfoColumns[0][1], bigPeopleInfoColumns[0][2]], [bigPeopleInfoColumns[0][3], bigPeopleInfoColumns[0][4]]];

export const bigDateInfoColumns = [
  [
    {
      label: t('functionCase.detail.caseColumConfig.reviewTime'),
      dataIndex: 'reviewDate'
    },
    {
      label: t('functionCase.detail.caseColumConfig.testCompleteTime'),
      dataIndex: 'testResultHandleDate'
    },
    {
      label: t('functionCase.detail.caseColumConfig.addTime'),
      dataIndex: 'createdDate'
    },
    {
      label: t('functionCase.detail.caseColumConfig.deadline'),
      dataIndex: 'deadlineDate'
    },
    {
      label: t('functionCase.detail.caseColumConfig.lastModifiedTime'),
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
      label: t('functionCase.detail.caseColumConfig.enableReview'),
      dataIndex: 'review',
      customRender: ({ text }) => text ? t('status.yes') : t('status.no')
    },
    {
      label: t('functionCase.detail.caseColumConfig.reviewCount'),
      dataIndex: 'reviewNum'
    },
    {
      label: t('functionCase.detail.caseColumConfig.reviewFailCount'),
      dataIndex: 'reviewFailNum'
    },
    {
      label: t('functionCase.detail.caseColumConfig.oneTimePass'),
      dataIndex: 'oneReviewPass'
    },
    {
      label: t('functionCase.detail.caseColumConfig.reviewOpinion'),
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
      label: t('functionCase.detail.caseColumConfig.testCount'),
      dataIndex: 'testNum'
    },
    {
      label: t('functionCase.detail.caseColumConfig.failCount'),
      dataIndex: 'testFailNum'
    },
    {
      label: t('functionCase.detail.caseColumConfig.oneTimePass'),
      dataIndex: 'oneTestPass'
    },
    {
      label: t('functionCase.detail.caseColumConfig.resultRemark'),
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
      label: t('functionCase.detail.caseColumConfig.id'),
      dataIndex: 'apisId'
    },
    {
      label: t('functionCase.detail.caseColumConfig.name'),
      dataIndex: 'apisSummary'
    },
    {
      label: t('functionCase.detail.caseColumConfig.method'),
      dataIndex: 'method',
      customRender: ({ text }) => text?.message
    },
    {
      label: t('functionCase.detail.caseColumConfig.path'),
      dataIndex: 'endpoint'
    },
    {
      label: t('functionCase.detail.caseColumConfig.projectId'),
      dataIndex: 'apisProjectId'
    },
    {
      label: t('functionCase.detail.caseColumConfig.projectName'),
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
