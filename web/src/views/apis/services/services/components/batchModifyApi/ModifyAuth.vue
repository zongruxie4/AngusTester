<script lang="ts" setup>
import { defineAsyncComponent, ref } from 'vue';

const Authorization = defineAsyncComponent(() => import('@/views/apis/services/apiHttp/Authorization.vue'));

const authData = ref();
const auth = ref(false);

const changeData = (data) => {
  authData.value = data;
};

defineExpose({
  getData: () => {
    return authData.value;
  }
});

</script>
<template>
  <div>
    <Authorization
      v-model:auth="auth"
      @change="changeData" />
  </div>
</template>
