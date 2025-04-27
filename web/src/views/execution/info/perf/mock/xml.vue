<script setup lang="ts">
import { ref } from 'vue';
// eslint-disable-next-line import/no-absolute-path
import { ExecInfo } from '/public/plugins/MockXml/index.mjs';
// eslint-disable-next-line import/no-absolute-path
import '/public/plugins/MockXml/style.css';

interface Props {
  dataSource: Record<string, any>
}

const props = withDefaults(defineProps<Props>(), {
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
