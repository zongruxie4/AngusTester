<script setup lang="ts">
import { computed, inject, onMounted, ref, watch } from 'vue';
import { Button } from 'ant-design-vue';
import { Icon, modal, notification, Table, TaskPriority, TestResult } from '@xcan-angus/vue-ui';
import { utils } from '@xcan-angus/infra';
import { funcCase } from '@/api/tester';
import { useI18n } from 'vue-i18n';

import { getCurrentPage } from '@/utils/utils';
import { CaseItem } from '../PropsType';

const { t } = useI18n();

type Props = {
  projectId: string;
  params: { createdBy?: string; favouriteBy?: boolean; followBy?: boolean; testerId?: string; testResult?: string; commentBy?: string;};
  total: number;
  notify: string;
  deletedNotify: string;
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  params: undefined,
  total: 0,
  notify: undefined,
  deletedNotify: undefined
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'update:total', value: number): void;
  (e: 'update:deletedNotify', value: string): void;
}>();

// eslint-disable-next-line @typescript-eslint/no-empty-function
const addTabPane = inject<(data: any) => void>('addTabPane', () => { });

// eslint-disable-next-line @typescript-eslint/no-empty-function
const deleteTabPane = inject<(data: any) => void>('deleteTabPane', () => { });

const updateRefreshNotify = inject<(value: string) => void>('updateRefreshNotify');

const tableData = ref<CaseItem[]>();
const loading = ref(false);
const loaded = ref(false);
const orderBy = ref<string>();
const orderSort = ref<'ASC' | 'DESC'>();
const pagination = ref<{
  total: number;
  current: number;
  pageSize: number;
  showSizeChanger: false;
  size: 'small';
  showTotal:(value: number) => string;
    }>({
      total: 0,
      current: 1,
      pageSize: 5,
      showSizeChanger: false,
      size: 'small',
      showTotal: (total: number) => {
        if (typeof pagination.value === 'object') {
          const totalPage = Math.ceil(total / pagination.value.pageSize);
          return t('functionHome.myCases.pageInfo', { current: pagination.value.current, total: totalPage });
        }
      }
    });

const openCase = (data: CaseItem) => {
  addTabPane({
    _id: 'case' + data.id,
    name: data.name,
    type: 'caseInfo',
    source: 'list',
    closable: true,
    caseId: data.id
  });
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
    favouriteBy?: boolean;
    followBy?: boolean;
    testerId?: string;
    testResult?: string;
    orderBy?: string;
    orderSort?: string;
    commentBy?: string;
  } = {
    projectId: props.projectId,
    pageNo: current,
    pageSize
  };

  if (orderSort.value) {
    params.orderBy = orderBy.value;
    params.orderSort = orderSort.value;
  }

  if (props.params) {
    if (props.params.createdBy) {
      params.createdBy = props.params.createdBy;
    }

    if (props.params.favouriteBy) {
      params.favouriteBy = props.params.favouriteBy;
    }

    if (props.params.followBy) {
      params.followBy = props.params.followBy;
    }

    if (props.params.commentBy) {
      params.commentBy = props.params.commentBy;
    }

    if (props.params.testerId) {
      params.testerId = props.params.testerId;
    }

    if (props.params.testResult) {
      params.testResult = props.params.testResult;
    }
  }
  const [error, res] = await funcCase.getCaseList(params);
  loading.value = false;
  loaded.value = true;
  if (error) {
    return;
  }

  const data = res?.data;
  tableData.value = data?.list;
  const total = +(data?.total || 0);
  pagination.value.total = total;
  emit('update:total', total);
};

const deleteHandler = (data: CaseItem) => {
  modal.confirm({
    content: t('functionHome.myCases.confirmDeleteCase', { name: data.name }),
    async onOk () {
      const id = data.id;
      const params = [id];
      const [error] = await funcCase.deleteCase(params);
      if (error) {
        return;
      }

      notification.success(t('functionHome.myCases.deleteSuccess'));
      emit('update:deletedNotify', utils.uuid());

      // 删除已经打开的tabpane
      deleteTabPane(['case' + id]);

      if (typeof updateRefreshNotify === 'function') {
        updateRefreshNotify(utils.uuid());
      }
    }
  });
};

const cancelFavourite = async (data: CaseItem) => {
  loading.value = true;
  const [error] = await funcCase.cancelFavouriteCase(data.id);
  loading.value = false;
  if (error) {
    return;
  }

  notification.success(t('functionHome.myCases.cancelFavoriteSuccess'));
  loadData();

  if (typeof updateRefreshNotify === 'function') {
    updateRefreshNotify(utils.uuid());
  }
};

