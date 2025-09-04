<script setup lang="ts">
import { onMounted, watch } from 'vue';
import { AuthAppFuncTree } from '@xcan-angus/infra';

// Composables
import { useMenuState } from './composables/useMenuState';

/**
 * Component props interface
 */
interface Props {
  menus: AuthAppFuncTree[];
}

const props = withDefaults(defineProps<Props>(), {
  menus: () => []
});

// Use composables
const {
  menuList,
  selectedUrl,
  handleMenuSelect,
  isMenuActive,
  watchRouteChanges,
  initializeMenuList
} = useMenuState(props.menus);

/**
 * Initialize component
 */
onMounted(() => {
  // Watch for route changes
  watchRouteChanges();

  // Watch for menu changes and initialize
  watch(() => props.menus, (newValue) => {
    if (!newValue?.length) {
      return;
    }

    initializeMenuList(newValue);

    // Find and select current menu
    const currentMenu = newValue.find(item => {
      return selectedUrl.value && item.url && (
        item.url.startsWith(selectedUrl.value) ||
        selectedUrl.value.startsWith(item.url)
      );
    });

    if (currentMenu) {
      handleMenuSelect(currentMenu);
    }
  }, { immediate: true });
});
</script>
<template>
  <ul class="menu-container flex text-3.5 header-item-text-normal space-x-6 truncate">
    <li
      v-for="item in menuList"
      :key="item.code"
      :class="{ 'menu-item-border-b link-active': isMenuActive(item.url || '') }"
      class="menu-item relative cursor-pointer whitespace-nowrap"
      @click="handleMenuSelect(item)">
      <RouterLink :to="item.url || ''" class="flex items-center header-item-text-normal text-theme-text-hover">
        <slot v-bind="item" :name="item.url?.split('/')?.[1]?.replace(/[^\da-z]/g, '')">
          <span class="flex-shrink-0">{{ item.showName }}</span>
        </slot>
      </RouterLink>
    </li>
  </ul>
</template>
<style scoped>
.anticon :deep(svg) {
  font-size: 12px;
}

.menu-container {
  height: 54px;
  line-height: 54px;
}

.menu-item-border-b::after {
  content: "";
  display: block;
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 2px;
  border-radius: 2px;
  background-color: var(--navigation-menu-item-border-selected);
}

.menu-item.link-active a {
  color: var(--content-special-text-hover);
}
</style>
