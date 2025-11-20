<script lang="ts" setup>
import { defineAsyncComponent, ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { ConfigMenuKey, createMenuItems } from './menu';

const LeftMenu = defineAsyncComponent(() => import('@/components/layout/leftMenu/index.vue'));
const AppInfo = defineAsyncComponent(() => import('@/views/config/app/index.vue'));
const Indicator = defineAsyncComponent(() => import('@/views/config/indicator/index.vue'));
const Event = defineAsyncComponent(() => import('@/views/config/event/index.vue'));
const Proxy = defineAsyncComponent(() => import('@/views/config/proxy/index.vue'));
const Node = defineAsyncComponent(() => import('@/views/config/node/index.vue'));

const { t } = useI18n();

const activeKey = ref<ConfigMenuKey>(ConfigMenuKey.APP_INFO);
const menuItems = createMenuItems(t);
</script>
<template>
  <LeftMenu v-model:activeKey="activeKey" key="config" :menuItems="menuItems">
    <template #appInfo>
      <AppInfo class="overflow-auto h-full" />
    </template>
    <template #indicator>
      <Indicator
        v-if="activeKey === ConfigMenuKey.INDICATOR"
        class="overflow-auto h-full" />
    </template>
    <template #event>
      <Event
        v-if="activeKey === ConfigMenuKey.EVENT"
        class="overflow-auto h-full" />
    </template>
    <template #proxy>
      <Proxy
        v-if="activeKey === ConfigMenuKey.PROXY"
        class="overflow-auto h-full" />
    </template>
    <template #node>
      <Node class="overflow-auto h-full" />
    </template>
  </LeftMenu>
</template>
