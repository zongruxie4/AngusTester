<script lang="ts" setup>
import { computed, ref, watch } from 'vue';
import { Collapse, CollapsePanel } from 'ant-design-vue';
import { Arrow, NoData } from '@xcan-angus/vue-ui';
import { utils } from '@xcan-angus/infra';

import { ExecContent } from '../../PropsType';

export interface Props {
  value: ExecContent
}

const props = withDefaults(defineProps<Props>(), {
  value: undefined
});

const panels:{id:string;name:string;key:'headers'|'body'}[] = [
  {
    id: 'headers',
    name: '响应头',
    key: 'headers'
  },
  {
    id: 'body',
    name: '响应体',
    key: 'body'
  }
];

const responseData = computed(() => {
  return props.value?.content?.response?.messageResponses || [];
});

const spreadMap = ref<{[key: string]: string[]}>({});

watch(() => responseData.value, newValue => {
  if (newValue) {
    for (const i in newValue) {
      spreadMap.value[i] = [];
    }
  } else {
    spreadMap.value = {};
  }
}, {
  immediate: true
});

const arrowChange = (id: string, idx) => {
  if (spreadMap.value[idx].includes(id)) {
    spreadMap.value[idx] = spreadMap.value[idx].filter(item => item !== id);
  } else {
    spreadMap.value[idx].push(id);
  }
};

</script>
<template>
  <div v-if="responseData.length" class="max-h-50 overflow-y-auto space-y-2 rounded">
    <div
      v-for="itemResp,idx in responseData"
      :key="idx"
      class="border rounded">
      <Collapse
        v-model:activeKey="spreadMap[idx]"
        :bordered="false"
        style="background-color: #fff;font-size: 12px;"
        class="tabs-collapse">
        <div class="pl-5 py-2 font-semibold">邮件{{ idx + 1 }}</div>
        <CollapsePanel
          v-for="item in panels"
          :key="item.id"
          :showArrow="false"
          collapsible="disabled">
          <template #header>
            <div class="w-full flex items-center pr-2">
              <Arrow :open="spreadMap[idx].includes(item.id)" @change="arrowChange(item.id, idx)" />
              <div class="ml-1 font-bold">{{ item.name }}</div>
              <div v-show="item.id==='headers'" class="flex-1 text-right">大小：{{ utils.formatBytes(itemResp.dataSize) }}</div>
            </div>
          </template>
          <template v-if="item.key==='headers'">
            <div v-if="itemResp.headers" class="pb-2 pl-2 pt-1 space-y-1 whitespace-pre">
              {{ itemResp.headers }}
            </div>
            <NoData v-else size="small" />
          </template>
          <template v-if="item.key==='body'">
            <div v-if="itemResp.data" class="pb-2 pl-2 pt-1 space-y-1  whitespace-pre">
              {{ itemResp.data }}
            </div>
            <NoData v-else size="small" />
          </template>
        </CollapsePanel>
      </Collapse>
    </div>
  </div>
  <NoData v-else size="small" />
</template>
<style scoped>
.ant-collapse.tabs-collapse > :deep(.ant-collapse-item) {
  border: none;
}

.ant-collapse.tabs-collapse > :deep(.ant-collapse-item) .ant-collapse-header{
  padding: 6px 0;
}
</style>
