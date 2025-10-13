import { AppInfo, User } from '@xcan-angus/infra';

/**
 * Data type enumeration for different item types
 */
export type DataType = 'dataset' | 'space' | 'datasource' | 'variable';

/**
 * Props interface for the Table component
 */
export type AddedTableProps = {
  projectId: string;
  userId: string;
  total: number;
  notify: string;
  deletedNotify: string;
  type: DataType;
}

/**
 * <p>
 * Created item interface representing a data item
 * </p>
 */
export type AddedItem = {
  id: string;
  name: string;
  createdBy: string;
  createdByName: string;
  ownerId: string;
  ownerName: string;
  avatar: string;
  createdDate: string;
  lastModifiedDate: string;
  auth: boolean;
}

/**
 * <p>Component props interface for main home component</p>
 * <p>Defines the required and optional properties passed to the component</p>
 */
export interface HomepageProps {
  /** Project identifier for the home */
  projectId: string;
  /** User information object containing user details */
  userInfo: User;
  /** Application information object */
  appInfo: AppInfo;
  /** Refresh notification string for triggering updates */
  refreshNotify: string;
}

/**
 * <p>Statistics data structure for user-specific statistics</p>
 * <p>Contains counts for services and APIs created by the user</p>
 */
export interface UserCreationStatistics {
  /** Total number of services created by user */
  allService: string;
  /** Services created in last week */
  serviceByLast7Days: string;
  /** Services created in last month */
  serviceByLastMonth: string;
  /** Total number of APIs created by user */
  allApis: string;
  /** APIs created in last week */
  apisByLast7Days: string;
  /** APIs created in last month */
  apisByLastMonth: string;
  /** Total number of unarchived APIs */
  allUnarchivedApis: string;
  /** Unarchived APIs created in last week */
  unarchivedApisByLast7Days: string;
  /** Unarchived APIs created in last month */
  unarchivedApisByLastMonth: string;
}

/**
 * <p>Project-wide statistics data structure</p>
 * <p>Contains counts for all project resources</p>
 */
export interface ProjectCreationStatistics {
  /** Total number of variables in project */
  allVariable: number;
  /** Total number of datasets in project */
  allDataset: number;
  /** Total number of datasources in project */
  allDatasource: number;
  /** Total number of files in project */
  allFile: number;
  /** Variable usage statistics */
  variableByUse: {
    IN_USE: number;
    NOT_IN_USE: number;
  };
  /** File resource type statistics */
  fileByResourceType: {
    SPACE: number;
    DIRECTORY: number;
    FILE: number;
  };
  /** Database type statistics for datasources */
  datasourceByDb: {
    DB2: number;
    POSTGRES: number;
    MARIADB: number;
    MYSQL: number;
    ORACLE: number;
    SQLSERVER: number;
  };
  /** Dataset usage statistics */
  datasetByUse?: {
    IN_USE: number;
    NOT_IN_USE: number;
  };
}

/**
 * <p>Statistics configuration for display cards</p>
 * <p>Defines the appearance and data mapping for statistics cards</p>
 */
export interface StatisticsCardConfig {
  /** CSS class for top section styling */
  topClass: string;
  /** CSS class for bottom section styling */
  bottomClass: string;
  /** Property name for total count */
  total: string;
  /** Property name for weekly count */
  week: string;
  /** Property name for monthly count */
  month: string;
  /** Display name for the statistics card */
  name: string;
}

/**
 * <p>ECharts chart option configuration</p>
 * <p>Base configuration for pie charts used in statistics</p>
 */
export interface ChartOption {
  title: {
    text: string;
    textStyle: {
      fontSize: number;
    };
  };
  tooltip: {
    show: boolean;
  };
  legend: {
    top?: string;
    bottom?: string;
    left?: string;
    right?: string;
    orient: string;
    itemHeight: number;
    itemWidth: number;
    itemGap?: number;
    textStyle?: {
      fontSize: number;
    };
  };
  grid: {
    top: string;
    left: string;
    right: string;
    bottom: string;
    containLabel: boolean;
  };
  series: Array<{
    name: string;
    type: string;
    radius: string[];
    center: string[];
    avoidLabelOverlap: boolean;
    label: {
      show: boolean;
      formatter: string;
      fontSize?: number;
    };
    emphasis: {
      label: {
        show: boolean;
        fontSize?: number;
        fontWeight?: string;
      };
    };
    labelLine: {
      show: boolean;
      length: number;
      length2?: number;
    };
    data: Array<{
      name: string;
      value: number;
      itemStyle: {
        color: string;
      };
    }>;
  }>;
}

/**
 * <p>Introduction tip item structure</p>
 * <p>Defines the content for introduction tips</p>
 */
export interface IntroductionTip {
  /** Title of the tip */
  title: string;
  /** Content description of the tip */
  content: string;
}
