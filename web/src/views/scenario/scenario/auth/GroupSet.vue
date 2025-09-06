<script setup lang="ts">
import { onMounted } from 'vue';
import { Icon, Image, Input, Scroll } from '@xcan-angus/vue-ui';

import { useGroupData } from './composables/useGroupData';
import type { GroupSetProps } from './types';


// Component props with proper typing
const props = withDefaults(defineProps<GroupSetProps>(), {
  type: 'user',
  loaded: false,
  checkedId: undefined,
  appId: undefined
});

// Define emits with proper typing
const emit = defineEmits<{
  (e: 'update:checkedId', id: string | undefined): void;
  (e: 'update:loaded', value: boolean): void;
}>();

// Use composable for group data management
const {
  dataSource,
  inputValue,
  activeId,
  nameKey,
  idKey,
  placeholder,
  apiPath,
  searchParams,
  handleScrollChange,
  handleInputChange,
  handleItemSelect,
  initializeConfig
} = useGroupData(props);

/**
 * Handle scroll data change and emit events
 */
const handleScrollDataChange = (data: any[]) => {
  const selectedId = handleScrollChange(data);
  if (selectedId) {
    emit('update:checkedId', selectedId);
  }

  if (!props.loaded) {
    emit('update:loaded', true);
  }
};

/**
 * Handle item selection and emit event
 */
const handleItemSelection = (id: string) => {
  const selectedId = handleItemSelect(id);
  emit('update:checkedId', selectedId);
};

// Lifecycle hooks
onMounted(() => {
  initializeConfig();
});
</script>
<template>
  <div class="h-full text-3 text-theme-title">
    <Input
      :value="inputValue"
      :allowClear="true"
      :placeholder="placeholder"
      size="small"
      class="mb-3.5"
      @change="(event: any) => handleInputChange(event)">
      <template #suffix>
        <Icon class="text-3.5 cursor-pointer text-theme-content" icon="icon-sousuo" />
      </template>
    </Input>

    <Scroll
      :lineHeight="44"
      :params="searchParams"
      :action="apiPath"
      style="height: calc(100% - 36px);"
      @change="handleScrollDataChange">
      <div
        v-for="item in dataSource"
        :key="item[idKey]"
        :class="{ 'active-item': activeId === item[idKey] }"
        class="flex items-center justify-between h-11 py-1.5 px-3 rounded cursor-pointer hover:bg-gray-hover"
        @click.stop="handleItemSelection(item[idKey])">
        <div class="flex items-center flex-nowrap">
          <Icon
            v-if="type === 'group'"
            class="mr-3 text-7"
            icon="icon-zu" />
          <Icon
            v-else-if="type === 'dept'"
            class="mr-3 text-7"
            icon="icon-bumen" />
          <Image
            v-else
            class="w-7 h-7 rounded-2xl mr-3"
            type="avatar"
            :src="item.avatar" />
          <span :title="item[nameKey]" class="leading-5 truncate">{{ item[nameKey] }}</span>
        </div>
      </div>
    </Scroll>
  </div>
</template>
<style scoped>
.active-item {
  background-color: rgba(239, 240, 243, 100%);
  color: #1890ff;
}
</style>
