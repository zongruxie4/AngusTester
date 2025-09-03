import { useRouter } from 'vue-router';
import type { CrumbType } from '@/views/data/file/types';

/**
 * Composable for managing breadcrumb navigation
 * Handles navigation between different paths in the file system
 *
 * @returns Object containing methods for breadcrumb navigation
 */
export function useCrumb () {
  const router = useRouter();

  /**
   * Navigate to a specific directory or the home page
   *
   * @param id - ID of the directory to navigate to, or undefined for home
   */
  const jumpToPath = (id?: string): void => {
    if (!id) {
      // Navigate to the main data page
      router.push('/data');
      return;
    }

    // Navigate to the specific directory
    router.push(`/data/file/${id}`);
  };

  /**
   * Process breadcrumb items for display
   * If there are more than 4 items, show ellipsis and only the last 3
   *
   * @param crumb - Array of breadcrumb items
   * @returns Processed breadcrumb items for display
   */
  const processCrumbItems = (crumb: CrumbType[]): CrumbType[] => {
    if (crumb.length > 4) {
      // Show ellipsis and only the last 3 items
      return crumb.slice(-3);
    }

    // Show all items
    return crumb;
  };

  /**
   * Determine if ellipsis should be shown
   *
   * @param crumb - Array of breadcrumb items
   * @returns Boolean indicating if ellipsis should be shown
   */
  const shouldShowEllipsis = (crumb: CrumbType[]): boolean => {
    return crumb.length > 4;
  };

  return {
    jumpToPath,
    processCrumbItems,
    shouldShowEllipsis
  };
}
