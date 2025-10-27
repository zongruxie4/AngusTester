<script setup lang="ts">
import { computed, ref, defineAsyncComponent, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { Collapse, CollapsePanel } from 'ant-design-vue';
import { utils } from '@xcan-angus/infra';
import { Arrow, NoData, Icon, Spin } from '@xcan-angus/vue-ui';

import { ExecContent } from '@/plugins/test/types';

// Initialize i18n for internationalization
const { t } = useI18n();

/**
 * Assertion status type
 */
type AssertionStatus = 'Disabled' | 'Ignored' | 'Success' | 'Failed';

/**
 * Component props interface
 */
export interface Props {
  value: ExecContent;         // Execution content containing assertions
  ignoreAssertions: boolean;  // Global flag to ignore all assertions
}

// Define props with default values
const props = withDefaults(defineProps<Props>(), {
  value: undefined,
  ignoreAssertions: undefined
});

/**
 * Async component for assertion result details
 * Loaded on-demand to improve initial load performance
 */
const Result = defineAsyncComponent(() => import('./Result/index.vue'));

/**
 * Component state
 */
const activeKeys = ref<string[]>([]);                    // Array of expanded panel IDs
const loadingMap = ref<Record<string, boolean>>({});    // Map of loading states by assertion ID

/**
 * Handle expand/collapse arrow click
 * Toggles the expanded state of an assertion panel
 * 
 * @param id - Assertion ID to toggle
 */
const arrowChange = (id: string): void => {
  if (activeKeys.value.includes(id)) {
    // Collapse: remove from active keys
    activeKeys.value = activeKeys.value.filter(item => item !== id);
  } else {
    // Expand: add to active keys
    activeKeys.value.push(id);
  }
};

/**
 * Computed: Assertions list with unique IDs
 * Adds a unique ID to each assertion for tracking and keying
 * 
 * @returns Array of assertions with generated IDs
 */
const assertions = computed(() => {
  return props.value?.content?.assertions?.map(item => {
    return {
      id: utils.uuid(),
      ...item
    };
  }) || [];
});

/**
 * Computed: Status map for all assertions
 * Determines the status of each assertion based on:
 * 1. Global ignore flag
 * 2. Assertion enabled state
 * 3. Condition ignore state
 * 4. Assertion result
 * 
 * Priority order (highest to lowest):
 * - Global ignore → Ignored
 * - Disabled → Disabled
 * - Condition not met → Ignored
 * - Result failure → Failed
 * - Otherwise → Success
 * 
 * @returns Map of assertion ID to status
 */
const statusMap = computed((): Record<string, AssertionStatus> => {
  return assertions.value.reduce((prev, cur) => {
    if (props.ignoreAssertions) {
      // Global ignore flag set
      prev[cur.id] = 'Ignored';
    } else if (cur.enabled === false) {
      // Assertion is disabled
      prev[cur.id] = 'Disabled';
    } else if (cur?.ignore === true) {
      // Execution condition not met
      prev[cur.id] = 'Ignored';
    } else if (cur.result?.failure === true) {
      // Assertion failed
      prev[cur.id] = 'Failed';
    } else {
      // Assertion passed
      prev[cur.id] = 'Success';
    }

    return prev;
  }, {} as Record<string, AssertionStatus>);
});

/**
 * Watch for assertions changes
 * Initializes loading state for all assertions when they change
 * Sets all assertions to loading state initially
 */
watch(() => assertions.value, (newValue) => {
  if (!newValue?.length) {
    return;
  }

  // Initialize loading map with all assertions set to loading
  loadingMap.value = newValue.reduce((prev, cur) => {
    prev[cur.id] = true;
    return prev;
  }, {} as Record<string, boolean>);
}, { immediate: true });
</script>

<template>
  <!-- Empty state: no assertions -->
  <template v-if="!assertions.length">
    <NoData size="small" class="my-10" />
  </template>
  
  <!-- Assertions list: collapsible panels -->
  <template v-else>
    <Collapse
      :activeKey="activeKeys"
      :bordered="false"
      style="background-color: #fff;font-size: 12px;"
      class="tabs-collapse">
      
      <!-- Individual assertion panel -->
      <CollapsePanel
        v-for="item in assertions"
        :key="item.id"
        :showArrow="false"
        collapsible="disabled">
        
        <!-- Panel header: name + status indicator -->
        <template #header>
          <div class="w-full flex items-center">
            <!-- Expand/collapse arrow -->
            <Arrow 
              :open="activeKeys.includes(item.id)" 
              @change="arrowChange(item.id)" />
            
            <!-- Assertion name (truncated with tooltip) -->
            <div class="ml-1 w-104 truncate" :title="item.name">
              {{ item.name }}
            </div>
            
            <!-- Failed status: orange with X icon -->
            <div
              v-if="statusMap[item.id] === 'Failed'"
              class="flex items-center"
              style="color:rgba(255, 129, 0, 100%);">
              <Icon icon="icon-chahao" class="mr-1.5 text-3.5" />
              <span>{{ t('status.failed') }}</span>
            </div>
            
            <!-- Success status: green with check icon -->
            <div
              v-else-if="statusMap[item.id] === 'Success'"
              class="flex items-center"
              style="color:#52c41a;">
              <Icon icon="icon-duihao" class="mr-1.5 text-3.5" />
              <span>{{ t('status.passed') }}</span>
            </div>
            
            <!-- Ignored status: gray with dot -->
            <div
              v-else-if="statusMap[item.id] === 'Ignored'"
              class="flex items-center"
              style="color:rgba(217, 217, 217, 100%);">
              <span 
                class="inline-block w-2 h-2 mr-1.5 rounded-md" 
                style="background-color:rgba(217, 217, 217, 100%);"></span>
              <span>{{ t('status.ignored') }}</span>
            </div>
            
            <!-- Disabled status: gray with dot -->
            <div
              v-else-if="statusMap[item.id] === 'Disabled'"
              class="flex items-center"
              style="color:rgba(217, 217, 217, 100%);">
              <span 
                class="inline-block w-2 h-2 mr-1.5 rounded-md" 
                style="background-color:rgba(217, 217, 217, 100%);"></span>
              <span>{{ t('status.disabled') }}</span>
            </div>
          </div>
        </template>
        
        <!-- Panel content: assertion result details with loading spinner -->
        <Spin :spinning="loadingMap[item.id]" class="min-h-31.5">
          <Result
            v-model:loading="loadingMap[item.id]"
            :value="item"
            :status="statusMap[item.id]"
            :execContent="props.value?.content"
            :ignoreAssertions="props.ignoreAssertions" />
        </Spin>
      </CollapsePanel>
    </Collapse>
  </template>
</template>

<style scoped>
.ant-collapse.tabs-collapse > :deep(.ant-collapse-item) {
  border: none;
}

.ant-collapse.tabs-collapse > :deep(.ant-collapse-item) .ant-collapse-header{
  padding: 8px 0;
}
</style>
