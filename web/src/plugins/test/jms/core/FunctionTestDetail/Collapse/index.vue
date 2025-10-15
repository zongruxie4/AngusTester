<script setup lang="ts">
import { computed, defineAsyncComponent, ref } from 'vue';
import { Collapse, CollapsePanel } from 'ant-design-vue';
import { Arrow } from '@xcan-angus/vue-ui';
import { utils } from '@xcan-angus/infra';
import { useI18n } from 'vue-i18n';

import StatusTag from '@/plugins/test/components/StatusTag/index.vue';
import { ExecInfo, ExecContent } from '../PropsType';

const { t } = useI18n();

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

const TransStart = defineAsyncComponent(() => import('@/plugins/test/components/functionTestDetailComp/TransStart/index.vue'));
const WaitingTime = defineAsyncComponent(() => import('@/plugins/test/components/functionTestDetailComp/WaitingTime/index.vue'));
const Throughput = defineAsyncComponent(() => import('@/plugins/test/components/functionTestDetailComp/Throughput/index.vue'));
const Rendezvous = defineAsyncComponent(() => import('@/plugins/test/components/functionTestDetailComp/Rendezvous/index.vue'));
const JMS = defineAsyncComponent(() => import('./JMS/index.vue'));
const TransEnd = defineAsyncComponent(() => import('@/plugins/test/components/functionTestDetailComp/TransEnd/index.vue'));

const UUID = utils.uuid();
const collapseActiveKey = ref<string>(UUID);
const arrowOpen = ref(collapseActiveKey.value === UUID);

const arrowChange = (open:boolean) => {
  arrowOpen.value = open;
  if (open) {
    collapseActiveKey.value = UUID;
    return;
  }

  collapseActiveKey.value = undefined;
};

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
  <Collapse
    :activeKey="collapseActiveKey"
    :bordered="false"
    style="background-color: #fff;font-size: 12px;"
    class="timeline-collapse">
    <CollapsePanel
      :key="UUID"
      :showArrow="false"
      collapsible="disabled">
      <template #header>
        <div class="w-full flex items-center">
          <div class="min-w-20 mr-3">{{ t('common.iterationCountTemplate', { iteration: props.iterations }) }}</div>
          <StatusTag :value="status" class="mr-3" />
          <Arrow :open="arrowOpen" @change="arrowChange" />
        </div>
      </template>

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
                <JMS
                  v-else-if="_item.target==='JMS'"
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
            <JMS
              v-else-if="item.target==='JMS'"
              :value="item"
              :content="props.execContent"
              :ignoreAssertions="props.ignoreAssertions" />
            <TransEnd
              v-else-if="item.target==='TRANS_END'"
              :value="item" />
          </template>
        </template>
      </div>
    </CollapsePanel>
  </Collapse>
</template>

<style scoped>
.timeline-collapse.ant-collapse>.ant-collapse-item {
  border: none;
}

.timeline-collapse.ant-collapse>.ant-collapse-item>:deep(.ant-collapse-content) > .ant-collapse-content-box {
  padding: 8px 0;
}

.ant-collapse > :deep(.ant-collapse-item) > .ant-collapse-header {
  padding-right: 0;
  padding-left: 0;
  line-height: 20px;
}
</style>
