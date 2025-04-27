<script setup lang="ts">
import { ref, watch, computed } from 'vue';
import { TableColumnType } from 'ant-design-vue';
import { utils } from '@xcan-angus/tools';
import { Icon, Table } from '@xcan-angus/vue-ui';

interface Props {
  isSingleInterface:boolean;
  columns: TableColumnType[];
  tableData: Record<string, any>[];
  brpsUnit:'KB' | 'MB';
  minBrpsUnit: 'KB' | 'MB';
  maxBrpsUnit:'KB' | 'MB';
  meanBrpsUnit:'KB' | 'MB';
  bwpsUnit:'KB' | 'MB';
  minBwpsUnit: 'KB' | 'MB';
  maxBwpsUnit:'KB' | 'MB';
  meanBwpsUnit:'KB' | 'MB';
  writeBytesUnit?: 'KB' | 'MB';
  tabKey?: string;
}

const props = withDefaults(defineProps<Props>(), {
  isSingleInterface: false,
  columns: () => [],
  tableData: () => [],
  brpsUnit: 'KB',
  bwpsUnit: 'KB',
  writeBytesUnit: 'KB',
  tabKey: ''
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
const isSaveExpand = ref(false);
const idsMap = ref({});

watch(() => props.tableData, (newValue) => {
  if (newValue.length) {
    if (!isSaveExpand.value) {
      expandedRowKeys.value = [];
    }
    dataSource.value = newValue.map(item => {
      if (!isSaveExpand.value) {
        if (!idsMap.value[item.name]) {
          idsMap.value[item.name] = utils.uuid();
        }
      }
      item.id = idsMap.value[item.name];
      if (item.list && Array.isArray(item.list) && item.list.length > 0) {
        if (!isSaveExpand.value) {
          expandedRowKeys.value.push(idsMap.value[item.name]);
        }
        item.list = item.list.map(item1 => {
          item1.id = utils.uuid();
          return item1;
        });
      }
      return item;
    });
  } else {
    isSaveExpand.value = false;
    idsMap.value = {};
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

  isSaveExpand.value = true;
};

const childTablepColumns = computed(() => {
  return props.columns.map(item => {
    if (item.dataIndex === 'name') {
      return {

        ...item,
        customCell: () => {
          return { style: 'padding-left: 20px' };
        }
      };
    } else {
      return item;
    }
  });
});

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
    <template #bodyCell="{column, text, record}">
      <template v-if="column.dataIndex === 'brps'">
        <template v-if="props.isSingleInterface && props.tabKey === 'aggregate'">
          <div class="flex items-cneter space-x-3">
            <span>{{ text ? `${text}(${props.brpsUnit})` : '--' }}</span>
            <span>|</span>
            <span>{{ record.minBrps ? `${record.minBrps}(${props.minBrpsUnit})` : '--' }}</span>
            <span>|</span>
            <span>{{ record.maxBrps ? `${record.maxBrps}(${props.maxBrpsUnit})` : '--' }}</span>
            <span>|</span>
            <span>{{ record.meanBrps ? `${record.meanBrps}(${props.meanBrpsUnit})` : '--' }}</span>
          </div>
        </template>
      </template>
      <template v-if="column.dataIndex === 'bwps'">
        <template v-if="props.isSingleInterface && props.tabKey === 'aggregate'">
          <div class="flex items-cneter space-x-3">
            <span>{{ text ? `${text}(${props.bwpsUnit})` : '--' }}</span>
            <span>|</span>
            <span>{{ record.minBwps ? `${record.minBwps}(${props.minBwpsUnit})` : '--' }}</span>
            <span>|</span>
            <span>{{ record.maxBwps ? `${record.maxBwps}(${props.maxBwpsUnit})` : '--' }}</span>
            <span>|</span>
            <span>{{ record.meanBwps ? `${record.meanBwps}(${props.meanBwpsUnit})` : '--' }}</span>
          </div>
        </template>
      </template>

      <template v-if="column.dataIndex === 'writeBytes'">
        {{ text === '--'?text:text+props.writeBytesUnit }}
      </template>
      <template v-if="column.dataIndex === 'errorRate'">
        {{ text === '--'?text:text+'%' }}
      </template>
    </template>
    <template #expandedRowRender="{record}">
      <template v-if="record.list?.length">
        <Table
          class="group-table"
          rowKey="name"
          :columns="childTablepColumns"
          :data-source="record.list"
          :rowExpandable="() => false"
          :pagination="false"
          :showHeader="false">
          <template #bodyCell="{column:c_column, text:c_text}">
            <template v-if="c_column.dataIndex === 'writeBytes'">
              {{ c_text === '--'?c_text:c_text+props.writeBytesUnit }}
            </template>
            <template v-if="c_column.dataIndex === 'errorRate'">
              {{ c_text === '--'?c_text:c_text+'%' }}
            </template>
          </template>
          <template #expandedRowRender />
        </Table>
      </template>
    </template>
  </Table>
</template>
<style scoped>
.group-table :deep(.ant-table){
  margin: 3.5px  -8px !important;
  font-size: 12px;
}

:deep(.ant-table .last-row > td){
  background-color:  #FAFCFC;
}

</style>
