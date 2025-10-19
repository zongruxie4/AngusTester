<script setup lang="ts">
import { ref } from 'vue';
import { Collapse, CollapsePanel } from 'ant-design-vue';
import { Icon, Colon } from '@xcan-angus/vue-ui';
import { useI18n } from 'vue-i18n';

/**
 * Sample item interface
 */
interface SampleItem {
  timestamp: string;  // Sample timestamp
  key: string;        // Sample unique identifier
  name: string;       // Sample name/description
  content?: string;   // Sample detailed content (optional, may be empty)
}

/**
 * Component props interface
 */
interface Props {
  sampleList: SampleItem[];  // Array of sampling records
}

// Define props with default values
const props = withDefaults(defineProps<Props>(), {
  sampleList: () => []
});

// Initialize i18n for internationalization
const { t } = useI18n();

/**
 * Active collapse panel keys
 * Controls which panels are expanded
 */
const activeKey = ref<(string | number)[]>([]);
</script>

<template>
  <!-- Main container for sampling data -->
  <div class="text-3 text-text-content">
    <!-- Header row with title -->
    <div class="h-9.5 leading-9.5 bg-theme-form-head flex font-medium">
      <!-- Spacer for expand icon alignment -->
      <div class="w-12"></div>
      
      <!-- Title label -->
      <div class="py-0.5 px-2 flex-1">
        {{ t('ftpPlugin.performanceTestDetail.sampling.title') }}
      </div>
    </div>
    
    <!-- Collapsible panels for sampling data -->
    <Collapse 
      v-model:activeKey="activeKey" 
      class="!bg-transparent">
      <!-- Custom expand/collapse icon -->
      <template #expandIcon="item">
        <Icon
          :icon="item.isActive ? 'icon-xiangxia' : 'icon-xiangshang'"
          :class="props.sampleList[item.panelKey].content 
            ? 'text-text-sub-content' 
            : 'text-text-disabled cursor-not-allowed'"
          class="text-3" />
      </template>
      
      <!-- Render collapse panel for each sample -->
      <CollapsePanel
        v-for="(item, index) in props.sampleList"
        :key="index"
        :collapsible="item.content ? undefined : 'disabled'">
        <!-- Panel header showing sample metadata -->
        <template #header>
          <div class="flex w-full text-3 text-text-content leading-5.5 space-x-2 pl-5">
            <!-- Sample timestamp -->
            <div class="flex-none w-45">
              <span class="mr-2">
                {{ t('ftpPlugin.performanceTestDetail.sampling.time') }}
                <Colon />
              </span>
              {{ item.timestamp }}
            </div>
            
            <!-- Sample ID -->
            <div class="flex-none w-45">
              <span class="mr-2">
                {{ t('ftpPlugin.performanceTestDetail.sampling.samplingId') }}
                <Colon />
              </span>
              {{ item.key }}
            </div>
            
            <!-- Sample name (truncated with tooltip) -->
            <div class="flex-1 truncate" :title="item.name">
              <span class="mr-2">
                {{ t('ftpPlugin.performanceTestDetail.sampling.name') }}
                <Colon />
              </span>
              {{ item.name }}
            </div>
          </div>
        </template>
        
        <!-- Panel content showing sample details -->
        <div>
          {{ item.content }}
        </div>
      </CollapsePanel>
    </Collapse>
  </div>
</template>

<style scoped>
/**
 * Remove default collapse border
 */
:deep(.ant-collapse) {
  border: 0;
}

/**
 * Customize collapse panel header padding
 */
:deep(.ant-collapse > .ant-collapse-item > .ant-collapse-header) {
  padding: 8px 14px;
}

/**
 * Customize collapse content box padding
 * Extra left padding to align with header content
 */
:deep(.ant-collapse-content > .ant-collapse-content-box) {
  padding: 8px 8px 8px 36px;
}

/**
 * Adjust collapse arrow vertical alignment
 * Shifts arrow up slightly for better visual alignment
 */
:deep(.ant-collapse > .ant-collapse-item > .ant-collapse-header .ant-collapse-arrow svg) {
  margin-top: -5px;
}
</style>
