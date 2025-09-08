<script setup lang="ts">
import { ref, computed, defineAsyncComponent, onMounted, watch } from 'vue';
import { TESTER, EnumMessage } from '@xcan-angus/infra';
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
  showChartParam: false // Default to not showing chart parameter control component
});

// Add datepicker related data
const datePicker = ref<string[] | undefined>(undefined);
const dateRangeType = ref<DateRangeType>(props.dateType || DateRangeType.MONTH);

// Chart data
const lineChartData = ref<any>(null);
const pieChartData = ref<any[]>([]);

// Calculate grid layout style
const gridStyle = computed(() => {
  const cols = props.config.layout?.cols || 2;
  const gap = props.config.layout?.gap || 16;
  return {
    display: 'grid',
    gridTemplateColumns: `repeat(${cols}, 1fr)`,
    gap: `${gap}px`
  };
});

// Fetch statistical data
const fetchChartData = async () => {
  if (!props.config || !props.config.charts || !props.config.charts.length) {
    return;
  }

  // If showChartParam is false, do not set date filtering, query all data
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
    // Build filter conditions
    let filters = [...(props.searchParams || [])];

    // Only add date filter conditions when showChartParam is true
    if (props.showChartParam) {
      // If there are datepicker values, add date filter conditions
      if (datePicker.value && datePicker.value.length === 2) {
        const dateFilters = getDateFiltersAndUnit(datePicker.value[0], datePicker.value[1]);
        filters = [...filters, ...dateFilters.filters];
      } else {
        // Use default date filter conditions
        const defaultDateFilters = getDefaultDateFilters(dateRangeType.value);
        filters = [...filters, ...defaultDateFilters];
      }
    }

    // Get data for each chart one by one
    const promises: Promise<void>[] = [];

    // Process line chart data
    const lineChart = props.config.charts.find((chart: ChartConfig) => chart.type === ChartType.LINE);
    if (lineChart) {
      // Note: aggregates parameter is not applicable for line charts
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
          nextValue || DateRangeType.DAY // If nextValue is undefined, use default value
        );
      });
      promises.push(linePromise);
    }

    // Process pie chart data
    const pieCharts = props.config.charts.filter((chart: ChartConfig) => chart.type === ChartType.PIE);
    if (pieCharts.length > 0) {
      const piePromises = pieCharts.map((chart: ChartConfig) => {
        // Note: aggregates parameter is required for pie charts, configured by business in dashboardConfig
        // If aggregates is configured in dashboardConfig, use the configured value, otherwise use default value
        const aggregates = chart.aggregates || [
          {
            column: 'id',
            function: AggregateFunction.COUNT
          }
        ];

        // Process fields, support single or multiple fields
        const groupByColumns = Array.isArray(chart.field) ? chart.field : [chart.field];
        return fetchSummaryData(props.apiRouter, {
          name: props.resource,
          groupBy: GroupBy.STATUS,
          groupByColumns: groupByColumns,
          aggregates: aggregates,
          filters: filters
        }).then(data => {
          // Process pie chart data, support multiple fields
          const pieDataArray: any[] = [];

          // Iterate through data for each field
          groupByColumns.forEach((field, index) => {
            if (data && data[field]) {
              const fieldData = data[field];
              // Calculate total, use TOTAL_COUNT_id (if exists) or sum up all parts
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

                // Only create pie chart data when there is data
                let fieldEnumMessages: EnumMessage<string>[] = [];
                if (chart.enumKey) {
                  if (Array.isArray(chart.enumKey[0])) {
                    // enumKey is array of arrays
                    fieldEnumMessages = (chart.enumKey as EnumMessage<string>[][])[index] || [];
                  } else {
                    // enumKey is single array
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
            // If no data, do not add anything to pieDataArray
          });

          return pieDataArray;
        });
      });

      // Wait for all pie chart data to be fetched
      const pieResults = await Promise.all(piePromises);
      // Flatten array because each pie chart may return multiple data items (when there are multiple fields)
      pieChartData.value = pieResults.flat();
    }

    // Wait for all data to be fetched
    await Promise.all(promises);
  } catch (error) {
    console.error('Failed to fetch chart data:', error);
  }
};

// Add methods to handle ChartParam events
const selectDate = (type: DateRangeType) => {
  dateRangeType.value = type;
};

const dateChange = (dates: string[] | undefined) => {
  datePicker.value = dates;
  // When user selects custom date range, we need to build corresponding filter conditions
  // This will automatically trigger fetchChartData through watch listener
};

// Modify watch listener to include datepicker changes
watch(
  () => {
    const baseDeps = [props.resource, props.dateType, props.searchParams];
    // Only listen to date-related changes when showChartParam is true
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

// Fetch data when component is mounted
onMounted(() => {
  fetchChartData();
});
</script>

<template>
  <div class="statistics-container">
    <!-- Configurable ChartParam component -->
    <ChartParam
      v-if="props.showChartParam"
      :datePicker="datePicker"
      :resource="props.resource"
      :dateType="dateRangeType"
      @selectDate="selectDate"
      @dateChange="dateChange" />

    <!-- Chart grid container -->
    <div class="statistics-dashboard" :style="gridStyle">
      <!-- Render line charts -->
      <div
        v-for="chart in config.charts"
        :key="JSON.stringify(chart.field)"
        class="chart-item">
        <LineChart
          v-if="chart.type === ChartType.LINE && lineChartData"
          :chartData="lineChartData" />
        <template v-else-if="chart.type === ChartType.PIE">
          <!-- If it's a pie chart, there may be multiple fields requiring multiple pie charts -->
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
