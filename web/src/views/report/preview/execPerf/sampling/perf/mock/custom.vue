<script setup lang="ts">
// eslint-disable-next-line import/no-absolute-path
import { ExecInfo } from '@/plugins/gendata/customIndex';
// eslint-disable-next-line import/no-absolute-path

interface Props {
  dataSource: Record<string, any>
}

const props = withDefaults(defineProps<Props>(), {
  dataSource: undefined
});
</script>
<template>
  <ExecInfo
    :dataSource="props.dataSource" />
</template>
