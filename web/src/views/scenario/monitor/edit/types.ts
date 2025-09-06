import { CreatedAt, PeriodicUnit, DayOfWeek } from '@xcan-angus/infra';

// Time setting configuration interface
export interface CreateTimeSetting {
  createdAt: CreatedAt;
  periodicCreationUnit?: PeriodicUnit;
  dayOfWeek?: DayOfWeek;
  createdAtSomeDate?: string;
  dayOfMonth?: string;
  timeOfDay?: string;
  minuteOfHour?: string;
  hourOfDay?: string;
}

// Component props interface
export interface CreatedDateProps {
  createTimeSetting: CreateTimeSetting;
  showPeriodically: boolean;
}

// Enum field names configuration
export interface EnumFieldNames {
  label: string;
  value: string;
}

// Option item interface for dropdowns
export interface OptionItem {
  label: string;
  value: string;
}

// Day of month option interface
export interface DayOfMonthOption {
  message: string;
  value: string;
}

// Time validation result
export interface TimeValidationResult {
  isValid: boolean;
  data?: CreateTimeSetting;
}

// Component exposed methods
export interface CreatedDateExpose {
  validate: () => TimeValidationResult;
  getData: () => CreateTimeSetting | undefined;
}
