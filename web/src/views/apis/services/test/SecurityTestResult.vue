<script lang="ts" setup>
import { Grid, Icon } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';

interface Props {
    dataSource: Record<string, any>
}

const props = withDefaults(defineProps<Props>(), {
  dataSource: () => ({})
});
const { t } = useI18n();
const columns = [
  [
    { dataIndex: 'testByName', label: t('service.service.securityResult.testName') },
    { dataIndex: 'testByDate', label: t('service.service.securityResult.testerDate') }
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
      <Icon icon="icon-anquanceshi" class="text-5" />
      <span class="text-4 font-semibold">{{ t('service.service.securityResult.title') }}</span>
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
