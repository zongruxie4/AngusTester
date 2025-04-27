<script setup lang="ts">
import { onMounted, ref, watch } from 'vue';
import { useRoute } from 'vue-router';

type IMenu = {
  hasAuthFlag: boolean;
  tags: string[];
  authCtrlFlag: boolean;
  code: string;
  icon: string;
  name: string;
  showName: string;
  type: 'MENU';
  url: string;
}

type Props = {
  menus: IMenu[];
}

const props = withDefaults(defineProps<Props>(), {
  menus: () => []
});

const route = useRoute();

const menuList = ref<IMenu[]>([]);
const selectedUrl = ref(location.pathname);

const handleSelect = (menu: IMenu) => {
  selectedUrl.value = menu.url;
};

const isActive = (url: string): boolean => {
  if (!url) {
    return false;
  }

  return !!(selectedUrl.value && (url.startsWith(selectedUrl.value) || selectedUrl.value.startsWith(url)));
};

onMounted(() => {
  watch(() => route?.path, (newValue) => {
    selectedUrl.value = newValue;
  }, { immediate: true });

  watch(() => props.menus, (newValue) => {
    if (!newValue?.length) {
      return;
    }

    menuList.value = newValue;
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
</script>
<template>
  <ul class="menu-container flex text-3.5 header-item-text-normal space-x-6 truncate">
    <li
      v-for="item in menuList"
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
