<script setup lang="ts">
import { onMounted, ref, watch } from 'vue';
import { useRoute } from 'vue-router';
import { Breadcrumb } from 'ant-design-vue';
import { Sidebar } from '@xcan-angus/vue-ui';

type MenuItem = {
  code: 'apis' | 'request' | 'activity' |'monitor' | 'set'|'log';
  icon: 'icon-jiekou' | 'icon-lishijilu' | 'icon-jiankong' | 'icon-shezhi1' | 'icon-qingqiushu'|'icon-zidingyizhibiao1';
  showName: string;
  url: string;
  show: true;
  breadcrumb: [
    { id:string;name: 'Mock'; path: '/apis#mock'; },
    { id:string;name: string; path?:string; }
  ]
}

const route = useRoute();
const id = route.params.id;

const currentMenu = ref<MenuItem>();

const menuItems:MenuItem[] = [
  {
    code: 'apis',
    icon: 'icon-jiekou',
    showName: '接口',
    url: `/mockservice/${id}/apis`,
    show: true,
    breadcrumb: [
      { name: 'Mock', path: '/apis#mock', id: '1001' },
      { name: '接口', id: '1002' }
    ]
  },
  {
    code: 'request',
    icon: 'icon-qingqiushu',
    showName: '请求',
    url: `/mockservice/${id}/request`,
    show: true,
    breadcrumb: [
      { name: 'Mock', path: '/apis#mock', id: '2001' },
      { name: '请求', id: '2002' }
    ]
  },
  {
    code: 'log',
    icon: 'icon-zidingyizhibiao1',
    showName: '日志',
    url: `/mockservice/${id}/log`,
    show: true,
    breadcrumb: [
      { name: 'Mock', path: '/apis#mock', id: '2001' },
      { name: '日志', id: '6002' }
    ]
  },
  {
    code: 'activity',
    icon: 'icon-lishijilu',
    showName: '活动',
    url: `/mockservice/${id}/activity`,
    show: true,
    breadcrumb: [
      { name: 'Mock', path: '/apis#mock', id: '3001' },
      { name: '活动', id: '3002' }
    ]
  },
  {
    code: 'monitor',
    icon: 'icon-jiankong',
    showName: '监控',
    url: `/mockservice/${id}/monitor`,
    show: true,
    breadcrumb: [
      { name: 'Mock', path: '/apis#mock', id: '4001' },
      { name: '监控', id: '4002' }
    ]
  },
  {
    code: 'set',
    icon: 'icon-shezhi1',
    showName: '设置',
    url: `/mockservice/${id}/setting`,
    show: true,
    breadcrumb: [
      { name: 'Mock', path: '/apis#mock', id: '5001' },
      { name: '设置', id: '5002' }
    ]
  }
];

onMounted(() => {
  watch(() => route.fullPath, (newValue) => {
    if (!newValue) {
      return;
    }

    const code = newValue.match(/\/mock\/service\/\d+\/(\w+)/)?.[1];
    currentMenu.value = menuItems.find(item => item.code === code);
  }, { immediate: true });
});
</script>
<template>
  <div class="flex flex-nowrap h-full">
    <Sidebar
      :dataSource="menuItems"
      storageKey="angus-mock"
      class="flex-grow-0 flex-shrink-0" />
    <div class="flex-1 h-full min-w-0">
      <Breadcrumb v-if="currentMenu" class="px-6 border-b border-theme-text-box bg-theme-container text-theme-title">
        <template v-for="item in currentMenu.breadcrumb" :key="item.id">
          <Breadcrumb.Item v-if="item.path">
            <RouterLink :to="item.path">
              <span class="text-theme-title text-theme-text-hover">{{ $t(item.name) }}</span>
            </RouterLink>
          </Breadcrumb.Item>
          <Breadcrumb.Item v-else>
            <span class="text-theme-title text-theme-text-hover">{{ $t(item.name) }}</span>
          </Breadcrumb.Item>
        </template>
      </Breadcrumb>
      <div style="height: calc(100% - 0px);" class="p-2 overflow-auto bg-theme-main">
        <RouterView :id="id" />
      </div>
    </div>
  </div>
</template>
