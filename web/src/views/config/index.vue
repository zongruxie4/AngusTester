<script lang="ts" setup>
import { defineAsyncComponent, ref } from 'vue';
import { useI18n } from 'vue-i18n';

import LeftMenu from '@/components/layout/leftMenu/index.vue';

const { t } = useI18n();

const AppInfo = defineAsyncComponent(() => import('@/views/config/appInfo/index.vue'));
const Indicator = defineAsyncComponent(() => import('@/views/config/indicator/index.vue'));
const Event = defineAsyncComponent(() => import('@/views/config/event/index.vue'));
const Proxy = defineAsyncComponent(() => import('@/views/config/proxy/index.vue'));

const Node = defineAsyncComponent(() => import('./node/index.vue'));

const activeKey = ref('appInfo');

const menuItems: {
  icon: string;
  name: string;
  key: string;
}[] = [
  { icon: 'icon-yingyongxinxi', name: t('app.name'), key: 'appInfo' },
  { icon: 'icon-zhibiao', name: t('indicator.name'), key: 'indicator' },
  { icon: 'icon-tuisongtongzhi', name: t('event.name'), key: 'event' },
  { icon: 'icon-guanlijiedian', name: t('node.name'), key: 'node' },
  { icon: 'icon-jiekoudaili', name: t('proxy.name'), key: 'proxy' }
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
    <template #proxy>
      <Proxy v-if="activeKey === 'proxy'" class="overflow-auto h-full" />
    </template>
    <template #node>
      <Node class="overflow-auto h-full" />
    </template>
  </LeftMenu>
</template>
