<script setup lang="ts">
import { computed, inject, onMounted, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { Button } from 'ant-design-vue';
import { Icon, IconCopy, modal, notification, Table } from '@xcan-angus/vue-ui';
import { utils } from '@xcan-angus/tools';
import { dataSource, dataSet, variable } from 'src/api/tester';
import { space } from '@/api/storage';

import { getCurrentPage } from '@/utils/utils';
import { CreatedItem } from './PropsType';

type Props = {
  projectId: string;
  userId: string;
  total: number;
  notify: string;
  deletedNotify: string;
  type: 'dataSet'|'space'|'dataSource'|'variable';
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  total: 0,
  userId: '',
  notify: undefined,
  deletedNotify: undefined
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'update:total', value: number): void;
  (e: 'update:deletedNotify', value: string): void;
}>();
const router = useRouter();

const loadDataApiConfig = {
  dataSource: dataSource.getDataSourceList,
  space: space.getSpaceList,
  dataSet: dataSet.getDataSetList,
  variable: variable.getVariablesList
};

const delDataApicConfig = {
  dataSource: dataSource.deleteDataSource,
  space: space.deleteSpace,
  dataSet: dataSet.deleteDataSet,
  variable: variable.deleteVariables
};
const appInfo = inject('appInfo', ref({ code: '' }));

const updateRefreshNotify = inject<(value: string) => void>('updateRefreshNotify');

const tableData = ref<CreatedItem[]>();
const loading = ref(false);
const loaded = ref(false);
const orderBy = ref<string>();
const orderSort = ref<'ASC' | 'DESC'>();
const pagination = ref<{
  total: number;
  current: number;
  pageSize: number;
  showSizeChanger:false;
  size:'small';
  showTotal:(value:number)=>string;
    }>({
      total: 0,
      current: 1,
      pageSize: 5,
      showSizeChanger: false,
      size: 'small',
      showTotal: (total: number) => {
        if (typeof pagination.value === 'object') {
          const totalPage = Math.ceil(total / pagination.value.pageSize);
          return `第${pagination.value.current}/${totalPage}页`;
        }
        return '';
      }
    });

const toCreateVariable = () => {
  router.push('/data#variables');
};

const toCreateDataSet = () => {
  router.push('/data#dataSet');
};

const tableChange = ({ current = 1, pageSize = 10 }, _filters, sorter: { orderBy: string; orderSort: 'ASC' | 'DESC'; }) => {
  orderBy.value = sorter.orderBy;
  orderSort.value = sorter.orderSort;
  pagination.value.current = current;
  pagination.value.pageSize = pageSize;
  loadData();
};

const loadData = async () => {
  loading.value = true;
  const { current, pageSize } = pagination.value;
  const params: {
    projectId: string;
    pageNo: number;
    pageSize: number;
    createdBy?: string;
    orderBy?: string;
    orderSort?: string;
    appCode?: string;
  } = {
    projectId: props.projectId,
    pageNo: current,
    pageSize,
    createdBy: props.userId,
    appCode: appInfo.value.code
  };

  if (orderSort.value) {
    params.orderBy = orderBy.value;
    params.orderSort = orderSort.value;
  }

  const [error, { data = {} }] = await loadDataApiConfig[props.type](params);
  loading.value = false;
  if (error) {
    return;
  }
  tableData.value = data.list || [];
  pagination.value.total = +data.total || 0;
  emit('update:total', pagination.value.total);
  loaded.value = true;
};

const deleteHandler = (data: CreatedItem) => {
  modal.confirm({
    content: `确定删除【${data.name}】吗？`,
    async onOk () {
      const id = data.id;
      const [error] = await delDataApicConfig[props.type]([id]);
      if (error) {
        return;
      }
      notification.success('删除成功');
      emit('update:deletedNotify', utils.uuid());
      if (typeof updateRefreshNotify === 'function') {
        updateRefreshNotify(utils.uuid());
      }
    }
  });
};
onMounted(() => {
  watch(() => props.projectId, () => {
    loadData();
  }, { immediate: true });

  watch(() => props.notify, (newValue) => {
    if (newValue === undefined || newValue === null || newValue === '') {
      return;
    }

    loadData();
  }, { immediate: true });

  watch(() => props.deletedNotify, (newValue) => {
    if (newValue === undefined || newValue === null || newValue === '') {
      return;
    }

    pagination.value.current = getCurrentPage(pagination.value.current, pagination.value.pageSize, pagination.value.total);
    loadData();
  }, { immediate: true });
});

