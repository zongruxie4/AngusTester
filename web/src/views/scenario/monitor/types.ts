import { ComputedRef, Ref } from 'vue';
import { AuthObjectType, EnumMessage, PageQuery, SearchCriteria } from '@xcan-angus/infra';
import { ScenarioMonitorStatus } from '@/enums/enums';
import { CreateTimeSetting } from '@/types/types';
import { QuickSearchConfig } from '@/components/quickSearch';

export type OrderByKey = 'createdDate' | 'createdByName';

// Type aliases for compatibility
export type FilterItem = SearchCriteria;
export type SearchPanelParams = PageQuery;

export interface MonitorListParams extends PageQuery{
  projectId: string;
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

// Monitor related types
export interface MonitorCount {
  avgDelayTime: string;
  failureNum: number;
  last7DayNum: string;
  last7DaySuccessNum: string;
  last7DaySuccessRate: string;
  last24HoursNum: string;
  last24HoursSuccessNum: string;
  last24HoursSuccessRate: string;
  last30DayNum: string;
  last30DaySuccessNum: string;
  last30DaySuccessRate: string;
  maxDelayTime: string;
  minDelayTime: string;
  p50DelayTime: string;
  p75DelayTime: string;
  p90DelayTime: string;
  successNum: number;
  successRate: string;
  totalNum: string;
}

export type MonitorInfo = {
  _id: string;
  id: string;
  name: string;
  description?: string;
  projectId: string;
  scenarioId: string;
  scenarioName: string;
  nextExecDate: string;
  status: EnumMessage<ScenarioMonitorStatus>;
  failureMessage?: string;
  noticeSetting: NoticeSetting
  serverSetting?: ServerSetting[];
  timeSetting: CreateTimeSetting;
  count?: MonitorCount,
  createdByName: string;
  createdDate: string;
  lastModifiedByName: string;
  lastModifiedDate: string;
  lastMonitorDate?: string;
  lastMonitorHistoryId?: string;
}

// Monitor parameters interface
export interface MonitorParams {
  scenarioId: string | undefined;
  description: string;
  name: string;
  id?: string;
  projectId: string;
  timeSetting: CreateTimeSetting;
  noticeSetting: NoticeSetting;
  serverSetting: ServerSetting[];
}

// CreatedDate component props interface
export interface CreatedDateProps {
  createTimeSetting: CreateTimeSetting;
  showPeriodically?: boolean;
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

export interface TabPaneData {
  value: string;
  _id: string;
  id: string;
  data: MonitorInfo;
  name: string;
}

export interface TabPaneInjection {
  addTabPane: (data: TabPaneData) => void;
  deleteTabPane: (keys: string[]) => void;
}

// History execution record types
export interface HistoryRecord {
  createdBy: string;
  createdByName: string;
  createdDate: string;
  execEndDate: string;
  execId: string;
  execStartDate: string;
  failureMessage: string;
  id: string;
  monitorId: string;
  projectId: string;
  responseDelay: string;
  status: EnumMessage<ScenarioMonitorStatus>;
}

export interface HistoryExecData {
  execId: string;
  execNode: string | {
    id: string;
    name: string;
    ip: string;
    agentPort: string;
    publicIp: string;
  };
  execStartDate: string;
  failureMessage?: string;
  sampleContents?: any[];
  sampleLogContent?: string;
  schedulingResult?: any;
  status: EnumMessage<ScenarioMonitorStatus>;
}

// Chart component props
export interface ChartProps {
  count?: {
    failureNum: number | string;
    successNum: number | string;
    successRate: string;
  };
}

// Tab pane injection type
export type AddTabPaneFunction = (params: {
  value: string;
  _id: string;
  id: string;
  data: any;
  name: string;
}) => void;

export interface UseMonitorDataReturn {
  // Data state
  loaded: Ref<boolean>;
  loading: Ref<boolean>;
  searchedFlag: Ref<boolean>;
  dataList: Ref<MonitorInfo[]>;
  total: Ref<number>;
  pageNo: Ref<number>;
  pageSize: Ref<number>;
  searchPanelParams: Ref<PageQuery>;

  // Methods
  loadData: () => Promise<void>;
  refresh: () => void;
  searchChange: (data: PageQuery) => void;
}

export interface UseMonitorActionsReturn {
  // Methods
  toDelete: (data: MonitorInfo) => Promise<void>;
  editMonitor: (data: MonitorInfo) => void;
  handleDetail: (data: MonitorInfo) => void;
  run: (data: MonitorInfo) => Promise<void>;
  getScenarioDetail: (scenarioId: string) => Promise<void>;
}

export interface UseMonitorUIReturn {
  // Refs
  dataListWrapRef: Ref<HTMLElement | undefined>;

  // Methods
  handleScrollList: (event: Event) => void;
  handleWindowResize: () => void;
}

// SearchPanel related types
export interface SearchPanelOption {
  valueKey: string;
  type: 'input' | 'select' | 'select-user' | 'date-range';
  placeholder: string | string[];
  allowClear?: boolean;
  maxlength?: number;
  action?: string;
  fieldNames?: { value: string; label: string };
  showTime?: boolean;
}

// Form state interface for monitor editing
export interface EditFormState {
  scenarioId: string | undefined;
  description: string;
  name: string;
}

export interface SortMenuItem {
  name: string;
  key: OrderByKey;
  orderSort: PageQuery.OrderSort;
}

export interface MenuItem {
  key: string;
  name: string;
}

export interface UseSearchPanelDataReturn {
  // Data state
  searchPanelOptions: SearchPanelOption[];
  sortMenuItems: SortMenuItem[];
  quickSearchConfig: ComputedRef<QuickSearchConfig>;
  orderBy: Ref<OrderByKey | undefined>;
  orderSort: Ref<PageQuery.OrderSort | undefined>;
  searchFilters: Ref<SearchCriteria[]>;
  quickSearchFilters: Ref<SearchCriteria[]>;
  assocFilters: Ref<SearchCriteria[]>;

  // Methods
  getParams: () => PageQuery;
  searchChange: (data: SearchCriteria[]) => void;
  toSort: (sortData: { orderBy: OrderByKey; orderSort: PageQuery.OrderSort }) => void;
  refresh: () => void;
}
