<script setup lang="ts">
import FileIcon from '@/views/data/file/components/Icon.vue';
import { DropdownSort } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';
import { useSort } from './composables/useSort';

const emit = defineEmits<{(e: 'sort', param: { orderBy?: string; orderSort?: string }): void}>();

const { t } = useI18n();

// Use the sort composable
const { sortMenuItems, handleSortSelection } = useSort();

/**
 * Handle sort menu item selection
 *
 * @param sortData - Selected sort data
 */
const handleSelect = (sortData: { orderBy: string; orderSort: string }): void => {
  const sortParams = handleSortSelection(sortData);
  emit('sort', sortParams);
};
</script>
<template>
  <DropdownSort :menuItems="menus" @click="handleSelect">
    <file-icon icon="icon-shunxu" :title="t('fileSpace.sort.title')"></file-icon>
  </DropdownSort>
</template>
<style scoped>
.sort-file:hover .icon {
  @apply visible;
}

.sor-active {
  @apply text-text-link;
}
</style>
