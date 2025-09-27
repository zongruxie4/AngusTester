import { ref, computed, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { SearchCriteria, enumUtils } from '@xcan-angus/infra';
import { DATE_TIME_FORMAT } from '@/utils/constant';
import type {
  QuickSearchConfig,
  QuickSearchOption,
  UseQuickSearchReturn,
  TimeRangeValue,
  AuditInfoOption,
  EnumStatusOption,
  TimeRangeOption,
  EnumTypeConfig
} from '../types';

/**
 * Time range calculation function
 * @param timeRange - The time range value
 * @returns Array of date strings [startDate, endDate] in DATE_TIME_FORMAT
 */
const getDateRange = (timeRange: TimeRangeValue): string[] => {
  const now = new Date();
  
  let days = 1;
  switch (timeRange) {
    case 'last1Day':
      days = 1;
      break;
    case 'last3Days':
      days = 3;
      break;
    case 'last7Days':
      days = 7;
      break;
    case 'last30Days':
      days = 30;
      break;
  }
  
  const endDate = now;
  const startDate = new Date(now.getTime() - days * 24 * 60 * 60 * 1000);
  
  // Format dates using DATE_TIME_FORMAT
  const formatDate = (date: Date) => {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    const seconds = String(date.getSeconds()).padStart(2, '0');
    
    return DATE_TIME_FORMAT
      .replace('YYYY', year.toString())
      .replace('MM', month)
      .replace('DD', day)
      .replace('HH', hours)
      .replace('mm', minutes)
      .replace('ss', seconds);
  };
  
  return [formatDate(startDate), formatDate(endDate)];
};

/**
 * Composable for managing quick search functionality
 * @param config - Configuration for quick search options
 * @param onChange - Callback when options change
 */
export function useQuickSearch (
  config: QuickSearchConfig,
  onChange?: (selectedKeys: string[], searchCriteria: SearchCriteria[]) => void
): UseQuickSearchReturn {
  const { t } = useI18n();

  // Internal state
  const selectedMap = ref<Map<string, QuickSearchOption>>(new Map());
  const isAllSelected = ref(false);

  /**
   * Create "All" option
   */
  const createAllOption = (): QuickSearchOption => ({
    key: 'all',
    name: t('common.all'),
    type: 'audit',
    fieldKey: '',
    fieldValue: ''
  });

  /**
   * Generate enum options from enum type configuration
   */
  const generateEnumOptionsFromType = (enumTypeConfig: EnumTypeConfig): EnumStatusOption[] => {
    const data = enumUtils.enumToMessages(enumTypeConfig.enum, enumTypeConfig.excludes);
    return data.map(item => ({
      key: item.value,
      name: item.message,
      type: 'enum' as const,
      fieldKey: enumTypeConfig.fieldKey,
      fieldValue: item.value
    }));
  };

  /**
   * Build all available menu items from configuration
   */
  const menuItems = computed((): QuickSearchOption[] => {
    const items: QuickSearchOption[] = [createAllOption()];
    
    // Add audit options
    if (config.auditOptions?.length) {
      items.push(...config.auditOptions);
    }
    
    // Add enum options (from enumOptions or generated from enumType)
    if (config.enumOptions?.length) {
      items.push(...config.enumOptions);
    } else if (config.enumType) {
      items.push(...generateEnumOptionsFromType(config.enumType));
    }
    
    // Add time options
    if (config.timeOptions?.length) {
      items.push(...config.timeOptions);
    }
    
    return items;
  });

  /**
   * Get currently selected option keys
   */
  const selectedOptions = computed((): string[] => {
    return Array.from(selectedMap.value.keys());
  });

  /**
   * Generate search criteria from selected options
   */
  const getSearchCriteria = (): SearchCriteria[] => {
    const criteria: SearchCriteria[] = [];

    // If "all" is selected, return empty criteria
    if (isAllSelected.value) {
      return criteria;
    }

    // Process each selected option
    selectedMap.value.forEach((option) => {
      if (option.type === 'audit' && option.fieldKey && option.fieldValue) {
        criteria.push({
          key: option.fieldKey,
          op: SearchCriteria.OpEnum.Equal,
          value: option.fieldValue
        });
      } else if (option.type === 'enum' && option.fieldKey && option.fieldValue) {
        criteria.push({
          key: option.fieldKey,
          op: SearchCriteria.OpEnum.Equal,
          value: option.fieldValue
        });
      } else if (option.type === 'time' && option.fieldKey) {
        const timeOption = option as TimeRangeOption;
        const dateRange = timeOption.getDateRange(timeOption.timeRange);
        // Use GreaterThanEqual and LessThanEqual for date range
        criteria.push({
          key: option.fieldKey,
          op: SearchCriteria.OpEnum.GreaterThanEqual,
          value: dateRange[0]
        });
        criteria.push({
          key: option.fieldKey,
          op: SearchCriteria.OpEnum.LessThanEqual,
          value: dateRange[1]
        });
      }
    });

    return criteria;
  };

  /**
   * Clear external conditions using provided function
   */
  const clearExternalConditions = () => {
    if (typeof config.externalClearFunction === 'function') {
      config.externalClearFunction();
    }
  };

  /**
   * Handle option click
   * @param option - The clicked option
   */
  const handleOptionClick = (option: QuickSearchOption) => {
    const key = option.key;

    // Handle "All" option
    if (key === 'all') {
      if (isAllSelected.value) {
        // Already selected, do nothing
        return;
      }

      // Select "All" and clear all other selections
      selectedMap.value.clear();
      isAllSelected.value = true;
      clearExternalConditions();
    } else {
      // Handle other options
      if (selectedMap.value.has(key)) {
        // Toggle off - remove from selection
        selectedMap.value.delete(key);
      } else {
        // Toggle on - add to selection
        // Clear "All" selection
        isAllSelected.value = false;

        // Handle mutual exclusion within groups
        if (option.type === 'enum') {
          // Clear other enum options
          if (config.enumOptions?.length) {
            config.enumOptions.forEach(enumOpt => {
              if (enumOpt.key !== key) {
                selectedMap.value.delete(enumOpt.key);
              }
            });
          } else if (config.enumType) {
            // Clear dynamically generated enum options
            const generatedOptions = generateEnumOptionsFromType(config.enumType);
            generatedOptions.forEach(enumOpt => {
              if (enumOpt.key !== key) {
                selectedMap.value.delete(enumOpt.key);
              }
            });
          }
        } else if (option.type === 'time') {
          // Clear other time options
          config.timeOptions?.forEach(timeOpt => {
            if (timeOpt.key !== key) {
              selectedMap.value.delete(timeOpt.key);
            }
          });
        }

        selectedMap.value.set(key, option);
      }
    }

    // Trigger change callback
    if (onChange) {
      onChange(selectedOptions.value, getSearchCriteria());
    }
  };

  /**
   * Reset all selections
   */
  const resetSelections = () => {
    selectedMap.value.clear();
    isAllSelected.value = false;
    clearExternalConditions();

    if (onChange) {
      onChange([], []);
    }
  };

  /**
   * Watch for external changes and trigger callback
   */
  watch(
    [selectedMap, isAllSelected],
    () => {
      if (onChange) {
        onChange(selectedOptions.value, getSearchCriteria());
      }
    },
    { deep: true }
  );

  return {
    menuItems: menuItems.value,
    selectedOptions: selectedOptions.value,
    isAllSelected: isAllSelected.value,
    handleOptionClick,
    resetSelections,
    getSearchCriteria,
    clearExternalConditions
  };
}

/**
 * Helper function to create audit info options
 * @param options - Array of audit option configurations
 * @param userId - Current user ID
 * @returns Array of audit info options
 */
export function createAuditOptions (
  options: Array<{ key: string; name: string; fieldKey: string }>,
  userId: string
): AuditInfoOption[] {
  return options.map(option => ({
    ...option,
    type: 'audit' as const,
    fieldValue: userId
  }));
}

/**
 * Helper function to create enum status options
 * @param options - Array of enum option configurations
 * @returns Array of enum status options
 */
export function createEnumOptions (
  options: Array<{ key: string; name: string; fieldKey: string; fieldValue: string }>
): EnumStatusOption[] {
  return options.map(option => ({
    ...option,
    type: 'enum' as const
  }));
}

/**
 * Helper function to create time range options
 * @param options - Array of time option configurations
 * @param fieldKey - Field key for date filtering
 * @returns Array of time range options
 */
export function createTimeOptions (
  options: Array<{ key: string; name: string; timeRange: TimeRangeValue }>,
  fieldKey: string
): TimeRangeOption[] {
  return options.map(option => ({
    ...option,
    type: 'time' as const,
    fieldKey,
    getDateRange: getDateRange
  }));
}

/**
 * Helper function to create enum type configuration
 * @param enumType - The enum type to use
 * @param fieldKey - Field key for filtering
 * @param excludes - Values to exclude from the enum
 * @returns Enum type configuration
 */
export function createEnumTypeConfig(
  enumType: any,
  fieldKey: string,
  excludes?: any[]
): EnumTypeConfig {
  return {
    enum: enumType,
    fieldKey,
    excludes
  };
}
