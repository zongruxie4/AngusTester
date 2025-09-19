<script setup lang="ts">
import { defineAsyncComponent } from 'vue';

interface Props {
  type:'configs'|'detail';
}

const props = withDefaults(defineProps<Props>(), {
  type: 'configs'
});

const Configs = defineAsyncComponent(() => import('./Configs.vue'));
const Detail = defineAsyncComponent(() => import('./Detail.vue'));
</script>

<template>
  <Configs v-if="props.type==='configs'" v-bind="props" />
  <Detail v-else v-bind="props" />
</template>
