<script setup lang="ts">
import { ref, computed, defineAsyncComponent, onMounted, watch } from 'vue';
import { TESTER, enumUtils, EnumMessage } from '@xcan-angus/infra';
import { fetchSummaryData, convertToLineChartData, getDefaultDateFilters, getDateFiltersAndUnit } from './utils';
import { GroupBy, AggregateFunction, DateRangeType, ChartType } from './enums';
import type { ChartConfig } from './types';

const LineChart = defineAsyncComponent(() => import('@/components/dashboard/chart/LineChart.vue'));
const PieChart = defineAsyncComponent(() => import('@/components/dashboard/chart/PieChart.vue'));
const ChartParam = defineAsyncComponent(() => import('@/components/dashboard/chart/ChartParam.vue'));

interface Props {
  config: any;
  apiRouter?: string;
  resource: string;
  barTitle: string;
  dateType?: DateRangeType;
  userId?: string;
  searchParams?: {
    key: string;
    op: string;
    value: string;
  }[];
  showChartParam?: boolean; // 是否显示图表参数控制组件
}
const props = withDefaults(defineProps<Props>(), {
  apiRouter: TESTER,
  dateType: DateRangeType.MONTH,
  userId: '',
  searchParams: () => [],
  showChartParam: false // 默认不显示图表参数控制组件
});

// 添加datepicker相关数据
const datePicker = ref<string[] | undefined>(undefined);
const dateRangeType = ref<DateRangeType>(props.dateType || DateRangeType.MONTH);

// 图表数据
const lineChartData = ref<any>(null);
const pieChartData = ref<any[]>([]);

// 计算网格布局样式
const gridStyle = computed(() => {
  const cols = props.config.layout?.cols || 2;
  const gap = props.config.layout?.gap || 16;
  return {
    display: 'grid',
    gridTemplateColumns: `repeat(${cols}, 1fr)`,
    gap: `${gap}px`
  };
});

// 获取统计数据
const fetchChartData = async () => {
  if (!props.config || !props.config.charts || !props.config.charts.length) {
    return;
  }

  // 如果showChartParam为false，则不设置日期过滤，查询所有数据
  let nextValue: DateRangeType | undefined;
  if (props.showChartParam) {
    switch (dateRangeType.value) {
      case DateRangeType.YEAR:
        nextValue = DateRangeType.MONTH;
        break;
      case DateRangeType.MONTH:
        nextValue = DateRangeType.DAY;
        break;
      case DateRangeType.WEEK:
        nextValue = DateRangeType.DAY;
        break;
      case DateRangeType.DAY:
        nextValue = DateRangeType.HOUR;
        break;
      default:
        nextValue = DateRangeType.DAY;
    }
  } else {
    nextValue = DateRangeType.MONTH;
  }

  try {
    // 构建过滤条件
    let filters = [...(props.searchParams || [])];

    // 只有当showChartParam为true时才添加日期过滤条件
    if (props.showChartParam) {
      // 如果有datepicker值，则添加日期过滤条件
      if (datePicker.value && datePicker.value.length === 2) {
        const dateFilters = getDateFiltersAndUnit(datePicker.value[0], datePicker.value[1]);
        filters = [...filters, ...dateFilters.filters];
      } else {
        // 使用默认日期过滤条件
        const defaultDateFilters = getDefaultDateFilters(dateRangeType.value);
        filters = [...filters, ...defaultDateFilters];
      }
    }

    // 逐个获取每个图表的数据
    const promises: Promise<void>[] = [];

    // 处理折线图数据
    const lineChart = props.config.charts.find((chart: ChartConfig) => chart.type === ChartType.LINE);
    if (lineChart) {
      // 注意：折线图时不适用aggregates参数
      const linePromise = fetchSummaryData(props.apiRouter, {
        name: props.resource,
        groupBy: GroupBy.DATE,
        groupByColumns: Array.isArray(lineChart.field) ? lineChart.field : [lineChart.field],
        dateRangeType: nextValue,
        filters: filters
      }).then(data => {
        lineChartData.value = convertToLineChartData(
          data,
          lineChart.title,
          nextValue || DateRangeType.DAY // 如果nextValue为undefined，使用默认值
        );
      });
      promises.push(linePromise);
    }

    // 处理饼图数据
    const pieCharts = props.config.charts.filter((chart: ChartConfig) => chart.type === ChartType.PIE);
    if (pieCharts.length > 0) {
      const piePromises = pieCharts.map((chart: ChartConfig) => {
        // 注意：饼图时需要使用aggregates参数，由业务在dashboardConfig中配置
        // 如果dashboardConfig中配置了aggregates，则使用配置的值，否则使用默认值
        const aggregates = chart.aggregates || [
          {
            column: 'id',
            function: AggregateFunction.COUNT
          }
        ];

        // 处理字段，支持单个或多个字段
        const groupByColumns = Array.isArray(chart.field) ? chart.field : [chart.field];
        return fetchSummaryData(props.apiRouter, {
          name: props.resource,
          groupBy: GroupBy.STATUS,
          groupByColumns: groupByColumns,
          aggregates: aggregates,
          filters: filters
        }).then(data => {
          // 处理饼图数据，支持多个字段
          const pieDataArray: any[] = [];

          // 遍历每个字段的数据
          groupByColumns.forEach((field, index) => {
            if (data && data[field]) {
              const fieldData = data[field];
              // 计算总数，使用TOTAL_COUNT_id（如果存在）或者通过累加各部分得到
              let total = 0;
              const fieldDataValues = Object.values(fieldData);
              if (fieldDataValues.length > 0) {
                const firstItem: any = fieldDataValues[0];
                if (firstItem.TOTAL_COUNT_id) {
                  total = parseFloat(firstItem.TOTAL_COUNT_id);
                } else {
                  total = fieldDataValues.reduce((sum: number, item: any) => {
                    return sum + (item.COUNT_id ? parseFloat(item.COUNT_id) : 0);
                  }, 0);
                }

                // 只有当有数据时才创建饼图数据
                let fieldEnumMessages: EnumMessage<string>[] = [];
                if (chart.enumKey) {
                  if (Array.isArray(chart.enumKey[0])) {
                    // enumKey是数组的数组
                    fieldEnumMessages = (chart.enumKey as EnumMessage<string>[][])[index] || [];
                  } else {
                    // enumKey是单个数组
                    fieldEnumMessages = chart.enumKey as EnumMessage<string>[];
                  }
                }
                if (fieldDataValues.length > 0) {
                  const pieData = {
                    key: field,
                    title: chart.title + (groupByColumns.length > 1 ? ` - ${field}` : ''),
                    total: total,
                    color: chart.pieConfig?.color || [
                      '#67D7FF', '#FFB925', '#F5222D', '#2acab8', '#2D8EFF', '#52C41A',
                      '#FF6B6B', '#4ECDC4', '#45B7D1', '#96CEB4', '#FFEAA7', '#DDA0DD',
                      '#98D8C8', '#F7DC6F', '#BB8FCE', '#85C1E9', '#F8C471', '#82E0AA',
                      '#F1948A', '#85C1E9', '#D7BDE2', '#A9CCE3', '#F9E79F', '#D5A6BD'
                    ],
                    legend: chart.pieConfig?.legend || fieldEnumMessages.map(message => ({
                      value: message.value,
                      message: message.message
                    })),
                    data: fieldEnumMessages.map(message => ({
                      name: message.message,
                      value: fieldData[message.value]?.COUNT_id ? parseFloat(fieldData[message.value].COUNT_id) : 0
                    }))
                  };

                  pieDataArray.push(pieData);
                }
              }
            }
            // 如果没有数据，不添加任何内容到pieDataArray
          });

          return pieDataArray;
        });
      });

      // 等待所有饼图数据获取完成
      const pieResults = await Promise.all(piePromises);
      // 展平数组，因为每个饼图可能返回多个数据项（当有多个字段时）
      pieChartData.value = pieResults.flat();
    }

    // 等待所有数据获取完成
    await Promise.all(promises);
  } catch (error) {
    console.error('Failed to fetch chart data:', error);
  }
};

