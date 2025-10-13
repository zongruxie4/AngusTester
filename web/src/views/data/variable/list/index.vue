<script setup lang="ts">
import { defineAsyncComponent, inject, onMounted } from 'vue';
import { Button } from 'ant-design-vue';
import { AsyncComponent, Dropdown, Icon, IconCopy, NoData, Spin, Table } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import { BasicProps } from '@/types/types';

// Import composables
import { useVariableList } from './composables/useVariableList';
import { useActions } from './composables/useActions';
import { useValuePreview } from './composables/useValuePreview';
import { useTableColumns } from './composables/useTableColumns';
import { useDropdownMenus } from './composables/useDropdownMenus';

// Import components
import SearchPanel from './SearchPanel.vue';

const { t } = useI18n();

// Component props
const props = withDefaults(defineProps<BasicProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  notify: undefined
});

// Inject dependencies
const deleteTabPane = inject<(keys: string[]) => void>('deleteTabPane', () => ({}));

// Async components
const Introduce = defineAsyncComponent(() => import('./Introduce.vue'));
const ImportModal = defineAsyncComponent(() => import('./Import.vue'));
const ExportModal = defineAsyncComponent(() => import('../export/index.vue'));

// Use composables
const {
  loaded,
  loading,
  searchedFlag,
  tableData,
  pagination,
  rowSelection,
  selectedCount,
  loadData,
  handleTableChange,
  handleSearchPanelChange,
  refresh,
  initializeRowSelection,
  cancelBatchDelete
} = useVariableList(props);

const {
  visibilityIdSet,
  errorMessageMap,
  showVariableValue,
  hideVariableValue,
  isValueVisible,
  getErrorMessage,
  resetPreviewState
} = useValuePreview();

const {
  columns
} = useTableColumns();

const {
  buttonDropdownMenuItems,
  tableDropdownMenuItems
} = useDropdownMenus();

const {
  importVariableModalVisible,
  exportVariableModalVisible,
  exportVariableId,
  navigateToCreateStaticVariable,
  handleButtonDropdownClick,
  showImportModal,
  handleImportSuccess,
  showExportModal,
  deleteVariable,
  navigateToEdit,
  handleTableDropdownClick,
  executeBatchDelete
} = useActions(
  props.projectId,
  deleteTabPane,
  loadData,
  {
    current: pagination.value.current,
    pageSize: pagination.value.pageSize,
    total: pagination.value.total
  },
  rowSelection.value,
  visibilityIdSet.value,
  errorMessageMap.value
);

/**
 * Handle table row selection for batch operations
 */
const handleBatchDelete = () => {
  initializeRowSelection();
  executeBatchDelete(rowSelection.value);
};

/**
 * Handle table change events (pagination, sorting)
 */
const handleTableChangeEvent = (paginationInfo: any, _filters: any, sorter: any) => {
  handleTableChange(
    paginationInfo.current,
    paginationInfo.pageSize,
    sorter
  );
};

/**
 * Handle search panel changes
 */
const handleSearchPanelChangeEvent = (data: any) => {
  handleSearchPanelChange(data);
};

/**
 * Handle refresh action
 */
const handleRefresh = () => {
  resetPreviewState();
  refresh();
};

/**
 * Handle import success
 */
const handleImportSuccessEvent = () => {
  handleImportSuccess();
};

/**
 * Handle export action
 */
const handleExport = (id?: string) => {
  showExportModal(id);
};

// Initialize visibility states for password variables
onMounted(() => {
  if (tableData.value.length > 0) {
    tableData.value.forEach(item => {
      if (!item.passwordValue) {
        visibilityIdSet.value.add(item.id);
      }
    });
  }
});

defineExpose({
  loadData
});
</script>

