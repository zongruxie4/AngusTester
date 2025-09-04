<script setup lang="ts">
import { defineAsyncComponent, onBeforeUnmount, onMounted, ref } from 'vue';
import { useRoute } from 'vue-router';
import { Button } from 'ant-design-vue';
import { AsyncComponent, Drawer, Icon, Input, modal, notification, Table } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';

import { fileApi, space } from '@/api/storage';
import { columns, CrumbType, SearchType, SortType, SourceType, SPACE_PERMISSIONS } from './types';
import { FileCapacity, FileCrumb, FileIcon, FileSearch, FileSort, FileUpload, SpaceInfo } from './components';
import { useFileManagement } from './composables/useFileManagement';
import { useFileRename } from './composables/useFileRename';
import { useDrawerMenu } from './composables/useDrawerMenu';

const { t } = useI18n();

// Type for target selection
type TargetType = 'file' | 'directory' | undefined;

// Async component imports for better performance
const Share = defineAsyncComponent(() => import('@/views/data/file/share/index.vue'));
const MoveModal = defineAsyncComponent(() => import('./components/Move.vue'));

// Route information
const route = useRoute();

// Composable usage for different concerns
const {
  loading,
  parentDirectoryId,
  spaceId,
  spaceName,
  state,
  targetId,
  targetType,
  isShowUpload,
  moveVisible,
  moveIds,
  shareAuth,
  downloadAuth,
  editAuth,
  deleteAuth,
  rowSelection,
  showAddDirectory,
  onSelectChange,
  customRow,
  getActionAuth,
  getList,
  getFileData,
  create,
  createBlur,
  createEnter,
  sureAdd,
  delConfirm,
  delFile,
  search,
  sort,
  copyDownloadUrl,
  downConfirm,
  compressFile,
  downSingleFile,
  getFileIcon,
  changePage,
  uploadFile,
  closeUpload,
  handleMove,
  handleMultiMove,
  confirmMove,
  loadPath,
  jumpPath
} = useFileManagement();

const {
  createInputRef,
  rename,
  renameBlur,
  renameEnter
} = useFileRename();

const {
  drawerRef,
  activeDrawerKey,
  fileDrawerMenu
} = useDrawerMenu();

// Share and operation states
const shareVisible = ref(false);
const shareIds = ref<string[]>([]);

/**
 * <p>Show delete confirmation modal for files.</p>
 * <p>Displays confirmation dialog before deleting selected files.</p>
 *
 * @param fileList - List of file IDs to delete
 */
const showDeleteConfirm = (fileList = state.selectedRowKeys) => {
  modal.confirm({
    centered: true,
    content: t('fileSpace.fileManagement.messages.deleteConfirm'),
    onOk: () => {
      delFile(fileList);
    }
  });
};

/**
 * <p>Open side drawer for file information.</p>
 * <p>Sets target file and opens drawer with file details.</p>
 *
 * @param record - File record to show details for
 */
const openSide = (record: SourceType) => {
  targetId.value = record.id;
  targetType.value = record.type.value.toLowerCase() as TargetType;
  drawerRef.value.open('info');
};

/**
 * <p>Remove temporary directory creation row from data source.</p>
 */
const removeTempRow = () => {
  state.dataSource.shift();
};

/**
 * <p>Create directory from temporary row data.</p>
 *
 * @param record - Temporary directory record
 */
const createDirectory = async (record: SourceType) => {
  const params = {
    name: record.name as string,
    parentDirectoryId: parentDirectoryId.value === '-1' ? undefined : parentDirectoryId.value,
    spaceId: spaceId.value
  };

  const [error] = await space.addDirectory(params);
  if (error) {
    return;
  }

  notification.success(t('fileSpace.fileManagement.messages.addDirectorySuccess'));
  await getList();
};

/**
 * <p>Component lifecycle hook for initialization.</p>
 * <p>Sets up route parameters, initial data loading, and authorization.</p>
 */
onMounted(() => {
  if (route.params.id) {
    spaceId.value = route.params.id as string || '-1';
    spaceName.value = route.query.spaceName as string;

    // Restore parent directory ID from session storage
    const parentId = sessionStorage.getItem('parentDirectoryId');
    if (parentId && +parentId > -1) {
      parentDirectoryId.value = parentId;
    } else {
      state.crumb.push({ name: spaceName.value, id: '-1' });
    }

    getList();
    getActionAuth();
  }
});

