<script setup lang="ts">
import StatusTag from '@/views/report/preview/execFunction/sampling/http/collapse/statusTag/index.vue';

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
</script>

<template>
  <div class="dashed-border h-10.5 leading-5 flex items-center px-3 rounded border border-solid border-theme-text-box">
    <span class="flex-shrink-0 mr-3">
      <svg
        t="1725160531920"
        class="icon"
        viewBox="0 0 1024 1024"
        version="1.1"
        xmlns="http://www.w3.org/2000/svg"
        p-id="14567"
        width="16"
        height="16"><path d="M248.16457156 687.32342844a41.14285687 41.14285687 0 0 1-20.928 54.29485687C188.16457156 758.92571469 169.14285688 779.264 169.14285688 794.144c0 37.056 95.47885688 82.28571469 219.42857156 82.28571469s219.42857156-45.22971469 219.42857156-82.28571469c0-14.4-17.89714312-34.05257156-55.03542844-51.15428531a41.14285687 41.14285687 0 1 1 34.42285688-74.74285781C650.55542844 697.33485688 690.28571469 740.94628531 690.28571469 794.144c0 99.264-137.856 164.57142844-301.71428625 164.57142844s-301.71428531-65.30742844-301.71428531-164.57142844c0-54.37714312 41.45828531-98.688 107.02628531-127.76228531a41.14285687 41.14285687 0 0 1 54.28114312 20.94171375zM849.23428531 73.14285688a68.57142844 68.57142844 0 0 1 53.84228625 111.03085781l-2.27657156 2.72914218-92.352 105.39428625 92.57142844 106.03885688a68.57142844 68.57142844 0 0 1-4.10057157 94.49142844l-2.45485687 2.26285687a68.57142844 68.57142844 0 0 1-41.55428531 16.81371469L849.37142844 512H429.71428531v260.57142844a41.14285687 41.14285687 0 0 1-82.28571375 0V114.28571469a41.14285687 41.14285687 0 0 1 41.14285688-41.14285782h460.64914312zM429.71428531 429.71428531h389.45828625l-88.48457156-101.36228531a54.85714313 54.85714313 0 0 1-1.81028531-69.984l1.86514218-2.24914313L818.96685687 155.42857156H429.71428531v274.28571375z" p-id="14568"></path></svg>
    </span>
    <div class="flex-1 flex items-center mr-3">
      <div :title="props.value?.name" class="truncate min-w-55 max-w-100 mr-5 name">{{ props.value?.name }}</div>
      <div class="flex items-center mr-5">
        <span class="mr-0.5">集合用户数</span>
        <span>{{ props.value?.threads }}</span>
      </div>
      <div class="flex items-center">
        <span class="mr-0.5">等待超时</span>
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
