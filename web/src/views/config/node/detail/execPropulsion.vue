<script lang="ts" setup>
import { inject, onMounted, ref } from 'vue';
import { modal, notification, Table } from '@xcan-angus/vue-ui';
import { Button } from 'ant-design-vue';
import { node } from '@/api/tester';
import { appContext } from '@xcan-angus/infra';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

interface Props {
  nodeId: string;
  tenantId: string;
}

const props = withDefaults(defineProps<Props>(), {
  nodeId: '',
  tenantId: undefined
});

const tenantInfo = ref(appContext.getTenant());

const dataSource = ref({});

const dataList = ref([]);

const loading = ref(false);
const loadNodeProcess = async () => {
  if (loading.value) {
    return;
  }
  const [error, { data = {} }] = await node.getRunnerProcess({
    nodeId: props.nodeId,
    broadcast: true
  });
  loading.value = false;
  if (error) {
    return;
  }
  dataSource.value = data || {};
  dataList.value = (data?.processes || []).reduce((pre, current) => {
    pre.push(current, current);
    return pre;
  }, []);
};

const columns = [
  {
    title: t('node.nodeDetail.execPropulsion.columns.processId'),
    dataIndex: 'processID',
    customCell: (_, idx) => {
      if (idx % 2 === 0) {
        return {
          rowSpan: 2
        };
      }
      return {
        rowSpan: 0
      };
    }
  },
  {
    title: t('node.nodeDetail.execPropulsion.columns.runTime'),
    dataIndex: 'upTime',
    customCell: (_, idx) => {
      if (idx % 2 === 0) {
        return {
          colSpan: 1
        };
      }
      return {
        colSpan: 7
      };
    },
    customRender: ({ record, index }) => {
      if (index % 2 === 0) {
        return record.upTime;
      } else {
        const str = record.commandLine;
        return str.replaceAll('\u0000', ' ');
      }
    }
  },
  {
    title: t('node.nodeDetail.execPropulsion.columns.user'),
    dataIndex: 'user',
    customCell: (_, idx) => {
      if (idx % 2 === 0) {
        return {
          colSpan: 1
        };
      }
      return {
        colSpan: 0
      };
    }
  },
  {
    title: t('node.nodeDetail.execPropulsion.columns.virtualMemory'),
    dataIndex: 'virtualSize',
    customCell: (_, idx) => {
      if (idx % 2 === 0) {
        return {
          colSpan: 1
        };
      }
      return {
        colSpan: 0
      };
    }
  },
  {
    title: t('node.nodeDetail.execPropulsion.columns.threadCount'),
    dataIndex: 'threadCount',
    customCell: (_, idx) => {
      if (idx % 2 === 0) {
        return {
          colSpan: 1
        };
      }
      return {
        colSpan: 0
      };
    }
  },
  {
    title: t('node.nodeDetail.execPropulsion.columns.openFiles'),
    dataIndex: 'openFiles',
    customCell: (_, idx) => {
      if (idx % 2 === 0) {
        return {
          colSpan: 1
        };
      }
      return {
        colSpan: 0
      };
    }
  },
  {
    title: t('node.nodeDetail.execPropulsion.columns.writeDisk'),
    dataIndex: 'bytesWritten',
    customCell: (_, idx) => {
      if (idx % 2 === 0) {
        return {
          colSpan: 1
        };
      }
      return {
        colSpan: 0
      };
    }
  },
  {
    title: t('node.nodeDetail.execPropulsion.columns.readDisk'),
    dataIndex: 'bytesRead',
    customCell: (_, idx) => {
      if (idx % 2 === 0) {
        return {
          colSpan: 1
        };
      }
      return {
        colSpan: 0
      };
    }
  },
  {
    title: t('node.nodeDetail.execPropulsion.columns.action'),
    dataIndex: 'action',
    customCell: (_, idx) => {
      if (idx % 2 === 0) {
        return {
          rowSpan: 2
        };
      }
      return {
        rowSpan: 0
      };
    }
  }
];

const killProcess = (item) => {
  modal.confirm({
    content: `${t('node.nodeDetail.execPropulsion.confirmKillProcess', { processId: item.processID })}`,
    async onOk () {
      const [error] = await node.killRunnerProcess({
        nodeId: props.nodeId,
        broadcast: true,
        pid: item.processID
      });
      if (error) {
        return;
      }
      notification.success(t('node.nodeDetail.execPropulsion.processExitSuccess'));
      loadNodeProcess();
    }
  });
};

onMounted(() => {
  if (props.nodeId) {
    loadNodeProcess();
  }
});

</script>
<template>
  <div class="text-3">
    <div class="flex space-x-8 pb-3">
      <div class="text-center p-2 rounded">
        <div class="text-6 font-semibold text-text-sub-content">{{ dataSource.processCount || '--' }}</div>
        <div class="text-theme-title font-medium">{{ t('node.nodeDetail.execPropulsion.processCount') }}</div>
      </div>
      <div class="text-center p-2 rounded">
        <div class="text-6 font-semibold text-text-sub-content">{{ dataSource.threadCount || '--' }}</div>
        <div class="text-theme-title font-medium">{{ t('node.nodeDetail.execPropulsion.threadCount') }}</div>
      </div>
      <div class="text-center p-2 rounded">
        <div class="text-6 font-semibold text-text-sub-content">{{ dataSource.openFiles || '--' }}</div>
        <div class="text-theme-title font-medium">{{ t('node.nodeDetail.execPropulsion.openFiles') }}</div>
      </div>
      <div class="text-center p-2 rounded">
        <div class="text-6 font-semibold text-text-sub-content">{{ dataSource.bytesWritten || '--' }}</div>
        <div class="text-theme-title font-medium">{{ t('node.nodeDetail.execPropulsion.writeDisk') }}</div>
      </div>
      <div class="text-center p-2 rounded">
        <div class="text-6 font-semibold text-text-sub-content">{{ dataSource.bytesRead || '--' }}</div>
        <div class="text-theme-title font-medium">{{ t('node.nodeDetail.execPropulsion.readDisk') }}</div>
      </div>
    </div>
    <Table
      bordered
      :loading="loading"
      :columns="columns"
      :dataSource="dataList"
      :pagination="false"
      class="propulsion_table"
      size="small"
      noDataSize="small">
      <template #bodyCell="{record, column}">
        <template v-if="column.dataIndex === 'action'">
          <Button
            :disabled="tenantId !== tenantInfo?.id"
            type="link"
            size="small"
            @click="killProcess(record)">
            {{ t('node.nodeDetail.execPropulsion.killProcess') }}
          </Button>
        </template>
      </template>
    </Table>
  </div>
</template>
<style scoped>
:deep(.propulsion_table.table-empty) .ant-table.ant-table-bordered>.ant-table-container{
  border-left: 0 !important;
}
</style>
