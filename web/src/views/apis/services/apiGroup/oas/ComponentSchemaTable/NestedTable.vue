<script lang="ts">
import { ref, watch } from 'vue';
import { Arrow, IconRequired, Table } from '@xcan-angus/vue-ui';

import ArrayExpandedRowRender from '../ArrayExpandedRowRender.vue';
import IntegerExpandedRowRender from '../IntegerExpandedRowRender.vue';
import StringExpandedRowRender from '../StringExpandedRowRender.vue';
import BooleanExpandedRowRender from '../BooleanExpandedRowRender.vue';

const columns = [
  {
    title: '参数',
    key: 'name',
    dataIndex: 'name',
    ellipsis: true
  },
  {
    title: '类型',
    key: 'type',
    dataIndex: 'type',
    ellipsis: true
  },
  {
    title: '格式',
    key: 'format',
    dataIndex: 'format',
    ellipsis: true
  },
  {
    title: '示例值',
    key: 'x-xc-example',
    dataIndex: 'x-xc-example',
    ellipsis: true
  },
  {
    title: '描述',
    key: 'description',
    dataIndex: 'description',
    ellipsis: true
  }
];

export default {
  name: 'NestedTable',
  components: {
    Table,
    Arrow,
    IconRequired,
    ArrayExpandedRowRender,
    IntegerExpandedRowRender,
    StringExpandedRowRender,
    BooleanExpandedRowRender
  },
  props: {
    showHeader: {
      type: Boolean,
      required: false,
      default: true
    },
    dataSource: {
      type: Array<any>,
      required: true
    }
  },
  setup (props) {
    const expandedRowKeys = ref<string[]>([]);

    const arrowChange = (open: boolean, id: string) => {
      if (open) {
        expandedRowKeys.value.push(id);
        return;
      }

      expandedRowKeys.value = expandedRowKeys.value.filter(item => item !== id);
    };

    watch(() => props.dataSource, () => {
      expandedRowKeys.value = [];
    });

    const emptyTextStyle = {
      margin: '10px 0'
    };

    return {
      expandedRowKeys,
      arrowChange,
      columns,
      emptyTextStyle
    };
  }
};
</script>

<template>
  <Table
    v-model:expandedRowKeys="expandedRowKeys"
    noDataSize="small"
    :columns="columns"
    :dataSource="dataSource"
    :showHeader="showHeader"
    :pagination="false"
    :emptyTextStyle="emptyTextStyle"
    class="xc-nested-table"
    rowKey="id">
    <template #bodyCell="{ record, column }">
      <div
        v-if="column.dataIndex === 'name'"
        :title="record.name"
        class="flex items-center overflow-hidden">
        <IconRequired v-if="record.required" class="flex-shrink-0" />
        <span class="truncate">{{ record.name }}</span>
      </div>
    </template>

    <template #expandedRowRender="{ record }">
      <template v-if="record.type === 'object'">
        <NestedTable
          :dataSource="record['x-xc-children']"
          :showHeader="true" />
      </template>
      <template v-else-if="record.type === 'array'">
        <ArrayExpandedRowRender :value="record" />
        <NestedTable
          :dataSource="record['x-xc-children']"
          :showHeader="true" />
      </template>
      <IntegerExpandedRowRender v-else-if="record.type === 'integer'||record.type === 'number'" :value="record" />
      <StringExpandedRowRender v-else-if="record.type === 'string'" :value="record" />
      <BooleanExpandedRowRender v-else-if="record.type === 'boolean'" :value="record" />
    </template>

    <template #expandIcon="{ record }">
      <Arrow
        v-if="record.type === 'array' || record.type === 'object'"
        :open="expandedRowKeys.includes(record.id)"
        @change="arrowChange($event, record.id)" />
      <template v-else>
        <button
          v-if="expandedRowKeys.includes(record.id)"
          type="button"
          class="ant-table-row-expand-icon ant-table-row-expand-icon-expanded"
          @click="arrowChange(false, record.id)"></button>
        <button
          v-else
          type="button"
          class="ant-table-row-expand-icon ant-table-row-expand-icon-collapsed"
          @click="arrowChange(true, record.id)"></button>
      </template>
    </template>
  </Table>
</template>
