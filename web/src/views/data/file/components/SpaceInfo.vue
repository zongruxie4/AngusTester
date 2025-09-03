<script setup lang="ts">
import { Grid, Spin } from '@xcan-angus/vue-ui';
import { useSpaceInfo } from './composables/useSpaceInfo';

interface Props {
  id: string,
  type?: 'space' | 'file' | 'directory',
  pubapi?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  type: 'space',
  pubapi: false
});

// Use the space info composable
const { dataSource, loading, infoColumns, init } = useSpaceInfo(props);

// Initialize the composable
init();
</script>
<template>
  <Spin :spinning="loading">
    <Grid
      class="mt-3"
      :columns="infoColumns"
      :dataSource="dataSource"
      font-size="12px">
    </Grid>
  </Spin>
</template>
