import { computed } from 'vue';
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
export function useEffectivenessConfig(countType: 'task' | 'useCase') {
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
        name: '总任务数',
        dataIndex: 'totalTaskNum',
        icon: 'icon-task'
      },
      {
        name: '进度',
        dataIndex: 'progress',
        icon: 'icon-progress',
        unit: '%'
      },
      {
        name: '待处理',
        dataIndex: 'pendingNum',
        icon: 'icon-pending'
      },
      {
        name: '进行中',
        dataIndex: 'inProgressNum',
        icon: 'icon-in-progress'
      }
    ],
    [
      {
        name: '确认中',
        dataIndex: 'confirmingNum',
        icon: 'icon-confirming'
      },
      {
        name: '已完成',
        dataIndex: 'completedNum',
        icon: 'icon-completed'
      },
      {
        name: '已取消',
        dataIndex: 'canceledNum',
        icon: 'icon-canceled'
      },
      {
        name: '逾期',
        dataIndex: 'overdueNum',
        icon: 'icon-overdue'
      }
    ],
    [
      {
        name: '一次通过率',
        dataIndex: 'oneTimePassedRate',
        icon: 'icon-pass-rate',
        unit: '%'
      },
      {
        name: '评估工作量',
        dataIndex: 'evalWorkload',
        icon: 'icon-eval-workload',
        unit: 'h'
      },
      {
        name: '实际工作量',
        dataIndex: 'actualWorkload',
        icon: 'icon-actual-workload',
        unit: 'h'
      },
      {
        name: '节省工作量',
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
        name: '总用例数',
        dataIndex: 'totalCaseNum',
        icon: 'icon-use-case'
      },
      {
        name: '进度',
        dataIndex: 'progress',
        icon: 'icon-progress',
        unit: '%'
      },
      {
        name: '待测试',
        dataIndex: 'pendingTestNum',
        icon: 'icon-pending-test'
      },
      {
        name: '已通过',
        dataIndex: 'passedTestNum',
        icon: 'icon-passed-test'
      }
    ],
    [
      {
        name: '未通过',
        dataIndex: 'notPassedTestNum',
        icon: 'icon-not-passed-test'
      },
      {
        name: '已阻塞',
        dataIndex: 'blockedTestNum',
        icon: 'icon-blocked-test'
      },
      {
        name: '用例命中率',
        dataIndex: 'testCaseHitRate',
        icon: 'icon-hit-rate',
        unit: '%'
      },
      {
        name: '一次通过率',
        dataIndex: 'oneTimePassedTestRate',
        icon: 'icon-pass-test-rate',
        unit: '%'
      }
    ],
    [
      {
        name: '一次通过评审率',
        dataIndex: 'oneTimePassReviewRate',
        icon: 'icon-pass-review-rate',
        unit: '%'
      },
      {
        name: '评估工作量',
        dataIndex: 'evalWorkload',
        icon: 'icon-eval-workload',
        unit: 'h'
      },
      {
        name: '实际工作量',
        dataIndex: 'actualWorkload',
        icon: 'icon-actual-workload',
        unit: 'h'
      },
      {
        name: '节省工作量',
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
