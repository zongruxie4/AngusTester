import { LeftMenuItem } from '@/layout/types';
import { EditionType } from '@xcan-angus/infra';

export enum IssueMenuKey {
  HOME = 'home',
  BACKLOG = 'backlog',
  SPRINT = 'sprint',
  ISSUE = 'issue',
  MEETING = 'meeting',
  ANALYSIS = 'analysis',
  TRASH = 'trash'
}

export type IssueMenuVisibility = {
  showTask: boolean;
  showBackLog: boolean;
  showMeeting: boolean;
  showSprint: boolean;
  showTasStatistics: boolean;
}

export const createMenuItems = (
  t: (key: string) => string,
  visibility: IssueMenuVisibility,
  editionType: EditionType | undefined
): LeftMenuItem<IssueMenuKey>[] => {
  const items: Array<LeftMenuItem<IssueMenuKey> | null> = [
    {
      name: t('home.title'),
      icon: 'icon-zhuye',
      key: IssueMenuKey.HOME
    },
    visibility.showBackLog
      ? {
          name: t('backlog.title'),
          icon: 'icon-backlog',
          key: IssueMenuKey.BACKLOG
        }
      : null,
    visibility.showSprint
      ? {
          name: t('sprint.title'),
          icon: 'icon-diedai',
          key: IssueMenuKey.SPRINT
        }
      : null,
    {
      name: t('issue.title'),
      icon: 'icon-renwu2',
      key: IssueMenuKey.ISSUE
    },
    visibility.showMeeting
      ? {
          name: t('meeting.title'),
          icon: 'icon-RT',
          key: IssueMenuKey.MEETING
        }
      : null,
    editionType !== undefined && editionType !== EditionType.COMMUNITY
      ? {
          name: t('issueAnalysis.title'),
          icon: 'icon-fenxi',
          key: IssueMenuKey.ANALYSIS
        }
      : null,
    {
      name: t('trash.title'),
      icon: 'icon-qingchu',
      key: IssueMenuKey.TRASH
    }
  ];
  return items.filter((i): i is LeftMenuItem<IssueMenuKey> => i !== null);
};
