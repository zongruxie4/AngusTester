<script setup lang="ts">
import { ref } from 'vue';
// eslint-disable-next-line import/no-absolute-path
import { ExecInfo } from '@/plugins/data/customIndex';

import { MockDataProps } from '@/views/execution/detail/types';

const props = withDefaults(defineProps<MockDataProps>(), {
  dataSource: undefined
});
const execInfo = ref();
defineExpose({
  restart: () => execInfo.value && execInfo.value.restart()
});
</script>
<template>
  <ExecInfo
    ref="execInfo"
    :dataSource="props.dataSource" />
</template>
