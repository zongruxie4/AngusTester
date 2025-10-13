<script setup lang='ts'>
import { defineAsyncComponent, nextTick, onBeforeUnmount, onMounted, ref, watch, inject } from 'vue';
import { useI18n } from 'vue-i18n';
import { Spin as ASpin, Tooltip } from 'ant-design-vue';
import { AsyncComponent, Dropdown, Colon, Icon, IconCopy, modal, notification, Spin, Table } from '@xcan-angus/vue-ui';
import { useRoute } from 'vue-router';
import router from '@/router';
import { TESTER, appContext } from '@xcan-angus/infra';
import { MockServicePermission, MockServiceStatus } from '@/enums/enums';
import { ApiMenuKey } from '@/views/apis/types';

// Import composables
import { useMockData } from './composables/useMockData';
import { useMockActions } from './composables/useMockActions';
import { useMockUI } from './composables/useMockUI';

// Import types
import type { MockService } from './types';
import { mock } from '@/api/tester';

// Import components
const AuthorizeModal = defineAsyncComponent(() => import('@/components/AuthorizeModal/index.vue'));
const Introduce = defineAsyncComponent(() => import('@/views/apis/mock/Introduce.vue'));
const Export = defineAsyncComponent(() => import('@/views/apis/mock/Export.vue'));
const SearchPanel = defineAsyncComponent(() => import('@/views/apis/mock/SearchPanel.vue'));

// Use composables
const { t } = useI18n();
const route = useRoute();
const appInfo = appContext.getAccessApp();

// Use mock data composable
const mockData = useMockData();
const {
  loading,
  tableData,
  pagination,
  projectId,
  fetchList,
  handleSearchChange,
  handleTableChange
} = mockData;

// Use mock actions composable
const mockActions = useMockActions(mockData, projectId);
const {
  rouSelection,
  batchStart,
  batchDelete,
  handleDelete,
  forceDelete,
  handleRefresh,
  handleUpdateStatus,
  clearAllTimers
} = mockActions;

// Use mock UI composable
const mockUI = useMockUI();
const {
  exportVisible,
  exportData,
  authVisible,
  authData,
  searchOptions,
  columns,
  actionMenus,
  statusStyleMap,
  indicator,
  handleMenuClick,
  handleResetInstance,
  handleExport,
  openAuth,
  handleAuthFlagChange
} = mockUI;

// Template refs
const searchPanelRef = ref();

// Lifecycle hooks
onMounted(() => {
  watch(() => projectId.value, newValue => {
    if (newValue) {
      if (route.query.sid) {
        nextTick(() => {
          searchPanelRef.value.setConfigs([{ valueKey: 'name', value: route.query.sid }]);
        });
        router.replace(`/apis#${ApiMenuKey.MOCK}`);
      } else {
        fetchList();
      }
    }
  }, {
    immediate: true
  });
});

onBeforeUnmount(() => {
  clearAllTimers();
});

// Event handlers
const handleClick = (key: string, record: MockService) => {
  handleMenuClick(
    key,
    record,
    openAuth,
    handleExport,
    (item) => handleResetInstance(item, loading, modal, mock, notification),
    forceDelete
  );
};

const authFlagChange = ({ auth }: { auth: boolean }) => {
  handleAuthFlagChange({ auth }, tableData, authData);
};
</script>

