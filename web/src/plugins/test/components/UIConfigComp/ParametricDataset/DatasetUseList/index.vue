<script setup lang="ts">
import { onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Button } from 'ant-design-vue';
import { Hints, Table, Icon, NoData, Spin } from '@xcan-angus/vue-ui';
import { dataset } from '@/api/tester';

// Initialize i18n for internationalization
const { t } = useI18n();

/**
 * Dataset usage target item interface
 */
interface TargetItem {
  targetId: string;    // Resource unique identifier
  targetType: string;  // Resource type (e.g., SCENARIO, API)
  targetName: string;  // Resource name
}

/**
 * Component props interface
 */
type Props = {
  id: string;  // Dataset ID to query usage for
}

// Define props with default values
const props = withDefaults(defineProps<Props>(), {
  id: undefined
});

/**
 * State Management
 */
const loading = ref(false);   // Loading state during API call
const loaded = ref(false);    // Whether data has been loaded at least once

/**
 * Pagination configuration
 */
const pagination = ref<{ 
  current: number; 
  pageSize: number; 
  total: number; 
  showSizeChanger: false; 
}>({ 
  current: 1,             // Current page number
  pageSize: 10,           // Items per page
  total: 0,               // Total items count
  showSizeChanger: false  // Hide page size changer
});

/**
 * List of resources using this dataset
 */
const dataList = ref<TargetItem[]>([]);

/**
 * Refresh data list
 * Prevents double-loading if already loading
 */
const refresh = (): void => {
  if (loading.value) {
    return;
  }

  loadData();
};

/**
 * Load dataset usage targets from API
 * Fetches list of resources that reference this dataset
 */
const loadData = async (): Promise<void> => {
  loading.value = true;
  const [error, res] = await dataset.getDataSetTarget(props.id);
  loading.value = false;
  loaded.value = true;
  
  // Handle error
  if (error) {
    pagination.value.total = 0;
    dataList.value = [];
    return;
  }

  // Process response data
  const data = res?.data || [];
  pagination.value.total = data.length;
  dataList.value = data;
};

/**
 * Component mount lifecycle hook
 * Sets up watcher to load data when ID changes
 */
onMounted(() => {
  /**
   * Watch for dataset ID changes
   * Loads usage data when ID is provided
   */
  watch(() => props.id, (newValue) => {
    if (!newValue) {
      return;
    }

    loadData();
  }, { immediate: true });
});

/**
 * Table column definitions
 */
const columns = [
  {
    title: t('httpPlugin.uiConfig.httpConfigs.parametric.dataset.datasetUseListField.resourceType'),
    dataIndex: 'targetType',
    width: '10%',
    ellipsis: true  // Truncate long text with ellipsis
  },
  {
    title: t('httpPlugin.uiConfig.httpConfigs.parametric.dataset.datasetUseListField.resourceId'),
    dataIndex: 'targetId',
    width: '25%',
    ellipsis: true  // Truncate long text with ellipsis
  },
  {
    title: t('httpPlugin.uiConfig.httpConfigs.parametric.dataset.datasetUseListField.resourceName'),
    dataIndex: 'targetName',
    ellipsis: true  // Truncate long text with ellipsis
  }
];
</script>

<template>
  <!-- Loading spinner wrapper -->
  <Spin :spinning="loading" class="text-3 leading-5">
    <!-- Header: description hint and refresh button -->
    <div class="flex items-center justify-between mb-2">
      <!-- Description hint label -->
      <Hints :text="t('common.description')" />
      
      <!-- Refresh button -->
      <Button
        :disabled="loading"
        size="small"
        type="text"
        class="flex items-center px-0 h-5 leading-5 border-0 text-theme-text-hover"
        @click="refresh">
        <Icon icon="icon-shuaxin" class="text-3.5" />
        <span class="ml-1">{{ t('actions.refresh') }}</span>
      </Button>
    </div>

    <!-- Content (shown after initial load) -->
    <template v-if="loaded">
      <!-- No data placeholder (when no resources use this dataset) -->
      <NoData
        v-if="!dataList.length"
        size="small"
        class="mt-5 mb-10" />

      <!-- Usage targets table (resources using this dataset) -->
      <Table
        v-else
        rowKey="targetId"
        class="flex-1 mb-3.5"
        :dataSource="dataList"
        :columns="columns"
        :pagination="pagination">
        <template #bodyCell="{ column, record }">
          <div v-if="column.dataIndex === 'targetType'" class="truncate">
            {{ record.targetType?.message }}
          </div>
        </template>
      </Table>
    </template>
  </Spin>
</template>
<style scoped>
:deep(.ant-pagination) {
  margin-bottom: 0;
}
</style>
