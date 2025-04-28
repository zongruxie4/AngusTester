<script setup lang="ts">

interface Props {
    value:string;
}

const props = withDefaults(defineProps<Props>(), {
  value: ''
});

</script>
<template>
  <div v-html="props.value"></div>
<!--  <RichBrowser :value="props.value" contentStyle="body{font-size: 12px;margin:0; };.mce-content-body[data-mce-placeholder]:not(.mce-visualblocks)::before{color:#BFBFBF;font-size:12px;}" />-->
</template>
