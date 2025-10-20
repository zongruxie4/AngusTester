<script lang="ts" setup>
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';
import { Input, IconCopy, Hints } from '@xcan-angus/vue-ui';
import { utils } from '@xcan-angus/infra';

// Initialize i18n for internationalization
const { t } = useI18n();

/**
 * Parameter option interface
 */
export interface Option {
  name: string;   // Parameter name
  value: string;  // Parameter value
}

/**
 * Component props interface
 */
export interface Props {
  columnIndex: number;  // Starting column index for parameter extraction
  dataSource: {         // Array of parameter name-value pairs
    name: string;
    value: string;
  }[];
  hintText: string;     // Help text to display at the top
}

// Define props with default values
const props = withDefaults(defineProps<Props>(), {
  columnIndex: 0,
  dataSource: () => [],
  hintText: undefined
});

/**
 * Computed data list with unique IDs
 * Adds UUID to each item for Vue's key binding
 * 
 * @returns Array of parameters with unique IDs
 */
const dataList = computed(() => {
  return props.dataSource?.map(item => {
    return {
      ...item,
      id: utils.uuid()
    };
  }) || [];
});

/**
 * Computed column index as number
 * Ensures columnIndex is always a valid number (defaults to 0)
 * 
 * @returns Column index as number
 */
const _columnIndex = computed(() => {
  return +(props.columnIndex || 0);
});
</script>

<template>
  <!-- Main container -->
  <div>
    <!-- Help text hint at the top -->
    <Hints class="mb-1.5" :text="props.hintText" />

    <!-- Table header row -->
    <div class="flex items-center space-x-2 mb-1">
      <!-- Parameter name column header -->
      <div class="w-90 flex items-center">
        <span>{{ t('httpPlugin.uiConfig.httpConfigs.parametric.dataset.extractParametersField.name') }}</span>
      </div>
      
      <!-- Read column index header -->
      <div class="flex-1 flex items-center">
        <span>{{ t('httpPlugin.uiConfig.httpConfigs.parametric.dataset.extractParametersField.readColumn') }}</span>
      </div>
    </div>

    <!-- Parameter list -->
    <div class="space-y-2.5">
      <!-- Individual parameter row -->
      <div
        v-for="(item, index) in dataList"
        :key="item.id"
        class="flex items-center space-x-2">
        <div class="flex items-center flex-1 space-x-2">
          <!-- Parameter name field (read-only with copy button) -->
          <div class="w-90 flex items-center">
            <Input
              :value="item.name"
              readonly
              size="small"
              class="flex-1 has-suffix">
              <!-- Custom suffix: variable syntax with copy button -->
              <template #suffix>
                <div v-if="item.name" class="h-full flex items-center overflow-hidden">
                  <!-- Variable syntax display: {parameterName} -->
                  <div 
                    :title="`{${item.name}}`" 
                    class="flex-1 flex items-center text-3 overflow-hidden">
                    <span>{</span>
                    <span class="truncate">{{ item.name }}</span>
                    <span>}</span>
                  </div>
                  
                  <!-- Copy button for variable syntax -->
                  <IconCopy 
                    :copyText="`{${item.name}}`" 
                    class="flex-shrink-0 ml-1.75" />
                </div>
              </template>
            </Input>
          </div>
          
          <!-- Column index field (read-only, auto-calculated) -->
          <Input
            :value="String(_columnIndex + index + 1)"
            class="flex-1"
            readonly />
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.has-suffix :deep(.ant-input-suffix) {
  display: inline-block;
  width: 120px;
  margin-left: 4px;
  padding-left: 7px;
  overflow: hidden;
  border-left: 1px solid var(--border-text-box);
}

.ant-input-affix-wrapper:focus,
.ant-input-affix-wrapper-focused,
.ant-input-affix-wrapper:hover {
  border: 1px solid var(--border-text-box);
}
</style>
