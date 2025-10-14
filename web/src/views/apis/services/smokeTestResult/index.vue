<script lang="ts" setup>
import { Grid, Icon } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';

interface Props {
    dataSource: Record<string, any>
}

const { t } = useI18n();
const props = withDefaults(defineProps<Props>(), {
  dataSource: () => ({})
});

const columns = [
  [
    { dataIndex: 'serviceId', label: t('common.id') },
    { dataIndex: 'serviceName', label: t('common.name') }
  ],
  [
    { dataIndex: 'testByName', label: t('service.sidebar.smokeResult.testName') },
    { dataIndex: 'testByDate', label: t('service.sidebar.smokeResult.testDate') }
  ],
  [
    { dataIndex: 'scriptId', label: t('common.scriptId') },
    { dataIndex: 'scriptName', label: t('common.scriptName') }
  ]
];

</script>
<template>
  <div class="p-5">
    <div class="flex items-center space-x-2">
      <Icon icon="icon-maoyanceshi" class="text-5" />
      <span class="text-4 font-semibold">{{ t('service.sidebar.smokeResult.title') }}</span>
    </div>
    <div class="flex items-center space-x-5 mt-2">
      <div class="rounded-full w-14 h-14 border text-5 text-center flex flex-col justify-center">
        {{ t('status.passed') }}
      </div>
      <Grid
        class="w-120"
        :columns="columns"
        :dataSource="props.dataSource" />
    </div>
  </div>
</template>
