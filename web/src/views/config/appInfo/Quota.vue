<script setup lang="ts">
import { IconRefresh, Table, Hints } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import { useQuotaData } from './composables/useQuotaData';

const { t } = useI18n();

// Use composable for quota data management
const {
  // Reactive data
  loading,
  dataSource,
  pagination,

  // Methods
  loadData,
  changePage,
  init
} = useQuotaData();

// Initialize data on component mount
init();

/**
 * Column configuration for quota table
 */
const columns = [
  {
    title: t('app.config.quota.table.columns.resourceName'),
    dataIndex: 'name'
  },
  {
    title: t('app.config.quota.table.columns.quotaKey'),
    dataIndex: 'key'
  },
  {
    title: t('app.config.quota.table.columns.currentQuota'),
    dataIndex: 'quota'
  },
  {
    title: t('app.config.quota.table.columns.defaultQuota'),
    dataIndex: 'default0'
  },
  {
    title: t('app.config.quota.table.columns.maxQuota'),
    dataIndex: 'max'
  }
];
</script>
<template>
  <div class="flex items-center text-3">
    <div class="flex-1 min-w-0 truncate">
      <Hints :text="t('app.config.quota.hints')" />
    </div>
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
