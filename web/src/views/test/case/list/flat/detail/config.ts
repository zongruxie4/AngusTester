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
      dataIndex: 'creator'
    },
    {
      label: t('common.modifier'),
      dataIndex: 'modifier'
    }]
];

export const minPeopleInfoColumns = [
  [
    bigPeopleInfoColumns[0][0],
    bigPeopleInfoColumns[0][1],
    bigPeopleInfoColumns[0][2]
  ],
  [
    bigPeopleInfoColumns[0][3],
    bigPeopleInfoColumns[0][4]
  ]
];

export const bigDateInfoColumns = [
  [
    {
      label: t('common.createdDate'),
      dataIndex: 'createdDate'
    },
    {
      label: t('common.deadlineDate'),
      dataIndex: 'deadlineDate'
    },
    {
      label: t('common.reviewDate'),
      dataIndex: 'reviewDate'
    },
    {
      label: t('common.completedDate'),
      dataIndex: 'testResultHandleDate'
    },
    {
      label: t('common.modifiedDate'),
      dataIndex: 'modifiedDate'
    }
  ]
];

export const minDateInfoColumns = [
  [
    bigDateInfoColumns[0][0],
    bigDateInfoColumns[0][1],
    bigDateInfoColumns[0][2]
  ],
  [
    bigDateInfoColumns[0][3],
    bigDateInfoColumns[0][4]
  ]
];

export const bigReviewInfoColumns = [
  [
    {
      label: t('status.enabled'),
      dataIndex: 'review',
      customRender: ({ text }) => text ? t('status.yes') : t('status.no')
    },
    {
      label: t('common.counts.reviewCount'),
      dataIndex: 'reviewNum'
    },
    {
      label: t('common.counts.reviewFailCount'),
      dataIndex: 'reviewFailNum'
    },
    {
      label: t('common.counts.oneTimePassed'),
      dataIndex: 'oneReviewPass'
    },
    {
      label: t('common.reviewOpinion'),
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
      label: t('common.counts.testCount'),
      dataIndex: 'testNum'
    },
    {
      label: t('common.counts.testFailCount'),
      dataIndex: 'testFailNum'
    },
    {
      label: t('common.counts.oneTimePassed'),
      dataIndex: 'oneTestPass'
    },
    {
      label: t('common.testResult'),
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
