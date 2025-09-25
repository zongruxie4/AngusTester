<script lang="ts" setup>
import { Icon, Tooltip, Grid } from '@xcan-angus/vue-ui';
// import { Tooltip } from 'ant-design-vue';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

interface API {
  name: string;
  id: string;
}

interface Props {
  dataSource: API[]
}

const columns = [
  [
    { dataIndex: 'description', label: t('common.description') },
    { dataIndex: 'scenarioId', label: t('xcan_httpTestInfo.associatedScenarioId') },
    { dataIndex: 'scenarioName', label: t('xcan_httpTestInfo.associatedScenarioName') },
    { dataIndex: 'createdByName', label: t('xcan_httpTestInfo.creator') },
    { dataIndex: 'createdDate', label: t('xcan_httpTestInfo.creationTime') }
  ]
];

const props = withDefaults(defineProps<Props>(), {
  dataSource: () => ([])
});

</script>
<template>
  <div class="text-3">
    <div
      v-for="item in props.dataSource"
      :key="item.id"
      class="border border-gray-light rounded bg-gray-light">
      <Tooltip placement="rightTop">
        <template #title>
          <div class="max-h-100 overflow-y-auto">
            <Grid
              :dataSource="item || {}"
              :columns="columns" />
          </div>
        </template>
        <div class="px-1 flex h-6 items-center">
          <Icon icon="icon-ceshijiaoben" class="mr-1" />
          <span class="flex min-w-0 truncate flex-1"><a :href="`/scripts?id=${item.id}`">{{ item.name }}</a></span>
        </div>
      </Tooltip>
    </div>
  </div>
</template>
