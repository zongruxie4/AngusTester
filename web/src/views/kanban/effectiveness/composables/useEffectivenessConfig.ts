import { computed } from 'vue';

import { useI18n } from 'vue-i18n';
import { OverviewConfig } from '../types';
/**
 * <p>
 * Effectiveness configuration management composable
 * </p>
 * <p>
 * Provides configuration data for overview sections and chart displays
 * </p>
 *
 * @param countType - Current count type (task or useCase)
 * @returns Object containing configuration data and computed properties
 */
export function useEffectivenessConfig (countType: 'task' | 'useCase') {

  const { t } = useI18n();
  /**
   * <p>
   * Task overview configuration for effectiveness dashboard
   * </p>
   * <p>
   * Defines the layout and data mapping for task-related overview metrics
   * </p>
   */
  const taskOverViewConfig: OverviewConfig = [
    [
      {
        name: t('kanban.effectiveness.totalTaskCount'),
        dataIndex: 'totalTaskNum',
        icon: 'icon-task'
      },
      {
        name: t('kanban.effectiveness.progress'),
        dataIndex: 'progress',
        icon: 'icon-progress',
        unit: '%'
      },
      {
        name: t('kanban.effectiveness.pending'),
        dataIndex: 'pendingNum',
        icon: 'icon-pending'
      },
      {
        name: t('kanban.effectiveness.inProgress'),
        dataIndex: 'inProgressNum',
        icon: 'icon-in-progress'
      }
    ],
    [
      {
        name: t('kanban.effectiveness.confirming'),
        dataIndex: 'confirmingNum',
        icon: 'icon-confirming'
      },
      {
        name: t('kanban.effectiveness.completed'),
        dataIndex: 'completedNum',
        icon: 'icon-completed'
      },
      {
        name: t('kanban.effectiveness.canceled'),
        dataIndex: 'canceledNum',
        icon: 'icon-canceled'
      },
      {
        name: t('kanban.effectiveness.overdue'),
        dataIndex: 'overdueNum',
        icon: 'icon-overdue'
      }
    ],
    [
      {
        name: t('kanban.effectiveness.oneTimePassRate'),
        dataIndex: 'oneTimePassedRate',
        icon: 'icon-pass-rate',
        unit: '%'
      },
      {
        name: t('kanban.effectiveness.evalWorkload'),
        dataIndex: 'evalWorkload',
        icon: 'icon-eval-workload',
        unit: 'h'
      },
      {
        name: t('kanban.effectiveness.actualWorkload'),
        dataIndex: 'actualWorkload',
        icon: 'icon-actual-workload',
        unit: 'h'
      },
      {
        name: t('kanban.effectiveness.savingWorkload'),
        dataIndex: 'savingWorkload',
        icon: 'icon-saving-workload',
        unit: 'h'
      }
    ]
  ];

  /**
   * <p>
   * Use case overview configuration for effectiveness dashboard
   * </p>
   * <p>
   * Defines the layout and data mapping for use case-related overview metrics
   * </p>
   */
  const caseOverViewConfig: OverviewConfig = [
    [
      {
        name: t('kanban.effectiveness.totalCaseCount'),
        dataIndex: 'totalCaseNum',
        icon: 'icon-use-case'
      },
      {
        name: t('kanban.effectiveness.progress'),
        dataIndex: 'progress',
        icon: 'icon-progress',
        unit: '%'
      },
      {
        name: t('kanban.effectiveness.pendingTest'),
        dataIndex: 'pendingTestNum',
        icon: 'icon-pending-test'
      },
      {
        name: t('kanban.effectiveness.passedTest'),
        dataIndex: 'passedTestNum',
        icon: 'icon-passed-test'
      }
    ],
    [
      {
        name: t('kanban.effectiveness.notPassedTest'),
        dataIndex: 'notPassedTestNum',
        icon: 'icon-not-passed-test'
      },
      {
        name: t('kanban.effectiveness.blockedTest'),
        dataIndex: 'blockedTestNum',
        icon: 'icon-blocked-test'
      },
      {
        name: t('kanban.effectiveness.testCaseHitRate'),
        dataIndex: 'testCaseHitRate',
        icon: 'icon-hit-rate',
        unit: '%'
      },
      {
        name: t('kanban.effectiveness.oneTimePassRate'),
        dataIndex: 'oneTimePassedTestRate',
        icon: 'icon-pass-test-rate',
        unit: '%'
      }
    ],
    [
      {
        name: t('kanban.effectiveness.oneTimePassReviewRate'),
        dataIndex: 'oneTimePassReviewRate',
        icon: 'icon-pass-review-rate',
        unit: '%'
      },
      {
        name: t('kanban.effectiveness.evalWorkload'),
        dataIndex: 'evalWorkload',
        icon: 'icon-eval-workload',
        unit: 'h'
      },
      {
        name: t('kanban.effectiveness.actualWorkload'),
        dataIndex: 'actualWorkload',
        icon: 'icon-actual-workload',
        unit: 'h'
      },
      {
        name: t('kanban.effectiveness.savingWorkload'),
        dataIndex: 'savingWorkload',
        icon: 'icon-saving-workload',
        unit: 'h'
      }
    ]
  ];

  /**
   * <p>
   * Computed current overview configuration based on count type
   * </p>
   * <p>
   * Returns task or use case configuration based on the current countType
   * </p>
   */
  const currentOverviewConfig = computed(() => {
    return countType === 'task' ? taskOverViewConfig : caseOverViewConfig;
  });

  return {
    // Configuration data
    taskOverViewConfig,
    caseOverViewConfig,

    // Computed properties
    currentOverviewConfig
  };
}
