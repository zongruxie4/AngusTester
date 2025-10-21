<script setup lang="ts">
// Vue core imports
import { computed, provide, ref } from 'vue';

// UI component imports
import { Tooltip } from 'ant-design-vue';
import { Icon } from '@xcan-angus/vue-ui';

/**
 * Navigation item interface for drawer slot component
 */
interface NavItem {
  icon: string;
  name: string;
  id: string;
}

/**
 * Component props interface for drawer slot
 */
interface Props {
  dataSource: NavItem[];
}

// Component props with defaults
const props = withDefaults(defineProps<Props>(), {
  dataSource: () => []
});

// Component state
const isDrawerExpanded = ref(false);
const selectedNavigationItem = ref<NavItem>();

/**
 * Handle drawer close action
 */
const handleDrawerClose = (): void => {
  isDrawerExpanded.value = false;
  selectedNavigationItem.value = undefined;
};

/**
 * Handle navigation item selection
 */
const handleNavigationSelection = async (navigationId: string): Promise<void> => {
  selectedNavigationItem.value = props.dataSource.find(item => item.id === navigationId);
  if (selectedNavigationItem.value?.id) {
    isDrawerExpanded.value = true;
  } else {
    isDrawerExpanded.value = false;
  }
};

// Computed properties
const selectedNavigationId = computed(() => {
  return selectedNavigationItem.value?.id;
});

const drawerSpreadClass = computed(() => {
  return isDrawerExpanded.value ? 'open' : '';
});

// Component exposure and provide
defineExpose({ handleNavigationSelection, isDrawerExpanded, selectedNavigationId }); // Expose to parent component
provide('handleNavigationSelection', handleNavigationSelection); // Provide to child components
provide('handleDrawerClose', handleDrawerClose);
</script>
<template>
  <div class="flex flex-shrink-0 flex-grow-0 h-full border-l border-theme-text-box bg-white overflow-hidden">
    <div class="pt-4.5">
      <div
        v-for="navigationItem in props.dataSource"
        :key="navigationItem.id"
        :class="{ 'text-theme-special': selectedNavigationItem?.id === navigationItem.id }"
        class="h-6 px-5 mb-3.5 leading-6 cursor-pointer text-theme-text-hover"
        @click="handleNavigationSelection(navigationItem.id)">
        <Tooltip placement="left" color="#fff">
          <template #title>{{ navigationItem.name }}</template>
          <Icon class="text-3.5 leading-3.5" :icon="navigationItem.icon" />
        </Tooltip>
      </div>
    </div>
    <div
      :class="drawerSpreadClass"
      class="main-container h-full whitespace-nowrap relative overflow-hidden">
      <Icon
        class="absolute right-5 top-5.5 cursor-pointer z-9 text-gray-icon hover:text-text-link-hover"
        icon="icon-shanchuguanbi"
        @click="handleDrawerClose" />
      <div class="h-full pt-6 pr-5 overflow-auto">
        <div class="text-3 leading-3 text-text-sub-content font-medium">{{ selectedNavigationItem?.name }}</div>
        <slot :id="selectedNavigationItem?.id" name="default">
        </slot>
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
