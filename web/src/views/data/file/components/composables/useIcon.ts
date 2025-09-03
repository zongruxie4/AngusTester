/**
 * Composable for managing icon display properties
 * Provides standardized icon configuration and tooltip handling
 *
 * @returns Object containing methods for icon management
 */
export function useIcon () {
  /**
   * Get icon configuration
   *
   * @param title - Tooltip title
   * @param icon - Icon identifier
   */
  const getIconConfig = (title: string, icon: string) => {
    return {
      title,
      icon
    };
  };

  return {
    getIconConfig
  };
}
