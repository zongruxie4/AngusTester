<template>
  <div class="quick-search-options">
    <div class="flex items-center mb-3">
      <div class="flex items-start transform-gpu translate-y-0.5">
        <!-- Title indicator -->
        <div class="w-1 h-3 bg-gradient-to-b from-blue-500 to-blue-600 mr-2 mt-1 rounded-full"></div>

        <!-- Title and description -->
        <div class="whitespace-nowrap text-3 mt-0.5 text-text-sub-content">
          <span>{{ config.title || t('quickSearch.title') }}</span>
          <Colon />
        </div>

        <!-- Description slot -->
        <div v-if="descriptionSlot || $slots.description" class="ml-2 text-3 text-text-sub-content">
          <slot name="description">
            <div v-html="descriptionSlot"></div>
          </slot>
        </div>

        <!-- Options container -->
        <div class="flex flex-wrap ml-2">
          <div
            v-for="item in menuItems"
            :key="item.key"
            :class="{ 'active-key': isItemSelected(item.key) }"
            class="px-2.5 h-6 leading-6 mr-3 rounded bg-gray-light cursor-pointer font-semibold text-3 hover:bg-gray-200 transition-colors"
            @click="handleOptionClick(item)">
            {{ item.name }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
// import { computed } from 'vue';
import { useI18n } from 'vue-i18n';
import { Colon } from '@xcan-angus/vue-ui';
import { SearchCriteria } from '@xcan-angus/infra';
import { useQuickSearch } from './composables/useQuickSearch';
import type { QuickSearchOptionsProps } from './types';

const { t } = useI18n();

const props = withDefaults(defineProps<QuickSearchOptionsProps>(), {
  selectedOptions: () => [],
  descriptionSlot: ''
});

const emit = defineEmits<{
  (e: 'change', selectedKeys: string[], searchCriteria: SearchCriteria[]): void;
}>();

// Use the quick search composable
const {
  menuItems,
  selectedOptions,
  isAllSelected,
  handleOptionClick,
  resetSelections,
  getSearchCriteria,
  clearExternalConditions
} = useQuickSearch(props.config, (selectedKeys, searchCriteria) => {
  emit('change', selectedKeys, searchCriteria);
});

/**
 * Check if an item is selected
 * @param key - Item key to check
 * @returns Whether the item is selected
 */
const isItemSelected = (key: string): boolean => {
  if (key === 'all') {
    return isAllSelected;
  }
  return selectedOptions.includes(key);
};

// Expose methods for parent components
defineExpose({
  resetSelections,
  getSearchCriteria,
  clearExternalConditions
});
</script>

<style scoped>
.quick-search-options {
  margin-top: 0.625rem;
  margin-bottom: 0.875rem;
}

.active-key {
  background-color: #4ea0fd;
  color: #fff;
}

.active-key:hover {
  background-color: #3b82f6;
}
</style>
