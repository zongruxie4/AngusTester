<script setup lang="ts">
import { defineAsyncComponent } from 'vue';
import { ExecContent } from './PropsType';

const SmtpItem = defineAsyncComponent(() => import('./Smtp.vue'));

export interface Props {
  content: ExecContent[];
}

const props = withDefaults(defineProps<Props>(), {
  content: () => ([])
});

</script>
<template>
  <div class="space-y-3">
    <SmtpItem
      v-for="(item, idx) in props.content"
      :key="idx"
      :content="item" />
  </div>
</template>
