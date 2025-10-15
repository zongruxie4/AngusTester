<script setup lang="ts">
import { computed, ref } from 'vue';
import { Collapse, CollapsePanel } from 'ant-design-vue';
import { utils } from '@xcan-angus/infra';
import { Arrow, Colon, NoData } from '@xcan-angus/vue-ui';

import { useI18n } from 'vue-i18n';
import { ExecContent } from '../../../PropsType';
const { t } = useI18n();

export interface Props {
  value: ExecContent
}

const props = withDefaults(defineProps<Props>(), {
  value: undefined
});

const panels:{id:string;name:string;key:'general'|'request'|'response'}[] = [
  {
    id: utils.uuid(),
    name: t('common.general'),
    key: 'general'
  },
  {
    id: utils.uuid(),
    name: t('protocol.requestHeader'),
    key: 'request'
  },
  {
    id: utils.uuid(),
    name: t('protocol.responseHeader'),
    key: 'response'
  }
];

const activeKeys = ref<string[]>([panels[0].id]);

const arrowChange = (id: string) => {
  if (activeKeys.value.includes(id)) {
    activeKeys.value = activeKeys.value.filter(item => item !== id);
  } else {
    activeKeys.value.push(id);
  }
};

const url = computed(() => {
  const url = props.value?.content?.request0?.url;
  const queryString = props.value?.content?.request0?.queryString;
  if (queryString) {
    return url + '?' + queryString;
  }

  return url;
});

const method = computed(() => {
  return props.value?.content?.request0?.method;
});

const statusCode = computed(() => {
  return +props.value?.content?.response?.status || '';
});

const requestHeaders = computed(() => {
  const headers = props.value?.content?.request0?.headerArray;
  if (!headers?.length) {
    return [];
  }

  return headers.reduce((prev, cur, index) => {
    if (index % 2 === 0) {
      prev.push({
        id: utils.uuid(),
        name: cur,
        value: ''
      });
    } else {
      prev[prev.length - 1].value = cur;
    }

    return prev;
  }, [] as {id:string;name:string;value:string}[]);
});

const responseHeaders = computed(() => {
  const headers = props.value?.content?.response?.headerArray;
  if (!headers?.length) {
    return [];
  }

  return headers.reduce((prev, cur, index) => {
    if (index % 2 === 0) {
      prev.push({
        id: utils.uuid(),
        name: cur,
        value: ''
      });
    } else {
      prev[prev.length - 1].value = cur;
    }

    return prev;
  }, [] as {id:string;name:string;value:string}[]);
});
</script>
<template>
  <Collapse
    :activeKey="activeKeys"
    :bordered="false"
    style="background-color: #fff;font-size: 12px;"
    class="tabs-collapse">
    <CollapsePanel
      v-for="item in panels"
      :key="item.id"
      :showArrow="false"
      collapsible="disabled">
      <template #header>
        <div class="w-full flex items-center">
          <Arrow :open="activeKeys.includes(item.id)" @change="arrowChange(item.id)" />
          <div class="ml-1 font-bold">{{ item.name }}</div>
        </div>
      </template>
      <template v-if="item.key==='general'">
        <div class="pb-2 pl-2 pt-1 space-y-1">
          <div class="flex items-start">
            <div class="w-55">Request URL<Colon /></div>
            <div class="flex-1 whitespace-pre-wrap break-words break-all">{{ url }}</div>
          </div>
          <div class="flex items-start">
            <div class="w-55">Request Method<Colon /></div>
            <div class="flex-1 whitespace-pre-wrap break-words break-all">{{ method }}</div>
          </div>
          <div class="flex items-start">
            <div class="w-55">Status Code<Colon /></div>
            <div class="flex items-center flex-1 whitespace-pre-wrap break-words break-all">
              <span
                v-if="statusCode>=200&&statusCode<=299"
                class="inline-block w-3 h-3 rounded-lg mr-1 transform-gpu -translate-y-0.25"
                style="background-color: rgba(3, 206, 92,100%);"></span>
              <span v-else-if="statusCode>=400&&statusCode<=599" class="inline-block w-3 h-3 rounded-lg mr-1 bg-red-500 transform-gpu -translate-y-0.25"></span>
              <span>{{ statusCode }}</span>
            </div>
          </div>
        </div>
      </template>
      <template v-else-if="item.key==='request'">
        <div v-if="requestHeaders.length" class="pb-2 pl-2 pt-1 space-y-1">
          <div
            v-for="header in requestHeaders"
            :key="header.id"
            class="flex items-start">
            <div class="w-55">{{ header.name }}<Colon /></div>
            <div class="flex-1 whitespace-pre-wrap break-words break-all">{{ header.value }}</div>
          </div>
        </div>
        <NoData v-else size="small" />
      </template>
      <template v-else-if="item.key==='response'">
        <div v-if="responseHeaders.length" class="pb-2 pl-2 pt-1 space-y-1">
          <div
            v-for="header in responseHeaders"
            :key="header.id"
            class="flex items-start">
            <div class="w-55">{{ header.name }}<Colon /></div>
            <div class="flex-1 whitespace-pre-wrap break-words break-all">{{ header.value }}</div>
          </div>
        </div>
        <NoData v-else size="small" />
      </template>
    </CollapsePanel>
  </Collapse>
</template>

<style scoped>
.ant-collapse.tabs-collapse > :deep(.ant-collapse-item) {
  border: none;
}

.ant-collapse.tabs-collapse > :deep(.ant-collapse-item) .ant-collapse-header{
  padding: 6px 0;
}
</style>
