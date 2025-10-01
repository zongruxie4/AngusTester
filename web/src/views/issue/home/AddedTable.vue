<script setup lang="ts">
import { computed, inject, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Button } from 'ant-design-vue';
import { Icon, IconTask, modal, notification, Table } from '@xcan-angus/vue-ui';
import { http, PageQuery, ProjectPageQuery, utils } from '@xcan-angus/infra';
import { task } from '@/api/tester';
import { TaskStatus } from '@/enums/enums';

import { getCurrentPage } from '@/utils/utils';
import { TaskDetail } from '../types';

import TaskStatusV from '@/components/TaskStatus/index.vue';
import TaskPriority from '@/components/TaskPriority/index.vue';

const { t } = useI18n();

/**
 * Props interface for AddedTable component.
 */
type Props = {
  projectId: string;
  params: {
    createdBy?: string;
    favouriteBy?: boolean;
    followBy?: boolean;
    confirmerId?: string;
    assigneeId?: string;
    commentBy?: string;
    status?: TaskStatus;
  };
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

// Inject refresh notification function from parent component
const updateRefreshNotify = inject<(value: string) => void>('updateRefreshNotify');

// Table data and state management
const tableData = ref<TaskDetail[]>();
const isLoading = ref(false);
const isDataLoaded = ref(false);
const currentOrderBy = ref<string>();
const currentOrderSort = ref<PageQuery.OrderSort>();

/**
 * Pagination configuration for the table.
 */
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
          return t('pagination.pageInfo', {
            current: paginationConfig.value.current,
            totalPage: totalPage
          });
        }
      }
    });

/**
 * Handles table changes including pagination, sorting, and filtering.
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
  loadTaskData();
};

/**
 * Builds query parameters for task list API call.
 */
const buildQueryParams = () => {
  const { current, pageSize } = paginationConfig.value;
  const queryParams: ProjectPageQuery & {
    backlog: false;
    createdBy?: string;
    favouriteBy?: boolean;
    followBy?: boolean;
    confirmerId?: string;
    assigneeId?: string;
    status?: TaskStatus;
    commentBy?: string;
  } = {
    backlog: false,
    projectId: props.projectId,
    pageNo: current,
    pageSize
  };

  // Add sorting parameters if available
  if (currentOrderSort.value) {
    queryParams.orderBy = currentOrderBy.value;
    queryParams.orderSort = currentOrderSort.value as unknown as string;
  }

  // Add filtering parameters from props
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

    if (props.params.assigneeId) {
      queryParams.assigneeId = props.params.assigneeId;
    }

    if (props.params.confirmerId) {
      queryParams.confirmerId = props.params.confirmerId;
    }

    if (props.params.status) {
      queryParams.status = props.params.status;
    }
  }
  return queryParams;
};

/**
 * Loads task data from the API and updates the table.
 */
const loadTaskData = async () => {
  const queryParams = buildQueryParams();
  isLoading.value = true;
  const [error, response] = await task.getTaskList(queryParams);
  isLoading.value = false;
  isDataLoaded.value = true;

  if (error) {
    return;
  }

  const responseData = (response?.data || { total: 0, list: [] }) as { total: string; list: TaskDetail[] };
  const totalCount = +responseData.total;
  paginationConfig.value.total = totalCount;
  emit('update:total', totalCount);

  const pageNo = +queryParams.pageNo;
  const pageSize = +queryParams.pageSize;

  // Generate task data with link URLs for navigation
  tableData.value = (responseData.list || []).map((taskItem, index) => {
    const linkParams = {
      ...queryParams,
      taskId: taskItem.id,
      pageNo: (pageNo - 1) * pageSize + index + 1,
      pageSize: 1,
      total: totalCount
    };

    return {
      ...taskItem,
      linkUrl: '/issue#issue?' + http.getURLSearchParams(linkParams, true)
    };
  });
};

/**
 * Handles task deletion with confirmation dialog.
 */
