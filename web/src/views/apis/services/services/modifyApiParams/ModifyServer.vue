<script lang="ts" setup>
import { defineAsyncComponent, ref } from 'vue';

const ServerForm = defineAsyncComponent(() => import('@/views/apis/services/services/slider/server/ServerEditForm.vue'));
const serverFormRef = ref();

defineExpose({
  getData: () => {
    return serverFormRef.value.getData();
  }
});

</script>
<template>
  <div>
    <ServerForm ref="serverFormRef" :hideButton="true" />
  </div>
</template>
