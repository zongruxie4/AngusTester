import { SearchCriteria } from '@xcan-angus/infra';

/**
 * Base option interface for all quick search items
 */
export interface BaseQuickSearchOption {
  /** Unique key for the option */
  key: string;
  /** Display name for the option */
  name: string;
  /** Optional description for the option */
  description?: string;
}

/**
 * Audit information option for user-related filters
 * e.g., "我创建的", "我修改的"
 */
export interface AuditInfoOption extends BaseQuickSearchOption {
  /** Type identifier for audit options */
  type: 'audit';
  /** Field key to filter by (e.g., 'createdBy', 'lastModifiedBy') */
  fieldKey: string;
  /** Value to filter by (usually current user ID) */
  fieldValue: string;
}

/**
 * Enum status option for predefined enum values
 * e.g., script types, status values
 */
export interface EnumStatusOption extends BaseQuickSearchOption {
  /** Type identifier for enum options */
  type: 'enum';
  /** Field key to filter by */
  fieldKey: string;
  /** Enum value to filter by */
  fieldValue: string;
}

/**
 * Time range option for predefined time periods
 */
export interface TimeRangeOption extends BaseQuickSearchOption {
  /** Type identifier for time options */
  type: 'time';
  /** Field key to filter by (e.g., 'createdDate', 'lastModifiedDate') */
  fieldKey: string;
  /** Time range value (e.g., 'last1Day', 'last3Days', 'last7Days', 'last30Days') */
  timeRange: TimeRangeValue;
  /** Function to calculate date range based on time range */
  getDateRange: (timeRange: TimeRangeValue) => string[];
}

/**
 * Supported time range values
 */
export type TimeRangeValue = 'last1Day' | 'last3Days' | 'last7Days' | 'last30Days';

/**
 * Union type for all quick search options
 */
export type QuickSearchOption = AuditInfoOption | EnumStatusOption | TimeRangeOption;

/**
 * Enum type configuration for automatic enum options generation
 */
export interface EnumTypeConfig {
  /** The enum type to use */
  enum: any;
  /** Field key for filtering */
  fieldKey: string;
  /** Values to exclude from the enum */
  excludes?: any[];
}

/**
 * Configuration for quick search component
 */
export interface QuickSearchConfig {
  /** Title text for the quick search section */
  title?: string;
  /** Audit information options */
  auditOptions?: AuditInfoOption[];
  /** Enum status options */
  enumOptions?: EnumStatusOption[];
  /** Enum type configuration for automatic generation */
  enumType?: EnumTypeConfig;
  /** Time range options */
  timeOptions?: TimeRangeOption[];
  /** Custom external clear function */
  externalClearFunction?: () => void;
}

/**
 * Props for QuickSearchOptions component
 */
export interface QuickSearchOptionsProps {
  /** Configuration for the quick search options */
  config: QuickSearchConfig;
  /** Currently selected options */
  selectedOptions?: string[];
  /** Callback when options change */
  onChange?: (selectedKeys: string[], searchCriteria: SearchCriteria[]) => void;
  /** Custom description slot content (can be HTML string or plain text) */
  descriptionSlot?: string;
}

/**
 * Internal state for quick search component
 */
export interface QuickSearchState {
  /** Map of selected options */
  selectedMap: Map<string, QuickSearchOption>;
  /** Whether "all" option is selected */
  isAllSelected: boolean;
}

/**
 * Composable return type
 */
export interface UseQuickSearchReturn {
  /** All available menu items */
  menuItems: QuickSearchOption[];
  /** Currently selected options */
  selectedOptions: string[];
  /** Whether "all" option is selected */
  isAllSelected: boolean;
  /** Handle option click */
  handleOptionClick: (option: QuickSearchOption) => void;
  /** Reset all selections */
  resetSelections: () => void;
  /** Get search criteria for API requests */
  getSearchCriteria: () => SearchCriteria[];
  /** Clear external conditions */
  clearExternalConditions: () => void;
}
