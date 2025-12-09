import { ScriptType, EnumMessage } from '@xcan-angus/infra';
import { TestResultStatus, TaskType } from '@/enums/enums';

// Scenario detail component types
export interface ScenarioDetailProps {
  id: string;
  _id: string;
  appInfo: { [key: string]: any };
  userInfo: { [key: string]: any };
  projectId: string;
  data: {
    scenarioId: string;
  };
}

export interface ScenarioData {
  id: string;
  name: string;
  plugin: string;
  auth: boolean;
  follow: boolean;
  favourite: boolean;
}

export interface ScenarioResult {
  enabledTestTypes: ScriptType;
  passed: boolean;
  resultSummary: {
    resultStatus: EnumMessage<TestResultStatus>;
    testFailureNum: string;
    testNum: string;
    testSuccessRate: string;
    testSuccessNum: string;
  };
  resultDetailVoMap: {
    [key: string]: { [key: string]: any };
  };
}

export interface TestSummaryProps {
  appInfo: { [key: string]: any };
  userInfo: { [key: string]: any };
  projectId: string;
  dataSource: ScenarioResult;
  scriptType: ScriptType;
}

export interface TaskProps {
  scenarioId: string;
  projectId: string;
}

export interface TaskItem {
  id: string;
  code: string;
  name: string;
  sprintName: string;
  priority: any;
  assigneeName: string;
  confirmerName: string;
  deadlineDate: string;
  taskType: EnumMessage<TaskType>;
  linkUrl: string;
  overdue: boolean;
  status: any;
  scriptType: EnumMessage<ScriptType>;
}

export interface PaginationConfig {
  current: number;
  pageSize: number;
  total: number;
}

export interface TableColumn {
  key: string;
  title: string;
  dataIndex: string;
  ellipsis?: boolean;
  sorter?: boolean;
  width?: string | number;
  actionKey?: 'createdBy' | 'favouriteBy' | 'followBy';
}

export interface TestData {
  passed: boolean;
  failureMessage?: string;
  sampleSummary?: {
    tps: string;
    tranP90: string;
    errorRate: string;
  };
  indicatorPerf?: {
    tps: string;
    art: string;
    errorRate: string;
  };
  indicatorStability?: {
    tps: string;
    art: string;
    errorRate: string;
  };
  targetSummary?: {
    totalNum: string;
    successNum: string;
    failNum: string;
    disabledNum: string;
  };
}

export interface ChartConfig {
  title: {
    text: string;
    left: string;
    bottom: string;
    padding: number;
    subtext: string;
    itemGap: number;
    textAlign: string;
    textStyle: {
      fontSize: number;
      fontWeight: string;
    };
    subtextStyle: {
      fontSize: number;
      color: string;
    };
  };
  tooltip: {
    trigger: string;
  };
  legend: {
    top: string;
    right: string;
    orient: string;
    itemHeight: number;
    itemWidth: number;
    itemGap: number;
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
    };
    itemStyle: {
      borderRadius: number;
      borderColor: string;
      borderWidth: number;
    };
    emphasis: {
      label: {
        show: boolean;
      };
    };
    labelLine: {
      show: boolean;
      length: number;
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

export interface ConfigInfoItem {
  label: string;
  dataIndex: string;
  bgColor: string;
}
