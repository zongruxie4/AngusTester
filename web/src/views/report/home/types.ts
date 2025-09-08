import { Ref } from 'vue';
import { PageQuery, SearchCriteria, EnumMessage, CombinedTargetType } from '@xcan-angus/infra';
import { ReportCategory, ReportPermission, ReportStatus, ReportTemplate } from '@/enums/enums';

// Base types
export type OrderByKey = 'createdDate' | 'createdByName';

// Report related types
export interface Report {
  id: string;
  name: string;
  version: string;
  description?: string;
  category: EnumMessage<ReportCategory>;
  template: EnumMessage<ReportTemplate>;
  status?: EnumMessage<ReportStatus>;
  targetType?: EnumMessage<CombinedTargetType>;
  targetId?: string;
  targetName?: string;
  nextGenerationDate?: string;
  currentAuths?: ReportPermission[];
  auth?: boolean;
  failureMessage?: string;
  createdBy?: string;
  createdByName?: string;
  createdDate?: string;
  lastModifiedBy?: string;
  lastModifiedByName?: string;
  lastModifiedDate?: string;
}

export interface TargetIdFilter {
  key: 'targetId';
  op: 'EQUAL';
  value: string | undefined;
}

// Menu item types
export type MenuItemKey = 'none' | 'createdBy' | 'lastModifiedBy' | 'lastDay' | 'lastThreeDays' | 'lastWeek';

export interface MenuItem {
  key: MenuItemKey;
  name: string;
}

// Tree data types
export interface TreeDataNode {
  name: string;
  value: string;
  key: string;
  children?: TreeDataNode[];
}

// Pagination types
export interface PaginationConfig {
  current: number;
  pageSize: number;
  total: number;
}

// User and app info types
export interface UserInfo {
  id: string | number;
}

export interface AppInfo {
  id: string | number;
}

export interface ProjectInfo {
  id: string;
  type: {
    value: string;
  };
}

// Search panel types
export interface SearchOption {
  valueKey: string;
  placeholder: string | string[];
  type: 'input' | 'select' | 'select-enum' | 'date-range' | 'select-user' | 'select-app' | 'select-dept' | 'select-group' | 'select-intl' | 'select-itc' | 'select-service' | 'select-tag' | 'select-tenant' | 'date' | 'tree-select';
  maxlength?: number;
  enumKey?: string;
  excludes?: (data: any) => boolean;
  noDefaultSlot?: boolean;
  showTime?: boolean;
  width?: number;
}

// Action types
export interface ActionItem {
  name: string;
  key: string;
  icon: string;
  permission: string;
}

// Table column types
export interface TableColumn {
  key: string;
  dataIndex: string;
  title: string;
  sorter?: boolean;
  width?: number;
  groupName?: string;
  hide?: boolean;
  ellipsis?: boolean;
  customRender?: (params: { text: any; record: Report }) => any;
}

// Props types
export interface CategorySelectProps {
  category: string;
}

export interface SearchPanelProps {
  projectId: string;
  userInfo: UserInfo;
  appInfo: AppInfo;
  notify: string;
}

export interface PieChartProps {
  projectId: string;
}

// Emit types
export interface CategorySelectEmits {
  (e: 'update:category', value: string): void;
}

export interface SearchPanelEmits {
  (e: 'openAuth'): void;
  (e: 'add'): void;
  (e: 'sort', value: { orderBy: OrderByKey; orderSort: PageQuery.OrderSort }): void;
  (e: 'change', value: SearchCriteria[]): void;
}

// Composable return types
export interface UseReportDataReturn {
  dataList: Ref<Report[]>;
  loading: Ref<boolean>;
  pagination: Ref<PaginationConfig>;
  filters: Ref<SearchCriteria[]>;
  category: Ref<string>;
  selectId: Ref<string | undefined>;
  selectReportPermissions: Ref<string[]>;
  generateLoading: Ref<Record<string, boolean>>;
  projectId: Ref<string>;
  appInfo: Ref<any>;
  userInfo: Ref<any>;
  loadDataList: () => Promise<void>;
  deleteReport: (report: Report) => void;
  getShareToken: (report: Report) => Promise<void>;
  generateReport: (report: Report) => Promise<void>;
  addReport: (reportId?: string) => void;
  viewReport: (report?: Report) => void;
  authFlagChange: (params: { auth: boolean }) => void;
  searchPanelChange: (data: SearchCriteria[]) => void;
  categoryChange: (value: string) => void;
  onPageChange: (page: any, _: any, sortData: any) => void;
  toSort: (data: { orderBy: OrderByKey; orderSort: PageQuery.OrderSort }) => void;
}

export interface UseTableColumnsReturn {
  columns: TableColumn[];
  actionItems: ActionItem[];
}

export interface UseSearchPanelReturn {
  searchOptions: Ref<SearchOption[]>;
  menuItems: MenuItem[];
  sortMenus: Array<{
    name: string;
    key: OrderByKey;
    orderSort: PageQuery.OrderSort;
  }>;
  selectedMenuMap: Ref<Map<string, Omit<MenuItem, 'name'>>>;
  quickDateMap: Ref<Map<MenuItemKey, string[]>>;
  filters: Ref<SearchCriteria[]>;
  targetIdFilter: Ref<TargetIdFilter>;
  searchPanelRef: Ref<any>;
  isProjectTargetType: Ref<boolean>;
  isServiceTargetType: Ref<boolean>;
  isAPITargetType: Ref<boolean>;
  isTaskTargetType: Ref<boolean>;
  isSprintTargetType: Ref<boolean>;
  isPlanTargetType: Ref<boolean>;
  isCaseTargetType: Ref<boolean>;
  isExecutionTargetType: Ref<boolean>;
  isScenarioTargetType: Ref<boolean>;
  menuItemClick: (data: MenuItem) => void;
  searchPanelChange: (data: SearchCriteria[], headers?: { [key: string]: string }, key?: string) => void;
  targetIdChange: (value: string) => void;
  toSort: (data: { orderBy: OrderByKey; orderSort: PageQuery.OrderSort }) => void;
  toRefresh: () => void;
  toAuth: () => void;
  getData: () => SearchCriteria[];
}

export interface UseCategorySelectReturn {
  moduleTreeData: Ref<TreeDataNode[]>;
  categoryIcon: Record<string, string>;
  handleSelectKeysChange: (value: string[]) => void;
  loadOpt: () => void;
}
