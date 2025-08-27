<script setup lang="ts">
import { computed, inject, onMounted, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { Button } from 'ant-design-vue';
import { Icon, IconCopy, modal, notification, Table } from '@xcan-angus/vue-ui';
import { utils } from '@xcan-angus/infra';
import { dataSource, dataSet, variable } from '@/api/tester';
import { space } from '@/api/storage';
import { useI18n } from 'vue-i18n';

import { getCurrentPage } from '@/utils/utils';
import { CreatedItem } from './PropsType';

const { t } = useI18n();

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
          return t('dataHome.summaryTable.pagination.pageInfo', { current: pagination.value.current, totalPage });
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
    content: t('dataHome.summaryTable.messages.deleteConfirm', { name: data.name }),
    async onOk () {
      const id = data.id;
      const [error] = await delDataApicConfig[props.type]([id]);
      if (error) {
        return;
      }
      notification.success(t('tips.deleteSuccess'));
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
      title: t('dataHome.summaryTable.columns.id'),
      dataIndex: 'id'
    },
    {
      title: t('dataHome.summaryTable.columns.name'),
      dataIndex: 'name',
      ellipsis: true,
      sorter: true
    },
    ['space', 'dataSource'].includes(props.type) && {
      title: t('dataHome.summaryTable.columns.createdBy'),
      dataIndex: 'createdByName'
    },
    ['space', 'dataSource'].includes(props.type) && {
      title: t('dataHome.summaryTable.columns.createdDate'),
      dataIndex: 'createdDate'
    },
    ['variable', 'dataSet'].includes(props.type) && {
      title: t('dataHome.summaryTable.columns.lastModifiedBy'),
      dataIndex: 'lastModifiedByName',
      ellipsis: true
    },
    ['variable', 'dataSet'].includes(props.type) && {
      title: t('dataHome.summaryTable.columns.lastModifiedDate'),
      dataIndex: 'lastModifiedDate'
    },
    {
      title: t('dataHome.summaryTable.columns.action'),
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
              <span>{{ t('dataHome.summaryTable.emptyData.noVariable') }}</span>
              <Button
                type="link"
                size="small"
                class="py-0 px-1 h-5 leading-5"
                @click="toCreateVariable">
                {{ t('dataHome.summaryTable.emptyData.addVariable') }}
              </Button>
            </template>
            <template v-else-if="props.type === 'dataSet'">
              <span>{{ t('dataHome.summaryTable.emptyData.noDataset') }}</span>
              <Button
                type="link"
                size="small"
                class="py-0 px-1 h-5 leading-5"
                @click="toCreateDataSet">
                {{ t('dataHome.summaryTable.emptyData.addDataset') }}
              </Button>
            </template>
            <template v-else-if="props.type === 'space'">
              <span>{{ t('dataHome.summaryTable.emptyData.noSpace') }}</span>
            </template>
            <template v-else-if="props.type === 'dataSource'">
              <span>{{ t('dataHome.summaryTable.emptyData.noDataSource') }}</span>
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
              :title="t('actions.delete')"
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
