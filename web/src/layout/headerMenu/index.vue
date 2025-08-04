<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue';
import { useRoute } from 'vue-router';
import { Dropdown, Menu, MenuItem } from 'ant-design-vue';
import elementResizeDetector from 'element-resize-detector';
import { debounce } from 'throttle-debounce';
import { duration } from '@xcan-angus/infra';
import { Icon } from '@xcan-angus/vue-ui';

type Tag = 'DYNAMIC_POSITION' | 'TOP_NAVIGATION' | 'FIXED_POSITION' | 'HEADER_MENU_POPOVER'

interface IMenu {
  hasAuth: boolean;
  tags: {name: Tag}[];
  authCtrlFlag: boolean;
  code: string;
  icon: string;
  name: string;
  showName: string;
  type: 'BUTTON' | 'MENU' | 'PANEL';
  url: string;
  visible: boolean; // 显示的菜单
  width: number; // 菜单宽度
  children?: IMenu[];
  enabled: boolean;
  authCtrl: boolean;
}

interface Props {
  menus: IMenu[];
}

const props = withDefaults(defineProps<Props>(), {
  menus: () => []
});

const route = useRoute();
const emit = defineEmits<{(e: 'change', menus: Array<IMenu>): void }>();

const erd = elementResizeDetector({ strategy: 'scroll' });
const containerRef = ref();

const menuList = ref<IMenu[]>([]);

const moreRef = ref();

const moreElementWidth = computed(() => {
  return moreRef.value?.offsetWidth || 0;
});

const selectedUrl = ref(location.pathname);
const overlayStyle = { minWidth: '120px' };

const setMenuAttr = (_menus: IMenu[]) => {
  const domList: HTMLElement[] = [];
  const temp = _menus?.map(item => {
    const ghostDom = document.createElement('a');
    ghostDom.innerText = item.showName;
    ghostDom.setAttribute('style', 'position:absolute;z-index:-999999;color:transparent;background:transparent;padding:0;font-size:14px;');
    document.body.appendChild(ghostDom);
    domList.push(ghostDom);
    item.width = ghostDom.offsetWidth;
    item.show = true;
    return item;
  });

  domList.every(item => {
    document.body.removeChild(item);
    return true;
  });

  return temp;
};

const handleSelect = (menu: IMenu) => {
  selectedUrl.value = menu.url;
  emit('change', menu.children || []);
};

const isActive = (url: string): boolean => {
  if (!url) {
    return false;
  }

  return !!(selectedUrl.value && (url.startsWith(selectedUrl.value) || selectedUrl.value.startsWith(url)));
};

const visibleMenuList = computed(() => {
  return menuList.value.filter(item => item.show);
});

const dropdownMenuList = computed(() => {
  return menuList.value.filter(item => !item.show);
});

const resizeHandler = debounce(duration.resize, () => {
  const width = containerRef.value?.offsetWidth || 0;
  let totalWidth = 0;
  for (let i = 0, len = menuList.value.length; i < len; i++) {
    totalWidth += menuList.value[i].width + (i > 0 ? 28 : 0);
    if (totalWidth <= width) {
      menuList.value[i].show = true;
    } else {
      for (let j = menuList.value.length; j--;) {
        if (j >= i) {
          menuList.value[j].show = false;
        } else {
          totalWidth -= (menuList.value[j].width + (j > 0 ? 28 : 0));
          if ((totalWidth + moreElementWidth.value + 28) <= width) {
            break;
          } else {
            menuList.value[j].show = false;
          }
        }
      }
      break;
    }
  }
});

onMounted(() => {
  erd.listenTo(containerRef.value, resizeHandler);

  watch(() => route?.path, (newValue) => {
    selectedUrl.value = newValue;
  }, { immediate: true });

  watch(() => props.menus, (newValue) => {
    if (!newValue?.length) {
      return;
    }
    menuList.value = setMenuAttr(newValue.filter(i => i.code !== 'Mock' && (i.enabled || !i.authCtrl) && (i.tags || []).some(t  => t.name === 'DYNAMIC_POSITION')));

    if (menuList.value?.length) {
      const currentMenu = menuList.value?.find(item => {
        return selectedUrl.value && (item.url.startsWith(selectedUrl.value) || selectedUrl.value.startsWith(item.url));
      });

      if (currentMenu) {
        handleSelect(currentMenu);
      }
    }
  }, { immediate: true });
});

onBeforeUnmount(() => {
  erd.removeListener(containerRef.value, resizeHandler);
});
</script>
<template>
  <ul ref="containerRef" class="menu-container flex text-3.5 header-item-text-normal space-x-6 truncate">
    <li
      v-for="item in visibleMenuList"
      :key="item.code"
      :class="{ 'menu-item-border-b link-active': isActive(item.url) }"
      class="menu-item relative cursor-pointer whitespace-nowrap"
      @click="handleSelect(item)">
      <RouterLink :to="item.url" class="flex items-center header-item-text-normal text-theme-text-hover">
        <slot v-bind="item" :name="item.url?.split('/')?.[1]?.replace(/[^\da-z]/g, '')">
          <span class="flex-shrink-0">{{ item.showName }}</span>
        </slot>
      </RouterLink>
    </li>
    <Dropdown :overlayStyle="overlayStyle">
      <li
        ref="moreRef"
        :class="{ hide: !dropdownMenuList?.length }"
        class="more-item">
        <span>更多</span>
        <Icon icon="icon-xiala" class="ml-2 text-theme-sub-content" />
      </li>
      <template #overlay>
        <Menu class="header-dropdown-menu-container">
          <MenuItem
            v-for="ele in dropdownMenuList"
            :key="ele.code"
            :class="{ 'link-active': isActive(ele.url) }">
            <RouterLink
              :to="ele.url"
              class="flex items-center header-item-text-normal text-theme-text-hover"
              @click.prevent="handleSelect(ele)">
              {{ ele.showName }}
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
