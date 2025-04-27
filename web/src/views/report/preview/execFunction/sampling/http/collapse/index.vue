<script setup lang="ts">
import { computed, defineAsyncComponent } from 'vue';

import StatusTag from '@/views/report/preview/execFunction/sampling/http/collapse/statusTag/index.vue';
import { ExecContent, ExecInfo } from '../PropsType';

interface Props {
  iterations:string;
  ignoreAssertions:boolean;
  pipelines:ExecInfo['task']['pipelines'];
  execContent:ExecContent[];
}

const props = withDefaults(defineProps<Props>(), {
  iterations: undefined,
  ignoreAssertions: false,
  pipelines: () => [],
  execContent: () => []
});

const TransStart = defineAsyncComponent(() => import('@/views/report/preview/execFunction/sampling/http/collapse/transStart/index.vue'));
const WaitingTime = defineAsyncComponent(() => import('@/views/report/preview/execFunction/sampling/http/collapse/waitingTime/index.vue'));
const Throughput = defineAsyncComponent(() => import('@/views/report/preview/execFunction/sampling/http/collapse/throughput/index.vue'));
const Rendezvous = defineAsyncComponent(() => import('@/views/report/preview/execFunction/sampling/http/collapse/rendezvous/index.vue'));
const HTTP = defineAsyncComponent(() => import('@/views/report/preview/execFunction/sampling/http/collapse/http/index.vue'));
const TransEnd = defineAsyncComponent(() => import('@/views/report/preview/execFunction/sampling/http/collapse/transEnd/index.vue'));

const status = computed(() => {
  if (!props.execContent?.length) {
    return 'block';
  }

  const totalNum = props.execContent.length;
  const successNum = props.execContent.filter(item => item.content?.success).length;
  return totalNum === successNum ? 'success' : 'fail';
});
</script>
<template>
  <div class="mb-7 last:mb-0">
    <div class="flex items-center mb-2">
      <div class="min-w-20 mr-3 font-semibold">第{{ props.iterations }}次迭代</div>
      <StatusTag :value="status" />
    </div>

    <div class="space-y-3">
      <template v-for="item in props.pipelines" :key="item.id">
        <TransStart
          v-if="item.target==='TRANS_START'"
          :value="item"
          :content="props.execContent">
          <template v-if="item.children?.length">
            <template v-for="_item in item.children" :key="_item.id">
              <WaitingTime
                v-if="_item.target==='WAITING_TIME'"
                :value="_item"
                class="embed" />
              <Throughput
                v-else-if="_item.target==='THROUGHPUT'"
                :value="_item"
                class="embed" />
              <Rendezvous
                v-else-if="_item.target==='RENDEZVOUS'"
                :value="_item"
                class="embed" />
              <HTTP
                v-else-if="_item.target==='HTTP'"
                :value="_item"
                :content="props.execContent"
                :ignoreAssertions="props.ignoreAssertions"
                class="embed" />
              <TransEnd
                v-else-if="_item.target==='TRANS_END'"
                :value="_item" />
            </template>
          </template>
        </TransStart>
        <template v-if="!item.transactionName">
          <WaitingTime
            v-if="item.target==='WAITING_TIME'"
            :value="item" />
          <Throughput
            v-else-if="item.target==='THROUGHPUT'"
            :value="item" />
          <Rendezvous
            v-else-if="item.target==='RENDEZVOUS'"
            :value="item" />
          <HTTP
            v-else-if="item.target==='HTTP'"
            :value="item"
            :content="props.execContent"
            :ignoreAssertions="props.ignoreAssertions" />
          <TransEnd
            v-else-if="item.target==='TRANS_END'"
            :value="item" />
        </template>
      </template>
    </div>
  </div>
</template>
