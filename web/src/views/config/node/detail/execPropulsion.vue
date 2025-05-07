<script lang="ts" setup>
import { inject, onMounted, ref } from 'vue';
import { modal, notification, Table } from '@xcan-angus/vue-ui';
import { Button } from 'ant-design-vue';
import { node } from '@/api/tester';

interface Props {
  nodeId: string;
  tenantId: string;
}

const props = withDefaults(defineProps<Props>(), {
  nodeId: '',
  tenantId: undefined
});

const tenantInfo = inject('tenantInfo', ref({}));

const dataSource = ref({});

const dataList = ref([]);

const loading = ref(false);
const loadNodeProcess = async () => {
  if (loading.value) {
    return;
  }
  const [error, { data = {} }] = await node.loadRunnerProcess({
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
    title: '进程ID',
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
    title: '运行时间',
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
    title: '用户',
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
    title: '可使用虚拟内存',
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
    title: '线程数',
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
    title: '打开文件数',
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
    title: '写磁盘',
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
    title: '读磁盘',
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
    title: '操作',
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
    content: `确认强制退出进程【${item.processID}】？`,
    async onOk () {
      const [error] = await node.killRunnerProcess({
        nodeId: props.nodeId,
        broadcast: true,
        pid: item.processID
      });
      if (error) {
        return;
      }
      notification.success('进程退出成功');
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
        <div class="text-theme-title font-medium">进程数</div>
      </div>
      <div class="text-center p-2 rounded">
        <div class="text-6 font-semibold text-text-sub-content">{{ dataSource.threadCount || '--' }}</div>
        <div class="text-theme-title font-medium">线程数</div>
      </div>
      <div class="text-center p-2 rounded">
        <div class="text-6 font-semibold text-text-sub-content">{{ dataSource.openFiles || '--' }}</div>
        <div class="text-theme-title font-medium">打开文件数</div>
      </div>
      <div class="text-center p-2 rounded">
        <div class="text-6 font-semibold text-text-sub-content">{{ dataSource.bytesWritten || '--' }}</div>
        <div class="text-theme-title font-medium">写磁盘</div>
      </div>
      <div class="text-center p-2 rounded">
        <div class="text-6 font-semibold text-text-sub-content">{{ dataSource.bytesRead || '--' }}</div>
        <div class="text-theme-title font-medium">读磁盘</div>
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
            :disabled="tenantId !== tenantInfo?.tenantId"
            type="link"
            size="small"
            @click="killProcess(record)">
            强制退出(kill)
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
