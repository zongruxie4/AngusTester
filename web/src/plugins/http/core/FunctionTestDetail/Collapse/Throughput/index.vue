<script setup lang="ts">
import { useI18n } from 'vue-i18n';
import { Icon } from '@xcan-angus/vue-ui';

import StatusTag from '../StatusTag/index.vue';

const { t } = useI18n();

export interface Props {
  value: {
    target: 'THROUGHPUT';
    name: string;
    description: string;
    enabled: boolean;
    beforeName: string;
    transactionName: string;
    permitsPerSecond: string;
    timeoutInMs: string;
  };
}

const props = withDefaults(defineProps<Props>(), {
  value: undefined
});

</script>

<template>
  <div class="dashed-border h-10.5 leading-5 flex items-center px-3 rounded border border-solid border-theme-text-box">
    <Icon class="flex-shrink-0 text-4 mr-3" icon="icon-zusai" />
    <div class="flex-1 flex items-center mr-3">
      <div :title="props.value?.name" class="truncate min-w-55 max-w-100 mr-5 name">{{ props.value?.name }}</div>
      <div class="flex items-center mr-5">
        <span class="mr-0.5">{{ t('httpPlugin.functionTestDetail.throughput.requestsPerSecond') }}</span>
        <span>{{ props.value?.permitsPerSecond }}</span>
      </div>
      <div class="flex items-center mr-5">
        <span class="mr-0.5">{{ t('httpPlugin.functionTestDetail.throughput.waitTimeout') }}</span>
        <span>{{ props.value?.timeoutInMs }}</span>
        <span>ms</span>
      </div>
    </div>
    <StatusTag v-if="!props.value?.enabled" class="mr-7" />
  </div>
</template>

<style scoped>
.embed.dashed-border {
  padding: 0 12px;
  border: none;
  border-bottom: 1px dashed  var(--border-text-box);
}

.embed .name {
  min-width: 208px;
}
</style>
