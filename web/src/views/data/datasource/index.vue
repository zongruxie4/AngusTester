<script setup lang="ts">
import { computed, defineAsyncComponent, inject, onMounted, ref, watch } from 'vue';
import { Button, Pagination } from 'ant-design-vue';
import { useI18n } from 'vue-i18n';
import {
  AsyncComponent,
  GridList,
  Icon,
  IconRefresh,
  Image,
  modal,
  NoData,
  notification,
  SearchPanel,
  Spin,
  Tooltip
} from '@xcan-angus/vue-ui';

import { dataSource } from 'src/api/tester';
import { getCurrentPage } from '@/utils/utils';

type FilterOp = 'EQUAL' | 'NOT_EQUAL' | 'GREATER_THAN' | 'GREATER_THAN_EQUAL' | 'LESS_THAN' | 'LESS_THAN_EQUAL' | 'CONTAIN' | 'NOT_CONTAIN' | 'MATCH_END' | 'MATCH' | 'IN' | 'NOT_IN'
type Filters = { key: string, value: string | boolean | string[], op: FilterOp };
type SearchParam = {
    pageNo: number;
    pageSize: number;
    filters?: Filters[];
    orderBy?: string;
    orderSort?: 'ASC' | 'DESC';
    [key: string]: any;
};

const AddModal = defineAsyncComponent(() => import('./add.vue'));

const { t } = useI18n();
const projectInfo = inject('projectInfo', ref({ id: '' }));
const projectId = computed(() => {
  return projectInfo.value?.id;
});

const isFirstLoad = ref(true);
const loading = ref(false);
const params = ref<SearchParam>({ pageNo: 1, pageSize: 40, filters: [] });
const total = ref(0);
const searchChange = (data: { key: string; value: string; op: FilterOp; }[]) => {
  params.value.pageNo = 1;
  params.value.filters = data;
  getList();
};

const dataList = ref<Record<string, any>[]>([]);

const dataMap = ref<Record<string, any>>({});
const getList = async () => {
  dataMap.value = {};
  if (loading.value) {
    return;
  }
  loading.value = true;
  const [error, { data = { list: [], total: 0 } }] = await dataSource.getDataSourceList({ ...params.value, projectId: projectId.value });
  loading.value = false;
  isFirstLoad.value = false;
  if (error) {
    return;
  }
  const len = data.list.length;
  if (len > 0) {
    for (let i = 0; i < len; i++) {
      const item = {
        ...data.list[i],
        connSuccess: undefined,
        connFailureMessage: undefined,
        testLoading: false
      };
      dataMap.value[item.id] = item;
    }
  }
  dataList.value = data.list || [];
  total.value = +data.total;
};

const refreshList = () => {
  params.value.pageNo = 1;
  getList();
};

const visible = ref(false);
const handleAdd = () => {
  editData.value = undefined;
  visible.value = true;
};

const editData = ref();
const handleEdit = (record) => {
  editData.value = record;
  visible.value = true;
};

const handleDel = (record) => {
  modal.confirm({
    centered: true,
    content: `确定【${record.name}】吗？`,
    async onOk () {
      loading.value = true;
      const [error] = await dataSource.deleteDataSource(record.id);
      loading.value = false;
      if (error) {
        return;
      }
      notification.success('删除成功');
      params.value.pageNo = getCurrentPage(params.value.pageNo as number, params.value.pageSize as number, total.value);
      getList();
    }
  });
};

const testLink = async (record) => {
  if (loading.value) {
    return;
  }
  loading.value = true;
  dataMap.value[record.id].testLoading = true;
  const [error, { data }] = await dataSource.testById(record.id);
  dataMap.value[record.id].testLoading = false;
  loading.value = false;
  if (error) {
    return;
  }

  dataMap.value[record.id].connSuccess = data.connSuccess;
  dataMap.value[record.id].connFailureMessage = data.connFailureMessage;
};

