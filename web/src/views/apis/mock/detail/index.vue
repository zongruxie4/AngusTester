<script setup lang="ts">
import { useRoute } from 'vue-router';
import { Breadcrumb } from 'ant-design-vue';
import { Sidebar } from '@xcan-angus/vue-ui';

import { useMockNavigation } from './composables/useMockNavigation';

const route = useRoute();
const id = route.params.id as string;

// Use the mock navigation composable
const { currentMenu, menuItems } = useMockNavigation(id);
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
