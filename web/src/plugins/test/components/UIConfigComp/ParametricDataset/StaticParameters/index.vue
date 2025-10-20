/**
 * StaticParameters Component
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
  dataSource: {        // Array of parameter name-value pairs
    name: string;
    value: string;
  }[];
}

// Define props with default values
const props = withDefaults(defineProps<Props>(), {
  dataSource: () => []
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
</script>

<template>
  <!-- Main container -->
  <div>
    <!-- Help text hint at the top -->
    <Hints 
      class="mb-1.5" 
      :text="t('httpPlugin.uiConfig.httpConfigs.parametric.dataset.staticParametersDescription')" />

    <!-- Table header row -->
    <div class="flex items-center space-x-2 mb-1">
      <!-- Parameter name column header -->
      <div class="w-90 flex items-center">
        <span>{{ t('httpPlugin.uiConfig.httpConfigs.parametric.dataset.name') }}</span>
      </div>
      
      <!-- Parameter value column header -->
      <div class="flex-1 flex items-center">
        <span>{{ t('common.value') }}</span>
      </div>
    </div>

    <!-- Parameter list -->
    <div class="space-y-2.5">
      <!-- Individual parameter row -->
      <div
        v-for="item in dataList"
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
          
          <!-- Parameter value field (read-only) -->
          <Input
            :value="item.value"
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