onMounted(() => {
  watch(() => projectId.value, newValue => {
    if (newValue) {
      params.value.pageNo = 1;
      getList();
    }
  }, {
    immediate: true
  });
});

const searchOption = [
  {
    valueKey: 'name',
    type: 'input',
    placeholder: t('查询数据源名称'),
    allowClear: true,
    maxlength: 100
  },
  {
    valueKey: 'database',
    type: 'select-enum',
    enumKey: 'DatabaseType',
    placeholder: t('选择数据库类型'),
    allowClear: true
  },
  {
    valueKey: 'lastModifiedBy',
    type: 'select-user',
    placeholder: '选择修改人',
    maxlength: 100
  },
  {
    valueKey: 'lastModifiedDate',
    type: 'date-range',
    placeholder: [t('修改时间从'), t('修改时间到')],
    allowClear: true,
    showTime: true
  }
];

const showTotal = (total: number) => {
  const totalPage = Math.ceil(total / params.value.pageSize);
  return t('execute.pageShowTotal', { total, pageNo: params.value.pageNo, totalPage });
};

const paginationChange = (page:number, size:number) => {
  params.value.pageNo = page;
  params.value.pageSize = size;
  getList();
};
</script>
<template>
  <Spin
    :spinning="loading"
    class="pl-5 py-5 w-full h-full flex flex-col">
    <div class="text-3.5 font-semibold mb-2.5">关于数据源</div>
    <div class="mb-6 text-3">
      <div>支持管理和连接多种数据源，配置数据源信息后，可以快速生成测试、项目演示等场景数据，以及在JDBC测试、参数化变量、数据集中快速引用数据源配置信息。</div>
    </div>
    <div class="text-3.5 font-semibold mb-2.5">已添加数据源</div>
    <div class="flex pr-5">
      <SearchPanel
        :options="searchOption"
        :width="284"
        class="flex-1"
        @change="searchChange" />
      <div class="flex space-x-2.5">
        <!-- <ButtonAuth
          code="DataSourcesAdd"
          type="primary"
          icon="icon-jia"
          @click="handleAdd" /> -->
        <Button
          type="primary"
          size="small"
          class="flex space-x-1"
          @click="handleAdd">
          <Icon icon="icon-jia" />
          添加数据源
        </Button>
        <Button
          class="flex items-center"
          size="small"
          type="default"
          @click="refreshList">
          <IconRefresh />
        </Button>
      </div>
    </div>
    <template v-if="!isFirstLoad">
      <template v-if="dataList?.length">
        <GridList
          class="mt-3 overflow-y-auto flex flex-warp text-3 text-text-content"
          style="padding-right: 13px; scrollbar-gutter: stable;"
          :itemWidth="328"
          :dataSource="dataList">
          <template #default="record">
            <div class="px-3 pt-3 pb-2.5 border rounded border-border-divider flex flex-col justify-between w-full">
              <div class="w-full flex-1">
                <div class="flex items-center w-full mb-4">
                  <div style="background-color: #F7F8FB;" class="w-15 h-15 flex-none p-1.25 rounded mr-2.5">
                    <template v-if="record.database === 'H2'">
                      <img src="../../../assets/database/H2.png" class="w-full h-full" />
                    </template>
                    <template v-if="record.database === 'HSQLDB'">
                      <img src="../../../assets/database/HSQLDB.png" class="w-full h-full" />
                    </template>
                    <template v-if="record.database === 'SQLITE'">
                      <img src="../../../assets/database/SQLITE.png" class="w-full h-full" />
                    </template>
                    <template v-if="record.database === 'POSTGRES'">
                      <img src="../../../assets/database/POSTGRES.png" class="w-full h-full" />
                    </template>
                    <template v-if="record.database === 'MARIADB'">
                      <img src="../../../assets/database/MARIADB.png" class="w-full h-full" />
                    </template>
                    <template v-if="record.database === 'MYSQL'">
                      <img src="../../../assets/database/MYSQL.png" class="w-full h-full" />
                    </template>
                    <template v-if="record.database === 'ORACLE'">
                      <img src="../../../assets/database/ORACLE.png" class="w-full h-full" />
                    </template>
                    <template v-if="record.database === 'SQLSERVER'">
                      <img src="../../../assets/database/SQLSERVER.png" class="w-full h-full" />
                    </template>
                    <template v-if="record.database === 'DB2'">
                      <img src="../../../assets/database/DB2.png" class="w-full h-full" />
                    </template>
                  </div>
                  <div class="flex items-center" style="width: calc(100% - 70px);">
                    <div class="mr-5 truncate flex-1 text-3.5 font-medium text-text-title" :title="record.name">{{ record.name }}</div>
                    <div class="px-2 py-0.25 whitespace-nowrap rounded -mt-10" style="background-color: #F7F8FB;color: #828894;">{{ record.database }}</div>
                  </div>
                </div>
                <div class="flex items-center text-text-sub-content">
                  <div class="flex-none">
                    <Image
                      :src="record?.avatar"
                      class="w-5 h-5 rounded-full flex-none mr-2"
                      type="avatar" />
                  </div>
                  <div
                    style="max-width: 110px;"
                    class="truncate leading-5"
                    :title="record.lastModifiedByName">
                    {{ record.lastModifiedByName }}
                  </div>
                  <div class="flex-none leading-5">&nbsp;&nbsp;最后修改于&nbsp;&nbsp;{{ record.lastModifiedDate }}</div>
                </div>
              </div>
              <div class="border-t border-border-divider my-2.5"></div>
              <div class="flex justify-between items-center leading-3">
                <div class="flex space-x-2">
                  <Icon
                    icon="icon-shuxie"
                    class="text-3.5 text-text-sub-content cursor-pointer hover:text-text-link-hover"
                    @click="handleEdit(record)" />
                  <Icon
                    icon="icon-qingchu"
                    class="text-3.5 text-text-sub-content cursor-pointer hover:text-text-link-hover"
                    @click="handleDel(record)" />
                </div>
                <div class="flex items-center h-3">
                  <tempalte v-if="dataMap[record.id]?.testLoading" class="text-text-sub-content">测试中...</tempalte>
                  <template v-else>
                    <template v-if="(typeof dataMap[record.id]?.connSuccess) === 'boolean'">
                      <div class="flex items-center">
                        <template v-if="dataMap[record.id]?.connSuccess">
                          <Icon icon="icon-duihao" class="text-status-success mr-1 text-3.25" />
                          <span>成功</span>
                        </template>
                        <template v-else>
                          <Tooltip
                            placement="topLeft"
                            arrowPointAtCenter
                            :title="dataMap[record.id]?.connFailureMessage"
                            :overlayStyle="{'max-width': '400px'}">
                            <div class="flex items-center cursor-pointer">
                              <Icon icon="icon-jinggao" class="text-status-error mr-1 text-3.25 -mt-0.25" />
                              <span>失败</span>
                            </div>
                          </Tooltip>
                          <div><a class="ml-2" @click="testLink(record)">重新测试</a></div>
                        </template>
                      </div>
                    </template>
                    <template v-else>
                      <div><a @click="testLink(record)">测试连接</a></div>
                    </template>
                  </template>
                </div>
              </div>
            </div>
          </template>
        </GridList>
        <Pagination
          v-if="total > 40"
          :current="params.pageNo"
          :pageSize="params.pageSize"
          :total="total"
          :hideOnSinglePage="false"
          :showTotal="showTotal"
          :showSizeChanger="false"
          size="middle"
          class="justify-end mr-5"
          @change="paginationChange" />
      </template>
      <template v-if="!loading && !dataList?.length">
        <div class="pt-45">
          <NoData />
        </div>
      </template>
    </template>
  </Spin>
  <AsyncComponent :visible="visible">
    <AddModal
      v-model:visible="visible"
      :editData="editData"
      :projectId="projectId"
      @refresh="refreshList" />
  </AsyncComponent>
</template>
