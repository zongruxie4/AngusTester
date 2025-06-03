<script setup lang="ts">
import { defineAsyncComponent, onMounted, ref, watch } from 'vue';
import { BarData, DateType, PieData, PieSetting } from './PropsType';
import dayjs from 'dayjs';
import { http } from '@xcan-angus/tools';
import { useI18n } from 'vue-i18n';

interface Props {
  geteway:string,
  resource:string,
  pieParmas:PieSetting[];
  barTitle:string;
  dateType:DateType;
  hasPieChart:boolean;
  visible?:boolean;
  userId?:string;
  searchParams?:{
    key: string,
    op: string,
    value: string
  }[]
}
const props = withDefaults(defineProps<Props>(), {
  resource: '',
  pieParmas: () => [],
  barTitle: '',
  dateType: 'MONTH',
  hasPieChart: false,
  visible: true,
  userId: '',
  searchParams: () => []
});

const BarChartParam = defineAsyncComponent(() => import('./barChartParam.vue'));
const PieChart = defineAsyncComponent(() => import('./pieChart.vue'));
const LineChart = defineAsyncComponent(() => import('./lineChart.vue'));

const { t } = useI18n();

const datePicker = ref([]);
const dateChange = (value) => {
  let unit:string[] = [];
  let filters:{key:string, op:string, value:string}[] = [];
  if (!value?.length) {
    // 其他清空 默认展示近一月
    filters = [{ key: 'created_date', op: 'GREATER_THAN_EQUAL', value: `"${dayjs().startOf('date').subtract(1, 'week').add(1, 'day').format('YYYY-MM-DD HH:mm:ss')}"` },
      { key: 'created_date', op: 'LESS_THAN_EQUAL', value: `"${dayjs().format('YYYY-MM-DD HH:mm:ss')}"` }];
    unit = ['DAY', t('day')];
  }

  filters = [{ key: 'created_date', op: 'GREATER_THAN_EQUAL', value: `"${value[0]}"` },
    { key: 'created_date', op: 'LESS_THAN_EQUAL', value: `"${value[1]}"` }];
  const startDate = dayjs(value[0]);
  const endDate = dayjs(value[1]);
  const often = endDate.diff(startDate, 'day');
  if (often <= 1) {
    unit = ['HOUR', t('hour')];
  } else if (often > 1 && often <= 30) {
    unit = ['DAY', t('day')];
  } else if (often > 30 && often <= 120) {
    unit = ['WEEK', t('week')];
  } else if (often > 120 && often <= 730) {
    unit = ['MONTH', t('month')];
  } else {
    unit = ['YEAR', t('year')];
  }

  dateFilters.value = filters;
  dateRangeType.value = unit;
  loadCount();
  loadDateCount();
};

const selectDate = (value) => {
  datePicker.value = [];
  let filters:{key:string, op:string, value:string}[] = [];
  let unit:string[] = [];
  switch (value) {
    case 'DAY':
      filters = [{ key: 'created_date', op: 'GREATER_THAN_EQUAL', value: `"${dayjs().startOf('date').format('YYYY-MM-DD HH:mm:ss')}"` },
        { key: 'created_date', op: 'LESS_THAN_EQUAL', value: `"${dayjs().endOf('date').format('YYYY-MM-DD HH:mm:ss')}"` }];
      unit = ['HOUR', t('hour')];
      break;
    case 'WEEK':
      filters = [{ key: 'created_date', op: 'GREATER_THAN_EQUAL', value: `"${dayjs().startOf('date').subtract(1, 'week').add(1, 'day').format('YYYY-MM-DD HH:mm:ss')}"` },
        { key: 'created_date', op: 'LESS_THAN_EQUAL', value: `"${dayjs().format('YYYY-MM-DD HH:mm:ss')}"` }];
      unit = ['DAY', t('day')];
      break;
    case 'MONTH':
      filters = [{ key: 'created_date', op: 'GREATER_THAN_EQUAL', value: `"${dayjs().startOf('date').subtract(1, 'month').add(1, 'day').format('YYYY-MM-DD HH:mm:ss')}"` },
        { key: 'created_date', op: 'LESS_THAN_EQUAL', value: `"${dayjs().format('YYYY-MM-DD HH:mm:ss')}"` }];
      unit = ['DAY', t('day')];
      break;
    case 'YEAR':
      filters = [{ key: 'created_date', op: 'GREATER_THAN_EQUAL', value: `"${dayjs().startOf('date').subtract(1, 'year').add(1, 'day').format('YYYY-MM-DD HH:mm:ss')}"` },
        { key: 'created_date', op: 'LESS_THAN_EQUAL', value: `"${dayjs().format('YYYY-MM-DD HH:mm:ss')}"` }];
      unit = ['MONTH', t('month')];
      break;
  }

  dateFilters.value = filters;
  dateRangeType.value = unit;
  loadCount();
  loadDateCount();
};

