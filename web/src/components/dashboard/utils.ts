import { http } from '@xcan-angus/infra';
import { SummaryQueryParams, SummaryData, LineChartData, PieChartData } from './types';
import { GroupBy, DateRangeType } from './enums';
import dayjs from 'dayjs';

/**
 * 统计数据查询工具方法
 * @param apiRouter API路由前缀
 * @param params 查询参数
 * @returns Promise<SummaryData>
 */
export const fetchSummaryData = async (
  apiRouter: string,
  params: SummaryQueryParams
): Promise<SummaryData> => {
  // 构造参数对象
  const requestParams: any = {
    name: params.name,
    groupBy: params.groupBy
  };

  // 添加分组列参数，支持多个字段
  if (params.groupByColumns && params.groupByColumns.length > 0) {
    params.groupByColumns.forEach((column, index) => {
      requestParams[`groupByColumns[${index}]`] = column;
    });
  }

  // 只有在groupBy为DATE时才添加dateRangeType参数
  if (params.groupBy === GroupBy.DATE && params.dateRangeType) {
    requestParams.dateRangeType = params.dateRangeType;
  }

  // 添加聚合参数
  if (params.aggregates) {
    params.aggregates.forEach((aggregate, index) => {
      requestParams[`aggregates[${index}].column`] = aggregate.column;
      requestParams[`aggregates[${index}].function`] = aggregate.function;
    });
  }

  // 添加过滤参数
  if (params.filters) {
    params.filters.forEach((filter, index) => {
      requestParams[`filters[${index}].key`] = filter.key;
      requestParams[`filters[${index}].op`] = filter.op;
      requestParams[`filters[${index}].value`] = filter.value;
    });
  }

  // 添加其他可选参数
  if (params.closeMultiTenantCtrl !== undefined) {
    requestParams.closeMultiTenantCtrl = params.closeMultiTenantCtrl;
  }
  if (params.projectId !== undefined) {
    requestParams.projectId = params.projectId;
  }

  const [error, { data }] = await http.get(
    `${apiRouter}/analysis/customization/summary`,
    requestParams
  );

  if (error) {
    throw new Error('Failed to fetch summary data');
  }

  return data;
};

/**
 * 批量统计数据查询工具方法
 * @param apiRouter API路由前缀
 * @param params 查询参数数组
 * @returns Promise<Record<string, SummaryData>>
 */
export const fetchBatchSummaryData = async (
  apiRouter: string,
  params: SummaryQueryParams[]
): Promise<Record<string, SummaryData>> => {
  const [error, { data }] = await http.get(
    `${apiRouter}/analysis/customization/summary/batch`,
    { dto: params }
  );

  if (error) {
    throw new Error('Failed to fetch batch summary data');
  }

  return data;
};

/**
 * 将统计数据转换为折线图数据
 * @param data 统计数据
 * @param title 图表标题
 * @param unit 单位
 * @returns LineChartData
 */
export const convertToLineChartData = (
  data: Record<string, any>,
  title: string,
  unit: string
): LineChartData => {
  if (!data) {
    return {
      title,
      unit,
      xData: [],
      yData: []
    };
  }

  const keys = Object.keys(data);
  if (!keys.length) {
    return {
      title,
      unit,
      xData: [],
      yData: []
    };
  }

  return {
    title,
    unit,
    xData: keys,
    yData: Object.values(data).map(item => (item.COUNT_id ? +item.COUNT_id : 0))
  };
};

/**
 * 将统计数据转换为饼图数据
 * @param group 分组配置
 * @param data 统计数据
 * @returns PieChartData[]
 */
export const convertToPieChartData = (
  group: Array<{
    key: string;
    value: string;
    type: { value: string | number; message: string }[];
    color: string[];
  }>,
  data: Record<string, any>
): PieChartData[] => {
  const dataSource: PieChartData[] = [];

  for (let i = 0; i < group.length; i++) {
    const column = group[i];
    const res = data[column.key];

    if (!res) {
      const _dataSource: PieChartData = {
        key: column.key,
        title: column.value,
        total: 0,
        color: column.color,
        legend: column.type,
        data: column.type.map(m => ({ name: m.message, value: 0 }))
      };
      dataSource.push(_dataSource);
      continue;
    }

    const arr = Object.entries(res);
    if (!arr.length) {
      const _dataSource: PieChartData = {
        key: column.key,
        title: column.value,
        total: 0,
        color: column.color,
        legend: column.type,
        data: column.type.map(m => ({ name: m.message, value: 0 }))
      };
      dataSource.push(_dataSource);
      continue;
    }

    // 判断每一组下是否是空对象
    const _group = Object.keys(res);
    if (!_group.length) {
      continue;
    }

    // 枚举类型数据处理
    if (['target_type'].includes(column.key)) {
      const _data: { name: string; value: number; codes?: number }[] = [];
      let _total = 0;

      for (let j = 0; j < column.type.length; j++) {
        const _key = column.type[j].value;
        if (res[_key]) {
          const _item: { name: string; value: number; codes?: number } = {
            name: column.type[j]?.message,
            value: +res[_key]?.COUNT_id
          };
          _data.push(_item);
          _total = +res[_key]?.TOTAL_COUNT_id;
        } else {
          _data.push({ name: column.type[j]?.message, value: 0 });
        }
      }

      const _dataSource: PieChartData = {
        key: column.key,
        title: column.value,
        total: _total,
        color: column.color,
        legend: column.type,
        data: _data.length ? _data : []
      };
      dataSource.push(_dataSource);
    }

    // 布尔类型数据处理
    if (['sys_admin_flag'].includes(column.key)) {
      const _data: { name: string; value: number }[] = [];

      for (let j = 0; j < column.type.length; j++) {
        const _key = column.type[j].value;
        if (res[_key]) {
          _data.push({ name: column.type[j]?.message, value: +res[_key]?.COUNT_id });
        } else {
          _data.push({ name: column.type[j]?.message, value: 0 });
        }
      }

      const _dataSource: PieChartData = {
        key: column.key,
        title: column.value,
        total: res[0]?.TOTAL_COUNT_id ? +res[0].TOTAL_COUNT_id : +res[1]?.TOTAL_COUNT_id,
        color: column.color,
        legend: column.type,
        data: _data || []
      };
      dataSource.push(_dataSource);
    }
  }

  return dataSource;
};