const cancelFollow = async (data: CaseItem) => {
  loading.value = true;
  const [error] = await funcCase.cancelFollowCase(data.id);
  loading.value = false;
  if (error) {
    return;
  }

  notification.success(t('functionHome.myCases.cancelFollowSuccess'));
  loadData();

  if (typeof updateRefreshNotify === 'function') {
    updateRefreshNotify(utils.uuid());
  }
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
    actionKey?: 'createdBy' | 'favouriteBy' | 'followBy';
  }[] = Object.prototype.hasOwnProperty.call(props.params, 'testResult')
    ? [
        {
          title: t('functionHome.myCases.code'),
          dataIndex: 'code',
          ellipsis: true,
          width: '12%'
        },
        {
          title: t('functionHome.myCases.name'),
          dataIndex: 'name',
          ellipsis: true,
          sorter: true,
          width: '37%'
        },
        {
          title: t('functionHome.myCases.plan'),
          dataIndex: 'planName',
          ellipsis: true,
          width: '25%'
        },
        {
          title: t('functionHome.myCases.priority'),
          dataIndex: 'priority',
          ellipsis: true,
          sorter: true,
          width: '9%'
        },
        {
          title: t('functionHome.myCases.deadline'),
          dataIndex: 'deadlineDate',
          ellipsis: true,
          sorter: true,
          width: '17%'
        }
      ]
    : [
        {
          title: t('functionHome.myCases.code'),
          dataIndex: 'code',
          ellipsis: true,
          width: '12%'
        },
        {
          title: t('functionHome.myCases.name'),
          dataIndex: 'name',
          ellipsis: true,
          sorter: true,
          width: '32%'
        },
        {
          title: t('functionHome.myCases.plan'),
          dataIndex: 'planName',
          ellipsis: true,
          width: '21%'
        },
        {
          title: t('functionHome.myCases.testResult'),
          dataIndex: 'testResult',
          ellipsis: true,
          width: '9%'
        },
        {
          title: t('functionHome.myCases.priority'),
          dataIndex: 'priority',
          ellipsis: true,
          sorter: true,
          width: '9%'
        },
        {
          title: t('functionHome.myCases.deadline'),
          dataIndex: 'deadlineDate',
          ellipsis: true,
          sorter: true,
          width: '17%'
        }
      ];

  const actionColumn: {
    title: string;
    dataIndex: string;
    ellipsis?: boolean;
    sorter?: boolean;
    width?: string | number;
    actionKey?: 'favouriteBy' | 'followBy';
  } = {
    title: t('functionHome.myCases.actions'),
    dataIndex: 'action',
    width: 50
  };

  const _params = props.params;
  if (_params) {
    if (_params.favouriteBy) {
      actionColumn.actionKey = 'favouriteBy';
    } else if (_params.followBy) {
      actionColumn.actionKey = 'followBy';
    }
  }

  _columns.push(actionColumn);

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
            <template v-if="!!props.params?.createdBy">
              <span>{{ t('functionHome.myCases.noAddedCases') }}</span>
              <RouterLink to="/function#cases" class="ml-1 link">{{ t('functionHome.myCases.addCase') }}</RouterLink>
            </template>

            <template v-else-if="!!props.params?.favouriteBy">
              <span>{{ t('functionHome.myCases.noFavoritedCases') }}</span>
            </template>

            <template v-else-if="!!props.params?.followBy">
              <span>{{ t('functionHome.myCases.noFollowedCases') }}</span>
            </template>

            <template v-else-if="!!props.params?.testerId">
              <span>{{ t('functionHome.myCases.noPendingCases') }}</span>
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
          <div v-if="column.dataIndex === 'name'" class="flex items-center">
            <div
              class="link truncate"
              :title="record.name"
              @click="openCase(record)">
              {{ record.name }}
            </div>
            <span
              v-if="record.overdue"
              class="flex-shrink-0 border border-status-error rounded px-0.5 ml-2 mr-2"
              style="color: rgba(245, 34, 45, 100%);line-height: 16px;">
              <span class="inline-block transform-gpu scale-90">{{ t('functionHome.myCases.overdue') }}</span>
            </span>
          </div>

          <TaskPriority v-else-if="column.dataIndex === 'priority'" :value="record.priority" />
          <TestResult v-else-if="column.dataIndex === 'testResult'" :value="record.testResult" />

          <div v-else-if="column.dataIndex === 'action'">
            <template v-if="column.actionKey === 'favouriteBy'">
              <Button
                :title="t('functionHome.myCases.cancelFavorite')"
                size="small"
                type="text"
                class="space-x-1 flex items-center py-0 px-1"
                @click="cancelFavourite(record)">
                <Icon icon="icon-quxiaoshoucang" class="text-3.5 cursor-pointer text-theme-text-hover" />
              </Button>
            </template>

            <template v-else-if="column.actionKey === 'followBy'">
              <Button
                :title="t('functionHome.myCases.cancelFollow')"
                size="small"
                type="text"
                class="space-x-1 flex items-center py-0 px-1"
                @click="cancelFollow(record)">
                <Icon icon="icon-quxiaoguanzhu" class="text-3.5 cursor-pointer text-theme-text-hover" />
              </Button>
            </template>

            <template v-else>
              <Button
                :title="t('functionHome.myCases.delete')"
                size="small"
                type="text"
                class="space-x-1 flex items-center py-0 px-1"
                @click="deleteHandler(record)">
                <Icon icon="icon-qingchu" class="text-3.5 cursor-pointer text-theme-text-hover" />
              </Button>
            </template>
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

:deep(.ant-pagination) {
  margin-bottom: 0;
}
</style>
