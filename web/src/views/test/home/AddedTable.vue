<script setup lang="ts">
import { computed, inject, onMounted, ref, watch } from 'vue';
import { Button } from 'ant-design-vue';
import { Icon, modal, notification, Table } from '@xcan-angus/vue-ui';
import { PageQuery, ProjectPageQuery, utils } from '@xcan-angus/infra';
import { testCase } from '@/api/tester';
import { useI18n } from 'vue-i18n';
import { BasicProps } from '@/types/types';

import { getCurrentPage } from '@/utils/utils';
import { CaseInfo } from '@/views/test/types';
import { TestMenuKey } from '@/views/test/menu';

import TestResult from '@/components/test/TestResult/index.vue';
import TaskPriority from '@/components/task/TaskPriority/index.vue';

// Props and Emits
const props = withDefaults(defineProps<BasicProps>(), {
  projectId: undefined,
  params: undefined,
  total: 0,
  notify: undefined,
  refreshNotify: undefined
});

const emit = defineEmits<{
  (e: 'update:total', value: number): void;
  (e: 'update:deletedNotify', value: string): void;
}>();

// Composables
const { t } = useI18n();

// Injected Dependencies
// eslint-disable-next-line @typescript-eslint/no-empty-function
const addTabPane = inject<(data: any) => void>('addTabPane', () => { });
// eslint-disable-next-line @typescript-eslint/no-empty-function
const deleteTabPane = inject<(data: string | string[]) => void>('deleteTabPane', () => { });
const updateRefreshNotify = inject<(value: string) => void>('updateRefreshNotify');

// Reactive State
const tableData = ref<CaseInfo[]>();
const isLoading = ref(false);
const isDataLoaded = ref(false);
const currentOrderBy = ref<string>();
const currentOrderSort = ref<PageQuery.OrderSort>();

// Pagination Configuration
const paginationConfig = ref<{
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
        if (typeof paginationConfig.value === 'object') {
          const totalPage = Math.ceil(total / paginationConfig.value.pageSize);
          return t('pagination.pageInfo', { current: paginationConfig.value.current, totalPage });
        }
      }
    });

/**
 * <p>Opens a case in a new tab pane.</p>
 * <p>Creates a tab configuration and adds it to the tab pane system.</p>
 */
const openCase = (caseData: CaseInfo) => {
  addTabPane({
    _id: 'case' + caseData.id,
    name: caseData.name,
    type: 'caseInfo',
    source: 'list',
    closable: true,
    caseId: caseData.id
  });
};

/**
 * <p>Handles table pagination and sorting changes.</p>
 * <p>Updates sorting parameters and pagination state, then reloads data.</p>
 */
const handleTableChange = (
  { current = 1, pageSize = 10 },
  _filters,
  sorter: { orderBy: string; orderSort: PageQuery.OrderSort; }
) => {
  currentOrderBy.value = sorter.orderBy;
  currentOrderSort.value = sorter.orderSort;
  paginationConfig.value.current = current;
  paginationConfig.value.pageSize = pageSize;
  loadTableData();
};

/**
 * <p>Loads case list data from the API.</p>
 * <p>Constructs query parameters based on current filters and pagination state.</p>
 * <p>Updates table data and pagination information after successful response.</p>
 */
const loadTableData = async () => {
  isLoading.value = true;
  const { current, pageSize } = paginationConfig.value;
  const queryParams: ProjectPageQuery & {
    createdBy?: string;
    favouriteBy?: string;
    followBy?: string;
    testerId?: string;
    testResult?: string;
    commentBy?: string;
  } = {
    projectId: props.projectId,
    pageNo: current,
    pageSize
  };

  if (currentOrderSort.value) {
    queryParams.orderBy = currentOrderBy.value;
    queryParams.orderSort = currentOrderSort.value;
  }

  if (props.params) {
    if (props.params.createdBy) {
      queryParams.createdBy = props.params.createdBy;
    }

    if (props.params.favouriteBy) {
      queryParams.favouriteBy = props.params.favouriteBy;
    }

    if (props.params.followBy) {
      queryParams.followBy = props.params.followBy;
    }

    if (props.params.commentBy) {
      queryParams.commentBy = props.params.commentBy;
    }

    if (props.params.testerId) {
      queryParams.testerId = props.params.testerId;
    }

    if (props.params.testResult) {
      queryParams.testResult = props.params.testResult;
    }
  }

  const [error, res] = await testCase.getCaseList(queryParams);
  isLoading.value = false;
  isDataLoaded.value = true;
  if (error) {
    return;
  }

  const responseData = res?.data;
  tableData.value = responseData?.list;
  const totalCount = +(responseData?.total || 0);
  paginationConfig.value.total = totalCount;
  emit('update:total', totalCount);
};

