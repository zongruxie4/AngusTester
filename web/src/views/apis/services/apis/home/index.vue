
<script setup lang="ts">
import { ServicesPermission } from '@/enums/enums';

import QuickStarted from './QuickStarted.vue';
import Configure from './Configure.vue';

interface Props {
  serviceInfo:any;
  serviceAuths: ServicesPermission[];
}

withDefaults(defineProps<Props>(), {
  serviceInfo: {},
  serviceAuths: () => []
});

const emits = defineEmits<{(e: 'openDrawer', key: string): void}>();

</script>
<template>
  <div class="flex flex-col py-3.5 px-7.5 flex-1 space-y-7.5 overflow-y-auto">
    <QuickStarted
      :serviceInfo="serviceInfo"
      :serviceAuths="serviceAuths" />
    <Configure
      :serviceInfo="serviceInfo"
      :serviceAuths="serviceAuths"
      @openDrawer="emits('openDrawer', $event)" />
  </div>
</template>
