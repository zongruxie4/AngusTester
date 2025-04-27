<script setup lang="ts">
import { computed, provide, ref, shallowRef, watch } from 'vue';
import { VuexHelper, Icon, NoData } from '@xcan-angus/vue-ui';
import { Tooltip } from 'ant-design-vue';

import { NavItem } from './PropsType';

interface Props {
  dataSource: NavItem[],
  beforeSelect?: ()=> Promise<void>,
  compProps?: Record<string, any>
}

const props = withDefaults(defineProps<Props>(), {
  dataSource: () => [],
  beforeSelect: () => Promise.resolve(),
  compProps: () => ({})
});

const isSpread = ref(false);
const currentNav = shallowRef<NavItem | null>();

const close = (): void => {
  isSpread.value = false;
  currentNav.value = null;
};

const selectHandle = async (id: string): Promise<void> => {
  await props.beforeSelect && props.beforeSelect();
  currentNav.value = props.dataSource.find(item => item.value === id);
  if (currentNav.value?.value) {
    isSpread.value = true;
  } else {
    isSpread.value = false;
  }
};

const spreadClass = computed(() => {
  return isSpread.value ? 'open' : '';
});

const { useState } = VuexHelper;
const { activeDrawer, activeDrawerId } = useState(['activeDrawer', 'activeDrawerId'], 'apiStore');
watch(() => activeDrawer.value, () => {
  selectHandle(activeDrawerId.value);
}, {
  deep: true
});

defineExpose({ isSpread, selectHandle }); // 暴露给父级控制
provide('selectHandle', selectHandle); // 暴露给子组件控制
</script>
<template>
  <div class="flex flex-shrink-0 flex-grow-0 h-full border-l border-theme-text-box bg-white overflow-hidden">
    <div class="pt-4.5">
      <div
        v-for="item in dataSource"
        :key="item.value"
        :class="{ 'text-theme-special': currentNav?.value === item.value }"
        class="h-6 px-5 mb-3.5 leading-6 cursor-pointer text-theme-text-hover"
        @click="selectHandle(item.value)">
        <Tooltip placement="left" color="#fff">
          <template #title><div class="leading-5">{{ item.name }}</div></template>
          <Icon class="text-3.5 leading-3.5" :icon="item.icon" />
        </Tooltip>
      </div>
    </div>
    <div :class="spreadClass" class="main-container h-full whitespace-nowrap relative overflow-hidden flex flex-col">
      <Icon
        class="absolute right-5 top-5.5 cursor-pointer z-9 text-gray-icon hover:text-text-link-hover"
        icon="icon-shanchuguanbi"
        @click="close" />
      <div class="text-3 leading-3 text-text-sub-content font-medium pt-5.5 pb-2">
        {{ currentNav?.name }}
      </div>
      <div class="flex-1 pr-3.5 overflow-auto py-2">
        <template v-if="currentNav?.component">
          <component
            :is="currentNav.component"
            v-bind="{...compProps, ...currentNav}"></component>
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