const handleTaskDeletion = (taskData: TaskDetail) => {
  modal.confirm({
    content: t('actions.tips.confirmDelete', { name: taskData.name }),
    async onOk () {
      const [error] = await task.deleteTask([taskData.id]);
      if (error) {
        return;
      }

      notification.success(t('actions.tips.deleteSuccess'));
      emit('update:deletedNotify', utils.uuid());

      if (typeof updateRefreshNotify === 'function') {
        updateRefreshNotify(utils.uuid());
      }
    }
  });
};

/**
 * Handles removing task from favorites.
 */
const handleUnfavoriteTask = async (taskData: TaskDetail) => {
  isLoading.value = true;
  const [error] = await task.cancelFavouriteTask(taskData.id);
  isLoading.value = false;

  if (error) {
    return;
  }

  notification.success(t('actions.tips.cancelFavoriteSuccess'));
  await loadTaskData();

  if (typeof updateRefreshNotify === 'function') {
    updateRefreshNotify(utils.uuid());
  }
};

/**
 * Handles removing task from followed tasks.
 */
const handleUnfollowTask = async (taskData: TaskDetail) => {
  isLoading.value = true;
  const [error] = await task.cancelFollowTask(taskData.id);
  isLoading.value = false;

  if (error) {
    return;
  }

  notification.success(t('actions.tips.cancelFollowSuccess'));
  await loadTaskData();

  if (typeof updateRefreshNotify === 'function') {
    updateRefreshNotify(utils.uuid());
  }
};

onMounted(() => {
  // Watch for project ID changes and reload data
  watch(() => props.projectId, () => {
    loadTaskData();
  }, { immediate: true });

  // Watch for refresh notifications and reload data
  watch(() => props.notify, (newValue) => {
    if (newValue === undefined || newValue === null || newValue === '') {
      return;
    }

    loadTaskData();
  }, { immediate: true });

  // Watch for deletion notifications and adjust pagination
  watch(() => props.deletedNotify, (newValue) => {
    if (newValue === undefined || newValue === null || newValue === '') {
      return;
    }

    paginationConfig.value.current = getCurrentPage(
      paginationConfig.value.current,
      paginationConfig.value.pageSize,
      paginationConfig.value.total
    );
    loadTaskData();
  }, { immediate: true });
});

/**
 * Computed property for table columns configuration.
 */
const tableColumns = computed(() => {
  const baseColumns: {
    key: string;
    title: string;
    dataIndex: string;
    ellipsis?: boolean;
    sorter?: boolean;
    width?: string | number;
    actionKey?: 'createdBy' | 'favouriteBy' | 'followBy';
  }[] = Object.prototype.hasOwnProperty.call(props.params, 'status')
    ? [
        // Columns for status-filtered view (without status column)
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
          key: 'sprintName',
          title: t('common.sprint'),
          dataIndex: 'sprintName',
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
          key: 'deadlineDate',
          title: t('common.deadlineDate'),
          dataIndex: 'deadlineDate',
          ellipsis: true,
          sorter: true,
          width: '17%'
        }
      ]
    : [
        // Columns for general view (with status column)
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
          key: 'sprintName',
          title: t('common.sprint'),
          dataIndex: 'sprintName',
          ellipsis: true,
          width: '21%'
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
          key: 'status',
          title: t('common.status'),
          dataIndex: 'status',
          ellipsis: true,
          width: '9%'
        },
        {
          key: 'deadlineDate',
          title: t('common.deadlineDate'),
          dataIndex: 'deadlineDate',
          ellipsis: true,
          sorter: true,
          width: '17%'
        }
      ];

  // Add action column based on current filter type
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

// Empty state styling configuration
const emptyStateStyle = {
  margin: '14px auto',
  height: 'auto'
};
</script>

