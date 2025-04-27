<script setup lang="ts">
import { computed, ref } from 'vue';
import { Collapse, CollapsePanel } from 'ant-design-vue';
import { Arrow, Icon } from '@xcan-angus/vue-ui';
import { utils } from '@xcan-angus/tools';

import StatusTag from '../StatusTag/index.vue';
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
      target: 'HTTP' | 'TRANS_END' | 'RENDEZVOUS' | 'WAITING_TIME' | 'THROUGHPUT';
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

  return props.value?.children?.filter(item => item.target === 'HTTP')?.map(item => item.linkName);
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
  const enabledApis = props.value?.children?.filter(item => item.target === 'HTTP' && item.enabled) || [];
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
          <Icon class="flex-shrink-0 text-4 mr-3" icon="icon-shiwu" />
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
