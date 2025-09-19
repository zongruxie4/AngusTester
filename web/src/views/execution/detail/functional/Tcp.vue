<script setup lang="ts">
// eslint-disable-next-line import/no-absolute-path
import Plugin from '@/plugins/test/tcp/index';

import { FunctionalTestProps } from '@/views/execution/detail/types';

// eslint-disable-next-line import/no-absolute-path
const props = withDefaults(defineProps<FunctionalTestProps>(), {
  execInfo: undefined,
  execContent: undefined,
  exception: undefined
});

</script>
<template>
  <Plugin
    v-bind="props"
    type="detail"
    :execInfo="props.execInfo"
    :execContent="props.execContent"
    :exception="props.exception" />
</template>
