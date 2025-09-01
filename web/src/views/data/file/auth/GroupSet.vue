<script setup lang="ts">
import { onMounted, watch } from 'vue';
import { Icon, Image, Input, Scroll } from '@xcan-angus/vue-ui';
import { useGroupSet } from './composables/useGroupSet';
import type { GroupSetProps, GroupSetEmits } from './types';

/**
 * <p>
 * GroupSet component for selecting users, departments, or groups
 * </p>
 * <p>
 * This component provides a searchable list interface for selecting different types
 * of entities with support for infinite scrolling and dynamic configuration
 * </p>
 */

// Component props with default values
const props = withDefaults(defineProps<GroupSetProps>(), {
  type: 'user',
  loaded: false,
  visible: false,
  checkedId: undefined,
  appId: undefined
});

// Component emits
const emit = defineEmits<GroupSetEmits>();

// Use the group set composable for logic separation
const {
  // State
  dataSource,
  inputValue,
  activeId,
  notify,
  nameKey,
  idKey,
  placeholder,
  apiPath,
  
  // Computed
  params,
  
  // Methods
  scrollChange,
  inputChange,
  checkedHandler,
  setupConfiguration,
  resetState
} = useGroupSet(props, emit);

// Lifecycle hooks
onMounted(() => {
  // Set up initial configuration
  watch([() => props.appId, () => props.type], () => {
    setupConfiguration();
  }, { immediate: true });

  // Reset state when visibility changes
  watch(() => props.visible, (newValue) => {
    if (!newValue) {
      return;
    }
    resetState();
  }, { immediate: true });
});
</script>

<template>
  <div class="h-full text-3 text-theme-title">
    <!-- Search Input -->
    <Input
      :value="inputValue"
      :allowClear="true"
      :placeholder="placeholder"
      size="small"
      class="mb-2"
      @change="inputChange"
    />
    
    <!-- Scrollable List -->
    <Scroll
      :lineHeight="44"
      :params="params"
      :action="apiPath"
      :notify="notify"
      style="height: calc(100% - 36px);"
      @change="scrollChange"
    >
      <!-- List Items -->
      <div
        v-for="item in dataSource"
        :key="item[idKey]"
        :class="{ 'active-item': activeId === item[idKey] }"
        class="flex items-center justify-between h-11 py-1.5 px-3 rounded cursor-pointer hover:bg-gray-hover"
        @click.stop="checkedHandler(item[idKey])"
      >
        <div class="flex items-center flex-nowrap">
          <!-- Department Icon -->
          <Icon
            v-if="type === 'group'"
            class="mr-3 text-7"
            icon="icon-zu"
          />
          <!-- Group Icon -->
          <Icon
            v-else-if="type === 'dept'"
            class="mr-3 text-7"
            icon="icon-bumen"
          />
          <!-- User Avatar -->
          <Image
            v-else
            class="w-7 h-7 rounded-2xl mr-3"
            type="avatar"
            :src="item.avatar"
          />
          
          <!-- Item Name -->
          <span 
            :title="item[nameKey]" 
            class="leading-5 truncate"
          >
            {{ item[nameKey] }}
          </span>
        </div>
      </div>
    </Scroll>
  </div>
</template>

<style scoped>
/**
 * <p>
 * Component-specific styles for the GroupSet component
 * </p>
 */

/* Active item styling */
.active-item {
  background-color: rgba(239, 240, 243, 100%);
  color: #1890ff;
}

/* Hover effects */
.hover\:bg-gray-hover:hover {
  background-color: rgba(239, 240, 243, 0.5);
}

/* Text styling */
.text-3 {
  font-size: 0.875rem;
}

.text-theme-title {
  color: var(--theme-title);
}
</style>
