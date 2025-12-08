import { LeftMenuItem } from '@/layout/types';

export enum ProjectMenuKey {
  PROJECT = 'project',
  AI = 'AI',
  MODULE = 'module',
  VERSION = 'version',
  TAGS = 'tags',
  EVALUATION = 'evaluation',
  ACTIVITY = 'activity',
  TRASH = 'trash',
  TEMPLATES = 'templates'
}

export type ProjectMenuVisibility = {
  aiEnabled: boolean;
  hasProjectId: boolean;
}

export const createMenuItems = (
  t: (key: string) => string,
  visibility: ProjectMenuVisibility
): LeftMenuItem<ProjectMenuKey>[] => {
  const items: Array<LeftMenuItem<ProjectMenuKey> | null> = [
    {
      icon: 'icon-xiangmu',
      name: t('project.title'),
      key: ProjectMenuKey.PROJECT
    },
    visibility.aiEnabled
      ? {
          icon: 'icon-AIzhushou',
          name: t('AI.title'),
          key: ProjectMenuKey.AI
        }
      : null,
    visibility.hasProjectId
      ? {
          icon: 'icon-banben1',
          name: t('version.title'),
          key: ProjectMenuKey.VERSION
        }
      : null,
    visibility.hasProjectId
      ? {
          icon: 'icon-mokuai1',
          name: t('module.title'),
          key: ProjectMenuKey.MODULE
        }
      : null,
    visibility.hasProjectId
      ? {
          icon: 'icon-biaoqian3',
          name: t('tag.title'),
          key: ProjectMenuKey.TAGS
        }
      : null,
    visibility.hasProjectId
      ? {
          icon: 'icon-pingshen',
          name: t('evaluation.title'),
          key: ProjectMenuKey.EVALUATION
        }
      : null,
    {
      icon: 'icon-fabu',
      name: t('activity.title'),
      key: ProjectMenuKey.ACTIVITY
    },
    {
      name: t('testTemplate.title'),
      key: ProjectMenuKey.TEMPLATES,
      icon: 'icon-kanbanshitu'
    },
    {
      icon: 'icon-qingchu',
      name: t('trash.title'),
      key: ProjectMenuKey.TRASH
    }
  ];
  return items.filter((i): i is LeftMenuItem<ProjectMenuKey> => i !== null);
};
