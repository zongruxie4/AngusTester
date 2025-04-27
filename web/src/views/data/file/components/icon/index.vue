<script lang="ts" setup>
import { Tooltip } from 'ant-design-vue';
import { Icon } from '@xcan-angus/vue-ui';

interface Props {
  title:string,
  icon:string
}

const props = withDefaults(defineProps<Props>(), {});

</script>
<template>
  <Tooltip>
    <template #title>{{ props?.title }}</template>
    <Icon
      :icon="props?.icon"
      class="ml-5 cursor-pointer text-4" />
  </Tooltip>
</template>
