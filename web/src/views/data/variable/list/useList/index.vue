<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { Button } from 'ant-design-vue';
import { Hints, Icon, modal, NoData, notification, Spin, Table } from '@xcan-angus/vue-ui';
import { http, TESTER } from '@xcan-angus/infra';
import { SourceItem } from '../../PropsType';
import { paramTarget, variable } from '@/api/tester';

type Props = {
  id: string;
}

const props = withDefaults(defineProps<Props>(), {
  id: undefined
});

const MAX_NUM = 200;

const loading = ref(false);
const loaded = ref(false);
const pagination = ref<{ current: number; pageSize: number; total: number; showSizeChanger: false; }>({ current: 1, pageSize: 10, total: 0, showSizeChanger: false });
const rowSelection = ref<{
  onChange:(key: string[]) => void;
  getCheckboxProps: (data: SourceItem) => ({ disabled: boolean; });
  selectedRowKeys: string[];
}>();
const dataList = ref<SourceItem[]>([]);

const toDelete = (data: SourceItem) => {
  modal.confirm({
    content: `确定删除资源【${data.targetName}】吗？`,
    async onOk () {
      loading.value = true;
      const params = [props.id];
      const [error] = await paramTarget.deleteVariable(data.targetId, data.targetType?.value, params, { dataType: true });
      loading.value = false;
      if (error) {
        return;
      }

      pagination.value.total = pagination.value.total - 1;
      notification.success('删除资源成功');
      dataList.value = dataList.value.filter((item) => item.targetId !== data.targetId);
    }
  });
};

const toBatchDelete = () => {
  if (!rowSelection.value) {
    rowSelection.value = {
      onChange: tableSelect,
      getCheckboxProps: () => {
        return {
          disabled: false
        };
      },
      selectedRowKeys: []
    };

    return;
  }

  const selectedRowKeys = rowSelection.value.selectedRowKeys;
  const selectedNum = selectedRowKeys.length;
  if (!selectedNum) {
    rowSelection.value = undefined;
    return;
  }

  if (selectedNum > MAX_NUM) {
    notification.error(`最大支持批量删除 ${MAX_NUM} 个资源，当前已选中 ${selectedNum} 个资源。`);
    return;
  }

  modal.confirm({
    content: `确定删除选中的 ${selectedNum} 条资源吗？`,
    async onOk () {
      const ids = selectedRowKeys;
      const num = ids.length;
      const params = [props.id];
      const promises: Promise<any>[] = [];
      for (let i = 0; i < num; i++) {
        const data = dataList.value.find((item) => item.targetId === ids[i]);
        if (data) {
          const _promise = paramTarget.deleteVariable(data.targetId, data.targetType?.value, params, { dataType: true, silence: true });
          promises.push(_promise);
        }
      }

      loading.value = true;
      Promise.all(promises).then((res: [Error | null, any][]) => {
        loading.value = false;
        const errorIds: string[] = [];
        for (let i = 0, len = res.length; i < len; i++) {
          if (res[i][0]) {
            errorIds.push(ids[i]);
          }
        }

        const errorNum = errorIds.length;
        if (errorNum === 0) {
          notification.success(`选中的 ${num} 条资源全部删除成功`);
          pagination.value.total = pagination.value.total - num;
          rowSelection.value = undefined;
          dataList.value = dataList.value.filter((item) => !ids.includes(item.targetId));
          return;
        }

        if (errorNum === num) {
          notification.error(`选中的 ${num} 条资源全部删除失败`);
          return;
        }

        const successIds = ids.filter(item => !errorIds.includes(item));
        notification.warning(`选中的 ${num - errorNum} 条资源删除成功，${errorNum} 条资源删除失败`);
        rowSelection.value!.selectedRowKeys = rowSelection.value!.selectedRowKeys.filter((item) => !successIds.includes(item));
        dataList.value = dataList.value.filter((item) => !successIds.includes(item.targetId));
      });
    }
  });
};

const toCancelBatchDelete = () => {
  rowSelection.value = undefined;
};

