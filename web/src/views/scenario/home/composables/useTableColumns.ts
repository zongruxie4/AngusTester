import { computed } from 'vue';
import { useI18n } from 'vue-i18n';
import type { ScenarioQueryParams, TableColumn } from '../types';

/**
 * Composable for managing table column configuration
 * Handles dynamic column generation based on query parameters
 */
export function useTableColumns (params: ScenarioQueryParams) {
  const { t } = useI18n();

  /**
   * Generate table columns based on current query parameters
   */
  const columns = computed((): TableColumn[] => {
    const baseColumns: TableColumn[] = [
      {
        key: 'name',
        title: t('scenarioHome.myScenarios.table.columns.name'),
        dataIndex: 'name',
        ellipsis: true,
        sorter: true,
        width: '45%'
      },
      {
        key: 'plugin',
        title: t('scenarioHome.myScenarios.table.columns.plugin'),
        dataIndex: 'plugin',
        width: '15%',
        ellipsis: true
      },
      {
        key: 'scriptType',
        title: t('scenarioHome.myScenarios.table.columns.testType'),
        dataIndex: 'scriptType',
        width: '20%',
        ellipsis: true
      },
      {
        key: 'createdDate',
        title: t('scenarioHome.myScenarios.table.columns.addTime'),
        dataIndex: 'createdDate',
        ellipsis: true,
        width: '20%',
        sorter: true
      }
    ];

    // Add action column based on query parameters
    const actionColumn: TableColumn = {
      key: 'action',
      title: t('scenarioHome.myScenarios.table.columns.operation'),
      dataIndex: 'action',
      width: 80
    };

    // Determine action type based on query parameters
    if (params.favouriteBy) {
      actionColumn.actionKey = 'favouriteBy';
    } else if (params.followBy) {
      actionColumn.actionKey = 'followBy';
    }

    return [...baseColumns, actionColumn];
  });

  /**
   * Get empty text style configuration
   */
  const emptyTextStyle = {
    margin: '14px auto',
    height: 'auto'
  };

  return {
    columns,
    emptyTextStyle
  };
}
