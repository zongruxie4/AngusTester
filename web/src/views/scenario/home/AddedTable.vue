<script setup lang="ts">
import { inject, onMounted, ref, Ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Button } from 'ant-design-vue';
import { Icon, Table } from '@xcan-angus/vue-ui';
import { getCurrentPage } from '@/utils/utils';
import { ScenarioMenuKey } from '@/views/scenario/menu';
import type { MyScenariosTableProps, TableChangeParams } from './types';
import { useData } from './composables/useData';
import { useTableColumns } from './composables/useTableColumns';
import { useTableActions } from './composables/useTableActions';

const { t } = useI18n();

// Component props with proper typing
const props = withDefaults(defineProps<MyScenariosTableProps>(), {
  params: undefined,
  total: 0,
  notify: undefined,
  refreshNotify: undefined
});

// Event emissions
// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'update:total', value: number): void;
  (e: 'update:deletedNotify', value: string): void;
}>();

const updateTotal = (total: number) => {
  emit('update:total', total);
};

// Inject project information
const projectId = inject<Ref<string>>('projectId', ref(''));

// Use composables for different concerns
const {
  tableData,
  loading,
  loaded,
  pagination,
  loadData,
  handleTableChange
} = useData(projectId, props.params, props.notify, props.refreshNotify, updateTotal);

const { columns, emptyTextStyle } = useTableColumns(props.params);

const {
  loading: actionLoading,
  handleDelete,
  handleUnfavorite,
  handleUnfollow
} = useTableActions(loadData, emit);

// Initialize watchers on component mount
onMounted(() => {
  watch(() => projectId, () => {
    loadData();
  }, { immediate: true });

  watch(() => props.notify, (newValue) => {
    if (newValue === undefined || newValue === null || newValue === '') {
      return;
    }

    loadData();
  }, { immediate: true });

  watch(() => props.refreshNotify, (newValue) => {
    if (newValue === undefined || newValue === null || newValue === '') {
      return;
    }

    pagination.value.current = getCurrentPage(pagination.value.current,
      pagination.value.pageSize, pagination.value.total);

    loadData();
  }, { immediate: true });
});

/**
 * Handle table change events and emit total updates
 */
const onTableChange = (params: TableChangeParams, filters: any, sorter: any) => {
  handleTableChange(params, filters, sorter);
  // Emit total update after data is loaded
  emit('update:total', pagination.value.total);
};

/**
 * Get appropriate action handler based on action key
 */
const getActionHandler = (actionKey: string | undefined, record: any) => {
  switch (actionKey) {
    case 'favouriteBy':
      return () => handleUnfavorite(record);
    case 'followBy':
      return () => handleUnfollow(record);
    default:
      return () => handleDelete(record);
  }
};

/**
 * Get action button title based on action key
 */
const getActionTitle = (actionKey: string | undefined) => {
  switch (actionKey) {
    case 'favouriteBy':
      return t('actions.cancelFavourite');
    case 'followBy':
      return t('actions.cancelFollow');
    default:
      return t('actions.delete');
  }
};

/**
 * Get action icon based on action key
 */
const getActionIcon = (actionKey: string | undefined) => {
  switch (actionKey) {
    case 'favouriteBy':
      return 'icon-quxiaoshoucang';
    case 'followBy':
      return 'icon-quxiaoguanzhu';
    default:
      return 'icon-qingchu';
  }
};
</script>

<template>
  <div>
    <!-- Loading state -->
    <div v-if="loading && !loaded" class="flex justify-center items-center h-32">
      <div class="text-theme-sub-content">{{ t('common.loading') }}</div>
    </div>

    <!-- Data content -->
    <template v-else-if="loaded">
      <!-- Empty state -->
      <template v-if="!tableData?.length">
        <div class="flex-1 flex flex-col items-center justify-center">
          <img class="w-27.5" src="../../../assets/images/nodata.png">
          <div class="flex items-center text-theme-sub-content text-3 leading-5">
            <template v-if="!!props.params?.createdBy">
              <span>{{ t('scenarioHome.myScenarios.table.messages.noAddedScenarios') }}</span>
              <RouterLink :to="`/scenario#${ScenarioMenuKey.SCENARIO}`" class="ml-1 link">
                {{ t('scenarioHome.myScenarios.actions.addScenario') }}
              </RouterLink>
            </template>

            <template v-else-if="!!props.params?.favouriteBy">
              <span>{{ t('scenarioHome.myScenarios.table.messages.noFavoritedScenarios') }}</span>
            </template>

            <template v-else-if="!!props.params?.followBy">
              <span>{{ t('scenarioHome.myScenarios.table.messages.noFollowedScenarios') }}</span>
            </template>
          </div>
        </div>
      </template>

      <!-- Data table -->
      <Table
        v-else
        :dataSource="tableData"
        :columns="columns as any"
        :pagination="pagination"
        :loading="loading || actionLoading"
        :emptyTextStyle="emptyTextStyle"
        :minSize="5"
        rowKey="id"
        size="small"
        noDataSize="small"
        :noDataText="t('common.noData')"
        @change="onTableChange">
        <!-- Custom cell renderers -->
        <template #bodyCell="{ record, column }">
          <!-- Scenario name with link -->
          <RouterLink
            v-if="column.dataIndex === 'name'"
            class="link truncate"
            :title="record.name"
            :to="`/scenario#${ScenarioMenuKey.SCENARIO}?id=${record.id}&name=${record.name}&plugin=${record.plugin}&type=detail`">
            {{ record.name }}
          </RouterLink>

          <!-- Script type display -->
          <div v-else-if="column.dataIndex === 'scriptType'" class="truncate">
            {{ record.scriptType?.message }}
          </div>

          <!-- Action buttons -->
          <div v-else-if="column.dataIndex === 'action'">
            <Button
              :title="getActionTitle(column.actionKey)"
              size="small"
              type="text"
              class="space-x-1 flex items-center py-0 px-1"
              @click="getActionHandler(column.actionKey, record)()">
              <Icon
                :icon="getActionIcon(column.actionKey)"
                class="text-3.5 cursor-pointer text-theme-text-hover" />
            </Button>
          </div>
        </template>
      </Table>
    </template>
  </div>
</template>

<style scoped>
.link {
  color: #1890ff;
  cursor: pointer;
}

:deep(.ant-pagination) {
  margin-bottom: 0;
}
</style>
