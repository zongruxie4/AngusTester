<script lang="ts" setup>
import { computed, defineAsyncComponent, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue';
import { TESTER, http, download } from '@xcan-angus/tools';
import { Icon } from '@xcan-angus/vue-ui';
import { Button, Table, Tag } from 'ant-design-vue';
import { TemplateIconConfig } from '@/views/task/analysis/list/PropTypes';
import { debounce } from 'throttle-debounce';
import { Analysis } from '../PropType';

interface Props {
  data?: Record<string, string>;
  projectId: string;
  userInfo: {id: string};
  onShow: boolean
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  userInfo: () => ({ id: '' }),
  data: undefined,
  onShow: false
});

const chartRef = ref();

const Process = defineAsyncComponent(() => import('./progress/index.vue'));
const Burndown = defineAsyncComponent(() => import('./burndown/index.vue'));
const Workload = defineAsyncComponent(() => import('./workload/index.vue'));
const OverdueAssessment = defineAsyncComponent(() => import('./overdueAssessment/index.vue'));
const Bugs = defineAsyncComponent(() => import('./bugs/index.vue'));
const HandlingEfficiency = defineAsyncComponent(() => import('./handlingEfficiency/index.vue'));
const CoreKpi = defineAsyncComponent(() => import('./coreKpi/index.vue'));
const Failures = defineAsyncComponent(() => import('./failures/index.vue'));
const BascklogTasks = defineAsyncComponent(() => import('./backlogTasks/index.vue'));
const RecentDelivery = defineAsyncComponent(() => import('./recentDelivery/index.vue'));
const LeadTime = defineAsyncComponent(() => import('./leadTime/index.vue'));
const UnplannedTasks = defineAsyncComponent(() => import('./unplannedTasks/index.vue'));
const TaskGrowthTread = defineAsyncComponent(() => import('./taskGrowthTread/index.vue'));
const ResourceCreation = defineAsyncComponent(() => import('./resourceCreation/index.vue'));

const dataSource = ref<Analysis>({});

const loadAnalysisInfo = async (id) => {
  const [error, { data }] = await http.get(`${TESTER}/analysis/${id}`);
  if (error) {
    return;
  }
  dataSource.value = data;
  nextTick(() => {
    chartRef.value && chartRef.value.resize();
  });
};

const columns = computed(() => {
  if (dataSource.value.data?.dataDetailTitles?.length) {
    const headers = dataSource.value.data?.dataDetailTitles;
    return headers.map((name, idx) => {
      return {
        title: name,
        dataIndex: idx,
        fixed: idx === 0 ? 'left' : false,
        ellipsis: idx === 0,
        width: 80
      };
    });
  }
  return [];
});

const tableData = computed(() => {
  if (dataSource.value.data?.tableData?.length) {
    const datas = dataSource.value.data?.tableData.splice(1);
    return datas.map((item) => {
      const data = {};
      item.forEach((value, idx) => {
        data[idx] = value;
      });
      return data;
    });
  }
  return [];
});

const recentDeliveryTodayTable = computed(() => {
  if (dataSource.value.data?.multiTableData?.today?.length) {
    const datas = dataSource.value.data?.multiTableData?.today.splice(1);
    return datas.map((item) => {
      const data = {};
      item.forEach((value, idx) => {
        data[idx] = value;
      });
      return data;
    });
  }
  return [];
});
const recentDeliveryLastWeekTable = computed(() => {
  if (dataSource.value.data?.multiTableData?.lastWeek?.length) {
    const datas = dataSource.value.data?.multiTableData?.lastWeek.splice(1);
    return datas.map((item) => {
      const data = {};
      item.forEach((value, idx) => {
        data[idx] = value;
      });
      return data;
    });
  }
  return [];
});

const recentDeliveryLastMonthTable = computed(() => {
  if (dataSource.value.data?.multiTableData?.lastMonth?.length) {
    const datas = dataSource.value.data?.multiTableData?.lastMonth.splice(1);
    return datas.map((item) => {
      const data = {};
      item.forEach((value, idx) => {
        data[idx] = value;
      });
      return data;
    });
  }
  return [];
});

const downloading = ref(false);
const exportDetail = async () => {
  downloading.value = true;
  await download(`${TESTER}/analysis/${props.data?.id}/overview/export`);
  downloading.value = false;
};

let windowSizeChange = false;
const handleResize = debounce(300, () => {
  if (!props.onShow) {
    windowSizeChange = true;
    return;
  }
  windowSizeChange = false;
  chartRef.value.resize();
});

onMounted(() => {
  if (props.data?.id) {
    loadAnalysisInfo(props.data?.id);
  }
  watch(() => props.onShow, (newValue) => {
    if (newValue && windowSizeChange) {
      handleResize();
    }
  });
  window.addEventListener('resize', handleResize);
});

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize);
});
</script>

