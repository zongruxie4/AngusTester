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
  const isAllSelected = ref(true); // 默认选中"全部"选项

  // Initialize with "all" selected and trigger initial callback
  const initializeWithAllSelected = () => {
    if (onChange) {
      onChange(['all'], []);
    }
  };

  // Generate enum options at setup time if enumType is provided
  const generatedEnumOptions = ref<EnumStatusOption[]>([]);
  if (config.enumType) {
    try {
      const data = enumUtils.enumToMessages(config.enumType.enum, config.enumType.excludes);
      generatedEnumOptions.value = data.map(item => ({
        key: item.value,
        name: item.message,
        type: 'enum' as const,
        fieldKey: config.enumType!.fieldKey,
        fieldValue: item.value
      }));
    } catch (error) {
      console.warn('Failed to generate enum options:', error);
      generatedEnumOptions.value = [];
    }
  }

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
    } else if (generatedEnumOptions.value.length > 0) {
      items.push(...generatedEnumOptions.value);
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
    if (isAllSelected.value) {
      return ['all'];
    }
    return Array.from(selectedMap.value.keys());
  });

  /**
   * Generate search criteria from selected options
   */
  const getSearchCriteria = (): SearchCriteria[] => {
    const criteria: SearchCriteria[] = [];

    // If "all" is selected or no options are selected, return empty criteria (query all data)
    if (isAllSelected.value || selectedMap.value.size === 0) {
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
        // Already selected, toggle off - clear all selections
        selectedMap.value.clear();
        isAllSelected.value = false;
        clearExternalConditions();

        // Manually trigger onChange since watch might not detect the change
        if (onChange) {
          onChange(selectedOptions.value, getSearchCriteria());
        }
      } else {
        // Select "All" and clear all other selections
        selectedMap.value.clear();
        isAllSelected.value = true;
        clearExternalConditions();

        // Manually trigger onChange since watch might not detect the change
        if (onChange) {
          onChange(selectedOptions.value, getSearchCriteria());
        }
      }
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
          } else if (generatedEnumOptions.value.length > 0) {
            // Clear pre-generated enum options
            generatedEnumOptions.value.forEach(enumOpt => {
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
    { deep: true, immediate: false }
  );

  // Initialize with "all" selected
  initializeWithAllSelected();

  return {
    menuItems,
    selectedOptions,
    isAllSelected,
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
export function createEnumTypeConfig (
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
