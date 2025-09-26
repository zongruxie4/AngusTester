import { computed } from 'vue';
import { useI18n } from 'vue-i18n';
import { enumUtils, TESTER } from '@xcan-angus/infra';
import { CombinedTargetType } from '@/enums/enums';
import { ChartType, DateRangeType } from '@/components/dashboard/enums';

/**
 * Composable for managing dashboard configuration
 * Provides chart configurations and layout settings for the activity dashboard
 *
 * @returns Object containing dashboard configuration
 */
export function useDashboardConfig () {
  const { t } = useI18n();

  // Pre-compute enum messages at setup level
  const resourceTypeEnums = enumUtils.enumToMessages(CombinedTargetType);

  /**
   * Dashboard configuration including charts and layout
   */
  const dashboardConfig = computed(() => ({
    charts: [
      {
        type: ChartType.LINE,
        title: t('activity.title'),
        field: 'opt_date'
      },
      {
        type: ChartType.PIE,
        title: t('activity.chart.resourceType'),
        field: 'target_type',
        enumKey: resourceTypeEnums
      }
    ],
    layout: {
      cols: 2,
      gap: 16
    }
  }));

  /**
   * Dashboard constants
   */
  const dashboardConstants = {
    apiRouter: TESTER,
    resource: 'Activity',
    barTitle: computed(() => t('activity.name')),
    dateType: DateRangeType.MONTH,
    showChartParam: true
  };

  return {
    dashboardConfig,
    dashboardConstants
  };
}
