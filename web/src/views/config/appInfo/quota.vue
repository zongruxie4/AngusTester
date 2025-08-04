<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { Icon, IconRefresh, Table } from '@xcan-angus/vue-ui';
import { setting } from '@/api/gm';
import { appContext } from '@xcan-angus/infra';

const userInfo = ref(appContext.getUser());

const columns = [
  {
    title: '资源名称',
    dataIndex: 'name'
  },
  {
    title: '配额Key',
    dataIndex: 'key'
  },
  {
    title: '当前配额',
    dataIndex: 'quota'
  },
  {
    title: '默认配额',
    dataIndex: 'default0'
  },
  {
    title: '允许上限',
    dataIndex: 'max'
  }
];

const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0
});

const loading = ref(false);
const dataSource = ref([]);

const loadData = async () => {
  loading.value = true;
  const { current, pageSize } = pagination;
  const [error, res] = await setting.getQuotaList({ appCode: 'AngusTester', tenantId: userInfo.value.id, pageNo: current, pageSize });
  loading.value = false;
  if (error) {
    return;
  }
  dataSource.value = res.data.list || [];
  pagination.total = +res.data.total;
};

const changePage = (page) => {
  pagination.current = page.current;
  pagination.pageSize = page.pageSize;
  loadData();
};

onMounted(() => {
  loadData();
});
</script>
<template>
  <div class="flex items-center text-3">
    <Icon icon="icon-tishi1" class="text-blue-icon mr-2 text-3.5" />
    <p class="flex-1">若有扩展资源配额的需求，可以提交工单联系工单工程师帮您处理！</p>
    <IconRefresh
      :loading="loading"
      class="self-end text-4.5"
      @click="loadData" />
  </div>
  <Table
    :loading="loading"
    :dataSource="dataSource"
    :columns="columns"
    :pagination="pagination"
    class="mt-3"
    size="small"
    @change="changePage">
    <template #bodyCell="{text, record, column}">
      <template v-if="column.dataIndex === 'name'">
        {{ text.message }}
      </template>
      <template v-if="column.dataIndex === 'key'">
        {{ record.name.value }}
      </template>
    </template>
  </Table>
</template>