const targetTypeFilter = ref<{
    key: string,
    op: string,
    value: string
  }[]>([]);
const userFilter = ref<{
    key: string,
    op: string,
    value: string
  }[]>([]);

const publicParams = {
  'aggregates[0].column': 'id',
  'aggregates[0].function': 'COUNT',
  groupBy: 'STATUS',
  name: props.resource
};
const pieLoading = ref(true);
const pieChartData = ref<PieData []>([]);
const loadCount = async () => {
  const params = {
    ...publicParams,
    groupByColumns: props.pieParmas.map(item => item.key),
    filters: [...dateFilters.value, ...targetTypeFilter.value, ...userFilter.value],
    dateRangeType: dateRangeType.value[0]
  };

  params.filters.forEach(item => {
    if (item.key === 'created_date') {
      item.key = 'opt_date';
    }
  });

  const [error, { data }] = await http.get(`${props.geteway}/analysis/customization/summary`, params);
  pieLoading.value = false;
  if (error) {
    return;
  }
  pieChartData.value = getCountData(props.pieParmas, data);
};

const getCountData = (group, data) => {
  const dataSource:PieData[] = [];
  for (let i = 0; i < group.length; i++) {
    const cloum = group[i];
    const res = data[cloum.key];
    if (!res) {
      const _dataSource = {
        key: cloum.key,
        title: cloum.value,
        total: 0,
        color: cloum.color,
        legend: cloum.type,
        data: cloum.type.map(m => ({ name: m.message, value: 0 }))
      };
      dataSource.push(_dataSource);
      continue;
    }
    const arr = Object.entries(res);
    if (!arr.length) {
      const _dataSource = {
        key: cloum.key,
        title: cloum.value,
        total: 0,
        color: cloum.color,
        legend: cloum.type,
        data: cloum.type.map(m => ({ name: m.message, value: 0 }))
      };
      dataSource.push(_dataSource);
      continue;
    }

    // 判断每一组下是否是空对象,每一组对象里后台不会反回只有key的情况，有key肯定有值
    const _group = Object.keys(res);
    if (!_group.length) {
      continue;
    }

    // 所有来源只有枚举类型数据
    if (['target_type'].includes(cloum.key)) {
      setEnumDatasource(cloum, res, dataSource);
    }

    // 所有来源只有boolean 类型数据
    if (['sys_admin_flag'].includes(cloum.key)) {
      setBooleanDatasource(cloum, res, dataSource);
    }
  }
  return dataSource;
};

// 设置布尔型数据
const setBooleanDatasource = (cloum, res, dataSource) => {
  const _data:{name:string, value:number | null}[] = [];
  for (let j = 0; j < cloum.type.length; j++) {
    const _key = cloum.type[j].value;
    if (res[_key]) {
      _data.push({ name: cloum.type[j]?.message, value: +res[_key]?.COUNT_id });
    } else {
      if (props.resource !== 'Dept') {
        _data.push({ name: cloum.type[j]?.message, value: null });
      }
    }
  }
  const _dataSource = {
    key: cloum.key,
    title: cloum.value,
    total: res[0]?.TOTAL_COUNT_id ? +res[0].TOTAL_COUNT_id : +res[1]?.TOTAL_COUNT_id,
    color: cloum.color,
    legend: cloum.type,
    data: _data || []
  };
  dataSource.push(_dataSource);
};
// 设置枚举型数据
const setEnumDatasource = (cloum, res, dataSource) => {
  const _data:{name:string, value:number | null, codes?:number | null}[] = [];
  let _total = 0;

  for (let j = 0; j < cloum.type.length; j++) {
    const _key = cloum.type[j].value;
    if (res[_key]) {
      const _item:{name:string, value:number | null, codes?:number | null} = { name: cloum.type[j]?.message, value: +res[_key]?.COUNT_id };
      _data.push(_item);
      _total = +res[_key]?.TOTAL_COUNT_id;
    } else {
      _data.push({ name: cloum.type[j]?.message, value: null });
    }
  }
  const _dataSource = {
    key: cloum.key,
    title: cloum.value,
    total: _total,
    color: cloum.color,
    legend: cloum.type,
    data: _data.length ? _data : []
  };
  dataSource.push(_dataSource);
};

const barChartData = ref<BarData>({} as BarData);
const getDefaultDateRangeType = () => {
  return ['DAY', t('day')];
};

const dateRangeType = ref(getDefaultDateRangeType());

const getDefaultDateFilters = () => {
  return [
    {
      key: 'created_date',
      op: 'GREATER_THAN_EQUAL',
      value: `"${dayjs().subtract(1, 'month').format('YYYY-MM-DD HH:mm:ss')}"`
    },
    {
      key: 'created_date',
      op: 'LESS_THAN_EQUAL',
      value: `"${dayjs().format('YYYY-MM-DD HH:mm:ss')}"`
    }
  ];
};