/**
 * <p>Component lifecycle hook for cleanup.</p>
 * <p>Removes session storage data when component unmounts.</p>
 */
onBeforeUnmount(() => {
  sessionStorage.removeItem('parentDirectoryId');
});
</script>

<template>
  <div class="flex text-theme-content h-full">
    <!-- Main content area -->
    <div class="pl-10 pr-10 pb-12 h-full min-w-0 flex-1">
      <!-- Header with breadcrumb and action buttons -->
      <div class="flex justify-between items-center h-16.25">
        <!-- Breadcrumb navigation -->
        <FileCrumb :crumb="state.crumb" @jump="jumpPath" />

        <!-- Action buttons -->
        <div class="flex items-center text-3">
          <!-- Create directory button -->
          <FileIcon
            v-if="showAddDirectory"
            :title="t('fileSpace.fileManagement.directory')"
            icon="icon-chuangjianwenjianjia"
            @click="create" />

          <!-- Upload button -->
          <FileIcon
            v-if="editAuth"
            :title="t('actions.upload')"
            icon="icon-shangchuanxiao"
            @click="uploadFile" />

          <!-- Delete button -->
          <FileIcon
            v-if="state.selectedRowKeys?.length > 0 && deleteAuth"
            :title="t('actions.delete')"
            icon="icon-qingchu"
            class="text-3.5"
            @click="showDeleteConfirm()" />

          <!-- Move button -->
          <FileIcon
            v-if="state.selectedRowKeys?.length > 0 && editAuth"
            :title="t('actions.move')"
            icon="icon-yidong"
            @click="handleMultiMove" />

          <!-- Search component -->
          <file-search :spaceId="spaceId" @search="search" />

          <!-- Sort component -->
          <file-sort @sort="sort" />

          <!-- Refresh button -->
          <FileIcon
            :title="t('actions.refresh')"
            icon="icon-shuaxin"
            @click="getList" />
        </div>
      </div>

      <!-- File upload component -->
      <FileUpload
        v-if="isShowUpload"
        :parentDirectoryId="parentDirectoryId"
        @close="closeUpload"
        @success="getList" />

      <!-- Files and directories table -->
      <Table
        :pagination="state.pagination"
        size="small"
        :loading="loading"
        rowKey="id"
        :columns="columns"
        :rowClassName="(record) => record.id === targetId ? 'ant-table-row-target' : ''"
        :customRow="customRow"
        :dataSource="state.dataSource"
        :rowSelection="rowSelection"
        @change="changePage">
        <!-- Custom cell rendering -->
        <template #bodyCell="{ column, text, record }">
          <!-- Name column with icon and actions -->
          <template v-if="column.dataIndex == 'name'">
            <div class="flex items-center">
              <div class="flex items-center flex-1 truncate">
                <Icon :icon="getFileIcon(record)" class="mr-2.5 flex-shrink-0 text-3.5" />

                <!-- Existing file/directory name -->
                <div
                  v-if="record.id !== '-1'"
                  :class="{flex: !!record.renameFlag}"
                  class="flex-1 items-center flex-shrink-0 truncate">
                  <template v-if="!record.renameFlag">
                    <span
                      class="cursor-pointer text-theme-text-hover"
                      :title="text"
                      @click.stop="getFileData(record)">{{ text }}</span>
                  </template>

                  <!-- Rename input field -->
                  <template v-else>
                    <Input
                      :id="record.id"
                      v-model:value="record.name"
                      :disabled="!record.renameFlag"
                      :allowClear="false"
                      :maxlength="400"
                      trimAll
                      @keyup.enter="renameEnter"
                      @blur="renameBlur(record)" />
                  </template>
                </div>

                <!-- Directory creation input field -->
                <Input
                  v-if="record.id === '-1'"
                  ref="createInputRef"
                  v-model:value="record.name"
                  trimAll
                  :placeholder="t('fileSpace.fileManagement.createDirectory')"
                  :maxlength="400"
                  @click="(e) => e.stopPropagation()"
                  @keyup.enter="createEnter"
                  @blur="createBlur(record, state.dataSource, removeTempRow, createDirectory)" />
              </div>
            </div>
          </template>

          <!-- File count column -->
          <template v-if="column.dataIndex === 'fileNum'">
            <span>{{ record.type.value === 'DIRECTORY' ? record.summary.subFileNum : '--' }}</span>
          </template>

          <!-- Subdirectory count column -->
          <template v-if="column.dataIndex === 'subDirectoryNum'">
            <span>{{ record.type.value === 'DIRECTORY' ? record.summary.subDirectoryNum : '--' }}</span>
          </template>

          <!-- Size column -->
          <template v-if="column.dataIndex == 'size'">
            <span>{{ record.summary.usedSize }}</span>
          </template>

          <!-- Action column -->
          <template v-if="column.dataIndex === 'action'">
            <div
              v-show="record.id !== '-1'"
              class="text-3 whitespace-nowrap">

              <!-- Delete action -->
              <Button
                :disabled="!deleteAuth"
                type="text"
                size="small"
                class="!h-6"
                @click.stop="showDeleteConfirm([record.id])">
                <Icon icon="icon-qingchu" class="align-text-bottom mr-0.5" />
                {{ t('fileSpace.fileManagement.fileActions.delete') }}
              </Button>

              <!-- Rename action -->
              <Button
                :disabled="!editAuth"
                type="text"
                size="small"
                class="!h-6"
                @click.stop="rename(record)">
                <Icon icon="icon-bianji" class="align-text-bottom mr-0.5" />
                {{ t('fileSpace.fileManagement.fileActions.rename') }}
              </Button>

              <!-- Move action -->
              <Button
                :disabled="!editAuth"
                type="text"
                size="small"
                class="!h-6"
                @click.stop="handleMove(record)">
                <Icon icon="icon-yidong" class="align-text-bottom mr-0.5" />
                {{ t('fileSpace.fileManagement.fileActions.move') }}
              </Button>

              <!-- Details action -->
              <!-- <Button
                type="text"
                size="small"
                class="!h-6"
                @click.stop="openSide(record)">
                <Icon icon="icon-fuwuxinxi" class="align-text-bottom mr-0.5" />
                {{ t('fileSpace.fileManagement.fileActions.details') }}
              </Button>-->

              <!-- Download action -->
              <Button
                :disabled="!downloadAuth || record.type.value !== 'FILE'"
                type="text"
                size="small"
                class="!h-6"
                @click.stop="downConfirm([record.id])">
                <Icon icon="icon-daochu" class="align-text-bottom mr-0.5" />
                {{ t('fileSpace.fileManagement.fileActions.download') }}
              </Button>

              <!-- Share link action -->
              <Button
                :disabled="!downloadAuth || record.type.value !== 'FILE'"
                type="text"
                size="small"
                class="!h-6"
                @click.stop="copyDownloadUrl(record)">
                <Icon icon="icon-fuzhi" class="align-text-bottom mr-0.5" />
                {{ t('fileSpace.fileManagement.fileActions.shareLink') }}
              </Button>
            </div>
          </template>
        </template>
      </Table>

      <!-- Async modals -->
      <AsyncComponent :visible="shareVisible">
        <Share
          v-model:visible="shareVisible"
          :spaceId="spaceId"
          :spaceName="spaceName"
          :defaultIds="shareIds" />
      </AsyncComponent>

      <AsyncComponent :visible="moveVisible">
        <MoveModal
          v-model:visible="moveVisible"
          :moveIds="moveIds"
          @ok="confirmMove" />
      </AsyncComponent>
    </div>

    <!-- Right sidebar drawer -->
    <Drawer
      v-show="!!targetId"
      ref="drawerRef"
      v-model:activeKey="activeDrawerKey"
      :menuItems="fileDrawerMenu">
      <!-- File information tab -->
      <template #info>
        <SpaceInfo :id="targetId" :type="targetType" />
      </template>

      <!-- File capacity tab -->
      <template #size>
        <FileCapacity :id="targetId" />
      </template>
    </Drawer>
  </div>
</template>

<style scoped>
.ant-input-affix-wrapper {
  @apply w-72.5 absolute left-10;
}

.ant-popover-inner-content {
  @apply py-2 px-0;
}

.disabled.ant-input-affix-wrapper-disabled {
  @apply border-none bg-transparent cursor-default;
}

.disabled.ant-input-affix-wrapper-disabled:hover {
  border-color: transparent;
}

:deep(.ant-table-wrapper .ant-table-tbody>tr.ant-table-row-selected>td) {
  background: #f7f8fb;
}

:deep(.ant-table-wrapper .ant-table-tbody>tr.ant-table-row-target>td) {
  background: #e6f7ff;
}

.disabled.ant-input-affix-wrapper-disabled :deep(.ant-input) {
  @apply cursor-default text-ellipsis select-none;
}

button.ant-btn-text {
  @apply !bg-transparent;
}
</style>
