
<script setup lang="ts">
import { ServicesPermission } from '@/enums/enums';

import QuickStarted from './QuickStarted.vue';
import Configure from './Configure.vue';

interface Props {
  info:any;
  serviceAuths: ServicesPermission[];
}

withDefaults(defineProps<Props>(), {
  info: {},
  serviceAuths: () => []
});

const emits = defineEmits<{(e: 'openDrawer', key: string): void}>();

</script>
<template>
  <div class="flex flex-col py-3.5 px-7.5 flex-1 space-y-7.5 overflow-y-auto">
    <QuickStarted
      :info="info"
      :serviceAuths="serviceAuths" />
    <Configure
      :info="info"
      :serviceAuths="serviceAuths"
      @openDrawer="emits('openDrawer', $event)" />
  </div>
</template>
