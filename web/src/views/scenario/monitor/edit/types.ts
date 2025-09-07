import { AuthObjectType, CreatedAt } from '@xcan-angus/infra';

// Form state interface for monitor editing
export interface FormState {
  scenarioId: string | undefined;
  description: string;
  name: string;
}

// Time setting configuration
export interface TimeSetting {
  createdAt: CreatedAt;
  createdAtSomeDate?: string;
  dayOfMonth?: string;
  dayOfWeek?: string;
  periodicCreationUnit?: 'DAILY' | 'WEEKLY' | 'MONTHLY';
  timeOfDay?: string;
}

// Notice setting configuration
export interface NoticeSetting {
  enabled: boolean;
  orgType: AuthObjectType;
  orgs: { id: string; name: string }[];
}

// Server setting configuration
export interface ServerSetting {
  url: string;
  description?: string;
  variables?: {
    [key: string]: {
      enum: string[];
      default: string;
    };
  };
}

// Monitor information interface
export interface MonitorInfo {
  _id: string;
  id?: string;
  scenarioId: string;
  description: string;
  name: string;
  projectId: string;
  timeSetting: TimeSetting;
  noticeSetting: NoticeSetting;
  serverSetting: ServerSetting[];
}

// Organization item interface
export interface OrgItem {
  id: string;
  name: string;
  fullName?: string;
}

// Component props interface
export interface MonitorEditProps {
  projectId: string;
  userInfo: {
    id: string;
    fullName: string;
  };
  appInfo: {
    id: string;
  };
  _id: string;
  data: {
    _id: string;
    id: string | undefined;
  };
}

// Scenario option interface
export interface ScenarioOption {
  id: string;
  name: string;
  plugin: string;
}

// Monitor parameters interface
export interface MonitorParams {
  scenarioId: string | undefined;
  description: string;
  name: string;
  id?: string;
  projectId: string;
  timeSetting: TimeSetting;
  noticeSetting: NoticeSetting;
  serverSetting: ServerSetting[];
}

// Time setting interface for CreatedDate component
export interface CreateTimeSetting {
  createdAt: CreatedAt;
  createdAtSomeDate?: string;
  dayOfMonth?: string;
  dayOfWeek?: string;
  periodicCreationUnit?: 'DAILY' | 'WEEKLY' | 'MONTHLY';
  timeOfDay?: string;
  hourOfDay?: string;
  minuteOfHour?: string;
}

// CreatedDate component props interface
export interface CreatedDateProps {
  createTimeSetting: CreateTimeSetting;
  showPeriodically?: boolean;
}

// Enum field names configuration
export interface EnumFieldNames {
  label: string;
  value: string;
}

// Day of month option interface
export interface DayOfMonthOption {
  message: string;
  value: string;
}

// Generic option item interface
export interface OptionItem {
  label: string;
  value: string;
}

// Time validation result interface
export interface TimeValidationResult {
  isValid: boolean;
  message?: string;
}
