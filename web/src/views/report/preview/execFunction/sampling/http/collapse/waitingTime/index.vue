<script setup lang="ts">
import { computed } from 'vue';

import StatusTag from '@/views/report/preview/execFunction/sampling/http/collapse/statusTag/index.vue';

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

const waitTypeMap = {
  fixed: '固定等待',
  random: '随机等待'
};
</script>

<template>
  <div class="dashed-border h-10.5 leading-5 flex items-center px-3 rounded border border-solid border-theme-text-box">
    <span class="flex-shrink-0 mr-3">
      <svg
        t="1725160070925"
        class="icon"
        viewBox="0 0 1024 1024"
        version="1.1"
        xmlns="http://www.w3.org/2000/svg"
        p-id="14096"
        width="16"
        height="16"><path d="M512 169.14285688c219.648 0 397.71428531 178.06628531 397.71428531 397.71428624S731.648 964.57142844 512 964.57142844 114.28571469 786.50514312 114.28571469 566.85714313s178.06628531-397.71428531 397.71428531-397.71428625z m0 82.28571468c-174.21257156 0-315.42857156 141.216-315.42857156 315.42857156s141.216 315.42857156 315.42857156 315.42857157 315.42857156-141.216 315.42857156-315.42857157-141.216-315.42857156-315.42857156-315.42857156z m-13.71428531 103.40571375a41.14285687 41.14285687 0 0 1 41.07428531 38.72914313l0.06857156 2.41371469v167.78057156l92.70857156 94.50514218a41.14285687 41.14285687 0 0 1 1.30285688 56.22857157l-1.87885688 1.96114312a41.14285687 41.14285687 0 0 1-56.22857156 1.30285688l-1.94742844-1.86514313-100.55314312-102.51428531a54.85714313 54.85714313 0 0 1-15.59314313-35.10857156l-0.096-3.29142844V395.97714312a41.14285687 41.14285687 0 0 1 41.14285782-41.14285781zM690.28571469 59.42857156a41.14285687 41.14285687 0 0 1 0 82.28571375H333.71428531a41.14285687 41.14285687 0 0 1 0-82.28571375h356.57142938z" p-id="14097"></path></svg>
    </span>
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
        <div class="mr-1">至</div>
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
