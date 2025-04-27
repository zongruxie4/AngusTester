<script lang="ts" setup>
import { onMounted, ref, watch } from 'vue';
import { http, TESTER } from '@xcan-angus/tools';
import { Icon, modal, notification, Table } from '@xcan-angus/vue-ui';
import { Badge, Button } from 'ant-design-vue';

interface Props {
  reportId: string;
  projectId: string;
  permissions: string[];

}
type EnumFieldNames = {
  message: string,
  value: string
}

type Record = {
  reportName: string;
  id: string;
  status: EnumFieldNames;
  failureMessage?: string;
  createdByName: string;
  category: EnumFieldNames;
}

const props = withDefaults(defineProps<Props>(), {
  reportId: '',
  permissions: () => []
});

const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0
});

const columns = [
  {
    dataIndex: 'id',
    title: 'ID'
  },
  {
    dataIndex: 'reportName',
    title: '报告名称'
  },

  {
    dataIndex: 'category',
    title: '类型',
    customRender: ({ text }) => {
      return text?.message;
    }
  },
  {
    dataIndex: 'template',
    title: '模板',
    customRender: ({ text }) => {
      return text?.message;
    }
  },
  {
    dataIndex: 'createdByName',
    title: '添加人'
  },
  {
    dataIndex: 'createdDate',
    title: '生成时间'
  },
  {
    dataIndex: 'action',
    title: '操作'
  }
];

const recordData = ref<Record[]>([]);

const loadRecord = async () => {
  const [error, { data }] = await http.get(`${TESTER}/report/record`, {
    projectId: props.projectId,
    reportId: props.reportId
  });
  if (error) {
    return;
  }
  recordData.value = data.list || [];
  pagination.value.total = data.total || 0;
};

const changePage = (page) => {
  pagination.value.current = page.current;
  pagination.value.pageSize = page.pageSize;
  loadRecord();
};

// 删除记录
const delRecord = (record: Record) => {
  modal.confirm({
    title: '删除报告记录',
    content: `确认删除报告记录【${record.reportName}】吗？`,
    onOk () {
      return http.del(`${TESTER}/report/record`, [record.id])
        .then((resp) => {
          const [error] = resp;
          if (error) {
            return;
          }
          if (pagination.value.current !== 1 && recordData.value.length === 1) {
            pagination.value.current -= 1;
          }
          loadRecord();
          notification.success('删除成功');
        });
    }
  });
};

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
        <Badge v-if="record.status?.value === 'PENDING'" status="processing" />
        <Badge v-if="record.status?.value === 'SUCCESS'" status="success" />
        <Badge v-if="record.status?.value === 'FAILURE'" status="error" />
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
          删除
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
