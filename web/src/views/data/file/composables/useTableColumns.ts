import { computed } from 'vue';
import { useI18n } from 'vue-i18n';

/**
 * <p>Composable for managing table column configurations.</p>
 * <p>Provides column definitions for both space list and file list tables.</p>
 *
 * @returns Object containing column configurations for different table types
 */
export function useTableColumns () {
  const { t } = useI18n();

  /**
   * <p>Column configuration for space list table.</p>
   * <p>Defines the structure and display properties for space information columns.</p>
   */
  const spaceColumns = computed(() => [
    {
      dataIndex: 'name',
      key: 'name',
      title: t('fileSpace.columns.spaceName'),
      width: '20%',
      ellipsis: true
    },
    {
      dataIndex: 'subDirectoryNum',
      key: 'subDirectoryNum',
      title: t('fileSpace.columns.folderCount'),
      width: '10%',
      ellipsis: true
    },
    {
      dataIndex: 'subFileNum',
      key: 'subFileNum',
      title: t('fileSpace.columns.fileCount'),
      width: '10%',
      ellipsis: true
    },
    {
      dataIndex: 'size',
      key: 'size',
      title: t('fileSpace.columns.used'),
      width: '10%',
      ellipsis: true
    },
    {
      dataIndex: 'quotaSize',
      key: 'quotaSize',
      title: t('fileSpace.columns.quota'),
      width: '10%',
      ellipsis: true
    },
    {
      dataIndex: 'createdByName',
      key: 'createdByName',
      title: t('fileSpace.columns.addBy'),
      width: '13%',
      ellipsis: true
    },
    {
      dataIndex: 'createdDate',
      key: 'createdDate',
      title: t('common.createdDate'),
      width: '13%',
      ellipsis: true
    },
    {
      dataIndex: 'action',
      key: 'action',
      title: t('common.actions'),
      width: 160
    }
  ]);

  /**
   * <p>Column configuration for file list table.</p>
   * <p>Defines the structure and display properties for file and directory information columns.</p>
   */
  const fileColumns = computed(() => [
    {
      title: t('common.name'),
      dataIndex: 'name',
      width: '30%',
      ellipsis: true
    },
    {
      title: t('fileSpace.columns.fileCount'),
      dataIndex: 'fileNum',
      width: 60,
      ellipsis: true
    },
    {
      title: t('fileSpace.columns.folderCount'),
      dataIndex: 'subDirectoryNum',
      width: 80,
      ellipsis: true
    },
    {
      title: t('fileSpace.columns.actualSize'),
      dataIndex: 'size',
      ellipsis: true,
      width: 80
    },
    {
      title: t('common.lastModifiedDate'),
      dataIndex: 'lastModifiedDate',
      ellipsis: true,
      width: 160
    },
    {
      title: t('common.actions'),
      dataIndex: 'action',
      width: 380,
      fixed: 'right'
    }
  ]);

  /**
   * <p>Column configuration for file list table with additional metadata.</p>
   * <p>Includes subdirectory count and other file-specific information.</p>
   */
  const fileColumnsWithMetadata = computed(() => [
    ...fileColumns.value.slice(0, -1), // All columns except action
    {
      title: t('fileSpace.columns.subDirectoryCount'),
      dataIndex: 'subDirectoryNum',
      width: 80,
      ellipsis: true
    },
    fileColumns.value[fileColumns.value.length - 1] // Action column
  ]);

  return {
    spaceColumns,
    fileColumns,
    fileColumnsWithMetadata
  };
}
