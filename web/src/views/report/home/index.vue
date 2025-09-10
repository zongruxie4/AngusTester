<script lang="ts" setup>
import { defineAsyncComponent, nextTick, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { AsyncComponent, Dropdown, Icon, Table } from '@xcan-angus/vue-ui';
import { Badge, Button, Popover } from 'ant-design-vue';
import { TESTER } from '@xcan-angus/infra';
import { ReportPermission, ReportStatus } from '@/enums/enums';

// Import composables
import { useReportData } from './composables/useReportData';
import { useTableColumns } from './composables/useTableColumns';

// Async components
const AuthorizeModal = defineAsyncComponent(() => import('@/components/AuthorizeModal/index.vue'));
const Summary = defineAsyncComponent(() => import('@/views/report/home/Introduce.vue'));
const SearchPanel = defineAsyncComponent(() => import('@/views/report/home/SearchPanel.vue'));
const PieChart = defineAsyncComponent(() => import('@/views/report/home/PieChart.vue'));
const AddReportModal = defineAsyncComponent(() => import('@/views/report/add/index.vue'));
const ViewReportModal = defineAsyncComponent(() => import('@/views/report/detail/index.vue'));
const GlobalAuthorizeModal = defineAsyncComponent(() => import('@/views/report/home/auth/index.vue'));
const CategorySelectList = defineAsyncComponent(() => import('@/views/report/home/CategorySelect.vue'));

const { t } = useI18n();

// Use composables
const {
  dataList,
  loading,
  pagination,
  category,
  selectId,
  selectReportPermissions,
  projectId,
  appInfo,
  userInfo,
  loadDataList,
  deleteReport,
  getShareToken,
  generateReport,
  addReport,
  viewReport,
  authFlagChange,
  searchPanelChange,
  categoryChange,
  onPageChange
} = useReportData();

const { columns, actionItems } = useTableColumns();

// Modal visibility states
const authModalVisible = ref(false);
const reportModalVisible = ref(false);
const viewModalVisible = ref(false);
const authVisible = ref(false);

/**
 * Handle action button clicks
 */
const actionClick = (action: string, report: any) => {
  selectId.value = report.id;
  switch (action) {
    case 'auth':
      authModalVisible.value = true;
      break;
    case 'delete':
      deleteReport(report);
      break;
    case 'share':
      getShareToken(report);
      break;
    case 'generate':
      generateReport(report);
      break;
  }
};

/**
 * Handle add report action
 */
const handleAddReport = (reportId?: string) => {
  nextTick(() => {
    addReport(reportId);
    reportModalVisible.value = true;
  });
};

/**
 * Handle view report action
 */
const handleViewReport = (report?: any) => {
  nextTick(() => {
    viewReport(report);
    viewModalVisible.value = true;
  });
};

/**
 * Handle open auth modal
 */
const openAuth = () => {
  nextTick(() => {
    authVisible.value = true;
  });
};

// Watch for category changes
watch(() => category.value, () => {
  categoryChange(category.value);
});
</script>

<template>
  <div class="p-5 flex flex-col h-full">
    <!-- Summary and Chart Section -->
    <div class="bg-gray-2 flex items-center rounded mb-5">
      <Summary />
      <PieChart :projectId="projectId" />
    </div>

    <!-- Search Panel Section -->
    <SearchPanel
      v-if="!!projectId && userInfo"
      :key="projectId"
      class="mt-3"
      :projectId="projectId || ''"
      :userInfo="userInfo || { id: '' }"
      :appInfo="appInfo || { id: '' }"
      :notify="''"
      @add="handleAddReport"
      @change="searchPanelChange"
      @openAuth="openAuth" />

    <!-- Main Content Section -->
    <div class="flex flex-1 mt-3 min-h-0 space-x-2">
      <!-- Category Selection -->
      <CategorySelectList
        v-model:category="category"
        class="w-50 h-full bg-gray-1" />

      <!-- Data Table -->
      <div class="flex-1 overflow-y-auto h-full">
        <Table
          :dataSource="dataList"
          :columns="columns"
          :pagination="pagination"
          :loading="loading"
          noDataSize="middle"
          :noDataText="t('common.noData')"
          @change="onPageChange">
          <template #bodyCell="{ record, column }">
            <!-- Report Name Column -->
            <template v-if="column.dataIndex === 'name'">
              <RouterLink
                v-if="record.status?.value === ReportStatus.SUCCESS"
                class="link truncate"
                target="_blank"
                :title="record.name"
                :to="`/report/${record.id}/preview`">
                {{ record.name }}
              </RouterLink>
            </template>

            <!-- Action Column -->
            <template v-if="column.dataIndex === 'action'">
              <div class="flex items-center">
                <Button
                  type="text"
                  size="small"
                  :disabled="!record.currentAuths.includes(ReportPermission.MODIFY)"
                  @click="handleAddReport(record.id)">
                  <Icon icon="icon-xiugai" class="mr-1" />
                  {{ t('actions.edit') }}
                </Button>
                <Button
                  type="text"
                  size="small"
                  @click="handleViewReport(record)">
                  <Icon icon="icon-chakanhuodong" class="mr-1" />
                  {{ t('reportHome.actions.view') }}
                </Button>
                <Dropdown
                  :menuItems="actionItems"
                  :permissions="record.currentAuths"
                  @click="actionClick($event.key, record)">
                  <Button
                    type="text"
                    size="small"
                    class="flex items-center px-0">
                    <Icon icon="icon-gengduo" />
                  </Button>
                </Dropdown>
              </div>
            </template>

            <!-- Status Column -->
            <template v-if="column.dataIndex === 'status'">
              <Badge v-if="record.status?.value === ReportStatus.PENDING" status="processing" />
              <Badge v-if="record.status?.value === ReportStatus.SUCCESS" status="success" />
              <Badge v-if="record.status?.value === ReportStatus.FAILURE" status="error" />
              {{ record.status?.message }}
              <template v-if="record.failureMessage">
                <Popover>
                  <template #content>
                    {{ record.failureMessage }}
                  </template>
                  <Icon icon="icon-tishi1" class="text-status-warn" />
                </Popover>
              </template>
            </template>
          </template>
        </Table>
      </div>
    </div>

    <!-- Modals -->
    <!-- Auth Modal -->
    <AsyncComponent :visible="authModalVisible">
      <AuthorizeModal
        v-model:visible="authModalVisible"
        enumKey="ReportPermission"
        :appId="appInfo?.id"
        :listUrl="`${TESTER}/report/auth?reportId=${selectId}`"
        :delUrl="`${TESTER}/report/auth`"
        :addUrl="`${TESTER}/report/${selectId}/auth`"
        :updateUrl="`${TESTER}/report/auth`"
        :enabledUrl="`${TESTER}/report/${selectId}/auth/enabled`"
        :initStatusUrl="`${TESTER}/report/${selectId}/auth/status`"
        :onTips="t('reportHome.permission.onTips')"
        :offTips="t('reportHome.permission.offTips')"
        :title="t('reportHome.modal.reportPermission')"
        @change="authFlagChange" />
    </AsyncComponent>

    <!-- Add/Edit Report Modal -->
    <AsyncComponent
      :visible="reportModalVisible"
      :key="`add-report-${selectId || 'new'}`">
      <AddReportModal
        v-model:visible="reportModalVisible"
        :reportId="selectId"
        @ok="loadDataList" />
    </AsyncComponent>

    <!-- View Report Modal -->
    <AsyncComponent
      :visible="viewModalVisible"
      :key="`view-report-${selectId || ''}`">
      <ViewReportModal
        v-model:visible="viewModalVisible"
        :reportId="selectId || ''"
        :permissions="selectReportPermissions" />
    </AsyncComponent>

    <!-- Global Auth Modal -->
    <AsyncComponent :visible="authVisible">
      <GlobalAuthorizeModal
        v-model:visible="authVisible"
        :projectId="projectId || ''"
        :appId="appInfo?.id || ''" />
    </AsyncComponent>
  </div>
</template>

<style scoped>
.link {
  color: #1890ff;
  cursor: pointer;
}
</style>
