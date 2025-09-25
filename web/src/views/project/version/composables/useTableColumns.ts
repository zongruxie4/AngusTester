import { computed } from 'vue';
import { useI18n } from 'vue-i18n';

export function useTableColumns () {
  const { t } = useI18n();

  /**
   * Generate table columns configuration
   * Defines column structure, widths, and custom renderers
   */
  const columns = computed(() => [
    {
      title: t('version.columns.version'),
      dataIndex: 'name',
      width: 100
    },
    {
      title: t('common.status'),
      dataIndex: 'status',
      width: 100
    },
    {
      title: t('common.progress'),
      dataIndex: 'progress',
      width: 200
    },
    {
      title: t('version.columns.startDate'),
      dataIndex: 'startDate',
      customRender: ({ text }: { text: string }) => text || '--',
      width: 150
    },
    {
      title: t('version.columns.releaseDate'),
      dataIndex: 'releaseDate',
      customRender: ({ text }: { text: string }) => text || '--',
      width: 150
    },
    {
      title: t('common.description'),
      dataIndex: 'description',
      width: 200
    },
    {
      title: t('version.columns.lastModifier'),
      dataIndex: 'lastModifiedByName',
      groupName: 'person',
      width: 100
    },
    {
      title: t('common.creator'),
      dataIndex: 'createdByName',
      groupName: 'person',
      hide: true,
      width: 100
    },
    {
      title: t('version.columns.lastModifyTime'),
      dataIndex: 'lastModifiedDate',
      groupName: 'date',
      width: 100
    },
    {
      title: t('version.columns.createTime'),
      dataIndex: 'createdDate',
      groupName: 'date',
      hide: true,
      width: 100
    },
    {
      title: t('common.actions'),
      dataIndex: 'actions',
      width: 150
    }
  ]);

  return {
    columns
  };
}
