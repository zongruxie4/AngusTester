<script lang="ts" setup>
import { defineAsyncComponent, ref } from 'vue';

const ServerForm = defineAsyncComponent(() => import('@/views/apis/services/services/slider/server/ServerEditForm.vue'));
const serverFormRef = ref();

defineExpose({
  getData: () => {
    const data = serverFormRef.value.getData();
    return {
      ...data,
      variables: data.variables.reduce((pre, cur) => {
        pre[cur.name] = {
          default: cur.default,
          description: cur.description,
          enum: (cur.enum || []).map(item => item.value)
        }
        return pre;
      }, {})
    };
  }
});
</script>
<template>
  <div>
    <ServerForm ref="serverFormRef" :hideButton="true" />
  </div>
</template>
