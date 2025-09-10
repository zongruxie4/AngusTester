<script setup lang="ts">
import { computed, defineAsyncComponent, inject, onMounted, watch, ref, Ref } from 'vue';
import { Button } from 'ant-design-vue';
import { AsyncComponent, Drawer, Icon, IconRefresh, Input, Table } from '@xcan-angus/vue-ui';
import { STORAGE, appContext } from '@xcan-angus/infra';
import { useI18n } from 'vue-i18n';

import { FileCapacity, SpaceInfo } from './components';
import { useSpaceData } from './composables/useSpaceData';
import { useTableColumns } from './composables/useTableColumns';
import { useSpaceManagement } from './composables/useSpaceManagement';
import { useDrawerMenu } from './composables/useDrawerMenu';

const { t } = useI18n();

// Async component imports for better performance
const Share = defineAsyncComponent(() => import('@/views/data/file/share/index.vue'));
const EditSpaceModal = defineAsyncComponent(() => import('@/views/data/file/EditSpace.vue'));
const Shared = defineAsyncComponent(() => import('@/views/data/file/share/ShareList.vue'));
const GlobalAuth = defineAsyncComponent(() => import('@/views/data/file/auth/index.vue'));
const Introduce = defineAsyncComponent(() => import('@/views/data/file/Introduce.vue'));
const AuthorizeModal = defineAsyncComponent(() => import('@/components/AuthorizeModal/index.vue'));

// Dependency injection for app context
const appInfo = appContext.getAccessApp();
const isAdmin = inject('isAdmin', ref(false));
// Inject project information
const projectId = inject<Ref<string>>('projectId', ref(''));

// Application state
const isPrivate = ref(false);

// Composable usage for different concerns
const {
  pagination,
  dataList,
  tableLoading,
  keyword,
  selectedRowKey,
  selectedRow,
  getNameById,
  loadData,
  changePage,
  delConfirm,
  openSpace,
  handleRowSelect,
  getSafeAuth
} = useSpaceData();

const { spaceColumns } = useTableColumns();

const {
  editVisible,
  authModalVisible,
  globalAuthVisible,
  selectId,
  editSpace,
  createSpace,
  openAuthorizeModal,
  editAuth,
  authFlagChange,
  saveSpace
} = useSpaceManagement();

const {
  drawerRef,
  activeDrawerKey,
  dynamicDrawerMenu
} = useDrawerMenu();

/**
 * <p>Watch for project ID changes and reload data when project changes.</p>
 * <p>This ensures the space list is updated when switching between projects.</p>
 */
watch(() => projectId.value, (newValue) => {
  if (newValue) {
    pagination.current = 1;
    loadData(newValue, isAdmin.value);
  }
}, {
  immediate: true
});

/**
 * <p>Component lifecycle hook for initialization.</p>
 * <p>Sets up private edition flag and initial data loading.</p>
 */
onMounted(async () => {
  isPrivate.value = appContext.isPrivateEdition();
});

/**
 * <p>Computed drawer menu items based on selected row data.</p>
 * <p>Dynamically shows relevant menu items based on available information.</p>
 */
const drawerMenu = computed(() => {
  return dynamicDrawerMenu.value(selectedRow.value, selectedRowKey.value);
});
</script>

