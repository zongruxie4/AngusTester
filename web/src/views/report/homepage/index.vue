<script lang="ts" setup>
import { computed, defineAsyncComponent, inject, ref, watch } from 'vue';
import { AsyncComponent, AuthorizeModal, Dropdown, Icon, modal, notification, Table } from '@xcan-angus/vue-ui';
import { Badge, Button, Popover } from 'ant-design-vue';
import { clipboard, TESTER } from '@xcan-angus/tools';
import { report } from '@/api/tester';

import { getCurrentPage } from '@/utils/utils';

const Summary = defineAsyncComponent(() => import('@/views/report/homepage/summary/index.vue'));
const SearchPanel = defineAsyncComponent(() => import('@/views/report/homepage/searchPanel/index.vue'));
const PieChart = defineAsyncComponent(() => import('@/views/report/homepage/pieChart/index.vue'));
const AddReportModal = defineAsyncComponent(() => import('@/views/report/add/index.vue'));
const ViewReportModal = defineAsyncComponent(() => import('@/views/report/detail/index.vue'));
const GlobalAuthorizeModal = defineAsyncComponent(() => import('@/views/report/homepage/globalAuth/index.vue'));
const CategorySelectList = defineAsyncComponent(() => import('@/views/report/homepage/categorySelect/index.vue'));

type OrderByKey = 'createdDate' | 'createdByName';
type OrderSortKey = 'ASC' | 'DESC';

type Report = {
  id: string;
  name: string;
  version: string;
  category: {
    value: string;
    message: string;
  };
  template: {
    value: string;
    message: string;
  };
}

const appInfo = inject('appInfo');
const tenantInfo = inject('tenantInfo');
const projectInfo = inject('projectInfo', ref({ id: '', type: { value: '' } }));

const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0
});
const loading = ref(false);
const orderBy = ref<OrderByKey>();
const orderSort = ref<OrderSortKey>();
const filters = ref<{ key: string; op: string; value: boolean | string | string[]; }[]>([]);
const category = ref();
const dataList = ref<Report[]>([]);
const selectId = ref();
const selectReportPermissions = ref([]);
const authModalVisible = ref(false);
const reportModalVisible = ref(false);
const viewModalVisible = ref(false);
const authVisible = ref(false);

const toSort = (data: { orderBy: OrderByKey; orderSort: OrderSortKey; }) => {
  orderBy.value = data.orderBy;
  orderSort.value = data.orderSort;
  pagination.value.current = 1;
  loadDataList();
};

const searchPanelChange = (data: { key: string; op: string; value: boolean | string | string[]; }[]) => {
  filters.value = data;
  pagination.value.current = 1;
  loadDataList();
};

const onPageChange = (page, _, sortData) => {
  pagination.value.current = page.current;
  pagination.value.pageSize = page.pageSize;

  orderBy.value = sortData.orderBy;
  orderSort.value = sortData.orderSort;
  loadDataList();
};

watch(() => category.value, () => {
  pagination.value.current = 1;
  loadDataList();
});

const openAuth = () => {
  authVisible.value = true;
};

const authFlagChange = async ({ auth }: { auth: boolean }) => {
  const _list = dataList.value;
  const targetId = selectId.value;
  for (let i = 0, len = _list.length; i < len; i++) {
    if (_list[i].id === targetId) {
      _list[i].auth = auth;
      break;
    }
  }
};

