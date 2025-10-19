
<script setup lang="ts">
import { defineAsyncComponent } from 'vue';
import { Collapse, CollapsePanel } from 'ant-design-vue';
import { Icon } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';

// Lazy-loaded pie chart component for error distribution visualization
const PirChart = defineAsyncComponent(() => import('./PirChart.vue'));

/**
 * Error statistics item interface
 */
interface ErrorStatItem {
  name: string;        // API or pipeline name
  errorNum: number;    // Total error count
  errorRate: string;   // Error rate percentage
  list?: ErrorStatItem[];  // Child items (nested errors)
}

/**
 * Component props interface
 */
interface Props {
  list: ErrorStatItem[];  // Array of error statistics items
}

// Define props with default values
const props = withDefaults(defineProps<Props>(), {
  list: () => []
});

// Initialize i18n for internationalization
const { t } = useI18n();
</script>
<template>
  <!-- Main container for error statistics list -->
  <div class="text-3 text-text-content">
    <!-- Table header with column titles -->
    <div class="h-9.5 leading-9.5 bg-theme-form-head flex font-medium w-full">
      <!-- Spacer for expand icon alignment -->
      <div class="w-12"></div>
      
      <!-- Error statistics label (flexible width) -->
      <div class="py-0.5 px-2 flex-1">
        {{ t('ftpPlugin.performanceTestDetail.conterList.errorStatistics') }}
      </div>
      
      <!-- Error count column (30% width) -->
      <div style="width:30%" class="py-0.5 px-2">
        {{ t('ftpPlugin.performanceTestDetail.conterList.errorCount') }}
      </div>
      
      <!-- Error rate column (30% width) -->
      <div style="width:30%" class="py-0.5 px-2">
        {{ t('ftpPlugin.performanceTestDetail.conterList.errorRate') }}
      </div>
    </div>
    
    <!-- Collapsible panels for error items -->
    <Collapse class="!bg-transparent">
      <!-- Custom expand/collapse icon -->
      <template #expandIcon="record">
        <Icon
          :icon="record.isActive ? 'icon-xiangxia' : 'icon-xiangshang'"
          class="text-3 text-text-sub-content"
          :class="props.list?.[record.panelKey]?.list?.length 
            ? 'text-text-sub-content' 
            : 'text-text-disabled cursor-not-allowed'" />
      </template>
      
      <!-- Render collapse panel for each error item -->
      <CollapsePanel
        v-for="(item, index) in props.list"
        :key="index"
        :style="{ 'background-color': item.name === 'Total' ? '#FAFCFC' : '' }"
        :collapsible="item.list?.length ? undefined : 'disabled'">
        
        <!-- Panel header showing main item statistics -->
        <template #header>
          <div class="flex text-3 text-text-content leading-5.5 w-full">
            <!-- Item name (flexible width, truncated) -->
            <div class="py-0.5 px-2 flex-1 ml-2.5 truncate" :title="item.name">
              {{ item.name }}
            </div>
            
            <!-- Error count (31% width to align with header) -->
            <div style="width:31%" class="py-0.5 px-2">
              {{ item.errorNum }}
            </div>
            
            <!-- Error rate (30% width) -->
            <div style="width:30%" class="py-0.5 px-2">
              {{ item.errorRate }}
            </div>
          </div>
        </template>
        
        <!-- Panel content with child items and pie chart -->
        <div class="flex w-full relative items-center" style="min-height: 140px">
          <!-- Child items list -->
          <div class="w-full">
            <div
              v-for="(child, childIndex) in item.list"
              :key="childIndex"
              class="flex w-full h-7 leading-7 text-3 text-text-content">
              <!-- Spacer for indentation -->
              <div class="w-12"></div>
              
              <!-- Child item name (flexible width with word break) -->
              <div class="py-0.5 px-2 flex-1 break-words leading-4">
                {{ child.name }}
              </div>
              
              <!-- Child error count (60% width) -->
              <div style="width:60%" class="py-0.5 px-2">
                {{ child.errorNum }}
              </div>
            </div>
          </div>
          
          <!-- Pie chart visualization (absolutely positioned) -->
          <div v-if="item.list" class="pie-chart-class">
            <PirChart 
              :dataSource="item.list.map(m => ({ 
                name: m.name, 
                value: m.errorNum || 0 
              }))" />
          </div>
        </div>
      </CollapsePanel>
    </Collapse>
  </div>
</template>
<style scoped>
:deep(.ant-collapse) {
  border: 0;
}

:deep(.ant-collapse > .ant-collapse-item > .ant-collapse-header) {
  padding: 8px 14px;
}


:deep(.ant-collapse-content > .ant-collapse-content-box) {
  padding: 0;
}


:deep(.ant-collapse > .ant-collapse-item > .ant-collapse-header .ant-collapse-arrow svg) {
  margin-top: -5px;
}


.pie-chart-class {
  position: absolute;
  top: 50%;
  left: 74%;
  transform: translate(-50%, -50%);
}
</style>
