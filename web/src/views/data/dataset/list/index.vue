<script setup lang="ts">
import { defineAsyncComponent, inject } from 'vue';
import { Button } from 'ant-design-vue';
import { AsyncComponent, Dropdown, Icon, IconCopy, modal, NoData, Spin, Table } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';

import { DataSetDetail } from '../types';
import { BasicProps } from '@/types/types';

// Import composables
import { useDatasetList } from './composables/useDatasetList';
import { useTableColumns } from './composables/useTableColumns';
import { useActions } from './composables/useActions';
import { useModals } from './composables/useModals';
import { useNavigation } from './composables/useNavigation';
import { useDropdownMenus } from './composables/useDropdownMenus';

import SearchPanel from '@/views/data/dataset/list/SearchPanel.vue';

// Internationalization
const { t } = useI18n();

const props = withDefaults(defineProps<BasicProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  notify: undefined
});

// Injected dependencies
const deleteTabPane = inject<(keys: string[]) => void>('deleteTabPane', () => ({}));

// Async components
const Introduce = defineAsyncComponent(() => import('@/views/data/dataset/list/Introduce.vue'));
const ImportModal = defineAsyncComponent(() => import('@/views/data/dataset/list/Import.vue'));
const ExportModal = defineAsyncComponent(() => import('@/views/data/dataset/export/index.vue'));
const PreviewDataModal = defineAsyncComponent(() => import('@/views/data/dataset/list/PreviewDataModal.vue'));

// Composables usage
const {
  loaded,
  loading,
  searchedFlag,
  pagination,
  tableData,
  loadData,
  handleTableChange,
  handleSearchPanelChange,
  refresh,
  deleteDataset,
  cloneDataset
} = useDatasetList(props, deleteTabPane);

const { columns } = useTableColumns();

const {
  rowSelection,
  selectedNum,
  initBatchDelete,
  cancelBatchDelete,
  executeBatchDelete
} = useActions(tableData, pagination, loadData, deleteTabPane, t);

const {
  previewDataSetModalVisible,
  importDataSetModalVisible,
  exportDataSetModalVisible,
  selectedData,
  exportDataSetId,
  openPreviewModal,
  openImportModal,
  openExportModal,
  handleImportSuccess
} = useModals();

const {
  toCreateStaticDataSet,
  handleCreationDropdownClick,
  toEditDataset
} = useNavigation();

const {
  buttonDropdownMenuItems,
  tableDropdownMenuItems,
  handleTableDropdownClick
} = useDropdownMenus();

// Event handlers
/**
 * Handle dataset deletion
 * Shows confirmation dialog before deleting a dataset
 *
 * @param data - Dataset item to delete
 */
const toDelete = (data: DataSetDetail) => {
  modal.confirm({
    content: t('actions.tips.confirmDelete', { name: data.name }),
    async onOk () {
      await deleteDataset(data, t);
    }
  });
};

/**
 * Handle dataset cloning
 * Clones a dataset and refreshes the list
 *
 * @param data - Dataset item to clone
 */
const toClone = async (data: DataSetDetail) => {
  await cloneDataset(data, t);
};

/**
 * Initialize batch delete mode
 * Enables batch selection for multiple datasets
 */
const toBatchDelete = () => {
  initBatchDelete();
};

/**
 * Cancel batch delete mode
 * Disables batch selection mode
 */
const toCancelBatchDelete = () => {
  cancelBatchDelete();
};

/**
 * Execute batch delete operation
 * Performs deletion of all selected datasets
 */
const toExecuteBatchDelete = () => {
  executeBatchDelete(loading);
};

/**
 * Handle import success
 * Refreshes the dataset list after successful import
 */
const importOk = () => {
  handleImportSuccess(pagination, loadData);
};

/**
 * Handle table dropdown menu clicks
 * Routes actions to appropriate handlers based on menu item
 *
 * @param menuItem - Selected menu item
 * @param data - Dataset item associated with the action
 */
const tableDropdownClick = (menuItem: { key: 'preview' | 'export' | 'clone' }, data: DataSetDetail) => {
  handleTableDropdownClick(menuItem, data, openPreviewModal, openExportModal, toClone);
};
</script>

