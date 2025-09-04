import { PaginationProps } from 'ant-design-vue';

/**
 * Permission keys for script operations
 */
export type PermissionKey = 'TEST' | 'VIEW' | 'MODIFY' | 'DELETE' | 'EXPORT' | 'COLON' | 'GRANT';

/**
 * Select option for dropdown components
 */
export type SelectOption = {
    id: string;
    name: string;
    showTitle: string;
    showName: string;
}

/**
 * Menu item for quick search options
 */
export type MenuItem = {
    key: string;
    name: string;
}

/**
 * Resource information for script statistics
 * Contains counts for different types of scripts and sources
 */
export type ResourceInfo = {
    /** Total number of scripts */
    totalScriptNum: string;
    /** Number of performance test scripts */
    perfScriptNum: string;
    /** Number of functional test scripts */
    functionalScriptNum: string;
    /** Number of stability test scripts */
    stabilityScriptNum: string;
    /** Number of customization test scripts */
    customizationScriptNum: string;
    /** Number of mock data scripts */
    mockDataScriptNum: string;
    /** Number of mock API scripts */
    mockApisScriptNum: string;
    /** Number of created sources */
    createdSourceNum: string;
    /** Number of imported sources */
    importedSourceNum: string;
    /** Number of API sources */
    apisSourceNum: string;
    /** Number of case sources */
    caseSourceNum: string;
    /** Number of scenario sources */
    scenarioSourceNum: string;
}

export type ScriptSearchProps = {
  projectId: string;
  userInfo: { id: string; };
  appInfo: { id: string; };
  notify: string;
}

/**
 * Script information with metadata
 */
export type ScriptInfo = {
    /** Unique identifier for the script */
    id: string;
    /** Script name */
    name: string;
    /** Optional link URL for the script name */
    nameLinkUrl?: string;
    /** Plugin identifier */
    plugin: string;
    /** Script type information */
    type?: {
        value: string;
        message: string;
    };
    /** Source information */
    source?: {
        value: string;
        message: string;
    };
    /** Source identifier */
    sourceId?: string;
    /** Source name */
    sourceName?: string;
    /** Optional link URL for source name */
    sourceNameLinkUrl?: string;
    /** Associated tags */
    tags?: string[];
    /** Creator name */
    createdByName?: string;
    /** Last modifier name */
    lastModifiedByName?: string;
    /** Creation date */
    createdDate: string;
    /** Last modification date */
    lastModifiedDate: string;
    /** Authorization flag */
    auth: boolean;
    /** Page number for pagination */
    pageNo?: number;
    /** Page size for pagination */
    pageSize?: number;
}

export type ScriptTableProps = {
  projectId: string;
  appId: string;
  userId: string;
  dataSource: ScriptInfo[];
  permissionsMap: { [key: string]: PermissionKey[] };
  pagination: PaginationProps;
  allowImportSamplesFlag: boolean;
  loaded: boolean;
  loading: boolean;
  resetSelectedIdsNotify: string;
}

/**
 * Chart data item for pie chart visualization
 */
export type ChartDataItem = {
    /** Data item name */
    name: string;
    /** Data item value */
    value: number;
}

/**
 * ECharts configuration options for pie chart
 */
export type PieChartOption = {
    /** Chart tooltip configuration */
    tooltip: {
        trigger: 'item';
        axisPointer: { type: 'shadow' };
        textStyle: {
            fontSize: number;
        };
    };
    /** Chart legend configuration */
    legend: {
        top: string;
        right: number;
        orient: 'vertical';
        itemHeight: number;
        itemWidth: number;
        itemGap: number;
        formatter: (name: string) => string;
    };
    /** Chart color palette */
    color: string[];
    /** Chart series configuration */
    series: Array<{
        name: string;
        type: 'pie';
        center: [string, string];
        radius: [string, string];
        avoidLabelOverlap: boolean;
        itemStyle: {
            borderRadius: number;
            borderColor: string;
            borderWidth: number;
        };
        label: {
            show: boolean;
            formatter: string;
        };
        labelLine: {
            show: boolean;
        };
        emphasis: {
            label: {
                show: boolean;
                fontSize: number;
                fontWeight: string;
            };
        };
        data: ChartDataItem[];
    }>;
}
