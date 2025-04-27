<script setup lang="ts">
import { inject, onMounted, ref, watch } from 'vue';
import { Button } from 'ant-design-vue';
import { Icon, Image, notification, Table } from '@xcan-angus/vue-ui';
import { http, TESTER } from '@xcan-angus/tools';

import { getCurrentPage } from '@/utils/utils';
import { TrashItem } from './PropsType';

type Props = {
  projectId: string;
  userInfo: { id: string; };
  params: {
    targetType: 'CASE' | 'PLAN';
    targetName?: string;
  };
  notify: string;
  spinning: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  userInfo: undefined,
  params: undefined,
  notify: undefined,
  spinning: false
});

// eslint-disable-next-line func-call-spacing
const emit = defineEmits<{
  (e: 'update:spinning', value: boolean): void;
  (e: 'listChange', value: TrashItem[]):void;
}>();

const isAdmin = inject('isAdmin', ref<boolean>());

const loaded = ref(false);

const tableData = ref<TrashItem[]>([]);
const orderBy = ref<string>();
const orderSort = ref<'ASC' | 'DESC'>();
const pagination = ref<{ total: number; current: number; pageSize: number; }>({
  total: 0,
  current: 1,
  pageSize: 10
});

const recoverHandler = async (data: TrashItem) => {
  emit('update:spinning', true);
  const [error] = await http.patch(`${TESTER}/task/trash/${data.id}/back`);
  if (error) {
    emit('update:spinning', false);
    return;
  }

  notification.success('还原成功');
  pagination.value.current = getCurrentPage(pagination.value.current, pagination.value.pageSize, pagination.value.total);
  loadData();
};

const deleteHandler = async (data: TrashItem) => {
  emit('update:spinning', true);
  const [error] = await http.del(`${TESTER}/task/trash/${data.id}`);
  if (error) {
    emit('update:spinning', false);
    return;
  }

  notification.success('删除成功');
  pagination.value.current = getCurrentPage(pagination.value.current, pagination.value.pageSize, pagination.value.total);
  loadData();
};

const tableChange = ({ current = 1, pageSize = 10 }, _filters, sorter: { orderBy: string; orderSort: 'ASC' | 'DESC'; }) => {
  orderBy.value = sorter.orderBy;
  orderSort.value = sorter.orderSort;
  pagination.value.current = current;
  pagination.value.pageSize = pageSize;
  loadData();
};

const loadData = async () => {
  emit('update:spinning', true);
  const params: {
    projectId: string;
    pageNo: number;
    pageSize: number;
    targetType?: 'CASE' | 'PLAN';
    targetName?: string;
    orderBy?: string;
    orderSort?: string;
  } = {
    projectId: props.projectId,
    pageNo: pagination.value.current,
    pageSize: pagination.value.pageSize
  };

  if (props.params) {
    params.targetType = props.params.targetType;
    params.targetName = props.params.targetName;
  }

  if (orderSort.value) {
    params.orderBy = orderBy.value;
    params.orderSort = orderSort.value;
  }

  const [error, res] = await http.get(`${TESTER}/task/trash/search`, params);
  loaded.value = true;
  emit('update:spinning', false);
  if (error) {
    return;
  }

  const data = res?.data || { list: [], total: 0 };
  const userId = props.userInfo?.id;
  tableData.value = data.list.map(item => {
    item.disabled = true;
    if (isAdmin || userId === item.createdBy || userId === item.deletedBy) {
      item.disabled = false;
    }

    return item;
  });
  emit('listChange', tableData.value);
  pagination.value.total = +(data.total || 0);
};

onMounted(() => {
  watch([() => props.projectId, () => props.params], () => {
    pagination.value.current = 1;
    loadData();
  }, { immediate: true });

  watch(() => props.notify, (newValue) => {
    if (newValue === undefined || newValue === null || newValue === '') {
      return;
    }

    pagination.value.current = 1;
    loadData();
  }, { immediate: true });
});

const columns = [
  // {
  //   title: 'ID',
  //   dataIndex: 'targetId',
  //   ellipsis: true,
  //   sorter: false
  // },
  {
    title: '名称',
    dataIndex: 'targetName',
    width: '35%',
    ellipsis: true,
    sorter: false
  },
  {
    title: '添加人',
    dataIndex: 'createdByName',
    ellipsis: true,
    sorter: false
  },
  {
    title: '删除人',
    dataIndex: 'deletedByName',
    ellipsis: true,
    sorter: false
  },
  {
    title: '删除时间',
    dataIndex: 'deletedDate',
    ellipsis: true,
    sorter: true
  },
  {
    title: '操作',
    dataIndex: 'action',
    width: 70
  }
];

const emptyTextStyle = {
  margin: '140px auto',
  height: 'auto'
};
</script>

<template>
  <Table
    v-if="loaded"
    :dataSource="tableData"
    :columns="columns"
    :pagination="pagination"
    :emptyTextStyle="emptyTextStyle"
    rowKey="id"
    size="small"
    @change="tableChange">
    <template #bodyCell="{ record, column }">
      <div
        v-if="column.dataIndex === 'deletedByName'"
        :title="record.deletedByName"
        class="flex items-center overflow-hidden">
        <div class="flex items-center flex-shrink-0 w-5 h-5 rounded-xl overflow-hidden mr-2">
          <Image
            :src="record.deletedByAvatar"
            type="avatar"
            class="w-full" />
        </div>
        <div class="flex-1 truncate">{{ record.deletedByName }}</div>
      </div>
      <div
        v-else-if="column.dataIndex === 'createdByName'"
        :title="record.createdByName"
        class="flex items-center overflow-hidden">
        <div class="flex items-center flex-shrink-0 w-5 h-5 rounded-xl overflow-hidden mr-2">
          <Image
            :src="record.createdByAvatar"
            type="avatar"
            class="w-full" />
        </div>
        <div class="flex-1 truncate">{{ record.createdByName }}</div>
      </div>
      <div v-else-if="column.dataIndex === 'action'" class="flex items-center space-x-2.5">
        <Button
          :disabled="record.disabled"
          title="还原"
          size="small"
          type="text"
          class="space-x-1 flex items-center p-0"
          @click="recoverHandler(record)">
          <Icon icon="icon-zhongzhi" class="cursor-pointer text-theme-text-hover" />
        </Button>
        <Button
          :disabled="record.disabled"
          title="删除"
          size="small"
          type="text"
          class="space-x-1 flex items-center p-0"
          @click="deleteHandler(record)">
          <Icon icon="icon-qingchu" class="text-3.5 cursor-pointer text-theme-text-hover" />
        </Button>
      </div>
    </template>
  </Table>
</template>

<style scoped>
.link {
  color: #1890ff;
  cursor: pointer;
}
</style>