const dateFilters = ref(getDefaultDateFilters());
const barLoading = ref(true);

const loadDateCount = async () => {
  const params = {
    groupByColumns: 'opt_date',
    groupBy: 'DATE',
    name: props.resource,
    dateRangeType: dateRangeType.value[0],
    filters: [...dateFilters.value, ...targetTypeFilter.value, ...userFilter.value]
  };
  params.filters.forEach(item => {
    if (item.key === 'created_date') {
      item.key = 'opt_date';
    }
  });
  const [error, { data }] = await http.get(`${props.geteway}/analysis/customization/summary`, params);
  barLoading.value = false;
  if (error) {
    return;
  }
  setBarCharCount(data);
};

const setBarChartDefault = () => {
  barChartData.value.xData = [];
  barChartData.value.yData = [];
  const startDate = dayjs(dateFilters.value[0].value.replace(/^"(.*)"$/, '$1'));
  const endDate = dayjs(dateFilters.value[1].value.replace(/^"(.*)"$/, '$1'));
  const often = endDate.diff(startDate, 'hour');
  if (often <= 24) {
    for (let i = startDate.hour(); i <= endDate.hour(); i++) {
      barChartData.value.xData.push(i + '');
      barChartData.value.yData.push(null);
    }
  } else if (often > 24 && often <= 120 * 24) {
    for (let date = startDate; date.isBefore(endDate) || date.isSame(endDate); date = date.add(1, 'day')) {
      barChartData.value.xData.push(date.format('YYYY-MM-DD'));
      barChartData.value.yData.push(null);
    }
  } else if (often > 120 * 24 && often <= 730 * 24) {
    for (let date = startDate; date.isBefore(endDate) || date.isSame(endDate); date = date.add(1, 'month')) {
      barChartData.value.xData.push(date.format('YYYY-MM'));
      barChartData.value.yData.push(null);
    }
  } else if (often > 730 * 24) {
    for (let date = startDate; date.isBefore(endDate) || date.isSame(endDate); date = date.add(1, 'year')) {
      barChartData.value.xData.push(date.format('YYYY'));
      barChartData.value.yData.push(null);
    }
  }

  barChartData.value.title = `${t('添加')}${props.barTitle}`;
  barChartData.value.unit = `${t('时间单位')}: ${dateRangeType.value[1]}`;
};

const setBarCharCount = (data:Record<string, any>) => {
  if (!data) {
    setBarChartDefault();
    return;
  }
  const keys = Object.keys(data);
  if (!keys.length) {
    setBarChartDefault();
    return;
  }
  setBarChartDefault();

  barChartData.value.xData = dateRangeType.value[0] !== 'HOUR' ? keys : keys.map(item => dayjs(item).format('HH'));
  barChartData.value.yData = Object.values(data).map(item => item.COUNT_id ? +item.COUNT_id : null);
};

onMounted(() => {
  loadDateCount();
});

watch(() => props.hasPieChart, newValue => {
  if (newValue) {
    loadCount();
  }
}, {
  immediate: true
});

watch(() => props.searchParams, newValue => {
  if (newValue.length) {
    targetTypeFilter.value = [];
    userFilter.value = [];
    for (let i = 0; i < newValue.length; i++) {
      if (newValue[i].key === 'targetType') {
        targetTypeFilter.value = [{
          key: 'target_type',
          op: 'EQUAL',
          value: newValue[i].value
        }];
      }
      if (newValue[i].key === 'userId') {
        userFilter.value = [{
          key: 'user_id',
          op: 'EQUAL',
          value: newValue[i].value
        }];
      }
    }
  } else {
    targetTypeFilter.value = [];
    userFilter.value = [];
  }

  loadCount();
  loadDateCount();
});

</script>
<template>
  <div
    class="statistics-container overflow-hidden relative h-57"
    :class="{'show-statistics':props.visible}">
    <BarChartParam
      :datePicker="datePicker"
      :resource="props.resource"
      :dateType="props.dateType"
      class="search-bar"
      @selectDate="selectDate"
      @dateChange="dateChange" />
    <div
      v-if="!pieLoading"
      class="flex w-full"
      style="height: calc(100% - 28px);">
      <LineChart
        class="w-1/2"
        :title="barChartData?.title"
        :unit="barChartData?.unit"
        :xData="barChartData?.xData"
        :yData="barChartData?.yData" />
      <div class="flex w-1/2">
        <PieChart
          v-for="item in pieChartData"
          :key="item.key"
          class="flex-1"
          :title="item.title"
          :color="item.color"
          :total="item.total"
          :dataSource="item.data" />
      </div>
    </div>
  </div>
</template>
