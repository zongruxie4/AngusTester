<script lang="ts" setup>
import { useI18n } from 'vue-i18n';
import { Icon, Tooltip, Grid } from '@xcan-angus/vue-ui';

const { t } = useI18n();

interface ScriptItem {
  name: string;
  id: string;
}

interface Props {
  dataSource: ScriptItem[];
}

// Component props with default values
const props = withDefaults(defineProps<Props>(), {
  dataSource: () => ([])
});

// Script tooltip table configuration
const scriptTooltipTableColumns = [
  [
    { dataIndex: 'description', label: t('common.description') },
    { dataIndex: 'scenarioId', label: t('xcan_httpTestInfo.associatedScenarioId') },
    { dataIndex: 'scenarioName', label: t('xcan_httpTestInfo.associatedScenarioName') },
    { dataIndex: 'createdByName', label: t('common.creator') },
    { dataIndex: 'createdDate', label: t('xcan_httpTestInfo.creationTime') }
  ]
];
</script>
<template>
  <div class="text-3">
    <div
      v-for="scriptItem in props.dataSource"
      :key="scriptItem.id"
      class="border border-gray-light rounded bg-gray-light">
      <Tooltip placement="rightTop">
        <template #title>
          <div class="max-h-100 overflow-y-auto">
            <Grid
              :dataSource="scriptItem || {}"
              :columns="scriptTooltipTableColumns" />
          </div>
        </template>
        <div class="px-1 flex h-6 items-center">
          <Icon icon="icon-ceshijiaoben" class="mr-1" />
          <span class="flex min-w-0 truncate flex-1"><a :href="`/scripts?id=${scriptItem.id}`">{{ scriptItem.name }}</a></span>
        </div>
      </Tooltip>
    </div>
  </div>
</template>