const tableSelect = (keys: string[]) => {
  if (!rowSelection.value) {
    return;
  }

  const currentIds = dataList.value.map(item => item.targetId);
  const deleteIds = currentIds.reduce((prev, cur) => {
    if (!keys.includes(cur)) {
      prev.push(cur);
    }

    return prev;
  }, [] as string[]);

  // 删除反选的数据集
  const selectedRowKeys = rowSelection.value.selectedRowKeys.filter(item => !deleteIds.includes(item));

  for (let i = 0, len = keys.length; i < len; i++) {
    if (!selectedRowKeys.includes(keys[i])) {
      selectedRowKeys.push(keys[i]);
    }
  }

  const selectedNum = selectedRowKeys.length;
  if (selectedNum > MAX_NUM) {
    notification.info(`最大支持批量删除 ${MAX_NUM} 个资源，当前已选中 ${selectedNum} 个资源。`);
  }

  rowSelection.value.selectedRowKeys = selectedRowKeys;
};

const refresh = () => {
  if (loading.value) {
    return;
  }

  loadData();
};

const loadData = async () => {
  loading.value = true;
  const [error, res] = await variable.getVariableTargetDetail(props.id);
  loading.value = false;
  loaded.value = true;
  if (error) {
    pagination.value.total = 0;
    dataList.value = [];
    if (rowSelection.value) {
      rowSelection.value = undefined;
    }

    return;
  }

  const data = res?.data || [];
  pagination.value.total = data.length;
  dataList.value = data;

  if (rowSelection.value) {
    rowSelection.value = undefined;
  }
};

onMounted(() => {
  watch(() => props.id, (newValue) => {
    if (!newValue) {
      return;
    }

    loadData();
  }, { immediate: true });
});

const selectedNum = computed(() => {
  return rowSelection.value?.selectedRowKeys?.length || 0;
});

const columns = [
  {
    title: '资源类型',
    dataIndex: 'targetType',
    width: '10%',
    ellipsis: true
  },
  {
    title: '资源ID',
    dataIndex: 'targetId',
    width: '20%',
    ellipsis: true
  },
  {
    title: '资源名称',
    dataIndex: 'targetName',
    ellipsis: true
  },
  {
    title: '操作',
    dataIndex: 'action',
    width: 70
  }
];
</script>

<template>
  <Spin :spinning="loading" class="text-3 leading-5">
    <div class="flex items-center justify-between mt-1 mb-2">
      <Hints text="查看使用数据集的资源信息。" />
      <div v-if="!rowSelection" class="flex items-center space-x-2.5">
        <Button
          v-if="dataList.length"
          :disabled="loading"
          size="small"
          type="text"
          class="flex items-center px-0 h-5 leading-5 border-0 text-theme-text-hover"
          @click="toBatchDelete">
          <Icon icon="icon-qingchu" class="text-3.5" />
          <span class="ml-1">批量删除</span>
        </Button>

        <Button
          :disabled="loading"
          size="small"
          type="text"
          class="flex items-center px-0 h-5 leading-5 border-0 text-theme-text-hover"
          @click="refresh">
          <Icon icon="icon-shuaxin" class="text-3.5" />
          <span class="ml-1">刷新</span>
        </Button>
      </div>

      <div v-else class="flex items-center space-x-2.5">
        <Button
          danger
          type="text"
          size="small"
          class="flex items-center px-0 h-5 leading-5 border-0"
          @click="toBatchDelete">
          <Icon icon="icon-qingchu" class="mr-1 text-3.5" />
          <div class="flex items-center">
            <span class="mr-0.5">删除选中</span>
            <span>({{ selectedNum }})</span>
          </div>
        </Button>

        <Button
          type="text"
          size="small"
          class="flex items-center px-0 h-5 leading-5 border-0 text-theme-text-hover"
          @click="toCancelBatchDelete">
          <Icon icon="icon-fanhui" class="mr-1" />
          <span>取消删除</span>
        </Button>
      </div>
    </div>

    <template v-if="loaded">
      <NoData
        v-if="!dataList.length"
        size="small"
        class="my-4" />

      <Table
        v-else
        rowKey="targetId"
        class="flex-1"
        :dataSource="dataList"
        :columns="columns"
        :pagination="pagination"
        :rowSelection="rowSelection">
        <template #bodyCell="{ column, record }">
          <div v-if="column.dataIndex === 'targetType'" class="truncate">
            {{ record.targetType?.message }}
          </div>

          <Button
            v-else-if="column.dataIndex === 'action'"
            :disabled="!!rowSelection?.selectedRowKeys?.length"
            type="text"
            size="small"
            class="flex items-center px-0"
            @click="toDelete(record)">
            <Icon icon="icon-qingchu" class="mr-1 text-3.5" />
            <span>删除</span>
          </Button>
        </template>
      </Table>
    </template>
  </Spin>
</template>
<style scoped>
:deep(.ant-pagination) {
  margin-bottom: 0;
}
</style>
