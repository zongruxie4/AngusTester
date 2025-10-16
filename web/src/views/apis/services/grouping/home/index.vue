
<script setup lang="ts">
import QuickStarted from './QuickStarted.vue';
import Configure from './Configure.vue';

interface Props {
  info:any;
  serviceAuths: string[];
}

const emits = defineEmits<{(e: 'openDrawer', key: string): void}>();
withDefaults(defineProps<Props>(), {
  info: {}
});

</script>
<template>
  <div class="flex flex-col py-3.5 px-7.5 flex-1 space-y-7.5 overflow-y-auto">
    <QuickStarted :info="info" :serviceAuths="serviceAuths" />
    <Configure
      :info="info"
      :serviceAuths="serviceAuths"
      @openDrawer="emits('openDrawer', $event)" />
  </div>
</template>
