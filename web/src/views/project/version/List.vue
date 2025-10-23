<script setup lang="ts">
import { defineAsyncComponent } from 'vue';
import { Button, Progress, Tag } from 'ant-design-vue';
import { AsyncComponent, Dropdown, Icon, NoData, Spin, Table } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import { RouterLink } from 'vue-router';
import { useVersionList } from './composables/useVersionList';
import { useTableColumns } from './composables/useTableColumns';
import type { VersionListProps } from './types';
import { ProjectMenuKey } from '@/views/project/menu';

// Component props with default values
const props = withDefaults(defineProps<VersionListProps>(), {
  projectId: undefined,
  userInfo: undefined,
  appInfo: undefined,
  notify: undefined,
  showDetail: true
});

const { t } = useI18n();

// Async components
const SearchPanel = defineAsyncComponent(() => import('@/views/project/version/SearchPanel.vue'));
const Introduce = defineAsyncComponent(() => import('@/views/project/version/Introduce.vue'));
const Edit = defineAsyncComponent(() => import('@/views/project/version/Edit.vue'));
const Merge = defineAsyncComponent(() => import('@/views/project/version/Merge.vue'));

// Use version list composable for data management
const {
  loaded,
  loading,
  searchedFlag,
  editVisible,
  selectVersionId,
  mergeVisible,
  pagination,
  dataList,
  statusColorConfig,
  refresh,
  searchChange,
  paginationChange,
  toDelete,
  editVersion,
  toMerge,
  changeStatus,
  handleMergeOk,
  getMenus
} = useVersionList(props);

// Use table columns composable for column configuration
const { columns } = useTableColumns();
</script>

<template>
  <div class="flex flex-col h-full overflow-auto px-5 py-5 leading-5 text-3">
    <div class="flex space-x-2">
      <Introduce
        class="flex-1"
        :class="{'mb-4': props.showDetail}"
        :showFunc="props.showDetail" />
    </div>

    <div class="flex items-center space-x-2">
      <span class="text-3.5 font-semibold mb-1.5">{{ t('version.list.addedVersions') }}</span>
    </div>
    <Spin :spinning="loading" class="flex-1 flex flex-col">
      <template v-if="loaded">
        <div v-if="!searchedFlag && dataList.length === 0" class="flex-1 flex flex-col items-center justify-center">
          <img src="../../../assets/images/nodata.png">
          <div class="flex items-center text-theme-sub-content text-3.5 leading-5 space-x-1">
            <span>{{ t('version.list.noVersions') }}</span>
            <Button type="link" @click="editVersion">{{ t('version.actions.addVersion') }}</Button>
          </div>
        </div>

        <template v-else>
          <SearchPanel
            @change="searchChange"
            @refresh="refresh"
            @toMerge="toMerge"
            @add="editVersion" />
          <NoData v-if="dataList.length === 0" class="flex-1" />
          <template v-else>
            <Table
              :columns="columns"
              :dataSource="dataList"
              :pagination="pagination"
              @change="paginationChange">
              <template #bodyCell="{column, record}">
                <template v-if="column.dataIndex === 'name'">
                  <RouterLink
                    v-if="props.showDetail"
                    class="router-link"
                    :title="record.name"
                    :to="`/project#${ProjectMenuKey.VERSION}?id=${record.id}`">
                    {{ record.name }}
                  </RouterLink>
                </template>
                <template v-if="column.dataIndex === 'status'">
                  <Tag :color="statusColorConfig[record.status?.value]">
                    {{ record.status?.message }}
                  </Tag>
                </template>
                <template v-if="column.dataIndex === 'description'">
                  <template v-if="record.description">{{ record.description }}</template>
                  <span v-else class="text-text-sub-content">--</span>
                </template>
                <template v-if="column.dataIndex === 'actions'">
                  <Button
                    type="text"
                    size="small"
                    @click="editVersion(record)">
                    <Icon icon="icon-bianji" class="mr-1" />
                    {{ t('actions.edit') }}
                  </Button>
                  <Button
                    type="text"
                    size="small"
                    @click="toDelete(record)">
                    <Icon icon="icon-qingchu" class="mr-1" />
                    {{ t('actions.delete') }}
                  </Button>
                  <Dropdown
                    noAuth
                    :menuItems="getMenus(record)"
                    @click="changeStatus($event, record)">
                    <Button size="small" type="text">
                      <Icon icon="icon-gengduo" />
                    </Button>
                  </Dropdown>
                </template>
                <template v-if="column.dataIndex === 'progress'">
                  <Progress size="small" :percent="record.progress?.completedRate || 0" />
                </template>
              </template>
            </Table>
          </template>
        </template>
      </template>
    </Spin>
    <AsyncComponent :visible="editVisible">
      <Edit
        v-model:visible="editVisible"
        :versionId="selectVersionId"
        :projectId="props.projectId"
        @ok="refresh" />
    </AsyncComponent>
    <AsyncComponent :visible="mergeVisible">
      <Merge
        v-model:visible="mergeVisible"
        :projectId="props.projectId"
        @ok="handleMergeOk" />
    </AsyncComponent>
  </div>
</template>

<style scoped>
.PENDING {
  background-color: rgba(45, 142, 255, 100%);
}

.IN_PROGRESS {
  background-color: rgba(103, 215, 255, 100%);
}

.COMPLETED {
  background-color: rgba(82, 196, 26, 100%);
}

.BLOCKED {
  background-color: rgba(255, 165, 43, 100%);
}

:deep(.ant-progress-outer) {
  width: 100px;
}

.router-link,
.link {
  color: #1890ff;
  cursor: pointer;
}

.link:hover {
  border-radius: 4px;
  background-color: rgba(239, 240, 243, 100%);
}

:deep(.ant-popover-inner-content) {
  padding: 8px 4px;
}
</style>
