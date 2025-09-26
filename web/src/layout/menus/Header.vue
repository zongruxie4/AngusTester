<script setup lang="ts">
import { onMounted, watch } from 'vue';
import { Dropdown, Menu, MenuItem } from 'ant-design-vue';
import { AuthAppFuncTree } from '@xcan-angus/infra';
import { Icon } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';

// Composables
import { useMenuState } from './composables/useMenuState';
import { useMenuResize } from './composables/useMenuResize';

const { t } = useI18n();

/**
 * Component props interface
 */
interface Props {
  menus: AuthAppFuncTree[];
}

const props = withDefaults(defineProps<Props>(), {
  menus: () => []
});

/**
 * Component emits interface
 */
const emit = defineEmits<{
  (e: 'change', menus: Array<AuthAppFuncTree>): void;
}>();

// Use composables
const {
  selectedUrl,
  handleMenuSelect,
  isMenuActive,
  watchRouteChanges
} = useMenuState(props.menus);

const {
  containerRef,
  moreRef,
  visibleMenuList,
  dropdownMenuList,
  initializeMenuList
} = useMenuResize();

/**
 * Handle menu selection with emit
 */
const handleMenuSelection = (menu: AuthAppFuncTree): void => {
  handleMenuSelect(menu);
  emit('change', menu.children || []);
};

/**
 * Dropdown overlay style
 */
const dropdownOverlayStyle = { minWidth: '120px' };

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
      handleMenuSelection(currentMenu);
    }
  }, { immediate: true });
});
</script>
<template>
  <ul ref="containerRef" class="menu-container flex text-3.5 header-item-text-normal space-x-6 truncate">
    <li
      v-for="item in visibleMenuList"
      :key="item.code"
      :class="{ 'menu-item-border-b link-active': isMenuActive(item.url || '') }"
      class="menu-item relative cursor-pointer whitespace-nowrap"
      @click="handleMenuSelection(item)">
      <RouterLink :to="item.url || ''" class="flex items-center header-item-text-normal text-theme-text-hover">
        <slot v-bind="item" :name="item.url?.split('/')?.[1]?.replace(/[^\da-z]/g, '')">
          <span class="flex-shrink-0">{{ item.showName }}</span>
        </slot>
      </RouterLink>
    </li>
    <Dropdown :overlayStyle="dropdownOverlayStyle">
      <li
        ref="moreRef"
        :class="{ hide: !dropdownMenuList?.length }"
        class="more-item">
        <span>{{ t('common.more') }}</span>
        <Icon icon="icon-xiala" class="ml-2 text-theme-sub-content" />
      </li>
      <template #overlay>
        <Menu class="header-dropdown-menu-container">
          <MenuItem
            v-for="menuItem in dropdownMenuList"
            :key="menuItem.code"
            :class="{ 'link-active': isMenuActive(menuItem.url || '') }">
            <RouterLink
              :to="menuItem.url || ''"
              class="flex items-center header-item-text-normal text-theme-text-hover"
              @click.prevent="handleMenuSelection(menuItem)">
              {{ menuItem.showName }}
            </RouterLink>
          </MenuItem>
        </Menu>
      </template>
    </Dropdown>
  </ul>
</template>
<style scoped>
li.last-type:last-of-type {
  margin-right: 0;
}

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

.more-item {
  display: flex;
  align-items: center;
  white-space: nowrap;
  cursor: pointer;
}

.more-item.hide {
  position: absolute;
  z-index: -99;
  top: -54px;
  opacity: 0;
  background-color: transparent;
  color: transparent;
}

.menu-item.link-active a {
  color: var(--content-special-text-hover);
}
</style>

<style>
.header-dropdown-menu-container .ant-dropdown-menu-item {
  font-size: 14px;
}

.header-dropdown-menu-container .ant-dropdown-menu-item+.ant-dropdown-menu-item {
  margin-top: 2px;
}

.header-dropdown-menu-container .link-active.ant-dropdown-menu-item a {
  background-color: var(--content-tabs-bg-hover);
  color: var(--content-special-text-hover);
}
</style>
