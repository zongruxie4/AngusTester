<script setup lang="ts">
import { computed, ref } from 'vue';
import { Collapse, CollapsePanel } from 'ant-design-vue';
import { Arrow } from '@xcan-angus/vue-ui';
import { utils } from '@xcan-angus/infra';

import StatusTag from '@/views/report/preview/execFunction/sampling/http/collapse/statusTag/index.vue';
import { ExecContent } from '../../PropsType';

interface Props {
  value: {
    target: 'TRANS_START';
    name: string;
    description: string;
    enabled: boolean;
    beforeName: string;
    transactionName: string;
    children?: {
      target: 'http' | 'TRANS_END' | 'RENDEZVOUS' | 'WAITING_TIME' | 'THROUGHPUT';
      name: string;
      linkName: string;
      description: string;
      enabled: boolean;
      beforeName: string;
      transactionName: string;
    }[];
  };
  content: ExecContent[];
}

const props = withDefaults(defineProps<Props>(), {
  value: undefined,
  content: undefined
});

const UUID = utils.uuid();
const collapseActiveKey = ref<string>();
const arrowOpen = ref(collapseActiveKey.value === UUID);

const arrowChange = (open: boolean) => {
  arrowOpen.value = open;
  if (open) {
    collapseActiveKey.value = UUID;
    return;
  }

  collapseActiveKey.value = undefined;
};

const httpNames = computed(() => {
  if (!props.value?.children?.length) {
    return [];
  }

  return props.value?.children?.filter(item => item.target === 'http')?.map(item => item.linkName);
});

const httpContents = computed(() => {
  const names = httpNames.value;
  if (names.length === 0) {
    return [];
  }

  return props.content?.filter(item => names.includes(item.name));
});

// const bodySize = computed(() => {
//   const size = httpContents.value.reduce((prev, cur) => {
//     prev = prev + (+cur.content.response.bodySize);
//     return prev;
//   }, 0);

//   return utils.formatBytes(size);
// });

const status = computed(() => {
  if (!props.content?.length) {
    return 'block';
  }

  // 所有启用的接口
  const enabledApis = props.value?.children?.filter(item => item.target === 'http' && item.enabled) || [];
  const totalNum = enabledApis.length;
  const successNum = httpContents.value.filter(item => item.content?.success)?.length;
  if (totalNum === successNum) {
    return 'success';
  }

  return 'fail';
});
</script>
<template>
  <Collapse
    :activeKey="collapseActiveKey"
    style="background-color: #fff;font-size: 12px;"
    class="timeline-collapse">
    <CollapsePanel
      :key="UUID"
      :showArrow="false"
      collapsible="disabled">
      <template #header>
        <div class="w-full flex items-center px-3 py-2.5 cursor-pointer" @click="arrowChange(!arrowOpen)">
          <span class="flex-shrink-0 mr-3">
            <svg
              t="1725160294475"
              class="icon"
              viewBox="0 0 1024 1024"
              version="1.1"
              xmlns="http://www.w3.org/2000/svg"
              p-id="14238"
              width="16"
              height="16"><path d="M512 74.85714248c73.65857168 0 134.16428584 56.31428584 140.82428584 128.22428584a383.88857168 383.88857168 0 0 1 60.73714248 30.27857168C827.05142832 303.04571416 897.71428584 426.59 897.71428584 562.27142832c0 22.78285752-1.98 45.33428584-5.88857168 67.44857168A141.23571416 141.23571416 0 0 1 949.14285752 743.42857168c0 78.10714248-63.32142832 141.42857168-141.42857168 141.4285708-22.69285752 0-44.13857168-5.34857168-63.15428584-14.85A384.14571416 384.14571416 0 0 1 512 947.98571416a384.23571416 384.23571416 0 0 1-232.58571416-77.97857168A140.72142832 140.72142832 0 0 1 216.28571416 884.85714248C138.17857168 884.85714248 74.85714248 821.53571416 74.85714248 743.42857168c0-46.60714248 22.53857168-87.94285752 57.31714336-113.70857168A387 387 0 0 1 126.28571416 562.27142832c0-131.01428584 65.85428584-250.86857167 173.03142832-321.82714248a384.64714248 384.64714248 0 0 1 71.8714292-37.36285752C377.83571416 131.17142832 438.32857168 74.85714248 512 74.85714248z m126.02571416 205.66285752l-0.54 1.06714248A141.41571416 141.41571416 0 0 1 512 357.71428584a141.42857168 141.42857168 0 0 1-126.02571416-77.18142832 306.32142832 306.32142832 0 0 0-44.06142832 24.23571415C256.07857168 361.59714248 203.42857168 457.42142832 203.42857168 562.27142832c0 13.5 0.86142832 26.89714248 2.57142832 40.11428584 3.40714248-0.25714248 6.82714248-0.38571416 10.28571416-0.38571416 78.10714248 0 141.42857168 63.32142832 141.42857168 141.42857168 0 26.80714248-7.45714248 51.87857168-20.41714336 73.23428584A307.06714248 307.06714248 0 0 0 512 870.84285752c63.48857167 0 123.93-19.20857168 174.70285752-54.18A140.72142832 140.72142832 0 0 1 666.28571416 743.42857168c0-78.10714248 63.32142832-141.42857168 141.42857168-141.42857168 3.45857168 0 6.89142832 0.12857168 10.28571416 0.37285752 1.71-13.20428584 2.57142833-26.58857168 2.57142832-40.11428584 0-108.57857168-56.50714248-207.38571416-147.3685708-263.16000001a307.59428584 307.59428584 0 0 0-35.17714336-18.57857167zM807.71428584 679.14285752a64.28571416 64.28571416 0 1 0 0 128.57142832 64.28571416 64.28571416 0 0 0 0-128.57142832z m-591.42857168 0a64.28571416 64.28571416 0 1 0 0 128.57142832 64.28571416 64.28571416 0 0 0 0-128.57142832z m295.71428584-257.14285752c78.10714248 0 141.42857168 63.32142832 141.42857168 141.42857167s-63.32142832 141.42857168-141.42857168 141.42857081-141.42857168-63.32142832-141.42857168-141.42857081 63.32142832-141.42857168 141.42857168-141.42857167z m0 77.14285752a64.28571416 64.28571416 0 1 0 0 128.57142832 64.28571416 64.28571416 0 0 0 0-128.57142832z m0-347.14285752a64.28571416 64.28571416 0 1 0 0 128.57142832 64.28571416 64.28571416 0 0 0 0-128.57142832z" p-id="14239"></path></svg>
          </span>
          <div :title="props.value?.name" class="truncate min-w-55 max-w-100 mr-5 name">{{ props.value?.name }}</div>
          <div class="flex-1 justify-end flex items-center mr-3">
            <!-- <div class="mr-5">{{ bodySize }}</div> -->
            <template v-if="!props.value?.enabled">
              <StatusTag />
            </template>
            <template v-else>
              <StatusTag :value="status" />
            </template>
          </div>
          <Arrow :open="arrowOpen" @change="arrowChange" />
        </div>
      </template>
      <slot name="default"></slot>
    </CollapsePanel>
  </Collapse>
</template>

<style scoped>
.ant-collapse> :deep(.ant-collapse-item)>.ant-collapse-header {
  padding: 0;
  border-color: var(--border-divider);
  line-height: 20px;
}

.ant-collapse> :deep(.ant-collapse-item) .ant-collapse-content-box {
  padding: 0 12px;
  line-height: 20px;
}
</style>
