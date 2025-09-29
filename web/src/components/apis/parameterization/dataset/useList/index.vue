<script setup lang="ts">
// Vue core imports
import { onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';

// UI component imports
import { Button } from 'ant-design-vue';
import { Hints, Icon, NoData, Spin, Table } from '@xcan-angus/vue-ui';

// API imports
import { dataSet } from '@/api/tester';

const { t } = useI18n();

/**
 * Target usage item interface
 */
interface TargetUsageItem {
  targetId: string;
  targetType: string;
  targetName: string;
}

/**
 * Component props interface for use list
 */
type Props = {
  id: string;
}

// Component props with defaults
const props = withDefaults(defineProps<Props>(), {
  id: undefined
});

// Component state
const isLoading = ref(false);
const isDataLoaded = ref(false);
const paginationConfig = ref<{ current: number; pageSize: number; total: number; showSizeChanger: false; }>({ current: 1, pageSize: 10, total: 0, showSizeChanger: false });
const targetUsageList = ref<TargetUsageItem[]>([]);

/**
 * Handle refresh button click
 */
const handleRefresh = () => {
  if (isLoading.value) {
    return;
  }

  loadTargetUsageData();
};

/**
 * Load target usage data from API
 */
const loadTargetUsageData = async () => {
  isLoading.value = true;
  const [error, response] = await dataSet.getDataSetTarget(props.id);
  isLoading.value = false;
  isDataLoaded.value = true;
  if (error) {
    paginationConfig.value.total = 0;
    targetUsageList.value = [];
    return;
  }

  const responseData = response?.data || [];
  paginationConfig.value.total = responseData.length;
  targetUsageList.value = responseData;
};

// Component initialization and watchers
onMounted(() => {
  watch(() => props.id, (newValue) => {
    if (!newValue) {
      return;
    }

    loadTargetUsageData();
  }, { immediate: true });
});

// Table column configuration
const tableColumns = [
  {
    key: 'targetType',
    title: t('commonComp.apis.parameterizationDataset.useList.targetType'),
    dataIndex: 'targetType',
    width: '10%',
    ellipsis: true
  },
  {
    key: 'targetId',
    title: t('commonComp.apis.parameterizationDataset.useList.targetId'),
    dataIndex: 'targetId',
    width: '25%',
    ellipsis: true
  },
  {
    key: 'targetName',
    title: t('commonComp.apis.parameterizationDataset.useList.targetName'),
    dataIndex: 'targetName',
    ellipsis: true
  }
];
</script>

<template>
  <Spin :spinning="isLoading" class="text-3 leading-5">
    <div class="flex items-center justify-between mb-2">
      <Hints :text="t('commonComp.apis.parameterizationDataset.useList.hintText')" />
      <Button
        :disabled="isLoading"
        size="small"
        type="text"
        class="flex items-center px-0 h-5 leading-5 border-0 text-theme-text-hover"
        @click="handleRefresh">
        <Icon icon="icon-shuaxin" class="text-3.5" />
        <span class="ml-1">{{ t('actions.refresh') }}</span>
      </Button>
    </div>

    <template v-if="isDataLoaded">
      <NoData
        v-if="!targetUsageList.length"
        size="small"
        class="mt-5 mb-10" />

      <Table
        v-else
        rowKey="targetId"
        class="flex-1 mb-3.5"
        :dataSource="targetUsageList"
        :columns="tableColumns"
        :pagination="paginationConfig">
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
