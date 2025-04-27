<script lang="ts" setup>
import { defineAsyncComponent, ref } from 'vue';

import LeftMenu from '@/components/layout/leftMenu/index.vue';

const AppInfo = defineAsyncComponent(() => import('@/views/config/appInfo/index.vue'));
const Indicator = defineAsyncComponent(() => import('@/views/config/indicator/index.vue'));
const Event = defineAsyncComponent(() => import('@/views/config/event/index.vue'));
const Agent = defineAsyncComponent(() => import('@/views/config/agent/index.vue'));

const Node = defineAsyncComponent(() => import('./node/index.vue'));

const activeKey = ref('appInfo');

const menuItems: {
  icon: string;
  name: string;
  key: string;
}[] = [
  { icon: 'icon-yingyongxinxi', name: '应用', key: 'appInfo' },
  { icon: 'icon-zhibiao', name: '指标', key: 'indicator' },
  { icon: 'icon-tuisongtongzhi', name: '事件', key: 'event' },
  { icon: 'icon-guanlijiedian', name: '节点', key: 'node' },
  { icon: 'icon-jiekoudaili', name: '代理', key: 'agent' }
];
</script>
<template>
  <LeftMenu v-model:activeKey="activeKey" :menuItems="menuItems">
    <template #appInfo>
      <AppInfo class="overflow-auto h-full" />
    </template>
    <template #indicator>
      <Indicator v-if="activeKey === 'indicator'" class="overflow-auto h-full" />
    </template>
    <template #event>
      <Event v-if="activeKey === 'event'" class="overflow-auto h-full" />
    </template>
    <template #agent>
      <Agent v-if="activeKey === 'agent'" class="overflow-auto h-full" />
    </template>
    <template #node>
      <Node class="overflow-auto h-full" />
    </template>
  </LeftMenu>
</template>