<template>
  <div class="p-5 h-full overflow-auto">
    <div class="bg-gray-1 p-2 flex items-center space-x-2">
      <Icon :icon="TemplateIconConfig[dataSource.template]" class="text-20" />
      <div class="flex-1 min-w-0 flex flex-col justify-around">
        <div>
          <span class="text-3.5 font-semibold">{{ dataSource.name }}</span><Tag
            v-if="dataSource.datasource?.value"
            color="geekblue"
            class="ml-2">
            {{ dataSource.datasource?.value === 'SNAPSHOT_DATA' ? '快照' : dataSource.datasource?.value === 'REAL_TIME_DATA' ? '实时' : '' }}
          </Tag>
        </div>
        <div class="mt-2">{{ dataSource.description }}</div>
        <div><span class="font-semibold">{{ dataSource.lastModifiedByName }}</span> 最后修改于{{ dataSource.lastModifiedDate }}</div>
      </div>
    </div>
    <div ref="chartWrapRef" class="mt-4">
      <div class="detail-title font-semibold pl-2 relative text-3.5 mb-3">图表</div>
      <div v-if="dataSource.template === 'PROGRESS'" class=" w-200">
        <Process ref="chartRef" :analysisInfo="dataSource" />
      </div>
      <div v-if="dataSource.template === 'BURNDOWN'" class=" w-275">
        <Burndown ref="chartRef" :analysisInfo="dataSource" />
      </div>
      <div v-if="dataSource.template === 'WORKLOAD'" class="max-w-300">
        <Workload ref="chartRef" :analysisInfo="dataSource" />
      </div>
      <div v-if="dataSource.template === 'OVERDUE_ASSESSMENT'" class="max-w-275">
        <OverdueAssessment ref="chartRef" :analysisInfo="dataSource" />
      </div>
      <div v-if="dataSource.template === 'BUGS'" class="max-w-400">
        <Bugs ref="chartRef" :analysisInfo="dataSource" />
      </div>
      <div v-if="dataSource.template === 'HANDLING_EFFICIENCY'" class="max-w-300">
        <HandlingEfficiency ref="chartRef" :analysisInfo="dataSource" />
      </div>
      <div v-if="dataSource.template === 'CORE_KPI'" class="max-w-400">
        <CoreKpi ref="chartRef" :analysisInfo="dataSource" />
      </div>
      <div v-if="dataSource.template === 'FAILURES'" class="max-w-300">
        <Failures ref="chartRef" :analysisInfo="dataSource" />
      </div>
      <div v-if="dataSource.template === 'BACKLOG_TASKS'" class="max-w-400">
        <BascklogTasks ref="chartRef" :analysisInfo="dataSource" />
      </div>
      <div v-if="dataSource.template === 'RECENT_DELIVERY'" class="max-w-300">
        <RecentDelivery ref="chartRef" :analysisInfo="dataSource" />
      </div>
      <div v-if="dataSource.template === 'LEAD_TIME'" class="max-w-300">
        <LeadTime ref="chartRef" :analysisInfo="dataSource" />
      </div>
      <div v-if="dataSource.template === 'UNPLANNED_TASKS'" class="max-w-300">
        <UnplannedTasks ref="chartRef" :analysisInfo="dataSource" />
      </div>
      <div v-if="dataSource.template === 'TASK_GROWTH_TREND'" class="max-w-300">
        <TaskGrowthTread ref="chartRef" :analysisInfo="dataSource" />
      </div>
      <div v-if="dataSource.template === 'RESOURCE_CREATION'" class="max-w-300">
        <ResourceCreation ref="chartRef" :analysisInfo="dataSource" />
      </div>
    </div>

    <div v-if="dataSource?.containsDataDetail" class="mt-4">
      <div class="detail-title font-semibold pl-2 relative text-3.5 flex items-center">
        <span>明细</span>
        <Button
          v-show="props.data?.id"
          type="link"
          :loading="downloading"
          size="small"
          class="ml-3"
          @click="exportDetail">
          <Icon icon="icon-daochu1" class="mr-1" />
          导出
        </Button>
      </div>
      <template v-if="dataSource.template === 'RECENT_DELIVERY'">
        <div class="text-center mt-3">
          今天
        </div>
        <Table
          key="taday"
          class="w-full mb-5 mt-2"
          size="small"
          :pagination="false"
          :scroll="{x: 1000, y: 300}"
          :columns="columns"
          :dataSource="recentDeliveryTodayTable" />
        <div class="text-center">
          近一周
        </div>
        <Table
          key="lastWeek"
          class="w-full mb-5 mt-2"
          size="small"
          :pagination="false"
          :scroll="{x: 1000, y: 300}"
          :columns="columns"
          :dataSource="recentDeliveryLastWeekTable" />
        <div class="text-center">
          近一周
        </div>
        <Table
          key="lastMonth"
          class="w-full mb-5 mt-2"
          size="small"
          :pagination="false"
          :scroll="{x: 1000, y: 300}"
          :columns="columns"
          :dataSource="recentDeliveryLastMonthTable" />
      </template>
      <!-- <Table v-else-if="dataSource.template === 'RESOURCE_CREATION'"
      class="w-full mt-3"
      size="small"
      :pagination="false"
      :scroll="{x: 1000, y: 300}"
      :columns="columns"
      :dataSource="tableData">
    </Table> -->
      <Table
        v-else
        class="w-full mt-3"
        size="small"
        :pagination="false"
        :scroll="{x: 1000, y: 300}"
        :columns="columns"
        :dataSource="tableData">
      </Table>
    </div>
  </div>
</template>
<style scoped>
.detail-title {
  @apply relative;
}

.detail-title::before{
  @apply bg-blue-1;

  content: '';
  display: inline-block;
  position: absolute;
  top: 2px;
  left: 0;
  width: 4px;
  height: 18px;
}
</style>
