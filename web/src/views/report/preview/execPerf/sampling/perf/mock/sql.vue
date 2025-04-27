<script setup lang="ts">
// eslint-disable-next-line import/no-absolute-path
import { ExecInfo } from '/public/plugins/MockSql/index.mjs';
// eslint-disable-next-line import/no-absolute-path
import '/public/plugins/MockSql/style.css';

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
