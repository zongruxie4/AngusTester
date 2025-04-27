<script lang="ts" setup>
import { defineAsyncComponent, ref } from 'vue';

const Authorization = defineAsyncComponent(() => import('@/views/apis/services/apiHttp/authorization/index.vue'));

const authData = ref();
const authFlag = ref(false);

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
      v-model:authFlag="authFlag"
      @change="changeData" />
  </div>
</template>