/**
 * <p>Handles case deletion with confirmation modal.</p>
 * <p>Deletes the case and updates related UI state after confirmation.</p>
 */
const handleDeleteCase = (caseData: CaseInfo) => {
  modal.confirm({
    content: t('actions.tips.confirmDelete', { name: caseData.name }),
    async onOk () {
      const caseId = caseData.id;
      const deleteParams = [caseId];
      const [error] = await testCase.deleteCase(deleteParams as any);
      if (error) {
        return;
      }

      notification.success(t('actions.tips.deleteSuccess'));
      emit('update:deletedNotify', utils.uuid());

      deleteTabPane(['case' + caseId]);

      if (typeof updateRefreshNotify === 'function') {
        updateRefreshNotify(utils.uuid());
      }
    }
  });
};

/**
 * <p>Removes case from user's favorites.</p>
 * <p>Updates the case favorite status and refreshes the table data.</p>
 */
const handleCancelFavorite = async (caseData: CaseInfo) => {
  isLoading.value = true;
  const [error] = await testCase.cancelFavouriteCase(caseData.id);
  isLoading.value = false;
  if (error) {
    return;
  }

  notification.success(t('actions.tips.cancelFavouriteSuccess'));
  await loadTableData();

  if (typeof updateRefreshNotify === 'function') {
    updateRefreshNotify(utils.uuid());
  }
};

/**
 * <p>Removes case from user's follow list.</p>
 * <p>Updates the case follow status and refreshes the table data.</p>
 */
const handleCancelFollow = async (caseData: CaseInfo) => {
  isLoading.value = true;
  const [error] = await testCase.cancelFollowCase(caseData.id);
  isLoading.value = false;
  if (error) {
    return;
  }

  notification.success(t('actions.tips.cancelFollowSuccess'));
  await loadTableData();

  if (typeof updateRefreshNotify === 'function') {
    updateRefreshNotify(utils.uuid());
  }
};

// Computed Properties
const tableColumns = computed(() => {
  const baseColumns: {
    key: string;
    title: string;
    dataIndex: string;
    ellipsis?: boolean;
    sorter?: boolean;
    width?: string | number;
    actionKey?: 'createdBy' | 'favouriteBy' | 'followBy';
  }[] = Object.prototype.hasOwnProperty.call(props.params, 'testResult')
    ? [
        {
          key: 'code',
          title: t('common.code'),
          dataIndex: 'code',
          ellipsis: true,
          width: '12%'
        },
        {
          key: 'name',
          title: t('common.name'),
          dataIndex: 'name',
          ellipsis: true,
          sorter: true,
          width: '37%'
        },
        {
          key: 'plan',
          title: t('common.plan'),
          dataIndex: 'planName',
          ellipsis: true,
          width: '25%'
        },
        {
          key: 'priority',
          title: t('common.priority'),
          dataIndex: 'priority',
          ellipsis: true,
          sorter: true,
          width: '9%'
        },
        {
          key: 'deadline',
          title: t('common.deadlineDate'),
          dataIndex: 'deadlineDate',
          ellipsis: true,
          sorter: true,
          width: '17%'
        }
      ]
    : [
        {
          key: 'code',
          title: t('common.code'),
          dataIndex: 'code',
          ellipsis: true,
          width: '12%'
        },
        {
          key: 'name',
          title: t('common.name'),
          dataIndex: 'name',
          ellipsis: true,
          sorter: true,
          width: '32%'
        },
        {
          key: 'plan',
          title: t('common.plan'),
          dataIndex: 'planName',
          ellipsis: true,
          width: '21%'
        },
        {
          key: 'testResult',
          title: t('common.testResult'),
          dataIndex: 'testResult',
          ellipsis: true,
          width: '12%'
        },
        {
          key: 'priority',
          title: t('common.priority'),
          dataIndex: 'priority',
          ellipsis: true,
          sorter: true,
          width: '12%'
        },
        {
          key: 'deadline',
          title: t('common.deadlineDate'),
          dataIndex: 'deadlineDate',
          ellipsis: true,
          sorter: true,
          width: '18%'
        }
      ];

  const actionColumn: {
    key: string;
    title: string;
    dataIndex: string;
    ellipsis?: boolean;
    sorter?: boolean;
    width?: string | number;
    actionKey?: 'favouriteBy' | 'followBy';
  } = {
    key: 'action',
    title: t('common.actions'),
    dataIndex: 'action',
    width: 80
  };

  const currentParams = props.params;
  if (currentParams) {
    if (currentParams.favouriteBy) {
      actionColumn.actionKey = 'favouriteBy';
    } else if (currentParams.followBy) {
      actionColumn.actionKey = 'followBy';
    }
  }

  baseColumns.push(actionColumn);

  return baseColumns;
});

