<script lang="ts" setup>
import { ref } from 'vue';
import { Icon } from '@xcan-angus/vue-ui';
import ExpandGrid from './expandGrid.vue';
import { Button, Table } from 'ant-design-vue';

const editable = ref(false);

const columns = [
  {
    dataIndex: 'level',
    title: '等级'
  },
  {
    dataIndex: 'workload',
    title: '工作量'
  },
  {
    dataIndex: 'completedRate',
    title: '完成率(小于等于)'
  },
  {
    dataIndex: 'overdueRate',
    title: '逾期率(小于等于)'
  },
  {
    dataIndex: 'oneTimePassedRate',
    title: '一次性通过率(大于等于)'
  },
  {
    dataIndex: 'savingWorkloadRate',
    title: '工作量节省率(大于等于)'
  }
];
const data = ref([
  { level: '极差', workload: '100', completedRate: '35%', overdueRate: '35%', oneTimePassedRate: '30%', savingWorkloadRate: '0%' },
  { level: '差', workload: '100', completedRate: '50%', overdueRate: '25%', oneTimePassedRate: '45%', savingWorkloadRate: '0%' },
  { level: '中等', workload: '100', completedRate: '65%', overdueRate: '15%', oneTimePassedRate: '60%', savingWorkloadRate: '10%' },
  { level: '良好', workload: '100', completedRate: '90%', overdueRate: '5%', oneTimePassedRate: '85%', savingWorkloadRate: '20%' },
  { level: '优秀', workload: '100', completedRate: '100%', overdueRate: '2%', oneTimePassedRate: '90%', savingWorkloadRate: '30%' }
]);

</script>
<template>
  <ExpandGrid title="平台默认效能指标">
    <template #button>
      <div class="text-3 flex items-center">
        <template v-if="editable">
          <span class="cursor-pointer"><Icon icon="icon-zhongzhi2" class="mr-1" />取消</span>
          <Button
            type="text"
            class="ml-2 text-3 py-0 h-5">
            <Icon icon="icon-baocun" class="mr-1" />保存
          </Button>
        </template>
      </div>
    </template>
    <Table
      :columns="columns"
      :dataSource="data"
      size="small"
      :pagination="false">
    </Table>
  </ExpandGrid>
</template>