// 添加处理ChartParam事件的方法
const selectDate = (type: DateRangeType) => {
  dateRangeType.value = type;
};

const dateChange = (dates: string[] | undefined) => {
  datePicker.value = dates;
  // 当用户选择自定义日期范围时，我们需要构建相应的过滤条件
  // 这会通过watch监听器自动触发fetchChartData
};

// 修改watch监听器以包含datepicker的变化
watch(
  () => {
    const baseDeps = [props.resource, props.dateType, props.searchParams];
    // 只有当showChartParam为true时才监听日期相关的变化
    if (props.showChartParam) {
      return [...baseDeps, datePicker.value, dateRangeType.value];
    }
    return baseDeps;
  },
  () => {
    fetchChartData();
  },
  { deep: true }
);

// 组件挂载时获取数据
onMounted(() => {
  fetchChartData();
});
</script>

<template>
  <div class="statistics-container">
    <!-- 可配置的ChartParam组件 -->
    <ChartParam
      v-if="props.showChartParam"
      :datePicker="datePicker"
      :resource="props.resource"
      :dateType="dateRangeType"
      @selectDate="selectDate"
      @dateChange="dateChange" />

    <!-- 图表网格容器 -->
    <div class="statistics-dashboard" :style="gridStyle">
      <!-- 渲染折线图 -->
      <div
        v-for="chart in config.charts"
        :key="JSON.stringify(chart.field)"
        class="chart-item">
        <LineChart
          v-if="chart.type === ChartType.LINE && lineChartData"
          :chartData="lineChartData" />
        <template v-else-if="chart.type === ChartType.PIE">
          <!-- 如果是饼图，可能有多个字段需要渲染多个饼图 -->
          <PieChart
            v-for="pieData in pieChartData.filter(item =>
              Array.isArray(chart.field)
                ? chart.field.includes(item.key)
                : item.key === chart.field
            )"
            :key="pieData.key"
            :chartData="pieData" />
        </template>
      </div>
    </div>
  </div>
</template>

<style scoped>
.statistics-container {
  width: 100%;
}

.chart-item {
  min-height: 200px;
}
</style>
