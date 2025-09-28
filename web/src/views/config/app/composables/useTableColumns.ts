import { computed } from 'vue';
import { useI18n } from 'vue-i18n';
import { AuthObjectType } from '@xcan-angus/infra';

/**
 * Composable for managing table column configurations
 * @param activeKey - The currently active member type (USER, DEPT, GROUP)
 */
export function useTableColumns (activeKey: AuthObjectType) {
  const { t } = useI18n();

  /**
   * Computed property for policy table columns based on active member type
   */
  const columns = computed(() => {
    const baseColumns = [
      {
        title: t('app.config.members.list.table.columns.policyId'),
        dataIndex: 'id',
        width: 120
      },
      {
        title: t('app.config.members.list.table.columns.policyName'),
        dataIndex: 'name',
        ellipsis: true,
        width: 150
      },
      {
        title: t('app.config.members.list.table.columns.policyCode'),
        dataIndex: 'code',
        width: 180
      },
      {
        title: t('app.config.members.list.table.columns.policyDescription'),
        dataIndex: 'description'
      }
    ];

    // Only show source column for users
    if (activeKey === AuthObjectType.USER) {
      baseColumns.push({
        title: t('app.config.members.list.table.columns.authSource'),
        dataIndex: 'source',
        width: 130
      });
    }

    baseColumns.push(
      {
        title: t('app.config.members.list.table.columns.authTime'),
        dataIndex: 'createdDate',
        width: 180
      },
      {
        title: t('common.actions'),
        dataIndex: 'action',
        align: 'center',
        width: 120
      }
    );

    return baseColumns;
  });

  return {
    columns
  };
}
