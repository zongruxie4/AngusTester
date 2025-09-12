<script lang="ts" setup>
import { inject, ref } from 'vue';
import { Icon, Tooltip, Grid } from '@xcan-angus/vue-ui';
// import { Tooltip } from 'ant-design-vue';
import { useI18n } from 'vue-i18n';

const { t } = useI18n();

interface API {
  name: string;
  id: string;
  code: string;
  serviceId: string;
  serviceName: string;
  taskType: 'API_TEST'| 'BUG'| 'SCENARIO_TEST'| 'STORY'| 'TASK';
  targetId: string;
}

interface Props {
  dataSource: API[]
}

const projectInfo = inject('projectInfo', ref({ id: '' }));

const getTaskIcon = (taskType: string) => {
  switch (taskType) {
    case 'API_TEST':
      return 'icon-jiekouceshi';
    case 'SCENARIO_TEST':
      return 'icon-changjingceshi';
    case 'BUG':
      return 'icon-quexian';
    case 'STORY':
      return 'icon-gushi';
    case 'TASK':
      return 'icon-renwu1';
    default:
      return 'icon-renwu1';
  }
};

const columns = [
  [
    {
      dataIndex: 'sprintName',
      label: t('xcan_httpTestInfo.sprintName')
    },
    {
      dataIndex: 'assigneeName',
      label: t('xcan_httpTestInfo.assigneeName')
    },
    {
      dataIndex: 'confirmerName',
      label: t('xcan_httpTestInfo.confirmerName')
    },
    { dataIndex: 'createdByName', label: t('xcan_httpTestInfo.createdByName') },
    { dataIndex: 'createdDate', label: t('xcan_httpTestInfo.createdDate') }
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
          <Icon :icon="getTaskIcon(item.taskType)" />
          <span class="min-w-0 truncate flex-1" :title="item.name"><a target="_blank" :href="`/tasks#tasks?projectId=${projectInfo.id}&taskId=${item.targetId}&taskName=${item.name}`">{{ item.name }}</a></span>
        </div>
      </Tooltip>
    </div>
  </div>
</template>