<template>
  <div class="flex flex-col h-full overflow-auto px-5 py-5 leading-5 text-3">
    <Introduce class="mb-7" />

    <div class="text-3.5 font-semibold mb-1">{{ t('dataset.list.addedTitle') }}</div>

    <Spin
      :spinning="loading"
      style="height: calc(100% - 41px);"
      class="flex flex-col">
      <template v-if="loaded">
        <div
          v-if="!searchedFlag && tableData.length === 0"
          class="flex-1 flex flex-col items-center justify-center">
          <img src="../../../../assets/images/nodata.png" :alt="t('dataset.list.noDataImageAlt')">
          <div class="flex items-center text-theme-sub-content text-3.5 leading-7">
            <span>{{ t('dataset.list.noDataText') }}</span>

            <Dropdown
              :menuItems="buttonDropdownMenuItems"
              @click="handleCreationDropdownClick">
              <Button
                type="link"
                size="small"
                class="text-3.5 py-0 px-0 mx-1"
                @click="toCreateStaticDataSet">
                <span>{{ t('dataset.actions.addStaticDataset') }}</span>
              </Button>
            </Dropdown>

            <span>{{ t('dataset.list.or') }}</span>

            <Button
              type="link"
              size="small"
              class="text-3.5 py-0 px-0 mx-1"
              @click="openImportModal">
              <span>{{ t('dataset.actions.uploadDataset') }}</span>
            </Button>
          </div>
        </div>

        <template v-else>
          <SearchPanel
            :selectedNum="selectedNum"
            width="260"
            @change="handleSearchPanelChange"
            @to-import="openImportModal"
            @to-export="openExportModal()"
            @to-cancel-batch-delete="toCancelBatchDelete"
            @to-batch-delete="toBatchDelete"
            @to-execute-batch-delete="toExecuteBatchDelete"
            @refresh="refresh" />
          <NoData v-if="tableData.length === 0" class="flex-1" />

          <Table
            v-else
            :dataSource="tableData"
            :columns="columns"
            :pagination="pagination"
            :rowSelection="rowSelection"
            rowKey="id"
            class="flex-1"
            noDataSize="small"
            :noDataText="t('common.noData')"
            @change="handleTableChange">
            <template #bodyCell="{ column, record }">
              <div v-if="column.dataIndex === 'name'" class="flex items-center">
                <RouterLink
                  class="link flex-1 truncate"
                  :title="record.name"
                  :to="`/data#dataSet?id=${record.id}`">
                  {{ record.name }}
                </RouterLink>
                <IconCopy :copyText="`{${record.name}}`" class="ml-1.5" />
              </div>

              <template v-if="column.dataIndex === 'description'">
                <span v-if="!record.description" class="text-text-sub-content">--</span>
              </template>

              <div v-else-if="column.dataIndex === 'action'" class="flex items-center">
                <Button
                  type="text"
                  size="small"
                  class="flex items-center px-0 mr-2"
                  @click="() => toEditDataset(record.id)">
                  <Icon icon="icon-shuxie" class="mr-1 text-3.5" />
                  <span>{{ t('actions.edit') }}</span>
                </Button>

                <Button
                  type="text"
                  size="small"
                  class="flex items-center px-0 mr-2"
                  @click="() => toDelete(record)">
                  <Icon icon="icon-qingchu" class="mr-1 text-3.5" />
                  <span>{{ t('actions.delete') }}</span>
                </Button>

                <Dropdown :menuItems="tableDropdownMenuItems" @click="(menuItem) => tableDropdownClick(menuItem, record)">
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

    <AsyncComponent :visible="previewDataSetModalVisible">
      <PreviewDataModal
        v-model:visible="previewDataSetModalVisible"
        :projectId="props.projectId"
        :dataSource="selectedData || undefined" />
    </AsyncComponent>

    <AsyncComponent :visible="importDataSetModalVisible">
      <ImportModal
        v-model:visible="importDataSetModalVisible"
        :projectId="props.projectId"
        @ok="importOk" />
    </AsyncComponent>

    <AsyncComponent :visible="exportDataSetModalVisible">
      <ExportModal
        :id="exportDataSetId"
        v-model:visible="exportDataSetModalVisible"
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