const columns = computed(() => {
  const _columns: {
    title: string;
    dataIndex: string;
    ellipsis?: boolean;
    sorter?: boolean;
    width?: string | number;
    actionKey?: 'variableBy' | 'dataSetBy';
  }[] = [
    {
      title: 'ID',
      dataIndex: 'id'
    },
    {
      title: '名称',
      dataIndex: 'name',
      ellipsis: true,
      sorter: true
    },
    ['space', 'dataSource'].includes(props.type) && {
      title: '添加人',
      dataIndex: 'createdByName'
    },
    ['space', 'dataSource'].includes(props.type) && {
      title: '添加时间',
      dataIndex: 'createdDate'
    },
    ['variable', 'dataSet'].includes(props.type) && {
      title: '最后修改人',
      dataIndex: 'lastModifiedByName',
      ellipsis: true
    },
    ['variable', 'dataSet'].includes(props.type) && {
      title: '最后修改时间',
      dataIndex: 'lastModifiedDate'
    },
    {
      title: '操作',
      dataIndex: 'action',
      width: 50
    }
  ].filter(Boolean);
  return _columns;
});

const emptyTextStyle = {
  margin: '14px auto',
  height: 'auto'
};
</script>

<template>
  <div>
    <template v-if="loaded">
      <template v-if="!tableData?.length">
        <div class="flex-1 flex flex-col items-center justify-center">
          <img class="w-27.5" src="../../../../assets/images/nodata.png">
          <div class="flex items-center text-theme-sub-content text-3 leading-5">
            <template v-if="props.type === 'variable'">
              <span>您尚未添加任何变量，立即</span>
              <Button
                type="link"
                size="small"
                class="py-0 px-1 h-5 leading-5"
                @click="toCreateVariable">
                添加变量
              </Button>
            </template>
            <template v-else-if="props.type === 'dataSet'">
              <span>您尚未添加任何数据集，立即</span>
              <Button
                type="link"
                size="small"
                class="py-0 px-1 h-5 leading-5"
                @click="toCreateDataSet">
                添加数据集
              </Button>
            </template>
            <template v-else-if="props.type === 'space'">
              <span>您没有添加空间</span>
            </template>
            <template v-else-if="props.type === 'dataSource'">
              <span>您没有添加数据源</span>
            </template>
          </div>
        </div>
      </template>
      <Table
        v-else
        :dataSource="tableData"
        :columns="columns"
        :pagination="pagination"
        :loading="loading"
        :emptyTextStyle="emptyTextStyle"
        :minSize="5"
        rowKey="id"
        size="small"
        @change="tableChange">
        <template #bodyCell="{ record, column }">
          <template v-if="props.type==='variable'">
            <div v-if="column.dataIndex === 'name'" class="flex items-center">
              <RouterLink
                class="link flex-1 truncate"
                :title="record.name"
                :to="`/data#variables?id=${record.id}`">
                {{ record.name }}
              </RouterLink>
              <IconCopy :copyText="`{${record.name}}`" class="ml-1.5" />
            </div>
          </template>

          <template v-else-if="props.type==='dataSet'">
            <div v-if="column.dataIndex === 'name'" class="flex items-center">
              <RouterLink
                class="link flex-1 truncate"
                :title="record.name"
                :to="`/data#dataSet?id=${record.id}`">
                {{ record.name }}
              </RouterLink>
              <IconCopy :copyText="`{${record.name}}`" class="ml-1.5" />
            </div>
          </template>

          <div v-if="column.dataIndex === 'action'">
            <Button
              title="删除"
              size="small"
              type="text"
              class="space-x-1 flex items-center py-0 px-1"
              @click="deleteHandler(record)">
              <Icon icon="icon-qingchu" class="text-3.5 cursor-pointer text-theme-text-hover" />
            </Button>
          </div>
        </template>
      </Table>
    </template>
  </div>
</template>

<style scoped>
.link {
  color: #1890ff;
  cursor: pointer;
}

.api-status-UNKNOWN {
  color: rgba(140, 140, 140, 100%);
}

.api-status-IN_DESIGN {
  color: rgba(255, 129, 0, 100%);
}

.api-status-IN_DEV {
  color: rgba(0, 119, 255, 100%);
}

.api-status-DEV_COMPLETED {
  color: rgba(82, 196, 26, 100%);
}

.api-status-RELEASED {
  color: rgb(56, 158, 13, 100%);
}

:deep(.ant-pagination) {
  margin-bottom: 0;
}
</style>
