<script setup lang="ts">
import { Spin } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
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

const { t } = useI18n();

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
