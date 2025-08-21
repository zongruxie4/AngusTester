<script setup lang='ts'>
import { computed, defineAsyncComponent, onMounted, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { Hints, Image, Table } from '@xcan-angus/vue-ui';
import { debounce } from 'throttle-debounce';
import { duration, TESTER } from '@xcan-angus/infra';
import DOMPurify from 'dompurify';

import { activity } from 'src/api/tester';
import { setting } from '@/api/gm';

import SearchPanel from '@/views/project/activity/searchPanel/index.vue';

type FilterOp = 'EQUAL' | 'NOT_EQUAL' | 'GREATER_THAN' | 'GREATER_THAN_EQUAL' | 'LESS_THAN' | 'LESS_THAN_EQUAL' | 'CONTAIN' | 'NOT_CONTAIN' | 'MATCH_END' | 'MATCH' | 'IN' | 'NOT_IN'
type Filters = { key: string, value: string | boolean | string[], op: FilterOp };
type SearchParam = {
    pageNo: number;
    pageSize: number;
    filters?: Filters[];
    orderBy?: string;
    orderSort?: 'ASC' | 'DESC';
    [key: string]: any;
};

const Statistics = defineAsyncComponent(() => import('@/views/project/activity/countChart/index.vue'));

const { t } = useI18n();

interface Activity {
    id: string
    optDate: string
    targetId: string
    targetType: any
    title: string,
    fullName: string,
    description: string,
    detail: string,
    details: string[],
    avatar?: string
}

const showCount = ref(true);
const openCount = () => {
  showCount.value = !showCount.value;
};

const total = ref(0);
const params = ref<SearchParam>({ pageNo: 1, pageSize: 10, filters: [] });
const tableData = ref<Activity[]>([]);

const loading = ref(false);
const getList = async () => {
  if (loading.value) {
    return;
  }
  loading.value = true;
  const [error, { data = { list: [], total: 0 } }] = await activity.getActivityList(params.value);
  loading.value = false;
  if (error) {
    return;
  }
  tableData.value = data.list?.map(item => {
    return {
      ...item,
      detail: DOMPurify.sanitize(item.detail)
    };
  });
  total.value = +data.total;
};

const searchChange = (data:{filters: Filters[]}) => {
  params.value.pageNo = 1;
  params.value.filters = data.filters;
  getList();
};

const maxResource = ref('0');
const getMaxResourceActivities = async () => {
  const [error, { data }] = await setting.getSettingByKey('MAX_RESOURCE_ACTIVITIES');
  if (error) {
    return;
  }
  maxResource.value = data.maxResourceActivities;
};

const tableChange = debounce(duration.search, (_pagination, _filters, sorter) => {
  const { current, pageSize } = _pagination;
  params.value.pageNo = current;
  params.value.pageSize = pageSize;
  params.value.orderBy = sorter.orderBy;
  params.value.orderSort = sorter.orderSort;
  getList();
});

const pagination = computed(() => {
  return {
    current: params.value.pageNo,
    pageSize: params.value.pageSize,
    total: total.value
  };
});

onMounted(() => {
  getList();
  getMaxResourceActivities();
});

const columns = [
  {
    title: t('projectActivity.table.operator'),
    dataIndex: 'fullName',
    width: '10%',
    ellipsis: true
  },
  {
    title: t('projectActivity.table.activityTime'),
    dataIndex: 'optDate',
    sorter: true,
    width: '10%',
    customCell: () => {
      return { style: 'white-space:nowrap;' };
    }
  },
  {
    title: t('projectActivity.table.projectName'),
    dataIndex: 'projectName',
    groupName: 'project',
    width: '15%',
    ellipsis: true
  },
  {
    title: t('projectActivity.table.projectId'),
    dataIndex: 'projectId',
    groupName: 'project',
    hide: true,
    width: '15%'
  },
  {
    title: t('projectActivity.table.resourceType'),
    dataIndex: 'targetType',
    customRender: ({ text }) => text?.message,
    width: '8%'
  },
  {
    title: t('projectActivity.table.targetId'),
    dataIndex: 'targetId',
    width: '17%',
    groupName: 'source',
    hide: true,
    customCell: () => {
      return { style: 'white-space:nowrap;' };
    }
  },
  {
    title: t('projectActivity.table.targetName'),
    dataIndex: 'targetName',
    width: '17%',
    ellipsis: true,
    groupName: 'source'
  },
  {
    title: t('projectActivity.table.activityContent'),
    width: '40%',
    dataIndex: 'detail'
  }
];

// const searchParams = computed(() => params.value.filters);
</script>
<template>
  <div class="p-3.5 px-5 text-3">
    <Statistics
      :visible="showCount"
      :barTitle="t('projectActivity.chart.activity')"
      resource="Activity"
      :apiRouter="TESTER" />
    <Hints class="mb-2" :text="t('projectActivity.hints.maxResourceActivities', { maxResource })" />
    <SearchPanel
      :loading="loading"
      :showCount="showCount"
      @openCount="openCount"
      @change="searchChange"
      @refresh="getList" />
    <Table
      rowKey="id"
      :loading="loading"
      :columns="columns"
      :dataSource="tableData"
      :pagination="pagination"
      @change="tableChange">
      <template #bodyCell="{ column, text, record }">
        <template v-if="column.dataIndex === 'fullName'">
          <div class="flex items-center">
            <Image
              :src="record.avatar"
              class="w-5 h-5 rounded-full flex-none mr-1"
              type="avatar" />
            {{ text }}
          </div>
        </template>
        <template v-if="column.dataIndex === 'detail'">
          <div
            class="w-full truncate cursor-pointer"
            :title="text"
            v-html="text">
          </div>
        </template>
      </template>
    </Table>
  </div>
</template>
