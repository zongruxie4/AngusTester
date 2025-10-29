import { watch } from 'vue';
import { useData } from './useData';
import { useSearchConfig } from './useSearchConfig';
import { useUIActions } from './useUIActions';

/**
 * <p>Main composable for DataSource component business logic</p>
 * <p>Integrates all other composables and provides the main component functionality</p>
 */
export function useDataSource (props: {projectId: string}) {
  // Initialize all composables
  const data = useData();
  const searchConfig = useSearchConfig();
  const uiActions = useUIActions();

  /**
   * <p>Handle search parameter changes</p>
   * <p>Updates search filters and fetches new data</p>
   */
  const handleSearchChange = (filters: { key: string; value: string; op: any }[]): void => {
    data.updateSearchParams(filters);
  };

  /**
   * <p>Handle pagination changes</p>
   * <p>Updates page parameters and fetches new data</p>
   */
  const handlePaginationChange = (page: number, size: number): void => {
    data.updatePaginationParams(page, size);
  };

  /**
   * <p>Handle refresh action</p>
   * <p>Refreshes the data source list</p>
   */
  const handleRefresh = (): void => {
    data.refreshDataSourceList();
  };

  /**
   * <p>Handle add action</p>
   * <p>Opens the add modal for creating new data source</p>
   */
  const handleAdd = (): void => {
    uiActions.openAddModal();
  };

  /**
   * <p>Handle edit action</p>
   * <p>Opens the edit modal for modifying existing data source</p>
   */
  const handleEdit = (record: any): void => {
    uiActions.openEditModal(record);
  };

  /**
   * <p>Handle delete action</p>
   * <p>Shows confirmation dialog and deletes data source if confirmed</p>
   */
  const handleDelete = (record: any): void => {
    uiActions.showDeleteConfirmation(record, async () => {
      await data.deleteDataSource(record.id);
      uiActions.showSuccessNotification('actions.tips.deleteSuccess');
    });
  };

  /**
   * <p>Handle connection test action</p>
   * <p>Tests the connection to a specific data source</p>
   */
  const handleTestConnection = (record: any): void => {
    data.testDataSourceConnection(record);
  };

  /**
   * <p>Handle modal close</p>
   * <p>Closes modal and refreshes data if needed</p>
   */
  const handleModalClose = (): void => {
    uiActions.closeModal();
  };

  /**
   * <p>Handle modal refresh</p>
   * <p>Closes modal and refreshes data source list</p>
   */
  const handleModalRefresh = (): void => {
    uiActions.closeModal();
    data.refreshDataSourceList();
  };

  // Watch for project ID changes and fetch data
  watch(() => props.projectId, (newValue) => {
    
    if (newValue) {
      data.params.value.pageNo = 1;
      data.getDataSourceList(newValue);
    }
  }, {
    immediate: true
  });

  return {
    // Data state and methods
    ...data,

    // Search configuration
    ...searchConfig,

    // UI actions
    ...uiActions,

    // Event handlers
    handleSearchChange,
    handlePaginationChange,
    handleRefresh,
    handleAdd,
    handleEdit,
    handleDelete,
    handleTestConnection,
    handleModalClose,
    handleModalRefresh
  };
}
