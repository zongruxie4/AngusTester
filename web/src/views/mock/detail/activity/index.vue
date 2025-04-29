<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { ActivityInfo, IconRefresh, Input, PureCard, Spin } from '@xcan-angus/vue-ui';
import { Pagination } from 'ant-design-vue';
import { debounce } from 'throttle-debounce';
import { duration } from '@xcan-angus/tools';
import { activity } from '@/api/altester';

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

interface Props {
  id:string;
}

const props = withDefaults(defineProps<Props>(), {
  id: undefined
});

const total = ref(0);
const params = ref<{pageNo:number, pageSize:number, filters:{key:string, value:string | string[], op:string}[]}>({ pageNo: 1, pageSize: 20, filters: [{ key: 'targetType', value: 'MOCK_SERVICE', op: 'EQUAL' }, { key: 'targetId', value: props.id, op: 'EQUAL' }] });
const activities = ref<Activity[]>([]);
const detail = ref('');
const searchInputChange = debounce(duration.search, (value) => {
  if (value) {
    params.value.filters = [...params.value.filters, { key: 'detail', op: 'MATCH_END', value: value }];
  } else {
    params.value.filters = params.value.filters.filter(item => item.key !== 'detail');
  }
  getList();
});

const loading = ref(false);
const getList = async () => {
  if (loading.value) {
    return;
  }
  loading.value = true;
  const [error, { data = { list: [], total: 0 } }] = await activity.loadActivities(params.value);
  loading.value = false;
  if (error) {
    return;
  }
  activities.value = data.list;
  total.value = +data.total;
};

const showTotal = (total) => {
  const totalPage = Math.ceil(total / params.value.pageSize);
  return `共${total}条记录，第${params.value.pageNo} / ${totalPage}页`;
};

const onShowSizeChange = (page: number, pageSize: number) => {
  params.value.pageNo = page;
  params.value.pageSize = pageSize;
  getList();
};

onMounted(() => {
  getList();
});
</script>
<template>
  <PureCard class="p-3.5 flex flex-col h-full justify-between space-y-3.5">
    <div class="flex items-center justify-between">
      <Input
        v-model:value="detail"
        :allowClear="true"
        class="w-100"
        placeholder="查询活动详情"
        size="small"
        @change="searchInputChange($event.target.value)" />
      <IconRefresh
        :loading="loading"
        class="text-4.5"
        @click="getList" />
    </div>
    <Spin :spinning="loading" class="flex-1 overflow-y-auto -mr-3.5 pr-2">
      <ActivityInfo :dataSource="activities" />
    </Spin>
    <Pagination
      v-if="total>10"
      v-model:current="params.pageNo"
      v-model:page-size="params.pageSize"
      :total="total"
      showSizeChanger
      :showTotal="showTotal"
      @change="onShowSizeChange" />
  </PureCard>
</template>
