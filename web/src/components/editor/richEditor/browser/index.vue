<script setup lang="ts">

interface Props {
    value:string;
    emptyText: string;
}

const props = withDefaults(defineProps<Props>(), {
  value: '',
  emptyText: ''
});

</script>
<template>
  <div :class="{'text-sub-content leading-5.5': !props.value}" v-html="props.value || props.emptyText"></div>
<!--  <RichBrowser :value="props.value" contentStyle="body{font-size: 12px;margin:0; };.mce-content-body[data-mce-placeholder]:not(.mce-visualblocks)::before{color:#BFBFBF;font-size:12px;}" />-->
</template>
