<script setup lang="ts">
// Vue core imports
import { computed, provide, ref, shallowRef, watch } from 'vue';

// UI component imports
import { VuexHelper, Icon, NoData } from '@xcan-angus/vue-ui';
import { Tooltip } from 'ant-design-vue';

// Local imports
import { NavItem } from './PropsType';

/**
 * Component props interface for drawer
 */
interface Props {
  dataSource: NavItem[];
  beforeSelect?: () => Promise<void>;
  compProps?: Record<string, any>;
}

// Component props with defaults
const props = withDefaults(defineProps<Props>(), {
  dataSource: () => [],
  beforeSelect: () => Promise.resolve(),
  compProps: () => ({})
});

// Component state
const isDrawerExpanded = ref(false);
const selectedNavigationItem = shallowRef<NavItem | null>();

/**
 * Handle drawer close action
 */
const handleDrawerClose = (): void => {
  isDrawerExpanded.value = false;
  selectedNavigationItem.value = null;
};

/**
 * Handle navigation item selection
 */
const handleNavigationSelection = async (navigationId: string): Promise<void> => {
  await props.beforeSelect && props.beforeSelect();
  selectedNavigationItem.value = props.dataSource.find(item => item.value === navigationId);
  if (selectedNavigationItem.value?.value) {
    isDrawerExpanded.value = true;
  } else {
    isDrawerExpanded.value = false;
  }
};

// Computed properties
const drawerSpreadClass = computed(() => {
  return isDrawerExpanded.value ? 'open' : '';
});

// Vuex integration
const { useState } = VuexHelper;
const { activeDrawer, activeDrawerId } = useState(['activeDrawer', 'activeDrawerId'], 'apiStore');

// Watchers
watch(() => activeDrawer.value, () => {
  handleNavigationSelection(activeDrawerId.value as string);
}, {
  deep: true
});

// Component exposure and provide
defineExpose({ isDrawerExpanded, handleNavigationSelection }); // Expose to parent component
provide('handleNavigationSelection', handleNavigationSelection); // Provide to child components
</script>
<template>
  <div class="flex flex-shrink-0 flex-grow-0 h-full border-l border-theme-text-box bg-white overflow-hidden">
    <div class="pt-4.5">
      <div
        v-for="navigationItem in props.dataSource"
        :key="navigationItem.value"
        :class="{ 'text-theme-special': selectedNavigationItem?.value === navigationItem.value }"
        class="h-6 px-5 mb-3.5 leading-6 cursor-pointer text-theme-text-hover"
        @click="handleNavigationSelection(navigationItem.value)">
        <Tooltip placement="left" color="#fff">
          <template #title><div class="leading-5">{{ navigationItem.name }}</div></template>
          <Icon class="text-3.5 leading-3.5" :icon="navigationItem.icon" />
        </Tooltip>
      </div>
    </div>
    <div :class="drawerSpreadClass" class="main-container h-full whitespace-nowrap relative overflow-hidden flex flex-col">
      <Icon
        class="absolute right-5 top-5.5 cursor-pointer z-9 text-gray-icon hover:text-text-link-hover"
        icon="icon-shanchuguanbi"
        @click="handleDrawerClose" />
      <div class="text-3 leading-3 text-text-sub-content font-medium pt-5.5 pb-2">
        {{ selectedNavigationItem?.name }}
      </div>
      <div class="flex-1 pr-3.5 overflow-auto py-2">
        <template v-if="(selectedNavigationItem as any)?.component">
          <component
            :is="(selectedNavigationItem as any).component"
            v-bind="{...props.compProps, ...selectedNavigationItem}"></component>
        </template>
        <template v-else>
          <NoData />
        </template>
      </div>
    </div>
  </div>
</template>
<style scoped>
.main-container {
  @apply opacity-0 w-0 transition-all duration-500;
}

.open {
  @apply opacity-100 w-75;
}
</style>
