import { i18n } from '@xcan-angus/infra';

const t = i18n.getI18n()?.global?.t || ((value: string) => value);
export const bigPeopleInfoColumns = [
  [
    {
      label: t('common.tester'),
      dataIndex: 'testerName'
    },
    {
      label: t('common.developer'),
      dataIndex: 'developerName'
    },
    {
      label: t('common.reviewer'),
      dataIndex: 'reviewerName'
    },
    {
      label: t('common.creator'),
      dataIndex: 'createdByName'
    },
    {
      label: t('common.modifier'),
      dataIndex: 'lastModifiedByName'
    }]
];
export const minPeopleInfoColumns = [[bigPeopleInfoColumns[0][0], bigPeopleInfoColumns[0][1], bigPeopleInfoColumns[0][2]], [bigPeopleInfoColumns[0][3], bigPeopleInfoColumns[0][4]]];

export const bigDateInfoColumns = [
  [
    {
      label: t('common.reviewDate'),
      dataIndex: 'reviewDate'
    },
    {
      label: t('testCase.detail.caseColumConfig.testCompleteTime'),
      dataIndex: 'testResultHandleDate'
    },
    {
      label: t('common.createdDate'),
      dataIndex: 'createdDate'
    },
    {
      label: t('common.deadlineDate'),
      dataIndex: 'deadlineDate'
    },
    {
      label: t('common.lastModifiedDate'),
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
      label: t('testCase.detail.caseColumConfig.enableReview'),
      dataIndex: 'review',
      customRender: ({ text }) => text ? t('status.yes') : t('status.no')
    },
    {
      label: t('testCase.detail.caseColumConfig.reviewCount'),
      dataIndex: 'reviewNum'
    },
    {
      label: t('testCase.detail.caseColumConfig.reviewFailCount'),
      dataIndex: 'reviewFailNum'
    },
    {
      label: t('common.counts.oneTimePassed'),
      dataIndex: 'oneReviewPass'
    },
    {
      label: t('testCase.detail.caseColumConfig.reviewOpinion'),
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
    {
      label: t('testCase.detail.caseColumConfig.testCount'),
      dataIndex: 'testNum'
    },
    {
      label: t('testCase.detail.caseColumConfig.failCount'),
      dataIndex: 'testFailNum'
    },
    {
      label: t('common.counts.oneTimePassed'),
      dataIndex: 'oneTestPass'
    },
    {
      label: t('testCase.detail.caseColumConfig.resultRemark'),
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
      label: t('common.id'),
      dataIndex: 'apisId'
    },
    {
      label: t('common.name'),
      dataIndex: 'apisSummary'
    },
    {
      label: t('common.method'),
      dataIndex: 'method',
      customRender: ({ text }) => text?.message
    },
    {
      label: t('common.path'),
      dataIndex: 'endpoint'
    },
    {
      label: t('common.projectId'),
      dataIndex: 'apisProjectId'
    },
    {
      label: t('common.projectName'),
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
