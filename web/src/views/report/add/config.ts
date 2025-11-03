import { i18n } from '@xcan-angus/infra';

const t = i18n.getI18n()?.global?.t || ((value: string):string => value);

// 添加执行菜单选项
export const reportMenus = [
  {
    key: 'PROJECT',
    label: t('reportAdd.menus.project.label'),
    type: 'group',
    icon: 'icon-xiangmu',
    children: [
      {
        key: 'PROJECT_PROGRESS',
        label: t('reportAdd.menus.project.progress.label'),
        description: t('reportAdd.menus.project.progress.description')
      }
    ]
  },
  {
    key: 'TASK',
    label: t('reportAdd.menus.task.label'),
    icon: 'icon-renwuceshibaogao',
    type: 'group',
    children: [
      {
        key: 'TASK_SPRINT',
        description: t('reportAdd.menus.task.sprint.description'),
        label: t('reportAdd.menus.task.sprint.label')
      },
      {
        key: 'TASK',
        label: t('reportAdd.menus.task.task.label'),
        description: t('reportAdd.menus.task.task.description')
      }
    ]
  },
  {
    key: 'FUNCTIONAL',
    label: t('reportAdd.menus.functional.label'),
    type: 'group',
    icon: 'icon-gongnengceshibaogao',
    children: [
      {
        key: 'FUNC_TESTING_PLAN',
        description: t('reportAdd.menus.functional.plan.description'),
        label: t('reportAdd.menus.functional.plan.label')
      },
      {
        key: 'FUNC_TESTING_CASE',
        description: t('reportAdd.menus.functional.case.description'),
        label: t('reportAdd.menus.functional.case.label')
      }
    ]
  },
  {
    key: 'APIS',
    label: t('reportAdd.menus.apis.label'),
    type: 'group',
    icon: 'icon-jiekou',
    children: [
      {
        key: 'SERVICES_TESTING_RESULT',
        description: t('reportAdd.menus.apis.services.description'),
        label: t('reportAdd.menus.apis.services.label')
      },
      {
        key: 'APIS_TESTING_RESULT',
        description: t('reportAdd.menus.apis.apis.description'),
        label: t('reportAdd.menus.apis.apis.label')
      }
    ]
  },
  {
    key: 'SCENARIO',
    label: t('reportAdd.menus.scenario.label'),
    icon: 'icon-changjingguanli',
    type: 'group',
    children: [
      {
        key: 'SCENARIO_TESTING_RESULT',
        description: t('reportAdd.menus.scenario.result.description'),
        label: t('reportAdd.menus.scenario.result.label')
      }
    ]
  },
  {
    key: 'EXECUTION',
    label: t('reportAdd.menus.execution.label'),
    icon: 'icon-zhihang',
    type: 'group',
    children: [
      {
        key: 'EXEC_FUNCTIONAL_RESULT',
        description: t('reportAdd.menus.execution.functional.description'),
        label: t('reportAdd.menus.execution.functional.label')
      },
      {
        key: 'EXEC_PERFORMANCE_RESULT',
        description: t('reportAdd.menus.execution.performance.description'),
        label: t('reportAdd.menus.execution.performance.label')
      },
      {
        key: 'EXEC_STABILITY_RESULT',
        description: t('reportAdd.menus.execution.stability.description'),
        label: t('reportAdd.menus.execution.stability.label')
      },
      {
        key: 'EXEC_CUSTOMIZATION_RESULT',
        description: t('reportAdd.menus.execution.customization.description'),
        label: t('reportAdd.menus.execution.customization.label')
      }
    ]
  }
];
