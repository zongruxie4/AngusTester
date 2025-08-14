<script lang="ts" setup>
import { ref } from 'vue';
import { Icon } from '@xcan-angus/vue-ui';
import ExpandGrid from './expandGrid.vue';
import { Button, Table } from 'ant-design-vue';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

const editable = ref(false);

const columns = [
  {
    dataIndex: 'level',
    title: t('indicator.efficiency.table.columns.level')
  },
  {
    dataIndex: 'workload',
    title: t('indicator.efficiency.table.columns.workload')
  },
  {
    dataIndex: 'completedRate',
    title: t('indicator.efficiency.table.columns.completedRate')
  },
  {
    dataIndex: 'overdueRate',
    title: t('indicator.efficiency.table.columns.overdueRate')
  },
  {
    dataIndex: 'oneTimePassedRate',
    title: t('indicator.efficiency.table.columns.oneTimePassedRate')
  },
  {
    dataIndex: 'savingWorkloadRate',
    title: t('indicator.efficiency.table.columns.savingWorkloadRate')
  }
];
const data = ref([
  { level: t('indicator.efficiency.table.data.levels.veryPoor'), workload: '100', completedRate: '35%', overdueRate: '35%', oneTimePassedRate: '30%', savingWorkloadRate: '0%' },
  { level: t('indicator.efficiency.table.data.levels.poor'), workload: '100', completedRate: '50%', overdueRate: '25%', oneTimePassedRate: '45%', savingWorkloadRate: '0%' },
  { level: t('indicator.efficiency.table.data.levels.medium'), workload: '100', completedRate: '65%', overdueRate: '15%', oneTimePassedRate: '60%', savingWorkloadRate: '10%' },
  { level: t('indicator.efficiency.table.data.levels.good'), workload: '100', completedRate: '90%', overdueRate: '5%', oneTimePassedRate: '85%', savingWorkloadRate: '20%' },
  { level: t('indicator.efficiency.table.data.levels.excellent'), workload: '100', completedRate: '100%', overdueRate: '2%', oneTimePassedRate: '90%', savingWorkloadRate: '30%' }
]);

</script>
<template>
  <ExpandGrid :title="t('settings.indicator.efficiency.title')">
    <template #button>
      <div class="text-3 flex items-center">
        <template v-if="editable">
          <span class="cursor-pointer"><Icon icon="icon-zhongzhi2" class="mr-1" />{{ t('actions.cancel') }}</span>
          <Button
            type="text"
            class="ml-2 text-3 py-0 h-5">
            <Icon icon="icon-baocun" class="mr-1" />{{ t('actions.save') }}
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
