<script setup lang="ts">
import { ref, watch } from 'vue';
import { Icon, Table } from '@xcan-angus/vue-ui';
import { TableColumnType } from 'ant-design-vue';
import { utils } from '@xcan-angus/tools';

interface Props {
  columns: TableColumnType[];
  tableData: Record<string, any>[];
  brpsUnit?: 'KB' | 'MB';
  bwpsUnit?: 'KB' | 'MB';
  writeBytesUnit?: 'KB' | 'MB';
}

const props = withDefaults(defineProps<Props>(), {
  columns: () => [],
  tableData: () => [],
  brpsUnit: 'KB',
  bwpsUnit: 'KB',
  writeBytesUnit: 'KB'
});

const customRow = (record) => {
  if (record.name === 'Total') {
    return {
      class: 'last-row'
    };
  }
  return {};
};

const expandedRowKeys = ref<string[]>([]);
const dataSource = ref<Record<string, any>[]>([]);

watch(() => props.tableData, (newValue) => {
  if (newValue.length) {
    expandedRowKeys.value = [];
    dataSource.value = newValue.map(item => {
      item.id = utils.uuid();
      if (item.list && Array.isArray(item.list) && item.list.length > 0) {
        expandedRowKeys.value.push(item.id);
        item.list = item.list.map(item => {
          item.id = utils.uuid();
          return item;
        });
      }
      return item;
    });
  }
}, {
  immediate: true
});

const handleExpand = (record) => {
  const _id = record.record.id;
  if (record.expanded) {
    expandedRowKeys.value = expandedRowKeys.value.filter(item => item !== _id);
  } else {
    if (!expandedRowKeys.value.includes(_id)) {
      expandedRowKeys.value.push(_id);
    }
  }
};

</script>
<template>
  <Table
    v-model:expandedRowKeys="expandedRowKeys"
    rowKey="id"
    size="small"
    defaultExpandAllRows
    :columns="props.columns"
    :dataSource="dataSource"
    :pagination="false"
    :rowExpandable="(record) => record?.list?.length"
    :customRow="customRow">
    <template #expandIcon="record">
      <Icon
        v-if="record.record.list?.length"
        :icon="record.expanded?'icon-xiangxia':'icon-xiangshang'"
        class="text-3 text-text-sub-content cursor-pointer ml-1.5"
        @click="handleExpand(record)" />
    </template>
    <template #bodyCell="{column, text}">
      <template v-if="column.dataIndex === 'brps'">
        {{ text+props.brpsUnit }}
      </template>
      <template v-if="column.dataIndex === 'bwps'">
        {{ text+props.bwpsUnit }}
      </template>
      <template v-if="column.dataIndex === 'writeBytes'">
        {{ text+props.writeBytesUnit }}
      </template>
      <template v-if="column.dataIndex === 'errorRate'">
        {{ text+'%' }}
      </template>
    </template>
    <template #expandedRowRender="{record}">
      <template v-if="record.list?.length">
        <Table
          class="group-table"
          rowKey="name"
          :columns="props.columns"
          :data-source="record.list"
          :rowExpandable="() => false"
          :pagination="false"
          :showHeader="false">
          <template #expandedRowRender />
          <template #bodyCell="{column, text}">
            <template v-if="column.dataIndex === 'brps'">
              {{ text+props.brpsUnit }}
            </template>
            <template v-if="column.dataIndex === 'bwps'">
              {{ text+props.bwpsUnit }}
            </template>
            <template v-if="column.dataIndex === 'writeBytes'">
              {{ text+props.writeBytesUnit }}
            </template>
            <template v-if="column.dataIndex === 'errorRate'">
              {{ text+'%' }}
            </template>
          </template>
        </Table>
      </template>
    </template>
  </Table>
</template>
<style scoped>
.group-table :deep(.ant-table){
  margin: 3.5px -7px 3.5px -8px !important;
  font-size: 12px;
}

:deep(.ant-table .last-row > td){
  background-color:  #FAFCFC;
}

</style>
