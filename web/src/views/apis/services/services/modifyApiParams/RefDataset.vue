<script lang="ts" setup>
import { ref } from 'vue';
import { Select } from '@xcan-angus/vue-ui';
import { TESTER } from '@xcan-angus/infra';

interface Props {
  projectId: string;
}

const props = withDefaults(defineProps<Props>(), {
  projectId: ''
});

const fieldNames = {
  value: 'name',
  label: 'name'
};

const names = ref([]);

defineExpose({
  getData: () => {
    return names.value;
  }
});
</script>
<template>
  <div>
    <Select
      v-model:value="names"
      class="w-100"
      :fieldNames="fieldNames"
      :action="`${TESTER}/dataset?projectId=${props.projectId}&fullTextSearch=true`"
      mode="multiple" />
  </div>
</template>
