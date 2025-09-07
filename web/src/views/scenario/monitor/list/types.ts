import { ComputedRef, Ref } from 'vue';
import { EnumMessage, PageQuery, SearchCriteria } from '@xcan-angus/infra';
import { ScenarioMonitorStatus } from '@/enums/enums';

export interface MonitorInfo {
  id: string;
  name: string;
  status?: EnumMessage<ScenarioMonitorStatus>;
  failureMessage?: string;
  count?: {
    successRate?: number;
    avgDelayTime?: string;
  };
  scenarioId: string;
  scenarioName: string;
  nextExecDate?: string;
  lastMonitorDate?: string;
  createdByName: string;
  createdDate: string;
}

export type OrderByKey = 'createdDate' | 'createdByName';

export interface MonitorListParams extends PageQuery{
  projectId: string;
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
  placeholder: string;
  allowClear?: boolean;
  maxlength?: number;
  action?: string;
  fieldNames?: { value: string; label: string };
  showTime?: boolean;
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
  menuItems: ComputedRef<MenuItem[]>;
  selectedMenuMap: Ref<{[key: string]: boolean}>;
  orderBy: Ref<OrderByKey | undefined>;
  orderSort: Ref<PageQuery.OrderSort | undefined>;
  searchFilters: Ref<SearchCriteria[]>;
  quickSearchFilters: Ref<SearchCriteria[]>;
  assocFilters: Ref<SearchCriteria[]>;

  // Methods
  getParams: () => PageQuery;
  searchChange: (data: SearchCriteria[]) => void;
  toSort: (sortData: { orderBy: OrderByKey; orderSort: PageQuery.OrderSort }) => void;
  menuItemClick: (data: MenuItem) => void;
  refresh: () => void;
}

export interface UseSearchPanelFiltersReturn {
  // Data state
  statusOpt: MenuItem[];
  statusKeys: string[];
  assocKeys: string[];
  timeKeys: string[];

  // Methods
  formatDateString: (key: string) => [string, string];
}
