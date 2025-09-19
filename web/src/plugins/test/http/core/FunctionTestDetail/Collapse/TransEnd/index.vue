<script setup lang="ts">
import { computed } from 'vue';
import { Icon } from '@xcan-angus/vue-ui';

export interface Props {
  value: {
    target: 'RENDEZVOUS';
    name: string;
    description: string;
    enabled: boolean;
    beforeName: string;
    transactionName: string;
    timeoutInMs: string;
    threads: string;
  };
}

const props = withDefaults(defineProps<Props>(), {
  value: undefined
});

const showName = computed(() => {
  const _name = props.value?.name?.replace(/(.+)_end$/, '$1');
  return _name ? (_name + '结束') : '';
});
</script>

<template>
  <div class="h-10.5 leading-5 flex items-center px-3 rounded">
    <Icon class="flex-shrink-0 text-4 mr-3" icon="icon-shiwu" />
    <div :title="showName" class="truncate min-w-55 max-w-100 name">{{ showName }}</div>
  </div>
</template>