<template>
  <div>
    <template v-if="isDataLoaded">
      <!-- Empty state display when no data is available -->
      <template v-if="!tableData?.length">
        <div class="flex-1 flex flex-col items-center justify-center">
          <img class="w-27.5" src="../../../assets/images/nodata.png">
          <div class="flex items-center text-theme-sub-content text-3 leading-5">
            <template v-if="!!props.params?.createdBy">
              <span>{{ t('issueHome.addedTable.emptyStates.noCreatedIssues') }}</span>
              <RouterLink to="/issue#issue" class="ml-1 link">
                {{ t('issueHome.addedTable.emptyStates.addIssue') }}
              </RouterLink>
            </template>

            <template v-else-if="props.params?.assigneeId && props.params?.status === TaskStatus.PENDING">
              <span>{{ t('issueHome.addedTable.emptyStates.noPendingIssues') }}</span>
            </template>

            <template v-else-if="props.params?.confirmerId && props.params?.status === TaskStatus.CONFIRMING">
              <span>{{ t('issueHome.addedTable.emptyStates.noConfirmingIssues') }}</span>
            </template>

            <template v-else-if="props.params?.assigneeId && props.params?.status === TaskStatus.COMPLETED">
              <span>{{ t('issueHome.addedTable.emptyStates.noCompletedIssues') }}</span>
            </template>

            <template v-else-if="!!props.params?.followBy">
              <span>{{ t('issueHome.addedTable.emptyStates.noFollowedIssues') }}</span>
            </template>

            <template v-else-if="!!props.params?.favouriteBy">
              <span>{{ t('issueHome.addedTable.emptyStates.noFavouriteIssues') }}</span>
            </template>

            <template v-else-if="!!props.params?.commentBy">
              <span>{{ t('issueHome.addedTable.emptyStates.noCommentIssues') }}</span>
            </template>
          </div>
        </div>
      </template>

      <!-- Task table display when data is available -->
      <Table
        v-else
        :dataSource="tableData"
        :columns="tableColumns"
        :pagination="paginationConfig"
        :loading="isLoading"
        :emptyTextStyle="emptyStateStyle"
        :minSize="5"
        :noDataSize="'small'"
        :noDataText="t('common.noData')"
        rowKey="id"
        size="small"
        @change="handleTableChange">
        <template #bodyCell="{ record, column }">
          <div v-if="column.dataIndex === 'name'" class="flex items-center">
            <IconTask :value="record.taskType?.value" class="text-4 flex-shrink-0" />
            <RouterLink
              class="link truncate ml-1"
              :title="record.name"
              :to="record.linkUrl">
              {{ record.name }}
            </RouterLink>
            <span
              v-if="record.overdue"
              class="flex-shrink-0 border border-status-error rounded px-0.5 ml-2 mr-2"
              style="color: rgba(245, 34, 45, 100%);line-height: 16px;">
              <span class="inline-block transform-gpu scale-90">{{ t('status.overdue') }}</span>
            </span>
          </div>

          <TaskPriority v-else-if="column.dataIndex === 'priority'" :value="record.priority" />

          <TaskStatusV v-else-if="column.dataIndex === 'status'" :value="record.status" />

          <div v-else-if="column.dataIndex === 'scriptType'" class="truncate">
            {{ record.scriptType?.message }}
          </div>

          <div v-else-if="column.dataIndex === 'action'">
            <template v-if="column.actionKey === 'favouriteBy'">
              <Button
                :title="t('actions.cancelFavourite')"
                size="small"
                type="text"
                class="space-x-1 flex items-center py-0 px-1"
                @click="handleUnfavoriteTask(record)">
                <Icon icon="icon-quxiaoshoucang" class="text-3.5 cursor-pointer text-theme-text-hover" />
              </Button>
            </template>

            <template v-else-if="column.actionKey === 'followBy'">
              <Button
                :title="t('actions.cancelFollow')"
                size="small"
                type="text"
                class="space-x-1 flex items-center py-0 px-1"
                @click="handleUnfollowTask(record)">
                <Icon icon="icon-quxiaoguanzhu" class="text-3.5 cursor-pointer text-theme-text-hover" />
              </Button>
            </template>

            <template v-else>
              <Button
                :title="t('actions.delete')"
                size="small"
                type="text"
                class="space-x-1 flex items-center py-0 px-1"
                @click="handleTaskDeletion(record)">
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
/* Link styling for task navigation */
.link {
  color: #1890ff;
  cursor: pointer;
}

/* Pagination margin adjustment */
:deep(.ant-pagination) {
  margin-bottom: 0;
}
</style>
