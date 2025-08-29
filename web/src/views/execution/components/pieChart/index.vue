<script setup lang="ts">
import { computed, inject, onMounted, Ref, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { PieData, PieSetting } from './PropsType';
import { ScriptType, enumUtils } from '@xcan-angus/infra';
import { ExecStatus } from '@/enums/enums';
import { analysis } from '@/api/tester';
import Charts from './charts.vue';

const { t } = useI18n();

const groupByGroup = ref<PieSetting[]>([
  { key: 'script_type', value: t('execution.chartInfo.scriptType'), type: [] },
  { key: 'status', value: t('execution.chartInfo.executionStatus'), type: [] }
]);

const projectInfo = inject<Ref<{ id: string; avatar: string; name: string; }>>('projectInfo', ref({ id: '', avatar: '', name: '' }));
const projectId = computed(() => {
  return projectInfo.value?.id;
});

const scriptTypeData = ref<{name:string, value:number}[]>([]);
const scriptTypeColor = ref<string[]>([]);

const statusData = ref<{name:string, value:number}[]>([]);
const statusColor = ref<string[]>([]);

const init = async () => {
  await loadScriptType();
  await loadExecStatus();
  await loadCount();
};

const loadScriptType = async () => {
  const data = enumUtils.enumToMessages(ScriptType);
  const enums = data.filter(item => item.value !== ScriptType.MOCK_APIS);
  groupByGroup.value[0].type = enums;
  scriptTypeData.value = enums.map(item => {
    let color = '';
    switch (item.value) {
      case 'TEST_PERFORMANCE':
        color = 'rgba(45,142,255, 1)';
        break;
      case 'TEST_FUNCTIONALITY':
        color = 'rgba(82,196,26, 1)';
        break;
      case 'TEST_STABILITY':
        color = 'rgba(255, 185, 37, 1)';
        break;
      case 'TEST_CUSTOMIZATION':
        color = 'rgba(251, 129, 255, 1)';
        break;
      case 'MOCK_DATA':
        color = 'rgba(191, 199, 255, 1)';
        break;
    }
    scriptTypeColor.value.push(color);
    return { name: item.message, value: 0 };
  });
};

const loadExecStatus = () => {
  const data = enumUtils.enumToMessages(ExecStatus);
  groupByGroup.value[1].type = data;
  statusData.value = data.map(item => {
    let color = '';
    switch (item.value) {
      case 'CREATED':
        color = 'rgba(45,142,255, 1)';
        break;
      case 'PENDING':
        color = 'rgba(255,165,43, 1)';
        break;
      case 'RUNNING':
        color = 'rgba(103,215,255, 1)';
        break;
      case 'STOPPED':
        color = 'rgba(217, 217, 217, 1)';
        break;
      case 'FAILED':
        color = 'rgba(245,34,45, 1)';
        break;
      case 'COMPLETED':
        color = 'rgba(82,196,26, 1)';
        break;
      case 'TIMEOUT':
        color = 'rgba(201,119,255, 1)';
        break;
    }
    statusColor.value.push(color);
    return { name: item.message, value: 0 };
  });
};

const publicParams = {
  'aggregates[0].column': 'id',
  'aggregates[0].function': 'COUNT',
  groupBy: 'STATUS',
  name: 'Exec'
};
const pieloading = ref(true); // 饼图统计是否加载完成
const pieChartData = ref<PieData[]>([]);
const loadCount = async () => {
  const params = { ...publicParams, groupByColumns: groupByGroup.value.map(item => item.key), projectId: projectId.value };
  const [error, { data }] = await analysis.getCustomizationSummary(params);
  pieloading.value = false;
  if (error) {
    return;
  }

  pieChartData.value = getCountData(groupByGroup.value, data);
  const _scriptTypeData = pieChartData.value[0].data;
  if (_scriptTypeData?.length) {
    for (let i = 0; i < scriptTypeData.value.length; i++) {
      scriptTypeData.value[i].value = _scriptTypeData[i].value ? _scriptTypeData[i].value : 0;
    }
  }

  const _statusData = pieChartData.value[1].data;
  if (_statusData?.length) {
    for (let i = 0; i < statusData.value.length; i++) {
      statusData.value[i].value = _statusData[i].value ? _statusData[i].value : 0;
    }
  }
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
        legend: cloum.type,
        data: []
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
        legend: cloum.type,
        data: []
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
    if (['script_type', 'status'].includes(cloum.key)) {
      setEnumDatasource(cloum, res, dataSource);
    }
  }
  return dataSource;
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
    legend: cloum.type,
    data: _data.length ? _data : []
  };
  dataSource.push(_dataSource);
};

onMounted(() => {
  init();
});

defineExpose({
  loadCount
});
</script>
<template>
  <div class="charts-container flex-shrink-0  h-45">
    <Charts
      key="1"
      style="width: 228px;"
      class="chart-item"
      :title="t('execution.chartInfo.scriptType')"
      type="script_type"
      :color="scriptTypeColor"
      :total="pieChartData[0]?.total"
      :data-source="scriptTypeData" />
    <Charts
      key="2"
      style="width: 274px;"
      class="chart-item"
      :title="t('execution.chartInfo.executionStatus')"
      type="status"
      :color="statusColor"
      :total="pieChartData[1]?.total"
      :data-source="statusData" />
  </div>
</template>
<style scoped>
.charts-container {
  height: 180px;
  white-space: nowrap;
}

.chart-item {
  display: inline-block;
}

.chart-item + .chart-item {
  margin-left: 0;
}

@media screen and (min-width: 1480px) {
  .chart-item + .chart-item {
    margin-left: 40px;
  }
}
</style>
