import { EnumMessage } from '@xcan-angus/infra';
import { GroupBy, AggregateFunction, DateRangeType, ChartType } from './enums';

// 统计查询参数类型
export interface SummaryQueryParams {
  name: string; // 资源名称
  groupBy?: GroupBy; // 分组方式
  groupByColumns?: string[]; // 分组列
  dateRangeType?: DateRangeType; // 日期范围类型
  aggregates?: Array<{
    column: string;
    function: AggregateFunction;
  }>;
  filters?: Array<{
    key: string;
    op: string;
    value: string;
  }>;
  closeMultiTenantCtrl?: boolean; // 是否关闭多租户控制
  projectId?: number; // 项目ID
}

// 统计返回数据类型
export interface SummaryData {
  [key: string]: any;
}

// 折线图数据类型
export interface LineChartData {
  title: string;
  unit: DateRangeType;
  total?: number;
  xData: string[];
  yData: (number | null)[];
}

// 饼图数据类型
export interface PieChartData {
  key: string;
  title: string;
  total: number;
  color: string[];
  legend: { value: string | number; message: string }[];
  data: { name: string; value: number; codes?: number }[];
}

// 图表配置类型
export interface ChartConfig {
  type: ChartType;
  title: string;
  field: string | string[]; // 统计字段，支持单个或多个字段
  enumKey: EnumMessage<string>[] | EnumMessage<string>[][];
  // 饼图特有配置
  pieConfig?: {
    color: string[];
    legend: { value: string | number; message: string }[];
  };
  // 折线图特有配置
  lineConfig?: {
    unit: string;
  };
  // 聚合配置
  aggregates?: Array<{
    column: string;
    function: AggregateFunction;
  }>;
}