<template>
  <Spin :spinning="loading" class="w-full h-full py-5 px-5 overflow-y-auto flex flex-col">
    <Introduce class="mb-5" />
    <SearchPanel
      ref="searchPanelRef"
      :options="searchOptions"
      :width="284"
      :loading="loading"
      @change="handleSearchChange"
      @batch-start="batchStart"
      @batch-delete="batchDelete"
      @export="handleExport"
      @refresh="handleRefresh" />
    <Table
      rowKey="id"
      :columns="columns"
      :dataSource="tableData"
      :rowSelection="rouSelection"
      :pagination="pagination"
      @change="handleTableChange">
      <template #bodyCell="{ column, text, record }">
        <template v-if="column.dataIndex === 'name'">
          <template v-if="(!record.auth || record.currentAuthsValue.includes(MockServicePermission.VIEW))">
            <RouterLink
              :to="`/mockservice/${record.id}/apis`"
              class="text-text-link hover:text-text-link-hover cursor-pointer break-all">
              {{ text }}
            </RouterLink>
          </template>
          <template v-else>
            {{ text }}
          </template>
        </template>
        <template v-if="column.dataIndex === 'nodeName'">
          <RouterLink
            :to="`/node/detail/${record.nodeId}`"
            class="text-text-link hover:text-text-link-hover cursor-pointer break-all">
            {{ text }}
          </RouterLink>
        </template>
        <template v-if="column.dataIndex === 'serviceHostUrl'">
          <div>
            <div class="flex items-start">
              <span>{{ text }}</span>
              <span :title="t('actions.copy')">
                <IconCopy class="ml-2 -mt-0.5 text-3.5" :copyText="text" />
              </span>
            </div>
            <div v-if="text && record?.serviceDomainUrl">
              <span>{{ record?.serviceDomainUrl }}</span>
              <span :title="t('actions.copy')">
                <IconCopy class="ml-2 -mt-0.5 text-3.5" :copyText="record?.serviceDomainUrl" />
              </span>
            </div>
          </div>
        </template>
        <template v-if="column.dataIndex === 'status'">
          <div class="flex items-center">
            <div class="flex items-center">
              <div class="w-1.5 h-1.5 rounded-xl mr-1" :style="'background-color:' + statusStyleMap[text.value]"></div>
              <span>{{ text?.message || "--" }}</span>
            </div>
            <template v-if="text.value === 'STARTING'">
              <ASpin :indicator="indicator" class="mx-1" />
            </template>
            <template v-if="record?.failTips">
              <Tooltip
                placement="topLeft"
                arrowPointAtCenter
                :overlayStyle="{'max-width': '400px'}">
                <template #title>
                  <div class="flex items-center text-text-content">
                    <span>
                      {{ record?.failTips.message }}
                      <span
                        v-if="record.failTips?.exitCode !== null || record.failTips?.exitCode !== ''"
                        class="ml-2">
                        ({{ t('mock.exitCode') }}<Colon class="mr-1" />{{ record.failTips.exitCode }})
                      </span>
                    </span>
                  </div>
                  <div v-if="record.failTips?.console?.length">
                    <div class="mt-2">{{ t('mock.console') }}<Colon /></div>
                    <div
                      v-for="(item, index) in record.failTips.console"
                      :key="index"
                      class="leading-4 mt-1">
                      {{ item }}
                    </div>
                  </div>
                </template>
                <Icon icon="icon-tishi1" class="ml-1 text-3.5 text-status-error1 cursor-pointer" />
              </Tooltip>
            </template>
          </div>
        </template>
        <template v-if="column.dataIndex === 'source'">
          {{ record.source?.message }}
        </template>
        <template v-if="column.dataIndex === 'action'">
          <div class="flex items-center">
            <a
              v-if="(!record.auth || record.currentAuthsValue.includes(MockServicePermission.RUN)) &&
                record.status?.value !== 'STARTING'"
              class="ml-2 cursor-pointer flex items-center text-3"
              @click="handleUpdateStatus(record)">
              <Icon
                :icon="record.status?.value !== MockServiceStatus.RUNNING ? 'icon-qidong' : 'icon-zhongzhi2'"
                class="mr-1" />
              {{ record.status?.value !== MockServiceStatus.RUNNING ? t('actions.start') : t('actions.stop') }}
            </a>
            <a
              v-else
              class="ml-2 flex items-center text-3"
              disabled>
              <Icon
                :icon="record.status?.value !== MockServiceStatus.RUNNING ? 'icon-qidong' : 'icon-zhongzhi2'"
                class="mr-1" />
              {{ record.status?.value !== MockServiceStatus.RUNNING ? t('actions.start') : t('actions.stop') }}
            </a>
            <a
              v-if="(!record.auth || record.currentAuthsValue.includes(MockServicePermission.DELETE)) &&
                record.status?.value === MockServiceStatus.NOT_STARTED"
              class="mx-2 cursor-pointer flex items-center"
              @click="handleDelete([record.id])">
              <Icon icon="icon-qingchu" class="mr-1 text-3.5" />{{ t('actions.delete') }}
            </a>
            <a
              v-else
              class="mx-2 flex items-center"
              disabled>
              <Icon icon="icon-qingchu" class="mr-1 text-3.5" />{{ t('actions.delete') }}
            </a>
            <Dropdown
              :admin="false"
              :menuItems="actionMenus"
              :permissions="record.currentAuthsValue"
              @click="handleClick($event.key, record)">
              <Icon icon="icon-gengduo" class="cursor-pointer outline-none items-center" />
            </Dropdown>
          </div>
        </template>
      </template>
    </Table>
  </Spin>
  <AsyncComponent :visible="exportVisible">
    <Export
      v-model:visible="exportVisible"
      :mockService="exportData"
      type="APIS" />
  </AsyncComponent>
  <AsyncComponent :visible="authVisible">
    <AuthorizeModal
      v-model:visible="authVisible"
      :enumKey="MockServicePermission"
      :appId="appInfo?.id"
      :listUrl="`${TESTER}/mock/service/${authData?.id}/auth`"
      :delUrl="`${TESTER}/mock/service/auth`"
      :addUrl="`${TESTER}/mock/service/${authData?.id}/auth`"
      :updateUrl="`${TESTER}/mock/service/auth`"
      :enabledUrl="`${TESTER}/mock/service/${authData?.id}/auth/enabled`"
      :initStatusUrl="`${TESTER}/mock/service/${authData?.id}/auth/status`"
      :onTips="t('mock.auth.onTips')"
      :offTips="t('mock.auth.offTips')"
      :title="t('mock.auth.title')"
      @change="authFlagChange" />
  </AsyncComponent>
</template>
