<script lang="ts" setup>
import { ref, watch } from 'vue';
import { Table } from '@xcan-angus/vue-ui';
import { nodeCtrl } from 'src/api/ctrl';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

interface Props {
  id: string; // node id
}

const props = withDefaults(defineProps<Props>(), {
  id: ''
});

const columns = [
  {
    dataIndex: 'id',
    title: t('node.nodeDetail.execute.columns.execId')
  },
  {
    dataIndex: 'name',
    title: t('node.nodeDetail.execute.columns.execName')
  },

  {
    dataIndex: 'scriptType',
    title: t('node.nodeDetail.execute.columns.testType')
  },
  {
    dataIndex: 'plugin',
    title: t('node.nodeDetail.execute.columns.plugin')
  },
  {
    dataIndex: 'execByName',
    title: t('node.nodeDetail.execute.columns.executor')
  },
  {
    dataIndex: 'actualStartDate',
    title: t('node.nodeDetail.execute.columns.startTime')
  }
];

const dataSource = ref<{[key: string]: string}[]>([]);

watch(() => props.id, async newValue => {
  if (newValue) {
    const [error, { data }] = await nodeCtrl.getExecInfo(props.id);
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
