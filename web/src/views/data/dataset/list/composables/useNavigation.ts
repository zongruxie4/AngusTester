import { useRouter } from 'vue-router';
import { DataMenuKey } from '@/views/data/menu';

/**
 * Navigation composable
 * Handles routing navigation for dataset list operations
 */
export function useNavigation () {
  const router = useRouter();

  /**
   * Navigate to create static dataset page
   * Routes to the static dataset creation form
   */
  const toCreateStaticDataSet = () => {
    router.push(`/data#${DataMenuKey.DATASET}?source=VALUE`);
  };

  /**
   * Navigate to create file dataset page
   * Routes to the file dataset creation form
   */
  const toCreateFileDataSet = () => {
    router.push(`/data#${DataMenuKey.DATASET}?source=FILE`);
  };

  /**
   * Navigate to create JDBC dataset page
   * Routes to the JDBC dataset creation form
   */
  const toCreateJdbcDataSet = () => {
    router.push(`/data#${DataMenuKey.DATASET}?source=JDBC`);
  };

  /**
   * Handle dropdown menu click for creation options
   * Routes to the appropriate dataset creation page based on selection
   */
  const handleCreationDropdownClick = ({ key }: { key: 'file' | 'jdbc' }) => {
    if (key === 'file') {
      toCreateFileDataSet();
      return;
    }

    if (key === 'jdbc') {
      toCreateJdbcDataSet();
    }
  };

  /**
   * Navigate to edit dataset page
   * Routes to the dataset edit form with the specified dataset ID
   */
  const toEditDataset = (id: string) => {
    router.push(`/data#${DataMenuKey.DATASET}?id=${id}`);
  };

  return {
    // Navigation methods
    toCreateStaticDataSet,
    toCreateFileDataSet,
    toCreateJdbcDataSet,
    handleCreationDropdownClick,
    toEditDataset
  };
}
