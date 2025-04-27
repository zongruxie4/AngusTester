<script lang="ts" setup>
import { defineAsyncComponent, ref } from 'vue';

const ServerForm = defineAsyncComponent(() => import('@/views/apis/services/components/serverConfig/editForm.vue'));
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
