<script lang="ts" setup>
import { computed, inject, onMounted, ref, watch } from 'vue';
import { Table } from '@xcan-angus/vue-ui';
import { mock } from '@/api/tester';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

interface Props {
  nodeId: string
}

const projectInfo = inject('projectInfo', ref({ id: '' }));
const projectId = computed(() => {
  return projectInfo.value?.id;
});

const props = withDefaults(defineProps<Props>(), {
  nodeId: ''
});
const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0,
  pageSizeOptions: [10]
});

const columns = [
  {
    dataIndex: 'id',
    title: t('node.nodeDetail.mockService.columns.serviceId')
  },
  {
    dataIndex: 'name',
    title: t('node.nodeDetail.mockService.columns.name')
  },
  {
    dataIndex: 'status',
    title: t('node.nodeDetail.mockService.columns.status')
  },
  {
    dataIndex: 'serviceHostUrl',
    title: t('node.nodeDetail.mockService.columns.accessUrl')
  },
  {
    dataIndex: 'servicePort',
    title: t('node.nodeDetail.mockService.columns.accessPort')
  },
  {
    dataIndex: 'createdByName',
    title: t('node.nodeDetail.mockService.columns.creator')
  },
  {
    dataIndex: 'createdDate',
    title: t('node.nodeDetail.mockService.columns.createTime')
  }
];
// 服务ID、名称、状态、访问地址、端口、添加人、添加时间
const servieData = ref([]);
const loadServiceData = async () => {
  const { current, pageSize } = pagination.value;
  const [error, { data = { list: [], total: 0 } }] = await mock.getServiceList({ nodeId: props.nodeId, projectId: projectId.value, pageNo: current, pageSize });
  if (error) {
    return;
  }
  servieData.value = data.list || [];
  pagination.value.total = +data.total || 0;
};

const onPageChange = (page) => {
  pagination.value.current = page.current;
  pagination.value.pageSize = page.pageSize;
  loadServiceData();
};
onMounted(() => {
  watch(() => projectId.value, (newValue) => {
    if (newValue) {
      if (props.nodeId) {
        loadServiceData();
      }
    }
  }, {
    immediate: true
  });
});

</script>
<template>
  <Table
    :dataSource="servieData"
    :columns="columns"
    :pagination="pagination"
    size="small"
    noDataSize="small"
    @change="onPageChange">
    <template #bodyCell="{ column, record }">
      <template v-if="column.dataIndex === 'status'">
        {{ record.status?.message }}
      </template>
    </template>
  </Table>
</template>
