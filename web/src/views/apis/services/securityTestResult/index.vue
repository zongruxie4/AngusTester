<script lang="ts" setup>
import { Grid, Icon } from '@xcan-angus/vue-ui';

interface Props {
    dataSource: Record<string, any>
}

const props = withDefaults(defineProps<Props>(), {
  dataSource: () => ({})
});

const columns = [
  [
    { dataIndex: 'testByName', label: '测试人' },
    { dataIndex: 'testByDate', label: '测试时间' }
  ],
  [
    { dataIndex: 'scriptId', label: '脚本ID' },
    { dataIndex: 'scriptName', label: '脚本名称' }
  ]
];

</script>
<template>
  <div class="p-5">
    <div class="flex items-center space-x-2">
      <Icon icon="icon-anquanceshi" class="text-5" />
      <span class="text-4 font-semibold">安全测试</span>
    </div>
    <div class="flex items-center space-x-5 mt-2">
      <div class="rounded-full w-14 h-14 border text-5 text-center flex flex-col justify-center">
        通过
      </div>
      <Grid
        class="w-120"
        :columns="columns"
        :dataSource="props.dataSource" />
    </div>
  </div>
</template>
