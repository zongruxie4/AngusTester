<script setup lang="ts">
import { computed, inject, onMounted, ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Button } from 'ant-design-vue';
import { Icon, modal, notification, Table } from '@xcan-angus/vue-ui';
import { PageQuery, utils } from '@xcan-angus/infra';
import { scenario } from '@/api/tester';

import { getCurrentPage } from '@/utils/utils';
import { SceneItem } from './types';

const { t } = useI18n();

type Props = {
  projectId: string;
  params: { createdBy?: string; favouriteBy?: boolean; followBy?: boolean; };
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

const updateRefreshNotify = inject<(value: string) => void>('updateRefreshNotify');

const tableData = ref<SceneItem[]>();
const loading = ref(false);
const loaded = ref(false);
const orderBy = ref<string>();
const orderSort = ref<PageQuery.OrderSort>();
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
          return t('scenarioHome.myScenarios.table.messages.pageInfo', { current: pagination.value.current, total: totalPage });
        }
      }
    });

const tableChange = ({ current = 1, pageSize = 10 }, _filters, sorter: { orderBy: string; orderSort: PageQuery.OrderSort; }) => {
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
    orderBy?: string;
    orderSort?: string;
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
  }
  const [error, res] = await scenario.getScenarioList(params);
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

const deleteHandler = (data: SceneItem) => {
  modal.confirm({
    content: t('scenarioHome.myScenarios.table.messages.deleteConfirm', { name: data.name }),
    async onOk () {
      const [error] = await scenario.deleteScenario(data.id);
      if (error) {
        return;
      }

      notification.success(t('scenarioHome.myScenarios.table.messages.deleteSuccess'));
      emit('update:deletedNotify', utils.uuid());

      if (typeof updateRefreshNotify === 'function') {
        updateRefreshNotify(utils.uuid());
      }
    }
  });
};

const cancelFavourite = async (data: SceneItem) => {
  loading.value = true;
  const [error] = await scenario.deleteScenarioFavorite(data.id);
  loading.value = false;
  if (error) {
    return;
  }

  notification.success(t('scenarioHome.myScenarios.table.messages.unfavoriteSuccess'));
  await loadData();

  if (typeof updateRefreshNotify === 'function') {
    updateRefreshNotify(utils.uuid());
  }
};

const cancelFollow = async (data: SceneItem) => {
  loading.value = true;
  const [error] = await scenario.deleteScenarioFollow(data.id);
  loading.value = false;
  if (error) {
    return;
  }

  notification.success(t('scenarioHome.myScenarios.table.messages.unfollowSuccess'));
  await loadData();

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
  }[] = [
    {
      title: t('scenarioHome.myScenarios.table.columns.name'),
      dataIndex: 'name',
      ellipsis: true,
      sorter: true,
      width: '45%'
    },
    {
      title: t('scenarioHome.myScenarios.table.columns.plugin'),
      dataIndex: 'plugin',
      ellipsis: true
    },
    {
      title: t('scenarioHome.myScenarios.table.columns.testType'),
      dataIndex: 'scriptType',
      ellipsis: true
    },
    {
      title: t('scenarioHome.myScenarios.table.columns.addTime'),
      dataIndex: 'createdDate',
      ellipsis: true,
      sorter: true
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
    title: t('scenarioHome.myScenarios.table.columns.operation'),
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
          <img class="w-27.5" src="../../../assets/images/nodata.png">
          <div class="flex items-center text-theme-sub-content text-3 leading-5">
            <template v-if="!!props.params?.createdBy">
              <span>{{ t('scenarioHome.myScenarios.table.messages.noAddedScenarios') }}</span>
              <RouterLink to="/scenario#scenario" class="ml-1 link">{{ t('scenarioHome.myScenarios.table.messages.addScenario') }}</RouterLink>
            </template>

            <template v-else-if="!!props.params?.favouriteBy">
              <span>{{ t('scenarioHome.myScenarios.table.messages.noFavoritedScenarios') }}</span>
            </template>

            <template v-else-if="!!props.params?.followBy">
              <span>{{ t('scenarioHome.myScenarios.table.messages.noFollowedScenarios') }}</span>
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
          <RouterLink
            v-if="column.dataIndex === 'name'"
            class="link truncate"
            :title="record.name"
            :to="`/scenario#scenario?id=${record.id}&name=${record.name}&plugin=${record.plugin}`">
            {{ record.name }}
          </RouterLink>
          <div v-else-if="column.dataIndex === 'scriptType'" class="truncate">
            {{ record.scriptType?.message }}
          </div>
          <div v-else-if="column.dataIndex === 'action'">
            <template v-if="column.actionKey === 'favouriteBy'">
              <Button
                :title="t('scenarioHome.myScenarios.table.actions.unfavorite')"
                size="small"
                type="text"
                class="space-x-1 flex items-center py-0 px-1"
                @click="cancelFavourite(record)">
                <Icon icon="icon-quxiaoshoucang" class="text-3.5 cursor-pointer text-theme-text-hover" />
              </Button>
            </template>

            <template v-else-if="column.actionKey === 'followBy'">
              <Button
                :title="t('scenarioHome.myScenarios.table.actions.unfollow')"
                size="small"
                type="text"
                class="space-x-1 flex items-center py-0 px-1"
                @click="cancelFollow(record)">
                <Icon icon="icon-quxiaoguanzhu" class="text-3.5 cursor-pointer text-theme-text-hover" />
              </Button>
            </template>

            <template v-else>
              <Button
                :title="t('actions.delete')"
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
