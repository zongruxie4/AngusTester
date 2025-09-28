import { computed } from 'vue';

import { useI18n } from 'vue-i18n';
import { OverviewConfig, OverviewConfigItem } from '../types';

/**
 * Effectiveness configuration management composable
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
   * Task overview configuration for effectiveness dashboard
   * <p>
   * Defines the layout and data mapping for task-related overview metrics
   * </p>
   */
  const taskOverViewConfig: OverviewConfig = [
    [
      {
        name: t('status.total'),
        dataIndex: 'totalTaskNum',
        icon: 'icon-task',
        semantic: 'countHighGood'
      },
      {
        name: t('common.progress'),
        dataIndex: 'progress',
        icon: 'icon-progress',
        unit: '%',
        semantic: 'rateHighGood'
      },
      {
        name: t('status.pending'),
        dataIndex: 'pendingNum',
        icon: 'icon-pending',
        semantic: 'countLowGood'
      },
      {
        name: t('status.inProgress'),
        dataIndex: 'inProgressNum',
        icon: 'icon-in-progress',
        semantic: 'countHighGood'
      }
    ],
    [
      {
        name: t('status.pendingConfirmation'),
        dataIndex: 'confirmingNum',
        icon: 'icon-confirming',
        semantic: 'countHighGood'
      },
      {
        name: t('status.completed'),
        dataIndex: 'completedNum',
        icon: 'icon-completed',
        semantic: 'countHighGood'
      },
      {
        name: t('status.cancelled'),
        dataIndex: 'canceledNum',
        icon: 'icon-canceled',
        semantic: 'countLowGood'
      },
      {
        name: t('status.overdue'),
        dataIndex: 'overdueNum',
        icon: 'icon-overdue',
        semantic: 'countLowGood'
      }
    ],
    [
      {
        name: t('kanban.effectiveness.oneTimePassRate'),
        dataIndex: 'oneTimePassedRate',
        icon: 'icon-pass-rate',
        unit: '%',
        semantic: 'rateHighGood'
      },
      {
        name: t('kanban.effectiveness.evalWorkload'),
        dataIndex: 'evalWorkload',
        icon: 'icon-eval-workload',
        semantic: 'neutral'
      },
      {
        name: t('kanban.effectiveness.actualWorkload'),
        dataIndex: 'actualWorkload',
        icon: 'icon-actual-workload',
        semantic: 'neutral'
      },
      {
        name: t('kanban.effectiveness.savingWorkload'),
        dataIndex: 'savingWorkload',
        icon: 'icon-saving-workload',
        semantic: 'workloadSaving'
      }
    ]
  ];

  /**
   * Use case overview configuration for effectiveness dashboard
   * <p>
   * Defines the layout and data mapping for use case-related overview metrics
   * </p>
   */
  const caseOverViewConfig: OverviewConfig = [
    [
      {
        name: t('kanban.effectiveness.totalCaseCount'),
        dataIndex: 'totalCaseNum',
        icon: 'icon-use-case',
        semantic: 'countHighGood'
      },
      {
        name: t('common.progress'),
        dataIndex: 'progress',
        icon: 'icon-progress',
        unit: '%',
        semantic: 'rateHighGood'
      },
      {
        name: t('kanban.effectiveness.pendingTest'),
        dataIndex: 'pendingTestNum',
        icon: 'icon-pending-test',
        semantic: 'countLowGood'
      },
      {
        name: t('kanban.effectiveness.passedTest'),
        dataIndex: 'passedTestNum',
        icon: 'icon-passed-test',
        semantic: 'countHighGood'
      }
    ],
    [
      {
        name: t('kanban.effectiveness.notPassedTest'),
        dataIndex: 'notPassedTestNum',
        icon: 'icon-not-passed-test',
        semantic: 'countLowGood'
      },
      {
        name: t('kanban.effectiveness.blockedTest'),
        dataIndex: 'blockedTestNum',
        icon: 'icon-blocked-test',
        semantic: 'countLowGood'
      },
      {
        name: t('kanban.effectiveness.testCaseHitRate'),
        dataIndex: 'testCaseHitRate',
        icon: 'icon-hit-rate',
        unit: '%',
        semantic: 'rateHighGood'
      },
      {
        name: t('kanban.effectiveness.oneTimePassRate'),
        dataIndex: 'oneTimePassedTestRate',
        icon: 'icon-pass-test-rate',
        unit: '%',
        semantic: 'rateHighGood'
      }
    ],
    [
      {
        name: t('kanban.effectiveness.oneTimePassReviewRate'),
        dataIndex: 'oneTimePassReviewRate',
        icon: 'icon-pass-review-rate',
        unit: '%',
        semantic: 'rateHighGood'
      },
      {
        name: t('kanban.effectiveness.evalWorkload'),
        dataIndex: 'evalWorkload',
        icon: 'icon-eval-workload',
        semantic: 'neutral'
      },
      {
        name: t('kanban.effectiveness.actualWorkload'),
        dataIndex: 'actualWorkload',
        icon: 'icon-actual-workload',
        semantic: 'neutral'
      },
      {
        name: t('kanban.effectiveness.savingWorkload'),
        dataIndex: 'savingWorkload',
        icon: 'icon-saving-workload',
        semantic: 'workloadSaving'
      }
    ]
  ];

  /**
   * Computed current overview configuration based on count type
   * <p>
   * Returns task or use case configuration based on the current countType
   * </p>
   */

  return {
    // Configuration data
    taskOverViewConfig,
    caseOverViewConfig

  };
}

/**
 * Resolve CSS class for overview value text color by semantic and value
 */
export function getOverviewValueColorClass (item: OverviewConfigItem | any, rawValue: unknown): string {
  const value = typeof rawValue === 'number' ? rawValue : Number(rawValue || 0);
  switch (item?.semantic) {
    case 'rateHighGood':
      if (!isFinite(value)) return 'value-neutral';
      if (value >= 90) return 'value-good';
      if (value >= 60) return 'value-warning';
      return 'value-bad';
    case 'countHighGood':
      if (value > 0) return 'value-primary';
      return 'value-neutral';
    case 'countLowGood':
      if (value > 0) return 'value-bad';
      return 'value-good';
    case 'workloadSaving':
      if (value > 0) return 'value-good';
      return 'value-neutral';
    default:
      return 'value-neutral';
  }
}
