<script lang="ts" setup>
import { inject, onMounted } from 'vue';
import { Button } from 'ant-design-vue';
import { Icon, Image, Input, Spin, Table } from '@xcan-angus/vue-ui';

import { TrashProps } from './types';
import { useTrashData } from './composables/useData';
import { useTableColumns } from './composables/useTableColumns';

/**
 * <p>Trash component for managing deleted project items.</p>
 * <p>Provides functionality to view, recover, and permanently delete items from project trash.</p>
 */
const props = withDefaults(defineProps<TrashProps>(), {
  projectId: undefined,
  userInfo: undefined,
  notify: undefined
});

// Inject admin status from parent component
const isAdmin = inject('isAdmin', false);

// Initialize composables for data management and table configuration
const {
  loading, loaded, tableData, inputValue, pagination,
  backTrash, deleteTrash, backAll, deleteAll,
  toRefresh, inputChange, tableChange, watchProjectId, watchNotify
} = useTrashData(props.projectId, props.userInfo, isAdmin);

const { columns, emptyTextStyle } = useTableColumns();

/**
 * <p>Component lifecycle hook for setting up watchers and initial data loading.</p>
 * <p>Watches for project ID and notification changes to refresh the trash list.</p>
 */
onMounted(() => {
  // Watch for project ID changes and refresh list
  watchProjectId();

  // Watch for notification changes and refresh list
  watchNotify(props.notify);
});
</script>

<template>
  <!-- Main container with loading spinner and padding -->
  <Spin :spinning="loading" class="h-full px-5 py-5 overflow-auto text-3">
    <!-- Header section with search and action buttons -->
    <div class="flex items-center justify-between mb-3.5">
      <!-- Left side: Search input and admin hint -->
      <div class="flex items-center">
        <!-- Search input with debounced change handler -->
        <Input
          v-model:value="inputValue"
          :allowClear="true"
          :maxlength="200"
          trim
          :placeholder="$t('projectTrash.ui.searchPlaceholder')"
          class="w-75"
          @change="inputChange">
          <template #suffix>
            <Icon class="text-3.5 cursor-pointer text-theme-content" icon="icon-sousuo" />
          </template>
        </Input>

        <!-- Admin hint message -->
        <div class="flex-1 truncate text-theme-sub-content space-x-1 ml-2">
          <Icon icon="icon-tishi1" class="text-3.5 text-tips" />
          <span>{{ $t('projectTrash.ui.adminHint') }}</span>
        </div>
      </div>

      <!-- Right side: Action buttons -->
      <div class="space-x-2.5">
        <!-- Recover all items button -->
        <Button
          :disabled="!tableData?.length"
          size="small"
          type="primary"
          @click="backAll">
          <Icon icon="icon-zhongzhi" class="text-3.5 mr-1" />
          <span>{{ $t('projectTrash.ui.recoverAll') }}</span>
        </Button>

        <!-- Delete all items button -->
        <Button
          :disabled="!tableData?.length"
          size="small"
          type="primary"
          danger
          @click="deleteAll">
          <Icon icon="icon-qingchu" class="text-3.5 mr-1" />
          <span>{{ $t('projectTrash.ui.deleteAll') }}</span>
        </Button>

        <!-- Refresh button -->
        <Button
          size="small"
          type="default"
          @click="toRefresh">
          <Icon icon="icon-shuaxin" class="text-3.5 mr-1" />
          <span>{{ $t('actions.refresh') }}</span>
        </Button>
      </div>
    </div>

    <!-- Data table with conditional rendering -->
    <Table
      v-if="loaded"
      :dataSource="tableData"
      :columns="columns"
      :pagination="pagination"
      :emptyTextStyle="emptyTextStyle"
      rowKey="id"
      size="small"
      v-bind="{} as any"
      @change="tableChange">
      <!-- Custom cell rendering for specific columns -->
      <template #bodyCell="{ record, column }">
        <!-- Deleter avatar and name cell -->
        <div
          v-if="column.dataIndex === 'deletedByName'"
          :title="record.deletedByName"
          class="flex items-center overflow-hidden">
          <div class="flex items-center flex-shrink-0 w-5 h-5 rounded-xl overflow-hidden mr-2">
            <Image
              :src="record.deletedByAvatar"
              type="avatar"
              class="w-full" />
          </div>
          <div class="flex-1 truncate">{{ record.deletedByName }}</div>
        </div>

        <!-- Creator avatar and name cell -->
        <div
          v-else-if="column.dataIndex === 'createdByName'"
          :title="record.createdByName"
          class="flex items-center overflow-hidden">
          <div class="flex items-center flex-shrink-0 w-5 h-5 rounded-xl overflow-hidden mr-2">
            <Image
              :src="record.createdByAvatar"
              type="avatar"
              class="w-full" />
          </div>
          <div class="flex-1 truncate">{{ record.createdByName }}</div>
        </div>

        <!-- Action buttons cell -->
        <div v-else-if="column.dataIndex === 'action'" class="flex items-center space-x-2.5">
          <!-- Recover item button -->
          <Button
            :disabled="record.disabled"
            :title="$t('projectTrash.ui.recover')"
            size="small"
            type="text"
            class="space-x-1 flex items-center p-0"
            @click="backTrash(record)">
            <Icon icon="icon-zhongzhi" class="cursor-pointer text-theme-text-hover" />
          </Button>

          <!-- Delete item button -->
          <Button
            :disabled="record.disabled"
            :title="$t('actions.delete')"
            size="small"
            type="text"
            class="space-x-1 flex items-center p-0"
            @click="deleteTrash(record)">
            <Icon icon="icon-qingchu" class="text-3.5 cursor-pointer text-theme-text-hover" />
          </Button>
        </div>
      </template>
    </Table>
  </Spin>
</template>