// Constants
const emptyTextStyle = {
  margin: '14px auto',
  height: 'auto'
};

// Lifecycle
onMounted(() => {
  watch(() => props.projectId, () => {
    loadTableData();
  }, { immediate: true });

  watch(() => props.notify, (newValue) => {
    if (newValue === undefined || newValue === null || newValue === '') {
      return;
    }

    loadTableData();
  }, { immediate: true });

  watch(() => props.refreshNotify, (newValue) => {
    if (newValue === undefined || newValue === null || newValue === '') {
      return;
    }

    paginationConfig.value.current = getCurrentPage(paginationConfig.value.current, paginationConfig.value.pageSize, paginationConfig.value.total);
    loadTableData();
  }, { immediate: true });
});
</script>

<template>
  <div>
    <template v-if="isDataLoaded">
      <template v-if="!tableData?.length">
        <div class="flex-1 flex flex-col items-center justify-center">
          <img class="w-27.5" src="../../../assets/images/nodata.png">
          <div class="flex items-center text-theme-sub-content text-3 leading-5">
            <template v-if="!!props.params?.createdBy">
              <span>{{ t('testHome.myCases.noAddedCases') }}</span>
              <RouterLink :to="`/test#${TestMenuKey.CASES}`" class="ml-1 link">{{ t('testHome.myCases.addCase') }}</RouterLink>
            </template>

            <template v-else-if="!!props.params?.favouriteBy">
              <span>{{ t('testHome.myCases.noFavouritedCases') }}</span>
            </template>

            <template v-else-if="!!props.params?.followBy">
              <span>{{ t('testHome.myCases.noFollowedCases') }}</span>
            </template>

            <template v-else-if="!!props.params?.testerId">
              <span>{{ t('testHome.myCases.noPendingCases') }}</span>
            </template>
          </div>
        </div>
      </template>

      <Table
        v-else
        :dataSource="tableData"
        :columns="tableColumns"
        :pagination="paginationConfig"
        :loading="isLoading"
        :emptyTextStyle="emptyTextStyle"
        :minSize="5"
        :noDataSize="'small'"
        :noDataText="t('common.noData')"
        rowKey="id"
        size="small"
        @change="handleTableChange">
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
              <span class="inline-block transform-gpu scale-90">{{ t('status.overdue') }}</span>
            </span>
          </div>

          <TaskPriority v-else-if="column.dataIndex === 'priority'" :value="record.priority" />
          <TestResult v-else-if="column.dataIndex === 'testResult'" :value="record.testResult" />

          <div v-else-if="column.dataIndex === 'action'">
            <template v-if="column.actionKey === 'favouriteBy'">
              <Button
                :title="t('actions.cancelFavourite')"
                size="small"
                type="text"
                class="space-x-1 flex items-center py-0 px-1"
                @click="handleCancelFavorite(record)">
                <Icon icon="icon-quxiaoshoucang" class="text-3.5 cursor-pointer text-theme-text-hover" />
              </Button>
            </template>

            <template v-else-if="column.actionKey === 'followBy'">
              <Button
                :title="t('actions.cancelFollow')"
                size="small"
                type="text"
                class="space-x-1 flex items-center py-0 px-1"
                @click="handleCancelFollow(record)">
                <Icon icon="icon-quxiaoguanzhu" class="text-3.5 cursor-pointer text-theme-text-hover" />
              </Button>
            </template>

            <template v-else>
              <Button
                :title="t('actions.delete')"
                size="small"
                type="text"
                class="space-x-1 flex items-center py-0 px-1"
                @click="handleDeleteCase(record)">
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
