<template>
  <div class="script-search-example">
    <h3>Script Search Example</h3>

    <!-- Using QuickSearchOptions component -->
    <QuickSearchOptions
      ref="quickSearchRef"
      :config="quickSearchConfig"
      :descriptionSlot="descriptionText"
      @change="handleQuickSearchChange" />

    <!-- Other search conditions -->
    <div class="other-search-conditions">
      <p>Other search conditions area...</p>
      <button @click="clearOtherConditions">Clear Other Conditions</button>
    </div>

    <!-- Display current search criteria -->
    <div class="current-criteria">
      <h4>Current Search Criteria:</h4>
      <pre>{{ JSON.stringify(currentSearchCriteria, null, 2) }}</pre>
    </div>
  </div>
</template>
<script setup lang="ts">
import { ref } from 'vue';
import { SearchCriteria } from '@xcan-angus/infra';
import {
  QuickSearchOptions,
  createAuditOptions,
  createTimeOptions,
  type QuickSearchConfig
} from '../index';

// const { t } = useI18n();

// Mock current user ID
const currentUserId = 'user123';

// Description text (can be HTML string)
const descriptionText = 'Quickly filter script data to <strong>improve search efficiency</strong>';

// Quick search configuration
const quickSearchConfig: QuickSearchConfig = {
  title: 'Quick Search',
  // Audit information options
  auditOptions: createAuditOptions([
    { key: 'createdBy', name: 'Created by me', fieldKey: 'createdBy' },
    { key: 'lastModifiedBy', name: 'Modified by me', fieldKey: 'lastModifiedBy' }
  ], currentUserId),

  // Enum status options (using enumType for automatic generation)
  // enumOptions: createEnumOptions([
  //   { key: 'performance', name: 'Performance Test', fieldKey: 'type', fieldValue: 'PERFORMANCE' },
  //   { key: 'functional', name: 'Functional Test', fieldKey: 'type', fieldValue: 'FUNCTIONAL' },
  //   { key: 'stability', name: 'Stability Test', fieldKey: 'type', fieldValue: 'STABILITY' },
  //   { key: 'customization', name: 'Customization Test', fieldKey: 'type', fieldValue: 'CUSTOMIZATION' }
  // ]),

  // Using enumType for automatic enum options generation
  // enumType: createEnumTypeConfig(ScriptType, 'type', [ScriptType.MOCK_APIS]),

  // Time options
  timeOptions: createTimeOptions([
    { key: 'last1Day', name: 'Last 1 Day', timeRange: 'last1Day' },
    { key: 'last3Days', name: 'Last 3 Days', timeRange: 'last3Days' },
    { key: 'last7Days', name: 'Last 7 Days', timeRange: 'last7Days' },
    { key: 'last30Days', name: 'Last 30 Days', timeRange: 'last30Days' }
  ], 'createdDate'),

  // External clear function
  externalClearFunction: () => {
    clearOtherConditions();
  }
};

// Component reference
const quickSearchRef = ref();

// Current search criteria
const currentSearchCriteria = ref<SearchCriteria[]>([]);

/**
 * Handle quick search changes
 */
const handleQuickSearchChange = (selectedKeys: string[], searchCriteria: SearchCriteria[]) => {
  console.log('Selected options:', selectedKeys);
  console.log('Search criteria:', searchCriteria);
  currentSearchCriteria.value = searchCriteria;

  // Here you can call API to perform search
  // performSearch(searchCriteria);
};

/**
 * Clear other conditions
 */
const clearOtherConditions = () => {
  console.log('Clear other search conditions');
  // Here implement the logic to clear other search conditions
};

/**
 * Reset quick search
 */
// const resetQuickSearch = () => {
//   quickSearchRef.value?.resetSelections();
// };

/**
 * Get current search criteria
 */
// const getCurrentCriteria = () => {
//   return quickSearchRef.value?.getSearchCriteria() || [];
// };
</script>

<style scoped>
.script-search-example {
  padding: 20px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  margin: 20px 0;
}

.other-search-conditions {
  margin: 20px 0;
  padding: 10px;
  background-color: #f5f5f5;
  border-radius: 4px;
}

.current-criteria {
  margin-top: 20px;
  padding: 10px;
  background-color: #f9f9f9;
  border-radius: 4px;
}

.current-criteria pre {
  margin: 0;
  font-size: 12px;
  color: #666;
}
</style>
