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
      title: t('common.version'),
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
      title: t('common.startDate'),
      dataIndex: 'startDate',
      customRender: ({ text }: { text: string }) => text || '--',
      width: 130
    },
    {
      title: t('common.releaseDate'),
      dataIndex: 'releaseDate',
      customRender: ({ text }: { text: string }) => text || '--',
      width: 130
    },
    {
      title: t('common.description'),
      dataIndex: 'description',
      width: 200
    },
    {
      title: t('common.modifier'),
      dataIndex: 'modifier',
      groupName: 'person',
      width: 100
    },
    {
      title: t('common.creator'),
      dataIndex: 'creator',
      groupName: 'person',
      hide: true,
      width: 100
    },
    {
      title: t('common.modifiedDate'),
      dataIndex: 'modifiedDate',
      groupName: 'date',
      width: 130
    },
    {
      title: t('common.createdDate'),
      dataIndex: 'createdDate',
      groupName: 'date',
      hide: true,
      width: 130
    },
    {
      title: t('common.actions'),
      dataIndex: 'actions',
      width: 130
    }
  ]);

  return {
    columns
  };
}
