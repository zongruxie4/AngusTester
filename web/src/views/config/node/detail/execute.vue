<script lang="ts" setup>
import { ref, watch } from 'vue';
import { Table } from '@xcan-angus/vue-ui';
import { nodeCtrl } from 'src/api/ctrl';

interface Props {
  id: string; // node id
}

const props = withDefaults(defineProps<Props>(), {
  id: ''
});

const columns = [
  {
    dataIndex: 'id',
    title: '执行ID'
  },
  {
    dataIndex: 'name',
    title: '执行名称'
  },

  {
    dataIndex: 'scriptType',
    title: '测试类型'
  },
  {
    dataIndex: 'plugin',
    title: '插件'
  },
  {
    dataIndex: 'execByName',
    title: '执行人'
  },
  {
    dataIndex: 'actualStartDate',
    title: '开始时间'
  }
];

const dataSource = ref<{[key: string]: string}[]>([]);

watch(() => props.id, async newValue => {
  if (newValue) {
    const [error, { data }] = await nodeCtrl.getNodeExec(props.id);
    if (error) {
      return;
    }
    dataSource.value = data || [];
  }
}, {
  immediate: true
});

</script>
<template>
  <Table
    :dataSource="dataSource"
    :columns="columns"
    :pagination="false"
    size="small"
    noDataSize="small">
    <template #bodyCell="{ column, record }">
      <template v-if="column.dataIndex === 'name'">
        <a
          class="text-theme-special"
          :href="`/execution/info/${record.id}`"
          target="_blank">{{ record.name || '--' }}</a>
      </template>
      <template v-if="column.dataIndex === 'scriptType'">
        {{ record.scriptType?.message }}
      </template>
    </template>
  </Table>
</template>
