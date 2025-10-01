<script setup lang="ts">
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';
import { Input, Colon } from '@xcan-angus/vue-ui';
import { debounce } from 'throttle-debounce';
import { duration } from '@xcan-angus/infra';

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
  (e: 'update:search', value: SearchState):void;
}

const props = defineProps<Props>();
const emit = defineEmits<Emits>();
const { t } = useI18n();

const searchValue = computed({
  get: () => props.search.searchValue || '',
  set: (value: string) => {
    emit('update:search', {
      ...props.search,
      searchValue: value
    });
    // emit('searchChange', value);
  }
});

const handleSearchChange = debounce(duration.search, (event) => {
  emit('update:search', {
    ...props.search,
    searchValue: event.target.value
  });
  emit('searchChange', event.target.value);
});

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
      :value="searchValue"
      :maxlength="200"
      allowClear
      class="search-input mr-5"
      :placeholder="t('common.placeholders.searchKeyword')"
      trim
      @change="handleSearchChange" />

    <div class="whitespace-nowrap text-text-sub-content mr-2 font-semibold">
      <span>{{ t('quickSearch.title') }}</span>
      <Colon />
    </div>

    <div
      :class="{ 'active-key': !!selectNone }"
      class="px-2.5 h-6 leading-6 rounded bg-gray-light cursor-pointer select-none mr-2 font-semibold"
      @click="handleClearAllFilters">
      {{ t('common.all') }}
    </div>

    <div
      :class="{ 'active-key': search.createdBy === userId }"
      class="px-2.5 h-6 leading-6 rounded bg-gray-light cursor-pointer select-none mr-2 font-semibold"
      @click="handleToggleCreatedByMeFilter">
      {{ t('quickSearch.createdByMe') }}
    </div>

    <div
      :class="{ 'active-key': search.assigneeId === userId }"
      class="px-2.5 h-6 leading-6 rounded bg-gray-light cursor-pointer select-none mr-2 font-semibold"
      @click="handleToggleAssignedToMeFilter">
      {{ t('quickSearch.assignedToMe') }}
    </div>

    <div
      :class="{ 'active-key': search.quickDate === '1' }"
      class="px-2.5 h-6 leading-6 rounded bg-gray-light cursor-pointer select-none mr-2 font-semibold"
      @click="handleToggleDateFilter('1')">
      {{ t('quickSearch.last1Day') }}
    </div>

    <div
      :class="{ 'active-key': search.quickDate === '3' }"
      class="px-2.5 h-6 leading-6 rounded bg-gray-light cursor-pointer select-none mr-2 font-semibold"
      @click="handleToggleDateFilter('3')">
      {{ t('quickSearch.last3Days') }}
    </div>

    <div
      :class="{ 'active-key': search.quickDate === '7' }"
      class="px-2.5 h-6 leading-6 rounded bg-gray-light cursor-pointer select-none font-semibold"
      @click="handleToggleDateFilter('7')">
      {{ t('quickSearch.last7Days') }}
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