<template>
  <div class="flex h-full">
    <!-- Main content area -->
    <div class="p-5 flex-1 overflow-y-auto">
      <!-- Page title and introduction -->
      <div class="text-3.5 font-semibold mb-2.5">
        {{ t('fileSpace.title') }}
      </div>
      <Introduce />

      <!-- Added spaces section -->
      <div class="text-3.5 font-semibold mb-2.5 mt-4">
        {{ t('fileSpace.addedTitle') }}
      </div>

      <!-- Search and action bar -->
      <div class="flex justify-between pb-3">
        <!-- Search input -->
        <div class="w-75 flex-shrink-0">
          <Input
            v-model:value="keyword"
            :maxlength="100"
            allowClear
            :placeholder="t('fileSpace.searchPlaceholder')">
            <template #suffix>
              <Icon icon="icon-sousuo" class="text-theme-placeholder" />
            </template>
          </Input>
        </div>

        <!-- Action buttons -->
        <div class="flex items-center space-x-2.5">
          <!-- Generate data button -->
          <Button
            type="primary"
            size="small"
            href="/data/generate"
            class="flex space-x-1">
            <Icon icon="icon-shengchengshuju" />
            {{ t('fileSpace.buttons.generateData') }}
          </Button>

          <!-- Add space button -->
          <Button
            type="primary"
            size="small"
            class="flex space-x-1"
            @click="createSpace">
            <Icon icon="icon-create-script" />
            {{ t('fileSpace.buttons.addSpace') }}
          </Button>

          <!-- Space permission button -->
          <Button
            class="flex items-center"
            size="small"
            type="default"
            @click="openAuthorizeModal">
            <Icon icon="icon-quanxian1" class="mr-1" />
            <span>{{ t('fileSpace.buttons.spacePermission') }}</span>
          </Button>

          <!-- Refresh button -->
          <Button
            :disabled="tableLoading"
            class="flex items-center"
            size="small"
            type="default"
            @click="loadData(projectId, isAdmin)">
            <IconRefresh />
          </Button>
        </div>
      </div>

      <!-- Spaces table -->
      <Table
        rowKey="id"
        size="small"
        :columns="spaceColumns"
        :pagination="pagination"
        :dataSource="dataList"
        :loading="tableLoading"
        :rowClassName="(record) => record.id === selectedRowKey ? 'ant-table-row-selected' : ''"
        :customRow="(record) => ({ onClick: () => handleRowSelect(record as any) })"
        @change="changePage">
        <!-- Custom cell rendering -->
        <template #bodyCell="{ record, column }">
          <!-- Space name column -->
          <template v-if="column.dataIndex === 'name'">
            <div class="flex items-center">
              <Icon icon="icon-kongjian" class="flex-shrink-0 text-4 mr-2" />
              <div class="flex items-center w-75 space-name-wrapper">
                <span
                  class="cursor-pointer text-theme-text-hover"
                  @click.stop="openSpace(record)">
                  {{ record.name }}
                </span>
              </div>
            </div>
          </template>

          <!-- Quota size column -->
          <template v-if="column.dataIndex === 'quotaSize'">
            {{ record.quotaSize ? (record.quotaSize.value + record.quotaSize.unit.message) : '--' }}
          </template>

          <!-- Action column -->

          <template v-if="column.dataIndex === 'action'">
            <div class="space-x-2.5 flex items-center leading-4">
              <!-- Edit action -->
              <template v-if="getSafeAuth(record).includes('MODIFY')">
                <a class="whitespace-nowrap inline-flex items-center" @click.stop="editSpace(record.id)">
                  <Icon icon="icon-bianji" class="mr-0.5" />
                  {{ t('actions.edit') }}
                </a>
              </template>
              <template v-else>
                <span class="text-text-disabled whitespace-nowrap inline-flex items-center">
                  <Icon icon="icon-bianji" class="mr-0.5" />
                  {{ t('actions.edit') }}
                </span>
              </template>

              <!-- Permission action -->
              <template v-if="getSafeAuth(record).includes('GRANT')">
                <a class="whitespace-nowrap inline-flex items-center" @click.stop="editAuth(record)">
                  <Icon icon="icon-quanxian1" class="mr-0.5" />
                  {{ t('fileSpace.actions.permission') }}
                </a>
              </template>
              <template v-else>
                <span class="text-text-disabled whitespace-nowrap inline-flex items-center">
                  <Icon icon="icon-quanxian1" class="mr-0.5" />
                  {{ t('fileSpace.actions.permission') }}
                </span>
              </template>

              <!-- Delete action -->
              <template v-if="getSafeAuth(record).includes('DELETE')">
                <a class="whitespace-nowrap inline-flex items-center" @click.stop="delConfirm(record, projectId, isAdmin)">
                  <Icon icon="icon-qingchu" class="mr-0.5" />
                  {{ t('actions.delete') }}
                </a>
              </template>
              <template v-else>
                <span class="text-text-disabled whitespace-nowrap inline-flex items-center">
                  <Icon icon="icon-qingchu" class="mr-0.5" />
                  {{ t('actions.delete') }}
                </span>
              </template>
            </div>
          </template>
        </template>
      </Table>
    </div>

    <!-- Right sidebar drawer -->
    <Drawer
      v-show="selectedRow"
      ref="drawerRef"
      v-model:activeKey="activeDrawerKey"
      :menuItems="drawerMenu">
      <!-- Basic info tab -->
      <template #info>
        <SpaceInfo
          v-if="activeDrawerKey === 'info'"
          :id="selectedRowKey"
          type="space" />
      </template>

      <!-- Space capacity tab -->
      <template #size>
        <FileCapacity
          v-if="activeDrawerKey === 'size'"
          :id="selectedRowKey" />
      </template>

      <!-- Share tab -->
      <template #share>
        <Shared
          v-if="activeDrawerKey === 'share'"
          :id="selectedRowKey"
          :name="getNameById" />
      </template>
    </Drawer>

    <!-- Async modals -->
    <AsyncComponent :visible="editVisible">
      <EditSpaceModal
        :id="selectId"
        v-model:visible="editVisible"
        @ok="(form) => saveSpace(form, projectId, () => loadData(projectId, isAdmin))" />
    </AsyncComponent>

    <AsyncComponent :visible="authModalVisible">
      <AuthorizeModal
        v-model:visible="authModalVisible"
        enumKey="SpacePermission"
        :appId="appInfo?.id"
        :listUrl="`${STORAGE}/space/auth?spaceId=${selectId}`"
        :delUrl="`${STORAGE}/space/auth`"
        :addUrl="`${STORAGE}/space/${selectId}/auth`"
        :updateUrl="`${STORAGE}/space/auth`"
        :enabledUrl="`${STORAGE}/space/${selectId}/auth/enabled`"
        :initStatusUrl="`${STORAGE}/space/${selectId}/auth/status`"
        :onTips="t('fileSpace.permissionModal.onTips')"
        :offTips="t('fileSpace.permissionModal.offTips')"
        :title="t('fileSpace.permissionModal.title')"
        @change="(authData) => authFlagChange(authData, dataList)" />
    </AsyncComponent>

    <AsyncComponent :visible="globalAuthVisible">
      <GlobalAuth v-model:visible="globalAuthVisible" :appId="appInfo?.id" />
    </AsyncComponent>
  </div>
</template>

<style scoped>
.space-name-wrapper {
  @apply truncate;
}
</style>
