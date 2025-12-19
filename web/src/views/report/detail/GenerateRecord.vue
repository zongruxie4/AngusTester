<script lang="ts" setup>
import { onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Icon, modal, notification, Table } from '@xcan-angus/vue-ui';
import { Badge, Button } from 'ant-design-vue';
import { EnumMessage } from '@xcan-angus/infra';
import { ReportCategory, ReportStatus } from '@/enums/enums';
import { report } from '@/api/tester';

const { t } = useI18n();

// Component props definition
interface Props {
  reportId: string;
  projectId: string;
  permissions: string[];
}

type Record = {
  reportName: string;
  id: string;
  status: EnumMessage<ReportStatus>;
  failureMessage?: string;
  creator: string;
  category: EnumMessage<ReportCategory>;
};

const props = withDefaults(defineProps<Props>(), {
  reportId: '',
  permissions: () => []
});

// Pagination configuration
const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0
});

// Table columns configuration
const columns = [
  {
    key: 'id',
    dataIndex: 'id',
    title: t('reportHome.reportDetail.generateRecord.reportName')
  },
  {
    key: 'reportName',
    dataIndex: 'reportName',
    title: t('reportHome.reportDetail.generateRecord.reportName')
  },
  {
    key: 'category',
    dataIndex: 'category',
    title: t('common.type'),
    customRender: ({ text }) => {
      return text?.message;
    }
  },
  {
    key: 'template',
    dataIndex: 'template',
    title: t('reportHome.reportDetail.generateRecord.template'),
    customRender: ({ text }) => {
      return text?.message;
    }
  },
  {
    key: 'creator',
    dataIndex: 'creator',
    title: t('common.creator')
  },
  {
    key: 'createdDate',
    dataIndex: 'createdDate',
    title: t('reportHome.reportDetail.generateRecord.generateTime')
  },
  {
    key: 'action',
    dataIndex: 'action',
    title: t('common.actions')
  }
];

// Record data
const recordData = ref<Record[]>([]);

/**
 * Load report generation records
 * Fetches records based on project ID and report ID
 */
const loadRecord = async () => {
  const [error, { data }] = await report.getReportRecord({
    projectId: props.projectId,
    reportId: props.reportId
  });
  if (error) {
    return;
  }
  recordData.value = data.list || [];
  pagination.value.total = data.total || 0;
};

/**
 * Handle pagination change
 * @param page - Pagination information
 */
const changePage = (page) => {
  pagination.value.current = page.current;
  pagination.value.pageSize = page.pageSize;
  loadRecord();
};

/**
 * Delete a report generation record
 * Shows confirmation dialog before deletion
 * @param record - Record to be deleted
 */
const delRecord = (record: Record) => {
  modal.confirm({
    content: t('actions.tips.confirmDelete', { name: record.reportName }),
    onOk () {
      return report.deleteReportRecord([record.id])
        .then((resp) => {
          const [error] = resp;
          if (error) {
            return;
          }
          // Adjust current page if needed
          if (pagination.value.current !== 1 && recordData.value.length === 1) {
            pagination.value.current -= 1;
          }
          loadRecord();
          notification.success(t('actions.tips.deleteSuccess'));
        });
    }
  });
};

/**
 * Lifecycle hook - Initialize component
 * Watch for report ID changes and load records
 */
onMounted(() => {
  watch(() => props.reportId, (newValue) => {
    newValue && loadRecord();
  }, {
    immediate: true
  });
});
</script>
<template>
  <Table
    :dataSource="recordData"
    :columns="columns"
    :pagination="pagination"
    size="small"
    noDataSize="small"
    @change="changePage">
    <template #bodyCell="{record, column}">
      <template v-if="column.dataIndex === 'reportName'">
        <RouterLink
          class="link truncate"
          target="_blank"
          :title="record.name"
          :to="`/report/${props.reportId}?recordId=${record.id}`">
          {{ record.reportName }}
        </RouterLink>
      </template>
      <template v-if="column.dataIndex === 'status'">
        <Badge v-if="record.status?.value === ReportStatus.PENDING" status="processing" />
        <Badge v-if="record.status?.value === ReportStatus.SUCCESS" status="success" />
        <Badge v-if="record.status?.value === ReportStatus.FAILURE" status="error" />
        {{ record.status?.message }}
        <template v-if="record.failureMessage">
          ({{ record.failureMessage }})
        </template>
      </template>
      <template v-if="column.dataIndex === 'action'">
        <Button
          type="text"
          size="small"
          :disabled="!props.permissions.includes('DELETE')"
          @click="delRecord(record)">
          <Icon icon="icon-qingchu" class="mr-1" />
          {{ t('actions.delete') }}
        </Button>
      </template>
    </template>
  </Table>
</template>
<style scoped>
.link {
  color: #1890ff;
  cursor: pointer;
}
</style>
