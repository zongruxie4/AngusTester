
<script setup lang="ts">
import FastEntry from './FastEntry.vue';
import Configure from './Configure.vue';

interface Props {
  info:any;
  projectAuths: string[];
}

const emits = defineEmits<{(e: 'openDrawer', key: string): void}>();
withDefaults(defineProps<Props>(), {
  info: {}
});

</script>
<template>
  <div class="flex flex-col py-3.5 px-7.5 flex-1 space-y-7.5 overflow-y-auto">
    <FastEntry :info="info" :projectAuths="projectAuths" />
    <Configure
      :info="info"
      :projectAuths="projectAuths"
      @openDrawer="emits('openDrawer', $event)" />
  </div>
</template>
