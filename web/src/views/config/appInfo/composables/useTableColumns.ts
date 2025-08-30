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
        title: t('app.config.memberList.table.columns.policyId'),
        dataIndex: 'id',
        width: '10%'
      },
      {
        title: t('app.config.memberList.table.columns.policyName'),
        dataIndex: 'name',
        ellipsis: true,
        width: '12%'
      },
      {
        title: t('app.config.memberList.table.columns.policyCode'),
        dataIndex: 'code',
        width: '15%'
      },
      {
        title: t('app.config.memberList.table.columns.policyDescription'),
        dataIndex: 'description'
      }
    ];

    // Only show source column for users
    if (activeKey === AuthObjectType.USER) {
      baseColumns.push({
        title: t('app.config.memberList.table.columns.policySource'),
        dataIndex: 'source',
        width: '15%'
      });
    }

    baseColumns.push(
      {
        title: t('app.config.memberList.table.columns.joinTime'),
        dataIndex: 'createdDate',
        width: 140
      },
      {
        title: t('app.config.memberList.table.columns.action'),
        dataIndex: 'action',
        align: 'center',
        width: 100
      }
    );

    return baseColumns;
  });

  return {
    columns
  };
}
