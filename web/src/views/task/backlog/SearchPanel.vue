<script setup lang="ts">
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';
import { Input, Colon } from '@xcan-angus/vue-ui';

interface SearchState {
  searchValue?: string;
  createdBy?: string;
  assigneeId?: string;
  quickDate?: string;
}

interface Props {
  search: SearchState;
  userId?: string;
  selectNone?: boolean;
}

interface Emits {
  (e: 'searchChange', value: string): void;
  (e: 'clearAllFilters'): void;
  (e: 'toggleCreatedByMeFilter', userId: string): void;
  (e: 'toggleAssignedToMeFilter', userId: string): void;
  (e: 'toggleDateFilter', value: string): void;
}

const props = defineProps<Props>();
const emit = defineEmits<Emits>();
const { t } = useI18n();

const searchValue = computed({
  get: () => props.search.searchValue || '',
  set: (value: string) => {
    emit('searchChange', value);
  }
});

const handleSearchChange = (value: string) => {
  emit('searchChange', value);
};

const handleClearAllFilters = () => {
  emit('clearAllFilters');
};

const handleToggleCreatedByMeFilter = () => {
  if (props.userId) {
    emit('toggleCreatedByMeFilter', props.userId);
  }
};

const handleToggleAssignedToMeFilter = () => {
  if (props.userId) {
    emit('toggleAssignedToMeFilter', props.userId);
  }
};

const handleToggleDateFilter = (value: string) => {
  emit('toggleDateFilter', value);
};
</script>
<template>
  <div class="flex items-center">
    <Input
      v-model:value="searchValue"
      :maxlength="200"
      allowClear
      class="search-input mr-5"
      :placeholder="t('backlog.placeholder.searchTaskName')"
      trim
      @change="handleSearchChange" />

    <div class="whitespace-nowrap text-text-sub-content mr-2 font-semibold">
      <span>{{ t('quickSearch') }}</span>
      <Colon />
    </div>

    <div
      :class="{ 'active-key': !!selectNone }"
      class="px-2.5 h-6 leading-6 rounded bg-gray-light cursor-pointer select-none mr-2 font-semibold"
      @click="handleClearAllFilters">
      {{ t('backlog.quickSearch.all') }}
    </div>

    <div
      :class="{ 'active-key': search.createdBy === userId }"
      class="px-2.5 h-6 leading-6 rounded bg-gray-light cursor-pointer select-none mr-2 font-semibold"
      @click="handleToggleCreatedByMeFilter">
      {{ t('backlog.quickSearch.createdByMe') }}
    </div>

    <div
      :class="{ 'active-key': search.assigneeId === userId }"
      class="px-2.5 h-6 leading-6 rounded bg-gray-light cursor-pointer select-none mr-2 font-semibold"
      @click="handleToggleAssignedToMeFilter">
      {{ t('backlog.quickSearch.assignedToMe') }}
    </div>

    <div
      :class="{ 'active-key': search.quickDate === '1' }"
      class="px-2.5 h-6 leading-6 rounded bg-gray-light cursor-pointer select-none mr-2 font-semibold"
      @click="handleToggleDateFilter('1')">
      {{ t('backlog.quickSearch.lastDay') }}
    </div>

    <div
      :class="{ 'active-key': search.quickDate === '3' }"
      class="px-2.5 h-6 leading-6 rounded bg-gray-light cursor-pointer select-none mr-2 font-semibold"
      @click="handleToggleDateFilter('3')">
      {{ t('backlog.quickSearch.last3Day') }}
    </div>

    <div
      :class="{ 'active-key': search.quickDate === '7' }"
      class="px-2.5 h-6 leading-6 rounded bg-gray-light cursor-pointer select-none font-semibold"
      @click="handleToggleDateFilter('7')">
      {{ t('backlog.quickSearch.last7Day') }}
    </div>
  </div>
</template>
<style scoped>
.search-input {
  width: 240px !important;
  max-width: 240px !important;
  min-width: 240px !important;
}

.active-key {
  background-color: #4ea0fd;
  color: #fff;
}
</style>
