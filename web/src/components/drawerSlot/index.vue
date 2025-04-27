<script setup lang="ts">
import { computed, provide, ref } from 'vue';
import { Tooltip } from 'ant-design-vue';
import { Icon } from '@xcan-angus/vue-ui';

interface NavItem {
  icon: string;
  name: string;
  id: string;
}

interface Props {
  dataSource: NavItem[];
}

const props = withDefaults(defineProps<Props>(), {
  dataSource: () => []
});

const isSpread = ref(false);
const currentNav = ref<NavItem>();

const close = (): void => {
  isSpread.value = false;
  currentNav.value = undefined;
};

const selectHandle = async (id: string): Promise<void> => {
  currentNav.value = props.dataSource.find(item => item.id === id);
  if (currentNav.value?.id) {
    isSpread.value = true;
  } else {
    isSpread.value = false;
  }
};

const id = computed(() => {
  return currentNav.value?.id;
});

const spreadClass = computed(() => {
  return isSpread.value ? 'open' : '';
});

defineExpose({ selectHandle, isSpread, id }); // 暴露给父级控制
provide('selectHandle', selectHandle); // 暴露给子组件控制
provide('close', close);
</script>
<template>
  <div class="flex flex-shrink-0 flex-grow-0 h-full border-l border-theme-text-box bg-white overflow-hidden">
    <div class="pt-4.5">
      <div
        v-for="item in dataSource"
        :key="item.id"
        :class="{ 'text-theme-special': currentNav?.id === item.id }"
        class="h-6 px-5 mb-3.5 leading-6 cursor-pointer text-theme-text-hover"
        @click="selectHandle(item.id)">
        <Tooltip placement="left" color="#fff">
          <template #title>{{ item.name }}</template>
          <Icon class="text-3.5 leading-3.5" :icon="item.icon" />
        </Tooltip>
      </div>
    </div>
    <div
      :class="spreadClass"
      class="main-container h-full whitespace-nowrap relative overflow-hidden">
      <Icon
        class="absolute right-5 top-5.5 cursor-pointer z-9 text-gray-icon hover:text-text-link-hover"
        icon="icon-shanchuguanbi"
        @click="close" />
      <div class="h-full pt-6 pr-5 overflow-auto">
        <div class="text-3 leading-3 text-text-sub-content font-medium">{{ currentNav?.title }}</div>
        <slot :id="currentNav?.id" name="default">
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
