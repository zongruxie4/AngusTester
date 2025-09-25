import { computed } from 'vue';
import { useI18n } from 'vue-i18n';
import { enumUtils, TESTER, ScriptType } from '@xcan-angus/infra';
import { ExecStatus } from '@/enums/enums';
import { ChartType } from '@/components/dashboard/enums';

/**
 * Composable for managing dashboard configuration
 * Provides chart configurations and layout settings for the activity dashboard
 *
 * @returns Object containing dashboard configuration
 */
export function useDashboardConfig () {
  const { t } = useI18n();

  // Pre-compute enum messages at setup level
  const scriptTypeEnums = enumUtils.enumToMessages(ScriptType, [ScriptType.MOCK_APIS]);
  const execStatusEnums = enumUtils.enumToMessages(ExecStatus);

  /**
   * Dashboard configuration including charts and layout
   */
  const dashboardConfig = computed(() => ({
    charts: [
      {
        type: ChartType.PIE,
        title: t('common.scriptType'),
        field: 'script_type',
        enumKey: scriptTypeEnums,
        pieConfig: {
          color: ['#F1948A', '#4ECDC4', '#45B7D1', '#96CEB4', '#FFEAA7', '#DDA0DD']
        }
      },
      {
        type: ChartType.PIE,
        title: t('execution.chartInfo.executionStatus'),
        field: 'status',
        enumKey: execStatusEnums,
        pieConfig: {
          color: ['#DDA0DD', '#98D8C8', '#67D7FF', '#F5222D', '#FF6B6B', '#FFB925', '#52C41A']
        }
      }
    ],
    layout: {
      cols: 2,
      gap: 8
    }
  }));

  /**
   * Dashboard constants
   */
  const dashboardConstants = {
    apiRouter: TESTER,
    resource: 'Exec',
    barTitle: computed(() => t('common.name')),
    showChartParam: false
  };

  return {
    dashboardConfig,
    dashboardConstants
  };
}
