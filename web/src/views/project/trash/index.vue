<script lang="ts" setup>
import { inject, onMounted, ref, watch } from 'vue';
import { Button } from 'ant-design-vue';
import { Icon, Image, Input, notification, Spin, Table } from '@xcan-angus/vue-ui';
import { debounce } from 'throttle-debounce';
import { duration, http, TESTER } from '@xcan-angus/tools';
// import MavonEditor from 'mavon-editor';
// const mavonEditor = MavonEditor.mavonEditor
import { getCurrentPage } from '@/utils/utils';
import { TrashItem } from './PropsType';

type Props = {
  projectId: string;
  userInfo: { id: string; };
  notify: string;
}

const props = withDefaults(defineProps<Props>(), {
  projectId: undefined,
  userInfo: undefined,
  notify: undefined
});

const isAdmin = inject('isAdmin', ref<boolean>());

const loading = ref(false);
const loaded = ref(false);

const tableData = ref<TrashItem[]>([]);

const inputValue = ref<string>();
const orderBy = ref<string>();
const orderSort = ref<'ASC' | 'DESC'>();
const pagination = ref<{ total: number; current: number; pageSize: number; }>({
  total: 0,
  current: 1,
  pageSize: 10
});

const inputChange = debounce(duration.search, () => {
  pagination.value.current = 1;
  loadData();
});

const recoverAll = async () => {
  loading.value = true;
  const params = { projectId: props.projectId };
  const [error] = await http.patch(`${TESTER}/project/trash/back`, params, { paramsType: true });
  if (error) {
    loading.value = false;
    return;
  }

  notification.success('全部还原成功');
  pagination.value.current = 1;
  loadData();
};

const deleteAll = async () => {
  loading.value = true;
  const params = { projectId: props.projectId };
  const [error] = await http.del(`${TESTER}/project/trash`, params);
  if (error) {
    loading.value = false;
    return;
  }

  notification.success('全部删除成功');
  pagination.value.current = 1;
  loadData();
};

const toRefresh = () => {
  pagination.value.current = 1;
  loadData();
};

const recoverHandler = async (data:TrashItem) => {
  loading.value = true;
  const [error] = await http.patch(`${TESTER}/project/trash/${data.id}/back`);
  if (error) {
    loading.value = false;
    return;
  }

  notification.success('还原成功');
  pagination.value.current = getCurrentPage(pagination.value.current, pagination.value.pageSize, pagination.value.total);
  loadData();
};

const deleteHandler = async (data:TrashItem) => {
  loading.value = true;
  const [error] = await http.del(`${TESTER}/project/trash/${data.id}`);
  if (error) {
    loading.value = false;
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
  loading.value = true;
  const params: {
    projectId: string;
    pageNo: number;
    pageSize: number;
    targetName?: string;
    orderBy?: string;
    orderSort?: string;
  } = {
    projectId: props.projectId,
    pageNo: pagination.value.current,
    pageSize: pagination.value.pageSize
  };

  if (inputValue.value) {
    params.targetName = inputValue.value;
  }

  if (orderSort.value) {
    params.orderBy = orderBy.value;
    params.orderSort = orderSort.value;
  }

  const [error, res] = await http.get(`${TESTER}/project/trash/search`, params);
  loaded.value = true;
  loading.value = false;
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
  pagination.value.total = +(data.total || 0);
};

onMounted(() => {
  watch(() => props.projectId, () => {
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
  {
    title: 'ID',
    dataIndex: 'targetId',
    ellipsis: true,
    sorter: false
  },
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
  <Spin :spinning="loading" class="h-full px-5 py-5 overflow-auto text-3">
    <div class="flex items-center justify-between mb-3.5">
      <div class="flex items-center">
        <Input
          v-model:value="inputValue"
          :allowClear="true"
          :maxlength="200"
          trim
          placeholder="请输入查询关键字"
          class="w-75"
          @change="inputChange">
          <template #suffix>
            <Icon class="text-3.5 cursor-pointer text-theme-content" icon="icon-sousuo" />
          </template>
        </Input>
        <div class="flex-1 truncate text-theme-sub-content space-x-1 ml-2">
          <Icon icon="icon-tishi1" class="text-3.5 text-tips" />
          <span>只允许管理员、删除人还原和删除回收站。</span>
        </div>
      </div>
      <div class="space-x-2.5">
        <Button
          :disabled="!tableData?.length"
          size="small"
          type="primary"
          @click="recoverAll">
          <Icon icon="icon-zhongzhi" class="text-3.5 mr-1" />
          <span class>全部还原</span>
        </Button>
        <Button
          :disabled="!tableData?.length"
          size="small"
          type="primary"
          danger
          @click="deleteAll">
          <Icon icon="icon-qingchu" class="text-3.5 mr-1" />
          <span class>全部删除</span>
        </Button>
        <Button
          size="small"
          type="default"
          @click="toRefresh">
          <Icon icon="icon-shuaxin" class="text-3.5 mr-1" />
          <span class>刷新</span>
        </Button>
      </div>
    </div>
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
    <!-- <mavonEditor></mavonEditor> -->
  </Spin>
</template>