/**
 * 根据日期范围类型生成默认日期过滤条件
 * @param dateType 日期类型
 * @returns 默认日期过滤条件
 */
export const getDefaultDateFilters = (dateType: DateRangeType) => {
  const now = dayjs();
  switch (dateType) {
    case DateRangeType.DAY:
      return [
        {
          key: 'opt_date',
          op: 'GREATER_THAN_EQUAL',
          value: `"${now.startOf('date').format('YYYY-MM-DD HH:mm:ss')}"`
        },
        {
          key: 'opt_date',
          op: 'LESS_THAN_EQUAL',
          value: `"${now.endOf('date').format('YYYY-MM-DD HH:mm:ss')}"`
        }
      ];
    case DateRangeType.WEEK:
      return [
        {
          key: 'opt_date',
          op: 'GREATER_THAN_EQUAL',
          value: `"${now.startOf('date').subtract(1, 'week').add(1, 'day').format('YYYY-MM-DD HH:mm:ss')}"`
        },
        {
          key: 'opt_date',
          op: 'LESS_THAN_EQUAL',
          value: `"${now.format('YYYY-MM-DD HH:mm:ss')}"`
        }
      ];
    case DateRangeType.MONTH:
      return [
        {
          key: 'opt_date',
          op: 'GREATER_THAN_EQUAL',
          value: `"${now.startOf('date').subtract(1, 'month').add(1, 'day').format('YYYY-MM-DD HH:mm:ss')}"`
        },
        {
          key: 'opt_date',
          op: 'LESS_THAN_EQUAL',
          value: `"${now.format('YYYY-MM-DD HH:mm:ss')}"`
        }
      ];
    case DateRangeType.YEAR:
      return [
        {
          key: 'opt_date',
          op: 'GREATER_THAN_EQUAL',
          value: `"${now.startOf('date').subtract(1, 'year').add(1, 'day').format('YYYY-MM-DD HH:mm:ss')}"`
        },
        {
          key: 'opt_date',
          op: 'LESS_THAN_EQUAL',
          value: `"${now.format('YYYY-MM-DD HH:mm:ss')}"`
        }
      ];
    default:
      return [
        {
          key: 'opt_date',
          op: 'GREATER_THAN_EQUAL',
          value: `"${now.subtract(1, 'month').format('YYYY-MM-DD HH:mm:ss')}"`
        },
        {
          key: 'opt_date',
          op: 'LESS_THAN_EQUAL',
          value: `"${now.format('YYYY-MM-DD HH:mm:ss')}"`
        }
      ];
  }
};

/**
 * 根据自定义日期范围生成过滤条件
 * @param startDate 开始日期
 * @param endDate 结束日期
 * @returns 日期过滤条件和单位
 */
export const getDateFiltersAndUnit = (startDate: string, endDate: string) => {
  const start = dayjs(startDate);
  const end = dayjs(endDate);
  const diffDays = end.diff(start, 'day');

  let unit: string[] = [];

  if (diffDays <= 1) {
    unit = ['HOUR', '小时'];
  } else if (diffDays > 1 && diffDays <= 30) {
    unit = ['DAY', '天'];
  } else if (diffDays > 30 && diffDays <= 120) {
    unit = ['WEEK', '周'];
  } else if (diffDays > 120 && diffDays <= 730) {
    unit = ['MONTH', '月'];
  } else {
    unit = ['YEAR', '年'];
  }

  return {
    filters: [
      { key: 'opt_date', op: 'GREATER_THAN_EQUAL', value: `"${startDate}"` },
      { key: 'opt_date', op: 'LESS_THAN_EQUAL', value: `"${endDate}"` }
    ],
    unit
  };
};

/**
 * 构造过滤条件的工具方法
 * @param filters 过滤条件数组
 * @returns 构造好的过滤条件
 */
export const buildFilters = (filters: Array<{key: string; op: string; value: string}> = []) => {
  return filters.map((filter, index) => ({
    [`filters[${index}].key`]: filter.key,
    [`filters[${index}].op`]: filter.op,
    [`filters[${index}].value`]: filter.value
  })).reduce((acc, curr) => ({ ...acc, ...curr }), {});
};
