<script setup lang="ts">
// eslint-disable-next-line import/no-absolute-path
import Plugin from '@/plugins/websocket/index';
// eslint-disable-next-line import/no-absolute-path

interface Props {
  execInfo:{
    id:string;
    reportInterval:string;
    status:{
      message:string;
      value:'PENDING'|'RUNNING'|'COMPLETED';
    }
  };
  execContent:{[key:string]:any}[];
  exception?: { codeName: string; messageName: string; code: string; message: string;};
}

const props = withDefaults(defineProps<Props>(), {
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