const loadDataList = async () => {
  if (loading.value || !projectId.value) {
    return;
  }

  const { current, pageSize } = pagination.value;
  const params: {
    pageNo: number;
    pageSize: number;
    projectId: string;
    filters?: {
      key: string;
      op: string;
      value: boolean | string | string[];
    }[];
    orderBy?: OrderByKey;
    orderSort?: OrderSortKey;
  } = {
    pageNo: current,
    pageSize,
    projectId: projectId.value,
    filters: []
  };

  if (filters.value?.length) {
    params.filters = filters.value;
  }
  if (category.value) {
    params.filters?.push({ key: 'category', op: 'EQUAL', value: category.value });
  }

  if (orderSort.value) {
    params.orderBy = orderBy.value;
    params.orderSort = orderSort.value;
  }

  loading.value = true;
  const [error, { data = {} }] = await report.searchList(params);
  loading.value = false;
  if (error) {
    return;
  }
  dataList.value = (data.list || []).map(i => ({
    ...i,
    currentAuths: i.currentAuths.map(auth => auth.value)
  }));
  pagination.value.total = +data.total || 0;
  if (dataList.value.length === 0 && pagination.value.current !== 1) {
    pagination.value.current = 1;
    loadDataList();
  }
};

const actionClick = (action, report) => {
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

// 删除
const deleteReport = (report) => {
  modal.confirm({
    title: '删除报告',
    content: `确认删除报告【${report.name}】吗？`,
    onOk () {
      return report.delete([report.id]).then((resp) => {
        const [error] = resp;
        if (error) {
          return;
        }

        pagination.value.current = getCurrentPage(pagination.value.current, pagination.value.pageSize, pagination.value.total);
        loadDataList();
        notification.success('删除成功');
      });
    }
  });
};

// 获取分享 getShareToken
const getShareToken = async (report) => {
  const [error, { data }] = await report.getShareToken(report.id);
  if (error) {
    notification.error('获取token失败');
    return;
  }
  const url = data;
  await clipboard.toClipboard(url);
  notification.success('已复制分享链接到剪贴板');
};

const addReport = (reportId?: string) => {
  if (reportId) {
    selectId.value = reportId;
  } else {
    selectId.value = undefined;
  }
  reportModalVisible.value = true;
};

const viewReport = (report) => {
  if (report) {
    selectId.value = report.id;
    selectReportPermissions.value = report.currentAuths || [];
  } else {
    selectId.value = undefined;
    selectReportPermissions.value = [];
  }
  viewModalVisible.value = true;
};

const generateLoading = ref({});

const generateReport = async (report) => {
  generateLoading.value[report.id] = true;
  const [error] = await report.generateReport(report.id);
  generateLoading.value[report.id] = false;
  if (error) {
    return;
  }
  notification.success('正在生成报告');
  loadDataList();
};
const projectId = computed(() => {
  return projectInfo.value?.id;
});

const actionItems = [
  {
    name: '立即生成',
    key: 'generate',
    icon: 'icon-guanlijiedian',
    permission: 'GENERATE'
  },
  {
    name: '权限',
    key: 'auth',
    icon: 'icon-quanxian1',
    permission: 'GRANT'
  },
  {
    name: '删除',
    key: 'delete',
    icon: 'icon-qingchu',
    permission: 'DELETE'
  }
];

const columns = [
  {
    dataIndex: 'name',
    title: '名称',
    sorter: true
  },
  {
    dataIndex: 'version',
    title: '版本',
    width: 70,
    sorter: true
  },
  {
    dataIndex: 'template',
    title: '模板',
    customRender: ({ text }) => {
      return text?.message;
    },
    width: 160
  },
  {
    dataIndex: 'status',
    title: '状态',
    customRender: ({ text }) => {
      return text?.message;
    },
    width: 80
  },
  {
    dataIndex: 'targetName',
    title: '资源名称',
    groupName: 'resource',
    hide: false
  },
  {
    dataIndex: 'targetType',
    title: '资源类型',
    groupName: 'resource',
    hide: true,
    customRender: ({ text }) => {
      return text?.message;
    }
  },
  {
    dataIndex: 'targetId',
    title: '资源ID',
    groupName: 'resource',
    hide: true
  },
  {
    dataIndex: 'description',
    title: '描述',
    ellipsis: true
  },
  {
    dataIndex: 'createdBy',
    title: '添加人',
    groupName: 'creat',
    hide: false,
    width: 90,
    sorter: true,
    customRender: ({ record }) => record.createdByName
  },
  {
    dataIndex: 'createdDate',
    title: '添加时间',
    groupName: 'creat',
    hide: true,
    width: 160,
    sorter: true
  },
  {
    dataIndex: 'lastModifiedBy',
    title: '最后修改人',
    groupName: 'creat',
    hide: true,
    width: 130,
    sorter: true,
    customRender: ({ record }) => record.lastModifiedByName
  },
  {
    dataIndex: 'lastModifiedDate',
    title: '最后修改时间',
    groupName: 'creat',
    hide: true,
    width: 160,
    sorter: true
  },
  {
    dataIndex: 'nextGenerationDate',
    title: '下次生成时间',
    groupName: 'creat',
    hide: true,
    width: 160,
    sorter: true
  },
  {
    dataIndex: 'action',
    title: '操作',
    width: 160
  }
];
</script>
<template>
  <div class="p-5 flex flex-col h-full">
    <div class="bg-gray-2 flex items-center rounded mb-5">
      <Summary />
      <PieChart :projectId="projectId" />
    </div>

    <SearchPanel
      v-if="!!projectId && tenantInfo"
      :key="projectInfo?.type?.value"
      class="mt-3"
      :projectId="projectId"
      :userInfo="tenantInfo"
      @add="addReport"
      @change="searchPanelChange"
      @openAuth="openAuth" />

    <div class="flex flex-1 mt-3 min-h-0 space-x-2">
      <CategorySelectList v-model:category="category" class="w-50 h-full bg-gray-1"></CategorySelectList>
      <div class="flex-1 overflow-y-auto h-full">
        <Table
          :dataSource="dataList"
          :columns="columns"
          :pagination="pagination"
          :loading="loading"
          @change="onPageChange">
          <template #bodyCell="{ record, column }">
            <template v-if="column.dataIndex === 'name'">
              <RouterLink
                v-if="record.status?.value === 'SUCCESS'"
                class="link truncate"
                target="_blank"
                :title="record.name"
                :to="`/report/${record.id}/preview`">
                {{ record.name }}
              </RouterLink>
            </template>
            <template v-if="column.dataIndex === 'action'">
              <div class="flex items-center">
                <Button
                  type="text"
                  size="small"
                  :disabled="!record.currentAuths.includes('MODIFY')"
                  @click="addReport(record.id)">
                  <Icon icon="icon-xiugai" class="mr-1" />
                  编辑
                </Button>
                <Button
                  type="text"
                  size="small"
                  @click="viewReport(record)">
                  <Icon icon="icon-chakanhuodong" class="mr-1" />
                  查看
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
            <template v-if="column.dataIndex === 'status'">
              <Badge v-if="record.status?.value === 'PENDING'" status="processing" />
              <Badge v-if="record.status?.value === 'SUCCESS'" status="success" />
              <Badge v-if="record.status?.value === 'FAILURE'" status="error" />
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
        onTips="开启权限控制后，需要手动授权后才会有相应操作权限。"
        offTips="无权限限制，账号中的所有用户都可以查看、操作，默认不开启权限控制。"
        title="报告权限"
        @change="authFlagChange" />
    </AsyncComponent>
    <AsyncComponent :visible="reportModalVisible">
      <AddReportModal
        v-model:visible="reportModalVisible"
        :reportId="selectId"
        @ok="loadDataList" />
    </AsyncComponent>
    <AsyncComponent :visible="viewModalVisible">
      <ViewReportModal
        v-model:visible="viewModalVisible"
        :reportId="selectId"
        :permissions="selectReportPermissions" />
    </AsyncComponent>
    <AsyncComponent :visible="authVisible">
      <GlobalAuthorizeModal
        v-model:visible="authVisible"
        :projectId="projectId"
        :appId="appInfo?.id" />
    </AsyncComponent>
  </div>
</template>

<style scoped>
.link {
  color: #1890ff;
  cursor: pointer;
}
</style>
