<script setup lang="ts">
import { defineAsyncComponent } from 'vue';
import { Icon, IconRefresh, Input, IntervalTimestamp, PureCard, Table } from '@xcan-angus/vue-ui';
import { Tooltip } from 'ant-design-vue';
import { TESTER } from '@xcan-angus/infra';
import { useI18n } from 'vue-i18n';
import { useChartData } from './composables/useChartData';
import { useApiStats } from './composables/useApiStats';
import { useApiTable } from './composables/useApiTable';
import { useTableColumns } from './composables/useTableColumns';

interface Props {
  id: string;
}

const props = withDefaults(defineProps<Props>(), {
  id: undefined
});

// Lazy load chart components for better performance
const CacheChart = defineAsyncComponent(() => import('@/components/chart/mock/cacheChart.vue'));
const RamChart = defineAsyncComponent(() => import('@/components/chart/mock/ramChart.vue'));
const CpuChart = defineAsyncComponent(() => import('@/components/chart/mock/cpuChart.vue'));

const { t } = useI18n();

// Use composables to manage different aspects of the control panel
const { cacheChartData, ramChartData, cpuChartData, handleChartDataChange } = useChartData();
const { stats: apiStats } = useApiStats(props.id);
const {
  tableData,
  loading: tableLoading,
  paginationConfig,
  handleSearch,
  handleTableChange,
  refreshTable
} = useApiTable(props.id);
const {
  columns,
  mockServiceCountConfig,
  countIconColors,
  httpMethodColors
} = useTableColumns();

</script>
<template>
  <PureCard class="py-3.5">
    <div class="flex justify-end pr-15">
      <IntervalTimestamp
        :action="`${TESTER}/mock/service/${props.id}/metrics`"
        @change="handleChartDataChange" />
    </div>
    <div class="flex h-60 space-x-2 mt-2">
      <CacheChart
        class="w-1/3"
        :xData="cacheChartData.xData"
        :numData="cacheChartData.numData"
        :totalData="cacheChartData.totalData"
        :usedData="cacheChartData.usedData"
        :maxNumData="cacheChartData.maxNumData"
        :maxTotalData="cacheChartData.maxTotalData" />
      <RamChart
        class="w-1/3"
        :xData="ramChartData.xData"
        :submitData="ramChartData.submitData"
        :totalData="ramChartData.totalData"
        :usedData="ramChartData.usedData" />
      <CpuChart
        class="w-1/3"
        :xData="cpuChartData.xData"
        :totalData="cpuChartData.totalData"
        :systemCpuUsed="cpuChartData.systemCpuUsed"
        :processCpuUsed="cpuChartData.processCpuUsed" />
    </div>
  </PureCard>
  <PureCard class="p-3.5 mt-2">
    <div class="flex text-3 space-x-3">
      <div
        v-for="(item,index) in mockServiceCountConfig"
        :key="index"
        class="rounded p-2 flex-1 flex justify-around space-x-2 items-center"
        style="min-width: 140px;background-color: rgba(15, 159, 255, 10%);">
        <Icon
          :icon="item.icon"
          class="text-5"
          :class="countIconColors[item.key]" />
        <span class="text-text-sub-content">{{ item.name }}</span>
        <span class="text-text-title font-semibold">{{ apiStats[item.key] }}</span>
      </div>
    </div>
  </PureCard>
  <PureCard class="p-3.5 mt-2">
    <div class="flex items-center text-3 mb-2 justify-between">
      <Input
        allowClear
        class="w-60"
        :placeholder="t('mock.mockDetail.control.searchApiNamePath')"
        @change="handleSearch($event.target.value)" />
      <IconRefresh
        :loading="tableLoading"
        class="text-4.5"
        @click="refreshTable" />
    </div>
    <Table
      rowKey="id"
      :loading="tableLoading"
      :columns="columns as any"
      :dataSource="tableData"
      :pagination="paginationConfig"
      @change="handleTableChange">
      <template #headerCell="{title, column}">
        <template v-if="column.dataIndex === 'requestNum'">
          {{ title }}
          <Tooltip
            :title="t('mock.mockDetail.control.tooltips.requestCount')"
            placement="top"
            arrowPointAtCenter
            :overlayStyle="{'max-width': '600px'}">
            <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1" />
          </Tooltip>
        </template>
        <template v-if="column.dataIndex === 'simulateErrorNum'">
          {{ title }}
          <Tooltip
            :title="t('mock.mockDetail.control.tooltips.mockExceptionCount')"
            placement="top"
            arrowPointAtCenter
            :overlayStyle="{'max-width': '600px'}">
            <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1" />
          </Tooltip>
        </template>
        <template v-if="column.dataIndex === 'pushbackNum'">
          {{ title }}
          <Tooltip
            :title="t('mock.mockDetail.control.tooltips.pushbackCount')"
            placement="top"
            arrowPointAtCenter
            :overlayStyle="{'max-width': '600px'}">
            <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1" />
          </Tooltip>
        </template>
        <template v-if="column.dataIndex === 'successNum'">
          {{ title }}
          <Tooltip
            :title="t('mock.mockDetail.control.tooltips.successCount')"
            placement="top"
            arrowPointAtCenter
            :overlayStyle="{'max-width': '600px'}">
            <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1" />
          </Tooltip>
        </template>
        <template v-if="column.dataIndex === 'exceptionNum'">
          {{ title }}
          <Tooltip
            :title="t('mock.mockDetail.control.tooltips.exceptionCount')"
            placement="top"
            arrowPointAtCenter
            :overlayStyle="{'max-width': '600px'}">
            <Icon icon="icon-tishi1" class="text-3.5 text-tips cursor-pointer ml-1" />
          </Tooltip>
        </template>
      </template>
      <template #bodyCell="{text, record, column}">
        <template v-if="column.dataIndex === 'method'">
          <div class="flex">
            <div :class="httpMethodColors[text.value]" class="leading-5 text-center flex-none">{{ text?.message || '--' }}</div>
            <div class="flex-1 truncate ml-3.5" :title="record.endpoint">{{ record.endpoint }}</div>
          </div>
        </template>
      </template>
    </Table>
  </PureCard>
</template>
