<script setup lang="ts">
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';
import { Icon } from '@xcan-angus/vue-ui';

import StatusTag from '@/plugins/test/components/StatusTag/index.vue';

const { t } = useI18n();

export interface Props {
  value: {
    beforeName: string;
    transactionName: string;
    target: 'WAITING_TIME';
    name: string;
    description: string;
    enabled: boolean;
    maxWaitTimeInMs: string;
    minWaitTimeInMs?: string;// 固定等待时间不用传
  };
}

const props = withDefaults(defineProps<Props>(), {
  value: undefined
});

const waitType = computed(() => {
  if (!props.value) {
    return 'fixed';
  }

  const { maxWaitTimeInMs, minWaitTimeInMs } = props.value;
  if ((![null, undefined].includes(maxWaitTimeInMs)) && ([null, undefined].includes(minWaitTimeInMs))) {
    return 'fixed';
  }

  return 'random';
});

const waitTypeMap = computed(() => ({
  fixed: t('httpPlugin.functionTestDetail.waitingTime.fixedWait'),
  random: t('httpPlugin.functionTestDetail.waitingTime.randomWait')
}));
</script>

<template>
  <div class="dashed-border h-10.5 leading-5 flex items-center px-3 rounded border border-solid border-theme-text-box">
    <Icon class="flex-shrink-0 text-4 mr-3" icon="icon-dengdaishijian" />
    <div class="flex-1 flex items-center mr-3">
      <div :title="props.value?.name" class="truncate min-w-55 max-w-100 mr-5 name">{{ props.value?.name }}</div>
      <div class="mr-5">{{ waitTypeMap[waitType] }}</div>
      <div v-if="waitType==='fixed'">
        <span>{{ props.value?.maxWaitTimeInMs }}</span>
        <span>ms</span>
      </div>
      <div
        v-else
        class="flex items-center">
        <div class="mr-1">
          <span>{{ props.value?.minWaitTimeInMs }}</span>
          <span>ms</span>
        </div>
        <div class="mr-1">{{ t('httpPlugin.functionTestDetail.waitingTime.to') }}</div>
        <div>
          <span>{{ props.value?.maxWaitTimeInMs }}</span>
          <span>ms</span>
        </div>
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