<template>
  <div class="flex flex-col h-full overflow-auto px-5 py-5 leading-5 text-3">
    <!-- Introduction Section -->
    <Introduce class="mb-7" />

    <!-- Page Title -->
    <div class="text-3.5 font-semibold mb-1">
      {{ t('variable.list.addedTitle') }}
    </div>

    <!-- Main Content -->
    <Spin
      :spinning="loading"
      style="height: calc(100% - 41px);"
      class="flex flex-col">
      <template v-if="loaded">
        <!-- No Data State -->
        <div
          v-if="!searchedFlag && tableData.length === 0"
          class="flex-1 flex flex-col items-center justify-center">
          <img src="../../../../assets/images/nodata.png" alt="No Data">
          <div class="flex items-center text-theme-sub-content text-3.5 leading-7">
            <span>{{ t('common.noData') }}</span>

            <Dropdown
              :menuItems="buttonDropdownMenuItems"
              @click="handleButtonDropdownClick">
              <Button
                type="link"
                size="small"
                class="text-3.5 py-0 px-0 mx-1"
                @click="navigateToCreateStaticVariable">
                <span>{{ t('variable.actions.static') }}</span>
              </Button>
            </Dropdown>

            <span>{{ t('variable.list.or') }}</span>

            <Button
              type="link"
              size="small"
              class="text-3.5 py-0 px-0 mx-1"
              @click="showImportModal">
              <span>{{ t('variable.actions.uploadVariable') }}</span>
            </Button>
          </div>
        </div>

        <!-- Data Table State -->
        <template v-else>
          <!-- Search Panel -->
          <SearchPanel
            width="260"
            :selectedNum="selectedCount"
            @to-import="showImportModal"
            @to-export="handleExport()"
            @to-cancel-batch-delete="cancelBatchDelete"
            @to-batch-delete="handleBatchDelete"
            @change="handleSearchPanelChangeEvent"
            @refresh="handleRefresh" />

          <!-- No Data After Search -->
          <NoData v-if="tableData.length === 0" class="flex-1" />

          <!-- Data Table -->
          <Table
            v-else
            storageKey="variableList"
            :dataSource="tableData"
            :columns="columns"
            :pagination="pagination"
            :rowSelection="rowSelection"
            rowKey="id"
            class="flex-1"
            @change="handleTableChangeEvent">
            <!-- Custom Cell Renderers -->
            <template #bodyCell="{ column, record }">
              <!-- Name Column -->
              <div v-if="column.dataIndex === 'name'" class="flex items-center">
                <RouterLink
                  class="link flex-1 truncate"
                  :title="record.name"
                  :to="`/data#${DataMenuKey.VARIABLES}?id=${record.id}`">
                  {{ record.name }}
                </RouterLink>
                <IconCopy :copyText="`{${record.name}}`" class="ml-1.5" />
              </div>

              <template v-if="column.dataIndex === 'description'">
                <span v-if="!record.description" class="text-text-sub-content">--</span>
              </template>

              <!-- Value Column -->
              <template v-if="column.dataIndex === 'value'">
                <div v-if="record.passwordValue" class="flex items-center">
                  <template v-if="isValueVisible(record.id)">
                    <div :title="record.value" class="flex-1 truncate">
                      {{ record.value }}
                    </div>
                    <IconCopy
                      :disabled="!record.value"
                      :copyText="record.value"
                      class="ml-1.5" />
                    <Icon
                      icon="icon-zhengyan"
                      class="flex-shrink-0 ml-1.5 text-4 cursor-pointer text-theme-text-hover"
                      @click="hideVariableValue(record)" />
                  </template>
                  <template v-else>
                    <div class="flex-1 truncate">******</div>
                    <IconCopy
                      :disabled="!record.value"
                      :copyText="record.value"
                      class="ml-1.5" />
                    <Icon
                      icon="icon-biyan"
                      class="flex-shrink-0 ml-1.5 text-4 cursor-pointer text-theme-text-hover"
                      @click="showVariableValue(record)" />
                  </template>
                </div>

                <div v-else class="flex items-center">
                  <div
                    v-if="getErrorMessage(record.id)"
                    :title="getErrorMessage(record.id)"
                    class="flex-1 truncate text-status-error">
                    {{ getErrorMessage(record.id) }}
                  </div>

                  <div
                    v-else
                    :title="record.value"
                    class="flex-1 truncate">
                    {{ record.value }}
                  </div>

                  <IconCopy
                    :disabled="!record.value"
                    :copyText="record.value"
                    class="ml-1.5" />
                  <Icon
                    icon="icon-zhengyan"
                    class="flex-shrink-0 ml-1.5 text-4 cursor-pointer text-theme-text-hover"
                    @click="showVariableValue(record)" />
                </div>
              </template>

              <!-- Action Column -->
              <div v-else-if="column.dataIndex === 'action'" class="flex items-center">
                <Button
                  type="text"
                  size="small"
                  class="flex items-center px-0 mr-2"
                  @click="navigateToEdit(record)">
                  <Icon icon="icon-shuxie" class="mr-1 text-3.5" />
                  <span>{{ t('actions.edit') }}</span>
                </Button>

                <Button
                  type="text"
                  size="small"
                  class="flex items-center px-0 mr-2"
                  @click="deleteVariable(record)">
                  <Icon icon="icon-qingchu" class="mr-1 text-3.5" />
                  <span>{{ t('actions.delete') }}</span>
                </Button>

                <Dropdown
                  :menuItems="tableDropdownMenuItems"
                  @click="handleTableDropdownClick($event, record)">
                  <Button
                    type="text"
                    size="small"
                    class="flex items-center px-0">
                    <Icon icon="icon-gengduo" />
                  </Button>
                </Dropdown>
              </div>
            </template>
          </Table>
        </template>
      </template>
    </Spin>

    <!-- Import Modal -->
    <AsyncComponent :visible="importVariableModalVisible">
      <ImportModal
        v-model:visible="importVariableModalVisible"
        :projectId="props.projectId"
        @ok="handleImportSuccessEvent" />
    </AsyncComponent>

    <!-- Export Modal -->
    <AsyncComponent :visible="exportVariableModalVisible">
      <ExportModal
        v-if="exportVariableId"
        :id="[exportVariableId]"
        v-model:visible="exportVariableModalVisible"
        :projectId="props.projectId" />
      <ExportModal
        v-else
        v-model:visible="exportVariableModalVisible"
        :projectId="props.projectId" />
    </AsyncComponent>
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
