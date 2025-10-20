<script setup lang="ts">
import { defineAsyncComponent } from 'vue';
import { ExecContent } from './PropsType';

const WebsocketItem = defineAsyncComponent(() => import('./websocket.vue'));

export interface Props {
  content: ExecContent[];
}

const props = withDefaults(defineProps<Props>(), {
  content: () => ([])
});

</script>
<template>
  <div class="space-y-3">
    <WebsocketItem
      v-for="(item, idx) in props.content"
      :key="idx"
      :content="item" />
  </div>
</template>
