<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { Icon, IconRefresh, Table } from '@xcan-angus/vue-ui';
import { setting } from '@/api/gm';
import { appContext } from '@xcan-angus/infra';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

const userInfo = ref(appContext.getUser());

const columns = [
  {
    title: t('appConfig.quota.table.columns.resourceName'),
    dataIndex: 'name'
  },
  {
    title: t('appConfig.quota.table.columns.quotaKey'),
    dataIndex: 'key'
  },
  {
    title: t('appConfig.quota.table.columns.currentQuota'),
    dataIndex: 'quota'
  },
  {
    title: t('appConfig.quota.table.columns.defaultQuota'),
    dataIndex: 'default0'
  },
  {
    title: t('appConfig.quota.table.columns.maxQuota'),
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
    <p class="flex-1">{{ t('appConfig.quota.hints') }}</p>
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
